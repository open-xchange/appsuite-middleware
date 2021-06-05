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

package com.openexchange.drive.impl.comparison;

import com.openexchange.drive.DriveVersion;


/**
 * {@link ThreeWayComparison}
 *
 * Determines the synchronization state based on the presence and state of three drive versions. This type of comparison involves the
 * server version, the client version variant and a base original version. The base version represents a common ancestor for the local
 * (server) and remote (client) versions.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @param <T>
 */
public class ThreeWayComparison<T extends DriveVersion> {

    private T clientVersion;
    private T originalVersion;
    private T serverVersion;

    /**
     * Initializes a new {@link ThreeWayComparison}.
     */
    public ThreeWayComparison() {
        super();
    }

    /**
     * Gets the {@link Change} between the original and the server version.
     *
     * @return The change between the original and server version
     */
    public Change getServerChange() {
        return Change.get(originalVersion, serverVersion);
    }

    /**
     * Gets the {@link Change} between the original and the client version.
     *
     * @return The change between the original and client version
     */
    public Change getClientChange() {
        return Change.get(originalVersion, clientVersion);
    }

    /**
     * Gets the client version.
     *
     * @return The clientVersion, or <code>null</code> if not set.
     */
    public T getClientVersion() {
        return clientVersion;
    }

    /**
     * Sets the client version
     *
     * @param clientVersion The client version to set
     */
    public void setClientVersion(T clientVersion) {
        this.clientVersion = clientVersion;
    }

    /**
     * Gets the original version
     *
     * @return The original version, or <code>null</code> if not set.
     */
    public T getOriginalVersion() {
        return originalVersion;
    }

    /**
     * Sets the originalVersion
     *
     * @param originalVersion The originalVersion to set
     */
    public void setOriginalVersion(T originalVersion) {
        this.originalVersion = originalVersion;
    }

    /**
     * Gets the server version
     *
     * @return The server version, or <code>null</code> if not set.
     */
    public T getServerVersion() {
        return serverVersion;
    }

    /**
     * Sets the server version
     *
     * @param serverVersion The server version to set
     */
    public void setServerVersion(T serverVersion) {
        this.serverVersion = serverVersion;
    }

    @Override
    public String toString() {
        return "ThreeWayComparison [clientVersion=" + clientVersion + ", originalVersion=" + originalVersion + ", serverVersion=" + serverVersion + "]";
    }

}
