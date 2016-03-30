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
