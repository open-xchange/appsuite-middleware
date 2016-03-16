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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TargetPermission other = (TargetPermission) obj;
        if (entityId != other.entityId)
            return false;
        if (isGroup != other.isGroup)
            return false;
        if (permissionBits != other.permissionBits)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TargetPermission [entityId=" + entityId + ", isGroup=" + isGroup + ", permissionBits=" + permissionBits + "]";
    }

}
