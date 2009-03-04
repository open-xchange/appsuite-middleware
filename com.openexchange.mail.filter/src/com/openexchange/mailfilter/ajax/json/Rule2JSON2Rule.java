/*
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
package com.openexchange.mailfilter.ajax.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.jsieve.NumberArgument;
import org.apache.jsieve.SieveException;
import org.apache.jsieve.TagArgument;
import org.apache.jsieve.parser.generated.Token;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.jsieve.commands.IfCommand;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.jsieve.commands.RuleComment;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.mailfilter.ajax.fields.RuleFields;
import com.openexchange.tools.servlet.OXJSONException;

/**
 * @author cutmasta
 *
 */
public class Rule2JSON2Rule extends AbstractObject2JSON2Object<Rule> {

    private final static String[] RULE_FIELDS_LIST = {
        RuleFields.ID,
        RuleFields.POSITION,
        RuleFields.RULENAME,
        RuleFields.ACTIVE,
        RuleFields.FLAGS,
        RuleFields.TEST,
        RuleFields.ACTIONCMDS,
        RuleFields.TEXT,
        RuleFields.ERRORMSG
    };

    static class GeneralFields {
        final static String ID = "id";
    }

    private static class AddressEnvelopeAndHeaderTestFields {
        final static String COMPARISON = "comparison";
        final static String HEADERS = "headers";
        final static String VALUES = "values";
    }

    private static class NotTestFields {
        final static String TEST = "test";
    }

    private static class SizeTestFields {
        final static String COMPARISON = "comparison";
        final static String SIZE = "size";
    }

    private static class BodyTestFields {
        final static String COMPARISON = "comparison";
        final static String EXTENSIONSKEY ="extensionskey";
        final static String EXTENSIONSVALUE = "extensionsvalue";
        final static String VALUES = "values";
    }

    private static class AllofOrAnyOfTestFields {
        final static String TESTS = "tests";
    }

    static class RedirectActionFields {
        final static String TO = "to";
    }

    static class MoveActionFields {
        final static String INTO = "into";
    }

    static class RejectActionFields {
        final static String TEXT = "text";
    }

    enum VacationActionFields {
        DAYS("days",":days"),
        ADDRESSES("addresses",":addresses"),
        SUBJECT("subject",":subject"),
        TEXT("text",null);
        
        private final String fieldname;
        
        private final String tagname;
        
        private VacationActionFields(final String fieldname, final String tagname) {
            this.fieldname = fieldname;
            this.tagname = tagname;
        }

        /**
         * @return the fieldname
         */
        public final String getFieldname() {
            return fieldname;
        }

        /**
         * @return the tagname
         */
        public final String getTagname() {
            return tagname;
        }
        
        
    }
    
    static class AddFlagsActionFields {
        final static String FLAGS = "flags";
    }
    
    public Rule2JSON2Rule() {      
        super();
    }
    
    @Override
	protected Mapper<Rule>[] allMapper() {        
        return mappers;
    }
    
    @Override
	protected Rule createObject() {       
        return new Rule();
    }
    
    @Override
	protected String[] getListFields() {        
        return RULE_FIELDS_LIST;
    }
    
    private final static Map<String, Mapper<Rule>> attr2Mapper;

    @Override
	protected Mapper<Rule> getMapper(final String attrName) {
        return attr2Mapper.get(attrName);
    }
    
    static TagArgument createTagArg(final VacationActionFields fields) {
        final Token token = new Token();
        token.image = fields.getTagname();
        return new TagArgument(token);
    }

    private static TagArgument createTagArg(final String string) {
        final Token token = new Token();
        token.image = ":" + string;
        return new TagArgument(token);
    }
    
    static List<String> JSONArrayToStringList(final JSONArray jarray) throws JSONException {
        final ArrayList<String> retval = new ArrayList<String>(jarray.length());
        for (int i = 0; i < jarray.length(); i++) {
            retval.add(jarray.getString(i));
        }
        return retval;
    }
    
    static NumberArgument createNumberArg(final String string) {
        final Token token = new Token();
        token.image = string;
        return new NumberArgument(token);
    }

    private static final Mapper<Rule>[] mappers = new Mapper[] { 
        new IDMapper(),
        
        new Mapper<Rule>() {
            
            public String getAttrName() {
                return RuleFields.POSITION;
            }
            
            public Object getAttribute(final Rule obj) throws JSONException {
                return Integer.valueOf(obj.getPosition());
            }
            
            public boolean isNull(final Rule obj) {
                return -1 == obj.getPosition();
            }
            
            public void setAttribute(final Rule obj, final Object attr) throws JSONException {                
                obj.setPosition(((Integer)attr).intValue());
            }
        },
        
        new Mapper<Rule>() {

            public String getAttrName() {
                return RuleFields.RULENAME;
            }

            public Object getAttribute(final Rule obj) throws JSONException {
                final RuleComment ruleComment = obj.getRuleComment();
                if (null != ruleComment) {
                    final String rulename = ruleComment.getRulename();
                    return (null == rulename) ? JSONObject.NULL :rulename;
                }
				return JSONObject.NULL;
            }

            public boolean isNull(final Rule obj) {                
                return false;
            }

            public void setAttribute(final Rule obj, final Object attr) throws JSONException {                
                final RuleComment ruleComment = obj.getRuleComment();
                if (null != ruleComment) {
                    ruleComment.setRulename((String)attr);
                } else {
                    obj.setRuleComments(new RuleComment((String)attr));
                }
            }
        },
        
        new Mapper<Rule>() {

            public String getAttrName() {
                return RuleFields.ACTIVE;
            }

            public Object getAttribute(final Rule obj) throws JSONException {
                return Boolean.valueOf(!obj.isCommented());
            }

            public boolean isNull(final Rule obj) {                
                return false;
            }

            public void setAttribute(final Rule obj, final Object attr) throws JSONException {
                obj.setCommented(!((Boolean) attr).booleanValue());
            }
        },
        
        new Mapper<Rule>() {

            public String getAttrName() {
                return RuleFields.FLAGS;
            }

            public Object getAttribute(final Rule obj) throws JSONException {
                final JSONArray tmp = new JSONArray();
                final RuleComment name = obj.getRuleComment();
                if (null != name && null != name.getFlags()) {
                    for (final String string : name.getFlags()) {
                        tmp.put(string);
                    }
                }
                return tmp;
            }

            public boolean isNull(final Rule obj) {                
                return false;
            }

            public void setAttribute(final Rule rule, final Object obj) throws JSONException {
                final JSONArray array = (JSONArray) obj;
                final ArrayList<String> list = new ArrayList<String>(array.length());
                for (int i = 0; i < array.length(); i++) {
                    list.add(array.getString(i));
                }
                final RuleComment ruleComment = rule.getRuleComment();
                if (null != ruleComment) {
                    ruleComment.setFlags(list);
                } else {
                    rule.setRuleComments(new RuleComment(list));
                }
            }
        },

        new Mapper<Rule>() {

            public String getAttrName() {
                return RuleFields.TEST;
            }

            public Object getAttribute(final Rule obj) throws JSONException {
                final JSONObject tmp = new JSONObject();

                final TestCommand testCommand = obj.getTestCommand();
                createJSONFromTestCommand(tmp, testCommand);
                return tmp;
            }

            public boolean isNull(final Rule obj) {                
                return (null == obj.getTestCommand());
            }

            public void setAttribute(final Rule rule, final Object obj) throws JSONException, SieveException, OXJSONException {
                final JSONObject jobj = (JSONObject) obj;
                final String id = jobj.getString(GeneralFields.ID);
                final TestCommand testCommand = rule.getTestCommand();
                final TestCommand testCommand2 = createTestCommandFromJSON(jobj, id);
                if (null == testCommand) {
                    if (rule.getCommands().isEmpty()) {
                        rule.addCommand(new IfCommand(testCommand2));
                    }
                } else {
                    rule.getIfCommand().setTestcommand(testCommand2);
                }
            }

            private TestCommand createTestCommandFromJSON(final JSONObject jobj, final String id) throws JSONException, SieveException, OXJSONException {
                if (TestCommand.Commands.ADDRESS.getCommandname().equals(id)) {
                    return createAddressEnvelopeOrHeaderTest(jobj, TestCommand.Commands.ADDRESS);
                } else if (TestCommand.Commands.ENVELOPE.getCommandname().equals(id)) {
                    return createAddressEnvelopeOrHeaderTest(jobj, TestCommand.Commands.ENVELOPE);
                } else if (TestCommand.Commands.TRUE.getCommandname().equals(id)) {
                    return new TestCommand(TestCommand.Commands.TRUE, new ArrayList<Object>(), new ArrayList<TestCommand>());
                } else if (TestCommand.Commands.NOT.getCommandname().equals(id)) {
                    final List<Object> argList = new ArrayList<Object>();
                    final JSONArray valuesArray = jobj.getJSONArray(AddressEnvelopeAndHeaderTestFields.VALUES);
                    argList.add(JSONArrayToStringList(valuesArray));
                    return new TestCommand(TestCommand.Commands.NOT, argList, new ArrayList<TestCommand>());
                } else if (TestCommand.Commands.SIZE.getCommandname().equals(id)) {
                    final List<Object> argList = new ArrayList<Object>();
                    argList.add(createTagArg(getString(jobj, SizeTestFields.COMPARISON, id)));
                    argList.add(createNumberArg(getString(jobj, SizeTestFields.SIZE, id)));
                    return new TestCommand(TestCommand.Commands.SIZE, argList, new ArrayList<TestCommand>());
                } else if (TestCommand.Commands.HEADER.getCommandname().equals(id)) {
                    return createAddressEnvelopeOrHeaderTest(jobj, TestCommand.Commands.HEADER);
                } else if (TestCommand.Commands.BODY.getCommandname().equals(id)) {
                    final List<Object> argList = new ArrayList<Object>();
                    argList.add(createTagArg(getString(jobj, BodyTestFields.COMPARISON, id)));
                    final String extensionkey = getString(jobj, BodyTestFields.EXTENSIONSKEY, id);
                    if (null != extensionkey) {
                        if (extensionkey.equals("text")) {
                            argList.add(createTagArg("text"));
                        } else if (extensionkey.equals("content")) {
                            // TODO: This part should be tested for correct operation, our GUI doesn't use this, but this is
                            // allowed according to our specification
                            argList.add(createTagArg("content"));
                            final String extensionvalue = getString(jobj, BodyTestFields.EXTENSIONSVALUE, id);
                            if (null != extensionkey) {
                                argList.add(extensionvalue);
                            } else {
                                throw new OXJSONException(OXJSONException.Code.JSON_READ_ERROR, "Body rule: The extensionkey content needs a mime value but none given");
                            }
                        } else {
                            throw new OXJSONException(OXJSONException.Code.JSON_READ_ERROR, "Body rule: The extensionskey " + extensionkey + " is not a valid extensionkey");
                        }
                    }
                    argList.add(JSONArrayToStringList(getJSONArray(jobj, AddressEnvelopeAndHeaderTestFields.VALUES, id)));
                    return new TestCommand(TestCommand.Commands.BODY, argList, new ArrayList<TestCommand>());
                } else if (TestCommand.Commands.ALLOF.getCommandname().equals(id)) {
                    return createAllofOrAnyofTestCommand(jobj, id, TestCommand.Commands.ALLOF);
                } else if (TestCommand.Commands.ANYOF.getCommandname().equals(id)) {
                    return createAllofOrAnyofTestCommand(jobj, id, TestCommand.Commands.ANYOF);
                } else {
                    throw new JSONException("Unknown test command while creating object: " + id);
                }
            }

            private TestCommand createAllofOrAnyofTestCommand(final JSONObject jobj, final String id, final Commands command) throws JSONException, SieveException, OXJSONException {
                final JSONArray jarray = getJSONArray(jobj, AllofOrAnyOfTestFields.TESTS, id);
                final ArrayList<TestCommand> commandlist = new ArrayList<TestCommand>(jarray.length());
                for (int i = 0; i < jarray.length(); i++) {
                    final JSONObject object = jarray.getJSONObject(i);
                    commandlist.add(createTestCommandFromJSON(object, getString(object, GeneralFields.ID, id)));
                }
                return new TestCommand(command, new ArrayList<Object>(), commandlist);
            }
            
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
                        tmp.put(GeneralFields.ID, TestCommand.Commands.ADDRESS.getCommandname());
                        tmp.put(AddressEnvelopeAndHeaderTestFields.COMPARISON, testCommand.getMatchtype().substring(1));
                        tmp.put(AddressEnvelopeAndHeaderTestFields.HEADERS, new JSONArray((List) testCommand.getArguments().get(1)));
                        tmp.put(AddressEnvelopeAndHeaderTestFields.VALUES, new JSONArray((List) testCommand.getArguments().get(2)));
                    } else if (TestCommand.Commands.ENVELOPE.equals(testCommand.getCommand())) {
                        tmp.put(GeneralFields.ID, TestCommand.Commands.ENVELOPE.getCommandname());
                        tmp.put(AddressEnvelopeAndHeaderTestFields.COMPARISON, testCommand.getMatchtype().substring(1));
                        tmp.put(AddressEnvelopeAndHeaderTestFields.HEADERS, new JSONArray((List) testCommand.getArguments().get(1)));
                        tmp.put(AddressEnvelopeAndHeaderTestFields.VALUES, new JSONArray((List) testCommand.getArguments().get(2)));
                    } else if (TestCommand.Commands.TRUE.equals(testCommand.getCommand())) {
                        tmp.put(GeneralFields.ID, TestCommand.Commands.TRUE.getCommandname());
                    } else if (TestCommand.Commands.NOT.equals(testCommand.getCommand())) {
                        tmp.put(GeneralFields.ID, TestCommand.Commands.NOT.getCommandname());
                        final JSONObject testobject = new JSONObject();
                        createJSONFromTestCommand(testobject, testCommand.getTestcommands().get(0));
                        tmp.put(NotTestFields.TEST, testobject);
                    } else if (TestCommand.Commands.SIZE.equals(testCommand.getCommand())) {
                        tmp.put(GeneralFields.ID, TestCommand.Commands.SIZE.getCommandname());
                        tmp.put(SizeTestFields.COMPARISON, testCommand.getMatchtype().substring(1));
                        tmp.put(SizeTestFields.SIZE, Long.parseLong(testCommand.getArguments().get(1).toString()));
                    } else if (TestCommand.Commands.HEADER.equals(testCommand.getCommand())) {
                        tmp.put(GeneralFields.ID, TestCommand.Commands.HEADER.getCommandname());
                        tmp.put(AddressEnvelopeAndHeaderTestFields.COMPARISON, testCommand.getMatchtype().substring(1));
                        tmp.put(AddressEnvelopeAndHeaderTestFields.HEADERS, new JSONArray((List)testCommand.getArguments().get(1)));
                        tmp.put(AddressEnvelopeAndHeaderTestFields.VALUES, new JSONArray((List)testCommand.getArguments().get(2)));
                    } else if (TestCommand.Commands.BODY.equals(testCommand.getCommand())) {
                        tmp.put(GeneralFields.ID, TestCommand.Commands.BODY.getCommandname());
                        tmp.put(BodyTestFields.COMPARISON, testCommand.getMatchtype().substring(1));
                        final String extensionkey = testCommand.getTagarguments().get(1).substring(1);
                        tmp.put(BodyTestFields.EXTENSIONSKEY, extensionkey);
                        if ("content".equals(extensionkey)) {
                            // TODO: This part should be tested for correct operation, our GUI doesn't use this, but this is
                            // allowed according to our specification
                            tmp.put(BodyTestFields.EXTENSIONSVALUE, testCommand.getArguments().get(2));
                            tmp.put(BodyTestFields.VALUES, new JSONArray((List)testCommand.getArguments().get(3)));
                        } else {
                            tmp.put(BodyTestFields.EXTENSIONSVALUE, JSONObject.NULL);
                            tmp.put(BodyTestFields.VALUES, new JSONArray((List)testCommand.getArguments().get(2)));
                        }
                    } else if (TestCommand.Commands.ALLOF.equals(testCommand.getCommand())) {
                        createAllofOrAnyofObjects(tmp, testCommand, TestCommand.Commands.ALLOF);
                    } else if (TestCommand.Commands.ANYOF.equals(testCommand.getCommand())) {
                        createAllofOrAnyofObjects(tmp, testCommand, TestCommand.Commands.ANYOF);
                    }
                }
            }

            private void createAllofOrAnyofObjects(final JSONObject tmp, final TestCommand testCommand, final Commands command) throws JSONException {
                tmp.put(GeneralFields.ID, command.getCommandname());
                final JSONArray array = new JSONArray();
                for (final TestCommand testCommand2 : testCommand.getTestcommands()) {
                    final JSONObject object = new JSONObject();
                    createJSONFromTestCommand(object, testCommand2);
                    array.put(object);
                }
                tmp.put(AllofOrAnyOfTestFields.TESTS, array);
            }

            private TestCommand createAddressEnvelopeOrHeaderTest(final JSONObject jobj, final Commands command) throws JSONException, SieveException, OXJSONException {
                final List<Object> argList = new ArrayList<Object>();
                argList.add(createTagArg(getString(jobj, AddressEnvelopeAndHeaderTestFields.COMPARISON, command.getCommandname())));
                argList.add(JSONArrayToStringList(getJSONArray(jobj, AddressEnvelopeAndHeaderTestFields.HEADERS, command.getCommandname())));
                argList.add(JSONArrayToStringList(getJSONArray(jobj, AddressEnvelopeAndHeaderTestFields.VALUES, command.getCommandname())));
                return new TestCommand(command, argList, new ArrayList<TestCommand>());
            }
            
            private JSONArray getJSONArray(final JSONObject jobj, final String value, final String component) throws OXJSONException {
                try {
                    return jobj.getJSONArray(value);
                } catch (final JSONException e) {
                    throw new OXJSONException(OXJSONException.Code.JSON_READ_ERROR, e, "Error while reading TestCommand " + component + ": " + e.getMessage());
                }
            }
            
            private String getString(final JSONObject jobj, final String value, final String component) throws OXJSONException {
                try {
                    return jobj.getString(value);
                } catch (final JSONException e) {
                    throw new OXJSONException(OXJSONException.Code.JSON_READ_ERROR, e, "Error while reading TestCommand " + component + ": " + e.getMessage());
                }
            }
        },
        
        new ActionCommandMapper(),
        
        new Mapper<Rule>() {

            public String getAttrName() {
                return RuleFields.TEXT;
            }

            public Object getAttribute(final Rule obj) throws JSONException {
                return obj.getText();
            }

            public boolean isNull(final Rule obj) {
                return null == obj.getText();
            }

            public void setAttribute(final Rule obj, final Object attr) throws JSONException {
                obj.setText((String)attr);
            }
            
        },

        new Mapper<Rule>() {
            
            public String getAttrName() {
                return RuleFields.ERRORMSG;
            }
            
            public Object getAttribute(final Rule obj) throws JSONException {
                return obj.getErrormsg();
            }
            
            public boolean isNull(final Rule obj) {
                return null == obj.getErrormsg();
            }
            
            public void setAttribute(final Rule obj, final Object attr) throws JSONException {
                obj.setErrormsg((String)attr);
            }
            
        }
        
    };
    
    static {
        final Map<String, Mapper<Rule>> tmp = new HashMap<String, Mapper<Rule>>();
        for (final Mapper<Rule> mapper : mappers) {
            tmp.put(mapper.getAttrName(), mapper);
        }
        attr2Mapper = Collections.unmodifiableMap(tmp);
    }

}
