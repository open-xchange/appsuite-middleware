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

package com.openexchange.mail.categories.json;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.mail.categories.MailCategoriesConfigService;
import com.openexchange.mail.categories.ReorganizeParameter;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link TrainAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class TrainAction extends AbstractCategoriesAction {

    private static final String CREATE_RULE_PARAMETER = "apply-for-future-ones";
    private static final String REORGANIZE_PARAMETER = "apply-for-existing";
    private static final String CATEGORY_ID_PARAMETER = "category_id";

    private static final String BODY_FIELD_FROM = "from";

    /**
     * Initializes a new {@link TrainAction}.
     *
     * @param services
     */
    protected TrainAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        MailCategoriesConfigService mailCategoriesService = services.getService(MailCategoriesConfigService.class);
        if (mailCategoriesService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(MailCategoriesConfigService.class.getSimpleName());
        }

        String category = requestData.requireParameter(CATEGORY_ID_PARAMETER);

        Object data = requestData.getData();

        if (!(data instanceof JSONObject)) {
            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
        }

        JSONObject json = (JSONObject) data;

        data = json.get(BODY_FIELD_FROM);

        if (!(data instanceof JSONArray)) {
            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
        }

        List<Object> objects = ((JSONArray) data).asList();
        List<String> addresses = new ArrayList<>(objects.size());
        for (Object obj : objects) {
            if (obj instanceof String) {
                addresses.add((String) obj);
            }
        }
        if (addresses.size() == 0) {
            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
        }

        boolean createRule = AJAXRequestDataTools.parseBoolParameter(CREATE_RULE_PARAMETER, requestData, true);

        // Check for re-organize flag
        boolean reorganize = AJAXRequestDataTools.parseBoolParameter(REORGANIZE_PARAMETER, requestData);
        ReorganizeParameter reorganizeParameter = ReorganizeParameter.getParameterFor(reorganize);

        mailCategoriesService.trainCategory(category, addresses, createRule, reorganizeParameter, session);

        AJAXRequestResult result = new AJAXRequestResult();
        if (reorganizeParameter.hasWarnings()) {
            result.addWarnings(reorganizeParameter.getWarnings());
        }
        return result;
    }

}
