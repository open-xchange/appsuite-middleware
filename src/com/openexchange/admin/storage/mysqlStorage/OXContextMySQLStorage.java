
package com.openexchange.admin.storage.mysqlStorage;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.exceptions.DatabaseContextMappingException;
import com.openexchange.admin.exceptions.OXContextException;
import com.openexchange.admin.exceptions.TargetDatabaseException;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.impl.OXUser;
import com.openexchange.admin.storage.interfaces.OXGroupStorageInterface;
import com.openexchange.admin.storage.interfaces.OXResourceStorageInterface;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUserStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.storage.sqlStorage.OXContextSQLStorage;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.database.TableColumnObject;
import com.openexchange.admin.tools.database.TableObject;
import com.openexchange.admin.tools.database.TableRowObject;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.IDGenerator;
import com.openexchange.tools.oxfolder.OXFolderAdminHelper;

/**
 * This class provides the implementation for the storage into a MySQL database
 * 
 * @author d7
 * @auhtor cutmasta
 */
public class OXContextMySQLStorage extends OXContextSQLStorage {

    private class OXContextMySQLStorageCommonPriv extends OXContextMySQLStorageCommon {
        @Override
        protected int getFileStoreID(final Connection configdb_read) throws SQLException, StorageException {
            int return_store_id = 0;
            ResultSet rs = null;
            PreparedStatement stmt = null;
            try {
                long average_size = Long.parseLong(prop.getProp("AVERAGE_CONTEXT_SIZE", "100"));
                average_size *= Math.pow(2, 20);// to byte
                stmt = configdb_read.prepareStatement("SELECT id,size,max_context FROM filestore");

                rs = stmt.executeQuery();
                while (rs.next()) {
                    final int store_max_contexts = rs.getInt("max_context");
                    // don't add contexts if 0
                    if (store_max_contexts == 0) {
                        continue;
                    }

                    final int store_id = rs.getInt("id");
                    final long store_size = rs.getLong("size"); // must be as byte in the db
                    final PreparedStatement pis = configdb_read.prepareStatement("SELECT COUNT(cid) FROM context WHERE filestore_id = ?");
                    pis.setInt(1, store_id);
                    final ResultSet rsi = pis.executeQuery();
                    if (!rsi.next()) {
                        throw new StorageException("Unable to determine usage of filestore=" + store_id);
                    }
                    final Integer store_count = rsi.getInt("COUNT(cid)");

                    rsi.close();
                    pis.close();
                    // don't add if limit reached
                    if (store_count >= store_max_contexts) {
                        continue;
                    }

                    final long used_mb = store_count * average_size; // theoretical
                    // used storage in store
                    final long with_this_context = used_mb + average_size; // theoretical
                    // used storage in store including the new one
                    if (with_this_context <= store_size) {
                        return_store_id = store_id;
                        break;
                    }
                }
                // all stores are set to 0 in max_context(means they should NOT
                // be
                // touched or increased)
                if (return_store_id == 0) {
                    throw new StorageException("No usable or free enough filestore found");
                }
            } catch (final java.lang.NumberFormatException juppes) {
                log.error("Invalid average context size", juppes);
                throw new StorageException("Invalid average context size");
            } finally {
                try {
                    rs.close();
                } catch (final SQLException exp) {
                    log.error("Error closing Resultset", exp);
                }
                try {
                    stmt.close();
                } catch (final Exception e) {
                    log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
                }
            }

            return return_store_id;
        }
    }

    private static final Log log = LogFactory.getLog(OXContextMySQLStorage.class);

    private int CONTEXTS_PER_SCHEMA = 1;

    private static final int UNIT_CONTEXT = 1;

    private static final int UNIT_USER = 2;

    private Connection dbConnection = null;

    private String catalogname = null;

    private String selectionCriteria = null;

    private int criteriaType = -1;

    private Object criteriaMatch = null;

    private Vector<TableObject> tableObjects = null;

    private DatabaseMetaData dbmetadata = null;

    private int USE_UNIT = UNIT_CONTEXT;

    private final OXContextMySQLStorageCommonPriv oxcontextcommon = new OXContextMySQLStorageCommonPriv();

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
                log.warn("unknown unit " + unit + ", using context");
            }
        } catch (final OXContextException e) {
            log.error("Error init", e);
        }
    }

    /**
     * 
     * @throws StorageException
     * @see com.openexchange.admin.storage.sqlStorage.OXContextSQLStorage#deleteContext(int)
     */
    @Override
    public void delete(final Context ctx) throws StorageException {
        Connection write_ox_con = null;
        Connection con_write = null;
        PreparedStatement del_stmt = null;
        final PreparedStatement stmt2 = null;
        final PreparedStatement stmt = null;
        final int context_id = ctx.getIdAsInt();
        try {

            con_write = cache.getWRITEConnectionForCONFIGDB();
            con_write.setAutoCommit(false);

            write_ox_con = cache.getWRITEConnectionForContext(context_id);
            try {
                final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
                // delete all users within this context except admin, admin must
                // be the last user
                final int admin_id = tool.getAdminForContext(ctx, write_ox_con);

                write_ox_con.setAutoCommit(false);

                del_stmt = write_ox_con.prepareStatement("SELECT id FROM user WHERE cid = ? AND id != ?");
                del_stmt.setInt(1, context_id);
                del_stmt.setLong(2, admin_id);
                final ResultSet r22 = del_stmt.executeQuery();

                final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
                final ArrayList<Integer> list = new ArrayList<Integer>();
                while (r22.next()) {
                    list.add(r22.getInt("id"));
                }
                r22.close();
                del_stmt.close();

                final int[] del_ids = new int[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    del_ids[i] = list.get(i);
                }
                log.debug("Deleting users with ids " + del_ids + " from context " + context_id);
                oxu.delete(ctx, del_ids, write_ox_con);

                log.debug("Deleting admin (Id:" + admin_id + ") for context " + context_id);
                oxu.delete(ctx, new int[] { admin_id }, write_ox_con);

                // delete all folder stuff via groupware api
                final OXFolderAdminHelper aa = new OXFolderAdminHelper();
                log.debug("Deleting context folders via OX API ");
                aa.deleteAllContextFolders(context_id, write_ox_con, write_ox_con);

                log.debug("Deleting admin mapping for context " + context_id);
                del_stmt = write_ox_con.prepareStatement("DELETE FROM user_setting_admin WHERE cid = ?");
                del_stmt.setInt(1, context_id);
                del_stmt.executeUpdate();
                del_stmt.close();

                log.debug("Deleting group members for context " + context_id);
                // delete from members
                del_stmt = write_ox_con.prepareStatement("DELETE FROM groups_member WHERE cid = ?");
                del_stmt.setInt(1, context_id);
                del_stmt.executeUpdate();
                del_stmt.close();

                log.debug("Deleting groups for context " + context_id);
                // delete from groups
                del_stmt = write_ox_con.prepareStatement("DELETE FROM groups WHERE cid = ?");
                del_stmt.setInt(1, context_id);
                del_stmt.executeUpdate();
                del_stmt.close();

                log.debug("Deleting resources for context " + context_id);
                // delete from resource
                del_stmt = write_ox_con.prepareStatement("DELETE FROM resource WHERE cid = ?");
                del_stmt.setInt(1, context_id);
                del_stmt.executeUpdate();
                del_stmt.close();

                // delete sequences
                this.oxcontextcommon.deleteSequenceTables(context_id, write_ox_con);

                // call ALL delete methods to delete from del_* tables
                final OXGroupStorageInterface oxgroup = OXGroupStorageInterface.getInstance();
                oxgroup.deleteAllRecoveryData(ctx, write_ox_con);
                final OXUserStorageInterface oxuser = OXUserStorageInterface.getInstance();
                oxuser.deleteAllRecoveryData(ctx, write_ox_con);
                final OXResourceStorageInterface oxres = OXResourceStorageInterface.getInstance();
                oxres.deleteAllRecoveryData(ctx, write_ox_con);

                write_ox_con.commit();
                // } catch (SQLException exp) {
                // log.error("Error processing deleteContext!Rollback starts!",
                // exp);
                // handleContextDeleteRollback(write_ox_con, con_write);
                // throw exp;
                // } catch (PoolException pexp) {
                // log.error("Error processing deleteContext!Rollback starts!",
                // pexp);
                // handleContextDeleteRollback(write_ox_con, con_write);
                // throw pexp;
                // } catch (DBPoolingException pexp2) {
                // log.error("Error processing deleteContext!Rollback starts!",
                // pexp2);
                // handleContextDeleteRollback(write_ox_con, con_write);
                // throw pexp2;
                // } catch (ContextException pexp3) {
                // log.error("Error processing deleteContext!Rollback starts!",
                // pexp3);
                // handleContextDeleteRollback(write_ox_con, con_write);
                // throw pexp3;
                // } catch (DeleteFailedException pexp4) {
                // log.error("Error processing deleteContext!Rollback starts!",
                // pexp4);
                // handleContextDeleteRollback(write_ox_con, con_write);
                // throw pexp4;
                // } catch (LdapException pexp5) {
                // log.error("Error processing deleteContext!Rollback starts!",
                // pexp5);
                // handleContextDeleteRollback(write_ox_con, con_write);
                // throw pexp5;
            } finally {
                try {
                    if (write_ox_con != null) {
                        cache.pushOXDBWrite(context_id, write_ox_con);
                    }
                } catch (final Exception exp) {
                    log.error("Error pushing ox write connection to pool!", exp);
                }
            }

            // execute deletecontextfromconfigdb
            this.oxcontextcommon.deleteContextFromConfigDB(con_write, context_id);

            con_write.commit();
        } catch (final SQLException exp) {
            log.error("SQL Error", exp);
            this.oxcontextcommon.handleContextDeleteRollback(write_ox_con, con_write);
            throw new StorageException(exp);
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            this.oxcontextcommon.handleContextDeleteRollback(write_ox_con, con_write);
            throw new StorageException(e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
            }

            try {
                if (stmt2 != null) {
                    stmt2.close();
                }
            } catch (final SQLException e) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
            }

            try {
                if (del_stmt != null) {
                    del_stmt.close();
                }
            } catch (final SQLException e) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
            }

            try {
                if (con_write != null) {
                    cache.pushConfigDBWrite(con_write);
                }
            } catch (final PoolException exp) {
                log.error("Pool Error", exp);
            }
        }
    }

    /**
     * 
     * @throws StorageException
     * @see com.openexchange.admin.storage.sqlStorage.OXContextSQLStorage#disableAllContexts(int)
     */
    @Override
    public void disableAll(final MaintenanceReason reason) throws StorageException {
        try {
            myLockUnlockAllContexts(false, reason.getId());
        } catch (final SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        }
    }

    /**
     * 
     * @throws StorageException
     * @see com.openexchange.admin.storage.sqlStorage.OXContextSQLStorage#disableContext(int,
     *      int)
     */
    @Override
    public void disable(final Context ctx, final MaintenanceReason reason) throws StorageException {
        try {
            myEnableDisableContext(ctx.getIdAsInt(), false, reason.getId());
        } catch (final SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        }
    }

    /**
     * 
     * @throws StorageException
     * @see com.openexchange.admin.storage.sqlStorage.OXContextSQLStorage#enableAllContexts()
     */
    @Override
    public void enableAll() throws StorageException {
        try {
            myLockUnlockAllContexts(true, 1);
        } catch (final SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        }
    }

    /**
     * 
     * @throws StorageException
     * @see com.openexchange.admin.storage.sqlStorage.OXContextSQLStorage#enableContext(int)
     */
    @Override
    public void enable(final Context ctx) throws StorageException {
        try {
            myEnableDisableContext(ctx.getIdAsInt(), true, -1);
        } catch (final SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        }
    }

    /**
     * 
     * @throws StorageException
     * @see com.openexchange.admin.storage.sqlStorage.OXContextSQLStorage#getContext(int)
     */
    @Override
    public Context getData(final Context ctx) throws StorageException {
        // returns webdav infos, database infos(mapping), context status
        // (disabled,enabled,text)
        Connection config_db_read = null;
        PreparedStatement prep = null;
        try {
            config_db_read = cache.getREADConnectionForCONFIGDB();
            
            return this.oxcontextcommon.getData(ctx, config_db_read,
                    Long.parseLong(prop.getProp("AVERAGE_CONTEXT_SIZE", "100")));
            
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            try {
                if (null != prep) {
                    prep.close();
                }
            } catch (final SQLException ecp) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT);
            }

            try {
                if (null != config_db_read) {
                    cache.pushConfigDBRead(config_db_read);
                }
            } catch (final PoolException exp) {
                log.error("Error pushing configdb connection to pool!", exp);
            }
        }
    }

    /**
     * 
     * @throws StorageException
     * @see com.openexchange.admin.storage.sqlStorage.OXContextSQLStorage#moveDatabaseContext(int,
     *      Database, int)
     */
    @Override
    public void moveDatabaseContext(final Context ctx, final Database target_database_id, final MaintenanceReason reason) throws StorageException {
        long start = 0;
        long end = 0;
        if (log.isDebugEnabled()) {
            start = System.currentTimeMillis();
        }
        log.debug("Move of data for context " + ctx.getIdAsInt() + " is now starting to target database " + target_database_id + "!");

        Connection ox_db_write_con = null;
        Connection configdb_write_con = null;

        PreparedStatement stm = null;

        Connection target_ox_db_con = null;

        TableObject contextserver2dbpool_backup = null;
        int source_database_id = -1;

        try {
            configdb_write_con = cache.getWRITEConnectionForCONFIGDB();
            // ox_db_write_con = cache.getWRITEConnectionForContext(context_id);
            source_database_id = cache.getDBPoolIdForContextId(ctx.getIdAsInt());
            final String scheme = cache.getSchemeForContextId(ctx.getIdAsInt());

            ox_db_write_con = cache.getWRITEConnectionForPoolId(source_database_id, scheme);

            /*
             * 1. Lock the context if not already locked. if already locked,
             * throw exception cause the context could be already in progress
             * for moving.
             */
            log.debug("Context " + ctx.getIdAsInt() + " will now be disabled for moving!");
            disable(ctx, reason);
            log.debug("Context " + ctx.getIdAsInt() + " is now disabled!");

            this.dbConnection = ox_db_write_con;
            this.catalogname = ox_db_write_con.getCatalog();
            this.selectionCriteria = "cid";
            this.criteriaType = Types.INTEGER;
            this.criteriaMatch = ctx.getIdAsInt();

            /*
             * 2. Fetch tables with cid column which could perhaps store data
             * relevant for us
             */
            log.debug("Fetching table structure from database scheme!");
            fetchTableObjects();
            // ####### ##### geht hier was kaputt -> enableContext(); ########
            log.debug("Table structure fetched!");

            // this must sort the tables by references (foreign keys)
            log.debug("Try to find foreign key dependencies between tables and sort table!");
            final ArrayList<TableObject> sorted_tables = sortTableObjects();
            // ####### ##### geht hier was kaputt -> enableContext(); ########
            log.debug("Dependencies found and tables sorted!");

            // fetch data for db handle to create database
            log.debug("Get database handle information for target database system!");
            final Database db_handle = getDatabaseHandleById(target_database_id, configdb_write_con);
            // ####### ##### geht hier was kaputt -> enableContext(); ########
            log.debug("Database handle information found!");

            // backup old mapping in contextserver2dbpool for recovery if
            // something breaks
            log.debug("Backing up current configdb entries for context " + ctx.getIdAsInt());
            contextserver2dbpool_backup = backupContextServer2DBPoolEntry(ctx.getIdAsInt(), configdb_write_con);
            // ####### ##### geht hier was kaputt -> enableContext(); ########
            log.debug("Backup complete!");

            // create database or use existing database AND update the mapping
            // in contextserver2dbpool
            try {
                log.debug("Creating new scheme or using existing scheme on target database system!");
                createDatabaseAndMappingForContext(db_handle, configdb_write_con, ctx.getIdAsInt());
                cache.resetPoolMappingForContext(ctx.getIdAsInt());
                log.debug("Scheme found and mapping in configdb changed to new target database system!");
            } catch (final RemoteException rem) {
                log.error("Remote Error", rem);
                throw new DatabaseContextMappingException("" + rem.getMessage());
            } catch (final SQLException sqle) {
                log.error("SQL Error", sqle);
                throw new DatabaseContextMappingException("" + sqle.getMessage());
            } catch (final PoolException poolex) {
                log.error("Pool Error", poolex);
                throw new DatabaseContextMappingException("" + poolex.getMessage());
            }

            // now insert all data to target db
            log.debug("Now filling target database system " + target_database_id + " with data of context " + ctx.getIdAsInt() + "!");
            try {
                target_ox_db_con = cache.getWRITEConnectionForContext(ctx.getIdAsInt());
                target_ox_db_con.setAutoCommit(false);
                fillTargetDatabase(sorted_tables, target_ox_db_con);
                // commit ALL tables with all data of every row
                target_ox_db_con.commit();
            } catch (final SQLException sql) {
                log.error("SQL Error", sql);
                throw new TargetDatabaseException("" + sql.getMessage());
            } catch (final PoolException pexp) {
                log.error("Pool Error", pexp);
                throw new TargetDatabaseException("" + pexp.getMessage());
            }

            log.debug("Filling completed for target database system " + target_database_id + " with data of context " + ctx.getIdAsInt() + "!");

            // now delete from old database schema all the data
            // For delete from database we loop recursive
            ox_db_write_con.setAutoCommit(false);
            log.debug("Now deleting data for context " + ctx.getIdAsInt() + " from old scheme!");
            for (int a = sorted_tables.size() - 1; a >= 0; a--) {
                final TableObject to = sorted_tables.get(a);
                stm = ox_db_write_con.prepareStatement("DELETE FROM " + to.getName() + " WHERE cid = ?");
                stm.setInt(1, ctx.getIdAsInt());
                log.debug("Deleting data from table \"" + to.getName() + "\" for context " + ctx.getIdAsInt());
                stm.executeUpdate();
                stm.close();
            }
            log.debug("Data delete for context " + ctx.getIdAsInt() + " completed!");

            // check if scheme is empty after deleting context data on source db
            // if yes, drop whole database
            deleteSchemeFromDatabaseIfEmpty(ox_db_write_con, configdb_write_con, source_database_id, scheme);
            ox_db_write_con.commit();

            // all this was ok , then enable context back again
            log.debug("Enabling context " + ctx.getIdAsInt() + " back again!");
            enable(ctx);
        } catch (final DatabaseContextMappingException dcme) {
            log.error("Exception caught while updating mapping in configdb", dcme);
            // revoke contextserver2dbpool()
            try {
                log.error("Now revoking entries in configdb (cs2dbpool) for context " + ctx.getIdAsInt());
                revokeConfigdbMapping(contextserver2dbpool_backup, configdb_write_con, ctx.getIdAsInt());
                cache.resetPoolMappingForContext(ctx.getIdAsInt());
            } catch (final Exception ecp) {
                log.fatal("!!!!!!WARNING!!!!! Could not revoke configdb entries for " + ctx.getIdAsInt() + "!!!!!!WARNING!!! INFORM ADMINISTRATOR!!!!!!", ecp);
            }

            // enableContext() back

            enableContextBackAfterError(ctx);

            throw new StorageException(dcme);
        } catch (final TargetDatabaseException tde) {
            log.error("Exception caught while moving data for context " + ctx.getIdAsInt() + " to target database " + target_database_id, tde);
            log.error("Target database rollback starts for context " + ctx.getIdAsInt());
            // rollback insert on target db
            if (target_ox_db_con != null) {
                try {
                    target_ox_db_con.rollback();
                    log.error("Target database rollback finished for context " + ctx.getIdAsInt());
                } catch (final SQLException ecp) {
                    log.error("Error rollback on target database", ecp);
                }
            }

            // revoke contextserver2dbpool()
            try {
                log.error("Now revoking entries in configdb (cs2dbpool) for context " + ctx.getIdAsInt());
                revokeConfigdbMapping(contextserver2dbpool_backup, configdb_write_con, ctx.getIdAsInt());
                cache.resetPoolMappingForContext(ctx.getIdAsInt());
            } catch (final SQLException ecp) {
                log.fatal("!!!!!!WARNING!!!!! Could not revoke configdb entries for " + ctx.getIdAsInt() + "!!!!!!WARNING!!! INFORM ADMINISTRATOR!!!!!!", ecp);
            } catch (final PoolException ecp) {
                log.fatal("!!!!!!WARNING!!!!! Could not revoke configdb entries for " + ctx.getIdAsInt() + "!!!!!!WARNING!!! INFORM ADMINISTRATOR!!!!!!", ecp);
            }

            // enableContext() back
            enableContextBackAfterError(ctx);

            throw new StorageException(tde);
        } catch (final SQLException sql) {
            // enableContext back
            log.error("SQL Error caught while moving data " + "for context " + ctx.getIdAsInt() + " to target database " + target_database_id, sql);
            enable(ctx);

            // rollback
            if (ox_db_write_con != null) {
                try {
                    ox_db_write_con.rollback();
                } catch (final SQLException ecp) {
                    log.error("Error rollback connection", ecp);
                }
            }

            // rollback
            if (configdb_write_con != null) {
                try {
                    configdb_write_con.rollback();
                } catch (final Exception ecp) {
                    log.error("Error rollback connection", ecp);
                }
            }

            throw new StorageException(sql);
        } catch (final PoolException pexp) {
            log.error("Pool exception caught!", pexp);

            // rollback
            if (null != ox_db_write_con) {
                try {
                    ox_db_write_con.rollback();
                } catch (final SQLException ecp) {
                    log.error("Error rollback connection", ecp);
                }
            }

            // rollback
            try {
                if (null != configdb_write_con && !configdb_write_con.getAutoCommit()) {
                    try {
                        configdb_write_con.rollback();
                    } catch (final SQLException ecp) {
                        log.error("Error rollback connection", ecp);
                    }
                }
            } catch (final SQLException e) {
                log.error("SQL Error", e);
                e.initCause(pexp);
                throw new StorageException(e);
            }

            enableContextBackAfterError(ctx);
            throw new StorageException(pexp);
        } finally {
            if (ox_db_write_con != null) {
                try {
                    cache.pushWRITEConnectionForPoolId(source_database_id, ox_db_write_con);
                } catch (final Exception ex) {
                    log.error("Error pushing connection", ex);
                }
            }
            if (configdb_write_con != null) {
                try {
                    cache.pushConfigDBWrite(configdb_write_con);
                } catch (final Exception ex) {
                    log.error("Error pushing connection", ex);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (final Exception ex) {
                    log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, ex);
                }
            }
            if (target_ox_db_con != null) {
                try {
                    cache.pushWRITEConnectionForPoolId(target_database_id.getId(), target_ox_db_con);
                } catch (final Exception ex) {
                    log.error("Error pushing connection", ex);
                }
            }
        }

        if (log.isDebugEnabled()) {
            end = System.currentTimeMillis();
            double time_ = end - start;
            time_ = time_ / 1000;
            log.debug("Data moving for context " + ctx.getIdAsInt() + " to target database system " + target_database_id + " completed in " + time_ + " seconds!");
        }
    }

    @Override
    public String moveContextFilestore(final Context ctx, final Filestore dst_filestore_id, final MaintenanceReason reason) throws StorageException {
        return null;
    }

    /**
     * 
     * @see com.openexchange.admin.storage.OXContextSQLStorage#searchContext(java.lang.String)
     */
    @Override
    public Context[] searchContext(final String search_pattern) throws StorageException {
        Connection configdb_read = null;
        PreparedStatement stmt = null;
        try {
            configdb_read = cache.getREADConnectionForCONFIGDB();

            final String search_patterntmp = search_pattern.replace('*', '%');
            stmt = configdb_read.prepareStatement("SELECT context_server2db_pool.cid FROM context_server2db_pool INNER JOIN (server, context) ON (context_server2db_pool.server_id=server.server_id AND server.name=? AND context.cid=context_server2db_pool.cid) WHERE  context.name LIKE ? OR context.cid LIKE ?");
            stmt.setString(1, prop.getProp(AdminProperties.Prop.SERVER_NAME, "local"));
            stmt.setString(2, search_patterntmp);
            stmt.setString(3, search_patterntmp);
            final ResultSet rs = stmt.executeQuery();

            final ArrayList<Context> list = new ArrayList<Context>();

            while (rs.next()) {
                Context cs = new Context();
                final int cid = rs.getInt("context_server2db_pool.cid");
                cs.setID(cid);
                cs = this.oxcontextcommon.getData(cs, configdb_read,
                        Long.parseLong(prop.getProp("AVERAGE_CONTEXT_SIZE", "100")));
                
                list.add(cs);
            }
            rs.close();
            stmt.close();

            return list.toArray(new Context[list.size()]);
        } catch (final SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
            }
            try {
                if (configdb_read != null) {
                    cache.pushConfigDBRead(configdb_read);
                }
            } catch (final PoolException exp) {
                log.error("Error pushing configdb connection to pool!", exp);
            }
        }
    }

    /**
     * 
     * @see com.openexchange.admin.storage.OXContextSQLStorage#searchContextByDatabase(java.lang.String)
     */
    @Override
    public Context[] searchContextByDatabase(final Database db_host) throws StorageException {
        Connection con = null;
        // maybe we should make the search pattern configurable
        PreparedStatement stmt = null;

        try {
            con = cache.getREADConnectionForCONFIGDB();
            stmt = con.prepareStatement("SELECT context_server2db_pool.cid FROM context_server2db_pool INNER JOIN (server, db_pool) ON (context_server2db_pool.server_id=server.server_id AND db_pool.db_pool_id=context_server2db_pool.read_db_pool_id OR context_server2db_pool.write_db_pool_id=db_pool.db_pool_id) WHERE server.name=? AND db_pool.url LIKE ?");
            stmt.setString(1, prop.getProp(AdminProperties.Prop.SERVER_NAME, "local"));
            stmt.setString(2, db_host.getUrl());
            final ResultSet rs = stmt.executeQuery();
            final ArrayList<Context> list = new ArrayList<Context>();
            while (rs.next()) {
                list.add(new Context(rs.getInt("cid")));
            }
            rs.close();
            stmt.close();
            return list.toArray(new Context[list.size()]);
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
            }
            if (con != null) {
                try {
                    cache.pushConfigDBRead(con);
                } catch (final PoolException exp) {
                    log.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    /**
     * 
     * @see com.openexchange.admin.storage.OXContextSQLStorage#searchContextByFilestore(java.lang.String)
     */
    @Override
    public Context[] searchContextByFilestore(final Filestore filestore) throws StorageException {
        Connection con = null;
        // maybe we should make the search pattern configurable
        // filestore_url = filestore_url.replace('*','%');
        PreparedStatement stmt = null;
        try {
            con = cache.getREADConnectionForCONFIGDB();
            stmt = con.prepareStatement("SELECT context_server2db_pool.cid FROM context_server2db_pool INNER JOIN (server, context, filestore) ON (context_server2db_pool.server_id=server.server_id AND context_server2db_pool.cid=context.cid AND context.filestore_id=filestore.id) WHERE server.name=? AND filestore.uri LIKE ?");
            stmt.setString(1, prop.getProp(AdminProperties.Prop.SERVER_NAME, "local"));
            stmt.setString(2, filestore.getUrl());
            final ResultSet rs = stmt.executeQuery();
            final ArrayList<Context> list = new ArrayList<Context>();
            while (rs.next()) {
                list.add(new Context(rs.getInt("cid")));
            }
            rs.close();
            stmt.close();

            return list.toArray(new Context[list.size()]);
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
            }
            if (con != null) {
                try {
                    cache.pushConfigDBRead(con);
                } catch (final PoolException exp) {
                    log.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }
    
    @Override
    public Context[] searchContextByFilestoreId(final Filestore filestore) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = cache.getREADConnectionForCONFIGDB();
            stmt = con.prepareStatement("SELECT context_server2db_pool.cid FROM context_server2db_pool INNER JOIN (server, context, filestore) ON (context_server2db_pool.server_id=server.server_id AND context_server2db_pool.cid=context.cid AND context.filestore_id=filestore.id) WHERE server.name=? AND filestore.id=?");
            stmt.setString(1, prop.getProp(AdminProperties.Prop.SERVER_NAME, "local"));
            stmt.setInt(2, filestore.getId());
            final ResultSet rs = stmt.executeQuery();
            final ArrayList<Context> list = new ArrayList<Context>();
            while (rs.next()) {
                list.add(new Context(rs.getInt("cid")));
            }
            rs.close();
            stmt.close();
    
            return list.toArray(new Context[list.size()]);
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
            }
            if (con != null) {
                try {
                    cache.pushConfigDBRead(con);
                } catch (final PoolException exp) {
                    log.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    private void changeStorageDataImpl(Context ctx,Connection configdb_write_con) throws SQLException, StorageException{
        
        if(ctx.getFilestoreId()!=null){
            final OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
            Filestore filestore = oxutil.getFilestore(ctx.getFilestoreId());
            PreparedStatement prep = null;
            final int context_id = ctx.getIdAsInt();
            try {

                if (filestore.getId()!=null && -1 != filestore.getId().intValue()) {
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


                if (ctx.getMaxQuota()!=null && ctx.getMaxQuota()!=-1) {
                    final long filestore_quota_max = ctx.getMaxQuota();
                    // convert to byte for db
                    final long quota_max_in_byte = (long) (filestore_quota_max * Math.pow(2, 20));
                    prep = configdb_write_con.prepareStatement("UPDATE context SET quota_max = ? WHERE cid = ?");
                    prep.setLong(1, quota_max_in_byte);
                    prep.setInt(2, context_id);
                    prep.executeUpdate();
                    prep.close();
                }


            }finally{
                try {
                    if (prep != null) {
                        prep.close();
                    }
                } catch (final SQLException exp) {
                    log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT);
                }
            }
        }
    }

    public void changeStorageData(final Context ctx) throws StorageException {
        Connection configdb_write_con = null;
        PreparedStatement prep = null;      
        try {
            configdb_write_con = cache.getWRITEConnectionForCONFIGDB();
            configdb_write_con.setAutoCommit(false);
            
            changeStorageDataImpl(ctx, configdb_write_con);

            configdb_write_con.commit();
        }catch (final DataTruncation dt){
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final SQLException exp) {
            log.error("SQL Error", exp);
            try {
                if (configdb_write_con != null && !configdb_write_con.getAutoCommit()) {
                    configdb_write_con.rollback();
                }
            } catch (final SQLException expd) {
                log.error("Error processing rollback of connection!", expd);
            }
            throw new StorageException(exp);
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            try {
                if (configdb_write_con != null && !configdb_write_con.getAutoCommit()) {
                    configdb_write_con.rollback();
                }
            } catch (final SQLException expd) {
                log.error("Error processing rollback of connection!", expd);
            }
            throw new StorageException(e);
        } finally {
            try {
                if (prep != null) {
                    prep.close();
                }
            } catch (final SQLException exp) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT);
            }
            try {
                if (configdb_write_con != null) {
                    cache.pushConfigDBWrite(configdb_write_con);
                }
            } catch (final PoolException ecp) {
                log.error("Error pushing configdb connection to pool!", ecp);
            }
        }
    }

    public Context create(final Context ctx, final User admin_user) throws StorageException, InvalidDataException {
        Connection configdb_write_con = null;
        Connection ox_write_con = null;
        final int context_id = ctx.getIdAsInt();
        try {
            if (admin_user != null) {
                final OXUtilStorageInterface oxu = OXUtilStorageInterface.getInstance();
                // Get config_db/ox_db connection from pool
                configdb_write_con = cache.getWRITEConnectionForCONFIGDB();

                final Database db = getNextDBHandleByWeight(configdb_write_con);

                // dbid is the id in db_pool of database engine to use for next
                // context
                final Integer dbid = db.getId();

                boolean newSchemaCreated = false;
                if (this.CONTEXTS_PER_SCHEMA == 1) {
                    // synchronized (ClientAdminThread.create_mutex) {
                    // FIXME: generate unique schema name
                    String schema_name;
                    synchronized (ClientAdminThread.create_mutex) {
                        configdb_write_con.setAutoCommit(false);
                        final int srv_id = IDGenerator.getId(configdb_write_con);
                        configdb_write_con.commit();
                        schema_name = db.getName() + '_' + srv_id;
                    }

                    db.setScheme(schema_name);
                    oxu.createDatabase(db);
                    newSchemaCreated = true;

                    this.oxcontextcommon.fillContextAndServer2DBPool(ctx, configdb_write_con , db);
                    // }
                } else {
                    // check if there's a db schema which is not yet full
                    synchronized (ClientAdminThread.create_mutex) {
                        String schema_name = getNextUnfilledSchemaFromDB(dbid, configdb_write_con);
                        // there's none? create one
                        if (schema_name == null) {
                            configdb_write_con.setAutoCommit(false);
                            final int srv_id = IDGenerator.getId(configdb_write_con);
                            configdb_write_con.commit();
                            schema_name = db.getName() + '_' + srv_id;
                            db.setScheme(schema_name);
                            oxu.createDatabase(db);
                            newSchemaCreated = true;
                            this.oxcontextcommon.fillContextAndServer2DBPool(ctx, configdb_write_con, db);
                        } else {
                            db.setScheme(schema_name);
                            this.oxcontextcommon.fillContextAndServer2DBPool(ctx, configdb_write_con, db);
                        }
                    }
                }
                configdb_write_con.setAutoCommit(false);
                // create login2context mapping in configdb
                fillLogin2ContextTable(ctx, configdb_write_con);
                configdb_write_con.commit();

                ox_write_con = cache.getWRITEConnectionForContext(context_id);
                ox_write_con.setAutoCommit(false);

                this.oxcontextcommon.initSequenceTables(context_id, ox_write_con); // perhaps
                                                                        // the
                // seqs must be
                // deleted on
                // exception
                ox_write_con.commit();

                if (newSchemaCreated) {
                    this.oxcontextcommon.initVersionTable(context_id, ox_write_con);
                    ox_write_con.commit();
                }

                // must be fetched before any other actions, else all statements
                // are commited on this con
                final int group_id = IDGenerator.getId(context_id, com.openexchange.groupware.Types.PRINCIPAL, ox_write_con);
                ox_write_con.commit();

                final int internal_user_id_for_admin = IDGenerator.getId(context_id, com.openexchange.groupware.Types.PRINCIPAL, ox_write_con);
                ox_write_con.commit();

                final int contact_id_for_admin = IDGenerator.getId(context_id, com.openexchange.groupware.Types.CONTACT, ox_write_con);
                ox_write_con.commit();

                int uid_number = -1;
                if (Integer.parseInt(prop.getUserProp(AdminProperties.User.UID_NUMBER_START, "-1")) > 0) {
                    uid_number = IDGenerator.getId(context_id, com.openexchange.groupware.Types.UID_NUMBER, ox_write_con);
                    ox_write_con.commit();
                }

                int gid_number = -1;
                if (Integer.parseInt(prop.getGroupProp(AdminProperties.Group.GID_NUMBER_START, "-1")) > 0) {
                    gid_number = IDGenerator.getId(context_id, com.openexchange.groupware.Types.GID_NUMBER, ox_write_con);
                    ox_write_con.commit();
                }

                // create group users for context
                // get display name for context default group resolved via
                // admins language
                final Locale langus = OXUser.getLanguage(admin_user);
                final String lang = langus.getLanguage() + "_" + langus.getCountry();

                final String def_group_disp_name = prop.getGroupProp("DEFAULT_CONTEXT_GROUP_" + lang.toUpperCase(), "Users");
                this.oxcontextcommon.createStandardGroupForContext(context_id, ox_write_con, def_group_disp_name, group_id, gid_number);

                final UserModuleAccess access = new UserModuleAccess();
                // webmail package access per default
                access.disableAll();
                access.setWebmail(true);
                access.setContacts(true);
                this.oxcontextcommon.createAdminForContext(ctx, admin_user, ox_write_con, internal_user_id_for_admin, contact_id_for_admin, uid_number, access);
                // create system folder for context
                // get lang and displayname of admin
                String display = String.valueOf(admin_user.getId());
                final String displayname = admin_user.getDisplay_name();
                if (null != displayname) {
                    display = displayname;
                } else {
                    final String givenname = admin_user.getGiven_name();
                    final String surname = admin_user.getSur_name();
                    if (null != givenname) {
                        // SET THE DISPLAYNAME AS NEEDED BY CUSTOMER, SHOULD BE
                        // DEFINED ON SERVER SIDE
                        display = givenname + " " + surname;

                    } else {
                        display = surname;
                    }
                    admin_user.setDisplay_name(display);
                }

                final OXFolderAdminHelper oxa = new OXFolderAdminHelper();
                oxa.addContextSystemFolders(context_id, display, lang, ox_write_con);

                ox_write_con.commit();

                // context created
                log.info("Context " + context_id + " created!");
            } // end if admin_user
            // TODO: cutmasta call setters and fill all required fields
            ctx.setEnabled(true);
            return ctx;
        }catch (final DataTruncation dt){
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            this.oxcontextcommon.handleCreateContextRollback(configdb_write_con, ox_write_con, context_id);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final OXException oxae) {
            log.error("Error", oxae);
            this.oxcontextcommon.handleCreateContextRollback(configdb_write_con, ox_write_con, context_id);
            throw new StorageException(oxae);
        } catch (final StorageException ste) {
            log.error("Storage Error", ste);
            this.oxcontextcommon.handleCreateContextRollback(configdb_write_con, ox_write_con, context_id);
            throw ste;
        } catch (final SQLException ecop) {
            log.error("SQL Error", ecop);
            this.oxcontextcommon.handleCreateContextRollback(configdb_write_con, ox_write_con, context_id);
            throw new StorageException(ecop);
            // } catch (OXException ecop4) {
            // log.error("Error processing createContext!Rollback starts!",
            // ecop4);
            // handleCreateContextRollback(configdb_write_con, ox_write_con,
            // context_id);
            // throw ecop4;
            // } catch (RemoteException ecop5) {
            // log.error("Error processing createContext!Rollback starts!",
            // ecop5);
            // handleCreateContextRollback(configdb_write_con, ox_write_con,
            // context_id);
            // throw ecop5;
            // } catch (DBPoolingException ecop6) {
            // log.error("Error processing createContext!Rollback starts!",
            // ecop6);
            // handleCreateContextRollback(configdb_write_con, ox_write_con,
            // context_id);
            // throw ecop6;
            // } catch (NoSuchAlgorithmException ecop7) {
            // log.error("Error processing createContext!Rollback starts!",
            // ecop7);
            // handleCreateContextRollback(configdb_write_con, ox_write_con,
            // context_id);
            // throw ecop7;
        } catch (final OXContextException e) {
            log.error("Context Error", e);
            this.oxcontextcommon.handleCreateContextRollback(configdb_write_con, ox_write_con, context_id);
            throw new StorageException(e);
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            this.oxcontextcommon.handleCreateContextRollback(configdb_write_con, ox_write_con, context_id);
            throw new StorageException(e);
        } catch (final Exception ecp) {
            log.error("Internal Error", ecp);
            this.oxcontextcommon.handleCreateContextRollback(configdb_write_con, ox_write_con, context_id);
            throw new StorageException("Internal server error occured");
        } finally {
            try {
                if (configdb_write_con != null) {
                    cache.pushConfigDBWrite(configdb_write_con);
                }
            } catch (final PoolException ecp) {
                log.error("Error pushing configdb connection to pool!", ecp);
            }

            try {
                if (ox_write_con != null) {
                    cache.pushOXDBWrite(context_id, ox_write_con);
                }
            } catch (final PoolException ecp) {
                log.error("Error pushing ox write connection to pool!", ecp);
            }
        }
    }

    /**
     * Internally used object for getnextdbhandlebyweight method instead of
     */
    private class DatabaseHandle extends Database {

        /**
         * 
         */
        private static final long serialVersionUID = -4816706296673058930L;

        private int count;

        public DatabaseHandle() {
            super();
            this.count = -1;
        }

        public int getCount() {
            return this.count;
        }

        public void setCount(final int count) {
            this.count = count;
        }
    }

    /*
     * ============================================================================
     * Private part
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
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
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
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
            }
        }
        return ret;
    }

    private void createDatabaseAndMappingForContext(final Database db, final Connection configdb_write_con, final int context_id) throws RemoteException, SQLException, PoolException, StorageException {

        final OXUtilStorageInterface oxu = OXUtilStorageInterface.getInstance();
        if (this.CONTEXTS_PER_SCHEMA == 1) {
            String schema_name;
            synchronized (ClientAdminThread.create_mutex) {
                configdb_write_con.setAutoCommit(false);
                final int srv_id = IDGenerator.getId(configdb_write_con);
                configdb_write_con.commit();
                schema_name = db.getName() + "_" + srv_id;
            }
            db.setScheme(schema_name);
            oxu.createDatabase(db);

            // update contextserver2dbpool table with new infos
            updateContextServer2DbPool(db, configdb_write_con, context_id);
        } else {
            // check if there's a db schema which is not yet full
            synchronized (ClientAdminThread.create_mutex) {
                String schema_name = getNextUnfilledSchemaFromDB(db.getId(), configdb_write_con);
                // there's none? create one
                if (schema_name == null) {
                    configdb_write_con.setAutoCommit(false);

                    final int srv_id = IDGenerator.getId(configdb_write_con);
                    configdb_write_con.commit();
                    configdb_write_con.setAutoCommit(true);
                    schema_name = db.getName() + "_" + srv_id;

                    db.setScheme(schema_name);
                    oxu.createDatabase(db);

                    // update contextserver2dbpool table with new infos
                    updateContextServer2DbPool(db, configdb_write_con, context_id);
                } else {
                    db.setScheme(schema_name);
                    // update contextserver2dbpool table with new infos
                    updateContextServer2DbPool(db, configdb_write_con, context_id);
                }
            }
        }
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
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
            }
        }
    }

    private String getNextUnfilledSchemaFromDB(final Integer pool_id, final Connection con) throws SQLException, StorageException {
        if (null != pool_id) {
            PreparedStatement pstm = null;
            
            try {
                pstm = con.prepareStatement("SELECT db_schema,COUNT(db_schema) FROM context_server2db_pool WHERE write_db_pool_id = ? GROUP BY db_schema");
                pstm.setInt(1, pool_id);
                final ResultSet rs = pstm.executeQuery();
                String ret = null;
                final OXToolStorageInterface oxt = OXToolStorageInterface.getInstance();
                
                while (rs.next()) {
                    final String schema = rs.getString("db_schema");
                    final int count = rs.getInt("COUNT(db_schema)");
                    if (count < this.CONTEXTS_PER_SCHEMA) {
                        if (oxt.schemaBeingLockedOrNeedsUpdate(pool_id, schema)) {
                            log.debug("schema " + schema + "is locked or updated, trying next one");
                            continue;
                        }
                        log.debug("count =" + count + " of schema " + schema + ", using it for next context");
                        ret = schema;
                        break;
                    }
                }
                
                return ret;
            } catch (final SQLException e) {
                log.error("SQL Error", e);
                throw e;
            } finally {
                try {
                    if (pstm != null) {
                        pstm.close();
                    }
                } catch (final Exception e) {
                    log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT);
                }
            }
        } else {
            throw new StorageException("pool_id in getNextUnfilledSchemaFromDB must be != null");
        }
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
                log.debug("NO remaining contexts found in scheme " + scheme + " on pool with id " + source_database_id + "!");
                log.debug("NOW dropping scheme " + scheme + " on pool with id " + source_database_id + "!");
                dropstmt = ox_db_write_con.prepareStatement("DROP DATABASE if exists `" + scheme + "`");
                dropstmt.executeUpdate();
                log.debug("Scheme " + scheme + " on pool with id " + source_database_id + " dropped successfully!");
            }
            rs.close();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final Exception ex) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, ex);
            }
            try {
                if (dropstmt != null) {
                    dropstmt.close();
                }
            } catch (final Exception ex) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, ex);
            }
        }
    }

    private void revokeConfigdbMapping(final TableObject contextserver2dbpool_backup, final Connection configdb_write_con, final int context_id) throws SQLException {
        for (int a = 0; a < contextserver2dbpool_backup.getDataRowCount(); a++) {
            final TableRowObject tro = contextserver2dbpool_backup.getDataRow(a);

            final StringBuilder prep_sql = new StringBuilder();

            prep_sql.append("UPDATE " + contextserver2dbpool_backup.getName() + " SET ");

            Enumeration enumi = tro.getColumnNames();

            // Save the order of the columns in this list, that all values are
            // correct mapped to their fields
            // for later use in prepared_statement
            final ArrayList<String> columns_list = new ArrayList<String>();

            while (enumi.hasMoreElements()) {
                final String column = (String) enumi.nextElement();
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
                    log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
                }
            }
            // end of test table
        }
    }

    private void enableContextBackAfterError(final Context ctx) throws StorageException {
        log.error("Try enabling context " + ctx.getIdAsInt() + " back again!");
        enable(ctx);
        log.error("Context " + ctx.getIdAsInt() + " enabled back again!");
    }

    private void fillTargetDatabase(final ArrayList<TableObject> sorted_tables, final Connection target_ox_db_con) throws PoolException, SQLException {
        // do the inserts for all tables!
        for (int a = 0; a < sorted_tables.size(); a++) {
            TableObject to = sorted_tables.get(a);
            to = getDataForTable(to);
            if (to.getDataRowCount() > 0) {
                // ok data in table found, copy to db
                for (int i = 0; i < to.getDataRowCount(); i++) {
                    final StringBuilder prep_sql = new StringBuilder();
                    final StringBuilder sb_values = new StringBuilder();

                    prep_sql.append("INSERT INTO " + to.getName() + " ");
                    prep_sql.append("(");
                    sb_values.append("(");

                    final TableRowObject tro = to.getDataRow(i);
                    Enumeration enumi = tro.getColumnNames();

                    // Save the order of the columns in this list, that all
                    // values are correct mapped to their fields
                    // for later use in prepared_statement
                    final List<String> columns_list = new ArrayList<String>();

                    while (enumi.hasMoreElements()) {
                        final String column = (String) enumi.nextElement();
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
                            log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
                        }
                    }
                    // }// end of test table
                }// end of datarow loop

            }// end of if table has data
            to = null;
        }// end of table loop
    }

    private void fetchTableObjects() throws SQLException {
        this.tableObjects = new Vector<TableObject>();

        this.dbmetadata = this.dbConnection.getMetaData();
        // get the tables to check
        final ResultSet rs2 = this.dbmetadata.getTables(null, null, null, null);
        TableObject to = null;
        while (rs2.next()) {
            final String table_name = rs2.getString("TABLE_NAME");
            to = new TableObject();
            to.setName(table_name);
            // fetch all columns from table and see if it contains matching
            // column
            final ResultSet columns_res = this.dbmetadata.getColumns(this.catalogname, null, table_name, null);
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
                to.setColumn(tco);
            }
            columns_res.close();
            if (table_matches) {
                this.tableObjects.add(to);
            }
        }
        log.debug("####### Found -> " + this.tableObjects.size() + " tables");

    }

    private ArrayList<TableObject> sortTableObjects() throws SQLException {
        findReferences();
        // thx http://de.wikipedia.org/wiki/Topologische_Sortierung :)
        return sortTablesByForeignKey();
    }

    private ArrayList<TableObject> sortTablesByForeignKey() {
        final ArrayList<TableObject> nasty_order = new ArrayList<TableObject>();

        final ArrayList<TableObject> unsorted = new ArrayList<TableObject>();
        unsorted.addAll(this.tableObjects);

        // now sort the table with a topological sort mech :)
        // work with the unsorted vector
        while (unsorted.size() > 0) {
            for (int a = 0; a < unsorted.size(); a++) {
                final TableObject to = (TableObject) unsorted.get(a);
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
    private void findReferences() throws SQLException {
        for (int v = 0; v < this.tableObjects.size(); v++) {
            final TableObject to = (TableObject) this.tableObjects.get(v);
            // get references from this table to another
            final String table_name = to.getName();
            // ResultSet table_references =
            // dbmetadata.getCrossReference("%",null,table_name,getCatalogName(),null,getCatalogName());
            final ResultSet table_references = this.dbmetadata.getImportedKeys(this.catalogname, null, table_name);
            log.debug("Table " + table_name + " has pk reference to table-column:");
            while (table_references.next()) {
                final String pk = table_references.getString("PKTABLE_NAME");
                final String pkc = table_references.getString("PKCOLUMN_NAME");
                log.debug("--> Table: " + pk + " column ->" + pkc);
                to.addCrossReferenceTable(pk);
                final int pos_in_list = tableListContainsObject(pk);
                if (pos_in_list != -1) {
                    log.debug("Found referenced by " + table_name + "<->" + pk + "->" + pkc);
                    final TableObject edit_me = (TableObject) this.tableObjects.get(pos_in_list);
                    edit_me.addReferencedBy(table_name);
                }
            }
            table_references.close();
        }
    }

    /**
     * remove no more needed element from list and remove the reference to
     * removed element so that a new element exists which has now references.
     */
    private void removeAndSortNew(final ArrayList<TableObject> list, final TableObject to) {
        list.remove(to);
        for (int i = 0; i < list.size(); i++) {
            final TableObject tob = (TableObject) list.get(i);
            tob.removeCrossReferenceTable(to.getName());
        }
    }

    /**
     * Returns -1 if not found else the position in the Vector where the object
     * is located.
     */
    private int tableListContainsObject(final String table_name) {
        int found_at_position = -1;
        for (int v = 0; v < this.tableObjects.size(); v++) {
            final TableObject to = (TableObject) this.tableObjects.get(v);
            if (to.getName().equals(table_name)) {
                found_at_position = v;
            }
        }
        return found_at_position;
    }

    private TableObject getDataForTable(final TableObject to) throws SQLException {
        final Vector column_objects = to.getColumns();
        // build the statement string
        final StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        for (int a = 0; a < column_objects.size(); a++) {
            final TableColumnObject tco = (TableColumnObject) column_objects.get(a);
            sb.append("`" + tco.getName() + "`,");
        }
        sb.delete(sb.length() - 1, sb.length());
        sb.append(" FROM " + to.getName() + " WHERE " + this.selectionCriteria + " = ?");

        // fetch data from table
        PreparedStatement prep = null;
        try {
            prep = this.dbConnection.prepareStatement(sb.toString());
            prep.setObject(1, this.criteriaMatch, this.criteriaType);
            log.debug("######## " + sb.toString());
            final ResultSet rs = prep.executeQuery();
            while (rs.next()) {
                final TableRowObject tro = new TableRowObject();
                for (int b = 0; b < column_objects.size(); b++) {
                    final TableColumnObject tco = (TableColumnObject) column_objects.get(b);
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
                log.error("Error closing statement", e);
            }
        }

        return to;
    }

    private void myLockUnlockAllContexts(final boolean lock_all, final int reason_id) throws SQLException, PoolException {
        Connection con_write = null;
        PreparedStatement stmt = null;
        try {
            con_write = cache.getWRITEConnectionForCONFIGDB();
            con_write.setAutoCommit(false);
            if (reason_id != -1) {
                stmt = con_write.prepareStatement("UPDATE context SET enabled = ?, reason_id = ?");
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
            log.error("SQL Error", sql);
            try {
                con_write.rollback();
            } catch (final SQLException ec) {
                log.error("Error rollback configdb connection", ec);
            }
            throw sql;
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            try {
                con_write.rollback();
            } catch (final SQLException ec) {
                log.error("Error rollback configdb connection", ec);
            }
            throw e;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
            }
            if (con_write != null) {
                try {
                    cache.pushConfigDBWrite(con_write);
                } catch (final PoolException exp) {
                    log.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    private void myEnableDisableContext(final int context_id, final boolean enabled, final int reason_id) throws SQLException, PoolException {
        Connection con_write = null;
        PreparedStatement stmt = null;
        try {
            con_write = cache.getWRITEConnectionForCONFIGDB();
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
                        log.error("Invalid reason ID!", exp);
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
            log.error("SQL Error", sql);
            try {
                con_write.rollback();
            } catch (final SQLException ec) {
                log.error("Error rollback configdb connection", ec);
            }
            throw sql;
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            try {
                con_write.rollback();
            } catch (final SQLException ec) {
                log.error("Error rollback configdb connection", ec);
            }
            throw e;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
            }
            if (con_write != null) {
                try {
                    cache.pushConfigDBWrite(con_write);
                } catch (final PoolException exp) {
                    log.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    /**
     * determine the next database to use depending on database weight factor
     * 
     * @param configdb_con
     * @return Database handle containing information about database
     * @throws SQLException
     * @throws OXContextException
     */
    private Database getNextDBHandleByWeight(final Connection configdb_con) throws SQLException, OXContextException {
        PreparedStatement pstm = null;
        try {
            pstm = configdb_con.prepareStatement("SELECT db_pool_id,url,driver,login,password,name,weight,max_units FROM db_pool, db_cluster WHERE db_cluster.write_db_pool_id = db_pool_id");
            ResultSet rs = pstm.executeQuery();

            int totalDatabases = 0;
            final ArrayList<DatabaseHandle> list = new ArrayList<DatabaseHandle>();
            double maxdist = 0;

            while (rs.next()) {
                final DatabaseHandle datahandle = new DatabaseHandle();
                datahandle.setUrl(rs.getString("url"));
                datahandle.setId(rs.getInt("db_pool_id"));
                datahandle.setDriver(rs.getString("driver"));
                datahandle.setLogin(rs.getString("login"));
                datahandle.setPassword(rs.getString("password"));
                datahandle.setClusterWeight(rs.getInt("weight"));
                datahandle.setMaxUnits(rs.getInt("max_units"));

                final int db_count = countUnits(datahandle, configdb_con);
                datahandle.setCount(db_count);

                totalDatabases += db_count;
                final String name = rs.getString("name");
                datahandle.setName(name);

                list.add(datahandle);

                // log.debug("SERVERDATA(" + rs.getString("name") + ")= " +
                // sdata);
            }
            rs.close();
            pstm.close();

            DatabaseHandle selected_server = null;
            // Here we have to do a second loop because we know the amount of
            // totalDatabase
            // after the first loop
            for (final DatabaseHandle handle : list) {
                final int unit_max = handle.getMaxUnits();
                final int weight = handle.getClusterWeight();
                final int db_count = handle.getCount();
                final String name = handle.getName();

                if (unit_max == -1 || (unit_max != 0 && db_count < unit_max)) {
                    double currweight = (double) totalDatabases / 100 * db_count;
                    final double x = currweight / weight;
                    currweight -= (int) x * weight;
                    final double currdist = weight - currweight;
                    log.debug(name + ":\tX=" + x + "\tcurrweight=" + currweight + "\tcurrdist=" + currdist + "\tmaxdist=" + maxdist);
                    if (currdist > maxdist) {
                        selected_server = handle;
                        maxdist = weight - currweight;
                    }
                }
            }

            if (selected_server == null) {
                throw new OXContextException("Unable to find a suitable server");
            }

            pstm = configdb_con.prepareStatement("SELECT read_db_pool_id FROM db_cluster WHERE write_db_pool_id = ?");
            pstm.setInt(1, selected_server.getId());
            rs = pstm.executeQuery();
            if (!rs.next()) {
                throw new OXContextException("Unable to read table db_cluster");
            }
            // Never used so commented
            final int slave_id = rs.getInt("read_db_pool_id");
            rs.close();
            pstm.close();

            final Database retval = selected_server;
            if (slave_id > 0) {
                retval.setRead_id(slave_id);
            }

            return retval;
        } finally {
            try {
                if (pstm != null) {
                    pstm.close();
                }
            } catch (final SQLException e) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
            }
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
                        Connection rcon = cache.getSimpleSqlConnection(db.getUrl() + schema, db.getLogin(), db.getPassword(), db.getDriver());
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
                        log.fatal("Error counting users of db pool", e);
                        throw new OXContextException(e.toString());
                    } finally {
                        rsi.close();
                    }
                }
                rpool.close();
                log.debug("***** found " + count + " users on " + pool_id);
            } else {
                throw new OXContextException("UNKNOWN UNIT TO COUNT: " + this.USE_UNIT);
            }

            return count;
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (final SQLException e) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
            }
            try {
                if (ppool != null) {
                    ppool.close();
                }
            } catch (final SQLException e) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
            }
        }
    }

    private void fillLogin2ContextTable(final Context ctx, final Connection configdb_write_con) throws SQLException {
        HashSet<String> loginMappings = ctx.getLoginMappings();
        final Integer ctxid = ctx.getIdAsInt();
        if (null == loginMappings || loginMappings.isEmpty()) {
            loginMappings = new HashSet<String>();
            loginMappings.add(ctx.getIdAsString());
        }
        PreparedStatement stmt = null;
        try {
            stmt = configdb_write_con.prepareStatement("INSERT INTO login2context (cid,login_info) VALUES (?,?)");
            for (final String mapping : loginMappings) {
                stmt.setInt(1, ctxid);
                stmt.setString(2, mapping);
                stmt.executeUpdate();
            }
        } catch (final SQLException sql) {
            log.error("SQL Error", sql);
            throw sql;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
            }
        }
    }

    @Override
    public void change(Context ctx) throws StorageException {
        
        Connection config_db_write = null;
        
        try{
            
            config_db_write = cache.getWRITEConnectionForCONFIGDB();
            config_db_write.setAutoCommit(false);
                        
            // Change login mappings in configdb          
            changeLoginMappingsForContext(ctx,config_db_write);
            
            // Change context name in configdb
            changeNameForContext(ctx,config_db_write);
            
            // Change quota size in config db
            changeQuotaForContext(ctx,config_db_write);
                        
            // Change storage data
            changeStorageDataImpl(ctx, config_db_write);
            
            // commit changes to db
            config_db_write.commit();
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            if(config_db_write!=null){
                try {
                    config_db_write.rollback();
                } catch (SQLException e1) {
                    log.error("Error in rollback of configdb connection",e1);
                    throw new StorageException(e1);                   
                }
            }
            
            log.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
           
            try {
                if (null != config_db_write) {
                    cache.pushConfigDBWrite(config_db_write);
                }
            } catch (final PoolException exp) {
                log.error("Error pushing configdb connection to pool!", exp);
            }
        }
        
    }
    
    private void changeQuotaForContext(final Context ctx, final Connection configdb_con) throws SQLException{
        
        
        // check if max quota is set in context 
       if(ctx.getMaxQuota()!=null){
           
           long quota_max_temp = ctx.getMaxQuota().longValue();
           
           if (ctx.getMaxQuota().longValue() != -1) {
               quota_max_temp *= Math.pow(2, 20);
           }  
           
           PreparedStatement prep = null;
           try {
               
               prep = configdb_con.prepareStatement("UPDATE context SET quota_max=? WHERE cid=?");
               prep.setLong(1, quota_max_temp);
               prep.setInt(2, ctx.getIdAsInt());
               prep.executeUpdate();
               prep.close();
           
           }finally{
                try {
                   if (prep != null) {
                        prep.close();
                   }
                } catch (final SQLException e) {
                       log.error("SQL Error closing statement!", e);
                }            
           } 
           
       }
              
    }
    
   
   
    
    private void changeLoginMappingsForContext(final Context ctx, final Connection configdb_con) throws SQLException{
        
        PreparedStatement prep = null;
        
        try {
           
            if(ctx.getLoginMappings()!=null && ctx.getLoginMappings().size()>0){
                
                HashSet<String> login_map = ctx.getLoginMappings();
                login_map.remove(ctx.getIdAsString()); // Deny change of mapping cid<->cid
               
                    
                    // first delete all mappings excluding default mapping from cid <-> cid
                    prep = configdb_con.prepareStatement("DELETE FROM login2context WHERE cid = ? AND login_info!=?");
                    prep.setInt(1, ctx.getIdAsInt().intValue());
                    prep.setInt(2, ctx.getIdAsInt().intValue());
                    prep.executeUpdate();
                    prep.close();
                    
                    // now insert all mappings from the hashset if size >0 
                    if(login_map.size()>0){
                        Iterator<String> itr = login_map.iterator();
                        while(itr.hasNext()){
                            String mapping_entry = (itr.next()).trim();
                            if(mapping_entry.length()>0){
                                
                                // check if no mapping which the client wants to add already exists for another context
                                prep = configdb_con.prepareStatement("SELECT cid FROM login2context WHERE login_info = ?");
                                prep.setString(1, mapping_entry);
                                ResultSet rs = prep.executeQuery();
                                if(rs.next()){
                                    // throw exception back to client 
                                    String err_msg ="A mapping with login info \""+mapping_entry+"\" already exists in the system!";
                                    log.error(err_msg);
                                    throw new SQLException(err_msg);
                                }
                                rs.close();
                                prep.close();
                                
                                prep = configdb_con.prepareStatement("INSERT INTO login2context (cid,login_info) values (?,?)");
                                prep.setInt(1, ctx.getIdAsInt().intValue());
                                prep.setString(2, mapping_entry);
                                prep.executeUpdate();
                                prep.close();
                            }                               
                        }
                    }
                
            } // end of updating login mappings ###
        }finally{
            try {
                if (prep != null) {
                    prep.close();
                }
            } catch (final SQLException e) {
                log.error("SQL Error closing statement!", e);
            }            
        }       
    }
    
    private void changeNameForContext(final Context ctx, final Connection configdb_con) throws SQLException{
        
        PreparedStatement prep = null;
        
        try {
            
            
            // first check if name is set and has a valid name
            if(ctx.getName()!=null && ctx.getName().trim().length()>0){
                // ok , now check if a context with this name the client wants to change already exists
                // BUT exclude the name of the current context, because this context can of course be renamed to the same
                // name as it had before the update :)
                
                prep = configdb_con.prepareStatement("SELECT cid FROM context WHERE name = ? AND cid !=?");
                prep.setString(1, ctx.getName().trim());
                prep.setInt(2, ctx.getIdAsInt().intValue());
                ResultSet rs = prep.executeQuery();
                if(rs.next()){
                    // context with the name already exists in the system,
                    String err_msg ="A context with context name \""+ctx.getName().trim()+"\" already exists in the system!";
                    log.error(err_msg);
                    // throw error
                    throw new SQLException(err_msg);
                }
                rs.close();
                prep.close();
                
                // if we reach here, update the table
                prep = configdb_con.prepareStatement("UPDATE context SET name = ? where cid = ?");
                prep.setString(1, ctx.getName().trim());
                prep.setInt(2, ctx.getIdAsInt().intValue());
                prep.executeUpdate();
                prep.close();
                
            }
        }finally{
            try {
                if (prep != null) {
                    prep.close();
                }
            } catch (final SQLException e) {
                log.error("SQL Error closing statement!", e);
            }
        }
        
    }
    
    
}
