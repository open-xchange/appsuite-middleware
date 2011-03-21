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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import javax.activation.DataHandler;
import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MIMEDefaultSession;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.ParameterizedHeader;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.session.Session;

/**
 * {@link MIMEStructureParser}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MIMEStructureParser {

    /**
     * Initializes a new {@link MIMEStructureParser}.
     */
    private MIMEStructureParser() {
        super();
    }

    /**
     * Parses specified JSON mail structure to a {@link MailMessage} instance.
     * 
     * @param jsonStructure The JSON mail structure
     * @return The {@link MailMessage} instance
     * @throws MailException If parsing fails
     */
    public MailMessage parseStructure(final JSONObject jsonStructure) throws MailException {
        /*
         * Parse JSON to MIME message
         */
        final MimeMessage mimeMessage = parseStructure2Message(jsonStructure);
        /*
         * Create appropriate MailMessage instance
         */
        return MIMEMessageConverter.convertMessage(mimeMessage);
    }

    /**
     * Parses specified JSON mail structure to a transportable {@link ComposedMailMessage} instance.
     * 
     * @param jsonStructure The JSON mail structure
     * @param session The session
     * @return The transportable {@link ComposedMailMessage} instance
     * @throws MailException If parsing fails
     */
    public ComposedMailMessage parseStructure(final JSONObject jsonStructure, final Session session) throws MailException {
        try {
            /*
             * Create appropriate ComposedMailMessage instance
             */
            final Context ctx = ContextStorage.getStorageContext(session.getContextId());
            return new ComposedMailWrapper(parseStructure(jsonStructure), session, ctx);
        } catch (final ContextException e) {
            throw new MailException(e);
        }
    }

    private static MimeMessage parseStructure2Message(final JSONObject jsonStructure) throws MailException {
        final MimeMessage mimeMessage = new MimeMessage(MIMEDefaultSession.getDefaultSession());
        parseMessage(jsonStructure, mimeMessage);
        return mimeMessage;
    }

    private static void parseMessage(final JSONObject jsonMessage, final MimeMessage mimeMessage) throws MailException {
        parseFlags(jsonMessage, mimeMessage);
        parsePart(jsonMessage, mimeMessage);
    }

    private static void parseFlags(final JSONObject jsonMessage, final MimeMessage mimeMessage) throws MailException {
        try {
            Flags msgFlags = null;
            /*
             * System flags
             */
            if (jsonMessage.hasAndNotNull("flags")) {
                msgFlags = new Flags();
                final int flags = jsonMessage.getInt("flags");
                if ((flags & MailMessage.FLAG_ANSWERED) > 0) {
                    msgFlags.add(Flags.Flag.ANSWERED);
                }
                if ((flags & MailMessage.FLAG_DELETED) > 0) {
                    msgFlags.add(Flags.Flag.DELETED);
                }
                if ((flags & MailMessage.FLAG_DRAFT) > 0) {
                    msgFlags.add(Flags.Flag.DRAFT);
                }
                if ((flags & MailMessage.FLAG_FLAGGED) > 0) {
                    msgFlags.add(Flags.Flag.FLAGGED);
                }
                if ((flags & MailMessage.FLAG_RECENT) > 0) {
                    msgFlags.add(Flags.Flag.RECENT);
                }
                if ((flags & MailMessage.FLAG_SEEN) > 0) {
                    msgFlags.add(Flags.Flag.SEEN);
                }
                if ((flags & MailMessage.FLAG_USER) > 0) {
                    msgFlags.add(Flags.Flag.USER);
                }
                if ((flags & MailMessage.FLAG_FORWARDED) > 0) {
                    msgFlags.add(MailMessage.USER_FORWARDED);
                }
                if ((flags & MailMessage.FLAG_READ_ACK) > 0) {
                    msgFlags.add(MailMessage.USER_READ_ACK);
                }
            }
            /*
             * Color label
             */
            if (jsonMessage.hasAndNotNull("color_label")) {
                if (msgFlags == null) {
                    msgFlags = new Flags();
                }
                msgFlags.add(MailMessage.getColorLabelStringValue(jsonMessage.getInt("color_label")));
            }
            /*
             * User flags
             */
            if (jsonMessage.hasAndNotNull("user")) {
                if (msgFlags == null) {
                    msgFlags = new Flags();
                }
                final JSONArray jsonUser = jsonMessage.getJSONArray("user");
                final int length = jsonUser.length();
                for (int i = length - 1; i >= 0; i--) {
                    msgFlags.add(jsonUser.getString(i));
                }
            }
            /*
             * Finally, apply flags to message
             */
            if (msgFlags != null) {
                mimeMessage.setFlags(msgFlags, true);
            }
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
    }

    private static void parsePart(final JSONObject jsonPart, final MimePart mimePart) throws MailException {
        try {
            /*
             * Parse headers
             */
            final ContentType contentType = new ContentType(MIMETypes.MIME_DEFAULT);
            parseHeaders(jsonPart.getJSONObject("headers"), mimePart, contentType);
            /*
             * Determine Content-Type
             */
            if (contentType.startsWith("text/")) {
                /*
                 * A text/* part
                 */
                parseSimpleBodyText(jsonPart.getJSONObject("body"), mimePart, contentType);
            } else if (contentType.startsWith("multipart/")) {
                /*
                 * A multipart
                 */
                parseMultipartBody(jsonPart.getJSONArray("body"), mimePart);
            } else if (contentType.startsWith("message/rfc822")) {
                /*
                 * A nested message
                 */
                parseMessageBody(jsonPart.getJSONObject("body"), mimePart);
            } else {
                parseSimpleBodyBinary(jsonPart.getJSONObject("body"), mimePart, contentType);
            }
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    private static void parseMessageBody(final JSONObject jsonMessage, final MimePart mimePart) throws MailException {
        try {
            final MimeMessage mimeMessage = new MimeMessage(MIMEDefaultSession.getDefaultSession());
            parseMessage(jsonMessage, mimeMessage);
            mimePart.setContent(mimeMessage, "message/rfc822");
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
    }

    private static void parseMultipartBody(final JSONArray jsonMultiparts, final MimePart mimePart) throws MailException {
        try {
            final MimeMultipart multipart = new MimeMultipart();
            final int length = jsonMultiparts.length();
            for (int i = length - 1; i >= 0; i--) {
                final MimeBodyPart bodyPart = new MimeBodyPart();
                parsePart(jsonMultiparts.getJSONObject(i), bodyPart);
                multipart.addBodyPart(bodyPart);
            }
            mimePart.setContent(multipart);
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
    }

    private static void parseSimpleBodyText(final JSONObject jsonBody, final MimePart mimePart, final ContentType contentType) throws MailException {
        try {
            if (isText(contentType.getBaseType())) {
                mimePart.setText(jsonBody.getString("data"), "UTF-8", contentType.getSubType());
                mimePart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, contentType.toString(true));
            } else {
                final byte[] bytes = jsonBody.getString("data").toString().getBytes("UTF-8");
                mimePart.setDataHandler(new DataHandler(new MessageDataSource(bytes, contentType.toString(true))));
                mimePart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, contentType.toString(true));
            }
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        } catch (final UnsupportedEncodingException e) {
            throw new MailException(MailException.Code.ENCODING_ERROR, e, e.getMessage());
        }
    }

    private static void parseSimpleBodyBinary(final JSONObject jsonBody, final MimePart mimePart, final ContentType contentType) throws MailException {
        try {
            mimePart.setDataHandler(new DataHandler(new MessageDataSource(Base64.decodeBase64(jsonBody.getString("data").getBytes(
                "US-ASCII")), contentType.getBaseType())));
            mimePart.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "base64");
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        } catch (final UnsupportedEncodingException e) {
            throw new MailException(MailException.Code.ENCODING_ERROR, e, e.getMessage());
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

    private static void parseHeaders(final JSONObject jsonHeaders, final MimePart mimePart, final ContentType contentType) throws MailException {
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
                    contentType.reset();
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
            /*
             * Cannot occur
             */
            throw new MailException(MailException.Code.ENCODING_ERROR, e, e.getMessage());
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
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

    private static final String PRIMARY_TEXT = "text/";

    private static final String[] SUB_TEXT = { "plain", "htm" };

    /**
     * Checks if content type matches one of text content types:
     * <ul>
     * <li><code>text/plain</code></li>
     * <li><code>text/htm</code>&nbsp;or&nbsp;<code>text/html</code></li>
     * </ul>
     * 
     * @param contentType The content type
     * @return <code>true</code> if content type matches text; otherwise <code>false</code>
     */
    private static boolean isText(final String contentType) {
        if (contentType.startsWith(PRIMARY_TEXT, 0)) {
            final int off = PRIMARY_TEXT.length();
            for (final String subtype : SUB_TEXT) {
                if (contentType.startsWith(subtype, off)) {
                    return true;
                }
            }
        }
        return false;
    }

}
