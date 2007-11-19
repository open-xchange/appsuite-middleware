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

import static com.openexchange.ajax.container.Response.DATA;

import java.io.Writer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.api2.LinkSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbLinkSQLInterface;
import com.openexchange.groupware.container.LinkObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;

public class LinkRequest {
	
	private static final String PARAMETER_MODULE = "module";
	
	private Session sessionObj;
	
	private final User user;
	
	final JSONWriter jsonWriter;

	public LinkRequest(final Session sessionObj, final Writer pw) {
		this.sessionObj = sessionObj;
		this.jsonWriter = new JSONWriter(pw);
		user = UserStorage.getStorageUser(sessionObj.getUserId(), sessionObj.getContext());
	}
	
	public void action(final String action, final JSONObject jsonObject) throws OXMandatoryFieldException, OXException, JSONException, AjaxException, OXJSONException {
		if (action.equalsIgnoreCase(AJAXServlet.ACTION_ALL)) {
			actionAll(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_NEW)) {
			actionNew(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_DELETE)) {
			actionDelete(jsonObject);
		} else {
			throw new AjaxException(AjaxException.Code.UnknownAction, action);
		}
	}
	
	public void actionAll(final JSONObject jsonObj) throws JSONException, OXMandatoryFieldException, OXException, OXJSONException, AjaxException {
		final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
		final int folder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_INFOLDER);
		final int type = DataParser.checkInt(jsonObj, PARAMETER_MODULE);

		final int user = this.user.getId();
		final int[] group =	this.user.getGroups();
		
		final LinkSQLInterface linksql = new RdbLinkSQLInterface();
		
		final LinkObject[] lo = linksql.getLinksOfObject(id,type,folder,user,group,sessionObj);
		
		if (lo == null) {
			//throw new NullPointerException("LINKOBJECT IS NULL");
			jsonWriter.array();
			jsonWriter.endArray();
		} else {
			jsonWriter.array();
			try {
				for (int i = 0;i < lo.length;i++){
					final LinkObject lol = lo[i];

					if (lol == null){
						continue;
					}
					jsonWriter.object();
					jsonWriter.key("id1").value(lol.getFirstId());
					jsonWriter.key("module1").value(lol.getFirstType());
					jsonWriter.key("folder1").value(lol.getFirstFolder());
					jsonWriter.key("id2").value(lol.getSecondId());
					jsonWriter.key("module2").value(lol.getSecondType());
					jsonWriter.key("folder2").value(lol.getSecondFolder());
					jsonWriter.endObject();
				}
			} finally {
				jsonWriter.endArray();				
			}
		}
	}

	public void actionNew(final JSONObject jsonObj) throws JSONException, OXException, AjaxException {
		final LinkObject lo = new LinkObject();
		final int user = this.user.getId();
		final int[] group =	this.user.getGroups();
		final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);

		
		if (jData.has("id1")) {
			lo.setFirstId(jData.getInt("id1"));
		}
		if (jData.has("module1")) {
			lo.setFirstType(jData.getInt("module1"));
		}
		if (jData.has("folder1")) {
			lo.setFirstFolder(jData.getInt("folder1"));
		}
		if (jData.has("id2")) {
			lo.setSecondId(jData.getInt("id2"));
		}
		if (jData.has("module2")) {
			lo.setSecondType(jData.getInt("module2"));
		}
		if (jData.has("folder2")) {
			lo.setSecondFolder(jData.getInt("folder2"));
		}
		lo.setContext(sessionObj.getContext().getContextId());
		
		final LinkSQLInterface linksql = new RdbLinkSQLInterface();
		linksql.saveLink(lo,user,group,sessionObj);
		
		jsonWriter.object();
		jsonWriter.key(DATA).value("");
		jsonWriter.endObject();

	}

	public void actionDelete(final JSONObject jsonObj) throws JSONException, OXMandatoryFieldException, OXException, OXJSONException, AjaxException {
		final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
		final int folder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_INFOLDER);
		final int type = DataParser.checkInt(jsonObj, PARAMETER_MODULE);
		final int user = this.user.getId();
		final int[] group =	this.user.getGroups();
		final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
		
		final JSONArray jo = jData.getJSONArray(DATA);
		
		final int[][] del = new int[jo.length()][3];
		
		for (int i = 0; i < jo.length(); i++){
			final JSONArray dl = jo.getJSONArray(i);
			del[i][0] = dl.getInt(0);
			del[i][1] = dl.getInt(1);
			del[i][2] = dl.getInt(2);
		}
		final LinkSQLInterface linksql = new RdbLinkSQLInterface();
		final int[][] rep = linksql.deleteLinks(id,type,folder,del,user,group,sessionObj);
		
		jsonWriter.array();

		final JSONArray jo2 = new JSONArray();
		for (int i=0; i<rep.length;i++) {
			jo2.put(0,rep[i][0]);
			jo2.put(1,rep[i][1]);
			jo2.put(2,rep[i][2]);
			jsonWriter.value(jo2);
			jo2.reset();
		}
		
		jsonWriter.endArray();
	}
}
