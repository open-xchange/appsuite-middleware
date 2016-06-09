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
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.java.Autoboxing.l;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.collections.keyvalue.MultiKey;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.storage.sqlStorage.OXUtilSQLStorage;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.AdminCacheExtended;
import com.openexchange.admin.tools.DatabaseTools;
import com.openexchange.database.Databases;
import com.openexchange.filestore.FileStorages;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.sql.DBUtils;

/**
 * @author d7
 * @author cutmasta
 */
public class OXUtilMySQLStorage extends OXUtilSQLStorage {

    /** The logger */
    final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXUtilMySQLStorage.class);

    private static final ThreadPools.ExpectedExceptionFactory<StorageException> EXCEPTION_FACTORY = new ThreadPools.ExpectedExceptionFactory<StorageException>() {

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

    public OXUtilMySQLStorage() {
        super();
    }

    @Override
    public int createMaintenanceReason(final MaintenanceReason reason) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;
        boolean rollback = false;
        try {
            con = cache.getConnectionForConfigDB();

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
                DBUtils.rollback(con);
            }
            DBUtils.closeSQLStuff(stmt);
            if (con != null) {
                try {
                    cache.pushConnectionForConfigDB(con);
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
            con = cache.getConnectionForConfigDB();
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
            DBUtils.rollback(con);
            rollback = false;
            LOG.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final SQLException sql) {
            DBUtils.rollback(con);
            rollback = false;
            LOG.error("SQL Error", sql);
            throw new StorageException(sql.toString(), sql);
        } finally {
            if (rollback) {
                DBUtils.rollback(con);
            }
            DBUtils.autocommit(con);
            try {
                cache.pushConnectionForConfigDB(con);
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

            final List<Object> params = new LinkedList<Object>();
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

            if (db.getClusterWeight() != null) {
                if (first) {
                    first = false;
                } else {
                    sqlBuilder.append(", ");
                }
                sqlBuilder.append("db_cluster.weight = ?");
                params.add(db.getClusterWeight());
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
            DBUtils.closeSQLStuff(prep);
        }
    }

    @Override
    public void changeFilestoreDataFor(Context ctx) throws StorageException {
        Connection configDbCon = null;
        boolean rollback = false;
        try {
            configDbCon = cache.getConnectionForConfigDB();
            configDbCon.setAutoCommit(false);
            rollback = true;

            changeFilestoreDataFor(ctx, configDbCon);

            configDbCon.commit();
            rollback = false;
        } catch (SQLException exp) {
            throw new StorageException(exp);
        } catch (PoolException e) {
            throw new StorageException(e);
        } finally {
            if (null != configDbCon) {
                if (rollback) {
                    Databases.rollback(configDbCon);
                }
                Databases.autocommit(configDbCon);
                try {
                    cache.pushConnectionForConfigDB(configDbCon);
                } catch (PoolException e) {
                    // Ignroe
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
        boolean rollback = false;
        try {
            con = cache.getConnectionForContext(contextId);
            con.setAutoCommit(false);
            rollback = true;

            prepareFilestoreUsageFor(user, ctx, con);

            con.commit();
            rollback = false;
        } catch (SQLException exp) {
            throw new StorageException(exp);
        } catch (PoolException e) {
            throw new StorageException(e);
        } finally {
            if (null != con) {
                if (rollback) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
                try {
                    cache.pushConnectionForContext(contextId, con);
                } catch (PoolException e) {
                    // Ignroe
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
                prep.executeUpdate();
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
        boolean rollback = false;
        try {
            con = cache.getConnectionForContext(contextId);
            con.setAutoCommit(false);
            rollback = true;

            cleanseFilestoreUsageFor(user, ctx, con);

            con.commit();
            rollback = false;
        } catch (SQLException exp) {
            throw new StorageException(exp);
        } catch (PoolException e) {
            throw new StorageException(e);
        } finally {
            if (null != con) {
                if (rollback) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
                try {
                    cache.pushConnectionForContext(contextId, con);
                } catch (PoolException e) {
                    // Ignroe
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
        boolean rollback = false;
        try {
            con = cache.getConnectionForContext(contextId);
            con.setAutoCommit(false);
            rollback = true;

            changeFilestoreDataFor(user, ctx, con);

            con.commit();
            rollback = false;
        } catch (SQLException exp) {
            throw new StorageException(exp);
        } catch (PoolException e) {
            throw new StorageException(e);
        } finally {
            if (null != con) {
                if (rollback) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
                try {
                    cache.pushConnectionForContext(contextId, con);
                } catch (PoolException e) {
                    // Ignroe
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
                    Databases.closeSQLStuff(prep);

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

        try {
            configdb_write_con = cache.getConnectionForConfigDB();
            configdb_write_con.setAutoCommit(false);

            final Integer id = fstore.getId();
            final String url = fstore.getUrl();
            if (null != url) {
                prep = configdb_write_con.prepareStatement("UPDATE filestore SET uri = ? WHERE id = ?");
                prep.setString(1, url);
                prep.setInt(2, id);
                prep.executeUpdate();
                prep.close();
            }

            final Long store_size = fstore.getSize();
            if (null != store_size && fstore.getSize() != -1) {
                final Long l_max = MAX_FILESTORE_CAPACITY;
                if (store_size.longValue() > l_max.longValue()) {
                    throw new StorageException("Filestore size to large for database (max=" + l_max.longValue() + ")");
                }
                long store_size_double = store_size.longValue();
                store_size_double = store_size_double << 20;
                prep = configdb_write_con.prepareStatement("UPDATE filestore SET size = ? WHERE id = ?");
                prep.setLong(1, store_size_double);
                prep.setInt(2, id);
                prep.executeUpdate();
                prep.close();
            }

            final Integer maxContexts = fstore.getMaxContexts();
            if (null != maxContexts) {
                prep = configdb_write_con.prepareStatement("UPDATE filestore SET max_context = ? WHERE id = ?");
                prep.setInt(1, maxContexts);
                prep.setInt(2, id);
                prep.executeUpdate();
                prep.close();
            }

            configdb_write_con.commit();
        } catch (final DataTruncation dt) {
            LOG.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            try {
                if (configdb_write_con != null && !configdb_write_con.getAutoCommit()) {
                    configdb_write_con.rollback();
                }
            } catch (final SQLException expd) {
                LOG.error("Error processing rollback of configdb connection!", expd);
            }
            throw AdminCache.parseDataTruncation(dt);
        } catch (final PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException exp) {
            LOG.error("SQL Error", exp);
            try {
                if (configdb_write_con != null && !configdb_write_con.getAutoCommit()) {
                    configdb_write_con.rollback();
                }
            } catch (final SQLException expd) {
                LOG.error("Error processing rollback of configdb connection!", expd);
            }
            throw new StorageException(exp);
        } finally {
            try {
                if (prep != null) {
                    prep.close();
                }
            } catch (final SQLException e) {
                LOG.error("Error closing statement", e);
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
    public void createDatabase(final Database db) throws StorageException {
        final OXUtilMySQLStorageCommon oxutilcommon = new OXUtilMySQLStorageCommon();
        oxutilcommon.createDatabase(db);
    }

    @Override
    public void deleteDatabase(final Database db) throws StorageException {
        final OXUtilMySQLStorageCommon oxutilcommon = new OXUtilMySQLStorageCommon();
        oxutilcommon.deleteDatabase(db);
    }

    @Override
    public void deleteMaintenanceReason(final int[] reason_ids) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = cache.getConnectionForConfigDB();
            con.setAutoCommit(false);
            for (final int element : reason_ids) {
                stmt = con.prepareStatement("DELETE FROM reason_text WHERE id = ?");
                stmt.setInt(1, element);
                stmt.executeUpdate();
                stmt.close();
            }
            con.commit();
        } catch (final PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            LOG.error("SQL Error", ecp);
            try {
                if (con != null && !con.getAutoCommit()) {
                    con.rollback();
                }
            } catch (final SQLException exp) {
                LOG.error("Error processing rollback of configdb connection!", exp);
            }
            throw new StorageException(ecp);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                LOG.error("Erroe closing statement", e);
            }
            try {
                if (con != null) {
                    cache.pushConnectionForConfigDB(con);
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
            con = cache.getConnectionForConfigDB();

            stmt = con.prepareStatement("SELECT id,text FROM reason_text");
            final ResultSet rs = stmt.executeQuery();
            final ArrayList<MaintenanceReason> list = new ArrayList<MaintenanceReason>();
            while (rs.next()) {
                list.add(new MaintenanceReason(rs.getInt("id"), rs.getString("text")));
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
                    cache.pushConnectionForConfigDB(con);
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
            con = cache.getConnectionForConfigDB();

            stmt = con.prepareStatement("SELECT id,text FROM reason_text WHERE text like ?");
            stmt.setString(1, new_search_pattern);
            final ResultSet rs = stmt.executeQuery();
            final ArrayList<MaintenanceReason> list = new ArrayList<MaintenanceReason>();
            while (rs.next()) {
                list.add(new MaintenanceReason(rs.getInt("id"), rs.getString("text")));
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
                    cache.pushConnectionForConfigDB(con);
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
            con = cache.getConnectionForConfigDB();

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
            final ResultSet rs = stmt.executeQuery();
            final ArrayList<MaintenanceReason> list = new ArrayList<MaintenanceReason>();
            while (rs.next()) {
                list.add(new MaintenanceReason(rs.getInt("id"), rs.getString("text")));
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
                    cache.pushConnectionForConfigDB(con);
                }
            } catch (final PoolException exp) {
                LOG.error("Error pushing configdb connection to pool!", exp);
            }

        }
    }

    @Override
    public Filestore[] listFilestores(String pattern, boolean omitUsage) throws StorageException {
        final Connection con;
        try {
            con = cache.getConnectionForConfigDB();
        } catch (final PoolException e) {
            throw new StorageException(e);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        final List<Filestore> stores = new ArrayList<Filestore>();
        try {
            stmt = con.prepareStatement("SELECT id,uri,size,max_context FROM filestore WHERE uri LIKE ?");
            stmt.setString(1, pattern.replace('*', '%'));
            result = stmt.executeQuery();
            while (result.next()) {
                int i = 1;
                final Filestore fs = new Filestore();
                fs.setId(I(result.getInt(i++)));
                fs.setUrl(result.getString(i++));
                fs.setSize(L(result.getLong(i++)));
                fs.setMaxContexts(I(result.getInt(i++)));
                stores.add(fs);
            }
        } catch (final SQLException e) {
            throw new StorageException(e.getMessage(), e);
        } finally {
            closeSQLStuff(result, stmt);
            try {
                cache.pushConnectionForConfigDB(con);
            } catch (final PoolException e) {
                LOG.error("Error pushing configdb connection to pool!", e);
            }
        }
        if (!omitUsage) {
            updateFilestoresWithRealUsage(stores);
        }
        return stores.toArray(new Filestore[stores.size()]);
    }

    private static List<Integer> listAllFilestoreIds() throws StorageException {
        final Connection con;
        try {
            con = cache.getConnectionForConfigDB();
        } catch (final PoolException e) {
            throw new StorageException(e);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        final List<Integer> ids = new ArrayList<Integer>();
        try {
            stmt = con.prepareStatement("SELECT id FROM filestore");
            result = stmt.executeQuery();
            while (result.next()) {
                ids.add(I(result.getInt(1)));
            }
        } catch (final SQLException e) {
            throw new StorageException(e);
        } finally {
            closeSQLStuff(result, stmt);
            try {
                cache.pushConnectionForConfigDB(con);
            } catch (final PoolException e) {
                LOG.error("Error pushing configdb connection to pool!", e);
            }
        }
        return ids;
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
                DBUtils.rollback(con);
            }
        }
    }

    @Override
    public int registerDatabase(final Database db) throws StorageException {
        Connection con = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean rollback = false;
        try {
            con = cache.getConnectionForConfigDB();

            final int db_id = nextId(con);
            final int c_id = db.isMaster() ? nextId(con) : -1;

            con.setAutoCommit(false);
            rollback = true;

            lock(con);

            prep = con.prepareStatement("INSERT INTO db_pool VALUES (?,?,?,?,?,?,?,?,?);");
            prep.setInt(1, db_id);
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
            prep.setInt(6, db.getPoolHardLimit());
            prep.setInt(7, db.getPoolMax());
            prep.setInt(8, db.getPoolInitial());
            if (db.getName() != null) {
                prep.setString(9, db.getName());
            } else {
                prep.setNull(9, Types.VARCHAR);
            }

            prep.executeUpdate();
            prep.close();

            if (db.isMaster()) {
                prep = con.prepareStatement("INSERT INTO db_cluster VALUES (?,?,?,?,?);");
                prep.setInt(1, c_id);

                // I am the master, set read_db_pool_id = 0
                prep.setInt(2, 0);
                prep.setInt(3, db_id);
                prep.setInt(4, db.getClusterWeight());
                prep.setInt(5, db.getMaxUnits());
                prep.executeUpdate();
                prep.close();

            } else {
                prep = con.prepareStatement("SELECT db_pool_id FROM db_pool WHERE db_pool_id = ?");
                prep.setInt(1, db.getMasterId());
                rs = prep.executeQuery();
                if (!rs.next()) {
                    throw new StorageException("No such master with ID=" + db.getMasterId());
                }
                rs.close();
                prep.close();

                prep = con.prepareStatement("SELECT cluster_id FROM db_cluster WHERE write_db_pool_id = ?");
                prep.setInt(1, db.getMasterId());
                rs = prep.executeQuery();
                if (!rs.next()) {
                    throw new StorageException("No such master with ID=" + db.getMasterId() + " IN db_cluster TABLE");
                }
                final int cluster_id = rs.getInt("cluster_id");
                rs.close();
                prep.close();

                prep = con.prepareStatement("UPDATE db_cluster SET read_db_pool_id=? WHERE cluster_id=?;");

                prep.setInt(1, db_id);
                prep.setInt(2, cluster_id);
                prep.executeUpdate();
                prep.close();

            }
            con.commit();
            rollback = false;
            return db_id;
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
                DBUtils.rollback(con);
            }
            DBUtils.closeSQLStuff(rs, prep);
            if (con != null) {
                try {
                    cache.pushConnectionForConfigDB(con);
                } catch (final PoolException e) {
                    LOG.error("Error pushing configdb connection to pool!", e);
                }
            }
        }
    }

    @Override
    public int registerFilestore(final Filestore fstore) throws StorageException {
        Connection con = null;
        long store_size = fstore.getSize();
        final Long l_max = MAX_FILESTORE_CAPACITY;
        if (store_size > l_max.longValue()) {
            throw new StorageException("Filestore size to large for database (max=" + l_max.longValue() + ")");
        }
        store_size = store_size << 20;
        PreparedStatement stmt = null;
        boolean rollback = false;
        try {
            con = cache.getConnectionForConfigDB();

            final int fstore_id = nextId(con);

            con.setAutoCommit(false);
            rollback = true;
            stmt = con.prepareStatement("INSERT INTO filestore (id,uri,size,max_context) VALUES (?,?,?,?)");
            stmt.setInt(1, fstore_id);
            stmt.setString(2, fstore.getUrl());
            stmt.setLong(3, store_size);
            stmt.setInt(4, fstore.getMaxContexts());
            stmt.executeUpdate();
            con.commit();
            rollback = false;

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
            if (rollback) {
                DBUtils.rollback(con);
            }
            DBUtils.closeSQLStuff(stmt);
            if (con != null) {
                try {
                    cache.pushConnectionForConfigDB(con);
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
            con = cache.getConnectionForConfigDB();

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
            DBUtils.closeSQLStuff(rs, stmt);
            if (con != null) {
                try {
                    cache.pushConnectionForConfigDB(con);
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

        return findFilestoreForEntity(false);
    }

    @Override
    public Filestore findFilestoreForContext() throws StorageException {
        return findFilestoreForEntity(true);
    }

    private Filestore findFilestoreForEntity(boolean forContext) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getConnectionForConfigDB();

            // Define candidate class
            class Candidate {
                final int id;
                final int maxNumberOfEntities;
                final int numberOfEntities;

                Candidate(final int id, final int maxNumberOfContexts, final int numberOfContexts) {
                    super();
                    this.id = id;
                    this.maxNumberOfEntities = maxNumberOfContexts;
                    this.numberOfEntities = numberOfContexts;
                }
            }

            // Load potential candidates
            List<Candidate> candidates = new LinkedList<Candidate>();
            {
                stmt = con.prepareStatement("SELECT filestore.id, filestore.max_context, COUNT(context.cid) AS num FROM filestore LEFT JOIN context ON filestore.id=context.filestore_id GROUP BY filestore.id ORDER BY num ASC");
                rs = stmt.executeQuery();

                if (!rs.next()) {
                    // None found
                    throw new StorageException("No filestore found");
                }

                do {
                    candidates.add(new Candidate(rs.getInt(1), rs.getInt(2), rs.getInt(3)));
                } while (rs.next());

                // Close resources
                closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;
            }

            // Determine available pools/schemas
            Set<PoolAndSchema> pools = new LinkedHashSet<PoolAndSchema>();
            {
                try {
                    stmt = con.prepareStatement("SELECT DISTINCT write_db_pool_id, db_schema FROM context_server2db_pool WHERE server_id=?");
                    stmt.setInt(1, cache.getServerId());
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        pools.add(new PoolAndSchema(rs.getInt(1), rs.getString(2)));
                    }
                } catch (PoolException e) {
                    LOG.error("SQL Error", e);
                    throw new StorageException(e);
                } catch (SQLException e) {
                    LOG.error("SQL Error", e);
                    throw new StorageException(e);
                } finally {
                    closeSQLStuff(rs, stmt);
                    rs = null;
                    stmt = null;
                }
            }

            // Determine file storage user/context counts
            Map<Integer, Integer> filestoreUserCounts = getFilestoreUserCounts(pools);

            // Find a suitable one from ordered list of candidates
            boolean loadRealUsage = false;
            for (Candidate candidate : candidates) {
                if (candidate.maxNumberOfEntities > 0) {
                    int entityCount = candidate.numberOfEntities;

                    // Get user count information
                    Integer count = filestoreUserCounts.get(Integer.valueOf(candidate.id));
                    if (null != count) {
                        entityCount += count.intValue();
                    }

                    if (entityCount < candidate.maxNumberOfEntities) {
                        FilestoreUsage userFilestoreUsage = new FilestoreUsage(null == count ? 0 : count.intValue(), 0L);

                        // Get filestore
                        Filestore filestore = getFilestore(candidate.id, loadRealUsage, pools, null, userFilestoreUsage, con);
                        if (forContext) {
                            if (enoughSpaceForContext(filestore)) {
                                return filestore;
                            }
                        } else {
                            if (enoughSpaceForUser(filestore)) {
                                return filestore;
                            }
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
            DBUtils.closeSQLStuff(rs, stmt);
            if (con != null) {
                try {
                    cache.pushConnectionForConfigDB(con);
                } catch (final PoolException exp) {
                    LOG.error("Error pushing configdb connection to pool!", exp);
                }
            }
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
        return 0 == filestore.getMaxContexts().intValue() || filestore.getCurrentContexts().intValue() >= filestore.getMaxContexts().intValue();
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
        final String value = prop.getProp("AVERAGE_USER_SIZE", "100");
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
            con = cache.getConnectionForConfigDB();

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
                DBUtils.rollback(con);
            }
            DBUtils.closeSQLStuff(prep);
            if (con != null) {
                try {
                    cache.pushConnectionForConfigDB(con);
                } catch (final PoolException e) {
                    LOG.error("Error pushing configdb connection to pool!", e);
                }
            }
        }

    }

    @Override
    public Database[] searchForDatabase(final String search_pattern) throws StorageException {

        Connection con = null;
        PreparedStatement pstmt = null;
        PreparedStatement cstmt = null;

        try {

            con = cache.getConnectionForConfigDB();
            final String my_search_pattern = search_pattern.replace('*', '%');

            pstmt = con.prepareStatement("SELECT db_pool_id,url,driver,login,password,hardlimit,max,initial,name,weight,max_units,read_db_pool_id,write_db_pool_id FROM db_pool JOIN db_cluster ON ( db_pool_id = db_cluster.write_db_pool_id OR db_pool_id = db_cluster.read_db_pool_id) WHERE name LIKE ? OR db_pool_id LIKE ? OR url LIKE ?");
            pstmt.setString(1, my_search_pattern);
            pstmt.setString(2, my_search_pattern);
            pstmt.setString(3, my_search_pattern);
            final ResultSet rs = pstmt.executeQuery();
            final ArrayList<Database> tmp = new ArrayList<Database>();

            while (rs.next()) {

                final Database db = new Database();

                Boolean ismaster = Boolean.TRUE;
                final int readid = rs.getInt("read_db_pool_id");
                final int writeid = rs.getInt("write_db_pool_id");
                final int id = rs.getInt("db_pool_id");
                int masterid = 0;
                int nrcontexts = 0;
                if (readid == id) {
                    ismaster = Boolean.FALSE;
                    masterid = writeid;
                } else {
                    // we are master
                    cstmt = con.prepareStatement("SELECT COUNT(cid) FROM context_server2db_pool WHERE write_db_pool_id = ?");
                    cstmt.setInt(1, writeid);
                    final ResultSet rs1 = cstmt.executeQuery();
                    if (!rs1.next()) {
                        throw new StorageException("Unable to count contexts");
                    }
                    nrcontexts = Integer.parseInt(rs1.getString("COUNT(cid)"));
                    rs1.close();
                    cstmt.close();
                }
                db.setClusterWeight(rs.getInt("weight"));
                db.setName(rs.getString("name"));
                db.setDriver(rs.getString("driver"));
                db.setId(rs.getInt("db_pool_id"));
                db.setLogin(rs.getString("login"));
                db.setMaster(ismaster.booleanValue());
                db.setMasterId(masterid);
                db.setMaxUnits(rs.getInt("max_units"));
                db.setPassword(rs.getString("password"));
                db.setPoolHardLimit(rs.getInt("hardlimit"));
                db.setPoolInitial(rs.getInt("initial"));
                db.setPoolMax(rs.getInt("max"));
                db.setUrl(rs.getString("url"));
                db.setCurrentUnits(nrcontexts);
                tmp.add(db);
            }
            rs.close();

            return tmp.toArray(new Database[tmp.size()]);

        } catch (final PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            LOG.error("SQL Error", ecp);
            throw new StorageException(ecp);
        } finally {

            try {
                if (cstmt != null) {
                    cstmt.close();
                }
            } catch (final SQLException e) {
                LOG.error("Error closing statement", e);
            }
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (final SQLException e) {
                LOG.error("Error closing statement", e);
            }

            if (con != null) {
                try {
                    cache.pushConnectionForConfigDB(con);
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

            con = cache.getConnectionForConfigDB();

            stmt = con.prepareStatement("SELECT name,server_id FROM server WHERE name LIKE ? OR server_id = ?");

            final String my_search_pattern = search_pattern.replace('*', '%');
            stmt.setString(1, my_search_pattern);
            stmt.setString(2, my_search_pattern);

            final ResultSet rs = stmt.executeQuery();
            final ArrayList<Server> tmp = new ArrayList<Server>();
            while (rs.next()) {
                final Server srv = new Server();
                srv.setId(rs.getInt("server_id"));
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

            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                LOG.error("Error closing statement", e);
            }
            if (con != null) {
                try {
                    if (con != null) {
                        cache.pushConnectionForConfigDB(con);
                    }
                } catch (final PoolException exp) {
                    LOG.error("Error pushing configdb connection to pool!", exp);
                }
            }

        }
    }

    @Override
    public void unregisterDatabase(final int dbId, final boolean isMaster) throws StorageException {
        final Connection con;
        try {
            con = cache.getConnectionForConfigDB();
        } catch (final PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        }
        PreparedStatement stmt = null;
        try {
            DBUtils.startTransaction(con);

            lock(con);

            if (isMaster) {
                try {
                    stmt = con.prepareStatement("DELETE db_pool FROM db_pool JOIN db_cluster WHERE db_pool.db_pool_id=db_cluster.read_db_pool_id AND db_cluster.write_db_pool_id=?");
                    stmt.setInt(1, dbId);
                    stmt.executeUpdate();
                } finally {
                    closeSQLStuff(stmt);
                }
                try {
                    stmt = con.prepareStatement("DELETE FROM db_cluster WHERE write_db_pool_id=?");
                    stmt.setInt(1, dbId);
                    stmt.executeUpdate();
                } finally {
                    closeSQLStuff(stmt);
                }
            } else {
                try {
                    stmt = con.prepareStatement("UPDATE db_cluster SET read_db_pool_id=0 WHERE read_db_pool_id=?");
                    stmt.setInt(1, dbId);
                    stmt.executeUpdate();
                } finally {
                    closeSQLStuff(stmt);
                }
            }
            try {
                stmt = con.prepareStatement("DELETE FROM db_pool WHERE db_pool_id=?");
                stmt.setInt(1, dbId);
                stmt.executeUpdate();
            } finally {
                closeSQLStuff(stmt);
            }
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final RuntimeException e) {
            rollback(con);
            LOG.error("Runtime Error", e);
            throw new StorageException(e);
        } finally {
            try {
                cache.pushConnectionForConfigDB(con);
            } catch (final PoolException e) {
                LOG.error("Error pushing configdb connection to pool!", e);
            }
        }
    }

    @Override
    public void unregisterFilestore(final int store_id) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = cache.getConnectionForConfigDB();
            con.setAutoCommit(false);
            stmt = con.prepareStatement("DELETE FROM filestore WHERE id = ?");
            stmt.setInt(1, store_id);
            stmt.executeUpdate();
            con.commit();
        } catch (final PoolException pe) {
            LOG.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            LOG.error("SQL Error", ecp);
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (final SQLException exp) {
                LOG.error("Error processing rollback of ox connection!", exp);
            }
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
                    cache.pushConnectionForConfigDB(con);
                }
            } catch (final PoolException exp) {
                LOG.error("Error pushing configdb connection to pool!", exp);
            }
        }
    }

    @Override
    public void unregisterServer(final int server_id) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = cache.getConnectionForConfigDB();
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
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                LOG.error("Error closing statement", e);
            }
            if (con != null) {
                try {
                    cache.pushConnectionForConfigDB(con);
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
            con = cache.getConnectionForConfigDB();
            return getFilestoreURI(filestoreId, con);
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            if (null != con) {
                try {
                    cache.pushConnectionForConfigDB(con);
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
            configDbCon = cache.getConnectionForConfigDB();

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
                    cache.pushConnectionForConfigDB(configDbCon);
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
                return new ArrayList<URI>(1);
            }

            class FidAndName {
                final int filestoreId;
                final String name;

                FidAndName(int filestoreId, String name) {
                    super();
                    this.filestoreId = filestoreId;
                    this.name = name;
                }
            };

            List<FidAndName> fids = new LinkedList<FidAndName>();
            do {
                int filestoreId = result.getInt(1);
                String path = result.getString(2);
                fids.add(new FidAndName(filestoreId, path));
            } while (result.next());
            closeSQLStuff(result, stmt);
            result = null;
            stmt = null;

            Map<Integer, String> baseUris = new HashMap<Integer, String>(fids.size());
            List<URI> uris = new ArrayList<URI>(fids.size());
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
            con = cache.getConnectionForConfigDB();
            return getFilestoreBasic(id, con);
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            if (null != con) {
                try {
                    cache.pushConnectionForConfigDB(con);
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
            con = cache.getConnectionForConfigDB();
            return getFilestore(id, loadRealUsage, con);
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            if (null != con) {
                try {
                    cache.pushConnectionForConfigDB(con);
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
        return getFilestore(id, loadRealUsage, null, null, null, configdbCon);
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
    private Filestore getFilestore(int id, boolean loadRealUsage, Collection<PoolAndSchema> pools, FilestoreUsage contextFilestoreUsage, FilestoreUsage userFilestoreUsage, Connection configdbCon) throws StorageException {
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
        FilestoreUsage usrCounts = null == userFilestoreUsage ? getUserUsage(id, loadRealUsage, pools, configdbCon) : userFilestoreUsage;
        FilestoreUsage ctxCounts = null == contextFilestoreUsage ? getContextUsage(id, loadRealUsage, configdbCon) : contextFilestoreUsage;
        fs.setUsed(L(toMB(ctxCounts.usage + usrCounts.usage)));
        fs.setCurrentContexts(I(ctxCounts.entityCount + usrCounts.entityCount));
        fs.setReserved(L((getAverageFilestoreSpaceForContext() * ctxCounts.entityCount) + (getAverageFilestoreSpaceForUser() * usrCounts.entityCount)));
        return fs;
    }

    private void updateFilestoresWithRealUsage(final List<Filestore> stores) throws StorageException {
        Collection<FilestoreContextBlock> blocks = makeBlocksFromFilestoreContexts();

        // Sort by database.
        Map<Integer, Collection<FilestoreContextBlock>> dbMap = new HashMap<Integer, Collection<FilestoreContextBlock>>();
        for (FilestoreContextBlock block : blocks) {
            int poolId = block.writeDBPoolID;
            String schema = block.schema;
            if (OXToolStorageInterface.getInstance().schemaBeingLockedOrNeedsUpdate(poolId, schema)) {
                throw new StorageException(new DatabaseUpdateException("Database with pool-id " + poolId + " and schema \"" + schema + "\" needs update. Please run \"runupdate\" for that database."));
            }

            Collection<FilestoreContextBlock> dbBlock = dbMap.get(I(block.writeDBPoolID));
            if (null == dbBlock) {
                dbBlock = new ArrayList<FilestoreContextBlock>();
                dbMap.put(I(block.writeDBPoolID), dbBlock);
            }
            dbBlock.add(block);
        }

        // Create callables for every database server and submit them to the completion service.
        final CompletionService<Void> completionService = new ThreadPoolCompletionService<Void>(AdminServiceRegistry.getInstance().getService(ThreadPoolService.class));
        int taskCount = 0;
        for (final Collection<FilestoreContextBlock> dbBlock : dbMap.values()) {
            completionService.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    updateBlocksInSameDBWithFilestoreUsage(dbBlock);
                    return null;
                }
            });
            taskCount++;
        }

        // Await completion
        ThreadPools.<Void, StorageException> takeCompletionService(completionService, taskCount, EXCEPTION_FACTORY);

        // Combine the information from the blocks into the stores collection.
        updateFilestoresWithUsageFromBlocks(stores, blocks);

        // We read bytes from the database but the RMI client wants to see mega bytes.
        for (final Filestore store: stores){
            store.setSize(L(toMB(l(store.getSize()))));
            store.setUsed(L(toMB(l(store.getUsed()))));
        }
    }

    private static Collection<FilestoreContextBlock> makeBlocksFromFilestoreContexts() throws StorageException {
        final ConcurrentMap<MultiKey, FilestoreContextBlock> blocks = new ConcurrentHashMap<MultiKey, FilestoreContextBlock>();

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            int serverId = cache.getServerId();

            con = cache.getConnectionForConfigDB();
            stmt = con.prepareStatement("SELECT d.cid,d.write_db_pool_id,d.db_schema,c.filestore_id FROM context_server2db_pool d JOIN context c ON d.cid=c.cid WHERE d.server_id=?");
            stmt.setInt(1, serverId);
            result = stmt.executeQuery();
            while (result.next()) {
                FilestoreInfo temp = new FilestoreInfo(result.getInt(1), result.getInt(2), result.getString(3), result.getInt(4));
                MultiKey key = new MultiKey(I(temp.writeDBPoolID), temp.dbSchema, I(temp.filestoreID));
                FilestoreContextBlock block = blocks.get(key);
                if (null == block) {
                    block = new FilestoreContextBlock(temp.writeDBPoolID, temp.dbSchema, temp.filestoreID);
                    blocks.put(key, block);
                }
                block.addForContext(temp);
            }

            closeSQLStuff(result, stmt);

            Set<PoolAndSchema> retval = new LinkedHashSet<PoolAndSchema>();

            stmt = con.prepareStatement("SELECT write_db_pool_id, db_schema FROM context_server2db_pool WHERE server_id=?");
            stmt.setInt(1, serverId);
            result = stmt.executeQuery();
            while (result.next()) {
                retval.add(new PoolAndSchema(result.getInt(1), result.getString(2)));
            }

            CompletionService<Void> completionService = new ThreadPoolCompletionService<Void>(AdminServiceRegistry.getInstance().getService(ThreadPoolService.class));
            int taskCount = 0;

            for (final PoolAndSchema poolAndSchema : retval) {
                final AdminCacheExtended cache = OXUtilMySQLStorage.cache;
                completionService.submit(new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {
                        Connection con = null;
                        PreparedStatement stmt = null;
                        ResultSet result = null;
                        try {
                            con = cache.getWRITENoTimeoutConnectionForPoolId(poolAndSchema.poolId, poolAndSchema.dbSchema);
                            stmt = con.prepareStatement("SELECT cid, id, filestore_id FROM user WHERE filestore_id > 0");
                            result = stmt.executeQuery();

                            while (result.next()) {
                                FilestoreInfo temp = new FilestoreInfo(result.getInt(1), result.getInt(2), poolAndSchema.poolId, poolAndSchema.dbSchema, result.getInt(3));
                                MultiKey key = new MultiKey(I(temp.writeDBPoolID), temp.dbSchema, I(temp.filestoreID));

                                FilestoreContextBlock block = blocks.get(key);
                                if (null == block) {
                                    FilestoreContextBlock newBlock = new FilestoreContextBlock(poolAndSchema.poolId, poolAndSchema.dbSchema, temp.filestoreID);
                                    block = blocks.putIfAbsent(key, newBlock);
                                    if (null == block) {
                                        block = newBlock;
                                    }
                                }

                                block.addForUser(temp);
                            }

                            return null;
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
                                    cache.pushWRITENoTimeoutConnectionForPoolId(poolAndSchema.poolId, con);
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
            ThreadPools.<Void, StorageException> takeCompletionService(completionService, taskCount, EXCEPTION_FACTORY);
        } catch (final PoolException e) {
            throw new StorageException(e);
        } catch (final SQLException e) {
            throw new StorageException(e);
        } finally {
            closeSQLStuff(result, stmt);
            if (null != con) {
                try {
                    cache.pushConnectionForConfigDB(con);
                } catch (final PoolException e) {
                    throw new StorageException(e);
                }
            }
        }


        return blocks.values();
    }

    protected static void updateBlocksInSameDBWithFilestoreUsage(Collection<FilestoreContextBlock> blocks) throws StorageException {
        for (FilestoreContextBlock block : blocks) {
            updateBlockWithFilestoreUsage(block);
        }
    }

    private static void updateBlockWithFilestoreUsage(final FilestoreContextBlock block) throws StorageException {
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

    private void updateFilestoresWithUsageFromBlocks(final List<Filestore> stores, final Collection<FilestoreContextBlock> blocks) throws StorageException {
        Map<Integer, Long> filestore2ctxUsage = new HashMap<Integer, Long>();
        Map<Integer, Integer> filestore2ctxCount = new HashMap<Integer, Integer>();

        Map<Integer, Long> filestore2usrUsage = new HashMap<Integer, Long>();
        Map<Integer, Integer> filestore2usrCount = new HashMap<Integer, Integer>();

        for (FilestoreContextBlock block : blocks) {
            for (FilestoreInfo info : block.contextFilestores.values()) {
                Integer fid = I(info.filestoreID);
                long usage = filestore2ctxUsage.containsKey(fid) ? l(filestore2ctxUsage.get(fid)) : 0;
                filestore2ctxUsage.put(fid, L(usage + info.usage));
                filestore2ctxCount.put(fid, I(filestore2ctxCount.containsKey(fid) ? i(filestore2ctxCount.get(fid)) + 1 : 1));
            }

            for (Map<Integer, FilestoreInfo> usersInfo : block.userFilestores.values()) {
                for (FilestoreInfo info : usersInfo.values()) {
                    Integer fid = I(info.filestoreID);
                    long usage = filestore2usrUsage.containsKey(fid) ? l(filestore2usrUsage.get(fid)) : 0;
                    filestore2usrUsage.put(fid, L(usage + info.usage));
                    filestore2usrCount.put(fid, I(filestore2usrCount.containsKey(fid) ? i(filestore2usrCount.get(fid)) + 1 : 1));
                }
            }
        }

        for (Filestore store : stores) {
            Long usedBytesCtx = filestore2ctxUsage.get(store.getId());
            if (null == usedBytesCtx) {
                usedBytesCtx = L(0);
            }
            Integer numContexts = filestore2ctxCount.get(store.getId());
            if (null == numContexts) {
                numContexts = I(0);
            }

            Long usedBytesUsr = filestore2usrUsage.get(store.getId());
            if (null == usedBytesUsr) {
                usedBytesUsr = L(0);
            }
            Integer numUsers = filestore2usrCount.get(store.getId());
            if (null == numUsers) {
                numUsers = I(0);
            }


            store.setUsed(Long.valueOf(usedBytesCtx.longValue() + usedBytesUsr.longValue()));
            store.setCurrentContexts(Integer.valueOf(numContexts.intValue() + numUsers.intValue()));
            store.setReserved(L((getAverageFilestoreSpaceForContext() * i(numContexts)) + (getAverageFilestoreSpaceForUser() * i(numUsers))));
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
        return getContextUsage(filestoreId, loadRealUsage, null);
    }

    /**
     * Loads the context usage information for a file storage.
     *
     * @param filestoreId the unique identifier of the file storage.
     * @param loadRealUsage <code>true</code> to load the file storage usage from every context in it. BEWARE! This is a slow operation.
     * @return The {@link FilestoreUsage} object for the file storage.
     * @throws StorageException if some problem occurs loading the information.
     */
    private FilestoreUsage getContextUsage(int filestoreId, boolean loadRealUsage, Connection configdbCon) throws StorageException {
        if (false == loadRealUsage) {
            // Only load context count for given file storage
            Connection con = configdbCon;
            boolean push = false;
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                if (null == con) {
                    con = cache.getConnectionForConfigDB();
                    push = true;
                }
                stmt = con.prepareStatement("SELECT COUNT(cid) FROM context WHERE filestore_id=?");
                stmt.setInt(1, filestoreId);
                result = stmt.executeQuery();
                if (false == result.next()) {
                    return new FilestoreUsage(0, 0L);
                }

                return new FilestoreUsage(result.getInt(1), 0L);
            } catch (PoolException e) {
                LOG.error("SQL Error", e);
                throw new StorageException(e);
            } catch (SQLException e) {
                LOG.error("SQL Error", e);
                throw new StorageException(e);
            } finally {
                closeSQLStuff(result, stmt);
                if (push && null != con) {
                    try {
                        cache.pushConnectionForConfigDB(con);
                    } catch (final PoolException e) {
                        LOG.error("Error pushing configdb connection to pool!", e);
                    }
                }
            }
        }

        // Get count and usage for given file storage
        Map<PoolAndSchema, List<Integer>> map = new LinkedHashMap<PoolAndSchema, List<Integer>>();
        int count = 0;

        {
            Connection con = configdbCon;
            boolean push = false;
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                if (null == con) {
                    con = cache.getConnectionForConfigDB();
                    push = true;
                }
                stmt = con.prepareStatement("SELECT c.cid, s.write_db_pool_id, s.db_schema FROM context AS c JOIN context_server2db_pool AS s ON c.cid=s.cid WHERE c.filestore_id=? AND s.server_id=?");
                stmt.setInt(1, filestoreId);
                stmt.setInt(2, cache.getServerId());
                result = stmt.executeQuery();
                while (result.next()) {
                    count++;

                    PoolAndSchema poolAndSchema = new PoolAndSchema(result.getInt(2), result.getString(3));
                    List<Integer> cids = map.get(poolAndSchema);
                    if (null == cids) {
                        cids = new LinkedList<Integer>();
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
                if (push && null != con) {
                    try {
                        cache.pushConnectionForConfigDB(con);
                    } catch (final PoolException e) {
                        LOG.error("Error pushing configdb connection to pool!", e);
                    }
                }
            }
        }

        CompletionService<Long> completionService = new ThreadPoolCompletionService<Long>(AdminServiceRegistry.getInstance().getService(ThreadPoolService.class));
        int taskCount = 0;

        for (Map.Entry<PoolAndSchema, List<Integer>> entry : map.entrySet()) {
            final PoolAndSchema poolAndSchema = entry.getKey();
            final List<Integer> cids = entry.getValue();
            final AdminCacheExtended cache = OXUtilMySQLStorage.cache;

            completionService.submit(new Callable<Long>() {

                @Override
                public Long call() throws StorageException {
                    Connection con = null;
                    PreparedStatement stmt = null;
                    ResultSet result = null;
                    try {
                        con = cache.getWRITENoTimeoutConnectionForPoolId(poolAndSchema.poolId, poolAndSchema.dbSchema);
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
                                cache.pushWRITENoTimeoutConnectionForPoolId(poolAndSchema.poolId, con);
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
     * @return The {@link FilestoreUsage} object for the file storage.
     * @throws StorageException if some problem occurs loading the information.
     */
    private FilestoreUsage getUserUsage(final int filestoreId, boolean loadRealUsage) throws StorageException {
        return getUserUsage(filestoreId, loadRealUsage, null, null);
    }

    /**
     * Loads the user usage information for a file storage.
     *
     * @param filestoreId the unique identifier of the file storage.
     * @param loadRealUsage <code>true</code> to load the file storage usage from every context in it. BEWARE! This is a slow operation.
     * @param pools Available database pools
     * @return The {@link FilestoreUsage} object for the file storage.
     * @throws StorageException if some problem occurs loading the information.
     */
    private FilestoreUsage getUserUsage(final int filestoreId, boolean loadRealUsage, Collection<PoolAndSchema> pools, Connection configdbCon) throws StorageException {
        Collection<PoolAndSchema> retval = pools;

        if (null == retval) {
            Connection con = configdbCon;
            boolean push = false;
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                if (null == con) {
                    con = cache.getConnectionForConfigDB();
                    push = true;
                }
                stmt = con.prepareStatement("SELECT DISTINCT write_db_pool_id, db_schema FROM context_server2db_pool WHERE server_id=?");
                stmt.setInt(1, cache.getServerId());
                result = stmt.executeQuery();
                retval = new LinkedHashSet<PoolAndSchema>();
                while (result.next()) {
                    retval.add(new PoolAndSchema(result.getInt(1), result.getString(2)));
                }
            } catch (PoolException e) {
                LOG.error("SQL Error", e);
                throw new StorageException(e);
            } catch (SQLException e) {
                LOG.error("SQL Error", e);
                throw new StorageException(e);
            } finally {
                closeSQLStuff(result, stmt);
                if (push && null != con) {
                    try {
                        cache.pushConnectionForConfigDB(con);
                    } catch (final PoolException e) {
                        LOG.error("Error pushing configdb connection to pool!", e);
                    }
                }
            }
        }

        if (false == loadRealUsage) {

            ThreadPoolCompletionService<Integer> completionService = new ThreadPoolCompletionService<Integer>(AdminServiceRegistry.getInstance().getService(ThreadPoolService.class));

            final AdminCacheExtended cache = OXUtilMySQLStorage.cache;
            for (final PoolAndSchema poolAndSchema : retval) {
                completionService.submit(new Callable<Integer>() {

                    @Override
                    public Integer call() throws StorageException {
                        Connection con = null;
                        PreparedStatement stmt = null;
                        ResultSet result = null;
                        try {
                            con = cache.getWRITENoTimeoutConnectionForPoolId(poolAndSchema.poolId, poolAndSchema.dbSchema);
                            stmt = con.prepareStatement("SELECT COUNT(id) FROM user WHERE filestore_id=?");
                            stmt.setInt(1, filestoreId);
                            result = stmt.executeQuery();
                            int numUsers = 0;
                            if (result.next()) {
                                numUsers = result.getInt(1);
                            }
                            return Integer.valueOf(numUsers);
                        } catch (PoolException e) {
                            LOG.error("Pooling Error", e);
                            throw new StorageException(e);
                        } catch (SQLException e) {
                            LOG.error("SQL Error", e);
                            throw new StorageException(e);
                        } finally {
                            closeSQLStuff(result, stmt);
                            if (null != con) {
                                try {
                                    cache.pushWRITENoTimeoutConnectionForPoolId(poolAndSchema.poolId, con);
                                } catch (PoolException e) {
                                    LOG.error("Error pushing connection to pool!", e);
                                }
                            }
                        }
                    }
                });
            }

            // Await completion
            List<Integer> counts = ThreadPools.<Integer, StorageException> takeCompletionService(completionService, completionService.getNumberOfSubmits(), EXCEPTION_FACTORY);

            int numUsers = 0;
            for (Integer count : counts) {
                numUsers += count.intValue();
            }
            return new FilestoreUsage(numUsers, 0L);
        }

        CompletionService<FilestoreUsage> completionService = new ThreadPoolCompletionService<FilestoreUsage>(AdminServiceRegistry.getInstance().getService(ThreadPoolService.class));
        int taskCount = 0;

        for (final PoolAndSchema poolAndSchema : retval) {
            final AdminCacheExtended cache = OXUtilMySQLStorage.cache;
            completionService.submit(new Callable<FilestoreUsage>() {

                @Override
                public FilestoreUsage call() throws Exception {
                    Connection con = null;
                    PreparedStatement stmt = null;
                    ResultSet result = null;
                    try {
                        con = cache.getWRITENoTimeoutConnectionForPoolId(poolAndSchema.poolId, poolAndSchema.dbSchema);
                        stmt = con.prepareStatement("SELECT u.cid, u.id, fu.used FROM user AS u JOIN filestore_usage AS fu ON u.cid=fu.cid AND u.id=fu.user WHERE u.filestore_id=?");
                        stmt.setInt(1, filestoreId);
                        result = stmt.executeQuery();

                        long used = 0L;
                        int numUsers = 0;
                        while (result.next()) {
                            used += result.getLong(3);
                            numUsers++;
                        }

                        return new FilestoreUsage(numUsers, used);
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
                                cache.pushWRITENoTimeoutConnectionForPoolId(poolAndSchema.poolId, con);
                            } catch (PoolException e) {
                                LOG.error("Error pushing connection to pool!", e);
                            }
                        }
                    }
                }
            });
        }

        // Await completion
        List<FilestoreUsage> counts = ThreadPools.<FilestoreUsage, StorageException> takeCompletionService(completionService, taskCount, EXCEPTION_FACTORY);

        long used = 0L;
        int numUsers = 0;
        for (FilestoreUsage fu : counts) {
            numUsers += fu.entityCount;
            used += fu.usage;
        }
        return new FilestoreUsage(numUsers, used);
    }

    /**
     * Gets the file storage context counts for given configDb connection
     *
     * @param con The configDb connection
     * @return The context counts (as a file storage to count mapping)
     * @throws StorageException If operation fails
     */
    private Map<Integer, Integer> getFilestoreContextCounts(Connection con) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT filestore_id, cid FROM context WHERE filestore_id>0");
            result = stmt.executeQuery();

            if (false == result.next()) {
                return Collections.emptyMap();
            }

            Map<Integer, Integer> map = new HashMap<Integer, Integer>();
            do {
                Integer fsId = Integer.valueOf(result.getInt(1));
                Integer count = map.get(fsId);
                map.put(fsId, null == count ? Integer.valueOf(1) : Integer.valueOf(count.intValue() + 1));
            } while (result.next());
            return map;
        } catch (SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    /**
     * Gets the file storage user counts for given database pools
     *
     * @param pools The database pools
     * @return The user counts (as a file storage to count mapping)
     * @throws StorageException If operation fails
     */
    private Map<Integer, Integer> getFilestoreUserCounts(Collection<PoolAndSchema> pools) throws StorageException {
        CompletionService<Map<Integer, Integer>> completionService = new ThreadPoolCompletionService<Map<Integer, Integer>>(AdminServiceRegistry.getInstance().getService(ThreadPoolService.class));
        int taskCount = 0;

        for (final PoolAndSchema poolAndSchema : pools) {
            final AdminCacheExtended cache = OXUtilMySQLStorage.cache;
            completionService.submit(new Callable<Map<Integer, Integer>>() {

                @Override
                public Map<Integer, Integer> call() throws StorageException {
                    Connection con = null;
                    PreparedStatement stmt = null;
                    ResultSet result = null;
                    try {
                        con = cache.getWRITENoTimeoutConnectionForPoolId(poolAndSchema.poolId, poolAndSchema.dbSchema);

                        if (!DatabaseTools.columnExists(con, "user","filestore_id")) {
                            // This schema cannot hold users having an individual file storage assigned
                        	return Collections.<Integer,Integer>emptyMap();
                        }

                        stmt = con.prepareStatement("SELECT u.filestore_id, u.id FROM user AS u JOIN filestore_usage AS fu ON u.cid=fu.cid AND u.id=fu.user WHERE u.filestore_id>0");
                        result = stmt.executeQuery();

                        if (false == result.next()) {
                            return Collections.emptyMap();
                        }

                        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
                        do {
                            Integer fsId = Integer.valueOf(result.getInt(1));
                            Integer count = map.get(fsId);
                            map.put(fsId, null == count ? Integer.valueOf(1) : Integer.valueOf(count.intValue() + 1));
                        } while (result.next());
                        return map;
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
                                cache.pushWRITENoTimeoutConnectionForPoolId(poolAndSchema.poolId, con);
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
        List<Map<Integer, Integer>> counts = ThreadPools.<Map<Integer, Integer>, StorageException> takeCompletionService(completionService, taskCount, EXCEPTION_FACTORY);

        Map<Integer, Integer> mainMap = new HashMap<Integer, Integer>();
        for (Map<Integer, Integer> map : counts) {
            if (false == map.isEmpty()) {
                for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                    Integer fsId = entry.getKey();
                    Integer count = entry.getValue();

                    Integer prevCount = mainMap.get(fsId);
                    mainMap.put(fsId, null == prevCount ? count : Integer.valueOf(prevCount.intValue() + count.intValue()));
                }
            }
        }

        return mainMap;
    }

    // -------------------------------------------------------------------------------------------------------------------------

    private static class PoolAndSchema {

        final int poolId;
        final String dbSchema;
        private final int hash;

        PoolAndSchema(int poolId, String dbSchema) {
            super();
            this.poolId = poolId;
            this.dbSchema = dbSchema;

            int result = 31 * 1 + poolId;
            result = 31 * result + ((dbSchema == null) ? 0 : dbSchema.hashCode());
            this.hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PoolAndSchema)) {
                return false;
            }
            PoolAndSchema other = (PoolAndSchema) obj;
            if (poolId != other.poolId) {
                return false;
            }
            if (dbSchema == null) {
                if (other.dbSchema != null) {
                    return false;
                }
            } else if (!dbSchema.equals(other.dbSchema)) {
                return false;
            }
            return true;
        }
    }

    private static class CountAndUsage {

        final int count;
        final long usage;

        CountAndUsage(int count, long usage) {
            super();
            this.count = count;
            this.usage = usage;
        }
    }

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
            con = cache.getConnectionForConfigDB();
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
            } else {
                LOG.error("The specified cluster id '{}' has no database pool references", clusterId);
                throw new StorageException("The specified cluster id '" + clusterId + "' has no database pool references");
            }
        } catch (SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            closeSQLStuff(rs, stmt);
            try {
                cache.pushConnectionForConfigDB(con);
            } catch (final PoolException e) {
                LOG.error("Error pushing configdb connection to pool!", e);
            }
        }
    }
}
