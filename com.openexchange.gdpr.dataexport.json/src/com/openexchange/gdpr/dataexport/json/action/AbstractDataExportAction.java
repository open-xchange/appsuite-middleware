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

package com.openexchange.gdpr.dataexport.json.action;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExportArguments;
import com.openexchange.gdpr.dataexport.DataExportService;
import com.openexchange.gdpr.dataexport.Module;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractDataExportAction} - Abstract GDPR data export action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractDataExportAction implements AJAXActionService {

    /**
     * The service look-up
     */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractDataExportAction}.
     *
     * @param services The service look-up
     */
    protected AbstractDataExportAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Gets the GDPR data export service.
     *
     * @return The GDPR data export service
     * @throws OXException If GDPR data export service cannot be returned
     */
    protected DataExportService getDataExportService() throws OXException {
        DataExportService service = services.getService(DataExportService.class);
        if (null == service) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DataExportService.class.getName());
        }
        return service;
    }

    private static final String JSON_MAX_FILE_SIZE = "maxFileSize";

    /**
     * Parses specified JSON representation of arguments.
     *
     * @param jArgs The JSON arguments
     * @return The parsed arguments
     */
    protected static DataExportArguments parseArguments(JSONObject jArgs) {
        DataExportArguments arguments = new DataExportArguments();

        arguments.setMaxFileSize(jArgs.optLong(JSON_MAX_FILE_SIZE, -1));

        int length = jArgs.length();
        List<Module> modules = new ArrayList<>(length);
        for (String moduleId : jArgs.keySet()) {
            if (!JSON_MAX_FILE_SIZE.equals(moduleId) ) {
                JSONObject jProperties = jArgs.optJSONObject(moduleId);
                modules.add(Module.valueOf(moduleId, jProperties == null ? null : jProperties.asMap()));
            }
        }
        arguments.setModules(modules);

        return arguments;
    }

    /**
     * Requires a JSON content in given request's data.
     *
     * @param requestData The request data to read from
     * @return The JSON content
     * @throws OXException If JSON content cannot be returned
     */
    protected JSONValue requireJSONBody(AJAXRequestData requestData) throws OXException {
        Object data = requestData.getData();
        if (null == data) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        JSONValue jBody = requestData.getData(JSONValue.class);
        if (null == jBody) {
            throw AjaxExceptionCodes.INVALID_REQUEST_BODY.create(JSONValue.class, data.getClass());
        }
        return (JSONValue) data;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        if (!services.getService(CapabilityService.class).getCapabilities(session).contains("dataexport")) {
            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create("gdpr/dataexport");
        }
        try {
            return doPerform(requestData, session);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Performs given GDPR data export request.
     *
     * @param requestData The request data
     * @param session The session providing user information
     * @return The AJAX result
     * @throws OXException If performing request fails
     */
    protected abstract AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException;

}
