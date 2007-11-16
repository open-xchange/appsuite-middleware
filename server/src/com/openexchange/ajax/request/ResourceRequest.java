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

package com.openexchange.ajax.request;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.ParticipantsFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.writer.ResourceWriter;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.ResourceStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.sessiond.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;

public class ResourceRequest {
	
	private Session sessionObj;
	
	private Date timestamp;
	
	private static final Log LOG = LogFactory.getLog(AppointmentRequest.class);
	
	public ResourceRequest(Session sessionObj) {
		this.sessionObj = sessionObj;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	public Object action(String action, JSONObject jsonObject) throws OXMandatoryFieldException, LdapException, JSONException, SearchIteratorException, AjaxException, OXJSONException {
		if (action.equalsIgnoreCase(AJAXServlet.ACTION_LIST)) {
			return actionList(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_GET)) {
			return actionGet(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_SEARCH)) {
			return actionSearch(jsonObject);
		} else {
			throw new AjaxException(AjaxException.Code.UnknownAction, action);
		}
	}
	
	public JSONArray actionList(JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, LdapException, OXJSONException, AjaxException {
		timestamp = new Date(0);
		
		Date lastModified = null;
		
		final JSONArray jsonResponseArray = new JSONArray();
		final JSONArray jsonArray = DataParser.checkJSONArray(jsonObj, "data");
		
		UserStorage userStorage = null;
		final ResourceStorage resourceStorage = ResourceStorage.getInstance();
		
		for (int a = 0; a < jsonArray.length(); a++) {
			JSONObject jData = jsonArray.getJSONObject(a);
			final int id = DataParser.checkInt(jData, DataFields.ID);
			com.openexchange.groupware.ldap.Resource r = null;
			
			try {
				r = resourceStorage.getResource(id, sessionObj.getContext());
			} catch (LdapException exc) {
				LOG.debug("resource not found try to find id in user table", exc);
			}
			
			if (r == null) {
				if (userStorage == null) {
					userStorage = UserStorage.getInstance();
				}
				
				final User u = userStorage.getUser(id, sessionObj.getContext());
				
				r = new com.openexchange.groupware.ldap.Resource();
				r.setIdentifier(u.getId());
				r.setDisplayName(u.getDisplayName());
				r.setLastModified(new Date(0));
			}
			
			final ResourceWriter resourceWriter = new ResourceWriter();
			final JSONObject jsonResourceObj = new JSONObject();
			resourceWriter.writeResource(r, jsonResourceObj);
			jsonResponseArray.put(jsonResourceObj);
			
			lastModified = r.getLastModified();
			
			if (timestamp.getTime() < lastModified.getTime()) {
				timestamp = lastModified;
			}
		}
		
		return jsonResponseArray;
	}
	
	public JSONObject actionGet(JSONObject jsonObj) throws LdapException, OXMandatoryFieldException, JSONException, OXJSONException, AjaxException {
		timestamp = new Date(0);
		
		final ResourceStorage resourceStorage = ResourceStorage.getInstance();
		final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
		final com.openexchange.groupware.ldap.Resource r = resourceStorage.getResource(id, sessionObj.getContext());
		
		final ResourceWriter resourceWriter = new ResourceWriter();
		final JSONObject jsonResourceObj = new JSONObject();
		resourceWriter.writeResource(r, jsonResourceObj);
		
		timestamp = r.getLastModified();
		
		return jsonResourceObj;
	}
	
	public JSONArray actionSearch(JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, SearchIteratorException, LdapException, AjaxException {
		timestamp = new Date(0);
		
		SearchIterator it = null;
		
		final JSONArray jsonResponseArray = new JSONArray();
		
		try {
			final JSONObject jData = DataParser.checkJSONObject(jsonObj, "data");
			String searchpattern = null;

			if (jData.has("pattern")) {
				searchpattern = jData.getString("pattern");
			}

			final ResourceStorage resourceStorage = ResourceStorage.getInstance();
			final com.openexchange.groupware.ldap.Resource[] resources = resourceStorage.searchResources(searchpattern,
					sessionObj.getContext());

			final ResourceWriter resourceWriter = new ResourceWriter();

			for (int a = 0; a < resources.length; a++) {
				final JSONObject jsonResourceObj = new JSONObject();
				resourceWriter.writeParameter(ParticipantsFields.DISPLAY_NAME, resources[a].getDisplayName(),
						jsonResourceObj);
				resourceWriter.writeParameter(ParticipantsFields.ID, resources[a].getIdentifier(), jsonResourceObj);
				jsonResponseArray.put(jsonResourceObj);
			}

			return jsonResponseArray;
		} finally {
			if (it != null) {
				it.close();
			}
		}
	}
}
