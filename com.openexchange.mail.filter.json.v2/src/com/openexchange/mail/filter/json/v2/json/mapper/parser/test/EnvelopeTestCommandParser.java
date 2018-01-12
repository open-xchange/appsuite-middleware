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
    public void parse(JSONObject jsonObject, TestCommand command, boolean transformToNotMatcher) throws JSONException, OXException {
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
