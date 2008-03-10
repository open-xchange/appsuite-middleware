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

package com.openexchange.mail.filter.ajax.parser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.parser.DataParser;
import com.openexchange.mail.filter.Rule;
import com.openexchange.mail.filter.action.AbstractAction;
import com.openexchange.mail.filter.ajax.fields.RuleFields;
import com.openexchange.mail.filter.ajax.parser.action.ActionParser;
import com.openexchange.mail.filter.ajax.parser.action.ActionParserFactory;
import com.openexchange.mail.filter.ajax.parser.test.TestParser;
import com.openexchange.mail.filter.ajax.parser.test.TestParserFactory;
import com.openexchange.mail.filter.test.AbstractTest;

/**
 * MailFilterParser
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */

public class MailFilterParser extends DataParser {

	private static final Log LOG = LogFactory.getLog(MailFilterParser.class);

	public MailFilterParser() {

	}

	public void parseMailFilter(final Rule rule, final JSONObject jsonObj) throws JSONException {
		rule.setId(parseString(jsonObj, RuleFields.ID));
		rule.setName(parseString(jsonObj, RuleFields.RULENAME));
		rule.setActive(parseBoolean(jsonObj, RuleFields.ACTIVE));

		if (jsonObj.has(RuleFields.FLAGS)) {
			final JSONArray flagsArray = jsonObj.getJSONArray(RuleFields.FLAGS);
			parseFlags(flagsArray, rule);
		}

		final JSONArray actionCommandArray = jsonObj.getJSONArray(RuleFields.ACTIONCMDS);
		parseActionCommand(actionCommandArray, rule);

		final JSONObject testObj = jsonObj.getJSONObject("test");
		parseTest(testObj, rule);
	}

	public static void parseFlags(final JSONArray jsonFlagArray, final Rule rule)
			throws JSONException {
		String[] flags = new String[jsonFlagArray.length()];
		for (int a = 0; a < jsonFlagArray.length(); a++) {
			flags[a] = jsonFlagArray.getString(a);
		}

		rule.setFlags(flags);
	}

	public static void parseActionCommand(final JSONArray jsonActionArray, final Rule rule)
			throws JSONException {
		final AbstractAction[] abstractActionArray = new AbstractAction[jsonActionArray.length()];
		for (int a = 0; a < jsonActionArray.length(); a++) {
			final JSONObject actionCommandObj = jsonActionArray.getJSONObject(a);
			final String actionname = actionCommandObj.getString("name");
			final ActionParser actionParser = ActionParserFactory.getWriter(actionname);
			abstractActionArray[a] = actionParser.parseAction(actionname, actionCommandObj);
		}

		rule.setActioncmds(abstractActionArray);
	}
	
	public static void parseTest(final JSONObject jsonObj, final Rule rule) throws JSONException {
		final String testname = jsonObj.getString("name");
		final TestParser testParser = TestParserFactory.getParser(testname);
		final AbstractTest abstractTest = testParser.parseTest(testname, jsonObj);
		rule.setTest(abstractTest);
	}
}
