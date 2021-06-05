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

package com.openexchange.folderstorage;


/**
 * {@link ImmutableTypePermission} is an implementation of the {@link BasicPermission} class which only allows to change the type of the permission once.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public final class ImmutableTypePermission extends BasicPermission {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Initializes a new {@link ImmutableTypePermission}.
     *
     * @param entity
     * @param group
     * @param bits
     */
    public ImmutableTypePermission(int entity, boolean group, int bits) {
        super(entity, group, bits);
    }

    @Override
    public void setType(FolderPermissionType type) {
        if (this.type == null) {
            super.setType(type);
        }
    }

    @Override
    public Object clone() {
        // Clones are not immutable and therefore a new BasicPermission is returned
        BasicPermission clone = new BasicPermission();
        clone.identifier = getIdentifier();
        clone.entity = getEntity();
        clone.entityInfo = getEntityInfo();
        clone.group = isGroup();
        clone.system = getSystem();
        clone.type = getType();
        clone.legator = getPermissionLegator();
        clone.admin = isAdmin();
        clone.folderPermission = getFolderPermission();
        clone.readPermission = getReadPermission();
        clone.writePermission = getWritePermission();
        clone.deletePermission = getDeletePermission();
        return clone;
    }

}
