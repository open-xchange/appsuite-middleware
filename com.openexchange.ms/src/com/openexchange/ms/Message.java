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

import java.util.EventObject;

/**
 * {@link Message} - Represents a message e.g. published through a topic
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Message<E> extends EventObject {

    private static final long serialVersionUID = -6955173203274621420L;

    /** The message object */
    protected final E messageObject;

    /** The sender identifier */
    protected final String senderId;

    /** Whether this message has its origin from a remote sender */
    protected final boolean remote;

    /**
     * Initializes a new {@link Message}.
     * 
     * @param name The name of associated topic/queue
     * @param senderId The identifier of the resource that dispatched this message
     * @param messageObject The message's object
     * @param remote Whether this message has its origin from a remote sender
     */
    public Message(final String name, final String senderId, final E messageObject, final boolean remote) {
        super(name);
        this.messageObject = messageObject;
        this.senderId = senderId;
        this.remote = remote;
    }

    /**
     * Whether this message has its origin from a remote sender.
     * 
     * @return <code>true</code> if remote origin; otherwise <code>false</code>
     */
    public boolean isRemote() {
        return remote;
    }

    /**
     * Gets the name of associated topic/queue.
     * 
     * @return The name
     */
    public String getName() {
        return getSource().toString();
    }

    /**
     * Gets this message's sender identifier,
     * 
     * @return The sender identifier
     */
    public String getSenderId() {
        return senderId;
    }

    /**
     * Gets the message's object.
     * 
     * @return The message's object
     */
    public E getMessageObject() {
        return messageObject;
    }
}
