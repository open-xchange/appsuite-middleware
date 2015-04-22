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
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.notification.DefaultLinkProvider;
import com.openexchange.share.notification.LinkProvider;
import com.openexchange.share.notification.ShareCreationDetails;
import com.openexchange.share.notification.ShareNotification;
import com.openexchange.share.notification.ShareNotificationHandler;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.mail.MailNotifications;
import com.openexchange.share.notification.mail.MailNotifications.ShareCreatedBuilder;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.tools.session.ServerSession;


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
        String productName=null;
        try {
            productName = determineProductName(requestData, session);
        } catch (OXException e) {
            warnings.add(e);
            return warnings;
        }
        
        /*
         * To send the notifications we have to biuld NotificationInfo instances per recipient and share that contain the needed data to
         * build the actual notification instance.
         */
        List<NotificationInfo> notificationInfos = new ArrayList<NotificationInfo>(createdShares.size());
        for (Entry<ShareRecipient, List<ShareInfo>> entry : createdShares.entrySet()) {
            ShareRecipient recipient = entry.getKey();
            List<ShareInfo> shareInfos = entry.getValue();
            if (shareInfos != null && !shareInfos.isEmpty()) {
                notificationInfos.add(new NotificationInfo(recipient, shareInfos.get(0).getGuest(), shareInfos, productName, transport,
                    message, session, requestData));
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
        

        if (Strings.isEmpty(notificationInfo.getGuestInfo().getEmailAddress())) {
            GuestInfo guestInfo = notificationInfo.getGuestInfo();
            throw ShareNotifyExceptionCodes.MISSING_MAIL_ADDRESS.create(guestInfo.getGuestID(), guestInfo.getContextID());
        }

        ServerSession session = notificationInfo.getSession();
        AJAXRequestData requestData = notificationInfo.getRequestData();
        List<ShareInfo> createdShares = notificationInfo.getShareInfos();
        GuestInfo guest = notificationInfo.getGuestInfo();
        ShareRecipient recipient = notificationInfo.getRecipient();

        String shareToken = createdShares.size() == 1 ? createdShares.get(0).getToken() : guest.getBaseToken();
        try {
            ShareCreatedBuilder builder = MailNotifications.shareCreated()
                .setTransportInfo(new InternetAddress(guest.getEmailAddress(), true))
                .setLinkProvider(buildLinkProvider(session, requestData, shareToken))
                .setGuestContext(guest.getContextID())
                .setGuestID(guest.getGuestID())
                .setLocale(guest.getLocale())
                .setSession(session)
                .setTargets(getTargets(createdShares))
                .setMessage(notificationInfo.getMessage())
                .setCreationDetails(new ShareCreationDetails(notificationInfo.getProductName(), isNewGuest(recipient)));

            AuthenticationMode authMode = guest.getAuthentication();
            switch (authMode) {
                case ANONYMOUS:
                    builder.setAuthMode(AuthenticationMode.ANONYMOUS);
                    break;
                case ANONYMOUS_PASSWORD:
                    builder.setAuthMode(AuthenticationMode.ANONYMOUS_PASSWORD);
                    builder.setPassword(((AnonymousRecipient) recipient).getPassword());
                    break;
                case GUEST_PASSWORD:
                    builder.setAuthMode(AuthenticationMode.GUEST_PASSWORD);
                    builder.setUsername(guest.getEmailAddress());
                    builder.setPassword(((GuestRecipient) recipient).getPassword());
                    break;
            }
            return builder.build();
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
    
    /**
     * Get the product name that matches the current session and requestData.
     * 
     * @param requestData
     * @param session
     * @return The product name that matches the current session and requestData.
     * @throws OXException If no product name can be found
     */
    protected String determineProductName(AJAXRequestData requestData, ServerSession session) throws OXException {
        String productName = null;
        
        JSONObject serverConfig = serviceLookup.getService(ServerConfigService.class).getServerConfig(requestData, session);
        try {
            productName = serverConfig.getString("productName");
            if(Strings.isEmpty(productName)) {
                throw ShareNotifyExceptionCodes.INVALID_PRODUCT_NAME.create(session.getUserId(), session.getContextId());
            }
        } catch (JSONException e) {
            throw ShareNotifyExceptionCodes.INVALID_PRODUCT_NAME.create(session.getUserId(), session.getContextId());
        }
        
        return productName;
    }

    protected LinkProvider buildLinkProvider(ServerSession session, AJAXRequestData requestData, String shareToken) {
        return new DefaultLinkProvider(determineProtocol(requestData), determineHostname(session, requestData), determineServletPrefix(), shareToken);
    }

    protected static String determineProtocol(AJAXRequestData requestData) {
        HttpServletRequest servletRequest = requestData.optHttpServletRequest();
        if (null != servletRequest) {
            return com.openexchange.tools.servlet.http.Tools.getProtocol(servletRequest);
        }
        return requestData.isSecure() ? "https://" : "http://";
    }

    protected String determineHostname(ServerSession session, AJAXRequestData requestData) {
        HostnameService hostNameService = serviceLookup.getOptionalService(HostnameService.class);
        if(hostNameService != null) {
            return hostNameService.getHostname(session.getUserId(), session.getContextId());
        }
        HttpServletRequest servletRequest = requestData.optHttpServletRequest();
        if (null != servletRequest) {
            return servletRequest.getServerName();
        }
        return requestData.getHostname();
    }
    
    protected String determineServletPrefix() {
        DispatcherPrefixService prefixService = serviceLookup.getService(DispatcherPrefixService.class);
        if (prefixService == null) {
            return DispatcherPrefixService.DEFAULT_PREFIX;
        }
        return prefixService.getPrefix();
    }

}
