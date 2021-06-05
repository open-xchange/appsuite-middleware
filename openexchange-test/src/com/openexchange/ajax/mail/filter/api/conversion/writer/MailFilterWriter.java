/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.openexchange.ajax.mail.filter.api.dao.TestCommand;
import com.openexchange.ajax.mail.filter.api.dao.action.Action;
import com.openexchange.ajax.mail.filter.api.dao.action.argument.ActionArgument;
import com.openexchange.ajax.mail.filter.api.dao.test.Test;
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

        if (rule.getPosition() >= 0) {
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
     */
    private JSONArray getFlagsAsJSON(String[] flags) {
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
    private JSONObject getTestCommandAsJSON(Test<?> abstractTest) throws JSONException {
        final TestCommand name = abstractTest.getTestCommand();
        final TestWriter testWriter = TestWriterFactory.getWriter(name);
        return testWriter.write(abstractTest, new JSONObject());
    }
}
