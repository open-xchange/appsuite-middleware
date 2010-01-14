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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.json.JSONAssertion;
import com.openexchange.messaging.MessageHeader;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.SimpleMessagingMessage;
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
        message.setUserFlags(Arrays.asList("eins", "zwo", "drei","vier","fünf"));
        message.setSize(13);
        message.setThreadLevel(15);
        message.setDisposition(MessagingMessage.INLINE);
        message.setId("message123");
        
        JSONObject messageJSON = new MessagingMessageWriter().write(message);
        
        JSONAssertion assertion = new JSONAssertion()
            .isObject()
                .hasKey("colorLabel").withValue(2)
                .hasKey("flags").withValue(12)
                .hasKey("received_date").withValue(1337)
                .hasKey("user").withValueArray().withValues("eins","zwo","drei","vier","fünf").inAnyOrder()
                .hasKey("size").withValue(13)
                .hasKey("threadLevel").withValue(15)
                .hasKey("disposition").withValue(MessagingMessage.INLINE)
                .hasKey("id").withValue("message123");
        
        assertValidates(assertion, messageJSON);
    }
    
    // TODO: What about special headers, with special structures?
    public void testHeaders() throws JSONException, MessagingException {
        SimpleMessagingMessage message = new SimpleMessagingMessage();
        Map<String, Collection<MessageHeader>> headers = new HashMap<String, Collection<MessageHeader>>();
        
        headers.put("simpleHeader", header("simpleHeader", "Value1"));
        headers.put("multiHeader", header("multiHeader", "v1", "v2", "v3"));
        
        message.setHeaders(headers);
        
        JSONObject messageJSON = new MessagingMessageWriter().write(message);
        
        JSONAssertion assertion = new JSONAssertion()
            .isObject()
                .hasKey("headers").withValueObject()
                    .hasKey("simpleHeader").withValue("Value1")
                    .hasKey("multiHeader").withValueArray().withValues("v1", "v2", "v3").inStrictOrder()
                .objectEnds()
            .objectEnds()
        ;
        
        assertValidates(assertion, messageJSON);
    }
    
    public void testPlainMessage() throws MessagingException, JSONException {
        SimpleMessagingMessage message = new SimpleMessagingMessage();
        message.setContent("content");
        
        JSONObject messageJSON = new MessagingMessageWriter().write(message);
        
        JSONAssertion assertion = new JSONAssertion()
            .isObject()
                .hasKey("content").withValue("content")
            .objectEnds()
        ;
        
        assertValidates(assertion, messageJSON);
        
    }
    
    public void testBinaryMessage() {
        
    }
    
    public void testMultipartMessage() {
        
    }
    
    private Collection<MessageHeader> header(final String name, String...values) {
        List<MessageHeader> header = new ArrayList<MessageHeader>();
        for (final String value : values) {
            header.add(new MessageHeader() {

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
