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
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.mail.filter.json.v2.json.fields.GeneralField;
import com.openexchange.mail.filter.json.v2.json.fields.NotTestField;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.CommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.CommandParserRegistry;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.TestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.TestCommandParserRegistry;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link NotTestCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 */
public class NotTestCommandParser extends AbstractTestCommandParser {

    /**
     * Initializes a new {@link NotTestCommandParser}.
     */
    public NotTestCommandParser(ServiceLookup services) {
        super(services, Commands.NOT);
    }

    @Override
    public TestCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {
        final List<Object> argList = new ArrayList<Object>();
        ArrayList<TestCommand> testcommands = new ArrayList<TestCommand>();

        final JSONObject innerJsonObject = jsonObject.optJSONObject(NotTestField.test.name());
        if (null != innerJsonObject) {
            CommandParserRegistry<TestCommand, TestCommandParser<TestCommand>> parserRegistry = services.getService(TestCommandParserRegistry.class);
            CommandParser<TestCommand> parser = parserRegistry.get(innerJsonObject.optString(GeneralField.id.name()));
            testcommands.add(parser.parse(innerJsonObject, session));
        }
        return new TestCommand(TestCommand.Commands.NOT, argList, testcommands);
    }

    @Override
    public void parse(JSONObject jsonObject, TestCommand command, boolean transformToNotMatcher) throws JSONException, OXException {
        TestCommand nestedTestCommand = command.getTestCommands().get(0);
        CommandParserRegistry<TestCommand, TestCommandParser<TestCommand>> parserRegistry = services.getService(TestCommandParserRegistry.class);
        TestCommandParser<TestCommand> parser = parserRegistry.get(nestedTestCommand.getCommand().getCommandName());
        parser.parse(jsonObject, nestedTestCommand, transformToNotMatcher == false);
    }
}
