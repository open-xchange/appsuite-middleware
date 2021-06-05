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
import com.openexchange.mailfilter.json.ajax.json.fields.BodyTestField;
import com.openexchange.mailfilter.json.ajax.json.fields.GeneralField;
import com.openexchange.mailfilter.json.ajax.json.fields.HeaderTestField;
import com.openexchange.mailfilter.json.ajax.json.mapper.ArgumentUtil;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link BodyTestCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class BodyTestCommandParser implements CommandParser<TestCommand> {

    /**
     * Initialises a new {@link BodyTestCommandParser}.
     */
    public BodyTestCommandParser() {
        super();
    }

    @Override
    public TestCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {
        String commandName = Commands.BODY.getCommandName();
        final List<Object> argList = new ArrayList<Object>();
        argList.add(ArgumentUtil.createTagArgument(CommandParserJSONUtil.getString(jsonObject, BodyTestField.comparison.name(), commandName)));
        final String extensionkey = CommandParserJSONUtil.getString(jsonObject, BodyTestField.extensionskey.name(), commandName);
        if (null != extensionkey && !extensionkey.equals(JSONObject.NULL.toString())) {
            if (extensionkey.equals("text")) {
                argList.add(ArgumentUtil.createTagArgument("text"));
            } else if (extensionkey.equals("content")) {
                // TODO: This part should be tested for correct operation, our GUI doesn't use this, but this is
                // allowed according to our specification
                argList.add(ArgumentUtil.createTagArgument("content"));
                final String extensionvalue = CommandParserJSONUtil.getString(jsonObject, BodyTestField.extensionsvalue.name(), commandName);
                argList.add(extensionvalue);
            } else {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create("Body rule: The extensionskey " + extensionkey + " is not a valid extensionkey");
            }
        }
        argList.add(CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(jsonObject, HeaderTestField.values.name(), commandName)));
        return new TestCommand(TestCommand.Commands.BODY, argList, new ArrayList<TestCommand>());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void parse(JSONObject jsonObject, TestCommand command) throws JSONException, OXException {
        jsonObject.put(GeneralField.id.name(), Commands.BODY.getCommandName());
        jsonObject.put(BodyTestField.comparison.name(), command.getMatchType().substring(1));
        List<String> tagArguments = command.getTagArguments();
        if (tagArguments.size() > 1) {
            final String extensionkey = tagArguments.get(1).substring(1);
            jsonObject.put(BodyTestField.extensionskey.name(), extensionkey);
            if ("content".equals(extensionkey)) {
                // TODO: This part should be tested for correct operation, our GUI doesn't use this, but this is
                // allowed according to our specification
                jsonObject.put(BodyTestField.extensionsvalue.name(), command.getArguments().get(2));
                jsonObject.put(BodyTestField.values.name(), new JSONArray((List) command.getArguments().get(3)));
            } else {
                jsonObject.put(BodyTestField.extensionsvalue.name(), JSONObject.NULL);
                jsonObject.put(BodyTestField.values.name(), new JSONArray((List) command.getArguments().get(2)));
            }
        } else {
            // no extensionskey
            jsonObject.put(BodyTestField.extensionskey.name(), JSONObject.NULL);
            jsonObject.put(BodyTestField.extensionsvalue.name(), JSONObject.NULL);
            jsonObject.put(BodyTestField.values.name(), new JSONArray((List) command.getArguments().get(1)));
        }
    }
}
