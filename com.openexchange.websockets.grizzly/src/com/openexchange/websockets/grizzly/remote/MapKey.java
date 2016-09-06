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
 * {@link MapKey} - A key for Hazelcast map.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
class MapKey {

    /**
     * Parses the map key information from specified string.
     *
     * @param key The key as string
     * @return The parsed key
     * @throws IllegalArgumentException If key cannot be parsed
     */
    static MapKey parseFrom(String key) {
        int atPos = key.indexOf('@', 0);
        if (atPos < 0) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }

        int usPos = key.indexOf('_', atPos + 1);
        if (usPos < 0) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }

        try {
            return new MapKey(Integer.parseInt(key.substring(0, atPos)), Integer.parseInt(key.substring(atPos + 1, usPos)), key.substring(usPos + 1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid key: " + key, e);
        }
    }

    // ---------------------------------------------------------------------------------

    private final int userId;
    private final int contextId;
    private final String address;

    /**
     * Initializes a new {@link MapKey}.
     */
    MapKey(int userId, int contextId, String address) {
        super();
        this.userId = userId;
        this.contextId = contextId;
        this.address = address;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    int getUserId() {
        return userId;
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    int getContextId() {
        return contextId;
    }

    /**
     * Gets the member address; e.g. <code>"192.168.2.109:5557"</code>.
     *
     * @return The member address
     */
    String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(48);
        builder.append("{userId=").append(userId).append(", contextId=").append(contextId).append(", ");
        if (address != null) {
            builder.append("address=").append(address);
        }
        builder.append("}");
        return builder.toString();
    }

}
