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

package com.openexchange.websockets;

import java.util.Set;

/**
 * {@link WebSocketSession} - A session associated with a Web Socket to store states.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface WebSocketSession {

    /**
     * Gets the object bound with the specified name in this session, or
     * <code>null</code> if no object is bound under the name.
     *
     * @param name The string specifying the name of the object
     * @return The object bound to the specified name or <code>null</code>
     */
    <V> V getAttribute(String name);

    /**
     * Gets the set view for the names of all the objects bound to this session.
     *
     * @return The set view for attribute names
     */
    Set<String> getAttributeNames();

    /**
     * Binds an object to this session, using the name specified.
     * <p>
     * If an object of the same name is already bound to the session, the object is replaced.
     * <p>
     * If the value passed in is <code>null</code>, this has the same effect as calling <code>removeAttribute()</code>.
     *
     *
     * @param name The name to which the object is bound
     * @param value The object to be bound
     */
    void setAttribute(String name, Object value);

    /**
     * Removes the object bound with the specified name from this session.
     * <p>
     * If the session does not have an object bound with the specified name, this method does nothing.
     *
     * @param name The name of the object to remove from this session
     */
    void removeAttribute(String name);

}
