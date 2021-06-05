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
import com.openexchange.mailfilter.json.ajax.json.fields.EnvelopeTestField;
import com.openexchange.mailfilter.json.ajax.json.fields.GeneralField;
import com.openexchange.mailfilter.json.ajax.json.mapper.ArgumentUtil;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link EnvelopeTestCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 */
public class EnvelopeTestCommandParser implements CommandParser<TestCommand> {

    /**
     * Initialises a new {@link EnvelopeTestCommandParser}.
     */
    public EnvelopeTestCommandParser() {
        super();
    }

    @Override
    public TestCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {

        final List<Object> argList = new ArrayList<Object>();
        argList.add(ArgumentUtil.createTagArgument(CommandParserJSONUtil.getString(jsonObject, EnvelopeTestField.comparison.name(), Commands.ENVELOPE.getCommandName())));
        if (jsonObject.hasAndNotNull(EnvelopeTestField.addresspart.name())) {
            argList.add(ArgumentUtil.createTagArgument(CommandParserJSONUtil.getString(jsonObject, EnvelopeTestField.addresspart.name(), Commands.ENVELOPE.getCommandName())));
        }
        argList.add(CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(jsonObject, EnvelopeTestField.headers.name(), Commands.ENVELOPE.getCommandName())));
        argList.add(CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(jsonObject, EnvelopeTestField.values.name(), Commands.ENVELOPE.getCommandName())));
        return new TestCommand(Commands.ENVELOPE, argList, new ArrayList<TestCommand>());
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void parse(JSONObject jsonObject, TestCommand command) throws JSONException {
        jsonObject.put(GeneralField.id.name(), command.getCommand().getCommandName());
        if (command.getMatchType() == null) {
            jsonObject.put(EnvelopeTestField.comparison.name(), "is");
        } else {
            jsonObject.put(EnvelopeTestField.comparison.name(), command.getMatchType().substring(1));
        }
        if (command.getAddressPart() != null) {
            jsonObject.put(EnvelopeTestField.addresspart.name(), command.getAddressPart().substring(1));
        }
        jsonObject.put(EnvelopeTestField.headers.name(), new JSONArray((List) command.getArguments().get(command.getTagArguments().size())));
        jsonObject.put(EnvelopeTestField.values.name(), new JSONArray((List) command.getArguments().get(command.getTagArguments().size() + 1)));
    }
}
