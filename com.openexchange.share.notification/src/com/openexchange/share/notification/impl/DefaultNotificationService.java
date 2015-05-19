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
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.serverconfig.ShareMailConfig;
import com.openexchange.session.Session;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.GuestShare;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.notification.DefaultLinkProvider;
import com.openexchange.share.notification.LinkProvider;
import com.openexchange.share.notification.ShareNotification;
import com.openexchange.share.notification.ShareNotification.NotificationType;
import com.openexchange.share.notification.ShareNotificationHandler;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.mail.MailNotifications;
import com.openexchange.share.notification.mail.ShareMailAware;
import com.openexchange.share.notification.mail.impl.PasswordResetConfirmMailNotification;
import com.openexchange.share.notification.mail.impl.ShareCreatedMailNotification;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.InternalRecipient;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.tools.session.ServerSession;
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

    private ServiceLookup serviceLookup;

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
    public <T extends ShareNotification<?>> void send(T notification) throws OXException {
        ShareNotificationHandler handler = handlers.get(notification.getTransport());
        if (handler == null) {
            throw new OXException(new IllegalArgumentException("No provider exists to handle notifications for transport " + notification.getTransport().toString()));
        }

        handler.send(notification);
    }

    @Override
    // match enum type from com.openexchange.share.notification.ShareNotification.NotificationType<T>
    public List<OXException> sendShareCreatedNotifications(Transport transport, Map<ShareRecipient, List<ShareInfo>> createdShares, String message, ServerSession session, AJAXRequestData requestData) {
        List<OXException> warnings = new ArrayList<OXException>();

        /*
         * To send the notifications we have to biuld NotificationInfo instances per recipient and share that contain the needed data to
         * build the actual notification instance.
         */
        List<NotificationInfo> notificationInfos = new ArrayList<NotificationInfo>(createdShares.size());
        for (Entry<ShareRecipient, List<ShareInfo>> entry : createdShares.entrySet()) {
            ShareRecipient recipient = entry.getKey();
            List<ShareInfo> shareInfos = entry.getValue();
            if (shareInfos != null && !shareInfos.isEmpty()) {
                if (!InternalRecipient.class.isInstance(recipient) || serviceLookup.getService(ConfigurationService.class).getBoolProperty("com.openexchange.share.notifyInternal", false)) {
                    notificationInfos.add(new NotificationInfo(recipient, shareInfos.get(0).getGuest(), shareInfos, transport, message, session, requestData));
                }
            }
        }

        for (NotificationInfo notificationInfo : notificationInfos) {
            try {
                ShareNotification<?> shareNotification = buildShareCreatedNotification(notificationInfo);
                send(shareNotification);
            } catch (OXException oxe) {
                warnings.add(oxe);
            } catch (Exception e) {
                warnings.add(ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage()));
            }
        }

        return warnings;
    }

    private ShareNotification<?> buildShareCreatedNotification(NotificationInfo notificationInfo) throws OXException {
        ShareNotification<?> shareNotification = null;
        switch (notificationInfo.getTransport()) {
            case MAIL:
                shareNotification = buildShareCreatedMailNotification(notificationInfo);
                break;
            default:
                ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create("Unknown transport: " + notificationInfo.getTransport());
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
            throw ShareNotifyExceptionCodes.MISSING_MAIL_ADDRESS.create(guestInfo.getGuestID(), guestInfo.getContextID());
        }

        ServerSession session = notificationInfo.getSession();
        int userId = session.getUserId();
        int contextId = session.getContextId();
        AJAXRequestData requestData = notificationInfo.getRequestData();
        List<ShareInfo> createdShares = notificationInfo.getShareInfos();
        GuestInfo guest = notificationInfo.getGuestInfo();
        ShareRecipient recipient = notificationInfo.getRecipient();
        String shareToken = createdShares.size() == 1 ? createdShares.get(0).getToken() : guest.getBaseToken();
        String protocol = determineProtocol(requestData);

        try {
            ShareCreatedMailNotification scmn = new ShareCreatedMailNotification();

            scmn.setTransportInfo(new InternetAddress(guest.getEmailAddress(), true));
            scmn.setLinkProvider(buildLinkProvider(session.getUserId(), session.getContextId(), requestData.getHostname(), shareToken, protocol));
            scmn.setGuestContextID(guest.getContextID());
            scmn.setGuestID(guest.getGuestID());
            scmn.setLocale(guest.getLocale());
            scmn.setSession(session);
            scmn.setTargets(getTargets(createdShares));
            scmn.setMessage(notificationInfo.getMessage());
            scmn.setCausedGuestCreation(isNewGuest(recipient));

            AuthenticationMode authMode = guest.getAuthentication();
            switch (authMode) {
                case ANONYMOUS:

                    scmn.setAuthMode(AuthenticationMode.ANONYMOUS);
                    break;
                case ANONYMOUS_PASSWORD:
                    scmn.setAuthMode(AuthenticationMode.ANONYMOUS_PASSWORD);
                    scmn.setPassword(((AnonymousRecipient) recipient).getPassword());
                    break;
                case GUEST_PASSWORD:
                    scmn.setAuthMode(AuthenticationMode.GUEST_PASSWORD);
                    scmn.setUsername(guest.getEmailAddress());
                    scmn.setPassword(((GuestRecipient) recipient).getPassword());
                    break;
            }

            makeMailAware(scmn, requestData.getHostname(), userId, contextId);

            return scmn;
        } catch (AddressException e) {
            throw ShareNotifyExceptionCodes.INVALID_MAIL_ADDRESS.create(guest.getEmailAddress());
        }
    }

    @Override
    public void sendPasswordResetConfirmationNotification(Transport transport, GuestShare guestShare, String shareToken, String requestHostname, String protocol, String hash) throws OXException {
        try {
            UserService userService = serviceLookup.getService(UserService.class);
            GuestInfo guestInfo = guestShare.getGuest();
            String mailAddress = guestInfo.getEmailAddress();
            User guest = userService.getUser(guestInfo.getGuestID(), guestInfo.getContextID());
            LinkProvider linkProvider = buildLinkProvider(guestInfo.getGuestID(), guestInfo.getContextID(), requestHostname, shareToken, protocol);

            ShareNotification<InternetAddress> notification = MailNotifications.passwordConfirm()
                .setTransportInfo(new InternetAddress(mailAddress, true))
                .setLinkProvider(linkProvider)
                .setGuestContext(guestInfo.getContextID())
                .setGuestID(guestInfo.getGuestID())
                .setLocale(guest.getLocale())
                .setShareToken(shareToken)
                .setConfirm(hash)
                .setAccount(mailAddress)
                .build();

            PasswordResetConfirmMailNotification n = (PasswordResetConfirmMailNotification) notification;

            makeMailAware(n, requestHostname, guestInfo.getGuestID(), guestInfo.getContextID());
            send(n);
        } catch (Exception e) {
            throw ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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

    /**
     * Get the product name that matches the current session and requestData.
     *
     * @param userID
     * @param contextID
     * @param hostname The hostname to use when looking up the product name
     * @return The product name that matches the current session and requestData.
     * @throws OXException If no product name can be found
     */
    protected String determineProductName(int userID, int contextID, String hostname) throws OXException {
        String productName = null;
        String determinedHostname = determineHostname(userID, contextID, hostname);
        ServerConfig serverConfig = serviceLookup.getService(ServerConfigService.class).getServerConfig(determinedHostname, userID, contextID);
        productName = serverConfig.getProductName();
        if (Strings.isEmpty(productName)) {
            throw ShareNotifyExceptionCodes.INVALID_PRODUCT_NAME.create(userID, contextID);
        }

        return productName;
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
     * Get all mail relevant branding infos from the {@linkServerConfig} and add all the needed infos for the mailcomposer to actually build the mail.
     *
     * @param shareMailAware The {@link ShareMailAware} {@link ShareNotification} to enrich with information.
     * @param hostname The hostname
     * @param userId The userId
     * @param contextId The contextId
     */
    private void makeMailAware(ShareMailAware shareMailAware, String hostname, int userId, int contextId) throws OXException {
        ServerConfig serverConfig = serviceLookup.getService(ServerConfigService.class).getServerConfig(hostname, userId, contextId);
        String productName = serverConfig.getProductName();
        if (Strings.isEmpty(productName)) {
            throw ShareNotifyExceptionCodes.INVALID_PRODUCT_NAME.create(userId, contextId);
        }
        shareMailAware.setProductName(productName);

        ShareMailConfig shareMailConfig = serverConfig.getShareMailConfig();
        if (shareMailConfig == null) {
            throw ShareNotifyExceptionCodes.INVALID_SHARE_MAIL_CONFIG.create(userId, contextId);
        }
        shareMailAware.setButtonBackgroundColor(shareMailConfig.getButtonBackgroundColor());
        shareMailAware.setButtonBorderColor(shareMailConfig.getButtonBorderColor());
        shareMailAware.setButtonColor(shareMailConfig.getButtonColor());
        shareMailAware.setFooterImage(shareMailConfig.getFooterImage());
        shareMailAware.setFooterText(shareMailConfig.getFooterText());
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
        AJAXRequestData requestData = notificationInfo.getRequestData();
        InternalRecipient recipient = (InternalRecipient) notificationInfo.getRecipient();
        List<ShareInfo> createdShares = notificationInfo.getShareInfos();
        int userId = session.getUserId();
        int contextId = session.getContextId();
        String protocol = determineProtocol(requestData);
        String uiWebPath = serviceLookup.getService(ConfigurationService.class).getProperty("com.openexchange.UIWebPath", "/appsuite");
        User internalUser = serviceLookup.getService(UserService.class).getUser(recipient.getEntity(), contextId);
        ShareCreatedMailNotification scmn = new ShareCreatedMailNotification(NotificationType.INTERNAL_SHARE_CREATED);
        try {
            scmn.setTransportInfo(new InternetAddress(internalUser.getMail(), true));
            if (createdShares.size() == 1 && null != createdShares.get(0).getShare().getTarget()) {
                String module = Module.getForFolderConstant(createdShares.get(0).getShare().getTarget().getModule()).getName();
                String folder = createdShares.get(0).getShare().getTarget().getFolder();
                String item = createdShares.get(0).getShare().getTarget().getItem();
                StringBuilder sb = new StringBuilder(uiWebPath).append("/ui#!!").append("&app=io.ox/").append(module).append("&folder=").append(folder);
                if (null != item && !Strings.isEmpty(item)) {
                    sb.append("&item=").append(item);
                }
                scmn.setLinkProvider(buildLinkProvider(userId, contextId, requestData.getHostname(), null, protocol, sb.toString()));
            } else {
                StringBuilder sb = new StringBuilder(uiWebPath).append("/ui#!!").append("&app=io.ox/").append("files").append("&folder=").append(10);
                scmn.setLinkProvider(buildLinkProvider(userId, contextId, requestData.getHostname(), null, protocol, sb.toString()));
            }
            scmn.setContextID(contextId);
            scmn.setGuestContextID(contextId);
            scmn.setGuestID(internalUser.getId());
            scmn.setLocale(internalUser.getLocale());
            scmn.setSession(session);
            scmn.setTargets(getTargets(createdShares));
            scmn.setMessage(notificationInfo.getMessage());
            scmn.setCausedGuestCreation(false);
        } catch (Exception e) {
            throw ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        makeMailAware(scmn, requestData.getHostname(), userId, contextId);
        return scmn;
    }

}
