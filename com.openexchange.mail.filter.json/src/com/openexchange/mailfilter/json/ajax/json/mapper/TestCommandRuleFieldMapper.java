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

package com.openexchange.mailfilter.json.ajax.json.mapper;

import org.apache.jsieve.SieveException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.IfCommand;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.mailfilter.json.ajax.json.fields.GeneralField;
import com.openexchange.mailfilter.json.ajax.json.fields.RuleField;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParserRegistry;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.TestCommandParserRegistry;
import com.openexchange.mailfilter.json.osgi.Services;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link TestCommandRuleFieldMapper}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class TestCommandRuleFieldMapper implements RuleFieldMapper {

    /**
     * Initialises a new {@link TestCommandRuleFieldMapper}.
     */
    public TestCommandRuleFieldMapper() {
        super();
    }

    @Override
    public RuleField getAttributeName() {
        return RuleField.test;
    }

    @Override
    public boolean isNull(Rule rule) {
        return rule.getTestCommand() == null;
    }

    @Override
    public Object getAttribute(Rule rule) throws JSONException, OXException {
        JSONObject object = new JSONObject();
        if (!isNull(rule)) {
            TestCommand testCommand = rule.getTestCommand();
            if (testCommand!=null){
                String commandName = testCommand.getCommand().getCommandName();
                CommandParserRegistry<TestCommand> parserRegistry = Services.getService(TestCommandParserRegistry.class);
                CommandParser<TestCommand> parser = parserRegistry.get(commandName);
                parser.parse(object, testCommand);
            }
        }
        return object;
    }

    @Override
    public void setAttribute(Rule rule, Object attribute, ServerSession session) throws JSONException, SieveException, OXException {
        JSONObject object = (JSONObject) attribute;
        String id = object.getString(GeneralField.id.name());

        TestCommand existingTestCommand = rule.getTestCommand();
        CommandParserRegistry<TestCommand> parserRegistry = Services.getService(TestCommandParserRegistry.class);
        CommandParser<TestCommand> parser = parserRegistry.get(id);
        if (parser == null) {
            //TODO: better exception handling
            throw new JSONException("Unknown test command while creating object: " + id);
        }
        TestCommand parsedTestCommand = parser.parse(object, session);
        if (existingTestCommand != null) {
            rule.getIfCommand().setTestcommand(parsedTestCommand);
            return;
        }

        if (rule.getCommands().isEmpty()) {
            rule.addCommand(new IfCommand(parsedTestCommand));
        }
    }
}
