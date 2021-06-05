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

package com.openexchange.folderstorage.outlook.memory.impl;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.folderstorage.outlook.memory.MemoryCRUD;
import com.openexchange.folderstorage.outlook.memory.MemoryFolder;
import com.openexchange.java.ConcurrentHashSet;

/**
 * {@link MemoryCRUDImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class MemoryCRUDImpl implements MemoryCRUD {

    private final ConcurrentMap<String, MemoryFolder> folderMap;
    private final ConcurrentMap<String, Set<MemoryFolder>> parentMap;
    private final ConcurrentMap<String, String> folder2parentMap;

    public MemoryCRUDImpl(final ConcurrentMap<String, MemoryFolder> folderMap, final ConcurrentMap<String, Set<MemoryFolder>> parentMap, final ConcurrentMap<String, String> folder2parentMap) {
        super();
        this.folderMap = folderMap;
        this.parentMap = parentMap;
        this.folder2parentMap = folder2parentMap;
    }

    @Override
    public MemoryFolder putIfAbsent(final MemoryFolder folder) {
        return putIfAbsent(folder.getId(), folder);
    }

    @Override
    public MemoryFolder putIfAbsent(final String folderId, final MemoryFolder folder) {
        final MemoryFolder prev = folderMap.putIfAbsent(folderId, folder);
        if (null == prev) {
            // PUT successful
            final String parentId = folder.getParentId();
            folder2parentMap.put(folderId, parentId);
            Set<MemoryFolder> set = parentMap.get(parentId);
            if (null == set) {
                final Set<MemoryFolder> newset = new ConcurrentHashSet<MemoryFolder>();
                set = parentMap.putIfAbsent(parentId, newset);
                if (null == set) {
                    set = newset;
                }
            }
            set.add(folder);
        }
        return prev;
    }

    @Override
    public boolean containsFolder(final String folderId) {
        return folderMap.containsKey(folderId);
    }

    @Override
    public MemoryFolder get(final String folderId) {
        return folderMap.get(folderId);
    }

    @Override
    public MemoryFolder put(final MemoryFolder folder) {
        return put(folder.getId(), folder);
    }

    @Override
    public MemoryFolder put(final String folderId, final MemoryFolder folder) {
        final MemoryFolder ret = folderMap.put(folderId, folder);
        final String parentId = folder.getParentId();
        folder2parentMap.put(folderId, parentId);
        Set<MemoryFolder> set = parentMap.get(parentId);
        if (null == set) {
            final Set<MemoryFolder> newset = new ConcurrentHashSet<MemoryFolder>();
            set = parentMap.putIfAbsent(parentId, newset);
            if (null == set) {
                set = newset;
            }
        }
        set.add(folder);
        return ret;
    }

    @Override
    public MemoryFolder remove(final String folderId) {
        final MemoryFolder ret = folderMap.remove(folderId);
        if (ret != null) {
            folder2parentMap.remove(folderId);
            final String parentId = ret.getParentId();
            final Set<MemoryFolder> set = parentMap.get(parentId);
            if (null != set) {
                set.remove(ret);
            }
        }
        return ret;
    }

}
