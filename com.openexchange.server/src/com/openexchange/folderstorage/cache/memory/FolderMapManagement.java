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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.folderstorage.cache.memory;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weighers;
import com.openexchange.mailaccount.Tools;
import com.openexchange.session.Session;

/**
 * {@link FolderMapManagement}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderMapManagement {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FolderMapManagement.class);

    private static final FolderMapManagement INSTANCE = new FolderMapManagement();

    /**
     * Gets the {@link FolderMapManagement management} instance.
     *
     * @return The management instance
     */
    public static FolderMapManagement getInstance() {
        return INSTANCE;
    }

    private final ConcurrentMap<Integer, ConcurrentMap<Integer, FolderMap>> map;

    /**
     * Initializes a new {@link FolderMapManagement}.
     */
    private FolderMapManagement() {
        super();
        map = new ConcurrentLinkedHashMap.Builder<Integer, ConcurrentMap<Integer, FolderMap>>().initialCapacity(64).maximumWeightedCapacity(5000).weigher(Weighers.entrySingleton()).build();
    }

    /**
     * Clears the folder management.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Drop caches for given context.
     *
     * @param contextId The context identifier
     */
    public void dropFor(final int contextId) {
        map.remove(Integer.valueOf(contextId));
        LOG.debug("Cleaned user-sensitive folder cache for context {}", contextId);
    }

    /**
     * Drop caches for given session's user.
     *
     * @param session The session
     */
    public void dropFor(final Session session) {
        final ConcurrentMap<Integer, FolderMap> contextMap = map.get(Integer.valueOf(session.getContextId()));
        if (null != contextMap) {
            contextMap.remove(Integer.valueOf(session.getUserId()));
        }
        LOG.debug("Cleaned user-sensitive folder cache for user {} in context {}", session.getUserId(), session.getContextId());
    }

    /**
     * Drop caches for given session's user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public void dropFor(final int userId, final int contextId) {
        final ConcurrentMap<Integer, FolderMap> contextMap = map.get(Integer.valueOf(contextId));
        if (null != contextMap) {
            contextMap.remove(Integer.valueOf(userId));
        }
        LOG.debug("Cleaned user-sensitive folder cache for user {} in context {}", userId, contextId);
    }

    /**
     * Gets the folder map for specified session.
     *
     * @param session The session
     * @return The folder map
     */
    public FolderMap getFor(final Session session) {
        final Integer cid = Integer.valueOf(session.getContextId());
        ConcurrentMap<Integer, FolderMap> contextMap = map.get(cid);
        if (null == contextMap) {
            final ConcurrentMap<Integer, FolderMap> newMap = new NonBlockingHashMap<Integer, FolderMap>(256);
            contextMap = map.putIfAbsent(cid, newMap);
            if (null == contextMap) {
                contextMap = newMap;
            }
        }
        final Integer us = Integer.valueOf(session.getUserId());
        FolderMap folderMap = contextMap.get(us);
        if (null == folderMap) {
            final FolderMap newFolderMap = new FolderMap(1024, 300, TimeUnit.SECONDS, session.getUserId(), session.getContextId());
            folderMap = contextMap.putIfAbsent(us, newFolderMap);
            if (null == folderMap) {
                folderMap = newFolderMap;
            }
        }
        return folderMap;
    }

    /**
     * Optionally gets the folder map for specified session.
     *
     * @param session The session
     * @return The folder map or <code>null</code> if absent
     */
    public FolderMap optFor(final Session session) {
        final ConcurrentMap<Integer, FolderMap> contextMap = map.get(Integer.valueOf(session.getContextId()));
        if (null == contextMap) {
            return null;
        }
        return contextMap.get(Integer.valueOf(session.getUserId()));
    }

    /**
     * Optionally gets the folder map for specified user in given context.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The folder map or <code>null</code> if absent
     */
    public FolderMap optFor(final int userId, final int contextId) {
        final ConcurrentMap<Integer, FolderMap> contextMap = map.get(Integer.valueOf(contextId));
        if (null == contextMap) {
            return null;
        }
        return contextMap.get(Integer.valueOf(userId));
    }

    /**
     * Drop folder from all user caches for given context.
     *
     * @param folderId The folder id
     * @param treeId The tree id
     * @param optUser The optional user identifier
     * @param contextId The context identifier
     */
    public void dropFor(final String folderId, final String treeId, final int optUser, final int contextId) {
        dropFor(folderId, treeId, optUser, contextId, null);
    }

    /**
     * Drop folder from all user caches for given context.
     *
     * @param folderId The folder id
     * @param treeId The tree id
     * @param optUser The optional user identifier
     * @param contextId The context identifier
     * @param optSession The optional session
     */
    public void dropFor(final String folderId, final String treeId, final int optUser, final int contextId, final Session optSession) {
        if ((null == folderId) || (null == treeId)) {
            return;
        }
        final ConcurrentMap<Integer, FolderMap> contextMap = map.get(Integer.valueOf(contextId));
        if (null == contextMap) {
            return;
        }
        //  If folder identifier is not a number AND user identifier is valid
        //  (because numbers hint to former global folders; e.g. database folders)
        //  Then it is sufficient to clean in user-associated map only
        if (optUser > 0 && Tools.getUnsignedInteger(folderId) < 0) {
            final FolderMap folderMap = contextMap.get(Integer.valueOf(optUser));
            if (null != folderMap) {
                folderMap.remove(folderId, treeId, optSession);
            }
        } else {
            // Delete all known
            for (final FolderMap folderMap : contextMap.values()) {
                if (null == optSession) {
                    folderMap.remove(folderId, treeId);
                } else {
                    folderMap.remove(folderId, treeId, optSession);
                }
            }
        }
    }

}
