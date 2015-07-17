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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.share.notification.ShareNotifyExceptionCodes;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link SendLinkAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class SendLinkAction extends AbstractShareAction {

    /**
     * Initializes a new {@link SendLinkAction}.
     *
     * @param services The service lookup
     */
    public SendLinkAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        try {
            JSONObject json = (JSONObject) requestData.requireData();
            ShareTarget target = ShareJSONParser.parseTarget(json, getTimeZone(requestData, session), services.getService(ModuleSupport.class));
            String jTransport = json.optString("transport", null);
            if (jTransport == null || "mail".equals(jTransport)) {
                ShareService shareService = services.getService(ShareService.class);
                List<ShareInfo> shares = shareService.getShares(session, moduleFor(target), target.getFolder(), target.getItem());
                ShareInfo link = null;
                for (ShareInfo share : shares) {
                    if (share.getGuest().getRecipientType() == RecipientType.ANONYMOUS) {
                        link = share;
                        break;
                    }
                }

                if (link == null) {
                    throw ShareExceptionCodes.INVALID_LINK_TARGET.create(target.getModule(), target.getFolder(), target.getItem());
                }

                JSONArray jRecipients = json.getJSONArray("recipients");
                List<Object> transportInfos = new ArrayList<>();
                for (int i = 0; i < jRecipients.length(); i++) {
                    transportInfos.add(parseAddress(jRecipients.getJSONArray(i)));
                }

                ShareNotificationService shareNotificationService = services.getService(ShareNotificationService.class);
                List<OXException> warnings = shareNotificationService.sendLinkNotifications(
                    Transport.MAIL,
                    transportInfos,
                    json.optString("message", null),
                    link,
                    session,
                    requestData.getHostData());

                AJAXRequestResult result = new AJAXRequestResult();
                result.addWarnings(warnings);
                result.setResultObject(new JSONObject(), "json");
                result.setTimestamp(new Date());
                return result;
            }

            throw ShareNotifyExceptionCodes.UNKNOWN_NOTIFICATION_TRANSPORT.create(jTransport);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
    }

    private static InternetAddress parseAddress(JSONArray jRecipient) throws JSONException, OXException {
        try {
            if (jRecipient.length() == 1) {
                return new QuotedInternetAddress(jRecipient.getString(0));
            } else if (jRecipient.length() == 2) {
                return new QuotedInternetAddress(jRecipient.getString(1), jRecipient.getString(0), "UTF-8");
            }

            throw ShareExceptionCodes.INVALID_MAIL_ADDRESS.create(jRecipient.get(0));
        } catch (AddressException | UnsupportedEncodingException e) {
            throw ShareExceptionCodes.INVALID_MAIL_ADDRESS.create(jRecipient.get(0));
        }
    }

}
