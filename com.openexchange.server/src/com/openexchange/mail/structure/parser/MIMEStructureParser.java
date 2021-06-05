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

package com.openexchange.mail.structure.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import javax.activation.DataHandler;
import javax.mail.Flags;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.common.collect.ImmutableSet;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.ParameterizedHeader;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link MIMEStructureParser}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MIMEStructureParser {

    public static void main(String[] args) {
        try {
            {
                final JSONObject jsonMail = new JSONObject("{\n" +
                		"  \"user\": [],\n" +
                		"  \"headers\": {\n" +
                		"    \"to\": [{\"address\": \"bob@foobar.com\"}, {\"address\": \"cane@rofl.com\"}],\n" +
                		"    \"received\": [\n" +
                		"      \"from localhost (localhost.localdomain [127.0.0.1]) by ox.open-xchange.com (Postfix) with ESMTP id 63BA22AC4004 for <holger.achtziger@open-xchange.com>; Fri, 30 Oct 2009 08:54:58 +0100 (CET)\",\n" +
                		"      \"from ox.open-xchange.com ([127.0.0.1]) by localhost (ox.open-xchange.com [127.0.0.1]) (amavisd-new, port 10024) with ESMTP id UjQ1iBeTBbxN for <holger.achtziger@open-xchange.com>; Fri, 30 Oct 2009 08:54:58 +0100 (CET)\"\n" +
                		"    ],\n" +
                		"    \"content-disposition\": {\n" +
                		"      \"type\": \"inline\",\n" +
                		"      \"params\": {\"filename\": \"foo.txt\"}\n" +
                		"    },\n" +
                		"    \"from\": [{\n" +
                		"      \"address\": \"alice@foobar.com\",\n" +
                		"      \"personal\": \"Alice Doe\"\n" +
                		"    }],\n" +
                		"    \"subject\": \"The mail subject\",\n" +
                		"    \"content-type\": {\n" +
                		"      \"type\": \"text/plain\",\n" +
                		"      \"params\": {\n" +
                		"        \"charset\": \"UTF-8\",\n" +
                		"        \"name\": \"foo.txt\"\n" +
                		"      }\n" +
                		"    },\n" +
                		"    \"date\": {\n" +
                		"      \"utc\": 1258214589000,\n" +
                		"      \"date\": \"Sat, 14 Nov 2009 17:03:09 +0100 (CET)\"\n" +
                		"    },\n" +
                		"    \"mime-version\": \"1.0\",\n" +
                		"    \"x-priority\": \"3\",\n" +
                		"    \"message-id\": \"<1837640730.5.1258214590077.JavaMail.foobar@foobar>\"\n" +
                		"  },\n" +
                		"  \"color_label\": 0,\n" +
                		"  \"flags\": 0,\n" +
                		"  \"received_date\": null,\n" +
                		"  \"body\": {\n" +
                		"    \"data\": \"Hello Dave.\\nPeople have been asking ...\",\n" +
                		"    \"id\": \"1\"\n" +
                		"  }\n" +
                		"}");

                final MailMessage mail = parseStructure(jsonMail);
                System.out.println(mail.getSource());
            }

            {

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes a new {@link MIMEStructureParser}.
     */
    private MIMEStructureParser() {
        super();
    }

    /**
     * Parses specified JSON mail structure to corresponding RFC822 bytes.
     *
     * @param jsonStructure The JSON mail structure
     * @return The RFC822 bytes
     * @throws OXException If parsing fails
     */
    public static byte[] parseStructure2MIME(JSONObject jsonStructure) throws OXException {
        try {
            /*
             * Parse JSON to MIME message
             */
            final MimeMessage mimeMessage = parseStructure2Message(jsonStructure);
            /*
             * Write MIME message's source
             */
            final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(4096);
            mimeMessage.writeTo(out);
            return out.toByteArray();
        } catch (IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Parses specified JSON mail structure to a {@link MailMessage} instance.
     *
     * @param jsonStructure The JSON mail structure
     * @return The {@link MailMessage} instance
     * @throws OXException If parsing fails
     */
    public static MailMessage parseStructure(JSONObject jsonStructure) throws OXException {
        /*
         * Parse JSON to MIME message
         */
        final MimeMessage mimeMessage = parseStructure2Message(jsonStructure);
        /*
         * Create appropriate MailMessage instance
         */
        return MimeMessageConverter.convertMessage(mimeMessage);
    }

    /**
     * Parses specified JSON mail structure to a transportable {@link ComposedMailMessage} instance.
     *
     * @param jsonStructure The JSON mail structure
     * @param session The session
     * @return The transportable {@link ComposedMailMessage} instance
     * @throws OXException If parsing fails
     */
    public static ComposedMailMessage parseStructure(JSONObject jsonStructure, Session session) throws OXException {
        /*
         * Create appropriate ComposedMailMessage instance
         */
        final Context ctx = ContextStorage.getStorageContext(session.getContextId());
        return new ComposedMailWrapper(parseStructure(jsonStructure), session, ctx);
    }

    /**
     * Parses specified JSON mail structure to a transportable {@link ComposedMailMessage} instance.
     *
     * @param jsonStructure The JSON mail structure
     * @param session The session
     * @return The transportable {@link ComposedMailMessage} instance
     * @throws OXException If parsing fails
     */
    public static ComposedMailMessage parseStructure(JSONObject jsonStructure, ServerSession session) throws OXException {
        /*
         * Create appropriate ComposedMailMessage instance
         */
        return new ComposedMailWrapper(parseStructure(jsonStructure), session, session.getContext());
    }

    private static MimeMessage parseStructure2Message(JSONObject jsonStructure) throws OXException {
        final MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession());
        parseMessage(jsonStructure, mimeMessage);
        return mimeMessage;
    }

    private static void parseMessage(JSONObject jsonMessage, MimeMessage mimeMessage) throws OXException {
        parseFlags(jsonMessage, mimeMessage);
        parsePart(jsonMessage, mimeMessage);
    }

    private static void parseFlags(JSONObject jsonMessage, MimeMessage mimeMessage) throws OXException {
        try {
            Flags msgFlags = null;
            /*
             * System flags
             */
            if (jsonMessage.hasAndNotNull("flags")) {
                final int flags = jsonMessage.getInt("flags");
                if (flags > 0) {
                    msgFlags = new Flags();
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
            }
            /*
             * Color label
             */
            if (jsonMessage.hasAndNotNull("color_label")) {
                final int colorLabel = jsonMessage.getInt("color_label");
                if (colorLabel != MailMessage.COLOR_LABEL_NONE) {
                    if (msgFlags == null) {
                        msgFlags = new Flags();
                    }
                    msgFlags.add(MailMessage.getColorLabelStringValue(colorLabel));
                }
            }
            /*
             * User flags
             */
            if (jsonMessage.hasAndNotNull("user")) {
                final JSONArray jsonUser = jsonMessage.getJSONArray("user");
                final int length = jsonUser.length();
                if (length > 0) {
                    if (msgFlags == null) {
                        msgFlags = new Flags();
                    }
                    for (int i = length - 1; i >= 0; i--) {
                        msgFlags.add(jsonUser.getString(i));
                    }
                }
            }
            /*
             * Finally, apply flags to message
             */
            if (msgFlags != null) {
                mimeMessage.setFlags(msgFlags, true);
            }
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private static void parsePart(JSONObject jsonPart, MimePart mimePart) throws OXException {
        try {
            /*
             * Parse headers
             */
            final ContentType contentType = new ContentType(MimeTypes.MIME_DEFAULT);
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
                parseMultipartBody(jsonPart.getJSONArray("body"), mimePart, contentType.getSubType());
            } else if (contentType.startsWith("message/rfc822") || (contentType.getNameParameter() != null && contentType.getNameParameter().endsWith(".eml"))) {
                /*
                 * A nested message
                 */
                parseMessageBody(jsonPart.getJSONObject("body"), mimePart);
            } else {
                parseSimpleBodyBinary(jsonPart.getJSONObject("body"), mimePart, contentType);
            }
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static void parseMessageBody(JSONObject jsonMessage, MimePart mimePart) throws OXException {
        try {
            final MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession());
            parseMessage(jsonMessage, mimeMessage);
            MessageUtility.setContent(mimeMessage, mimePart);
            // mimePart.setContent(mimeMessage, "message/rfc822");
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private static void parseMultipartBody(JSONArray jsonMultiparts, MimePart mimePart, String subtype) throws OXException {
        try {
            final MimeMultipart multipart = new MimeMultipart(subtype);
            final int length = jsonMultiparts.length();
            for (int i = 0; i < length; i++) {
                final MimeBodyPart bodyPart = new MimeBodyPart();
                parsePart(jsonMultiparts.getJSONObject(i), bodyPart);
                multipart.addBodyPart(bodyPart);
            }
            MessageUtility.setContent(multipart, mimePart);
            // mimePart.setContent(multipart);
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private static void parseSimpleBodyText(JSONObject jsonBody, MimePart mimePart, ContentType contentType) throws OXException {
        try {
            if (isText(contentType.getBaseType())) {
                MessageUtility.setText(jsonBody.getString("data"), "UTF-8", contentType.getSubType(), mimePart);
                // mimePart.setText(jsonBody.getString("data"), "UTF-8", contentType.getSubType());
                mimePart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, contentType.toString(true));
            } else {
                final byte[] bytes = jsonBody.getString("data").toString().getBytes(com.openexchange.java.Charsets.UTF_8);
                mimePart.setDataHandler(new DataHandler(new MessageDataSource(bytes, contentType.toString(true))));
                mimePart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, contentType.toString(true));
            }
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private static void parseSimpleBodyBinary(JSONObject jsonBody, MimePart mimePart, ContentType contentType) throws OXException {
        try {
            mimePart.setDataHandler(new DataHandler(new MessageDataSource(Base64.decodeBase64(jsonBody.getString("data").getBytes(
                "US-ASCII")), contentType.getBaseType())));
            mimePart.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "base64");
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (UnsupportedEncodingException e) {
            throw MailExceptionCode.ENCODING_ERROR.create(e, e.getMessage());
        }
    }

    private static final Set<String> HEADERS_ADDRESS = ImmutableSet.of(
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
        "disposition-notification-to");

    private static final Set<String> HEADERS_DATE = ImmutableSet.of("date");

    private static void parseHeaders(JSONObject jsonHeaders, MimePart mimePart, ContentType contentType) throws OXException {
        try {
            final StringBuilder headerNameBuilder = new StringBuilder(16);
            for (Entry<String, Object> entry : jsonHeaders.entrySet()) {
                final String name = entry.getKey().toLowerCase(Locale.ENGLISH);
                if (HEADERS_ADDRESS.contains(name)) {
                    final JSONArray jsonAddresses = (JSONArray) entry.getValue();
                    final int length = jsonAddresses.length();
                    final StringBuilder builder = new StringBuilder(32 * length);
                    final String delim = ", ";
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
                        builder.insert(0, qia.toString()).insert(0, delim);
                    }
                    mimePart.setHeader(toHeaderCase(name, headerNameBuilder), builder.delete(0, delim.length()).toString());
                } else if (HEADERS_DATE.contains(name)) {
                    final JSONObject jsonDate = (JSONObject) entry.getValue();
                    mimePart.setHeader(toHeaderCase(name, headerNameBuilder), jsonDate.getString("date"));
                } else if ("content-type".equals(name)) {
                    final JSONObject jsonContentType = (JSONObject) entry.getValue();
                    contentType.reset();
                    contentType.setBaseType(jsonContentType.getString("type"));
                    parseParameterList(jsonContentType.getJSONObject("params"), contentType);
                    mimePart.setHeader(toHeaderCase(name, headerNameBuilder), contentType.toString(true));
                } else if ("content-disposition".equals(name)) {
                    final JSONObject jsonContentDisposition = (JSONObject) entry.getValue();
                    final ContentDisposition contentDisposition = new ContentDisposition();
                    contentDisposition.setDisposition(jsonContentDisposition.getString("type"));
                    parseParameterList(jsonContentDisposition.getJSONObject("params"), contentDisposition);
                    mimePart.setHeader(toHeaderCase(name, headerNameBuilder), contentDisposition.toString(true));
                } else {
                    final Object value = entry.getValue();
                    if (value instanceof JSONArray) {
                        final JSONArray jsonHeader = (JSONArray) value;
                        final int length = jsonHeader.length();
                        final String headerName = toHeaderCase(name, headerNameBuilder);
                        for (int i = length - 1; i >= 0; i--) {
                            mimePart.addHeader(headerName, jsonHeader.getString(i));
                        }
                    } else {
                        mimePart.setHeader(toHeaderCase(name, headerNameBuilder), (String) value);
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            /*
             * Cannot occur
             */
            throw MailExceptionCode.ENCODING_ERROR.create(e, e.getMessage());
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static void parseParameterList(JSONObject jsonParameters, ParameterizedHeader parameterizedHeader) throws JSONException {
        for (Entry<String, Object> entry : jsonParameters.entrySet()) {
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
    private static boolean isText(String contentType) {
        if (contentType.startsWith(PRIMARY_TEXT, 0)) {
            final int off = PRIMARY_TEXT.length();
            for (String subtype : SUB_TEXT) {
                if (contentType.startsWith(subtype, off)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String toHeaderCase(String name, StringBuilder builder) {
        if (null == name) {
            return null;
        }
        if ("mime-version".equals(name)) {
            return "MIME-Version";
        }
        if ("message-id".equals(name)) {
            return "Message-ID";
        }
        final int len = name.length();
        if (len <= 0) {
            return "";
        }
        final StringBuilder sb;
        if (builder == null) {
            sb = new StringBuilder(len);
        } else {
            sb = builder;
            sb.setLength(0);
        }
        sb.append(Character.toUpperCase(name.charAt(0)));
        int i = 1;
        while (i < len) {
            final char c = name.charAt(i++);
            if ('-' == c && i < len) {
                sb.append(c).append(Character.toUpperCase(name.charAt(i++)));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
