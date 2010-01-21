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
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.MultipartContent;
import com.openexchange.messaging.StringContent;

/**
 * {@link MessagingMessageWriter}
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
    }
    
    private JSONObject write(MessagingPart message) throws JSONException, MessagingException {
        JSONObject messageJSON = new JSONObject();
        
        messageJSON.put("id", message.getId());
        if(null != message.getHeaders() && ! message.getHeaders().isEmpty()) {
            JSONObject headerJSON = new JSONObject();
            
            for (Map.Entry<String, Collection<MessagingHeader>> entry : message.getHeaders().entrySet()) {

                MessagingHeaderWriter writer = selectWriter(entry);
                headerJSON.put(writer.writeKey(entry), writer.writeValue(entry));
                
            }
            
            messageJSON.put("headers", headerJSON);
        }
        
        MessagingContent content = message.getContent();
        
        if(content != null) {
            MessagingContentWriter writer = null;
            int priority = 0;
            for (MessagingContentWriter renderer : contentWriters) {
                if(renderer.handles(message, content)) {
                    if(writer == null) {
                        writer = renderer;
                        priority = renderer.getPriority();
                    } else if (priority < renderer.getPriority()){
                        writer = renderer;
                        priority = renderer.getPriority();
                    }
                }
            }
            if(writer != null) {
                messageJSON.put("content", writer.write(message, content));
            }
        }
        
        return messageJSON;

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

    public JSONObject write(MessagingMessage message) throws JSONException, MessagingException {
        JSONObject messageJSON = write((MessagingPart)message);

        if (message.getColorLabel() > 0) {
            messageJSON.put("colorLabel", message.getColorLabel());
        }
        messageJSON.put("flags", message.getFlags());

        if (message.getReceivedDate() > 0) {
            messageJSON.put("received_date", message.getReceivedDate());
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
        return messageJSON;
    }

    private final Collection<MessagingContentWriter> contentWriters = new ConcurrentLinkedQueue<MessagingContentWriter>() {
        {
            add(new StringContentRenderer());
            add(new BinaryContentRenderer());
            add(new MultipartContentRenderer());
        }
    };

    protected static interface MessagingContentWriter {

        public boolean handles(MessagingPart part, MessagingContent content);

        public int getPriority();

        public Object write(MessagingPart part, MessagingContent content) throws MessagingException, JSONException;
    }

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

    public void addHeaderWriter(MessagingHeaderWriter writer) {
        headerWriters.add(writer);
    }

    public void addContentWriter(MessagingContentWriter contentWriter) {
        contentWriters.add(contentWriter);
    }

}
