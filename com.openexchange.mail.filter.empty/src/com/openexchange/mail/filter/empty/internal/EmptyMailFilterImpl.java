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

package com.openexchange.mail.filter.empty.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.openexchange.mail.filter.MailFilterConfig;
import com.openexchange.mail.filter.MailFilterService;
import com.openexchange.mail.filter.MailFilterSession;
import com.openexchange.mail.filter.Rule;
import com.openexchange.mail.filter.ajax.parser.MailFilterParser;
import com.openexchange.mail.filter.ajax.writer.MailFilterWriter;

/**
 * JSONFileMailFilterImpl
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class EmptyMailFilterImpl implements MailFilterService {

	private static transient final Log LOG = LogFactory.getLog(EmptyMailFilterImpl.class);

	protected String scriptDirectory = null;

	public EmptyMailFilterImpl() {
		scriptDirectory = System.getProperty("JSONFileScriptDir");
		if (scriptDirectory == null) {
			LOG.error("missing property: JSONFileScriptDir");
		} else {
			LOG.info("script dir: " + scriptDirectory);
		}
		if (!scriptDirectory.endsWith("/")) {
			scriptDirectory += "/";
		}
	}

	public MailFilterConfig getConfig() {
		return new MailFilterConfigImpl();
	}

	public void addRule(MailFilterSession session, Rule rule) {
		final List<Rule> ruleList = new ArrayList<Rule>();
		Rule[] rules = loadRules(session.getLoginName(), null);
		if (rules != null) {
			for (int a = 0; a < rules.length; a++) {
				ruleList.add(rules[a]);
			}
		}

		if (rule.getId() == null) {
			rule.setId("RuleID" + String.valueOf(System.currentTimeMillis()));
		}
		ruleList.add(rule.getPosition(), rule);

		saveRules(ruleList.toArray(new Rule[ruleList.size()]), session.getLoginName());
	}

	public void editRule(MailFilterSession session, Rule rule) {
		final List<Rule> ruleList = new ArrayList<Rule>();
		Rule[] rules = loadRules(session.getLoginName(), null);
		for (int a = 0; a < rules.length; a++) {
			if (rules[a].getId().equals(rule.getId())) {
				ruleList.add(rule);
			} else {
				ruleList.add(rules[a]);
			}
		}

		saveRules(ruleList.toArray(new Rule[ruleList.size()]), session.getLoginName());
	}

	public void deleteRule(MailFilterSession session, String id) {
		final List<Rule> ruleList = new ArrayList<Rule>();
		Rule[] rules = loadRules(session.getLoginName(), null);
		if (rules != null) {
			for (int a = 0; a < rules.length; a++) {
				if (!rules[a].getId().equals(id)) {
					ruleList.add(rules[a]);
				}
			}
		}

		saveRules(ruleList.toArray(new Rule[ruleList.size()]), session.getLoginName());
	}

	public Rule[] listRules(MailFilterSession session, String flag) {
		return loadRules(session.getLoginName(), flag);
	}

	private void saveRules(Rule[] rules, String forUser) {
		final MailFilterWriter mailFilterWriter = new MailFilterWriter();
		try {
			final JSONArray jsonArray = new JSONArray();
			for (int a = 0; a < rules.length; a++) {
				final JSONObject jsonObj = new JSONObject();
				mailFilterWriter.writeMailFilter(rules[a], jsonObj);
				jsonArray.put(jsonObj);
			}

			final File file = new File(scriptDirectory + forUser);
			final FileOutputStream fileOutputStream = new FileOutputStream(file);
			byte data[] = jsonArray.toString().getBytes("UTF-8");
			fileOutputStream.write(data);
			fileOutputStream.flush();
			fileOutputStream.close();
		} catch (Exception exc) {
			LOG.error("saveRules: " + exc);
		}
	}

	private Rule[] loadRules(String forUser, String flag) {
		final MailFilterParser mailFilterParser = new MailFilterParser();
		try {
			File file = new File(scriptDirectory + forUser);
			byte data[] = new byte[(int) file.length()];
			FileInputStream fileInputStream = new FileInputStream(file);
			fileInputStream.read(data);

			JSONArray jsonArray = new JSONArray(new String(data));
			final List<Rule> ruleList = new ArrayList<Rule>();
			for (int a = 0; a < jsonArray.length(); a++) {
				final JSONObject jsonObj = jsonArray.getJSONObject(a);
				final Rule rule = new Rule();
				mailFilterParser.parseMailFilter(rule, jsonObj);

				if (flag != null) {
					if (containsFlag(rule, flag)) {
						ruleList.add(rule);
					}
				} else {
					ruleList.add(rule);
				}
			}

			fileInputStream.close();

			return ruleList.toArray(new Rule[ruleList.size()]);
		} catch (Exception exc) {
			LOG.error("loadRules: " + exc);
		}
		return null;
	}

	private boolean containsFlag(Rule rule, String flag) {
		final String[] flags = rule.getFlags();
		for (int a = 0; a < flags.length; a++) {
			if (flags[a].equals(flag)) {
				return true;
			}
		}
		return false;
	}
}
