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
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.commons.codec.binary.Base64InputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.mail.text.HTMLProcessing;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.messaging.BinaryContent;
import com.openexchange.messaging.DateMessagingHeader;
import com.openexchange.messaging.MessagingBodyPart;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingHeader.HeaderType;
import com.openexchange.messaging.MessagingHeader.KnownHeader;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingMessageGetSwitch;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.MultipartContent;
import com.openexchange.messaging.StringContent;
import com.openexchange.messaging.generic.Utility;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * A parser to emit JSON representations of MessagingMessages. Note that writing can be customized by registering one or more
 * {@link MessagingHeaderWriter} and one or more {@link MessagingContentWriter}.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MessagingMessageWriter {

    private static final class SimpleEntry<K, V> implements Map.Entry<K, V> {

        public static <K, V> SimpleEntry<K, V> valueOf(final K key, final V value) {
            return new SimpleEntry<K, V>(key, value);
        }

        private final K key;

        private V value;

        private SimpleEntry(final K key, final V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(final V value) {
            final V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

    }

    /**
     * The default {@link MessagingHeaderWriter header writer} for multiple headers.
     */
    private static final MessagingHeaderWriter MULTI_HEADER_WRITER = new MessagingHeaderWriter() {

        public int getRanking() {
            return 0;
        }

        public boolean handles(final Entry<String, Collection<MessagingHeader>> entry) {
            return entry.getValue().size() > 1;
        }

        public String writeKey(final Entry<String, Collection<MessagingHeader>> entry) throws JSONException, MessagingException {
            return entry.getKey();
        }

        public Object writeValue(final Entry<String, Collection<MessagingHeader>> entry, final ServerSession session) throws JSONException, MessagingException {
            final JSONArray array = new JSONArray();
            for (final MessagingHeader header : entry.getValue()) {
                array.put(getHeaderValue(header, session));
            }
            return array;
        }

    };

    /**
     * The default {@link MessagingHeaderWriter header writer} for single headers.
     */
    private static final MessagingHeaderWriter SINGLE_HEADER_WRITER = new MessagingHeaderWriter() {

        public int getRanking() {
            return 0;
        }

        public boolean handles(final Entry<String, Collection<MessagingHeader>> entry) {
            return entry.getValue().size() <= 1;
        }

        public String writeKey(final Entry<String, Collection<MessagingHeader>> entry) throws JSONException, MessagingException {
            return entry.getKey();
        }

        public Object writeValue(final Entry<String, Collection<MessagingHeader>> entry, final ServerSession session) throws JSONException, MessagingException {
            final Collection<MessagingHeader> collection = entry.getValue();
            if (null == collection || collection.isEmpty()) {
                return ""; // Empty string
            }
            return getHeaderValue(collection.iterator().next(), session);
        }

    };

    /**
     * Gets the value of specified header.
     * 
     * @param header The header
     * @param session The session
     * @return The header value
     */
    static String getHeaderValue(final MessagingHeader header, final ServerSession session) {
        if (HeaderType.DATE.equals(header.getHeaderType())) {
            final SimpleDateFormat mailDateFormat = Utility.getDefaultMailDateFormat();
            synchronized (mailDateFormat) {
                return mailDateFormat.format(new Date(addTimeZoneOffset(
                    ((DateMessagingHeader) header).getTime(),
                    session.getUser().getTimeZone())));
            }
        }
        return header.getValue();
    }

    /**
     * Adds time zone's offset to specified UTC time.
     * 
     * @param date The UTC time
     * @param timeZone The time zone
     * @return The UTC time with offset applied
     */
    static long addTimeZoneOffset(final long date, final String timeZone) {
        return Utility.addTimeZoneOffset(date, timeZone);
    }

    protected static class StringContentRenderer implements MessagingContentWriter {

        public boolean handles(final MessagingPart part, final MessagingContent content) {
            return StringContent.class.isInstance(content);
        }

        public Object write(final MessagingPart part, final MessagingContent content, final ServerSession session, final DisplayMode mode) {
            final String data = ((StringContent) content).getData();
            if (null == session || null == mode) {
                return data;
            }
            return HTMLProcessing.formatTextForDisplay(data, session.getUserSettingMail(), mode);
        }

        public int getRanking() {
            return 0;
        }

    }

    protected static class BinaryContentRenderer implements MessagingContentWriter {

        public boolean handles(final MessagingPart part, final MessagingContent content) {
            return BinaryContent.class.isInstance(content);
        }

        public Object write(final MessagingPart part, final MessagingContent content, final ServerSession session, final DisplayMode mode) throws MessagingException {
            final BinaryContent binContent = (BinaryContent) content;
            final InputStream is = new BufferedInputStream(new Base64InputStream(binContent.getData(), true, -1, null));
            final ByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream();
            int i = 0;
            try {
                while ((i = is.read()) > 0) {
                    baos.write(i);
                }
            } catch (final IOException e) {
                throw new MessagingException(Category.INTERNAL_ERROR, -1, e.getMessage(), e);
            } finally {
                try {
                    is.close();
                } catch (final IOException e) {
                    org.apache.commons.logging.LogFactory.getLog(MessagingMessageWriter.class).error("Closing input stream failed.", e);
                }
            }
            try {
                return new String(baos.toByteArray(), "US-ASCII");
            } catch (final UnsupportedEncodingException e) {
                org.apache.commons.logging.LogFactory.getLog(MessagingMessageWriter.class).error("Unsupported encoding: " + e.getMessage(), e);
                return null;
            }

        }

        public int getRanking() {
            return 0;
        }

    }

    protected static class MultipartContentRenderer implements MessagingContentWriter {

        private final MessagingMessageWriter writer;

        protected MultipartContentRenderer(final MessagingMessageWriter writer) {
            super();
            this.writer = writer;
        }

        public boolean handles(final MessagingPart part, final MessagingContent content) {
            return MultipartContent.class.isInstance(content);
        }

        public Object write(final MessagingPart part, final MessagingContent content, final ServerSession session, final DisplayMode mode) throws MessagingException, JSONException {
            final MultipartContent multipart = (MultipartContent) content;
            final JSONArray array = new JSONArray();
            for (int i = 0, size = multipart.getCount(); i < size; i++) {
                final MessagingBodyPart bodyPart = multipart.get(i);
                final JSONObject partJSON = writer.write(bodyPart, session, mode);
                if (null != bodyPart.getDisposition()) {
                    partJSON.put("disposition", bodyPart.getDisposition());
                }
                if (null != bodyPart.getFileName()) {
                    partJSON.put("fileName", bodyPart.getFileName());
                }
                array.put(partJSON);

            }
            return array;
        }

        public int getRanking() {
            return 0;
        }
    }

    /*-
     * ------------------------- Member stuff -------------------------
     */

    /**
     * The collection of {@link MessagingHeaderWriter header writers}.
     */
    private final Collection<MessagingHeaderWriter> headerWriters;

    /**
     * The collection of {@link MessagingContentWriter content writers}.
     */
    private final Collection<MessagingContentWriter> contentWriters;

    /**
     * Initializes a new {@link MessagingMessageWriter}.
     */
    public MessagingMessageWriter() {
        super();
        // Header writers
        headerWriters = new ConcurrentLinkedQueue<MessagingHeaderWriter>();
        headerWriters.add(new ContentTypeWriter());
        headerWriters.add(new ContentDispositionWriter());
        headerWriters.add(new AddressHeaderWriter());
        // Content writers
        contentWriters = new ConcurrentLinkedQueue<MessagingContentWriter>();
        contentWriters.add(new StringContentRenderer());
        contentWriters.add(new BinaryContentRenderer());
        contentWriters.add(new ReferenceContentRenderer());
        contentWriters.add(new MultipartContentRenderer(this));
    }

    /**
     * Writes specified part as a JSON object.
     * 
     * @param part The message part
     * @param session The session providing user information
     * @param mode The display mode
     * @return The resulting JSON object
     * @throws JSONException If a JSON error occurs
     * @throws MessagingException If a messaging error occurs
     */
    JSONObject write(final MessagingPart part, final ServerSession session, final DisplayMode mode) throws JSONException, MessagingException {
        final JSONObject messageJSON = new JSONObject();

        messageJSON.putOpt("sectionId", part.getSectionId());

        {
            final Map<String, Collection<MessagingHeader>> headers = part.getHeaders();
            if (null != headers && !headers.isEmpty()) {
                final JSONObject headerJSON = writeHeaders(headers, session);
                messageJSON.put("headers", headerJSON);
            }
        }

        {
            final MessagingContent content = part.getContent();
            if (content != null) {
                final MessagingContentWriter writer = optContentWriter(part, content);
                if (writer != null) {
                    messageJSON.put("body", writer.write(part, content, session, mode));
                }
            }
        }

        for (final MessagingField field : MessagingField.values()) {
            final KnownHeader knownHeader = field.getEquivalentHeader();
            if (null != knownHeader) {
                final Collection<MessagingHeader> header = part.getHeader(knownHeader.toString());
                if (header != null && !header.isEmpty()) {
                    final SimpleEntry<String, Collection<MessagingHeader>> entry = SimpleEntry.valueOf(knownHeader.toString(), header);
                    final MessagingHeaderWriter writer = getHeaderWriter(entry);
                    messageJSON.put(field.toString(), writer.writeValue(entry, session));
                }
            }
        }

        return messageJSON;
    }

    /**
     * Writes specified headers to its JSON representation.
     * 
     * @param headers The headers
     * @param session The session
     * @return The resulting JSON representation
     * @throws MessagingException If a messaging error occurs
     * @throws JSONException If a JSON error occurs
     */
    JSONObject writeHeaders(final Map<String, Collection<MessagingHeader>> headers, final ServerSession session) throws MessagingException, JSONException {
        final JSONObject headerJSON = new JSONObject();
        for (final Map.Entry<String, Collection<MessagingHeader>> entry : headers.entrySet()) {
            final MessagingHeaderWriter writer = getHeaderWriter(entry);
            headerJSON.put(writer.writeKey(entry), writer.writeValue(entry, session));

        }
        return headerJSON;
    }

    /**
     * Gets the appropriate content writer for specified message part and content.
     * 
     * @param part The message part
     * @param content The content
     * @return The appropriate content writer or <code>null</code> if none found
     */
    MessagingContentWriter optContentWriter(final MessagingPart part, final MessagingContent content) {
        int ranking = 0;
        MessagingContentWriter writer = null;
        /*
         * Get content writer with highest ranking
         */
        for (final MessagingContentWriter renderer : contentWriters) {
            if (renderer.handles(part, content) && (writer == null || ranking < renderer.getRanking())) {
                writer = renderer;
                ranking = renderer.getRanking();
            }
        }
        return writer;
    }

    /**
     * Gets the appropriate header writer for specified header name and value.
     * 
     * @param entry The entry providing header name and value
     * @return The appropriate header writer (not <code>null</code>)
     */
    private MessagingHeaderWriter getHeaderWriter(final Entry<String, Collection<MessagingHeader>> entry) {
        int ranking = 0;
        MessagingHeaderWriter candidate = null;
        /*
         * Get header writer with highest ranking
         */
        for (final MessagingHeaderWriter writer : headerWriters) {
            if (writer.handles(entry) && ((candidate == null) || (ranking < writer.getRanking()))) {
                candidate = writer;
                ranking = writer.getRanking();
            }
        }
        return (candidate == null) ? getDefaultWriter(entry) : candidate;
    }

    private static MessagingHeaderWriter getDefaultWriter(final Entry<String, Collection<MessagingHeader>> entry) {
        return (MULTI_HEADER_WRITER.handles(entry)) ? MULTI_HEADER_WRITER : SINGLE_HEADER_WRITER;
    }

    /**
     * Renders a MessagingMessage in its JSON representation.
     * 
     * @param string
     */
    public JSONObject write(final MessagingMessage message, final String folderPrefix, final ServerSession session, final DisplayMode mode) throws JSONException, MessagingException {
        final JSONObject messageJSON = write(message, session, mode);

        {
            final String id = message.getId();
            if (id != null) {
                messageJSON.put("id", id);
            }
        }
        {
            final int colorLabel = message.getColorLabel();
            if (colorLabel > 0) {
                messageJSON.put("colorLabel", colorLabel);
            }
        }

        messageJSON.put("flags", message.getFlags());

        {
            final long receivedDate = message.getReceivedDate();
            if (receivedDate > 0) {
                final long dateWithOffset = addTimeZoneOffset(receivedDate, session.getUser().getTimeZone());
                
                System.out.println("date: " + receivedDate + ", date-with-offset: " + dateWithOffset);
                
                messageJSON.put("receivedDate", dateWithOffset);
            }
        }

        {
            final long size = message.getSize();
            if (size >= 0) {
                messageJSON.put("size", size);
            }
        }

        messageJSON.put("threadLevel", message.getThreadLevel());

        {
            final Collection<String> userFlags = message.getUserFlags();
            if (null != userFlags && !userFlags.isEmpty()) {
                final JSONArray userFlagsJSON = new JSONArray();
                for (final String flag : userFlags) {
                    userFlagsJSON.put(flag);
                }
                messageJSON.put("user", userFlagsJSON);
            }
        }

        messageJSON.put("folder", new StringBuilder(folderPrefix).append('/').append(message.getFolder()).toString());

        {
            final String picture = message.getPicture();
            if (picture != null) {
                messageJSON.put("picture", picture);
            }
        }

        {
            final String url = message.getUrl();
            if (url != null) {
                messageJSON.put("url", url);
            }
        }

        return messageJSON;
    }

    /**
     * Registers a custom writer for a header
     */
    public void addHeaderWriter(final MessagingHeaderWriter writer) {
        headerWriters.add(writer);
    }

    public void removeHeaderWriter(final MessagingHeaderWriter writer) {
        headerWriters.remove(writer);
    }

    /**
     * Registers a custom writer for a {@link MessagingContent}
     * 
     * @param contentWriter
     */
    public void addContentWriter(final MessagingContentWriter contentWriter) {
        contentWriters.add(contentWriter);
    }

    public void removeContentWriter(final MessagingContentWriter contentWriter) {
        contentWriters.remove(contentWriter);
    }

    private static interface JSONFieldHandler {
        
        Object handle(Object value, MessagingMessage message, String folderPrefix, ServerSession session, DisplayMode mode, MessagingMessageWriter messageWriter) throws MessagingException, JSONException;
    }

    private static final EnumMap<MessagingField, JSONFieldHandler> JSON_FIELD_HANDLERS;

    static {
        final EnumMap<MessagingField, JSONFieldHandler> map = new EnumMap<MessagingField, JSONFieldHandler>(MessagingField.class);
        map.put(MessagingField.HEADERS, new JSONFieldHandler() {
            
            public Object handle(final Object value, final MessagingMessage message, final String folderPrefix, final ServerSession session, final DisplayMode mode, final MessagingMessageWriter messageWriter) throws MessagingException, JSONException {
                return messageWriter.writeHeaders(message.getHeaders(), session);
            }
        });
        map.put(MessagingField.BODY, new JSONFieldHandler() {
            
            public Object handle(final Object value, final MessagingMessage message, final String folderPrefix, final ServerSession session, final DisplayMode mode, final MessagingMessageWriter messageWriter) throws MessagingException, JSONException {
                final MessagingContent content = (MessagingContent) value;
                final MessagingContentWriter writer = messageWriter.optContentWriter(message, content);
                if (writer != null) {
                    return writer.write(message, content, session, mode);
                }
                return value;
            }
        });
        map.put(MessagingField.FOLDER_ID, new JSONFieldHandler() {
            
            public Object handle(final Object value, final MessagingMessage message, final String folderPrefix, final ServerSession session, final DisplayMode mode, final MessagingMessageWriter messageWriter) throws MessagingException, JSONException {
                return new StringBuilder(folderPrefix).append('/').append(value).toString();
            }
        });
        /*
         * Date fields
         */
        final JSONFieldHandler dateHandler = new JSONFieldHandler() {
            
            public Object handle(final Object value, final MessagingMessage message, final String folderPrefix, final ServerSession session, final DisplayMode mode, final MessagingMessageWriter messageWriter) throws MessagingException, JSONException {
                final long date = ((Long) value).longValue();
                if (date < 0) {
                    return null;
                }
                return Long.valueOf(addTimeZoneOffset(date, session.getUser().getTimeZone()));
            }
        };
        map.put(MessagingField.RECEIVED_DATE, dateHandler);
        map.put(MessagingField.SENT_DATE, dateHandler);
        /*
         * Number fields
         */
        final JSONFieldHandler numberHandler = new JSONFieldHandler() {

            private final Long longNum = Long.valueOf(-1);

            private final Integer intNum = Integer.valueOf(-1);
            
            public Object handle(final Object value, final MessagingMessage message, final String folderPrefix, final ServerSession session, final DisplayMode mode, final MessagingMessageWriter messageWriter) throws MessagingException, JSONException {
                if (longNum.equals(value) || intNum.equals(value)) {
                    return null;
                }
                return value;
            }
        };
        map.put(MessagingField.THREAD_LEVEL, numberHandler);
        map.put(MessagingField.SIZE, numberHandler);
        map.put(MessagingField.PRIORITY, numberHandler);
        /*
         * Set constant
         */
        JSON_FIELD_HANDLERS = map;
    }

    /**
     * Renders a message as a list of fields. The fields to be written are given in the MessagingField array. Individual fields are rendered
     * exactly as in the JSONObject representation using custom header writers and content writers.
     * 
     * @param folderPrefix
     */
    public JSONArray writeFields(final MessagingMessage message, final MessagingField[] fields, final String folderPrefix, final ServerSession session, final DisplayMode mode) throws MessagingException, JSONException {
        final JSONArray fieldJSON = new JSONArray();

        final MessagingMessageGetSwitch switcher = new MessagingMessageGetSwitch();

        for (final MessagingField messagingField : fields) {
            Object value = messagingField.doSwitch(switcher, message);
            if (value == null) {
                /*
                 * Nothing to do...
                 */
                fieldJSON.put(value);
            } else {
                /*
                 * Get appropriate handler
                 */
                final JSONFieldHandler handler = JSON_FIELD_HANDLERS.get(messagingField);
                if (null == handler) {
                    final KnownHeader header = messagingField.getEquivalentHeader();
                    if (header != null) {
                        @SuppressWarnings("unchecked") final Collection<MessagingHeader> collection = (Collection<MessagingHeader>) value;
                        final Entry<String, Collection<MessagingHeader>> entry = SimpleEntry.valueOf(header.toString(), collection);
                        final MessagingHeaderWriter writer = getHeaderWriter(entry);
                        value = writer.writeValue(entry, session);
                    } else if (MessagingContent.class.isInstance(value)) {
                        final MessagingContent content = (MessagingContent) value;
                        final MessagingContentWriter writer = optContentWriter(message, content);
                        if (writer != null) {
                            value = writer.write(message, content, session, mode);
                        }
                    }
                    /*
                     * Put value to JSON
                     */
                    fieldJSON.put(value);
                } else {
                    fieldJSON.put(handler.handle(value, message, folderPrefix, session, mode, this));
                }
            }
        }
        return fieldJSON;
    }

}
