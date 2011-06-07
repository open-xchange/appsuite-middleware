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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import com.openexchange.messaging.ByteArrayContent;
import com.openexchange.messaging.MessagingBodyPart;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.StringContent;
import com.openexchange.messaging.StringMessageHeader;
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
    public MessagingMessage parse(final JSONObject messageJSON, final MessagingInputStreamRegistry registry) throws JSONException, MessagingException, IOException {

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

        return message;
    }

    protected void setValues(final MimeMessagingBodyPart message, final MessagingInputStreamRegistry registry, final JSONObject messageJSON) throws JSONException, MessagingException, IOException {

        if (messageJSON.has("id")) {
            message.setSectionId(messageJSON.getString("id"));
        }

        if (messageJSON.has("size")) {
            message.setSize(messageJSON.getLong("size"));
        }

        JSONObject headers = messageJSON.optJSONObject("headers");
        if (headers == null) {
            headers = new JSONObject();
        }

        for (final MessagingField field : MessagingField.values()) {
            if (field.getEquivalentHeader() != null && messageJSON.has(field.toString())) {
                headers.put(field.getEquivalentHeader().toString(), messageJSON.get(field.toString()));
            }
        }

        setHeaders(headers, message);

        if (messageJSON.has("body")) {
            setContent(messageJSON.get("body"), registry, message);
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

    private void setHeaders(final JSONObject object, final MimeMessagingBodyPart message) throws JSONException, MessagingException {
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
        message.setAllHeaders(headers);
    }

    private void setContent(final Object content, final MessagingInputStreamRegistry registry, final MimeMessagingBodyPart message) throws MessagingException, JSONException, IOException {
        MessagingContentParser candidate = null;
        int ranking = 0;

        for (final MessagingContentParser parser : contentParsers) {
            if (parser.handles(message, content)) {
                if (candidate == null || ranking < parser.getRanking()) {
                    candidate = parser;
                    ranking = parser.getRanking();
                }
            }
        }
        if (candidate != null) {
            final MessagingContent parsedContent = candidate.parse(message, content, registry);
            message.setContent(parsedContent, message.getContentType().getValue());
        }
    }

    private static final class MultiStringParser implements MessagingHeaderParser {

        public MultiStringParser() {
            super();
        }

        public int getRanking() {
            return 0;
        }

        public boolean handles(final String key, final Object value) {
            return JSONArray.class.isInstance(value);
        }

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

        public int getRanking() {
            return 0;
        }

        public boolean handles(final MessagingBodyPart partlyParsedMessage, final Object content) throws MessagingException {
            if (null != partlyParsedMessage.getContentType()) {
                return partlyParsedMessage.getContentType().getPrimaryType().equals("text");
            }
            return false;
        }

        public MessagingContent parse(final MessagingBodyPart partlyParsedMessage, final Object content, final MessagingInputStreamRegistry registry) throws JSONException, MessagingException {
            return new StringContent((String) content);
        }
    }

    private static final class BinaryContentParser implements MessagingContentParser {

        public BinaryContentParser() {
            super();
        }

        public int getRanking() {
            return 0;
        }

        public boolean handles(final MessagingBodyPart partlyParsedMessage, final Object content) throws MessagingException {
            if (null != partlyParsedMessage.getContentType()) {
                final String primaryType = partlyParsedMessage.getContentType().getPrimaryType();
                return !primaryType.equals("text") && !primaryType.equals("multipart");
            }
            return false;
        }

        public MessagingContent parse(final MessagingBodyPart partlyParsedMessage, final Object content, final MessagingInputStreamRegistry registry) throws JSONException, MessagingException, IOException {
            if (String.class.isInstance(content)) {
                final byte[] decoded = Base64.decode((String) content);
                return new ByteArrayContent(decoded);
            } else if (JSONObject.class.isInstance(content)) {
                final JSONObject reference = (JSONObject) content;
                final Object id = reference.get("ref");

                InputStream in = null;
                ByteArrayOutputStream out = null;
                try {
                    in = new BufferedInputStream(registry.get(id));
                    out = new UnsynchronizedByteArrayOutputStream();
                    int b = -1;
                    while ((b = in.read()) != -1) {
                        out.write(b);
                    }
                } finally {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                }

                if (out != null) {
                    return new ByteArrayContent(out.toByteArray());
                }
                return null; // Should never happen
            } else {
                return null; // FIXME
            }
        }

    }

    private final class MultipartContentParser implements MessagingContentParser {

        public MultipartContentParser() {
            super();
        }

        public int getRanking() {
            return 0;
        }

        public boolean handles(final MessagingBodyPart partlyParsedMessage, final Object content) throws MessagingException {
            if (null != partlyParsedMessage.getContentType()) {
                final String primaryType = partlyParsedMessage.getContentType().getPrimaryType();
                return primaryType.equals("multipart");
            }
            return false;
        }

        public MessagingContent parse(final MessagingBodyPart partlyParsedMessage, final Object content, final MessagingInputStreamRegistry registry) throws JSONException, MessagingException, IOException {

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

}
