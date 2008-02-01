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

package com.openexchange.spellcheck.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.PermissionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.session.Session;
import com.openexchange.spellcheck.SpellCheck;
import com.openexchange.spellcheck.SpellCheckError;
import com.openexchange.spellcheck.SpellCheckException;
import com.openexchange.spellcheck.internal.SpellCheckImpl;
import com.openexchange.spellcheck.internal.SpellCheckUtility;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link SpellCheckServlet}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class SpellCheckServlet extends PermissionServlet {

	private static final String PARAM_LANG = "lang";

	private static final String ACTION_CHECK = "check";

	private static final String ACTION_SUGGESTIONS = "suggestions";

	private static final String ACTION_ADD = "add";

	private static final String ACTION_REMOVE = "remove";

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(SpellCheckServlet.class);

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 449494568630871599L;

	/**
	 * Initializes a new {@link SpellCheckServlet}
	 */
	public SpellCheckServlet() {
		super();
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
			IOException {
		resp.setContentType(CONTENTTYPE_JAVASCRIPT);
		/*
		 * The magic spell to disable caching
		 */
		Tools.disableCaching(resp);
		try {
			actionGet(req, resp);
		} catch (final AbstractOXException e) {
			LOG.error("doGet", e);
			final Response response = new Response();
			response.setException(e);
			final PrintWriter writer = resp.getWriter();
			try {
				Response.write(response, writer);
			} catch (final JSONException e1) {
				throw new ServletException(e1);
			}
			writer.flush();
		} catch (final JSONException e) {
			LOG.error("doGet", e);
			final Response response = new Response();
			response.setException(new SpellCheckException(SpellCheckException.Code.JSON_ERROR, e, e
					.getLocalizedMessage()));
			final PrintWriter writer = resp.getWriter();
			try {
				Response.write(response, writer);
			} catch (final JSONException e1) {
				throw new ServletException(e1);
			}
			writer.flush();
		}
	}

	@Override
	protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
			IOException {
		resp.setContentType(CONTENTTYPE_JAVASCRIPT);
		/*
		 * The magic spell to disable caching
		 */
		Tools.disableCaching(resp);
		try {
			actionPut(req, resp);
		} catch (final AbstractOXException e) {
			LOG.error("doPut", e);
			final Response response = new Response();
			response.setException(e);
			final PrintWriter writer = resp.getWriter();
			try {
				Response.write(response, writer);
			} catch (final JSONException e1) {
				throw new ServletException(e1);
			}
			writer.flush();
		} catch (final JSONException e) {
			LOG.error("doPut", e);
			final Response response = new Response();
			response.setException(new SpellCheckException(SpellCheckException.Code.JSON_ERROR, e, e
					.getLocalizedMessage()));
			final PrintWriter writer = resp.getWriter();
			try {
				Response.write(response, writer);
			} catch (final JSONException e1) {
				throw new ServletException(e1);
			}
			writer.flush();
		}
	}

	private void actionPut(final HttpServletRequest req, final HttpServletResponse resp) throws SpellCheckException,
			JSONException, IOException {
		final String actionStr = checkStringParam(req, PARAMETER_ACTION);
		if (actionStr.equalsIgnoreCase(ACTION_CHECK)) {
			actionPutCheck(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_SUGGESTIONS)) {
			actionPutSuggestions(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_ADD)) {
			actionPutAdd(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_REMOVE)) {
			actionPutRemove(req, resp);
		} else {
			throw new SpellCheckException(SpellCheckException.Code.UNSUPPORTED_PARAM, PARAMETER_ACTION, actionStr);
		}
	}

	private void actionGet(final HttpServletRequest req, final HttpServletResponse resp) throws SpellCheckException,
			JSONException, IOException {
		final String actionStr = checkStringParam(req, PARAMETER_ACTION);
		if (actionStr.equalsIgnoreCase(ACTION_LIST)) {
			actionGetList(req, resp);
		} else {
			throw new SpellCheckException(SpellCheckException.Code.UNSUPPORTED_PARAM, PARAMETER_ACTION, actionStr);
		}
	}

	private void actionGetList(final HttpServletRequest req, final HttpServletResponse resp) throws JSONException,
			IOException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final OXJSONWriter jsonWriter = new OXJSONWriter();
		final Session session = getSessionObject(req);
		/*
		 * Start response
		 */
		jsonWriter.array();
		try {
			final List<String> userWords = SpellCheckImpl.newSpellCheck(session.getUserId(), session.getContext())
					.getUserWords();
			for (final String userWord : userWords) {
				jsonWriter.value(userWord);
			}
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} finally {
			jsonWriter.endArray();
		}
		/*
		 * Close response and flush print writer
		 */
		response.setData(jsonWriter.getObject());
		response.setTimestamp(null);
		Response.write(response, resp.getWriter());
	}

	private void actionPutCheck(final HttpServletRequest req, final HttpServletResponse resp) throws JSONException,
			IOException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final OXJSONWriter jsonWriter = new OXJSONWriter();
		final Session session = getSessionObject(req);
		/*
		 * Start response
		 */
		jsonWriter.array();
		try {
			/*
			 * Read in parameter(s)
			 */
			final Locale locale = SpellCheckUtility.parseLocaleString(checkStringParam(req, PARAM_LANG));
			/*
			 * Do the spell check on request's body data (which is supposed to
			 * be html content)
			 */
			final Set<String> misspeltWords;
			{
				final SpellCheck spellCheck = SpellCheckImpl.newSpellCheck(session.getUserId(), locale, session
						.getContext());
				final SpellCheckError[] errors = spellCheck
						.checkSpelling(SpellCheckUtility.html2Document(getBody(req)));
				misspeltWords = new HashSet<String>(errors.length);
				for (final SpellCheckError error : errors) {
					misspeltWords.add(error.getInvalidWord());
				}
			}
			for (final String misspeltWord : misspeltWords) {
				jsonWriter.value(misspeltWord);
			}
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} finally {
			jsonWriter.endArray();
		}
		/*
		 * Close response and flush print writer
		 */
		response.setData(jsonWriter.getObject());
		response.setTimestamp(null);
		Response.write(response, resp.getWriter());
	}

	private void actionPutSuggestions(final HttpServletRequest req, final HttpServletResponse resp)
			throws JSONException, IOException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final OXJSONWriter jsonWriter = new OXJSONWriter();
		final Session session = getSessionObject(req);
		/*
		 * Start response
		 */
		jsonWriter.array();
		try {
			/*
			 * Read in parameter(s)
			 */
			final Locale locale = SpellCheckUtility.parseLocaleString(checkStringParam(req, PARAM_LANG));
			/*
			 * Determine suggestions
			 */
			final List<String> suggestions;
			{
				final SpellCheck spellCheck = SpellCheckImpl.newSpellCheck(session.getUserId(), locale, session
						.getContext());
				suggestions = spellCheck.getSuggestions(getBody(req), 0);
			}
			for (final String suggestion : suggestions) {
				jsonWriter.value(suggestion);
			}
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} finally {
			jsonWriter.endArray();
		}
		/*
		 * Close response and flush print writer
		 */
		response.setData(jsonWriter.getObject());
		response.setTimestamp(null);
		Response.write(response, resp.getWriter());
	}

	private void actionPutAdd(final HttpServletRequest req, final HttpServletResponse resp) throws JSONException,
			IOException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final Session session = getSessionObject(req);
		try {
			final SpellCheck spellCheck = SpellCheckImpl.newSpellCheck(session.getUserId(), session.getContext());
			spellCheck.addWord(getBody(req));
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		}
		/*
		 * Close response and flush print writer
		 */
		response.setData(JSONObject.NULL);
		response.setTimestamp(null);
		Response.write(response, resp.getWriter());
	}

	private void actionPutRemove(final HttpServletRequest req, final HttpServletResponse resp) throws JSONException,
			IOException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final Session session = getSessionObject(req);
		try {
			final SpellCheck spellCheck = SpellCheckImpl.newSpellCheck(session.getUserId(), session.getContext());
			spellCheck.removeWord(getBody(req));
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		}
		/*
		 * Close response and flush print writer
		 */
		response.setData(JSONObject.NULL);
		response.setTimestamp(null);
		Response.write(response, resp.getWriter());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.ajax.PermissionServlet#hasModulePermission(com.openexchange.session.Session)
	 */
	@Override
	protected boolean hasModulePermission(final Session sessionObj) {
		return true;
	}

	private static String checkStringParam(final HttpServletRequest req, final String paramName)
			throws SpellCheckException {
		final String paramVal = req.getParameter(paramName);
		if (paramVal == null || paramVal.length() == 0 || "null".equals(paramVal)) {
			throw new SpellCheckException(SpellCheckException.Code.MISSING_PARAM, paramName);
		}
		return paramVal;
	}

	/**
	 * 
	 * <pre>
	 * private static String optStringParam(final HttpServletRequest req, final String paramName) throws SpellCheckException {
	 * 	final String paramVal = req.getParameter(paramName);
	 * 	if (paramVal == null || paramVal.length() == 0 || &quot;null&quot;.equals(paramVal)) {
	 * 		return null;
	 * 	}
	 * 	return paramVal;
	 * }
	 * </pre>
	 */
}
