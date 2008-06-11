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

package com.openexchange.resource.servlet.request;

import static com.openexchange.resource.servlet.services.ResourceServletServiceRegistry.getServiceRegistry;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.SearchFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.requesthandler.AJAXRequestHandler;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.api2.OXConcurrentModificationException;
import com.openexchange.api2.OXConcurrentModificationException.ConcurrentModificationCode;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.resource.ResourceException;
import com.openexchange.resource.ResourceService;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxException;

/**
 * {@link ResourceRequest} - Executes a resource request
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class ResourceRequest implements AJAXRequestHandler {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ResourceRequest.class);

	/**
	 * Initializes a new {@link ResourceRequest}
	 * 
	 * @param session
	 *            The session providing needed user data
	 * @param ctx
	 *            The context
	 */
	public ResourceRequest() {
		super();
	}

	public AJAXRequestResult performAction(final String action, final JSONObject jsonObject, final Session session,
			final Context ctx) throws AbstractOXException, JSONException {
		if (action.equalsIgnoreCase(AJAXServlet.ACTION_LIST)) {
			return actionList(jsonObject, ctx);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_GET)) {
			return actionGet(jsonObject, ctx);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_SEARCH)) {
			return actionSearch(jsonObject, ctx);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_ALL)) {
			return actionAll(ctx);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_NEW)) {
			return actionNew(jsonObject, session, ctx);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATE)) {
			return actionUpdate(jsonObject, session, ctx);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_DELETE)) {
			return actionDelete(jsonObject, session, ctx);
		} else {
			throw new AjaxException(AjaxException.Code.UnknownAction, action);
		}
	}

	private AJAXRequestResult actionList(final JSONObject jsonObj, final Context ctx) throws AbstractOXException,
			JSONException {
		final ResourceService resourceService = getServiceRegistry().getService(ResourceService.class);
		if (null == resourceService) {
			throw new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE, ResourceService.class.getName());
		}

		final JSONArray jsonResponseArray = new JSONArray();

		UserStorage userStorage = null;

		final JSONArray jsonArray = DataParser.checkJSONArray(jsonObj, AJAXServlet.PARAMETER_DATA);
		final int len = jsonArray.length();
		final Date timestamp;
		if (len > 0) {
			long lastModified = Long.MIN_VALUE;
			for (int a = 0; a < len; a++) {
				final JSONObject jData = jsonArray.getJSONObject(a);
				final int id = DataParser.checkInt(jData, DataFields.ID);
				com.openexchange.resource.Resource r = null;

				try {
					r = resourceService.getResource(id, ctx);
				} catch (final ResourceException exc) {
					LOG.debug("resource not found try to find id in user table", exc);
				}

				if (r == null) {
					if (userStorage == null) {
						userStorage = UserStorage.getInstance();
					}

					final User u = userStorage.getUser(id, ctx);

					r = new com.openexchange.resource.Resource();
					r.setIdentifier(u.getId());
					r.setDisplayName(u.getDisplayName());
					r.setLastModified(new Date(0));
				}

				if (lastModified < r.getLastModified().getTime()) {
					lastModified = r.getLastModified().getTime();
				}

				jsonResponseArray.put(com.openexchange.resource.json.ResourceWriter.writeResource(r));
			}
			timestamp = new Date(lastModified);
		} else {
			timestamp = new Date(0);
		}

		return new AJAXRequestResult(jsonResponseArray, timestamp);
	}

	private AJAXRequestResult actionGet(final JSONObject jsonObj, final Context ctx) throws AbstractOXException,
			JSONException {
		final ResourceService resourceService = getServiceRegistry().getService(ResourceService.class);
		if (null == resourceService) {
			throw new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE, ResourceService.class.getName());
		}

		final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
		com.openexchange.resource.Resource r = null;
		try {
			r = resourceService.getResource(id, ctx);
		} catch (final ResourceException exc) {
			LOG.debug("resource not found try to find id in user table", exc);
		}

		if (r == null) {
			final User u = UserStorage.getInstance().getUser(id, ctx);

			r = new com.openexchange.resource.Resource();
			r.setIdentifier(u.getId());
			r.setDisplayName(u.getDisplayName());
			r.setLastModified(new Date(0));
		}

		return new AJAXRequestResult(com.openexchange.resource.json.ResourceWriter.writeResource(r), r
				.getLastModified());
	}

	private AJAXRequestResult actionSearch(final JSONObject jsonObj, final Context ctx) throws AbstractOXException,
			JSONException {
		final ResourceService resourceService = getServiceRegistry().getService(ResourceService.class);
		if (null == resourceService) {
			throw new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE, ResourceService.class.getName());
		}

		final JSONArray jsonResponseArray = new JSONArray();

		final String searchpattern;
		final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
		if (jData.has(SearchFields.PATTERN) && !jData.isNull(SearchFields.PATTERN)) {
			searchpattern = jData.getString(SearchFields.PATTERN);
		} else {
			if (LOG.isWarnEnabled()) {
				LOG.warn(new StringBuilder(64).append("Missing field \"").append(SearchFields.PATTERN).append(
						"\" in JSON data. Searching for all as fallback"));
			}
			return actionAll(ctx);
		}

		final com.openexchange.resource.Resource[] resources = resourceService.searchResources(searchpattern, ctx);
		final Date timestamp;
		if (resources.length > 0) {
			long lastModified = Long.MIN_VALUE;
			for (final com.openexchange.resource.Resource resource : resources) {
				if (lastModified < resource.getLastModified().getTime()) {
					lastModified = resource.getLastModified().getTime();
				}
				jsonResponseArray.put(com.openexchange.resource.json.ResourceWriter.writeResource(resource));
			}
			timestamp = new Date(lastModified);
		} else {
			timestamp = new Date(0);
		}

		return new AJAXRequestResult(jsonResponseArray, timestamp);
	}

	private static final String STR_ALL = "*";

	/**
	 * Performs an all request
	 * 
	 * @return A JSON array of all available resources' IDs
	 * @throws AbstractOXException
	 *             If all resources cannot be retrieved from resource storage
	 */
	private AJAXRequestResult actionAll(final Context ctx) throws AbstractOXException {
		final ResourceService resourceService = getServiceRegistry().getService(ResourceService.class);
		if (null == resourceService) {
			throw new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE, ResourceService.class.getName());
		}

		final JSONArray jsonResponseArray = new JSONArray();

		final com.openexchange.resource.Resource[] resources = resourceService.searchResources(STR_ALL, ctx);
		final Date timestamp;
		if (resources.length > 0) {
			long lastModified = Long.MIN_VALUE;
			for (final com.openexchange.resource.Resource resource : resources) {
				if (lastModified < resource.getLastModified().getTime()) {
					lastModified = resource.getLastModified().getTime();
				}
				jsonResponseArray.put(resource.getIdentifier());
			}
			timestamp = new Date(lastModified);
		} else {
			timestamp = new Date(0);
		}

		return new AJAXRequestResult(jsonResponseArray, timestamp);
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
		if (jsonObj.has(AJAXServlet.PARAMETER_TIMESTAMP)
				&& !jsonObj.isNull(AJAXServlet.PARAMETER_TIMESTAMP)
				&& jsonObj.getLong(AJAXServlet.PARAMETER_TIMESTAMP) < resourceService.getResource(
						resource.getIdentifier(), ctx).getLastModified().getTime()) {
			throw new OXConcurrentModificationException(EnumComponent.RESOURCE,
					ConcurrentModificationCode.CONCURRENT_MODIFICATION, new Object[0]);
		}
		/*
		 * Update resource
		 */
		resourceService.update(UserStorage.getStorageUser(session.getUserId(), ctx), ctx, resource);
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
		if (jsonObj.has(AJAXServlet.PARAMETER_TIMESTAMP)
				&& !jsonObj.isNull(AJAXServlet.PARAMETER_TIMESTAMP)
				&& jsonObj.getLong(AJAXServlet.PARAMETER_TIMESTAMP) < resourceService.getResource(
						resource.getIdentifier(), ctx).getLastModified().getTime()) {
			throw new OXConcurrentModificationException(EnumComponent.RESOURCE,
					ConcurrentModificationCode.CONCURRENT_MODIFICATION, new Object[0]);
		}
		/*
		 * Delete resource
		 */
		resourceService.delete(UserStorage.getStorageUser(session.getUserId(), ctx), ctx, resource);
		/*
		 * Write JSON null
		 */
		return new AJAXRequestResult(JSONObject.NULL);
	}

}
