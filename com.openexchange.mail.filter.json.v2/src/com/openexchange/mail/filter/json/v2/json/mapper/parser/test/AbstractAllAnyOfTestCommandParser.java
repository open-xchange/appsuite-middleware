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

package com.openexchange.mail.filter.json.v2.json.mapper.parser.test;

import java.util.ArrayList;
import java.util.List;
import org.apache.jsieve.SieveException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.mail.filter.json.v2.json.fields.AllOfOrAnyOfTestField;
import com.openexchange.mail.filter.json.v2.json.fields.GeneralField;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.CommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.CommandParserRegistry;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.TestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.TestCommandParserRegistry;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractAllAnyOfTestCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 */
abstract class AbstractAllAnyOfTestCommandParser extends AbstractTestCommandParser {

    /**
     * Initializes a new {@link AbstractAllAnyOfTestCommandParser}.
     */
    protected AbstractAllAnyOfTestCommandParser(ServiceLookup services, Commands testCommand) {
        super(services, testCommand);
    }

    TestCommand parse(JSONObject jsonObject, Commands command, ServerSession session) throws OXException, JSONException, SieveException {
        JSONArray jarray = CommandParserJSONUtil.getJSONArray(jsonObject, AllOfOrAnyOfTestField.tests.name(), command.getCommandName());
        int length = jarray.length();

        ArrayList<TestCommand> commandlist = new ArrayList<>(length);
        CommandParserRegistry<TestCommand, TestCommandParser<TestCommand>> parserRegistry = services.getService(TestCommandParserRegistry.class);

        for (int i = 0; i < length; i++) {
            JSONObject object = jarray.getJSONObject(i);
            String commandName = CommandParserJSONUtil.getString(object, GeneralField.id.name(), command.getCommandName());
            CommandParser<TestCommand> parser = parserRegistry.get(commandName);
            commandlist.add(parser.parse(object, session));
        }
        return new TestCommand(command, new ArrayList<>(), commandlist);
    }

    /**
     * Parses the specified {@link TestCommand} to a {@link JSONObject}.
     *
     * @param jsonObject The {@link JSONException}
     * @param testCommand The {@link TestCommand}
     * @param command The command
     * @param transformToNotMatcher Whether to negate the test command
     * @throws JSONException if a JSON error is occurred
     * @throws OXException if an error is occurred
     */
    void parse(JSONObject jsonObject, TestCommand testCommand, Commands command, boolean transformToNotMatcher) throws JSONException, OXException {
        jsonObject.put(GeneralField.id.name(), command.getCommandName());
        CommandParserRegistry<TestCommand, TestCommandParser<TestCommand>> parserRegistry = services.getService(TestCommandParserRegistry.class);
        List<TestCommand> testCommands = testCommand.getTestCommands();

        JSONArray array = new JSONArray(testCommands.size());
        for (TestCommand testCommand2 : testCommands) {
            CommandParser<TestCommand> parser = parserRegistry.get(testCommand2.getCommand().getCommandName());
            JSONObject object = new JSONObject();
            parser.parse(object, testCommand2);
            array.put(object);
        }
        jsonObject.put(AllOfOrAnyOfTestField.tests.name(), array);
    }
}
