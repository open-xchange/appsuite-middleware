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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.jsieve.NumberArgument;
import org.apache.jsieve.SieveException;
import org.apache.jsieve.TagArgument;
import org.apache.jsieve.parser.generated.Token;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import com.openexchange.mailfilter.json.ajax.json.RuleFieldMapper;
import com.openexchange.mailfilter.json.ajax.json.fields.AddFlagsActionField;
import com.openexchange.mailfilter.json.ajax.json.fields.EnotifyActionField;
import com.openexchange.mailfilter.json.ajax.json.fields.GeneralField;
import com.openexchange.mailfilter.json.ajax.json.fields.MoveActionField;
import com.openexchange.mailfilter.json.ajax.json.fields.PGPEncryptActionField;
import com.openexchange.mailfilter.json.ajax.json.fields.RedirectActionField;
import com.openexchange.mailfilter.json.ajax.json.fields.RejectActionField;
import com.openexchange.mailfilter.json.ajax.json.fields.RuleField;
import com.openexchange.mailfilter.json.ajax.json.fields.VacationActionField;
import com.openexchange.mailfilter.json.osgi.Services;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.sun.mail.imap.protocol.BASE64MailboxDecoder;
import com.sun.mail.imap.protocol.BASE64MailboxEncoder;

/**
 * {@link ActionCommandRuleFieldMapper}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ActionCommandRuleFieldMapper implements RuleFieldMapper {

    /**
     * Initialises a new {@link ActionCommandRuleFieldMapper}.
     */
    public ActionCommandRuleFieldMapper() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.json.ajax.json.RuleFieldMapper#getAttributeName()
     */
    @Override
    public RuleField getAttributeName() {
        return RuleField.actioncmds;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.json.ajax.json.RuleFieldMapper#isNull(com.openexchange.jsieve.commands.Rule)
     */
    @Override
    public boolean isNull(Rule rule) {
        return rule.getIfCommand() == null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.json.ajax.json.RuleFieldMapper#getAttribute(com.openexchange.jsieve.commands.Rule)
     */
    @Override
    public Object getAttribute(Rule rule) throws JSONException {
        if (isNull(rule)) {
            return null;
        }
        JSONArray array = new JSONArray();
        IfCommand ifCommand = rule.getIfCommand();
        List<ActionCommand> actionCommands = ifCommand.getActionCommands();
        for (ActionCommand actionCommand : actionCommands) {
            JSONObject object = new JSONObject();
            // TODO: Create an action command registry with action command parsers
            // e.g. Map<ActionCommand, ActionCommandParser> parsers;
            createJSONFromActionCommand(object, actionCommand);
            array.put(object);
        }
        return array;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.json.ajax.json.RuleFieldMapper#setAttribute(com.openexchange.jsieve.commands.Rule, java.lang.Object)
     */
    @Override
    public void setAttribute(Rule rule, Object attribute) throws JSONException, SieveException, OXException {
        if (isNull(rule)) {
            throw new SieveException("There is no if command where the action command can be applied to in rule " + rule);
        }

        IfCommand ifCommand = rule.getIfCommand();
        // Delete all existing actions, this is especially needed if this is used by update
        ifCommand.setActionCommands(null);
        JSONArray array = (JSONArray) attribute;
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            // TODO: Create an action command registry with action command parsers
            ActionCommand actionCommand = createActionCommandFromJSON(object);
            ifCommand.addActionCommand(actionCommand);
        }
    }

    //////////////////////////////////////////////////// HELPERS ////////////////////////////////////////////////
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
            tmp.put(GeneralField.id.name(), ActionCommand.Commands.KEEP.getJsonname());
        } else if (ActionCommand.Commands.DISCARD.equals(actionCommand.getCommand())) {
            tmp.put(GeneralField.id.name(), ActionCommand.Commands.DISCARD.getJsonname());
        } else {
            final ArrayList<Object> arguments = actionCommand.getArguments();
            if (ActionCommand.Commands.REDIRECT.equals(actionCommand.getCommand())) {
                createOneParameterJSON(tmp, arguments, ActionCommand.Commands.REDIRECT, RedirectActionField.to.name());
            } else if (ActionCommand.Commands.FILEINTO.equals(actionCommand.getCommand())) {
                createFileintoJSON(tmp, arguments, ActionCommand.Commands.FILEINTO, MoveActionField.into.name());
            } else if (ActionCommand.Commands.REJECT.equals(actionCommand.getCommand())) {
                createOneParameterJSON(tmp, arguments, ActionCommand.Commands.REJECT, RejectActionField.text.name());
            } else if (ActionCommand.Commands.STOP.equals(actionCommand.getCommand())) {
                tmp.put(GeneralField.id.name(), ActionCommand.Commands.STOP.getJsonname());
            } else if (ActionCommand.Commands.VACATION.equals(actionCommand.getCommand())) {
                tmp.put(GeneralField.id.name(), ActionCommand.Commands.VACATION.getJsonname());
                final Hashtable<String, List<String>> tagarguments = actionCommand.getTagarguments();
                final List<String> days = tagarguments.get(VacationActionField.days.getTagName());
                if (null != days) {
                    tmp.put(VacationActionField.days.getFieldName(), days.get(0));
                }
                final List<String> addresses = tagarguments.get(VacationActionField.addresses.getTagName());
                if (null != addresses) {
                    tmp.put(VacationActionField.addresses.getFieldName(), addresses);
                }
                final List<String> subject = tagarguments.get(VacationActionField.subject.getTagName());
                if (null != subject) {
                    String decodedSubject = MimeMessageUtility.decodeEnvelopeSubject(subject.get(0));
                    tmp.put(VacationActionField.subject.getFieldName(), decodedSubject);
                }
                final List<String> from = tagarguments.get(VacationActionField.from.getTagName());
                if (null != from) {
                    String decodedFrom = MimeMessageUtility.decodeEnvelopeSubject(from.get(0));
                    tmp.put(VacationActionField.from.getFieldName(), decodedFrom);
                }
                tmp.put(VacationActionField.text.getFieldName(), ((List<String>) arguments.get(arguments.size() - 1)).get(0));
            } else if (ActionCommand.Commands.ENOTIFY.equals(actionCommand.getCommand())) {
                tmp.put(GeneralField.id.name(), ActionCommand.Commands.ENOTIFY.getJsonname());
                final Hashtable<String, List<String>> tagarguments = actionCommand.getTagarguments();
                final List<String> message = tagarguments.get(EnotifyActionField.message.getTagName());
                if (null != message) {
                    tmp.put(EnotifyActionField.message.getFieldName(), message.get(0));
                }
                tmp.put(EnotifyActionField.method.getFieldName(), ((List<String>) arguments.get(arguments.size() - 1)).get(0));
            } else if (ActionCommand.Commands.ADDFLAG.equals(actionCommand.getCommand())) {
                tmp.put(GeneralField.id.name(), ActionCommand.Commands.ADDFLAG.getJsonname());
                tmp.put(AddFlagsActionField.flags.name(), (List<String>) arguments.get(0));
            } else if (ActionCommand.Commands.PGP_ENCRYPT.equals(actionCommand.getCommand())) {
                tmp.put(GeneralField.id.name(), ActionCommand.Commands.PGP_ENCRYPT.getJsonname());
                final Hashtable<String, List<String>> tagarguments = actionCommand.getTagarguments();
                final List<String> keys = tagarguments.get(PGPEncryptActionField.keys.getTagName());
                if (null != keys) {
                    tmp.put(PGPEncryptActionField.keys.getFieldName(), keys);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void createOneParameterJSON(final JSONObject tmp, final ArrayList<Object> arguments, final ActionCommand.Commands command, final String field) throws JSONException {
        tmp.put(GeneralField.id.name(), command.getJsonname());
        tmp.put(field, ((List<String>) arguments.get(0)).get(0));
    }

    @SuppressWarnings("unchecked")
    private void createFileintoJSON(final JSONObject tmp, final ArrayList<Object> arguments, final ActionCommand.Commands command, final String field) throws JSONException {
        tmp.put(GeneralField.id.name(), command.getJsonname());

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

    private ActionCommand createActionCommandFromJSON(final JSONObject object) throws JSONException, SieveException, OXException {
        final String id = object.getString(GeneralField.id.name());
        if (ActionCommand.Commands.KEEP.getJsonname().equals(id)) {
            return new ActionCommand(ActionCommand.Commands.KEEP, new ArrayList<Object>());
        } else if (ActionCommand.Commands.DISCARD.getJsonname().equals(id)) {
            return new ActionCommand(ActionCommand.Commands.DISCARD, new ArrayList<Object>());
        } else if (ActionCommand.Commands.REDIRECT.getJsonname().equals(id)) {
            return createOneParameterActionCommand(object, RedirectActionField.to.name(), ActionCommand.Commands.REDIRECT);
        } else if (ActionCommand.Commands.FILEINTO.getJsonname().equals(id)) {
            return createFileintoActionCommand(object, MoveActionField.into.name(), ActionCommand.Commands.FILEINTO);
        } else if (ActionCommand.Commands.REJECT.getJsonname().equals(id)) {
            return createOneParameterActionCommand(object, RejectActionField.text.name(), ActionCommand.Commands.REJECT);
        } else if (ActionCommand.Commands.STOP.getJsonname().equals(id)) {
            return new ActionCommand(ActionCommand.Commands.STOP, new ArrayList<Object>());
        } else if (ActionCommand.Commands.VACATION.getJsonname().equals(id)) {
            final ArrayList<Object> arrayList = new ArrayList<Object>();
            final String days = object.getString(VacationActionField.days.getFieldName());
            if (null != days) {
                arrayList.add(createTagArg(VacationActionField.days));
                arrayList.add(createNumberArg(days));
            }
            final JSONArray addresses = object.optJSONArray(VacationActionField.addresses.getFieldName());
            if (null != addresses) {
                arrayList.add(createTagArg(VacationActionField.addresses));
                arrayList.add(JSONArrayToStringList(addresses));
            }
            final String subjectFieldname = VacationActionField.subject.getFieldName();
            if (object.has(subjectFieldname)) {
                String subject = object.getString(subjectFieldname);
                subject = MimeMessageUtility.quotePhrase(subject, true);
                arrayList.add(createTagArg(VacationActionField.subject));
                arrayList.add(stringToList(subject));
            }
            final String fromFieldName = VacationActionField.from.getFieldName();
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
                        throw OXJSONExceptionCodes.INVALID_VALUE.create(from, VacationActionField.from.getFieldName());
                    }
                } else if (obj instanceof String) {
                    // Get string
                    String fromStr = object.getString(fromFieldName);
                    try {
                        new QuotedInternetAddress(fromStr, strict);
                        from = fromStr;
                    } catch (AddressException e) {
                        throw OXJSONExceptionCodes.INVALID_VALUE.create(from, VacationActionField.from.getFieldName());
                    }
                }

                if (!Strings.isEmpty(from)) {
                    arrayList.add(createTagArg(VacationActionField.from));
                    arrayList.add(stringToList(from));
                }
            }
            final String text = object.getString(VacationActionField.text.getFieldName());
            if (null == text) {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create("Parameter " + VacationActionField.text.getFieldName() + " is missing for " + ActionCommand.Commands.VACATION.getJsonname() + " is missing in JSON-Object. This is a required field");
            }
            arrayList.add(stringToList(text.replaceAll("(\r)?\n", "\r\n")));
            return new ActionCommand(ActionCommand.Commands.VACATION, arrayList);
        } else if (ActionCommand.Commands.ENOTIFY.getJsonname().equals(id)) {
            final ArrayList<Object> arrayList = new ArrayList<Object>();
            final String messageFieldName = EnotifyActionField.message.getFieldName();
            if (object.has(messageFieldName)) {
                final String message = object.getString(messageFieldName);
                arrayList.add(createTagArg(EnotifyActionField.message));
                arrayList.add(stringToList(message));
            }
            final String method = object.getString(EnotifyActionField.method.getFieldName());
            if (null == method) {
                throw new JSONException("Parameter " + EnotifyActionField.method.getFieldName() + " is missing for " + ActionCommand.Commands.ENOTIFY.getJsonname() + " is missing in JSON-Object. This is a required field");
            }
            arrayList.add(stringToList(method.replaceAll("(\r)?\n", "\r\n")));
            return new ActionCommand(ActionCommand.Commands.ENOTIFY, arrayList);
        } else if (ActionCommand.Commands.ADDFLAG.getJsonname().equals(id)) {
            final JSONArray array = object.getJSONArray(AddFlagsActionField.flags.name());
            if (null == array) {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create("Parameter " + AddFlagsActionField.flags + " is missing for " + ActionCommand.Commands.ADDFLAG.getJsonname() + " is missing in JSON-Object. This is a required field");
            }
            final ArrayList<Object> arrayList = new ArrayList<Object>();
            arrayList.add(JSONArrayToStringList(array));
            return new ActionCommand(ActionCommand.Commands.ADDFLAG, arrayList);
        } else if (ActionCommand.Commands.PGP_ENCRYPT.getJsonname().equals(id)) {
            final ArrayList<Object> arrayList = new ArrayList<Object>();
            final JSONArray keys = object.optJSONArray(PGPEncryptActionField.keys.getFieldName());
            if (null != keys) {
                if (0 == keys.length()) {
                    throw new JSONException("Empty string-arrays are not allowed in sieve.");
                }
                arrayList.add(createTagArg(PGPEncryptActionField.keys));
                arrayList.add(JSONArrayToStringList(keys));
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

    private String getString(final JSONObject jobj, final String value, final String component) throws OXException {
        try {
            return jobj.getString(value);
        } catch (final JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, "Error while reading ActionCommand " + component + ": " + e.getMessage());
        }
    }

    private TagArgument createTagArg(final VacationActionField field) {
        final Token token = new Token();
        token.image = field.getTagName();
        return new TagArgument(token);
    }

    private TagArgument createTagArg(final EnotifyActionField field) {
        final Token token = new Token();
        token.image = field.getTagName();
        return new TagArgument(token);
    }

    private TagArgument createTagArg(final PGPEncryptActionField field) {
        final Token token = new Token();
        token.image = field.getTagName();
        return new TagArgument(token);
    }

    private List<String> JSONArrayToStringList(JSONArray jarray) throws JSONException {
        int length = jarray.length();
        List<String> retval = new ArrayList<String>(length);
        for (int i = 0; i < length; i++) {
            retval.add(jarray.getString(i));
        }
        return retval;
    }

    private NumberArgument createNumberArg(final String string) {
        final Token token = new Token();
        token.image = string;
        return new NumberArgument(token);
    }

}
