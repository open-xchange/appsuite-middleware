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

package com.openexchange.push.udp;

import java.net.InetAddress;
import java.util.Date;

/**
 * {@link RemoteHostObject} - Represents a remote Open-Xchange server.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public final class RemoteHostObject {

    private InetAddress host;

    private int port;

    private Date timer = new Date();

    /**
     * Initializes a new {@link RemoteHostObject}.
     */
    public RemoteHostObject() {
        super();
        timer = new Date();
    }

    public InetAddress getHost() {
        return host;
    }

    public void setHost(final InetAddress host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public Date getTimer() {
        return timer;
    }

    public void setTimer(final Date timer) {
        this.timer = timer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoteHostObject other = (RemoteHostObject) obj;
        if (host == null) {
            if (other.host != null) {
                return false;
            }
        } else if (!host.getHostAddress().equals(other.host.getHostAddress())) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + port;
        return result;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("HOST=").append(getHost()).append(",PORT=").append(getPort()).append(",TIMER=").append(getTimer()).toString();
    }
}
