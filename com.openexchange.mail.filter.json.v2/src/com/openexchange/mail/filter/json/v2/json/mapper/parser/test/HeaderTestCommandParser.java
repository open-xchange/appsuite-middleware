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
import com.openexchange.java.Strings;
import com.openexchange.jsieve.commands.MatchType;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.mail.filter.json.v2.json.fields.GeneralField;
import com.openexchange.mail.filter.json.v2.json.fields.HeaderTestField;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.CommandParserRegistry;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.TestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.TestCommandParserRegistry;
import com.openexchange.mail.filter.json.v2.mapper.ArgumentUtil;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link HeaderTestCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 */
public class HeaderTestCommandParser extends AbstractSimplifiedMatcherAwareCommandParser {

    /**
     * Initialises a new {@link HeaderTestCommandParser}.
     */
    public HeaderTestCommandParser(ServiceLookup services) {
        super(services, Commands.HEADER);
    }

    @Override
    public TestCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {
        final List<Object> argList = new ArrayList<Object>();
        String matcher = CommandParserJSONUtil.getString(jsonObject, HeaderTestField.comparison.name(), Commands.HEADER.getCommandName());
        String normalizedMatcher = MatchType.getNormalName(matcher);

        String comparison = Strings.isEmpty(normalizedMatcher) ? matcher : normalizedMatcher;
        boolean wrap = MatchType.valueOf(comparison).getNotName().equals(matcher);

        if (StartsOrEndsWithMatcherUtil.isSimplifiedMatcher(comparison)) {
            handleSimplifiedMatcher(comparison, argList, jsonObject);
        } else if (TestCommand.Commands.EXISTS.getCommandName().equals(comparison)) {
            CommandParserRegistry<TestCommand, TestCommandParser<TestCommand>> parserRegistry = services.getService(TestCommandParserRegistry.class);
            TestCommandParser<TestCommand> testCommandParser = parserRegistry.get(comparison);
            TestCommand testCommand = testCommandParser.parse(jsonObject, session);
            return wrap ? NotTestCommandUtil.wrapTestCommand(testCommand) : testCommand;
        } else {
            argList.add(ArgumentUtil.createTagArgument(comparison));
            argList.add(CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(jsonObject, HeaderTestField.headers.name(), Commands.HEADER.getCommandName())));
            argList.add(CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(jsonObject, HeaderTestField.values.name(), Commands.HEADER.getCommandName())));
        }

        TestCommand testCommand = new TestCommand(Commands.HEADER, argList, new ArrayList<TestCommand>());
        return wrap ? NotTestCommandUtil.wrapTestCommand(testCommand) : testCommand;
    }

    @Override
    void handleSimplifiedMatcher(String matcher, List<Object> argList, JSONObject data) throws JSONException, OXException {
        StartsOrEndsWithMatcherUtil.insertMatchesMatcher(argList);
        argList.add(CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(data, HeaderTestField.headers.name(), Commands.HEADER.getCommandName())));
        List<String> list = CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(data, HeaderTestField.values.name(), Commands.ENVELOPE.getCommandName()));
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
            jsonObject.put(HeaderTestField.comparison.name(), MatchType.is.name());
            type = MatchType.is;
        } else {
            matchType = matchType.substring(1);
            type = MatchType.valueOf(matchType);
            type = StartsOrEndsWithMatcherUtil.checkMatchType(type, values);
            jsonObject.put(HeaderTestField.comparison.name(), transformToNotMatcher ? type.getNotName() : type.name());
        }
        jsonObject.put(HeaderTestField.headers.name(), new JSONArray((List<?>) command.getArguments().get(command.getTagArguments().size())));
        jsonObject.put(HeaderTestField.values.name(), new JSONArray(StartsOrEndsWithMatcherUtil.retrieveListForMatchType(values, type)));
    }
}
