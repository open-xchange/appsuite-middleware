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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.api2.LinkSQLInterface;
import com.openexchange.api2.RdbLinkSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.LinkObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link LinkRequest} - Handles request to link module.
 *
 * @author <a href="mailto:ben.pahne@open-xchange.com">Ben Pahne</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class LinkRequest {

	private static final String PARAMETER_MODULE = "module";

	private final Session session;

	private final User user;

	private final JSONWriter jsonWriter;

	private final Context ctx;

	/**
	 * Initializes a new {@link LinkRequest}
	 *
	 * @param session The session
	 * @param pw The (print) writer to write to
	 * @param ctx The context
	 * @throws OXException
	 */
	public LinkRequest(final Session session, final Writer pw, final Context ctx) throws OXException {
		this.session = session;
		this.jsonWriter = new JSONWriter(pw);
		this.ctx = ctx;
		user = UserStorage.getInstance().getUser(session.getUserId(), ctx);
	}

	/**
	 * Handles specified action.
	 *
	 * @param action The action
	 * @param jsonObject The JSON object containing request data
	 * @throws OXMandatoryFieldException If handling action fails due to missing mandatory field
	 * @throws OXException If handling action fails due to an OX server error
	 * @throws JSONException If handling action fails due to a JSON error
	 * @throws OXException If handling action fails due to an AJAX error
	 * @throws OXException If handling action fails due to a JSON error
	 */
	public void action(final String action, final JSONObject jsonObject) throws OXException, JSONException {
		if (action.equalsIgnoreCase(AJAXServlet.ACTION_ALL)) {
			actionAll(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_NEW)) {
			actionNew(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_DELETE)) {
			actionDelete(jsonObject);
		} else {
			throw AjaxExceptionCodes.UNKNOWN_ACTION.create( action);
		}
	}

	public void actionAll(final JSONObject jsonObj) throws JSONException, OXException,
			OXException, OXException {
		final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
		final int folder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_INFOLDER);
		final int type = DataParser.checkInt(jsonObj, PARAMETER_MODULE);

		final int user = this.user.getId();
		final int[] group = this.user.getGroups();

		final LinkSQLInterface linksql = new RdbLinkSQLInterface();

		final Set<LinkObject> availableLinks = new HashSet<LinkObject>();
		{
			LinkObject[] lo = linksql.getLinksOfObject(id, type, folder, user, group, session);
			if (lo != null && lo.length > 0) {
				availableLinks.addAll(Arrays.asList(lo));
			}
			// Try with object ID only
			lo = linksql.getLinksByObjectID(id, type, user, group, session);
			if (lo != null && lo.length > 0) {
				availableLinks.addAll(Arrays.asList(lo));
			}
		}

		if (availableLinks.isEmpty()) {
			// Immediate return
			jsonWriter.array();
			jsonWriter.endArray();
			return;
		}

		jsonWriter.array();
		try {
			final int size = availableLinks.size();
			final Iterator<LinkObject> iter = availableLinks.iterator();
			for (int i = 0; i < size; i++) {
				final LinkObject lol = iter.next();
				if (lol == null) {
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

	public void actionNew(final JSONObject jsonObj) throws JSONException, OXException, OXException {
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
		lo.setContext(ctx.getContextId());

		final LinkSQLInterface linksql = new RdbLinkSQLInterface();
		linksql.saveLink(lo,user,group,session);

		jsonWriter.object();
		jsonWriter.key(ResponseFields.DATA).value("");
		jsonWriter.endObject();

	}

	public void actionDelete(final JSONObject jsonObj) throws JSONException, OXException {
		final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
		final int folder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_INFOLDER);
		final int type = DataParser.checkInt(jsonObj, PARAMETER_MODULE);
		final int user = this.user.getId();
		final int[] group =	this.user.getGroups();
		final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);

		final JSONArray jo = jData.getJSONArray(ResponseFields.DATA);

		final int[][] del = new int[jo.length()][3];

		for (int i = 0; i < jo.length(); i++){
			final JSONArray dl = jo.getJSONArray(i);
			del[i][0] = dl.getInt(0);
			del[i][1] = dl.getInt(1);
			del[i][2] = dl.getInt(2);
		}
		final LinkSQLInterface linksql = new RdbLinkSQLInterface();
		final int[][] rep = linksql.deleteLinks(id,type,folder,del,user,group,session);

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
