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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.request.TaskRequest;
import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderNotFoundException;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;

/**
 * Tasks
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Tasks extends DataServlet {
	
	private static final long serialVersionUID = 8092832647688901704L;
	
	private static final Log LOG = LogFactory.getLog(Tasks.class);
	
	@Override
	protected void doGet(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws ServletException, IOException {
		final Response response = new Response();
		try {
			final String action = parseMandatoryStringParameter(httpServletRequest, PARAMETER_ACTION);
			final Session sessionObj = getSessionObject(httpServletRequest);
			final JSONObject jsonObj;
			try {			
				 jsonObj = convertParameter2JSONObject(httpServletRequest);
			} catch (JSONException e) {
				LOG.error(e.getMessage(), e);
	            response.setException(new OXJSONException(OXJSONException.Code.JSON_BUILD_ERROR, e));
	            writeResponse(response, httpServletResponse);
	            return;
			}

			final TaskRequest taskRequest = new TaskRequest(sessionObj);
			final Object responseObj = taskRequest.action(action, jsonObj);
			response.setTimestamp(taskRequest.getTimestamp());
			response.setData(responseObj);
		} catch (OXMandatoryFieldException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (OXConflictException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (JSONException e) {
            final OXJSONException oje = new OXJSONException(OXJSONException.Code
                .JSON_WRITE_ERROR, e);
            LOG.error(oje.getMessage(), oje);
            response.setException(oje);
        } catch (OXJSONException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
		} catch (OXFolderNotFoundException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (OXObjectNotFoundException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (OXPermissionException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (SearchIteratorException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (AjaxException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (OXException e) {
			if (e.getCategory() == Category.USER_INPUT) {
				LOG.debug(e.getMessage(), e);
			} else {
				LOG.error(e.getMessage(), e);
			}

			response.setException(e);
		}
		
		writeResponse(response, httpServletResponse);
	}

	@Override
	protected void doPut(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws ServletException, IOException {
		final Response response = new Response();
		try {
			final String action = parseMandatoryStringParameter(httpServletRequest, PARAMETER_ACTION);
			final Session sessionObj = getSessionObject(httpServletRequest);
			
			final String data = getBody(httpServletRequest);
			if (data.length() > 0) {
				final TaskRequest taskRequest = new TaskRequest(sessionObj);
				final JSONObject jsonObj;

				try {
					jsonObj = convertParameter2JSONObject(httpServletRequest);
				} catch (JSONException e) {
					LOG.error(e.getMessage(), e);
		            response.setException(new OXJSONException(OXJSONException.Code.JSON_BUILD_ERROR, e));
		            writeResponse(response, httpServletResponse);
		            return;
				}
				
				if (data.charAt(0) == '[') {
					final JSONArray jsonDataArray = new JSONArray(data);
					jsonObj.put(PARAMETER_DATA, jsonDataArray);
				
					final Object responseObj = taskRequest.action(action, jsonObj);
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
            final OXJSONException oje = new OXJSONException(OXJSONException.Code
                .JSON_WRITE_ERROR, e);
            LOG.error(oje.getMessage(), oje);
            response.setException(oje);
        } catch (OXJSONException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
		} catch (SearchIteratorException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (AjaxException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (OXException e) {
			if (e.getCategory() == Category.USER_INPUT) {
				LOG.debug(e.getMessage(), e);
			} else {
				LOG.error(e.getMessage(), e);
			}

			response.setException(e);
		}
		
		writeResponse(response, httpServletResponse);
	}
	
	protected boolean hasModulePermission(final Session sessionObj) {
		return UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(),
				sessionObj.getContext()).hasTask();
	}
}
