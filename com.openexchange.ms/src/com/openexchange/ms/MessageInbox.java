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

package com.openexchange.ms;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * {@link MessageInbox} - The message Inbox for direct messages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MessageInbox {

    /**
     * Returns an iterator over the messages in this Inbox.
     * <p>
     * There are no guarantees concerning the order in which the elements are returned.
     *
     * @return An <tt>Iterator</tt> over the elements in this collection
     */
    Iterator<Message<?>> iterator();

    /**
     * Checks if this Inbox is currently empty.
     *
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    boolean isEmpty();

    /**
     * Retrieves and removes the first message in this Inbox, or returns <tt>null</tt> if empty.
     *
     * @return The first message, or <tt>null</tt> if this queue is empty
     */
    Message<?> poll();

    /**
     * Retrieves but does not remove the first message in this Inbox, or returns <tt>null</tt> if empty.
     *
     * @return The first message, or <tt>null</tt> if this queue is empty
     */
    Message<?> peek();

    /**
     * Retrieves and removes the first message in this Inbox, waiting if necessary until a message arrives.
     *
     * @return The first (incoming) message
     * @throws InterruptedException If interrupted while waiting
     */
    Message<?> take() throws InterruptedException;

    /**
     * Retrieves and removes the first message in this Inbox, waiting up to the specified wait time if necessary for a message to arrive.
     *
     * @param timeout how long to wait before giving up, in units of <tt>unit</tt>
     * @param unit a <tt>TimeUnit</tt> determining how to interpret the <tt>timeout</tt> parameter
     * @return The first (incoming) message, or <tt>null</tt> if the specified waiting time elapses before a message arrived
     * @throws InterruptedException If interrupted while waiting
     */
    Message<?> poll(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Removes all of the messages from this Inbox.
     */
    void clear();

}
