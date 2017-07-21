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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import static com.openexchange.tools.sql.DBUtils.startTransaction;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import javax.mail.internet.idn.IDNA;
import org.apache.commons.io.FileUtils;
import org.osgi.framework.ServiceException;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.exceptions.TargetDatabaseException;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.SchemaSelectStrategy;
import com.openexchange.admin.rmi.dataobjects.SchemaSelectStrategy.Strategy;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.EnforceableDataObjectException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.OXContextException;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.schemacache.ContextCountPerSchemaClosure;
import com.openexchange.admin.schemacache.DefaultContextCountPerSchemaClosure;
import com.openexchange.admin.schemacache.SchemaCache;
import com.openexchange.admin.schemacache.SchemaCacheFinalize;
import com.openexchange.admin.schemacache.SchemaCacheProvider;
import com.openexchange.admin.schemacache.SchemaCacheResult;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.services.I18nServices;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUserStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.storage.sqlStorage.OXAdminPoolInterface;
import com.openexchange.admin.storage.sqlStorage.OXContextSQLStorage;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.AdminCacheExtended;
import com.openexchange.admin.tools.database.TableColumnObject;
import com.openexchange.admin.tools.database.TableObject;
import com.openexchange.admin.tools.database.TableRowObject;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.database.Assignment;
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
import com.openexchange.quota.groupware.AmountQuotas;
import com.openexchange.threadpool.CompletionFuture;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.file.external.QuotaFileStorages;
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
 * @author cutmasta
 */
public class OXContextMySQLStorage extends OXContextSQLStorage {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXContextMySQLStorage.class);

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
                LOG.warn("unknown unit {}, using context", unit);
            }
        } catch (final OXContextException e) {
            LOG.error("Error init", e);
        }
    }

    private Connection getConnectionForConfigDb() throws StorageException {
        try {
            return cache.getConnectionForConfigDB();
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }
    }

    @Override
    public void delete(final Context ctx) throws StorageException {
        LOG.debug("Fetching connection and scheme for context {}", ctx.getId());
        // groupware context must be loaded before entry from user_setting_admin table is removed.
        com.openexchange.groupware.contexts.Context gwCtx = null;
        try {
            final ContextService service = AdminServiceRegistry.getInstance().getService(ContextService.class, true);
            gwCtx = service.getContext(ctx.getId().intValue());
        } catch (final OXException e) {
            LOG.error("", e);
        } catch (final ServiceException e) {
            LOG.error("", e);
        }

        Connection conForConfigDB = getConnectionForConfigDb();
        boolean rollback = false;
        try {
            // fetch infos for filestore from configdb before deleting on this connection
            final URI storageURI;
            if (null != gwCtx) {
                storageURI = FilestoreStorage.createURI(conForConfigDB, gwCtx);
            } else {
                storageURI = FilestoreStorage.createURI(conForConfigDB, ctx.getId().intValue());
            }
            // Delete filestore directory of the context
            LOG.debug("Starting filestore delete(cid={}) from disc!", ctx.getId());
            boolean simpleDelete = null == gwCtx;
            if (!simpleDelete) {
                try {
                    QuotaFileStorage.getInstance(storageURI, gwCtx).remove();
                } catch (final OXException e) {
                    simpleDelete = true;
                    LOG.error("File storage implementation failed to remove the file storage. Trying to hard-delete the file storage contents.", e);
                }
            }
            if (simpleDelete) {
                if ("file".equalsIgnoreCase(storageURI.getScheme())) {
                    FileUtils.deleteDirectory(new File(storageURI));
                } else {
                    throw new StorageException("Can't hard-delete non-local file store at \"" + storageURI + "\"");
                }
            }
            LOG.debug("Filestore delete(cid={}) from disc finished!", ctx.getId());

            // we need the right connection and scheme for this context
            final int poolId;
            final Connection conForContext;
            try {
                poolId = cache.getDBPoolIdForContextId(ctx.getId().intValue());
                final String scheme = cache.getSchemeForContextId(ctx.getId().intValue());
                conForContext = cache.getWRITENoTimeoutConnectionForPoolId(poolId, scheme);
                LOG.debug("Connection and scheme fetched for context {}", ctx.getId());
            } catch (final PoolException e) {
                LOG.error("Pool Error", e);
                throw new StorageException(e);
            }
            final List<TableObject> sorted_tables;
            try {
                // fetch tables which can contain context data and sort these tables magically by foreign keys
                LOG.debug("Fetching table structure from database scheme for context {}", ctx.getId());
                final List<TableObject> fetchTableObjects = fetchTableObjects(conForContext);
                LOG.debug("Table structure fetched for context {}\nTry to find foreign key dependencies between tables and sort table for context {}", ctx.getId(), ctx.getId());
                // sort the tables by references (foreign keys)
                sorted_tables = sortTableObjects(fetchTableObjects, conForContext);
                LOG.debug("Dependencies found and tables sorted for context {}", ctx.getId());
                // loop through tables and execute delete statements on each table
                deleteContextData(ctx, conForContext, sorted_tables);
            } catch (final SQLException e) {
                LOG.error("SQL Error", e);
                throw new StorageException(e);
            } finally {
                // must be pushed back here, because in the "deleteContextFromConfigDB" the connection is "reset" in the pool.
                // else we would get an not nice error in the logfile from the dbpool
                try {
                    cache.pushWRITENoTimeoutConnectionForPoolId(poolId, conForContext);
                } catch (final PoolException e) {
                    LOG.error("Pool Error", e);
                }
            }

            // Execute delete context from configdb AND the drop database command if this context is the last one
            conForConfigDB.setAutoCommit(false);
            rollback = true;
            contextCommon.deleteContextFromConfigDB(conForConfigDB, ctx.getId().intValue());
            // submit delete to database under any circumstance before the filestore gets deleted.see bug 9947
            conForConfigDB.commit();
            rollback = false;
            // Force to re-initialize schema cache on next access
            SchemaCache schemaCache = SchemaCacheProvider.getInstance().optSchemaCache();
            if (null != schemaCache) {
                schemaCache.clearFor(poolId);
            }
            LOG.info("Context {} deleted.", ctx.getId());
        } catch (final OXException e) {
            LOG.error("", e);
            throw new StorageException(e);
        } catch (final IOException e) {
            LOG.error("", e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            LOG.error("", e);
            throw new StorageException(e);
        } finally {
            if (rollback) {
                rollback(conForConfigDB);
            }
            autocommit(conForConfigDB);
            try {
                cache.pushConnectionForConfigDB(conForConfigDB);
            } catch (final PoolException exp) {
                LOG.error("Pool Error", exp);
            }
        }

        // Invalidate caches
        try {
            final int contextID = ctx.getId().intValue();
            ContextStorage.getInstance().invalidateContext(contextID);
            final CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
            if (null != cacheService) {
                try {
                    Cache cache = cacheService.getCache("MailAccount");
                    cache.clear();
                } catch (final Exception e) {
                    LOG.error("", e);
                }
                try {
                    Cache cache = cacheService.getCache("Capabilities");
                    cache.invalidateGroup(ctx.getId().toString());
                } catch (final Exception e) {
                    LOG.error("", e);
                }
            }
        } catch (final Exception e) {
            LOG.error("Error invalidating context {} in ox context storage", ctx.getId(), e);
        }
    }

    private void deleteContextData(final Context ctx, final Connection con, final List<TableObject> sorted_tables) throws StorageException {
        LOG.debug("Now deleting data for context {}", ctx.getId());
        PreparedStatement dynamicAttrDel = null;
        try {
            con.setAutoCommit(false);
            // first delete everything with OSGi DeleteListener services.
            try {
                final DeleteEvent event = new DeleteEvent(this, ctx.getId().intValue(), DeleteEvent.TYPE_CONTEXT, ctx.getId().intValue());
                DeleteRegistry.getInstance().fireDeleteEvent(event, con, con);
            } catch (final OXException e) {
                LOG.error("Some implementation deleting context specific data failed. Continuing with hard delete from tables using cid column.", e);
            }
            // now go through tables and delete the remainders
            for (int i = sorted_tables.size() - 1; i >= 0; i--) {
                deleteTableData(ctx, con, sorted_tables.get(i));
            }
            // commit groupware data scheme deletes BEFORE database get dropped in "deleteContextFromConfigDB" .see bug #10501
            dynamicAttrDel = con.prepareStatement("DELETE FROM contextAttribute WHERE cid = ?");
            dynamicAttrDel.setInt(1, ctx.getId().intValue());
            dynamicAttrDel.executeUpdate();
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw new StorageException(e);
        } finally {
            closeSQLStuff(dynamicAttrDel);
            autocommit(con);
        }
        LOG.debug("Data delete for context {} completed!", ctx.getId());
    }

    private void deleteTableData(final Context ctx, final Connection con, final TableObject to) throws SQLException {
        LOG.debug("Deleting data from table {} for context {}", to.getName(), ctx.getId());
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
            myLockUnlockAllContexts(false, reason.getId().intValue(), addtionaltable, sqlconjunction);
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
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
        }
    }

    @Override
    public void disable(String schema, MaintenanceReason reason) throws StorageException {
        Connection con = getConnectionForConfigDb();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE context SET enabled = 0, reason_id = ? WHERE cid IN (SELECT cid FROM context_server2db_pool WHERE db_schema = ?) AND enabled = 1");
            stmt.setInt(1, reason.getId());
            stmt.setString(2, schema);

            int numDisabled = stmt.executeUpdate();
            LOG.info("Disabled {} contexts in schema '{}' with reason {}", numDisabled, schema, reason.getId());
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(stmt);
            if (con != null) {
                try {
                    cache.pushConnectionForConfigDB(con);
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
        }
    }

    @Override
    public void enable(String schema, MaintenanceReason reason) throws StorageException {
        Connection con = getConnectionForConfigDb();
        PreparedStatement stmt = null;
        try {
            String query;
            if (reason == null) {
                query = "UPDATE context SET enabled = 1, reason_id = NULL WHERE cid IN (SELECT cid FROM context_server2db_pool WHERE db_schema = ?) AND enabled = 0";
            } else {
                query = "UPDATE context SET enabled = 1, reason_id = NULL WHERE cid IN (SELECT cid FROM context_server2db_pool WHERE db_schema = ?) AND enabled = 0 AND reason_id = ?";
            }

            stmt = con.prepareStatement(query);
            stmt.setString(1, schema);
            if (reason != null) {
                stmt.setInt(2, reason.getId());
            }

            int numEnabled = stmt.executeUpdate();
            LOG.info("Enabled {} contexts in schema '{}' with reason {}", numEnabled, schema, reason == null ? "'all'" : reason.getId());
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(stmt);
            if (con != null) {
                try {
                    cache.pushConnectionForConfigDB(con);
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
            configCon = cache.getConnectionForConfigDB();
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
                    cache.pushConnectionForConfigDB(configCon);
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
        Connection configCon = getConnectionForConfigDb();
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
            final List<TableObject> fetchTableObjects = fetchTableObjects(ox_db_write_con);
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
            configdb_write_con = getConnectionForConfigDb();
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

            // check if scheme is empty after deleting context data on source db
            // if yes, drop whole database
            deleteSchemeFromDatabaseIfEmpty(ox_db_write_con, configdb_write_con, source_database_id, scheme);
            configdb_write_con.commit();
            ox_db_write_con.commit();

            // Force to re-initialize schema cache on next access
            SchemaCache schemaCache = SchemaCacheProvider.getInstance().optSchemaCache();
            if (null != schemaCache) {
                schemaCache.clearFor(source_database_id);
                schemaCache.clearFor(target_database_id.getId().intValue());
            }
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
            double time = (System.currentTimeMillis() - start) / 1000;
            LOG.debug("Data moving for context {} to target database system {} completed in {} seconds!", ctx.getId(), target_database_id, time);
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

        List<ContextSearcher> searchers = new ArrayList<ContextSearcher>();
        searchers.add(new ContextSearcher(cache, "SELECT cid FROM context WHERE name LIKE ?", sqlPattern));
        searchers.add(new ContextSearcher(cache, "SELECT cid FROM login2context WHERE login_info LIKE ?", sqlPattern));
        try {
            Integer.parseInt(sqlPattern);
            searchers.add(new ContextSearcher(cache, "SELECT cid FROM context WHERE cid = ?", sqlPattern));
        } catch (NumberFormatException e) {
            // Ignore and do nothing, because we are not including that query to the searcher
            // since the specified sqlPattern does not solely consists out of numbers.
        }

        final CompletionFuture<Collection<Integer>> completion = threadPoolS.invoke(searchers);
        final Set<Integer> cids = new HashSet<Integer>();
        try {
            for (ContextSearcher searcher : searchers) {
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

        return contextCommon.loadContexts(filteredCids != null ? filteredCids : cids, Long.parseLong(prop.getProp("AVERAGE_CONTEXT_SIZE", "100")), loaders, failOnMissing);
    }

    @Override
    public Context[] searchContextByDatabase(final Database db_host) throws StorageException {
        try {
            // Load context identifiers
            final int[] contextIds = cache.getPool().listContexts(db_host.getId());

            // Load each context's data
            final List<Integer> cids = new ArrayList<Integer>(contextIds.length);
            for (int contextId : contextIds) {
                cids.add(I(contextId));
            }

            return contextCommon.loadContexts(cids, Long.parseLong(prop.getProp("AVERAGE_CONTEXT_SIZE", "100")), null, false);
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }
    }

    @Override
    public List<Integer> getContextIdsBySchema(final String schema) throws StorageException {
        Connection con = getConnectionForConfigDb();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
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
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (con != null) {
                try {
                    cache.pushConnectionForConfigDB(con);
                } catch (PoolException e) {
                    LOG.error("Error pushing configdb connection to pool!", e);
                }
            }
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
            con = getConnectionForConfigDb();

            stmt = con.prepareStatement("SELECT context.cid, context.name, context.enabled, context.reason_id, context.filestore_id, context.filestore_name, context.quota_max, context_server2db_pool.write_db_pool_id, context_server2db_pool.read_db_pool_id, context_server2db_pool.db_schema FROM context LEFT JOIN ( context_server2db_pool, server ) ON ( context.cid = context_server2db_pool.cid AND context_server2db_pool.server_id = server.server_id ) WHERE server.name = ? AND context.filestore_id = ?");
            logininfo = con.prepareStatement("SELECT login_info FROM `login2context` WHERE cid=?");
            final String serverName = AdminServiceRegistry.getInstance().getService(ConfigurationService.class).getProperty(AdminProperties.Prop.SERVER_NAME, "local");
            stmt.setString(1, serverName);
            stmt.setInt(2, filestore.getId().intValue());
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
                    cs.setMaintenanceReason(new MaintenanceReason(Integer.valueOf(reason_id)));
                }
                cs.setFilestoreId(Integer.valueOf(rs.getInt(5))); // filestore_id
                cs.setFilestore_name(rs.getString(6)); // filestorename
                long quota_max = rs.getLong(7); // quota max
                if (quota_max != -1) {
                    quota_max /= Math.pow(2, 20);
                    // set quota max also in context setup object
                    cs.setMaxQuota(Long.valueOf(quota_max));
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
                    boolean hasUserColumn = QuotaFileStorages.hasUserColumn(con, context_id);

                    stmt2 = oxdb_read.prepareStatement(hasUserColumn ? "SELECT filestore_usage.used FROM filestore_usage WHERE filestore_usage.cid = ? AND filestore_usage.user = 0" : "SELECT filestore_usage.used FROM filestore_usage WHERE filestore_usage.cid = ?");
                    stmt2.setInt(1, context_id);
                    rs2 = stmt2.executeQuery();

                    while (rs2.next()) {
                        quota_used = rs2.getLong(1);
                    }
                } finally {
                    Databases.closeSQLStuff(rs2, stmt2);
                    cache.pushConnectionForContextAfterReading(context_id, oxdb_read);
                    oxdb_read = null;
                }

                quota_used /= Math.pow(2, 20);
                // set used quota in context setup
                cs.setUsedQuota(Long.valueOf(quota_used));

                cs.setAverage_size(Long.valueOf(prop.getProp("AVERAGE_CONTEXT_SIZE", "100")));

                // context id
                cs.setId(Integer.valueOf(context_id));

                list.add(cs);
            }

            return list.toArray(new Context[list.size()]);
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final OXException e) {
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
        Connection configdb_write_con = getConnectionForConfigDb();
        PreparedStatement prep = null;
        boolean rollback = false;
        try {
            configdb_write_con.setAutoCommit(false);
            rollback = true;

            changeStorageDataImpl(ctx, configdb_write_con);

            configdb_write_con.commit();
            rollback = false;
        } catch (final DataTruncation dt) {
            LOG.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final SQLException exp) {
            LOG.error("SQL Error", exp);
            throw new StorageException(exp);
        } finally {
            Databases.closeSQLStuff(prep);
            if (rollback) {
                rollback(configdb_write_con);
            }

            autocommit(configdb_write_con);
            try {
                cache.pushConnectionForConfigDB(configdb_write_con);
            } catch (final PoolException ecp) {
                LOG.error("Error pushing configdb connection to pool!", ecp);
            }
        }
    }

    @Override
    public Context create(final Context ctx, final User adminUser, final UserModuleAccess access, SchemaSelectStrategy schemaSelectStrategy) throws StorageException, InvalidDataException, ContextExistsException {
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

        Connection configCon = getConnectionForConfigDb();
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
                throw new StorageException("Unable to create context: " + e.getMessage());
            }

            // Two separate try-catch blocks are necessary because roll-back only works after starting a transaction.
            int contextId = ctx.getId().intValue();
            SchemaCacheFinalize cacheFinalize = null;
            boolean contextCreated = false;
            boolean rollback = false;
            boolean configConCommitted = false;
            boolean automaticStrategyUsed = true;
            try {
                // Start transaction & mark to perform a roll-back if any error occurs
                startTransaction(configCon);
                rollback = true;

                // Set next suitable schema (dependent on strategy) in passed com.openexchange.admin.rmi.dataobjects.Database instance
                SchemaResult schemaResult = findOrCreateSchema(configCon, db, schemaSelectStrategy);
                cacheFinalize = schemaResult.getCacheFinalize();
                automaticStrategyUsed = Strategy.AUTOMATIC == schemaResult.getStrategy();

                // Write other configdb data
                contextCommon.fillContextAndServer2DBPool(ctx, configCon, db);
                contextCommon.fillLogin2ContextTable(ctx, configCon);

                /*-
                 * Continue with context creation depending on utilized schema-select strategy:
                 *
                 *
                 * If AUTOMATIC was used, then write context data _before_ committing the configdb connection
                 *
                 * Otherwise commit the configdb connection and write context data _afterwards_
                 */
                Context retval;
                if (automaticStrategyUsed) {
                    // Write context data before COMMIT
                    retval = writeContext(ctx, adminUser, access);

                    // Commit transaction and unmark to perform a roll-back
                    configCon.commit();
                    rollback = false;
                    configConCommitted = true;
                } else {
                    // Commit transaction
                    configCon.commit();
                    configConCommitted = true;

                    // Write context data after COMMIT and unmark to perform a roll-back
                    retval = writeContext(ctx, adminUser, access);
                    rollback = false;
                }

                // Apparently, no error occurred
                contextCreated = true;

                LOG.info("Context {} created with strategy {}!", retval.getId(), schemaResult.getStrategy().toString());
                return retval;
            } catch (SQLException e) {
                throw new StorageException(e.getMessage(), e);
            } catch (ContextExistsException e) {
                throw e;
            } catch (StorageException e) {
                throw e;
            } finally {
                if (null != cacheFinalize) {
                    try {
                        cacheFinalize.finalize(contextCreated);
                    } catch (Exception x) {
                        LOG.debug("An error occurred while performing a finalisation on context '{}': {}", contextId, x.getMessage(), x);
                    }
                }

                if (rollback) {
                    if (automaticStrategyUsed) {
                        // A commit on configDb connection is guaranteed that is has not been performed
                        rollback(configCon);
                        OXContextMySQLStorageCommon.deleteEmptySchema(i(db.getId()), db.getScheme());
                    } else {
                        // Either commit on configDb connection or writeContext() invocation failed
                        // Attempt to roll-back configDb connection (in case the commit on configDb connection failed)
                        rollback(configCon);

                        // Manually drop possibly already created data from configDb in case configDb connection has already been committed
                        if (configConCommitted) {
                            new OXContextMySQLStorageCommon().handleCreateContextRollback(configCon, contextId);
                        }
                    }
                }

                autocommit(configCon);
            }
        } finally {
            pushConnectionToPoolConfigDB(configCon);
        }
    }

    private Context writeContext(final Context ctx, final User adminUser, final UserModuleAccess access) throws StorageException {
        final int contextId = ctx.getId().intValue();
        try {
            DBUtils.TransactionRollbackCondition condition = new DBUtils.TransactionRollbackCondition(3);
            do {
                Connection oxCon;
                try {
                    oxCon = cache.getConnectionForContext(contextId);
                } catch (PoolException e) {
                    LOG.error("Pool Error", e);
                    throw new StorageException(e);
                }

                condition.resetTransactionRollbackException();
                boolean rollback = false;
                try {
                    DBUtils.startTransaction(oxCon);
                    rollback = true;

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
                } catch (final DataTruncation e) {
                    LOG.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, e);
                    throw AdminCache.parseDataTruncation(e);
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
            } while (retry(condition));
        } catch (final SQLException sql) {
            throw new StorageException(sql.toString(), sql);
        }

        // Cannot occur
        return ctx;
    }

    private boolean retry(DBUtils.TransactionRollbackCondition condition) throws SQLException {
        boolean retry = condition.checkRetry();
        if (retry) {
            int retryCount = condition.getCount();
            long nanosToWait = TimeUnit.NANOSECONDS.convert((retryCount * 1000) + ((long) (Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
        }
        return retry;
    }

    private void updateDynamicAttributes(final Connection oxCon, final Context ctx) throws SQLException {
        int contextId = ctx.getId().intValue();
        PreparedStatement stmtinsertattribute = null;
        PreparedStatement stmtdelattribute = null;
        try {

            if (ctx.isUserAttributesset()) {
                stmtinsertattribute = oxCon.prepareStatement("INSERT INTO contextAttribute (value, cid, name) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE value=?");
                stmtinsertattribute.setInt(2, contextId);

                stmtdelattribute = oxCon.prepareStatement("DELETE FROM contextAttribute WHERE cid=? AND name=?");
                stmtdelattribute.setInt(1, contextId);

                for (final Map.Entry<String, Map<String, String>> ns : ctx.getUserAttributes().entrySet()) {
                    final String namespace = ns.getKey();
                    for (final Map.Entry<String, String> pair : ns.getValue().entrySet()) {
                        final String name = namespace + "/" + pair.getKey();
                        final String value = pair.getValue();
                        if (value != null) {
                            stmtinsertattribute.setString(1, value);
                            stmtinsertattribute.setString(3, name);
                            stmtinsertattribute.setString(4, value);
                            stmtinsertattribute.executeUpdate();
                        } else {
                            stmtdelattribute.setString(2, name);
                            stmtdelattribute.executeUpdate();
                        }
                    }
                }

            }
        } finally {
            Databases.closeSQLStuff(stmtinsertattribute);
            Databases.closeSQLStuff(stmtdelattribute);
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
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = configdb_write.prepareStatement("SELECT url,driver,login,password,name FROM db_pool WHERE db_pool_id = ?");
            pstm.setInt(1, database_id.getId().intValue());
            rs = pstm.executeQuery();
            if (false == rs.next()) {
                throw new StorageException("No such database with identifier " + database_id.getId());
            }

            Database retval = new Database();
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

    /**
     * Looks-up the next suitable schema dependent on given strategy (fall-back is {@link Strategy#AUTOMATIC automatic}).
     *
     * @param configCon a write connection to the configuration database that is already in a transaction.
     * @param db The database
     * @param schemaSelectStrategy The optional strategy to use; may be <code>null</code>
     * @return The possible schema result from cache that is needed for further processing
     */
    private SchemaResult findOrCreateSchema(final Connection configCon, final Database db, SchemaSelectStrategy schemaSelectStrategy) throws StorageException {
        if (CONTEXTS_PER_SCHEMA == 1) {
            // Ignore strategy as there shall be only one schema per context
            int schemaUnique;
            try {
                schemaUnique = IDGenerator.getId(configCon);
            } catch (SQLException e) {
                throw new StorageException(e.getMessage(), e);
            }
            String schemaName = db.getName() + '_' + schemaUnique;
            db.setScheme(schemaName);
            OXUtilStorageInterface.getInstance().createDatabase(db, configCon);
            return SchemaResult.AUTOMATIC;
        }

        // The effective strategy
        SchemaSelectStrategy effectiveStrategy = null == schemaSelectStrategy ? SchemaSelectStrategy.getDefault() : schemaSelectStrategy;

        // Determine the schema name according to effective strategy
        switch (effectiveStrategy.getStrategy()) {
            case SCHEMA:
                // Pre-defined schema name
                applyPredefinedSchemaName(effectiveStrategy.getSchema(), db);
                return SchemaResult.SCHEMA_NAME;
            case IN_MEMORY:
                // Get the schema name advertised by cache
                return inMemoryLookupSchema(configCon, db);
            default:
                automaticLookupSchema(configCon, db);
                return SchemaResult.AUTOMATIC;
        }
    }

    private void applyPredefinedSchemaName(String schemaName, Database db) throws StorageException {
        SchemaCache optCache = SchemaCacheProvider.getInstance().optSchemaCache();
        if (null != optCache) {
            optCache.clearFor(db.getId().intValue());
        }

        // Pre-defined schema name
        db.setScheme(schemaName);
    }

    private SchemaResult inMemoryLookupSchema(Connection configCon, Database db) throws StorageException {
        // Get cache instance
        SchemaCache schemaCache = SchemaCacheProvider.getInstance().getSchemaCache();
        ContextCountPerSchemaClosure closure = new DefaultContextCountPerSchemaClosure(configCon, ClientAdminThread.cache.getPool());

        // Get next known suitable schema
        int poolId = db.getId().intValue();
        SchemaCacheResult schemaResult = schemaCache.getNextSchemaFor(poolId, this.CONTEXTS_PER_SCHEMA, closure);
        if (SchemaCacheResult.DATABASE_EMPTY == schemaResult) {
            // No schema at all... Need to create one anyway
            autoFindOrCreateSchema(configCon, db, true);
            return SchemaResult.AUTOMATIC;
        }
        if (null != schemaResult) {
            db.setScheme(schemaResult.getSchemaName());
            return SchemaResult.inMemoryWith(schemaResult);
        }

        // No suitable schema known to cache. Therefore return null to initiate regular schema look-up/creation
        automaticLookupSchema(configCon, db);
        return SchemaResult.AUTOMATIC;
    }

    private void automaticLookupSchema(Connection configCon, Database db) throws StorageException {
        autoFindOrCreateSchema(configCon, db, false);
    }

    /**
     * Looks-up the next suitable schema.
     *
     * @param configCon The connection to configDb
     * @param db The database to get the schema for
     * @param forceCreate <code>true</code> to enforce schema creation <b>w/o</b> checking if an unfilled one is available; otherwise <code>false</code> to check prior to creation
     * @throws StorageException If a suitable schema cannot be found
     */
    private void autoFindOrCreateSchema(Connection configCon, Database db, boolean forceCreate) throws StorageException {
        // Clear schema cache once "live" schema information is requested
        {
            SchemaCache optCache = SchemaCacheProvider.getInstance().optSchemaCache();
            if (null != optCache) {
                optCache.clearFor(db.getId().intValue());
            }
        }

        // Freshly determine the next schema to use
        String schemaName = forceCreate ? null : getNextUnfilledSchemaFromDB(db.getId(), configCon);
        if (schemaName == null) {
            int schemaUnique;
            try {
                schemaUnique = IDGenerator.getId(configCon);
            } catch (SQLException e) {
                throw new StorageException(e.getMessage(), e);
            }
            schemaName = db.getName() + '_' + schemaUnique;
            db.setScheme(schemaName);
            OXUtilStorageInterface.getInstance().createDatabase(db, configCon);
        } else {
            db.setScheme(schemaName);
        }
    }

    private void createDatabaseAndMappingForContext(Database db, Connection con, int contextId) throws StorageException {
        findOrCreateSchema(con, db, null);
        try {
            updateContextServer2DbPool(db, con, contextId);
        } catch (PoolException e) {
            throw new StorageException(e.getMessage(), e);
        }
    }

    private static void updateContextServer2DbPool(final Database db, Connection con, final int contextId) throws PoolException {
        final int serverId = ClientAdminThread.cache.getServerId();
        ClientAdminThread.cache.getPool().writeAssignment(con, new Assignment() {

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
        OXAdminPoolInterface pool = ClientAdminThread.cache.getPool();
        final String[] unfilledSchemas;
        try {
            pool.lock(con, i(poolId));
            unfilledSchemas = pool.getUnfilledSchemas(con, i(poolId), this.CONTEXTS_PER_SCHEMA);
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

    private void deleteSchemeFromDatabaseIfEmpty(final Connection ox_db_write_con, final Connection configdb_con, final int source_database_id, final String scheme) throws SQLException, StorageException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // check if any context is in scheme X on database Y
            stmt = configdb_con.prepareStatement("SELECT 1 FROM context_server2db_pool WHERE db_schema = ? AND write_db_pool_id = ?");
            stmt.setString(1, scheme);
            stmt.setInt(2, source_database_id);
            rs = stmt.executeQuery();
            boolean empty = (false == rs.next());
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            if (empty) {
                // no contexts found on this scheme and db, DROP scheme from db
                LOG.debug("NO remaining contexts found in scheme {} on pool with id {}!", scheme, source_database_id);
                LOG.debug("NOW dropping scheme {} on pool with id {}!", scheme, source_database_id);

                OXUtilMySQLStorageCommon.deleteDatabase(ox_db_write_con, new Database(source_database_id, scheme), configdb_con);

                LOG.debug("Scheme {} on pool with id {} dropped successfully!", scheme, source_database_id);
            }
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private void enableContextBackAfterError(final Context ctx) throws StorageException {
        LOG.error("Try enabling context {} back again!", ctx.getId());
        enable(ctx);
        LOG.error("Context {} enabled back again!", ctx.getId());
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

    private List<TableObject> fetchTableObjects(final Connection ox_db_write_connection) throws SQLException {
        final List<TableObject> tableObjects = new LinkedList<TableObject>();

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
        LOG.debug("####### Found -> {} tables", tableObjects.size());
        return tableObjects;
    }

    private List<TableObject> sortTableObjects(List<TableObject> fetchTableObjects, Connection ox_db_write_con) throws SQLException {
        findReferences(fetchTableObjects, ox_db_write_con);
        // thx http://de.wikipedia.org/wiki/Topologische_Sortierung :)
        return sortTablesByForeignKey(fetchTableObjects);
    }

    private List<TableObject> sortTablesByForeignKey(List<TableObject> fetchTableObjects) {
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
    private void findReferences(List<TableObject> fetchTableObjects, Connection ox_db_write_con) throws SQLException {
        DatabaseMetaData dbmeta = ox_db_write_con.getMetaData();
        String dbCatalog = ox_db_write_con.getCatalog();
        for (TableObject to : fetchTableObjects) {
            // get references from this table to another
            String tableName = to.getName();
            // ResultSet table_references =
            // dbmetadata.getCrossReference("%",null,table_name,getCatalogName(),null,getCatalogName());
            final ResultSet tableReferences = dbmeta.getImportedKeys(dbCatalog, null, tableName);
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
            tableReferences.close();
        }
    }

    /**
     * remove no more needed element from list and remove the reference to removed element so that a new element exists which has now
     * references.
     */
    private void removeAndSortNew(List<TableObject> unsorted, TableObject to) {
        unsorted.remove(to);
        for (int i = 0; i < unsorted.size(); i++) {
            TableObject tob = unsorted.get(i);
            tob.removeCrossReferenceTable(to.getName());
        }
    }

    /**
     * Returns -1 if not found else the position in the Vector where the object is located.
     */
    private int tableListContainsObject(String table_name, List<TableObject> fetchTableObjects) {
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

    private void myLockUnlockAllContexts(final boolean lock_all, final int reason_id, final String additionaltable, final String sqlconjunction) throws SQLException, StorageException {
        Connection con_write = getConnectionForConfigDb();
        PreparedStatement stmt = null;
        boolean rollback = false;
        try {
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
            Databases.closeSQLStuff(stmt);

            con_write.commit();
            rollback = false;
        } catch (final SQLException sql) {
            LOG.error("SQL Error", sql);
            throw sql;
        } finally {
            closePreparedStatement(stmt);
            if (rollback) {
                rollback(con_write);
            }

            autocommit(con_write);
            if (con_write != null) {
                try {
                    cache.pushConnectionForConfigDB(con_write);
                } catch (final PoolException exp) {
                    LOG.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    private void myEnableDisableContext(final int context_id, final boolean enabled, final int reason_id) throws SQLException, StorageException {
        Connection con_write = getConnectionForConfigDb();
        PreparedStatement stmt = null;
        boolean rollback = false;
        try {
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
            Databases.closeSQLStuff(stmt);
            stmt = null;

            con_write.commit();
            rollback = false;
        } catch (final SQLException sql) {
            LOG.error("SQL Error", sql);
            throw sql;
        } finally {
            Databases.closeSQLStuff(stmt);

            if (rollback) {
                rollback(con_write);
            }

            autocommit(con_write);
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
            throw new OXContextException("The new context could not be created. The maximum number of contexts in every database cluster has been reached. Use register-, create- or change database to resolve the problem.");
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
                LOG.error("", e);
            }
        }
        if (null == retval) {
            throw new OXContextException("The new context could not be created. All not full databases can not be connected to.");
        }
        return retval;
    }

    private List<DatabaseHandle> removeFull(final List<DatabaseHandle> list) {
        final List<DatabaseHandle> retval = new ArrayList<DatabaseHandle>(list.size());
        for (final DatabaseHandle db : list) {
            final int maxUnit = i(db.getMaxUnits());
            if (maxUnit == -1 || (maxUnit != 0 && db.getCount() < maxUnit)) {
                retval.add(db);
            }
        }
        return retval;
    }

    private List<DatabaseHandle> loadDatabases(final Connection con) throws SQLException, OXContextException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (this.USE_UNIT == UNIT_CONTEXT) {
                stmt = con.prepareStatement("SELECT d.db_pool_id,d.url,d.driver,d.login,d.password,d.name,c.read_db_pool_id,c.weight,c.max_units,p.count FROM db_pool AS d JOIN db_cluster AS c ON c.write_db_pool_id=d.db_pool_id LEFT JOIN contexts_per_dbpool AS p ON d.db_pool_id=p.db_pool_id");
                rs = stmt.executeQuery();
                if (false == rs.next()) {
                    // No databases at all...
                    return Collections.emptyList();
                }

                List<DatabaseHandle> databases = new LinkedList<DatabaseHandle>();
                do {
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
                    db.setCount(rs.getInt(pos++));
                    if (rs.wasNull()) {
                        throw new OXContextException("Unable to count contexts of db_pool_id=" + db.getId());
                    }
                    databases.add(db);
                } while (rs.next());
                return databases;
            }

            // Load databases and then determine counters
            stmt = con.prepareStatement("SELECT db_pool_id,url,driver,login,password,name,read_db_pool_id,weight,max_units FROM db_pool JOIN db_cluster ON write_db_pool_id=db_pool_id");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                // No databases at all...
                return Collections.emptyList();
            }

            List<DatabaseHandle> retval = new LinkedList<DatabaseHandle>();
            do {
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
            } while (rs.next());
            DBUtils.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            for (final DatabaseHandle db : retval) {
                final int db_count = countUnits(db, con);
                db.setCount(db_count);
            }

            return retval;
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
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

            final int pool_id = db.getId().intValue();

            if (this.USE_UNIT == UNIT_CONTEXT) {
                ps = configdb_con.prepareStatement("SELECT count FROM contexts_per_dbpool WHERE db_pool_id=?");
                ps.setInt(1, pool_id);
                final ResultSet rsi = ps.executeQuery();

                if (!rsi.next()) {
                    throw new OXContextException("Unable to count contexts of db_pool_id=" + pool_id);
                }
                count = rsi.getInt("count");
                rsi.close();
                ps.close();
            } else if (this.USE_UNIT == UNIT_USER) {
                // FIXME: do not forget this condition!!!
                ppool = configdb_con.prepareStatement("SELECT db_schema FROM context_server2db_pool WHERE write_db_pool_id=?");
                ppool.setInt(1, pool_id);
                final ResultSet rpool = ppool.executeQuery();
                while (rpool.next()) {
                    final String schema = rpool.getString("db_schema");
                    ResultSet rsi = null;
                    try {
                        Connection rcon = AdminCacheExtended.getSimpleSqlConnection(IDNA.toASCII(db.getUrl()) + schema, db.getLogin(), db.getPassword(), db.getDriver());
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
                        LOG.error("Error counting users of db pool", e);
                        throw new OXContextException(e.toString());
                    } finally {
                        if (null != rsi) {
                            try {
                                rsi.close();
                            } catch (Exception e) { /* ignore */
                            }
                        }
                    }
                }
                rpool.close();
                LOG.debug("***** found {} users on {}", count, pool_id);
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
    public void changeQuota(final Context ctx, final List<String> modules, final long quota, final Credentials auth) throws StorageException {
        final int contextId = ctx.getId().intValue();

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
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final PoolException e) {
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
            try {
                cache.pushConnectionForContext(contextId, con);
            } catch (PoolException e) {
                LOG.error("Error pushing connection to pool for context {}!", contextId, e);
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
                    final Serializable key = Integer.valueOf(ctx.getId().intValue());
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
        final Connection configCon = getConnectionForConfigDb();
        boolean rollback = false;
        try {
            configCon.setAutoCommit(false);
            rollback = true;

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
                prep.setInt(2, ctx.getId().intValue());
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
            final Filestore filestore = oxutil.getFilestore(ctx.getFilestoreId().intValue(), false);
            final int context_id = ctx.getId().intValue();

            PreparedStatement prep = null;
            ResultSet rs = null;
            try {

                if (filestore.getId() != null && -1 != filestore.getId().intValue()) {
                    prep = configdb_write_con.prepareStatement("SELECT context.filestore_id FROM context WHERE context.cid = ?");
                    prep.setInt(1, context_id);
                    rs = prep.executeQuery();
                    int oldFilestoreId = rs.next() ? rs.getInt(1) : 0;
                    Databases.closeSQLStuff(rs, prep);
                    rs = null;
                    prep = null;

                    prep = configdb_write_con.prepareStatement("UPDATE context SET filestore_id = ? WHERE cid = ?");
                    prep.setInt(1, filestore.getId().intValue());
                    prep.setInt(2, context_id);
                    prep.executeUpdate();
                    Databases.closeSQLStuff(prep);
                    prep = null;

                    if (oldFilestoreId != 0) {
                        prep = configdb_write_con.prepareStatement("UPDATE contexts_per_filestore SET count=count-1 WHERE filestore_id=?");
                        prep.setInt(1, oldFilestoreId);
                        prep.executeUpdate();
                        Databases.closeSQLStuff(prep);
                        prep = null;
                    }

                    prep = configdb_write_con.prepareStatement("UPDATE contexts_per_filestore SET count=count+1 WHERE filestore_id=?");
                    prep.setInt(1, filestore.getId().intValue());
                    prep.executeUpdate();
                    Databases.closeSQLStuff(prep);
                    prep = null;
                }

                String filestore_name = ctx.getFilestore_name();
                if (null != filestore_name) {
                    prep = configdb_write_con.prepareStatement("UPDATE context SET filestore_name = ? WHERE cid = ?");
                    prep.setString(1, filestore_name);
                    prep.setInt(2, context_id);
                    prep.executeUpdate();
                    Databases.closeSQLStuff(prep);
                    prep = null;
                }

            } finally {
                Databases.closeSQLStuff(rs, prep);
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

    @Override
    public void updateContextReferences(String sourceSchema, String targetSchema, int targetClusterId) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getConnectionForConfigDb();

            // Get the database pool identifier from the specified target cluster identifier
            String getPoolIds = "SELECT read_db_pool_id, write_db_pool_id FROM db_cluster WHERE cluster_id = ?";
            stmt = con.prepareStatement(getPoolIds);
            stmt.setInt(1, targetClusterId);
            stmt.executeQuery();
            rs = stmt.executeQuery();
            final int writeDbPoolId;
            final int readDbPoolId;
            if (rs.next()) {
                readDbPoolId = rs.getInt(1);
                writeDbPoolId = rs.getInt(2);
            } else {
                LOG.error("The specified target cluster id '{}' has no database pool references", targetClusterId);
                throw new StorageException("The specified target cluster id '" + targetClusterId + "' has no database pool references");
            }
            stmt.close();

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
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (con != null) {
                try {
                    cache.pushConnectionForConfigDB(con);
                } catch (PoolException e) {
                    LOG.error("Error pushing configdb connection to pool!", e);
                }
            }
        }
    }

    @Override
    public String createSchema(int targetClusterId) throws StorageException {
        // Get connection to 'configdb'
        Connection configCon = getConnectionForConfigDb();
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
                cache.pushConnectionForConfigDB(configCon);
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
            configCon = cache.getConnectionForConfigDBNoTimeout();
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
                cache.pushConnectionForConfigDBNoTimeout(configCon);
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
            } else {
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;
            }

            // Check count entries for existing ones
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
            // Drop non-existing ones held in count table
            stmt = configCon.prepareStatement("SELECT contexts_per_dbschema.db_pool_id, contexts_per_dbschema.schemaname FROM contexts_per_dbschema LEFT JOIN context_server2db_pool ON contexts_per_dbschema.db_pool_id=context_server2db_pool.write_db_pool_id AND contexts_per_dbschema.schemaname=context_server2db_pool.db_schema WHERE context_server2db_pool.write_db_pool_id IS NULL");
            rs = stmt.executeQuery();
            if (rs.next()) {
                class DbAndSchema {

                    final int dbId;
                    final String schema;

                    DbAndSchema(int dbId, String schema) {
                        super();
                        this.dbId = dbId;
                        this.schema = schema;
                    }
                }

                List<DbAndSchema> ids = new LinkedList<DbAndSchema>();
                do {
                    ids.add(new DbAndSchema(rs.getInt(1), rs.getString(2)));
                } while (rs.next());
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;

                stmt = configCon.prepareStatement("DELETE FROM contexts_per_dbschema WHERE db_pool_id=? AND schemaname=?");
                for (DbAndSchema das : ids) {
                    stmt.setInt(1, das.dbId);
                    stmt.setString(2, das.schema);
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
            stmt = configCon.prepareStatement("SELECT write_db_pool_id FROM db_cluster ORDER BY write_db_pool_id ASC");
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

            stmt = configCon.prepareStatement("INSERT INTO contexts_per_dbschema (db_pool_id, schemaname, count, creating_date) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE count=?");
            long now = System.currentTimeMillis();
            for (Map.Entry<Integer, List<SchemaCount>> entry : counts.entrySet()) {
                int poolId = entry.getKey().intValue();
                List<SchemaCount> schemaCounts = entry.getValue();
                for (SchemaCount schemaCount : schemaCounts) {
                    stmt.setInt(1, poolId);
                    stmt.setString(2, schemaCount.schemaName);
                    stmt.setInt(3, schemaCount.count);
                    stmt.setLong(4, now);
                    stmt.setInt(5, schemaCount.count);
                    stmt.addBatch();
                }
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

}
