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

import java.util.Date;

/**
 * {@link RegisterObject} - Represents a registered client waiting for remote events.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class RegisterObject extends AbstractPushObject {

    private final int userId;
    private final String hostAddress;
    private final int port;
    private final Date timestamp;

    public RegisterObject(final int userId, final int contextId, final String hostAddress, final int port, final boolean isSync) {
        super(contextId, isSync);
        this.userId = userId;
        this.hostAddress = hostAddress;
        this.port = port;
        timestamp = new Date();
    }

    public int getUserId() {
        return userId;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public int getPort() {
        return port;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("USER_ID=").append(userId).append(",CONTEXT_ID=").append(getContextId()).append(",ADDRESS=").append(
            hostAddress).append(",PORT").append(port).toString();
    }
}
