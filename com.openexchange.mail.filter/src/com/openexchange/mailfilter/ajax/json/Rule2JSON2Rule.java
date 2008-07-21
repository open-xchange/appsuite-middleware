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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.jsieve.NumberArgument;
import org.apache.jsieve.SieveException;
import org.apache.jsieve.TagArgument;
import org.apache.jsieve.parser.generated.Token;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.IfCommand;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.jsieve.commands.RuleComment;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailfilter.ajax.fields.RuleFields;
import com.openexchange.tools.servlet.OXJSONException;

/**
 * @author cutmasta
 *
 */
@SuppressWarnings("unchecked")
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

    private class GeneralFields {
        final static String ID = "id";
    }

    private class AddressEnvelopeAndHeaderTestFields {
        final static String COMPARISON = "comparison";
        final static String HEADERS = "headers";
        final static String VALUES = "values";
    }

    private class NotTestFields {
        final static String TEST = "test";
    }

    private class SizeTestFields {
        final static String COMPARISON = "comparison";
        final static String SIZE = "size";
    }

    private class BodyTestFields {
        final static String COMPARISON = "comparison";
        final static String EXTENSIONSKEY ="extensionskey";
        final static String EXTENSIONSVALUE = "extensionsvalue";
        final static String VALUES = "values";
    }

    private class AllofOrAnyOfTestFields {
        final static String TESTS = "tests";
    }

    private class RedirectActionFields {
        final static String TO = "to";
    }

    private class MoveActionFields {
        final static String INTO = "into";
    }

    private class RejectActionFields {
        final static String TEXT = "text";
    }

    private enum VacationActionFields {
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
    
    private class AddFlagsActionFields {
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
    
    private static TagArgument createTagArg(VacationActionFields fields) {
        final Token token = new Token();
        token.image = fields.getTagname();
        final TagArgument tagArgument = new TagArgument(token);
        return tagArgument;
    }

    private static TagArgument createTagArg(final String string) {
        final Token token = new Token();
        token.image = ":" + string;
        final TagArgument tagArgument = new TagArgument(token);
        return tagArgument;
    }
    
    private static List<String> JSONArrayToStringList(final JSONArray jarray) throws JSONException {
        final ArrayList<String> retval = new ArrayList<String>(jarray.length());
        for (int i = 0; i < jarray.length(); i++) {
            retval.add(jarray.getString(i));
        }
        return retval;
    }
    
    private static NumberArgument createNumberArg(final String string) {
        final Token token = new Token();
        token.image = string;
        final NumberArgument numberArgument = new NumberArgument(token);
        return numberArgument;
    }

    private static final Mapper<Rule>[] mappers = new Mapper[] { 
        new Mapper<Rule>() {

            public String getAttrName() {
                return RuleFields.ID;
            }

            public Object getAttribute(final Rule obj) throws JSONException {
                final RuleComment ruleComment = obj.getRuleComment();
                if (null != ruleComment) {
                    return ruleComment.getUniqueid();
                } else {
                    return null;
                }
            }

            public boolean isNull(final Rule obj) {
                return ((null == obj.getRuleComment()) || (-1 == obj.getRuleComment().getUniqueid()));
            }

            public void setAttribute(final Rule obj, final Object attr) throws JSONException {                
                final RuleComment ruleComment = obj.getRuleComment();
                if (null != ruleComment) {
                    ruleComment.setUniqueid((Integer)attr);
                } else {
                    obj.setRuleComments(new RuleComment((Integer)attr));
                }
            }
        },
        
        new Mapper<Rule>() {
            
            public String getAttrName() {
                return RuleFields.POSITION;
            }
            
            public Object getAttribute(final Rule obj) throws JSONException {
                return obj.getPosition();
            }
            
            public boolean isNull(final Rule obj) {
                return -1 == obj.getPosition();
            }
            
            public void setAttribute(final Rule obj, final Object attr) throws JSONException {                
                obj.setPosition((Integer)attr);
            }
        },
        
        new Mapper<Rule>() {

            public String getAttrName() {
                return RuleFields.RULENAME;
            }

            public Object getAttribute(final Rule obj) throws JSONException {
                final RuleComment ruleComment = obj.getRuleComment();
                if (null != ruleComment) {
                    return ruleComment.getRulename();
                } else {
                    return null;
                }
            }

            public boolean isNull(final Rule obj) {                
                final RuleComment ruleComment = obj.getRuleComment();
                return ((null == ruleComment) || (null == ruleComment.getRulename()));
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
                return !obj.isCommented();
            }

            public boolean isNull(final Rule obj) {                
                return false;
            }

            public void setAttribute(final Rule obj, final Object attr) throws JSONException {
                obj.setCommented(!(Boolean) attr);
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
                    // TODO: get the extensions from the right object parts
                    argList.add(getString(jobj, BodyTestFields.EXTENSIONSKEY, id));
                    argList.add(getString(jobj, BodyTestFields.EXTENSIONSVALUE, id));
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
                        // TODO: get the extensions from the right object parts
                        tmp.put(BodyTestFields.EXTENSIONSKEY, testCommand.getArguments().get(0));
                        tmp.put(BodyTestFields.EXTENSIONSVALUE, testCommand.getArguments().get(0));
                        tmp.put(BodyTestFields.VALUES, testCommand.getArguments().get(1));
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
        
        new Mapper<Rule>() {

            public String getAttrName() {
                return RuleFields.ACTIONCMDS;
            }

            public Object getAttribute(final Rule obj) throws JSONException {
                final JSONArray array = new JSONArray();
                final IfCommand ifCommand = obj.getIfCommand();
                if (null != ifCommand) {
                    final List<ActionCommand> actionCommands = ifCommand.getActioncommands();
                    for (final ActionCommand actionCommand : actionCommands) {
                        final JSONObject object = new JSONObject();
                        createJSONFromActionCommand(object, actionCommand);
                        array.put(object);
                    }
                    return array;
                }
                return null;
            }

            public boolean isNull(final Rule obj) {                
                return (null == obj.getIfCommand());
            }

            public void setAttribute(final Rule rule, final Object obj) throws JSONException, SieveException, OXJSONException {
                final JSONArray jarray = (JSONArray) obj;
                final IfCommand ifCommand = rule.getIfCommand();
                if (null == ifCommand) {
                    throw new SieveException("There no if command where the action command can be applied to in rule " + rule);
                }
                // Delete all existing actions, this is especially needed if this is used by update
                ifCommand.setActioncommands(null);
                for (int i = 0; i < jarray.length(); i++) {
                    final JSONObject object = jarray.getJSONObject(i);
                    final ActionCommand actionCommand = createActionCommandFromJSON(object);
                    ifCommand.addActioncommands(actionCommand);
                }
            }
            
            private ActionCommand createActionCommandFromJSON(final JSONObject object) throws JSONException, SieveException, OXJSONException {
                final String id = object.getString(GeneralFields.ID);
                if (ActionCommand.Commands.KEEP.getJsonname().equals(id)) {
                    return new ActionCommand(ActionCommand.Commands.KEEP, new ArrayList<Object>());
                } else if (ActionCommand.Commands.DISCARD.getJsonname().equals(id)) {
                    return new ActionCommand(ActionCommand.Commands.DISCARD, new ArrayList<Object>());
                } else if (ActionCommand.Commands.REDIRECT.getJsonname().equals(id)) {
                    return createOneParameterActionCommand(object, RedirectActionFields.TO, ActionCommand.Commands.REDIRECT);
                } else if (ActionCommand.Commands.FILEINTO.getJsonname().equals(id)) {
                    return createFileintoActionCommand(object, MoveActionFields.INTO, ActionCommand.Commands.FILEINTO);
                } else if (ActionCommand.Commands.REJECT.getJsonname().equals(id)) {
                    return createOneParameterActionCommand(object, RejectActionFields.TEXT, ActionCommand.Commands.REJECT);
                } else if (ActionCommand.Commands.STOP.getJsonname().equals(id)) {
                    return new ActionCommand(ActionCommand.Commands.STOP, new ArrayList<Object>());
                } else if (ActionCommand.Commands.VACATION.getJsonname().equals(id)) {
                    final ArrayList<Object> arrayList = new ArrayList<Object>();
                    final String days = object.getString(VacationActionFields.DAYS.getFieldname());
                    if (null != days) {
                        arrayList.add(createTagArg(VacationActionFields.DAYS));
                        arrayList.add(createNumberArg(days));
                    }
                    final JSONArray addresses = object.getJSONArray(VacationActionFields.ADDRESSES.getFieldname());
                    if (null != addresses) {
                        arrayList.add(createTagArg(VacationActionFields.ADDRESSES));
                        arrayList.add(JSONArrayToStringList(addresses));
                    }
                    final String subject = object.getString(VacationActionFields.SUBJECT.getFieldname());
                    if (null != subject) {
                        arrayList.add(createTagArg(VacationActionFields.SUBJECT));
                        arrayList.add(stringToList(subject));
                    }
                    final String text = object.getString(VacationActionFields.TEXT.getFieldname());
                    if (null != text) {
                        arrayList.add(stringToList(text));
                        return new ActionCommand(ActionCommand.Commands.VACATION, arrayList);
                    } else {
                        throw new OXJSONException(OXJSONException.Code.JSON_READ_ERROR, "Parameter " + VacationActionFields.TEXT.getFieldname() + " is missing for " + 
                                ActionCommand.Commands.VACATION.getJsonname() + " is missing in JSON-Object. This is a required field");
                    }
                } else if (ActionCommand.Commands.ADDFLAG.getJsonname().equals(id)) {
                    final JSONArray array = object.getJSONArray(AddFlagsActionFields.FLAGS);
                    if (null != array) {
                        final ArrayList<Object> arrayList = new ArrayList<Object>();
                        arrayList.add(JSONArrayToStringList(array));
                        return new ActionCommand(ActionCommand.Commands.ADDFLAG, arrayList);
                    } else {
                        throw new OXJSONException(OXJSONException.Code.JSON_READ_ERROR, "Parameter " + AddFlagsActionFields.FLAGS + " is missing for " + 
                                ActionCommand.Commands.ADDFLAG.getJsonname() + " is missing in JSON-Object. This is a required field");
                    }
                } else {
                    throw new JSONException("Unknown action command while creating object: " + id);
                }
            }

            private ActionCommand createOneParameterActionCommand(final JSONObject object, final String parameter, final ActionCommand.Commands command) throws JSONException, SieveException, OXJSONException {
                final String stringparam = getString(object, parameter, command.getCommandname());
                if (null != stringparam) {
                    return new ActionCommand(command, createArrayArray(stringparam));
                } else {
                    throw new JSONException("The parameter " + parameter + " is missing for action command " + 
                            command.getCommandname() + ".");
                }
            }

            private ActionCommand createFileintoActionCommand(final JSONObject object, final String parameter, final ActionCommand.Commands command) throws JSONException, SieveException, OXJSONException {
                final String stringparam = getString(object, parameter, command.getCommandname());
                if (null != stringparam) {
                    return new ActionCommand(command, createArrayArray(MailFolderUtility.prepareMailFolderParam(stringparam)));
                } else {
                    throw new JSONException("The parameter " + parameter + " is missing for action command " + 
                            command.getCommandname() + ".");
                }
            }
            
            private ArrayList<Object> createArrayArray(final String string) {
                final ArrayList<Object> retval = new ArrayList<Object>();
                final ArrayList<String> strings = new ArrayList<String>();
                strings.add(string);
                retval.add(strings);
                return retval;
            }

            private List<String> stringToList(final String string) {
                final ArrayList<String> retval = new ArrayList<String>(1);
                retval.add(string);
                return retval;
            }
            
            /**
             * This method is used to create a JSON object from a TestCommand. It is done this way because a separate
             * converter class would have to do the check for the right TestCommand for each id.
             * 
             * @param tmp the JSONObject into which the values are written
             * @param actionCommand the TestCommand itself
             * @throws JSONException
             */
            private void createJSONFromActionCommand(final JSONObject tmp, final ActionCommand actionCommand) throws JSONException {
                if (null != actionCommand) {
                    if (ActionCommand.Commands.KEEP.equals(actionCommand.getCommand())) {
                        tmp.put(GeneralFields.ID, ActionCommand.Commands.KEEP.getJsonname());
                    } else if (ActionCommand.Commands.DISCARD.equals(actionCommand.getCommand())) {
                        tmp.put(GeneralFields.ID, ActionCommand.Commands.DISCARD.getJsonname());
                    } else {
                        final ArrayList<Object> arguments = actionCommand.getArguments();
                        if (ActionCommand.Commands.REDIRECT.equals(actionCommand.getCommand())) {
                            createOneParameterJSON(tmp, arguments, ActionCommand.Commands.REDIRECT, RedirectActionFields.TO);
                        } else if (ActionCommand.Commands.FILEINTO.equals(actionCommand.getCommand())) {
                            createFileintoJSON(tmp, arguments, ActionCommand.Commands.FILEINTO, MoveActionFields.INTO);
                        } else if (ActionCommand.Commands.REJECT.equals(actionCommand.getCommand())) {
                            createOneParameterJSON(tmp, arguments, ActionCommand.Commands.REJECT, RejectActionFields.TEXT);
                        } else if (ActionCommand.Commands.STOP.equals(actionCommand.getCommand())) {
                            tmp.put(GeneralFields.ID, ActionCommand.Commands.STOP.getJsonname());
                        } else if (ActionCommand.Commands.VACATION.equals(actionCommand.getCommand())) {
                            tmp.put(GeneralFields.ID, ActionCommand.Commands.VACATION.getJsonname());
                            final Hashtable<String, List<String>> tagarguments = actionCommand.getTagarguments();
                            final List<String> days = tagarguments.get(VacationActionFields.DAYS.getTagname());
                            if (null != days) {
                                tmp.put(VacationActionFields.DAYS.getFieldname(), days.get(0));
                            }
                            final List<String> addresses = tagarguments.get(VacationActionFields.ADDRESSES.getTagname());
                            if (null != addresses) {
                                tmp.put(VacationActionFields.ADDRESSES.getFieldname(), addresses);
                            }
                            final List<String> subject = tagarguments.get(VacationActionFields.SUBJECT.getTagname());
                            if (null != subject) {
                                tmp.put(VacationActionFields.SUBJECT.getFieldname(), subject.get(0));
                            }
                            tmp.put(VacationActionFields.TEXT.getFieldname(), ((List<String>)arguments.get(arguments.size() - 1)).get(0));
                        } else if (ActionCommand.Commands.ADDFLAG.equals(actionCommand.getCommand())) {
                            tmp.put(GeneralFields.ID, ActionCommand.Commands.ADDFLAG.getJsonname());
                            tmp.put(AddFlagsActionFields.FLAGS, (List<String>)arguments.get(0));
                        }
                    }
                }
            }

            private void createOneParameterJSON(final JSONObject tmp, final ArrayList<Object> arguments, final com.openexchange.jsieve.commands.ActionCommand.Commands command, final String field) throws JSONException {
                tmp.put(GeneralFields.ID, command.getJsonname());
                tmp.put(field, ((List<String>)arguments.get(0)).get(0));
            }

            private void createFileintoJSON(final JSONObject tmp, final ArrayList<Object> arguments, final com.openexchange.jsieve.commands.ActionCommand.Commands command, final String field) throws JSONException {
                tmp.put(GeneralFields.ID, command.getJsonname());
                tmp.put(field, MailFolderUtility.prepareFullname(((List<String>)arguments.get(0)).get(0)));
            }
            
            private String getString(final JSONObject jobj, final String value, final String component) throws OXJSONException {
                try {
                    return jobj.getString(value);
                } catch (final JSONException e) {
                    throw new OXJSONException(OXJSONException.Code.JSON_READ_ERROR, e, "Error while reading ActionCommand " + component + ": " + e.getMessage());
                }
            }

        },
        
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
