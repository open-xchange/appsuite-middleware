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

package com.openexchange.admin.storage.mysqlStorage;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.database.Databases.rollback;
import static com.openexchange.database.Databases.startTransaction;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.openexchange.admin.exceptions.TargetDatabaseException;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.Quota;
import com.openexchange.admin.rmi.dataobjects.SchemaSelectStrategy;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.EnforceableDataObjectException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.OXContextException;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.services.I18nServices;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUserStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.storage.sqlStorage.OXAdminPoolInterface;
import com.openexchange.admin.storage.sqlStorage.OXContextSQLStorage;
import com.openexchange.admin.storage.utils.PoolAndSchema;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.AdminCacheExtended;
import com.openexchange.admin.tools.PropertyHandlerExtended;
import com.openexchange.admin.tools.database.TableColumnObject;
import com.openexchange.admin.tools.database.TableObject;
import com.openexchange.admin.tools.database.TableRowObject;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.config.Reloadables;
import com.openexchange.database.Assignment;
import com.openexchange.database.Databases;
import com.openexchange.database.SchemaInfo;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorages;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFinishedListenerRegistry;
import com.openexchange.groupware.delete.DeleteRegistry;
import com.openexchange.groupware.downgrade.DowngradeEvent;
import com.openexchange.groupware.downgrade.DowngradeRegistry;
import com.openexchange.groupware.i18n.Groups;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.java.Sets;
import com.openexchange.java.Strings;
import com.openexchange.quota.groupware.AmountQuotas;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.CompletionFuture;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.openexchange.tools.oxfolder.OXFolderAdminHelper;
import com.openexchange.tools.pipesnfilters.DataSource;
import com.openexchange.tools.pipesnfilters.Filter;
import com.openexchange.tools.pipesnfilters.PipesAndFiltersException;
import com.openexchange.tools.pipesnfilters.PipesAndFiltersService;
import com.openexchange.tools.sql.DBUtils;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * This class provides the implementation for the storage into a MySQL database
 *
 * @author d7
 * @author cutmasta
 */
public class OXContextMySQLStorage extends OXContextSQLStorage {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXContextMySQLStorage.class);

    private final int maxNumberOfContextsPerSchema;

    private final boolean lockOnWriteContextToPayloadDb;

    private final String selectionCriteria = "cid";

    private final int criteriaType = Types.INTEGER;

    private final OXContextMySQLStorageCommon contextCommon;

    private final PropertyHandlerExtended prop;

    /**
     * Initializes a new {@link OXContextMySQLStorage}.
     */
    public OXContextMySQLStorage() {
        super();
        this.prop = cache.getProperties();
        this.contextCommon = new OXContextMySQLStorageCommon();

        int maxNumberOfContextsPerSchema = 1;
        try {
            maxNumberOfContextsPerSchema = Integer.parseInt(prop.getProp("CONTEXTS_PER_SCHEMA", "1"));
            if (maxNumberOfContextsPerSchema <= 0) {
                throw new OXContextException("CONTEXTS_PER_SCHEMA MUST BE > 0");
            }
        } catch (final OXContextException e) {
            LOG.error("Error init", e);
        }
        this.maxNumberOfContextsPerSchema = maxNumberOfContextsPerSchema;

        boolean lockOnWriteContextToPayloadDb = false;
        try {
            String sbol = prop.getProp("LOCK_ON_WRITE_CONTEXT_INTO_PAYLOAD_DB", "false").trim();
            if ("true".equalsIgnoreCase(sbol)) {
                lockOnWriteContextToPayloadDb = true;
            } else if ("false".equalsIgnoreCase(sbol)) {
                lockOnWriteContextToPayloadDb = false;
            } else {
                throw new OXContextException("LOCK_ON_WRITE_CONTEXT_INTO_PAYLOAD_DB MUST BE EITHER \"true\" or \"false\"");
            }
        } catch (OXContextException e) {
            LOG.error("Error init", e);
        }
        this.lockOnWriteContextToPayloadDb = lockOnWriteContextToPayloadDb;
    }

    @Override
    public void delete(final Context ctx) throws StorageException {
        // Delete filestores of the context
        {
            LOG.debug("Starting filestore deletion for context {}...", ctx.getId());
            Utils.removeFileStorages(ctx, true);
            LOG.debug("Filestore deletion for context {} from finished!", ctx.getId());
        }

        AdminCacheExtended adminCache = cache;

        // Delete context data from context-associated schema
        try {
            // Get connection and schema for given context
            int poolId;
            String scheme;
            try {
                SchemaInfo schemaInfo = adminCache.getSchemaInfoForContextId(ctx.getId().intValue());
                poolId = schemaInfo.getPoolId();
                scheme = schemaInfo.getSchema();
            } catch (PoolException e) {
                throw new StorageException(e);
            }

            DBUtils.TransactionRollbackCondition condition = new DBUtils.TransactionRollbackCondition(3);
            do {
                SubmittingRunnable<Void> pendingInvocation = null;
                Connection conForContext = null;
                try {
                    // Initialize connection to context-associated database schema
                    conForContext = adminCache.getWRITENoTimeoutConnectionForPoolId(poolId, scheme);
                    List<Integer> userIds = getUsersToDelete(ctx, conForContext);
                    // Loop through tables and execute delete statements on each table (using transaction)
                    pendingInvocation = deleteContextData(ctx, conForContext, userIds, poolId, scheme);
                } catch (PoolException e) {
                    LOG.error("Pool Error", e);
                    throw new StorageException(e);
                } catch (SQLException sql) {
                    if (!condition.isFailedTransactionRollback(sql)) {
                        LOG.error("SQL Error", sql);
                        throw new StorageException(sql.toString(), sql);
                    }
                } finally {
                    // Needs to be pushed back here, because in the "deleteContextFromConfigDB()" the connection is "reset" in the pool.
                    if (null != conForContext) {
                        try {
                            adminCache.pushWRITENoTimeoutConnectionForPoolId(poolId, conForContext);
                        } catch (PoolException e) {
                            LOG.error("Pool Error", e);
                        }
                    }
                }

                if (null != pendingInvocation) {
                    pendingInvocation.run();
                }
            } while (retryDelete(condition, ctx));
        } catch (SQLException sql) {
            throw new StorageException(sql.toString(), sql);
        }

        // Delete context from ConfigDB
        try {
            DBUtils.TransactionRollbackCondition condition = new DBUtils.TransactionRollbackCondition(3);
            do {
                Connection conForConfigDB = null;
                condition.resetTransactionRollbackException();
                boolean rollbackConfigDB = false;
                try {
                    // Get connection for ConfigDB
                    conForConfigDB = adminCache.getWriteConnectionForConfigDB();

                    // Start transaction on ConfigDB
                    Databases.startTransaction(conForConfigDB);
                    rollbackConfigDB = true;

                    // Execute to delete context on Configdb AND to drop associated database if this context is the last one
                    contextCommon.deleteContextFromConfigDB(conForConfigDB, ctx.getId().intValue());

                    // submit delete to database under any circumstance before the filestore gets deleted.see bug 9947
                    conForConfigDB.commit();
                    rollbackConfigDB = false;

                    LOG.info("Context {} deleted.", ctx.getId());
                } catch (PoolException e) {
                    LOG.error("Pool Error", e);
                    throw new StorageException(e);
                } catch (StorageException st) {
                    // Examine cause
                    SQLException sqle = DBUtils.extractSqlException(st);
                    if (!condition.isFailedTransactionRollback(sqle)) {
                        LOG.error("Storage Error", st);
                        throw st;
                    }
                } catch (SQLException sql) {
                    if (!condition.isFailedTransactionRollback(sql)) {
                        LOG.error("SQL Error", sql);
                        throw new StorageException(sql.toString(), sql);
                    }
                } finally {
                    if (rollbackConfigDB) {
                        rollback(conForConfigDB);
                    }
                    autocommit(conForConfigDB);
                    if (null != conForConfigDB) {
                        try {
                            adminCache.pushWriteConnectionForConfigDB(conForConfigDB);
                        } catch (PoolException exp) {
                            LOG.error("Pool Error", exp);
                        }
                    }
                }
            } while (retryDelete(condition, ctx));
        } catch (SQLException sql) {
            throw new StorageException(sql.toString(), sql);
        }

        // Invalidate caches
        try {
            final int contextID = ctx.getId().intValue();
            ContextStorage.getInstance().invalidateContext(contextID);
            final CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
            if (null != cacheService) {
                try {
                    cacheService.getCache("MailAccount").clear();
                } catch (final Exception e) {
                    LOG.error("", e);
                }
                try {
                    cacheService.getCache("Capabilities").invalidateGroup(ctx.getId().toString());
                } catch (final Exception e) {
                    LOG.error("", e);
                }
            }
        } catch (final Exception e) {
            LOG.error("Error invalidating context {} in ox context storage", ctx.getId(), e);
        }
    }

    /**
     * Gets the users to delete
     *
     * @param ctx The {@link Context}
     * @param conForContext The {@link Connection}
     * @return A {@link List} with all users of the context, or an empty list if there are none.
     * @throws SQLException if an SQL Error is occurred
     */
    private List<Integer> getUsersToDelete(final Context ctx, Connection conForContext) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conForContext.prepareStatement("SELECT id FROM user WHERE cid=?");
            stmt.setInt(1, ctx.getId().intValue());
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptyList();
            }

            List<Integer> userIds = new LinkedList<>();
            do {
                userIds.add(Integer.valueOf(rs.getInt(1)));
            } while (rs.next());
            return userIds;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private boolean retryDelete(DBUtils.TransactionRollbackCondition condition, Context ctx) throws SQLException {
        SQLException sqle = condition.getTransactionRollbackException();
        boolean retry = condition.checkRetry();
        if (retry) {
            // Wait with exponential backoff
            int retryCount = condition.getCount();
            long nanosToWait = TimeUnit.NANOSECONDS.convert((retryCount * 1000) + ((long) (Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            LOG.info("Retrying to delete context {} as suggested by: {}", ctx.getId(), sqle.getMessage());
        }
        return retry;
    }

    private SubmittingRunnable<Void> deleteContextData(Context ctx, final Connection conForContext, List<Integer> userIds, final int poolId, final String scheme) throws SQLException {
        LOG.debug("Now deleting data for context {} from schema {} in database {}", ctx.getId(), scheme, Integer.valueOf(poolId));

        ThreadPoolService threadPool = AdminServiceRegistry.getInstance().getService(ThreadPoolService.class);

        // Initiate transaction & fire delete event
        {
            boolean rollback = false;
            try {
                conForContext.setAutoCommit(false);
                rollback = true;

                fireDeleteEventAndOptionallyDeleteTableData(ctx, conForContext, userIds, null == threadPool);

                // Commit groupware data scheme deletes BEFORE database get dropped in "deleteContextFromConfigDB" .see bug #10501
                conForContext.commit();
                rollback = false;

                try {
                    DeleteEvent event = DeleteEvent.createDeleteEventForContextDeletion(this, ctx.getId().intValue(), userIds);
                    DeleteFinishedListenerRegistry.getInstance().fireDeleteEvent(event);
                } catch (Exception e) {
                    LOG.warn("Failed to trigger delete finished listeners", e);
                }
            } finally {
                if (rollback) {
                    rollback(conForContext);
                }
                autocommit(conForContext);
            }
        }

        if (null == threadPool) {
            LOG.debug("Data delete for context {} from schema {} in database {} completed!", ctx.getId(), scheme, Integer.valueOf(poolId));
            return null;
        }

        // Create a task to hard-cleanse from tables and pushing back used connection to pool
        final AdminCacheExtended adminCache = cache;
        final Integer contextId = ctx.getId();
        final String selectionCriteria = this.selectionCriteria;
        AbstractTask<Void> task = new AbstractTask<Void>() {

            @Override
            public Void call() throws Exception {
                Connection conForContext = null;
                try {
                    conForContext = adminCache.getWRITENoTimeoutConnectionForPoolId(poolId, scheme);
                    deleteTablesData(selectionCriteria, contextId, conForContext, false);
                    LOG.debug("Data delete for context {} from schema {} in database {} completed!", contextId, scheme, Integer.valueOf(poolId));
                } finally {
                    try {
                        adminCache.pushWRITENoTimeoutConnectionForPoolId(poolId, conForContext);
                    } catch (PoolException e) {
                        LOG.error("Pool Error", e);
                    }
                }
                return null;
            }
        };
        return new SubmittingRunnable<Void>(task, threadPool);
    }

    private void fireDeleteEventAndOptionallyDeleteTableData(Context ctx, Connection con, List<Integer> userIds, boolean deleteTablesData) throws SQLException {
        // First delete everything with OSGi DeleteListener services.
        try {
            DeleteEvent event = DeleteEvent.createDeleteEventForContextDeletion(this, ctx.getId().intValue(), userIds);
            DeleteRegistry.getInstance().fireDeleteEvent(event, con, con);
        } catch (Exception e) {
            SQLException sqle = DBUtils.extractSqlException(e);
            if (null != sqle) {
                throw sqle;
            }
            LOG.error("Some implementation deleting context specific data failed. Continuing with hard delete from tables using cid column.", e);
        }

        // Now go through tables and delete the remainders (if desired)
        if (deleteTablesData) {
            deleteTablesData(selectionCriteria, ctx.getId(), con, true);
        }
    }

    static void deleteTablesData(String selectionCriteria, Integer contextId, Connection conForContext, boolean failOnError) throws SQLException {
        // Fetch tables which can contain context data and sort these tables magically by foreign keys
        LOG.debug("Fetching table structure from database scheme for context {}", contextId);
        List<TableObject> fetchTableObjects = fetchTableObjects(selectionCriteria, conForContext);
        LOG.debug("Table structure fetched for context {}\nTry to find foreign key dependencies between tables and sort table for context {}", contextId, contextId);

        // Sort the tables by references (foreign keys)
        List<TableObject> sorted_tables = sortTableObjects(fetchTableObjects, conForContext);
        LOG.debug("Dependencies found and tables sorted for context {}", contextId);

        StringBuilder stmtBuilder = new StringBuilder(64).append("DELETE FROM ");
        int reslen = stmtBuilder.length();
        for (int i = sorted_tables.size(); i-- > 0;) {
            stmtBuilder.setLength(reslen);
            if (failOnError) {
                deleteTableData(sorted_tables.get(i).getName(), contextId, stmtBuilder, conForContext);
            } else {
                deleteTableDataSafe(sorted_tables.get(i).getName(), contextId, stmtBuilder, conForContext);
            }
        }
    }

    private static void deleteTableDataSafe(String tableName, Integer contextId, StringBuilder stmtBuilder, Connection con) {
        try {
            deleteTableData(tableName, contextId, stmtBuilder, con);
        } catch (Exception e) {
            LOG.warn("Failed to remove possibly remaining entries from table '{}' during deletion of context {}", tableName, contextId, e);
        }
    }

    private static void deleteTableData(String tableName, Integer contextId, StringBuilder stmtBuilder, Connection con) throws SQLException {
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.executeUpdate(stmtBuilder.append(tableName).append(" WHERE cid=").append(contextId).toString());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    /**
     * @throws StorageException
     * @see com.openexchange.admin.storage.sqlStorage.OXContextSQLStorage#disableAllContexts(int)
     */
    @Override
    public void disableAll(final MaintenanceReason reason) throws StorageException {
        disableAll(reason, null, null);
    }

    /**
     * @throws StorageException
     * @see com.openexchange.admin.storage.sqlStorage.OXContextSQLStorage#disableAllContexts(int)
     */
    @Override
    public void disableAll(final MaintenanceReason reason, final String addtionaltable, final String sqlconjunction) throws StorageException {
        try {
            myLockUnlockAllContexts(false, reason.getId().intValue(), addtionaltable, sqlconjunction);
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }
    }

    /**
     * @throws StorageException
     * @see com.openexchange.admin.storage.sqlStorage.OXContextSQLStorage#disableContext(int, int)
     */
    @Override
    public void disable(final Context ctx, final MaintenanceReason reason) throws StorageException {
        try {
            myEnableDisableContext(ctx.getId().intValue(), false, reason.getId().intValue());
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }
    }

    @Override
    public void disable(String schema, MaintenanceReason reason) throws StorageException {
        Connection con = null;
        boolean readOnly = true;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getReadConnectionForConfigDB();

            stmt = con.prepareStatement("SELECT cid FROM context_server2db_pool WHERE db_schema = ?");
            stmt.setString(1, schema);
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                // No contexts available for specified schema
                return;
            }

            // Put context identifiers into a list
            List<Integer> contextIds = new ArrayList<>(maxNumberOfContextsPerSchema >> 1);
            do {
                contextIds.add(Integer.valueOf(rs.getInt(1)));
            } while (rs.next());
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            // Switch from read-only to read-write connection
            cache.pushReadConnectionForConfigDB(con);
            con = null; // Null'ify prior to reassign
            con = cache.getWriteConnectionForConfigDB();
            readOnly = false;

            int numDisabled = 0;
            for (List<Integer> partition : Lists.partition(contextIds, Databases.IN_LIMIT)) {
                stmt = con.prepareStatement(Databases.getIN("UPDATE context SET enabled = 0, reason_id = ? WHERE enabled = 1 AND cid IN (", partition.size()));
                stmt.setInt(1, reason.getId().intValue());
                int pos = 2;
                for (Integer contextId : partition) {
                    stmt.setInt(pos++, contextId.intValue());
                }
                numDisabled += stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);
                stmt = null;
            }

            LOG.info("Disabled {} contexts in schema '{}' with reason {}", numDisabled, schema, reason.getId());
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (con != null) {
                try {
                    if (readOnly) {
                        cache.pushReadConnectionForConfigDB(con);
                    } else {
                        cache.pushWriteConnectionForConfigDB(con);
                    }
                } catch (PoolException e) {
                    LOG.error("Error pushing configdb connection to pool!", e);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.admin.storage.interfaces.OXContextStorageInterface#enableAll()
     */
    @Override
    public void enableAll() throws StorageException {
        enableAll(null, null);
    }

    /**
     * @throws StorageException
     * @see com.openexchange.admin.storage.sqlStorage.OXContextSQLStorage#enableAllContexts()
     */
    @Override
    public void enableAll(final String additionaltable, final String sqlconjunction) throws StorageException {
        try {
            myLockUnlockAllContexts(true, 1, additionaltable, sqlconjunction);
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }
    }

    /**
     * @throws StorageException
     * @see com.openexchange.admin.storage.sqlStorage.OXContextSQLStorage#enableContext(int)
     */
    @Override
    public void enable(final Context ctx) throws StorageException {
        try {
            myEnableDisableContext(ctx.getId().intValue(), true, -1);
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }
    }

    @Override
    public void enable(String schema, MaintenanceReason reason) throws StorageException {
        Connection con = null;
        boolean readOnly = true;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getReadConnectionForConfigDB();

            stmt = con.prepareStatement("SELECT cid FROM context_server2db_pool WHERE db_schema = ?");
            stmt.setString(1, schema);
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                // No contexts available for specified schema
                return;
            }

            // Put context identifiers into a list
            List<Integer> contextIds = new ArrayList<>(maxNumberOfContextsPerSchema >> 1);
            do {
                contextIds.add(Integer.valueOf(rs.getInt(1)));
            } while (rs.next());
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            // Switch from read-only to read-write connection
            cache.pushReadConnectionForConfigDB(con);
            con = null; // Null'ify prior to reassign
            con = cache.getWriteConnectionForConfigDB();
            readOnly = false;

            int numEnabled = 0;
            for (List<Integer> partition : Lists.partition(contextIds, Databases.IN_LIMIT)) {
                stmt = con.prepareStatement(Databases.getIN("UPDATE context SET enabled = 1, reason_id = NULL WHERE enabled = 0 " + (reason == null ? "" : "AND reason_id = ? ") + "AND cid IN (", partition.size()));
                int pos = 1;
                if (reason != null) {
                    stmt.setInt(pos++, reason.getId());
                }
                for (Integer contextId : partition) {
                    stmt.setInt(pos++, contextId.intValue());
                }
                numEnabled += stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);
                stmt = null;
            }

            LOG.info("Enabled {} contexts in schema '{}' with reason {}", numEnabled, schema, reason == null ? "'all'" : reason.getId());
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(stmt);
            if (con != null) {
                try {
                    if (readOnly) {
                        cache.pushReadConnectionForConfigDB(con);
                    } else {
                        cache.pushWriteConnectionForConfigDB(con);
                    }
                } catch (PoolException e) {
                    LOG.error("Error pushing configdb connection to pool!", e);
                }
            }
        }
    }

    @Override
    public Set<String> getLoginMappings(Context ctx) throws StorageException {
        Connection configCon = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            configCon = cache.getReadConnectionForConfigDB();
            prep = configCon.prepareStatement("SELECT login_info FROM login2context WHERE cid=?");
            prep.setInt(1, ctx.getId().intValue());
            rs = prep.executeQuery();
            if (false == rs.next()) {
                return Collections.emptySet();
            }

            Set<String> loginMappings = new HashSet<String>(4);
            String idAsString = ctx.getIdAsString();
            do {
                String loginMapping = rs.getString(1);
                // DO NOT RETURN THE CONTEXT ID AS A MAPPING!!
                // THIS CAN CAUSE ERRORS IF CHANGING LOGINMAPPINGS AFTERWARDS!
                // SEE #11094 FOR DETAILS!
                if (null != loginMapping && !idAsString.equals(loginMapping)) {
                    loginMappings.add(loginMapping);
                }
            } while (rs.next());
            return loginMappings;
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(prep);
            if (null != configCon) {
                try {
                    cache.pushReadConnectionForConfigDB(configCon);
                } catch (final PoolException exp) {
                    LOG.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    /**
     * @throws StorageException
     * @see com.openexchange.admin.storage.interfaces.OXContextStorageInterface#getData(com.openexchange.admin.rmi.dataobjects.Context)
     */
    @Override
    public Context getData(final Context ctx) throws StorageException {
        return getData(new Context[] { ctx })[0];
    }

    @Override
    public Context[] getData(final Context[] ctxs) throws StorageException {
        // returns webdav infos, database infos(mapping), context status
        // (disabled,enabled,text)
        final Connection configCon;
        try {
            configCon = cache.getReadConnectionForConfigDB();
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }
        try {

            final ArrayList<Context> retval = new ArrayList<Context>();
            for (final Context ctx : ctxs) {
                retval.add(contextCommon.getData(ctx, configCon, Long.parseLong(prop.getProp("AVERAGE_CONTEXT_SIZE", "100"))));
            }
            return retval.toArray(new Context[retval.size()]);
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            if (null != configCon) {
                try {
                    cache.pushReadConnectionForConfigDB(configCon);
                } catch (final PoolException exp) {
                    LOG.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    /**
     * @throws StorageException
     * @see com.openexchange.admin.storage.sqlStorage.OXContextSQLStorage#moveDatabaseContext(int, Database, int)
     */
    @Override
    public void moveDatabaseContext(final Context ctx, final Database target_database_id, final MaintenanceReason reason) throws StorageException {
        long start = System.currentTimeMillis();
        LOG.debug("Move of data for context {} is now starting to target database {}!", ctx.getId(), target_database_id);
        final int source_database_id;
        final String scheme;
        try {
            source_database_id = cache.getDBPoolIdForContextId(ctx.getId().intValue());
            scheme = cache.getSchemeForContextId(ctx.getId().intValue());
        } catch (PoolException e) {
            LOG.error(e.getMessage(), e);
            throw new StorageException(e.getMessage(), e);
        }
        // backup old mapping in contextserver2dbpool for recovery if something breaks
        LOG.debug("Backing up current configdb entries for context {}", ctx.getId());
        final Database dbHandleBackup = OXToolStorageInterface.getInstance().loadDatabaseById(source_database_id);
        dbHandleBackup.setScheme(scheme);
        // ####### ##### geht hier was kaputt -> enableContext(); ########
        LOG.debug("Backup complete!");

        Connection ox_db_write_con = null;
        Connection configdb_write_con = null;
        PreparedStatement stm = null;
        Connection target_ox_db_con = null;
        try {
            ox_db_write_con = cache.getWRITENoTimeoutConnectionForPoolId(source_database_id, scheme);

            /*
             * 1. Lock the context if not already locked. if already locked, throw exception cause the context could be already in progress
             * for moving.
             */
            LOG.debug("Context {} will now be disabled for moving!", ctx.getId());
            disable(ctx, reason);
            LOG.debug("Context {} is now disabled!", ctx.getId());

            /*
             * 2. Fetch tables with cid column which could perhaps store data relevant for us
             */
            LOG.debug("Fetching table structure from database scheme!");
            final List<TableObject> fetchTableObjects = fetchTableObjects(this.selectionCriteria, ox_db_write_con);
            // ####### ##### geht hier was kaputt -> enableContext(); ########
            LOG.debug("Table structure fetched!");

            // this must sort the tables by references (foreign keys)
            LOG.debug("Try to find foreign key dependencies between tables and sort table!");
            final List<TableObject> sorted_tables = sortTableObjects(fetchTableObjects, ox_db_write_con);
            // ####### ##### geht hier was kaputt -> enableContext(); ########
            LOG.debug("Dependencies found and tables sorted!");

            // fetch data for db handle to create database
            LOG.debug("Get database handle information for target database system!");
            final Database db_handle = OXToolStorageInterface.getInstance().loadDatabaseById(i(target_database_id.getId()));
            // ####### ##### geht hier was kaputt -> enableContext(); ########
            LOG.debug("Database handle information found!");

            // create database or use existing database AND update the mapping in contextserver2dbpool
            LOG.debug("Creating new scheme or using existing scheme on target database system!");
            configdb_write_con = cache.getWriteConnectionForConfigDB();
            startTransaction(configdb_write_con);
            createDatabaseAndMappingForContext(db_handle, configdb_write_con, ctx.getId().intValue());
            LOG.debug("Scheme found and mapping in configdb changed to new target database system!");

            // now insert all data to target db
            LOG.debug("Now filling target database system {} with data of context {}!", target_database_id, ctx.getId());
            target_ox_db_con = cache.getConnectionForContextNoTimeout(ctx.getId().intValue());
            try {
                target_ox_db_con.setAutoCommit(false);
                fillTargetDatabase(sorted_tables, target_ox_db_con, ox_db_write_con, ctx.getId());
                // commit ALL tables with all data of every row
                target_ox_db_con.commit();
            } catch (final SQLException sql) {
                rollback(target_ox_db_con);
                LOG.error("SQL Error", sql);
                throw new TargetDatabaseException("" + sql.getMessage());
            }

            LOG.debug("Filling completed for target database system {} with data of context {}!", target_database_id, ctx.getId());

            // now delete from old database schema all the data
            // For delete from database we loop recursive
            ox_db_write_con.setAutoCommit(false);
            LOG.debug("Now deleting data for context {} from old scheme!", ctx.getId());
            for (int a = sorted_tables.size() - 1; a >= 0; a--) {
                final TableObject to = sorted_tables.get(a);
                stm = ox_db_write_con.prepareStatement("DELETE FROM " + to.getName() + " WHERE cid = ?");
                stm.setInt(1, ctx.getId().intValue());
                LOG.debug("Deleting data from table \"{}\" for context {}", to.getName(), ctx.getId());
                stm.executeUpdate();
                stm.close();
            }
            LOG.debug("Data delete for context {} completed!", ctx.getId());

            configdb_write_con.commit();
            ox_db_write_con.commit();
        } catch (final TargetDatabaseException tde) {
            LOG.error("Exception caught while moving data for context {} to target database {}", ctx.getId(), target_database_id, tde);
            LOG.error("Target database rollback starts for context {}", ctx.getId());

            // revoke contextserver2dbpool()
            try {
                LOG.error("Now revoking entries in configdb (cs2dbpool) for context {}", ctx.getId());
                updateContextServer2DbPool(dbHandleBackup, configdb_write_con, i(ctx.getId()));
            } catch (PoolException e) {
                LOG.error("!!!!!!WARNING!!!!! Could not revoke configdb entries for " + ctx.getId() + "!!!!!!WARNING!!! INFORM ADMINISTRATOR!!!!!!", e);
            }
            throw new StorageException(tde);
        } catch (final SQLException sql) {
            // enableContext back
            LOG.error("SQL Error caught while moving data for context {} to target database {}", ctx.getId(), target_database_id, sql);

            // rollback
            if (ox_db_write_con != null) {
                try {
                    ox_db_write_con.rollback();
                } catch (final SQLException ecp) {
                    LOG.error("Error rollback connection", ecp);
                }
            }

            // rollback
            if (configdb_write_con != null) {
                try {
                    configdb_write_con.rollback();
                } catch (final Exception ecp) {
                    LOG.error("Error rollback connection", ecp);
                }
            }
            throw new StorageException(sql);
        } catch (final PoolException pexp) {
            LOG.error("Pool exception caught!", pexp);

            // rollback
            if (null != ox_db_write_con) {
                try {
                    ox_db_write_con.rollback();
                } catch (final SQLException ecp) {
                    LOG.error("Error rollback connection", ecp);
                }
            }

            // rollback
            try {
                if (null != configdb_write_con && !configdb_write_con.getAutoCommit()) {
                    try {
                        configdb_write_con.rollback();
                    } catch (final SQLException ecp) {
                        LOG.error("Error rollback connection", ecp);
                    }
                }
            } catch (final SQLException e) {
                LOG.error("SQL Error", e);
                e.initCause(pexp);
                throw new StorageException(e);
            }
            throw new StorageException(pexp);
        } finally {
            if (ox_db_write_con != null) {
                Databases.autocommit(ox_db_write_con);
                try {
                    cache.pushWRITENoTimeoutConnectionForPoolId(source_database_id, ox_db_write_con);
                } catch (final Exception ex) {
                    LOG.error("Error pushing connection", ex);
                }
            }
            if (configdb_write_con != null) {
                Databases.autocommit(configdb_write_con);
                try {
                    cache.pushWriteConnectionForConfigDB(configdb_write_con);
                } catch (final Exception ex) {
                    LOG.error("Error pushing connection", ex);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (final Exception ex) {
                    LOG.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, ex);
                }
            }
            if (target_ox_db_con != null) {
                Databases.autocommit(target_ox_db_con);
                try {
                    cache.pushWRITENoTimeoutConnectionForPoolId(target_database_id.getId().intValue(), target_ox_db_con);
                } catch (final Exception ex) {
                    LOG.error("Error pushing connection", ex);
                }
            }
            LOG.debug("Enabling context {} back again!", ctx.getId());
            enable(ctx);
        }
        if (LOG.isDebugEnabled()) {
            long time = (System.currentTimeMillis() - start);
            LOG.debug("Data moving for context {} to target database system {} completed in {}msec!", ctx.getId(), target_database_id, Long.toString(time));
        }
    }

    @Override
    public String moveContextFilestore(final Context ctx, final Filestore dst_filestore_id, final MaintenanceReason reason) throws StorageException {
        return null;
    }

    private static <T> List<T> listFor(Collection<T> col) {
        if (null == col) {
            return null;
        }

        if (col instanceof List) {
            return (List<T>) col;
        }

        return new ArrayList<>(col);
    }

    @Override
    public Context[] listContext(String pattern, List<Filter<Integer, Integer>> filters, List<Filter<Context, Context>> loaders, int offset, int length) throws StorageException {
        boolean withLimit = true;
        if (offset < 0 || length < 0) {
            withLimit = false;
        }
        if (withLimit && length < 0) {
            throw new StorageException("Invalid length: " + length);
        }
        if (withLimit && (offset + length) < 0) {
            throw new StorageException("Invalid offset/length: " + offset + ", " + length);
        }
        if (length == 0) {
            return new Context[0];
        }

        String sqlPattern = null == pattern ? null : pattern.replace('*', '%');
        if (Strings.containsSurrogatePairs(sqlPattern)) {
            return new Context[0];
        }
        if ((null == sqlPattern || "%".equals(sqlPattern)) && (null == filters || filters.isEmpty())) {
            Connection con = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                con = cache.getReadConnectionForConfigDB();
                if (withLimit) {
                    stmt = con.prepareStatement("SELECT cid, name, enabled, reason_id, filestore_id, filestore_name, quota_max FROM context ORDER BY cid LIMIT " + offset + ", " + length);
                } else {
                    stmt = con.prepareStatement("SELECT cid, name, enabled, reason_id, filestore_id, filestore_name, quota_max FROM context");
                }
                rs = stmt.executeQuery();
                if (false == rs.next()) {
                    return new Context[0];
                }

                Long averageContextFileStoreSize = Long.valueOf(prop.getProp("AVERAGE_CONTEXT_SIZE", "100"));
                List<Context> contexts = ContextLoadUtility.loadBasicContexts(rs, true, averageContextFileStoreSize, length);
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;

                // Check if empty
                if (contexts.isEmpty()) {
                    return new Context[0];
                }

                // Load login mappings per context and group by database schema association
                TIntObjectMap<Context> id2context = new TIntObjectHashMap<Context>(Databases.IN_LIMIT);
                Map<PoolAndSchema, List<Context>> schema2contexts = ContextLoadUtility.fillLoginMappingsAndDatabases(contexts, id2context, con);

                // Connection to ConfigDB no more needed
                cache.pushReadConnectionForConfigDB(con);
                con = null;

                // Query used quota per schema
                ContextLoadUtility.fillUsageAndAttributes(schema2contexts, true, id2context, cache);
                id2context = null; // Might help GC

                if (null != loaders && false == loaders.isEmpty()) {
                    try {
                        for (Filter<Context, Context> loader : loaders) {
                            loader.filter(contexts);
                        }
                    } catch (final PipesAndFiltersException e) {
                        final Throwable cause = e.getCause();
                        if (cause instanceof StorageException) {
                            throw (StorageException) cause;
                        }
                        throw new StorageException(cause.getMessage(), cause);
                    }
                }

                return contexts.toArray(new Context[contexts.size()]);
            } catch (PoolException e) {
                throw new StorageException(e);
            } catch (SQLException e) {
                throw new StorageException(e);
            } finally {
                Databases.closeSQLStuff(rs, stmt);
                if (null != con) {
                    try {
                        cache.pushReadConnectionForConfigDB(con);
                    } catch (PoolException e1) {
                        LOG.error("", e1);
                    }
                }
            }
        }

        // Search pattern and/or additional filters specified
        Collection<Integer> cids;
        if (null == sqlPattern || "%".equals(sqlPattern)) {
            cids = new ContextSearcher(cache, "SELECT cid FROM context ORDER BY cid", null).execute();
        } else {
            ThreadPoolService threadPool;
            try {
                threadPool = AdminServiceRegistry.getInstance().getService(ThreadPoolService.class, true);
            } catch (final OXException e) {
                throw new StorageException(e.getMessage(), e);
            }

            List<ContextSearcher> searchers = new ArrayList<ContextSearcher>();
            searchers.add(new ContextSearcher(cache, "SELECT cid FROM context WHERE name LIKE ?", sqlPattern));
            searchers.add(new ContextSearcher(cache, "SELECT cid FROM login2context WHERE login_info LIKE ?", sqlPattern));
            int optContextId = Strings.parsePositiveInt(sqlPattern);
            if (optContextId > 0) {
                searchers.add(new NumericContextSearcher(cache, optContextId));
            }

            // Invoke & add into sorted set
            CompletionFuture<Collection<Integer>> completion = threadPool.invoke(searchers);
            cids = new TreeSet<Integer>();
            try {
                for (int i = searchers.size(); i-- > 0;) {
                    cids.addAll(completion.take().get());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new StorageException(e.getMessage(), e);
            } catch (CancellationException e) {
                throw new StorageException(e.getMessage(), e);
            } catch (ExecutionException e) {
                throw ThreadPools.launderThrowable(e, StorageException.class);
            }
        }

        if (cids.isEmpty()) {
            return new Context[0];
        }

        if (null != filters && filters.size() > 0) {
            PipesAndFiltersService pnfService;
            try {
                pnfService = AdminServiceRegistry.getInstance().getService(PipesAndFiltersService.class, true);
            } catch (final OXException e) {
                throw new StorageException(e.getMessage(), e);
            }
            DataSource<Integer> output = pnfService.create(cids);
            for (final Object f : filters.toArray()) {
                output = output.addFilter((Filter<Integer, Integer>) f);
            }
            Set<Integer> filteredCids = new HashSet<Integer>(cids.size());
            try {
                while (output.hasData()) {
                    output.getData(filteredCids);
                }
            } catch (final PipesAndFiltersException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof StorageException) {
                    throw (StorageException) cause;
                }
                throw new StorageException(cause.getMessage(), cause);
            }

            if (filteredCids.isEmpty()) {
                return new Context[0];
            }

            cids = filteredCids;
        }

        // Slice
        if (withLimit) {
            if (offset >= cids.size()) {
                return new Context[0];
            }

            List<Integer> subset = new ArrayList<>(length);
            int i = 0;
            int numAdded = 0;
            for (Iterator<Integer> iter = cids.iterator(); numAdded < length && iter.hasNext();) {
                Integer cid = iter.next();
                if (i++ >= offset) {
                    subset.add(cid);
                    numAdded++;
                }
            }

            if (subset.isEmpty()) {
                return new Context[0];
            }

            cids = subset;
        }

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getReadConnectionForConfigDB();

            // Grab context data
            Long averageContextFileStoreSize = Long.valueOf(prop.getProp("AVERAGE_CONTEXT_SIZE", "100"));
            List<Context> contexts = new ArrayList<>(cids.size());
            for (List<Integer> partition : Lists.partition(listFor(cids), Databases.IN_LIMIT)) {
                stmt = con.prepareStatement(Databases.getIN("SELECT cid, name, enabled, reason_id, filestore_id, filestore_name, quota_max FROM context WHERE cid IN (", partition.size()));
                int pos = 1;
                for (Integer contextId : partition) {
                    stmt.setInt(pos++, contextId.intValue());
                }
                rs = stmt.executeQuery();
                contexts.addAll(ContextLoadUtility.loadBasicContexts(rs, false, averageContextFileStoreSize, partition.size()));
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;
            }

            // Load login mappings per context and group by database schema association
            TIntObjectMap<Context> id2context = new TIntObjectHashMap<Context>(Databases.IN_LIMIT);
            Map<PoolAndSchema, List<Context>> schema2contexts = ContextLoadUtility.fillLoginMappingsAndDatabases(contexts, id2context, con);

            // Connection to ConfigDB no more needed
            cache.pushReadConnectionForConfigDB(con);
            con = null;

            // Query used quota per schema
            ContextLoadUtility.fillUsageAndAttributes(schema2contexts, true, id2context, cache);
            id2context = null; // Might help GC

            if (null != loaders && false == loaders.isEmpty()) {
                try {
                    for (Filter<Context, Context> loader : loaders) {
                        loader.filter(contexts);
                    }
                } catch (final PipesAndFiltersException e) {
                    final Throwable cause = e.getCause();
                    if (cause instanceof StorageException) {
                        throw (StorageException) cause;
                    }
                    throw new StorageException(cause.getMessage(), cause);
                }
            }

            return contexts.toArray(new Context[contexts.size()]);
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (null != con) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (PoolException e) {
                    LOG.error("Error pushing ox read connection to pool!", e);
                }
            }
        }
    }

    @Override
    public Context[] searchContextByDatabase(final Database db_host, int offset, int length) throws StorageException {
        boolean withLimit = true;
        if (offset < 0 || length < 0) {
            withLimit = false;
        }
        if (withLimit && length < 0) {
            throw new StorageException("Invalid length: " + length);
        }
        if (withLimit && (offset + length) < 0) {
            throw new StorageException("Invalid offset/length: " + offset + ", " + length);
        }
        if (length == 0) {
            return new Context[0];
        }

        int poolId = db_host.getId().intValue();

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getReadConnectionForConfigDB();

            // Get the identifier of the read-write pool for given database pool identifier
            stmt = con.prepareStatement("SELECT write_db_pool_id FROM db_cluster WHERE read_db_pool_id=? OR write_db_pool_id=?");
            stmt.setInt(1, poolId);
            stmt.setInt(2, poolId);
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                // No such database known
                return new Context[0];
            }

            // Load context identifiers by pool identifier
            int writePoolId = rs.getInt(1);
            closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            if (withLimit) {
                stmt = con.prepareStatement("SELECT cid FROM context_server2db_pool WHERE write_db_pool_id=? ORDER BY cid LIMIT " + offset + ", " + length);
            } else {
                stmt = con.prepareStatement("SELECT cid FROM context_server2db_pool WHERE write_db_pool_id=?");
            }
            stmt.setInt(1, writePoolId);
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return new Context[0];
            }

            List<Integer> tmp = length > 0 ? new ArrayList<>(length) : new LinkedList<>();
            do {
                tmp.add(Integer.valueOf(rs.getInt(1)));
            } while (rs.next());
            closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            // Grab context data
            Long averageContextFileStoreSize = Long.valueOf(prop.getProp("AVERAGE_CONTEXT_SIZE", "100"));
            List<Context> contexts = new ArrayList<>(tmp.size());
            for (List<Integer> partition : Lists.partition(tmp, Databases.IN_LIMIT)) {
                stmt = con.prepareStatement(Databases.getIN("SELECT cid, name, enabled, reason_id, filestore_id, filestore_name, quota_max FROM context WHERE cid IN (", partition.size()));
                int pos = 1;
                for (Integer contextId : partition) {
                    stmt.setInt(pos++, contextId.intValue());
                }
                rs = stmt.executeQuery();
                contexts.addAll(ContextLoadUtility.loadBasicContexts(rs, false, averageContextFileStoreSize, partition.size()));
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;
            }

            // Check if empty
            if (contexts.isEmpty()) {
                return new Context[0];
            }

            // Load login mappings per context and group by database schema association
            TIntObjectMap<Context> id2context = new TIntObjectHashMap<Context>(Databases.IN_LIMIT);
            Map<PoolAndSchema, List<Context>> schema2contexts = ContextLoadUtility.fillLoginMappingsAndDatabases(contexts, id2context, con);

            // Connection to ConfigDB no more needed
            cache.pushReadConnectionForConfigDB(con);
            con = null;

            // Query used quota per schema
            ContextLoadUtility.fillUsageAndAttributes(schema2contexts, false, id2context, cache);
            id2context = null; // Might help GC

            return contexts.toArray(new Context[contexts.size()]);
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (null != con) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (PoolException e) {
                    LOG.error("Error pushing ox read connection to pool!", e);
                }
            }
        }
    }

    @Override
    public List<Integer> getContextIdsBySchema(final String schema) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getReadConnectionForConfigDB();
            stmt = con.prepareStatement("SELECT cid FROM context_server2db_pool WHERE db_schema = ?");
            stmt.setString(1, schema);
            rs = stmt.executeQuery();
            List<Integer> contextIds = new LinkedList<Integer>();
            while (rs.next()) {
                contextIds.add(rs.getInt(1));
            }
            return contextIds;
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (con != null) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (PoolException e) {
                    LOG.error("Error pushing configdb connection to pool!", e);
                }
            }
        }
    }

    @Override
    public Context[] searchContextByFilestore(Filestore filestore, int offset, int length) throws StorageException {
        boolean withLimit = true;
        if (offset < 0 || length < 0) {
            withLimit = false;
        }
        if (withLimit && length < 0) {
            throw new StorageException("Invalid length: " + length);
        }
        if (withLimit && (offset + length) < 0) {
            throw new StorageException("Invalid offset/length: " + offset + ", " + length);
        }
        if (length == 0) {
            return new Context[0];
        }

        int filestoreId = filestore.getId().intValue();

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getReadConnectionForConfigDB();
            if (withLimit) {
                stmt = con.prepareStatement("SELECT cid, name, enabled, reason_id, filestore_id, filestore_name, quota_max FROM context WHERE filestore_id=? ORDER BY cid LIMIT " + offset + ", " + length);
            } else {
                stmt = con.prepareStatement("SELECT cid, name, enabled, reason_id, filestore_id, filestore_name, quota_max FROM context WHERE filestore_id=?");
            }
            stmt.setInt(1, filestoreId);
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return new Context[0];
            }

            Long averageContextFileStoreSize = Long.valueOf(prop.getProp("AVERAGE_CONTEXT_SIZE", "100"));
            List<Context> contexts = ContextLoadUtility.loadBasicContexts(rs, true, averageContextFileStoreSize, length);
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            // Check if empty
            if (contexts.isEmpty()) {
                return new Context[0];
            }

            // Load login mappings per context and group by database schema association
            TIntObjectMap<Context> id2context = new TIntObjectHashMap<Context>(Databases.IN_LIMIT);
            Map<PoolAndSchema, List<Context>> schema2contexts = ContextLoadUtility.fillLoginMappingsAndDatabases(contexts, id2context, con);

            // Connection to ConfigDB no more needed
            cache.pushReadConnectionForConfigDB(con);
            con = null;

            // Query used quota per schema
            ContextLoadUtility.fillUsageAndAttributes(schema2contexts, false, id2context, cache);
            id2context = null; // Might help GC

            return contexts.toArray(new Context[contexts.size()]);
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (null != con) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (PoolException e) {
                    LOG.error("Error pushing ox read connection to pool!", e);
                }
            }
        }
    }

    @Override
    @Deprecated
    public void changeStorageData(final Context ctx) throws StorageException {
        OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
        oxcox.changeFilestoreDataFor(ctx);
    }

    @Override
    public Context create(final Context ctx, final User adminUser, final UserModuleAccess access, SchemaSelectStrategy schemaSelectStrategy) throws StorageException, InvalidDataException, ContextExistsException {
        if (null == adminUser) {
            throw new StorageException("Context administrator is not defined.");
        }

        // The effective strategy
        SchemaSelectStrategy effectiveStrategy = null == schemaSelectStrategy ? SchemaSelectStrategy.getDefault() : schemaSelectStrategy;

        Database db = null;
        boolean decrementFileStoreCount = false;
        boolean decrementDatabaseCount = false;
        boolean decrementDatabaseSchemaCount = false;
        OXUtilStorageInterface utils = OXUtilStorageInterface.getInstance();

        // Initiate connection to ConfigDB
        Connection configCon;
        try {
            configCon = cache.getWriteConnectionForConfigDB();
        } catch (PoolException e) {
            throw new StorageException(e.getMessage(), e);
        }
        try {
            // Find filestore for context.
            ctx.setFilestore_name(FileStorages.getNameForContext(ctx.getId().intValue()));
            Filestore filestore;
            {
                Integer storeId = ctx.getFilestoreId();
                if (null == storeId) {
                    // No filestore specified
                    filestore = utils.findFilestoreForContext(configCon);
                    ctx.setFilestoreId(filestore.getId());
                } else {
                    filestore = utils.getFilestoreBasic(i(storeId));
                    contextCommon.updateContextsPerFilestoreCount(true, ctx);
                }
                decrementFileStoreCount = true;

                // Load it to ensure validity
                try {
                    URI uri = FileStorages.getFullyQualifyingUriForContext(ctx.getId().intValue(), new URI(filestore.getUrl()));
                    FileStorages.getFileStorageService().getFileStorage(uri);
                } catch (OXException e) {
                    throw new StorageException(e.getMessage(), e);
                } catch (URISyntaxException e) {
                    throw new StorageException("Filestore " + filestore.getId() + " contains invalid URI", e);
                }
            }

            // Find database for context
            boolean updateContextsPerDBSchemaCount = true;
            {
                Database givenDatabase = ctx.getWriteDatabase();
                if (null == givenDatabase) {
                    // No database specified
                    db = utils.getNextDBHandleByWeight(configCon, true);
                    // Resolved with respect to schema?
                    String preferredSchema = db.getScheme();
                    if (null != preferredSchema) {
                        effectiveStrategy = SchemaSelectStrategy.schema(preferredSchema);
                        updateContextsPerDBSchemaCount = false;
                    }
                } else {
                    db = OXToolStorageInterface.getInstance().loadDatabaseById(i(givenDatabase.getId()));
                    if (db.getMaxUnits().intValue() <= 0) {
                        // Must not be used for a context association
                        throw new StorageException("Database " + givenDatabase.getId() + " must not be used.");
                    }
                    contextCommon.updateContextsPerDBPoolCount(!decrementDatabaseCount, db, configCon);
                }
                decrementDatabaseCount = true;
            }

            // Determine the schema name according to effective strategy
            switch (effectiveStrategy.getStrategy()) {
                case SCHEMA: {
                    // Pre-defined schema name
                    applyPredefinedSchemaName(effectiveStrategy.getSchema(), db);
                    if (updateContextsPerDBSchemaCount) {
                        contextCommon.updateContextsPerDBSchemaCount(true, db.getScheme(), db, configCon);
                    }
                    decrementDatabaseSchemaCount = true;
                    break;
                }
                case AUTOMATIC:
                    // fall-through
                default: {
                    // Find or create suitable schema (within transaction)
                    startTransaction(configCon);
                    boolean rollback = true;
                    try {
                        autoFindOrCreateSchema(configCon, db);
                        contextCommon.updateContextsPerDBSchemaCount(true, db.getScheme(), db, configCon);
                        configCon.commit();
                        rollback = false;
                        decrementDatabaseSchemaCount = true;
                    } finally {
                        if (rollback) {
                            rollback(configCon);
                        }
                        autocommit(configCon);
                    }
                    break;
                }
            }

            LOG.debug("Using schema \"{}\" from database {} for creation of context {}", db.getScheme(), db.getId(), ctx.getId());

            // Create context (within transaction)
            Context context;
            {
                startTransaction(configCon);
                boolean rollback = true;
                try {
                    contextCommon.fillContextAndServer2DBPool(ctx, configCon, db);
                    contextCommon.fillLogin2ContextTable(ctx, configCon);

                    context = writeContext(ctx, adminUser, access);

                    configCon.commit();
                    rollback = false;
                } finally {
                    if (rollback) {
                        rollback(configCon);
                    }
                    autocommit(configCon);
                }
            }

            // Everything successful
            decrementDatabaseSchemaCount = false;
            decrementDatabaseCount = false;
            decrementFileStoreCount = false;
            return context;
        } catch (SQLException e) {
            throw new StorageException(e.getMessage(), e);
        } finally {
            if (decrementDatabaseSchemaCount) {
                if (null != db) {
                    try {
                        contextCommon.updateContextsPerDBSchemaCount(false, db.getScheme(), db, configCon);
                    } catch (Exception e) {
                        LOG.error("Failed to decrement contexts-per-dbschema count", e);
                    }
                }
            }
            if (decrementDatabaseCount) {
                try {
                    contextCommon.updateContextsPerDBPoolCount(false, db, configCon);
                } catch (Exception e) {
                    LOG.error("Failed to decrement contexts-per-dbpool count", e);
                }
            }
            if (decrementFileStoreCount) {
                try {
                    contextCommon.updateContextsPerFilestoreCount(false, ctx, configCon);
                } catch (Exception e) {
                    LOG.error("Failed to decrement contexts-per-filestore count", e);
                }
            }

            try {
                cache.pushWriteConnectionForConfigDB(configCon);
            } catch (PoolException e) {
                LOG.error("Error pushing ox write connection to pool!", e);
            }
        }
    }

    /**
     * Writes the context data into context-associated payload (non-configDb) database.
     * <ul>
     * <li>Add context entry into (ID) sequence tables</li>
     * <li>Add context entry into <code>replicationMonitor</code> table</li>
     * <li>Add context entry into <code>filestore_usage</code> table</li>
     * <li>Add context attributes to <code>contextAttribute</code> table</li>
     * <li>Create context administrator user</li>
     * <li>...</li>
     * </ul>
     *
     * @param ctx The new context to add to payload database
     * @param adminUser The context administrator to create
     * @param access The access permissions to apply to context administrator user
     * @return The specified <code>ctx</code> argument with new attributes applied
     * @throws StorageException If writing context data into payload database fails
     */
    private Context writeContext(final Context ctx, final User adminUser, final UserModuleAccess access) throws StorageException {
        final int contextId = ctx.getId().intValue();
        try {
            DBUtils.TransactionRollbackCondition condition = new DBUtils.TransactionRollbackCondition(5);
            do {
                Connection oxCon = null;
                try {
                    oxCon = cache.getConnectionForContext(contextId);
                } catch (PoolException e) {
                    LOG.error("Pool Error", e);
                    throw new StorageException(e);
                }

                condition.resetTransactionRollbackException();
                boolean rollback = false;
                try {
                    Databases.startTransaction(oxCon);
                    rollback = true;

                    if (lockOnWriteContextToPayloadDb) {
                        lockWriteContextToPayloadDb(contextId, oxCon);
                    }

                    contextCommon.initSequenceTables(contextId, oxCon);
                    contextCommon.initReplicationMonitor(oxCon, contextId);
                    contextCommon.initFilestoreUsage(oxCon, contextId);

                    updateDynamicAttributes(oxCon, ctx);

                    final int groupId = IDGenerator.getId(contextId, com.openexchange.groupware.Types.PRINCIPAL, oxCon);
                    final int adminId = IDGenerator.getId(contextId, com.openexchange.groupware.Types.PRINCIPAL, oxCon);
                    final int contactId = IDGenerator.getId(contextId, com.openexchange.groupware.Types.CONTACT, oxCon);
                    int uidNumber = -1;
                    if (Integer.parseInt(prop.getUserProp(AdminProperties.User.UID_NUMBER_START, "-1")) > 0) {
                        uidNumber = IDGenerator.getId(contextId, com.openexchange.groupware.Types.UID_NUMBER, oxCon);
                    }
                    int gidNumber = -1;
                    if (Integer.parseInt(prop.getGroupProp(AdminProperties.Group.GID_NUMBER_START, "-1")) > 0) {
                        gidNumber = IDGenerator.getId(contextId, com.openexchange.groupware.Types.GID_NUMBER, oxCon);
                    }

                    // create group users for context
                    final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
                    adminUser.setContextadmin(true);
                    tool.checkCreateUserData(ctx, adminUser);
                    final String groupName = translateGroupName(adminUser);
                    contextCommon.createStandardGroupForContext(contextId, oxCon, groupName, groupId, gidNumber);
                    final OXUserStorageInterface oxs = OXUserStorageInterface.getInstance();
                    oxs.create(ctx, adminUser, access, oxCon, adminId, contactId, uidNumber);

                    // create system folder for context
                    // get lang and displayname of admin
                    String display = String.valueOf(adminUser.getId());
                    final String displayName = adminUser.getDisplay_name();
                    if (null != displayName) {
                        display = displayName;
                    } else {
                        final String givenName = adminUser.getGiven_name();
                        final String surname = adminUser.getSur_name();
                        if (null != givenName) {
                            // SET THE DISPLAYNAME AS NEEDED BY CUSTOMER, SHOULD BE
                            // DEFINED ON SERVER SIDE
                            display = givenName + " " + surname;
                        } else {
                            display = surname;
                        }
                        adminUser.setDisplay_name(display);
                    }
                    final OXFolderAdminHelper oxa = new OXFolderAdminHelper();
                    oxa.addContextSystemFolders(contextId, display, adminUser.getLanguage(), oxCon);

                    oxCon.commit();
                    rollback = false;

                    ctx.setEnabled(Boolean.TRUE);
                    adminUser.setId(I(adminId));
                    return ctx;
                } catch (final OXException e) {
                    SQLException sqle = DBUtils.extractSqlException(e);
                    if (!condition.isFailedTransactionRollback(sqle)) {
                        LOG.error("Error", e);
                        throw new StorageException(e.toString());
                    }
                } catch (final StorageException e) {
                    SQLException sqle = DBUtils.extractSqlException(e);
                    if (!condition.isFailedTransactionRollback(sqle)) {
                        LOG.error("Storage Error", e);
                        throw e;
                    }
                } catch (final DataTruncation e) {
                    LOG.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, e);
                    throw AdminCache.parseDataTruncation(e);
                } catch (final SQLException e) {
                    if (!condition.isFailedTransactionRollback(e)) {
                        LOG.error("SQL Error", e);
                        throw new StorageException(e);
                    }
                } catch (final InvalidDataException e) {
                    LOG.error("InvalidData Error", e);
                    throw new StorageException(e);
                } catch (final EnforceableDataObjectException e) {
                    LOG.error("Enforceable DataObject Error", e);
                    throw new StorageException(e);
                } catch (final RuntimeException e) {
                    LOG.error("Internal Error", e);
                    throw new StorageException("Internal server error occured", e);
                } finally {
                    if (rollback) {
                        rollback(oxCon);
                    }
                    autocommit(oxCon);
                    if (null != oxCon) {
                        try {
                            cache.pushConnectionForContext(contextId, oxCon);
                        } catch (final PoolException ecp) {
                            LOG.error("Error pushing ox write connection to pool!", ecp);
                        }
                    }
                }
            } while (retryWriteContextToPayloadDb(condition, ctx));
        } catch (StorageException e) {
            throw e;
        }

        return ctx;
    }

    private boolean retryWriteContextToPayloadDb(DBUtils.TransactionRollbackCondition condition, Context ctx) throws StorageException {
        try {
            SQLException sqle = condition.getTransactionRollbackException();
            boolean retry = condition.checkRetry();
            if (retry) {
                int numRetries = condition.getCount();
                long nanosToWait = TimeUnit.NANOSECONDS.convert((numRetries * 1000) + ((long) (Math.random() * 1000)), TimeUnit.MILLISECONDS);
                LockSupport.parkNanos(nanosToWait);
                LOG.info("Retrying to write data from context {} into payload database suggested by: {}", ctx.getId(), sqle.getMessage());
            }
            return retry;
        } catch (SQLException e) {
            // Max. retry count exceeded.
            throw new StorageException("Repetitively failed to write context into payload database", e);
        }
    }

    private void lockWriteContextToPayloadDb(int contextId, Connection con) throws SQLException {
        if (null == con) {
            return;
        }
        PreparedStatement stmt = null;
        try {
            if (con.getAutoCommit()) {
                throw new SQLException("Connection is not in transaction state.");
            }
            stmt = con.prepareStatement("SELECT COUNT(*) FROM contextAttribute FOR UPDATE");
            stmt.executeQuery();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void updateDynamicAttributes(final Connection oxCon, final Context ctx) throws SQLException {
        if (!ctx.isUserAttributesset()) {
            return;
        }

        PreparedStatement insertStmt = null;
        PreparedStatement deleteStmt = null;
        Set<String> changedConfigAttributes = new HashSet<>();
        try {
            int contextId = ctx.getId().intValue();

            for (Map.Entry<String, Map<String, String>> ns : ctx.getUserAttributes().entrySet()) {
                String namespace = ns.getKey();
                for (Map.Entry<String, String> pair : ns.getValue().entrySet()) {
                    String name = namespace + "/" + pair.getKey();
                    String value = pair.getValue();
                    if (value == null) {
                        if (null == deleteStmt) {
                            deleteStmt = oxCon.prepareStatement("DELETE FROM contextAttribute WHERE cid=? AND name=?");
                            deleteStmt.setInt(1, contextId);
                        }
                        deleteStmt.setString(2, name);
                        deleteStmt.addBatch();
                    } else {
                        if (null == insertStmt) {
                            insertStmt = oxCon.prepareStatement("INSERT INTO contextAttribute (value, cid, name) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE value=?");
                            insertStmt.setInt(2, contextId);
                        }
                        insertStmt.setString(1, value);
                        insertStmt.setString(3, name);
                        insertStmt.setString(4, value);
                        insertStmt.addBatch();
                    }
                    if ("config".equals(namespace)) {
                        changedConfigAttributes.add(name);
                    }
                }
            }

            if (null != deleteStmt) {
                deleteStmt.executeBatch();
                Databases.closeSQLStuff(deleteStmt);
                deleteStmt = null;
            }
            if (null != insertStmt) {
                insertStmt.executeBatch();
                Databases.closeSQLStuff(insertStmt);
                insertStmt = null;
            }

            if (false == changedConfigAttributes.isEmpty()) {
                Reloadables.propagatePropertyChange(changedConfigAttributes);
            }

            {
                // Invalidate caches
                CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
                if (null != cacheService) {
                    try {
                        Cache lCache = cacheService.getCache("UserSettingMail");
                        for (int userId : OXUserStorageInterface.getInstance().getAll(ctx, oxCon)) {
                            lCache.remove(cacheService.newCacheKey(contextId, userId));
                        }
                    } catch (final Exception e) {
                        LOG.error("", e);
                    }
                    try {
                        final Cache jcs = cacheService.getCache("Capabilities");
                        jcs.invalidateGroup(ctx.getId().toString());
                    } catch (final Exception e) {
                        LOG.error("", e);
                    }
                }
            }
        } finally {
            Databases.closeSQLStuff(insertStmt, deleteStmt);
        }
    }

    /**
     * Translate display name for context default group resolved via administrators language.
     *
     * @param administrator administrator user of the context.
     * @return the translated group name if a corresponding service is available.
     */
    private String translateGroupName(final User administrator) {
        final Locale locale = LocaleTools.getLocale(administrator.getLanguage());
        return I18nServices.getInstance().translate(locale, Groups.STANDARD_GROUP);
    }

    /*
     * ============================================================================ Private part
     * ============================================================================
     */
    private Database getDatabaseHandleById(final Database database_id, final Connection configdb_write) throws SQLException, StorageException {
        ResultSet rs = null;
        PreparedStatement pstm = null;
        try {
            pstm = configdb_write.prepareStatement("SELECT url,driver,login,password,name FROM db_pool WHERE db_pool_id = ?");
            pstm.setInt(1, database_id.getId().intValue());
            rs = pstm.executeQuery();

            if (false == rs.next()) {
                throw new StorageException("No such database with identifier " + database_id.getId());
            }

            final Database retval = new Database();
            retval.setId(database_id.getId());
            retval.setLogin(rs.getString("login"));
            retval.setPassword(rs.getString("password"));
            retval.setDriver(rs.getString("driver"));
            retval.setUrl(rs.getString("url"));
            retval.setName(rs.getString("name"));
            return retval;
        } finally {
            Databases.closeSQLStuff(rs, pstm);
        }
    }

    private void applyPredefinedSchemaName(String schemaName, Database db) {
        // Pre-defined schema name
        db.setScheme(schemaName);
    }

    /**
     * Looks-up the next suitable schema or creates one.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">Acquires a lock for specified database/pool.</div>
     * <p>
     *
     * @param configCon The connection to configDb
     * @param db The database to get the schema for
     * @param forceCreate <code>true</code> to enforce schema creation <b>w/o</b> checking if an unfilled one is available; otherwise <code>false</code> to check prior to creation
     * @throws StorageException If a suitable schema cannot be found
     */
    private void autoFindOrCreateSchema(Connection configCon, Database db) throws StorageException {
        // Freshly determine the next schema to use
        String schemaName = getNextUnfilledSchemaFromDB(db.getId(), configCon);
        if (schemaName != null) {
            // Found a suitable schema on specified database host
            db.setScheme(schemaName);
            return;
        }

        // Need to create a new schema
        int schemaUnique;
        try {
            schemaUnique = IDGenerator.getId(configCon);
        } catch (SQLException e) {
            throw new StorageException(e.getMessage(), e);
        }
        schemaName = db.getName() + '_' + schemaUnique;
        db.setScheme(schemaName);
        OXUtilStorageInterface.getInstance().createDatabase(db, configCon);
    }

    private void createDatabaseAndMappingForContext(Database db, Connection con, int contextId) throws StorageException {
        autoFindOrCreateSchema(con, db);
        try {
            updateContextServer2DbPool(db, con, contextId);
        } catch (PoolException e) {
            throw new StorageException(e.getMessage(), e);
        }
    }

    private static void updateContextServer2DbPool(final Database db, Connection con, final int contextId) throws PoolException {
        final int serverId = cache.getServerId();
        cache.getPool().writeAssignment(con, new Assignment() {

            @Override
            public int getWritePoolId() {
                return i(db.getId());
            }

            @Override
            public int getServerId() {
                return serverId;
            }

            @Override
            public String getSchema() {
                return db.getScheme();
            }

            @Override
            public int getReadPoolId() {
                Integer readId = db.getRead_id();
                if (null == readId) {
                    // Hints to a pool w/o a slave; return write-pool identifier instead
                    return getWritePoolId();
                }
                return i(readId);
            }

            @Override
            public int getContextId() {
                return contextId;
            }
        });
    }

    private String getNextUnfilledSchemaFromDB(final Integer poolId, final Connection con) throws StorageException {
        if (null == poolId) {
            throw new StorageException("pool_id in getNextUnfilledSchemaFromDB must be != null");
        }
        OXAdminPoolInterface pool = cache.getPool();
        final String[] unfilledSchemas;
        try {
            pool.lock(con, i(poolId));
            unfilledSchemas = pool.getUnfilledSchemas(con, i(poolId), this.maxNumberOfContextsPerSchema);
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }
        final OXToolStorageInterface oxt = OXToolStorageInterface.getInstance();
        String found = null;
        for (String schema : unfilledSchemas) {
            if (oxt.schemaBeingLockedOrNeedsUpdate(i(poolId), schema)) {
                LOG.debug("schema {} is locked or updated, trying next one", schema);
            } else {
                found = schema;
                break;
            }
        }
        LOG.debug("using schema {} it for next context", found);
        return found;
    }

    private void fillTargetDatabase(List<TableObject> sorted_tables, Connection target_ox_db_con, Connection ox_db_connection, Object criteriaMatch) throws SQLException {
        // do the inserts for all tables!
        StringBuilder prep_sql = new StringBuilder();
        StringBuilder sb_values = new StringBuilder();
        for (TableObject to : sorted_tables) {
            to = getDataForTable(to, ox_db_connection, criteriaMatch);
            if (to.getDataRowCount() > 0) {
                // ok data in table found, copy to db
                for (int i = 0; i < to.getDataRowCount(); i++) {
                    prep_sql.setLength(0);
                    sb_values.setLength(0);

                    prep_sql.append("INSERT INTO " + to.getName() + " ");
                    prep_sql.append("(");
                    sb_values.append("(");

                    final TableRowObject tro = to.getDataRow(i);
                    Enumeration<String> enumi = tro.getColumnNames();

                    // Save the order of the columns in this list, that all
                    // values are correct mapped to their fields
                    // for later use in prepared_statement
                    final List<String> columns_list = new ArrayList<String>();

                    while (enumi.hasMoreElements()) {
                        final String column = enumi.nextElement();
                        columns_list.add(column);
                        prep_sql.append("" + column + ",");
                        sb_values.append("?,");
                    }

                    // set up the sql query for the prep statement
                    prep_sql.deleteCharAt(prep_sql.length() - 1);
                    sb_values.deleteCharAt(sb_values.length() - 1);
                    prep_sql.append(") ");
                    sb_values.append(") ");
                    prep_sql.append(" VALUES ");
                    prep_sql.append(sb_values.toString());

                    // now create the statements for each row
                    PreparedStatement prep_ins = null;
                    try {
                        prep_ins = target_ox_db_con.prepareStatement(prep_sql.toString());
                        enumi = tro.getColumnNames();
                        int ins_pos = 1;
                        for (int c = 0; c < columns_list.size(); c++) {
                            final TableColumnObject tco = tro.getColumn(columns_list.get(c));
                            prep_ins.setObject(ins_pos, tco.getData(), tco.getType());
                            ins_pos++;
                        }

                        prep_ins.executeUpdate();
                        prep_ins.close();
                    } finally {
                        try {
                            if (prep_ins != null) {
                                prep_ins.close();
                            }
                        } catch (final Exception e) {
                            LOG.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
                        }
                    }
                    // }// end of test table
                }// end of datarow loop

            }// end of if table has data
            to = null;
        }// end of table loop
    }

    // Deduced from https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-reference-type-conversions.html
    private static final Map<String, Integer> MYSQL_TYPES = ImmutableMap.<String, Integer> builder().put("BIT", Types.BIT).put("TINYINT", Types.TINYINT).put("BOOL", Types.BOOLEAN).put("BOOLEAN", Types.BOOLEAN).put("SMALLINT", Types.SMALLINT).put("MEDIUMINT", Types.INTEGER).put("INT", Types.INTEGER).put("INTEGERR", Types.INTEGER).put("BIGINT", Types.BIGINT).put("FLOAT", Types.FLOAT).put("DOUBLE", Types.DOUBLE).put("DECIMAL", Types.DECIMAL).put("DATE", Types.DATE).put("DATETIME", Types.TIMESTAMP).put("TIMESTAMP", Types.TIMESTAMP).put("TIME", Types.TIME).put("YEAR", Types.DATE).put("CHAR", Types.CHAR).put("VARCHAR", Types.VARCHAR).put("BINARY", Types.BINARY).put("VARBINARY", Types.VARBINARY).put("TINYBLOB", Types.BLOB).put("TINYTEXT", Types.VARCHAR).put("BLOB", Types.BLOB).put("TEXT", Types.VARCHAR).put("MEDIUMBLOB", Types.BLOB).put("MEDIUMTEXT", Types.VARCHAR).put("LONGBLOB", Types.BLOB).put("LONGTEXT", Types.VARCHAR).put("ENUM", Types.CHAR).put("SET", Types.CHAR).build();

    private static List<TableObject> fetchTableObjects(String selectionCriteria, Connection ox_db_write_connection) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = ox_db_write_connection.prepareStatement("SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=?");
            stmt.setString(1, ox_db_write_connection.getCatalog());
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                // No tables
                return new ArrayList<>(0);
            }

            List<TableObject> tableObjects = new LinkedList<TableObject>();
            TableObject to = null;
            do {
                String tableName = rs.getString(1);
                if (null == to || !to.getName().equals(tableName)) {
                    to = new TableObject();
                    to.setName(tableName);
                }

                TableColumnObject tco = new TableColumnObject();
                String columnName = rs.getString(2);
                tco.setName(columnName); // COLUMN_NAME
                Integer type = MYSQL_TYPES.get(Strings.toUpperCase(rs.getString(3)));
                tco.setType(null == type ? Types.VARCHAR : type.intValue());
                tco.setColumnSize((int) rs.getLong(4));

                // if table has our criteria column, we should fetch data from it
                if (columnName.equals(selectionCriteria)) {
                    tableObjects.add(to);
                }
                // add column to table
                to.addColumn(tco);
            } while (rs.next());
            LOG.debug("####### Found -> {} tables", tableObjects.size());
            return tableObjects;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private static List<TableObject> sortTableObjects(List<TableObject> fetchTableObjects, Connection ox_db_write_con) throws SQLException {
        findReferences(fetchTableObjects, ox_db_write_con);
        // thx http://de.wikipedia.org/wiki/Topologische_Sortierung :)
        return sortTablesByForeignKey(fetchTableObjects);
    }

    private static List<TableObject> sortTablesByForeignKey(List<TableObject> fetchTableObjects) {
        List<TableObject> nastyOrder = new ArrayList<TableObject>(fetchTableObjects.size());

        List<TableObject> unsorted = new ArrayList<TableObject>(fetchTableObjects);

        // now sort the table with a topological sort mech :)
        // work with the unsorted vector
        while (unsorted.size() > 0) {
            for (int a = 0; a < unsorted.size(); a++) {
                final TableObject to = unsorted.get(a);
                if (!to.hasCrossReferences()) {
                    // log.error("removing {}", to.getName());
                    nastyOrder.add(to);
                    // remove object from list and sort the references new
                    removeAndSortNew(unsorted, to);
                    a--;
                }
            }
        }
        // printTables(nasty_order);
        return nastyOrder;
    }

    /**
     * Finds references for each table
     */
    private static void findReferences(List<TableObject> fetchTableObjects, Connection ox_db_write_con) throws SQLException {
        DatabaseMetaData dbmeta = ox_db_write_con.getMetaData();
        String dbCatalog = ox_db_write_con.getCatalog();

        for (TableObject to : fetchTableObjects) {
            // get references from this table to another
            String tableName = to.getName();
            // ResultSet table_references =
            // dbmetadata.getCrossReference("%",null,table_name,getCatalogName(),null,getCatalogName());

            ResultSet tableReferences = dbmeta.getImportedKeys(dbCatalog, null, tableName);
            try {
                LOG.debug("Table {} has pk reference to table-column:", tableName);
                while (tableReferences.next()) {
                    final String pk = tableReferences.getString("PKTABLE_NAME");
                    final String pkc = tableReferences.getString("PKCOLUMN_NAME");
                    LOG.debug("--> Table: {} column ->{}", pk, pkc);
                    to.addCrossReferenceTable(pk);
                    final int pos = tableListContainsObject(pk, fetchTableObjects);
                    if (pos != -1) {
                        LOG.debug("Found referenced by {}<->{}->{}", tableName, pk, pkc);
                        final TableObject editMe = fetchTableObjects.get(pos);
                        editMe.addReferencedBy(tableName);
                    }
                }
            } finally {
                closeSQLStuff(tableReferences);
            }
        }
    }

    /**
     * remove no more needed element from list and remove the reference to removed element so that a new element exists which has now
     * references.
     */
    private static void removeAndSortNew(List<TableObject> unsorted, TableObject to) {
        unsorted.remove(to);
        for (int i = 0; i < unsorted.size(); i++) {
            TableObject tob = unsorted.get(i);
            tob.removeCrossReferenceTable(to.getName());
        }
    }

    /**
     * Returns -1 if not found else the position in the Vector where the object is located.
     */
    private static int tableListContainsObject(String table_name, List<TableObject> fetchTableObjects) {
        int size = fetchTableObjects.size();
        int pos = -1;
        for (int v = 0; v < size; v++) {
            TableObject to = fetchTableObjects.get(v);
            if (to.getName().equals(table_name)) {
                pos = v;
            }
        }
        return pos;
    }

    private TableObject getDataForTable(final TableObject to, final Connection ox_db_connection, final Object criteriaMatch) throws SQLException {
        final Vector<TableColumnObject> column_objects = to.getColumns();
        // build the statement string
        final StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        for (int a = 0; a < column_objects.size(); a++) {
            final TableColumnObject tco = column_objects.get(a);
            sb.append("`" + tco.getName() + "`,");
        }
        sb.delete(sb.length() - 1, sb.length());
        sb.append(" FROM " + to.getName() + " WHERE " + this.selectionCriteria + " = ?");

        // fetch data from table
        PreparedStatement prep = null;
        try {
            prep = ox_db_connection.prepareStatement(sb.toString());
            prep.setObject(1, criteriaMatch, this.criteriaType);
            LOG.debug("######## {}", sb);
            final ResultSet rs = prep.executeQuery();
            while (rs.next()) {
                final TableRowObject tro = new TableRowObject();
                for (int b = 0; b < column_objects.size(); b++) {
                    final TableColumnObject tco = column_objects.get(b);
                    final Object o = rs.getObject(tco.getName());

                    final TableColumnObject tc2 = new TableColumnObject();
                    tc2.setColumnSize(tco.getColumnSize());
                    tc2.setData(o);
                    tc2.setName(tco.getName());
                    tc2.setType(tco.getType());

                    tro.setColumn(tc2);
                }
                to.setDataRow(tro);
            }
            rs.close();
            prep.close();
        } finally {
            try {
                if (prep != null) {
                    prep.close();
                }
            } catch (final Exception e) {
                LOG.error("Error closing statement", e);
            }
        }

        return to;
    }

    private void myLockUnlockAllContexts(final boolean lock_all, final int reason_id, final String additionaltable, final String sqlconjunction) throws SQLException, PoolException {
        Connection con_write = null;
        PreparedStatement stmt = null;
        boolean rollback = false;
        try {
            con_write = cache.getWriteConnectionForConfigDB();
            con_write.setAutoCommit(false);
            rollback = true;

            if (reason_id != -1) {
                stmt = con_write.prepareStatement("UPDATE context " + (additionaltable != null ? "," + additionaltable : "") + " SET enabled = ?, reason_id = ? " + (sqlconjunction != null ? sqlconjunction : ""));
                stmt.setBoolean(1, lock_all);
                stmt.setInt(2, reason_id);
            } else {
                stmt = con_write.prepareStatement("UPDATE context SET enabled = ?");
                stmt.setBoolean(1, lock_all);
            }
            stmt.executeUpdate();
            stmt.close();
            con_write.commit();
            rollback = false;
        } catch (final SQLException sql) {
            LOG.error("SQL Error", sql);
            throw sql;
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw e;
        } finally {
            closePreparedStatement(stmt);
            if (rollback) {
                rollback(con_write);
            }

            autocommit(con_write);
            if (con_write != null) {
                try {
                    cache.pushWriteConnectionForConfigDB(con_write);
                } catch (final PoolException exp) {
                    LOG.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    private void myEnableDisableContext(final int context_id, final boolean enabled, final int reason_id) throws SQLException, PoolException {
        Connection con_write = null;
        PreparedStatement stmt = null;
        boolean rollback = false;
        try {
            con_write = cache.getWriteConnectionForConfigDB();
            con_write.setAutoCommit(false);
            rollback = true;

            stmt = con_write.prepareStatement("UPDATE context SET enabled = ?, reason_id = ? WHERE cid = ?");

            stmt.setBoolean(1, enabled);
            if (enabled) {
                stmt.setNull(2, java.sql.Types.INTEGER);
            } else {
                if (reason_id != -1) {
                    try {
                        stmt.setLong(2, reason_id);
                    } catch (final SQLException exp) {
                        LOG.error("Invalid reason ID!", exp);
                    }
                } else {
                    stmt.setNull(2, java.sql.Types.INTEGER);
                }
            }
            stmt.setInt(3, context_id);
            stmt.executeUpdate();
            stmt.close();
            con_write.commit();
            rollback = false;
        } catch (final SQLException sql) {
            LOG.error("SQL Error", sql);
            throw sql;
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw e;
        } finally {
            closeSQLStuff(stmt);
            if (rollback) {
                rollback(con_write);
            }

            autocommit(con_write);
            if (con_write != null) {
                try {
                    cache.pushWriteConnectionForConfigDB(con_write);
                } catch (final PoolException exp) {
                    LOG.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    @Override
    public Quota[] listQuotas(Context ctx) throws StorageException {
        int contextId = ctx.getId().intValue();
        Connection con = null;
        try {
            con = cache.getConnectionForContext(contextId);

            String[] moduleIds = AmountQuotas.getQuotaModuleIDs(con, contextId);
            int length = moduleIds.length;
            if (length == 0) {
                return new Quota[0];
            }

            List<Quota> quotaList = new ArrayList<>();
            for (int i = length; i-- > 0;) {
                String module = moduleIds[i];
                Long qlimit = AmountQuotas.getQuotaFromDB(con, contextId, module);
                if (qlimit != null) {
                    quotaList.add(new Quota(qlimit.longValue(), module));
                }
            }

            return quotaList.size() > 0 ? quotaList.toArray(new Quota[quotaList.size()]) : new Quota[0];
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } catch (OXException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            if (null != con) {
                try {
                    cache.pushConnectionForContext(contextId, con);
                } catch (PoolException e) {
                    LOG.error("Error pushing connection to pool for context {}!", contextId, e);
                }
            }
        }
    }

    @Override
    public void changeQuota(final Context ctx, final List<String> modules, final long quota, final Credentials auth) throws StorageException {
        int contextId = ctx.getId().intValue();

        // SQL resources
        Connection con = null;
        boolean rollback = false;
        boolean autocommit = false;
        try {
            con = cache.getConnectionForContext(contextId);
            con.setAutoCommit(false); // BEGIN
            autocommit = true;
            rollback = true;
            AmountQuotas.setLimit(contextId, modules, quota, con);
            con.commit(); // COMMIT
            rollback = false;
        } catch (SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } catch (OXException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            if (rollback) {
                rollback(con);
            }
            if (autocommit) {
                autocommit(con);
            }
            if (null != con) {
                try {
                    cache.pushConnectionForContext(contextId, con);
                } catch (PoolException e) {
                    LOG.error("Error pushing connection to pool for context {}!", contextId, e);
                }
            }
        }
    }

    @Override
    public Set<String> getCapabilities(Context ctx) throws StorageException {
        final int contextId = ctx.getId().intValue();
        // SQL resources
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getConnectionForContext(contextId);

            stmt = con.prepareStatement("SELECT cap FROM capability_context WHERE cid=?");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.<String> emptySet();
            }
            final Set<String> caps = new HashSet<String>(16);
            do {
                caps.add(rs.getString(1));
            } while (rs.next());
            return caps;
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (null != con) {
                try {
                    cache.pushConnectionForContext(contextId, con);
                } catch (final PoolException e) {
                    LOG.error("Error pushing connection to pool for context {}!", contextId, e);
                }
            }
        }
    }

    @Override
    public void changeCapabilities(final Context ctx, final Set<String> capsToAdd, final Set<String> capsToRemove, final Set<String> capsToDrop, final Credentials auth) throws StorageException {
        final int contextId = ctx.getId().intValue();
        // SQL resources
        Connection con = null;
        PreparedStatement stmt = null;
        boolean rollback = false;
        boolean autocommit = false;
        try {
            con = cache.getConnectionForContext(contextId);
            con.setAutoCommit(false); // BEGIN
            autocommit = true;
            rollback = true;
            // First drop
            if (null != capsToDrop && !capsToDrop.isEmpty()) {
                for (final String cap : capsToDrop) {
                    if (null == stmt) {
                        stmt = con.prepareStatement("DELETE FROM capability_context WHERE cid=? AND cap=?");
                        stmt.setInt(1, contextId);
                    }
                    stmt.setString(2, cap);
                    stmt.addBatch();
                    if (cap.startsWith("-")) {
                        stmt.setString(2, cap.substring(1));
                        stmt.addBatch();
                    } else {
                        stmt.setString(2, "-" + cap);
                        stmt.addBatch();
                    }
                }
                if (null != stmt) {
                    stmt.executeBatch();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }
            }
            // Determine what is already present
            final Set<String> existing;
            {
                stmt = con.prepareStatement("SELECT cap FROM capability_context WHERE cid=?");
                stmt.setInt(1, contextId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    existing = new HashSet<String>(16);
                    do {
                        existing.add(rs.getString(1));
                    } while (rs.next());
                } else {
                    existing = Collections.<String> emptySet();
                }
                Databases.closeSQLStuff(rs, stmt);
                stmt = null;
                rs = null;
            }
            final Set<String> capsToInsert = new HashSet<String>(capsToAdd);
            // Delete existing ones
            if (null != capsToRemove && !capsToRemove.isEmpty()) {
                for (final String cap : capsToRemove) {
                    if (existing.contains(cap)) {
                        if (null == stmt) {
                            stmt = con.prepareStatement("DELETE FROM capability_context WHERE cid=? AND cap=?");
                            stmt.setInt(1, contextId);
                        }
                        stmt.setString(2, cap);
                        stmt.addBatch();
                        existing.remove(cap);
                    }
                    final String plusCap = "+" + cap;
                    if (existing.contains(plusCap)) {
                        if (null == stmt) {
                            stmt = con.prepareStatement("DELETE FROM capability_context WHERE cid=? AND cap=?");
                            stmt.setInt(1, contextId);
                        }
                        stmt.setString(2, plusCap);
                        stmt.addBatch();
                        existing.remove(plusCap);
                    }
                    final String minusCap = "-" + cap;
                    if (!existing.contains(minusCap)) {
                        capsToInsert.add(minusCap);
                    }
                }
                if (null != stmt) {
                    stmt.executeBatch();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }
            }
            // Insert new ones
            if (!capsToInsert.isEmpty()) {
                for (final String capToAdd : capsToAdd) {
                    final String minusCap = "-" + capToAdd;
                    if (existing.contains(minusCap)) {
                        if (null == stmt) {
                            stmt = con.prepareStatement("DELETE FROM capability_context WHERE cid=? AND cap=?");
                            stmt.setInt(1, contextId);
                        }
                        stmt.setString(2, minusCap);
                        stmt.addBatch();
                    }
                }
                if (null != stmt) {
                    stmt.executeBatch();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }

                stmt = con.prepareStatement("INSERT INTO capability_context (cid, cap) VALUES (?, ?)");
                stmt.setInt(1, contextId);
                for (final String cap : capsToInsert) {
                    if (cap.startsWith("-")) {
                        // A capability to remove
                        stmt.setString(2, cap);
                        stmt.addBatch();
                    } else {
                        if (!existing.contains(cap) && !existing.contains("+" + cap)) {
                            // A capability to add
                            stmt.setString(2, cap);
                            stmt.addBatch();
                        }
                    }
                }
                stmt.executeBatch();
                Databases.closeSQLStuff(stmt);
                stmt = null;
            }
            con.commit(); // COMMIT
            rollback = false;

            // Invalidate cache
            final CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
            if (null != cacheService) {
                try {
                    final Cache jcs = cacheService.getCache("CapabilitiesContext");
                    final Serializable key = ctx.getId();
                    jcs.remove(key);
                } catch (final Exception e) {
                    LOG.error("", e);
                }
                try {
                    final Cache jcs = cacheService.getCache("Capabilities");
                    jcs.invalidateGroup(ctx.getId().toString());
                } catch (final Exception e) {
                    LOG.error("", e);
                }
            }

        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(stmt);
            if (rollback) {
                rollback(con);
            }
            if (autocommit) {
                autocommit(con);
            }
            if (null != con) {
                try {
                    cache.pushConnectionForContext(contextId, con);
                } catch (final PoolException e) {
                    LOG.error("Error pushing connection to pool for context {}!", contextId, e);
                }
            }
        }
    }

    @Override
    public void change(final Context ctx) throws StorageException {
        {
            Connection configCon = null;
            boolean rollback = false;
            try {
                // Change login mappings in configdb
                {
                    Set<String> loginMappings = ctx.getLoginMappings();
                    if (null != loginMappings) {
                        // Fetch connection
                        configCon = cache.getWriteConnectionForConfigDB();
                        configCon.setAutoCommit(false);
                        rollback = true;
                        changeLoginMappingsForContext(loginMappings, ctx, configCon);
                    }
                }

                // Change context name in configdb
                {
                    // first check if name is set and has a valid value
                    if (ctx.getName() != null) {
                        // Fetch connection (if not done already)
                        if (null == configCon) {
                            configCon = cache.getWriteConnectionForConfigDB();
                            configCon.setAutoCommit(false);
                            rollback = true;
                        }
                        String name = ctx.getName().trim();
                        if (name.length() > 0) {
                            changeNameForContext(name, ctx, configCon);
                        }
                    }
                }

                // Change quota size in config db
                {
                    if (ctx.getMaxQuota() != null) {
                        // Fetch connection (if not done already)
                        if (null == configCon) {
                            configCon = cache.getWriteConnectionForConfigDB();
                            configCon.setAutoCommit(false);
                            rollback = true;
                        }
                        long quota_max_temp = ctx.getMaxQuota().longValue();
                        changeQuotaForContext(quota_max_temp, ctx, configCon);
                    }
                }

                // Change storage data
                {
                    if (ctx.getFilestoreId() != null) {
                        // Fetch connection (if not done already)
                        if (null == configCon) {
                            configCon = cache.getWriteConnectionForConfigDB();
                            configCon.setAutoCommit(false);
                            rollback = true;
                        }
                        changeStorageDataImpl(ctx.getFilestoreId().intValue(), ctx, configCon);
                    }
                }

                // commit changes to db
                if (null != configCon) {
                    configCon.commit();
                    rollback = false;
                }
            } catch (final PoolException e) {
                LOG.error("Pool Error", e);
                throw new StorageException(e);
            } catch (final SQLException e) {
                LOG.error("SQL Error", e);
                throw new StorageException(e);
            } finally {
                if (rollback) {
                    rollback(configCon);
                }
                autocommit(configCon);
                if (null != configCon) {
                    try {
                        cache.pushWriteConnectionForConfigDB(configCon);
                    } catch (final PoolException e) {
                        LOG.error("Error pushing configdb connection to pool!", e);
                    }
                }
            }
        }

        if (ctx.isUserAttributesset()) {
            Connection oxCon = null;
            boolean rollback = false;
            try {
                oxCon = cache.getConnectionForContext(i(ctx.getId()));
                oxCon.setAutoCommit(false);
                rollback = true;

                updateDynamicAttributes(oxCon, ctx);

                oxCon.commit();
                rollback = false;
            } catch (final PoolException e) {
                LOG.error("Pool Error", e);
                throw new StorageException(e);
            } catch (final SQLException e) {
                LOG.error("SQL Error", e);
                throw new StorageException(e);
            } finally {
                if (rollback) {
                    rollback(oxCon);
                }
                autocommit(oxCon);
                if (null != oxCon) {
                    try {
                        cache.pushConnectionForContext(i(ctx.getId()), oxCon);
                    } catch (final PoolException e) {
                        LOG.error("SQL Error", e);
                    }
                }
            }
        }

        LOG.info("Context {} changed.", ctx.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void downgrade(final Context ctx) throws StorageException {
        final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
        final User[] users = oxu.list(ctx, "*");
        final DowngradeRegistry registry = DowngradeRegistry.getInstance();
        final UserConfigurationStorage uConfStorage = UserConfigurationStorage.getInstance();
        final ContextStorage cStor = ContextStorage.getInstance();
        final Connection con;
        final com.openexchange.groupware.contexts.Context gCtx;
        try {
            gCtx = cStor.getContext(ctx.getId().intValue());
            con = cache.getConnectionForContext(ctx.getId().intValue());
        } catch (final OXException e) {
            LOG.error("Can't get groupware context object.", e);
            throw new StorageException(e);
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }
        try {
            con.setAutoCommit(false);
            for (final User user : users) {
                final UserConfiguration uConf = uConfStorage.getUserConfiguration(user.getId().intValue(), gCtx);
                final DowngradeEvent event = new DowngradeEvent(uConf, con, gCtx);
                registry.fireDowngradeEvent(event);
            }
            con.commit();
        } catch (final OXException e) {
            try {
                con.rollback();
            } catch (final SQLException e1) {
                LOG.error("Error in rollback of database connection.", e1);
                throw new StorageException(e1);
            }
            LOG.error("Internal Error.", e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            try {
                con.rollback();
            } catch (final SQLException e1) {
                LOG.error("Error in rollback of configdb connection", e1);
                throw new StorageException(e1);
            }
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            try {
                cache.pushConnectionForContext(ctx.getId().intValue(), con);
            } catch (final PoolException exp) {
                LOG.error("Error pushing configdb connection to pool!", exp);
            }
        }
    }

    private void changeQuotaForContext(long quota_max_temp, Context ctx, Connection configdb_con) throws SQLException {
        if (quota_max_temp != -1) {
            quota_max_temp = quota_max_temp << 20;
        }

        PreparedStatement prep = null;
        try {
            prep = configdb_con.prepareStatement("UPDATE context SET quota_max=? WHERE cid=?");
            prep.setLong(1, quota_max_temp);
            prep.setInt(2, ctx.getId().intValue());
            prep.executeUpdate();

            try {
                CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
                Cache qfsCache = cacheService.getCache("QuotaFileStorages");
                qfsCache.invalidateGroup(ctx.getId().toString());
            } catch (Exception e) {
                LOG.error("Failed to invalidate caches. Restart recommended.", e);
            }
        } finally {
            Databases.closeSQLStuff(prep);
        }
    }

    private void changeLoginMappingsForContext(Set<String> loginMappings, Context ctx, Connection con) throws SQLException, StorageException {
        // add always the context name
        if (ctx.getName() != null) {
            // a new context Name has been specified
            loginMappings.add(ctx.getName());
        } else {
            // try to read context name from database
            ResultSet rs = null;
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement("SELECT name FROM context WHERE cid=?");
                stmt.setInt(1, i(ctx.getId()));
                rs = stmt.executeQuery();
                if (rs.next()) {
                    loginMappings.add(rs.getString(1));
                }
            } finally {
                closeSQLStuff(rs, stmt);
            }
        }
        loginMappings.remove(ctx.getIdAsString()); // Deny change of mapping cid<->cid

        // first delete all mappings excluding default mapping from cid <-> cid
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM login2context WHERE cid=? AND login_info NOT LIKE ?");
            stmt.setInt(1, ctx.getId().intValue());
            stmt.setString(2, ctx.getIdAsString());
            stmt.executeUpdate();
            stmt.close();
        } finally {
            closeSQLStuff(stmt);
        }
        // now check if some other context uses one of the login mappings
        checkForExistingLoginMapping(con, loginMappings);
        // now insert all mappings from the set
        PreparedStatement stmt2 = null;
        try {
            stmt2 = con.prepareStatement("INSERT INTO login2context (cid,login_info) VALUES (?,?)");
            stmt2.setInt(1, ctx.getId().intValue());
            for (final String loginMapping : loginMappings) {
                if (loginMapping.length() == 0) {
                    continue;
                }
                stmt2.setString(2, loginMapping);
                stmt2.executeUpdate();
            }
        } finally {
            closeSQLStuff(stmt2);
        }
    }

    /**
     * Check if no mapping which the client wants to add already exists for some context.
     *
     * @param con readable connection to the configuration database.
     * @param loginMappings login mappings to check for existance.
     */
    private void checkForExistingLoginMapping(final Connection con, final Set<String> loginMappings) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT cid FROM login2context WHERE login_info=?");
            for (final String loginMapping : loginMappings) {
                if (loginMapping.length() == 0) {
                    LOG.warn("Ignoring empty login mapping.");
                    continue;
                } else if (Strings.containsSurrogatePairs(loginMapping)) {
                    LOG.warn("Ignoring login mapping that contains surrogate pairs '{}'.", loginMapping);
                    continue;
                }
                stmt.setString(1, loginMapping);
                try {
                    result = stmt.executeQuery();
                    if (result.next()) {
                        throw new StorageException("A mapping with login info \"" + loginMapping + "\" already exists in the system!");
                    }
                } finally {
                    closeSQLStuff(result);
                }
            }
        } catch (final SQLException e) {
            throw new StorageException(e.getMessage(), e);
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private void changeNameForContext(String name, Context ctx, Connection configdb_con) throws SQLException {
        PreparedStatement prep = null;
        try {
            prep = configdb_con.prepareStatement("UPDATE context SET name = ? where cid = ?");
            prep.setString(1, name);
            prep.setInt(2, ctx.getId().intValue());
            prep.executeUpdate();
            prep.close();
        } finally {
            Databases.closeSQLStuff(prep);
        }
    }

    private void changeStorageDataImpl(int filestoreId, Context ctx, Connection configdb_write_con) throws SQLException, StorageException {
        OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
        Filestore filestore = oxutil.getFilestore(filestoreId, false);
        int context_id = ctx.getId().intValue();

        PreparedStatement prep = null;
        ResultSet rs = null;
        try {

            if (filestore.getId() != null && -1 != filestore.getId().intValue()) {
                prep = configdb_write_con.prepareStatement("SELECT context.filestore_id FROM context WHERE context.cid = ?");
                prep.setInt(1, context_id);
                rs = prep.executeQuery();
                int oldFilestoreId = rs.next() ? rs.getInt(1) : 0;
                closeSQLStuff(rs, prep);
                rs = null;
                prep = null;

                prep = configdb_write_con.prepareStatement("UPDATE context SET filestore_id = ? WHERE cid = ?");
                prep.setInt(1, filestore.getId().intValue());
                prep.setInt(2, context_id);
                prep.executeUpdate();
                closeSQLStuff(prep);
                prep = null;

                if (oldFilestoreId != 0) {
                    prep = configdb_write_con.prepareStatement("UPDATE contexts_per_filestore SET count=count-1 WHERE filestore_id=? AND count>0");
                    prep.setInt(1, oldFilestoreId);
                    prep.executeUpdate();
                    closeSQLStuff(prep);
                    prep = null;
                }

                prep = configdb_write_con.prepareStatement("UPDATE contexts_per_filestore SET count=count+1 WHERE filestore_id=?");
                prep.setInt(1, filestore.getId().intValue());
                prep.executeUpdate();
                closeSQLStuff(prep);
                prep = null;
            }

            String filestore_name = ctx.getFilestore_name();
            if (null != filestore_name) {
                prep = configdb_write_con.prepareStatement("UPDATE context SET filestore_name = ? WHERE cid = ?");
                prep.setString(1, filestore_name);
                prep.setInt(2, context_id);
                prep.executeUpdate();
                closeSQLStuff(prep);
                prep = null;
            }

        } finally {
            closeSQLStuff(prep);
        }
    }

    private void closePreparedStatement(final PreparedStatement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (final SQLException e) {
            LOG.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
        }
    }

    @Override
    public void updateContextReferences(String sourceSchema, String targetSchema, int targetClusterId) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getWriteConnectionForConfigDB();

            // Get the database pool identifiers from the specified target cluster identifier
            String getPoolIds = "SELECT read_db_pool_id, write_db_pool_id FROM db_cluster WHERE cluster_id = ?";
            stmt = con.prepareStatement(getPoolIds);
            stmt.setInt(1, targetClusterId);
            rs = stmt.executeQuery();
            int writeDbPoolId;
            int readDbPoolId;
            if (rs.next()) {
                readDbPoolId = rs.getInt(1);
                writeDbPoolId = rs.getInt(2);
            } else {
                LOG.error("The specified target cluster id '{}' has no database pool references", targetClusterId);
                throw new StorageException("The specified target cluster id '" + targetClusterId + "' has no database pool references");
            }
            stmt.close();

            // Select the identifier from write-pool as read-pool in case the one from read-pool is 0 (zero)
            // This matches the behavior from OXContextMySQLStorageCommon.fillContextAndServer2DBPool(Context, Connection, Database)
            if (readDbPoolId <= 0) {
                readDbPoolId = writeDbPoolId;
            }

            // Update the relevant references
            String query = "UPDATE context_server2db_pool SET write_db_pool_id = ?, read_db_pool_id = ?, db_schema = ? WHERE db_schema = ?";
            stmt = con.prepareStatement(query);
            stmt.setInt(1, writeDbPoolId);
            stmt.setInt(2, readDbPoolId);
            stmt.setString(3, targetSchema);
            stmt.setString(4, sourceSchema);
            stmt.executeUpdate();

            checkCountsConsistency(con, true, false);

            LOG.info("Successfully restored database pool references in configdb for schema {}", targetSchema);
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (con != null) {
                try {
                    cache.pushWriteConnectionForConfigDB(con);
                } catch (PoolException e) {
                    LOG.error("Error pushing configdb connection to pool!", e);
                }
            }
        }
    }

    @Override
    public String createSchema(int targetClusterId) throws StorageException {
        Connection configCon = null;

        // Get connection to 'configdb'
        try {
            configCon = cache.getWriteConnectionForConfigDB();
        } catch (PoolException e) {
            throw new StorageException(e.getMessage(), e);
        }

        Database database = null;
        boolean error = true;
        boolean created = false;
        boolean rollback = false;
        try {
            // Get database handle
            try {
                final int writePoolId = OXUtilMySQLStorage.getInstance().getWritePoolIdForCluster(targetClusterId);
                database = getDatabaseHandleById(new Database(writePoolId), configCon);
            } catch (SQLException e) {
                LOG.error("SQL Error", e);
                throw new StorageException(e);
            }

            // Create schema
            startTransaction(configCon);
            rollback = true;
            int schemaUnique;
            try {
                schemaUnique = IDGenerator.getId(configCon);
            } catch (SQLException e) {
                throw new StorageException(e.getMessage(), e);
            }
            String schemaName = database.getName() + '_' + schemaUnique;
            database.setScheme(schemaName);
            OXUtilStorageInterface.getInstance().createDatabase(database, configCon);
            created = true;
            configCon.commit();
            rollback = false;
            error = false;
            return database.getScheme();
        } catch (SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            if (rollback) {
                rollback(configCon);
            }

            autocommit(configCon);
            if (error && created) {
                if (database != null) {
                    OXContextMySQLStorageCommon.deleteEmptySchema(i(database.getId()), database.getScheme());
                }
            }
            try {
                cache.pushWriteConnectionForConfigDB(configCon);
            } catch (PoolException e) {
                LOG.error("Error pushing configdb connection to pool!", e);
            }
        }
    }

    @Override
    public void checkCountsConsistency(boolean checkDatabaseCounts, boolean checkFilestoreCounts) throws StorageException {
        // Get connection to 'configdb'
        Connection configCon;
        try {
            configCon = cache.getWriteConnectionForConfigDBNoTimeout();
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }

        boolean rollback = false;
        try {
            startTransaction(configCon);
            rollback = true;

            checkCountsConsistency(configCon, checkDatabaseCounts, checkFilestoreCounts);

            configCon.commit();
            rollback = false;
        } catch (SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            if (rollback) {
                rollback(configCon);
            }
            autocommit(configCon);
            try {
                cache.pushWriteConnectionForConfigDBNoTimeout(configCon);
            } catch (PoolException e) {
                LOG.error("Error pushing configdb connection to pool!", e);
            }
        }
    }

    @Override
    public void checkCountsConsistency(Connection configCon, boolean checkDatabaseCounts, boolean checkFilestoreCounts) throws StorageException {
        if (null == configCon) {
            checkCountsConsistency(checkDatabaseCounts, checkFilestoreCounts);
            return;
        }

        if (checkFilestoreCounts) {
            checkFilestoreCountConsistency(configCon);
        }

        if (checkDatabaseCounts) {
            checkDBPoolCountConsistency(configCon);
            checkDBPoolSchemaCountConsistency(configCon);
        }
    }

    private void checkFilestoreCountConsistency(Connection configCon) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // Drop non-existing ones held in count table
            stmt = configCon.prepareStatement("SELECT contexts_per_filestore.filestore_id FROM contexts_per_filestore LEFT JOIN filestore ON contexts_per_filestore.filestore_id=filestore.id WHERE filestore.id IS NULL");
            rs = stmt.executeQuery();
            if (rs.next()) {
                List<Integer> ids = new LinkedList<Integer>();
                do {
                    ids.add(Integer.valueOf(rs.getInt(1)));
                } while (rs.next());
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;

                stmt = configCon.prepareStatement("DELETE FROM contexts_per_filestore WHERE filestore_id=?");
                for (Integer id : ids) {
                    stmt.setInt(1, id.intValue());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                Databases.closeSQLStuff(rs, stmt);
                stmt = null;
            } else {
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;
            }

            // Check count entries for existing ones
            // GROUP BY CLAUSE: ensure ONLY_FULL_GROUP_BY compatibility
            stmt = configCon.prepareStatement("SELECT filestore.id, COUNT(context.cid) AS num FROM filestore LEFT JOIN context ON filestore.id=context.filestore_id GROUP BY filestore.id ORDER BY num ASC");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                // No file store registered...
                return;
            }

            Map<Integer, Integer> counts = new LinkedHashMap<Integer, Integer>(32, 0.9F);
            do {
                int filestoreId = rs.getInt(1);
                int numContexts = rs.getInt(2);
                counts.put(Integer.valueOf(filestoreId), Integer.valueOf(numContexts));
            } while (rs.next());
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            stmt = configCon.prepareStatement("INSERT INTO contexts_per_filestore (filestore_id, count) VALUES (?, ?) ON DUPLICATE KEY UPDATE count=?");
            for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
                int count = entry.getValue().intValue();
                stmt.setInt(1, entry.getKey().intValue());
                stmt.setInt(2, count);
                stmt.setInt(3, count);
                stmt.addBatch();
            }
            stmt.executeBatch();
            Databases.closeSQLStuff(rs, stmt);
            stmt = null;
        } catch (SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private void checkDBPoolCountConsistency(Connection configCon) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // Drop non-existing ones held in count table
            stmt = configCon.prepareStatement("SELECT contexts_per_dbpool.db_pool_id FROM contexts_per_dbpool LEFT JOIN db_cluster ON contexts_per_dbpool.db_pool_id=db_cluster.write_db_pool_id WHERE db_cluster.write_db_pool_id IS NULL");
            rs = stmt.executeQuery();
            if (rs.next()) {
                List<Integer> ids = new LinkedList<Integer>();
                do {
                    ids.add(Integer.valueOf(rs.getInt(1)));
                } while (rs.next());
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;

                stmt = configCon.prepareStatement("DELETE FROM contexts_per_dbschema WHERE db_pool_id=?");
                for (Integer id : ids) {
                    stmt.setInt(1, id.intValue());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                Databases.closeSQLStuff(rs, stmt);
                stmt = null;

                stmt = configCon.prepareStatement("DELETE FROM contexts_per_dbpool WHERE db_pool_id=?");
                for (Integer id : ids) {
                    stmt.setInt(1, id.intValue());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                Databases.closeSQLStuff(rs, stmt);
                stmt = null;

                stmt = configCon.prepareStatement("DELETE FROM dbpool_lock WHERE db_pool_id=?");
                for (Integer id : ids) {
                    stmt.setInt(1, id.intValue());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                Databases.closeSQLStuff(rs, stmt);
                stmt = null;
            } else {
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;
            }

            // Check count entries for existing ones
            // GROUP BY CLAUSE: ensure ONLY_FULL_GROUP_BY compatibility
            stmt = configCon.prepareStatement("SELECT db_cluster.write_db_pool_id, COUNT(context_server2db_pool.cid) AS num FROM db_cluster LEFT JOIN context_server2db_pool ON db_cluster.write_db_pool_id = context_server2db_pool.write_db_pool_id GROUP BY db_cluster.write_db_pool_id ORDER BY num ASC");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                // No database registered...
                return;
            }

            Map<Integer, Integer> counts = new LinkedHashMap<Integer, Integer>(32, 0.9F);
            do {
                int filestoreId = rs.getInt(1);
                int numContexts = rs.getInt(2);
                counts.put(Integer.valueOf(filestoreId), Integer.valueOf(numContexts));
            } while (rs.next());
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            stmt = configCon.prepareStatement("INSERT INTO contexts_per_dbpool (db_pool_id, count) VALUES (?, ?) ON DUPLICATE KEY UPDATE count=?");
            for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
                int count = entry.getValue().intValue();
                stmt.setInt(1, entry.getKey().intValue());
                stmt.setInt(2, count);
                stmt.setInt(3, count);
                stmt.addBatch();
            }
            stmt.executeBatch();
            Databases.closeSQLStuff(rs, stmt);
            stmt = null;

            stmt = configCon.prepareStatement("INSERT IGNORE INTO dbpool_lock (db_pool_id) VALUES (?)");
            for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
                stmt.setInt(1, entry.getKey().intValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
            Databases.closeSQLStuff(rs, stmt);
            stmt = null;
        } catch (SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private void checkDBPoolSchemaCountConsistency(Connection configCon) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // Determine registered databases (ignore those with max_units=0)
            stmt = configCon.prepareStatement("SELECT write_db_pool_id FROM db_cluster WHERE max_units <> 0 ORDER BY write_db_pool_id ASC");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                // No database registered...
                return;
            }

            List<Integer> poolIds = new LinkedList<Integer>();
            do {
                poolIds.add(Integer.valueOf(rs.getInt(1)));
            } while (rs.next());
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            // Determine referenced schemas in 'context_server2db_pool' associations
            Map<Integer, Database> databases;
            Map<Database, Set<String>> db2ReferencedSchemas;
            {
                stmt = configCon.prepareStatement("SELECT DISTINCT write_db_pool_id, db_schema FROM context_server2db_pool");
                rs = stmt.executeQuery();
                Map<Integer, Set<String>> poolAndSchemas;
                if (rs.next()) {
                    poolAndSchemas = new HashMap<>();
                    do {
                        Integer poolId = Integer.valueOf(rs.getInt(1));
                        Set<String> schemas = poolAndSchemas.get(poolId);
                        if (null == schemas) {
                            schemas = new LinkedHashSet<>();
                            poolAndSchemas.put(poolId, schemas);
                        }
                        schemas.add(rs.getString(2));
                    } while (rs.next());
                } else {
                    poolAndSchemas = null;
                }
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;

                databases = new HashMap<>(null == poolAndSchemas ? 16 : poolAndSchemas.size());
                db2ReferencedSchemas = new HashMap<>(null == poolAndSchemas ? 16 : poolAndSchemas.size());
                stmt = configCon.prepareStatement(Databases.getIN("SELECT db_pool_id, url, driver, login, password, name FROM db_pool WHERE db_pool_id IN (", poolIds.size()));
                int pos = 1;
                for (Integer poolId : poolIds) {
                    stmt.setInt(pos++, poolId.intValue());
                }
                rs = stmt.executeQuery();
                while (rs.next()) {
                    int poolId = rs.getInt(1);
                    Database db = new Database(poolId);
                    db.setUrl(rs.getString(2));
                    db.setDriver(rs.getString(3));
                    db.setLogin(rs.getString(4));
                    db.setPassword(rs.getString(5));
                    db.setName(rs.getString(6));
                    Set<String> schemas = null == poolAndSchemas ? new HashSet<String>(0) : poolAndSchemas.get(Integer.valueOf(poolId));
                    if (null == schemas) {
                        schemas = new HashSet<String>(0);
                    }
                    db2ReferencedSchemas.put(db, schemas);
                    databases.put(Integer.valueOf(poolId), db);
                }
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;
            }

            // Determine really existing schemas per database
            Map<Database, Set<String>> db2ExistingSchemas = new HashMap<>(db2ReferencedSchemas.size());
            {
                for (Database db : db2ReferencedSchemas.keySet()) {
                    List<String> schemas = OXUtilMySQLStorageCommon.listDatabases(db);
                    db2ExistingSchemas.put(db, new HashSet<>(schemas));
                }
            }

            // Determine contained schemas in 'contexts_per_dbschema' table
            Map<Database, Map<String, Integer>> db2ContainedSchemas = new HashMap<>(db2ReferencedSchemas.size());
            stmt = configCon.prepareStatement("SELECT db_pool_id, schemaname, count FROM contexts_per_dbschema");
            rs = stmt.executeQuery();
            Set<Integer> db2delete = null;
            Set<PoolAndSchema> schemas2delete = null;
            while (rs.next()) {
                Integer poolId = Integer.valueOf(rs.getInt(1));
                Database db = databases.get(poolId);
                if (null == db) {
                    // Such a database does not exist
                    if (null == db2delete) {
                        db2delete = new HashSet<>();
                    }
                    db2delete.add(poolId);
                } else {
                    String schema = rs.getString(2);
                    Set<String> existingSchemas = db2ExistingSchemas.get(db);
                    if (existingSchemas.remove(schema)) {
                        // Schema was contained in really existing schemas
                        Map<String, Integer> containedSchemas = db2ContainedSchemas.get(db);
                        if (null == containedSchemas) {
                            containedSchemas = new HashMap<>();
                            db2ContainedSchemas.put(db, containedSchemas);
                        }
                        containedSchemas.put(schema, Integer.valueOf(rs.getInt(3)));
                    } else {
                        // Contained but does not exist
                        if (null == schemas2delete) {
                            schemas2delete = new HashSet<>();
                        }
                        schemas2delete.add(new PoolAndSchema(poolId.intValue(), schema));
                    }
                }
            }
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            // Drop the ones referring to non-existing database hosts
            if (null != db2delete) {
                PreparedStatement deleteStmt = null;
                try {
                    deleteStmt = configCon.prepareStatement(Databases.getIN("DELETE FROM contexts_per_dbschema WHERE db_pool_id IN (", db2delete.size()));
                    int pos = 1;
                    for (Integer poolId : db2delete) {
                        deleteStmt.setInt(pos++, poolId.intValue());
                    }
                    deleteStmt.executeUpdate();
                    Databases.closeSQLStuff(deleteStmt);

                    deleteStmt = configCon.prepareStatement(Databases.getIN("DELETE FROM dbschema_lock WHERE db_pool_id IN (", db2delete.size()));
                    pos = 1;
                    for (Integer poolId : db2delete) {
                        deleteStmt.setInt(pos++, poolId.intValue());
                    }
                    deleteStmt.executeUpdate();
                } finally {
                    Databases.closeSQLStuff(deleteStmt);
                }
            }

            // Drop the ones referring to non-existing database schemas
            if (null != schemas2delete) {
                PreparedStatement deleteStmt = configCon.prepareStatement("DELETE FROM contexts_per_dbschema WHERE db_pool_id=? AND schemaname=?");
                PreparedStatement deleteStmt2 = configCon.prepareStatement("DELETE FROM dbschema_lock WHERE db_pool_id=? AND schemaname=?");
                try {
                    for (PoolAndSchema poolAndSchema : schemas2delete) {
                        deleteStmt.setInt(1, poolAndSchema.getPoolId());
                        deleteStmt.setString(2, poolAndSchema.getSchema());
                        deleteStmt.addBatch();

                        deleteStmt2.setInt(1, poolAndSchema.getPoolId());
                        deleteStmt2.setString(2, poolAndSchema.getSchema());
                        deleteStmt2.addBatch();
                    }
                    deleteStmt.executeBatch();
                    deleteStmt2.executeBatch();
                } finally {
                    Databases.closeSQLStuff(deleteStmt, deleteStmt2);
                }
            }

            // Insert really existing schemas to 'contexts_per_dbschema' (if not contained)
            {
                long now = System.currentTimeMillis();
                for (Map.Entry<Database, Set<String>> entry : db2ExistingSchemas.entrySet()) {
                    Database db = entry.getKey();
                    Set<String> schemas = entry.getValue();
                    if (null != schemas && !schemas.isEmpty()) {
                        for (Set<String> schemasToInsert : Sets.partition(schemas, 25)) {
                            PreparedStatement insertStmt = null;
                            PreparedStatement insertLockStmt = null;
                            try {
                                Map<String, Integer> containedSchemas = db2ContainedSchemas.get(db);
                                for (String schema : schemasToInsert) {
                                    if (false == containedSchemas.containsKey(schema)) {
                                        // Not yet contained
                                        if (null == insertStmt) {
                                            insertStmt = configCon.prepareStatement("INSERT IGNORE INTO contexts_per_dbschema (db_pool_id, schemaname, count, creating_date) VALUES (?, ?, ?, ?)");
                                        }
                                        insertStmt.setInt(1, db.getId().intValue());
                                        insertStmt.setString(2, schema);
                                        insertStmt.setInt(3, 0);
                                        insertStmt.setLong(4, now);
                                        insertStmt.addBatch();

                                        if (null == insertLockStmt) {
                                            insertLockStmt = configCon.prepareStatement("INSERT IGNORE INTO dbschema_lock (db_pool_id, schemaname) VALUES (?, ?)");
                                        }
                                        insertLockStmt.setInt(1, db.getId().intValue());
                                        insertLockStmt.setString(2, schema);
                                        insertLockStmt.addBatch();

                                        containedSchemas.put(schema, Integer.valueOf(0));
                                    }
                                }
                                if (null != insertStmt) {
                                    insertStmt.executeBatch();
                                }
                                if (null != insertLockStmt) {
                                    insertLockStmt.executeBatch();
                                }
                            } finally {
                                Databases.closeSQLStuff(insertLockStmt);
                                Databases.closeSQLStuff(insertStmt);
                            }
                        }
                    }
                }
            }

            // Determine non-referenced ones (contained in 'contexts_per_dbschema' but not referenced within 'context_server2db_pool' associations)
            PreparedStatement updateStmt = null;
            try {
                for (Map.Entry<Database, Map<String, Integer>> db2ContainedSchemasEntry : db2ContainedSchemas.entrySet()) {
                    Database db = db2ContainedSchemasEntry.getKey();
                    Map<String, Integer> containedSchemas = db2ContainedSchemasEntry.getValue();
                    Set<String> referencedSchemas = db2ReferencedSchemas.get(db);

                    for (Map.Entry<String, Integer> containedSchemaEntry : containedSchemas.entrySet()) {
                        String containedSchema = containedSchemaEntry.getKey();
                        if (false == referencedSchemas.contains(containedSchema)) {
                            // Current schema is not in 'context_server2db_pool' associations. Thus no context contained and count is required to be 0 (zero)
                            if (containedSchemaEntry.getValue().intValue() != 0) {
                                if (null == updateStmt) {
                                    updateStmt = configCon.prepareStatement("UPDATE contexts_per_dbschema SET count=0 WHERE db_pool_id=? AND schemaname=?");
                                }
                                updateStmt.setInt(1, db.getId().intValue());
                                updateStmt.setString(2, containedSchema);
                                updateStmt.addBatch();
                            }
                        }
                    }

                    // Don't know whether to check the other way around, which would mean there is an entry in 'context_server2db_pool' associations,
                    // but actually referring to a non-existing schema...
                }
                if (null != updateStmt) {
                    updateStmt.executeBatch();
                }
            } finally {
                Databases.closeSQLStuff(updateStmt);
            }

            class SchemaCount {

                final String schemaName;
                final int count;

                SchemaCount(String schemaName, int count) {
                    super();
                    this.count = count;
                    this.schemaName = schemaName;
                }
            }
            ;

            Map<Integer, List<SchemaCount>> counts = new LinkedHashMap<Integer, List<SchemaCount>>(32, 0.9F);
            for (Integer poolId : poolIds) {
                // GROUP BY CLAUSE: ensure ONLY_FULL_GROUP_BY compatibility
                stmt = configCon.prepareStatement("SELECT db_schema,COUNT(db_schema) AS count FROM context_server2db_pool WHERE write_db_pool_id=? GROUP BY db_schema ORDER BY count ASC");
                stmt.setInt(1, poolId.intValue());
                rs = stmt.executeQuery();

                List<SchemaCount> schemaCounts = new LinkedList<SchemaCount>();
                while (rs.next()) {
                    schemaCounts.add(new SchemaCount(rs.getString(1), rs.getInt(2)));
                }
                counts.put(poolId, schemaCounts);
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;
            }

            if (counts.isEmpty()) {
                // No entries in 'context_server2db_pool' associations
                return;
            }

            long now = System.currentTimeMillis();
            for (Map.Entry<Integer, List<SchemaCount>> entry : counts.entrySet()) {
                int poolId = entry.getKey().intValue();
                List<SchemaCount> schemaCounts = entry.getValue();
                Map<String, Integer> containedSchemas = db2ContainedSchemas.get(databases.get(Integer.valueOf(poolId)));

                // Insert with 100-sized batches
                for (List<SchemaCount> schemaCountSublist : Lists.partition(schemaCounts, 100)) {
                    Set<String> insertLockFor = null;
                    for (SchemaCount schemaCount : schemaCountSublist) {
                        Integer containedCount = containedSchemas.get(schemaCount.schemaName);
                        if (null == containedCount || containedCount.intValue() != schemaCount.count) {
                            if (null == stmt) {
                                stmt = configCon.prepareStatement("INSERT INTO contexts_per_dbschema (db_pool_id, schemaname, count, creating_date) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE count=?");
                            }
                            stmt.setInt(1, poolId);
                            stmt.setString(2, schemaCount.schemaName);
                            stmt.setInt(3, schemaCount.count);
                            stmt.setLong(4, now);
                            stmt.setInt(5, schemaCount.count);
                            stmt.addBatch();

                            if (null == containedCount) {
                                // Not contained, yet. Remember to add lock entry, too
                                if (null == insertLockFor) {
                                    insertLockFor = new HashSet<>(schemaCountSublist.size());
                                }
                                insertLockFor.add(schemaCount.schemaName);
                            }
                        }
                    }
                    if (null != stmt) {
                        stmt.executeBatch();
                        Databases.closeSQLStuff(rs, stmt);
                        stmt = null;
                    }

                    if (null != insertLockFor) {
                        stmt = configCon.prepareStatement("INSERT IGNORE INTO dbschema_lock (db_pool_id, schemaname) VALUES (?, ?)");
                        for (String schemaName : insertLockFor) {
                            stmt.setInt(1, poolId);
                            stmt.setString(2, schemaName);
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                        Databases.closeSQLStuff(rs, stmt);
                        stmt = null;
                    }
                }
            }
        } catch (SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    // --------------------------------------------------------------------------------------------------------------------------------------

    private static class SubmittingRunnable<V> implements Runnable {

        private final Task<V> task;
        private final ThreadPoolService threadPool;

        /**
         * Initializes a new {@link SubmittingRunnable}.
         */
        SubmittingRunnable(Task<V> task, ThreadPoolService threadPool) {
            super();
            this.task = task;
            this.threadPool = threadPool;
        }

        @Override
        public void run() {
            threadPool.submit(task, CallerRunsBehavior.<V> getInstance());
        }

    }
}
