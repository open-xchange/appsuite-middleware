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

package com.openexchange.ajax.mail.filter.api.conversion.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.mail.filter.api.conversion.parser.action.ActionParser;
import com.openexchange.ajax.mail.filter.api.conversion.parser.action.ActionParserFactory;
import com.openexchange.ajax.mail.filter.api.conversion.parser.test.TestParser;
import com.openexchange.ajax.mail.filter.api.conversion.parser.test.TestParserFactory;
import com.openexchange.ajax.mail.filter.api.dao.ActionCommand;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.Action;
import com.openexchange.ajax.mail.filter.api.dao.test.AbstractTest;
import com.openexchange.ajax.mail.filter.api.fields.RuleFields;
import com.openexchange.ajax.parser.DataParser;

/**
 * {@link MailFilterParser}
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MailFilterParser extends DataParser {

    public MailFilterParser() {
        super();
    }

    /**
     * 
     * @param jsonObj
     * @param rule
     * @throws JSONException
     */
    public void parse(final JSONObject jsonObj, final Rule rule) throws JSONException {
        if (jsonObj.has(RuleFields.ID)) {
            rule.setId(jsonObj.getInt(RuleFields.ID));
        }

        if (jsonObj.has(RuleFields.RULENAME)) {
            rule.setName(jsonObj.getString(RuleFields.RULENAME));
        }

        if (jsonObj.has(RuleFields.POSITION)) {
            rule.setPosition(jsonObj.getInt(RuleFields.POSITION));
        }

        if (jsonObj.has(RuleFields.ACTIVE)) {
            rule.setActive(jsonObj.getBoolean(RuleFields.ACTIVE));
        }

        if (jsonObj.has(RuleFields.FLAGS)) {
            parseFlags(jsonObj.getJSONArray(RuleFields.FLAGS), rule);
        }

        if (jsonObj.has(RuleFields.ACTIONCMDS)) {
            parseActionCommand(jsonObj.getJSONArray(RuleFields.ACTIONCMDS), rule);
        }

        if (jsonObj.has(RuleFields.TEST)) {
            parseTest(jsonObj.getJSONObject(RuleFields.TEST), rule);
        }
    }

    /**
     * 
     * @param jsonFlagArray
     * @param rule
     * @throws JSONException
     */
    private void parseFlags(final JSONArray jsonFlagArray, final Rule rule) throws JSONException {
        String[] flags = new String[jsonFlagArray.length()];
        for (int a = 0; a < jsonFlagArray.length(); a++) {
            flags[a] = jsonFlagArray.getString(a);
        }

        rule.setFlags(flags);
    }

    /**
     * 
     * @param jsonActionArray
     * @param rule
     * @throws JSONException
     */
    private void parseActionCommand(final JSONArray jsonActionArray, final Rule rule) throws JSONException {
        final Action[] abstractActionArray = new Action[jsonActionArray.length()];
        for (int a = 0; a < jsonActionArray.length(); a++) {
            final JSONObject actionCommandObj = jsonActionArray.getJSONObject(a);
            final String actionId = actionCommandObj.getString("id");
            ActionCommand actionCommand = ActionCommand.valueOf(actionId.toUpperCase());
            final ActionParser actionParser = ActionParserFactory.getWriter(actionCommand);
            abstractActionArray[a] = actionParser.parse(actionCommandObj);
        }

        rule.setActionCommands(abstractActionArray);
    }

    /**
     * 
     * @param jsonObj
     * @param rule
     * @throws JSONException
     */
    private void parseTest(final JSONObject jsonObj, final Rule rule) throws JSONException {
        final String testId = jsonObj.getString("id");
        final TestParser testParser = TestParserFactory.getParser(testId);
        final AbstractTest abstractTest = testParser.parseTest(testId, jsonObj);
        rule.setTest(abstractTest);
    }
}
