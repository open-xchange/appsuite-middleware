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

package com.openexchange.mail.filter.ajax.actions;

import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mail.filter.ConfigTestHolder;
import com.openexchange.mail.filter.MailFilterConfig;
import com.openexchange.mail.filter.MailFilterService;
import com.openexchange.mail.filter.MailFilterSession;
import com.openexchange.mail.filter.Rule;
import com.openexchange.mail.filter.action.AbstractAction;
import com.openexchange.mail.filter.ajax.fields.RuleFields;
import com.openexchange.mail.filter.ajax.parser.MailFilterParser;
import com.openexchange.mail.filter.ajax.writer.MailFilterWriter;
import com.openexchange.mail.filter.comparison.AbstractComparison;
import com.openexchange.mail.filter.internal.MailFilterSessionImpl;
import com.openexchange.mail.filter.osgi.MailFilterServiceHolder;
import com.openexchange.session.Session;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;

/**
 * MailFilterRequest
 * 
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class MailFilterRequest {

	private Session sessionObj;

	private Context ctx;

	private Date timestamp;

	private TimeZone timeZone;

	private static final Log LOG = LogFactory.getLog(MailFilterRequest.class);

	public MailFilterRequest(Session sessionObj, Context ctx) {
		this.sessionObj = sessionObj;
		this.ctx = ctx;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public Object action(final String action, final JSONObject jsonObject)
			throws AbstractOXException {
		MailFilterService mailFilterService = null;
		try {
			mailFilterService = MailFilterServiceHolder.getInstance().getService();
			if (mailFilterService == null) {
				throw new AjaxException(AjaxException.Code.IOError, action);
			} else {
				if (action.equalsIgnoreCase(AJAXServlet.ACTION_NEW)) {
					return actionNew(jsonObject, mailFilterService);
				} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_DELETE)) {
					return actionDelete(jsonObject, mailFilterService);
				} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATE)) {
					return actionUpdate(jsonObject, mailFilterService);
				} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_ALL)) {
					return actionAll(jsonObject, mailFilterService);
				} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_LIST)) {
					return actionList(jsonObject, mailFilterService);
				} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_CONFIG)) {
					return actionConfig(jsonObject, mailFilterService);
				} else {
					throw new AjaxException(AjaxException.Code.UnknownAction, action);
				}
			}
		} finally {
			MailFilterServiceHolder.getInstance().ungetService(mailFilterService);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected JSONObject actionConfig(final JSONObject request,
			final MailFilterService mailFilterService) throws AbstractOXException {

		final MailFilterConfig mailFilterConfig = mailFilterService.getConfig();
		final ConfigTestHolder[] configTests = mailFilterConfig.getTests();
		final String[] abstractActions = mailFilterConfig.getActionCommands();

		final JSONObject jsonObj = new JSONObject();
		final JSONArray jsonTestArray = new JSONArray();
		final JSONArray jsonActionArray = new JSONArray();
		try {
			// tests			
			for (int a = 0; a < configTests.length; a++) {
				final JSONObject jsonTestObj = new JSONObject();
				jsonTestObj.put("test", configTests[a].getTest());
				
				final JSONArray jsonCompArray = new JSONArray();
				final String[] comparisons = configTests[a].getComparisons();
				for (int b = 0; b < comparisons.length; b++) {
					jsonCompArray.put(comparisons[a]);
				}
				
				jsonTestObj.put("comparison", jsonCompArray);
				
				jsonTestArray.put(jsonTestObj);
			}
			
			jsonObj.put("tests", jsonTestArray);
			
			// actions
			for (int a = 0; a < abstractActions.length; a++) {
				jsonActionArray.put(abstractActions[a]);
			}
			
			jsonObj.put("actioncommands", jsonActionArray);
		} catch (JSONException exc) {
			OXJSONException oxJsonException = new OXJSONException(
					OXJSONException.Code.JSON_BUILD_ERROR, exc);
			throw new AbstractOXException(oxJsonException);
		}

		return jsonObj;
	}

	/**
	 * {@inheritDoc}
	 */
	protected JSONObject actionDelete(final JSONObject request,
			final MailFilterService mailFilterService) throws AbstractOXException {
		String forUser;
		String id;
		try {
			forUser = DataParser.parseString(request, "for_user");
			id = DataParser.checkString(request, AJAXServlet.PARAMETER_ID);

			if (forUser == null) {
				forUser = sessionObj.getUserlogin();
			}
		} catch (JSONException exc) {
			OXJSONException oxJsonException = new OXJSONException(
					OXJSONException.Code.JSON_BUILD_ERROR, exc);
			throw new AbstractOXException(oxJsonException);
		}

		final MailFilterSession mailFilterSession = new MailFilterSessionImpl(sessionObj
				.getUserId(), sessionObj.getContextId(), sessionObj.getLoginName(), sessionObj
				.getPassword());
		mailFilterService.deleteRule(mailFilterSession, id);

		return new JSONObject();
	}

	protected JSONArray actionAll(JSONObject request, MailFilterService mailFilterService)
			throws AbstractOXException {
		String forUser;
		String flag;
		try {
			final String[] sColumns = DataParser
					.checkString(request, AJAXServlet.PARAMETER_COLUMNS).split(",");
			final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
			forUser = DataParser.parseString(request, "for_user");
			flag = DataParser.parseString(request, "flag");

			if (forUser == null) {
				forUser = sessionObj.getUserlogin();
			}

			final MailFilterSession mailFilterSession = new MailFilterSessionImpl(sessionObj
					.getUserId(), sessionObj.getContextId(), sessionObj.getLoginName(), sessionObj
					.getPassword());
			final Rule[] rules = mailFilterService.listRules(mailFilterSession, flag);
			final MailFilterWriter mailFilterWriter = new MailFilterWriter();
			final JSONArray jsonArray = new JSONArray();
			if (rules != null) {
				for (int a = 0; a < rules.length; a++) {
					final JSONArray jsonRuleArray = new JSONArray();
					mailFilterWriter.writeMailFilterAsArray(rules[a], jsonRuleArray, columns);
					jsonArray.put(jsonRuleArray);
				}
			}

			timestamp = new Date();

			return jsonArray;
		} catch (JSONException exc) {
			OXJSONException oxJsonException = new OXJSONException(
					OXJSONException.Code.JSON_BUILD_ERROR, exc);
			throw new AbstractOXException(oxJsonException);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected JSONArray actionList(final JSONObject request,
			final MailFilterService mailFilterService) throws AbstractOXException {
		String forUser;
		String flag;
		try {
			final String[] sColumns = DataParser
					.checkString(request, AJAXServlet.PARAMETER_COLUMNS).split(",");
			final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);

			forUser = DataParser.checkString(request, "for_user");
			flag = DataParser.parseString(request, "flag");

			if (forUser == null) {
				forUser = sessionObj.getUserlogin();
			}

			final MailFilterSession mailFilterSession = new MailFilterSessionImpl(sessionObj
					.getUserId(), sessionObj.getContextId(), sessionObj.getLoginName(), sessionObj
					.getPassword());
			final Rule[] rules = mailFilterService.listRules(mailFilterSession, flag);
			final MailFilterWriter mailFilterWriter = new MailFilterWriter();
			final JSONArray jsonArray = new JSONArray();
			if (rules != null) {
				for (int a = 0; a < rules.length; a++) {
					final JSONArray jsonRuleArray = new JSONArray();
					mailFilterWriter.writeMailFilterAsArray(rules[a], jsonRuleArray, columns);
					jsonArray.put(jsonRuleArray);
				}
			}

			timestamp = new Date();

			return jsonArray;
		} catch (JSONException exc) {
			OXJSONException oxJsonException = new OXJSONException(
					OXJSONException.Code.JSON_BUILD_ERROR, exc);
			throw new AbstractOXException(oxJsonException);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected JSONObject actionNew(final JSONObject request,
			final MailFilterService mailFilterService) throws AbstractOXException {
		String forUser;
		try {
			forUser = DataParser.parseString(request, "for_user");

			if (forUser == null) {
				forUser = sessionObj.getUserlogin();
			}

			final JSONObject jData = DataParser
					.checkJSONObject(request, AJAXServlet.PARAMETER_DATA);
			final com.openexchange.mail.filter.ajax.parser.MailFilterParser mailFilterParser = new MailFilterParser();
			final Rule rule = new Rule();
			mailFilterParser.parseMailFilter(rule, jData);

			final MailFilterSession mailFilterSession = new MailFilterSessionImpl(sessionObj
					.getUserId(), sessionObj.getContextId(), sessionObj.getLoginName(), sessionObj
					.getPassword());
			mailFilterService.addRule(mailFilterSession, rule);

			final JSONObject jsonObj = new JSONObject();
			jsonObj.put(RuleFields.ID, rule.getId());
			return jsonObj;
		} catch (JSONException exc) {
			OXJSONException oxJsonException = new OXJSONException(
					OXJSONException.Code.JSON_BUILD_ERROR, exc);
			throw new AbstractOXException(oxJsonException);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected JSONObject actionUpdate(final JSONObject request,
			final MailFilterService mailFilterService) throws AbstractOXException {
		String forUser;
		try {
			forUser = DataParser.parseString(request, "for_user");

			if (forUser == null) {
				forUser = sessionObj.getUserlogin();
			}

			final JSONObject jData = DataParser
					.checkJSONObject(request, AJAXServlet.PARAMETER_DATA);
			final MailFilterParser mailFilterParser = new MailFilterParser();
			final Rule rule = new Rule();
			mailFilterParser.parseMailFilter(rule, jData);

			final MailFilterSession mailFilterSession = new MailFilterSessionImpl(sessionObj
					.getUserId(), sessionObj.getContextId(), sessionObj.getLoginName(), sessionObj
					.getPassword());
			mailFilterService.editRule(mailFilterSession, rule);
			return new JSONObject();
		} catch (JSONException exc) {
			OXJSONException oxJsonException = new OXJSONException(
					OXJSONException.Code.JSON_BUILD_ERROR, exc);
			throw new AbstractOXException(oxJsonException);
		}
	}
}
