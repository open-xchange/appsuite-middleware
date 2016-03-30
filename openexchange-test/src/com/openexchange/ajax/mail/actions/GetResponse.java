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

package com.openexchange.ajax.mail.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.HeaderCollection;
import com.openexchange.mail.mime.dataobjects.MimeMailMessage;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.messaging.MessagingExceptionCodes;

/**
 * {@link GetResponse}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class GetResponse extends AbstractAJAXResponse {

    private MailMessage mail;

    private JSONArray attachments;

    GetResponse(final Response response) {
        super(response);
    }

    /**
     * Parses an instance of {@link MailMessage} from response's data
     *
     * @param timeZone
     *            The user time zone
     * @return The parsed instance of {@link MailMessage}
     * @throws JSONException
     * @throws OXException
     */
    public MailMessage getMail(final TimeZone timeZone) throws JSONException, OXException {
        if (null == mail) {
            final MailMessage parsed = new MimeMailMessage();
            final JSONObject jsonObject;
            if (getResponse().getData() instanceof JSONObject) {
                jsonObject = (JSONObject) getResponse().getData();
            } else {
                jsonObject = new JSONObject(getResponse().getData().toString());
            }
            parse(jsonObject, parsed, timeZone);
            mail = parsed;
        }
        return mail;
    }

    public JSONArray getAttachments() throws JSONException {
        if (null == attachments) {
            final JSONObject jsonObj;
            if (getResponse().getData() instanceof JSONObject) {
                jsonObj = (JSONObject) getResponse().getData();
            } else {
                jsonObj = new JSONObject(getResponse().getData().toString());
            }
            if (jsonObj.hasAndNotNull(MailJSONField.ATTACHMENTS.getKey())) {
                attachments = jsonObj.getJSONArray(MailJSONField.ATTACHMENTS.getKey());
            }
        }
        return attachments;
    }

    private static void parse(final JSONObject jsonObj, final MailMessage mail, final TimeZone timeZone)
            throws OXException {
        try {
            /*
             * System flags
             */
            if (jsonObj.has(MailJSONField.FLAGS.getKey()) && !jsonObj.isNull(MailJSONField.FLAGS.getKey())) {
                mail.setFlags(jsonObj.getInt(MailJSONField.FLAGS.getKey()));
            }
            /*
             * Thread level
             */
            if (jsonObj.has(MailJSONField.THREAD_LEVEL.getKey())
                    && !jsonObj.isNull(MailJSONField.THREAD_LEVEL.getKey())) {
                mail.setThreadLevel(jsonObj.getInt(MailJSONField.THREAD_LEVEL.getKey()));
            }
            /*
             * User flags
             */
            if (jsonObj.has(MailJSONField.USER.getKey()) && !jsonObj.isNull(MailJSONField.USER.getKey())) {
                final JSONArray arr = jsonObj.getJSONArray(MailJSONField.USER.getKey());
                final int length = arr.length();
                final List<String> l = new ArrayList<String>(length);
                for (int i = 0; i < length; i++) {
                    l.add(arr.getString(i));
                }
                mail.addUserFlags(l.toArray(new String[l.size()]));
            }
            /*
             * Parse headers
             */
            if (jsonObj.has(MailJSONField.HEADERS.getKey()) && !jsonObj.isNull(MailJSONField.HEADERS.getKey())) {
                final JSONObject obj = jsonObj.getJSONObject(MailJSONField.HEADERS.getKey());
                final int size = obj.length();
                final HeaderCollection headers = new HeaderCollection(size);
                final Iterator<String> iter = obj.keys();
                for (int i = 0; i < size; i++) {
                    final String key = iter.next();
                    headers.addHeader(key, obj.getString(key));
                }
                mail.addHeaders(headers);
            }
            /*
             * From Only mandatory if non-draft message
             */
            mail.addFrom(parseAddressKey(MailJSONField.FROM.getKey(), jsonObj));
            /*
             * To Only mandatory if non-draft message
             */
            mail.addTo(parseAddressKey(MailJSONField.RECIPIENT_TO.getKey(), jsonObj));
            /*
             * Cc
             */
            mail.addCc(parseAddressKey(MailJSONField.RECIPIENT_CC.getKey(), jsonObj));
            /*
             * Bcc
             */
            mail.addBcc(parseAddressKey(MailJSONField.RECIPIENT_BCC.getKey(), jsonObj));
            /*
             * Disposition notification
             */
            if (jsonObj.has(MailJSONField.DISPOSITION_NOTIFICATION_TO.getKey())
                    && !jsonObj.isNull(MailJSONField.DISPOSITION_NOTIFICATION_TO.getKey())) {
                /*
                 * Ok, disposition-notification-to is set. Check if its value is
                 * a valid email address
                 */
                final String dispVal = jsonObj.getString(MailJSONField.DISPOSITION_NOTIFICATION_TO.getKey());
                if ("true".equalsIgnoreCase(dispVal)) {
                    /*
                     * Boolean value "true"
                     */
                    mail.setDispositionNotification(mail.getFrom().length > 0 ? mail.getFrom()[0] : null);
                } else {
                    final InternetAddress ia = getEmailAddress(dispVal);
                    if (ia == null) {
                        /*
                         * Any other value
                         */
                        mail.setDispositionNotification(null);
                    } else {
                        /*
                         * Valid email address
                         */
                        mail.setDispositionNotification(ia);
                    }
                }
            }
            /*
             * Priority
             */
            if (jsonObj.has(MailJSONField.PRIORITY.getKey()) && !jsonObj.isNull(MailJSONField.PRIORITY.getKey())) {
                mail.setPriority(jsonObj.getInt(MailJSONField.PRIORITY.getKey()));
            }
            /*
             * Color Label
             */
            if (jsonObj.has(MailJSONField.COLOR_LABEL.getKey()) && !jsonObj.isNull(MailJSONField.COLOR_LABEL.getKey())) {
                mail.setColorLabel(jsonObj.getInt(MailJSONField.COLOR_LABEL.getKey()));
            }
            /*
             * VCard
             */
            if (jsonObj.has(MailJSONField.VCARD.getKey()) && !jsonObj.isNull(MailJSONField.VCARD.getKey())) {
                mail.setAppendVCard((jsonObj.getInt(MailJSONField.VCARD.getKey()) > 0));
            }
            /*
             * Msg Ref
             */
            if (jsonObj.has(MailJSONField.MSGREF.getKey()) && !jsonObj.isNull(MailJSONField.MSGREF.getKey())) {
                mail.setMsgref(new MailPath(jsonObj.getString(MailJSONField.MSGREF.getKey())));
            }
            /*
             * Subject, etc.
             */
            if (jsonObj.has(MailJSONField.SUBJECT.getKey()) && !jsonObj.isNull(MailJSONField.SUBJECT.getKey())) {
                mail.setSubject(jsonObj.getString(MailJSONField.SUBJECT.getKey()));
            }
            /*
             * Size
             */
            if (jsonObj.has(MailJSONField.SIZE.getKey())) {
                mail.setSize(jsonObj.getInt(MailJSONField.SIZE.getKey()));
            }
            {
                /*
                 * Sent & received date
                 */
                if (jsonObj.has(MailJSONField.SENT_DATE.getKey()) && !jsonObj.isNull(MailJSONField.SENT_DATE.getKey())) {
                    final Date date = new Date(jsonObj.getLong(MailJSONField.SENT_DATE.getKey()));
                    final int offset = timeZone.getOffset(date.getTime());
                    mail.setSentDate(new Date(jsonObj.getLong(MailJSONField.SENT_DATE.getKey()) - offset));
                }
                if (jsonObj.has(MailJSONField.RECEIVED_DATE.getKey()) && !jsonObj.isNull(MailJSONField.RECEIVED_DATE.getKey())) {
                    Object object = jsonObj.get(MailJSONField.RECEIVED_DATE.getKey());
                    final Date date;
                    if (object instanceof JSONObject) {
                        date = new Date(((JSONObject)object).getLong("utc"));
                    } else {
                        date = new Date((Long)object);
                    }
                    final int offset = timeZone.getOffset(date.getTime());
                    mail.setReceivedDate(new Date(date.getTime() - offset));
                }
            }
            /*
             * TODO: Parse attachments
             */
        } catch (final JSONException e) {
            throw MessagingExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
    }

    private static InternetAddress getEmailAddress(final String addrStr) {
        if (addrStr == null || addrStr.length() == 0) {
            return null;
        }
        try {
            return InternetAddress.parse(addrStr, true)[0];
        } catch (final AddressException e) {
            return null;
        }
    }

    private static final InternetAddress[] EMPTY_ADDRS = new InternetAddress[0];

    private static InternetAddress[] parseAddressKey(final String key, final JSONObject jo) throws JSONException {
        String value = null;
        if (!jo.has(key) || jo.isNull(key) || (value = jo.getString(key)).length() == 0) {
            return EMPTY_ADDRS;
        }
        if (value.charAt(0) == '[') {
            final JSONArray outer = new JSONArray(value);
            final int l1 = outer.length();
            if (l1 == 0) {
                return EMPTY_ADDRS;
            }
            final StringBuilder sb = new StringBuilder(l1 * 64);
            {
                /*
                 * Add first address
                 */
                final JSONArray persAndAddr = outer.getJSONArray(0);
                final String personal = persAndAddr.getString(0);
                final boolean hasPersonal = (personal != null && !"null".equals(personal));
                if (hasPersonal) {
                    sb.append(MimeMessageUtility.quotePersonal(personal)).append(" <");
                }
                sb.append(persAndAddr.getString(1));
                if (hasPersonal) {
                    sb.append('>');
                }
            }
            for (int i = 1; i < l1; i++) {
                sb.append(", ");
                final JSONArray persAndAddr = outer.getJSONArray(i);
                final String personal = persAndAddr.getString(0);
                final boolean hasPersonal = (personal != null && !"null".equals(personal));
                if (hasPersonal) {
                    sb.append(MimeMessageUtility.quotePersonal(personal)).append(" <");
                }
                sb.append(persAndAddr.getString(1));
                if (hasPersonal) {
                    sb.append('>');
                }
            }
            value = sb.toString();
        }
        try {
            return InternetAddress.parse(value, false);
        } catch (final javax.mail.internet.AddressException e) {
            return new InternetAddress[] { new com.openexchange.mail.mime.PlainTextAddress(value) };
        }
    }
}
