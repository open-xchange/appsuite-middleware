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
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
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

/**
 * A parser to emit JSON representations of MessagingMessages. Note that writing can be customized by registering one or more
 * {@link MessagingHeaderWriter} and one or more {@link MessagingContentWriter}.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MessagingMessageWriter {

    /**
     * The default {@link MessagingHeaderWriter header writer} for multiple headers.
     */
    private static final MessagingHeaderWriter MULTI_HEADER_WRITER = new MessagingHeaderWriter() {

        public int getPriority() {
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

        public int getPriority() {
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
    private static long addTimeZoneOffset(final long date, final String timeZone) {
        return Utility.addTimeZoneOffset(date, timeZone);
    }

    /**
     * Initializes a new {@link MessagingMessageWriter}.
     */
    public MessagingMessageWriter() {
        headerWriters.add(new ContentTypeWriter());
        headerWriters.add(new AddressHeaderWriter());
    }

    private JSONObject write(final MessagingPart message, final ServerSession session, final DisplayMode mode) throws JSONException, MessagingException {
        final JSONObject messageJSON = new JSONObject();

        if (message.getSectionId() != null) {
            messageJSON.put("sectionId", message.getSectionId());
        }
        if (null != message.getHeaders() && !message.getHeaders().isEmpty()) {
            final JSONObject headerJSON = writeHeaders(message.getHeaders(), session);

            messageJSON.put("headers", headerJSON);
        }

        final MessagingContent content = message.getContent();

        if (content != null) {
            final MessagingContentWriter writer = getWriter(message, content);
            if (writer != null) {
                messageJSON.put("body", writer.write(message, content, session, mode));
            }
        }

        for (final MessagingField field : MessagingField.values()) {
            final KnownHeader knownHeader = field.getEquivalentHeader();
            if (null != knownHeader) {
                final Collection<MessagingHeader> header = message.getHeader(knownHeader.toString());
                if (header != null && !header.isEmpty()) {
                    final SimpleEntry<String, Collection<MessagingHeader>> entry = new SimpleEntry<String, Collection<MessagingHeader>>(
                        knownHeader.toString(),
                        header);
                    final MessagingHeaderWriter writer = selectWriter(entry);

                    messageJSON.put(field.toString(), writer.writeValue(entry, session));
                }
            }
        }

        return messageJSON;

    }

    private JSONObject writeHeaders(final Map<String, Collection<MessagingHeader>> headers, final ServerSession session) throws MessagingException, JSONException {
        final JSONObject headerJSON = new JSONObject();

        for (final Map.Entry<String, Collection<MessagingHeader>> entry : headers.entrySet()) {

            final MessagingHeaderWriter writer = selectWriter(entry);
            headerJSON.put(writer.writeKey(entry), writer.writeValue(entry, session));

        }
        return headerJSON;
    }

    private MessagingContentWriter getWriter(final MessagingPart message, final MessagingContent content) {
        int priority = 0;
        MessagingContentWriter writer = null;
        for (final MessagingContentWriter renderer : contentWriters) {
            if (renderer.handles(message, content)) {
                if (writer == null || priority < renderer.getPriority()) {
                    writer = renderer;
                    priority = renderer.getPriority();
                }
            }
        }
        return writer;
    }

    private MessagingHeaderWriter selectWriter(final Entry<String, Collection<MessagingHeader>> entry) {
        int priority = 0;
        MessagingHeaderWriter candidate = null;

        for (final MessagingHeaderWriter writer : headerWriters) {
            if (writer.handles(entry)) {
                if (candidate == null) {
                    candidate = writer;
                    priority = candidate.getPriority();
                } else if (priority < candidate.getPriority()) {
                    candidate = writer;
                    priority = candidate.getPriority();
                }
            }
        }

        return (candidate != null) ? candidate : getDefaultWriter(entry);
    }

    private MessagingHeaderWriter getDefaultWriter(final Entry<String, Collection<MessagingHeader>> entry) {
        return (MULTI_HEADER_WRITER.handles(entry)) ? MULTI_HEADER_WRITER : SINGLE_HEADER_WRITER;
    }

    private final Collection<MessagingHeaderWriter> headerWriters = new ConcurrentLinkedQueue<MessagingHeaderWriter>();

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
                messageJSON.put("receivedDate", addTimeZoneOffset(receivedDate, session.getUser().getTimeZone()));
            }
        }

        messageJSON.put("size", message.getSize());
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
                messageJSON.putOpt("url", url);
            }
        }
        return messageJSON;
    }

    private final Collection<MessagingContentWriter> contentWriters = new ConcurrentLinkedQueue<MessagingContentWriter>() {

        {
            add(new StringContentRenderer());
            add(new BinaryContentRenderer());
            add(new ReferenceContentRenderer());
            add(new MultipartContentRenderer());
        }
    };

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

        public int getPriority() {
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
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final BufferedOutputStream os = new BufferedOutputStream(baos);

            int i = 0;
            try {
                while ((i = is.read()) > 0) {
                    os.write(i);
                }
            } catch (final IOException e) {
                // FIXME
                throw new MessagingException(Category.INTERNAL_ERROR, -1, e.getMessage(), e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (final IOException e) {
                        // FIXME
                    }
                }

                if (os != null) {
                    try {
                        os.close();
                    } catch (final IOException e) {
                        // FIXME
                    }
                }
            }

            try {
                return new String(baos.toByteArray(), "ASCII");
            } catch (final UnsupportedEncodingException e) {
                // FIXME
                return null;
            }

        }

        public int getPriority() {
            return 0;
        }

    }

    protected class MultipartContentRenderer implements MessagingContentWriter {

        public boolean handles(final MessagingPart part, final MessagingContent content) {
            return MultipartContent.class.isInstance(content);
        }

        public Object write(final MessagingPart part, final MessagingContent content, final ServerSession session, final DisplayMode mode) throws MessagingException, JSONException {
            final MultipartContent multipart = (MultipartContent) content;
            final JSONArray array = new JSONArray();
            for (int i = 0, size = multipart.getCount(); i < size; i++) {
                final MessagingBodyPart message = multipart.get(i);
                final JSONObject messageJSON = MessagingMessageWriter.this.write(message, session, mode);
                if (null != message.getDisposition()) {
                    messageJSON.put("disposition", message.getDisposition());
                }
                if (null != message.getFileName()) {
                    messageJSON.put("fileName", message.getFileName());
                }
                array.put(messageJSON);

            }
            return array;
        }

        public int getPriority() {
            return 0;
        }
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
            Object value = transform(messagingField, messagingField.doSwitch(switcher, message));
            if (value == null) {

            } else if (messagingField == MessagingField.HEADERS) {
                value = writeHeaders(message.getHeaders(), session);
            } else if (messagingField.getEquivalentHeader() != null) {
                final Entry<String, Collection<MessagingHeader>> entry = new SimpleEntry<String, Collection<MessagingHeader>>(
                    messagingField.getEquivalentHeader().toString(),
                    (Collection<MessagingHeader>) value);
                final MessagingHeaderWriter writer = selectWriter(entry);
                value = writer.writeValue(entry, session);
            } else if (MessagingContent.class.isInstance(value)) {
                final MessagingContent content = (MessagingContent) value;
                final MessagingContentWriter writer = getWriter(message, content);
                if (writer != null) {
                    value = writer.write(message, content, session, mode);
                }

            } else if (MessagingField.FOLDER_ID == messagingField) {
                value = folderPrefix + "/" + value;
            }

            fieldJSON.put(value);
        }
        return fieldJSON;
    }

    private Object transform(final MessagingField messagingField, final Object value) {
        switch (messagingField) {
        case SIZE:
        case PRIORITY:
        case RECEIVED_DATE:
        case SENT_DATE:
        case THREAD_LEVEL: {
            if (Long.class.isInstance(value) && Long.valueOf(-1) == value) {
                return null;
            } else if (Integer.class.isInstance(value) && Integer.valueOf(-1) == value) {
                return null;
            }
        }
        }
        return value;
    }

    private static final class SimpleEntry<T1, T2> implements Map.Entry<T1, T2> {

        private final T1 key;

        private T2 value;

        public SimpleEntry(final T1 key, final T2 value) {
            this.key = key;
            this.value = value;
        }

        public T1 getKey() {
            return key;
        }

        public T2 getValue() {
            return value;
        }

        public T2 setValue(final T2 value) {
            final T2 oldValue = this.value;
            this.value = value;
            return oldValue;
        }

    }

}
