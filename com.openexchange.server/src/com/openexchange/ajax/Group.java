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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.request.GroupRequest;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * Servlet implementing group requests.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Group extends DataServlet {

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = 6699123983027304951L;
	private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Group.class);

	/**
	 * {@inheritDoc}
	 * @author Sebastian Kauss
	 */
	@Override
    protected void doGet(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws ServletException, IOException {
	    final ServerSession session = getSessionObject(httpServletRequest);
        final Response response = new Response(session);
		try {
			final String action = parseMandatoryStringParameter(httpServletRequest, PARAMETER_ACTION);
			JSONObject jsonObj;

			try {
				jsonObj = convertParameter2JSONObject(httpServletRequest);
			} catch (JSONException e) {
				LOG.error("", e);
	            response.setException(OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e));
	            writeResponse(response, httpServletResponse, session);
	            return;
			}
			final GroupRequest groupRequest = new GroupRequest(session);
			final Object responseObj = groupRequest.action(action, jsonObj);
			response.setTimestamp(groupRequest.getTimestamp());
			response.setData(responseObj);
			//response.setData(new JSONObject(sw.toString()));
		} catch (OXException e) {
            LOG.error("", e);
            response.setException(e);
		} catch (JSONException e) {
            final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            LOG.error("", oje);
            response.setException(oje);
		}

		writeResponse(response, httpServletResponse, session);
	}

	@Override
	protected void doPut(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws ServletException, IOException {
	    final ServerSession session = getSessionObject(httpServletRequest);
        final Response response = new Response(session);
		try {
			final String action = parseMandatoryStringParameter(httpServletRequest, PARAMETER_ACTION);

			final String data = getBody(httpServletRequest);
			JSONObject jsonObj;

			try {
				jsonObj = convertParameter2JSONObject(httpServletRequest);
			} catch (JSONException e) {
				LOG.error("", e);
	            response.setException(OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e));
	            writeResponse(response, httpServletResponse, session);
	            return;
			}
			final GroupRequest groupRequest = new GroupRequest(session);

			if (data.charAt(0) == '[') {
				final JSONArray jData = new JSONArray(data);

				jsonObj.put(AJAXServlet.PARAMETER_DATA, jData);

				final Object responseObj = groupRequest.action(action, jsonObj);
				response.setTimestamp(groupRequest.getTimestamp());
				// According to the documentation this is definitely an array
				response.setData(responseObj);
				//response.setData(new JSONArray(sw.toString()));

			} else if (data.charAt(0) == '{') {
				final JSONObject jData = new JSONObject(data);

				jsonObj.put(AJAXServlet.PARAMETER_DATA, jData);

				final Object responseObj = groupRequest.action(action, jsonObj);
				response.setTimestamp(groupRequest.getTimestamp());
				// According to the documentation this is definitely an array
				response.setData(responseObj);
				//response.setData(new JSONArray(sw.toString()));
			} else {
				httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid json object");
			}
		} catch (OXException e) {
            LOG.error("", e);
            response.setException(e);
		} catch (JSONException e) {
            final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            LOG.error("", oje);
            response.setException(oje);
		}

		writeResponse(response, httpServletResponse, session);
	}

	@Override
	protected boolean hasModulePermission(final ServerSession session) {
		return true;
	}
}
