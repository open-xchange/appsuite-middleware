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

package com.openexchange.ms.internal;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import com.openexchange.ms.Message;
import com.openexchange.ms.MessageInbox;

/**
 * {@link MessageInboxImpl}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessageInboxImpl implements MessageInbox {

    private static final MessageInboxImpl INSTANCE = new MessageInboxImpl();

    /**
     * Gets the Inbox instance.
     * 
     * @return The instance
     */
    public static MessageInboxImpl getInstance() {
        return INSTANCE;
    }

    private final BlockingQueue<Message<?>> blockingQueue;

    /**
     * Initializes a new {@link MessageInboxImpl}.
     */
    private MessageInboxImpl() {
        super();
        blockingQueue = new LinkedBlockingQueue<Message<?>>();
    }

    @Override
    public Iterator<Message<?>> iterator() {
        return blockingQueue.iterator();
    }

    @Override
    public boolean isEmpty() {
        return blockingQueue.isEmpty();
    }

    /**
     * Inserts the specified message into this Inbox if it is possible to do so immediately. Returning <tt>true</tt> upon success.
     * 
     * @param e The message to add
     * @return <tt>true</tt> if the message was added to this Inbox, else <tt>false</tt>
     */
    public boolean offer(final Message<?> e) {
        return blockingQueue.offer(e);
    }

    @Override
    public Message<?> poll() {
        return blockingQueue.poll();
    }

    @Override
    public Message<?> peek() {
        return blockingQueue.peek();
    }

    @Override
    public Message<?> take() throws InterruptedException {
        return blockingQueue.take();
    }

    @Override
    public Message<?> poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        return blockingQueue.poll(timeout, unit);
    }

    @Override
    public void clear() {
        blockingQueue.clear();
    }

}
