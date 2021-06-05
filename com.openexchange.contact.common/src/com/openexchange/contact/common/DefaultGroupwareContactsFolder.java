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

package com.openexchange.contact.common;

import java.util.Date;
import java.util.Map;

/**
 * {@link DefaultGroupwareContactsFolder}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class DefaultGroupwareContactsFolder extends DefaultContactsFolder implements GroupwareContactsFolder {

    private String parentId;
    private boolean isDefaultFolder;
    private int modifiedBy;
    private int createdBy;
    private Date creationDate;
    private GroupwareFolderType folderType;
    private Map<String, Object> meta;

    /**
     * Initializes a new {@link DefaultGroupwareContactsFolder}.
     */
    public DefaultGroupwareContactsFolder() {
        super();
    }

    /**
     * Initializes a new {@link DefaultGroupwareContactsFolder}, taking over the properties from another folder.
     *
     * @param folder The folder to copy the properties from
     */
    public DefaultGroupwareContactsFolder(ContactsFolder folder) {
        super(folder);
        if (GroupwareContactsFolder.class.isInstance(folder)) {
            GroupwareContactsFolder groupwareFolder = (GroupwareContactsFolder) folder;
            parentId = groupwareFolder.getParentId();
            isDefaultFolder = groupwareFolder.isDefaultFolder();
            modifiedBy = groupwareFolder.getModifiedBy();
            createdBy = groupwareFolder.getCreatedBy();
            creationDate = groupwareFolder.getCreationDate();
            folderType = groupwareFolder.getType();
            meta = groupwareFolder.getMeta();
        }
    }

    @Override
    public boolean isDefaultFolder() {
        return isDefaultFolder;
    }

    public void setDefaultFolder(boolean isDefaultFolder) {
        this.isDefaultFolder = isDefaultFolder;
    }

    @Override
    public int getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(int modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Override
    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @Override
    public GroupwareFolderType getType() {
        return folderType;
    }

    public void setFolderType(GroupwareFolderType folderType) {
        this.folderType = folderType;
    }

    @Override
    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    @Override
    public String toString() {
        return "DefaultGroupwareContactsFolder [parentId=" + parentId + ", getId()=" + getId() + ", getName()=" + getName() + "]";
    }

}
