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

package com.openexchange.share.json.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.notification.LinkProvider;
import com.openexchange.share.notification.mail.MailNotifications;
import com.openexchange.share.notification.mail.MailNotifications.ShareCreatedBuilder;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link InviteAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class InviteAction extends AbstractShareAction {

    /**
     * Initializes a new {@link InviteAction}.
     *
     * @param services The service lookup
     * @param translatorFactory
     */
    public InviteAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        try {
            JSONObject data = (JSONObject) requestData.requireData();
            List<ShareRecipient> recipients = ShareJSONParser.parseRecipients(data.getJSONArray("recipients"));
            List<ShareTarget> targets = ShareJSONParser.parseTargets(data.getJSONArray("targets"), getTimeZone(requestData, session));
            String message = data.optString("message", null);
            /*
             * create the shares, notify recipients
             */
            CreatePerformer createPerformer = new CreatePerformer(recipients, targets, session, services);
            Map<ShareRecipient, List<ShareInfo>> createdShares = createPerformer.perform();
            List<NotificationInfo> notificationInfos = getNotificationInfos(createdShares);
            List<OXException> warnings = sendNotifications(notificationInfos, message, requestData, session);
            /*
             * construct & return appropriate json result
             */
            AJAXRequestResult result = new AJAXRequestResult();
            result.addWarnings(warnings);
            JSONArray jTokens = new JSONArray(recipients.size());
            for (ShareRecipient recipient : recipients) {
                List<ShareInfo> shares = createdShares.get(recipient);
                if (null == shares || 0 == shares.size()) {
                    // internal recipient
                    jTokens.put(JSONObject.NULL);
                } else {
                    // external recipient
                    if (1 == shares.size()) {
                        jTokens.put(shares.get(0).getToken());
                    } else {
                        jTokens.put(shares.get(0).getGuest().getBaseToken());
                    }
                }
            }
            result.setResultObject(jTokens, "json");
            result.setTimestamp(new Date());
            return result;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
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
    private List<OXException> sendNotifications(List<NotificationInfo> notificationInfos, String message, AJAXRequestData requestData, ServerSession session) {
        List<OXException> warnings = new ArrayList<OXException>();
        for (NotificationInfo notificationInfo : notificationInfos) {
            if (false == Strings.isEmpty(notificationInfo.getGuestInfo().getEmailAddress())) {
                try {
                    sendNotification(notificationInfo, message, requestData, session);
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
    private void sendNotification(NotificationInfo notificationInfo, String message, AJAXRequestData requestData, ServerSession session) throws OXException {
        List<ShareInfo> createdShares = notificationInfo.getShareInfos();
        GuestInfo guest = notificationInfo.getGuestInfo();
        ShareRecipient recipient = notificationInfo.getRecipient();
        String shareToken = 1 == createdShares.size() ? createdShares.get(0).getToken() : guest.getBaseToken();
        try {
            LinkProvider linkProvider = buildLinkProvider(requestData, shareToken);
            ShareCreatedBuilder builder = MailNotifications.shareCreated()
                .setTransportInfo(new InternetAddress(guest.getEmailAddress(), true))
                .setLinkProvider(linkProvider)
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

            getNotificationService().send(builder.build());
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
    private static List<NotificationInfo> getNotificationInfos(Map<ShareRecipient, List<ShareInfo>> createdShares) {
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

    /**
     * A simple container holding all necessary information to send out a notification about the shares creation
     */
    private static final class NotificationInfo {

        private final ShareRecipient recipient;
        private final GuestInfo guestInfo;
        private final List<ShareInfo> shareInfos;

        public NotificationInfo(ShareRecipient recipient, GuestInfo guestInfo, List<ShareInfo> shareInfos) {
            super();
            this.recipient = recipient;
            this.guestInfo = guestInfo;
            this.shareInfos = shareInfos;
        }

        public ShareRecipient getRecipient() {
            return recipient;
        }

        public GuestInfo getGuestInfo() {
            return guestInfo;
        }

        public List<ShareInfo> getShareInfos() {
            return shareInfos;
        }

    }

}
