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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import javax.mail.MessagingException;
import javax.mail.Part;
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
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MIMEDefaultSession;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.MIMEType2ExtMap;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.structure.StructureHandler;
import com.openexchange.mail.structure.StructureMailMessageParser;
import com.openexchange.mail.uuencode.UUEncodedPart;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link MIMEStructureHandler} - The handler to generate a JSON object reflecting a message's MIME structure.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MIMEStructureHandler implements StructureHandler {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MIMEStructureHandler.class);

    private final LinkedList<JSONObject> mailJsonObjectQueue;

    private JSONObject currentMailObject;
    
    private JSONValue currentBodyObject;
    
    private int multipartCount;

    private final long maxSize;

    // private JSONValue bodyJsonObject;

    private JSONArray userFlags;

    /**
     * Initializes a new {@link MIMEStructureHandler}.
     * 
     * @param maxSize The max. size of a mail part to let its content being inserted as base64 encoded string.
     */
    public MIMEStructureHandler(final long maxSize) {
        super();
        mailJsonObjectQueue = new LinkedList<JSONObject>();
        mailJsonObjectQueue.addLast((currentMailObject = new JSONObject()));
        this.maxSize = maxSize;
    }

    /**
     * Gets the JSON representation of mail's MIME structure.
     * 
     * @return The JSON representation of mail's MIME structure
     */
    public JSONObject getMailJsonObject() {
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
            currentMailObject.put("body", currentBodyObject);
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
                currentMailObject.put("body", currentBodyObject);
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
            getUserFlags().put(MailMessage.getColorLabelStringValue(colorLabel));
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
                new Throwable(new StringBuilder("Unable to fetch content/type for '").append(filename).append("': ").append(e).toString());
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
        headers.put("Content-Type", sb.append(contentType).append("; name=").append(encodeFN).toString());
        sb.setLength(0);
        headers.put("Content-Disposition", new StringBuilder(Part.ATTACHMENT).append("; filename=").append(encodeFN).toString());
        headers.put("Content-Transfer-Encoding", "base64");
        /*
         * Add body part
         */
        addBodyPart(part.getFileSize(), new InputStreamProvider() {

            public InputStream getInputStream() throws IOException {
                return part.getInputStream();
            }
        }, id, headers.entrySet().iterator());
        return true;
    }

    public boolean handleInlineUUEncodedPlainText(final String decodedTextContent, final ContentType contentType, final int size, final String fileName, final String id) throws MailException {
        /*
         * Dummy headers
         */
        final Map<String, String> headers = new HashMap<String, String>(4);
        headers.put("Content-Type", "text/plain; charset=UTF-8");
        headers.put("Content-Disposition", Part.INLINE);
        headers.put("Content-Transfer-Encoding", "7bit");
        /*
         * Add body part
         */
        addBodyPart(size, new InputStreamProvider() {

            public InputStream getInputStream() throws IOException {
                return new UnsynchronizedByteArrayInputStream(decodedTextContent.getBytes("UTF-8"));

            }
        }, id, headers.entrySet().iterator());
        return true;
    }

    public boolean handleMultipartStart(final MailPart mp, final int bodyPartCount, final String id) throws MailException {
        try {
            // Increment
            multipartCount++;
            // Create a new mail object
            final JSONObject newMailObject = new JSONObject();
            // Apply new mail object to current mail object's body element
            add2BodyJsonObject(newMailObject);
            // Assign new mail object to current mail object
            currentMailObject = newMailObject;
            mailJsonObjectQueue.addLast(currentMailObject);
            currentBodyObject = null;
            return true;
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    public boolean handleMultipartEnd(final MailPart mp, final int bodyPartCount, final String id) throws MailException {
        // Dequeue
        mailJsonObjectQueue.removeLast();
        currentMailObject = mailJsonObjectQueue.getLast();
        currentBodyObject = (JSONValue) currentMailObject.opt("body");
        // Decrement
        multipartCount--;
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
            final MIMEStructureHandler inner = new MIMEStructureHandler(maxSize);
            new StructureMailMessageParser().parseMailMessage(nestedMail, inner, id);
            /*
             * Apply to this
             */
            final JSONObject bodyObject = new JSONObject();
            if (multipartCount > 0) {
                // Put headers
                generateHeadersObject(mailPart.getHeadersIterator(), bodyObject);
                // Put body
                bodyObject.put("body", inner.getMailJsonObject());
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
            if (multipartCount > 0) {
                // Put headers
                generateHeadersObject(part.getHeadersIterator(), bodyObject);
                // Put body
                final JSONObject body = new JSONObject();
                fillBodyPart(body, part, id);
                bodyObject.put("body", body);
            } else {
                // Put direct
                fillBodyPart(bodyObject, part, id);
            }
            add2BodyJsonObject(bodyObject);
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    private void addBodyPart(final long size, final InputStreamProvider isp, final String id, final Iterator<Entry<String, String>> iter) throws MailException {
        try {
            final JSONObject bodyObject = new JSONObject();
            if (multipartCount > 0) {
                // Put headers
                generateHeadersObject(iter, bodyObject);
                // Put body
                final JSONObject body = new JSONObject();
                fillBodyPart(body, size, isp, id);
                bodyObject.put("body", body);
            } else {
                // Put direct
                fillBodyPart(bodyObject, size, isp, id);
            }
            add2BodyJsonObject(bodyObject);
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    private void fillBodyPart(final JSONObject bodyObject, final MailPart part, final String id) throws MailException {
        try {
            bodyObject.put(MailListField.ID.getKey(), id);
            final long size = part.getSize();
            if (maxSize > 0 && size > maxSize) {
                bodyObject.put("data", JSONObject.NULL);
            } else {
                final byte[] bytes;
                {
                    final InputStream inputStream = part.getInputStream();
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
                        try {
                            inputStream.close();
                        } catch (final IOException e) {
                            org.apache.commons.logging.LogFactory.getLog(MIMEStructureHandler.class).error(e.getMessage(), e);
                        }
                    }
                }
                bodyObject.put("data", new String(Base64.encodeBase64(bytes, false), "US-ASCII"));
            }
        } catch (final UnsupportedEncodingException e) {
            // Cannot occur since US-ASCII is supported
            throw new MailException(MailException.Code.ENCODING_ERROR, e, e.getMessage());
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    private void fillBodyPart(final JSONObject bodyObject, final long size, final InputStreamProvider isp, final String id) throws MailException {
        try {
            bodyObject.put(MailListField.ID.getKey(), id);
            if (maxSize > 0 && size > maxSize) {
                bodyObject.put("data", JSONObject.NULL);
            } else {
                final byte[] bytes;
                {
                    InputStream inputStream = null;
                    try {
                        inputStream = isp.getInputStream();
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
                bodyObject.put("data", new String(Base64.encodeBase64(bytes, false), "US-ASCII"));
            }
        } catch (final UnsupportedEncodingException e) {
            // Cannot occur since US-ASCII is supported
            throw new MailException(MailException.Code.ENCODING_ERROR, e, e.getMessage());
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    private static void generateHeadersObject(final Iterator<Entry<String, String>> iter, final JSONObject parent) throws MailException {
        try {
            final JSONObject hdrObject = new JSONObject();
            while (iter.hasNext()) {
                final Entry<String, String> entry = iter.next();
                final String headerName = entry.getKey();
                if (hdrObject.has(headerName)) {
                    final Object previous = hdrObject.get(headerName);
                    final JSONArray ja;
                    if (previous instanceof JSONArray) {
                        ja = (JSONArray) previous;
                        ja.put(entry.getValue());
                    } else {
                        ja = new JSONArray();
                        hdrObject.put(headerName, ja);
                        ja.put(previous);
                    }
                    ja.put(entry.getValue());
                } else {
                    hdrObject.put(headerName, entry.getValue());
                }
            }
            parent.put(MailJSONField.HEADERS.getKey(), hdrObject.length() > 0 ? hdrObject : JSONObject.NULL);
        } catch (final JSONException e) {
            throw new MailException(MailException.Code.JSON_ERROR, e, e.getMessage());
        }
    }

    private static interface InputStreamProvider {

        InputStream getInputStream() throws IOException;
    }

}
