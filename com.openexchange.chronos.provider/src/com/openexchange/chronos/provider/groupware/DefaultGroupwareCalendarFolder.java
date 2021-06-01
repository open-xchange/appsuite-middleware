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

package com.openexchange.chronos.provider.groupware;

import java.util.Date;
import java.util.Map;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.DefaultCalendarFolder;
import com.openexchange.groupware.EntityInfo;

/**
 * {@link DefaultGroupwareCalendarFolder}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultGroupwareCalendarFolder extends DefaultCalendarFolder implements GroupwareCalendarFolder {

    private String parentId;
    private boolean isDefaultFolder;
    private EntityInfo modifiedFrom;
    private EntityInfo createdFrom;
    private Date creationDate;
    private GroupwareFolderType folderType;
    private Map<String, Object> meta;

    /**
     * Initializes a new {@link DefaultGroupwareCalendarFolder}.
     */
    public DefaultGroupwareCalendarFolder() {
        super();
    }

    /**
     * Initializes a new {@link DefaultGroupwareCalendarFolder}, taking over the properties from another folder.
     *
     * @param folder The folder to copy the properties from
     */
    public DefaultGroupwareCalendarFolder(CalendarFolder folder) {
        super(folder);
        if (GroupwareCalendarFolder.class.isInstance(folder)) {
            GroupwareCalendarFolder groupwareFolder = (GroupwareCalendarFolder) folder;
            parentId = groupwareFolder.getParentId();
            isDefaultFolder = groupwareFolder.isDefaultFolder();
            modifiedFrom = groupwareFolder.getModifiedFrom();
            createdFrom = groupwareFolder.getCreatedFrom();
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
    public EntityInfo getModifiedFrom() {
        return modifiedFrom;
    }

    public void setModifiedFrom(EntityInfo modifiedFrom) {
        this.modifiedFrom = modifiedFrom;
    }

    @Override
    public EntityInfo getCreatedFrom() {
        return createdFrom;
    }

    public void setCreatedFrom(EntityInfo createdFrom) {
        this.createdFrom = createdFrom;
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

}
