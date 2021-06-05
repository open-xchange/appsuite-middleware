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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.mailfilter.json.ajax.json.fields.ExistsTestField;
import com.openexchange.mailfilter.json.ajax.json.fields.GeneralField;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ExistsTestCommandParser} parses exists sieve tests.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class ExistsTestCommandParser implements CommandParser<TestCommand> {

    @Override
    public TestCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {
        String commandName = Commands.EXISTS.getCommandName();
        final List<Object> argList = new ArrayList<Object>();
        JSONArray array = CommandParserJSONUtil.getJSONArray(jsonObject, ExistsTestField.headers.name(), commandName);
        argList.add(CommandParserJSONUtil.coerceToStringList(array));
        return new TestCommand(TestCommand.Commands.EXISTS, argList, new ArrayList<TestCommand>());
    }

    @Override
    public void parse(JSONObject jsonObject, TestCommand command) throws JSONException {
        jsonObject.put(GeneralField.id.name(), command.getCommand().getCommandName());
        jsonObject.put(ExistsTestField.headers.name(), new JSONArray((List<?>) command.getArguments().get(0)));

    }

}
