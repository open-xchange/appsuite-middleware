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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import com.openexchange.messaging.ContentType;
import com.openexchange.messaging.MessagingBodyPart;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingPartArrayContent;
import com.openexchange.messaging.MultipartContent;
import com.openexchange.messaging.StringContent;
import com.openexchange.messaging.StringMessageHeader;
import com.openexchange.messaging.generic.internet.MimeContentType;
import com.openexchange.messaging.json.MessagingMessageParserTest.InvertedHeaderParser;
import com.openexchange.tools.encoding.Base64;


/**
 * {@link MessagingMessageParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MessagingMessageParser {

    private Collection<MessagingHeaderParser> headerParsers = new ConcurrentLinkedQueue<MessagingHeaderParser>();
    private Collection<MessagingContentParser> contentParsers = new ConcurrentLinkedQueue<MessagingContentParser>();
    
    MessagingMessageParser() {
        headerParsers.add(new ContentTypeParser());
        headerParsers.add(new MultiStringParser());
        
        contentParsers.add(new StringContentParser());
        contentParsers.add(new BinaryContentParser());
        contentParsers.add(new MultipartContentParser());
        
    }
    

    public MessagingMessage parse(JSONObject messageJSON, MessagingInputStreamRegistry registry) throws JSONException, MessagingException, IOException {
        JSONMessagingMessage message = new JSONMessagingMessage(messageJSON, registry);
        
        return message;
    }
    
    public void addHeaderParser(MessagingHeaderParser parser) {
        headerParsers.add(parser);
    }
    
    public void addContentParser(MessagingContentParser parser) {
        contentParsers.add(parser);
    }

    
    private final class JSONMessagingMessage implements MessagingMessage, MessagingBodyPart{
        private int colorLabel;
        private String id;
        private List<String> userFlags;
        private int flags;
        private long receivedDate;
        private long size;
        private int threadLevel;
        private String folder;
        private Map<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>();
        private MessagingContent content;
        private MultipartContent parent;

        public JSONMessagingMessage(JSONObject messageJSON, MessagingInputStreamRegistry registry) throws JSONException, MessagingException, IOException {
            super();
            if(messageJSON.has("colorLabel")) {
                this.colorLabel = messageJSON.getInt("colorLabel");
            }
            
            if(messageJSON.has("id")) {
                this.id = messageJSON.getString("id");
            }
            
            if(messageJSON.has("flags")) {
                this.flags = messageJSON.getInt("flags");
            }
            
            if(messageJSON.has("userFlags")) {
                JSONArray array = messageJSON.getJSONArray("userFlags");
                this.userFlags = new ArrayList<String>(array.length());
                for(int i = 0, size = array.length(); i < size; i++) {
                    userFlags.add(array.getString(i));
                }
            }
            
            if(messageJSON.has("receivedDate")) {
                this.receivedDate = messageJSON.getLong("receivedDate");
            }
            
            if(messageJSON.has("size")) {
                this.size = messageJSON.getLong("size");
            }
            
            if(messageJSON.has("threadLevel")) {
                this.threadLevel = messageJSON.getInt("threadLevel");
            }
            
            if(messageJSON.has("folder")) {
                this.folder = messageJSON.getString("folder");
            }
            
            if(messageJSON.has("headers")) {
                setHeaders( messageJSON.getJSONObject("headers") );
            }
            
            if(messageJSON.has("content")) {
                setContent( messageJSON.get("content"), registry);
            }
        }


        private void setHeaders(JSONObject object) throws JSONException, MessagingException {
            for (String key : object.keySet()) {
                Object value = object.get(key);
                
                MessagingHeaderParser candidate = null;
                int priority = 0;
                for(MessagingHeaderParser parser : headerParsers) {
                    if(parser.handles(key, value)) {
                        if(candidate == null || priority < parser.getPriority()) {
                            candidate = parser;
                            priority = parser.getPriority();
                        }
                    }
                }
                if(candidate != null) {
                    candidate.parseAndAdd(headers, key, value);
                } else {
                    StringMessageHeader header = new StringMessageHeader(key, value.toString());
                    headers.put(key, Arrays.asList((MessagingHeader)header));
                }
            }
        }

        private void setContent(Object content, MessagingInputStreamRegistry registry) throws MessagingException, JSONException, IOException {
            MessagingContentParser candidate = null;
            int priority = 0;
            
            for (MessagingContentParser parser : contentParsers) {
                if(parser.handles(this, content)) {
                    if(candidate == null || priority < parser.getPriority()) {
                        candidate = parser;
                        priority = parser.getPriority();
                    }
                }
            }
            if(candidate != null) {
                this.content = candidate.parse(this, content, registry);
            }
        }

        
        public int getColorLabel() {
            return colorLabel;
        }

        public int getFlags() {
            return flags;
        }

        public String getFolder() {
            return folder;
        }

        public long getReceivedDate() {
            return receivedDate;
        }

        public long getSize() {
            return size;
        }

        public int getThreadLevel() {
            return threadLevel;
        }

        public Collection<String> getUserFlags() {
            return userFlags;
        }

        public MessagingContent getContent() throws MessagingException {
            return content;
        }

        public String getDisposition() throws MessagingException {
            // TODO Auto-generated method stub
            return null;
        }

        public String getFileName() throws MessagingException {
            // TODO Auto-generated method stub
            return null;
        }

        public Collection<MessagingHeader> getHeader(String name) {
            return headers.get(name);
        }

        public Map<String, Collection<MessagingHeader>> getHeaders() {
            return headers;
        }

        public String getId() {
            return id;
        }

        public void writeTo(OutputStream os) throws IOException, MessagingException {
            throw new UnsupportedOperationException();
        }

        public ContentType getContentType() throws MessagingException {
            if(headers.containsKey(MimeContentType.getContentTypeName())) {
                return (ContentType) headers.get(MimeContentType.getContentTypeName()).iterator().next();
            }
            return null;
        }


        public MultipartContent getParent() throws MessagingException {
            return parent;
        }
        
        public void setParent(MultipartContent parent) {
            this.parent = parent;
        }

        public MessagingHeader getFirstHeader(String name) throws MessagingException {
            Collection<MessagingHeader> collection = getHeader(name);
            if(collection != null && ! collection.isEmpty()) {
                return collection.iterator().next();
            }
            return null;
        }
        
    }
    
    private static final class MultiStringParser implements MessagingHeaderParser {

        public int getPriority() {
            return 0;
        }

        public boolean handles(String key, Object value) {
            return JSONArray.class.isInstance(value);
        }

        public void parseAndAdd(Map<String, Collection<MessagingHeader>> headers, String key, Object value) throws JSONException {
            JSONArray values = (JSONArray) value;
            LinkedList<MessagingHeader> valueList = new LinkedList<MessagingHeader>();
            for(int i = 0, size = values.length(); i < size; i++) {
                StringMessageHeader header = new StringMessageHeader(key, values.getString(i));
                valueList.add(header);
            }
            headers.put(key, valueList);
        }
        
    }
    
    private static final class StringContentParser implements MessagingContentParser {

        public int getPriority() {
            return 0;
        }

        public boolean handles(MessagingMessage partlyParsedMessage, Object content) throws MessagingException {
            if(null != partlyParsedMessage.getContentType()) {
                return partlyParsedMessage.getContentType().getPrimaryType().equals("text");
            }
            return false;
        }

        public MessagingContent parse(MessagingMessage partlyParsedMessage, Object content, MessagingInputStreamRegistry registry) throws JSONException, MessagingException {
            return new StringContent((String) content);
        }
        
    }
    
    private static final class BinaryContentParser implements MessagingContentParser {

        public int getPriority() {
            return 0;
        }

        public boolean handles(MessagingMessage partlyParsedMessage, Object content) throws MessagingException {
            if(null != partlyParsedMessage.getContentType()) {
                String primaryType = partlyParsedMessage.getContentType().getPrimaryType();
                return !primaryType.equals("text") && ! primaryType.equals("multipart");
            }
            return false;
        }

        public MessagingContent parse(MessagingMessage partlyParsedMessage, Object content, MessagingInputStreamRegistry registry) throws JSONException, MessagingException, IOException {
            if(String.class.isInstance(content)) {
                byte[] decoded = Base64.decode((String) content);
                return new ByteArrayContent(decoded);
            } else if (JSONObject.class.isInstance(content)) {
                JSONObject reference = (JSONObject) content;
                Object id = reference.get("ref");
                
                InputStream in = null;
                ByteArrayOutputStream out = null;
                try {
                    in = new BufferedInputStream(registry.get(id));
                    out = new ByteArrayOutputStream();
                    int b = -1;
                    while((b = in.read()) != -1) {
                        out.write(b);
                    }
                } finally {
                    if(in != null) {
                        in.close();
                    }
                    if(out != null) {
                        out.close();
                    }
                }
                
                if(out != null) {
                    return new ByteArrayContent(out.toByteArray());
                }
                return null; // Should never happen
            } else {
                return null; // FIXME
            }
        }
        
    }
    
    private final class MultipartContentParser implements MessagingContentParser {

        public int getPriority() {
            return 0;
        }

        public boolean handles(MessagingMessage partlyParsedMessage, Object content) throws MessagingException {
            if(null != partlyParsedMessage.getContentType()) {
                String primaryType = partlyParsedMessage.getContentType().getPrimaryType();
                return primaryType.equals("multipart");
            }
            return false;
        }

        public MessagingContent parse(MessagingMessage partlyParsedMessage, Object content, MessagingInputStreamRegistry registry) throws JSONException, MessagingException, IOException {
            JSONArray multipartJSON = (JSONArray) content;
            List<JSONMessagingMessage> parts = new ArrayList<JSONMessagingMessage>();
            for(int i = 0, size = multipartJSON.length(); i < size; i++) {
                JSONObject partJSON = multipartJSON.getJSONObject(i);
                JSONMessagingMessage part = new JSONMessagingMessage(partJSON,  registry);
                parts.add(part);
            }
            MultipartContent multipart = new MessagingPartArrayContent(parts);
            for (JSONMessagingMessage messagingMessage : parts) {
                messagingMessage.setParent(multipart);
            }
            return multipart;
        }
        
    }

}
