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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
