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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import com.openexchange.session.Session;

/**
 * {@link FolderMapManagement}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderMapManagement {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(FolderMapManagement.class));

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
        map = new NonBlockingHashMap<Integer, ConcurrentMap<Integer, FolderMap>>(64);
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
        map.get(Integer.valueOf(contextId));
        if (LOG.isDebugEnabled()) {
            LOG.debug(new StringBuilder("Cleaned user-sensitive folder cache for context ").append(contextId).toString());
        }
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
        if (LOG.isDebugEnabled()) {
            LOG.debug(new StringBuilder("Cleaned user-sensitive folder cache for user ").append(session.getUserId()).append(" in context ").append(
                session.getContextId()).toString());
        }
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
        if (LOG.isDebugEnabled()) {
            LOG.debug(new StringBuilder("Cleaned user-sensitive folder cache for user ").append(userId).append(" in context ").append(
                contextId).toString());
        }
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

}
