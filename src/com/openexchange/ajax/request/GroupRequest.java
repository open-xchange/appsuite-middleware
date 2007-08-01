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

import com.openexchange.tools.servlet.OXJSONException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.ParticipantsFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.writer.GroupWriter;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.groupware.ldap.GroupStorage;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.servlet.AjaxException;

public class GroupRequest {
	
	private SessionObject sessionObj;
	
	private JSONWriter jsonWriter;
	
	private Date timestamp;
	
	public GroupRequest(SessionObject sessionObj, JSONWriter w) {
		this.sessionObj = sessionObj;
		this.jsonWriter = w;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void action(final String action, final JSONObject jsonObject) throws OXMandatoryFieldException, LdapException, JSONException, SearchIteratorException, AjaxException, OXJSONException {
		if (action.equalsIgnoreCase(AJAXServlet.ACTION_LIST)) {
			actionList(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_GET)) {
			actionGet(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_SEARCH)) {
			actionSearch(jsonObject);
		} else {
			throw new AjaxException(AjaxException.Code.UnknownAction, action);
		}
	}
	
	public void actionList(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, LdapException, OXJSONException {
		final JSONArray jsonArray = DataParser.checkJSONArray(jsonObj, "data");
		
		timestamp = new Date(0);
		
		Date lastModified = null;
		
		jsonWriter.array();
		try {
			final GroupStorage groupStorage = GroupStorage.getInstance(sessionObj.getContext(), true);
			
			for (int a = 0; a < jsonArray.length(); a++) {
				final JSONObject jData = jsonArray.getJSONObject(a);
				final com.openexchange.groupware.ldap.Group g = groupStorage.getGroup(DataParser.checkInt(jData, DataFields.ID));
			
				final GroupWriter groupWriter = new GroupWriter(jsonWriter);
				groupWriter.writeGroup(g);
			
				lastModified = g.getLastModified();
				
				if (timestamp.getTime() < lastModified.getTime()) {
					timestamp = lastModified;
				}
			} 
		} finally {
			jsonWriter.endArray();			
		}
	}

	public void actionGet(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, LdapException, OXJSONException {
		final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
		
		timestamp = new Date(0);
		
		final GroupStorage groupStorage = GroupStorage.getInstance(sessionObj.getContext(), true);
		final com.openexchange.groupware.ldap.Group g = groupStorage.getGroup(id);
		
		final GroupWriter groupWriter = new GroupWriter(jsonWriter);
		groupWriter.writeGroup(g);
		
		timestamp = g.getLastModified();
		
	}
	
	public void actionSearch(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, LdapException, SearchIteratorException {
		final JSONObject jData = DataParser.checkJSONObject(jsonObj, "data");
		
		String searchpattern = null;
		
		if (jData.has("pattern")) {
			searchpattern = DataParser.parseString(jData, "pattern");
		}
		
		timestamp = new Date(0);
		
		jsonWriter.array();
		final SearchIterator it = null;
		
		try {
			final GroupStorage groupStorage = GroupStorage.getInstance(sessionObj.getContext(), true);
			com.openexchange.groupware.ldap.Group[] groups = null;
			
			if ("*".equals(searchpattern)) {
                groups = groupStorage.getGroups();
			} else {
                groups = groupStorage.searchGroups(searchpattern);
			}
			
			final GroupWriter groupWriter = new GroupWriter(jsonWriter);
			
			for (int a = 0; a < groups.length; a++) {
				jsonWriter.object();
				groupWriter.writeParameter(ParticipantsFields.DISPLAY_NAME, groups[a].getDisplayName());
				groupWriter.writeParameter(ParticipantsFields.ID, groups[a].getIdentifier());
				jsonWriter.endObject();
				if (groups[a].getLastModified().after(timestamp)) {
					timestamp = groups[a].getLastModified();
				}
			}
		} finally {
			if (it != null) {
				it.close();
			}
			jsonWriter.endArray();
		}
		
	}
}
