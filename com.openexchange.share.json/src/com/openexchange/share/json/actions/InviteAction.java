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

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.GuestShare;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.notification.LinkProvider;
import com.openexchange.share.notification.ShareCreatedNotification;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.mail.MailNotifications;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

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

            CreatePerformer createPerformer = new CreatePerformer(recipients, targets, session, services);
            List<GuestShare> shares = createPerformer.perform();
            AJAXRequestResult result = new AJAXRequestResult();
            List<OXException> warnings = sendNotifications(recipients, shares, message, requestData, session);
            result.addWarnings(warnings);

            JSONArray jTokens = new JSONArray(recipients.size());
            for (GuestShare share : shares) {
                if (share == null) {
                    jTokens.put(JSONObject.NULL);
                } else {
                    jTokens.put(share.getGuest().getBaseToken());
                }
            }

            result.setResultObject(jTokens, "json");
            result.setTimestamp(new Date());
            return result;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }

    }

    private List<OXException> sendNotifications(List<ShareRecipient> recipients, List<GuestShare> shares, String message, AJAXRequestData requestData, ServerSession session) {
        List<OXException> warnings = new LinkedList<OXException>();
        try {
            if (!recipients.isEmpty()) {
                ShareNotificationService notificationService = getNotificationService();
                UserService userService = getUserService();
                Iterator<ShareRecipient> recipientIterator = recipients.iterator();
                Iterator<GuestShare> shareIterator = shares.iterator();
                while (recipientIterator.hasNext()) {
                    ShareRecipient recipient = recipientIterator.next();
                    GuestShare share = shareIterator.next();
                    switch (recipient.getType()) {
                        case GUEST:
                        {
                            String shareToken;
                            if (share.isMultiTarget()) {
                                shareToken = share.getGuest().getBaseToken();
                            } else {
                                shareToken = share.getGuest().getBaseToken() + '/' + share.getSingleTarget().getPath();
                            }

                            User guest = userService.getUser(share.getGuest().getGuestID(), session.getContextId());
                            String mailAddress = guest.getMail();
                            if (!Strings.isEmpty(mailAddress)) {
                                try {
                                    LinkProvider linkProvider = buildLinkProvider(requestData, shareToken, mailAddress);
                                    ShareCreatedNotification<InternetAddress> notification = MailNotifications.shareCreated()
                                        .setTransportInfo(new InternetAddress(mailAddress, true))
                                        .setLinkProvider(linkProvider)
                                        .setContext(share.getGuest().getContextID())
                                        .setLocale(guest.getLocale())
                                        .setSession(session)
                                        .setRecipient(recipient)
                                        .setTargets(share.getTargets())
                                        .setMessage(message)
                                        .build();
                                     notificationService.send(notification);
                                } catch (AddressException e) {
                                    warnings.add(ShareExceptionCodes.INVALID_MAIL_ADDRESS.create(mailAddress));
                                } catch (Exception e) {
                                    if (e instanceof OXException) {
                                        warnings.add((OXException) e);
                                    } else {
                                        warnings.add(ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage()));
                                    }
                                }
                            }
                            break;
                        }

                        default:
                            break;
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof OXException) {
                warnings.add((OXException) e);
            } else {
                warnings.add(ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage()));
            }
        }

        return warnings;
    }

}
