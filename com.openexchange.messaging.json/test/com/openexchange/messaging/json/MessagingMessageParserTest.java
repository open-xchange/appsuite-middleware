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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.messaging.BinaryContent;
import com.openexchange.messaging.MessagingBodyPart;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MultipartContent;
import com.openexchange.messaging.SimpleContent;
import com.openexchange.messaging.StringContent;
import com.openexchange.messaging.StringMessageHeader;
import com.openexchange.tools.encoding.Base64;
import junit.framework.TestCase;


/**
 * {@link MessagingMessageParserTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MessagingMessageParserTest extends TestCase {
    
    public class InvertedHeaderParser implements MessagingHeaderParser {

        public boolean handles(String key, Object value) {
            return true;
        }

        public void parseAndAdd(Map<String, Collection<MessagingHeader>> headers, String key, Object value) {
            StringMessageHeader header = new StringMessageHeader(key, new StringBuilder((String)value).reverse().toString());
            headers.put(key, Arrays.asList((MessagingHeader)header));
        }

        public int getPriority() {
            return 2;
        }

    }

    public void testParseSimpleFields() throws JSONException, MessagingException, IOException {
        JSONObject messageJSON = new JSONObject();
        
        messageJSON.put("colorLabel", 12);
        messageJSON.put("id", "13");
        messageJSON.put("flags", 313);
        messageJSON.put("receivedDate", 7331);
        messageJSON.put("size", 23);
        messageJSON.put("threadLevel", 3);
       
        JSONArray userFlags = new JSONArray();
        
        userFlags.put("flag1"); 
        userFlags.put("flag2");
        userFlags.put("flag3");
        
        messageJSON.put("userFlags", userFlags);
        messageJSON.put("folder", "niceFolder17");
        
        MessagingMessage message = new MessagingMessageParser().parse(messageJSON, null);
        
        assertNotNull(message);
        
        assertEquals("13", message.getSectionId());
        assertEquals(12, message.getColorLabel());
        assertEquals(313, message.getFlags());
    
        Collection<String> flags = message.getUserFlags();
        assertNotNull(flags);
        assertEquals(3, flags.size());
        Iterator<String> iterator = flags.iterator();
        Set<String> expectedFlags = new HashSet<String>(Arrays.asList("flag1", "flag2", "flag3"));
        while(iterator.hasNext()) {
            assertTrue(expectedFlags.remove(iterator.next()));
        }
        assertTrue(expectedFlags.isEmpty());
        assertEquals(7331, message.getReceivedDate());
    
        assertEquals(23, message.getSize());
        assertEquals(3, message.getThreadLevel());
        
        assertEquals("niceFolder17", message.getFolder());
    }
    
    
    public void testHeaders() throws JSONException, MessagingException, IOException {
        JSONObject messageJSON = new JSONObject();
        
        JSONObject headers = new JSONObject();
        headers.put("singleValue", "Value1");
        
        JSONArray multiValue = new JSONArray();
        multiValue.put("1").put("2").put("3");
        headers.put("multiValue", multiValue);
        
        messageJSON.put("headers", headers);
        
        MessagingMessage message = new MessagingMessageParser().parse(messageJSON, null);
        
        assertNotNull(message);
        
        Collection<MessagingHeader> header = message.getHeader("singleValue");
        assertNotNull(header);
        assertEquals("Value1", header.iterator().next().getValue());
        
        header = message.getHeader("multiValue");
        assertNotNull(header);
        
        assertEquals(3, header.size());
        
        Iterator<MessagingHeader> iterator = header.iterator();
        assertEquals("1", iterator.next().getValue());
        assertEquals("2", iterator.next().getValue());
        assertEquals("3", iterator.next().getValue());

        
    }
    
    public void testSpecialHeader() throws JSONException, MessagingException, IOException {
        JSONObject messageJSON = new JSONObject();
        
        JSONObject headers = new JSONObject();
        headers.put("singleValue", "1eulaV");
        
        messageJSON.put("headers", headers);
        
        MessagingMessageParser parser = new MessagingMessageParser();
     
        parser.addHeaderParser(new InvertedHeaderParser());
        MessagingMessage message = parser.parse(messageJSON, null);
        
        assertNotNull(message);
        
        Collection<MessagingHeader> header = message.getHeader("singleValue");
        assertNotNull(header);
        assertEquals("Value1", header.iterator().next().getValue());
    }
    
    public void testPlainBody() throws JSONException, MessagingException, IOException {
        JSONObject messageJSON = new JSONObject();

        messageJSON.put("content", "I am the content");
        
        JSONObject headers = new JSONObject();
        headers.put("content-type", "text/plain");
        messageJSON.put("headers", headers);
        
        MessagingMessage message = new MessagingMessageParser().parse(messageJSON, null);
        
        assertNotNull(message);
        
        MessagingContent content = message.getContent();
        assertNotNull(content);
        
        
        
        assertEquals("I am the content", getStringData(content));
    }
    
    private String getStringData(MessagingContent content) throws MessagingException, IOException {
        if(StringContent.class.isInstance(content)) {
            return ((StringContent) content).getData();
        } else if (BinaryContent.class.isInstance(content)) {
            return inputStream2String(((BinaryContent) content).getData());
        }
        return null;
    }


    public void testBinaryBodyInBase64() throws MessagingException, JSONException, IOException {
        JSONObject messageJSON = new JSONObject();

        messageJSON.put("content", Base64.encode("I am the content"));
        
        JSONObject headers = new JSONObject();
        headers.put("content-type", "application/octet-stream");
        messageJSON.put("headers", headers);
        
        MessagingMessage message = new MessagingMessageParser().parse(messageJSON, null);
        
        assertNotNull(message);
        
        MessagingContent content = message.getContent();
        assertNotNull(content);
        
        assertTrue(BinaryContent.class.isInstance(content));
        
        assertEquals("I am the content", inputStream2String( ((BinaryContent) content).getData() ));

    }
    
    public void testBinaryBodyByReference() throws MessagingException, IOException, JSONException {
        JSONObject messageJSON = new JSONObject();

        messageJSON.put("content", new JSONObject("{ref : '12'}"));
        
        JSONObject headers = new JSONObject();
        headers.put("content-type", "application/octet-stream");
        messageJSON.put("headers", headers);
        
        SimInputStreamRegistry registry = new SimInputStreamRegistry();
        
        MessagingMessage message = new MessagingMessageParser().parse(messageJSON, registry);
        
        assertEquals("12", registry.getId());
        assertNotNull(message);
        
        MessagingContent content = message.getContent();
        assertNotNull(content);
        
        assertTrue(BinaryContent.class.isInstance(content));
        
        assertEquals("Mock value", inputStream2String( ((BinaryContent) content).getData() ));
    }
    
    public void testMultipart() throws JSONException, MessagingException, IOException {
        JSONObject messageJSON = new JSONObject("{headers : {'content-type' : 'multipart/mixed'}}");

        JSONArray multipartJSON = new JSONArray();
        
        JSONObject body1 = new JSONObject("{content : 'simpleContent', headers: {content-type : 'text/plain'}, id: '1'}");
        JSONObject body2 = new JSONObject("{content : '"+Base64.encode("binaryData")+"', headers: {content-type : 'application/octet-stream'}, id: '2'}");
        multipartJSON.put(body1);
        multipartJSON.put(body2);
        
        
        messageJSON.put("content", multipartJSON);

        MessagingMessage message = new MessagingMessageParser().parse(messageJSON, null);
        
        assertNotNull(message);
        
        MessagingContent content = message.getContent();
        assertNotNull(content);
        assertTrue(MultipartContent.class.isInstance(content));
        
        MultipartContent multipart = (MultipartContent) content;
        
        assertEquals(2, multipart.getCount());
        
        MessagingBodyPart textPart = multipart.get(0);
        //assertEquals("1", textPart.getId());
        assertEquals("text/plain", textPart.getContentType().getBaseType());
        assertEquals("simpleContent", ((StringContent) textPart.getContent()).getData());
        
        MessagingBodyPart binPart = multipart.get(1);
        //assertEquals("2", binPart.getId());
        assertEquals("application/octet-stream", binPart.getContentType().getBaseType());
        assertEquals("binaryData", inputStream2String(((BinaryContent) binPart.getContent()).getData()));
        
        
    }
    
    private static final class ReversedContentParser implements MessagingContentParser {

        public int getPriority() {
            return 2;
        }

        public boolean handles(MessagingBodyPart partlyParsedMessage, Object content) throws MessagingException {
            return partlyParsedMessage.getContentType().getBaseType().equals("text/plain");
        }

        public MessagingContent parse(MessagingBodyPart partlyParsedMessage, Object content, MessagingInputStreamRegistry registry) throws JSONException, MessagingException, IOException {
            return new StringContent(new StringBuilder((String)content).reverse().toString());
        }
        
    }
    
    public void testSpecialBody() throws MessagingException, JSONException, IOException {
        JSONObject messageJSON = new JSONObject();

        messageJSON.put("content", "tnetnoc eht ma I");
        
        JSONObject headers = new JSONObject();
        headers.put("content-type", "text/plain");
        messageJSON.put("headers", headers);
        
        MessagingMessageParser parser = new MessagingMessageParser();
        parser.addContentParser(new ReversedContentParser());
        MessagingMessage message = parser.parse(messageJSON, null);
        
        assertNotNull(message);
        
        MessagingContent content = message.getContent();
        assertNotNull(content);
        
        assertEquals("I am the content", getStringData(content));
    }
    
    private String inputStream2String(InputStream data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b = 1;
        while((b = data.read()) != -1) {
            baos.write(b);
        }
        return new String(baos.toByteArray(), "UTF-8");
    }

    private static final class SimInputStreamRegistry implements MessagingInputStreamRegistry {

        private Object id;

        public InputStream get(Object id) throws MessagingException, IOException {
            this.id = id;
            return new ByteArrayInputStream("Mock value".getBytes("UTF-8"));
        }
        
        public Object getId() {
            return id;
        }
        
    }
    
}
