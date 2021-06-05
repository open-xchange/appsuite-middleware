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
