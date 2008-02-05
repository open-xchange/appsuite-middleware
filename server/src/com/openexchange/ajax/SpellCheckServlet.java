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
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.spellcheck.AJAXSpellCheck;
import com.openexchange.ajax.spellcheck.AJAXUserDictionaryException;
import com.openexchange.ajax.spellcheck.AJAXUserDictionaryException.DictionaryCode;
import com.openexchange.api.OXConflictException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.session.Session;

/**
 * SpellCheckServlet
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class SpellCheckServlet extends PermissionServlet {
	
	private static final long serialVersionUID = -8562058304257776778L;
	
	private static transient final Log LOG = LogFactory.getLog(SpellCheckServlet.class);

	public SpellCheckServlet() {
		super();
	}
	
	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
	throws ServletException, IOException {
		String action = null;
		try {
			action = getAction(req);
		} catch (OXConflictException ex) {
			resp.sendError(HttpServletResponse.SC_CONFLICT, "missing parameter: " + PARAMETER_ACTION);
			return;
		}
		final PrintWriter w = resp.getWriter();
		Session sessionObj = null;
		JSONWriter jw = null;
		boolean closeObject = false;
		try {
			if (action.equalsIgnoreCase("spellcheck")) {
				jw = new JSONWriter(w);
				sessionObj = getSessionObject(req);
				final String text = getBody(req);
				if (text == null || text.length() == 0) {
					throw new AJAXUserDictionaryException(DictionaryCode.MISSING_TEXT, new Object[0]);
				}
				jw.object();
				closeObject = true;
				jw.key(Response.DATA);
				final AJAXSpellCheck spellCheck = new AJAXSpellCheck(sessionObj, ContextStorage.getStorageContext(sessionObj.getContextId()));
				final JSONArray ja = spellCheck.getSpellCheckResultsAsJSONArray(text);
				jw.value(ja);
				jw.endObject();
				closeObject = false;
				w.flush();
				return;
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
			try {
				if (jw != null) {
					if (closeObject) {
						jw.value(JSONObject.NULL);
						jw.key(Response.ERROR);
						jw.value(e.getMessage());
						jw.endObject();
						w.flush();
						return;
					}
					jw.object();
					jw.key(Response.ERROR);
					jw.value(e.getMessage());
					jw.endObject();
					w.flush();
					return;
				}
			} catch (Exception exc) {
				LOG.error(exc.getMessage(), exc);
			}
		}
	}

	@Override
	protected boolean hasModulePermission(final Session session, final Context ctx) {
		return true;
	}

}
