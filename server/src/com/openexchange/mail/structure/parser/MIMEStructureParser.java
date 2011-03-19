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

package com.openexchange.mail.structure.parser;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import javax.mail.MessagingException;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.HeaderName;
import com.openexchange.mail.mime.MIMEDefaultSession;
import com.openexchange.mail.mime.ParameterizedHeader;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.tools.TimeZoneUtils;

/**
 * {@link MIMEStructureParser}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MIMEStructureParser {

    /**
     * Initializes a new {@link MIMEStructureParser}.
     */
    public MIMEStructureParser() {
        super();
    }

    public ComposedMailMessage parseStructure(final JSONObject jsonStructure) {
        return null;
    }

    private MimeMessage parseStructure2Message(final JSONObject jsonStructure) throws MailException {
        try {
            final MimeMessage mimeMessage = new MimeMessage(MIMEDefaultSession.getDefaultSession());

            final JSONObject jsonHeaders = jsonStructure.getJSONObject("headers");

            return mimeMessage;
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    private static final Set<String> HEADERS_ADDRESS = new HashSet<String>(Arrays.asList(
        "from",
        "to",
        "cc",
        "bcc",
        "reply-to",
        "sender",
        "errors-to",
        "resent-bcc",
        "resent-cc",
        "resent-from",
        "resent-to",
        "resent-sender",
        "disposition-notification-to"));

    private static final Set<String> HEADERS_DATE = new HashSet<String>(Arrays.asList("date"));

    private static void parseHeaders(final JSONObject jsonHeaders, final MimePart mimePart) throws JSONException, MessagingException, MailException {
        try {
            for (final Entry<String, Object> entry : jsonHeaders.entrySet()) {
                final String name = entry.getKey().toLowerCase(Locale.ENGLISH);
                if (HEADERS_ADDRESS.contains(name)) {
                    final JSONArray jsonAddresses = (JSONArray) entry.getValue();
                    final int length = jsonAddresses.length();
                    for (int i = length - 1; i >= 0; i--) {
                        final JSONObject jsonAddress = jsonAddresses.getJSONObject(i);
                        final String address = jsonAddress.getString("address");
                        final String personal;
                        if (jsonAddress.hasAndNotNull("personal")) {
                            personal = jsonAddress.getString("personal");
                        } else {
                            personal = null;
                        }
                        final QuotedInternetAddress qia = new QuotedInternetAddress(address, personal, "UTF-8");
                        mimePart.addHeader(name, qia.toString());
                    }
                } else if (HEADERS_DATE.contains(name)) {
                    final JSONObject jsonDate = (JSONObject) entry.getValue();
                    mimePart.setHeader(name, jsonDate.getString("date"));
                } else if ("content-type".equals(name)) {
                    final JSONObject jsonContentType = (JSONObject) entry.getValue();
                    final ContentType contentType = new ContentType();
                    contentType.setBaseType(jsonContentType.getString("type"));
                    parseParameterList(jsonContentType.getJSONObject("params"), contentType);
                    mimePart.setHeader(name, contentType.toString(true));
                } else if ("content-disposition".equals(name)) {
                    final JSONObject jsonContentDisposition = (JSONObject) entry.getValue();
                    final ContentDisposition contentDisposition = new ContentDisposition();
                    contentDisposition.setDisposition(jsonContentDisposition.getString("type"));
                    parseParameterList(jsonContentDisposition.getJSONObject("params"), contentDisposition);
                    mimePart.setHeader(name, contentDisposition.toString(true));
                } else {
                    final Object value = entry.getValue();
                    if (value instanceof JSONArray) {
                        final JSONArray jsonHeader = (JSONArray) value;
                        final int length = jsonHeader.length();
                        for (int i = length - 1; i >= 0; i--) {
                            mimePart.addHeader(name, jsonHeader.getString(i));
                        }
                    } else {
                        mimePart.setHeader(name, (String) value);
                    }
                }
            }
        } catch (final UnsupportedEncodingException e) {
            // Cannot occur
            throw new MessagingException(e.getMessage(), e);
        }
    }

    private static void parseParameterList(final JSONObject jsonParameters, final ParameterizedHeader parameterizedHeader) throws JSONException {
        for (final Entry<String, Object> entry : jsonParameters.entrySet()) {
            final String name = entry.getKey().toLowerCase(Locale.ENGLISH);
            if ("read-date".equals(name)) {
                final JSONObject jsonDate = (JSONObject) entry.getValue();
                parameterizedHeader.addParameter(name, jsonDate.getString("date"));
            } else {
                parameterizedHeader.addParameter(name, (String) entry.getValue());
            }
        }
    }
    
}
