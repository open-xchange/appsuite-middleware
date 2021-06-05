/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.messaging.json.cacheing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.IndexRange;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingMessageAccess;
import com.openexchange.messaging.SimMessageAccess;
import com.openexchange.messaging.SimpleMessagingMessage;

/**
 * {@link CacheingMessageAccessTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CacheingMessageAccessTest {

    @Test
    public void testGetTriesCache() throws OXException {
        final SimMessageAccess access = new SimMessageAccess();
        final TestCacheMessageAccess messageAccess = new TestCacheMessageAccess(access);

        final SimpleMessagingMessage message = new SimpleMessagingMessage();
        messageAccess.setCached(message);

        final MessagingMessage retval = messageAccess.getMessage("folder", "id", true);

        assertSame(message, retval);
    }

    @Test
    public void testBulkLoadingTriesCache() throws OXException {
        final SimMessageAccess access = new SimMessageAccess();
        final TestCacheMessageAccess messageAccess = new TestCacheMessageAccess(access);

        final SimpleMessagingMessage message = new SimpleMessagingMessage();
        messageAccess.setCached(message);

        final List<MessagingMessage> messages = messageAccess.getMessages("folder", new String[] { "a", "b", "c" }, null);

        assertEquals(3, messages.size());
        for (final MessagingMessage messagingMessage : messages) {
            assertSame(message, messagingMessage);
        }

    }

    @Test
    public void testGetFallbackOnCacheMiss() throws OXException {
        final SimMessageAccess access = new SimMessageAccess();
        final TestCacheMessageAccess messageAccess = new TestCacheMessageAccess(access);

        final SimpleMessagingMessage message = new SimpleMessagingMessage();
        access.setTemplateMessage(message);

        final MessagingMessage retval = messageAccess.getMessage("folder", "id", true);

        assertSame(message, retval);
        final List<MessagingMessage> rememberedMessages = messageAccess.getRememberedMessages();

        assertEquals(1, rememberedMessages.size());

        assertSame(message, rememberedMessages.get(0));
    }

    @Test
    public void testBulkFallbackOnCacheMiss() throws OXException {
        final SimMessageAccess access = new SimMessageAccess();
        final TestCacheMessageAccess messageAccess = new TestCacheMessageAccess(access);

        final SimpleMessagingMessage cachedMessage = new SimpleMessagingMessage();
        final SimpleMessagingMessage storedMessage = new SimpleMessagingMessage();
        storedMessage.setId("b");

        messageAccess.setCached(cachedMessage);
        messageAccess.forgetCachedAfterFirstHit();

        access.setTemplateMessage(storedMessage);

        messageAccess.forgetCachedAfterFirstHit();

        final List<MessagingMessage> messages = messageAccess.getMessages("folder", new String[] { "a", "b" }, null);

        assertEquals(2, messages.size());

        assertSame(cachedMessage, messages.get(0));
        assertSame(storedMessage, messages.get(1));
    }

    @Test
    public void testAllRefreshesCache() throws OXException {
        final SimMessageAccess access = new SimMessageAccess();
        final TestCacheMessageAccess messageAccess = new TestCacheMessageAccess(access);

        final SimpleMessagingMessage cachedMessage = new SimpleMessagingMessage();
        final SimpleMessagingMessage storedMessage = new SimpleMessagingMessage();

        messageAccess.setCached(cachedMessage);
        messageAccess.forgetCachedAfterFirstHit();

        access.setTemplateMessage(storedMessage);

        messageAccess.forgetCachedAfterFirstHit();

        final List<MessagingMessage> messages = messageAccess.getAllMessages("folder", IndexRange.NULL, null, null, MessagingField.class.cast(null));

        assertEquals(1, messages.size());

        assertSame(storedMessage, messages.get(0));
        assertEquals("folder", messageAccess.getClearedFolder());
    }

    private static final class TestCacheMessageAccess extends CacheingMessageAccess {

        private MessagingMessage cached;
        private final List<MessagingMessage> rememberedMessages = new LinkedList<MessagingMessage>();
        private boolean forgetAfterFirstHit;
        private String clearedFolder;

        public TestCacheMessageAccess(final MessagingMessageAccess delegate) {
            super(delegate, null, null, null);
        }

        public void forgetCachedAfterFirstHit() {
            forgetAfterFirstHit = true;
        }

        public List<MessagingMessage> getRememberedMessages() {
            return rememberedMessages;
        }

        public void setCached(final MessagingMessage message) {
            cached = message;
        }

        @Override
        protected MessagingMessage remember(final MessagingMessage message) {
            rememberedMessages.add(message);
            return message;
        }

        @Override
        protected MessagingMessage get(final String folder, final String id) {
            final MessagingMessage retval = cached;
            if (forgetAfterFirstHit) {
                cached = null;
            }
            return retval;
        }

        @Override
        protected void clear(final String folderId) {
            clearedFolder = folderId;
        }

        public String getClearedFolder() {
            return clearedFolder;
        }
    }
}
