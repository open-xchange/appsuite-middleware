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
import com.openexchange.ajax.request.ResourceRequest;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link Resource} - The servlet handling requests to "resource"
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class Resource extends DataServlet {

	private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Resource.class);

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = -8381608654367561643L;

	@Override
	protected void doGet(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse)
			throws ServletException, IOException {
	    final ServerSession session = getSessionObject(httpServletRequest);
        final Response response = new Response(session);
		try {
			final String action = parseMandatoryStringParameter(httpServletRequest, PARAMETER_ACTION);
			JSONObject jsonObj = null;
			try {
				jsonObj = convertParameter2JSONObject(httpServletRequest);
			} catch (JSONException e) {
				LOG.error("", e);
				response.setException(OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e));
				writeResponse(response, httpServletResponse, session);
				return;
			}

			final ResourceRequest resourceRequest = new ResourceRequest(session);
			final Object responseObj = resourceRequest.action(action, jsonObj);
			response.setTimestamp(resourceRequest.getTimestamp());
			response.setData(responseObj);
		} catch (OXException e) {
			LOG.error(_doGet, e);
			response.setException(e);
		} catch (JSONException e) {
			final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
			LOG.error("", oje);
			response.setException(oje);
		}

		writeResponse(response, httpServletResponse, session);
	}

	@Override
	protected void doPut(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse)
			throws ServletException, IOException {
	    final ServerSession session = getSessionObject(httpServletRequest);
        final Response response = new Response(session);

		try {
			final String action = parseMandatoryStringParameter(httpServletRequest, PARAMETER_ACTION);

			final String data = getBody(httpServletRequest);
			if (data.charAt(0) == '[') {
				JSONArray jData = null;
				try {
					jData = new JSONArray(data);
				} catch (JSONException e) {
					final OXException exc = OXJSONExceptionCodes.JSON_READ_ERROR.create(e, data);
					response.setException(exc);
					writeResponse(response, httpServletResponse, session);
					LOG.error("", exc);
					return;
				}
				JSONObject jsonObj = null;
				try {
					jsonObj = convertParameter2JSONObject(httpServletRequest);
				} catch (JSONException e) {
					LOG.error(_doPut, e);
					response.setException(OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e));
					writeResponse(response, httpServletResponse, session);
					return;
				}

				jsonObj.put(PARAMETER_DATA, jData);

				final ResourceRequest resourceRequest = new ResourceRequest(session);
				final Object responseObj = resourceRequest.action(action, jsonObj);
				response.setTimestamp(resourceRequest.getTimestamp());
				response.setData(responseObj);
			} else {
				JSONObject jData = null;
				JSONObject jsonObj = null;
				try {
					jData = new JSONObject(data);
					jsonObj = convertParameter2JSONObject(httpServletRequest);
				} catch (JSONException e) {
					LOG.error(_doPut, e);
					response.setException(OXJSONExceptionCodes.JSON_READ_ERROR.create(e));
					writeResponse(response, httpServletResponse, session);
					return;
				}

				jsonObj.put(PARAMETER_DATA, jData);

				final ResourceRequest resourceRequest = new ResourceRequest(session);
				final Object responseObj = resourceRequest.action(action, jsonObj);
				response.setTimestamp(resourceRequest.getTimestamp());
				response.setData(responseObj);
			}
		} catch (OXException e) {
			LOG.error(_doPut, e);
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
