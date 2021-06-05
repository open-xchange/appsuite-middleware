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

package com.openexchange.passwordchange.history.impl;

import com.openexchange.passwordchange.history.PasswordChangeInfo;

/**
 * {@link PasswordChangeInfoImpl} - Implementation of {@link PasswordChangeInfo}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class PasswordChangeInfoImpl implements PasswordChangeInfo {

    private final long created;
    private final String client;
    private final String ip;

    /**
     * Initializes a new {@link PasswordChangeInfoImpl}.
     *
     * @param created The time when the password change was made
     * @param client The client that did the change
     * @param ip The optional IP of the client
     */
    public PasswordChangeInfoImpl(long created, String client, String ip) {
        super();
        this.created = created;
        this.client = client;
        this.ip = ip;
    }

    @Override
    public long getCreated() {
        return created;
    }

    @Override
    public String getClient() {
        return client;
    }

    @Override
    public String getIP() {
        return ip;
    }

    @Override
    public int hashCode() {
        final int prime = 61;
        int result = 1;
        result = prime * result + (int) (created ^ (created >>> 32));
        result = prime * result + ((ip == null) ? 0 : ip.hashCode());
        result = prime * result + ((client == null) ? 0 : client.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PasswordChangeInfoImpl other = (PasswordChangeInfoImpl) obj;
        if (created != other.created) {
            return false;
        }
        if (ip == null) {
            if (other.ip != null) {
                return false;
            }
        } else if (!ip.equals(other.ip)) {
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
}
