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

package com.openexchange.mail.structure.parser;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.groupware.upload.impl.UploadFileImpl;
import com.openexchange.java.Charsets;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.dataobjects.compose.UploadFileMailPart;
import com.openexchange.mail.json.parser.AbortAttachmentHandler;
import com.openexchange.mail.json.parser.IAttachmentHandler;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.ParameterizedHeader;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link MIMEStructure2ComposedMailParser}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MIMEStructure2ComposedMailParser {

    private final List<ManagedFile> managedFiles;

    private final Session session;

    private final TransportProvider transportProvider;

    private final ComposedMailMessage composedMail;

    private final IAttachmentHandler attachmentHandler;

    private boolean headers;

    private int alternativeLevel;

    private int level;

    private TextBodyMailPart textBodyPart;

    /**
     * Initializes a new {@link MIMEStructure2ComposedMailParser}.
     *
     * @param accountId The account ID
     * @param session The session
     * @param protocol The server's protocol
     * @param hostName Thje server's host name
     * @throws OXException If initialization fails
     */
    public MIMEStructure2ComposedMailParser(final int accountId, final ServerSession session, final String protocol, final String hostName) throws OXException {
        super();
        this.session = session;
        managedFiles = new ArrayList<ManagedFile>(4);
        transportProvider = TransportProviderRegistry.getTransportProviderBySession(session, accountId);
        composedMail = transportProvider.getNewComposedMailMessage(session, session.getContext());
        attachmentHandler = new AbortAttachmentHandler(session);
    }

    /**
     * Gets the managed files created during parsing process.
     *
     * @return The managed files
     */
    public List<ManagedFile> getManagedFiles() {
        return managedFiles;
    }

    /**
     * Parses specified JSON message structure & returns the resulting {@link ComposedMailMessage} instances.
     *
     * @param jsonMessage The JSON message structure
     * @param warnings
     * @return The resulting {@link ComposedMailMessage} instances.
     * @throws OXException If parsing fails
     */
    public ComposedMailMessage[] parseMessage(final JSONObject jsonMessage, List<OXException> warnings) throws OXException {
        parseFlags(jsonMessage);
        parsePart(jsonMessage);
        /*
         * Fill composed mails
         */
        attachmentHandler.setTextPart(textBodyPart);
        return attachmentHandler.generateComposedMails(composedMail, warnings);
    }

    private void parseFlags(final JSONObject jsonMessage) throws OXException {
        try {
            /*
             * System flags
             */
            if (jsonMessage.hasAndNotNull("flags")) {
                final int flags = jsonMessage.getInt("flags");
                if (flags > 0) {
                    composedMail.setFlags(flags);
                }
            }
            /*
             * Color label
             */
            if (jsonMessage.hasAndNotNull("color_label")) {
                final int colorLabel = jsonMessage.getInt("color_label");
                if (colorLabel != MailMessage.COLOR_LABEL_NONE) {
                    composedMail.setColorLabel(colorLabel);
                }
            }
            /*
             * User flags
             */
            if (jsonMessage.hasAndNotNull("user")) {
                final JSONArray jsonUser = jsonMessage.getJSONArray("user");
                final int length = jsonUser.length();
                if (length > 0) {
                    for (int i = length - 1; i >= 0; i--) {
                        composedMail.addUserFlag(jsonUser.getString(i));
                    }
                }
            }
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void parsePart(final JSONObject jsonPart) throws OXException {
        try {
            /*
             * Parse headers
             */
            final ContentType contentType = new ContentType(MimeTypes.MIME_DEFAULT);
            final Map<String, String> m;
            if (headers) {
                final JSONObject jsonHeaders = jsonPart.getJSONObject("headers");
                parseContentType(jsonHeaders, contentType);
                final String contentId = parseContentID(jsonHeaders);
                if (null == contentId) {
                    m = Collections.<String, String> emptyMap();
                } else {
                    m = new HashMap<String, String>(1);
                    m.put("Content-ID", contentId);
                }
            } else {
                parseHeaders(jsonPart.getJSONObject("headers"), composedMail, contentType);
                m = Collections.<String, String> emptyMap();
                headers = true;
            }
            /*
             * Check Content-Type
             */
            if (contentType.startsWith("text/")) {
                /*
                 * A text/* part
                 */
                parseSimpleBodyText(jsonPart.getJSONObject("body"), contentType, m);
            } else if (contentType.startsWith("multipart/")) {
                /*
                 * A multipart
                 */
                parseMultipartBody(jsonPart.getJSONArray("body"), contentType.getSubType());
            } else if (contentType.startsWith("message/rfc822") || (contentType.getNameParameter() != null && contentType.getNameParameter().endsWith(".eml"))) {
                /*
                 * A nested message
                 */
                parseMessageBody(jsonPart.getJSONObject("body"));
            } else {
                parseSimpleBodyBinary(jsonPart.getJSONObject("body"), contentType, m);
            }
            /*
             * Check content type
             */
            if (null == textBodyPart) {
                textBodyPart = transportProvider.getNewTextBodyPart("");
                composedMail.setContentType("text/plain; charset=UTF-8");
            } else {
                if (textBodyPart.getHTML() == null) {
                    composedMail.setContentType("text/html; charset=UTF-8");
                } else { // has HTML
                    if (textBodyPart.getPlainText() == null) {
                        composedMail.setContentType("text/html; charset=UTF-8");
                    } else { // has plain-text
                        composedMail.setContentType("multipart/alternative");
                    }
                }
            }
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void parseMessageBody(final JSONObject jsonMessage) throws OXException {
        final MailMessage mail = MIMEStructureParser.parseStructure(jsonMessage);
        if (mail.getSize() < 0) {
            final ByteArrayOutputStream tmp = new UnsynchronizedByteArrayOutputStream(8192);
            mail.writeTo(tmp);
            mail.setSize(tmp.toByteArray().length);
        }
        attachmentHandler.addAttachment(transportProvider.getNewReferencedMail(mail, session));
    }

    private void parseMultipartBody(final JSONArray jsonMultiparts, final String subtype) throws OXException {
        try {
            final int length = jsonMultiparts.length();
            level++;
            if (alternativeLevel == 0 && "alternative".equalsIgnoreCase(subtype) && length >= 2) {
                alternativeLevel = level;
                composedMail.setContentType("multipart/alternative");
            }
            for (int i = 0; i < length; i++) {
                parsePart(jsonMultiparts.getJSONObject(i));
            }
            if (alternativeLevel == level) {
                alternativeLevel = 0;
            }
            level--;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void parseSimpleBodyText(final JSONObject jsonBody, final ContentType contentType, final Map<String, String> headers) throws OXException {
        try {
            if (isText(contentType.getBaseType())) {
                if (contentType.startsWith("text/plain")) {
                    if (null == textBodyPart) {
                        textBodyPart = transportProvider.getNewTextBodyPart(null);
                        textBodyPart.setPlainText(jsonBody.getString("data"));
                    } else if (alternativeLevel > 0 && textBodyPart.getPlainText() == null) {
                        textBodyPart.setPlainText(jsonBody.getString("data"));
                    } else {
                        /*
                         * Treat as an attachment
                         */
                        addAsAttachment(Charsets.toAsciiBytes(jsonBody.getString("data")), contentType, headers);
                    }
                } else {
                    if (null == textBodyPart) {
                        textBodyPart = transportProvider.getNewTextBodyPart(jsonBody.getString("data"));
                    } else if (alternativeLevel > 0 && textBodyPart.getHTML() == null) {
                        textBodyPart.setText(jsonBody.getString("data"));
                    } else {
                        /*
                         * Treat as an attachment
                         */
                        addAsAttachment(Charsets.toAsciiBytes(jsonBody.getString("data")), contentType, headers);
                    }
                }
            } else {
                addAsAttachment(Charsets.toAsciiBytes(jsonBody.getString("data")), contentType, headers);
            }
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void parseSimpleBodyBinary(final JSONObject jsonBody, final ContentType contentType, final Map<String, String> headers) throws OXException {
        try {
            addAsAttachment(Base64.decodeBase64(Charsets.toAsciiBytes(jsonBody.getString("data"))), contentType, headers);
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static final int IN_MEMORY_LIMIT = 1048576; // 1 MB

    private void addAsAttachment(final byte[] rawBytes, final ContentType contentType, final Map<String, String> headers) throws OXException {
        if (rawBytes.length <= IN_MEMORY_LIMIT) {
            addInMemory(rawBytes, contentType, headers);
        } else {
            final ManagedFileManagement mfm = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
            if (null == mfm) {
                addInMemory(rawBytes, contentType, headers);
            } else {
                try {
                    final ManagedFile managedFile = mfm.createManagedFile(rawBytes);
                    managedFiles.add(managedFile);
                    final UploadFileImpl uf = new UploadFileImpl();
                    uf.setContentType(managedFile.getContentType());
                    uf.setFileName(managedFile.getFileName());
                    uf.setSize(managedFile.getSize());
                    uf.setTmpFile(managedFile.getFile());
                    final UploadFileMailPart mailPart = transportProvider.getNewFilePart(uf);
                    if (!headers.isEmpty()) {
                        for (final Entry<String, String> entry : headers.entrySet()) {
                            mailPart.setHeader(entry.getKey(), entry.getValue());
                        }
                    }
                    attachmentHandler.addAttachment(mailPart);
                } catch (final OXException e) {
                    org.slf4j.LoggerFactory.getLogger(MIMEStructure2ComposedMailParser.class).warn(
                        "Creating managed file failed. Using in-memory version instead.",
                        e);
                    addInMemory(rawBytes, contentType, headers);
                }
            }
        }
    }

    private void addInMemory(final byte[] rawBytes, final ContentType contentType, final Map<String, String> headers) throws OXException {
        try {
            final MimeBodyPart mimePart = new MimeBodyPart();
            mimePart.setDataHandler(new DataHandler(new MessageDataSource(rawBytes, contentType.getBaseType())));
            mimePart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, contentType.toString());
            mimePart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, Part.ATTACHMENT);
            mimePart.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "base64");
            final MailPart mailPart = MimeMessageConverter.convertPart(mimePart, false);
            if (!headers.isEmpty()) {
                for (final Entry<String, String> entry : headers.entrySet()) {
                    mailPart.setHeader(entry.getKey(), entry.getValue());
                }
            }
            attachmentHandler.addAttachment(mailPart);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
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

    private static void parseHeaders(final JSONObject jsonHeaders, final MailMessage composedMail, final ContentType contentType) throws OXException {
        try {
            final StringBuilder headerNameBuilder = new StringBuilder(16);
            for (final Entry<String, Object> entry : jsonHeaders.entrySet()) {
                final String name = entry.getKey().toLowerCase(Locale.ENGLISH);
                if (HEADERS_ADDRESS.contains(name)) {
                    final JSONArray jsonAddresses = (JSONArray) entry.getValue();
                    final int length = jsonAddresses.length();
                    final List<InternetAddress> list = new ArrayList<InternetAddress>(length);
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
                        list.add(qia);
                    }
                    if ("from".equals(name)) {
                        composedMail.addFrom(list.toArray(new InternetAddress[list.size()]));
                    } else if ("to".equals(name)) {
                        composedMail.addTo(list.toArray(new InternetAddress[list.size()]));
                    } else if ("cc".equals(name)) {
                        composedMail.addCc(list.toArray(new InternetAddress[list.size()]));
                    } else if ("bcc".equals(name)) {
                        composedMail.addBcc(list.toArray(new InternetAddress[list.size()]));
                    } else if ("disposition-notification-to".equals(name)) {
                        composedMail.setDispositionNotification(list.get(0));
                    } else {
                        final StringBuilder builder = new StringBuilder(list.size() * 16);
                        final String delim = ", ";
                        for (final InternetAddress addr : list) {
                            builder.insert(0, addr.toString()).insert(0, delim);
                        }
                        composedMail.setHeader(toHeaderCase(name, headerNameBuilder), builder.delete(0, delim.length()).toString());
                    }
                } else if (HEADERS_DATE.contains(name)) {
                    final JSONObject jsonDate = (JSONObject) entry.getValue();
                    composedMail.setHeader(toHeaderCase(name, headerNameBuilder), jsonDate.getString("date"));
                } else if ("content-type".equals(name)) {
                    final JSONObject jsonContentType = (JSONObject) entry.getValue();
                    contentType.reset();
                    contentType.setBaseType(jsonContentType.getString("type"));
                    parseParameterList(jsonContentType.getJSONObject("params"), contentType);
                    composedMail.setHeader(toHeaderCase(name, headerNameBuilder), contentType.toString(true));
                } else if ("content-disposition".equals(name)) {
                    final JSONObject jsonContentDisposition = (JSONObject) entry.getValue();
                    final ContentDisposition contentDisposition = new ContentDisposition();
                    contentDisposition.setDisposition(jsonContentDisposition.getString("type"));
                    parseParameterList(jsonContentDisposition.getJSONObject("params"), contentDisposition);
                    composedMail.setHeader(toHeaderCase(name, headerNameBuilder), contentDisposition.toString(true));
                } else {
                    final Object value = entry.getValue();
                    if (value instanceof JSONArray) {
                        final JSONArray jsonHeader = (JSONArray) value;
                        final int length = jsonHeader.length();
                        final String headerName = toHeaderCase(name, headerNameBuilder);
                        for (int i = length - 1; i >= 0; i--) {
                            composedMail.addHeader(headerName, jsonHeader.getString(i));
                        }
                    } else {
                        composedMail.setHeader(toHeaderCase(name, headerNameBuilder), (String) value);
                    }
                }
            }
        } catch (final UnsupportedEncodingException e) {
            /*
             * Cannot occur
             */
            throw MailExceptionCode.ENCODING_ERROR.create(e, e.getMessage());
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static void parseContentType(final JSONObject jsonHeaders, final ContentType contentType) throws OXException {
        try {
            if (jsonHeaders.hasAndNotNull("content-type")) {
                final JSONObject jsonContentType = jsonHeaders.getJSONObject("content-type");
                contentType.reset();
                contentType.setBaseType(jsonContentType.getString("type"));
                parseParameterList(jsonContentType.getJSONObject("params"), contentType);
            }
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static String parseContentID(final JSONObject jsonHeaders) throws OXException {
        try {
            if (jsonHeaders.hasAndNotNull("content-id")) {
                return jsonHeaders.getString("content-id");
            }
            return null;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
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

    private static String toHeaderCase(final String name, final StringBuilder builder) {
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
