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

package com.openexchange.ajax.requesthandler.jobqueue.json.actions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AbstractJobQueueJsonAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public abstract class AbstractJobQueueJsonAction implements AJAXActionService {

    /** The service look-up */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractJobQueueJsonAction}.
     *
     * @param services The service look-up
     */
    protected AbstractJobQueueJsonAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Gets the specified field's string value.
     *
     * @param field The field name
     * @param jObject The JSON object to read from
     * @return The string value
     * @throws OXException If no such field is available
     */
    protected String requireStringField(String field, JSONObject jObject) throws OXException {
        String value = jObject.optString(field, null);
        if (null == value) {
            throw AjaxExceptionCodes.MISSING_FIELD.create(field);
        }
        return value;
    }

    /**
     * Gets the specified field's array value.
     *
     * @param field The field name
     * @param jObject The JSON object to read from
     * @return The array value
     * @throws OXException If no such field is available
     */
    protected JSONArray requireArrayField(String field, JSONObject jObject) throws OXException {
        JSONArray value = jObject.optJSONArray(field);
        if (null == value) {
            throw AjaxExceptionCodes.MISSING_FIELD.create(field);
        }
        return value;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        try {
            return doPerform(requestData, session);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the identifier of the action.
     *
     * @return The action identifier
     */
    public abstract String getAction();

    /**
     * Performs given request.
     *
     * @param requestData The request to perform
     * @param session The session providing needed user data
     * @return The result yielded for given request
     * @throws OXException If an error occurs
     */
    protected abstract AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException;

}
