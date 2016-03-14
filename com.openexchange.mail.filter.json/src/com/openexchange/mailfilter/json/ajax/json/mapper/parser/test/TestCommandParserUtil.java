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
import java.util.List;
import org.apache.jsieve.SieveException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.mailfilter.json.ajax.json.fields.AddressEnvelopeAndHeaderTestField;
import com.openexchange.mailfilter.json.ajax.json.fields.GeneralField;
import com.openexchange.mailfilter.json.ajax.json.mapper.ArgumentUtil;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParserJSONUtil;

/**
 * {@link TestCommandParserUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
final class TestCommandParserUtil {

    /**
     * Multi-purpose method for creating {@link Commands#ADDRESS}, {@link Commands#ENVELOPE} and {@link Commands#HEADER} {@link TestCommand}s from the specified {@link JSONObject}
     * 
     * @param jsonObject The {@link JSONObject} that contains the command
     * @param command The {@link Commands} command to create
     * @return The newly created {@link TestCommand}
     * @throws JSONException if a JSON parsing error occurs
     * @throws SieveException if a Sieve parsing error occurs
     * @throws OXException if a semantic error occurs
     */
    static final TestCommand createAddressEnvelopeOrHeaderTest(final JSONObject jsonObject, final Commands command) throws JSONException, SieveException, OXException {
        final List<Object> argList = new ArrayList<Object>();
        argList.add(ArgumentUtil.createTagArgument(CommandParserJSONUtil.getString(jsonObject, AddressEnvelopeAndHeaderTestField.comparison.name(), command.getCommandName())));
        argList.add(CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(jsonObject, AddressEnvelopeAndHeaderTestField.headers.name(), command.getCommandName())));
        argList.add(CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(jsonObject, AddressEnvelopeAndHeaderTestField.values.name(), command.getCommandName())));
        return new TestCommand(command, argList, new ArrayList<TestCommand>());
    }

    /**
     * Multi-purpose method for filling the specified {@link JSONObject} with {@link Commands#ADDRESS}, {@link Commands#ENVELOPE} and {@link Commands#HEADER} {@link TestCommand}s
     * 
     * @param jsonObject The {@link JSONObject}
     * @param command The {@link TestCommand}
     * @throws JSONException if a JSON parsing error occurs
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    static final void fillWithAddressEnvelopeOrHeaderTest(JSONObject jsonObject, TestCommand command) throws JSONException {
        jsonObject.put(GeneralField.id.name(), command.getCommand().getCommandName());
        jsonObject.put(AddressEnvelopeAndHeaderTestField.comparison.name(), command.getMatchType().substring(1));
        jsonObject.put(AddressEnvelopeAndHeaderTestField.headers.name(), new JSONArray((List) command.getArguments().get(command.getTagArguments().size())));
        jsonObject.put(AddressEnvelopeAndHeaderTestField.values.name(), new JSONArray((List) command.getArguments().get(command.getTagArguments().size() + 1)));
    }
}
