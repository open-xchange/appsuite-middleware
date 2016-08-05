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
 * {@link Removal} - Represents a remove operation from Hazelcast multi-map.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class Removal {

    private final int userId;
    private final int contextId;
    private final String memberUuid;
    private final String connectionId;
    private volatile Integer hash;

    /**
     * Initializes a new {@link Removal}.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param memberUuid The member UUID
     * @param connectionId The identifier of the Web Socket connection
     */
    public Removal(int userId, int contextId, String memberUuid, String connectionId) {
        super();
        this.userId = userId;
        this.contextId = contextId;
        this.memberUuid = memberUuid;
        this.connectionId = connectionId;
    }

    @Override
    public int hashCode() {
        Integer tmp = hash;
        if (null == tmp) {
            int prime = 31;
            int result = 1;
            result = prime * result + contextId;
            result = prime * result + userId;
            result = prime * result + ((memberUuid == null) ? 0 : memberUuid.hashCode());
            result = prime * result + ((connectionId == null) ? 0 : connectionId.hashCode());
            tmp = Integer.valueOf(result);
            hash = tmp;
        }
        return tmp.intValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Removal)) {
            return false;
        }
        Removal other = (Removal) obj;
        if (contextId != other.contextId) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        if (memberUuid == null) {
            if (other.memberUuid != null) {
                return false;
            }
        } else if (!memberUuid.equals(other.memberUuid)) {
            return false;
        }
        if (connectionId == null) {
            if (other.connectionId != null) {
                return false;
            }
        } else if (!connectionId.equals(other.connectionId)) {
            return false;
        }
        return true;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the member UUID
     *
     * @return The member UUID
     */
    public String getMemberUuid() {
        return memberUuid;
    }

    /**
     * Gets the identifier of the Web Socket connection
     *
     * @return The connection identifier
     */
    public String getConnectionId() {
        return connectionId;
    }

}
