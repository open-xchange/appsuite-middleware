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

package com.openexchange.messaging.json;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.java.Streams;
import com.openexchange.messaging.ByteArrayContent;
import com.openexchange.messaging.CaptchaParams;
import com.openexchange.messaging.ContentDisposition;
import com.openexchange.messaging.ContentType;
import com.openexchange.messaging.ManagedFileContent;
import com.openexchange.messaging.MessagingBodyPart;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingHeader.KnownHeader;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.MultipartContent;
import com.openexchange.messaging.StringContent;
import com.openexchange.messaging.StringMessageHeader;
import com.openexchange.messaging.generic.internet.MimeContentDisposition;
import com.openexchange.messaging.generic.internet.MimeContentType;
import com.openexchange.messaging.generic.internet.MimeMessagingBodyPart;
import com.openexchange.messaging.generic.internet.MimeMessagingMessage;
import com.openexchange.messaging.generic.internet.MimeMultipartContent;
import com.openexchange.tools.encoding.Base64;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * A parser to parse JSON representations of MessagingMessages. Note that parsing can be customized by registering one or more
 * {@link MessagingHeaderParser} and one or more {@link MessagingContentParser}.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MessagingMessageParser {

    /**
     * The collection of {@link MessagingHeaderParser header parsers}.
     */
    private final Collection<MessagingHeaderParser> headerParsers;

    /**
     * The collection of {@link MessagingContentParser content parsers}.
     */
    private final Collection<MessagingContentParser> contentParsers;

    /**
     * Initializes a new {@link MessagingMessageParser}.
     */
    public MessagingMessageParser() {
        super();
        headerParsers = new ConcurrentLinkedQueue<MessagingHeaderParser>();
        contentParsers = new ConcurrentLinkedQueue<MessagingContentParser>();
        // Header parsers
        headerParsers.add(new AddressHeaderParser());
        headerParsers.add(new ContentTypeParser());
        headerParsers.add(new ContentDispositionParser());
        headerParsers.add(new MultiStringParser());
        // Content parsers
        contentParsers.add(new StringContentParser());
        contentParsers.add(new BinaryContentParser());
        contentParsers.add(new MultipartContentParser());
    }

    /**
     * Parses the JSON representation of a messaging message. References to binaries are resolved using the attached registry.
     */
    public MessagingMessage parse(final JSONObject messageJSON, final MessagingInputStreamRegistry registry, final String remoteAddress) throws JSONException, OXException, IOException {

        final MimeMessagingMessage message = new MimeMessagingMessage();

        if (messageJSON.has("colorLabel")) {
            message.setColorLabel(messageJSON.getInt("colorLabel"));
        }

        if (messageJSON.has("flags")) {
            message.setFlags(messageJSON.getInt("flags"));
        }

        if (messageJSON.has("userFlags")) {
            final JSONArray array = messageJSON.getJSONArray("userFlags");
            final List<String> userFlags = new ArrayList<String>(array.length());
            for (int i = 0, size = array.length(); i < size; i++) {
                userFlags.add(array.getString(i));
            }
            message.setUserFlags(userFlags);
        }

        if (messageJSON.has("receivedDate")) {
            message.setReceivedDate(messageJSON.getLong("receivedDate"));
        }
        if (messageJSON.has("threadLevel")) {
            message.setThreadLevel(messageJSON.getInt("threadLevel"));
        }

        if (messageJSON.has("folder")) {
            message.setFolder(messageJSON.getString("folder"));
        }

        if (messageJSON.has("picture")) {
            message.setPicture(messageJSON.getString("picture"));
        }

        setValues(message, registry, messageJSON);

        /*
         * Parse possible file references
         */
        if (messageJSON.hasAndNotNull("attachments")) {
            parseAttachments(message, messageJSON, registry);
        }

        /*
         * Parse possible captcha element
         */
        if (messageJSON.hasAndNotNull("captcha")) {
            final JSONObject captcha = messageJSON.getJSONObject("captcha");
            final CaptchaParams params = new CaptchaParams();
            if (captcha.has("challenge")) {
                params.setChallenge(captcha.getString("challenge"));
            }
            if (captcha.has("response")) {
                params.setResponse(captcha.getString("response"));
            }
            if (null != remoteAddress) {
                params.setAddress(remoteAddress);
            }
            message.putParameter("__captchaParams", params);
        }

        return message;
    }

    private void parseAttachments(final MimeMessagingMessage message, final JSONObject messageJSON, final MessagingInputStreamRegistry registry) throws OXException, JSONException, IOException {
        final Object object = messageJSON.opt("attachments");
        if (object instanceof JSONArray) {
            final JSONArray attachments = (JSONArray) object;
            final int length = attachments.length();
            if (length > 0) {
                final MimeMultipartContent mimeMultipartContent = new MimeMultipartContent("mixed");
                /*
                 * Add previous message content
                 */
                {
                    final MessagingContent content = message.getContent();
                    if (content instanceof MultipartContent) {
                        final MultipartContent multipartContent = (MultipartContent) content;
                        if ("mixed".equals(multipartContent.getSubType())) {
                            final int count = multipartContent.getCount();
                            for (int i = 0; i < count; i++) {
                                mimeMultipartContent.addBodyPart((MimeMessagingBodyPart) multipartContent.get(i));
                            }
                        } else {
                            final MimeMessagingBodyPart bodyPart = new MimeMessagingBodyPart(mimeMultipartContent);
                            bodyPart.setContent((MimeMultipartContent) multipartContent);
                            mimeMultipartContent.addBodyPart(bodyPart);
                        }
                    } else {
                        final MimeMessagingBodyPart bodyPart = new MimeMessagingBodyPart(mimeMultipartContent);
                        bodyPart.setContent(content, message.getContentType().toString());
                        bodyPart.setDisposition(message.getDisposition());
                        mimeMultipartContent.addBodyPart(bodyPart);
                    }
                }
                /*
                 * Add referenced attachments to multipart
                 */
                for (int i = 0; i < length; i++) {
                    final String identifier = attachments.getString(i);
                    final Object registryEntry = registry.getRegistryEntry(identifier);
                    final MimeMessagingBodyPart bodyPart = new MimeMessagingBodyPart(mimeMultipartContent);
                    if (registryEntry instanceof ManagedFile) {
                        final ManagedFile managedFile = (ManagedFile) registryEntry;
                        final MessagingContent attachmentContent = new ManagedFileContentImpl(managedFile);
                        bodyPart.setContent(attachmentContent, managedFile.getContentType());
                        bodyPart.setDisposition(managedFile.getContentDisposition());
                        bodyPart.setFileName(managedFile.getFileName());
                    } else {
                        /*
                         * Initialize input stream
                         */
                        final InputStream in = new BufferedInputStream(registry.get(identifier), 65536);
                        try {
                            final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(8192 << 1);
                            final byte[] buf = new byte[8192];
                            int read;
                            while ((read = in.read(buf, 0, buf.length)) > 0) {
                                out.write(buf, 0, read);
                            }
                            final MessagingContent attachmentContent = new ByteArrayContent(out.toByteArray());
                            bodyPart.setContent(attachmentContent, "application/octet-stream");
                            bodyPart.setDisposition(MessagingPart.ATTACHMENT);
                        } finally {
                            Streams.close(in);
                        }
                    }
                    mimeMultipartContent.addBodyPart(bodyPart);
                    message.setContent(mimeMultipartContent);
                }
            }
        }
    }

    protected void setValues(final MimeMessagingBodyPart bodyPart, final MessagingInputStreamRegistry registry, final JSONObject messageJSON) throws JSONException, OXException, IOException {

        if (messageJSON.has("id")) {
            bodyPart.setSectionId(messageJSON.getString("id"));
        }

        if (messageJSON.has("size")) {
            bodyPart.setSize(messageJSON.getLong("size"));
        }

        JSONObject headers = messageJSON.optJSONObject("headers");
        if (headers == null) {
            headers = new JSONObject();
        }

        for (final MessagingField field : MessagingField.values()) {
            final KnownHeader header = field.getEquivalentHeader();
            if (header != null && messageJSON.hasAndNotNull(field.toString())) {
                headers.put(header.toString(), messageJSON.get(field.toString()));
            }
        }

        setHeaders(headers, bodyPart);

        if (messageJSON.hasAndNotNull("body")) {
            setContent(messageJSON.get("body"), registry, bodyPart);
        }

    }

    /**
     * Adds a {@link MessagingHeaderParser} to the list of known parsers. In this way new headers may be parsed in a custom manner
     *
     * @param parser
     */
    public void addHeaderParser(final MessagingHeaderParser parser) {
        headerParsers.add(parser);
    }

    public void removeHeaderParser(final MessagingHeaderParser parser) {
        headerParsers.remove(parser);
    }

    /**
     * Adds a {@link MessagingContentParser} to the list of known parsers. In this way new {@link MessagingContent} types may be parsed in a
     * custom manner
     *
     * @param parser
     */
    public void addContentParser(final MessagingContentParser parser) {
        contentParsers.add(parser);
    }

    public void removeContentParser(final MessagingContentParser parser) {
        contentParsers.remove(parser);
    }

    private void setHeaders(final JSONObject object, final MimeMessagingBodyPart bodyPart) throws JSONException, OXException {
        final Map<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>();
        for (final String key : object.keySet()) {
            final Object value = object.get(key);

            MessagingHeaderParser candidate = null;
            int ranking = 0;
            for (final MessagingHeaderParser parser : headerParsers) {
                if (parser.handles(key, value)) {
                    if (candidate == null || ranking < parser.getRanking()) {
                        candidate = parser;
                        ranking = parser.getRanking();
                    }
                }
            }
            if (candidate == null) {
                final StringMessageHeader header = new StringMessageHeader(key, value.toString());
                headers.put(key, Arrays.asList((MessagingHeader) header));
            } else {
                candidate.parseAndAdd(headers, key, value);
            }

        }
        bodyPart.setAllHeaders(headers);
    }

    private void setContent(final Object content, final MessagingInputStreamRegistry registry, final MimeMessagingBodyPart bodyPart) throws OXException, JSONException, IOException {
        MessagingContentParser candidate = null;
        int ranking = 0;

        for (final MessagingContentParser parser : contentParsers) {
            if (parser.handles(bodyPart, content)) {
                if ((candidate == null) || (ranking < parser.getRanking())) {
                    candidate = parser;
                    ranking = parser.getRanking();
                }
            }
        }
        if (candidate == null) {
            // Expect content to be a string
            final StringContent stringContent = new StringContent(content.toString());
            final String contentType = "text/plain; charset=ISO-8859-1";
            final ContentType mct = new MimeContentType(contentType);
            bodyPart.setHeader("Content-Type", mct.toString());
            bodyPart.setContent(stringContent, contentType);
        } else {
            final MessagingContent parsedContent = candidate.parse(bodyPart, content, registry);
            if (parsedContent instanceof ManagedFileContent) {
                final ManagedFileContent managedFileContent = (ManagedFileContent) parsedContent;
                final ContentType mct = new MimeContentType(managedFileContent.getContentType());
                final ContentDisposition mcd = new MimeContentDisposition(MessagingPart.ATTACHMENT);
                final String fileName = managedFileContent.getFileName();
                if (null != fileName) {
                    mct.setParameter("name", fileName);
                    mcd.setParameter("filename", fileName);
                }
                bodyPart.setHeader("Content-Type", mct.toString());
                bodyPart.setHeader("Content-Disposition", mcd.toString());
                bodyPart.setContent(managedFileContent, managedFileContent.getContentType());
            } else {
                bodyPart.setContent(parsedContent, bodyPart.getContentType().getValue());
            }
        }
    }

    private static final class MultiStringParser implements MessagingHeaderParser {

        public MultiStringParser() {
            super();
        }

        @Override
        public int getRanking() {
            return 0;
        }

        @Override
        public boolean handles(final String key, final Object value) {
            return JSONArray.class.isInstance(value);
        }

        @Override
        public void parseAndAdd(final Map<String, Collection<MessagingHeader>> headers, final String key, final Object value) throws JSONException {
            final JSONArray values = (JSONArray) value;
            final LinkedList<MessagingHeader> valueList = new LinkedList<MessagingHeader>();
            for (int i = 0, size = values.length(); i < size; i++) {
                final StringMessageHeader header = new StringMessageHeader(key, values.getString(i));
                valueList.add(header);
            }
            headers.put(key, valueList);
        }

    }

    private static final class StringContentParser implements MessagingContentParser {

        public StringContentParser() {
            super();
        }

        @Override
        public int getRanking() {
            return 0;
        }

        @Override
        public boolean handles(final MessagingBodyPart partlyParsedMessage, final Object content) throws OXException {
            final ContentType contentType = partlyParsedMessage.getContentType();
            return (null != contentType) && contentType.getPrimaryType().equals("text");
        }

        @Override
        public MessagingContent parse(final MessagingBodyPart partlyParsedMessage, final Object content, final MessagingInputStreamRegistry registry) throws JSONException, OXException {
            return new StringContent((String) content);
        }
    }

    private static final class BinaryContentParser implements MessagingContentParser {

        public BinaryContentParser() {
            super();
        }

        @Override
        public int getRanking() {
            return 0;
        }

        @Override
        public boolean handles(final MessagingBodyPart partlyParsedMessage, final Object content) throws OXException {
            final ContentType contentType = partlyParsedMessage.getContentType();
            if (null != contentType) {
                final String primaryType = contentType.getPrimaryType();
                return !primaryType.equals("text") && !primaryType.equals("multipart");
            }
            return false;
        }

        @Override
        public MessagingContent parse(final MessagingBodyPart partlyParsedMessage, final Object content, final MessagingInputStreamRegistry registry) throws JSONException, OXException, IOException {
            if (String.class.isInstance(content)) {
                final byte[] decoded = Base64.decode((String) content);
                return new ByteArrayContent(decoded);
            } else if (JSONObject.class.isInstance(content)) {
                final JSONObject reference = (JSONObject) content;
                final Object identifier = reference.get("ref");
                /*
                 * Check entry object first
                 */
                {
                    final Object registryEntry = registry.getRegistryEntry(identifier);
                    if (registryEntry instanceof ManagedFile) {
                        final ManagedFile managedFile = (ManagedFile) registryEntry;
                        return new ManagedFileContentImpl(managedFile);
                    }
                }
                /*
                 * Initialize input stream
                 */
                final InputStream in = new BufferedInputStream(registry.get(identifier), 65536);
                try {
                    final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(8192 << 1);
                    final byte[] buf = new byte[8192];
                    int read;
                    while ((read = in.read(buf, 0, buf.length)) > 0) {
                        out.write(buf, 0, read);
                    }
                    return new ByteArrayContent(out.toByteArray());
                } finally {
                    Streams.close(in);
                }
            } else if (byte[].class.isInstance(content)) {
                return new ByteArrayContent((byte[]) content);
            } else {
                throw MessagingExceptionCodes.UNEXPECTED_ERROR.create("Unexpected content type: " + (null == content ? "null" : content.getClass().getName()));
            }
        }

    }

    private final class MultipartContentParser implements MessagingContentParser {

        public MultipartContentParser() {
            super();
        }

        @Override
        public int getRanking() {
            return 0;
        }

        @Override
        public boolean handles(final MessagingBodyPart partlyParsedMessage, final Object content) throws OXException {
            final ContentType contentType = partlyParsedMessage.getContentType();
            return (null != contentType) && contentType.getPrimaryType().equals("multipart");
        }

        @Override
        public MessagingContent parse(final MessagingBodyPart partlyParsedMessage, final Object content, final MessagingInputStreamRegistry registry) throws JSONException, OXException, IOException {
            final MimeMultipartContent multipartContent = new MimeMultipartContent();
            final JSONArray multipartJSON = (JSONArray) content;
            for (int i = 0, size = multipartJSON.length(); i < size; i++) {
                final JSONObject partJSON = multipartJSON.getJSONObject(i);
                final MimeMessagingBodyPart part = new MimeMessagingBodyPart();
                setValues(part, registry, partJSON);

                multipartContent.addBodyPart(part);
                part.setParent(multipartContent);
            }
            return multipartContent;
        }

    }

    private static final class ManagedFileContentImpl implements ManagedFileContent {

        private final ManagedFile managedFile;

        /**
         * Initializes a new {@link ManagedFileContentImplementation}.
         *
         * @param managedFile
         */
        public ManagedFileContentImpl(final ManagedFile managedFile) {
            super();
            this.managedFile = managedFile;
        }

        @Override
        public InputStream getData() throws OXException {
            return managedFile.getInputStream();
        }

        @Override
        public String getFileName() {
            return managedFile.getFileName();
        }

        @Override
        public String getContentType() {
            return managedFile.getContentType();
        }

        @Override
        public String getId() {
            return managedFile.getID();
        }
    }

}
