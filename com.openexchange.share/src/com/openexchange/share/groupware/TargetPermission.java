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

package com.openexchange.share.groupware;

import com.openexchange.share.ShareTarget;


/**
 * A {@link TargetPermission} instance represents a permission of a groupware object
 * for a certain {@link ShareTarget}. The permission bits must be always defined as a full
 * folder permission bitmask. For items, the bits for reading, writing and deleting all(!)
 * objects in folders are taken into account. The highest value will be chosen based on the
 * following sequence: delete all > write all > read all.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class TargetPermission {

    private final int entityId;

    private final boolean isGroup;

    private final int permissionBits;

    /**
     * Initializes a new {@link TargetPermission}.
     *
     * @param entityId The entity ID (i.e. user or group ID)
     * @param isGroup <code>true</code> if this entity is a group
     * @param permissionBits The permission bits (folder permission bit mask)
     */
    public TargetPermission(int entityId, boolean isGroup, int permissionBits) {
        super();
        this.entityId = entityId;
        this.permissionBits = permissionBits;
        this.isGroup = isGroup;
    }

    /**
     * Gets the ID of the entity denoted by this permission instance.
     *
     * @return The entity ID
     */
    public int getEntity() {
        return entityId;
    }

    /**
     * Gets the permission bits (folder permission bit mask).
     *
     * @return The bit mask
     */
    public int getBits() {
        return permissionBits;
    }

    /**
     * Gets whether the denoted entity is a group or not.
     *
     * @return <code>true</code> if this entity is a group, <code>false</code>
     *         if it's a user
     */
    public boolean isGroup() {
        return isGroup;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + entityId;
        result = prime * result + (isGroup ? 1231 : 1237);
        result = prime * result + permissionBits;
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
        TargetPermission other = (TargetPermission) obj;
        if (entityId != other.entityId) {
            return false;
        }
        if (isGroup != other.isGroup) {
            return false;
        }
        if (permissionBits != other.permissionBits) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TargetPermission [entityId=" + entityId + ", isGroup=" + isGroup + ", permissionBits=" + permissionBits + "]";
    }

}
