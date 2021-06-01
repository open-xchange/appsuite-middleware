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

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.SimpleData;
import com.openexchange.conversion.datahandler.DataHandlers;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.subscription.ShareLinkAnalyzeResult;
import com.openexchange.share.subscription.ShareSubscriptionRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AnalyzeAction} - Analyzes a share link from a remote server and returns a state indicating on the operation for the link
 * and which service can handle the link
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class AnalyzeAction extends AbstractShareSubscriptionAction {

    /**
     * Initializes a new {@link AnalyzeAction}.
     * 
     * @param services The service lookup
     */
    public AnalyzeAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session, JSONObject json, String shareLink) throws OXException {
        ShareSubscriptionRegistry service = services.getServiceSafe(ShareSubscriptionRegistry.class);
        ShareLinkAnalyzeResult result = service.analyze(session, shareLink);
        JSONObject jsonObject = new JSONObject(5);
        try {
            jsonObject.put("state", result.getState());
            putDetails(result, jsonObject);
            return createResponse(result.getInfos(), jsonObject);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        }
    }

    private void putDetails(ShareLinkAnalyzeResult result, JSONObject jsonObject) throws OXException, JSONException {
        OXException details = result.getDetails();
        if (null == details) {
            return;
        }
        details.setStackTrace(new StackTraceElement[0]);
        ConversionService conversionService = services.getServiceSafe(ConversionService.class);
        DataHandler error2json = conversionService.getDataHandler(DataHandlers.OXEXCEPTION2JSON);

        ConversionResult rs = error2json.processData(new SimpleData<OXException>(details), new DataArguments(), null);
        Object data = rs.getData();
        if (data != null && JSONObject.class.isInstance(data)) {
            jsonObject.put("error", data);
        }
    }

}
