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
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.java.Autoboxing.l;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import com.google.common.collect.Lists;
import com.openexchange.admin.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.OXContextException;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.storage.sqlStorage.OXUtilSQLStorage;
import com.openexchange.admin.storage.utils.Filestore2UserUtil;
import com.openexchange.admin.storage.utils.Filestore2UserUtil.UserAndContext;
import com.openexchange.admin.storage.utils.PoolAndSchema;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.AdminCacheExtended;
import com.openexchange.admin.tools.PropertyHandlerExtended;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorageUnregisterListener;
import com.openexchange.filestore.FileStorageUnregisterListenerRegistry;
import com.openexchange.filestore.FileStorages;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.threadpool.BoundedCompletionService;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;

/**
 * @author d7
 * @author cutmasta
 */
public class OXUtilMySQLStorage extends OXUtilSQLStorage {

    /** The logger */
    final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXUtilMySQLStorage.class);

    static final ThreadPools.ExpectedExceptionFactory<StorageException> EXCEPTION_FACTORY = new ThreadPools.ExpectedExceptionFactory<StorageException>() {

        @Override
        public StorageException newUnexpectedError(final Throwable t) {
            return new StorageException(t);
        }

        @Override
        public Class<StorageException> getType() {
            return StorageException.class;
        }
    };

    private static final Long MAX_FILESTORE_CAPACITY = new Long("8796093022208");

    private final AdminCacheExtended cache;
    private final PropertyHandlerExtended prop;
    private final int maxNumberOfContextsPerSchema;

    /**
     * Initializes a new {@link OXUtilMySQLStorage}.
     */
    public OXUtilMySQLStorage() {
        super();
        this.cache = ClientAdminThreadExtended.cache;
        int maxNumberOfContextsPerSchema = 1;
        try {
            maxNumberOfContextsPerSchema = Integer.parseInt(cache.getProperties().getProp("CONTEXTS_PER_SCHEMA", "1"));
            if (maxNumberOfContextsPerSchema <= 0) {
                throw new OXContextException("CONTEXTS_PER_SCHEMA MUST BE > 0");
            }
        } catch (final OXContextException e) {
            LOG.error("Error init", e);
        }
        this.maxNumberOfContextsPerSchema = maxNumberOfContextsPerSchema;
        prop = cache.getProperties();
    }

    @Override
    public int createMaintenanceReason(final MaintenanceReason reason) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;
        boolean rollback = false;
        try {
            con = cache.getWriteConnectionForConfigDB();

            final int res_id = nextId(con);

            con.setAutoCommit(false);
            rollback = true;
            stmt = con.prepareStatement("INSERT INTO reason_text (id,text) VALUES(?,?)");
            stmt.setInt(1, res_id);
            stmt.setString(2, reason.getText());
            stmt.executeUpdate();
            con.commit();
            rollback = false;
            return res_id;
        } catch (final DataTruncation dt) {
            LOG.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final PoolException pexp) {
            LOG.error("Pool error", pexp);
            throw new StorageException(pexp);
        } catch (final SQLException ecp) {
            LOG.error("Error", ecp);
            throw new StorageException(ecp);
        } finally {
            if (rollback) {
                rollback(con);
            }
            closeSQLStuff(stmt);
            if (con != null) {
                try {
                    cache.pushWriteConnectionForConfigDB(con);
                } catch (final PoolException exp) {
                    LOG.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    @Override
    public void changeDatabase(final Database db) throws StorageException {
        // Get connection
        final Connection con;
        try {
            con = cache.getWriteConnectionForConfigDB();
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }

        // Process it...
        boolean rollback = true;
        try {
            con.setAutoCommit(false);

            // Lock appropriate rows
            final Integer id = db.getId();
            if (null == id) {
                throw new StorageException("Missing database identifier");
            }
            lock(con);

            // Change database
            changeDatabase(db, con);
            con.commit();
            rollback = false;
        } catch (final DataTruncation dt) {
            rollback(con);
            rollback = false;
            LOG.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final SQLException sql) {
            rollback(con);
            rollback = false;
            LOG.error("SQL Error", sql);
            throw new StorageException(sql.toString(), sql);
        } finally {
            if (rollback) {
                rollback(con);
            }
            autocommit(con);
            try {
                cache.pushWriteConnectionForConfigDB(con);
            } catch (final PoolException e) {
                LOG.error("Error pushing configdb connection to pool!", e);
            }
        }
    }

    private void lock(final Connection con) throws SQLException {
        if (null == con) {
            return;
        }
        Statement stmt = null;
        try {
            if (con.getAutoCommit()) {
                throw new SQLException("Connection is not in transaction state.");
            }
            stmt = con.createStatement();
            stmt.execute("SELECT COUNT(*) FROM db_cluster FOR UPDATE");
            closeSQLStuff(stmt);

            stmt = con.createStatement();
            stmt.execute("SELECT COUNT(*) FROM db_pool FOR UPDATE");
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void changeDatabase(final Database db, final Connection con) throws SQLException {
        PreparedStatement prep = null;
        try {
            final StringBuilder sqlBuilder = new StringBuilder(2048);
            sqlBuilder.append("UPDATE db_pool,db_cluster SET ");

            final List<Object> params = new LinkedList<>();
            boolean first = true;

            if (db.getName() != null && db.getName().length() > 0) {
                first = false;
                sqlBuilder.append("db_pool.name = ?");
                params.add(db.getName());
            }

            if (db.getLogin() != null && db.getLogin().length() > 0) {
                if (first) {
                    first = false;
                } else {
                    sqlBuilder.append(", ");
                }
                sqlBuilder.append("db_pool.login = ?");
                params.add(db.getLogin());
            }

            if (db.getPassword() != null && db.getPassword().length() > 0) {
                if (first) {
                    first = false;
                } else {
                    sqlBuilder.append(", ");
                }
                sqlBuilder.append("db_pool.password = ?");
                params.add(db.getPassword());
            }

            if (db.getDriver() != null && db.getDriver().length() > 0) {
                if (first) {
                    first = false;
                } else {
                    sqlBuilder.append(", ");
                }
                sqlBuilder.append("db_pool.driver = ?");
                params.add(db.getDriver());
            }

            if (db.getPoolInitial() != null) {
                if (first) {
                    first = false;
                } else {
                    sqlBuilder.append(", ");
                }
                sqlBuilder.append("db_pool.initial = ?");
                params.add(db.getPoolInitial());
            }

            if (db.getPoolMax() != null) {
                if (first) {
                    first = false;
                } else {
                    sqlBuilder.append(", ");
                }
                sqlBuilder.append("db_pool.max = ?");
                params.add(db.getPoolMax());
            }

            if (db.getPoolHardLimit() != null) {
                if (first) {
                    first = false;
                } else {
                    sqlBuilder.append(", ");
                }
                sqlBuilder.append("db_pool.hardlimit = ?");
                params.add(db.getPoolHardLimit());
            }

            if (db.getUrl() != null && db.getUrl().length() > 0) {
                if (first) {
                    first = false;
                } else {
                    sqlBuilder.append(", ");
                }
                sqlBuilder.append("db_pool.url = ?");
                params.add(db.getUrl());
            }

            if (db.getMaxUnits() != null) {
                if (first) {
                    first = false;
                } else {
                    sqlBuilder.append(", ");
                }
                sqlBuilder.append("db_cluster.max_units = ?");
                params.add(db.getMaxUnits());
            }

            if (first) {
                // No changes applied
                return;
            }

            // Finish SQL
            sqlBuilder.append(" WHERE db_pool.db_pool_id = ? AND (db_cluster.write_db_pool_id = ? OR db_cluster.read_db_pool_id = ?)");
            params.add(db.getId());
            params.add(db.getId());
            params.add(db.getId());

            // Create statement, fill parameters, and execute update
            prep = con.prepareStatement(sqlBuilder.toString());
            final int size = params.size();
            for (int pos = 1; pos <= size; pos++) {
                prep.setObject(pos, params.get(pos - 1));
            }
            prep.executeUpdate();
        } finally {
            closeSQLStuff(prep);
        }
    }

    @Override
    public void changeFilestoreDataFor(Context ctx) throws StorageException {
        Connection configDbCon = null;
        int rollback = 0;
        try {
            configDbCon = cache.getWriteConnectionForConfigDB();
            configDbCon.setAutoCommit(false);
            rollback = 1;

            changeFilestoreDataFor(ctx, configDbCon);

            configDbCon.commit();
            rollback = 2;
        } catch (SQLException exp) {
            throw new StorageException(exp);
        } catch (PoolException e) {
            throw new StorageException(e);
        } finally {
            if (null != configDbCon) {
                if (rollback > 0) {
                    if (rollback == 1) {
                        Databases.rollback(configDbCon);
                    }
                    Databases.autocommit(configDbCon);
                }
                try {
                    cache.pushWriteConnectionForConfigDB(configDbCon);
                } catch (PoolException e) {
                    LOG.debug("Failed to push connection back to pool", e);
                }
            }
        }
    }

    @Override
    public void changeFilestoreDataFor(Context ctx, Connection configDbCon) throws StorageException {
        if (ctx.getFilestoreId() != null) {
            final OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
            final Filestore filestore = oxutil.getFilestore(ctx.getFilestoreId().intValue(), false);
            PreparedStatement prep = null;
            final int context_id = ctx.getId().intValue();
            try {

                if (filestore.getId() != null && -1 != filestore.getId().intValue()) {
                    prep = configDbCon.prepareStatement("UPDATE context SET filestore_id = ? WHERE cid = ?");
                    prep.setInt(1, filestore.getId().intValue());
                    prep.setInt(2, context_id);
                    prep.executeUpdate();
                    prep.close();
                }

                final String filestore_name = ctx.getFilestore_name();
                if (null != filestore_name) {
                    prep = configDbCon.prepareStatement("UPDATE context SET filestore_name = ? WHERE cid = ?");
                    prep.setString(1, filestore_name);
                    prep.setInt(2, context_id);
                    prep.executeUpdate();
                    prep.close();
                }

            } catch (SQLException exp) {
                throw new StorageException(exp);
            } finally {
                Databases.closeSQLStuff(prep);
            }
        }
    }

    @Override
    public void prepareFilestoreUsageFor(User user, Context ctx) throws StorageException {
        int contextId = ctx.getId().intValue();
        Connection con = null;
        int rollback = 0;
        try {
            con = cache.getConnectionForContext(contextId);
            con.setAutoCommit(false);
            rollback = 1;

            prepareFilestoreUsageFor(user, ctx, con);

            con.commit();
            rollback = 2;
        } catch (SQLException exp) {
            throw new StorageException(exp);
        } catch (PoolException e) {
            throw new StorageException(e);
        } finally {
            if (null != con) {
                if (rollback > 0) {
                    if (rollback == 1) {
                        Databases.rollback(con);
                    }
                    Databases.autocommit(con);
                }
                try {
                    cache.pushConnectionForContext(contextId, con);
                } catch (PoolException e) {
                    LOG.debug("Failed to push connection back to pool", e);
                }
            }
        }
    }

    @Override
    public void prepareFilestoreUsageFor(User user, Context ctx, Connection con) throws StorageException {
        int contextId = ctx.getId().intValue();
        int userId = user.getId().intValue();

        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            prep = con.prepareStatement("SELECT 1 FROM filestore_usage WHERE cid=? AND user=?");
            prep.setInt(1, contextId);
            prep.setInt(2, userId);
            rs = prep.executeQuery();

            boolean exists = rs.next();
            Databases.closeSQLStuff(rs, prep);

            if (false == exists) {
                prep = con.prepareStatement("INSERT INTO filestore_usage (cid, user, used) VALUES (?, ?, ?)");
                prep.setInt(1, contextId);
                prep.setInt(2, userId);
                prep.setLong(3, 0L);

                try {
                    prep.executeUpdate();
                } catch (SQLException exp) {
                    if (Databases.isPrimaryKeyConflictInMySQL(exp)) {
                        // All fine. The needed entry was already added in the meantime
                        return;
                    }
                    // Otherwise re-throw...
                    throw exp;
                }
            }
        } catch (SQLException exp) {
            throw new StorageException(exp);
        } finally {
            Databases.closeSQLStuff(rs, prep);
        }
    }

    @Override
    public void cleanseFilestoreUsageFor(User user, Context ctx) throws StorageException {
        int contextId = ctx.getId().intValue();
        Connection con = null;
        int rollback = 0;
        try {
            con = cache.getConnectionForContext(contextId);
            con.setAutoCommit(false);
            rollback = 1;

            cleanseFilestoreUsageFor(user, ctx, con);

            con.commit();
            rollback = 2;
        } catch (SQLException exp) {
            throw new StorageException(exp);
        } catch (PoolException e) {
            throw new StorageException(e);
        } finally {
            if (null != con) {
                if (rollback > 0) {
                    if (rollback == 1) {
                        Databases.rollback(con);
                    }
                    Databases.autocommit(con);
                }
                try {
                    cache.pushConnectionForContext(contextId, con);
                } catch (PoolException e) {
                    LOG.debug("Failed to push connection back to pool", e);
                }
            }
        }
    }

    @Override
    public void cleanseFilestoreUsageFor(User user, Context ctx, Connection con) throws StorageException {
        int contextId = ctx.getId().intValue();
        int userId = user.getId().intValue();

        PreparedStatement prep = null;
        try {
            prep = con.prepareStatement("DELETE FROM filestore_usage WHERE cid=? AND user=?");
            prep.setInt(1, contextId);
            prep.setInt(2, userId);
            prep.executeUpdate();
        } catch (SQLException exp) {
            throw new StorageException(exp);
        } finally {
            Databases.closeSQLStuff(prep);
        }
    }

    @Override
    public void changeFilestoreDataFor(User user, Context ctx) throws StorageException {
        int contextId = ctx.getId().intValue();
        Connection con = null;
        int rollback = 0;
        try {
            con = cache.getConnectionForContext(contextId);
            con.setAutoCommit(false);
            rollback = 1;

            changeFilestoreDataFor(user, ctx, con);

            con.commit();
            rollback = 2;
        } catch (SQLException exp) {
            throw new StorageException(exp);
        } catch (PoolException e) {
            throw new StorageException(e);
        } finally {
            if (null != con) {
                if (rollback > 0) {
                    if (rollback == 1) {
                        Databases.rollback(con);
                    }
                    Databases.autocommit(con);
                }
                try {
                    cache.pushConnectionForContext(contextId, con);
                } catch (PoolException e) {
                    LOG.debug("Failed to push connection back to pool", e);
                }
            }
        }
    }

    @Override
    public void changeFilestoreDataFor(User user, Context ctx, Connection con) throws StorageException {
        if (user.getFilestoreId() != null && -1 != user.getFilestoreId().intValue()) {
            int filestoreId = user.getFilestoreId().intValue();

            int contextId = ctx.getId().intValue();
            int userId = user.getId().intValue();
            PreparedStatement prep = null;
            try {
                boolean changed = false;
                if (filestoreId >= 0) {
                    prep = con.prepareStatement("UPDATE user SET filestore_id = ? WHERE cid = ? AND id = ? AND filestore_id <> ?");
                    prep.setInt(1, filestoreId);
                    prep.setInt(2, contextId);
                    prep.setInt(3, userId);
                    prep.setInt(4, filestoreId);
                    changed = prep.executeUpdate() > 0;
                    prep.close();

                    if (changed) {
                        prep = con.prepareStatement("SELECT 1 FROM filestore_usage WHERE cid=? AND user=?");
                        prep.setInt(1, contextId);
                        prep.setInt(2, userId);
                        boolean entryAvailable = prep.executeQuery().next();
                        Databases.closeSQLStuff(prep);

                        if (false == entryAvailable) {
                            prep = con.prepareStatement("INSERT INTO filestore_usage (cid, user, used) VALUES (?, ?, ?)");
                            prep.setInt(1, contextId);
                            prep.setInt(2, userId);
                            prep.setLong(3, 0L);
                            prep.executeUpdate();
                            Databases.closeSQLStuff(prep);
                        }
                    }
                }

                if (filestoreId > 0) {
                    String filestoreName = user.getFilestore_name();
                    if (null == filestoreName) {
                        filestoreName = FileStorages.getNameForUser(userId, contextId);
                    }

                    prep = con.prepareStatement("UPDATE user SET filestore_name = ? where cid=? and id=? and (filestore_name IS NULL OR filestore_name != ?)");
                    prep.setString(1, filestoreName);
                    prep.setInt(2, contextId);
                    prep.setInt(3, userId);
                    prep.setString(4, filestoreName);
                    changed |= prep.executeUpdate() > 0;
                    Databases.closeSQLStuff(prep);
                } else {
                    prep = con.prepareStatement("UPDATE user SET filestore_name = ? where cid=? and id=?");
                    prep.setNull(1, Types.VARCHAR);
                    prep.setInt(2, contextId);
                    prep.setInt(3, userId);
                    changed |= prep.executeUpdate() > 0;
                    Databases.closeSQLStuff(prep);
                }

                if (changed) {
                    Integer fsOwner = user.getFilestoreOwner();
                    if (fsOwner != null && -1 != fsOwner.intValue()) {
                        prep = con.prepareStatement("UPDATE user SET filestore_owner = ? WHERE cid = ? AND id = ?");
                        prep.setInt(1, fsOwner.intValue() == userId ? 0 : fsOwner.intValue());
                        prep.setInt(2, contextId);
                        prep.setInt(3, userId);
                        prep.executeUpdate();
                        Databases.closeSQLStuff(prep);
                    } else {
                        prep = con.prepareStatement("UPDATE user SET filestore_owner = ? WHERE cid = ? AND id = ?");
                        prep.setInt(1, 0);
                        prep.setInt(2, contextId);
                        prep.setInt(3, userId);
                        prep.executeUpdate();
                        Databases.closeSQLStuff(prep);
                    }

                    Long maxQuota = user.getMaxQuota();
                    if (null != maxQuota) {
                        long quota_max_temp = maxQuota.longValue();
                        if (quota_max_temp != -1) {
                            quota_max_temp = quota_max_temp << 20;
                        }
                        prep = con.prepareStatement("UPDATE user SET quota_max = ? WHERE cid = ? AND id = ?");
                        prep.setLong(1, quota_max_temp);
                        prep.setInt(2, contextId);
                        prep.setInt(3, userId);
                        prep.executeUpdate();
                        Databases.closeSQLStuff(prep);
                    }
                }
            } catch (SQLException exp) {
                throw new StorageException(exp);
            } finally {
                Databases.closeSQLStuff(prep);
            }
        }
    }

    @Override
    public void changeFilestore(final Filestore fstore) throws StorageException {

        Connection configdb_write_con = null;
        PreparedStatement prep = null;
        int rollback = 0;
        try {
            configdb_write_con = cache.getWriteConnectionForConfigDB();
            configdb_write_con.setAutoCommit(false);
            rollback = 1;

            final Integer id = fstore.getId();
            final String url = fstore.getUrl();
            if (null != url) {
                prep = configdb_write_con.prepareStatement("UPDATE filestore SET uri = ? WHERE id = ?");
                prep.setString(1, url);
                prep.setInt(2, id.intValue());
                prep.executeUpdate();
                prep.close();
            }

            final Long store_size = fstore.getSize();
            if (null != store_size && store_size.longValue() != -1) {
                final Long l_max = MAX_FILESTORE_CAPACITY;
                if (store_size.longValue() > l_max.longValue()) {
                    throw new StorageException("Filestore size to large for database (max=" + l_max.longValue() + ")");
                }
                long store_size_double = store_size.longValue();
                store_size_double = store_size_double << 20;
                prep = configdb_write_con.prepareStatement("UPDATE filestore SET size = ? WHERE id = ?");
                prep.setLong(1, store_size_double);
                prep.setInt(2, id.intValue());
                prep.executeUpdate();
                prep.close();
            }

            final Integer maxContexts = fstore.getMaxContexts();
            if (null != maxContexts) {
                prep = configdb_write_con.prepareStatement("UPDATE filestore SET max_context = ? WHERE id = ?");
                prep.setInt(1, maxContexts.intValue());
                prep.setInt(2, id.intValue());
                prep.executeUpdate();
                prep.close();
            }

            configdb_write_con.commit();
            rollback = 2;
        } catch (final DataTruncation dt) {
            LOG.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException exp) {
            LOG.error("SQL Error", exp);
            throw new StorageException(exp);
        } finally {
            closeSQLStuff(prep);

            if (rollback > 0) {
                if (rollback == 1) {
                    rollback(configdb_write_con);
                }

                autocommit(configdb_write_con);
            }

            if (configdb_write_con != null) {
                try {
                    cache.pushWriteConnectionForConfigDB(configdb_write_con);
                } catch (final PoolException ecp) {
                    LOG.error("Error pushing configdb connection to pool!", ecp);
                }
            }
        }
    }

    @Override
    public void createDatabase(final Database db, Connection con) throws StorageException {
        OXUtilMySQLStorageCommon.createDatabase(db, con);
    }

    @Override
    public void deleteMaintenanceReason(final int[] reason_ids) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;
        boolean rollback = false;
        try {
            con = cache.getWriteConnectionForConfigDB();
            con.setAutoCommit(false);
            rollback = true;

            for (int element : reason_ids) {
                stmt = con.prepareStatement("DELETE FROM reason_text WHERE id = ?");
                stmt.setInt(1, element);
                stmt.executeUpdate();
                stmt.close();
            }
            con.commit();
            rollback = false;
        } catch (final PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            LOG.error("SQL Error", ecp);
            throw new StorageException(ecp);
        } finally {
            if (rollback) {
                rollback(con);
            }

            closeSQLStuff(stmt);
            autocommit(con);
            try {
                if (con != null) {
                    cache.pushWriteConnectionForConfigDB(con);
                }
            } catch (final PoolException exp) {
                LOG.error("Error pushing configdb connection to pool!", exp);
            }
        }
    }

    @Override
    public MaintenanceReason[] getAllMaintenanceReasons() throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = cache.getReadConnectionForConfigDB();

            stmt = con.prepareStatement("SELECT id,text FROM reason_text");

            ResultSet rs = stmt.executeQuery();
            List<MaintenanceReason> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new MaintenanceReason(I(rs.getInt("id")), rs.getString("text")));
            }
            rs.close();

            return list.toArray(new MaintenanceReason[list.size()]);
        } catch (final PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            LOG.error("SQL Error", ecp);
            throw new StorageException(ecp);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                LOG.error("Error closing statement", e);
            }
            try {
                if (con != null) {
                    cache.pushReadConnectionForConfigDB(con);
                }
            } catch (final PoolException exp) {
                LOG.error("Error pushing configdb connection to pool!", exp);
            }

        }
    }

    @Override
    public MaintenanceReason[] listMaintenanceReasons(final String search_pattern) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;
        final String new_search_pattern = search_pattern.replace('*', '%');
        try {
            con = cache.getReadConnectionForConfigDB();

            stmt = con.prepareStatement("SELECT id,text FROM reason_text WHERE text like ?");
            stmt.setString(1, new_search_pattern);

            ResultSet rs = stmt.executeQuery();
            List<MaintenanceReason> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new MaintenanceReason(I(rs.getInt("id")), rs.getString("text")));
            }
            rs.close();

            return list.toArray(new MaintenanceReason[list.size()]);
        } catch (final PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            LOG.error("SQL Error", ecp);
            throw new StorageException(ecp);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                LOG.error("Error closing statement", e);
            }
            try {
                if (con != null) {
                    cache.pushReadConnectionForConfigDB(con);
                }
            } catch (final PoolException exp) {
                LOG.error("Error pushing configdb connection to pool!", exp);
            }
        }
    }

    @Override
    public MaintenanceReason[] getMaintenanceReasons(final int[] reason_id) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = cache.getReadConnectionForConfigDB();

            final StringBuilder sb = new StringBuilder();
            sb.append("SELECT id,text FROM reason_text WHERE id IN ");

            sb.append("(");
            for (int i = 0; i < reason_id.length; i++) {
                sb.append("?,");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(")");

            stmt = con.prepareStatement(sb.toString());
            int stmt_count = 1;
            for (final int element : reason_id) {
                stmt.setInt(stmt_count, element);
                stmt_count++;
            }

            ResultSet rs = stmt.executeQuery();
            List<MaintenanceReason> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new MaintenanceReason(I(rs.getInt("id")), rs.getString("text")));
            }
            rs.close();

            return list.toArray(new MaintenanceReason[list.size()]);
        } catch (final PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            LOG.error("SQL Error", ecp);
            throw new StorageException(ecp);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                LOG.error("Error closing statement", e);
            }
            try {
                if (con != null) {
                    cache.pushReadConnectionForConfigDB(con);
                }
            } catch (final PoolException exp) {
                LOG.error("Error pushing configdb connection to pool!", exp);
            }

        }
    }

    @Override
    public Filestore[] listFilestores(String pattern, boolean omitUsage) throws StorageException {
        List<Filestore> stores;
        {
            Connection con = null;
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                con = cache.getReadConnectionForConfigDB();
                stmt = con.prepareStatement("SELECT id,uri,size,max_context FROM filestore WHERE uri LIKE ?");
                stmt.setString(1, pattern.replace('*', '%'));
                result = stmt.executeQuery();
                if (false == result.next()) {
                    return new Filestore[0];
                }

                stores = new LinkedList<>();
                do {
                    int i = 1;
                    final Filestore fs = new Filestore();
                    fs.setId(I(result.getInt(i++)));
                    fs.setUrl(result.getString(i++));
                    fs.setSize(L(result.getLong(i++)));
                    fs.setMaxContexts(I(result.getInt(i++)));
                    stores.add(fs);
                } while (result.next());
            } catch (final PoolException e) {
                throw new StorageException(e);
            } catch (final SQLException e) {
                throw new StorageException(e.getMessage(), e);
            } finally {
                closeSQLStuff(result, stmt);
                if (null != con) {
                    try {
                        cache.pushReadConnectionForConfigDB(con);
                    } catch (final PoolException e) {
                        LOG.error("Error pushing configdb connection to pool!", e);
                    }
                }
            }
        }

        if (omitUsage) {
            // Convert size to MB
            for (Filestore store : stores) {
                store.setSize(L(toMB(l(store.getSize()))));
            }
        } else {
            updateFilestoresWithRealUsage(stores);
        }

        return stores.toArray(new Filestore[stores.size()]);
    }

    private int nextId(final Connection con) throws SQLException {
        boolean rollback = false;
        try {
            // BEGIN
            con.setAutoCommit(false);
            rollback = true;
            // Acquire next available identifier
            final int id = IDGenerator.getId(con);
            // COMMIT
            con.commit();
            rollback = false;
            return id;
        } finally {
            if (rollback) {
                rollback(con);
            }
        }
    }

    @Override
    public Map<Database, List<String>> deleteEmptyDatabaseSchemas(Database db, int optNumberOfSchemasToKeep) throws StorageException {
        // Determine list of empty schemas to delete
        int numberOfSchemasToKeep = optNumberOfSchemasToKeep;
        boolean schemaSpecified = false;
        Map<Integer, List<Database>> databaseAndSchemasList;
        if (null == db) {
            databaseAndSchemasList = getEmptySchemasFromAllDatabases();
        } else {
            int poolId = db.getId().intValue();
            if (null == db.getScheme()) {
                databaseAndSchemasList = getEmptySchemasFromDatabase(db);
            } else {
                // Both database (host) and schema specified
                schemaSpecified = true;
                numberOfSchemasToKeep = 0;
                databaseAndSchemasList = OXUtilMySQLStorage.<Integer, List<Database>> singletonHashMap(Integer.valueOf(poolId), singletonArrayList(new Database(poolId, db.getScheme())));
            }
        }

        // Mark for deletion
        int newCount = Integer.MAX_VALUE; // Should be greater than CONTEXTS_PER_SCHEMA
        {
            Connection con = null;
            try {
                con = cache.getWriteConnectionForConfigDBNoTimeout();

                for (Iterator<List<Database>> iter = databaseAndSchemasList.values().iterator(); iter.hasNext();) {
                    List<Database> emptySchemas = iter.next();

                    // Check how many empty schemas are allowed to be deleted for current database host
                    int numOfSchemasToDelete = numberOfSchemasToKeep > 0 ? emptySchemas.size() - numberOfSchemasToKeep : emptySchemas.size();
                    if (numOfSchemasToDelete <= 0) {
                        // No schema is allowed to be deleted from current database host
                        iter.remove();
                    } else {
                        // Only keep the ones in 'emptySchemas' list that are allowed to be deleted and could be successfully marked
                        for (Iterator<Database> dbIter = emptySchemas.iterator(); dbIter.hasNext();) {
                            Database emptySchema = dbIter.next();
                            if (numOfSchemasToDelete > 0) {
                                if (tryUpdateDBSchemaCounter(0, newCount, emptySchema, con)) {
                                    // Successfully marked for deletion
                                    LOG.debug("Schema \"{}\" of database {} successfully marked for deletion", emptySchema.getScheme(), emptySchema.getId());
                                    numOfSchemasToDelete--;
                                } else {
                                    if (schemaSpecified) {
                                        throw new StorageException("Schema \"" + db.getScheme() + "\" of database " + db.getId() + " is in use");
                                    }

                                    // Schema in-use in the meantime
                                    dbIter.remove();
                                }
                            } else {
                                // Schema must not be deleted anymore
                                dbIter.remove();
                            }
                        }

                        if (emptySchemas.isEmpty()) {
                            iter.remove();
                        }
                    }
                }
            } catch (PoolException pe) {
                LOG.error("Pool Error", pe);
                throw new StorageException(pe);
            } catch (SQLException ecp) {
                LOG.error("SQL Error", ecp);
                throw new StorageException(ecp);
            } finally {
                if (con != null) {
                    try {
                        cache.pushWriteConnectionForConfigDBNoTimeout(con);
                    } catch (final PoolException e) {
                        LOG.error("Error pushing configdb connection to pool!", e);
                    }
                }
            }
        }

        // Delete reserved ones
        Map<Database, List<String>> deletedSchemas = new LinkedHashMap<>(databaseAndSchemasList.size());
        for (Map.Entry<Integer, List<Database>> databaseAndSchemas : databaseAndSchemasList.entrySet()) {
            int poolId = databaseAndSchemas.getKey().intValue();

            Connection con = null;
            boolean rollback = false;
            try {
                con = cache.getWriteConnectionForConfigDBNoTimeout();

                con.setAutoCommit(false);
                rollback = true;

                // Acquire lock
                cache.getPool().lock(con, poolId);

                List<Database> schemas = databaseAndSchemas.getValue();
                for (Iterator<Database> dbIter = schemas.iterator(); dbIter.hasNext();) {
                    Database schema = dbIter.next();
                    if (newCount != readDBSchemaCounter(schema, con)) {
                        // Schema in-use in the meantime
                        if (schemaSpecified) {
                            throw new StorageException("Schema \"" + db.getScheme() + "\" of database " + db.getId() + " is in use");
                        }
                        dbIter.remove();
                    }
                }

                // Delete the empty schemas from database host (if any)
                int numSchemas = schemas.size();
                if (numSchemas > 0) {
                    List<String> deleted = new ArrayList<>(numSchemas);
                    Database dbHost = null;
                    for (Database schema : schemas) {
                        if (null == dbHost) {
                            dbHost = new Database(schema);
                            dbHost.setScheme(null);
                        }
                        try {
                            OXUtilMySQLStorageCommon.deleteDatabase(schema, con);
                            LOG.info("Deleted empty schema \"{}\" from database {}", schema.getScheme(), schema.getId());
                            deleted.add(schema.getScheme());
                        } catch (StorageException e) {
                            LOG.error("Failed to delete empty schema \"{}\" of database {}", schema.getScheme(), schema.getId(), e);
                        } catch (RuntimeException e) {
                            LOG.error("Failed to delete empty schema \"{}\" of database {}", schema.getScheme(), schema.getId(), e);
                        }
                    }
                    deletedSchemas.put(dbHost, deleted);
                }

                con.commit();
                rollback = false;
            } catch (PoolException pe) {
                LOG.error("Pool Error", pe);
                throw new StorageException(pe);
            } catch (SQLException ecp) {
                LOG.error("SQL Error", ecp);
                throw new StorageException(ecp);
            } finally {
                if (rollback) {
                    rollback(con);
                }
                if (con != null) {
                    try {
                        cache.pushWriteConnectionForConfigDBNoTimeout(con);
                    } catch (final PoolException e) {
                        LOG.error("Error pushing configdb connection to pool!", e);
                    }
                }
            }
        }

        return deletedSchemas;
    }

    private static <K, V> HashMap<K, V> singletonHashMap(K key, V value) {
        HashMap<K, V> hashMap = new HashMap<>(1, 0.9F);
        hashMap.put(key, value);
        return hashMap;
    }

    private static <E> ArrayList<E> singletonArrayList(E element) {
        ArrayList<E> al = new ArrayList<>(1);
        al.add(element);
        return al;
    }

    private Map<Integer, List<Database>> getEmptySchemasFromAllDatabases() throws StorageException {
        return determineEmptySchemas(null);
    }

    private Map<Integer, List<Database>> getEmptySchemasFromDatabase(Database database) throws StorageException {
        return determineEmptySchemas(database);
    }

    private Map<Integer, List<Database>> determineEmptySchemas(Database optDatabase) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getReadConnectionForConfigDB();

            if (null != optDatabase) {
                int poolId = optDatabase.getId().intValue();
                stmt = con.prepareStatement("SELECT schemaname FROM contexts_per_dbschema WHERE db_pool_id=? AND count=0");
                stmt.setInt(1, poolId);
                rs = stmt.executeQuery();
                if (false == rs.next()) {
                    // No empty schemas on given database host
                    return singletonHashMap(optDatabase.getId(), Collections.<Database> emptyList());
                }

                OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
                Database mainDb = tool.loadDatabaseById(poolId);

                List<Database> emptySchemas = new LinkedList<>();
                do {
                    String schema = rs.getString(1);
                    emptySchemas.add(new Database(mainDb, schema));
                } while (rs.next());
                return singletonHashMap(optDatabase.getId(), emptySchemas);
            }

            stmt = con.prepareStatement("SELECT c.write_db_pool_id, s.schemaname FROM db_cluster AS c LEFT JOIN contexts_per_dbschema AS s ON c.write_db_pool_id=s.db_pool_id WHERE count=0");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                // No empty schemas at all
                return Collections.emptyMap();
            }

            OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
            Map<Integer, Database> mainDbCache = new HashMap<>();

            Map<Integer, List<Database>> db2schemas = new LinkedHashMap<>();
            do {
                Integer poolId = Integer.valueOf(rs.getInt(1));
                String schema = rs.getString(2);
                if (null != schema) {
                    Database mainDb = mainDbCache.get(poolId);
                    if (null == mainDb) {
                        mainDb = tool.loadDatabaseById(poolId.intValue());
                        mainDbCache.put(poolId, mainDb);
                    }

                    List<Database> emptySchemas = db2schemas.get(poolId);
                    if (null == emptySchemas) {
                        emptySchemas = new LinkedList<>();
                        db2schemas.put(poolId, emptySchemas);
                    }
                    emptySchemas.add(new Database(mainDb, schema));
                }
            } while (rs.next());
            return db2schemas;
        } catch (PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (SQLException ecp) {
            LOG.error("SQL Error", ecp);
            throw new StorageException(ecp);
        } finally {
            closeSQLStuff(rs, stmt);

            if (null != con) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (final PoolException e) {
                    LOG.error("Error pushing configdb connection to pool!", e);
                }
            }
        }
    }

    private boolean tryUpdateDBSchemaCounter(int expected, int update, Database db, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE contexts_per_dbschema SET count=? WHERE db_pool_id=? AND schemaname=? AND count=?");
            stmt.setInt(1, update);
            stmt.setInt(2, i(db.getId()));
            stmt.setString(3, db.getScheme());
            stmt.setInt(4, expected);
            boolean success = stmt.executeUpdate() > 0;
            closeSQLStuff(stmt);
            stmt = null;
            return success;
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private int readDBSchemaCounter(Database db, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT count FROM contexts_per_dbschema WHERE db_pool_id=? AND schemaname=?");
            stmt.setInt(1, i(db.getId()));
            stmt.setString(2, db.getScheme());
            rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public List<String> createDatabaseSchemas(Database db, int optNumberOfSchemas) throws StorageException {
        int numberOfSchemas = optNumberOfSchemas;
        if (numberOfSchemas <= 0) {
            // Number of schemas not specified; try to auto-determine
            int maxUnits = db.getMaxUnits().intValue();
            if (maxUnits < 0) {
                // Infinite...
                throw new StorageException("Number of schemas cannot be automatically calculated, since max. units is set to \"-1\". Please specify number of schemas explicitly.");
            }

            int maxNumberOfContextsPerSchema = this.maxNumberOfContextsPerSchema;
            if (maxNumberOfContextsPerSchema <= 0) {
                maxNumberOfContextsPerSchema = 1;
            }
            int maxNumberOfSchemas;
            {
                float quotient = maxUnits / (float) maxNumberOfContextsPerSchema;
                maxNumberOfSchemas = (int) quotient;
                if (quotient != maxNumberOfSchemas) {
                    maxNumberOfSchemas++;
                }
            }

            // Check how many are allowed to be created
            List<String> existingSchemas = OXUtilMySQLStorageCommon.listDatabases(db);
            numberOfSchemas = null == existingSchemas || existingSchemas.isEmpty() ? maxNumberOfSchemas : maxNumberOfSchemas - existingSchemas.size();
            if (numberOfSchemas <= 0) {
                // No more schemas are allowed to be created
                throw new StorageException("No more schemas are allowed to be created for database " + db.getId() + ". Please extend max. units or register a new database in order to create more schemas.");
            }
        }

        Connection con = null;
        boolean rollback = false;
        try {
            con = cache.getWriteConnectionForConfigDBNoTimeout();

            con.setAutoCommit(false);
            rollback = true;

            List<String> createdSchemas = doCreateSchemas(db, numberOfSchemas, con);

            con.commit();
            rollback = false;
            return createdSchemas;
        } catch (PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (SQLException ecp) {
            LOG.error("SQL Error", ecp);
            throw new StorageException(ecp);
        } finally {
            if (rollback) {
                rollback(con);
            }
            if (con != null) {
                try {
                    cache.pushWriteConnectionForConfigDBNoTimeout(con);
                } catch (final PoolException e) {
                    LOG.error("Error pushing configdb connection to pool!", e);
                }
            }
        }
    }

    private List<String> doCreateSchemas(Database db, int numberOfSchemas, Connection con) throws StorageException {
        List<String> schemasToRemove = null;
        try {
            schemasToRemove = new ArrayList<>(numberOfSchemas);

            // Create new schemas
            for (int i = numberOfSchemas; i-- > 0;) {
                int schemaUnique;
                try {
                    schemaUnique = IDGenerator.getId(con);
                } catch (SQLException e) {
                    throw new StorageException(e.getMessage(), e);
                }
                String schemaName = db.getName() + '_' + schemaUnique;
                db.setScheme(schemaName);
                OXUtilMySQLStorageCommon.createDatabase(db, con);
                LOG.info("Created empty schema \"{}\" in database {}", schemaName, db.getId());
                schemasToRemove.add(schemaName);
            }

            // All fine
            List<String> createdSchemas = new ArrayList<>(schemasToRemove);
            schemasToRemove = null;
            return createdSchemas;
        } finally {
            if (null != schemasToRemove) {
                // Drop created schemas in case an error occurred
                for (String schemaName : schemasToRemove) {
                    db.setScheme(schemaName);
                    OXUtilMySQLStorageCommon.deleteDatabase(db, con);
                    LOG.info("Error during schema creation. Deleted previously created schema \"{}\" from database {}", schemaName, db.getId());
                }
            }
        }
    }

    @Override
    public int registerDatabase(Database db, boolean createSchemas, int optNumberOfSchemas) throws StorageException {
        boolean master = null != db.isMaster() && db.isMaster().booleanValue();
        int maxUnits = db.getMaxUnits().intValue();
        int clusterWeight = 0;
        if (master) {
            clusterWeight = 100;
        }

        int numberOfSchemas = 0;
        if (createSchemas) {
            if (false == master) {
                throw new StorageException("Schemas cannot be created for slave database hosts.");
            }

            numberOfSchemas = optNumberOfSchemas;
            if (numberOfSchemas <= 0) {
                // Number of schemas not specified; try to auto-determine
                if (maxUnits < 0) {
                    // Infinite...
                    throw new StorageException("Number of schemas cannot be automatically calculated, since max. units is set to \"-1\". Please specify number of schemas explicitly.");
                }

                int maxNumberOfContextsPerSchema = this.maxNumberOfContextsPerSchema;
                if (maxNumberOfContextsPerSchema <= 0) {
                    maxNumberOfContextsPerSchema = 1;
                }
                {
                    float quotient = maxUnits / (float) maxNumberOfContextsPerSchema;
                    numberOfSchemas = (int) quotient;
                    if (quotient != numberOfSchemas) {
                        numberOfSchemas++;
                    }
                }
            }
        }

        Connection con = null;
        boolean rollback = false;
        try {
            con = createSchemas ? cache.getWriteConnectionForConfigDBNoTimeout() : cache.getWriteConnectionForConfigDB();

            int databaseId = nextId(con);
            int clusterId = master ? nextId(con) : -1;

            con.setAutoCommit(false);
            rollback = true;

            lock(con);

            doRegisterDatabase(databaseId, clusterId, db, master, maxUnits, clusterWeight, numberOfSchemas, con);

            con.commit();
            rollback = false;
            return databaseId;
        } catch (PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (SQLException ecp) {
            LOG.error("SQL Error", ecp);
            throw new StorageException(ecp);
        } finally {
            if (rollback) {
                rollback(con);
            }
            if (con != null) {
                try {
                    if (createSchemas) {
                        cache.pushWriteConnectionForConfigDBNoTimeout(con);
                    } else {
                        cache.pushWriteConnectionForConfigDB(con);
                    }
                } catch (final PoolException e) {
                    LOG.error("Error pushing configdb connection to pool!", e);
                }
            }
        }
    }

    private void doRegisterDatabase(int databaseId, int clusterId, Database db, boolean master, int maxUnits, int clusterWeight, int numberOfSchemas, Connection con) throws StorageException {
        List<String> schemasToRemove = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            prep = con.prepareStatement("INSERT INTO db_pool VALUES (?,?,?,?,?,?,?,?,?);");
            prep.setInt(1, databaseId);
            if (db.getUrl() != null) {
                prep.setString(2, db.getUrl());
            } else {
                prep.setNull(2, Types.VARCHAR);
            }
            if (db.getDriver() != null) {
                prep.setString(3, db.getDriver());
            } else {
                prep.setNull(3, Types.VARCHAR);
            }
            if (db.getLogin() != null) {
                prep.setString(4, db.getLogin());
            } else {
                prep.setNull(4, Types.VARCHAR);
            }
            if (db.getPassword() != null) {
                prep.setString(5, db.getPassword());
            } else {
                prep.setNull(5, Types.VARCHAR);
            }
            prep.setInt(6, db.getPoolHardLimit().intValue());
            prep.setInt(7, db.getPoolMax().intValue());
            prep.setInt(8, db.getPoolInitial().intValue());
            if (db.getName() != null) {
                prep.setString(9, db.getName());
            } else {
                prep.setNull(9, Types.VARCHAR);
            }

            prep.executeUpdate();
            prep.close();
            prep = null;

            if (master) {
                prep = con.prepareStatement("INSERT INTO db_cluster (cluster_id, read_db_pool_id, write_db_pool_id, weight, max_units) VALUES (?,?,?,?,?);");
                prep.setInt(1, clusterId);

                // I am the master, set read_db_pool_id = 0
                prep.setInt(2, 0);
                prep.setInt(3, databaseId);
                prep.setInt(4, clusterWeight);
                prep.setInt(5, maxUnits);
                prep.executeUpdate();
                prep.close();
                prep = null;

                // update counter table
                prep = con.prepareStatement("INSERT INTO contexts_per_dbpool (db_pool_id,count) VALUES(?,0);");
                prep.setInt(1, databaseId);
                prep.executeUpdate();
                prep.close();
                prep = null;

                // update lock table
                prep = con.prepareStatement("INSERT INTO dbpool_lock (db_pool_id) VALUES(?);");
                prep.setInt(1, databaseId);
                prep.executeUpdate();
                prep.close();
                prep = null;
            } else {
                prep = con.prepareStatement("SELECT db_pool_id FROM db_pool WHERE db_pool_id = ?");
                prep.setInt(1, db.getMasterId().intValue());
                rs = prep.executeQuery();
                if (!rs.next()) {
                    throw new StorageException("No such master with ID=" + db.getMasterId());
                }
                rs.close();
                rs = null;
                prep.close();
                prep = null;

                prep = con.prepareStatement("SELECT cluster_id FROM db_cluster WHERE write_db_pool_id = ?");
                prep.setInt(1, db.getMasterId().intValue());
                rs = prep.executeQuery();
                if (!rs.next()) {
                    throw new StorageException("No such master with ID=" + db.getMasterId() + " IN db_cluster TABLE");
                }
                final int cluster_id = rs.getInt("cluster_id");
                rs.close();
                rs = null;
                prep.close();
                prep = null;

                prep = con.prepareStatement("UPDATE db_cluster SET read_db_pool_id=? WHERE cluster_id=?;");
                prep.setInt(1, databaseId);
                prep.setInt(2, cluster_id);
                prep.executeUpdate();
                prep.close();
                prep = null;

                prep = con.prepareStatement("UPDATE context_server2db_pool SET read_db_pool_id = ? WHERE write_db_pool_id = ?");
                prep.setInt(1, databaseId);
                prep.setInt(2, db.getMasterId().intValue());
                prep.executeUpdate();
                prep.close();
                prep = null;

            }

            db.setId(Integer.valueOf(databaseId));

            if (numberOfSchemas > 0) {
                schemasToRemove = new ArrayList<>(numberOfSchemas);

                // Create new schemas
                for (int i = numberOfSchemas; i-- > 0;) {
                    int schemaUnique;
                    try {
                        schemaUnique = IDGenerator.getId(con);
                    } catch (SQLException e) {
                        throw new StorageException(e.getMessage(), e);
                    }
                    String schemaName = db.getName() + '_' + schemaUnique;
                    db.setScheme(schemaName);
                    OXUtilMySQLStorageCommon.createDatabase(db, con);
                    LOG.info("Created empty schema \"{}\" in database {}", schemaName, db.getId());
                    schemasToRemove.add(schemaName);
                }

                // All fine
                schemasToRemove = null;
            }
        } catch (DataTruncation dt) {
            LOG.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (SQLException ecp) {
            LOG.error("SQL Error", ecp);
            throw new StorageException(ecp);
        } finally {
            closeSQLStuff(rs, prep);

            if (null != schemasToRemove) {
                // Drop created schemas in case an error occurred
                for (String schemaName : schemasToRemove) {
                    db.setScheme(schemaName);
                    OXUtilMySQLStorageCommon.deleteDatabase(db, con);
                    LOG.info("Error during schema creation. Deleted previously created schema \"{}\" from database {}", schemaName, db.getId());
                }
            }
        }
    }

    @Override
    public int registerFilestore(final Filestore fstore) throws StorageException {
        Connection con = null;
        long store_size = fstore.getSize().longValue();
        final Long l_max = MAX_FILESTORE_CAPACITY;
        if (store_size > l_max.longValue()) {
            throw new StorageException("Filestore size to large for database (max=" + l_max.longValue() + ")");
        }
        store_size = store_size << 20;
        PreparedStatement stmt = null;
        int rollback = 0;
        try {
            con = cache.getWriteConnectionForConfigDB();

            final int fstore_id = nextId(con);

            con.setAutoCommit(false);
            rollback = 1;
            stmt = con.prepareStatement("INSERT INTO filestore (id,uri,size,max_context) VALUES (?,?,?,?)");
            stmt.setInt(1, fstore_id);
            stmt.setString(2, fstore.getUrl());
            stmt.setLong(3, store_size);
            stmt.setInt(4, fstore.getMaxContexts().intValue());
            stmt.executeUpdate();
            stmt.close();

            // update counter table
            stmt = con.prepareStatement("INSERT INTO contexts_per_filestore (filestore_id,count) VALUES (?,0)");
            stmt.setInt(1, fstore_id);
            stmt.executeUpdate();

            con.commit();
            rollback = 2;

            return fstore_id;
        } catch (final DataTruncation dt) {
            LOG.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            LOG.error("SQL Error", ecp);
            throw new StorageException(ecp);
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    rollback(con);
                }
            }
            closeSQLStuff(stmt);
            if (con != null) {
                try {
                    cache.pushWriteConnectionForConfigDB(con);
                } catch (final PoolException exp) {
                    LOG.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    @Override
    public int getFilestoreIdFromContext(int contextId) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getReadConnectionForConfigDB();

            stmt = con.prepareStatement("SELECT filestore_id FROM context WHERE cid=?");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new StorageException("There is no context with identifier " + contextId);
            }

            return rs.getInt(1);
        } catch (final SQLException ecp) {
            LOG.error("SQL Error", ecp);
            throw new StorageException(ecp);
        } catch (final PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } finally {
            closeSQLStuff(rs, stmt);
            if (con != null) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (final PoolException exp) {
                    LOG.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    @Override
    public Filestore findFilestoreForUser(int fileStoreId) throws StorageException {
        if (fileStoreId > 0) {
            Filestore filestore = getFilestore(fileStoreId, false);
            if (enoughSpaceForUser(filestore)) {
                return filestore;
            }
        }

        return findFilestoreForEntity(false, null);
    }

    @Override
    public Filestore findFilestoreForContext() throws StorageException {
        return findFilestoreForEntity(true, null);
    }

    @Override
    public Filestore findFilestoreForContext(Connection configDbCon) throws StorageException {
        return findFilestoreForEntity(true, configDbCon);
    }

    /**
     * Performs a wait according to exponential back-off strategy.
     * <pre>
     * (retry-count * base-millis) + random-millis
     * </pre>
     *
     * @param retryCount The current number of retries
     * @param baseMillis The base milliseconds
     */
    private static void exponentialBackoffWait(int retryCount, long baseMillis) {
        long nanosToWait = TimeUnit.NANOSECONDS.convert((retryCount * baseMillis) + ((long) (Math.random() * baseMillis)), TimeUnit.MILLISECONDS);
        LockSupport.parkNanos(nanosToWait);
    }

    private boolean tryIncrementFilestoreCounter(int filestoreId, int currentCount, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            // Try to update counter
            stmt = con.prepareStatement("UPDATE contexts_per_filestore SET count=? WHERE filestore_id=? AND count=?");
            stmt.setInt(1, currentCount + 1);
            stmt.setInt(2, filestoreId);
            stmt.setInt(3, currentCount);
            return stmt.executeUpdate() > 0;
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private Filestore findFilestoreForEntity(boolean forContext, Connection configDbCon) throws StorageException {
        Connection con = null;
        boolean readOnly = true;
        boolean manageConnection = true;
        if (null != configDbCon) {
            con = configDbCon;
            readOnly = false;
            manageConnection = false;
        }

        // Define candidate class
        class Candidate {

            final int id;
            final int maxNumberOfEntities;
            final int numberOfEntities;
            final String uri;
            final long size;

            Candidate(int id, int maxNumberOfEntities, int numberOfEntities, String uri, long size) {
                super();
                this.id = id;
                this.maxNumberOfEntities = maxNumberOfEntities;
                this.numberOfEntities = numberOfEntities;
                this.uri = uri;
                this.size = size;
            }
        }

        boolean loadRealUsage = false;

        if (forContext) {
            int retryCount = 0;
            while (true) {
                PreparedStatement stmt = null;
                ResultSet rs = null;
                try {
                    if (manageConnection) {
                        con = cache.getWriteConnectionForConfigDB();
                        readOnly = false;
                    }
                    if (null == con) {
                        throw new StorageException("Missing connection to ConfigDB"); // Keep IDE happy...
                    }

                    // GROUP BY CLAUSE: ensure ONLY_FULL_GROUP_BY compatibility
                    stmt = con.prepareStatement("SELECT filestore.id, filestore.max_context, contexts_per_filestore.count AS num, filestore.uri, filestore.size FROM filestore LEFT JOIN contexts_per_filestore ON filestore.id=contexts_per_filestore.filestore_id GROUP BY filestore.id ORDER BY num ASC");
                    rs = stmt.executeQuery();
                    if (false == rs.next()) {
                        throw new StorageException("No filestore found");
                    }

                    // Iterate candidates
                    boolean checkNext = true;
                    do {
                        // Create appropriate candidate instance
                        int maxNumberOfEntities = rs.getInt(2);
                        if (maxNumberOfEntities > 0) {
                            int numberOfContexts = rs.getInt(3); // In case NULL, then 0 (zero) is returned
                            Candidate candidate = new Candidate(rs.getInt(1), maxNumberOfEntities, numberOfContexts, rs.getString(4), toMB(rs.getLong(5)));

                            int entityCount = candidate.numberOfEntities;

                            // Get user count information
                            int count = Filestore2UserUtil.getUserCountFor(candidate.id, this.cache);
                            if (count > 0) {
                                entityCount += count;
                            }

                            if (entityCount < candidate.maxNumberOfEntities) {
                                FilestoreUsage userFilestoreUsage = new FilestoreUsage(count <= 0 ? 0 : count, 0L);

                                // Create filestore instance from candidate
                                Filestore filestore = new Filestore();
                                filestore.setId(I(candidate.id));
                                filestore.setUrl(candidate.uri);
                                filestore.setSize(L(candidate.size));
                                filestore.setMaxContexts(I(candidate.maxNumberOfEntities));

                                // Possible to pre-set context usage?
                                FilestoreUsage contextFilestoreUsage = null;
                                if (false == loadRealUsage) {
                                    // Only consider context count for given file storage
                                    contextFilestoreUsage = new FilestoreUsage(numberOfContexts, 0L);
                                }

                                loadFilestoreUsageFor(filestore, loadRealUsage, contextFilestoreUsage, userFilestoreUsage, con);

                                if (enoughSpaceForContext(filestore)) {
                                    // Try to atomically increment filestore counter while preserving max. number of entities condition
                                    if (tryIncrementFilestoreCounter(candidate.id, numberOfContexts, con)) {
                                        // Return...
                                        return filestore;
                                    }

                                    checkNext = false;
                                }
                            }
                        }
                    } while (checkNext && rs.next());

                    if (checkNext) { // Loop was exited because rs.next() returned false
                        // No suitable filestore found
                        throw new StorageException("No usable or suitable filestore found");
                    }
                } catch (DataTruncation dt) {
                    LOG.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
                    throw AdminCache.parseDataTruncation(dt);
                } catch (SQLException ecp) {
                    LOG.error("SQL Error", ecp);
                    throw new StorageException(ecp);
                } catch (PoolException pe) {
                    LOG.error("Pool Error", pe);
                    throw new StorageException(pe);
                } finally {
                    closeSQLStuff(rs, stmt);
                    if (manageConnection && con != null) {
                        try {
                            if (readOnly) {
                                cache.pushReadConnectionForConfigDB(con);
                            } else {
                                cache.pushWriteConnectionForConfigDB(con);
                            }
                        } catch (final PoolException exp) {
                            LOG.error("Error pushing configdb connection to pool!", exp);
                        }
                    }
                }

                // Exponential back-off
                exponentialBackoffWait(++retryCount, 1000L);
            }
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (manageConnection) {
                con = cache.getReadConnectionForConfigDB();
            }
            if (null == con) {
                throw new StorageException("Missing connection to ConfigDB"); // Keep IDE happy...
            }

            // Load potential candidates
            List<Candidate> candidates = new LinkedList<>();
            {
                // GROUP BY CLAUSE: ensure ONLY_FULL_GROUP_BY compatibility
                stmt = con.prepareStatement("SELECT filestore.id, filestore.max_context, contexts_per_filestore.count AS num, filestore.uri, filestore.size FROM filestore LEFT JOIN contexts_per_filestore ON filestore.id=contexts_per_filestore.filestore_id GROUP BY filestore.id ORDER BY num ASC");
                rs = stmt.executeQuery();

                if (!rs.next()) {
                    // None found
                    throw new StorageException("No filestore found");
                }

                do {
                    int maxNumberOfContexts = rs.getInt(2);
                    if (maxNumberOfContexts > 0) {
                        int numberOfEntities = rs.getInt(3); // In case NULL, then 0 (zero) is returned
                        candidates.add(new Candidate(rs.getInt(1), maxNumberOfContexts, numberOfEntities, rs.getString(4), toMB(rs.getLong(5))));
                    }
                } while (rs.next());

                // Close resources
                closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;
            }

            // Determine file storage user/context counts
            //FilestoreCountCollection filestoreUserCounts = getFilestoreUserCounts();

            // Find a suitable one from ordered list of candidates
            NextCandidate: for (Candidate candidate : candidates) {
                int entityCount = candidate.numberOfEntities;

                // Get user count information
                int count = Filestore2UserUtil.getUserCountFor(candidate.id, this.cache);
                if (count > 0) {
                    entityCount += count;
                }

                if (entityCount < candidate.maxNumberOfEntities) {
                    FilestoreUsage userFilestoreUsage = new FilestoreUsage(count, 0L);

                    // Create filestore instance froim candidate
                    Filestore filestore = new Filestore();
                    filestore.setId(I(candidate.id));
                    filestore.setUrl(candidate.uri);
                    filestore.setSize(L(candidate.size));
                    filestore.setMaxContexts(I(candidate.maxNumberOfEntities));

                    // Possible to pre-set context usage?
                    FilestoreUsage contextFilestoreUsage = null;
                    if (false == loadRealUsage) {
                        // Only consider context count for given file storage
                        int numberOfEntities = candidate.numberOfEntities;
                        contextFilestoreUsage = new FilestoreUsage(numberOfEntities, 0L);
                    }

                    loadFilestoreUsageFor(filestore, loadRealUsage, contextFilestoreUsage, userFilestoreUsage, con);

                    if (forContext) {
                        if (enoughSpaceForContext(filestore)) {
                            // Switch from read-only to read-write connection
                            closeSQLStuff(rs, stmt);
                            if (manageConnection && readOnly) {
                                cache.pushReadConnectionForConfigDB(con);
                                con = null;
                                con = cache.getWriteConnectionForConfigDB();
                                readOnly = false;
                            }

                            // Try to atomically increment filestore counter while preserving max. number of entities condition
                            boolean first = true;
                            boolean success;
                            int current;
                            do {
                                if (first) {
                                    current = candidate.numberOfEntities;
                                    first = false;
                                } else {
                                    current = getCurrentCountFor(filestore.getId().intValue(), con);
                                    if (current + (count <= 0 ? 0 : count) >= candidate.maxNumberOfEntities) {
                                        // Exceeded... repeat with next candidate
                                        continue NextCandidate;
                                    }
                                }
                                success = optIncrement(filestore.getId().intValue(), current, con);
                            } while (false == success);

                            // Return...
                            return filestore;
                        }
                    } else {
                        if (enoughSpaceForUser(filestore)) {
                            return filestore;
                        }
                    }
                }
            }

            // None found
            throw new StorageException("No usable or suitable filestore found");
        } catch (final DataTruncation dt) {
            LOG.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final SQLException ecp) {
            LOG.error("SQL Error", ecp);
            throw new StorageException(ecp);
        } catch (final PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } finally {
            closeSQLStuff(rs, stmt);
            if (manageConnection && con != null) {
                try {
                    if (readOnly) {
                        cache.pushReadConnectionForConfigDB(con);
                    } else {
                        cache.pushWriteConnectionForConfigDB(con);
                    }
                } catch (final PoolException exp) {
                    LOG.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    private boolean optIncrement(int filestoreId, int current, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE contexts_per_filestore SET count=? WHERE filestore_id=? AND count=?");
            stmt.setInt(1, current + 1);
            stmt.setInt(2, filestoreId);
            stmt.setInt(3, current);
            return stmt.executeUpdate() > 0;
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private int getCurrentCountFor(int filestoreId, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT count FROM contexts_per_filestore WHERE filestore_id=?");
            stmt.setInt(1, filestoreId);
            rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public boolean hasSpaceForAnotherUser(Filestore filestore) throws StorageException {
        return !isEntityLimitReached(filestore) && enoughSpaceForUser(filestore);
    }

    @Override
    public boolean hasSpaceForAnotherContext(Filestore filestore) throws StorageException {
        return !isEntityLimitReached(filestore) && enoughSpaceForContext(filestore);
    }

    private boolean isEntityLimitReached(Filestore filestore) {
        // 0 is the special value for not adding contexts to this filestore.
        // and check if the current contexts stored in the filestore reach the maximum context value for the filestore.
        int maxContexts = filestore.getMaxContexts().intValue();
        return 0 == maxContexts || filestore.getCurrentContexts().intValue() >= maxContexts;
    }

    private boolean enoughSpaceForContext(Filestore filestore) throws StorageException {
        long averageSize = getAverageFilestoreSpaceForContext();
        return filestore.getReserved().longValue() + averageSize <= filestore.getSize().longValue();
    }

    private boolean enoughSpaceForUser(Filestore filestore) throws StorageException {
        long averageSize = getAverageFilestoreSpaceForUser();
        return filestore.getReserved().longValue() + averageSize <= filestore.getSize().longValue();
    }

    /**
     * @return the configured average file store space per context in mega bytes (MB).
     * @throws StorageException if parsing the configuration option fails.
     */
    private long getAverageFilestoreSpaceForContext() throws StorageException {
        final String value = prop.getProp("AVERAGE_CONTEXT_SIZE", "200");
        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException e) {
            LOG.error("Unable to parse average context filestore space from configuration file.", e);
            throw new StorageException("Unable to parse average context filestore space from configuration file.", e);
        }
    }

    /**
     * @return the configured average file store space per user in mega bytes (MB).
     * @throws StorageException if parsing the configuration option fails.
     */
    private long getAverageFilestoreSpaceForUser() throws StorageException {
        String value = prop.getUserProp("AVERAGE_USER_SIZE", "100");
        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException e) {
            LOG.error("Unable to parse average user filestore space from configuration file.", e);
            throw new StorageException("Unable to parse average user filestore space from configuration file.", e);
        }
    }

    @Override
    public int registerServer(final String serverName) throws StorageException {
        Connection con = null;
        PreparedStatement prep = null;
        boolean rollback = false;
        try {
            con = cache.getWriteConnectionForConfigDB();

            final int srv_id = nextId(con);

            con.setAutoCommit(false);
            rollback = true;
            prep = con.prepareStatement("INSERT INTO server VALUES (?,?);");
            prep.setInt(1, srv_id);
            if (serverName != null) {
                prep.setString(2, serverName);
            } else {
                prep.setNull(2, Types.VARCHAR);
            }
            prep.executeUpdate();
            prep.close();
            con.commit();
            rollback = false;

            return srv_id;
        } catch (final DataTruncation dt) {
            LOG.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            LOG.error("SQL Error", ecp);
            throw new StorageException(ecp);
        } finally {
            if (rollback) {
                rollback(con);
            }
            closeSQLStuff(prep);
            if (con != null) {
                try {
                    cache.pushWriteConnectionForConfigDB(con);
                } catch (final PoolException e) {
                    LOG.error("Error pushing configdb connection to pool!", e);
                }
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.admin.storage.interfaces.OXUtilStorageInterface#changeServer(int, java.lang.String)
     */
    @Override
    public void changeServer(int serverId, String schemaName) throws StorageException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        boolean rollback = false;
        try {
            connection = cache.getWriteConnectionForConfigDB();
            connection.setAutoCommit(false);
            rollback = true;

            isSchemaDisabled(connection, schemaName);

            int parameterIndex = 1;
            statement = connection.prepareStatement("UPDATE context_server2db_pool SET server_id=? WHERE db_schema=?");
            statement.setInt(parameterIndex++, serverId);
            statement.setString(parameterIndex++, schemaName);

            int rows = statement.executeUpdate();
            if (rows <= 0) {
                throw new StorageException("Unable to change to server '" + serverId + "' for the specified schema '" + schemaName + "'. The schema is empty.");
            }

            connection.commit();
            rollback = false;
        } catch (final PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            LOG.error("SQL Error", ecp);
            throw new StorageException(ecp);
        } finally {
            if (rollback) {
                rollback(connection);
            }
            closeSQLStuff(resultSet, statement);

            if (connection != null) {
                try {
                    cache.pushWriteConnectionForConfigDB(connection);
                } catch (final PoolException exp) {
                    LOG.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    @Override
    public Database[] searchForDatabaseSchema(String searchPattern, boolean onlyEmptySchemas) throws StorageException {
        String searchPatternToUse = searchPattern.replace('*', '%');
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = cache.getReadConnectionForConfigDB();

            if ("%".equals(searchPatternToUse)) {
                pstmt = con.prepareStatement("SELECT d.db_pool_id,d.url,d.driver,d.login,d.password,d.hardlimit,d.max,d.initial,d.name,c.max_units,c.read_db_pool_id,c.write_db_pool_id,p.count,s.schemaname,s.count FROM db_pool AS d JOIN db_cluster AS c ON c.write_db_pool_id=d.db_pool_id LEFT JOIN contexts_per_dbpool AS p ON d.db_pool_id=p.db_pool_id LEFT JOIN contexts_per_dbschema AS s ON d.db_pool_id=s.db_pool_id WHERE (c.max_units <> 0)" + (onlyEmptySchemas ? " AND s.count=0" : ""));
            } else {
                pstmt = con.prepareStatement("SELECT d.db_pool_id,d.url,d.driver,d.login,d.password,d.hardlimit,d.max,d.initial,d.name,c.max_units,c.read_db_pool_id,c.write_db_pool_id,p.count,s.schemaname,s.count FROM db_pool AS d JOIN db_cluster AS c ON c.write_db_pool_id=d.db_pool_id LEFT JOIN contexts_per_dbpool AS p ON d.db_pool_id=p.db_pool_id LEFT JOIN contexts_per_dbschema AS s ON d.db_pool_id=s.db_pool_id WHERE (c.max_units <> 0) AND (d.name LIKE ? OR s.schemaname LIKE ? OR d.db_pool_id LIKE ? OR d.url LIKE ?)" + (onlyEmptySchemas ? " AND s.count=0" : ""));
                pstmt.setString(1, searchPatternToUse);
                pstmt.setString(2, searchPatternToUse);
                pstmt.setString(3, searchPatternToUse);
                pstmt.setString(4, searchPatternToUse);
            }
            rs = pstmt.executeQuery();
            if (false == rs.next()) {
                return new Database[0];
            }

            int maxNumberOfContextsPerSchema = this.maxNumberOfContextsPerSchema;
            List<Database> tmp = new ArrayList<>();
            do {
                Boolean ismaster = Boolean.TRUE;
                final int id = rs.getInt("d.db_pool_id");
                int masterid = 0;
                int nrcontexts = rs.getInt("s.count");
                if (false == rs.wasNull()) {
                    Database db = new Database();
                    db.setCurrentUnits(I(nrcontexts));
                    db.setName(rs.getString("d.name"));
                    db.setDriver(rs.getString("d.driver"));
                    db.setId(I(id));
                    db.setLogin(rs.getString("d.login"));
                    db.setMaster(ismaster);
                    db.setMasterId(I(masterid));
                    db.setMaxUnits(I(maxNumberOfContextsPerSchema));
                    db.setPassword(rs.getString("d.password"));
                    db.setPoolHardLimit(I(rs.getInt("d.hardlimit")));
                    db.setPoolInitial(I(rs.getInt("d.initial")));
                    db.setPoolMax(I(rs.getInt("d.max")));
                    db.setUrl(rs.getString("d.url"));
                    db.setScheme(rs.getString("s.schemaname"));
                    tmp.add(db);
                }
            } while (rs.next());

            return tmp.toArray(new Database[tmp.size()]);
        } catch (final PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            LOG.error("SQL Error", ecp);
            throw new StorageException(ecp);
        } finally {
            closeSQLStuff(rs, pstmt);

            if (con != null) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (final PoolException exp) {
                    LOG.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    @Override
    public Map<Database, Integer> countDatabaseSchema(String search_pattern, boolean onlyEmptySchemas) throws StorageException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = cache.getReadConnectionForConfigDB();
            String my_search_pattern = search_pattern.replace('*', '%');

            if (onlyEmptySchemas) {
                pstmt = con.prepareStatement("SELECT d.db_pool_id,d.url,d.driver,d.login,d.password,d.hardlimit,d.max,d.initial,d.name,c.max_units,c.read_db_pool_id,p.count,s.schemaname,s.count FROM db_pool AS d JOIN db_cluster AS c ON c.write_db_pool_id=d.db_pool_id LEFT JOIN contexts_per_dbpool AS p ON d.db_pool_id=p.db_pool_id LEFT JOIN contexts_per_dbschema AS s ON d.db_pool_id=s.db_pool_id WHERE (d.name LIKE ? OR d.db_pool_id LIKE ? OR d.url LIKE ?) AND s.count=0");
            } else {
                pstmt = con.prepareStatement("SELECT d.db_pool_id,d.url,d.driver,d.login,d.password,d.hardlimit,d.max,d.initial,d.name,c.max_units,c.read_db_pool_id,p.count,s.schemaname,s.count FROM db_pool AS d JOIN db_cluster AS c ON c.write_db_pool_id=d.db_pool_id LEFT JOIN contexts_per_dbpool AS p ON d.db_pool_id=p.db_pool_id LEFT JOIN contexts_per_dbschema AS s ON d.db_pool_id=s.db_pool_id WHERE d.name LIKE ? OR d.db_pool_id LIKE ? OR d.url LIKE ?");
            }
            pstmt.setString(1, my_search_pattern);
            pstmt.setString(2, my_search_pattern);
            pstmt.setString(3, my_search_pattern);
            rs = pstmt.executeQuery();

            if (false == rs.next()) {
                return Collections.emptyMap();
            }

            class Counter {

                int count = 0;

                Counter(int initial) {
                    super();
                    this.count = initial;
                }

                void increment() {
                    count++;
                }

                Integer getCount() {
                    return I(count);
                }
            }

            Map<Integer, Database> id2db = new HashMap<>(); // Ensures that there is only one Database instance associated with a counter
            Map<Database, Counter> counters = new LinkedHashMap<>();
            do {
                int maxUnits = rs.getInt("c.max_units");
                if (maxUnits != 0) {
                    String schemaName = rs.getString("s.schemaname");
                    if (null != schemaName && false == rs.wasNull()) {
                        int id = rs.getInt("d.db_pool_id");
                        Database db = id2db.get(I(id));
                        if (null == db) {
                            db = new Database();
                            int nrcontexts = rs.getInt("p.count");
                            db.setName(rs.getString("d.name"));
                            db.setDriver(rs.getString("d.driver"));
                            db.setId(I(id));
                            db.setLogin(rs.getString("d.login"));
                            db.setMaster(Boolean.TRUE);
                            db.setMasterId(I(0));
                            db.setMaxUnits(I(maxUnits));
                            db.setPassword(rs.getString("d.password"));
                            db.setPoolHardLimit(I(rs.getInt("d.hardlimit")));
                            db.setPoolInitial(I(rs.getInt("d.initial")));
                            db.setPoolMax(I(rs.getInt("d.max")));
                            db.setUrl(rs.getString("d.url"));
                            db.setCurrentUnits(I(nrcontexts));
                            id2db.put(I(id), db);
                        }

                        Counter counter = counters.get(db);
                        if (null == counter) {
                            counter = new Counter(0);
                            counters.put(db, counter);
                        }
                        counter.increment();
                    }
                }
            } while (rs.next());

            Map<Database, Integer> retval = new LinkedHashMap<>();
            for (Map.Entry<Database, Counter> countEntry : counters.entrySet()) {
                retval.put(countEntry.getKey(), countEntry.getValue().getCount());
            }
            return retval;
        } catch (final PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            LOG.error("SQL Error", ecp);
            throw new StorageException(ecp);
        } finally {
            closeSQLStuff(rs, pstmt);

            if (con != null) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (final PoolException exp) {
                    LOG.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    @Override
    public Database[] searchForDatabase(String searchPattern) throws StorageException {
        String searchPatternToUse = searchPattern.replace('*', '%');
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = cache.getReadConnectionForConfigDB();
            if ("%".equals(searchPatternToUse)) {
                pstmt = con.prepareStatement("SELECT d.db_pool_id,d.url,d.driver,d.login,d.password,d.hardlimit,d.max,d.initial,d.name,c.max_units,c.read_db_pool_id,c.write_db_pool_id,p.count FROM db_pool AS d JOIN db_cluster AS c ON (c.write_db_pool_id=d.db_pool_id OR c.read_db_pool_id=d.db_pool_id) LEFT JOIN contexts_per_dbpool AS p ON d.db_pool_id=p.db_pool_id");
            } else {
                pstmt = con.prepareStatement("SELECT d.db_pool_id,d.url,d.driver,d.login,d.password,d.hardlimit,d.max,d.initial,d.name,c.max_units,c.read_db_pool_id,c.write_db_pool_id,p.count FROM db_pool AS d JOIN db_cluster AS c ON (c.write_db_pool_id=d.db_pool_id OR c.read_db_pool_id=d.db_pool_id) LEFT JOIN contexts_per_dbpool AS p ON d.db_pool_id=p.db_pool_id WHERE (d.name LIKE ? OR d.db_pool_id LIKE ? OR d.url LIKE ?)");
                pstmt.setString(1, searchPatternToUse);
                pstmt.setString(2, searchPatternToUse);
                pstmt.setString(3, searchPatternToUse);
            }
            rs = pstmt.executeQuery();

            if (false == rs.next()) {
                return new Database[0];
            }

            List<Database> tmp = new ArrayList<>();
            do {
                Database db = new Database();

                Boolean ismaster = Boolean.TRUE;
                int readid = rs.getInt("c.read_db_pool_id");
                int writeid = rs.getInt("c.write_db_pool_id");
                int id = rs.getInt("d.db_pool_id");
                int maxNumberOfContext = rs.getInt("c.max_units");
                int masterid = 0;
                int nrcontexts = 0;
                if (readid == id) {
                    ismaster = Boolean.FALSE;
                    masterid = writeid;
                } else {
                    // we are master
                    nrcontexts = rs.getInt("p.count");
                    if (maxNumberOfContext != 0 && rs.wasNull()) {
                        throw new StorageException("Unable to count contexts. Consider running 'checkcountsconsistency' command-line tool to correct it.");
                    }
                }
                db.setName(rs.getString("d.name"));
                db.setDriver(rs.getString("d.driver"));
                db.setId(I(id));
                db.setLogin(rs.getString("d.login"));
                db.setMaster(ismaster);
                db.setMasterId(I(masterid));
                db.setMaxUnits(I(maxNumberOfContext));
                db.setPassword(rs.getString("d.password"));
                db.setPoolHardLimit(I(rs.getInt("d.hardlimit")));
                db.setPoolInitial(I(rs.getInt("d.initial")));
                db.setPoolMax(I(rs.getInt("d.max")));
                db.setUrl(rs.getString("d.url"));
                db.setCurrentUnits(I(nrcontexts));
                tmp.add(db);
            } while (rs.next());
            closeSQLStuff(rs, pstmt);
            rs = null;
            pstmt = null;

            return tmp.toArray(new Database[tmp.size()]);
        } catch (final PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            LOG.error("SQL Error", ecp);
            throw new StorageException(ecp);
        } finally {
            closeSQLStuff(rs, pstmt);

            if (con != null) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (final PoolException exp) {
                    LOG.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    @Override
    public Server[] searchForServer(final String search_pattern) throws StorageException {

        Connection con = null;
        PreparedStatement stmt = null;
        try {

            con = cache.getReadConnectionForConfigDB();

            stmt = con.prepareStatement("SELECT name,server_id FROM server WHERE name LIKE ? OR server_id = ?");

            final String my_search_pattern = search_pattern.replace('*', '%');
            stmt.setString(1, my_search_pattern);
            stmt.setString(2, my_search_pattern);

            final ResultSet rs = stmt.executeQuery();
            final ArrayList<Server> tmp = new ArrayList<>();
            while (rs.next()) {
                final Server srv = new Server();
                srv.setId(I(rs.getInt("server_id")));
                srv.setName(rs.getString("name"));
                tmp.add(srv);
            }
            rs.close();

            return tmp.toArray(new Server[tmp.size()]);
        } catch (final PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            LOG.error("SQL Error", ecp);
            throw new StorageException(ecp);
        } finally {
            closeSQLStuff(stmt);
            if (con != null) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (final PoolException exp) {
                    LOG.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    @Override
    public void unregisterDatabase(final int dbId, final boolean isMaster) throws StorageException {
        Connection con;
        try {
            con = cache.getWriteConnectionForConfigDB();
        } catch (final PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        }

        PreparedStatement stmt = null;
        boolean rollback = false;
        try {
            startTransaction(con);
            rollback = true;

            lock(con);

            if (isMaster) {
                stmt = con.prepareStatement("DELETE db_pool, db_cluster FROM db_pool INNER JOIN db_cluster ON db_pool.db_pool_id = db_cluster.write_db_pool_id OR db_pool.db_pool_id = db_cluster.read_db_pool_id WHERE db_cluster.write_db_pool_id=?");
                stmt.setInt(1, dbId);
                stmt.executeUpdate();
                closeSQLStuff(stmt);
                stmt = null;

                stmt = con.prepareStatement("DELETE FROM contexts_per_dbpool WHERE db_pool_id=?");
                stmt.setInt(1, dbId);
                stmt.executeUpdate();
                closeSQLStuff(stmt);
                stmt = null;

                stmt = con.prepareStatement("DELETE FROM dbpool_lock WHERE db_pool_id=?");
                stmt.setInt(1, dbId);
                stmt.executeUpdate();
                closeSQLStuff(stmt);
                stmt = null;

                // Just to be sure...
                {
                    stmt = con.prepareStatement("DELETE FROM contexts_per_dbschema WHERE db_pool_id=?");
                    stmt.setInt(1, dbId);
                    stmt.executeUpdate();
                    closeSQLStuff(stmt);
                    stmt = null;

                    stmt = con.prepareStatement("DELETE FROM dbschema_lock WHERE db_pool_id=?");
                    stmt.setInt(1, dbId);
                    stmt.executeUpdate();
                    closeSQLStuff(stmt);
                    stmt = null;
                }
            } else {
                stmt = con.prepareStatement("UPDATE context_server2db_pool SET read_db_pool_id = write_db_pool_id  WHERE read_db_pool_id = ?");
                stmt.setInt(1, dbId);
                stmt.executeUpdate();
                closeSQLStuff(stmt);
                stmt = null;

                stmt = con.prepareStatement("UPDATE db_cluster SET read_db_pool_id = write_db_pool_id WHERE read_db_pool_id=?");
                stmt.setInt(1, dbId);
                stmt.executeUpdate();
                closeSQLStuff(stmt);
                stmt = null;

                stmt = con.prepareStatement("DELETE FROM db_pool WHERE db_pool_id=?");
                stmt.setInt(1, dbId);
                stmt.executeUpdate();
                closeSQLStuff(stmt);
                stmt = null;
            }

            con.commit();
            rollback = false;
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final RuntimeException e) {
            LOG.error("Runtime Error", e);
            throw new StorageException(e);
        } finally {
            closeSQLStuff(stmt);
            if (rollback) {
                rollback(con);
            }
            try {
                cache.pushWriteConnectionForConfigDB(con);
            } catch (final PoolException e) {
                LOG.error("Error pushing configdb connection to pool!", e);
            }
        }
    }

    @Override
    public void unregisterFilestore(final int store_id) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;
        boolean rollback = false;
        try {
            con = cache.getWriteConnectionForConfigDB();
            con.setAutoCommit(false);
            rollback = true;

            {
                FileStorageUnregisterListenerRegistry listenerRegistry = AdminServiceRegistry.getInstance().getService(FileStorageUnregisterListenerRegistry.class);
                if (null != listenerRegistry) {
                    List<FileStorageUnregisterListener> listeners = listenerRegistry.getListeners();
                    for (FileStorageUnregisterListener listener : listeners) {
                        listener.onFileStorageUnregistration(store_id, con);
                    }
                }
            }

            stmt = con.prepareStatement("DELETE FROM filestore WHERE id = ?");
            stmt.setInt(1, store_id);
            stmt.executeUpdate();
            stmt.close();

            stmt = con.prepareStatement("DELETE FROM contexts_per_filestore WHERE filestore_id = ?");
            stmt.setInt(1, store_id);
            stmt.executeUpdate();

            con.commit();
            rollback = false;
        } catch (final PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            LOG.error("SQL Error", ecp);
            throw new StorageException(ecp);
        } catch (final OXException oxe) {
            LOG.error("OX Error", oxe);
            throw new StorageException(oxe);
        } finally {
            closeSQLStuff(stmt);
            if (con != null) {
                if (rollback) {
                    Databases.rollback(con);
                }
                try {
                    cache.pushWriteConnectionForConfigDB(con);
                } catch (final PoolException exp) {
                    LOG.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    @Override
    public void unregisterServer(final int server_id) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = cache.getWriteConnectionForConfigDB();
            stmt = con.prepareStatement("DELETE FROM server WHERE server_id = ?");
            stmt.setInt(1, server_id);
            stmt.executeUpdate();
            stmt.close();
        } catch (final PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            LOG.error("SQL Error", ecp);
            throw new StorageException(ecp);
        } finally {
            closeSQLStuff(stmt);
            if (con != null) {
                try {
                    cache.pushWriteConnectionForConfigDB(con);
                } catch (final PoolException exp) {
                    LOG.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    private static long toMB(long value) {
        return 0 == value ? 0 : value / 0x100000;
    }

    @Override
    public URI getFilestoreURI(int filestoreId) throws StorageException {
        Connection con = null;
        try {
            con = cache.getReadConnectionForConfigDB();
            return getFilestoreURI(filestoreId, con);
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            if (null != con) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (PoolException e) {
                    LOG.error("Error pushing configdb connection to pool!", e);
                }
            }
        }
    }

    private URI getFilestoreURI(int filestoreId, Connection con) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT uri FROM filestore WHERE id=?");
            stmt.setInt(1, filestoreId);
            result = stmt.executeQuery();
            if (!result.next()) {
                throw new StorageException("No such file storage for identifier " + filestoreId);
            }
            String uri = result.getString(1);
            try {
                return new java.net.URI(uri);
            } catch (URISyntaxException e) {
                throw new StorageException("Invalid file storage URI: " + uri, e);
            }
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    /**
     * Gets the URIs of the file storages that are in use by specified context (either itself or by one if its users).
     *
     * @param contextId The context identifier
     * @return The file storages in use
     * @throws StorageException If file storages cannot be determined
     */
    @Override
    public List<URI> getUrisforFilestoresUsedBy(int contextId) throws StorageException {
        Connection configDbCon = null;
        try {
            configDbCon = cache.getReadConnectionForConfigDB();

            // Get URI from context
            URI ctxUri = getFilestoreUsedByContext(contextId, configDbCon);

            // Get URIs from users
            List<URI> uris;
            {
                Connection con = null;
                try {
                    con = cache.getConnectionForContext(contextId);
                    uris = getFilestoresUsedByUsers(contextId, configDbCon, con);
                } finally {
                    if (null != con) {
                        try {
                            cache.pushConnectionForContext(contextId, con);
                        } catch (PoolException e) {
                            LOG.error("Error pushing configdb connection to pool!", e);
                        }
                    }
                }
            }

            // Insert context URI & return
            uris.add(0, ctxUri);
            return uris;
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            if (null != configDbCon) {
                try {
                    cache.pushReadConnectionForConfigDB(configDbCon);
                } catch (PoolException e) {
                    LOG.error("Error pushing configdb connection to pool!", e);
                }
            }
        }
    }

    private URI getFilestoreUsedByContext(int contextId, Connection configDbCon) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = configDbCon.prepareStatement("SELECT filestore_id, filestore_name FROM context WHERE cid=?");
            stmt.setInt(1, contextId);
            result = stmt.executeQuery();

            if (false == result.next()) {
                return null;
            }

            int filestoreId = result.getInt(1);
            String path = result.getString(2);
            closeSQLStuff(result, stmt);
            result = null;
            stmt = null;

            stmt = configDbCon.prepareStatement("SELECT uri FROM filestore WHERE id=?");
            stmt.setInt(1, filestoreId);
            result = stmt.executeQuery();
            if (!result.next()) {
                // No such file storage
                return null;
            }

            String fsUri = result.getString(1);
            closeSQLStuff(result, stmt);
            result = null;
            stmt = null;

            return new URI(fsUri + (fsUri.endsWith("/") ? "" : "/") + path);
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } catch (URISyntaxException e) {
            LOG.error("URI Syntax Error", e);
            throw new StorageException(e);
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private List<URI> getFilestoresUsedByUsers(int contextId, Connection configDbCon, Connection con) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT filestore_id, filestore_name FROM user WHERE cid=? AND filestore_id > 0");
            stmt.setInt(1, contextId);
            result = stmt.executeQuery();

            if (false == result.next()) {
                return new ArrayList<>(1);
            }

            class FidAndName {

                final int filestoreId;
                final String name;

                FidAndName(int filestoreId, String name) {
                    super();
                    this.filestoreId = filestoreId;
                    this.name = name;
                }
            }
            ;

            List<FidAndName> fids = new LinkedList<>();
            do {
                int filestoreId = result.getInt(1);
                String path = result.getString(2);
                fids.add(new FidAndName(filestoreId, path));
            } while (result.next());
            closeSQLStuff(result, stmt);
            result = null;
            stmt = null;

            Map<Integer, String> baseUris = new HashMap<>(fids.size());
            List<URI> uris = new ArrayList<>(fids.size());
            for (FidAndName fid : fids) {
                int filestoreId = fid.filestoreId;

                String fsUri;
                if (baseUris.containsKey(Integer.valueOf(filestoreId))) {
                    fsUri = baseUris.get(Integer.valueOf(filestoreId));
                } else {
                    stmt = configDbCon.prepareStatement("SELECT uri FROM filestore WHERE id=?");
                    stmt.setInt(1, filestoreId);
                    result = stmt.executeQuery();
                    fsUri = result.next() ? result.getString(1) : null;
                    baseUris.put(Integer.valueOf(filestoreId), fsUri);
                    closeSQLStuff(result, stmt);
                    result = null;
                    stmt = null;
                }

                if (null != fsUri) {
                    uris.add(new URI(fsUri + (fsUri.endsWith("/") ? "" : "/") + fid.name));
                }
            }
            return uris;
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } catch (URISyntaxException e) {
            LOG.error("URI Syntax Error", e);
            throw new StorageException(e);
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    /**
     * Loads filestore information, but w/o any usage information. Only basic information.
     *
     * @param id The unique identifier of the filestore.
     * @return Basic filestore information
     * @throws StorageException if loading the filestore information fails.
     */
    @Override
    public Filestore getFilestoreBasic(int id) throws StorageException {
        Connection con = null;
        try {
            con = cache.getReadConnectionForConfigDB();
            return getFilestoreBasic(id, con);
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            if (null != con) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (PoolException e) {
                    LOG.error("Error pushing configdb connection to pool!", e);
                }
            }
        }
    }

    /**
     * Loads filestore information, but w/o any usage information. Only basic information.
     *
     * @param id The unique identifier of the filestore.
     * @param configdbCon The connection to use
     * @return Basic filestore information
     * @throws StorageException if loading the filestore information fails.
     */
    public Filestore getFilestoreBasic(int id, Connection configdbCon) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = configdbCon.prepareStatement("SELECT uri, size, max_context FROM filestore WHERE id=?");
            stmt.setInt(1, id);
            result = stmt.executeQuery();
            if (!result.next()) {
                throw new StorageException("No such file storage for identifier " + id);
            }
            Filestore fs = new Filestore();
            fs.setId(I(id));
            fs.setUrl(result.getString(1));
            fs.setSize(L(toMB(result.getLong(2))));
            fs.setMaxContexts(I(result.getInt(3)));
            return fs;
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    @Override
    public Filestore getFilestore(final int id) throws StorageException {
        return getFilestore(id, true);
    }

    /**
     * Loads all filestore information. BEWARE! If loadRealUsage is set to <code>true</code> this operation may be very expensive because
     * the filestore usage for all contexts stored in that filestore must be loaded. Setting this parameter to <code>false</code> will set
     * the read usage of the filestore to 0.
     *
     * @param id unique identifier of the filestore.
     * @param loadRealUsage <code>true</code> to load the real file store usage of that filestore.
     * @return all filestore information
     * @throws StorageException if loading the filestore information fails.
     */
    @Override
    public Filestore getFilestore(int id, boolean loadRealUsage) throws StorageException {
        Connection con = null;
        try {
            con = cache.getReadConnectionForConfigDB();
            return getFilestore(id, loadRealUsage, con);
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            if (null != con) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (PoolException e) {
                    LOG.error("Error pushing configdb connection to pool!", e);
                }
            }
        }
    }

    /**
     * Loads all filestore information. BEWARE! If loadRealUsage is set to <code>true</code> this operation may be very expensive because
     * the filestore usage for all contexts stored in that filestore must be loaded. Setting this parameter to <code>false</code> will set
     * the real usage of the filestore to 0.
     *
     * @param id The unique identifier of the filestore.
     * @param loadRealUsage <code>true</code> to load the real file store usage of that filestore.
     * @return All filestore information
     * @throws StorageException if loading the filestore information fails.
     */
    private Filestore getFilestore(int id, boolean loadRealUsage, Connection configdbCon) throws StorageException {
        return getFilestore(id, loadRealUsage, null, null, configdbCon);
    }

    /**
     * Loads all filestore information. BEWARE! If loadRealUsage is set to <code>true</code> this operation may be very expensive because
     * the filestore usage for all contexts stored in that filestore must be loaded. Setting this parameter to <code>false</code> will set
     * the real usage of the filestore to 0.
     *
     * @param id The unique identifier of the filestore.
     * @param loadRealUsage <code>true</code> to load the real file store usage of that filestore.
     * @param pools Available database pools (<i>optional</i>)
     * @param contextFilestoreUsage The context filestore usage (<i>optional</i>)
     * @param userFilestoreUsage The user filestore usage (<i>optional</i>)
     * @return All filestore information
     * @throws StorageException if loading the filestore information fails.
     */
    private Filestore getFilestore(int id, boolean loadRealUsage, FilestoreUsage contextFilestoreUsage, FilestoreUsage userFilestoreUsage, Connection configdbCon) throws StorageException {
        Filestore fs;

        // Load data from database
        {
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                stmt = configdbCon.prepareStatement("SELECT uri, size, max_context FROM filestore WHERE id=?");
                stmt.setInt(1, id);
                result = stmt.executeQuery();
                if (!result.next()) {
                    throw new StorageException("No such file storage for identifier " + id);
                }
                fs = new Filestore();
                int pos = 1;
                fs.setId(I(id));
                fs.setUrl(result.getString(pos++));
                fs.setSize(L(toMB(result.getLong(pos++))));
                fs.setMaxContexts(I(result.getInt(pos++)));
            } catch (final SQLException e) {
                LOG.error("SQL Error", e);
                throw new StorageException(e);
            } finally {
                closeSQLStuff(result, stmt);
            }
        }

        // Load usage information
        loadFilestoreUsageFor(fs, loadRealUsage, contextFilestoreUsage, userFilestoreUsage, configdbCon);
        return fs;
    }

    private void loadFilestoreUsageFor(Filestore fs, boolean loadRealUsage, FilestoreUsage contextFilestoreUsage, FilestoreUsage userFilestoreUsage, Connection configdbCon) throws StorageException {
        int id = fs.getId().intValue();
        FilestoreUsage usrCounts = null == userFilestoreUsage ? getUserUsage(id, loadRealUsage, configdbCon) : userFilestoreUsage;
        FilestoreUsage ctxCounts = null == contextFilestoreUsage ? getContextUsage(id, loadRealUsage, configdbCon) : contextFilestoreUsage;
        fs.setUsed(L(toMB(ctxCounts.usage + usrCounts.usage)));
        fs.setCurrentContexts(I(ctxCounts.entityCount + usrCounts.entityCount));
        fs.setReserved(L((getAverageFilestoreSpaceForContext() * ctxCounts.entityCount) + (getAverageFilestoreSpaceForUser() * usrCounts.entityCount)));
    }

    private void updateFilestoresWithRealUsage(final List<Filestore> stores) throws StorageException {
        final AdminCacheExtended cache = this.cache;

        // Determine server identifier
        final int serverId;
        try {
            serverId = cache.getServerId();
        } catch (PoolException e) {
            throw new StorageException(e);
        }

        // Determine all DB schemas that are currently in use since each one is required to be queried to retrieve file storage usage
        final boolean onlyOneServerRegistered;
        List<PoolAndSchema> allFilledPoolsAndSchemas;
        {
            Connection con = null;
            try {
                con = cache.getReadConnectionForConfigDB();

                List<Integer> serversIds = getRegisteredServersIDs(con);
                if (serversIds.isEmpty()) {
                    throw new StorageException("No such server registered: " + serverId);
                }
                if (serversIds.size() == 1) {
                    if (serverId != serversIds.get(0).intValue()) {
                        // This node does not belong to registered server
                        for (Filestore store : stores) {
                            store.setSize(L(toMB(l(store.getSize()))));
                            store.setCurrentContexts(Integer.valueOf(0));
                            store.setUsed(L(0));
                        }
                        return;
                    }
                    onlyOneServerRegistered = true;
                } else {
                    onlyOneServerRegistered = false;
                }

                allFilledPoolsAndSchemas = getAllFilledPoolsAndSchemasForFilestoreUsage(con);
            } catch (PoolException e) {
                throw new StorageException(e);
            } finally {
                if (null != con) {
                    try {
                        cache.pushReadConnectionForConfigDB(con);
                    } catch (PoolException e) {
                        LOG.error("Error pushing configdb connection to pool!", e);
                    }
                }
            }
        }

        // Create mapping for file storage identifier and its usage
        class Usage {

            private final AtomicLong usage;
            private final AtomicInteger users;
            private final AtomicInteger contexts;

            Usage(long initialUsage, boolean forContext) {
                super();
                this.usage = new AtomicLong(initialUsage);
                if (forContext) {
                    this.contexts = new AtomicInteger(1);
                    this.users = new AtomicInteger(0);
                } else {
                    this.contexts = new AtomicInteger(0);
                    this.users = new AtomicInteger(1);
                }
            }

            void addForEntity(long usage, boolean forContext) {
                if (usage > 0) {
                    this.usage.addAndGet(usage);
                }
                if (forContext) {
                    contexts.incrementAndGet();
                } else {
                    users.incrementAndGet();
                }
            }

            long getUsage() {
                return usage.get();
            }

            int getNumContexts() {
                return contexts.get();
            }

            int getNumUsers() {
                return users.get();
            }
        }
        final ConcurrentMap<Integer, Usage> id2usage = new ConcurrentHashMap<>(stores.size());

        CompletionService<Void> completionService = new BoundedCompletionService<>(AdminServiceRegistry.getInstance().getService(ThreadPoolService.class), 25);
        int taskCount = 0;

        // Acquire usage from each database schema and add it to total file storage usage
        for (final PoolAndSchema poolAndSchema : allFilledPoolsAndSchemas) {
            final int poolId = poolAndSchema.getPoolId();
            final String schema = poolAndSchema.getSchema();
            completionService.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    // Check database schema
                    if (OXToolStorageInterface.getInstance().schemaBeingLockedOrNeedsUpdate(poolAndSchema.getPoolId(), poolAndSchema.getSchema())) {
                        // Locked or update process running...
                        throw new StorageException(new DatabaseUpdateException("Database with pool-id " + poolAndSchema.getPoolId() + " and schema \"" + poolAndSchema.getSchema() + "\" needs update. Please run \"runupdate\" for that database."));
                    }

                    Map<Integer, Integer> context2filestore = null;
                    Map<UserId, Integer> user2filestore = null;

                    // Initialize maps
                    {
                        Connection con = null;
                        try {
                            con = cache.getReadConnectionForConfigDB();
                            if (onlyOneServerRegistered || existsSchemaOnServer(poolId, schema, serverId, con)) {
                                context2filestore = getContext2FilestoreIdFor(poolAndSchema, con);
                                user2filestore = getUser2FilestoreIdFor(context2filestore.keySet(), con);
                            }
                        } catch (PoolException e) {
                            throw new StorageException(e);
                        } finally {
                            if (null != con) {
                                try {
                                    cache.pushReadConnectionForConfigDB(con);
                                } catch (PoolException e) {
                                    LOG.error("Error pushing configdb connection to pool!", e);
                                }
                            }
                        }
                    }

                    if (null != context2filestore && null != user2filestore) {
                        Connection userDbCon = null;
                        PreparedStatement stmt = null;
                        ResultSet rs = null;
                        try {
                            userDbCon = cache.getPool().getConnection(poolId, schema);

                            stmt = userDbCon.prepareStatement("SELECT cid, user, used FROM filestore_usage");
                            rs = stmt.executeQuery();
                            while (rs.next()) {
                                int contextId = rs.getInt(1);
                                int userId = rs.getInt(2);
                                long usage = rs.getLong(3);

                                Integer filestoreId;
                                boolean forContext = true;
                                if (userId > 0) {
                                    // User-associated file storage usage
                                    filestoreId = user2filestore.get(new UserId(userId, contextId));
                                    forContext = false;
                                } else {
                                    // Context-associated file storage usage
                                    filestoreId = context2filestore.get(Integer.valueOf(contextId));
                                }

                                if (null != filestoreId) {
                                    Usage fsUsage = id2usage.get(filestoreId);
                                    if (null == fsUsage) {
                                        Usage newUsage = new Usage(usage, forContext);
                                        fsUsage = id2usage.putIfAbsent(filestoreId, newUsage);
                                        if (null != fsUsage) {
                                            // Another thread inserted usage in the meantime
                                            fsUsage.addForEntity(usage, forContext);
                                        }
                                    } else {
                                        fsUsage.addForEntity(usage, forContext);
                                    }
                                }
                            }
                        } catch (SQLException e) {
                            throw new StorageException(e);
                        } catch (PoolException e) {
                            throw new StorageException(e);
                        } finally {
                            Databases.closeSQLStuff(rs, stmt);
                            if (null != userDbCon) {
                                try {
                                    cache.getPool().pushConnection(poolId, userDbCon);
                                } catch (PoolException e) {
                                    LOG.error("Error pushing userdb connection to pool!", e);
                                }
                            }
                        }
                    }
                    return null;
                }

                private Map<Integer, Integer> getContext2FilestoreIdFor(PoolAndSchema poolAndSchema, Connection con) throws StorageException {
                    PreparedStatement stmt = null;
                    ResultSet rs = null;
                    try {
                        stmt = con.prepareStatement("SELECT c.cid, t.filestore_id FROM context_server2db_pool AS c JOIN context AS t ON c.cid=t.cid WHERE c.write_db_pool_id=? and c.db_schema=?");
                        stmt.setInt(1, poolAndSchema.getPoolId());
                        stmt.setString(2, poolAndSchema.getSchema());
                        rs = stmt.executeQuery();
                        if (false == rs.next()) {
                            return Collections.emptyMap();
                        }

                        Map<Integer, Integer> l = new HashMap<>();
                        do {
                            l.put(Integer.valueOf(rs.getInt(1)), Integer.valueOf(rs.getInt(2)));
                        } while (rs.next());
                        return l;
                    } catch (SQLException e) {
                        throw new StorageException(e);
                    } finally {
                        Databases.closeSQLStuff(rs, stmt);
                    }
                }

                private Map<UserId, Integer> getUser2FilestoreIdFor(Set<Integer> contextIds, Connection con) throws StorageException {
                    Map<UserId, Integer> l = null;
                    for (List<Integer> contextIdsChunk : Lists.partition(new ArrayList<Integer>(contextIds), Databases.IN_LIMIT)) {
                        PreparedStatement stmt = null;
                        ResultSet rs = null;
                        try {
                            stmt = con.prepareStatement(Databases.getIN("SELECT cid, user, filestore_id FROM filestore2user WHERE cid IN (", contextIdsChunk.size()));
                            int pos = 1;
                            for (Integer contextId : contextIdsChunk) {
                                stmt.setInt(pos++, contextId.intValue());
                            }
                            rs = stmt.executeQuery();
                            if (rs.next()) {
                                if (null == l) {
                                    l = new HashMap<>();
                                }
                                do {
                                    l.put(new UserId(rs.getInt(2), rs.getInt(1)), Integer.valueOf(rs.getInt(3)));
                                } while (rs.next());
                            }
                        } catch (SQLException e) {
                            throw new StorageException(e);
                        } finally {
                            Databases.closeSQLStuff(rs, stmt);
                        }
                    }
                    return null == l ? Collections.<UserId, Integer> emptyMap() : l;
                }

                private boolean existsSchemaOnServer(int poolId, String schema, int serverId, Connection con) throws StorageException {
                    PreparedStatement stmt = null;
                    ResultSet rs = null;
                    try {
                        stmt = con.prepareStatement("SELECT 1 FROM context_server2db_pool WHERE server_id=? AND write_db_pool_id=? AND db_schema=? LIMIT 1");
                        stmt.setInt(1, serverId);
                        stmt.setInt(2, poolId);
                        stmt.setString(3, schema);
                        rs = stmt.executeQuery();
                        return rs.next();
                    } catch (SQLException e) {
                        throw new StorageException(e);
                    } finally {
                        Databases.closeSQLStuff(rs, stmt);
                    }
                }
            });
            taskCount++;
        }

        // Await completion
        ThreadPools.<Void, StorageException> awaitCompletionService(completionService, taskCount, EXCEPTION_FACTORY);

        // Correct size (in MB) and apply possible usage information
        for (Filestore store : stores) {
            Usage fsUsage = id2usage.get(store.getId());
            store.setSize(L(toMB(l(store.getSize()))));
            if (null == fsUsage) {
                store.setCurrentContexts(Integer.valueOf(0));
                store.setUsed(L(0));
                store.setReserved(L(0));
            } else {
                store.setCurrentContexts(Integer.valueOf(fsUsage.getNumContexts() + fsUsage.getNumUsers()));
                store.setUsed(L(toMB(fsUsage.getUsage())));
                store.setReserved(L((getAverageFilestoreSpaceForContext() * fsUsage.getNumContexts()) + (getAverageFilestoreSpaceForUser() * fsUsage.getNumUsers())));
            }
        }
    }

    private List<PoolAndSchema> getAllFilledPoolsAndSchemasForFilestoreUsage(Connection con) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT DISTINCT db_pool_id, schemaname FROM contexts_per_dbschema WHERE count > 0");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                // No database schema in use
                return Collections.emptyList();
            }

            List<PoolAndSchema> l = new LinkedList<>();
            do {
                l.add(new PoolAndSchema(rs.getInt(1), rs.getString(2)));
            } while (rs.next());
            return l;
        } catch (SQLException e) {
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private List<Integer> getRegisteredServersIDs(Connection con) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT server_id FROM server");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                // Huh...?
                return Collections.emptyList();
            }

            List<Integer> serverIds = new LinkedList<>();
            do {
                serverIds.add(Integer.valueOf(rs.getInt(1)));
            } while (rs.next());
            return serverIds;
        } catch (SQLException e) {
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    protected void updateBlocksInSameDBWithFilestoreUsage(Collection<FilestoreContextBlock> blocks) throws StorageException {
        for (FilestoreContextBlock block : blocks) {
            updateBlockWithFilestoreUsage(block);
        }
    }

    private void updateBlockWithFilestoreUsage(final FilestoreContextBlock block) throws StorageException {
        if (block.isEmpty()) {
            return;
        }
        Connection con = null;

        {
            int representativeContextID = Integer.MIN_VALUE;
            for (Integer cid : block.contextFilestores.keySet()) {
                // Try to get a database connection with all context identifier from the FilestoreContextBlock.
                try {
                    con = cache.getConnectionForContext(i(cid));
                    representativeContextID = i(cid);
                    break;
                } catch (final PoolException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
            if (null == con) {
                for (Integer cid : block.userFilestores.keySet()) {
                    // Try to get a database connection.
                    try {
                        con = cache.getConnectionForContext(i(cid));
                        representativeContextID = i(cid);
                        break;
                    } catch (final PoolException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }

            if (null == con) {
                // If none is available anymore, we can not add any usage data and we should just return.
                return;
            }

            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                stmt = con.prepareStatement("SELECT cid, used FROM filestore_usage WHERE user = 0");
                result = stmt.executeQuery();
                while (result.next()) {
                    int cid = result.getInt(1);
                    long usage = result.getLong(2);
                    block.updateForContext(cid, usage);
                }

                closeSQLStuff(result, stmt);

                stmt = con.prepareStatement("SELECT cid, user, used FROM filestore_usage WHERE user > 0");
                result = stmt.executeQuery();
                while (result.next()) {
                    int cid = result.getInt(1);
                    int userId = result.getInt(2);
                    long usage = result.getLong(3);
                    block.updateForUser(cid, userId, usage);
                }
            } catch (final SQLException e) {
                throw new StorageException(e);
            } finally {
                closeSQLStuff(result, stmt);
                try {
                    cache.pushConnectionForContext(representativeContextID, con);
                } catch (final PoolException e) {
                    throw new StorageException(e);
                }
            }
        }
    }

    /* -------------------------------------------------------------------------------------------------------------------- */

    /**
     * Loads the context usage information for a file storage.
     *
     * @param filestoreId the unique identifier of the file storage.
     * @param loadRealUsage <code>true</code> to load the file storage usage from every context in it. BEWARE! This is a slow operation.
     * @return The {@link FilestoreUsage} object for the file storage.
     * @throws StorageException if some problem occurs loading the information.
     */
    private FilestoreUsage getContextUsage(int filestoreId, boolean loadRealUsage) throws StorageException {
        Connection readConfigdbCon = null;
        try {
            readConfigdbCon = cache.getReadConnectionForConfigDB();
            return getContextUsage(filestoreId, loadRealUsage, readConfigdbCon);
        } catch (PoolException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            if (null != readConfigdbCon) {
                try {
                    cache.pushReadConnectionForConfigDB(readConfigdbCon);
                } catch (final PoolException e) {
                    LOG.error("Error pushing configdb connection to pool!", e);
                }
            }
        }
    }

    /**
     * Loads the context usage information for a file storage.
     *
     * @param filestoreId the unique identifier of the file storage.
     * @param loadRealUsage <code>true</code> to load the file storage usage from every context in it. BEWARE! This is a slow operation.
     * @param readConfigdbCon a read only connection
     * @return The {@link FilestoreUsage} object for the file storage.
     * @throws StorageException if some problem occurs loading the information.
     */
    private FilestoreUsage getContextUsage(int filestoreId, boolean loadRealUsage, Connection readConfigdbCon) throws StorageException {
        if (null == readConfigdbCon) {
            return getContextUsage(filestoreId, loadRealUsage);
        }

        if (false == loadRealUsage) {
            // Only load context count for given file storage
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                stmt = readConfigdbCon.prepareStatement("SELECT count FROM contexts_per_filestore WHERE filestore_id=?");
                stmt.setInt(1, filestoreId);
                result = stmt.executeQuery();
                if (false == result.next()) {
                    return new FilestoreUsage(0, 0L);
                }

                return new FilestoreUsage(result.getInt(1), 0L);
            } catch (SQLException e) {
                LOG.error("SQL Error", e);
                throw new StorageException(e);
            } finally {
                closeSQLStuff(result, stmt);
            }
        }

        // Get count and usage for given file storage
        final AdminCacheExtended cache = this.cache;
        Map<PoolAndSchema, List<Integer>> map = new LinkedHashMap<>();
        int count = 0;

        {
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                stmt = readConfigdbCon.prepareStatement("SELECT c.cid, s.write_db_pool_id, s.db_schema FROM context AS c JOIN context_server2db_pool AS s ON c.cid=s.cid WHERE c.filestore_id=? AND s.server_id=?");
                stmt.setInt(1, filestoreId);
                stmt.setInt(2, cache.getServerId());
                result = stmt.executeQuery();
                while (result.next()) {
                    count++;

                    PoolAndSchema poolAndSchema = new PoolAndSchema(result.getInt(2), result.getString(3));
                    List<Integer> cids = map.get(poolAndSchema);
                    if (null == cids) {
                        cids = new LinkedList<>();
                        map.put(poolAndSchema, cids);
                    }
                    cids.add(Integer.valueOf(result.getInt(1)));
                }
            } catch (PoolException e) {
                LOG.error("SQL Error", e);
                throw new StorageException(e);
            } catch (SQLException e) {
                LOG.error("SQL Error", e);
                throw new StorageException(e);
            } finally {
                closeSQLStuff(result, stmt);
            }
        }

        CompletionService<Long> completionService = new ThreadPoolCompletionService<>(AdminServiceRegistry.getInstance().getService(ThreadPoolService.class));
        int taskCount = 0;

        for (Map.Entry<PoolAndSchema, List<Integer>> entry : map.entrySet()) {
            final PoolAndSchema poolAndSchema = entry.getKey();
            final List<Integer> cids = entry.getValue();

            completionService.submit(new Callable<Long>() {

                @Override
                public Long call() throws StorageException {
                    Connection con = null;
                    PreparedStatement stmt = null;
                    ResultSet result = null;
                    try {
                        con = cache.getWRITENoTimeoutConnectionForPoolId(poolAndSchema.getPoolId(), poolAndSchema.getSchema());
                        stmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE user=0 AND cid IN " + getSqlInString(cids));
                        result = stmt.executeQuery();
                        long used = 0L;
                        while (result.next()) {
                            used += result.getLong(1);
                        }
                        return Long.valueOf(used);
                    } catch (PoolException e) {
                        LOG.error("SQL Error", e);
                        throw new StorageException(e);
                    } catch (SQLException e) {
                        LOG.error("SQL Error", e);
                        throw new StorageException(e);
                    } finally {
                        closeSQLStuff(result, stmt);
                        if (null != con) {
                            try {
                                cache.pushWRITENoTimeoutConnectionForPoolId(poolAndSchema.getPoolId(), con);
                            } catch (PoolException e) {
                                LOG.error("Error pushing connection to pool!", e);
                            }
                        }
                    }

                }
            });
            taskCount++;
        }

        // Await completion
        List<Long> usages = ThreadPools.<Long, StorageException> takeCompletionService(completionService, taskCount, EXCEPTION_FACTORY);

        long used = 0L;
        for (Long usg : usages) {
            used += usg.longValue();
        }
        return new FilestoreUsage(count, used);
    }

    /**
     * Loads the user usage information for a file storage.
     *
     * @param filestoreId the unique identifier of the file storage.
     * @param loadRealUsage <code>true</code> to load the file storage usage from every context in it. BEWARE! This is a slow operation.
     * @param pools Available database pools
     * @param readConfigdbCon a read only connection
     * @return The {@link FilestoreUsage} object for the file storage.
     * @throws StorageException if some problem occurs loading the information.
     */
    private FilestoreUsage getUserUsage(final int filestoreId, boolean loadRealUsage, Connection readConfigdbCon) throws StorageException {
        final AdminCacheExtended cache = this.cache;
        if (false == loadRealUsage) {
            int numUsers = Filestore2UserUtil.getUserCountFor(filestoreId, cache);
            return new FilestoreUsage(numUsers, 0L);
        }

        Set<UserAndContext> users = Filestore2UserUtil.getUsersFor(filestoreId, cache);
        CompletionService<Long> completionService = new ThreadPoolCompletionService<>(AdminServiceRegistry.getInstance().getService(ThreadPoolService.class));
        int taskCount = 0;

        for (final UserAndContext usrAndCtx : users) {
            completionService.submit(new Callable<Long>() {

                @Override
                public Long call() throws Exception {
                    int contextId = usrAndCtx.contextId;
                    Connection con = null;
                    PreparedStatement stmt = null;
                    ResultSet result = null;
                    try {
                        con = cache.getConnectionForContext(contextId);
                        stmt = con.prepareStatement("SELECT fu.used FROM filestore_usage AS fu WHERE fu.cid=? and fu.user=?");
                        stmt.setInt(1, contextId);
                        stmt.setInt(2, usrAndCtx.userId);
                        result = stmt.executeQuery();
                        return result.next() ? Long.valueOf(result.getLong(1)) : null;
                    } catch (PoolException e) {
                        LOG.error("Pool Error", e);
                        throw new StorageException(e);
                    } catch (SQLException e) {
                        LOG.error("SQL Error", e);
                        throw new StorageException(e);
                    } finally {
                        closeSQLStuff(result, stmt);
                        if (null != con) {
                            try {
                                cache.pushConnectionForContext(contextId, con);
                            } catch (PoolException e) {
                                LOG.error("Error pushing connection to pool!", e);
                            }
                        }
                    }
                }
            });
        }

        // Await completion
        List<Long> counts = ThreadPools.<Long, StorageException> takeCompletionService(completionService, taskCount, EXCEPTION_FACTORY);
        long used = 0L;
        for (Long fu : counts) {
            if (null != fu) {
                used += fu.longValue();
            }
        }
        return new FilestoreUsage(users.size(), used);
    }

    // -------------------------------------------------------------------------------------------------------------------------

    static String getSqlInString(Collection<Integer> col) {
        if (col == null || col.isEmpty()) {
            return null;
        }
        int size = col.size();
        StringBuilder sb = new StringBuilder(size << 4);
        Integer[] values = col.toArray(new Integer[col.size()]);
        sb.append('(');
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sb.append(',');
                sb.append(values[i]);
            } else {
                sb.append(values[i]);
            }
        }
        sb.append(')');
        return sb.toString();
    }

    @Override
    public int getWritePoolIdForCluster(int clusterId) throws StorageException {
        final Connection con;
        try {
            con = cache.getReadConnectionForConfigDB();
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String getDbInfo = "SELECT write_db_pool_id FROM db_cluster WHERE cluster_id = ?";
            stmt = con.prepareStatement(getDbInfo);
            stmt.setInt(1, clusterId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
            LOG.error("The specified cluster id '{}' has no database pool references", I(clusterId));
            throw new StorageException("The specified cluster id '" + clusterId + "' has no database pool references");
        } catch (SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            closeSQLStuff(rs, stmt);
            try {
                cache.pushReadConnectionForConfigDB(con);
            } catch (final PoolException e) {
                LOG.error("Error pushing configdb connection to pool!", e);
            }
        }
    }

    @Override
    public Database createSchema(Integer optDatabaseId) throws StorageException {

        Connection configCon = null;
        try {
            configCon = cache.getWriteConnectionForConfigDB();
            startTransaction(configCon);

            Database db;
            if (null == optDatabaseId || i(optDatabaseId) <= 0) {
                db = getNextDBHandleByWeight(configCon, false);
            } else {
                db = OXToolStorageInterface.getInstance().loadDatabaseById(i(optDatabaseId));
                if (db.getMaxUnits().intValue() == 0) {
                    throw new StorageException("Database " + optDatabaseId + " must not be used.");
                }
            }
            createSchema(configCon, db);

            configCon.commit();
            return db;
        } catch (SQLException e) {
            throw new StorageException(e.getMessage(), e);
        } catch (PoolException e) {
            throw new StorageException(e.getMessage(), e);
        } finally {
            autocommit(configCon);
            try {
                cache.pushWriteConnectionForConfigDB(configCon);
            } catch (PoolException e) {
                LOG.error("Error pushing ox write connection to pool!", e);
            }
        }
    }

    private boolean tryIncrementDBPoolCounter(DatabaseHandle db, Connection con) throws SQLException {
        return tryUpdateDBPoolCounter(true, db, con);
    }

    private boolean tryUpdateDBPoolCounter(boolean increment, DatabaseHandle db, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            // Try to update counter
            String schema = db.getScheme();
            if (null == schema) {
                stmt = con.prepareStatement("UPDATE contexts_per_dbpool SET count=? WHERE db_pool_id=? AND count=?");
                stmt.setInt(1, increment ? db.getCount() + 1 : db.getCount() - 1);
                stmt.setInt(2, i(db.getId()));
                stmt.setInt(3, db.getCount());
                return stmt.executeUpdate() > 0;
            }

            int schemaCount = db.getSchemaCount();
            stmt = con.prepareStatement("UPDATE contexts_per_dbschema SET count=? WHERE db_pool_id=? AND schemaname=? AND count=?");
            stmt.setInt(1, increment ? schemaCount + 1 : schemaCount - 1);
            stmt.setInt(2, i(db.getId()));
            stmt.setString(3, schema);
            stmt.setInt(4, schemaCount);
            boolean success = stmt.executeUpdate() > 0;
            closeSQLStuff(stmt);
            stmt = null;

            if (false == success) {
                return false;
            }

            stmt = con.prepareStatement("UPDATE contexts_per_dbpool SET count=count" + (increment ? "+" : "-") + "1 WHERE db_pool_id=?");
            stmt.setInt(1, db.getId().intValue());
            stmt.executeUpdate();
            return true;
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void updateDBPoolCounter(boolean increment, DatabaseHandle db, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            // Try to update counter
            String schema = db.getScheme();
            if (null == schema) {
                stmt = con.prepareStatement("UPDATE contexts_per_dbpool SET count=count" + (increment ? "+" : "-") + "1 WHERE db_pool_id=?");
                stmt.setInt(1, i(db.getId()));
                return;
            }

            stmt = con.prepareStatement("UPDATE contexts_per_dbschema SET count=count" + (increment ? "+" : "-") + "1 WHERE db_pool_id=? AND schemaname=?");
            stmt.setInt(1, i(db.getId()));
            stmt.setString(2, schema);
            stmt.executeUpdate();
            closeSQLStuff(stmt);
            stmt = null;

            stmt = con.prepareStatement("UPDATE contexts_per_dbpool SET count=count" + (increment ? "+" : "-") + "1 WHERE db_pool_id=?");
            stmt.setInt(1, db.getId().intValue());
            stmt.executeUpdate();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public Database getNextDBHandleByWeight(final Connection con, boolean forContext) throws SQLException, StorageException {
        int maxNumberOfContextsPerSchema = this.maxNumberOfContextsPerSchema;
        int retryCount = 0;
        while (true) {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                if (forContext) {
                    // Prefer non-exceeded databases having less filled schemas available
                    stmt = con.prepareStatement("SELECT d.db_pool_id,d.url,d.driver,d.login,d.password,d.name,c.read_db_pool_id,c.max_units,p.count,s.schemaname,s.count FROM contexts_per_dbpool AS p JOIN db_cluster AS c ON p.db_pool_id=c.write_db_pool_id LEFT JOIN contexts_per_dbschema AS s ON p.db_pool_id=s.db_pool_id JOIN db_pool AS d ON p.db_pool_id=d.db_pool_id WHERE (c.max_units < 0 OR (c.max_units > 0 AND c.max_units > p.count)) ORDER BY p.count, s.count ASC");
                } else {
                    stmt = con.prepareStatement("SELECT d.db_pool_id,d.url,d.driver,d.login,d.password,d.name,c.read_db_pool_id,c.max_units,p.count FROM db_pool AS d JOIN db_cluster AS c ON c.write_db_pool_id=d.db_pool_id LEFT JOIN contexts_per_dbpool AS p ON d.db_pool_id=p.db_pool_id WHERE (c.max_units < 0 OR (c.max_units > 0 AND c.max_units > p.count)) ORDER BY p.count ASC");
                }
                rs = stmt.executeQuery();
                if (false == rs.next()) {
                    // No databases at all...
                    throw new StorageException("The maximum number of contexts in every database cluster has been reached. Use register-, create- or change database to resolve the problem.");
                }

                // Iterate candidates
                Map<Integer, DatabaseHandle> dbsWithoutSchema = null;
                boolean checkNext;
                int lastDatabaseId = 0;
                do {
                    int pos = 1;
                    int databaseId = rs.getInt(pos++);
                    if (lastDatabaseId == 0 || lastDatabaseId != databaseId) {
                        checkNext = false;
                        lastDatabaseId = databaseId;

                        // Create appropriate database instance
                        DatabaseHandle db = new DatabaseHandle();
                        db.setId(I(databaseId));
                        db.setUrl(rs.getString(pos++));
                        db.setDriver(rs.getString(pos++));
                        db.setLogin(rs.getString(pos++));
                        db.setPassword(rs.getString(pos++));
                        db.setName(rs.getString(pos++));
                        final int slaveId = rs.getInt(pos++);
                        if (slaveId > 0) {
                            db.setRead_id(I(slaveId));
                        }
                        db.setMaxUnits(I(rs.getInt(pos++)));
                        db.setCount(rs.getInt(pos++));
                        if (rs.wasNull()) {
                            throw new StorageException("Unable to count contexts of db_pool_id=" + db.getId());
                        }

                        boolean selectDatabase = true;
                        if (forContext) {
                            String scheme = rs.getString(pos++); // if the value is SQL NULL, the value returned is null
                            if (null != scheme) {
                                int schemaCount = rs.getInt(pos++);
                                if (schemaCount < maxNumberOfContextsPerSchema && false == OXToolStorageInterface.getInstance().schemaBeingLockedOrNeedsUpdate(databaseId, scheme)) {
                                    db.setScheme(scheme);
                                    db.setSchemaCount(schemaCount);
                                } else {
                                    // Database w/o a schema
                                    if (null == dbsWithoutSchema) {
                                        dbsWithoutSchema = new LinkedHashMap<>();
                                        dbsWithoutSchema.put(I(databaseId), db);
                                    } else if (false == dbsWithoutSchema.containsKey(I(databaseId))) {
                                        dbsWithoutSchema.put(I(databaseId), db);
                                    }
                                    selectDatabase = false;
                                    checkNext = true;
                                }
                            } else {
                                // Database w/o a schema
                                if (null == dbsWithoutSchema) {
                                    dbsWithoutSchema = new LinkedHashMap<>();
                                    dbsWithoutSchema.put(I(databaseId), db);
                                } else if (false == dbsWithoutSchema.containsKey(I(databaseId))) {
                                    dbsWithoutSchema.put(I(databaseId), db);
                                }
                                selectDatabase = false;
                                checkNext = true;
                            }
                        }

                        // Try to update counter
                        if (selectDatabase) {
                            dbsWithoutSchema = null;
                            if (tryIncrementDBPoolCounter(db, con)) {
                                boolean decrement = true;
                                try {
                                    int dbPoolId = i(db.getId());
                                    final Connection dbCon = cache.getWRITEConnectionForPoolId(dbPoolId, null);
                                    cache.pushWRITEConnectionForPoolId(dbPoolId, dbCon);
                                    decrement = false;
                                    return db;
                                } catch (final PoolException e) {
                                    LOG.error("Failed to connect to database {}", db.getId(), e);
                                    checkNext = true;
                                } finally {
                                    if (decrement) {
                                        updateDBPoolCounter(false, db, con);
                                    }
                                }
                            }
                        } else {
                            // Reset last remembered database since not selected at all
                            lastDatabaseId = 0;
                        }
                    } else {
                        checkNext = true;
                    }
                } while (checkNext && rs.next());

                if (null != dbsWithoutSchema) {
                    Iterator<DatabaseHandle> it = dbsWithoutSchema.values().iterator();
                    do {
                        DatabaseHandle dbWithoutSchema = it.next();
                        checkNext = false;
                        if (tryIncrementDBPoolCounter(dbWithoutSchema, con)) {
                            boolean decrement = true;
                            try {
                                int dbPoolId = i(dbWithoutSchema.getId());
                                final Connection dbCon = cache.getWRITEConnectionForPoolId(dbPoolId, null);
                                cache.pushWRITEConnectionForPoolId(dbPoolId, dbCon);
                                decrement = false;
                                return dbWithoutSchema;
                            } catch (final PoolException e) {
                                LOG.error("Failed to connect to database {}", dbWithoutSchema.getId(), e);
                                checkNext = true;
                            } finally {
                                if (decrement) {
                                    updateDBPoolCounter(false, dbWithoutSchema, con);
                                }
                            }
                        }
                    } while (checkNext && it.hasNext());
                }

                if (checkNext) { // Loop was exited because rs.next() returned false
                    // No suitable database found
                    if (forContext) {
                        throw new StorageException("No suitable database available to complete the operation. Please ensure registered databases are reachable and their schemas have enough capacity left and are up-to-date.");
                    }
                    throw new StorageException("All not full databases cannot be connected to.");
                }
            } finally {
                closeSQLStuff(rs, stmt);
            }

            // Exponential back-off
            exponentialBackoffWait(++retryCount, 1000L);
        }
    }

    private List<DatabaseHandle> removeFull(List<DatabaseHandle> list) {
        List<DatabaseHandle> retval = new ArrayList<DatabaseHandle>(list.size());
        for (DatabaseHandle db : list) {
            int maxUnit = i(db.getMaxUnits());
            if (maxUnit < 0 || (maxUnit > 0 && db.getCount() < maxUnit)) {
                retval.add(db);
            }
        }
        return retval;
    }

    private List<DatabaseHandle> loadDatabases(final Connection con) throws SQLException, StorageException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT d.db_pool_id,d.url,d.driver,d.login,d.password,d.name,c.read_db_pool_id,c.max_units,p.count FROM db_pool AS d JOIN db_cluster AS c ON c.write_db_pool_id=d.db_pool_id LEFT JOIN contexts_per_dbpool AS p ON d.db_pool_id=p.db_pool_id");
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
                db.setMaxUnits(I(rs.getInt(pos++)));
                db.setCount(rs.getInt(pos++));
                if (rs.wasNull()) {
                    throw new StorageException("Unable to count contexts of db_pool_id=" + db.getId());
                }
                databases.add(db);
            } while (rs.next());
            return databases;
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Creates a new database schema.
     *
     * @param configCon The connection to configDb
     * @param db The database to get the schema for
     * @throws StorageException If a suitable schema cannot be found
     */
    private void createSchema(Connection configCon, Database db) throws StorageException {
        // Freshly determine the next schema to use
        int schemaUnique;
        try {
            schemaUnique = IDGenerator.getId(configCon);
        } catch (SQLException e) {
            throw new StorageException(e.getMessage(), e);
        }
        String schemaName = db.getName() + '_' + schemaUnique;
        db.setScheme(schemaName);
        OXUtilStorageInterface.getInstance().createDatabase(db, configCon);
    }

    /**
     * Checks if the specified schema and all its contexts are disabled.
     *
     * @param schemaName The schema to check
     * @throws StorageException if at least one of the contexts that reside within the specified schema
     *             is not disabled, or any other error occurs.
     */
    private void isSchemaDisabled(Connection connection, String schemaName) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement("SELECT cid FROM context_server2db_pool WHERE db_schema = ?");
            stmt.setString(1, schemaName);
            rs = stmt.executeQuery();
            if (rs.next() == false) {
                // No contexts found
                return;
            }

            // Put context identifiers into a list
            List<Integer> contextIds = new ArrayList<>(maxNumberOfContextsPerSchema >> 1);
            do {
                contextIds.add(Integer.valueOf(rs.getInt(1)));
            } while (rs.next());
            Databases.closeSQLStuff(rs, stmt);

            // Check if all contexts that reside with in the specified schema are disabled.
            for (List<Integer> partition : Lists.partition(contextIds, Databases.IN_LIMIT)) {
                stmt = connection.prepareStatement(Databases.getIN("SELECT cid FROM context WHERE enabled = 1 AND cid IN (", partition.size()));
                int index = 1;
                for (Integer contextId : partition) {
                    stmt.setInt(index++, contextId.intValue());
                }
                rs = stmt.executeQuery();
                if (rs.next()) {
                    List<Integer> notDisabledCids = new ArrayList<>();
                    do {
                        notDisabledCids.add(Integer.valueOf(rs.getInt(1)));
                    } while (rs.next());
                    throw new StorageException("The schema '" + schemaName + "' is not disabled. The following contexts are still enabled: " + notDisabledCids);
                }
                Databases.closeSQLStuff(stmt, rs);
                stmt = null;
                rs = null;
            }

        } catch (SQLException e) {
            LOG.error("SQL error: {}", e.getMessage(), e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }
}
