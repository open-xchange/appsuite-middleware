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
 *     Copyright (C) 2017-2020 OX Software GmbH
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
import org.apache.jsieve.TagArgument;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.mailfilter.json.ajax.json.fields.DateTestField;
import com.openexchange.mailfilter.json.ajax.json.fields.GeneralField;
import com.openexchange.mailfilter.json.ajax.json.mapper.ArgumentUtil;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractDateTestCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractDateTestCommandParser {

    private enum Comparison {
        is, ge, le
    }

    private enum DatePart {
        date, time, weekday
    }

    private final static String dateFormatPattern = "yyyy-MM-dd";
    private final static String timeFormatPattern = "HH:mm";

    /**
     * Parses the zone tag
     *
     * @param argList The argument list
     * @param jsonObject the {@link JSONObject}
     * @param session The session
     * @param commandName The command's name
     * @throws OXException if a parsing error is occurred
     */
    void parseZone(List<Object> argList, JSONObject jsonObject, ServerSession session, String commandName) throws OXException {
        if (jsonObject.hasAndNotNull(DateTestField.zone.name())) {
            argList.add(ArgumentUtil.createTagArgument("zone"));
            String zone = CommandParserJSONUtil.getString(jsonObject, DateTestField.zone.name(), commandName);
            argList.add(CommandParserJSONUtil.stringToList(zone));
        } else {
            // add the zone tag
            if (session != null && session.getUser().getTimeZone() != null) {
                argList.add(ArgumentUtil.createTagArgument("zone"));
                TimeZone tZone = TimeZone.getTimeZone(session.getUser().getTimeZone());
                String zone = String.format("%+03d%02d", tZone.getRawOffset() / 3600000, Math.abs((tZone.getRawOffset() / 60000) % 60));
                argList.add(CommandParserJSONUtil.stringToList(zone));
            }
        }
    }


    private static final TagArgument ZONE_TAG = ArgumentUtil.createTagArgument("zone");

    /**
     * Parses the zone tag
     *
     * @param jsonObject the {@link JSONObject}
     * @param command the test command
     * @throws JSONException if a JSON error is occurred
     */
    void parseZone(JSONObject jsonObject, TestCommand command) throws JSONException {
        for(int x=0; x<command.getArguments().size(); x++){
            Object arg = command.getArguments().get(x);
            if(ZONE_TAG.equals(arg)){
                Object zoneArgument = command.getArguments().get(x + 1);
                if(zoneArgument instanceof List<?>){
                    jsonObject.put(DateTestField.zone.name(), ((List<?>) zoneArgument).get(0));
                } else {
                    jsonObject.put(DateTestField.zone.name(), zoneArgument);
                }
                return;
            }
        }
    }

    /**
     * Parses the comparison tag
     *
     * @param argList The argument list
     * @param jsonObject the {@link JSONObject}
     * @param commandName The command's name
     * @throws OXException if a parsing error is occurred
     */
    void parseComparisonTag(List<Object> argList, JSONObject jsonObject, String commandName) throws OXException {
        // Parse the comparison tag
        final String comparisonTag = CommandParserJSONUtil.getString(jsonObject, DateTestField.comparison.name(), commandName);
        Comparison comparison = Comparison.valueOf(comparisonTag);
        switch (comparison) {
            case ge:
                argList.add(ArgumentUtil.createTagArgument("value"));
                argList.add(CommandParserJSONUtil.stringToList("ge"));
                break;
            case is:
                argList.add(ArgumentUtil.createTagArgument(comparison.name()));
                break;
            case le:
                argList.add(ArgumentUtil.createTagArgument("value"));
                argList.add(CommandParserJSONUtil.stringToList("le"));
                break;
            default:
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create(commandName + " rule: The comparison \"" + comparison + "\" is not a valid comparison");
        }
    }

    /**
     * Parses the header tag
     *
     * @param argList The argument list
     * @param jsonObject the {@link JSONObject}
     * @param commandName The command's name
     * @throws OXException if a parsing error is occurred
     */
    void parseHeader(List<Object> argList, JSONObject jsonObject, String commandName) throws OXException {
        // Parse the header
        String header = CommandParserJSONUtil.getString(jsonObject, DateTestField.header.name(), commandName);
        argList.add(CommandParserJSONUtil.stringToList(header));
    }

    /**
     * Parses the date part
     *
     * @param argList The argument list
     * @param jsonObject the {@link JSONObject}
     * @param commandName The command's name
     * @throws OXException if a parsing error is occurred
     * @throws JSONException if a JSON error is occurred
     */
    void parseDatePart(List<Object> argList, JSONObject jsonObject, String commandName) throws OXException, JSONException {
        // Parse the date part
        final String datepart = CommandParserJSONUtil.getString(jsonObject, DateTestField.datepart.name(), commandName);
        DatePart datePart = DatePart.valueOf(datepart);
        switch (datePart) {
            case date:
                argList.add(CommandParserJSONUtil.stringToList(datepart));
                argList.add(JSONDateArrayToStringList(CommandParserJSONUtil.getJSONArray(jsonObject, DateTestField.datevalue.name(), commandName), dateFormatPattern));
                break;
            case time:
                argList.add(CommandParserJSONUtil.stringToList(datepart));
                argList.add(JSONDateArrayToStringList(CommandParserJSONUtil.getJSONArray(jsonObject, DateTestField.datevalue.name(), commandName), timeFormatPattern));
                break;
            case weekday:
                argList.add(CommandParserJSONUtil.stringToList(datepart));
                argList.add(CommandParserJSONUtil.coerceToStringList(CommandParserJSONUtil.getJSONArray(jsonObject, DateTestField.datevalue.name(), commandName)));
                break;
            default:
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create(commandName + " rule: The datepart \"" + datepart + "\" is not a valid datepart");
        }
    }

    /**
     * Parses the comparison tag
     *
     * @param jsonObject the {@link JSONObject}
     * @param command the test command
     * @throws JSONException if a JSON error is occurred
     */
    @SuppressWarnings("unchecked")
    void parseComparisonTag(JSONObject jsonObject, TestCommand command) throws JSONException {
        jsonObject.put(GeneralField.id.name(), command.getCommand().getCommandName());
        final String comparison = command.getMatchType().substring(1);
        if ("value".equals(comparison)) {
            int compPos = command.getTagArguments().size() == 1 ? 1 : 3;
            jsonObject.put(DateTestField.comparison.name(), ((List<String>) command.getArguments().get(compPos)).get(0));
        } else {
            jsonObject.put(DateTestField.comparison.name(), comparison);
        }
    }

    /**
     * Parses the header tag
     *
     * @param jsonObject the {@link JSONObject}
     * @param command the test command
     * @throws JSONException if a JSON error is occurred
     */
    @SuppressWarnings("unchecked")
    void parseHeader(JSONObject jsonObject, TestCommand command) throws JSONException {
        final List<String> headers = (List<String>) command.getArguments().get(command.getArguments().size() - 3);
        String header = headers.get(0);
        jsonObject.put(DateTestField.header.name(), header);
    }

    /**
     * Parses the date part
     *
     * @param jsonObject the {@link JSONObject}
     * @param command the test command
     * @throws OXException if a parsing error is occurred
     * @throws JSONException if a JSON error is occurred
     */
    @SuppressWarnings("unchecked")
    void parseDatePart(JSONObject jsonObject, TestCommand command) throws JSONException, OXException {
        final List<String> value = (List<String>) command.getArguments().get(command.getArguments().size() - 2);
        String datepart = value.get(0);
        jsonObject.put(DateTestField.datepart.name(), datepart);

        DatePart datePart = DatePart.valueOf(datepart);
        int index = command.getArguments().size() - 1;
        switch (datePart) {
            case date:
                jsonObject.put(DateTestField.datevalue.name(), getJSONDateArray((List<String>) command.getArguments().get(index), dateFormatPattern));
                break;
            case time:
                jsonObject.put(DateTestField.datevalue.name(), getJSONDateArray((List<String>) command.getArguments().get(index), timeFormatPattern));
                break;
            case weekday:
                jsonObject.put(DateTestField.datevalue.name(), new JSONArray((List<String>) command.getArguments().get(index)));
                break;
            default:
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create("Date rule: The datepart \"" + datepart + "\" is not a valid datepart");
        }
    }

    /**
     * Converts the specified {@link JSONArray} to {@link String} {@link List} with the date
     *
     * @param jarray The {@link JSONArray} to convert
     * @param formatPattern The format pattern of the date
     * @return The {@link List} with the date
     * @throws JSONException if a parsing error is occurred
     */
    private List<String> JSONDateArrayToStringList(JSONArray jarray, String formatPattern) throws JSONException {
        int length = jarray.length();
        List<String> retval = new ArrayList<String>(length);
        for (int i = 0; i < length; i++) {
            retval.add(convertJSONDate2Sieve(jarray.getString(i), formatPattern));
        }
        return retval;
    }

    /**
     * Converts the specified string to a Sieve compatible date
     *
     * @param string The JSON string
     * @param formatPattern The format pattern
     * @return The Sieve date
     * @throws JSONException if a parsing error is occurred
     */
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

    /**
     * Retrieves from the specified {@link List} the date according to
     * the specified pattern and returns it as a {@link JSONArray}
     *
     * @param collection The {@link List} that contains
     * @param formatPattern The format pattern
     * @return The {@link JSONArray} with the date
     * @throws JSONException if a parsing error is occurred
     */
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
