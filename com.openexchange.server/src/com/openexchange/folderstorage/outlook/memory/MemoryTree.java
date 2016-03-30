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
