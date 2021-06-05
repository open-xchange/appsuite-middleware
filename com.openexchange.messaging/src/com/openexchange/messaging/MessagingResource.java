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

package com.openexchange.messaging;

import com.openexchange.exception.OXException;

/**
 * {@link MessagingResource} - A messaging resource which is {@link #connect() connectable}, {@link #close() closeable} and {@link #ping()
 * pingable}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public interface MessagingResource {

    /**
     * Opens this resource. May be invoked on an already opened resource.
     *
     * @throws OXException If the resource could not be opened for various reasons
     */
    void connect() throws OXException;

    /**
     * Checks if this connection is currently connected.
     *
     * @return <code>true</code> if connected; otherwise <code>false</code>
     */
    public boolean isConnected();

    /**
     * Closes this resource. May be invoked on an already closed resource.
     */
    void close();

    /**
     * Pings this resource to check if it can be opened and and immediately closes connection.
     *
     * @return <code>true</code> if a connection can be established; otherwise <code>false</code>
     * @throws OXException If the ping fails
     */
    boolean ping() throws OXException;

    /**
     * Indicates if this resource may be cached (for a little amount of time) once opened.
     *
     * @return <code>true</code> if this resource may be cached; otherwise <code>false</code>
     */
    boolean cacheable();

}
