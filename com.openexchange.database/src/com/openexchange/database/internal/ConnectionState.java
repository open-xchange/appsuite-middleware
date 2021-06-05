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

package com.openexchange.database.internal;

/**
 * {@link ConnectionState} - Tracks a connection state.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 */
public class ConnectionState {

    private boolean usedAsRead;
    private boolean usedForUpdate;
    private boolean updateCommitted;
    private boolean heartbeatEnabled;

    /**
     * Initializes a new {@link ConnectionState}.
     *
     * @param usedAsRead <code>true</code> if a read-write connection has been intentionally used for reading; otherwise <code>false</code>
     */
    public ConnectionState(boolean usedAsRead) {
        super();
        this.usedAsRead = usedAsRead;
        usedForUpdate = false;
        updateCommitted = false;
        heartbeatEnabled = false;
    }

    /**
     * Checks if heart-beat has been enabled.
     *
     * @return The heart-beat enabled flag
     */
    public boolean isHeartbeatEnabled() {
        return heartbeatEnabled;
    }

    /**
     * Sets the heart-beat enabled flag
     *
     * @param heartbeatEnabled The heart-beat enabled flag
     */
    public void setHeartbeatEnabled(boolean heartbeatEnabled) {
        this.heartbeatEnabled = heartbeatEnabled;
    }

    /**
     * Checks if a read-write connection has been intentionally used for reading.
     *
     * @return <code>true</code> if a read-write connection has been intentionally used for reading; otherwise <code>false</code>
     */
    public boolean isUsedAsRead() {
        return usedAsRead;
    }

    /**
     * Sets if a read-write connection has been intentionally used for reading.
     *
     * @param usedAsRead <code>true</code> if a read-write connection has been intentionally used for reading; otherwise <code>false</code>
     */
    public void setUsedAsRead(boolean usedAsRead) {
        this.usedAsRead = usedAsRead;
    }

    /**
     * Checks if associated connection has been used for a data modification operation; such as <code>INSERT</code>, <code>UPDATE</code>, <code>DELETE</code>.
     *
     * @return <code>true</code> for a data modification operation; otherwise <code>false</code>
     */
    public boolean isUsedForUpdate() {
        return usedForUpdate;
    }

    /**
     * Sets if associated connection has been used for a data modification operation; such as <code>INSERT</code>, <code>UPDATE</code>, <code>DELETE</code>.
     *
     * @param usedForUpdate <code>true</code> for a data modification operation; otherwise <code>false</code>
     */
    public void setUsedForUpdate(boolean usedForUpdate) {
        this.usedForUpdate = usedForUpdate;
    }

    /**
     * Checks if a <code>COMMIT</code> has been invoked on associated connection.
     *
     * @return <code>true</code> if a <code>COMMIT</code> has been invoked; otherwise <code>false</code>
     */
    public boolean isUpdateCommitted() {
        return updateCommitted;
    }

    /**
     * Sets if a <code>COMMIT</code> has been invoked on associated connection.
     *
     * @param updateCommitted <code>true</code> if a <code>COMMIT</code> has been invoked; otherwise <code>false</code>
     */
    public void setUpdateCommitted(boolean updateCommitted) {
        this.updateCommitted = updateCommitted;
    }

}
