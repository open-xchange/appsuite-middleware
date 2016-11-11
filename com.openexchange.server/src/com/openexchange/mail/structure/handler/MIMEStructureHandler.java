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

package com.openexchange.mail.structure.handler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.idn.IDNA;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.google.common.collect.ImmutableSet;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.exception.OXException;
import com.openexchange.java.CharsetDetector;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.HeaderName;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.ParameterizedHeader;
import com.openexchange.mail.mime.PlainTextAddress;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.structure.StructureHandler;
import com.openexchange.mail.structure.StructureJSONBinary;
import com.openexchange.mail.structure.StructureMailMessageParser;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.mail.uuencode.UUEncodedPart;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.regex.MatcherReplacer;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link MIMEStructureHandler} - The handler to generate a JSON object reflecting a message's MIME structure.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MIMEStructureHandler implements StructureHandler {

    /**
     * The logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MIMEStructureHandler.class);

    private static final MailDateFormat MAIL_DATE_FORMAT;

    static {
        MAIL_DATE_FORMAT = new MailDateFormat();
        MAIL_DATE_FORMAT.setTimeZone(TimeZoneUtils.getTimeZone("GMT"));
    }

    /*-
     * #####################################################################################
     */

    private static final String KEY_ID = MailListField.ID.getKey();

    private static final String KEY_HEADERS = MailJSONField.HEADERS.getKey();

    private static final String BODY = "body";

    private static final String DATA = "data";

    private static final String CONTENT_TRANSFER_ENCODING = "content-transfer-encoding";

    private static final String CONTENT_DISPOSITION = "content-disposition";

    private static final String CONTENT_TYPE = "content-type";

    private final LinkedList<JSONObject> mailJsonObjectQueue;

    private JSONObject currentMailObject;

    private JSONValue currentBodyObject;

    private final long maxSize;

    // private JSONValue bodyJsonObject;

    private JSONArray userFlags;

    private int multipartCount;

    private boolean forceJSONArray4Multipart;

    /**
     * Initializes a new {@link MIMEStructureHandler}.
     *
     * @param maxSize The max. size of a mail part to let its content being inserted as base64 encoded or UTF-8 string.
     */
    public MIMEStructureHandler(final long maxSize) {
        super();
        mailJsonObjectQueue = new LinkedList<JSONObject>();
        mailJsonObjectQueue.addLast((currentMailObject = new JSONObject()));
        this.maxSize = maxSize;
        forceJSONArray4Multipart = true;
    }

    /**
     * Sets whether a JSON array is enforced for a <code>multipart</code> even if it only consists of one part.
     *
     * @param forceJSONArray4Multipart <code>true</code> to enforce a JSON array; otherwise <code>false</code>
     * @return This handler with new behavior applied
     */
    public MIMEStructureHandler setForceJSONArray4Multipart(final boolean forceJSONArray4Multipart) {
        this.forceJSONArray4Multipart = forceJSONArray4Multipart;
        return this;
    }

    private static final int MB = 1048576;

    @Override
    public boolean handleEnd(final MailMessage mail) throws OXException {
        final JSONObject mailJsonObject = mailJsonObjectQueue.getFirst();
        /*
         * Message identifier and folder full name
         */
        final String mailId = mail.getMailId();
        if (null != mailId) {
            try {
                mailJsonObject.put(KEY_ID, mailId);
            } catch (final JSONException e) {
                throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
            }
        }
        final String folder = mail.getFolder();
        if (null != folder) {
            try {
                mailJsonObject.put(MailJSONField.FOLDER.getKey(), MailFolderUtility.prepareFullname(mail.getAccountId(), folder));
            } catch (final JSONException e) {
                throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
            }
        }
        /*
         * Probe for "headers" existence
         */
        final JSONObject headersJsonObject = mailJsonObject.optJSONObject(KEY_HEADERS);
        if (null == headersJsonObject) {
            return true;
        }
        /*
         * Write headers to byte array
         */
        final byte[] bytes;
        {
            final ByteArrayOutputStream buf = new UnsynchronizedByteArrayOutputStream(2048);
            MimeMessageUtility.writeHeaders(mail, buf);
            bytes = buf.toByteArray();
        }
        final int length = bytes.length;
        if (length <= 0 || length >= MB) {
            // No headers or far too many bytes
            return true;
        }
        /*
         * Insert literal base64-encoded headers
         */
        try {
            headersJsonObject.put("x-original-headers", new String(Base64.encodeBase64(bytes))); // ASCII-only, no charset needed
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
        return true;
    }

    /**
     * Gets the JSON representation of mail's MIME structure.
     *
     * @return The JSON representation of mail's MIME structure
     */
    public JSONObject getJSONMailObject() {
        return mailJsonObjectQueue.getFirst();
    }

    private JSONArray getUserFlags() throws JSONException {
        if (null == userFlags) {
            userFlags = new JSONArray();
            currentMailObject.put(MailJSONField.USER.getKey(), userFlags);
        }
        return userFlags;
    }

    private void add2BodyJsonObject(final JSONObject bodyObject) throws JSONException {
        if (null == currentBodyObject) {
            /*
             * Nothing applied before
             */
            if (forceJSONArray4Multipart && multipartCount > 0) {
                final JSONArray jsonArray = new JSONArray();
                jsonArray.put(bodyObject);
                currentBodyObject = jsonArray;
            } else {
                currentBodyObject = bodyObject;
            }
            currentMailObject.put(BODY, currentBodyObject);
        } else {
            /*
             * Already applied, turn to an array (if not done before) and append given JSON object
             */
            final JSONValue prev = currentBodyObject;
            final JSONArray jsonArray;
            if (prev.isArray()) {
                jsonArray = prev.toArray();
            } else {
                jsonArray = new JSONArray();
                jsonArray.put(prev);
                currentBodyObject = jsonArray;
                /*
                 * Replace
                 */
                currentMailObject.put(BODY, currentBodyObject);
            }
            jsonArray.put(bodyObject);
        }
    }

    private static final int BUFLEN = 8192;

    @Override
    public boolean handleAttachment(final MailPart part, final String id) throws OXException {
        final ContentType contentType = part.getContentType();
        if (isVCalendar(contentType.getBaseType()) && !contentType.containsParameter("method")) {
            /*
             * Check ICal part for a valid METHOD and its presence in Content-Type header
             */
            final ICalParser iCalParser = ServerServiceRegistry.getInstance().getService(ICalParser.class);
            if (iCalParser != null) {
                try {
                    final String method = iCalParser.parseProperty("METHOD", part.getInputStream());
                    if (null != method) {
                        /*
                         * Assume an iTIP response or request
                         */
                        contentType.setParameter("method", toUpperCase(method));
                        part.setContentType(contentType);
                    }
                } catch (final RuntimeException e) {
                    LOG.warn("A runtime error occurred.", e);
                }
            }
        }
        addBodyPart(part, id);
        return true;
    }

    private static final Set<String> REMAIN = ImmutableSet.of(KEY_HEADERS, MailJSONField.RECEIVED_DATE.getKey());

    @Override
    public boolean handleSMIMEBodyText(final MailPart part) throws OXException {
        try {
            final JSONObject bodyObject = new JSONObject();
            final JSONObject headerObject = new JSONObject();
            fillBodyPart(bodyObject, part, headerObject, null);
            final JSONObject jType = headerObject.optJSONObject(CONTENT_TYPE);
            if (null != jType) {
                bodyObject.put("type", jType.getString("type"));
            }
            /*
             * Add body object to parental structure object
             */
            final JSONObject jsonObject = currentMailObject;
            for (final String name : new HashSet<String>(jsonObject.keySet())) {
                if (!REMAIN.contains(name)) {
                    jsonObject.remove(name);
                }
            }
            jsonObject.put("smime_body_text", bodyObject);
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleSMIMEBodyData(final byte[] data) throws OXException {
        try {
            currentMailObject.put("smime_body_data", Charsets.toAsciiString(Base64.encodeBase64(data, false)));
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleColorLabel(final int colorLabel) throws OXException {
        try {
            /*-
             * TODO: Decide whether to add separate "color_label" field or add it to user flags:
             *
             * Uncomment this for adding to user flags:
             *
             *   getUserFlags().put(MailMessage.getColorLabelStringValue(colorLabel));
             */
            currentMailObject.put(MailJSONField.COLOR_LABEL.getKey(), colorLabel);
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleHeaders(final Iterator<Entry<String, String>> iter) throws OXException {
        generateHeadersObject(iter, currentMailObject);
        return true;
    }

    @Override
    public boolean handleInlineUUEncodedAttachment(final UUEncodedPart part, final String id) throws OXException {
        final String filename = part.getFileName();
        String contentType = MimeTypes.MIME_APPL_OCTET;
        try {
            contentType = MimeType2ExtMap.getContentType(new File(filename.toLowerCase()).getName()).toLowerCase();
        } catch (final Exception e) {
            final Throwable t =
                new Throwable(new StringBuilder("Unable to fetch content-type for '").append(filename).append("': ").append(e).toString());
            LOG.warn("", t);
        }
        /*
         * Dummy headers
         */
        final StringBuilder sb = new StringBuilder(64);
        final Map<String, String> headers = new HashMap<String, String>(4);
        final String encodeFN;
        try {
            encodeFN = MimeUtility.encodeText(filename, "UTF-8", "Q");
        } catch (final UnsupportedEncodingException e) {
            throw MailExceptionCode.ENCODING_ERROR.create(e, e.getMessage());
        }
        headers.put(CONTENT_TYPE, sb.append(contentType).append("; name=").append(encodeFN).toString());
        headers.put(CONTENT_DISPOSITION, new StringBuilder(Part.ATTACHMENT).append("; filename=").append(encodeFN).toString());
        headers.put(CONTENT_TRANSFER_ENCODING, "base64");
        /*
         * Add body part
         */
        addBodyPart(part.getFileSize(), new InputStreamProvider() {

            @Override
            public InputStream getInputStream() throws IOException {
                return part.getInputStream();
            }
        }, new ContentType(contentType), filename, id, headers.entrySet().iterator());
        return true;
    }

    @Override
    public boolean handleInlineUUEncodedPlainText(final String decodedTextContent, final ContentType contentType, final int size, final String fileName, final String id) throws OXException {
        /*
         * Dummy headers
         */
        final Map<String, String> headers = new HashMap<String, String>(4);
        headers.put(CONTENT_TYPE, "text/plain; charset=UTF-8");
        headers.put(CONTENT_DISPOSITION, Part.INLINE);
        /*
         * Add body part
         */
        addBodyPart(size, new InputStreamProvider() {

            @Override
            public InputStream getInputStream() throws IOException {
                return new UnsynchronizedByteArrayInputStream(decodedTextContent.getBytes(com.openexchange.java.Charsets.UTF_8));
            }
        }, contentType, fileName, id, headers.entrySet().iterator());
        return true;
    }

    @Override
    public boolean handleMultipartStart(final ContentType contentType, final int bodyPartCount, final String id) throws OXException {
        try {
            // Increment
            if (++multipartCount > 1) { // Enqueue nested multipart
                // Create a new mail object
                final JSONObject newMailObject = new JSONObject();
                // Apply new mail object to current mail object's body element
                add2BodyJsonObject(newMailObject);
                // Assign new mail object to current mail object
                currentMailObject = newMailObject;
                mailJsonObjectQueue.addLast(currentMailObject);
                currentBodyObject = null;
                // Add multipart's headers
                final Map<String, String> headers = new HashMap<String, String>(1);
                headers.put(CONTENT_TYPE, contentType.toString());
                generateHeadersObject(headers.entrySet().iterator(), currentMailObject);
            } else {
                /*
                 * Ensure proper content type
                 */
                final JSONObject headers = currentMailObject.optJSONObject(KEY_HEADERS);
                if (null == headers) {
                    // Add multipart's headers
                    final Map<String, String> headersMap = new HashMap<String, String>(1);
                    headersMap.put(CONTENT_TYPE, contentType.toString());
                    generateHeadersObject(headersMap.entrySet().iterator(), currentMailObject);
                } else {
                    // Set content type in existing headers
                    headers.put(CONTENT_TYPE, generateParameterizedHeader( contentType, com.openexchange.java.Strings.toLowerCase(contentType.getBaseType())));
                }
            }
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleMultipartEnd() throws OXException {
        // Decrement
        if (--multipartCount > 0) { // Dequeue nested multipart
            // Dequeue
            mailJsonObjectQueue.removeLast();
            currentMailObject = mailJsonObjectQueue.getLast();
            currentBodyObject = (JSONValue) currentMailObject.opt(BODY);
        }
        return true;
    }

    @Override
    public boolean handleNestedMessage(final MailPart mailPart, final String id) throws OXException {
        try {
            final Object content = mailPart.getContent();
            final MailMessage nestedMail;
            if (content instanceof MailMessage) {
                nestedMail = (MailMessage) content;
            } else if (content instanceof InputStream) {
                try {
                    nestedMail =
                        MimeMessageConverter.convertMessage(new MimeMessage(MimeDefaultSession.getDefaultSession(), (InputStream) content));
                } catch (final MessagingException e) {
                    throw MimeMailException.handleMessagingException(e);
                }
            } else {
                final StringBuilder sb = new StringBuilder(128);
                sb.append("Ignoring nested message.").append(
                    "Cannot handle part's content which should be a RFC822 message according to its content type: ");
                sb.append((null == content ? "null" : content.getClass().getSimpleName()));
                LOG.error(sb.toString());
                return true;
            }
            /*
             * Inner parser
             */
            final MIMEStructureHandler inner = new MIMEStructureHandler(maxSize);
            new StructureMailMessageParser().setParseTNEFParts(true).parseMailMessage(nestedMail, inner, id);
            /*
             * Apply to this handler
             */
            final JSONObject bodyObject;
            if (multipartCount > 0) {
                bodyObject = new JSONObject();
                /*
                 * Put headers
                 */
                generateHeadersObject(mailPart.getHeadersIterator(), bodyObject);
                /*
                 * Put body
                 */
                final JSONObject jsonMailObject = inner.getJSONMailObject();
                jsonMailObject.put(KEY_ID, mailPart.containsSequenceId() ? mailPart.getSequenceId() : id);
                bodyObject.put(BODY, jsonMailObject);
            } else {
                bodyObject = inner.getJSONMailObject();
            }
            /*
             * Add
             */
            add2BodyJsonObject(bodyObject);
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleReceivedDate(final Date receivedDate) throws OXException {
        try {
            if (receivedDate == null) {
                currentMailObject.put(MailJSONField.RECEIVED_DATE.getKey(), JSONObject.NULL);
            } else {
                final JSONObject dateObject = new JSONObject();
                dateObject.put("utc", receivedDate.getTime());
                synchronized (MAIL_DATE_FORMAT) {
                    dateObject.put("date", MAIL_DATE_FORMAT.format(receivedDate));
                }
                currentMailObject.put(MailJSONField.RECEIVED_DATE.getKey(), dateObject);
            }
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleSystemFlags(final int flags) throws OXException {
        try {
            final String key = MailJSONField.FLAGS.getKey();
            if (currentMailObject.hasAndNotNull(key)) {
                final int prevFlags = currentMailObject.getInt(key);
                currentMailObject.put(key, prevFlags | flags);
            } else {
                currentMailObject.put(key, flags);
            }
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean handleUserFlags(final String[] userFlags) throws OXException {
        if (null == userFlags || 0 == userFlags.length) {
            return true;
        }
        try {
            final JSONArray userFlagsArr = getUserFlags();
            for (final String userFlag : userFlags) {
                userFlagsArr.put(userFlag);
            }
            return true;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void addBodyPart(final MailPart part, final String id) throws OXException {
        try {
            final JSONObject bodyObject = new JSONObject();
            if (multipartCount > 0) {
                // Put headers
                final JSONObject headersObject = generateHeadersObject(part.getHeadersIterator(), bodyObject);
                // Put body
                final JSONObject body = new JSONObject();
                fillBodyPart(body, part, headersObject, id);
                bodyObject.put(BODY, body);
            } else {
                // Put direct
                final JSONObject headersJSONObject = currentMailObject.optJSONObject(KEY_HEADERS);
                fillBodyPart(bodyObject, part, null == headersJSONObject ? new JSONObject() : headersJSONObject, id);
            }
            add2BodyJsonObject(bodyObject);
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void addBodyPart(final long size, final InputStreamProvider isp, final ContentType contentType, final String filename, final String id, final Iterator<Entry<String, String>> iter) throws OXException {
        try {
            final JSONObject bodyObject = new JSONObject();
            if (multipartCount > 0) {
                // Put headers
                final JSONObject headersObject = generateHeadersObject(iter, bodyObject);
                // Put body
                final JSONObject body = new JSONObject();
                fillBodyPart(body, size, isp, contentType, filename, headersObject, id);
                bodyObject.put(BODY, body);
            } else {
                // Put direct
                final JSONObject headersJSONObject = currentMailObject.optJSONObject(KEY_HEADERS);
                fillBodyPart(bodyObject, size, isp, contentType, filename, null == headersJSONObject ? new JSONObject() : headersJSONObject, id);
            }
            add2BodyJsonObject(bodyObject);
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static final String PRIMARY_TEXT = "text/";

    private static final String TEXT_HTML = "text/htm";

    private static final String APPL_OCTET = MimeTypes.MIME_APPL_OCTET;

    private static final Pattern PAT_META_CT = Pattern.compile("<meta[^>]*?http-equiv=\"?content-type\"?[^>]*?>", Pattern.CASE_INSENSITIVE);

    private void fillBodyPart(final JSONObject bodyObject, final MailPart part, final JSONObject headerObject, final String id) throws OXException {
        try {
            if (null != id) {
                bodyObject.put(KEY_ID, id);
            }
            final long size = part.getSize();
            if (maxSize > 0 && size > maxSize) {
                bodyObject.put(DATA, JSONObject.NULL);
                bodyObject.put("exact_size", Streams.countInputStream(part.getInputStream()));
            } else {
                final ContentType contentType = part.getContentType();
                final TextResult tr = isText(contentType, part.getFileName());
                if (tr.text) {
                    // Check for special "text/comma-separated-values" Content-Type
                    if (contentType.startsWith("text/comma-separated-values")) {
                        fillBase64JSONString(part.getInputStream(), bodyObject, true);
                        // Set Transfer-Encoding to base64
                        headerObject.put(CONTENT_TRANSFER_ENCODING, "base64");
                        contentType.setPrimaryType("application").setSubType("vnd.ms-excel");
                        headerObject.put(CONTENT_TYPE, generateParameterizedHeader(contentType, com.openexchange.java.Strings.toLowerCase(contentType.getBaseType())));
                    } else {
                        // Set UTF-8 text
                        if (contentType.startsWith(TEXT_HTML)) {
                            final String html = readContent(part, contentType);
                            final Matcher m = PAT_META_CT.matcher(html);
                            final MatcherReplacer mr = new MatcherReplacer(m, html);
                            final StringBuilder replaceBuffer = new StringBuilder(html.length());
                            if (m.find()) {
                                replaceBuffer.append("<meta http-equiv=\"Content-Type\" content=\"").append(com.openexchange.java.Strings.toLowerCase(contentType.getBaseType()));
                                replaceBuffer.append("; charset=UTF-8\" />");
                                final String replacement = replaceBuffer.toString();
                                replaceBuffer.setLength(0);
                                mr.appendLiteralReplacement(replaceBuffer, replacement);
                            }
                            mr.appendTail(replaceBuffer);
                            bodyObject.put(DATA, replaceBuffer.toString());
                        } else {
                            bodyObject.put(DATA, readContent(part, contentType));
                        }
                        // Set header according to UTF-8 content without transfer-encoding
                        headerObject.remove(CONTENT_TRANSFER_ENCODING);
                        contentType.setCharsetParameter("UTF-8");
                        headerObject.put(CONTENT_TYPE, generateParameterizedHeader(contentType, com.openexchange.java.Strings.toLowerCase(contentType.getBaseType())));
                    }
                } else {
                    fillBase64JSONString(part.getInputStream(), bodyObject, true);
                    // Set Transfer-Encoding to base64
                    headerObject.put(CONTENT_TRANSFER_ENCODING, "base64");
                    // Reset Content-Type if applicable
                    final String typeByFileExtension = tr.typeByFileExtension;
                    if (null != typeByFileExtension) {
                        contentType.setBaseType(typeByFileExtension);
                        headerObject.put(CONTENT_TYPE, generateParameterizedHeader(contentType, com.openexchange.java.Strings.toLowerCase(contentType.getBaseType())));
                    }
                }
            }
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    private void fillBodyPart(final JSONObject bodyObject, final long size, final InputStreamProvider isp, final ContentType contentType, final String filename, final JSONObject headerObject, final String id) throws OXException {
        try {
            bodyObject.put(KEY_ID, id);
            if (maxSize > 0 && size > maxSize) {
                bodyObject.put(DATA, JSONObject.NULL);
                bodyObject.put("exact_size", Streams.countInputStream(isp.getInputStream()));
            } else {
                final TextResult tr = isText(contentType, filename);
                if (tr.text) {
                    // Check for special "text/comma-separated-values" Content-Type
                    if (contentType.startsWith("text/comma-separated-values")) {
                        fillBase64JSONString(isp.getInputStream(), bodyObject, true);
                        // Set Transfer-Encoding to base64
                        headerObject.put(CONTENT_TRANSFER_ENCODING, "base64");
                        contentType.setPrimaryType("application").setSubType("vnd.ms-excel");
                        headerObject.put(CONTENT_TYPE, generateParameterizedHeader(contentType, com.openexchange.java.Strings.toLowerCase(contentType.getBaseType())));
                    } else { // Regular text part
                        // Set UTF-8 text
                        bodyObject.put(DATA, readContent(isp, contentType));
                        // Set header according to utf-8 content without transfer-encoding
                        headerObject.remove(CONTENT_TRANSFER_ENCODING);
                        contentType.setCharsetParameter("UTF-8");
                        headerObject.put(CONTENT_TYPE, generateParameterizedHeader(contentType, com.openexchange.java.Strings.toLowerCase(contentType.getBaseType())));
                    }
                } else {
                    fillBase64JSONString(isp.getInputStream(), bodyObject, true);
                    // Set Transfer-Encoding to base64
                    headerObject.put(CONTENT_TRANSFER_ENCODING, "base64");
                    // Reset Content-Type if applicable
                    final String typeByFileExtension = tr.typeByFileExtension;
                    if (null != typeByFileExtension) {
                        contentType.setBaseType(typeByFileExtension);
                        headerObject.put(CONTENT_TYPE, generateParameterizedHeader(contentType, com.openexchange.java.Strings.toLowerCase(contentType.getBaseType())));
                    }
                }
            }
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    private static void fillBase64JSONString(final InputStream inputStream, final JSONObject bodyObject, final boolean streaming) throws OXException {
        try {
            if (streaming) {
                bodyObject.put(DATA, new StructureJSONBinary(inputStream));
            } else {
                final byte[] bytes;
                {
                    try {
                        final byte[] buf = new byte[BUFLEN];
                        final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(BUFLEN << 2);
                        int read;
                        while ((read = inputStream.read(buf, 0, BUFLEN)) > 0) {
                            out.write(buf, 0, read);
                        }
                        bytes = out.toByteArray();
                    } catch (final IOException e) {
                        if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                            throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
                        }
                        throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
                    } finally {
                        Streams.close(inputStream);
                    }
                }
                // Add own JSONString implementation to support streaming
                bodyObject.put(DATA, Charsets.toAsciiString(Base64.encodeBase64(bytes, false)));
            }
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static TextResult isText(final ContentType contentType, final String fileName) {
        if (null == contentType) {
            return new TextResult(false);
        }
        if (null == fileName) {
            return new TextResult(contentType.startsWith(PRIMARY_TEXT));
        }
        if (!contentType.startsWith(PRIMARY_TEXT)) {
            // No text/* at all
            return new TextResult(false);
        }
        final String ctbf = Strings.toLowerCase(MimeType2ExtMap.getContentType(fileName));
        if (ctbf.startsWith(APPL_OCTET)) {
            // Unknown
            return new TextResult(contentType.startsWith(PRIMARY_TEXT));
        }
        // Check file extension also implies text/*
        return new TextResult(ctbf.startsWith(PRIMARY_TEXT), ctbf);
    }

    private static final HeaderName HN_CONTENT_TYPE = HeaderName.valueOf(CONTENT_TYPE);

    private static final HeaderName HN_DATE = HeaderName.valueOf("date");

    private static final HeaderName HN_SUBJECT = HeaderName.valueOf("subject");

    private static final Set<HeaderName> PARAMETERIZED_HEADERS = ImmutableSet.of(HN_CONTENT_TYPE, HeaderName.valueOf(CONTENT_DISPOSITION));

    private static final Set<HeaderName> ADDRESS_HEADERS = ImmutableSet.of(
            HeaderName.valueOf("From"),
            HeaderName.valueOf("To"),
            HeaderName.valueOf("Cc"),
            HeaderName.valueOf("Bcc"),
            HeaderName.valueOf("Reply-To"),
            HeaderName.valueOf("Sender"),
            HeaderName.valueOf("Errors-To"),
            HeaderName.valueOf("Resent-Bcc"),
            HeaderName.valueOf("Resent-Cc"),
            HeaderName.valueOf("Resent-From"),
            HeaderName.valueOf("Resent-To"),
            HeaderName.valueOf("Resent-Sender"),
            HeaderName.valueOf("Disposition-Notification-To"));

    private static final Pattern P_DOUBLE_BACKSLASH = Pattern.compile(Pattern.quote("\\\\\\\""));

    private JSONObject generateHeadersObject(final Iterator<Entry<String, String>> iter, final JSONObject parent) throws OXException {
        try {
            final JSONObject hdrObject = new JSONObject();
            while (iter.hasNext()) {
                final Entry<String, String> entry = iter.next();
                final String name = com.openexchange.java.Strings.toLowerCase(entry.getKey());
                final HeaderName headerName = HeaderName.valueOf(name);
                if (ADDRESS_HEADERS.contains(headerName)) {
                    final InternetAddress[] internetAddresses = getAddressHeader(entry.getValue());
                    final JSONArray ja;
                    if (hdrObject.has(name)) {
                        ja = hdrObject.getJSONArray(name);
                    } else {
                        ja = new JSONArray();
                        hdrObject.put(name, ja);
                    }
                    for (final InternetAddress internetAddress : internetAddresses) {
                        final String address = IDNA.toIDN(internetAddress.getAddress());
                        if (!com.openexchange.java.Strings.isEmpty(address)) {
                            final JSONObject addressJsonObject = new JSONObject();
                            final String personal = internetAddress.getPersonal();
                            if (null != personal) {
                                addressJsonObject.put("personal", P_DOUBLE_BACKSLASH.matcher(personal).replaceAll("\\\""));
                            }
                            addressJsonObject.put("address", address);
                            ja.put(addressJsonObject);
                        }
                    }
                } else if (PARAMETERIZED_HEADERS.contains(headerName)) {
                    final JSONObject parameterJsonObject = generateParameterizedHeader(entry.getValue(), headerName);
                    if (hdrObject.has(name)) {
                        final Object previous = hdrObject.get(name);
                        final JSONArray ja;
                        if (previous instanceof JSONArray) {
                            ja = (JSONArray) previous;
                        } else {
                            ja = new JSONArray();
                            hdrObject.put(name, ja);
                            ja.put(previous);
                        }
                        ja.put(parameterJsonObject);
                    } else {
                        hdrObject.put(name, parameterJsonObject);
                    }
                } else if (HN_DATE.equals(headerName)) {
                    hdrObject.put(name, generateDateObject(entry.getValue()));
                } else if (HN_SUBJECT.equals(headerName)) {
                    hdrObject.put(name, MimeMessageUtility.decodeEnvelopeSubject(entry.getValue()));
                } else {
                    if (hdrObject.has(name)) {
                        final Object previous = hdrObject.get(name);
                        final JSONArray ja;
                        if (previous instanceof JSONArray) {
                            ja = (JSONArray) previous;
                        } else {
                            ja = new JSONArray();
                            hdrObject.put(name, ja);
                            ja.put(previous);
                        }
                        ja.put(MimeMessageUtility.decodeMultiEncodedHeader(entry.getValue()));
                    } else {
                        hdrObject.put(name, MimeMessageUtility.decodeMultiEncodedHeader(entry.getValue()));
                    }
                }
            }
            parent.put(KEY_HEADERS, hdrObject.length() > 0 ? hdrObject : JSONObject.NULL);
            return hdrObject;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private JSONObject generateParameterizedHeader(final String value, final HeaderName headerName) throws OXException, JSONException {
        if (HN_CONTENT_TYPE.equals(headerName)) {
            final ContentType ct = new ContentType(value);
            return generateParameterizedHeader(ct, com.openexchange.java.Strings.toLowerCase(ct.getBaseType()));
        }
        final ContentDisposition cd = new ContentDisposition(value);
        return generateParameterizedHeader(cd, com.openexchange.java.Strings.toLowerCase(cd.getDisposition()));
    }

    private JSONObject generateParameterizedHeader(final ParameterizedHeader parameterizedHeader, final String type) throws JSONException {
        final JSONObject parameterJsonObject = new JSONObject();
        parameterJsonObject.put("type", type);
        final JSONObject paramListJsonObject = new JSONObject();
        for (final Iterator<String> pi = parameterizedHeader.getParameterNames(); pi.hasNext();) {
            final String paramName = pi.next();
            if ("read-date".equalsIgnoreCase(paramName)) {
                paramListJsonObject.put(com.openexchange.java.Strings.toLowerCase(paramName), generateDateObject(parameterizedHeader.getParameter(paramName)));
            } else {
                paramListJsonObject.put(com.openexchange.java.Strings.toLowerCase(paramName), parameterizedHeader.getParameter(paramName));
            }
        }
        if (paramListJsonObject.length() > 0) {
            parameterJsonObject.put("params", paramListJsonObject);
        }
        return parameterJsonObject;
    }

    private Object generateDateObject(final String date) throws JSONException {
        if (null == date) {
            return JSONObject.NULL;
        }
        final JSONObject dateObject = new JSONObject();
        synchronized (MAIL_DATE_FORMAT) {
            try {
                final Date parsedDate = MAIL_DATE_FORMAT.parse(date);
                if (null != parsedDate) {
                    dateObject.put("utc", parsedDate.getTime());
                }
            } catch (final ParseException pex) {
                LOG.warn("Date string could not be parsed: {}", date);
            }
        }
        dateObject.put("date", date);
        return dateObject;
    }

    /**
     * Gets the address headers denoted by specified header name in a safe manner.
     * <p>
     * If strict parsing of address headers yields a {@link AddressException}, then a plain-text version is generated to display broken
     * address header as it is.
     *
     * @param name The address header name
     * @param message The message providing the address header
     * @return The parsed address headers as an array of {@link InternetAddress} instances
     */
    private static InternetAddress[] getAddressHeader(final String addresses) {
        if (null == addresses || 0 == addresses.length()) {
            return new InternetAddress[0];
        }
        try {
            return QuotedInternetAddress.parseHeader(addresses, true);
        } catch (final AddressException e) {
            return getAddressHeaderNonStrict(addresses);
        }
    }

    private static InternetAddress[] getAddressHeaderNonStrict(final String addressStrings) {
        try {
            final InternetAddress[] addresses = QuotedInternetAddress.parseHeader(addressStrings, false);
            final List<InternetAddress> addressList = new ArrayList<InternetAddress>(addresses.length);
            for (final InternetAddress internetAddress : addresses) {
                try {
                    addressList.add(new QuotedInternetAddress(internetAddress.toString()));
                } catch (final AddressException e) {
                    addressList.add(internetAddress);
                }
            }
            return addressList.toArray(new InternetAddress[addressList.size()]);
        } catch (final AddressException e) {
            LOG.debug("Internet addresses could not be properly parsed. Using plain addresses' string representation instead.", e);
            return getAddressesOnParseError(addressStrings);
        }
    }

    private static InternetAddress[] getAddressesOnParseError(final String addr) {
        return new InternetAddress[] { new PlainTextAddress(addr) };
    }

    private static String readContent(final MailPart mailPart, final ContentType contentType) throws OXException, IOException {
        final String charset = getCharset(mailPart, contentType);
        try {
            return MessageUtility.readMailPart(mailPart, charset);
        } catch (final java.io.CharConversionException e) {
            // Obviously charset was wrong or bogus implementation of character conversion
            final String fallback = "US-ASCII";
            LOG.warn("Character conversion exception while reading content with charset \"{}\". Using fallback charset \"{}\" instead.", charset, fallback, e);
            return MessageUtility.readMailPart(mailPart, fallback);
        }
    }

    private static String getCharset(final MailPart mailPart, final ContentType contentType) throws OXException {
        if (mailPart.containsHeader(MessageHeaders.HDR_CONTENT_TYPE)) {
            String cs = contentType.getCharsetParameter();
            if (null == cs) {
                // No charset parameter available
                // Auto-detect it or use default
                return contentType.startsWith(PRIMARY_TEXT) ? CharsetDetector.detectCharset(mailPart.getInputStream()) : MailProperties.getInstance().getDefaultMimeCharset();
            }

            // Check validity
            if (!CharsetDetector.isValid(cs)) {
                final String prev = cs;
                if (contentType.startsWith(PRIMARY_TEXT)) {
                    cs = CharsetDetector.detectCharset(mailPart.getInputStream());
                    LOG.warn("Illegal or unsupported encoding \"{}\". Using auto-detected encoding: \"{}\"", prev, cs);
                } else {
                    cs = MailProperties.getInstance().getDefaultMimeCharset();
                    LOG.warn("Illegal or unsupported encoding \"{}\". Using fallback encoding:: \"{}\"", prev, cs);
                }
            }
            return cs;
        }

        // No Content-Type available in mail part
        // Auto-detect it or use default
        return contentType.startsWith(PRIMARY_TEXT) ? CharsetDetector.detectCharset(mailPart.getInputStream()) : MailProperties.getInstance().getDefaultMimeCharset();
    }

    private static String readContent(final InputStreamProvider isp, final ContentType contentType) throws IOException {
        final String charset = getCharset(isp, contentType);
        try {
            return MessageUtility.readStream(isp.getInputStream(), charset);
        } catch (final java.io.CharConversionException e) {
            // Obviously charset was wrong or bogus implementation of character conversion
            final String fallback = "US-ASCII";
            LOG.warn("Character conversion exception while reading content with charset \"{}\". Using fallback charset \"{}\" instead.", charset, fallback, e);
            return MessageUtility.readStream(isp.getInputStream(), fallback);
        }
    }

    private static String getCharset(final InputStreamProvider isp, final ContentType contentType) throws IOException {
        final String charset;
        if (contentType.startsWith(PRIMARY_TEXT)) {
            final String cs = contentType.getCharsetParameter();
            if (!CharsetDetector.isValid(cs)) {
                charset = CharsetDetector.detectCharset(isp.getInputStream());
            } else {
                charset = cs;
            }
        } else {
            charset = MailProperties.getInstance().getDefaultMimeCharset();
        }
        return charset;
    }

    private static interface InputStreamProvider {

        InputStream getInputStream() throws IOException;
    }

    private static boolean isVCalendar(final String baseContentType) {
        return "text/calendar".equalsIgnoreCase(baseContentType) || "text/x-vcalendar".equalsIgnoreCase(baseContentType);
    }

    /** ASCII-wise to upper-case */
    private static String toUpperCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'a') && (c <= 'z') ? (char) (c & 0x5f) : c);
        }
        return builder.toString();
    }

    private static final class TextResult {
        final boolean text;
        final String typeByFileExtension;

        TextResult(final boolean text) {
            this(text, null);
        }

        TextResult(final boolean text, final String typeByFileExtension) {
            super();
            this.text = text;
            this.typeByFileExtension = typeByFileExtension;
        }
    } //  End of class TextResult

}
