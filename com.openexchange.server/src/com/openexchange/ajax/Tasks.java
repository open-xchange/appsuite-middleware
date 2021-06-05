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

package com.openexchange.ajax;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.request.TaskRequest;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * Tasks
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Tasks extends DataServlet {

    private static final long serialVersionUID = 8092832647688901704L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Tasks.class);

    @Override
    protected void doGet(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws IOException {
        final ServerSession session = getSessionObject(httpServletRequest);
        final Response response = new Response(session);
        try {
            final String action = parseMandatoryStringParameter(httpServletRequest, PARAMETER_ACTION);
            final JSONObject jsonObj;
            try {
                 jsonObj = convertParameter2JSONObject(httpServletRequest);
            } catch (JSONException e) {
                LOG.error("", e);
                response.setException(OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e));
                writeResponse(response, httpServletResponse, session);
                return;
            }
            final TaskRequest taskRequest = new TaskRequest(session);
            final JSONValue responseObj = taskRequest.action(action, jsonObj);
            response.setTimestamp(taskRequest.getTimestamp());
            response.setData(responseObj);
        } catch (OXException e) {
            if (e.getCategory() == Category.CATEGORY_USER_INPUT) {
                LOG.debug("", e);
            } else {
                LOG.error("", e);
            }
            response.setException(e);
         } catch (JSONException e) {
            final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            LOG.error("", oje);
            response.setException(oje);
        }
        writeResponse(response, httpServletResponse, session);
    }

    @Override
    protected void doPut(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws IOException {
        final ServerSession session = getSessionObject(httpServletRequest);
        final Response response = new Response(session);
        try {
            final String action = parseMandatoryStringParameter(httpServletRequest, PARAMETER_ACTION);

            final String data = getBody(httpServletRequest);
            if (data.length() > 0) {
                final TaskRequest taskRequest = new TaskRequest(session);
                final JSONObject jsonObj;

                try {
                    jsonObj = convertParameter2JSONObject(httpServletRequest);
                } catch (JSONException e) {
                    LOG.error("", e);
                    response.setException(OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e));
                    writeResponse(response, httpServletResponse, session);
                    return;
                }

                if (data.charAt(0) == '[') {
                    final JSONArray jsonDataArray = new JSONArray(data);
                    jsonObj.put(PARAMETER_DATA, jsonDataArray);

                    final JSONValue responseObj = taskRequest.action(action, jsonObj);
                    response.setTimestamp(taskRequest.getTimestamp());
                    response.setData(responseObj);
                } else if (data.charAt(0) == '{') {
                    final JSONObject jsonDataObject = new JSONObject(data);
                    jsonObj.put(PARAMETER_DATA, jsonDataObject);

                    final Object responseObj = taskRequest.action(action, jsonObj);
                    response.setTimestamp(taskRequest.getTimestamp());
                    response.setData(responseObj);
                } else {
                    httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid json object");
                }
            } else {
                httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "no data found");
            }
        } catch (JSONException e) {
            final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            LOG.error("", oje);
            response.setException(oje);
        } catch (OXException e) {
            if (e.getCategory() == Category.CATEGORY_USER_INPUT) {
                LOG.debug("", e);
            } else {
                LOG.error("", e);
            }
            response.setException(e);
        }
        writeResponse(response, httpServletResponse, session);
    }

    @Override
    protected boolean hasModulePermission(final ServerSession session) {
        return session.getUserPermissionBits().hasTask();
    }
}
