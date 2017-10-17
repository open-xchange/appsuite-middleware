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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import com.openexchange.mail.filter.json.v2.json.fields.ExistsTestField;
import com.openexchange.mail.filter.json.v2.json.fields.GeneralField;
import com.openexchange.mail.filter.json.v2.json.fields.HeaderTestField;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.simplified.SimplifiedHeaderTest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ExistsTestCommandParser} parses exists sieve tests.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.4
 */
public class ExistsTestCommandParser extends AbstractTestCommandParser {

    /**
     * Initialises a new {@link ExistsTestCommandParser}.
     */
    public ExistsTestCommandParser(ServiceLookup services) {
        super(services, Commands.EXISTS);
    }

    @Override
    public TestCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {
        String commandName = Commands.EXISTS.getCommandName();
        final List<Object> argList = new ArrayList<Object>();
        JSONArray array = CommandParserJSONUtil.getJSONArray(jsonObject, ExistsTestField.headers.name(), commandName);
        argList.add(CommandParserJSONUtil.coerceToStringList(array));
        return new TestCommand(TestCommand.Commands.EXISTS, argList, new ArrayList<TestCommand>());
    }

    @Override
    public void parse(JSONObject jsonObject, TestCommand command, boolean transformToNotMatcher) throws JSONException, OXException {
        // The exists test command only applies to 'header'
        jsonObject.put(GeneralField.id.name(), TestCommand.Commands.HEADER.getCommandName());
        jsonObject.put(HeaderTestField.comparison.name(), transformToNotMatcher ? MatchType.exists.getNotName() : MatchType.exists.name());

        // The fields 'headers' and 'values' are empty due to the simplified test,
        // i.e. the 'headers' are implicitly part of the 'id' field.
        jsonObject.put(HeaderTestField.headers.name(), new JSONArray());
        jsonObject.put(HeaderTestField.values.name(), new JSONArray());

        JSONArray headers = new JSONArray((List<?>) command.getArguments().get(0));
        boolean simplified = false;
        for (SimplifiedHeaderTest simplifiedTest : SimplifiedHeaderTest.values()) {
            simplified = TestCommandUtil.isSimplified(simplifiedTest, headers);
            if (simplified) {
                // Simplified detected, overwrite the 'id'
                jsonObject.put(GeneralField.id.name(), simplifiedTest.getCommandName());
                return;
            }
        }

        // No simplified test detected, therefore overwrite the headers with the custom header names
        if (!simplified) {
            jsonObject.put(ExistsTestField.headers.name(), headers);
        }
    }
}
