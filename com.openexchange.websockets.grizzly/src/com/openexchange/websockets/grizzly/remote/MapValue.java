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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.websockets.grizzly.remote;

/**
 * {@link MapValue} - The value for Hazelcast map.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
class MapValue {

    /**
     * Parses the map value information from specified string.
     *
     * @param socketInfo The socket information as string
     * @return The parsed value
     */
    static MapValue parseFrom(String socketInfo) {
        if (null == socketInfo) {
            return null;
        }

        int pos = socketInfo.lastIndexOf(':');
        if (pos <= 0) {
            throw new IllegalArgumentException("Illegal socket info: " + socketInfo);
        }

        return new MapValue(socketInfo.substring(0, pos), ++pos == socketInfo.length() ? null : socketInfo.substring(pos));
    }

    /**
     * Parses the Web Socket path information from specified string.
     *
     * @param socketInfo The socket information as string
     * @return The parsed path
     */
    static String parsePathFrom(String socketInfo) {
        if (null == socketInfo) {
            return null;
        }

        int pos = socketInfo.lastIndexOf(':');
        if (pos <= 0) {
            throw new IllegalArgumentException("Illegal socket info: " + socketInfo);
        }

        return ++pos == socketInfo.length() ? null : socketInfo.substring(pos);
    }

    // ------------------------------------------------------------------------------------

    private final String connectionId;
    private final String path;

    private int hash;

    /**
     * Initializes a new {@link MapValue}.
     *
     * @param connectionId The connection identifier
     * @param path The path
     */
    MapValue(String connectionId, String path) {
        super();
        this.connectionId = connectionId;
        this.path = path;
    }

    /**
     * Gets the connection identifier
     *
     * @return The connection identifier
     */
    String getConnectionId() {
        return connectionId;
    }

    /**
     * Gets the path
     *
     * @return The path
     */
    String getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        // No need to be thread-safe. Each thread then computes itself in worst case
        int h = hash;
        if (h == 0 && (null != connectionId || null != path)) {
            int prime = 31;
            h = prime * 1 + ((connectionId == null) ? 0 : connectionId.hashCode());
            h = prime * h + ((path == null) ? 0 : path.hashCode());
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MapValue)) {
            return false;
        }
        MapValue other = (MapValue) obj;
        if (connectionId == null) {
            if (other.connectionId != null) {
                return false;
            }
        } else if (!connectionId.equals(other.connectionId)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(48);
        builder.append("{");
        if (connectionId != null) {
            builder.append("connectionId=").append(connectionId).append(", ");
        }
        if (path != null) {
            builder.append("path=").append(path);
        }
        builder.append("}");
        return builder.toString();
    }

}
