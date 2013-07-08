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

package com.openexchange.tools.oxfolder.memory;

import gnu.trove.EmptyTIntSet;
import gnu.trove.TIntCollection;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.logging.Log;
import com.javacodegeeks.concurrent.ConcurrentLinkedHashMap;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.oxfolder.OXFolderBatchLoader;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link ConditionTreeMap} - Stores context-related condition trees for individual entities (users/groups).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConditionTreeMap {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(ConditionTreeMap.class);

    private static final EmptyTIntSet EMPTY_SET = EmptyTIntSet.getInstance();

    final ConcurrentMap<Integer, Future<ConditionTree>> entity2tree;
    private final int contextId;
    private final int time2live;

    /**
     * Initializes a new {@link ConditionTreeMap}.
     */
    public ConditionTreeMap(final int contextId, final int time2live) {
        super();
        // Evict user-associated entries after <time2live> milliseconds
        entity2tree = new ConcurrentLinkedHashMap<Integer, Future<ConditionTree>>(countEntities(contextId), 0.75F, 16, Integer.MAX_VALUE, new AgePolicy(time2live));
        this.contextId = contextId;
        this.time2live = time2live;
    }

    private static int countEntities(final int contextId) {
        final DatabaseService service = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            connection = service.getReadOnly(contextId);

            stmt = connection.prepareStatement("SELECT COUNT(id) FROM user WHERE cid=?");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            int count = rs.next() ? rs.getInt(1) : 0;
            DBUtils.closeSQLStuff(rs, stmt);

            stmt = connection.prepareStatement("SELECT COUNT(id) FROM groups WHERE cid=?");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            count += (rs.next() ? rs.getInt(1) : 0);
            return count;
        } catch (final Exception e) {
            return 1024;
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            if (null != connection) {
                service.backReadOnly(contextId, connection);
            }
        }
    }

    /**
     * Trims this context condition tree map; meaning removes all elapsed entries.
     */
    public void trim() {
        trim(System.currentTimeMillis() - time2live);
    }

    /**
     * Trims this context condition tree map; meaning removes all elapsed entries.
     */
    public void trim(final long stamp) {
        for (final Iterator<Future<ConditionTree>> it = entity2tree.values().iterator(); it.hasNext();) {
            final Future<ConditionTree> f = it.next();
            try {
                if (getFrom(f).isElapsed(stamp)) {
                    it.remove();
                }
            } catch (final Exception e) {
                // Drop on error
                it.remove();
            }
        }
    }

    /**
     * Initializes the tree map for map's associated context.
     *
     * @throws OXException If initialization fails
     */
    public void init() throws OXException {
        entity2tree.clear();

        final DatabaseService service = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            connection = service.getReadOnly(contextId);
            stmt =
                connection.prepareStatement("SELECT ot.fuid, op.permission_id, op.admin_flag, op.fp, ot.module, ot.type, ot.created_from, ot.changing_date, ot.parent" +
                    " FROM oxfolder_tree AS ot JOIN oxfolder_permissions AS op ON ot.cid = op.cid AND ot.fuid = op.fuid WHERE ot.cid = ?"
                /* + " ORDER BY default_flag DESC, fname" */);
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            int pos;
            while (rs.next()) {
                final Permission p = new Permission();
                pos = 1;
                p.fuid = rs.getInt(pos++);
                p.entity = rs.getInt(pos++);
                p.admin = rs.getInt(pos++) > 0;
                p.readFolder = rs.getInt(pos++) >= OCLPermission.READ_FOLDER;
                p.module = rs.getInt(pos++);
                p.type = rs.getInt(pos++);
                p.creator = rs.getInt(pos++);
                p.lastModified = rs.getLong(pos++);
                p.parent = rs.getInt(pos);
                insert(p);
            }
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            if (null != connection) {
                service.backReadOnly(contextId, connection);
            }
        }
    }

    /**
     * Initializes a new tree for given entity.
     *
     * @param entity The entity identifier
     * @throws OXException If initialization fails
     */
    public ConditionTree newTreeForEntity(final int entity) throws OXException {
        final DatabaseService service = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            connection = service.getReadOnly(contextId);
            stmt =
                connection.prepareStatement("SELECT ot.fuid, op.permission_id, op.admin_flag, op.fp, ot.module, ot.type, ot.created_from, ot.changing_date, ot.parent" +
                    " FROM oxfolder_tree AS ot JOIN oxfolder_permissions AS op ON ot.cid = op.cid AND ot.fuid = op.fuid WHERE ot.cid=? AND op.permission_id=?"
                /* + " ORDER BY default_flag DESC, fname" */);
            stmt.setInt(1, contextId);
            stmt.setInt(2, entity);
            rs = stmt.executeQuery();
            int pos;
            final ConditionTree tree = new ConditionTree();
            while (rs.next()) {
                final Permission p = new Permission();
                pos = 1;
                p.fuid = rs.getInt(pos++);
                p.entity = rs.getInt(pos++);
                p.admin = rs.getInt(pos++) > 0;
                p.readFolder = rs.getInt(pos++) >= OCLPermission.READ_FOLDER;
                p.module = rs.getInt(pos++);
                p.type = rs.getInt(pos++);
                p.creator = rs.getInt(pos++);
                p.lastModified = rs.getLong(pos++);
                p.parent = rs.getInt(pos);
                tree.insert(p);
            }
            return tree;
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            if (null != connection) {
                service.backReadOnly(contextId, connection);
            }
        }
    }

    /**
     * Clears whole tree map.
     */
    public void clear() {
        entity2tree.clear();
    }

    /**
     * Removes the tree for specified entity.
     *
     * @param entity The entity identifier
     */
    public void removeFor(final int entity) {
        entity2tree.remove(Integer.valueOf(entity));
    }

    /**
     * Feed given permission to this tree map.
     *
     * @param permission The permission
     */
    public void insert(final Permission permission) throws OXException {
        final Integer entity = Integer.valueOf(permission.entity);
        Future<ConditionTree> f = entity2tree.get(entity);
        if (null == f) {
            final FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new NewTreeCallable());
            f = entity2tree.putIfAbsent(entity, ft);
            if (null == f) {
                ft.run();
                f = ft;
            }
        }
        // Insert into tree
        getFrom(f).insert(permission);
    }

    /**
     * Gets the visible folders for given user.
     *
     * @param userId The user identifier
     * @param groups The groups associated with given user
     * @param accessibleModules The user's accessible modules
     * @param conditions The conditions to fulfill
     * @return The identifiers of visible folders
     */
    public TIntSet getVisibleForUser(final int userId, final int[] groups, final int[] accessibleModules, final Condition... conditions) throws OXException {
        Future<ConditionTree> f = entity2tree.get(Integer.valueOf(userId));
        if (null == f) {
            final FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(userId, LOG));
            f = entity2tree.putIfAbsent(Integer.valueOf(userId), ft);
            if (null == f) {
                ft.run();
                f = ft;
            }
        }
        final Condition condition = new CombinedCondition(new ModulesCondition(accessibleModules), conditions);
        /*
         * Iterate visible sets
         */
        final TIntSet set = getFrom(f).getVisibleFolderIds(condition);
        if (null != groups) {
            for (final int group : groups) {
                f = entity2tree.get(Integer.valueOf(group));
                if (null == f) {
                    final FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(group, LOG));
                    f = entity2tree.putIfAbsent(Integer.valueOf(group), ft);
                    if (null == f) {
                        ft.run();
                        f = ft;
                    }
                }
                set.addAll(getFrom(f).getVisibleFolderIds(condition));
            }
        }
        /*
         * Return set
         */
        return set;
    }

    /**
     * Gets the visible folders for given user.
     *
     * @param userId The user identifier
     * @param groups The groups associated with given user
     * @param accessibleModules The user's accessible modules
     * @param conditions The conditions to fulfill
     * @return The identifiers of visible folders
     */
    public TIntSet getVisibleForUser(final int userId, final int[] groups, final int[] accessibleModules, final Collection<Condition> conditions) throws OXException {
        Future<ConditionTree> f = entity2tree.get(Integer.valueOf(userId));
        if (null == f) {
            final FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(userId, LOG));
            f = entity2tree.putIfAbsent(Integer.valueOf(userId), ft);
            if (null == f) {
                ft.run();
                f = ft;
            }
        }
        final Condition condition = new CombinedCondition(new ModulesCondition(accessibleModules), conditions);
        /*
         * Iterate visible sets
         */
        final TIntSet set = getFrom(f).getVisibleFolderIds(condition);
        if (null != groups) {
            for (final int group : groups) {
                f = entity2tree.get(Integer.valueOf(group));
                if (null == f) {
                    final FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(group, LOG));
                    f = entity2tree.putIfAbsent(Integer.valueOf(group), ft);
                    if (null == f) {
                        ft.run();
                        f = ft;
                    }
                }
                set.addAll(getFrom(f).getVisibleFolderIds(condition));
            }
        }
        /*
         * Return set
         */
        return set;
    }

    /**
     * Checks for visibility of given user.
     *
     * @param userId The user identifier
     * @param groups The groups associated with given user
     * @param accessibleModules The user's accessible modules
     * @param folderId The folder identifier
     * @return <code>true</code> if visible; otherwise <code>false</code>
     */
    public boolean isVisibleFolder(final int userId, final int[] groups, final int[] accessibleModules, final int folderId) throws OXException {
        Future<ConditionTree> f = entity2tree.get(Integer.valueOf(userId));
        if (null == f) {
            final FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(userId, LOG));
            f = entity2tree.putIfAbsent(Integer.valueOf(userId), ft);
            if (null == f) {
                ft.run();
                f = ft;
            }
        }
        /*
         * Iterate visible sets
         */
        final Condition condition = new ModulesCondition(accessibleModules);
        TIntSet set = getFrom(f).getVisibleFolderIds(condition);
        if (!set.isEmpty() && set.contains(folderId)) {
            return true;
        }
        if (null != groups) {
            for (final int group : groups) {
                f = entity2tree.get(Integer.valueOf(group));
                if (null == f) {
                    final FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(group, LOG));
                    f = entity2tree.putIfAbsent(Integer.valueOf(group), ft);
                    if (null == f) {
                        ft.run();
                        f = ft;
                    }
                }
                set = getFrom(f).getVisibleFolderIds(condition);
                if (!set.isEmpty() && set.contains(folderId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks for any shared folder visible to given user.
     *
     * @param userId The user identifier
     * @param groups The groups associated with given user
     * @param accessibleModules The user's accessible modules
     * @param type The type
     * @return The identifiers of visible folders
     */
    public boolean hasSharedFolder(final int userId, final int[] groups, final int[] accessibleModules) throws OXException {
        Future<ConditionTree> f = entity2tree.get(Integer.valueOf(userId));
        if (null == f) {
            final FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(userId, LOG));
            f = entity2tree.putIfAbsent(Integer.valueOf(userId), ft);
            if (null == f) {
                ft.run();
                f = ft;
            }
        }
        final Condition condition = new CombinedCondition(new ModulesCondition(accessibleModules), new TypeCondition(FolderObject.SHARED, userId));
        /*
         * Iterate visible sets
         */
        TIntSet set = getFrom(f).getVisibleFolderIds(condition);
        if (!set.isEmpty()) {
            return true;
        }
        if (null != groups) {
            for (final int group : groups) {
                f = entity2tree.get(Integer.valueOf(group));
                if (null == f) {
                    final FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(group, LOG));
                    f = entity2tree.putIfAbsent(Integer.valueOf(group), ft);
                    if (null == f) {
                        ft.run();
                        f = ft;
                    }
                }

                if (!getFrom(f).getVisibleFolderIds(condition).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the visible folders for given user.
     *
     * @param userId The user identifier
     * @param groups The groups associated with given user
     * @param accessibleModules The user's accessible modules
     * @param type The type
     * @return The identifiers of visible folders
     */
    public TIntSet getVisibleTypeForUser(final int userId, final int[] groups, final int[] accessibleModules, final int type) throws OXException {
        Future<ConditionTree> f = entity2tree.get(Integer.valueOf(userId));
        if (null == f) {
            final FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(userId, LOG));
            f = entity2tree.putIfAbsent(Integer.valueOf(userId), ft);
            if (null == f) {
                ft.run();
                f = ft;
            }
        }
        final Condition condition = new CombinedCondition(new ModulesCondition(accessibleModules), new TypeCondition(type, userId));
        /*
         * Iterate visible sets
         */
        final TIntSet set = getFrom(f).getVisibleFolderIds(condition);
        if (null != groups) {
            for (final int group : groups) {
                f = entity2tree.get(Integer.valueOf(group));
                if (null == f) {
                    final FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(group, LOG));
                    f = entity2tree.putIfAbsent(Integer.valueOf(group), ft);
                    if (null == f) {
                        ft.run();
                        f = ft;
                    }
                }

                set.addAll(getFrom(f).getVisibleFolderIds(condition));
            }
        }
        /*
         * Return set
         */
        return set;
    }

    /**
     * Gets the visible folders for given user.
     *
     * @param userId The user identifier
     * @param groups The groups associated with given user
     * @param accessibleModules The user's accessible modules
     * @param module The module
     * @return The identifiers of visible folders
     */
    public TIntSet getVisibleModuleForUser(final int userId, final int[] groups, final int[] accessibleModules, final int module) throws OXException {
        final Condition condition;
        {
            if (null != accessibleModules) {
                final TIntSet modules = new TIntHashSet(accessibleModules);
                if (!modules.contains(module)) {
                    return EMPTY_SET;
                }
            }
            condition = ModuleCondition.moduleCondition(module);
        }
        Future<ConditionTree> f = entity2tree.get(Integer.valueOf(userId));
        if (null == f) {
            final FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(userId, LOG));
            f = entity2tree.putIfAbsent(Integer.valueOf(userId), ft);
            if (null == f) {
                ft.run();
                f = ft;
            }
        }
        /*
         * Iterate visible sets
         */
        final TIntSet set = getFrom(f).getVisibleFolderIds(condition);
        if (null != groups) {
            for (final int group : groups) {
                f = entity2tree.get(Integer.valueOf(group));
                if (null == f) {
                    final FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(group, LOG));
                    f = entity2tree.putIfAbsent(Integer.valueOf(group), ft);
                    if (null == f) {
                        ft.run();
                        f = ft;
                    }
                }
                set.addAll(getFrom(f).getVisibleFolderIds(condition));
            }
        }
        /*
         * Return set
         */
        return set;
    }

    /**
     * Gets the visible folders for given user.
     *
     * @param userId The user identifier
     * @param groups The groups associated with given user
     * @param accessibleModules The user's accessible modules
     * @param module The module
     * @param type The type
     * @return The identifiers of visible folders
     */
    public TIntSet getVisibleForUser(final int userId, final int[] groups, final int[] accessibleModules, final int module, final int type) throws OXException {
        final Condition condition;
        {
            if (null != accessibleModules) {
                final TIntSet modules = new TIntHashSet(accessibleModules);
                if (!modules.contains(module)) {
                    return EMPTY_SET;
                }
            }
            condition = new CombinedCondition(ModuleCondition.moduleCondition(module), new TypeCondition(type, userId));
        }
        Future<ConditionTree> f = entity2tree.get(Integer.valueOf(userId));
        if (null == f) {
            final FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(userId, LOG));
            f = entity2tree.putIfAbsent(Integer.valueOf(userId), ft);
            if (null == f) {
                ft.run();
                f = ft;
            }
        }
        /*
         * Iterate visible sets
         */
        final TIntSet set = getFrom(f).getVisibleFolderIds(condition);
        if (null != groups) {
            for (final int group : groups) {
                f = entity2tree.get(Integer.valueOf(group));
                if (null == f) {
                    final FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(group, LOG));
                    f = entity2tree.putIfAbsent(Integer.valueOf(group), ft);
                    if (null == f) {
                        ft.run();
                        f = ft;
                    }
                }
                set.addAll(getFrom(f).getVisibleFolderIds(condition));
            }
        }
        /*
         * Return set
         */
        return set;
    }

    /**
     * Creates a list for specified set.
     *
     * @param set The set to turn into a list
     * @param ctx The associated context
     * @return The list
     * @throws OXException If creating a list fails
     */
    public static List<FolderObject> asList(final TIntSet set, final Context ctx) throws OXException {
        return asList(set, ctx, null);
    }

    /**
     * Creates a list for specified set.
     *
     * @param set The set to turn into a list
     * @param ctx The associated context
     * @param con A connection in read mode
     * @return The list
     * @throws OXException If creating a list fails
     */
    public static List<FolderObject> asList(final TIntSet set, final Context ctx, final Connection con) throws OXException {
        if (null == set || set.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            /*
             * List of folders to return
             */
            final TIntObjectMap<FolderObject> m = new TIntObjectHashMap<FolderObject>(set.size());
            {
                final TIntSet toLoad = new TIntHashSet(set);
                final FolderCacheManager cacheManager = FolderCacheManager.getInstance();
                final boolean cacheEnabled = FolderCacheManager.isEnabled();
                if (cacheEnabled) {
                    for (final FolderObject fo : cacheManager.getTrimedFolderObjects(set.toArray(), ctx)) {
                        final int objectID = fo.getObjectID();
                        toLoad.remove(objectID); // Needs not to be loaded; therefore removed from set
                        m.put(objectID, fo);
                    }
                }
                /*
                 * Loadees...
                 */
                if (!toLoad.isEmpty()) {
                    if (null == con) {
                        loadBy(toLoad, m, cacheEnabled, cacheManager, ctx);
                    } else {
                        loadBy(toLoad, m, cacheEnabled, cacheManager, ctx, con);
                    }
                }
            }
            /*
             * Fill list from map
             */
            final List<FolderObject> list = new ArrayList<FolderObject>(m.size());
            final TIntProcedure procedure = new TIntProcedure() {

                @Override
                public boolean execute(final int folderId) {
                    try {
                        final FolderObject fo = m.get(folderId);
                        if (null != fo) {
                            list.add(fo);
                        }
                        return true;
                    } catch (final Exception e) {
                        throw new ProcedureFailedException(e);
                    }
                }
            };
            set.forEach(procedure);
            return list;
        } catch (final ProcedureFailedException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(cause, cause.getMessage());
        }
    }

    private static void loadBy(final TIntSet toLoad, final TIntObjectMap<FolderObject> m, final boolean cacheEnabled, final FolderCacheManager cacheManager, final Context ctx) throws OXException {
        final DatabaseService service = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        Connection con = null;
        try {
            con = service.getReadOnly(ctx);
            loadBy(toLoad, m, cacheEnabled, cacheManager, ctx, con);
        } finally {
            if (null != con) {
                service.backReadOnly(ctx, con);
            }
        }
    }

    private static void loadBy(final TIntSet toLoad, final TIntObjectMap<FolderObject> m, final boolean cacheEnabled, final FolderCacheManager cacheManager, final Context ctx, final Connection con) throws OXException {
        if (null == con) {
            loadBy(toLoad, m, cacheEnabled, cacheManager, ctx);
            return;
        }
        final List<FolderObject> loaded = OXFolderBatchLoader.loadFolderObjectsFromDB(toLoad.toArray(), ctx, con, true, true);
        // Put to cache asynchronously with a separate list
        if (cacheEnabled) {
            final List<FolderObject> tmp = new ArrayList<FolderObject>(loaded);
            ThreadPools.getThreadPool().submit(new AbstractTask<Void>() {

                @Override
                public Void call() throws Exception {
                    for (final FolderObject fo : tmp) {
                        if (null != fo) {
                            cacheManager.putFolderObject(fo, ctx, false, null);
                        }
                    }
                    return null;
                }
            });
        }
        // Put to map
        for (final FolderObject fo : loaded) {
            if (null != fo) {
                m.put(fo.getObjectID(), fo);
            }
        }
    }

    protected static FolderObject getFolderObject(final int folderId, final Context ctx, final Connection con) throws OXException {
        if (!FolderCacheManager.isEnabled()) {
            return FolderObject.loadFolderObjectFromDB(folderId, ctx, con, true, true);
        }
        final FolderCacheManager cacheManager = FolderCacheManager.getInstance();
        FolderObject fo = cacheManager.getFolderObject(folderId, ctx);
        if (null == fo) {
            fo = FolderObject.loadFolderObjectFromDB(folderId, ctx, con, true, true);
            cacheManager.putFolderObject(fo, ctx, false, null);
        }
        return fo;
    }

    private static final class CombinedCondition implements Condition {

        private final List<Condition> conditions;

        protected CombinedCondition(final Condition first, final Condition... others) {
            super();
            if (null != others) {
                conditions = new ArrayList<Condition>(others.length + 1);
                conditions.add(first);
                conditions.addAll(Arrays.asList(others));
            } else {
                conditions = new ArrayList<Condition>(1);
                conditions.add(first);
            }
        }

        public CombinedCondition(final ModulesCondition first, final Collection<Condition> conditions) {
            super();
            this.conditions = new ArrayList<Condition>(conditions.size() + 1);
            this.conditions.add(first);
            this.conditions.addAll(conditions);
        }

        @Override
        public boolean fulfilled(final Permission p) {
            for (final Condition condition : conditions) {
                if (!condition.fulfilled(p)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static final class PrivateCondition implements Condition {

        private final int userId;

        public PrivateCondition(final int userId) {
            super();
            this.userId = userId;
        }

        @Override
        public boolean fulfilled(final Permission p) {
            return (FolderObject.PRIVATE == p.type) && (p.creator == userId);
        }

    }

    public static final class ParentCondition implements Condition {

        private final int parent;

        public ParentCondition(final int parent) {
            super();
            this.parent = parent;
        }

        @Override
        public boolean fulfilled(final Permission p) {
            return (p.parent == parent);
        }

    }

    public static final class CreatorCondition implements Condition {

        private final int creator;

        public CreatorCondition(final int creator) {
            super();
            this.creator = creator;
        }

        @Override
        public boolean fulfilled(final Permission p) {
            return (p.creator == creator);
        }

    }

    public static final class TypeCondition implements Condition {

        private final int type;
        private final int userId;

        public TypeCondition(final int type, final int userId) {
            super();
            if (FolderObject.SHARED == type) {
                this.userId = userId;
                this.type = FolderObject.PRIVATE;
            } else {
                this.userId = -1;
                this.type = type;
            }
        }

        @Override
        public boolean fulfilled(final Permission p) {
            return (p.type == type) && (userId >= 0 ? (p.creator != userId) : true);
        }

    }

    public static final class ModuleCondition implements Condition {

        private static final ConcurrentMap<Integer, ModuleCondition> conditions = new ConcurrentHashMap<Integer, ModuleCondition>(8);

        static ModuleCondition moduleCondition(final int module) {
            final Integer key = Integer.valueOf(module);
            ModuleCondition mc = conditions.get(key);
            if (null == mc) {
                final ModuleCondition nmc = new ModuleCondition(module);
                mc = conditions.putIfAbsent(key, nmc);
                if (mc == null) {
                    mc = nmc;
                }
            }
            return mc;
        }

        private final int module;

        private ModuleCondition(final int module) {
            super();
            this.module = module;
        }

        @Override
        public boolean fulfilled(final Permission p) {
            return module == p.module;
        }

    }

    public static final class ModulesCondition implements Condition {

        private final TIntSet accessibleModules;

        public ModulesCondition(final int[] accessibleModules) {
            super();
            this.accessibleModules = new TIntHashSet(accessibleModules);
        }

        public ModulesCondition(final TIntCollection accessibleModules) {
            super();
            this.accessibleModules = new TIntHashSet(accessibleModules);
        }

        @Override
        public boolean fulfilled(final Permission p) {
            return accessibleModules.contains(p.module);
        }

    }

    public static final class LastModifiedCondition implements Condition {

        private final long lastModified;

        public LastModifiedCondition(final long lastModified) {
            super();
            this.lastModified = lastModified;
        }

        @Override
        public boolean fulfilled(final Permission p) {
            return p.lastModified > lastModified;
        }

    }

    private static final class ProcedureFailedException extends RuntimeException {

        private static final long serialVersionUID = 1821041261492515385L;

        protected ProcedureFailedException(final Throwable cause) {
            super(cause);
        }

    }

    // --------------------------------------------------------------------------------------- //

    protected ConditionTree getFrom(final Future<ConditionTree> f) throws OXException {
        if (null == f) {
            return null;
        }
        try {
            return f.get();
        } catch (final InterruptedException e) {
            // Cannot occur
            Thread.currentThread().interrupt();
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, e.getMessage());
        } catch (final ExecutionException e) {
            throw ThreadPools.launderThrowable(e, OXException.class);
        }
    }

    protected ConditionTree timedFrom(final Future<ConditionTree> f, final long timeoutMillis) throws OXException {
        if (null == f) {
            return null;
        }
        try {
            return f.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            // Cannot occur
            Thread.currentThread().interrupt();
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, e.getMessage());
        } catch (final ExecutionException e) {
            throw ThreadPools.launderThrowable(e, OXException.class);
        } catch (final TimeoutException e) {
            return null;
        }
    }

    private final class NewTreeCallable implements Callable<ConditionTree> {

        protected NewTreeCallable() {
            super();
        }

        @Override
        public ConditionTree call() {
            return new ConditionTree();
        }
    } // End of NewTreeCallable class

    private final class InitEntityCallable implements Callable<ConditionTree> {

        private final int entity;
        private final Log logger;

        protected InitEntityCallable(final int entity, final Log logger) {
            super();
            this.entity = entity;
            this.logger = logger;
        }

        @Override
        public ConditionTree call() {
            try {
                return newTreeForEntity(entity);
            } catch (final OXException e) {
                logger.warn(e.getMessage(), e);
                return null;
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
                return null;
            }
        }
    } // End of InitEntityCallable class

}
