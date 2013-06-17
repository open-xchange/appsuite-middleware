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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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

    private static final EmptyTIntSet EMPTY_SET = EmptyTIntSet.getInstance();

    private final ConcurrentMap<Integer, ConditionTree> trees;
    private final int contextId;
    protected final long stamp;

    /**
     * Initializes a new {@link ConditionTreeMap}.
     */
    public ConditionTreeMap(final int contextId, final long now) {
        super();
        trees = new ConcurrentHashMap<Integer, ConditionTree>(1024);
        this.contextId = contextId;
        stamp = now;
    }

    /**
     * Initializes the tree map for map's associated context.
     *
     * @throws OXException If initialization fails
     */
    public void init() throws OXException {
        trees.clear();

        final DatabaseService service = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            connection = service.getReadOnly(contextId);
            stmt =
                connection.prepareStatement("SELECT ot.fuid, op.permission_id, op.admin_flag, op.fp, ot.module, ot.type, ot.created_from, ot.changing_date, ot.parent" +
                    " FROM oxfolder_tree AS ot JOIN oxfolder_permissions AS op ON ot.cid = op.cid AND ot.fuid = op.fuid WHERE ot.cid = ?"
                    /*+ " ORDER BY default_flag DESC, fname"*/);
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
     * Clears whole tree map.
     */
    public void clear() {
        trees.clear();
    }

    /**
     * Removes the tree for specified entity.
     *
     * @param entity The entity identifier
     */
    public void removeFor(final int entity) {
        trees.remove(Integer.valueOf(entity));
    }

    /**
     * Feed given permission to this tree map.
     *
     * @param permission The permission
     */
    public void insert(final Permission permission) {
        final int entity = permission.entity;
        final Integer key = Integer.valueOf(entity);
        ConditionTree tree = trees.get(key);
        if (null == tree) {
            final ConditionTree newTree = new ConditionTree();
            tree = trees.putIfAbsent(key, newTree);
            if (null == tree) {
                tree = newTree;
            }
        }
        // Insert into tree
        tree.insert(permission);
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
    public TIntSet getVisibleForUser(final int userId, final int[] groups, final int[] accessibleModules, final Condition... conditions) {
        final ConditionTree tree = trees.get(Integer.valueOf(userId));
        if (null == tree) {
            return EMPTY_SET;
        }
        final Condition condition = new CombinedCondition(new ModulesCondition(accessibleModules), conditions);
        /*
         * Iterate visible sets
         */
        final TIntSet set = tree.getVisibleFolderIds(condition);
        if (null != groups) {
            for (final int group : groups) {
                final ConditionTree gtree = trees.get(Integer.valueOf(group));
                if (null != gtree) {
                    set.addAll(gtree.getVisibleFolderIds(condition));
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
    public TIntSet getVisibleForUser(final int userId, final int[] groups, final int[] accessibleModules, final Collection<Condition> conditions) {
        final ConditionTree tree = trees.get(Integer.valueOf(userId));
        if (null == tree) {
            return EMPTY_SET;
        }
        final Condition condition = new CombinedCondition(new ModulesCondition(accessibleModules), conditions);
        /*
         * Iterate visible sets
         */
        final TIntSet set = tree.getVisibleFolderIds(condition);
        if (null != groups) {
            for (final int group : groups) {
                final ConditionTree gtree = trees.get(Integer.valueOf(group));
                if (null != gtree) {
                    set.addAll(gtree.getVisibleFolderIds(condition));
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
    public boolean isVisibleFolder(final int userId, final int[] groups, final int[] accessibleModules, final int folderId) {
        final ConditionTree tree = trees.get(Integer.valueOf(userId));
        if (null == tree) {
            return false;
        }
        /*
         * Iterate visible sets
         */
        final Condition condition = new ModulesCondition(accessibleModules);
        TIntSet set = tree.getVisibleFolderIds(condition);
        if (!set.isEmpty() && set.contains(folderId)) {
            return true;
        }
        if (null != groups) {
            for (final int group : groups) {
                final ConditionTree gtree = trees.get(Integer.valueOf(group));
                if (null != gtree) {
                    set = gtree.getVisibleFolderIds(condition);
                    if (!set.isEmpty() && set.contains(folderId)) {
                        return true;
                    }
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
    public boolean hasSharedFolder(final int userId, final int[] groups, final int[] accessibleModules) {
        final ConditionTree tree = trees.get(Integer.valueOf(userId));
        if (null == tree) {
            return false;
        }
        final Condition condition = new CombinedCondition(new ModulesCondition(accessibleModules), new TypeCondition(FolderObject.SHARED, userId));
        /*
         * Iterate visible sets
         */
        final TIntSet set = tree.getVisibleFolderIds(condition);
        if (!set.isEmpty()) {
            return true;
        }
        if (null != groups) {
            for (final int group : groups) {
                final ConditionTree gtree = trees.get(Integer.valueOf(group));
                if (null != gtree && !gtree.getVisibleFolderIds(condition).isEmpty()) {
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
    public TIntSet getVisibleTypeForUser(final int userId, final int[] groups, final int[] accessibleModules, final int type) {
        final ConditionTree tree = trees.get(Integer.valueOf(userId));
        if (null == tree) {
            return EMPTY_SET;
        }
        final Condition condition = new CombinedCondition(new ModulesCondition(accessibleModules), new TypeCondition(type, userId));
        /*
         * Iterate visible sets
         */
        final TIntSet set = tree.getVisibleFolderIds(condition);
        if (null != groups) {
            for (final int group : groups) {
                final ConditionTree gtree = trees.get(Integer.valueOf(group));
                if (null != gtree) {
                    set.addAll(gtree.getVisibleFolderIds(condition));
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
    public TIntSet getVisibleModuleForUser(final int userId, final int[] groups, final int[] accessibleModules, final int module) {
        final ConditionTree tree = trees.get(Integer.valueOf(userId));
        if (null == tree) {
            return EMPTY_SET;
        }
        final Condition condition;
        {
            if (null != accessibleModules) {
                final TIntSet modules = new TIntHashSet(accessibleModules);
                if (!modules.contains(module)) {
                    return EMPTY_SET;
                }
            }
            condition = new CombinedCondition(new ModuleCondition(module));
        }
        /*
         * Iterate visible sets
         */
        final TIntSet set = tree.getVisibleFolderIds(condition);
        if (null != groups) {
            for (final int group : groups) {
                final ConditionTree gtree = trees.get(Integer.valueOf(group));
                if (null != gtree) {
                    set.addAll(gtree.getVisibleFolderIds(condition));
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
    public TIntSet getVisibleForUser(final int userId, final int[] groups, final int[] accessibleModules, final int module, final int type) {
        final ConditionTree tree = trees.get(Integer.valueOf(userId));
        if (null == tree) {
            return EMPTY_SET;
        }
        final Condition condition;
        {
            if (null != accessibleModules) {
                final TIntSet modules = new TIntHashSet(accessibleModules);
                if (!modules.contains(module)) {
                    return EMPTY_SET;
                }
            }
            condition = new CombinedCondition(new ModuleCondition(module), new TypeCondition(type, userId));
        }
        /*
         * Iterate visible sets
         */
        final TIntSet set = tree.getVisibleFolderIds(condition);
        if (null != groups) {
            for (final int group : groups) {
                final ConditionTree gtree = trees.get(Integer.valueOf(group));
                if (null != gtree) {
                    set.addAll(gtree.getVisibleFolderIds(condition));
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

        private final int module;

        public ModuleCondition(final int module) {
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

        public ProcedureFailedException(final Throwable cause) {
            super(cause);
        }

    }
}
