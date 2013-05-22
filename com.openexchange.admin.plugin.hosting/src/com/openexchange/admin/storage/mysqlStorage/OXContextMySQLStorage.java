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

package com.openexchange.admin.storage.mysqlStorage;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import static com.openexchange.tools.sql.DBUtils.startTransaction;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.mail.internet.idn.IDNA;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.osgi.framework.ServiceException;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.exceptions.DatabaseContextMappingException;
import com.openexchange.admin.exceptions.TargetDatabaseException;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.OXContextException;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.services.I18nServices;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUserStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.storage.sqlStorage.OXContextSQLStorage;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.database.TableColumnObject;
import com.openexchange.admin.tools.database.TableObject;
import com.openexchange.admin.tools.database.TableRowObject;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteRegistry;
import com.openexchange.groupware.downgrade.DowngradeEvent;
import com.openexchange.groupware.downgrade.DowngradeRegistry;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.groupware.i18n.Groups;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.log.LogFactory;
import com.openexchange.threadpool.CompletionFuture;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.oxfolder.OXFolderAdminHelper;
import com.openexchange.tools.pipesnfilters.DataSource;
import com.openexchange.tools.pipesnfilters.Filter;
import com.openexchange.tools.pipesnfilters.PipesAndFiltersException;
import com.openexchange.tools.pipesnfilters.PipesAndFiltersService;
import com.openexchange.tools.sql.DBUtils;

/**
 * This class provides the implementation for the storage into a MySQL database
 *
 * @author d7
 * @auhtor cutmasta
 */
public class OXContextMySQLStorage extends OXContextSQLStorage {

    static final Log LOG = LogFactory.getLog(OXContextMySQLStorage.class);

    private int CONTEXTS_PER_SCHEMA = 1;

    private static final int UNIT_CONTEXT = 1;

    private static final int UNIT_USER = 2;

    private final String selectionCriteria = "cid";

    private final int criteriaType = Types.INTEGER;

    // private Object criteriaMatch = null;

    private int USE_UNIT = UNIT_CONTEXT;

    private final OXContextMySQLStorageCommon contextCommon = new OXContextMySQLStorageCommon();

    public OXContextMySQLStorage() {
        try {
            this.CONTEXTS_PER_SCHEMA = Integer.parseInt(prop.getProp("CONTEXTS_PER_SCHEMA", "1"));
            if (this.CONTEXTS_PER_SCHEMA <= 0) {
                throw new OXContextException("CONTEXTS_PER_SCHEMA MUST BE > 0");
            }

            final String unit = prop.getProp("CREATE_CONTEXT_USE_UNIT", "context");
            if (unit.trim().toLowerCase().equals("context")) {
                this.USE_UNIT = UNIT_CONTEXT;
            } else if (unit.trim().toLowerCase().equals("user")) {
                this.USE_UNIT = UNIT_USER;
            } else {
                this.USE_UNIT = UNIT_CONTEXT;
                LOG.warn("unknown unit " + unit + ", using context");
            }
        } catch (final OXContextException e) {
            LOG.error("Error init", e);
        }
    }

    @Override
    public void delete(final Context ctx) throws StorageException {
        LOG.debug("Fetching connection and scheme for context " + ctx.getId());
        // groupware context must be loaded before entry from user_setting_admin table is removed.
        com.openexchange.groupware.contexts.Context gwCtx = null;
        try {
            final ContextService service = AdminServiceRegistry.getInstance().getService(ContextService.class, true);
            gwCtx = service.getContext(ctx.getId().intValue());
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        } catch (final ServiceException e) {
            LOG.error(e.getMessage(), e);
        }
        // we need the right connection and scheme for this context
        final int poolId;
        final Connection conForContext;
        try {
            poolId = cache.getDBPoolIdForContextId(ctx.getId().intValue());
            final String scheme = cache.getSchemeForContextId(ctx.getId().intValue());
            conForContext = cache.getWRITENoTimeoutConnectionForPoolId(poolId, scheme);
            LOG.debug("Connection and scheme fetched for context " + ctx.getId());
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }
        final List<TableObject> sorted_tables;
        try {
            // fetch tables which can contain context data and sort these tables magically by foreign keys
            LOG.debug("Fetching table structure from database scheme for context " + ctx.getId());
            final Vector<TableObject> fetchTableObjects = fetchTableObjects(conForContext);
            LOG.debug("Table structure fetched for context " + ctx.getId() + "\nTry to find foreign key dependencies between tables and sort table for context " + ctx.getId());
            // sort the tables by references (foreign keys)
            sorted_tables = sortTableObjects(fetchTableObjects, conForContext);
            LOG.debug("Dependencies found and tables sorted for context " + ctx.getId());
            // loop through tables and execute delete statements on each table
            deleteContextData(ctx, conForContext, sorted_tables);
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final StorageException e) {
            throw new StorageException(e.getMessage());
        } finally {
            // must be pushed back here, because in the "deleteContextFromConfigDB" the connection is "reset" in the pool.
            // else we would get an not nice error in the logfile from the dbpool
            try {
                cache.pushWRITENoTimeoutConnectionForPoolId(poolId, conForContext);
            } catch (final PoolException e) {
                LOG.error("Pool Error", e);
            }
        }
        final Connection conForConfigDB;
        try {
            conForConfigDB = cache.getConnectionForConfigDB();
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }
        try {
            // fetch infos for filestore from configdb before deleting on this connection
            final URI storageURI;
            if (null != gwCtx) {
                storageURI = FilestoreStorage.createURI(conForConfigDB, gwCtx);
            } else {
                storageURI = FilestoreStorage.createURI(conForConfigDB, ctx.getId().intValue());
            }
            // Delete filestore directory of the context
            LOG.debug("Starting filestore delete(cid=" + ctx.getId() + ") from disc!");
            boolean simpleDelete = null == gwCtx;
            try {
                if (!simpleDelete) {
                    QuotaFileStorage.getInstance(storageURI, gwCtx).remove();
                }
            } catch (final OXException e) {
                simpleDelete = true;
                LOG.error("File storage implementation failed to remove the file storage. Continuing with hard delete of file storage.", e);
            }
            if (simpleDelete) {
                FileUtils.deleteDirectory(new File(storageURI));
            }
            LOG.debug("Filestore delete(cid=" + ctx.getId() + ") from disc finished!");
            // Execute delete context from configdb AND the drop database command if this context is the last one
            conForConfigDB.setAutoCommit(false);
            contextCommon.deleteContextFromConfigDB(conForConfigDB, ctx.getId().intValue());
            // submit delete to database under any circumstance before the filestore gets deleted.see bug 9947
            conForConfigDB.commit();
            LOG.info("Context " + ctx.getId() + " deleted.");
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            throw new StorageException(e);
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            LOG.error(e.getMessage(), e);
            rollback(conForConfigDB);
            throw new StorageException(e);
        } finally {
            try {
                cache.pushConnectionForConfigDB(conForConfigDB);
            } catch (final PoolException exp) {
                LOG.error("Pool Error", exp);
            }
        }
    }

    private void deleteContextData(final Context ctx, final Connection con, final List<TableObject> sorted_tables) throws StorageException {
        LOG.debug("Now deleting data for context " + ctx.getId());
        PreparedStatement dynamicAttrDel = null;
        try {
            con.setAutoCommit(false);
            // first delete everything with OSGi DeleteListener services.
            try {
                final DeleteEvent event = new DeleteEvent(this, ctx.getId().intValue(), DeleteEvent.TYPE_CONTEXT, ctx.getId().intValue());
                DeleteRegistry.getInstance().fireDeleteEvent(event, con, con);
            } catch (final OXException e) {
                LOG.error(
                    "Some implementation deleting context specific data failed. Continuing with hard delete from tables using cid column.",
                    e);
            }
            // now go through tables and delete the remainders
            for (int i = sorted_tables.size() - 1; i >= 0; i--) {
                deleteTableData(ctx, con, sorted_tables.get(i));
            }
            // commit groupware data scheme deletes BEFORE database get dropped in "deleteContextFromConfigDB" .see bug #10501
            dynamicAttrDel = con.prepareStatement("DELETE FROM contextAttribute WHERE cid = ?");
            dynamicAttrDel.setInt(1, ctx.getId());
            dynamicAttrDel.executeUpdate();
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw new StorageException(e);
        } finally {
            closeSQLStuff(dynamicAttrDel);
            autocommit(con);
        }
        LOG.debug("Data delete for context " + ctx.getId() + " completed!");
    }

    private void deleteTableData(final Context ctx, final Connection con, final TableObject to) throws SQLException {
        LOG.debug("Deleting data from table " + to.getName() + " for context " + ctx.getId());
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM " + to.getName() + " WHERE cid=?");
            stmt.setInt(1, ctx.getId().intValue());
            stmt.executeUpdate();
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
            myLockUnlockAllContexts(false, reason.getId(), addtionaltable, sqlconjunction);
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
            myEnableDisableContext(ctx.getId(), false, reason.getId());
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }
    }

    /*
     * (non-Javadoc)
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
            myEnableDisableContext(ctx.getId(), true, -1);
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
            configCon = cache.getConnectionForConfigDB();
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
            try {
                if (null != configCon) {
                    cache.pushConnectionForConfigDB(configCon);
                }
            } catch (final PoolException exp) {
                LOG.error("Error pushing configdb connection to pool!", exp);
            }
        }
    }

    /**
     * @throws StorageException
     * @see com.openexchange.admin.storage.sqlStorage.OXContextSQLStorage#moveDatabaseContext(int, Database, int)
     */
    @Override
    public void moveDatabaseContext(final Context ctx, final Database target_database_id, final MaintenanceReason reason) throws StorageException {
        long start = 0;
        long end = 0;
        if (LOG.isDebugEnabled()) {
            start = System.currentTimeMillis();
        }
        LOG.debug("Move of data for context " + ctx.getId() + " is now starting to target database " + target_database_id + "!");

        Connection ox_db_write_con = null;
        Connection configdb_write_con = null;

        PreparedStatement stm = null;

        Connection target_ox_db_con = null;

        TableObject contextserver2dbpool_backup = null;
        int source_database_id = -1;

        try {
            configdb_write_con = cache.getConnectionForConfigDB();
            // ox_db_write_con = cache.getWRITEConnectionForContext(context_id);
            source_database_id = cache.getDBPoolIdForContextId(ctx.getId());
            final String scheme = cache.getSchemeForContextId(ctx.getId());

            ox_db_write_con = cache.getWRITEConnectionForPoolId(source_database_id, scheme);

            /*
             * 1. Lock the context if not already locked. if already locked, throw exception cause the context could be already in progress
             * for moving.
             */
            LOG.debug("Context " + ctx.getId() + " will now be disabled for moving!");
            disable(ctx, reason);
            LOG.debug("Context " + ctx.getId() + " is now disabled!");

            /*
             * 2. Fetch tables with cid column which could perhaps store data relevant for us
             */
            LOG.debug("Fetching table structure from database scheme!");
            final Vector<TableObject> fetchTableObjects = fetchTableObjects(ox_db_write_con);
            // ####### ##### geht hier was kaputt -> enableContext(); ########
            LOG.debug("Table structure fetched!");

            // this must sort the tables by references (foreign keys)
            LOG.debug("Try to find foreign key dependencies between tables and sort table!");
            final ArrayList<TableObject> sorted_tables = sortTableObjects(fetchTableObjects, ox_db_write_con);
            // ####### ##### geht hier was kaputt -> enableContext(); ########
            LOG.debug("Dependencies found and tables sorted!");

            // fetch data for db handle to create database
            LOG.debug("Get database handle information for target database system!");
            final Database db_handle = getDatabaseHandleById(target_database_id, configdb_write_con);
            // ####### ##### geht hier was kaputt -> enableContext(); ########
            LOG.debug("Database handle information found!");

            // backup old mapping in contextserver2dbpool for recovery if
            // something breaks
            LOG.debug("Backing up current configdb entries for context " + ctx.getId());
            contextserver2dbpool_backup = backupContextServer2DBPoolEntry(ctx.getId(), configdb_write_con);
            // ####### ##### geht hier was kaputt -> enableContext(); ########
            LOG.debug("Backup complete!");

            // create database or use existing database AND update the mapping
            // in contextserver2dbpool
            try {
                LOG.debug("Creating new scheme or using existing scheme on target database system!");
                createDatabaseAndMappingForContext(db_handle, configdb_write_con, ctx.getId());
                cache.resetPoolMappingForContext(ctx.getId());
                LOG.debug("Scheme found and mapping in configdb changed to new target database system!");
            } catch (final SQLException sqle) {
                LOG.error("SQL Error", sqle);
                throw new DatabaseContextMappingException("" + sqle.getMessage());
            } catch (final PoolException poolex) {
                LOG.error("Pool Error", poolex);
                throw new DatabaseContextMappingException("" + poolex.getMessage());
            }

            // now insert all data to target db
            LOG.debug("Now filling target database system " + target_database_id + " with data of context " + ctx.getId() + "!");
            try {
                target_ox_db_con = cache.getConnectionForContext(ctx.getId());
                target_ox_db_con.setAutoCommit(false);
                fillTargetDatabase(sorted_tables, target_ox_db_con, ox_db_write_con, ctx.getId());
                // commit ALL tables with all data of every row
                target_ox_db_con.commit();
            } catch (final SQLException sql) {
                LOG.error("SQL Error", sql);
                throw new TargetDatabaseException("" + sql.getMessage());
            } catch (final PoolException pexp) {
                LOG.error("Pool Error", pexp);
                throw new TargetDatabaseException("" + pexp.getMessage());
            }

            LOG.debug("Filling completed for target database system " + target_database_id + " with data of context " + ctx.getId() + "!");

            // now delete from old database schema all the data
            // For delete from database we loop recursive
            ox_db_write_con.setAutoCommit(false);
            LOG.debug("Now deleting data for context " + ctx.getId() + " from old scheme!");
            for (int a = sorted_tables.size() - 1; a >= 0; a--) {
                final TableObject to = sorted_tables.get(a);
                stm = ox_db_write_con.prepareStatement("DELETE FROM " + to.getName() + " WHERE cid = ?");
                stm.setInt(1, ctx.getId());
                LOG.debug("Deleting data from table \"" + to.getName() + "\" for context " + ctx.getId());
                stm.executeUpdate();
                stm.close();
            }
            LOG.debug("Data delete for context " + ctx.getId() + " completed!");

            // check if scheme is empty after deleting context data on source db
            // if yes, drop whole database
            deleteSchemeFromDatabaseIfEmpty(ox_db_write_con, configdb_write_con, source_database_id, scheme);
            ox_db_write_con.commit();

            // all this was ok , then enable context back again
            LOG.debug("Enabling context " + ctx.getId() + " back again!");
            enable(ctx);
        } catch (final DatabaseContextMappingException dcme) {
            LOG.error("Exception caught while updating mapping in configdb", dcme);
            // revoke contextserver2dbpool()
            try {
                LOG.error("Now revoking entries in configdb (cs2dbpool) for context " + ctx.getId());
                revokeConfigdbMapping(contextserver2dbpool_backup, configdb_write_con, ctx.getId());
                cache.resetPoolMappingForContext(ctx.getId());
            } catch (final Exception ecp) {
                LOG.fatal(
                    "!!!!!!WARNING!!!!! Could not revoke configdb entries for " + ctx.getId() + "!!!!!!WARNING!!! INFORM ADMINISTRATOR!!!!!!",
                    ecp);
            }

            // enableContext() back

            enableContextBackAfterError(ctx);

            throw new StorageException(dcme);
        } catch (final TargetDatabaseException tde) {
            LOG.error("Exception caught while moving data for context " + ctx.getId() + " to target database " + target_database_id, tde);
            LOG.error("Target database rollback starts for context " + ctx.getId());
            // rollback insert on target db
            if (target_ox_db_con != null) {
                try {
                    target_ox_db_con.rollback();
                    LOG.error("Target database rollback finished for context " + ctx.getId());
                } catch (final SQLException ecp) {
                    LOG.error("Error rollback on target database", ecp);
                }
            }

            // revoke contextserver2dbpool()
            try {
                LOG.error("Now revoking entries in configdb (cs2dbpool) for context " + ctx.getId());
                revokeConfigdbMapping(contextserver2dbpool_backup, configdb_write_con, ctx.getId());
                cache.resetPoolMappingForContext(ctx.getId());
            } catch (final SQLException ecp) {
                LOG.fatal(
                    "!!!!!!WARNING!!!!! Could not revoke configdb entries for " + ctx.getId() + "!!!!!!WARNING!!! INFORM ADMINISTRATOR!!!!!!",
                    ecp);
            } catch (final PoolException ecp) {
                LOG.fatal(
                    "!!!!!!WARNING!!!!! Could not revoke configdb entries for " + ctx.getId() + "!!!!!!WARNING!!! INFORM ADMINISTRATOR!!!!!!",
                    ecp);
            }

            // enableContext() back
            enableContextBackAfterError(ctx);

            throw new StorageException(tde);
        } catch (final SQLException sql) {
            // enableContext back
            LOG.error(
                "SQL Error caught while moving data " + "for context " + ctx.getId() + " to target database " + target_database_id,
                sql);
            enable(ctx);

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

            enableContextBackAfterError(ctx);
            throw new StorageException(pexp);
        } finally {
            if (ox_db_write_con != null) {
                try {
                    if (!ox_db_write_con.getAutoCommit()) {
                        ox_db_write_con.setAutoCommit(true);
                    }
                    cache.pushWRITEConnectionForPoolId(source_database_id, ox_db_write_con);
                } catch (final Exception ex) {
                    LOG.error("Error pushing connection", ex);
                }
            }
            if (configdb_write_con != null) {
                try {
                    if (!configdb_write_con.getAutoCommit()) {
                        configdb_write_con.setAutoCommit(true);
                    }
                    cache.pushConnectionForConfigDB(configdb_write_con);
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
                try {
                    if (!target_ox_db_con.getAutoCommit()) {
                        target_ox_db_con.setAutoCommit(true);
                    }
                    cache.pushWRITEConnectionForPoolId(target_database_id.getId(), target_ox_db_con);
                } catch (final Exception ex) {
                    LOG.error("Error pushing connection", ex);
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            end = System.currentTimeMillis();
            double time_ = end - start;
            time_ = time_ / 1000;
            LOG.debug("Data moving for context " + ctx.getId() + " to target database system " + target_database_id + " completed in " + time_ + " seconds!");
        }
    }

    @Override
    public String moveContextFilestore(final Context ctx, final Filestore dst_filestore_id, final MaintenanceReason reason) throws StorageException {
        return null;
    }

    @Override
    public Context[] listContext(final String pattern, final List<Filter<Integer, Integer>> filters, final List<Filter<Context, Context>> loaders) throws StorageException {
        final String sqlPattern = pattern.replace('*', '%');
        ThreadPoolService threadPoolS;
        try {
            threadPoolS = AdminServiceRegistry.getInstance().getService(ThreadPoolService.class, true);
        } catch (final OXException e) {
            throw new StorageException(e.getMessage(), e);
        }

        ContextSearcher[] searchers = null;
        searchers = new ContextSearcher[] {
            new ContextSearcher(cache, "SELECT cid FROM context WHERE name LIKE ?", sqlPattern),
            new ContextSearcher(cache, "SELECT cid FROM context WHERE cid = ?", sqlPattern),
            new ContextSearcher(cache, "SELECT cid FROM login2context WHERE login_info LIKE ?", sqlPattern) };
        final CompletionFuture<Collection<Integer>> completion = threadPoolS.invoke(searchers);
        final Set<Integer> cids = new HashSet<Integer>();
        try {
            for (int i = 0; i < searchers.length; i++) {
                final Future<Collection<Integer>> future = completion.take();
                cids.addAll(future.get());
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new StorageException(e.getMessage(), e);
        } catch (final CancellationException e) {
            throw new StorageException(e.getMessage(), e);
        } catch (final ExecutionException e) {
            throw ThreadPools.launderThrowable(e, StorageException.class);
        }

        Set<Integer> filteredCids = null;
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
            filteredCids = new HashSet<Integer>();
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
        }

        final boolean failOnMissing = false;

        return contextCommon.loadContexts(filteredCids != null ? filteredCids : cids, Long.parseLong(prop.getProp(
            "AVERAGE_CONTEXT_SIZE",
            "100")), loaders, failOnMissing);
    }

    @Override
    public Context[] searchContextByDatabase(final Database db_host) throws StorageException {
        Connection con = null;
        // maybe we should make the search pattern configurable
        PreparedStatement stmt = null;

        try {
            con = cache.getConnectionForConfigDB();
            stmt = con.prepareStatement("SELECT context_server2db_pool.cid FROM context_server2db_pool INNER JOIN (server,db_pool) ON (context_server2db_pool.server_id=server.server_id AND db_pool.db_pool_id=context_server2db_pool.read_db_pool_id OR context_server2db_pool.write_db_pool_id=db_pool.db_pool_id) WHERE server.name=? AND db_pool.db_pool_id=?");
            final String serverName = AdminServiceRegistry.getInstance().getService(ConfigurationService.class).getProperty(AdminProperties.Prop.SERVER_NAME, "local");
            stmt.setString(1, serverName);
            stmt.setInt(2, db_host.getId());
            final ResultSet rs = stmt.executeQuery();
            final ArrayList<Context> list = new ArrayList<Context>();
            while (rs.next()) {
                // TODO: This could be filled with the query directly to
                // optimize performance
                list.add(contextCommon.getData(
                    new Context(I(rs.getInt(1))),
                    con,
                    Long.parseLong(prop.getProp("AVERAGE_CONTEXT_SIZE", "100"))));
            }
            rs.close();
            stmt.close();
            return list.toArray(new Context[list.size()]);
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            closePreparedStatement(stmt);
            pushConnectionToPoolConfigDB(con);
        }
    }

    @Override
    public Context[] searchContextByFilestore(final Filestore filestore) throws StorageException {
        Connection con = null;
        Connection oxdb_read = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        PreparedStatement logininfo = null;
        ResultSet rs = null;
        ResultSet rs2 = null;
        int context_id = -1;
        try {
            con = cache.getConnectionForConfigDB();

            stmt = con.prepareStatement("SELECT context.cid, context.name, context.enabled, context.reason_id, context.filestore_id, context.filestore_name, context.quota_max, context_server2db_pool.write_db_pool_id, context_server2db_pool.read_db_pool_id, context_server2db_pool.db_schema FROM context LEFT JOIN ( context_server2db_pool, server ) ON ( context.cid = context_server2db_pool.cid AND context_server2db_pool.server_id = server.server_id ) WHERE server.name = ? AND context.filestore_id = ?");
            logininfo = con.prepareStatement("SELECT login_info FROM `login2context` WHERE cid=?");
            final String serverName = AdminServiceRegistry.getInstance().getService(ConfigurationService.class).getProperty(AdminProperties.Prop.SERVER_NAME, "local");
            stmt.setString(1, serverName);
            stmt.setInt(2, filestore.getId());
            rs = stmt.executeQuery();
            final ArrayList<Context> list = new ArrayList<Context>();
            while (rs.next()) {
                final Context cs = new Context();

                context_id = rs.getInt(1);

                final String name = rs.getString(2); // name
                // name of the context, currently same with contextid
                if (name != null) {
                    cs.setName(name);
                }

                cs.setEnabled(rs.getBoolean(3)); // enabled
                final int reason_id = rs.getInt(4); // reason
                // CONTEXT STATE INFOS #
                if (-1 != reason_id) {
                    cs.setMaintenanceReason(new MaintenanceReason(reason_id));
                }
                cs.setFilestoreId(rs.getInt(5)); // filestore_id
                cs.setFilestore_name(rs.getString(6)); // filestorename
                long quota_max = rs.getLong(7); // quota max
                if (quota_max != -1) {
                    quota_max /= Math.pow(2, 20);
                    // set quota max also in context setup object
                    cs.setMaxQuota(quota_max);
                }
                final int write_pool = rs.getInt(8); // write_pool_id
                final int read_pool = rs.getInt(9); // read_pool_id
                final String db_schema = rs.getString(10); // db_schema
                if (null != db_schema) {
                    cs.setReadDatabase(new Database(read_pool, db_schema));
                    cs.setWriteDatabase(new Database(write_pool, db_schema));
                }
                logininfo.setInt(1, context_id);
                rs2 = logininfo.executeQuery();
                try {
                    while (rs2.next()) {
                        cs.addLoginMapping(rs.getString(1));
                    }
                } finally {
                    rs2.close();
                }

                oxdb_read = cache.getConnectionForContext(context_id);
                long quota_used = 0;
                try {
                    stmt2 = oxdb_read.prepareStatement("SELECT filestore_usage.used FROM filestore_usage WHERE filestore_usage.cid = ?");
                    stmt2.setInt(1, context_id);
                    rs2 = stmt2.executeQuery();

                    while (rs2.next()) {
                        quota_used = rs2.getLong(1);
                    }
                } finally {
                    rs2.close();
                    stmt2.close();
                    cache.pushConnectionForContext(context_id, oxdb_read);
                }

                quota_used /= Math.pow(2, 20);
                // set used quota in context setup
                cs.setUsedQuota(quota_used);

                cs.setAverage_size(Long.parseLong(prop.getProp("AVERAGE_CONTEXT_SIZE", "100")));

                // context id
                cs.setId(context_id);

                list.add(cs);
            }

            return list.toArray(new Context[list.size()]);
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            closePreparedStatement(stmt);
            closePreparedStatement(logininfo);
            closeRecordset(rs);
            pushConnectionToPoolConfigDB(con);
        }
    }

    @Override
    public void changeStorageData(final Context ctx) throws StorageException {
        Connection configdb_write_con = null;
        final PreparedStatement prep = null;
        try {
            configdb_write_con = cache.getConnectionForConfigDB();
            configdb_write_con.setAutoCommit(false);

            changeStorageDataImpl(ctx, configdb_write_con);

            configdb_write_con.commit();
        } catch (final DataTruncation dt) {
            LOG.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final SQLException exp) {
            LOG.error("SQL Error", exp);
            try {
                if (configdb_write_con != null && !configdb_write_con.getAutoCommit()) {
                    configdb_write_con.rollback();
                }
            } catch (final SQLException expd) {
                LOG.error("Error processing rollback of connection!", expd);
            }
            throw new StorageException(exp);
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            try {
                if (configdb_write_con != null && !configdb_write_con.getAutoCommit()) {
                    configdb_write_con.rollback();
                }
            } catch (final SQLException expd) {
                LOG.error("Error processing rollback of connection!", expd);
            }
            throw new StorageException(e);
        } finally {
            try {
                if (prep != null) {
                    prep.close();
                }
            } catch (final SQLException exp) {
                LOG.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT);
            }
            try {
                if (configdb_write_con != null) {
                    cache.pushConnectionForConfigDB(configdb_write_con);
                }
            } catch (final PoolException ecp) {
                LOG.error("Error pushing configdb connection to pool!", ecp);
            }
        }
    }

    @Override
    public Context create(final Context ctx, final User adminUser, final UserModuleAccess access) throws StorageException {
        if (null == adminUser) {
            throw new StorageException("Context administrator is not defined.");
        }

        // Find filestore for context.
        ctx.setFilestore_name(ctx.getIdAsString() + "_ctx_store");
        Integer storeId = ctx.getFilestoreId();
        if (null == storeId) {
            storeId = OXUtilStorageInterface.getInstance().findFilestoreForContext().getId();
            ctx.setFilestoreId(storeId);
        } else {
            if (!OXToolStorageInterface.getInstance().existsStore(i(storeId))) {
                throw new StorageException("Filestore with identifier " + storeId + " does not exist.");
            }
        }

        final Connection configCon;
        try {
            configCon = cache.getConnectionForConfigDB();
        } catch (PoolException e) {
            throw new StorageException(e.getMessage(), e);
        }
        try {
            Integer dbId = null;
            if (null != ctx.getWriteDatabase()) {
                dbId = ctx.getWriteDatabase().getId();
            }
            final Database db;
            try {
                if (null == dbId || i(dbId) <= 0) {
                    db = getNextDBHandleByWeight(configCon);
                } else {
                    db = OXToolStorageInterface.getInstance().loadDatabaseById(i(dbId));
                }
            } catch (SQLException e) {
                throw new StorageException(e.getMessage(), e);
            } catch (OXContextException e) {
                LOG.error(e.getMessage(), e);
                throw new StorageException(e.getMessage());
            }
            // Two separate try-catch blocks are necessary because rollback only works after starting a transaction.
            try {
                startTransaction(configCon);
                findOrCreateSchema(configCon, db);
                contextCommon.fillContextAndServer2DBPool(ctx, configCon, db);
                contextCommon.fillLogin2ContextTable(ctx, configCon);
                configCon.commit();
                final Context retval = writeContext(configCon, ctx, adminUser, access);
                LOG.info("Context " + retval.getId() + " created!");
                return retval;
            } catch (SQLException e) {
                rollback(configCon);
                throw new StorageException(e.getMessage(), e);
            } catch (StorageException e) {
                rollback(configCon);
                throw e;
            } finally {
                autocommit(configCon);
            }
        } finally {
            pushConnectionToPoolConfigDB(configCon);
        }
    }

    private Context writeContext(final Connection configCon, final Context ctx, final User adminUser, final UserModuleAccess access) throws StorageException {
        final int contextId = ctx.getId().intValue();
        Connection oxCon = null;
        try {
            oxCon = cache.getConnectionForContext(contextId);
            oxCon.setAutoCommit(false);

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
            // TODO: cutmasta call setters and fill all required fields
            ctx.setEnabled(Boolean.TRUE);
            adminUser.setId(Integer.valueOf(adminId));
            return ctx;
        } catch (final DataTruncation e) {
            LOG.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, e);
            contextCommon.handleCreateContextRollback(configCon, oxCon, contextId);
            throw AdminCache.parseDataTruncation(e);
        } catch (final OXException e) {
            LOG.error("Error", e);
            contextCommon.handleCreateContextRollback(configCon, oxCon, contextId);
            throw new StorageException(e.toString());
        } catch (final StorageException e) {
            LOG.error("Storage Error", e);
            contextCommon.handleCreateContextRollback(configCon, oxCon, contextId);
            throw e;
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            contextCommon.handleCreateContextRollback(configCon, oxCon, contextId);
            throw new StorageException(e);
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            contextCommon.handleCreateContextRollback(configCon, oxCon, contextId);
            throw new StorageException(e);
        } catch (final Exception e) {
            LOG.error("Internal Error", e);
            contextCommon.handleCreateContextRollback(configCon, oxCon, contextId);
            throw new StorageException("Internal server error occured");
        } finally {
            autocommit(oxCon);
            try {
                if (oxCon != null) {
                    cache.pushConnectionForContext(contextId, oxCon);
                }
            } catch (final PoolException ecp) {
                LOG.error("Error pushing ox write connection to pool!", ecp);
            }
        }
    }


    private void updateDynamicAttributes(final Connection oxCon, final Context ctx) throws SQLException {
        PreparedStatement stmtupdateattribute = null;
        PreparedStatement stmtinsertattribute = null;
        PreparedStatement stmtdelattribute = null;
        final int contextId = ctx.getId();
        try {

            if (ctx.isUserAttributesset()) {

                stmtupdateattribute = oxCon.prepareStatement("UPDATE contextAttribute SET value = ? WHERE cid=? AND name=?");
                stmtupdateattribute.setInt(2, contextId);

                stmtinsertattribute = oxCon.prepareStatement("INSERT INTO contextAttribute (value, cid, name) VALUES (?, ?, ?)");
                stmtinsertattribute.setInt(2, contextId);

                stmtdelattribute = oxCon.prepareStatement("DELETE FROM contextAttribute WHERE cid=? AND name=?");
                stmtdelattribute.setInt(1, contextId);

                for (final Map.Entry<String, Map<String, String>> ns : ctx.getUserAttributes().entrySet()) {
                    final String namespace = ns.getKey();
                    for (final Map.Entry<String, String> pair : ns.getValue().entrySet()) {
                        final String name = namespace + "/" + pair.getKey();
                        final String value = pair.getValue();
                        if (value != null) {
                            stmtupdateattribute.setString(1, value);
                            stmtupdateattribute.setString(3, name);

                            final int changedRows = stmtupdateattribute.executeUpdate();
                            if (changedRows == 0) {
                                stmtinsertattribute.setString(1, value);
                                stmtinsertattribute.setString(3, name);
                                stmtinsertattribute.executeUpdate();
                            }
                        } else {
                            stmtdelattribute.setString(2, name);
                            stmtdelattribute.executeUpdate();
                        }
                    }
                }

            }
        } finally {
            if (stmtupdateattribute != null) {
                stmtupdateattribute.close();
            }
            if (stmtinsertattribute != null) {
                stmtinsertattribute.close();
            }
            if (stmtdelattribute != null) {
                stmtdelattribute.close();
            }
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
    private Database getDatabaseHandleById(final Database database_id, final Connection configdb_write) throws SQLException {
        final Database retval = new Database();
        retval.setId(database_id.getId());
        PreparedStatement pstm = null;
        try {
            pstm = configdb_write.prepareStatement("SELECT url,driver,login,password,name FROM db_pool WHERE db_pool_id = ?");
            pstm.setInt(1, database_id.getId());
            final ResultSet rs = pstm.executeQuery();
            rs.next();
            retval.setLogin(rs.getString("login"));
            retval.setPassword(rs.getString("password"));
            retval.setDriver(rs.getString("driver"));
            retval.setUrl(rs.getString("url"));
            retval.setName(rs.getString("name"));
            rs.close();
        } finally {
            try {
                if (pstm != null) {
                    pstm.close();
                }
            } catch (final Exception e) {
                LOG.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
            }
        }
        return retval;
    }

    private TableObject backupContextServer2DBPoolEntry(final int context_id, final Connection configdb_write_con) throws SQLException {
        final TableObject ret = new TableObject();
        PreparedStatement pstm = null;

        try {
            ret.setName("context_server2db_pool");
            pstm = configdb_write_con.prepareStatement("SELECT server_id,cid,read_db_pool_id,write_db_pool_id,db_schema FROM context_server2db_pool where cid = ?");
            pstm.setInt(1, context_id);
            final ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                final TableRowObject tro = new TableRowObject();

                final TableColumnObject tco = new TableColumnObject();
                final Object srv_id = rs.getObject("server_id");
                tco.setData(srv_id);
                tco.setName("server_id");
                tco.setType(java.sql.Types.INTEGER);
                tro.setColumn(tco);

                final TableColumnObject tco2 = new TableColumnObject();
                final Object cid = rs.getObject("cid");
                tco2.setData(cid);
                tco2.setName("cid");
                tco2.setType(java.sql.Types.INTEGER);
                tro.setColumn(tco2);

                final TableColumnObject tco3 = new TableColumnObject();
                final Object obj = rs.getObject("read_db_pool_id");
                tco3.setData(obj);
                tco3.setName("read_db_pool_id");
                tco3.setType(java.sql.Types.INTEGER);
                tro.setColumn(tco3);

                final TableColumnObject tco4 = new TableColumnObject();
                final Object obj2 = rs.getObject("write_db_pool_id");
                tco4.setData(obj2);
                tco4.setName("write_db_pool_id");
                tco4.setType(java.sql.Types.INTEGER);
                tro.setColumn(tco4);

                final TableColumnObject tco5 = new TableColumnObject();
                final Object obj3 = rs.getObject("db_schema");
                tco5.setData(obj3);
                tco5.setName("db_schema");
                tco5.setType(java.sql.Types.VARCHAR);
                tro.setColumn(tco5);

                ret.setDataRow(tro);
            }
            rs.close();
        } finally {
            try {
                if (pstm != null) {
                    pstm.close();
                }
            } catch (final Exception e) {
                LOG.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
            }
        }
        return ret;
    }

    /**
     * @param configCon a write connection to the configuration database that is already in a transaction.
     */
    private void findOrCreateSchema(final Connection configCon, final Database db) throws StorageException {
        String schemaName = getNextUnfilledSchemaFromDB(db.getId(), configCon);
        if (CONTEXTS_PER_SCHEMA == 1 || schemaName == null) {
            int schemaUnique;
            try {
                schemaUnique = IDGenerator.getId(configCon);
            } catch (SQLException e) {
                throw new StorageException(e.getMessage(), e);
            }
            schemaName = db.getName() + '_' + schemaUnique;
            db.setScheme(schemaName);
            OXUtilStorageInterface.getInstance().createDatabase(db);
        } else {
            db.setScheme(schemaName);
        }
    }

    private void createDatabaseAndMappingForContext(final Database db, final Connection configCon, final int context_id) throws SQLException, StorageException {

        final OXUtilStorageInterface oxu = OXUtilStorageInterface.getInstance();
        if (this.CONTEXTS_PER_SCHEMA == 1) {
            String schema_name;
            synchronized (ClientAdminThread.create_mutex) {
                configCon.setAutoCommit(false);
                final int srv_id = IDGenerator.getId(configCon);
                configCon.commit();
                schema_name = db.getName() + "_" + srv_id;
            }
            db.setScheme(schema_name);
            oxu.createDatabase(db);

            // update contextserver2dbpool table with new infos
        } else {
            // check if there's a db schema which is not yet full
            synchronized (ClientAdminThread.create_mutex) {
                String schema_name = getNextUnfilledSchemaFromDB(db.getId(), configCon);
                // there's none? create one
                if (schema_name == null) {
                    configCon.setAutoCommit(false);

                    final int srv_id = IDGenerator.getId(configCon);
                    configCon.commit();
                    configCon.setAutoCommit(true);
                    schema_name = db.getName() + "_" + srv_id;

                    db.setScheme(schema_name);
                    oxu.createDatabase(db);

                    // update contextserver2dbpool table with new infos
                } else {
                    db.setScheme(schema_name);
                    // update contextserver2dbpool table with new infos
                }
            }
        }
        updateContextServer2DbPool(db, configCon, context_id);
    }

    private void updateContextServer2DbPool(final Database db, final Connection configdb_write_con, final int context_id) throws SQLException {
        PreparedStatement pstm = null;
        try {
            pstm = configdb_write_con.prepareStatement("UPDATE " + "context_server2db_pool " + "SET " + "read_db_pool_id = ?," + "write_db_pool_id = ?," + "db_schema = ? " + "WHERE " + "cid = ?");
            pstm.setInt(1, db.getId());
            pstm.setInt(2, db.getId());
            pstm.setString(3, db.getScheme());
            pstm.setInt(4, context_id);
            pstm.executeUpdate();
        } finally {
            try {
                if (pstm != null) {
                    pstm.close();
                }
            } catch (final Exception e) {
                LOG.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
            }
        }
    }

    private String getNextUnfilledSchemaFromDB(final Integer poolId, final Connection con) throws StorageException {
        if (null == poolId) {
            throw new StorageException("pool_id in getNextUnfilledSchemaFromDB must be != null");
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        String found = null;
        int count = -1;
        try {
            stmt = con.prepareStatement("SELECT db_schema,COUNT(db_schema) AS count FROM context_server2db_pool WHERE write_db_pool_id=? GROUP BY db_schema HAVING count<? ORDER BY count");
            stmt.setInt(1, poolId.intValue());
            stmt.setInt(2, this.CONTEXTS_PER_SCHEMA);
            result = stmt.executeQuery();
            final OXToolStorageInterface oxt = OXToolStorageInterface.getInstance();
            while (result.next() && null == found) {
                final String schema = result.getString(1);
                count = result.getInt(2);
                if (oxt.schemaBeingLockedOrNeedsUpdate(poolId.intValue(), schema)) {
                    LOG.debug("schema " + schema + "is locked or updated, trying next one");
                } else {
                    found = schema;
                }
            }
        } catch (final SQLException e) {
            throw new StorageException(e.getMessage(), e);
        } finally {
            closeSQLStuff(result, stmt);
        }
        LOG.debug("count =" + count + " of schema " + found + ", using it for next context");
        return found;
    }

    private void deleteSchemeFromDatabaseIfEmpty(final Connection ox_db_write_con, final Connection configdb_con, final int source_database_id, final String scheme) throws SQLException {
        PreparedStatement stmt = null;
        PreparedStatement dropstmt = null;
        try {
            // check if any context is in scheme X on database Y
            stmt = configdb_con.prepareStatement("SELECT db_schema FROM context_server2db_pool WHERE db_schema = ? AND write_db_pool_id = ?");
            stmt.setString(1, scheme);
            stmt.setInt(2, source_database_id);
            final ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                // no contexts found on this scheme and db, DROP scheme from db
                LOG.debug("NO remaining contexts found in scheme " + scheme + " on pool with id " + source_database_id + "!");
                LOG.debug("NOW dropping scheme " + scheme + " on pool with id " + source_database_id + "!");
                dropstmt = ox_db_write_con.prepareStatement("DROP DATABASE if exists `" + scheme + "`");
                dropstmt.executeUpdate();
                LOG.debug("Scheme " + scheme + " on pool with id " + source_database_id + " dropped successfully!");
            }
            rs.close();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final Exception ex) {
                LOG.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, ex);
            }
            try {
                if (dropstmt != null) {
                    dropstmt.close();
                }
            } catch (final Exception ex) {
                LOG.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, ex);
            }
        }
    }

    private void revokeConfigdbMapping(final TableObject contextserver2dbpool_backup, final Connection configdb_write_con, final int context_id) throws SQLException {
        for (int a = 0; a < contextserver2dbpool_backup.getDataRowCount(); a++) {
            final TableRowObject tro = contextserver2dbpool_backup.getDataRow(a);

            final StringBuilder prep_sql = new StringBuilder();

            prep_sql.append("UPDATE " + contextserver2dbpool_backup.getName() + " SET ");

            Enumeration<String> enumi = tro.getColumnNames();

            // Save the order of the columns in this list, that all values are
            // correct mapped to their fields
            // for later use in prepared_statement
            final ArrayList<String> columns_list = new ArrayList<String>();

            while (enumi.hasMoreElements()) {
                final String column = enumi.nextElement();
                columns_list.add(column);
                prep_sql.append("" + column + "=?,");
            }

            // set up the sql query for the prep statement
            prep_sql.deleteCharAt(prep_sql.length() - 1);

            prep_sql.append(" WHERE cid = ?");

            // now create the statements for each row
            PreparedStatement prep_ins = null;
            try {
                prep_ins = configdb_write_con.prepareStatement(prep_sql.toString());
                enumi = tro.getColumnNames();
                int ins_pos = 1;
                for (int c = 0; c < columns_list.size(); c++) {
                    final TableColumnObject tco = tro.getColumn(columns_list.get(c));
                    prep_ins.setObject(ins_pos, tco.getData(), tco.getType());
                    ins_pos++;
                }
                prep_ins.setInt(ins_pos++, context_id);
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
            // end of test table
        }
    }

    private void enableContextBackAfterError(final Context ctx) throws StorageException {
        LOG.error("Try enabling context " + ctx.getId() + " back again!");
        enable(ctx);
        LOG.error("Context " + ctx.getId() + " enabled back again!");
    }

    private void fillTargetDatabase(final ArrayList<TableObject> sorted_tables, final Connection target_ox_db_con, final Connection ox_db_connection, final Object criteriaMatch) throws PoolException, SQLException {
        // do the inserts for all tables!
        for (int a = 0; a < sorted_tables.size(); a++) {
            TableObject to = sorted_tables.get(a);
            to = getDataForTable(to, ox_db_connection, criteriaMatch);
            if (to.getDataRowCount() > 0) {
                // ok data in table found, copy to db
                for (int i = 0; i < to.getDataRowCount(); i++) {
                    final StringBuilder prep_sql = new StringBuilder();
                    final StringBuilder sb_values = new StringBuilder();

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

    private Vector<TableObject> fetchTableObjects(final Connection ox_db_write_connection) throws SQLException {
        final Vector<TableObject> tableObjects = new Vector<TableObject>();

        // this.dbmetadata = this.dbConnection.getMetaData();
        final DatabaseMetaData db_metadata = ox_db_write_connection.getMetaData();
        // get the tables to check
        // final ResultSet rs2 = this.dbmetadata.getTables(null, null, null,
        // null);
        final ResultSet rs2 = db_metadata.getTables(null, null, null, null);
        TableObject to = null;
        while (rs2.next()) {
            final String table_name = rs2.getString("TABLE_NAME");
            to = new TableObject();
            to.setName(table_name);
            // fetch all columns from table and see if it contains matching
            // column
            // final ResultSet columns_res =
            // this.dbmetadata.getColumns(this.catalogname, null, table_name,
            // null);

            final ResultSet columns_res = db_metadata.getColumns(ox_db_write_connection.getCatalog(), null, table_name, null);

            boolean table_matches = false;
            while (columns_res.next()) {

                final TableColumnObject tco = new TableColumnObject();
                final String column_name = columns_res.getString("COLUMN_NAME");
                tco.setName(column_name);
                tco.setType(columns_res.getInt("DATA_TYPE"));
                tco.setColumnSize(columns_res.getInt("COLUMN_SIZE"));

                // if table has our ciriteria column, we should fetch data from
                // it
                if (column_name.equals(this.selectionCriteria)) {
                    table_matches = true;
                }
                // add column to table
                to.addColumn(tco);
            }
            columns_res.close();
            if (table_matches) {
                tableObjects.add(to);
            }
        }
        LOG.debug("####### Found -> " + tableObjects.size() + " tables");
        return tableObjects;
    }

    private ArrayList<TableObject> sortTableObjects(final Vector<TableObject> tableObjects, final Connection ox_db_write_con) throws SQLException {
        findReferences(tableObjects, ox_db_write_con);
        // thx http://de.wikipedia.org/wiki/Topologische_Sortierung :)
        return sortTablesByForeignKey(tableObjects);
    }

    private ArrayList<TableObject> sortTablesByForeignKey(final Vector<TableObject> tableObjects) {
        final ArrayList<TableObject> nasty_order = new ArrayList<TableObject>();

        final ArrayList<TableObject> unsorted = new ArrayList<TableObject>();
        unsorted.addAll(tableObjects);

        // now sort the table with a topological sort mech :)
        // work with the unsorted vector
        while (unsorted.size() > 0) {
            for (int a = 0; a < unsorted.size(); a++) {
                final TableObject to = unsorted.get(a);
                if (!to.hasCrossReferences()) {
                    // log.error("removing "+to.getName());
                    nasty_order.add(to);
                    // remove object from list and sort the references new
                    removeAndSortNew(unsorted, to);
                    a--;
                }
            }
        }
        // printTables(nasty_order);
        return nasty_order;
    }

    /**
     * Finds references for each table
     */
    private void findReferences(final Vector<TableObject> tableObjects, final Connection ox_db_write_con) throws SQLException {
        final DatabaseMetaData dbmeta = ox_db_write_con.getMetaData();
        final String db_catalog = ox_db_write_con.getCatalog();
        for (int v = 0; v < tableObjects.size(); v++) {
            final TableObject to = tableObjects.get(v);
            // get references from this table to another
            final String table_name = to.getName();
            // ResultSet table_references =
            // dbmetadata.getCrossReference("%",null,table_name,getCatalogName(),null,getCatalogName());
            final ResultSet table_references = dbmeta.getImportedKeys(db_catalog, null, table_name);
            LOG.debug("Table " + table_name + " has pk reference to table-column:");
            while (table_references.next()) {
                final String pk = table_references.getString("PKTABLE_NAME");
                final String pkc = table_references.getString("PKCOLUMN_NAME");
                LOG.debug("--> Table: " + pk + " column ->" + pkc);
                to.addCrossReferenceTable(pk);
                final int pos_in_list = tableListContainsObject(pk, tableObjects);
                if (pos_in_list != -1) {
                    LOG.debug("Found referenced by " + table_name + "<->" + pk + "->" + pkc);
                    final TableObject edit_me = tableObjects.get(pos_in_list);
                    edit_me.addReferencedBy(table_name);
                }
            }
            table_references.close();
        }
    }

    /**
     * remove no more needed element from list and remove the reference to removed element so that a new element exists which has now
     * references.
     */
    private void removeAndSortNew(final ArrayList<TableObject> list, final TableObject to) {
        list.remove(to);
        for (int i = 0; i < list.size(); i++) {
            final TableObject tob = list.get(i);
            tob.removeCrossReferenceTable(to.getName());
        }
    }

    /**
     * Returns -1 if not found else the position in the Vector where the object is located.
     */
    private int tableListContainsObject(final String table_name, final Vector<TableObject> tableObjects) {
        int found_at_position = -1;
        for (int v = 0; v < tableObjects.size(); v++) {
            final TableObject to = tableObjects.get(v);
            if (to.getName().equals(table_name)) {
                found_at_position = v;
            }
        }
        return found_at_position;
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
            LOG.debug("######## " + sb.toString());
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
        try {
            con_write = cache.getConnectionForConfigDB();
            con_write.setAutoCommit(false);
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
        } catch (final SQLException sql) {
            LOG.error("SQL Error", sql);
            try {
                if (null != con_write) {
                    con_write.rollback();
                }
            } catch (final SQLException ec) {
                LOG.error("Error rollback configdb connection", ec);
            }
            throw sql;
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            try {
                if (null != con_write) {
                    con_write.rollback();
                }
            } catch (final SQLException ec) {
                LOG.error("Error rollback configdb connection", ec);
            }
            throw e;
        } finally {
            closePreparedStatement(stmt);
            if (con_write != null) {
                try {
                    cache.pushConnectionForConfigDB(con_write);
                } catch (final PoolException exp) {
                    LOG.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    private void myEnableDisableContext(final int context_id, final boolean enabled, final int reason_id) throws SQLException, PoolException {
        Connection con_write = null;
        PreparedStatement stmt = null;
        try {
            con_write = cache.getConnectionForConfigDB();
            con_write.setAutoCommit(false);
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
        } catch (final SQLException sql) {
            LOG.error("SQL Error", sql);
            try {
                if (null != con_write) {
                    con_write.rollback();
                }
            } catch (final SQLException ec) {
                LOG.error("Error rollback configdb connection", ec);
            }
            throw sql;
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            try {
                if (null != con_write) {
                    con_write.rollback();
                }
            } catch (final SQLException ec) {
                LOG.error("Error rollback configdb connection", ec);
            }
            throw e;
        } finally {
            closePreparedStatement(stmt);
            if (con_write != null) {
                try {
                    cache.pushConnectionForConfigDB(con_write);
                } catch (final PoolException exp) {
                    LOG.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    /**
     * Determine the next database to use depending on database weight factor. Each database should be equal full according to their weight.
     * Additionally check each master for availability.
     *
     * @param con
     * @return Database handle containing information about database
     * @throws SQLException
     * @throws OXContextException
     */
    private Database getNextDBHandleByWeight(final Connection con) throws SQLException, OXContextException {
        List<DatabaseHandle> list = loadDatabases(con);
        int totalUnits = 0;
        int totalWeight = 0;
        for (final DatabaseHandle db : list) {
            totalUnits += db.getCount();
            totalWeight += i(db.getClusterWeight());
        }
        list = removeFull(list);
        if (list.isEmpty()) {
            throw new OXContextException(
                "The new context could not be created. The maximum number of contexts in every database cluster has been reached. Use register-, create- or change database to resolve the problem.");
        }
        Collections.sort(list, Collections.reverseOrder(new DBWeightComparator(totalUnits, totalWeight)));
        final Iterator<DatabaseHandle> iter = list.iterator();
        DatabaseHandle retval = null;
        while (iter.hasNext() && null == retval) {
            final DatabaseHandle db = iter.next();
            final int dbPoolId = i(db.getId());
            try {
                final Connection dbCon = cache.getWRITEConnectionForPoolId(dbPoolId, null);
                cache.pushWRITEConnectionForPoolId(dbPoolId, dbCon);
                retval = db;
            } catch (final PoolException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        if (null == retval) {
            throw new OXContextException("The new context could not be created. All not full databases can not be connected to.");
        }
        return retval;
    }

    private List<DatabaseHandle> removeFull(final List<DatabaseHandle> list) {
        final List<DatabaseHandle> retval = new ArrayList<DatabaseHandle>();
        for (final DatabaseHandle db : list) {
            final int maxUnit = i(db.getMaxUnits());
            if (maxUnit == -1 || (maxUnit != 0 && db.getCount() < maxUnit)) {
                retval.add(db);
            }
        }
        return retval;
    }

    private List<DatabaseHandle> loadDatabases(final Connection con) throws SQLException, OXContextException {
        final List<DatabaseHandle> retval = new ArrayList<DatabaseHandle>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT db_pool_id,url,driver,login,password,name,read_db_pool_id,weight,max_units FROM db_pool JOIN db_cluster ON write_db_pool_id=db_pool_id");
            rs = stmt.executeQuery();
            while (rs.next()) {
                final DatabaseHandle db = new DatabaseHandle();
                int pos = 1;
                db.setId(I(rs.getInt(pos++)));
                db.setUrl(rs.getString(pos++));
                db.setDriver(rs.getString(pos++));
                db.setLogin(rs.getString(pos++));
                db.setPassword(rs.getString(pos++));
                db.setName(rs.getString(pos++));
                final int slaveId = rs.getInt(pos++);
                if (slaveId > 0) {
                    db.setRead_id(I(slaveId));
                }
                db.setClusterWeight(I(rs.getInt(pos++)));
                db.setMaxUnits(I(rs.getInt(pos++)));
                retval.add(db);
            }
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
        for (final DatabaseHandle db : retval) {
            final int db_count = countUnits(db, con);
            db.setCount(db_count);
        }
        return retval;
    }

    /**
     * count the number of contexts (or users) on the given database
     *
     * @param db
     * @param configdb_con
     * @return number of units (contexts/user depending on settings)
     * @throws SQLException
     * @throws OXContextException
     */
    private int countUnits(final DatabaseHandle db, final Connection configdb_con) throws SQLException, OXContextException {
        PreparedStatement ps = null;
        PreparedStatement ppool = null;
        try {
            int count = 0;

            final int pool_id = db.getId();

            if (this.USE_UNIT == UNIT_CONTEXT) {
                ps = configdb_con.prepareStatement("SELECT COUNT(server_id) FROM context_server2db_pool WHERE write_db_pool_id=?");
                ps.setInt(1, pool_id);
                final ResultSet rsi = ps.executeQuery();

                if (!rsi.next()) {
                    throw new OXContextException("Unable to count contextsof db_pool_id=" + pool_id);
                }
                count = rsi.getInt("COUNT(server_id)");
                rsi.close();
                ps.close();
            } else if (this.USE_UNIT == UNIT_USER) {
                ppool = configdb_con.prepareStatement("SELECT db_schema FROM context_server2db_pool WHERE write_db_pool_id=?");
                ppool.setInt(1, pool_id);
                final ResultSet rpool = ppool.executeQuery();
                while (rpool.next()) {
                    final String schema = rpool.getString("db_schema");
                    ResultSet rsi = null;
                    try {
                        Connection rcon = cache.getSimpleSqlConnection(
                            IDNA.toASCII(db.getUrl()) + schema,
                            db.getLogin(),
                            db.getPassword(),
                            db.getDriver());
                        ps = rcon.prepareStatement("SELECT COUNT(id) FROM user");

                        rsi = ps.executeQuery();
                        if (!rsi.next()) {
                            throw new OXContextException("Unable to count users of db_pool_id=" + pool_id);
                        }
                        count += rsi.getInt("COUNT(id)");
                        rcon.close();
                        ps.close();
                        rsi = null;
                        rcon = null;
                    } catch (final ClassNotFoundException e) {
                        LOG.fatal("Error counting users of db pool", e);
                        throw new OXContextException(e.toString());
                    } finally {
                        rsi.close();
                    }
                }
                rpool.close();
                LOG.debug("***** found " + count + " users on " + pool_id);
            } else {
                throw new OXContextException("UNKNOWN UNIT TO COUNT: " + this.USE_UNIT);
            }

            return count;
        } finally {
            closePreparedStatement(ps);
            closePreparedStatement(ppool);
        }
    }

    @Override
    public void changeQuota(final Context ctx, final String module, final long quota, final Credentials auth) throws StorageException {
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
            // Determine if already present
            final boolean exists;
            {
                stmt = con.prepareStatement("SELECT 1 FROM quota_context WHERE cid=? AND module=?");
                stmt.setInt(1, contextId);
                stmt.setString(2, module);
                ResultSet rs = stmt.executeQuery();
                exists = rs.next();
                Databases.closeSQLStuff(rs, stmt);
                stmt = null;
                rs = null;
            }
            // Insert/update row
            if (exists) {
                stmt = con.prepareStatement("UPDATE quota_context SET value=? WHERE cid=? AND module=?");
                stmt.setLong(1, quota <= 0 ? 0 : quota);
                stmt.setInt(2, contextId);
                stmt.setString(3, module);
                stmt.executeUpdate();
            } else {
                stmt = con.prepareStatement("INSERT INTO quota_context (cid, module, value) VALUES (?, ?, ?)");
                stmt.setInt(1, contextId);
                stmt.setString(2, module);
                stmt.setLong(3, quota <= 0 ? 0 : quota);
                stmt.executeUpdate();
            }
            con.commit(); // COMMIT
            rollback = false;
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
            try {
                cache.pushConnectionForContext(contextId, con);
            } catch (PoolException e) {
                LOG.error("Error pushing connection to pool for context " + contextId + "!", e);
            }
        }
    }

    @Override
    public void changeCapabilities(final Context ctx, final Set<String> capsToAdd, final Set<String> capsToRemove, final Credentials auth) throws StorageException {
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
            // Delete existing ones
            if (null != capsToRemove && !capsToRemove.isEmpty()) {
                stmt = con.prepareStatement("DELETE FROM capability_context WHERE cid=? AND cap=?");
                stmt.setInt(1, contextId);
                for (final String cap : capsToRemove) {
                    stmt.setString(2, cap);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                Databases.closeSQLStuff(stmt);
                stmt = null;
            }
            // Insert new ones
            if (null != capsToAdd) {
                capsToAdd.removeAll(existing);
                if (!capsToAdd.isEmpty()) {
                    stmt = con.prepareStatement("INSERT INTO capability_context (cid, cap) VALUES (?, ?)");
                    stmt.setInt(1, contextId);
                    for (final String cap : capsToAdd) {
                        stmt.setString(2, cap);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }
            }
            con.commit(); // COMMIT
            rollback = false;
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
            try {
                cache.pushConnectionForContext(contextId, con);
            } catch (PoolException e) {
                LOG.error("Error pushing connection to pool for context " + contextId + "!", e);
            }
        }
    }

    @Override
    public void change(final Context ctx) throws StorageException {
        final Connection configCon;
        try {
            configCon = cache.getConnectionForConfigDB();
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }
        try {
            configCon.setAutoCommit(false);

            // Change login mappings in configdb
            changeLoginMappingsForContext(ctx, configCon);

            // Change context name in configdb
            changeNameForContext(ctx, configCon);

            // Change quota size in config db
            changeQuotaForContext(ctx, configCon);

            // Change storage data
            changeStorageDataImpl(ctx, configCon);

            // commit changes to db
            configCon.commit();
        } catch (final SQLException e) {
            rollback(configCon);
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final StorageException e) {
            rollback(configCon);
            throw e;
        } finally {
            try {
                cache.pushConnectionForConfigDB(configCon);
            } catch (final PoolException e) {
                LOG.error("Error pushing configdb connection to pool!", e);
            }
        }
        final Connection oxCon;
        try {
            oxCon = cache.getConnectionForContext(i(ctx.getId()));
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }
        try {
            updateDynamicAttributes(oxCon, ctx);
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            try {
                cache.pushConnectionForContext(i(ctx.getId()), oxCon);
            } catch (final PoolException e) {
                LOG.error("SQL Error", e);
            }
        }
        LOG.info("Context " + ctx.getId() + " changed.");
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

    private void changeQuotaForContext(final Context ctx, final Connection configdb_con) throws SQLException {

        // check if max quota is set in context
        if (ctx.getMaxQuota() != null) {

            long quota_max_temp = ctx.getMaxQuota().longValue();

            if (quota_max_temp != -1) {
                quota_max_temp *= Math.pow(2, 20);
            }

            PreparedStatement prep = null;
            try {

                prep = configdb_con.prepareStatement("UPDATE context SET quota_max=? WHERE cid=?");
                prep.setLong(1, quota_max_temp);
                prep.setInt(2, ctx.getId());
                prep.executeUpdate();
                prep.close();

            } finally {
                try {
                    if (prep != null) {
                        prep.close();
                    }
                } catch (final SQLException e) {
                    LOG.error("SQL Error closing statement!", e);
                }
            }

        }

    }

    private void changeLoginMappingsForContext(final Context ctx, final Connection con) throws SQLException, StorageException {
        if (null == ctx.getLoginMappings()) {
            return;
        }
        final Set<String> loginMappings = ctx.getLoginMappings();

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

    private void changeNameForContext(final Context ctx, final Connection configdb_con) throws SQLException {

        PreparedStatement prep = null;

        try {

            // first check if name is set and has a valid name
            if (ctx.getName() != null && ctx.getName().trim().length() > 0) {
                // ok , now check if a context with this name the client wants
                // to change already exists
                // BUT exclude the name of the current context, because this
                // context can of course be renamed to the same
                // name as it had before the update :)

                prep = configdb_con.prepareStatement("SELECT cid FROM context WHERE name = ? AND cid !=?");
                prep.setString(1, ctx.getName().trim());
                prep.setInt(2, ctx.getId().intValue());
                final ResultSet rs = prep.executeQuery();
                if (rs.next()) {
                    // context with the name already exists in the system,
                    final String err_msg = "A context with context name \"" + ctx.getName().trim() + "\" already exists in the system!";
                    LOG.error(err_msg);
                    // throw error
                    throw new SQLException(err_msg);
                }
                rs.close();
                prep.close();

                // if we reach here, update the table
                prep = configdb_con.prepareStatement("UPDATE context SET name = ? where cid = ?");
                prep.setString(1, ctx.getName().trim());
                prep.setInt(2, ctx.getId().intValue());
                prep.executeUpdate();
                prep.close();

            }
        } finally {
            try {
                if (prep != null) {
                    prep.close();
                }
            } catch (final SQLException e) {
                LOG.error("SQL Error closing statement!", e);
            }
        }

    }

    private void changeStorageDataImpl(final Context ctx, final Connection configdb_write_con) throws SQLException, StorageException {

        if (ctx.getFilestoreId() != null) {
            final OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
            final Filestore filestore = oxutil.getFilestore(ctx.getFilestoreId(), false);
            PreparedStatement prep = null;
            final int context_id = ctx.getId();
            try {

                if (filestore.getId() != null && -1 != filestore.getId().intValue()) {
                    prep = configdb_write_con.prepareStatement("UPDATE context SET filestore_id = ? WHERE cid = ?");
                    prep.setInt(1, filestore.getId().intValue());
                    prep.setInt(2, context_id);
                    prep.executeUpdate();
                    prep.close();
                }

                final String filestore_name = ctx.getFilestore_name();
                if (null != filestore_name) {
                    prep = configdb_write_con.prepareStatement("UPDATE context SET filestore_name = ? WHERE cid = ?");
                    prep.setString(1, filestore_name);
                    prep.setInt(2, context_id);
                    prep.executeUpdate();
                    prep.close();
                }

            } finally {
                try {
                    if (prep != null) {
                        prep.close();
                    }
                } catch (final SQLException exp) {
                    LOG.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT);
                }
            }
        }
    }

    private void closeRecordset(final ResultSet rs) {
        if (null != rs) {
            try {
                rs.close();
            } catch (final SQLException e) {
                LOG.error("Error closing recordset", e);
            }
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

    private void pushConnectionToPoolConfigDB(final Connection con) {
        if (con != null) {
            try {
                cache.pushConnectionForConfigDB(con);
            } catch (final PoolException exp) {
                LOG.error("Error pushing configdb connection to pool!", exp);
            }
        }
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
