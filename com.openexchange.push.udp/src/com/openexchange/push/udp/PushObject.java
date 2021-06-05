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

import java.util.Arrays;
import java.util.Date;
import com.openexchange.tools.StringCollection;

/**
 * {@link PushObject} - The push object.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class PushObject extends AbstractPushObject {

    private final int folderId;

    private final int module;

    private final int users[];

    private final Date creationDate = new Date();

    private final int hash;

    private final long timestamp;

    /**
     * Initializes a new {@link PushObject}.
     *
     * @param folderId The folder ID
     * @param module The module
     * @param contextId The context ID
     * @param users The user IDs as an array
     * @param isRemote <code>true</code> to mark this push object as remotely received; otherwise <code>false</code>
     */
    public PushObject(final int folderId, final int module, final int contextId, final int[] users, final boolean isRemote, final long timestamp) {
        super(contextId, isRemote);
        this.folderId = folderId;
        this.module = module;
        this.users = users;
        hash = hashCode0();
        this.timestamp = timestamp;
    }

    private int hashCode0() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + folderId;
        result = prime * result + module;
        result = prime * result + Arrays.hashCode(users);
        return result;
    }

    /**
     * Gets the folder ID.
     *
     * @return The folder ID
     */
    public int getFolderId() {
        return folderId;
    }

    /**
     * Gets the module.
     *
     * @return The module
     */
    public int getModule() {
        return module;
    }

    /**
     * Gets the user IDs as an array.
     *
     * @return The user IDs as an array
     */
    public int[] getUsers() {
        return users;
    }

    /**
     * Gets the creation date.
     *
     * @return The creation date
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Gets the time stamp or <code>0</code> if not available.
     *
     * @return The time stamp or <code>0</code> if not available
     */
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + folderId;
        result = prime * result + module;
        result = prime * result + Arrays.hashCode(users);
        return result;
    }

    public int hashCode1() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PushObject other = (PushObject) obj;
        if (folderId != other.folderId) {
            return false;
        }
        if (module != other.module) {
            return false;
        }
        if (!Arrays.equals(users, other.users)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("FOLDER_ID=").append(folderId).append(",MODULE=").append(module).append(",CONTEXT_ID=").append(
            getContextId()).append(",USERS=").append(StringCollection.convertArray2String(users)).append(",IS_REMOTE=").append(isRemote()).toString();
    }
}
