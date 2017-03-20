/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
import com.openexchange.jsieve.commands.MatchType;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.mail.filter.json.v2.json.fields.BodyTestField;
import com.openexchange.mail.filter.json.v2.json.fields.GeneralField;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.mail.filter.json.v2.mapper.ArgumentUtil;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link BodyTestCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 */
public class BodyTestCommandParser extends AbstractSimplifiedMatcherAwareCommandParser {

    /**
     * Initializes a new {@link BodyTestCommandParser}.
     */
    public BodyTestCommandParser(ServiceLookup services) {
        super(services, Commands.BODY);
    }

    @Override
    public TestCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {
        String commandName = Commands.BODY.getCommandName();
        final List<Object> argList = new ArrayList<Object>();
        String matcher = CommandParserJSONUtil.getString(jsonObject, BodyTestField.comparison.name(), commandName);
        String normalizedMatcher = MatchType.getNormalName(matcher);
        if(normalizedMatcher != null){
            if(StartsOrEndsWithMatcherUtil.isSimplifiedMatcher(normalizedMatcher)){
                handleSimplifiedMatcher(normalizedMatcher, argList, jsonObject);
            } else {
                argList.add(ArgumentUtil.createTagArgument(normalizedMatcher));
                final String extensionkey = CommandParserJSONUtil.getString(jsonObject, BodyTestField.extensionskey.name(), commandName);
                if (null != extensionkey && !extensionkey.equals(JSONObject.NULL.toString())) {
                    if (extensionkey.equals("text")) {
                        argList.add(ArgumentUtil.createTagArgument("text"));
                    } else if (extensionkey.equals("content")) {
                        argList.add(ArgumentUtil.createTagArgument("content"));
                        final String extensionvalue = CommandParserJSONUtil.getString(jsonObject, BodyTestField.extensionsvalue.name(), commandName);
                        argList.add(extensionvalue);
                    } else {
                        throw OXJSONExceptionCodes.JSON_READ_ERROR.create("Body rule: The extensionskey " + extensionkey + " is not a valid extensionkey");
                    }
                }
                argList.add(CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(jsonObject, BodyTestField.values.name(), commandName)));
            }
            return NotTestCommandUtil.wrapTestCommand(new TestCommand(TestCommand.Commands.BODY, argList, new ArrayList<TestCommand>()));
        } else {
            if(StartsOrEndsWithMatcherUtil.isSimplifiedMatcher(matcher)){
                handleSimplifiedMatcher(matcher, argList, jsonObject);
            } else {
                argList.add(ArgumentUtil.createTagArgument(matcher));
                final String extensionkey = CommandParserJSONUtil.getString(jsonObject, BodyTestField.extensionskey.name(), commandName);
                if (null != extensionkey && !extensionkey.equals(JSONObject.NULL.toString())) {
                    if (extensionkey.equals("text")) {
                        argList.add(ArgumentUtil.createTagArgument("text"));
                    } else if (extensionkey.equals("content")) {
                        argList.add(ArgumentUtil.createTagArgument("content"));
                        final String extensionvalue = CommandParserJSONUtil.getString(jsonObject, BodyTestField.extensionsvalue.name(), commandName);
                        argList.add(extensionvalue);
                    } else {
                        throw OXJSONExceptionCodes.JSON_READ_ERROR.create("Body rule: The extensionskey " + extensionkey + " is not a valid extensionkey");
                    }
                }
                argList.add(CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(jsonObject, BodyTestField.values.name(), commandName)));
            }
            return new TestCommand(TestCommand.Commands.BODY, argList, new ArrayList<TestCommand>());
        }
    }

    @Override
    void handleSimplifiedMatcher(String matcher, List<Object> argList, JSONObject data) throws JSONException, OXException{
        StartsOrEndsWithMatcherUtil.insertMatchesMatcher(argList);
        final String extensionkey = CommandParserJSONUtil.getString(data, BodyTestField.extensionskey.name(), Commands.BODY.getCommandName());
        if (null != extensionkey && !extensionkey.equals(JSONObject.NULL.toString())) {
            if (extensionkey.equals("text")) {
                argList.add(ArgumentUtil.createTagArgument("text"));
            } else if (extensionkey.equals("content")) {
                argList.add(ArgumentUtil.createTagArgument("content"));
                final String extensionvalue = CommandParserJSONUtil.getString(data, BodyTestField.extensionsvalue.name(), Commands.BODY.getCommandName());
                argList.add(extensionvalue);
            } else {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create("Body rule: The extensionskey " + extensionkey + " is not a valid extensionkey");
            }
        }
        List<String> list = CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(data, BodyTestField.values.name(), Commands.ENVELOPE.getCommandName()));
        StartsOrEndsWithMatcherUtil.insertValuesArgumentWithWildcards(list, matcher, argList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void parse(JSONObject jsonObject, TestCommand command, boolean transformToNotMatcher) throws JSONException, OXException {
        jsonObject.put(GeneralField.id.name(), Commands.BODY.getCommandName());

        List<String> tagArguments = command.getTagArguments();
        List<String> values;
        if (tagArguments.size() > 1) {
            final String extensionkey = tagArguments.get(1).substring(1);
            jsonObject.put(BodyTestField.extensionskey.name(), extensionkey);
            if ("content".equals(extensionkey)) {
                // allowed according to our specification
                jsonObject.put(BodyTestField.extensionsvalue.name(), command.getArguments().get(2));
                values = (List<String>) command.getArguments().get(3);
            } else {
                jsonObject.put(BodyTestField.extensionsvalue.name(), JSONObject.NULL);
                values = (List<String>) command.getArguments().get(2);
            }
        } else {
            // no extensionskey
            jsonObject.put(BodyTestField.extensionskey.name(), JSONObject.NULL);
            jsonObject.put(BodyTestField.extensionsvalue.name(), JSONObject.NULL);
            values = (List<String>) command.getArguments().get(1);
        }

        String matchType = command.getMatchType();
        MatchType type;
        if (matchType == null) {
            jsonObject.put(BodyTestField.comparison.name(), MatchType.is.name());
            type = MatchType.is;
        } else {
            matchType = matchType.substring(1);
            type = MatchType.valueOf(matchType);
            type = StartsOrEndsWithMatcherUtil.checkMatchType(type, values);
            if(transformToNotMatcher){
                jsonObject.put(BodyTestField.comparison.name(), type.getNotName());
            } else {
                jsonObject.put(BodyTestField.comparison.name(), type.name());
            }
        }

        jsonObject.put(BodyTestField.values.name(), new JSONArray(StartsOrEndsWithMatcherUtil.retrieveListForMatchType(values, type)));
    }
}
