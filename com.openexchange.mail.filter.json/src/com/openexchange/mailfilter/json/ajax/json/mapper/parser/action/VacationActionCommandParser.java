/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mailfilter.json.ajax.json.mapper.parser.action;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import org.apache.jsieve.SieveException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.CharMatcher;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.mail.json.parser.MessageParser;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mailfilter.json.ajax.json.fields.GeneralField;
import com.openexchange.mailfilter.json.ajax.json.fields.VacationActionField;
import com.openexchange.mailfilter.json.ajax.json.mapper.ArgumentUtil;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.exceptions.CommandParserExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link VacationActionCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class VacationActionCommandParser implements CommandParser<ActionCommand> {

    private static final Logger LOG = LoggerFactory.getLogger(VacationActionCommandParser.class);

    /**
     * Initialises a new {@link VacationActionCommandParser}.
     */
    public VacationActionCommandParser() {
        super();
    }

    @Override
    public ActionCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {
        final ArrayList<Object> arrayList = new ArrayList<Object>();
        final String days = jsonObject.getString(VacationActionField.days.getFieldName());
        if (days == null) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create("Parameter " + VacationActionField.days.getFieldName() + " is missing for " + ActionCommand.Commands.VACATION.getJsonName() + " is missing in JSON-Object. This is a required field");
        }
        arrayList.add(ArgumentUtil.createTagArgument(VacationActionField.days));
        arrayList.add(ArgumentUtil.createNumberArgument(days));

        final JSONArray addresses = jsonObject.optJSONArray(VacationActionField.addresses.getFieldName());
        if (null != addresses) {
            arrayList.add(ArgumentUtil.createTagArgument(VacationActionField.addresses));
            List<String> addressesList = CommandParserJSONUtil.coerceToStringList(addresses);
            validateAddresses(addressesList, true);
            arrayList.add(addressesList);
        }
        final String subjectFieldname = VacationActionField.subject.getFieldName();
        if (jsonObject.has(subjectFieldname)) {
            String subject = jsonObject.getString(subjectFieldname);
            subject = encode(subject, VacationActionField.subject);
            arrayList.add(ArgumentUtil.createTagArgument(VacationActionField.subject));
            arrayList.add(CommandParserJSONUtil.stringToList(subject));
        }
        final String fromFieldName = VacationActionField.from.getFieldName();
        if (jsonObject.has(fromFieldName)) {
            String from = "";
            Object obj = jsonObject.get(fromFieldName);
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
                String fromStr = jsonObject.getString(fromFieldName);
                try {
                    InternetAddressUtil.validateInternetAddress(fromStr, strict);
                    from = fromStr;
                } catch (AddressException e) {
                    throw OXJSONExceptionCodes.INVALID_VALUE.create(from, VacationActionField.from.getFieldName());
                }
            }

            if (Strings.isNotEmpty(from)) {
                arrayList.add(ArgumentUtil.createTagArgument(VacationActionField.from));
                arrayList.add(CommandParserJSONUtil.stringToList(from));
            }
        }
        final String text = jsonObject.getString(VacationActionField.text.getFieldName());
        if (null == text) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create("Parameter " + VacationActionField.text.getFieldName() + " is missing for " + ActionCommand.Commands.VACATION.getJsonName() + " is missing in JSON-Object. This is a required field");
        }
        arrayList.add(CommandParserJSONUtil.stringToList(text.replaceAll("(\r)?\n", "\r\n")));

        return new ActionCommand(ActionCommand.Commands.VACATION, arrayList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void parse(JSONObject jsonObject, ActionCommand actionCommand) throws JSONException, OXException {
        ArrayList<Object> arguments = actionCommand.getArguments();

        jsonObject.put(GeneralField.id.name(), ActionCommand.Commands.VACATION.getJsonName());
        final Hashtable<String, List<String>> tagArguments = actionCommand.getTagArguments();
        final List<String> days = tagArguments.get(VacationActionField.days.getTagName());
        if (null != days) {
            jsonObject.put(VacationActionField.days.getFieldName(), days.get(0));
        }
        final List<String> addresses = tagArguments.get(VacationActionField.addresses.getTagName());
        if (null != addresses) {
            jsonObject.put(VacationActionField.addresses.getFieldName(), addresses);
        }
        final List<String> subject = tagArguments.get(VacationActionField.subject.getTagName());
        if (null != subject) {
            String decodedSubject = decode(subject.get(0), VacationActionField.subject);
            jsonObject.put(VacationActionField.subject.getFieldName(), decodedSubject);
        }
        final List<String> from = tagArguments.get(VacationActionField.from.getTagName());
        if (null != from) {
            String decodedFrom = MimeMessageUtility.decodeEnvelopeSubject(from.get(0));
            jsonObject.put(VacationActionField.from.getFieldName(), decodedFrom);
        }
        jsonObject.put(VacationActionField.text.getFieldName(), ((List<String>) arguments.get(arguments.size() - 1)).get(0));
    }

    /**
     * Encodes the specified UTF-8 string if necessary and returns the encoded string
     *
     * @param string The string to encode
     * @param field The field
     * @return The encoded string
     * @throws OXException if the string cannot be decoded
     */
    private String encode(String string, VacationActionField field) throws OXException {
        if (CharMatcher.ascii().matchesAllOf(string)) {
            return string;
        }
        try {
            return MimeUtility.encodeText(string);
        } catch (UnsupportedEncodingException e) {
            throw CommandParserExceptionCodes.UNABLE_TO_ENCODE.create(field.name(), "Vacation");
        }
    }

    /**
     * Decodes the specified UTF-8 string if necessary and returns the decoded string
     *
     * @param utf8 The UTF-8 encoded string
     * @param field The field
     * @return The decoded string
     * @throws OXException if the string cannot be decoded
     */
    private String decode(String utf8, VacationActionField field) throws OXException {
        if (Strings.isEmpty(utf8)) {
            return utf8;
        }
        if (!utf8.startsWith("=?UTF")) {
            return utf8;
        }
        try {
            return MimeUtility.decodeText(utf8);
        } catch (UnsupportedEncodingException e) {
            throw CommandParserExceptionCodes.UNABLE_TO_DECODE.create(field.name(), "Vacation");
        }
    }

    /**
     * Validates the specified addresses and removes any that fail validation
     * 
     * @param addresses The addresses to validate
     * @param strict Whether or not to use strict mode
     */
    private void validateAddresses(List<String> addresses, boolean strict) {
        Iterator<String> iterator = addresses.iterator();
        while (iterator.hasNext()) {
            String address = iterator.next();
            try {
                InternetAddressUtil.validateInternetAddress(address, strict);
            } catch (AddressException e) {
                LOG.debug("The address '{}' is invalid. Ignoring", address, e);
                iterator.remove();
            }
        }
    }
}
