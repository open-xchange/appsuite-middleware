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

/**
 * {@link SubfolderAwareTargetPermission}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class SubfolderAwareTargetPermission extends TargetPermission {

    private final int type;
    private final String legator;
    private final int system;

    /**
     * Initializes a new {@link SubfolderAwareTargetPermission}.
     * @param entityId
     * @param isGroup
     * @param permissionBits
     * @param includeSubfolders
     */
    public SubfolderAwareTargetPermission(int entityId, boolean isGroup, int permissionBits, int type, String legator, int system) {
        super(entityId, isGroup, permissionBits);
        this.type = type;
        this.legator = legator;
        this.system = system;
    }

    /**
     * Gets this folder permission's type.
     *
     * @return This folder permission's type.
     */
    public int getType() {
        return type;
    }

    /**
     * If this permission is handed down from a parent folder this method retrieves the sharing parent folder id.
     *
     * @return This sharing folder id
     */
    public String getPermissionLegator() {
       return legator;
    }

    /**
     * Gets the system
     *
     * @return The system
     */
    public int getSystem() {
        return system;
    }

}
