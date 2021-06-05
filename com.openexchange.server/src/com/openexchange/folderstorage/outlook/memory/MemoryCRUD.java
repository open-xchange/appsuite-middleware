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

/**
 * {@link MemoryCRUD} - Provides CRUD (<b>CR</b>eate, <b>U</b>pdate, and <b>D</b>elete) operations.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MemoryCRUD {

    /**
     * Puts specified folder if no such a folder is already contained.
     *
     * @param folder The folder
     * @return The folder already contained or <code>null</code> for successful put operation
     */
    public MemoryFolder putIfAbsent(MemoryFolder folder);

    /**
     * Puts specified folder if no such a folder is already contained.
     *
     * @param folderId The folder identifier
     * @param folder The folder
     * @return The folder already contained or <code>null</code> for successful put operation
     */
    public MemoryFolder putIfAbsent(String folderId, MemoryFolder folder);

    /**
     * Checks if specified folder is contained.
     *
     * @param folderId The folder identifier
     * @return <code>true</code> if specified folder is contained; otherwise <code>false</code>
     */
    public boolean containsFolder(String folderId);

    /**
     * Gets the specified folder.
     *
     * @param folderId The folder identifier
     * @return The folder or <code>null</code> if there was no mapping
     */
    public MemoryFolder get(String folderId);

    /**
     * Puts specified folder
     *
     * @param folder The folder
     * @return The previous folder or <code>null</code> if there was no mapping
     */
    public MemoryFolder put(MemoryFolder folder);

    /**
     * Puts specified folder
     *
     * @param folderId The folder identifier
     * @param folder The folder
     * @return The previous folder or <code>null</code> if there was no mapping
     */
    public MemoryFolder put(String folderId, MemoryFolder folder);

    /**
     * Removes the specified folder.
     *
     * @param folderId The folder identifier
     * @return The removed folder or <code>null</code> if there was no mapping
     */
    public MemoryFolder remove(String folderId);

}
