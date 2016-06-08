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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.ajax.mail.filter.api.writer.copy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.AbstractAction;
import com.openexchange.ajax.mail.filter.api.dao.test.AbstractTest;
import com.openexchange.ajax.mail.filter.api.fields.RuleFields;
import com.openexchange.ajax.mail.filter.api.writer.action.ActionWriter;
import com.openexchange.ajax.mail.filter.api.writer.action.ActionWriterFactory;
import com.openexchange.ajax.mail.filter.api.writer.test.TestWriter;
import com.openexchange.ajax.mail.filter.api.writer.test.TestWriterFactory;
import com.openexchange.ajax.writer.DataWriter;

/**
 * MailFilterWriter
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */

public class MailFilterWriter extends DataWriter {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailFilterWriter.class);

	public MailFilterWriter() {
		super(null, null);
	}

	public void writeMailFilter(final Rule rule, final JSONObject jsonObj) throws JSONException {
		if (rule.getId() > 0) {
			writeParameter(RuleFields.ID, rule.getId(), jsonObj);
		}

		writeParameter(RuleFields.RULENAME, rule.getName(), jsonObj);
		writeParameter(RuleFields.ACTIVE, rule.isActive(), jsonObj);

		final JSONArray flagsArray = getFlagsAsJSON(rule.getFlags());
		if (flagsArray != null) {
			jsonObj.put(RuleFields.FLAGS, flagsArray);
		}

		final JSONArray actionCommandObj = getActionCommandsAsJSON(rule.getActioncmds());
		if (actionCommandObj != null) {
			jsonObj.put(RuleFields.ACTIONCMDS, actionCommandObj);
		}

		final JSONObject testObj = getTestAsJSON(rule.getTest());
		if (testObj != null) {
			jsonObj.put(RuleFields.TEST, testObj);
		}
	}

	public void writeMailFilterAsArray(final Rule rule, final JSONArray jsonArray, final int[] cols) throws JSONException {
		for (int a = 0; a < cols.length; a++) {
			write(cols[a], rule, jsonArray);
		}
	}

	public void write(final int field, final Rule rule, final JSONArray jsonArray) throws JSONException {
		switch (field) {
			case Rule.ID:
				writeValue(rule.getId(), jsonArray);
				break;
			case Rule.RULENAME:
				writeValue(rule.getName(), jsonArray);
				break;
			case Rule.ACTIVE:
				writeValue(rule.isActive(), jsonArray);
				break;
			case Rule.POSITION:
				writeValue(rule.getPosition(), jsonArray);
				break;
			case Rule.FLAGS:
				final JSONArray flagsArray = getFlagsAsJSON(rule.getFlags());
				if (flagsArray != null) {
					jsonArray.put(flagsArray);
				} else {
					jsonArray.put(JSONObject.NULL);
				}
				break;
			case Rule.ACTIONCMDS:
				final JSONArray actionCommandObj = getActionCommandsAsJSON(rule.getActioncmds());
				if (actionCommandObj != null) {
					jsonArray.put(actionCommandObj);
				} else {
					jsonArray.put(JSONObject.NULL);
				}
				break;
			case Rule.TEST:
				final JSONObject testObj = getTestAsJSON(rule.getTest());
				if (testObj != null) {
					jsonArray.put(testObj);
				} else {
					jsonArray.put(JSONObject.NULL);
				}
				break;
			default:
				LOG.warn("missing field in mapping: {}", field);
		}
	}

	protected JSONArray getFlagsAsJSON(String[] flags) throws JSONException {
		if (flags != null) {
			final JSONArray jsonArray = new JSONArray();
			for (int a = 0; a < flags.length; a++) {
				jsonArray.put(flags[a]);
			}

			return jsonArray;
		}
		return null;
	}

	protected JSONArray getActionCommandsAsJSON(AbstractAction[] actioncmds)
			throws JSONException {
		if (actioncmds != null) {
			final JSONArray jsonArray = new JSONArray();

			for (int a = 0; a < actioncmds.length; a++) {
				final AbstractAction abstractAction = actioncmds[a];
				final String name = abstractAction.getName();
				final ActionWriter actionWriter = ActionWriterFactory.getWriter(name);
				final JSONObject jsonCommandObj = actionWriter.writeAction(name, abstractAction);
				jsonArray.put(jsonCommandObj);
			}

			return jsonArray;
		}

		return null;
	}

	protected JSONObject getTestAsJSON(AbstractTest abstractTest) throws JSONException {
		final String name = abstractTest.getName();
		final TestWriter testWriter = TestWriterFactory.getWriter(name);
		return testWriter.writeTest(name, abstractTest);
	}
}
