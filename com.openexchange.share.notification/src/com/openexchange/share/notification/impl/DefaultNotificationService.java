/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.share.notification.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.GuestShare;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.notification.RequestContext;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.ShareNotifyExceptionCodes;
import com.openexchange.share.notification.impl.mail.MailNotifications;
import com.openexchange.share.notification.impl.mail.MailNotifications.ShareCreatedBuilder;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.InternalRecipient;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.user.UserService;

/**
 * {@link DefaultNotificationService} - The default share notification service.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.0
 */
public class DefaultNotificationService implements ShareNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultNotificationService.class);

    private final ServiceLookup serviceLookup;

    /** The queue for additional handlers */
    private final ConcurrentMap<Transport, ShareNotificationHandler> handlers;

    /**
     * Initializes a new {@link DefaultNotificationService}.
     */
    public DefaultNotificationService(ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
        handlers = new ConcurrentHashMap<Transport, ShareNotificationHandler>();
    }

    /**
     * Adds specified handler.
     *
     * @param handler The handler to add
     */
    public void add(ShareNotificationHandler handler) {
        handlers.putIfAbsent(handler.getTransport(), handler);
    }

    /**
     * Removes given handler
     *
     * @param handler The handler to remove
     */
    public void remove(ShareNotificationHandler handler) {
        handlers.remove(handler.getTransport(), handler);
    }

    @Override
    public List<OXException> sendShareCreatedNotifications(Transport transport, Map<ShareRecipient, List<ShareInfo>> createdShares, String message, Session session, RequestContext requestContext) {
        List<OXException> warnings = new ArrayList<OXException>();

        /*
         * To send the notifications we have to build NotificationInfo instances per recipient and share that contain the needed data to
         * build the actual notification instance.
         */
        List<NotificationInfo> notificationInfos = new ArrayList<NotificationInfo>(createdShares.size());
        boolean notifyInternalUsers = serviceLookup.getService(ConfigurationService.class).getBoolProperty("com.openexchange.share.notifyInternal", true);
        for (Entry<ShareRecipient, List<ShareInfo>> entry : createdShares.entrySet()) {
            ShareRecipient recipient = entry.getKey();
            List<ShareInfo> shareInfos = entry.getValue();
            if (shareInfos != null && !shareInfos.isEmpty()) {
                if (!InternalRecipient.class.isInstance(recipient) || notifyInternalUsers) {
                    notificationInfos.add(new NotificationInfo(recipient, shareInfos.get(0).getGuest(), shareInfos, transport, message, session, requestContext));
                }
            }
        }

        for (NotificationInfo notificationInfo : notificationInfos) {
            try {
                ShareNotification<?> shareNotification = buildShareCreatedNotification(notificationInfo);
                send(shareNotification);
            } catch (OXException e) {
                if (e.isPrefix(ShareNotifyExceptionCodes.PREFIX)) {
                    warnings.add(e);
                } else {
                    String emailAddress = notificationInfo.getGuestInfo().getEmailAddress();
                    LOG.error("Error while sending notification mail to {}", emailAddress, e);
                    warnings.add(ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage(), emailAddress));
                }
            } catch (Exception e) {
                String emailAddress = notificationInfo.getGuestInfo().getEmailAddress();
                LOG.error("Error while sending notification mail to {}", emailAddress, e);
                warnings.add(ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage(), emailAddress));
            }
        }

        return warnings;
    }

    @Override
    public void sendPasswordResetConfirmationNotification(Transport transport, GuestShare guestShare, String shareToken, String confirmToken, RequestContext requestContext) throws OXException {
        try {
            UserService userService = serviceLookup.getService(UserService.class);
            GuestInfo guestInfo = guestShare.getGuest();
            String mailAddress = guestInfo.getEmailAddress();
            User guest = userService.getUser(guestInfo.getGuestID(), guestInfo.getContextID());
            LinkProvider linkProvider = buildLinkProvider(guestInfo.getGuestID(), guestInfo.getContextID(), requestContext.getHostname(), shareToken, requestContext.getProtocol());

            ShareNotification<InternetAddress> notification = MailNotifications.passwordConfirm()
                .setTransportInfo(new InternetAddress(mailAddress, true))
                .setLinkProvider(linkProvider)
                .setGuestContext(guestInfo.getContextID())
                .setGuestID(guestInfo.getGuestID())
                .setLocale(guest.getLocale())
                .setShareToken(shareToken)
                .setConfirmToken(confirmToken)
                .setAccountName(mailAddress)
                .setRequestContext(requestContext) // TODO combine with link provider
                .build();

            send(notification);
        } catch (Exception e) {
            throw ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private <T extends ShareNotification<?>> void send(T notification) throws OXException {
        ShareNotificationHandler handler = handlers.get(notification.getTransport());
        if (handler == null) {
            throw new OXException(new IllegalArgumentException("No provider exists to handle notifications for transport " + notification.getTransport().toString()));
        }

        handler.send(notification);
    }

    private ShareNotification<?> buildShareCreatedNotification(NotificationInfo notificationInfo) throws OXException {
        ShareNotification<?> shareNotification = null;
        switch (notificationInfo.getTransport()) {
            case MAIL:
                shareNotification = buildShareCreatedMailNotification(notificationInfo);
                break;
            default:
                ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create("Unknown transport: " + notificationInfo.getTransport(), notificationInfo.getGuestInfo().getEmailAddress());
                break;
        }
        return shareNotification;
    }

    /**
     * Builds a {@link ShareNotification} ready to be sent out to a recipent via mail.
     *
     * @param notificationInfo The needed infos to build the {@link ShareNotification}
     * @return the built ShareNotification
     * @throws OXException if the email address needed to build the notification is missing
     */
    private ShareNotification<?> buildShareCreatedMailNotification(NotificationInfo notificationInfo) throws OXException {
        if (RecipientType.USER.equals(notificationInfo.getGuestInfo().getRecipientType())) {
            return buildInternalShareCreatedMailNotification(notificationInfo);
        }
        if (Strings.isEmpty(notificationInfo.getGuestInfo().getEmailAddress())) {
            GuestInfo guestInfo = notificationInfo.getGuestInfo();
            String guestName = guestInfo.getDisplayName();
            if (Strings.isEmpty(guestName)) {
                guestName = NotificationStrings.UNKNOWN_USER_NAME;
            }

            throw ShareNotifyExceptionCodes.MISSING_MAIL_ADDRESS.create(guestName, guestInfo.getGuestID(), guestInfo.getContextID());
        }

        Session session = notificationInfo.getSession();
        List<ShareInfo> createdShares = notificationInfo.getShareInfos();
        GuestInfo guest = notificationInfo.getGuestInfo();
        ShareRecipient recipient = notificationInfo.getRecipient();
        String shareToken = createdShares.size() == 1 ? createdShares.get(0).getToken() : guest.getBaseToken();

        try {
            RequestContext requestContext = notificationInfo.getRequestContext();
            ShareCreatedBuilder shareCreatedBuilder = MailNotifications.shareCreated()
                .setTransportInfo(new InternetAddress(guest.getEmailAddress(), true))
                .setLinkProvider(buildLinkProvider(session.getUserId(), session.getContextId(), requestContext.getHostname(), shareToken, requestContext.getProtocol()))
                .setGuestContext(guest.getContextID())
                .setGuestID(guest.getGuestID())
                .setLocale(guest.getLocale())
                .setSession(session)
                .setTargets(getTargets(createdShares))
                .setMessage(notificationInfo.getMessage())
                .setIntitialShare(isNewGuest(recipient))
                .setRequestContext(notificationInfo.getRequestContext()); // TODO combine with link provider

            return shareCreatedBuilder.build();
        } catch (AddressException e) {
            throw ShareNotifyExceptionCodes.INVALID_MAIL_ADDRESS.create(guest.getEmailAddress());
        }
    }

    /**
     * Extracts all targets from the supplied shares.
     *
     * @param shareInfos The share infos
     * @return The extracted targets
     */
    private static List<ShareTarget> getTargets(List<ShareInfo> shareInfos) {
        if (null == shareInfos) {
            return null;
        }
        List<ShareTarget> targets = new ArrayList<ShareTarget>(shareInfos.size());
        for (ShareInfo share : shareInfos) {
            targets.add(share.getShare().getTarget());
        }
        return targets;
    }

    /**
     * Determines if a {@link ShareRecipient} is a newly created {link GuestRecipient}.
     *
     * @param recipient The {@link ShareRecipient}
     * @return true if the recipient is a {link GuestRecipient} that had to be created.
     */
    private boolean isNewGuest(ShareRecipient recipient) {
        if (RecipientType.GUEST.equals(recipient.getType())) {
            GuestRecipient guestRecipient = GuestRecipient.class.cast(recipient);
            return guestRecipient.wasCreated();
        }
        return false;
    }

    protected LinkProvider buildLinkProvider(int userID, int contextID, String requestHostname, String shareToken, String protocol) {
        return new DefaultLinkProvider(protocol, determineHostname(userID, contextID, requestHostname), determineServletPrefix(), shareToken);
    }

    protected LinkProvider buildLinkProvider(int userID, int contextID, String requestHostname, String shareToken, String protocol, String internalLink) {
        return new DefaultLinkProvider(protocol, determineHostname(userID, contextID, requestHostname), determineServletPrefix(), shareToken, internalLink);
    }

    protected static String determineProtocol(AJAXRequestData requestData) {
        HttpServletRequest servletRequest = requestData.optHttpServletRequest();
        if (null != servletRequest) {
            return com.openexchange.tools.servlet.http.Tools.getProtocol(servletRequest);
        }
        return requestData.isSecure() ? "https://" : "http://";
    }

    /**
     * Determines the hostname by first asking the optional {@link HostnameService} and then falling back to the hostname contained in the request.
     *
     * @param userID The userID to use when querying the {@link HostnameService}
     * @param contextID The userID to use when querying the {@link HostnameService}
     * @param requestHostname The hostname as provided by the request
     * @return the determined hostname
     */
    protected String determineHostname(int userID, int contextID, String requestHostname) {
        HostnameService hostNameService = serviceLookup.getOptionalService(HostnameService.class);
        if (hostNameService != null) {
            return hostNameService.getHostname(userID, contextID);
        }
        return requestHostname;
    }

    protected String determineServletPrefix() {
        DispatcherPrefixService prefixService = serviceLookup.getService(DispatcherPrefixService.class);
        if (prefixService == null) {
            return DispatcherPrefixService.DEFAULT_PREFIX;
        }
        return prefixService.getPrefix();
    }

    /**
     * Builds a {@link ShareNotification} ready to be sent out to a internal recipent via mail.
     *
     * @param notificationInfo The needed infos to build the {@link ShareNotification}
     * @return the built ShareNotification
     * @throws OXException if the email address needed to build the notification is missing
     */
    private ShareNotification<?> buildInternalShareCreatedMailNotification(NotificationInfo notificationInfo) throws OXException {
        Session session = notificationInfo.getSession();
        RequestContext requestContext = notificationInfo.getRequestContext();
        InternalRecipient recipient = (InternalRecipient) notificationInfo.getRecipient();
        List<ShareInfo> createdShares = notificationInfo.getShareInfos();
        int userId = session.getUserId();
        int contextId = session.getContextId();
        String protocol = requestContext.getProtocol();
        String uiWebPath = serviceLookup.getService(ConfigurationService.class).getProperty("com.openexchange.UIWebPath", "/appsuite");
        User internalUser = serviceLookup.getService(UserService.class).getUser(recipient.getEntity(), contextId);

        // TODO
        LinkProvider linkProvider;
        if (createdShares.size() == 1 && null != createdShares.get(0).getShare().getTarget()) {
            String module = Module.getForFolderConstant(createdShares.get(0).getShare().getTarget().getModule()).getName();
            String folder = createdShares.get(0).getShare().getTarget().getFolder();
            String item = createdShares.get(0).getShare().getTarget().getItem();
            StringBuilder sb = new StringBuilder(uiWebPath).append("/ui#!!").append("&app=io.ox/").append(module).append("&folder=").append(folder);
            if (null != item && !Strings.isEmpty(item)) {
                sb.append("&item=").append(item);
            }
            linkProvider = buildLinkProvider(userId, contextId, requestContext.getHostname(), null, protocol, sb.toString());
        } else {
            StringBuilder sb = new StringBuilder(uiWebPath).append("/ui#!!").append("&app=io.ox/").append("files").append("&folder=").append(10);
            linkProvider = buildLinkProvider(userId, contextId, requestContext.getHostname(), null, protocol, sb.toString());
        }

        try {
            ShareCreatedBuilder builder = MailNotifications.shareCreated()
                .setGuestContext(contextId)
                .setGuestID(internalUser.getId())
                .setLocale(internalUser.getLocale())
                .setSession(session)
                .setTargets(getTargets(createdShares))
                .setMessage(notificationInfo.getMessage())
                .setRequestContext(requestContext)
                .setIntitialShare(false)
                .setTransportInfo(new InternetAddress(internalUser.getMail(), true))
                .setLinkProvider(linkProvider);
            return builder.build();
        } catch (Exception e) {
            throw ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage(), notificationInfo.getGuestInfo().getEmailAddress());
        }
    }

}
