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
import java.util.List;
import org.apache.jsieve.SieveException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.mailfilter.json.ajax.json.fields.GeneralField;
import com.openexchange.mailfilter.json.ajax.json.fields.NotTestField;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParserRegistry;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.TestCommandParserRegistry;
import com.openexchange.mailfilter.json.osgi.Services;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link NotTestCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class NotTestCommandParser implements CommandParser<TestCommand> {

    /**
     * Initialises a new {@link NotTestCommandParser}.
     */
    public NotTestCommandParser() {
        super();
    }

    @Override
    public TestCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {
        // FIXME Not parser seems to be broken by design


        final List<Object> argList = new ArrayList<Object>();
        ArrayList<TestCommand> testcommands = new ArrayList<TestCommand>();

        final JSONObject innerJsonObject = jsonObject.optJSONObject(NotTestField.test.name());
        if (null != innerJsonObject) {
            CommandParserRegistry<TestCommand> parserRegistry = Services.getService(TestCommandParserRegistry.class);
            CommandParser<TestCommand> parser = parserRegistry.get(innerJsonObject.optString(GeneralField.id.name()));
            if (null != parser) {
                testcommands.add(parser.parse(innerJsonObject, session));
            }
        }
        return new TestCommand(TestCommand.Commands.NOT, argList, testcommands);
    }

    @Override
    public void parse(JSONObject jsonObject, TestCommand command) throws JSONException, OXException {
        jsonObject.put(GeneralField.id.name(), Commands.NOT.getCommandName());
        final JSONObject testobject = new JSONObject();

        TestCommand nestedTestCommand = command.getTestCommands().get(0);
        CommandParserRegistry<TestCommand> parserRegistry = Services.getService(TestCommandParserRegistry.class);
        CommandParser<TestCommand> parser = parserRegistry.get(nestedTestCommand.getCommand().getCommandName());
        if (null != parser) {
            parser.parse(testobject, nestedTestCommand);
        }

        jsonObject.put(NotTestField.test.name(), testobject);
    }
}
