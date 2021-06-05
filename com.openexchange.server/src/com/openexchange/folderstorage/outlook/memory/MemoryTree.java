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

import java.util.List;
import java.util.Locale;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.SortableId;

/**
 * {@link MemoryTree} - The in-memory representation of a virtual tree.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MemoryTree {

    /**
     * Gets the name of the folder held in virtual tree for the folder denoted by given folder identifier.
     */
    public String getFolderName(String folderId);

    /**
     * Checks if the specified virtual tree contains a parent denoted by given parent identifier.
     */
    public boolean containsParent(String parentId);

    /**
     * Checks if the specified virtual tree contains the folder denoted by given folder identifier.
     */
    public boolean containsFolder(String folderId);

    /**
     * Checks if the specified virtual tree contains any of the folders denoted by given folder identifiers.
     */
    public boolean[] containsFolders(String[] folderIds);

    /**
     * Checks if the specified virtual tree contains any of the folders denoted by given folder identifiers.
     */
    public boolean[] containsFolders(SortableId[] folderIds);

    /** Checks for sub-folders for given parent */
    public boolean hasSubfolderIds(String parentId);

    /** Gets the sub-folders for given parent */
    public List<String[]> getSubfolderIds(String parentId);

    /** Gets all known folder identifiers */
    public List<String> getFolders();

    /**
     * Gets the sorted identifiers of the sub-folders located below specified parent.
     */
    public String[] getSubfolderIds(Locale locale, String parentId, List<String[]> realSubfolderIds);

    /**
     * Fills specified folder with data available from associated {@link MemoryFolder} instance.
     *
     * @param folder The folder
     * @return <code>true</code> if such a folder is available; else <code>false</code>
     */
    public boolean fillFolder(Folder folder);

    /**
     * Gets the CRUD (<b>C</b>reate <b>R</b>ead <b>U</b>pdate <b>D</b>elete) management.
     *
     * @return The CRUD management
     */
    public MemoryCRUD getCrud();

    /**
     * Gets the size of this memory tree.
     *
     * @return The size
     */
    public int size();

    /**
     * Checks if this memory tree is empty.
     *
     * @return <code>true</code> if empty; else <code>false</code>
     */
    public boolean isEmpty();

    /**
     * Clears this memory tree.
     */
    public void clear();

    /**
     * Gets the folder for given identifier.
     *
     * @param folderId The folder identifier
     * @return The folder or <code>null</code>
     */
    public MemoryFolder getFolder(String folderId);

    /**
     * Gets the parent identifier for specified folder identifier.
     *
     * @param folderId The folder identifier
     * @return The parent identifier or <code>null</code>
     */
    public String getParentOf(String folderId);

}
