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

package com.openexchange.mailfilter.json.ajax.json.mapper.parser.test;

import java.util.ArrayList;
import org.apache.jsieve.SieveException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.mailfilter.json.ajax.json.fields.AllOfOrAnyOfTestField;
import com.openexchange.mailfilter.json.ajax.json.fields.GeneralField;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParserRegistry;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.TestCommandParserRegistry;
import com.openexchange.mailfilter.json.osgi.Services;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractAllAnyOfTestCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractAllAnyOfTestCommandParser implements CommandParser<TestCommand> {

    /**
     * Initialises a new {@link AbstractAllAnyOfTestCommandParser}.
     */
    public AbstractAllAnyOfTestCommandParser() {
        super();
    }

    TestCommand parse(JSONObject jsonObject, Commands command, ServerSession session) throws OXException, JSONException, SieveException {
        final JSONArray jarray = CommandParserJSONUtil.getJSONArray(jsonObject, AllOfOrAnyOfTestField.tests.name(), command.getCommandName());
        final ArrayList<TestCommand> commandlist = new ArrayList<TestCommand>(jarray.length());
        CommandParserRegistry<TestCommand> parserRegistry = Services.getService(TestCommandParserRegistry.class);

        for (int i = 0; i < jarray.length(); i++) {
            final JSONObject object = jarray.getJSONObject(i);
            String commandName = CommandParserJSONUtil.getString(object, GeneralField.id.name(), command.getCommandName());
            CommandParser<TestCommand> parser = parserRegistry.get(commandName);
            commandlist.add(parser.parse(object, session));
        }
        return new TestCommand(command, new ArrayList<Object>(), commandlist);
    }

    void parse(JSONObject jsonObject, TestCommand testCommand, Commands command) throws JSONException, OXException {
        jsonObject.put(GeneralField.id.name(), command.getCommandName());
        final JSONArray array = new JSONArray();
        CommandParserRegistry<TestCommand> parserRegistry = Services.getService(TestCommandParserRegistry.class);
        for (final TestCommand testCommand2 : testCommand.getTestCommands()) {
            final JSONObject object = new JSONObject();
            CommandParser<TestCommand> parser = parserRegistry.get(testCommand2.getCommand().getCommandName());
            parser.parse(object, testCommand2);
            array.put(object);
        }
        jsonObject.put(AllOfOrAnyOfTestField.tests.name(), array);
    }
}
