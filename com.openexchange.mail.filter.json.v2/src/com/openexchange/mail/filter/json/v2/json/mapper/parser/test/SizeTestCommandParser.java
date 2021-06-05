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
import java.util.regex.Pattern;
import org.apache.jsieve.NumberArgument;
import org.apache.jsieve.SieveException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.MatchType;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.mail.filter.json.v2.json.fields.BodyTestField;
import com.openexchange.mail.filter.json.v2.json.fields.GeneralField;
import com.openexchange.mail.filter.json.v2.json.fields.SizeTestField;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.mail.filter.json.v2.mapper.ArgumentUtil;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SizeTestCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 */
public class SizeTestCommandParser extends AbstractTestCommandParser {

    private final static Pattern DIGITS = Pattern.compile("^\\-?\\d+$");

    /**
     * Initialises a new {@link SizeTestCommandParser}.
     */
    public SizeTestCommandParser(ServiceLookup services) {
        super(services, Commands.SIZE);
    }

    @Override
    public TestCommand parse(JSONObject jsonObject, ServerSession session) throws SieveException, OXException {
        String commandName = Commands.SIZE.getCommandName();
        String size = CommandParserJSONUtil.getString(jsonObject, SizeTestField.size.name(), commandName);
        try {
            final String sizeToSend;
            size = size.toLowerCase();
            if (size.endsWith("k")) {
                if (size.length() == 1) {
                    sizeToSend = "0";
                } else {
                    String substring = size.substring(0, size.length() - 1);
                    checkSize(substring, commandName);
                    sizeToSend = substring + "K";
                }
            } else if (size.endsWith("kb")) {
                if (size.length() == 2) {
                    sizeToSend = "0";
                } else {
                    String substring = size.substring(0, size.length() - 2);
                    checkSize(substring, commandName);
                    sizeToSend = substring + "K";
                }
            } else if (size.endsWith("m")) {
                if (size.length() == 1) {
                    sizeToSend = "0";
                } else {
                    String substring = size.substring(0, size.length() - 1);
                    checkSize(substring, commandName);
                    sizeToSend = substring + "M";
                }
            } else if (size.endsWith("mb")) {
                if (size.length() == 2) {
                    sizeToSend = "0";
                } else {
                    String substring = size.substring(0, size.length() - 2);
                    checkSize(substring, commandName);
                    sizeToSend = substring + "M";
                }
            } else if (size.endsWith("g")) {
                if (size.length() == 1) {
                    sizeToSend = "0";
                } else {
                    String substring = size.substring(0, size.length() - 1);
                    checkSize(substring, commandName);
                    sizeToSend = substring + "G";
                }
            } else if (size.endsWith("gb")) {
                if (size.length() == 2) {
                    sizeToSend = "0";
                } else {
                    String substring = size.substring(0, size.length() - 2);
                    checkSize(substring, commandName);
                    sizeToSend = substring + "G";
                }
            } else if (size.endsWith("b")) {
                if (size.length() == 1) {
                    sizeToSend = "0";
                } else {
                    String substring = size.substring(0, size.length() - 1);
                    checkSize(substring, commandName);
                    sizeToSend = substring;
                }
            } else {
                // implicit bytes
                checkSize(size, commandName);
                sizeToSend = size;
            }
            final List<Object> argList = new ArrayList<Object>();
            String matcher = CommandParserJSONUtil.getString(jsonObject, BodyTestField.comparison.name(), commandName);
            String normalizedMatcher = MatchType.getNormalName(matcher);
            if (normalizedMatcher != null){
                argList.add(ArgumentUtil.createTagArgument(normalizedMatcher));
                argList.add(ArgumentUtil.createNumberArgument(sizeToSend));
                return NotTestCommandUtil.wrapTestCommand(new TestCommand(TestCommand.Commands.SIZE, argList, new ArrayList<TestCommand>()));
            }
            argList.add(ArgumentUtil.createTagArgument(matcher));
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
    public void parse(JSONObject jsonObject, TestCommand command, boolean transformToNotMatcher) throws JSONException {
        jsonObject.put(GeneralField.id.name(), TestCommand.Commands.SIZE.getCommandName());

        String matchType = command.getMatchType();
        if (matchType == null) {
            jsonObject.put(SizeTestField.comparison.name(), MatchType.is.name());
        } else {
            if (transformToNotMatcher){
                String notMatchType = MatchType.getNotNameForArgumentName(matchType);
                jsonObject.put(SizeTestField.comparison.name(), notMatchType);
            } else {
                jsonObject.put(SizeTestField.comparison.name(), matchType.substring(1));
            }
        }

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
            } else if (intVal % 1024 == 0) {
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
