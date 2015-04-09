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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.share.core.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.notification.DefaultLinkProvider;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.mail.MailNotifications;
import com.openexchange.share.notification.mail.MailNotifications.ShareCreatedBuilder;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link NotificationSender}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class NotificationSender {
    
    private ServiceLookup services;
    private String protocol;
    private String domain;
    private String servletPrefix;

    public NotificationSender(ServiceLookup services, String protocol, String domain, String servletPrefix) {
        this.services = services;
        this.protocol = protocol;
        this.domain = domain;
        this.servletPrefix = servletPrefix;
    }

    /**
     * Sends notifications about one or more created shares to multiple guest user recipients.
     *
     * @param notificationInfos The notification infos; each one needs to have valid credentials according to its authentication mode
     * @param message The (optional) additional message for the notification
     * @param requestData Data of the underlying servlet request
     * @param session The session of the notifying user
     * @return Any exceptions occurred during notification, or an empty list if all was fine
     */
    public List<OXException> sendNotifications(Map<ShareRecipient, List<ShareInfo>> createdShares, String message, ServerSession session) {
        List<NotificationInfo> notificationInfos = getNotificationInfos(createdShares);
        List<OXException> warnings = new ArrayList<OXException>();
        for (NotificationInfo notificationInfo : notificationInfos) {
            if (!Strings.isEmpty(notificationInfo.getGuestInfo().getEmailAddress())) {
                try {
                    sendNotification(notificationInfo, message, session);
                } catch (OXException e) {
                    warnings.add(e);
                }
            }
        }
        return warnings;
    }

    /**
     * Sends a notification about one or more created shares to a guest user recipient.
     *
     * @param notificationInfo The notification infos; needs to have valid credentials according to its authentication mode
     * @param message The (optional) additional message for the notification
     * @param requestData Data of the underlying servlet request
     * @param session The session of the notifying user
     */
    private void sendNotification(NotificationInfo notificationInfo, String message, ServerSession session) throws OXException {
        List<ShareInfo> createdShares = notificationInfo.getShareInfos();
        GuestInfo guest = notificationInfo.getGuestInfo();
        ShareRecipient recipient = notificationInfo.getRecipient();
        String shareToken = createdShares.size() == 1 ? createdShares.get(0).getToken() : guest.getBaseToken();
        try {
            ShareCreatedBuilder builder = MailNotifications.shareCreated()
                .setTransportInfo(new InternetAddress(guest.getEmailAddress(), true))
                .setLinkProvider(new DefaultLinkProvider(protocol, domain, servletPrefix, shareToken))
                .setContext(guest.getContextID())
                .setLocale(guest.getLocale())
                .setSession(session)
                .setTargets(getTargets(createdShares))
                .setMessage(message);

            AuthenticationMode authMode = guest.getAuthentication();
            switch (authMode) {
                case ANONYMOUS:
                    builder.setAuthMode(AuthenticationMode.ANONYMOUS);
                    break;
                case ANONYMOUS_PASSWORD:
                    builder.setAuthMode(AuthenticationMode.ANONYMOUS_PASSWORD);
                    builder.setPassword(((AnonymousRecipient)recipient).getPassword());
                    break;
                case GUEST_PASSWORD:
                    builder.setAuthMode(AuthenticationMode.GUEST_PASSWORD);
                    builder.setUsername(guest.getEmailAddress());
                    builder.setPassword(((GuestRecipient)recipient).getPassword());
                    break;
            }

            services.getService(ShareNotificationService.class).send(builder.build());
        } catch (AddressException e) {
            throw ShareExceptionCodes.INVALID_MAIL_ADDRESS.create(guest.getEmailAddress());
        } catch (Exception e) {
            if (e instanceof OXException) {
                throw (OXException) e;
            } else {
                throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
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
     * Builds {@link NotificationInfo} instances for the passed map entries
     * @param createdShares The created shares as mappings from recipients to share infos
     * @return The notification infos
     */
    private List<NotificationInfo> getNotificationInfos(Map<ShareRecipient, List<ShareInfo>> createdShares) {
        List<NotificationInfo> notificationInfos = new ArrayList<NotificationInfo>(createdShares.size());
        for (Entry<ShareRecipient, List<ShareInfo>> entry : createdShares.entrySet()) {
            ShareRecipient recipient = entry.getKey();
            List<ShareInfo> shareInfos = entry.getValue();
            if (shareInfos != null && !shareInfos.isEmpty()) {
                notificationInfos.add(new NotificationInfo(recipient, shareInfos.get(0).getGuest(), shareInfos));
            }
        }

        return notificationInfos;
    }

}
