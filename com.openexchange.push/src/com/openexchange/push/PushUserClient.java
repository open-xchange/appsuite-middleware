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

package com.openexchange.push;

/**
 * {@link PushUserClient} - The push user client.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class PushUserClient implements Comparable<PushUserClient> {

    private final PushUser pushUser;
    private final String client;
    private final int hash;

    /**
     * Initializes a new {@link PushUserClient}.
     *
     * @param pushUser The associated push user
     * @param client The identifier of the associated client
     */
    public PushUserClient(PushUser pushUser, String client) {
        super();
        this.pushUser = pushUser;
        this.client = client;

        int prime = 31;
        int result = prime * 1 + ((client == null) ? 0 : client.hashCode());
        result = prime * result + ((pushUser == null) ? 0 : pushUser.hashCode());
        hash = result;
    }

    /**
     * Gets the push user
     *
     * @return The push user
     */
    public PushUser getPushUser() {
        return pushUser;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return pushUser.getUserId();
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        return pushUser.getContextId();
    }

    /**
     * Gets the client
     *
     * @return The client
     */
    public String getClient() {
        return client;
    }

    @Override
    public int compareTo(PushUserClient o) {
        return pushUser.compareTo(o.pushUser);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PushUserClient)) {
            return false;
        }
        PushUserClient other = (PushUserClient) obj;
        if (pushUser == null) {
            if (other.pushUser != null) {
                return false;
            }
        } else if (!pushUser.equals(other.pushUser)) {
            return false;
        }
        if (client == null) {
            if (other.client != null) {
                return false;
            }
        } else if (!client.equals(other.client)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder(48).append("[userId=").append(pushUser.getUserId()).append(", contextId=").append(pushUser.getContextId()).append(", client=").append(client).append(']').toString();
    }

}
