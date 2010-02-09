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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.commons.codec.binary.Base64InputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.messaging.BinaryContent;
import com.openexchange.messaging.MessagingBodyPart;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingMessageGetSwitch;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.MultipartContent;
import com.openexchange.messaging.SimpleMessagingMessage;
import com.openexchange.messaging.StringContent;

/**
 * A parser to emit JSON representations of MessagingMessages. Note that writing can be customized by registering
 * one or more {@link MessagingHeaderWriter} and one or more {@link MessagingContentWriter}. 
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */

public class MessagingMessageWriter {

    private static final MessagingHeaderWriter MULTI_HEADER_WRITER = new MessagingHeaderWriter() {

        public int getPriority() {
            return 0;
        }

        public boolean handles(Entry<String, Collection<MessagingHeader>> entry) {
            return entry.getValue().size() > 1;
        }

        public String writeKey(Entry<String, Collection<MessagingHeader>> entry) throws JSONException, MessagingException {
            return entry.getKey();
        }

        public Object writeValue(Entry<String, Collection<MessagingHeader>> entry) throws JSONException, MessagingException {
            JSONArray array = new JSONArray();
            for (MessagingHeader header : entry.getValue()) {
                array.put(header.getValue());
            }
            return array;
        }
        
    };
    
    private static final MessagingHeaderWriter SINGLE_HEADER_WRITER = new MessagingHeaderWriter() {

        public int getPriority() {
            return 0;
        }

        public boolean handles(Entry<String, Collection<MessagingHeader>> entry) {
            return entry.getValue().size() <= 1;
        }

        public String writeKey(Entry<String, Collection<MessagingHeader>> entry) throws JSONException, MessagingException {
            return entry.getKey();
        }

        public Object writeValue(Entry<String, Collection<MessagingHeader>> entry) throws JSONException, MessagingException {
            return entry.getValue().iterator().next().getValue();
        }
        
    };
    
    
    public MessagingMessageWriter() {
        headerWriters.add(new ContentTypeWriter());
        headerWriters.add(new AddressHeaderWriter());
    }
    
    private JSONObject write(MessagingPart message) throws JSONException, MessagingException {
        JSONObject messageJSON = new JSONObject();
        
        if(message.getSectionId() != null) {
            messageJSON.put("sectionId", message.getSectionId());
        }
        if(null != message.getHeaders() && ! message.getHeaders().isEmpty()) {
            JSONObject headerJSON = writeHeaders(message.getHeaders());
            
            messageJSON.put("headers", headerJSON);
        }
        
        MessagingContent content = message.getContent();
        
        if(content != null) {
            MessagingContentWriter writer = getWriter(message, content);
            if(writer != null) {
                messageJSON.put("body", writer.write(message, content));
            }
        }
        
        for (MessagingField field : MessagingField.values()) {
            if(null != field.getEquivalentHeader()) {
                Collection<MessagingHeader> header = message.getHeader(field.getEquivalentHeader().toString());
                if(header != null && ! header.isEmpty()) {
                    SimpleEntry<String, Collection<MessagingHeader>> entry = new SimpleEntry<String, Collection<MessagingHeader>>(field.getEquivalentHeader().toString(), header);
                    MessagingHeaderWriter writer = selectWriter(entry);
                    
                    messageJSON.put(field.toString(), writer.writeValue(entry));
                }
            }
        }
        
        return messageJSON;

    }

    private JSONObject writeHeaders(Map<String, Collection<MessagingHeader>> headers) throws MessagingException, JSONException {
        JSONObject headerJSON = new JSONObject();
        
        for (Map.Entry<String, Collection<MessagingHeader>> entry : headers.entrySet()) {

            MessagingHeaderWriter writer = selectWriter(entry);
            headerJSON.put(writer.writeKey(entry), writer.writeValue(entry));
            
        }
        return headerJSON;
    }

    private MessagingContentWriter getWriter(MessagingPart message, MessagingContent content) {
        int priority = 0;
        MessagingContentWriter writer = null;
        for (MessagingContentWriter renderer : contentWriters) {
            if(renderer.handles(message, content)) {
                if(writer == null || priority < renderer.getPriority()) {
                    writer = renderer;
                    priority = renderer.getPriority();
                }
            }
        }
        return writer;
    }

    private MessagingHeaderWriter selectWriter(Entry<String, Collection<MessagingHeader>> entry) {
        int priority = 0;
        MessagingHeaderWriter candidate = null;
        
        for (MessagingHeaderWriter writer : headerWriters) {
            if(writer.handles(entry)) {
                if(candidate == null) {
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

    private MessagingHeaderWriter getDefaultWriter(Entry<String, Collection<MessagingHeader>> entry) {
        return (MULTI_HEADER_WRITER.handles(entry)) ? MULTI_HEADER_WRITER : SINGLE_HEADER_WRITER ;
    }

    private Collection<MessagingHeaderWriter> headerWriters = new ConcurrentLinkedQueue<MessagingHeaderWriter>();

    /**
     * Renders a MessagingMessage in its JSON representation.
     */
    public JSONObject write(MessagingMessage message) throws JSONException, MessagingException {
        JSONObject messageJSON = write((MessagingPart)message);

        if(message.getId() != null) {
            messageJSON.put("id", message.getId());
        }
        
        if (message.getColorLabel() > 0) {
            messageJSON.put("colorLabel", message.getColorLabel());
        }
        messageJSON.put("flags", message.getFlags());

        if (message.getReceivedDate() > 0) {
            messageJSON.put("receivedDate", message.getReceivedDate());
        }

        messageJSON.put("size", message.getSize());
        messageJSON.put("threadLevel", message.getThreadLevel());

        if (null != message.getUserFlags()) {
            JSONArray userFlagsJSON = new JSONArray();
            for (String flag : message.getUserFlags()) {
                userFlagsJSON.put(flag);
            }
            messageJSON.put("user", userFlagsJSON);
        }
        
        messageJSON.put("folder", message.getFolder());
        
        if(message.getPicture() != null) {
            messageJSON.put("picture", message.getPicture());
        }
        return messageJSON;
    }

    private final Collection<MessagingContentWriter> contentWriters = new ConcurrentLinkedQueue<MessagingContentWriter>() {
        {
            add(new StringContentRenderer());
            add(new BinaryContentRenderer());
            add(new MultipartContentRenderer());
        }
    };

    protected static class StringContentRenderer implements MessagingContentWriter {

        public boolean handles(MessagingPart part, MessagingContent content) {
            return StringContent.class.isInstance(content);
        }

        public Object write(MessagingPart part, MessagingContent content) {
            return ((StringContent) content).getData();
        }

        public int getPriority() {
            return 0;
        }

    }

    protected static class BinaryContentRenderer implements MessagingContentWriter {

        public boolean handles(MessagingPart part, MessagingContent content) {
            return BinaryContent.class.isInstance(content);
        }

        public Object write(MessagingPart part, MessagingContent content) throws MessagingException {
            BinaryContent binContent = (BinaryContent) content;
            InputStream is = new BufferedInputStream(new Base64InputStream(binContent.getData(), true, -1, null));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedOutputStream os = new BufferedOutputStream(baos);

            int i = 0;
            try {
                while ((i = is.read()) > 0) {
                    os.write(i);
                }
            } catch (IOException e) {
                // FIXME
                throw new MessagingException(Category.INTERNAL_ERROR, -1, e.getMessage(), e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // FIXME
                    }
                }

                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        // FIXME
                    }
                }
            }

            try {
                return new String(baos.toByteArray(), "ASCII");
            } catch (UnsupportedEncodingException e) {
                // FIXME
                return null;
            }

        }

        public int getPriority() {
            return 0;
        }

    }

    protected class MultipartContentRenderer implements MessagingContentWriter {

        public boolean handles(MessagingPart part, MessagingContent content) {
            return MultipartContent.class.isInstance(content);
        }


        public Object write(MessagingPart part, MessagingContent content) throws MessagingException, JSONException {
            MultipartContent multipart = (MultipartContent) content;
            JSONArray array = new JSONArray();
            for(int i = 0, size = multipart.getCount(); i < size; i++) {
                MessagingBodyPart message = multipart.get(i);
                JSONObject messageJSON = MessagingMessageWriter.this.write(message);
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
    public void addHeaderWriter(MessagingHeaderWriter writer) {
        headerWriters.add(writer);
    }
    
    public void removeHeaderWriter(MessagingHeaderWriter writer) {
        headerWriters.remove(writer);
    }

    /**
     * Registers a custom writer for a {@link MessagingContent}
     * @param contentWriter
     */
    public void addContentWriter(MessagingContentWriter contentWriter) {
        contentWriters.add(contentWriter);
    }
    
    public void removeContentWriter(MessagingContentWriter contentWriter) {
        contentWriters.remove(contentWriter);
    }
    
    /**
     * Renders a message as a list of fields. The fields to be written are given in the MessagingField array. 
     * Individual fields are rendered exactly as in the JSONObject representation using custom header writers and content writers.
     */
    public JSONArray writeFields(MessagingMessage message, MessagingField[] fields) throws MessagingException, JSONException {
        JSONArray fieldJSON = new JSONArray();
        
        MessagingMessageGetSwitch switcher = new MessagingMessageGetSwitch();
        
        for (MessagingField messagingField : fields) {
            Object value = messagingField.doSwitch(switcher, message);
            if(value == null) {
                
            }else if(messagingField == MessagingField.HEADERS) {
                value = writeHeaders(message.getHeaders());
            }else if(messagingField.getEquivalentHeader() != null) {
                Entry<String, Collection<MessagingHeader>> entry = new SimpleEntry<String, Collection<MessagingHeader>>(messagingField.getEquivalentHeader().toString(), (Collection<MessagingHeader>) value);
                MessagingHeaderWriter writer = selectWriter(entry);
                value = writer.writeValue(entry);
            } else if (MessagingContent.class.isInstance(value)) {
                MessagingContent content = (MessagingContent) value;
                MessagingContentWriter writer = getWriter(message, content);
                if(writer != null) {
                    value = writer.write(message, content);
                }

            }
            
            fieldJSON.put( value );
        }
        return fieldJSON;
    }
    
    private static final class SimpleEntry<T1, T2> implements Map.Entry<T1, T2> {

        private T1 key;
        private T2 value;
        
        public SimpleEntry(T1 key, T2 value) {
            this.key = key;
            this.value = value;
        }

        public T1 getKey() {
            return key;
        }

        public T2 getValue() {
            return value;
        }

        public T2 setValue(T2 value) {
            T2 oldValue = this.value;
            this.value = value;
            return oldValue;
        }
        
    }

}
