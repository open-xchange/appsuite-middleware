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
import com.openexchange.jsieve.commands.MatchType;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.mail.filter.json.v2.json.fields.GeneralField;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.mail.filter.json.v2.mapper.ArgumentUtil;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link HasFlagCommandParser}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class HasFlagCommandParser extends AbstractSimplifiedMatcherAwareCommandParser {

    private static enum Fields {
        comparison,
        values
    }

    /**
     * Initializes a new {@link HasFlagCommandParser}.
     */
    public HasFlagCommandParser(ServiceLookup services) {
        super(services, Commands.HASFLAG);
    }

    @Override
    public TestCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {
        List<Object> flagArgList = new ArrayList<Object>(4);
        String matcher = CommandParserJSONUtil.getString(jsonObject, Fields.comparison.name(), Commands.HASFLAG.getCommandName());
        String normalizedMatcher = MatchType.getNormalName(matcher);
        if (normalizedMatcher != null) {
            if (StartsOrEndsWithMatcherUtil.isSimplifiedMatcher(normalizedMatcher)){
                handleSimplifiedMatcher(normalizedMatcher, flagArgList, jsonObject);
            } else {
                flagArgList.add(ArgumentUtil.createTagArgument(normalizedMatcher));
                flagArgList.add(CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(jsonObject, Fields.values.name(), Commands.HASFLAG.getCommandName())));
            }
            return NotTestCommandUtil.wrapTestCommand(new TestCommand(Commands.HASFLAG, flagArgList, new ArrayList<TestCommand>()));
        }
        if (StartsOrEndsWithMatcherUtil.isSimplifiedMatcher(matcher)) {
            handleSimplifiedMatcher(matcher, flagArgList, jsonObject);
        } else {
            flagArgList.add(ArgumentUtil.createTagArgument(matcher));
            flagArgList.add(CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(jsonObject, Fields.values.name(), Commands.HASFLAG.getCommandName())));
        }
        return new TestCommand(Commands.HASFLAG, flagArgList, new ArrayList<TestCommand>());
    }

    @Override
    void handleSimplifiedMatcher(String matcher, List<Object> argList, JSONObject data) throws JSONException, OXException{
        StartsOrEndsWithMatcherUtil.insertMatchesMatcher(argList);
        List<String> list = CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(data, Fields.values.name(), Commands.ENVELOPE.getCommandName()));
        StartsOrEndsWithMatcherUtil.insertValuesArgumentWithWildcards(list, matcher, argList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void parse(JSONObject jsonObject, TestCommand command, boolean transformToNotMatcher) throws JSONException {
        jsonObject.put(GeneralField.id.name(), command.getCommand().getCommandName());
        String matchType = command.getMatchType();
        List<String> values = (List<String>) command.getArguments().get(command.getTagArguments().size());
        MatchType type;
        if (matchType == null) {
            jsonObject.put(Fields.comparison.name(), MatchType.is.name());
            type = MatchType.is;
        } else {
            matchType = matchType.substring(1);
            type = MatchType.valueOf(matchType);
            type = StartsOrEndsWithMatcherUtil.checkMatchType(type, values);
            if (transformToNotMatcher){
                jsonObject.put(Fields.comparison.name(), type.getNotName());
            } else {
                jsonObject.put(Fields.comparison.name(), type.name());
            }
        }

        jsonObject.put(Fields.values.name(), new JSONArray(StartsOrEndsWithMatcherUtil.retrieveListForMatchType(values, type)));

    }
}
