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
