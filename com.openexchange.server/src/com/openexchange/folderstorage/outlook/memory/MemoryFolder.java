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

package com.openexchange.folderstorage.outlook.memory;

import java.util.Date;
import com.openexchange.folderstorage.Permission;

/**
 * {@link MemoryFolder} - The in-memory representation of a virtual folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MemoryFolder {

    /**
     * Gets the subscribed
     *
     * @return The subscribed
     */
    public Boolean getSubscribed();

    /**
     * Gets the treeId
     *
     * @return The treeId
     */
    public String getTreeId();

    /**
     * Gets the id
     *
     * @return The id
     */
    public String getId();

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName();

    /**
     * Gets the parentId
     *
     * @return The parentId
     */
    public String getParentId();

    /**
     * Gets the permissions
     *
     * @return The permissions
     */
    public Permission[] getPermissions();

    /**
     * Gets the modifiedBy
     *
     * @return The modifiedBy
     */
    public int getModifiedBy();

    /**
     * Gets the lastModified
     *
     * @return The lastModified
     */
    public Date getLastModified();

    /**
     * Gets the sort number.
     *
     * @return The sort number or <code>0</code> if not set
     */
    public int getSortNum();

}
