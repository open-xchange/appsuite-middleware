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
import java.util.regex.Pattern;
import org.apache.jsieve.NumberArgument;
import org.apache.jsieve.SieveException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.mailfilter.json.ajax.json.fields.GeneralField;
import com.openexchange.mailfilter.json.ajax.json.fields.SizeTestField;
import com.openexchange.mailfilter.json.ajax.json.mapper.ArgumentUtil;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SizeTestCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SizeTestCommandParser implements CommandParser<TestCommand> {

    private final static Pattern DIGITS = Pattern.compile("^\\-?\\d+$");

    /**
     * Initialises a new {@link SizeTestCommandParser}.
     */
    public SizeTestCommandParser() {
        super();
    }

    @Override
    public TestCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {
        String commandName = Commands.SIZE.getCommandName();
        String size = CommandParserJSONUtil.getString(jsonObject, SizeTestField.size.name(), commandName);
        try {
            final String sizeToSend;
            size = size.toLowerCase();
            if (size.endsWith("k")) {
                if (size.length() == 1) {
                    sizeToSend = "0";
                } else {
                    String substring = size.substring(0, size.length()-1);
                    checkSize(substring, commandName);
                    sizeToSend = substring + "K";
                }
            } else if (size.endsWith("kb")) {
                if (size.length() == 2) {
                    sizeToSend = "0";
                } else {
                    String substring = size.substring(0, size.length()-2);
                    checkSize(substring, commandName);
                    sizeToSend = substring + "K";
                }
            } else if (size.endsWith("m")) {
                if (size.length() == 1) {
                    sizeToSend = "0";
                } else {
                    String substring = size.substring(0, size.length()-1);
                    checkSize(substring, commandName);
                    sizeToSend = substring + "M";
                }
            } else if (size.endsWith("mb")) {
                if (size.length() == 2) {
                    sizeToSend = "0";
                } else {
                    String substring = size.substring(0, size.length()-2);
                    checkSize(substring, commandName);
                    sizeToSend = substring + "M";
                }
            } else if (size.endsWith("g")) {
                if (size.length() == 1) {
                    sizeToSend = "0";
                } else {
                    String substring = size.substring(0, size.length()-1);
                    checkSize(substring, commandName);
                    sizeToSend = substring + "G";
                }
            } else if (size.endsWith("gb")) {
                if (size.length() == 2) {
                    sizeToSend = "0";
                } else {
                    String substring = size.substring(0, size.length()-2);
                    checkSize(substring, commandName);
                    sizeToSend = substring + "G";
                }
            } else if (size.endsWith("b")) {
                if (size.length() == 1) {
                    sizeToSend = "0";
                } else {
                    String substring = size.substring(0, size.length()-1);
                    checkSize(substring, commandName);
                    sizeToSend = substring;
                }
            } else {
                // implicit bytes
                checkSize(size, commandName);
                sizeToSend = size;
            }
            final List<Object> argList = new ArrayList<Object>();
            argList.add(ArgumentUtil.createTagArgument(CommandParserJSONUtil.getString(jsonObject, SizeTestField.comparison.name(), commandName)));
            argList.add(ArgumentUtil.createNumberArgument(sizeToSend));
            return new TestCommand(TestCommand.Commands.SIZE, argList, new ArrayList<TestCommand>());
        } catch (NumberFormatException e) {
            throw OXJSONExceptionCodes.TOO_BIG_NUMBER.create(e, commandName);
        }
    }

    private void checkSize(String size, String commandName) throws OXException {
        if (!DIGITS.matcher(size).matches()) {
            throw OXJSONExceptionCodes.CONTAINS_NON_DIGITS.create(size, commandName);
        }
    }

    @Override
    public void parse(JSONObject jsonObject, TestCommand command) throws JSONException, OXException {
        jsonObject.put(GeneralField.id.name(), TestCommand.Commands.SIZE.getCommandName());
        jsonObject.put(SizeTestField.comparison.name(), command.getMatchType().substring(1));
        Object value = command.getArguments().get(1);
        if (value instanceof NumberArgument) {
            int intVal = ((NumberArgument) value).getInteger().intValue();
            int returnVal = 0;
            String type = null;
            if (intVal % 1073741824 == 0) {
                returnVal = intVal / 1073741824;
                type = "G";
            } else if (intVal % 1048576 == 0) {
                returnVal = intVal / 1048576;
                type = "M";
            }  else if (intVal % 1024 == 0) {
                returnVal = intVal / 1024;
                type = "K";
            } else {
                returnVal = intVal;
            }
            final String sizeString;
            if (null == type) {
                sizeString = Integer.toString(returnVal);
            } else {
                sizeString = returnVal + type;
            }
            jsonObject.put(SizeTestField.size.name(), sizeString);
        } else {
            jsonObject.put(SizeTestField.size.name(), value);
        }
    }

}
