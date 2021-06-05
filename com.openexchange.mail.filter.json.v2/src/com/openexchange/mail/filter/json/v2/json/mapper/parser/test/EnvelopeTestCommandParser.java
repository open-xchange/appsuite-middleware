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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.jsieve.SieveException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.jsieve.commands.AddressParts;
import com.openexchange.jsieve.commands.EnvelopeParts;
import com.openexchange.jsieve.commands.MatchType;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.mail.filter.json.v2.json.fields.EnvelopeTestField;
import com.openexchange.mail.filter.json.v2.json.fields.GeneralField;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.ExtendedFieldTestCommandParser;
import com.openexchange.mail.filter.json.v2.mapper.ArgumentUtil;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link EnvelopeTestCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 */
public class EnvelopeTestCommandParser extends AbstractSimplifiedMatcherAwareCommandParser implements ExtendedFieldTestCommandParser {

    /**
     * Initializes a new {@link EnvelopeTestCommandParser}.
     */
    public EnvelopeTestCommandParser(ServiceLookup services) {
        super(services, Commands.ENVELOPE);
    }

    @Override
    public TestCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {

        final List<Object> argList = new ArrayList<Object>();
        String matcher = CommandParserJSONUtil.getString(jsonObject, EnvelopeTestField.comparison.name(), Commands.ENVELOPE.getCommandName());
        String normalizedMatcher = MatchType.getNormalName(matcher);

        String comparison = Strings.isEmpty(normalizedMatcher) ? matcher : normalizedMatcher;
        boolean wrap = MatchType.valueOf(comparison).getNotName().equals(matcher);

        if (StartsOrEndsWithMatcherUtil.isSimplifiedMatcher(comparison)) {
            handleSimplifiedMatcher(comparison, argList, jsonObject);
        } else {
            argList.add(ArgumentUtil.createTagArgument(comparison));
            if (jsonObject.hasAndNotNull(EnvelopeTestField.addresspart.name())) {
                argList.add(ArgumentUtil.createTagArgument(CommandParserJSONUtil.getString(jsonObject, EnvelopeTestField.addresspart.name(), Commands.ENVELOPE.getCommandName())));
            }
            argList.add(CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(jsonObject, EnvelopeTestField.headers.name(), Commands.ENVELOPE.getCommandName())));
            argList.add(CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(jsonObject, EnvelopeTestField.values.name(), Commands.ENVELOPE.getCommandName())));

        }
        TestCommand testCommand = new TestCommand(Commands.ENVELOPE, argList, new ArrayList<TestCommand>());
        return wrap ? NotTestCommandUtil.wrapTestCommand(testCommand) : testCommand;
    }

    @Override
    void handleSimplifiedMatcher(String matcher, List<Object> argList, JSONObject data) throws JSONException, OXException {
        StartsOrEndsWithMatcherUtil.insertMatchesMatcher(argList);
        if (data.hasAndNotNull(EnvelopeTestField.addresspart.name())) {
            argList.add(ArgumentUtil.createTagArgument(CommandParserJSONUtil.getString(data, EnvelopeTestField.addresspart.name(), Commands.ENVELOPE.getCommandName())));
        }
        argList.add(CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(data, EnvelopeTestField.headers.name(), Commands.ENVELOPE.getCommandName())));
        List<String> list = CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(data, EnvelopeTestField.values.name(), Commands.ENVELOPE.getCommandName()));
        StartsOrEndsWithMatcherUtil.insertValuesArgumentWithWildcards(list, matcher, argList);
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
                jsonObject.put(EnvelopeTestField.comparison.name(), type.getNotName());
            } else {
                jsonObject.put(EnvelopeTestField.comparison.name(), type.name());
            }
        }

        if (command.getAddressPart() != null) {
            jsonObject.put(EnvelopeTestField.addresspart.name(), command.getAddressPart().substring(1));
        }
        jsonObject.put(EnvelopeTestField.headers.name(), new JSONArray(toLowerCase((List<String>) command.getArguments().get(command.getTagArguments().size()))));
        jsonObject.put(EnvelopeTestField.values.name(), new JSONArray(StartsOrEndsWithMatcherUtil.retrieveListForMatchType(values, type)));

    }

    private List<String> toLowerCase(List<String> data) {
        List<String> result = new ArrayList<>(data.size());
        for (String str : data) {
            result.add(str.toLowerCase());
        }
        return result;
    }

    @Override
    public Map<String, Set<String>> getAddtionalFields(Set<String> capabilities) {
        Map<String, Set<String>> result = new HashMap<>(2);
        // add envelope parts
        Set<String> parts = new HashSet<>();
        for (EnvelopeParts part : EnvelopeParts.values()) {
            parts.add(part.name());
        }
        result.put("headers", parts);

        // add address parts
        Set<String> addressParts = new HashSet<>();
        for (AddressParts part : AddressParts.values()) {
            if (part.isValid(capabilities)) {
                addressParts.add(part.name());
            }
        }
        result.put("parts", addressParts);

        return result;
    }
}
