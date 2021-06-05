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

package com.openexchange.monitoring.sockets;

/**
 * {@link SocketStatus} - A socket status.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public enum SocketStatus {

    /** A regular read was performed; socket appears to be OK */
    OK("OK"),
    /** A read attempt encountered a timeout */
    TIMED_OUT("TIMED_OUT"),
    /** An EOF was sent during read attempt; either expectedly or unexpectedly */
    EOF("END_OF_FILE"),
    /** An I/O error occurred while trying to read from socket */
    READ_ERROR("READ_ERROR"),
    /** A connect error occurred while trying establish a socket connection */
    CONNECT_ERROR("CONNECT_ERROR"),
    ;

    private final String id;

    private SocketStatus(String id) {
        this.id = id;
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }
}