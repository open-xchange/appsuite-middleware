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

package com.openexchange.messaging.json.cacheing;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.messaging.IndexRange;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingMessageAccess;
import com.openexchange.messaging.SimMessageAccess;
import com.openexchange.messaging.SimpleMessagingMessage;
import junit.framework.TestCase;


/**
 * {@link CacheingMessageAccessTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CacheingMessageAccessTest extends TestCase {

    public void testGetTriesCache() throws MessagingException {
        SimMessageAccess access = new SimMessageAccess();
        TestCacheMessageAccess messageAccess = new TestCacheMessageAccess(access);
        
        SimpleMessagingMessage message = new SimpleMessagingMessage();
        messageAccess.setCached(message);
        
        MessagingMessage retval = messageAccess.getMessage("folder", "id", true);
        
        assertSame(message, retval);
    }
    
    public void testBulkLoadingTriesCache() throws MessagingException {
        SimMessageAccess access = new SimMessageAccess();
        TestCacheMessageAccess messageAccess = new TestCacheMessageAccess(access);
        
        SimpleMessagingMessage message = new SimpleMessagingMessage();
        messageAccess.setCached(message);
        
        List<MessagingMessage> messages = messageAccess.getMessages("folder", new String[]{"a", "b", "c"}, null);
        
        assertEquals(3, messages.size());
        for (MessagingMessage messagingMessage : messages) {
            assertSame(message, messagingMessage);
        }
        
    }
    
    public void testGetFallbackOnCacheMiss() throws MessagingException {
        SimMessageAccess access = new SimMessageAccess();
        TestCacheMessageAccess messageAccess = new TestCacheMessageAccess(access);
        
        SimpleMessagingMessage message = new SimpleMessagingMessage();
        access.setTemplateMessage(message);
        
        MessagingMessage retval = messageAccess.getMessage("folder", "id", true);
        
        assertSame(message, retval);
        List<MessagingMessage> rememberedMessages = messageAccess.getRememberedMessages();
        
        assertEquals(1, rememberedMessages.size());
        
        assertSame(message, rememberedMessages.get(0));
    }
    
    public void testBulkFallbackOnCacheMiss() throws MessagingException {
        SimMessageAccess access = new SimMessageAccess();
        TestCacheMessageAccess messageAccess = new TestCacheMessageAccess(access);
        
        SimpleMessagingMessage cachedMessage = new SimpleMessagingMessage();
        SimpleMessagingMessage storedMessage = new SimpleMessagingMessage();
        storedMessage.setId("b");

        messageAccess.setCached(cachedMessage);
        messageAccess.forgetCachedAfterFirstHit();
        
        access.setTemplateMessage(storedMessage);
        
        messageAccess.forgetCachedAfterFirstHit();
        
        List<MessagingMessage> messages = messageAccess.getMessages("folder", new String[]{"a", "b"}, null);
        
        assertEquals(2, messages.size());
        
        assertSame(cachedMessage, messages.get(0));
        assertSame(storedMessage, messages.get(1));
    }
    
    public void testAllRefreshesCache() throws MessagingException {
        SimMessageAccess access = new SimMessageAccess();
        TestCacheMessageAccess messageAccess = new TestCacheMessageAccess(access);
        
        SimpleMessagingMessage cachedMessage = new SimpleMessagingMessage();
        SimpleMessagingMessage storedMessage = new SimpleMessagingMessage();

        messageAccess.setCached(cachedMessage);
        messageAccess.forgetCachedAfterFirstHit();
        
        access.setTemplateMessage(storedMessage);
        
        messageAccess.forgetCachedAfterFirstHit();
        
        List<MessagingMessage> messages = messageAccess.getAllMessages("folder", IndexRange.NULL, null, null, null);
        
        assertEquals(1, messages.size());
        
        assertSame(storedMessage, messages.get(0));
        assertEquals("folder", messageAccess.getClearedFolder());
    }
    
    private static final class TestCacheMessageAccess extends CacheingMessageAccess {

        
        private MessagingMessage cached;
        private List<MessagingMessage> rememberedMessages = new LinkedList<MessagingMessage>();
        private String folder;
        private String id;
        private boolean forgetAfterFirstHit;
        private String clearedFolder;

        public TestCacheMessageAccess(MessagingMessageAccess delegate) {
            super(delegate, null, null, null);
        }
        
        public void forgetCachedAfterFirstHit() {
            this.forgetAfterFirstHit = true;
        }

        public List<MessagingMessage> getRememberedMessages() {
            return rememberedMessages;
        }
        
        public void setCached(MessagingMessage message) {
            this.cached = message;
        }
        
        protected MessagingMessage remember(MessagingMessage message) {
            rememberedMessages.add( message );
            return message;
        }
        
        @Override
        protected MessagingMessage get(String folder, String id) {
            MessagingMessage retval = cached;
            if(forgetAfterFirstHit) {
                cached = null;
            }
            this.folder = folder;
            this.id = id;
            return retval;
        }
        
        @Override
        protected void clear(String folderId) {
            this.clearedFolder = folderId;
        }
        
        public String getClearedFolder() {
            return clearedFolder;
        }
        
    }
    
    
    
}
