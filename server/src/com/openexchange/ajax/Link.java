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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax;


import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.request.LinkRequest;
import com.openexchange.api.OXConflictException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;
import com.openexchange.tools.servlet.http.Tools;

public class Link extends DataServlet {
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(Link.class);
	
	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = 8612709224269281439L;
	
	@Override
	protected void doGet(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws ServletException, IOException {
		final Response response = new Response();
		final StringWriter sw = new StringWriter();

		try {
			final Session sessionObj = getSessionObject(httpServletRequest);
			final String action = getAction(httpServletRequest);
			JSONObject jsonObj;
			
			try {
				jsonObj = convertParameter2JSONObject(httpServletRequest);
			} catch (JSONException e) {
				LOG.error(e.getMessage(), e);
	            response.setException(new OXJSONException(OXJSONException.Code.JSON_BUILD_ERROR, e));
	            writeResponse(response, httpServletResponse);
	            return;
			}

			final Context ctx = ContextStorage.getStorageContext(sessionObj.getContextId());
			
			final LinkRequest linkRequest = new LinkRequest(sessionObj, sw, ctx);
			linkRequest.action(action, jsonObj);

			response.setData(new JSONArray(sw.toString()));
		} catch (OXConflictException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (OXJSONException exc) {
            LOG.error(exc.getMessage(), exc);
            response.setException(exc);
		} catch (OXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (JSONException e) {
            final OXJSONException oje = new OXJSONException(OXJSONException.Code
                .JSON_WRITE_ERROR, e);
            LOG.error(oje.getMessage(), oje);
            response.setException(oje);
		} catch (AjaxException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		}

		writeResponse(response, httpServletResponse);

	}
	
	@Override
	protected void doPut(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws ServletException, IOException {
		Response response = new Response();

		httpServletResponse.setContentType(CONTENTTYPE_JAVASCRIPT);
		/*
		 * Disable browser caching
		 */
		Tools.disableCaching(httpServletResponse);
		try {
			final StringWriter sw = new StringWriter();

			final String action = getAction(httpServletRequest);

			final Session sessionObj = getSessionObject(httpServletRequest);
			
			final String data = getBody(httpServletRequest);
			
			if (data.length() > 0) {
				final Context ctx = ContextStorage.getStorageContext(sessionObj.getContextId());
				
				final LinkRequest linkRequest = new LinkRequest(sessionObj, sw, ctx);

				final JSONObject jsonDataObj = new JSONObject(data);
				JSONObject jsonObj;
				try {
					jsonObj = convertParameter2JSONObject(httpServletRequest);
				} catch (JSONException e) {
					LOG.error(e.getMessage(), e);
		            response.setException(new OXJSONException(OXJSONException.Code.JSON_BUILD_ERROR, e));
		            writeResponse(response, httpServletResponse);
		            return;
				}

				jsonObj.put(AJAXServlet.PARAMETER_DATA, jsonDataObj);
				
				linkRequest.action(action, jsonObj);
				try {
					response.setData(new JSONArray(sw.toString()));	
				} catch (JSONException e) {
					response.setData(new JSONObject(sw.toString()));
				}
			} else {
				httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "no data found");
			}
		} catch (OXJSONException exc) {
            LOG.error(exc.getMessage(), exc);
            response.setException(exc);
		} catch (JSONException e) {
            final OXJSONException oje = new OXJSONException(OXJSONException.Code
                .JSON_WRITE_ERROR, e);
            LOG.error(oje.getMessage(), oje);
            response.setException(oje);
		} catch (OXConflictException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (OXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (AjaxException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		}
		
		writeResponse(response, httpServletResponse);
	}
	
	@Override
	protected boolean hasModulePermission(final Session sessionObj, final Context ctx) {
		return UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(),
				ctx).hasContact();
	}
}
