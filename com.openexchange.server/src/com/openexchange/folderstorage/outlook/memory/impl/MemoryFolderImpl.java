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

package com.openexchange.folderstorage.outlook.memory.impl;

import java.util.Date;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.outlook.memory.MemoryFolder;

/**
 * {@link MemoryFolderImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MemoryFolderImpl implements MemoryFolder {

    private String treeId;

    private String id;

    private String name;

    private String parentId;

    private Permission[] permissions;

    private int modifiedBy;

    private Date lastModified;

    private Boolean subscribed;

    private int sortNum;

    /**
     * Initializes a new {@link MemoryFolderImpl}.
     */
    public MemoryFolderImpl() {
        super();
        sortNum = 0;
    }

    @Override
    public Boolean getSubscribed() {
        return subscribed;
    }

    /**
     * Sets the subscribed
     *
     * @param subscribed The subscribed to set
     */
    public void setSubscribed(final Boolean subscribed) {
        this.subscribed = subscribed;
    }

    @Override
    public String getTreeId() {
        return treeId;
    }

    /**
     * Sets the treeId
     *
     * @param treeId The treeId to set
     */
    public void setTreeId(final String treeId) {
        this.treeId = treeId;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Sets the id
     *
     * @param id The id to set
     */
    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the name
     *
     * @param name The name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getParentId() {
        return parentId;
    }

    /**
     * Sets the parentId
     *
     * @param parentId The parentId to set
     */
    public void setParentId(final String parentId) {
        this.parentId = parentId;
    }

    @Override
    public Permission[] getPermissions() {
        return permissions;
    }

    /**
     * Sets the permissions
     *
     * @param permissions The permissions to set
     */
    public void setPermissions(final Permission[] permissions) {
        this.permissions = permissions;
    }

    @Override
    public int getModifiedBy() {
        return modifiedBy;
    }

    /**
     * Sets the modifiedBy
     *
     * @param modifiedBy The modifiedBy to set
     */
    public void setModifiedBy(final int modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Override
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Sets the lastModified
     *
     * @param lastModified The lastModified to set
     */
    public void setLastModified(final Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public int getSortNum() {
        return sortNum;
    }

    /**
     * Sets the sortNum
     *
     * @param sortNum The sortNum to set
     */
    public void setSortNum(final int sortNum) {
        this.sortNum = sortNum;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
        result = prime * result + ((treeId == null) ? 0 : treeId.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MemoryFolderImpl)) {
            return false;
        }
        final MemoryFolderImpl other = (MemoryFolderImpl) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (parentId == null) {
            if (other.parentId != null) {
                return false;
            }
        } else if (!parentId.equals(other.parentId)) {
            return false;
        }
        if (treeId == null) {
            if (other.treeId != null) {
                return false;
            }
        } else if (!treeId.equals(other.treeId)) {
            return false;
        }
        return true;
    }

}
