/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.share.json.actions;

import static com.openexchange.java.Autoboxing.I;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.ShareNotificationService.Transport;
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
        /*
         * parse parameters & target
         */
        JSONObject json = (JSONObject) requestData.requireData();
        ShareTarget target = getParser().parseTarget(json);
        Transport transport = getParser().parseNotificationTransport(json);
        String message = json.optString("message", null);
        List<Object> transportInfos;
        try {
            transportInfos = getParser().parseTransportInfos(transport, json.getJSONArray("recipients"));
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
        /*
         * lookup share
         */
        ShareInfo shareInfo = getShareService().optLink(session, target);
        if (null == shareInfo) {
            throw ShareExceptionCodes.INVALID_LINK_TARGET.create(I(target.getModule()), target.getFolder(), target.getItem());
        }
        /*
         * process notification(s)
         */
        ShareNotificationService shareNotificationService = services.getService(ShareNotificationService.class);
        List<OXException> warnings = shareNotificationService.sendLinkNotifications(
            transport, transportInfos, message, shareInfo, session, requestData.getHostData());
        /*
         * return empty result (including warnings) in case of success
         */
        AJAXRequestResult result = new AJAXRequestResult(new JSONObject(), new Date(), "json");
        result.addWarnings(warnings);
        return result;
    }

}
