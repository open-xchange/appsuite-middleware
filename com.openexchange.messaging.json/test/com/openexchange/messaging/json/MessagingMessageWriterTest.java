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

import static com.openexchange.json.JSONAssertion.assertValidates;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.groupware.ldap.SimUser;
import com.openexchange.json.JSONAssertion;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.MultipartContent;
import com.openexchange.messaging.SimpleMessagingMessage;
import com.openexchange.messaging.StringContent;
import com.openexchange.tools.encoding.Base64;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.SimServerSession;

/**
 * {@link MessagingMessageWriterTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MessagingMessageWriterTest extends TestCase {

    public void testWriteSimpleFields() throws JSONException, OXException {

        final SimpleMessagingMessage message = new SimpleMessagingMessage();
        message.setColorLabel(2);
        message.setFlags(12);
        message.setReceivedDate(1337);
        message.setUserFlags(Arrays.asList("eins", "zwo", "drei", "vier", "f\u00fcnf"));
        message.setSize(13);
        message.setThreadLevel(15);
        message.setDisposition(MessagingPart.INLINE);
        message.setId("message123");
        message.setFolder("niceFolder17");
        message.setPicture("http://www.somesite.invalid/somepic.png");
        message.setUrl("http://www.somesite.invalid/messageid");

        final JSONObject messageJSON = new MessagingMessageWriter().write(message, "com.openexchange.test2://account/folder/subfolder", new SimServerSession(new SimContext(1), new SimUser(), null), null);

        final JSONAssertion assertion = new JSONAssertion().isObject().hasKey("colorLabel").withValue(2).hasKey("flags").withValue(12).hasKey(
            "receivedDate").withValue(1337).hasKey("user").withValueArray().withValues("eins", "zwo", "drei", "vier", "f\u00fcnf").inAnyOrder().hasKey(
            "size").withValue(13).hasKey("threadLevel").withValue(15).hasKey("id").withValue("message123").hasKey("folder").withValue(
            "com.openexchange.test2://account/folder/subfolder/niceFolder17").hasKey("picture").withValue("http://www.somesite.invalid/somepic.png").hasKey("url").withValue("http://www.somesite.invalid/messageid");

        assertValidates(assertion, messageJSON);
    }

    public void testHeaders() throws JSONException, OXException {
        final SimpleMessagingMessage message = new SimpleMessagingMessage();
        final Map<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>();

        headers.put("simpleHeader", header("simpleHeader", "Value1"));
        headers.put("multiHeader", header("multiHeader", "v1", "v2", "v3"));

        message.setHeaders(headers);

        final JSONObject messageJSON = new MessagingMessageWriter().write(message, "", null, null);

        final JSONAssertion assertion = new JSONAssertion().isObject().hasKey("headers").withValueObject().hasKey("simpleHeader").withValue(
            "Value1").hasKey("multiHeader").withValueArray().withValues("v1", "v2", "v3").inStrictOrder().objectEnds().objectEnds();

        assertValidates(assertion, messageJSON);
    }

    public void testMirrorHeadersInAttributesIfTheyAreMessagingFields() throws OXException, JSONException {
        final SimpleMessagingMessage message = new SimpleMessagingMessage();
        final Map<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>();

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

        final JSONObject messageJSON = new MessagingMessageWriter().write(message, "", null, null);

        // Where happy if they are all included. Concrete header writing is tested elsewhere

        for (final MessagingField field : MessagingField.values()) {
            if(field.getEquivalentHeader() != null) {
                assertTrue("Missing field: "+field.toString()+" in ", messageJSON.has(field.toString()));
            }
        }
    }

    private static final class InverseWriter implements MessagingHeaderWriter {

        @Override
        public int getRanking() {
            return 2;
        }

        @Override
        public boolean handles(final Entry<String, Collection<MessagingHeader>> entry) {
            return true;
        }

        @Override
        public String writeKey(final Entry<String, Collection<MessagingHeader>> entry) throws JSONException, OXException {
            return entry.getKey();
        }

        @Override
        public Object writeValue(final Entry<String, Collection<MessagingHeader>> entry, final ServerSession session) throws JSONException, OXException {
            return new StringBuilder(entry.getValue().iterator().next().getValue()).reverse().toString();
        }

    }

    public void testSpecialHeader() throws OXException, JSONException {
        final SimpleMessagingMessage message = new SimpleMessagingMessage();
        final Map<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>();

        headers.put("simpleHeader", header("simpleHeader", "Value1"));

        message.setHeaders(headers);

        final MessagingMessageWriter writer = new MessagingMessageWriter();

        writer.addHeaderWriter(new InverseWriter());

        final JSONObject messageJSON = writer.write(message, "", null, null);

        final JSONAssertion assertion = new JSONAssertion().isObject().hasKey("headers").withValueObject().hasKey("simpleHeader").withValue(
            "1eulaV").objectEnds().objectEnds();

        assertValidates(assertion, messageJSON);

    }

    public void testPlainMessage() throws OXException, JSONException {
        final SimpleMessagingMessage message = new SimpleMessagingMessage();
        message.setContent("content");

        final JSONObject messageJSON = new MessagingMessageWriter().write(message, "", null, null);

        final JSONAssertion assertion = new JSONAssertion().isObject().hasKey("body").withValue("content").objectEnds();

        assertValidates(assertion, messageJSON);

    }

    public void testReferenceContent() throws OXException, JSONException {
        final SimpleMessagingMessage message = new SimpleMessagingMessage();
        message.setContentReference("coolReferenceId");

        final JSONObject messageJSON = new MessagingMessageWriter().write(message, "", null, null);

        final JSONAssertion assertion = new JSONAssertion().isObject().hasKey("body").withValueObject().hasKey("ref").withValue("coolReferenceId").objectEnds().objectEnds();

        assertValidates(assertion, messageJSON);

    }

    public void testBinaryMessage() throws OXException, JSONException, UnsupportedEncodingException {
        final SimpleMessagingMessage message = new SimpleMessagingMessage();
        message.setContent("content".getBytes("UTF-8"));

        final JSONObject messageJSON = new MessagingMessageWriter().write(message, "", null, null);

        final JSONAssertion assertion = new JSONAssertion().isObject().hasKey("body").withValue(Base64.encode("content")).objectEnds();

        assertValidates(assertion, messageJSON);

    }

    public void testMultipartMessage() throws UnsupportedEncodingException, OXException, JSONException {
        final SimpleMessagingMessage binMessage = new SimpleMessagingMessage();
        binMessage.setSectionId("1");
        binMessage.setContent("content".getBytes("UTF-8"));
        binMessage.setDisposition(MessagingPart.ATTACHMENT);
        binMessage.setFileName("content.txt");

        final SimpleMessagingMessage plainMessage = new SimpleMessagingMessage();
        plainMessage.setContent("content");
        plainMessage.setSectionId("2");

        final SimpleMessagingMessage message = new SimpleMessagingMessage();
        message.setContent(binMessage, plainMessage);

        plainMessage.setParent((MultipartContent) message.getContent());
        binMessage.setParent((MultipartContent) message.getContent());

        final JSONObject messageJSON = new MessagingMessageWriter().write(message, "", null, null);

        JSONAssertion assertion = new JSONAssertion().isObject().hasKey("body").withValueArray().objectEnds();

        assertValidates(assertion, messageJSON);

        final JSONArray array = messageJSON.getJSONArray("body");

        assertEquals(2, array.length());

        final JSONObject firstContent = array.getJSONObject(0);

        assertion = new JSONAssertion().isObject().hasKey("sectionId").withValue("1").hasKey("body").withValue(Base64.encode("content")).hasKey(
            "disposition").withValue(MessagingPart.ATTACHMENT).hasKey("fileName").withValue("content.txt").objectEnds();

        assertValidates(assertion, firstContent);

        final JSONObject secondContent = array.getJSONObject(1);

        assertion = new JSONAssertion().isObject().hasKey("sectionId").withValue("2").hasKey("body").withValue("content").objectEnds();

        assertValidates(assertion, secondContent);
    }

    private static class InverseContentWriter implements MessagingContentWriter {

        @Override
        public int getRanking() {
            return 2;
        }

        @Override
        public boolean handles(final MessagingPart part, final MessagingContent content) {
            return true;
        }

        @Override
        public Object write(final MessagingPart part, final MessagingContent content, final ServerSession session, final DisplayMode mode) throws OXException, JSONException {
            return new StringBuilder(((StringContent) content).getData()).reverse().toString();
        }

    }

    public void testCustomContentWriter() throws OXException, JSONException {
        final SimpleMessagingMessage message = new SimpleMessagingMessage();
        message.setContent("content");

        final MessagingMessageWriter writer = new MessagingMessageWriter();
        writer.addContentWriter(new InverseContentWriter());

        final JSONObject messageJSON = writer.write(message, "", null, null);

        final JSONAssertion assertion = new JSONAssertion().isObject().hasKey("body").withValue("tnetnoc").objectEnds();

        assertValidates(assertion, messageJSON);
    }

    public void testWriteSimpleArrayFields() throws OXException, JSONException {
        // Test with one header equivalent field and all non-header fields
        final MessagingField[] fields = new MessagingField[] {
            MessagingField.ID, MessagingField.FOLDER_ID, MessagingField.SUBJECT, MessagingField.SIZE, MessagingField.RECEIVED_DATE,
            MessagingField.FLAGS, MessagingField.THREAD_LEVEL, MessagingField.COLOR_LABEL, MessagingField.BODY, MessagingField.PICTURE, MessagingField.URL};


        //TODO AccountName ? What is that?
        final SimpleMessagingMessage message = new SimpleMessagingMessage();
        message.setId("msg123");
        message.setFolder("folder12");
        message.setSize(1337);
        message.setReceivedDate(1234567);
        message.setFlags(313);
        message.setThreadLevel(12);
        message.setColorLabel(13);
        message.setContent("Supercontent!");
        message.setPicture("pic");
        message.setUrl("http://url.tld");

        final Map<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>();
        headers.put("Subject", header("Subject", "the subject"));
        message.setHeaders(headers);

        final JSONArray fieldsJSON = new MessagingMessageWriter().writeFields(message, fields, "com.openexchange.test1://account12", new SimServerSession(new SimContext(1), new SimUser(), null), null);

        final JSONAssertion assertion = new JSONAssertion().isArray().withValues("msg123", "com.openexchange.test1://account12/folder12", "the subject", 1337l, 1234567l, 313, 12, 13, "Supercontent!", "pic", "http://url.tld").inStrictOrder();

        assertValidates(assertion, fieldsJSON);
    }

    public void testWritePostiveNumbersAsNullIfNegative() throws OXException, JSONException {
     // Test with one header equivalent field and all non-header fields
        final MessagingField[] fields = new MessagingField[] {
           MessagingField.SIZE, MessagingField.RECEIVED_DATE, MessagingField.THREAD_LEVEL};


        //TODO AccountName ? What is that?
        final SimpleMessagingMessage message = new SimpleMessagingMessage();
        message.setSize(-1);
        message.setReceivedDate(-1);
        message.setThreadLevel(-1);
        message.setColorLabel(-1);

        final Map<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>();
        headers.put("Subject", header("Subject", "the subject"));
        message.setHeaders(headers);

        final JSONArray fieldsJSON = new MessagingMessageWriter().writeFields(message, fields, "com.openexchange.test1://account12", null, null);

        assertEquals(3, fieldsJSON.length());

        assertNull(fieldsJSON.opt(0));
        assertNull(fieldsJSON.opt(1));
        assertNull(fieldsJSON.opt(2));

    }

    public void testWriteHeaderArrayField() throws OXException, JSONException {
        final MessagingField[] fields = new MessagingField[] {
            MessagingField.HEADERS};


        final SimpleMessagingMessage message = new SimpleMessagingMessage();

        final Map<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>(    );
        headers.put("Subject", header("Subject", "the subject"));
        message.setHeaders(headers);

        final JSONArray fieldsJSON = new MessagingMessageWriter().writeFields(message, fields, null, null, null);

        assertEquals(1, fieldsJSON.length());

        final JSONObject headersJSON = fieldsJSON.getJSONObject(0);

        final JSONAssertion assertion = new JSONAssertion().isObject().hasKey("Subject").withValue("the subject");

        assertValidates(assertion, headersJSON);
    }


    private Collection<MessagingHeader> header(final String name, final String... values) {
        final List<MessagingHeader> header = new ArrayList<MessagingHeader>();
        for (final String value : values) {
            header.add(new MessagingHeader() {

                @Override
                public String getName() {
                    return name;
                }

                @Override
                public String getValue() {
                    return value;
                }

                @Override
                public HeaderType getHeaderType() {
                    return HeaderType.PLAIN;
                }

            });
        }

        return header;
    }

}
