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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
