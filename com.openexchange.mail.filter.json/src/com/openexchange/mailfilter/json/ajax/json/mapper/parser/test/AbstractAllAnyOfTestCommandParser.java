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

package com.openexchange.mailfilter.json.ajax.json.mapper.parser.test;

import java.util.ArrayList;
import org.apache.jsieve.SieveException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.mailfilter.json.ajax.json.fields.AllOfOrAnyOfTestField;
import com.openexchange.mailfilter.json.ajax.json.fields.GeneralField;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParserRegistry;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.TestCommandParserRegistry;
import com.openexchange.mailfilter.json.osgi.Services;

/**
 * {@link AbstractAllAnyOfTestCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractAllAnyOfTestCommandParser implements CommandParser<TestCommand> {

    /**
     * Initialises a new {@link AbstractAllAnyOfTestCommandParser}.
     */
    public AbstractAllAnyOfTestCommandParser() {
        super();
    }

    TestCommand parse(JSONObject jsonObject, Commands command) throws OXException, JSONException, SieveException {
        final JSONArray jarray = CommandParserJSONUtil.getJSONArray(jsonObject, AllOfOrAnyOfTestField.tests.name(), command.getCommandName());
        final ArrayList<TestCommand> commandlist = new ArrayList<TestCommand>(jarray.length());
        CommandParserRegistry<TestCommand> parserRegistry = Services.getService(TestCommandParserRegistry.class);

        for (int i = 0; i < jarray.length(); i++) {
            final JSONObject object = jarray.getJSONObject(i);
            String commandName = CommandParserJSONUtil.getString(object, GeneralField.id.name(), command.getCommandName());
            CommandParser<TestCommand> parser = parserRegistry.get(commandName);
            commandlist.add(parser.parse(object));
        }
        return new TestCommand(command, new ArrayList<Object>(), commandlist);
    }

    void parse(JSONObject jsonObject, TestCommand testCommand, Commands command) throws JSONException, OXException {
        jsonObject.put(GeneralField.id.name(), command.getCommandName());
        final JSONArray array = new JSONArray();
        CommandParserRegistry<TestCommand> parserRegistry = Services.getService(TestCommandParserRegistry.class);
        for (final TestCommand testCommand2 : testCommand.getTestCommands()) {
            final JSONObject object = new JSONObject();
            CommandParser<TestCommand> parser = parserRegistry.get(testCommand2.getCommand().getCommandName());
            parser.parse(object, testCommand2);
            array.put(object);
        }
        jsonObject.put(AllOfOrAnyOfTestField.tests.name(), array);
    }
}
