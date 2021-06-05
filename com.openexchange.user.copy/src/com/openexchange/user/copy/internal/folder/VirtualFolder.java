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

package com.openexchange.user.copy.internal.folder;


/**
 * {@link VirtualFolder}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class VirtualFolder {

    private int tree;

    private String folderId;

    private String parentId;

    private String name;

    private long lastModified;

    private int modifiedBy;

    private String shadow;

    private int sortNum;


    public VirtualFolder() {
        super();
    }



    public int getTree() {
        return tree;
    }



    public void setTree(final int tree) {
        this.tree = tree;
    }



    public String getFolderId() {
        return folderId;
    }



    public void setFolderId(final String folderId) {
        this.folderId = folderId;
    }



    public String getParentId() {
        return parentId;
    }



    public void setParentId(final String parentId) {
        this.parentId = parentId;
    }



    public String getName() {
        return name;
    }



    public void setName(final String name) {
        this.name = name;
    }



    public long getLastModified() {
        return lastModified;
    }



    public void setLastModified(final long lastModified) {
        this.lastModified = lastModified;
    }



    public int getModifiedBy() {
        return modifiedBy;
    }



    public void setModifiedBy(final int modifiedBy) {
        this.modifiedBy = modifiedBy;
    }



    public String getShadow() {
        return shadow;
    }



    public void setShadow(final String shadow) {
        this.shadow = shadow;
    }



    public int getSortNum() {
        return sortNum;
    }



    public void setSortNum(final int sortNum) {
        this.sortNum = sortNum;
    }

}
