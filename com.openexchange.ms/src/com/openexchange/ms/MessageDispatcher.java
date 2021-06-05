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

/**
 * {@link MessageDispatcher} - The super type for message dispatching.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @see Topic
 * @see Queue
 */
public interface MessageDispatcher<E> {

    /**
     * Gets the topic's name
     *
     * @return The name
     */
    String getName();

    /**
     * Adds given listener to this topic.
     *
     * @param listener The listener
     */
    void addMessageListener(MessageListener<E> listener);

    /**
     * Removes given listener from this topic
     *
     * @param listener The listener
     */
    void removeMessageListener(MessageListener<E> listener);

    /**
     * Gets the sender identifier,
     *
     * @return The sender identifier
     */
    String getSenderId();

    /**
     * Destroys this instance.
     * <p>
     * Clears and releases all resources for this instance.
     */
    void destroy();
}
