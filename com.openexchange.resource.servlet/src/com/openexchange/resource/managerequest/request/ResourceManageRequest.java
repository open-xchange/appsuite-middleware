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

package com.openexchange.resource.managerequest.request;

import static com.openexchange.resource.managerequest.services.ResourceRequestServiceRegistry.getServiceRegistry;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.requesthandler.AJAXRequestHandler;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.resource.ResourceService;
import com.openexchange.security.BundleAccessSecurityService;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxException;

/**
 * {@link ResourceManageRequest} - Executes a resource-manage request
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class ResourceManageRequest implements AJAXRequestHandler {

	private static final String MODULE_RESOURCE = "resource";

	private static final Set<String> ACTIONS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			AJAXServlet.ACTION_NEW, AJAXServlet.ACTION_UPDATE, AJAXServlet.ACTION_DELETE)));

	/**
	 * Initializes a new {@link ResourceManageRequest}
	 * 
	 * @param session
	 *            The session providing needed user data
	 * @param ctx
	 *            The context
	 */
	public ResourceManageRequest() {
		super();
	}

	public AJAXRequestResult performAction(final String action, final JSONObject jsonObject, final Session session,
			final Context ctx) throws AbstractOXException, JSONException {
		final BundleAccessSecurityService securityService = getServiceRegistry().getService(
				BundleAccessSecurityService.class);
		if (null == securityService) {
			throw new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE, BundleAccessSecurityService.class
					.getName());
		}
		securityService.checkPermission(new String[] { "com.openexchange.resource.*" },
				"com.openexchange.resource.managerequest");
		if (action.equalsIgnoreCase(AJAXServlet.ACTION_NEW)) {
			return actionNew(jsonObject, session, ctx);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATE)) {
			return actionUpdate(jsonObject, session, ctx);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_DELETE)) {
			return actionDelete(jsonObject, session, ctx);
		} else {
			throw new AjaxException(AjaxException.Code.UnknownAction, action);
		}
	}

	/**
	 * Performs a create request
	 * 
	 * @param jsonObj
	 *            The JSON data object (containing "data", "timestamp", etc.)
	 * @return The newly created resource's ID
	 * @throws AbstractOXException
	 *             If creation fails
	 * @throws JSONException
	 *             If a JsSON error occurs
	 */
	private AJAXRequestResult actionNew(final JSONObject jsonObj, final Session session, final Context ctx)
			throws AbstractOXException, JSONException {
		/*
		 * Check for "data"
		 */
		final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
		/*
		 * Parse resource out of JSON object
		 */
		final com.openexchange.resource.Resource resource = com.openexchange.resource.json.ResourceParser
				.parseResource(jData);
		/*
		 * Create new resource
		 */
		final ResourceService resourceService = getServiceRegistry().getService(ResourceService.class);
		if (null == resourceService) {
			throw new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE, ResourceService.class.getName());
		}
		resourceService.create(UserStorage.getStorageUser(session.getUserId(), ctx), ctx, resource);
		/*
		 * Return its ID
		 */
		return new AJAXRequestResult(Integer.valueOf(resource.getIdentifier()));
	}

	/**
	 * Performs an update request
	 * 
	 * @param jsonObj
	 *            The JSON data object (containing "data", "timestamp", etc.)
	 * @return The modified resource's JSON representation
	 * @throws AbstractOXException
	 *             If update fails
	 * @throws JSONException
	 *             If a JsSON error occurs
	 */
	private AJAXRequestResult actionUpdate(final JSONObject jsonObj, final Session session, final Context ctx)
			throws AbstractOXException, JSONException {
		final ResourceService resourceService = getServiceRegistry().getService(ResourceService.class);
		if (null == resourceService) {
			throw new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE, ResourceService.class.getName());
		}
		/*
		 * Check for "data"
		 */
		final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
		final com.openexchange.resource.Resource resource = com.openexchange.resource.json.ResourceParser
				.parseResource(jData);
		final Date clientLastModified;
		if (jsonObj.has(AJAXServlet.PARAMETER_TIMESTAMP) && !jsonObj.isNull(AJAXServlet.PARAMETER_TIMESTAMP)) {
			clientLastModified = new Date(jsonObj.getLong(AJAXServlet.PARAMETER_TIMESTAMP));
		} else {
			clientLastModified = null;
		}
		/*
		 * Update resource
		 */
		resourceService.update(UserStorage.getStorageUser(session.getUserId(), ctx), ctx, resource, clientLastModified);
		/*
		 * Write updated resource
		 */
		return new AJAXRequestResult(com.openexchange.resource.json.ResourceWriter.writeResource(resource));
	}

	/**
	 * Performs a delete request
	 * 
	 * @param jsonObj
	 *            The JSON data object (containing "data", "timestamp", etc.)
	 * @return The constant {@link JSONObject#NULL NULL} since nothing is
	 *         intended to be returned to requester
	 * @throws AbstractOXException
	 *             If deletion fails
	 * @throws JSONException
	 *             If a JsSON error occurs
	 */
	private AJAXRequestResult actionDelete(final JSONObject jsonObj, final Session session, final Context ctx)
			throws AbstractOXException, JSONException {
		final ResourceService resourceService = getServiceRegistry().getService(ResourceService.class);
		if (null == resourceService) {
			throw new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE, ResourceService.class.getName());
		}
		/*
		 * Check for "data"
		 */
		final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
		final com.openexchange.resource.Resource resource = com.openexchange.resource.json.ResourceParser
				.parseResource(jData);
		final Date clientLastModified;
		if (jsonObj.has(AJAXServlet.PARAMETER_TIMESTAMP) && !jsonObj.isNull(AJAXServlet.PARAMETER_TIMESTAMP)) {
			clientLastModified = new Date(jsonObj.getLong(AJAXServlet.PARAMETER_TIMESTAMP));
		} else {
			clientLastModified = null;
		}
		/*
		 * Delete resource
		 */
		resourceService.delete(UserStorage.getStorageUser(session.getUserId(), ctx), ctx, resource, clientLastModified);
		/*
		 * Write JSON null
		 */
		return new AJAXRequestResult(JSONObject.NULL);
	}

	public String getModule() {
		return MODULE_RESOURCE;
	}

	public Set<String> getSupportedActions() {
		return ACTIONS;
	}

}
