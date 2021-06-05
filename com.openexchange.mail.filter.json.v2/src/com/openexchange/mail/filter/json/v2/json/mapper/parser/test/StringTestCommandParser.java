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
import com.openexchange.java.Strings;
import com.openexchange.jsieve.commands.MatchType;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.mail.filter.json.v2.json.fields.EnvelopeTestField;
import com.openexchange.mail.filter.json.v2.json.fields.GeneralField;
import com.openexchange.mail.filter.json.v2.json.fields.StringTestField;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.mail.filter.json.v2.mapper.ArgumentUtil;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 *
 * {@link StringTestCommandParser}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class StringTestCommandParser extends AbstractSimplifiedMatcherAwareCommandParser {

    /**
     * Initializes a new {@link StringTestCommandParser}.
     */
    public StringTestCommandParser(ServiceLookup services) {
        super(services, Commands.STRING);
    }

    @Override
    public TestCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {
        final List<Object> argList = new ArrayList<Object>();
        String matcher = CommandParserJSONUtil.getString(jsonObject, StringTestField.comparison.name(), Commands.STRING.getCommandName());
        String normalizedMatcher = MatchType.getNormalName(matcher);

        String comparison = Strings.isEmpty(normalizedMatcher) ? matcher : normalizedMatcher;
        boolean wrap = MatchType.valueOf(comparison).getNotName().equals(matcher);

        if (StartsOrEndsWithMatcherUtil.isSimplifiedMatcher(comparison)) {
            handleSimplifiedMatcher(comparison, argList, jsonObject);
        } else {
            argList.add(ArgumentUtil.createTagArgument(comparison));
            argList.add(CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(jsonObject, StringTestField.source.name(), Commands.STRING.getCommandName())));
            argList.add(CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(jsonObject, StringTestField.values.name(), Commands.STRING.getCommandName())));

        }
        TestCommand testCommand = new TestCommand(Commands.STRING, argList, new ArrayList<TestCommand>());
        return wrap ? NotTestCommandUtil.wrapTestCommand(testCommand) : testCommand;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void parse(JSONObject jsonObject, TestCommand command, boolean transformToNotMatcher) throws JSONException {
        jsonObject.put(GeneralField.id.name(), command.getCommand().getCommandName());
        List<String> values = (List<String>) command.getArguments().get(command.getTagArguments().size() + 1);
        String matchType = command.getMatchType();
        MatchType type;
        if (matchType == null) {
            jsonObject.put(EnvelopeTestField.comparison.name(), MatchType.is.name());
            type = MatchType.is;
        } else {
            matchType = matchType.substring(1);
            type = MatchType.valueOf(matchType);
            type = StartsOrEndsWithMatcherUtil.checkMatchType(type, values);
            if (transformToNotMatcher) {
                jsonObject.put(StringTestField.comparison.name(), type.getNotName());
            } else {
                jsonObject.put(StringTestField.comparison.name(), type.name());
            }
        }

        jsonObject.put(StringTestField.source.name(), new JSONArray(toLowerCase((List<String>) command.getArguments().get(command.getTagArguments().size()))));
        jsonObject.put(StringTestField.values.name(), new JSONArray(StartsOrEndsWithMatcherUtil.retrieveListForMatchType(values, type)));
    }

    private List<String> toLowerCase(List<String> data) {
        List<String> result = new ArrayList<>(data.size());
        for (String str : data) {
            result.add(str.toLowerCase());
        }
        return result;
    }

    @Override
    void handleSimplifiedMatcher(String matcher, List<Object> argList, JSONObject data) throws JSONException, OXException {
        StartsOrEndsWithMatcherUtil.insertMatchesMatcher(argList);
        argList.add(CommandParserJSONUtil.getString(data, StringTestField.source.name(), Commands.STRING.getCommandName()));
        List<String> list = CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(data, StringTestField.values.name(), Commands.STRING.getCommandName()));
        StartsOrEndsWithMatcherUtil.insertValuesArgumentWithWildcards(list, matcher, argList);
    }
}
