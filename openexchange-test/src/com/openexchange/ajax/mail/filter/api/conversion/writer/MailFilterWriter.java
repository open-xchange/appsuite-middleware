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

package com.openexchange.ajax.mail.filter.api.conversion.writer;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.mail.filter.api.conversion.writer.action.ActionWriter;
import com.openexchange.ajax.mail.filter.api.conversion.writer.action.ActionWriterFactory;
import com.openexchange.ajax.mail.filter.api.conversion.writer.test.TestWriter;
import com.openexchange.ajax.mail.filter.api.conversion.writer.test.TestWriterFactory;
import com.openexchange.ajax.mail.filter.api.dao.ActionCommand;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.Action;
import com.openexchange.ajax.mail.filter.api.dao.action.argument.ActionArgument;
import com.openexchange.ajax.mail.filter.api.dao.test.AbstractTest;
import com.openexchange.ajax.mail.filter.api.fields.RuleFields;
import com.openexchange.ajax.writer.DataWriter;

/**
 * {@link MailFilterWriter}
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MailFilterWriter extends DataWriter {

    /**
     * Initialises a new {@link MailFilterWriter}.
     */
    public MailFilterWriter() {
        super(null, null);
    }

    /**
     * Writes the specified {@link Rule} to the specified {@link JSONObject}
     * 
     * @param rule The {@link Rule} to write
     * @param jsonObject The {@link JSONObject} to write it to
     * @throws JSONException if a JSON error occurs
     */
    public void writeMailFilter(final Rule rule, final JSONObject jsonObject) throws JSONException {
        if (rule.getId() >= 0) {
            writeParameter(RuleFields.ID, rule.getId(), jsonObject);
        }
        
        if (rule.getPosition() >=0) {
            writeParameter("position", rule.getPosition(), jsonObject);
        }

        writeParameter(RuleFields.RULENAME, rule.getName(), jsonObject);
        writeParameter(RuleFields.ACTIVE, rule.isActive(), jsonObject);

        final JSONArray flagsArray = getFlagsAsJSON(rule.getFlags());
        if (flagsArray != null) {
            jsonObject.put(RuleFields.FLAGS, flagsArray);
        }

        final JSONArray actionCommandsArray = getActionCommandsAsJSON(rule.getActions());
        if (actionCommandsArray != null && !actionCommandsArray.isEmpty()) {
            jsonObject.put(RuleFields.ACTIONCMDS, actionCommandsArray);
        }

        final JSONObject testObj = getTestCommandAsJSON(rule.getTest());
        if (testObj != null) {
            jsonObject.put(RuleFields.TEST, testObj);
        }
    }

    /**
     * Writes the specified flags array as a {@link JSONArray}
     * 
     * @param flags The flags array
     * @return a {@link JSONArray}
     * @throws JSONException if a JSON error occurs
     */
    private JSONArray getFlagsAsJSON(String[] flags) throws JSONException {
        if (flags != null) {
            final JSONArray jsonArray = new JSONArray();
            for (int a = 0; a < flags.length; a++) {
                jsonArray.put(flags[a]);
            }

            return jsonArray;
        }
        return null;
    }

    /**
     * Writes the specified {@link Action}s as a {@link JSONArray}. If the actions array is null
     * an empty {@link JSONArray} is returned.
     * 
     * @param actions The array with the {@link Action}s to write
     * @return A {@link JSONArray} with the {@link Action}s as {@link JSONObject}s; or an empty array
     * @throws JSONException if a JSON error occurs
     */
    private JSONArray getActionCommandsAsJSON(List<Action<? extends ActionArgument>> actions) throws JSONException {
        if (actions == null) {
            return new JSONArray();
        }

        final JSONArray jsonArray = new JSONArray();

        for (Action<? extends ActionArgument> action : actions) {
            final String name = action.getActionCommand().name();
            ActionCommand actionCommand = ActionCommand.valueOf(name);
            final ActionWriter actionWriter = ActionWriterFactory.getWriter(actionCommand);
            final JSONObject jsonCommandObj = actionWriter.write((Action<ActionArgument>) action, new JSONObject()); //FIXME: Don't use the JSONObject as a parameter, the method has to create it's own object
            jsonArray.put(jsonCommandObj);
        }

        return jsonArray;
    }

    /**
     * Writes the specified {@link AbstractTest} as a {@link JSONObject}
     * 
     * @param abstractTest The {@link AbstractTest}
     * @return A {@link JSONObject}
     * @throws JSONException if a JSON error occurs
     */
    private JSONObject getTestCommandAsJSON(AbstractTest abstractTest) throws JSONException {
        final String name = abstractTest.getName();
        final TestWriter testWriter = TestWriterFactory.getWriter(name);
        return testWriter.writeTest(name, abstractTest);
    }
}
