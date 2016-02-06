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

package com.openexchange.mailfilter.json.ajax.json.mapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;
import org.apache.jsieve.SieveException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.IfCommand;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.mailfilter.json.ajax.json.fields.AddressEnvelopeAndHeaderTestField;
import com.openexchange.mailfilter.json.ajax.json.fields.AllOfOrAnyOfTestField;
import com.openexchange.mailfilter.json.ajax.json.fields.BodyTestField;
import com.openexchange.mailfilter.json.ajax.json.fields.CurrentDateTestField;
import com.openexchange.mailfilter.json.ajax.json.fields.GeneralField;
import com.openexchange.mailfilter.json.ajax.json.fields.NotTestField;
import com.openexchange.mailfilter.json.ajax.json.fields.RuleField;
import com.openexchange.mailfilter.json.ajax.json.fields.SizeTestField;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * {@link TestCommandRuleFieldMapper}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class TestCommandRuleFieldMapper implements RuleFieldMapper {

    private final static String dateFormatPattern = "yyyy-MM-dd";
    private final static String timeFormatPattern = "HH:mm";
    private final static Pattern DIGITS = Pattern.compile("^\\-?\\d+$");

    /**
     * Initialises a new {@link TestCommandRuleFieldMapper}.
     */
    public TestCommandRuleFieldMapper() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.json.ajax.json.RuleFieldMapper#getAttributeName()
     */
    @Override
    public RuleField getAttributeName() {
        return RuleField.test;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.json.ajax.json.RuleFieldMapper#isNull(com.openexchange.jsieve.commands.Rule)
     */
    @Override
    public boolean isNull(Rule rule) {
        return rule.getTestCommand() == null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.json.ajax.json.RuleFieldMapper#getAttribute(com.openexchange.jsieve.commands.Rule)
     */
    @Override
    public Object getAttribute(Rule rule) throws JSONException {
        JSONObject object = new JSONObject();
        if (!isNull(rule)) {
            TestCommand testCommand = rule.getTestCommand();
            createJSONFromTestCommand(object, testCommand);
        }
        return object;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.json.ajax.json.RuleFieldMapper#setAttribute(com.openexchange.jsieve.commands.Rule, java.lang.Object)
     */
    @Override
    public void setAttribute(Rule rule, Object attribute) throws JSONException, SieveException, OXException {
        JSONObject object = (JSONObject) attribute;
        String id = object.getString(GeneralField.id.name());

        TestCommand existingTestCommand = rule.getTestCommand();
        TestCommand parsedTestCommand = createTestCommandFromJSON(object, id);
        if (existingTestCommand == null) {
            if (rule.getCommands().isEmpty()) {
                rule.addCommand(new IfCommand(parsedTestCommand));
            }
        } else {
            rule.getIfCommand().setTestcommand(parsedTestCommand);
        }
    }

    ////////////////////////////////////////////////////// HELPERS //////////////////////////////////////////////////////

    /**
     * This method is used to create a JSON object from a TestCommand. It is done this way because a separate
     * converter class would have to do the check for the right TestCommand for each id.
     *
     * @param tmp the JSONObject into which the values are written
     * @param testCommand the TestCommand itself
     * @throws JSONException
     */
    private void createJSONFromTestCommand(final JSONObject tmp, final TestCommand testCommand) throws JSONException {
        if (null != testCommand) {
            if (TestCommand.Commands.ADDRESS.equals(testCommand.getCommand())) {
                tmp.put(GeneralField.id.name(), TestCommand.Commands.ADDRESS.getCommandname());
                tmp.put(AddressEnvelopeAndHeaderTestField.comparison.name(), testCommand.getMatchtype().substring(1));
                tmp.put(AddressEnvelopeAndHeaderTestField.headers.name(), new JSONArray((List) testCommand.getArguments().get(testCommand.getTagarguments().size())));
                tmp.put(AddressEnvelopeAndHeaderTestField.values.name(), new JSONArray((List) testCommand.getArguments().get(testCommand.getTagarguments().size() + 1)));
            } else if (TestCommand.Commands.ENVELOPE.equals(testCommand.getCommand())) {
                tmp.put(GeneralField.id.name(), TestCommand.Commands.ENVELOPE.getCommandname());
                tmp.put(AddressEnvelopeAndHeaderTestField.comparison.name(), testCommand.getMatchtype().substring(1));
                tmp.put(AddressEnvelopeAndHeaderTestField.headers.name(), new JSONArray((List) testCommand.getArguments().get(testCommand.getTagarguments().size())));
                tmp.put(AddressEnvelopeAndHeaderTestField.values.name(), new JSONArray((List) testCommand.getArguments().get(testCommand.getTagarguments().size() + 1)));
            } else if (TestCommand.Commands.TRUE.equals(testCommand.getCommand())) {
                tmp.put(GeneralField.id.name(), TestCommand.Commands.TRUE.getCommandname());
            } else if (TestCommand.Commands.NOT.equals(testCommand.getCommand())) {
                tmp.put(GeneralField.id.name(), TestCommand.Commands.NOT.getCommandname());
                final JSONObject testobject = new JSONObject();
                createJSONFromTestCommand(testobject, testCommand.getTestcommands().get(0));
                tmp.put(NotTestField.test.name(), testobject);
            } else if (TestCommand.Commands.SIZE.equals(testCommand.getCommand())) {
                tmp.put(GeneralField.id.name(), TestCommand.Commands.SIZE.getCommandname());
                tmp.put(SizeTestField.comparison.name(), testCommand.getMatchtype().substring(1));
                tmp.put(SizeTestField.size.name(), Long.parseLong(testCommand.getArguments().get(1).toString()));
            } else if (TestCommand.Commands.HEADER.equals(testCommand.getCommand())) {
                tmp.put(GeneralField.id.name(), TestCommand.Commands.HEADER.getCommandname());
                tmp.put(AddressEnvelopeAndHeaderTestField.comparison.name(), testCommand.getMatchtype().substring(1));
                tmp.put(AddressEnvelopeAndHeaderTestField.headers.name(), new JSONArray((List) testCommand.getArguments().get(testCommand.getTagarguments().size())));
                tmp.put(AddressEnvelopeAndHeaderTestField.values.name(), new JSONArray((List) testCommand.getArguments().get(testCommand.getTagarguments().size() + 1)));
            } else if (TestCommand.Commands.BODY.equals(testCommand.getCommand())) {
                tmp.put(GeneralField.id.name(), TestCommand.Commands.BODY.getCommandname());
                tmp.put(BodyTestField.comparison.name(), testCommand.getMatchtype().substring(1));
                final String extensionkey = testCommand.getTagarguments().get(1).substring(1);
                tmp.put(BodyTestField.extensionskey.name(), extensionkey);
                if ("content".equals(extensionkey)) {
                    // TODO: This part should be tested for correct operation, our GUI doesn't use this, but this is
                    // allowed according to our specification
                    tmp.put(BodyTestField.extensionsvalue.name(), testCommand.getArguments().get(2));
                    tmp.put(BodyTestField.values.name(), new JSONArray((List) testCommand.getArguments().get(3)));
                } else {
                    tmp.put(BodyTestField.extensionsvalue.name(), JSONObject.NULL);
                    tmp.put(BodyTestField.values.name(), new JSONArray((List) testCommand.getArguments().get(2)));
                }
            } else if (TestCommand.Commands.CURRENTDATE.equals(testCommand.getCommand())) {
                tmp.put(GeneralField.id.name(), testCommand.getCommand().getCommandname());
                final String comparison = testCommand.getMatchtype().substring(1);
                if ("value".equals(comparison)) {
                    tmp.put(CurrentDateTestField.comparison.name(), ((List) testCommand.getArguments().get(testCommand.getTagarguments().size())).get(0));
                } else {
                    tmp.put(CurrentDateTestField.comparison.name(), comparison);
                }
                final List value = (List) testCommand.getArguments().get(testCommand.getArguments().size() - 2);
                tmp.put(CurrentDateTestField.datepart.name(), value.get(0));
                if ("date".equals(value.get(0))) {
                    tmp.put(CurrentDateTestField.datevalue.name(), getJSONDateArray((List) testCommand.getArguments().get(testCommand.getArguments().size() - 1), dateFormatPattern));
                } else if ("time".equals(value.get(0))) {
                    tmp.put(CurrentDateTestField.datevalue.name(), getJSONDateArray((List) testCommand.getArguments().get(testCommand.getArguments().size() - 1), timeFormatPattern));
                } else {
                    tmp.put(CurrentDateTestField.datevalue.name(), new JSONArray((List) testCommand.getArguments().get(testCommand.getArguments().size() - 1)));
                }
            } else if (TestCommand.Commands.ALLOF.equals(testCommand.getCommand())) {
                createAllofOrAnyofObjects(tmp, testCommand, TestCommand.Commands.ALLOF);
            } else if (TestCommand.Commands.ANYOF.equals(testCommand.getCommand())) {
                createAllofOrAnyofObjects(tmp, testCommand, TestCommand.Commands.ANYOF);
            }
        }
    }

    private void createAllofOrAnyofObjects(final JSONObject tmp, final TestCommand testCommand, final Commands command) throws JSONException {
        tmp.put(GeneralField.id.name(), command.getCommandname());
        final JSONArray array = new JSONArray();
        for (final TestCommand testCommand2 : testCommand.getTestcommands()) {
            final JSONObject object = new JSONObject();
            createJSONFromTestCommand(object, testCommand2);
            array.put(object);
        }
        tmp.put(AllOfOrAnyOfTestField.tests.name(), array);
    }

    private TestCommand createTestCommandFromJSON(final JSONObject jobj, final String id) throws JSONException, SieveException, OXException {
        if (TestCommand.Commands.ADDRESS.getCommandname().equals(id)) {
            return createAddressEnvelopeOrHeaderTest(jobj, TestCommand.Commands.ADDRESS);
        } else if (TestCommand.Commands.ENVELOPE.getCommandname().equals(id)) {
            return createAddressEnvelopeOrHeaderTest(jobj, TestCommand.Commands.ENVELOPE);
        } else if (TestCommand.Commands.TRUE.getCommandname().equals(id)) {
            return new TestCommand(TestCommand.Commands.TRUE, new ArrayList<Object>(), new ArrayList<TestCommand>());
        } else if (TestCommand.Commands.NOT.getCommandname().equals(id)) {
            final List<Object> argList = new ArrayList<Object>();
            final JSONArray valuesArray = jobj.getJSONArray(AddressEnvelopeAndHeaderTestField.values.name());
            argList.add(JSONArrayToStringList(valuesArray));
            return new TestCommand(TestCommand.Commands.NOT, argList, new ArrayList<TestCommand>());
        } else if (TestCommand.Commands.SIZE.getCommandname().equals(id)) {
            final String size = getString(jobj, SizeTestField.size.name(), id);
            try {
                if (false == DIGITS.matcher(size).matches()) {
                    throw OXJSONExceptionCodes.CONTAINS_NON_DIGITS.create(size, id);
                }
                final List<Object> argList = new ArrayList<Object>();
                argList.add(ArgumentUtil.createTagArgument(getString(jobj, SizeTestField.comparison.name(), id)));
                argList.add(ArgumentUtil.createNumberArgument(size));
                return new TestCommand(TestCommand.Commands.SIZE, argList, new ArrayList<TestCommand>());
            } catch (NumberFormatException e) {
                throw OXJSONExceptionCodes.TOO_BIG_NUMBER.create(e, id);
            }
        } else if (TestCommand.Commands.HEADER.getCommandname().equals(id)) {
            return createAddressEnvelopeOrHeaderTest(jobj, TestCommand.Commands.HEADER);
        } else if (TestCommand.Commands.BODY.getCommandname().equals(id)) {
            final List<Object> argList = new ArrayList<Object>();
            argList.add(ArgumentUtil.createTagArgument(getString(jobj, BodyTestField.comparison.name(), id)));
            final String extensionkey = getString(jobj, BodyTestField.extensionskey.name(), id);
            if (null != extensionkey) {
                if (extensionkey.equals("text")) {
                    argList.add(ArgumentUtil.createTagArgument("text"));
                } else if (extensionkey.equals("content")) {
                    // TODO: This part should be tested for correct operation, our GUI doesn't use this, but this is
                    // allowed according to our specification
                    argList.add(ArgumentUtil.createTagArgument("content"));
                    final String extensionvalue = getString(jobj, BodyTestField.extensionsvalue.name(), id);
                    argList.add(extensionvalue);
                } else {
                    throw OXJSONExceptionCodes.JSON_READ_ERROR.create("Body rule: The extensionskey " + extensionkey + " is not a valid extensionkey");
                }
            }
            argList.add(JSONArrayToStringList(getJSONArray(jobj, AddressEnvelopeAndHeaderTestField.values.name(), id)));
            return new TestCommand(TestCommand.Commands.BODY, argList, new ArrayList<TestCommand>());
        } else if (TestCommand.Commands.CURRENTDATE.getCommandname().equals(id)) {
            final List<Object> argList = new ArrayList<Object>();
            final String comparison = getString(jobj, CurrentDateTestField.comparison.name(), id);
            if ("is".equals(comparison)) {
                argList.add(ArgumentUtil.createTagArgument(comparison));
            } else if ("ge".equals(comparison)) {
                argList.add(ArgumentUtil.createTagArgument("value"));
                argList.add(getArrayFromString("ge"));
            } else if ("le".equals(comparison)) {
                argList.add(ArgumentUtil.createTagArgument("value"));
                argList.add(getArrayFromString("le"));
            } else {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create("Currentdate rule: The comparison \"" + comparison + "\" is not a valid comparison");
            }
            final String datepart = getString(jobj, CurrentDateTestField.datepart.name(), id);
            if ("date".equals(datepart)) {
                argList.add(getArrayFromString(datepart));
                argList.add(JSONDateArrayToStringList(getJSONArray(jobj, CurrentDateTestField.datevalue.name(), id), dateFormatPattern));
            } else if ("time".equals(datepart)) {
                argList.add(getArrayFromString(datepart));
                argList.add(JSONDateArrayToStringList(getJSONArray(jobj, CurrentDateTestField.datevalue.name(), id), timeFormatPattern));
            } else if ("weekday".equals(datepart)) {
                argList.add(getArrayFromString(datepart));
                argList.add(JSONArrayToStringList(getJSONArray(jobj, CurrentDateTestField.datevalue.name(), id)));
            } else {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create("Currentdate rule: The datepart \"" + datepart + "\" is not a valid datepart");
            }

            return new TestCommand(TestCommand.Commands.CURRENTDATE, argList, new ArrayList<TestCommand>());
        } else if (TestCommand.Commands.ALLOF.getCommandname().equals(id)) {
            return createAllOfOrAnyofTestCommand(jobj, id, TestCommand.Commands.ALLOF);
        } else if (TestCommand.Commands.ANYOF.getCommandname().equals(id)) {
            return createAllOfOrAnyofTestCommand(jobj, id, TestCommand.Commands.ANYOF);
        } else {
            throw new JSONException("Unknown test command while creating object: " + id);
        }
    }

    private TestCommand createAddressEnvelopeOrHeaderTest(final JSONObject jobj, final Commands command) throws JSONException, SieveException, OXException {
        final List<Object> argList = new ArrayList<Object>();
        argList.add(ArgumentUtil.createTagArgument(getString(jobj, AddressEnvelopeAndHeaderTestField.comparison.name(), command.getCommandname())));
        argList.add(JSONArrayToStringList(getJSONArray(jobj, AddressEnvelopeAndHeaderTestField.headers.name(), command.getCommandname())));
        argList.add(JSONArrayToStringList(getJSONArray(jobj, AddressEnvelopeAndHeaderTestField.values.name(), command.getCommandname())));
        return new TestCommand(command, argList, new ArrayList<TestCommand>());
    }

    private String getString(final JSONObject jobj, final String value, final String component) throws OXException {
        try {
            return jobj.getString(value);
        } catch (final JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, "Error while reading TestCommand " + component + ": " + e.getMessage());
        }
    }

    private JSONArray getJSONArray(final JSONObject jobj, final String value, final String component) throws OXException {
        try {
            return jobj.getJSONArray(value);
        } catch (final JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, "Error while reading TestCommand " + component + ": " + e.getMessage());
        }
    }

    private List<String> JSONArrayToStringList(JSONArray jarray) throws JSONException {
        int length = jarray.length();
        List<String> retval = new ArrayList<String>(length);
        for (int i = 0; i < length; i++) {
            retval.add(jarray.getString(i));
        }
        return retval;
    }

    private TestCommand createAllOfOrAnyofTestCommand(final JSONObject jobj, final String id, final Commands command) throws JSONException, SieveException, OXException {
        final JSONArray jarray = getJSONArray(jobj, AllOfOrAnyOfTestField.tests.name(), id);
        final ArrayList<TestCommand> commandlist = new ArrayList<TestCommand>(jarray.length());
        for (int i = 0; i < jarray.length(); i++) {
            final JSONObject object = jarray.getJSONObject(i);
            commandlist.add(createTestCommandFromJSON(object, getString(object, GeneralField.id.name(), id)));
        }
        return new TestCommand(command, new ArrayList<Object>(), commandlist);
    }

    private List<String> getArrayFromString(String string) {
        final List<String> retval = new ArrayList<String>();
        retval.add(string);
        return retval;
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
