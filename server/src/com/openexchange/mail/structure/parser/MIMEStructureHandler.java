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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.HeaderName;
import com.openexchange.mail.mime.MIMEDefaultSession;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.MIMEType2ExtMap;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.ParameterizedHeader;
import com.openexchange.mail.mime.PlainTextAddress;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.mime.utils.MIMEMessageUtility;
import com.openexchange.mail.structure.Base64JSONString;
import com.openexchange.mail.structure.StructureHandler;
import com.openexchange.mail.structure.StructureMailMessageParser;
import com.openexchange.mail.utils.CharsetDetector;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.mail.uuencode.UUEncodedPart;
import com.openexchange.tools.ByteBuffers;
import com.openexchange.tools.encoding.Charsets;
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
    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MIMEStructureHandler.class);

    /*-
     * #####################################################################################
     */

    private static final String BODY = "body";

    private static final String DATA = "data";

    private static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";

    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    private static final String CONTENT_TYPE = "Content-Type";

    private final LinkedList<JSONObject> mailJsonObjectQueue;

    private JSONObject currentMailObject;

    private JSONValue currentBodyObject;

    private int multipartCount;

    private final long maxSize;

    // private JSONValue bodyJsonObject;

    private JSONArray userFlags;

    private InlineDetector inlineDetector;

    private boolean prepare;

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
        inlineDetector = LENIENT_DETECTOR;
        // prepare = true;
    }

    /**
     * Switches the INLINE detector behavior.
     * 
     * @param strict <code>true</code> to perform strict INLINE detector behavior; otherwise <code>false</code>
     * @return This handler with new behavior applied
     */
    public MIMEStructureHandler setInlineDetectorBehavior(final boolean strict) {
        inlineDetector = strict ? STRICT_DETECTOR : LENIENT_DETECTOR;
        return this;
    }

    /**
     * Set the prepare flag.
     * 
     * @param prepare The prepare flag
     * @return This handler with new prepare flag applied
     */
    public MIMEStructureHandler setPrepare(final boolean prepare) {
        this.prepare = prepare;
        return this;
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
            currentBodyObject = bodyObject;
            currentMailObject.put(BODY, currentBodyObject);
        } else {
            /*
             * Already applied, turn to an array (if not done before) and append given JSON object
             */
            final JSONValue prev = currentBodyObject;
            final JSONArray jsonArray;
            if (prev.isArray()) {
                jsonArray = (JSONArray) prev;
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

    public boolean handleAttachment(final MailPart part, final String id) throws MailException {
        addBodyPart(part, id);
        return true;
    }

    public boolean handleColorLabel(final int colorLabel) throws MailException {
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
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    public boolean handleHeaders(final Iterator<Entry<String, String>> iter) throws MailException {
        generateHeadersObject(iter, currentMailObject);
        return true;
    }

    public boolean handleInlineUUEncodedAttachment(final UUEncodedPart part, final String id) throws MailException {
        final String filename = part.getFileName();
        String contentType = MIMETypes.MIME_APPL_OCTET;
        try {
            contentType = MIMEType2ExtMap.getContentType(new File(filename.toLowerCase()).getName()).toLowerCase();
        } catch (final Exception e) {
            final Throwable t =
                new Throwable(new StringBuilder("Unable to fetch content-type for '").append(filename).append("': ").append(e).toString());
            LOG.warn(t.getMessage(), t);
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
            throw new MailException(MailException.Code.ENCODING_ERROR, e, e.getMessage());
        }
        headers.put(CONTENT_TYPE, sb.append(contentType).append("; name=").append(encodeFN).toString());
        sb.setLength(0);
        headers.put(CONTENT_DISPOSITION, new StringBuilder(Part.ATTACHMENT).append("; filename=").append(encodeFN).toString());
        headers.put(CONTENT_TRANSFER_ENCODING, "base64");
        /*
         * Add body part
         */
        addBodyPart(part.getFileSize(), new InputStreamProvider() {

            public InputStream getInputStream() throws IOException {
                return part.getInputStream();
            }
        }, new ContentType(contentType), false, id, headers.entrySet().iterator());
        return true;
    }

    public boolean handleInlineUUEncodedPlainText(final String decodedTextContent, final ContentType contentType, final int size, final String fileName, final String id) throws MailException {
        /*
         * Dummy headers
         */
        final Map<String, String> headers = new HashMap<String, String>(4);
        headers.put(CONTENT_TYPE, "text/plain; charset=UTF-8");
        headers.put(CONTENT_DISPOSITION, Part.INLINE);
        headers.put(CONTENT_TRANSFER_ENCODING, "7bit");
        /*
         * Add body part
         */
        addBodyPart(size, new InputStreamProvider() {

            public InputStream getInputStream() throws IOException {
                return ByteBuffers.newUnsynchronizedInputStream(Charsets.UTF_8.encode(decodedTextContent));
            }
        }, contentType, true, id, headers.entrySet().iterator());
        return true;
    }

    public boolean handleMultipartStart(final MailPart mp, final int bodyPartCount, final String id) throws MailException {
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
                headers.put(CONTENT_TYPE, mp.getContentType().toString());
                generateHeadersObject(headers.entrySet().iterator(), currentMailObject);
            }
            return true;
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    public boolean handleMultipartEnd(final MailPart mp, final int bodyPartCount, final String id) throws MailException {
        // Decrement
        if (--multipartCount > 0) { // Dequeue nested multipart
            // Dequeue
            mailJsonObjectQueue.removeLast();
            currentMailObject = mailJsonObjectQueue.getLast();
            currentBodyObject = (JSONValue) currentMailObject.opt(BODY);
        }
        return true;
    }

    public boolean handleNestedMessage(final MailPart mailPart, final String id) throws MailException {
        try {
            final Object content = mailPart.getContent();
            final MailMessage nestedMail;
            if (content instanceof MailMessage) {
                nestedMail = (MailMessage) content;
            } else if (content instanceof InputStream) {
                try {
                    nestedMail =
                        MIMEMessageConverter.convertMessage(new MimeMessage(MIMEDefaultSession.getDefaultSession(), (InputStream) content));
                } catch (final MessagingException e) {
                    throw MIMEMailException.handleMessagingException(e);
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
            final MIMEStructureHandler inner = new MIMEStructureHandler(maxSize).setPrepare(prepare);
            new StructureMailMessageParser().parseMailMessage(nestedMail, inner, id);
            /*
             * Apply to this
             */
            final JSONObject bodyObject = new JSONObject();
            if (multipartCount > 0) {
                // Put headers
                generateHeadersObject(mailPart.getHeadersIterator(), bodyObject);
                // Put body
                final JSONObject jsonMailObject = inner.getJSONMailObject();
                jsonMailObject.put(MailListField.ID.getKey(), mailPart.containsSequenceId() ? mailPart.getSequenceId() : id);
                bodyObject.put(BODY, jsonMailObject);
            }
            add2BodyJsonObject(bodyObject);
            return true;
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    public boolean handleReceivedDate(final Date receivedDate) throws MailException {
        try {
            currentMailObject.put(
                MailJSONField.RECEIVED_DATE.getKey(),
                receivedDate == null ? JSONObject.NULL : Long.valueOf(receivedDate.getTime()));
            return true;
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    public boolean handleSystemFlags(final int flags) throws MailException {
        try {
            if (currentMailObject.hasAndNotNull(MailJSONField.FLAGS.getKey())) {
                final int prevFlags = currentMailObject.getInt(MailJSONField.FLAGS.getKey());
                currentMailObject.put(MailJSONField.FLAGS.getKey(), prevFlags | flags);
            } else {
                currentMailObject.put(MailJSONField.FLAGS.getKey(), flags);
            }
            return true;
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    public boolean handleUserFlags(final String[] userFlags) throws MailException {
        try {
            final JSONArray userFlagsArr = getUserFlags();
            for (final String userFlag : userFlags) {
                userFlagsArr.put(userFlag);
            }
            return true;
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    private void addBodyPart(final MailPart part, final String id) throws MailException {
        try {
            final JSONObject bodyObject = new JSONObject();
            final String disposition = part.containsContentDisposition() ? part.getContentDisposition().getDisposition() : null;
            final boolean isInline = inlineDetector.isInline(disposition, part.getFileName());
            if (multipartCount > 0) {
                // Put headers
                generateHeadersObject(part.getHeadersIterator(), bodyObject);
                // Put body
                final JSONObject body = new JSONObject();
                fillBodyPart(body, part, isInline, id);
                bodyObject.put(BODY, body);
            } else {
                // Put direct
                fillBodyPart(bodyObject, part, isInline, id);
            }
            add2BodyJsonObject(bodyObject);
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    private void addBodyPart(final long size, final InputStreamProvider isp, final ContentType contentType, final boolean inline, final String id, final Iterator<Entry<String, String>> iter) throws MailException {
        try {
            final JSONObject bodyObject = new JSONObject();
            if (multipartCount > 0) {
                // Put headers
                generateHeadersObject(iter, bodyObject);
                // Put body
                final JSONObject body = new JSONObject();
                fillBodyPart(body, size, isp, contentType, inline, id);
                bodyObject.put(BODY, body);
            } else {
                // Put direct
                fillBodyPart(bodyObject, size, isp, contentType, inline, id);
            }
            add2BodyJsonObject(bodyObject);
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    private static final String PRIMARY_TEXT = "text/";

    private void fillBodyPart(final JSONObject bodyObject, final MailPart part, final boolean inline, final String id) throws MailException {
        try {
            bodyObject.put(MailListField.ID.getKey(), id);
            final long size = part.getSize();
            if (maxSize > 0 && size > maxSize) {
                bodyObject.put(DATA, JSONObject.NULL);
            } else {
                if (prepare && inline && part.getContentType().startsWith(PRIMARY_TEXT)) {
                    bodyObject.put(DATA, readContent(part, part.getContentType()));
                } else {
                    fillBase64JSONString(part.getInputStream(), bodyObject, true);
                }
            }
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        } catch (final IOException e) {
            throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
        }
    }

    private void fillBodyPart(final JSONObject bodyObject, final long size, final InputStreamProvider isp, final ContentType contentType, final boolean inline, final String id) throws MailException {
        try {
            bodyObject.put(MailListField.ID.getKey(), id);
            if (maxSize > 0 && size > maxSize) {
                bodyObject.put(DATA, JSONObject.NULL);
            } else {
                if (prepare && inline && contentType.startsWith(PRIMARY_TEXT)) {
                    bodyObject.put(DATA, readContent(isp, contentType));
                } else {
                    fillBase64JSONString(isp.getInputStream(), bodyObject, true);
                }
            }
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        } catch (final IOException e) {
            throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
        }
    }

    private static void fillBase64JSONString(final InputStream inputStream, final JSONObject bodyObject, final boolean streaming) throws MailException {
        try {
            if (streaming) {
                bodyObject.put(DATA, new Base64JSONString(inputStream));
            } else {
                final byte[] bytes;
                {
                    try {
                        final byte[] buf = new byte[BUFLEN];
                        final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(BUFLEN << 2);
                        int read;
                        while ((read = inputStream.read(buf, 0, BUFLEN)) >= 0) {
                            out.write(buf, 0, read);
                        }
                        bytes = out.toByteArray();
                    } catch (final IOException e) {
                        throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
                    } finally {
                        if (null != inputStream) {
                            try {
                                inputStream.close();
                            } catch (final IOException e) {
                                LOG.error(e.getMessage(), e);
                            }
                        }
                    }
                }
                // Add own JSONString implementation to support streaming
                bodyObject.put(DATA, new String(Base64.encodeBase64(bytes, false), "US-ASCII"));
            }
        } catch (final UnsupportedEncodingException e) {
            throw new MailException(MailException.Code.ENCODING_ERROR, e, "US-ASCII");
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    private static final HeaderName HN_CONTENT_TYPE = HeaderName.valueOf(CONTENT_TYPE);

    private static final Set<HeaderName> PARAMETERIZED_HEADERS =
        new HashSet<HeaderName>(Arrays.asList(HN_CONTENT_TYPE, HeaderName.valueOf(CONTENT_DISPOSITION)));

    private static final Set<HeaderName> ADDRESS_HEADERS =
        new HashSet<HeaderName>(Arrays.asList(
            HeaderName.valueOf("From"),
            HeaderName.valueOf("To"),
            HeaderName.valueOf("Cc"),
            HeaderName.valueOf("Bcc"),
            HeaderName.valueOf("Reply-To"),
            HeaderName.valueOf("Sender"),
            HeaderName.valueOf("Errors-To")));

    private static final MailDateFormat MAIL_DATE_FORMAT = new MailDateFormat();

    private void generateHeadersObject(final Iterator<Entry<String, String>> iter, final JSONObject parent) throws MailException {
        try {
            final JSONObject hdrObject = new JSONObject();
            if (prepare) {
                while (iter.hasNext()) {
                    final Entry<String, String> entry = iter.next();
                    final String name = entry.getKey().toLowerCase(Locale.ENGLISH);
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
                            final JSONObject addressJsonObject = new JSONObject();
                            final String personal = internetAddress.getPersonal();
                            if (null != personal) {
                                addressJsonObject.put("personal", personal);
                            }
                            addressJsonObject.put("address", internetAddress.getAddress());
                            ja.put(addressJsonObject);
                        }
                    } else if (PARAMETERIZED_HEADERS.contains(headerName)) {
                        final JSONObject parameterJsonObject = new JSONObject();
                        {
                            final ParameterizedHeader parameterizedHeader;
                            if (HN_CONTENT_TYPE.equals(headerName)) {
                                final ContentType ct = new ContentType(entry.getValue());
                                parameterJsonObject.put("type", ct.getBaseType().toLowerCase(Locale.ENGLISH));
                                parameterizedHeader = ct;
                            } else {
                                final ContentDisposition cd = new ContentDisposition(entry.getValue());
                                parameterJsonObject.put("type", cd.getDisposition().toLowerCase(Locale.ENGLISH));
                                parameterizedHeader = cd;
                            }
                            final JSONObject paramListJsonObject = new JSONObject();
                            for (final Iterator<String> pi = parameterizedHeader.getParameterNames(); pi.hasNext();) {
                                final String paramName = pi.next();
                                if ("read-date".equalsIgnoreCase(paramName)) {
                                    final String paramVal = parameterizedHeader.getParameter(paramName);
                                    synchronized (MAIL_DATE_FORMAT) {
                                        try {
                                            paramListJsonObject.put(
                                                paramName.toLowerCase(Locale.ENGLISH),
                                                MAIL_DATE_FORMAT.parse(paramVal).getTime());
                                        } catch (final ParseException pex) {
                                            paramListJsonObject.put(paramName.toLowerCase(Locale.ENGLISH), paramVal);
                                        }
                                    }
                                } else {
                                    paramListJsonObject.put(
                                        paramName.toLowerCase(Locale.ENGLISH),
                                        parameterizedHeader.getParameter(paramName));
                                }
                            }
                            parameterJsonObject.put("params", paramListJsonObject);
                        }
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
                            ja.put(MIMEMessageUtility.decodeMultiEncodedHeader(entry.getValue()));
                        } else {
                            hdrObject.put(name, MIMEMessageUtility.decodeMultiEncodedHeader(entry.getValue()));
                        }
                    }
                }
            } else {
                while (iter.hasNext()) {
                    final Entry<String, String> entry = iter.next();
                    final String name = entry.getKey();
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
                        ja.put(entry.getValue());
                    } else {
                        hdrObject.put(name, entry.getValue());
                    }
                }
            }
            parent.put(MailJSONField.HEADERS.getKey(), hdrObject.length() > 0 ? hdrObject : JSONObject.NULL);
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
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
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    new StringBuilder(128).append("Internet addresses could not be properly parsed: \"").append(e.getMessage()).append(
                        "\". Using plain addresses' string representation instead.").toString(),
                    e);
            }
            return getAddressesOnParseError(addresses);
        }
    }

    private static InternetAddress[] getAddressesOnParseError(final String addr) {
        return new InternetAddress[] { new PlainTextAddress(addr) };
    }

    private static String readContent(final MailPart mailPart, final ContentType contentType) throws MailException, IOException {
        final String charset = getCharset(mailPart, contentType);
        try {
            return MessageUtility.readMailPart(mailPart, charset);
        } catch (final java.io.CharConversionException e) {
            // Obviously charset was wrong or bogus implementation of character conversion
            final String fallback = "US-ASCII";
            if (LOG.isWarnEnabled()) {
                LOG.warn(new StringBuilder("Character conversion exception while reading content with charset \"").append(charset).append(
                    "\". Using fallback charset \"").append(fallback).append("\" instead."), e);
            }
            return MessageUtility.readMailPart(mailPart, fallback);
        }
    }

    private static String getCharset(final MailPart mailPart, final ContentType contentType) throws MailException {
        final String charset;
        if (mailPart.containsHeader(MessageHeaders.HDR_CONTENT_TYPE)) {
            String cs = contentType.getCharsetParameter();
            if (!CharsetDetector.isValid(cs)) {
                StringBuilder sb = null;
                if (null != cs) {
                    sb = new StringBuilder(64).append("Illegal or unsupported encoding: \"").append(cs).append("\".");
                }
                if (contentType.startsWith(PRIMARY_TEXT)) {
                    cs = CharsetDetector.detectCharset(mailPart.getInputStream());
                    if (LOG.isWarnEnabled() && null != sb) {
                        sb.append(" Using auto-detected encoding: \"").append(cs).append('"');
                        LOG.warn(sb.toString());
                    }
                } else {
                    cs = MailProperties.getInstance().getDefaultMimeCharset();
                    if (LOG.isWarnEnabled() && null != sb) {
                        sb.append(" Using fallback encoding: \"").append(cs).append('"');
                        LOG.warn(sb.toString());
                    }
                }
            }
            charset = cs;
        } else {
            if (contentType.startsWith(PRIMARY_TEXT)) {
                charset = CharsetDetector.detectCharset(mailPart.getInputStream());
            } else {
                charset = MailProperties.getInstance().getDefaultMimeCharset();
            }
        }
        return charset;
    }

    private static String readContent(final InputStreamProvider isp, final ContentType contentType) throws MailException, IOException {
        final String charset = getCharset(isp, contentType);
        try {
            return MessageUtility.readStream(isp.getInputStream(), charset);
        } catch (final java.io.CharConversionException e) {
            // Obviously charset was wrong or bogus implementation of character conversion
            final String fallback = "US-ASCII";
            if (LOG.isWarnEnabled()) {
                LOG.warn(new StringBuilder("Character conversion exception while reading content with charset \"").append(charset).append(
                    "\". Using fallback charset \"").append(fallback).append("\" instead."), e);
            }
            return MessageUtility.readStream(isp.getInputStream(), fallback);
        }
    }

    private static String getCharset(final InputStreamProvider isp, final ContentType contentType) throws IOException {
        final String charset;
        if (contentType.startsWith(PRIMARY_TEXT)) {
            charset = CharsetDetector.detectCharset(isp.getInputStream());
        } else {
            charset = MailProperties.getInstance().getDefaultMimeCharset();
        }
        return charset;
    }

    private static interface InputStreamProvider {

        InputStream getInputStream() throws IOException;
    }

    private static interface InlineDetector {

        public boolean isInline(String disposition, String fileName);
    }

    /**
     * If disposition equals ignore-case <code>"INLINE"</code>, then it is treated as inline in any case.<br>
     * Only if disposition is <code>null</code> the file name is examined.
     */
    private static final InlineDetector LENIENT_DETECTOR = new InlineDetector() {

        public boolean isInline(final String disposition, final String fileName) {
            return Part.INLINE.equalsIgnoreCase(disposition) || ((disposition == null) && (fileName == null));
        }
    };

    /**
     * Considered as inline if disposition equals ignore-case <code>"INLINE"</code> OR is <code>null</code>, but in any case the file name
     * must be <code>null</code>.
     */
    private static final InlineDetector STRICT_DETECTOR = new InlineDetector() {

        public boolean isInline(final String disposition, final String fileName) {
            return (Part.INLINE.equalsIgnoreCase(disposition) || (disposition == null)) && (fileName == null);
        }
    };

}
