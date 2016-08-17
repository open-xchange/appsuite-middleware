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

package com.openexchange.drive.impl.storage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.drive.impl.internal.PathNormalizer;
import com.openexchange.file.storage.FileStorageFolder;

/**
 * {@link FolderCache}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FolderCache {

    /** maps FileStorageFolder.getId() => path */
    private final Map<String, String> knownPaths;

    /** maps path => FileStorageFolder */
    private final Map<String, FileStorageFolder> knownFolders;

    /**
     * Initializes a new {@link FolderCache}.
     */
    public FolderCache() {
        super();
        this.knownFolders = new HashMap<String, FileStorageFolder>();
        this.knownPaths = new HashMap<String, String>();
    }

    /**
     * Gets the path to the folder with the supplied ID.
     *
     * @param folderID The ID of the folder
     * @return The path, or <code>null</code> if unknown
     */
    public String getPath(String folderID) {
        return knownPaths.get(folderID);
    }

    /**
     * Gets the folder behind the supplied path.
     *
     * @param path The path to the folder
     * @return The folder, or <code>null</code> if unknown
     */
    public FileStorageFolder getFolder(String path)  {
        return knownFolders.get(PathNormalizer.normalize(path));
    }

    /**
     * Remembers a folder path.
     *
     * @param path The path to the folder
     * @param folder The folder to remember
     */
    public void remember(String path, FileStorageFolder folder) {
        knownPaths.put(folder.getId(), PathNormalizer.normalize(path));
        knownFolders.put(PathNormalizer.normalize(path), folder);
    }

    /**
     * Removes a possibly cached folder path.
     *
     * @param path The path to the folder
     * @param folder The folder to forget
     * @param forgetSubfolders <code>true</code> to also remove cached subfolders, <code>false</code>, otherwise
     */
    public void forget(String path, FileStorageFolder folder, boolean forgetSubfolders) {
        knownPaths.remove(folder.getId());
        knownFolders.remove(PathNormalizer.normalize(path));
        if (forgetSubfolders) {
            Iterator<Entry<String, FileStorageFolder>> iterator = knownFolders.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, FileStorageFolder> knownFolder = iterator.next();
                if (knownFolder.getKey().startsWith(PathNormalizer.normalize(path))) {
                    knownPaths.remove(knownFolder.getValue().getId());
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Removes any cached folder paths.
     */
    public void clear() {
        knownFolders.clear();
        knownPaths.clear();
    }

}
