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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2016 Open-Xchange, Inc.
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.apache.jsieve.SieveException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.mailfilter.json.ajax.json.fields.CurrentDateTestField;
import com.openexchange.mailfilter.json.ajax.json.fields.GeneralField;
import com.openexchange.mailfilter.json.ajax.json.mapper.ArgumentUtil;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * {@link CurrentDateTestCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CurrentDateTestCommandParser implements CommandParser<TestCommand> {

    private final static String dateFormatPattern = "yyyy-MM-dd";
    private final static String timeFormatPattern = "HH:mm";

    /**
     * Initialises a new {@link CurrentDateTestCommandParser}.
     */
    public CurrentDateTestCommandParser() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParser#parse(org.json.JSONObject)
     */
    @Override
    public TestCommand parse(JSONObject jsonObject) throws JSONException, SieveException, OXException {
        String commandName = Commands.CURRENTDATE.getCommandName();
        final List<Object> argList = new ArrayList<Object>();
        final String comparison = CommandParserJSONUtil.getString(jsonObject, CurrentDateTestField.comparison.name(), commandName);
        // FIXME: replace if/strings with switch/enum
        if ("is".equals(comparison)) {
            argList.add(ArgumentUtil.createTagArgument(comparison));
        } else if ("ge".equals(comparison)) {
            argList.add(ArgumentUtil.createTagArgument("value"));
            argList.add(CommandParserJSONUtil.stringToList("ge"));
        } else if ("le".equals(comparison)) {
            argList.add(ArgumentUtil.createTagArgument("value"));
            argList.add(CommandParserJSONUtil.stringToList("le"));
        } else {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create("Currentdate rule: The comparison \"" + comparison + "\" is not a valid comparison");
        }
        final String datepart = CommandParserJSONUtil.getString(jsonObject, CurrentDateTestField.datepart.name(), commandName);
        if ("date".equals(datepart)) {
            argList.add(CommandParserJSONUtil.stringToList(datepart));
            argList.add(JSONDateArrayToStringList(CommandParserJSONUtil.getJSONArray(jsonObject, CurrentDateTestField.datevalue.name(), commandName), dateFormatPattern));
        } else if ("time".equals(datepart)) {
            argList.add(CommandParserJSONUtil.stringToList(datepart));
            argList.add(JSONDateArrayToStringList(CommandParserJSONUtil.getJSONArray(jsonObject, CurrentDateTestField.datevalue.name(), commandName), timeFormatPattern));
        } else if ("weekday".equals(datepart)) {
            argList.add(CommandParserJSONUtil.stringToList(datepart));
            argList.add(CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(jsonObject, CurrentDateTestField.datevalue.name(), commandName)));
        } else {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create("Currentdate rule: The datepart \"" + datepart + "\" is not a valid datepart");
        }

        return new TestCommand(TestCommand.Commands.CURRENTDATE, argList, new ArrayList<TestCommand>());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParser#parse(org.json.JSONObject, java.lang.Object)
     */
    @Override
    public void parse(JSONObject jsonObject, TestCommand command) throws JSONException {
        jsonObject.put(GeneralField.id.name(), command.getCommand().getCommandName());
        final String comparison = command.getMatchType().substring(1);
        if ("value".equals(comparison)) {
            jsonObject.put(CurrentDateTestField.comparison.name(), ((List) command.getArguments().get(command.getTagArguments().size())).get(0));
        } else {
            jsonObject.put(CurrentDateTestField.comparison.name(), comparison);
        }
        final List value = (List) command.getArguments().get(command.getArguments().size() - 2);
        jsonObject.put(CurrentDateTestField.datepart.name(), value.get(0));
        if ("date".equals(value.get(0))) {
            jsonObject.put(CurrentDateTestField.datevalue.name(), getJSONDateArray((List) command.getArguments().get(command.getArguments().size() - 1), dateFormatPattern));
        } else if ("time".equals(value.get(0))) {
            jsonObject.put(CurrentDateTestField.datevalue.name(), getJSONDateArray((List) command.getArguments().get(command.getArguments().size() - 1), timeFormatPattern));
        } else {
            jsonObject.put(CurrentDateTestField.datevalue.name(), new JSONArray((List) command.getArguments().get(command.getArguments().size() - 1)));
        }
    }

    private List<String> JSONDateArrayToStringList(JSONArray jarray, String formatPattern) throws JSONException {
        int length = jarray.length();
        List<String> retval = new ArrayList<String>(length);
        for (int i = 0; i < length; i++) {
            retval.add(convertJSONDate2Sieve(jarray.getString(i), formatPattern));
        }
        return retval;
    }

    private String convertJSONDate2Sieve(final String string, final String formatPattern) throws JSONException {
        try {
            final Date date = new Date(Long.parseLong(string));
            final SimpleDateFormat df = new SimpleDateFormat(formatPattern);
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            return df.format(date);
        } catch (NumberFormatException e) {
            throw new JSONException("Date field \"" + string + "\" is no date value");
        }
    }

    private JSONArray getJSONDateArray(final List<String> collection, final String formatPattern) throws JSONException {
        final SimpleDateFormat df = new SimpleDateFormat(formatPattern);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        final JSONArray retval = new JSONArray();
        for (final String part : collection) {
            Date parse;
            try {
                parse = df.parse(part);
                retval.put(parse.getTime());
            } catch (ParseException e) {
                throw new JSONException("Error while parsing date from string \"" + part + "\"");
            }
        }
        return retval;
    }
}
