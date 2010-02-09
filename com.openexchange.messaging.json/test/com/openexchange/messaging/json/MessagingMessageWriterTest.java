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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.json.JSONAssertion;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.MultipartContent;
import com.openexchange.messaging.SimpleMessagingMessage;
import com.openexchange.messaging.StringContent;
import com.openexchange.tools.encoding.Base64;
import junit.framework.TestCase;
import static com.openexchange.json.JSONAssertion.*;

/**
 * {@link MessagingMessageWriterTest}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MessagingMessageWriterTest extends TestCase {

    public void testWriteSimpleFields() throws JSONException, MessagingException {

        SimpleMessagingMessage message = new SimpleMessagingMessage();
        message.setColorLabel(2);
        message.setFlags(12);
        message.setReceivedDate(1337);
        message.setUserFlags(Arrays.asList("eins", "zwo", "drei", "vier", "f�nf"));
        message.setSize(13);
        message.setThreadLevel(15);
        message.setDisposition(MessagingMessage.INLINE);
        message.setId("message123");
        message.setFolder("niceFolder17");
        message.setPicture("http://www.somesite.invalid/somepic.png");

        JSONObject messageJSON = new MessagingMessageWriter().write(message);

        JSONAssertion assertion = new JSONAssertion().isObject().hasKey("colorLabel").withValue(2).hasKey("flags").withValue(12).hasKey(
            "receivedDate").withValue(1337).hasKey("user").withValueArray().withValues("eins", "zwo", "drei", "vier", "f�nf").inAnyOrder().hasKey(
            "size").withValue(13).hasKey("threadLevel").withValue(15).hasKey("id").withValue("message123").hasKey("folder").withValue(
            "niceFolder17").hasKey("picture").withValue("http://www.somesite.invalid/somepic.png");

        assertValidates(assertion, messageJSON);
    }

    public void testHeaders() throws JSONException, MessagingException {
        SimpleMessagingMessage message = new SimpleMessagingMessage();
        Map<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>();

        headers.put("simpleHeader", header("simpleHeader", "Value1"));
        headers.put("multiHeader", header("multiHeader", "v1", "v2", "v3"));

        message.setHeaders(headers);

        JSONObject messageJSON = new MessagingMessageWriter().write(message);

        JSONAssertion assertion = new JSONAssertion().isObject().hasKey("headers").withValueObject().hasKey("simpleHeader").withValue(
            "Value1").hasKey("multiHeader").withValueArray().withValues("v1", "v2", "v3").inStrictOrder().objectEnds().objectEnds();

        assertValidates(assertion, messageJSON);
    }
    
    public void testMirrorHeadersInAttributesIfTheyAreMessagingFields() throws MessagingException, JSONException {
        SimpleMessagingMessage message = new SimpleMessagingMessage();
        Map<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>();
        
        headers.put("Content-Type", header("Content-Type", "text/plain"));
        headers.put("From", header("From", "from.clark.kent@dailyplanet.com"));
        headers.put("To", header("To", "to.clark.kent@dailyplanet.com"));
        headers.put("Cc", header("Cc", "cc.clark.kent@dailyplanet.com"));
        headers.put("Bcc", header("Bcc", "bcc.clark.kent@dailyplanet.com"));
        headers.put("Subject", header("Subject", "Subject-Value"));
        headers.put("Date", header("Date", "Date-Value"));
        headers.put("Disposition-Notification-To", header("Disposition-Notification-To", "disp.notification.to.clark.kent@dailyplanet.com"));
        headers.put("X-Priority", header("X-Priority", "12"));
        
        message.setHeaders(headers);
        
        JSONObject messageJSON = new MessagingMessageWriter().write(message);
        
        // Where happy if they are all included. Concrete header writing is tested elsewhere
        
        for (MessagingField field : MessagingField.values()) {
            if(field.getEquivalentHeader() != null) {
                assertTrue("Missing field: "+field.toString()+" in ", messageJSON.has(field.toString()));
            }
        }
    }

    private static final class InverseWriter implements MessagingHeaderWriter {

        public int getPriority() {
            return 2;
        }

        public boolean handles(Entry<String, Collection<MessagingHeader>> entry) {
            return true;
        }

        public String writeKey(Entry<String, Collection<MessagingHeader>> entry) throws JSONException, MessagingException {
            return entry.getKey();
        }

        public Object writeValue(Entry<String, Collection<MessagingHeader>> entry) throws JSONException, MessagingException {
            return new StringBuilder((String) entry.getValue().iterator().next().getValue()).reverse().toString();
        }

    }

    public void testSpecialHeader() throws MessagingException, JSONException {
        SimpleMessagingMessage message = new SimpleMessagingMessage();
        Map<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>();

        headers.put("simpleHeader", header("simpleHeader", "Value1"));

        message.setHeaders(headers);

        MessagingMessageWriter writer = new MessagingMessageWriter();

        writer.addHeaderWriter(new InverseWriter());

        JSONObject messageJSON = writer.write(message);

        JSONAssertion assertion = new JSONAssertion().isObject().hasKey("headers").withValueObject().hasKey("simpleHeader").withValue(
            "1eulaV").objectEnds().objectEnds();

        assertValidates(assertion, messageJSON);

    }

    public void testPlainMessage() throws MessagingException, JSONException {
        SimpleMessagingMessage message = new SimpleMessagingMessage();
        message.setContent("content");

        JSONObject messageJSON = new MessagingMessageWriter().write(message);

        JSONAssertion assertion = new JSONAssertion().isObject().hasKey("body").withValue("content").objectEnds();

        assertValidates(assertion, messageJSON);

    }

    public void testBinaryMessage() throws MessagingException, JSONException, UnsupportedEncodingException {
        SimpleMessagingMessage message = new SimpleMessagingMessage();
        message.setContent("content".getBytes("UTF-8"));

        JSONObject messageJSON = new MessagingMessageWriter().write(message);

        JSONAssertion assertion = new JSONAssertion().isObject().hasKey("body").withValue(Base64.encode("content")).objectEnds();

        assertValidates(assertion, messageJSON);

    }

    public void testMultipartMessage() throws UnsupportedEncodingException, MessagingException, JSONException {
        SimpleMessagingMessage binMessage = new SimpleMessagingMessage();
        binMessage.setSectionId("1");
        binMessage.setContent("content".getBytes("UTF-8"));
        binMessage.setDisposition(MessagingMessage.ATTACHMENT);
        binMessage.setFileName("content.txt");

        SimpleMessagingMessage plainMessage = new SimpleMessagingMessage();
        plainMessage.setContent("content");
        plainMessage.setSectionId("2");

        SimpleMessagingMessage message = new SimpleMessagingMessage();
        message.setContent(binMessage, plainMessage);

        plainMessage.setParent((MultipartContent) message.getContent());
        binMessage.setParent((MultipartContent) message.getContent());

        JSONObject messageJSON = new MessagingMessageWriter().write(message);

        JSONAssertion assertion = new JSONAssertion().isObject().hasKey("body").withValueArray().objectEnds();

        assertValidates(assertion, messageJSON);

        JSONArray array = messageJSON.getJSONArray("body");

        assertEquals(2, array.length());

        JSONObject firstContent = array.getJSONObject(0);

        assertion = new JSONAssertion().isObject().hasKey("sectionId").withValue("1").hasKey("body").withValue(Base64.encode("content")).hasKey(
            "disposition").withValue(MessagingMessage.ATTACHMENT).hasKey("fileName").withValue("content.txt").objectEnds();

        assertValidates(assertion, firstContent);

        JSONObject secondContent = array.getJSONObject(1);

        assertion = new JSONAssertion().isObject().hasKey("sectionId").withValue("2").hasKey("body").withValue("content").objectEnds();

        assertValidates(assertion, secondContent);
    }

    private static class InverseContentWriter implements MessagingContentWriter {

        public int getPriority() {
            return 2;
        }

        public boolean handles(MessagingPart part, MessagingContent content) {
            return true;
        }

        public Object write(MessagingPart part, MessagingContent content) throws MessagingException, JSONException {
            return new StringBuilder(((StringContent) content).getData()).reverse().toString();
        }

    }

    public void testCustomContentWriter() throws MessagingException, JSONException {
        SimpleMessagingMessage message = new SimpleMessagingMessage();
        message.setContent("content");

        MessagingMessageWriter writer = new MessagingMessageWriter();
        writer.addContentWriter(new InverseContentWriter());

        JSONObject messageJSON = writer.write(message);

        JSONAssertion assertion = new JSONAssertion().isObject().hasKey("body").withValue("tnetnoc").objectEnds();

        assertValidates(assertion, messageJSON);
    }

    public void testWriteSimpleArrayFields() throws MessagingException, JSONException {
        // Test with one header equivalent field and all non-header fields
        MessagingField[] fields = new MessagingField[] {
            MessagingField.ID, MessagingField.FOLDER_ID, MessagingField.SUBJECT, MessagingField.SIZE, MessagingField.RECEIVED_DATE,
            MessagingField.FLAGS, MessagingField.THREAD_LEVEL, MessagingField.COLOR_LABEL, MessagingField.BODY, MessagingField.PICTURE};

        
        //TODO AccountName ? What is that?
        SimpleMessagingMessage message = new SimpleMessagingMessage();
        message.setId("msg123");
        message.setFolder("folder12");
        message.setSize(1337);
        message.setReceivedDate(1234567);
        message.setFlags(313);
        message.setThreadLevel(12);
        message.setColorLabel(13);
        message.setContent("Supercontent!");
        message.setPicture("pic");
        
        Map<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>();
        headers.put("Subject", header("Subject", "the subject"));
        message.setHeaders(headers);

        JSONArray fieldsJSON = new MessagingMessageWriter().writeFields(message, fields);

        JSONAssertion assertion = new JSONAssertion().isArray().withValues("msg123", "folder12", "the subject", 1337l, 1234567l, 313, 12, 13, "Supercontent!", "pic").inStrictOrder();

        assertValidates(assertion, fieldsJSON);
    }
    
    public void testWriteHeaderArrayField() throws MessagingException, JSONException {
        MessagingField[] fields = new MessagingField[] {
            MessagingField.HEADERS};

        
        SimpleMessagingMessage message = new SimpleMessagingMessage();
        
        Map<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>(    );
        headers.put("Subject", header("Subject", "the subject"));
        message.setHeaders(headers);

        JSONArray fieldsJSON = new MessagingMessageWriter().writeFields(message, fields);

        assertEquals(1, fieldsJSON.length());
        
        JSONObject headersJSON = fieldsJSON.getJSONObject(0);
    
        JSONAssertion assertion = new JSONAssertion().isObject().hasKey("Subject").withValue("the subject");
        
        assertValidates(assertion, headersJSON);
    }
    

    private Collection<MessagingHeader> header(final String name, String... values) {
        List<MessagingHeader> header = new ArrayList<MessagingHeader>();
        for (final String value : values) {
            header.add(new MessagingHeader() {

                public String getName() {
                    return name;
                }

                public String getValue() {
                    return value;
                }

            });
        }

        return header;
    }

}
