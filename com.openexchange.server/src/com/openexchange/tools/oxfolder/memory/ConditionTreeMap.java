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

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConditionTreeMap.class);

    private static final EmptyTIntSet EMPTY_SET = EmptyTIntSet.getInstance();

    final ConcurrentMap<Integer, Future<ConditionTree>> entity2tree;
    private final int contextId;
    private final int time2live;

    /**
     * Initializes a new {@link ConditionTreeMap}.
     */
    public ConditionTreeMap(int contextId, int time2live) {
        super();
        // Evict user-associated entries after <time2live> milliseconds
        entity2tree = new ConcurrentHashMap<Integer, Future<ConditionTree>>(countEntities(contextId), 0.9f, 1);
        this.contextId = contextId;
        this.time2live = time2live;
    }

    private static int countEntities(int contextId) {
        DatabaseService service = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
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
        } catch (Exception e) {
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
    public void trim(long stamp) {
        for (Iterator<Future<ConditionTree>> it = entity2tree.values().iterator(); it.hasNext();) {
            Future<ConditionTree> f = it.next();
            try {
                ConditionTree conditionTree = getFrom(f);
                if (null == conditionTree || conditionTree.isElapsed(stamp)) {
                    it.remove();
                }
            } catch (Exception e) {
                // Drop on error
                it.remove();
            }
        }
    }

    /**
     * Checks if this map is empty
     *
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return entity2tree.isEmpty();
    }

    private static final int READ_FOLDER = OCLPermission.READ_FOLDER;

    /**
     * Initializes the tree map for map's associated context.
     *
     * @throws OXException If initialization fails
     */
    public void init() throws OXException {
        entity2tree.clear();
        DatabaseService service = ServerServiceRegistry.getInstance().getService(DatabaseService.class);

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
            while (rs.next()) {
                boolean admin = rs.getInt(3) > 0;
                boolean readFolder = rs.getInt(4) >= READ_FOLDER;
                if (admin || readFolder) {
                    insert(new Permission(rs.getInt(1), rs.getInt(2), admin, readFolder, rs.getInt(5), rs.getInt(6), rs.getInt(7), rs.getLong(8), rs.getInt(9)));
                }
            }
        } catch (SQLException e) {
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
    public ConditionTree newTreeForEntity(int entity) throws OXException {
        DatabaseService service = ServerServiceRegistry.getInstance().getService(DatabaseService.class);

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            connection = service.getReadOnly(contextId);
            stmt =
                connection.prepareStatement("SELECT ot.fuid, op.admin_flag, op.fp, ot.module, ot.type, ot.created_from, ot.changing_date, ot.parent" +
                    " FROM oxfolder_tree AS ot JOIN oxfolder_permissions AS op ON ot.cid = op.cid AND ot.fuid = op.fuid WHERE ot.cid=? AND op.permission_id=?"
                /* + " ORDER BY default_flag DESC, fname" */);
            stmt.setInt(1, contextId);
            stmt.setInt(2, entity);
            rs = stmt.executeQuery();
            ConditionTree tree = new ConditionTree();
            while (rs.next()) {
                boolean admin = rs.getInt(2) > 0;
                boolean readFolder = rs.getInt(3) >= READ_FOLDER;
                if (admin || readFolder) {
                    tree.insert(new Permission(rs.getInt(1), entity, admin, readFolder, rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getLong(7), rs.getInt(8)));
                }
            }
            return tree;
        } catch (SQLException e) {
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
    public void removeFor(int entity) {
        entity2tree.remove(Integer.valueOf(entity));
    }

    /**
     * Feed given permission to this tree map.
     *
     * @param permission The permission
     */
    public void insert(Permission permission) throws OXException {
        Integer entity = Integer.valueOf(permission.entity);
        Future<ConditionTree> f = entity2tree.get(entity);
        if (null == f) {
            FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new NewTreeCallable());
            f = entity2tree.putIfAbsent(entity, ft);
            if (null == f) {
                ft.run();
                f = ft;
            }
        }

        // Insert into tree
        ConditionTree conditionTree = getFrom(f);
        if (null != conditionTree) {
            conditionTree.insert(permission);
        }
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
    public TIntSet getVisibleForUser(int userId, int[] groups, int[] accessibleModules, Condition... conditions) throws OXException {
        Future<ConditionTree> f = entity2tree.get(Integer.valueOf(userId));
        if (null == f) {
            FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(userId, LOG));
            f = entity2tree.putIfAbsent(Integer.valueOf(userId), ft);
            if (null == f) {
                ft.run();
                f = ft;
            }
        }
        Condition condition = new CombinedCondition(new ModulesCondition(accessibleModules), conditions);
        /*
         * Iterate visible sets
         */
        TIntSet set = getFrom(f).getVisibleFolderIds(condition);
        if (null == set) {
            set = new TIntHashSet();
        }
        if (null != groups) {
            for (int group : groups) {
                f = entity2tree.get(Integer.valueOf(group));
                if (null == f) {
                    FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(group, LOG));
                    f = entity2tree.putIfAbsent(Integer.valueOf(group), ft);
                    if (null == f) {
                        ft.run();
                        f = ft;
                    }
                }
                ConditionTree conditionTree = getFrom(f);
                if (null != conditionTree) {
                    set.addAll(conditionTree.getVisibleFolderIds(condition));
                }
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
    public TIntSet getVisibleForUser(int userId, int[] groups, int[] accessibleModules, Collection<Condition> conditions) throws OXException {
        Future<ConditionTree> f = entity2tree.get(Integer.valueOf(userId));
        if (null == f) {
            FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(userId, LOG));
            f = entity2tree.putIfAbsent(Integer.valueOf(userId), ft);
            if (null == f) {
                ft.run();
                f = ft;
            }
        }
        Condition condition = new CombinedCondition(new ModulesCondition(accessibleModules), conditions);
        /*
         * Iterate visible sets
         */
        TIntSet set = getFrom(f).getVisibleFolderIds(condition);
        if (null == set) {
            set = new TIntHashSet();
        }
        if (null != groups) {
            for (int group : groups) {
                f = entity2tree.get(Integer.valueOf(group));
                if (null == f) {
                    FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(group, LOG));
                    f = entity2tree.putIfAbsent(Integer.valueOf(group), ft);
                    if (null == f) {
                        ft.run();
                        f = ft;
                    }
                }
                ConditionTree conditionTree = getFrom(f);
                if (null != conditionTree) {
                    set.addAll(conditionTree.getVisibleFolderIds(condition));
                }
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
    public boolean isVisibleFolder(int userId, int[] groups, int[] accessibleModules, int folderId) throws OXException {
        Future<ConditionTree> f = entity2tree.get(Integer.valueOf(userId));
        if (null == f) {
            FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(userId, LOG));
            f = entity2tree.putIfAbsent(Integer.valueOf(userId), ft);
            if (null == f) {
                ft.run();
                f = ft;
            }
        }
        /*
         * Iterate visible sets
         */
        Condition condition = new ModulesCondition(accessibleModules);
        TIntSet set = getFrom(f).getVisibleFolderIds(condition);
        if (!set.isEmpty() && set.contains(folderId)) {
            return true;
        }
        if (null != groups) {
            for (int group : groups) {
                f = entity2tree.get(Integer.valueOf(group));
                if (null == f) {
                    FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(group, LOG));
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
    public boolean hasSharedFolder(int userId, int[] groups, int[] accessibleModules) throws OXException {
        Future<ConditionTree> f = entity2tree.get(Integer.valueOf(userId));
        if (null == f) {
            FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(userId, LOG));
            f = entity2tree.putIfAbsent(Integer.valueOf(userId), ft);
            if (null == f) {
                ft.run();
                f = ft;
            }
        }
        Condition condition = new CombinedCondition(new ModulesCondition(accessibleModules), new TypeCondition(FolderObject.SHARED, userId));
        /*
         * Iterate visible sets
         */
        TIntSet set = getFrom(f).getVisibleFolderIds(condition);
        if (null != set && !set.isEmpty()) {
            return true;
        }
        if (null != groups) {
            for (int group : groups) {
                f = entity2tree.get(Integer.valueOf(group));
                if (null == f) {
                    FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(group, LOG));
                    f = entity2tree.putIfAbsent(Integer.valueOf(group), ft);
                    if (null == f) {
                        ft.run();
                        f = ft;
                    }
                }

                ConditionTree conditionTree = getFrom(f);
                if (null != conditionTree && !conditionTree.getVisibleFolderIds(condition).isEmpty()) {
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
    public TIntSet getVisibleTypeForUser(int userId, int[] groups, int[] accessibleModules, int type) throws OXException {
        Future<ConditionTree> f = entity2tree.get(Integer.valueOf(userId));
        if (null == f) {
            FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(userId, LOG));
            f = entity2tree.putIfAbsent(Integer.valueOf(userId), ft);
            if (null == f) {
                ft.run();
                f = ft;
            }
        }
        Condition condition = new CombinedCondition(new ModulesCondition(accessibleModules), new TypeCondition(type, userId));
        /*
         * Iterate visible sets
         */
        TIntSet set = getFrom(f).getVisibleFolderIds(condition);
        if (null == set) {
            set = new TIntHashSet();
        }
        if (null != groups) {
            for (int group : groups) {
                f = entity2tree.get(Integer.valueOf(group));
                if (null == f) {
                    FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(group, LOG));
                    f = entity2tree.putIfAbsent(Integer.valueOf(group), ft);
                    if (null == f) {
                        ft.run();
                        f = ft;
                    }
                }

                ConditionTree conditionTree = getFrom(f);
                if (null != conditionTree) {
                    set.addAll(conditionTree.getVisibleFolderIds(condition));
                }
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
    public TIntSet getVisibleModuleForUser(int userId, int[] groups, int[] accessibleModules, int module) throws OXException {
        Condition condition;
        {
            if (null != accessibleModules) {
                TIntSet modules = new TIntHashSet(accessibleModules);
                if (!modules.contains(module)) {
                    return EMPTY_SET;
                }
            }
            condition = ModuleCondition.moduleCondition(module);
        }
        Future<ConditionTree> f = entity2tree.get(Integer.valueOf(userId));
        if (null == f) {
            FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(userId, LOG));
            f = entity2tree.putIfAbsent(Integer.valueOf(userId), ft);
            if (null == f) {
                ft.run();
                f = ft;
            }
        }
        /*
         * Iterate visible sets
         */
        TIntSet set = getFrom(f).getVisibleFolderIds(condition);
        if (null == set) {
            set = new TIntHashSet();
        }
        if (null != groups) {
            for (int group : groups) {
                f = entity2tree.get(Integer.valueOf(group));
                if (null == f) {
                    FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(group, LOG));
                    f = entity2tree.putIfAbsent(Integer.valueOf(group), ft);
                    if (null == f) {
                        ft.run();
                        f = ft;
                    }
                }
                ConditionTree ct = getFrom(f);
                if (null != ct) {
                    set.addAll(ct.getVisibleFolderIds(condition));
                }
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
    public TIntSet getVisibleForUser(int userId, int[] groups, int[] accessibleModules, int module, int type) throws OXException {
        Condition condition;
        {
            if (null != accessibleModules) {
                TIntSet modules = new TIntHashSet(accessibleModules);
                if (!modules.contains(module)) {
                    return EMPTY_SET;
                }
            }
            condition = new CombinedCondition(ModuleCondition.moduleCondition(module), new TypeCondition(type, userId));
        }
        Future<ConditionTree> f = entity2tree.get(Integer.valueOf(userId));
        if (null == f) {
            FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(userId, LOG));
            f = entity2tree.putIfAbsent(Integer.valueOf(userId), ft);
            if (null == f) {
                ft.run();
                f = ft;
            }
        }
        /*
         * Iterate visible sets
         */
        TIntSet set = getFrom(f).getVisibleFolderIds(condition);
        if (null == set) {
            set = new TIntHashSet();
        }
        if (null != groups) {
            for (int group : groups) {
                f = entity2tree.get(Integer.valueOf(group));
                if (null == f) {
                    FutureTask<ConditionTree> ft = new FutureTask<ConditionTree>(new InitEntityCallable(group, LOG));
                    f = entity2tree.putIfAbsent(Integer.valueOf(group), ft);
                    if (null == f) {
                        ft.run();
                        f = ft;
                    }
                }
                ConditionTree conditionTree = getFrom(f);
                if (null != conditionTree) {
                    set.addAll(conditionTree.getVisibleFolderIds(condition));
                }
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
    public static List<FolderObject> asList(TIntSet set, Context ctx) throws OXException {
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
    public static List<FolderObject> asList(TIntSet set, Context ctx, Connection con) throws OXException {
        if (null == set || set.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            /*
             * List of folders to return
             */
            final TIntObjectMap<FolderObject> m = new TIntObjectHashMap<FolderObject>(set.size());
            {
                TIntSet toLoad = new TIntHashSet(set);
                FolderCacheManager cacheManager = FolderCacheManager.getInstance();
                boolean cacheEnabled = FolderCacheManager.isEnabled();
                if (cacheEnabled) {
                    for (FolderObject fo : cacheManager.getTrimedFolderObjects(set.toArray(), ctx)) {
                        int objectID = fo.getObjectID();
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
            TIntProcedure procedure = new TIntProcedure() {

                @Override
                public boolean execute(int folderId) {
                    try {
                        FolderObject fo = m.get(folderId);
                        if (null != fo) {
                            list.add(fo);
                        }
                        return true;
                    } catch (Exception e) {
                        throw new ProcedureFailedException(e);
                    }
                }
            };
            set.forEach(procedure);
            return list;
        } catch (ProcedureFailedException e) {
            Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(cause, cause.getMessage());
        }
    }

    private static void loadBy(TIntSet toLoad, TIntObjectMap<FolderObject> m, boolean cacheEnabled, FolderCacheManager cacheManager, Context ctx) throws OXException {
        DatabaseService service = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
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

    private static void loadBy(TIntSet toLoad, TIntObjectMap<FolderObject> m, boolean cacheEnabled, final FolderCacheManager cacheManager, final Context ctx, Connection con) throws OXException {
        if (null == con) {
            loadBy(toLoad, m, cacheEnabled, cacheManager, ctx);
            return;
        }
        List<FolderObject> loaded = OXFolderBatchLoader.loadFolderObjectsFromDB(toLoad.toArray(), ctx, con, true, true);
        // Put to cache asynchronously with a separate list
        if (cacheEnabled) {
            final List<FolderObject> tmp = new ArrayList<FolderObject>(loaded);
            ThreadPools.getThreadPool().submit(new AbstractTask<Void>() {

                @Override
                public Void call() throws Exception {
                    for (FolderObject fo : tmp) {
                        if (null != fo) {
                            cacheManager.putFolderObject(fo, ctx, false, null);
                        }
                    }
                    return null;
                }
            });
        }
        // Put to map
        for (FolderObject fo : loaded) {
            if (null != fo) {
                m.put(fo.getObjectID(), fo);
            }
        }
    }

    protected static FolderObject getFolderObject(int folderId, Context ctx, Connection con) throws OXException {
        if (!FolderCacheManager.isEnabled()) {
            return FolderObject.loadFolderObjectFromDB(folderId, ctx, con, true, true);
        }
        FolderCacheManager cacheManager = FolderCacheManager.getInstance();
        FolderObject fo = cacheManager.getFolderObject(folderId, ctx);
        if (null == fo) {
            fo = FolderObject.loadFolderObjectFromDB(folderId, ctx, con, true, true);
            cacheManager.putFolderObject(fo, ctx, false, null);
        }
        return fo;
    }

    private static final class CombinedCondition implements Condition {

        private final List<Condition> conditions;

        protected CombinedCondition(Condition first, Condition... others) {
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

        public CombinedCondition(ModulesCondition first, Collection<Condition> conditions) {
            super();
            this.conditions = new ArrayList<Condition>(conditions.size() + 1);
            this.conditions.add(first);
            this.conditions.addAll(conditions);
        }

        @Override
        public boolean fulfilled(Permission p) {
            for (Condition condition : conditions) {
                if (!condition.fulfilled(p)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static final class PrivateCondition implements Condition {

        private final int userId;

        public PrivateCondition(int userId) {
            super();
            this.userId = userId;
        }

        @Override
        public boolean fulfilled(Permission p) {
            return (FolderObject.PRIVATE == p.type) && (p.creator == userId);
        }

    }

    public static final class ParentCondition implements Condition {

        private final int parent;

        public ParentCondition(int parent) {
            super();
            this.parent = parent;
        }

        @Override
        public boolean fulfilled(Permission p) {
            return (p.parent == parent);
        }

    }

    public static final class CreatorCondition implements Condition {

        private final int creator;

        public CreatorCondition(int creator) {
            super();
            this.creator = creator;
        }

        @Override
        public boolean fulfilled(Permission p) {
            return (p.creator == creator);
        }

    }

    public static final class TypeCondition implements Condition {

        private final int type;
        private final int userId;

        public TypeCondition(int type, int userId) {
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
        public boolean fulfilled(Permission p) {
            return (p.type == type) && (userId >= 0 ? (p.creator != userId) : true);
        }

    }

    public static final class ModuleCondition implements Condition {

        private static final ConcurrentMap<Integer, ModuleCondition> conditions = new ConcurrentHashMap<Integer, ModuleCondition>(8, 0.9f, 1);

        static ModuleCondition moduleCondition(int module) {
            Integer key = Integer.valueOf(module);
            ModuleCondition mc = conditions.get(key);
            if (null == mc) {
                ModuleCondition nmc = new ModuleCondition(module);
                mc = conditions.putIfAbsent(key, nmc);
                if (mc == null) {
                    mc = nmc;
                }
            }
            return mc;
        }

        private final int module;

        private ModuleCondition(int module) {
            super();
            this.module = module;
        }

        @Override
        public boolean fulfilled(Permission p) {
            return module == p.module;
        }

    }

    public static final class ModulesCondition implements Condition {

        private final TIntSet accessibleModules;

        public ModulesCondition(int[] accessibleModules) {
            super();
            this.accessibleModules = new TIntHashSet(accessibleModules);
        }

        public ModulesCondition(TIntCollection accessibleModules) {
            super();
            this.accessibleModules = new TIntHashSet(accessibleModules);
        }

        @Override
        public boolean fulfilled(Permission p) {
            return accessibleModules.contains(p.module);
        }

    }

    public static final class LastModifiedCondition implements Condition {

        private final long lastModified;

        public LastModifiedCondition(long lastModified) {
            super();
            this.lastModified = lastModified;
        }

        @Override
        public boolean fulfilled(Permission p) {
            return p.lastModified > lastModified;
        }

    }

    private static final class ProcedureFailedException extends RuntimeException {

        private static final long serialVersionUID = 1821041261492515385L;

        protected ProcedureFailedException(Throwable cause) {
            super(cause);
        }

    }

    // --------------------------------------------------------------------------------------- //

    protected ConditionTree getFrom(Future<ConditionTree> f) throws OXException {
        if (null == f) {
            return null;
        }
        try {
            return f.get();
        } catch (InterruptedException e) {
            // Cannot occur
            Thread.currentThread().interrupt();
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, e.getMessage());
        } catch (ExecutionException e) {
            throw ThreadPools.launderThrowable(e, OXException.class);
        }
    }

    protected ConditionTree timedFrom(Future<ConditionTree> f, long timeoutMillis) throws OXException {
        if (null == f) {
            return null;
        }
        try {
            return f.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // Cannot occur
            Thread.currentThread().interrupt();
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, e.getMessage());
        } catch (ExecutionException e) {
            throw ThreadPools.launderThrowable(e, OXException.class);
        } catch (TimeoutException e) {
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
        private final org.slf4j.Logger logger;

        protected InitEntityCallable(int entity, org.slf4j.Logger logger) {
            super();
            this.entity = entity;
            this.logger = logger;
        }

        @Override
        public ConditionTree call() throws OXException {
            try {
                return newTreeForEntity(entity);
            } catch (OXException e) {
                logger.warn("", e);
                throw e;
            } catch (RuntimeException e) {
                logger.error("", e);
                throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, e.getMessage());
            }
        }
    } // End of InitEntityCallable class

}
