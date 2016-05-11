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

package com.openexchange.mailfilter.json.ajax.json;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import org.apache.jsieve.SieveException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.common.base.CharMatcher;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Filter;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.IfCommand;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.mail.json.parser.MessageParser;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailfilter.MailFilterProperties;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;
import com.openexchange.mailfilter.json.ajax.fields.RuleFields;
import com.openexchange.mailfilter.json.ajax.json.AbstractObject2JSON2Object.Mapper;
import com.openexchange.mailfilter.json.ajax.json.Rule2JSON2Rule.AddFlagsActionFields;
import com.openexchange.mailfilter.json.ajax.json.Rule2JSON2Rule.EnotifyActionFields;
import com.openexchange.mailfilter.json.ajax.json.Rule2JSON2Rule.GeneralFields;
import com.openexchange.mailfilter.json.ajax.json.Rule2JSON2Rule.MoveActionFields;
import com.openexchange.mailfilter.json.ajax.json.Rule2JSON2Rule.PGPEncryptActionFields;
import com.openexchange.mailfilter.json.ajax.json.Rule2JSON2Rule.RedirectActionFields;
import com.openexchange.mailfilter.json.ajax.json.Rule2JSON2Rule.RejectActionFields;
import com.openexchange.mailfilter.json.ajax.json.Rule2JSON2Rule.VacationActionFields;
import com.openexchange.mailfilter.json.osgi.Services;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.sun.mail.imap.protocol.BASE64MailboxDecoder;
import com.sun.mail.imap.protocol.BASE64MailboxEncoder;

final class ActionCommandMapper implements Mapper<Rule> {

    @Override
    public String getAttrName() {
        return RuleFields.ACTIONCMDS;
    }

    @Override
    public Object getAttribute(final Rule obj) throws JSONException {
        final JSONArray array = new JSONArray();
        final IfCommand ifCommand = obj.getIfCommand();
        if (null == ifCommand) {
            return null;
        }
        final List<ActionCommand> actionCommands = ifCommand.getActioncommands();
        for (final ActionCommand actionCommand : actionCommands) {
            final JSONObject object = new JSONObject();
            createJSONFromActionCommand(object, actionCommand);
            array.put(object);
        }
        return array;
    }

    @Override
    public boolean isNull(final Rule obj) {
        return (null == obj.getIfCommand());
    }

    @Override
    public void setAttribute(final Rule rule, final Object obj) throws JSONException, SieveException, OXException {
        final JSONArray jarray = (JSONArray) obj;
        final IfCommand ifCommand = rule.getIfCommand();
        if (null == ifCommand) {
            throw new SieveException("There is no if command where the action command can be applied to in rule " + rule);
        }
        // Delete all existing actions, this is especially needed if this is used by update
        ifCommand.setActioncommands(null);
        for (int i = 0; i < jarray.length(); i++) {
            final JSONObject object = jarray.getJSONObject(i);
            final ActionCommand actionCommand = createActionCommandFromJSON(object);
            ifCommand.addActioncommands(actionCommand);
        }
    }

    private ActionCommand createActionCommandFromJSON(final JSONObject object) throws JSONException, SieveException, OXException {
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
                arrayList.add(Rule2JSON2Rule.createTagArg(VacationActionFields.DAYS));
                arrayList.add(Rule2JSON2Rule.createNumberArg(days));
            }
            final JSONArray addresses = object.optJSONArray(VacationActionFields.ADDRESSES.getFieldname());
            if (null != addresses) {
                arrayList.add(Rule2JSON2Rule.createTagArg(VacationActionFields.ADDRESSES));
                arrayList.add(Rule2JSON2Rule.JSONArrayToStringList(addresses));
            }
            final String subjectFieldname = VacationActionFields.SUBJECT.getFieldname();
            if (object.has(subjectFieldname)) {
                String subject = object.getString(subjectFieldname);
                //subject = MimeMessageUtility.quotePhrase(subject, true);
                subject = encode(subject, "subject");
                arrayList.add(Rule2JSON2Rule.createTagArg(VacationActionFields.SUBJECT));
                arrayList.add(stringToList(subject));
            }
            final String fromFieldName = VacationActionFields.FROM.getFieldname();
            if (object.has(fromFieldName)) {
                String from = "";
                Object obj = object.get(fromFieldName);
                boolean strict = true;
                if (obj instanceof JSONArray) {
                    // Create the object with the array of arrays as it is expected to be form the MessageParser.parseAddressKey
                    try {
                        InternetAddress[] fromArr = MessageParser.parseAdressArray(new JSONArray().put(obj), 1, strict);
                        if (fromArr.length > 0) {
                            from = fromArr[0].toString();
                        }
                    } catch (AddressException e) {
                        throw OXJSONExceptionCodes.INVALID_VALUE.create(from, VacationActionFields.FROM.getFieldname());
                    }
                } else if (obj instanceof String) {
                    // Get string
                    String fromStr = object.getString(fromFieldName);
                    try {
                        new QuotedInternetAddress(fromStr, strict);
                        from = fromStr;
                    } catch (AddressException e) {
                        throw OXJSONExceptionCodes.INVALID_VALUE.create(from, VacationActionFields.FROM.getFieldname());
                    }
                }

                if (!Strings.isEmpty(from)) {
                    arrayList.add(Rule2JSON2Rule.createTagArg(VacationActionFields.FROM));
                    arrayList.add(stringToList(from));
                }
            }
            final String text = object.getString(VacationActionFields.TEXT.getFieldname());
            if (null == text) {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create("Parameter " + VacationActionFields.TEXT.getFieldname() + " is missing for " + ActionCommand.Commands.VACATION.getJsonname() + " is missing in JSON-Object. This is a required field");
            }
            arrayList.add(stringToList(text.replaceAll("(\r)?\n", "\r\n")));
            return new ActionCommand(ActionCommand.Commands.VACATION, arrayList);
        } else if (ActionCommand.Commands.ENOTIFY.getJsonname().equals(id)) {
            final ArrayList<Object> arrayList = new ArrayList<Object>();
            final String messageFieldName = EnotifyActionFields.MESSAGE.getFieldname();
            if (object.has(messageFieldName)) {
                final String message = object.getString(messageFieldName);
                arrayList.add(Rule2JSON2Rule.createTagArg(EnotifyActionFields.MESSAGE));
                arrayList.add(stringToList(message));
            }
            final String method = object.getString(EnotifyActionFields.METHOD.getFieldname());
            if (null == method) {
                throw new JSONException("Parameter " + EnotifyActionFields.METHOD.getFieldname() + " is missing for " + ActionCommand.Commands.ENOTIFY.getJsonname() + " is missing in JSON-Object. This is a required field");
            }
            arrayList.add(stringToList(method.replaceAll("(\r)?\n", "\r\n")));
            return new ActionCommand(ActionCommand.Commands.ENOTIFY, arrayList);
        } else if (ActionCommand.Commands.ADDFLAG.getJsonname().equals(id)) {
            final JSONArray array = object.getJSONArray(AddFlagsActionFields.FLAGS);
            if (null == array) {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create("Parameter " + AddFlagsActionFields.FLAGS + " is missing for " + ActionCommand.Commands.ADDFLAG.getJsonname() + " is missing in JSON-Object. This is a required field");
            }
            final ArrayList<Object> arrayList = new ArrayList<Object>();
            arrayList.add(Rule2JSON2Rule.JSONArrayToStringList(array));
            return new ActionCommand(ActionCommand.Commands.ADDFLAG, arrayList);
        } else if (ActionCommand.Commands.PGP_ENCRYPT.getJsonname().equals(id)) {
            final ArrayList<Object> arrayList = new ArrayList<Object>();
            final JSONArray keys = object.optJSONArray(PGPEncryptActionFields.KEYS.getFieldname());
            if (null != keys) {
                if (0 == keys.length()) {
                    throw new JSONException("Empty string-arrays are not allowed in sieve.");
                }
                arrayList.add(Rule2JSON2Rule.createTagArg(PGPEncryptActionFields.KEYS));
                arrayList.add(Rule2JSON2Rule.JSONArrayToStringList(keys));
            }
            return new ActionCommand(ActionCommand.Commands.PGP_ENCRYPT, arrayList);
        } else {
            throw new JSONException("Unknown action command while creating object: " + id);
        }
    }

    private ActionCommand createOneParameterActionCommand(final JSONObject object, final String parameter, final ActionCommand.Commands command) throws JSONException, SieveException, OXException {
        final String stringparam = getString(object, parameter, command.getCommandname());
        if (null == stringparam) {
            throw new JSONException("The parameter " + parameter + " is missing for action command " + command.getCommandname() + ".");
        }
        if (ActionCommand.Commands.REDIRECT.equals(command)) {
            // Check for valid email address here:
            try {
                new QuotedInternetAddress(stringparam, true);
            } catch (final AddressException e) {
                throw MailFilterExceptionCode.INVALID_REDIRECT_ADDRESS.create(e, stringparam);
            }
            // And finally check of that forward address is allowed
            final ConfigurationService service = Services.getService(ConfigurationService.class);
            final Filter filter;
            if (null != service && (null != (filter = service.getFilterFromProperty("com.openexchange.mail.filter.redirectWhitelist"))) && !filter.accepts(stringparam)) {
                throw MailFilterExceptionCode.REJECTED_REDIRECT_ADDRESS.create(stringparam);
            }
        }
        return new ActionCommand(command, createArrayArray(stringparam));
    }

    private ActionCommand createFileintoActionCommand(final JSONObject object, final String parameter, final ActionCommand.Commands command) throws JSONException, SieveException, OXException {
        final String stringparam = getString(object, parameter, command.getCommandname());
        if (null == stringparam) {
            throw new JSONException("The parameter " + parameter + " is missing for action command " + command.getCommandname() + ".");
        }

        final ConfigurationService config = Services.getService(ConfigurationService.class);
        final String encodingProperty = config.getProperty(MailFilterProperties.Values.USE_UTF7_FOLDER_ENCODING.property);
        final boolean useUTF7Encoding = Boolean.parseBoolean(encodingProperty);

        final String folderName;
        if (useUTF7Encoding) {
            folderName = BASE64MailboxEncoder.encode(MailFolderUtility.prepareMailFolderParam(stringparam).getFullname());
        } else {
            folderName = MailFolderUtility.prepareMailFolderParam(stringparam).getFullname();
        }

        return new ActionCommand(command, createArrayArray(folderName));
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
     * This method is used to create a JSON object from a TestCommand. It is done this way because a separate converter class would have to
     * do the check for the right TestCommand for each id.
     *
     * @param tmp the JSONObject into which the values are written
     * @param actionCommand the TestCommand itself
     * @throws JSONException
     */
    @SuppressWarnings("unchecked")
    private void createJSONFromActionCommand(final JSONObject tmp, final ActionCommand actionCommand) throws JSONException {
        if (null == actionCommand) {
            return;
        }
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
                    String decodedSubject = decode(subject.get(0), "subject");
                    tmp.put(VacationActionFields.SUBJECT.getFieldname(), decodedSubject);
                }
                final List<String> from = tagarguments.get(VacationActionFields.FROM.getTagname());
                if (null != from) {
                    String decodedFrom = MimeMessageUtility.decodeEnvelopeSubject(from.get(0));
                    tmp.put(VacationActionFields.FROM.getFieldname(), decodedFrom);
                }
                tmp.put(VacationActionFields.TEXT.getFieldname(), ((List<String>) arguments.get(arguments.size() - 1)).get(0));
            } else if (ActionCommand.Commands.ENOTIFY.equals(actionCommand.getCommand())) {
                tmp.put(GeneralFields.ID, ActionCommand.Commands.ENOTIFY.getJsonname());
                final Hashtable<String, List<String>> tagarguments = actionCommand.getTagarguments();
                final List<String> message = tagarguments.get(EnotifyActionFields.MESSAGE.getTagname());
                if (null != message) {
                    tmp.put(EnotifyActionFields.MESSAGE.getFieldname(), message.get(0));
                }
                tmp.put(EnotifyActionFields.METHOD.getFieldname(), ((List<String>) arguments.get(arguments.size() - 1)).get(0));
            } else if (ActionCommand.Commands.ADDFLAG.equals(actionCommand.getCommand())) {
                tmp.put(GeneralFields.ID, ActionCommand.Commands.ADDFLAG.getJsonname());
                tmp.put(AddFlagsActionFields.FLAGS, (List<String>) arguments.get(0));
            } else if (ActionCommand.Commands.PGP_ENCRYPT.equals(actionCommand.getCommand())) {
                tmp.put(GeneralFields.ID, ActionCommand.Commands.PGP_ENCRYPT.getJsonname());
                final Hashtable<String, List<String>> tagarguments = actionCommand.getTagarguments();
                final List<String> keys = tagarguments.get(PGPEncryptActionFields.KEYS.getTagname());
                if (null != keys) {
                    tmp.put(PGPEncryptActionFields.KEYS.getFieldname(), keys);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void createOneParameterJSON(final JSONObject tmp, final ArrayList<Object> arguments, final com.openexchange.jsieve.commands.ActionCommand.Commands command, final String field) throws JSONException {
        tmp.put(GeneralFields.ID, command.getJsonname());
        tmp.put(field, ((List<String>) arguments.get(0)).get(0));
    }

    @SuppressWarnings("unchecked")
    private void createFileintoJSON(final JSONObject tmp, final ArrayList<Object> arguments, final com.openexchange.jsieve.commands.ActionCommand.Commands command, final String field) throws JSONException {
        tmp.put(GeneralFields.ID, command.getJsonname());

        final ConfigurationService config = Services.getService(ConfigurationService.class);
        final String encodingProperty = config.getProperty(MailFilterProperties.Values.USE_UTF7_FOLDER_ENCODING.property);
        final boolean useUTF7Encoding = Boolean.parseBoolean(encodingProperty);

        final String folderName;
        if (useUTF7Encoding) {
            folderName = BASE64MailboxDecoder.decode(((List<String>) arguments.get(0)).get(0));
        } else {
            folderName = ((List<String>) arguments.get(0)).get(0);
        }

        tmp.put(field, MailFolderUtility.prepareFullname(0, folderName));
    }

    private String getString(final JSONObject jobj, final String value, final String component) throws OXException {
        try {
            return jobj.getString(value);
        } catch (final JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, "Error while reading ActionCommand " + component + ": " + e.getMessage());
        }
    }

    /**
     * Encodes the specified UTF-8 string if necessary and returns the encoded string
     * 
     * @param string The string to encode
     * @param field The field
     * @return The encoded string
     * @throws OXException if the string cannot be decoded
     */
    private String encode(String string, String field) throws OXException {
        if (CharMatcher.ASCII.matchesAllOf(string)) {
            return string;
        }
        try {
            return MimeUtility.encodeText(string);
        } catch (UnsupportedEncodingException e) {
            throw MailFilterExceptionCode.PROBLEM.create(e, "Unable to encode the field '" + field + "'");
        }
    }

    /**
     * Decodes the specified UTF-8 string if necessary and returns the decoded string
     * 
     * @param utf8 The UTF-8 encoded string
     * @param field The field
     * @return The decoded string
     * @throws JSONException if the string cannot be decoded
     */
    private String decode(String utf8, String field) throws JSONException {
        if (Strings.isEmpty(utf8)) {
            return utf8;
        }
        if (!utf8.startsWith("=?UTF")) {
            return utf8;
        }
        try {
            return MimeUtility.decodeText(utf8);
        } catch (UnsupportedEncodingException e) {
            throw new JSONException("Unable to decode the field '" + field + "'", e);
        }
    }
}
