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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import org.apache.commons.collections.keyvalue.MultiKey;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.storage.sqlStorage.OXUtilSQLStorage;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.arrays.Collections;
import com.openexchange.tools.sql.DBUtils;

/**
 * @author d7
 * @author cutmasta
 */
public class OXUtilMySQLStorage extends OXUtilSQLStorage {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXUtilMySQLStorage.class);

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
                final Long l_max = new Long("8796093022208");
                if (store_size > l_max.longValue()) {
                    throw new StorageException("Filestore size to large for database (max=" + l_max.longValue() + ")");
                }
                double store_size_double = store_size;
                store_size_double *= Math.pow(2, 20);
                prep = configdb_write_con.prepareStatement("UPDATE filestore SET size = ? WHERE id = ?");
                prep.setLong(1, Math.round(store_size_double));
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
        final Long l_max = new Long("8796093022208");
        if (store_size > l_max.longValue()) {
            throw new StorageException("Filestore size to large for database (max=" + l_max.longValue() + ")");
        }
        store_size *= Math.pow(2, 20);
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
    public Filestore findFilestoreForContext() throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getConnectionForConfigDB();
            stmt = con.prepareStatement("SELECT filestore.id, filestore.max_context, COUNT(context.cid) AS num FROM filestore LEFT JOIN context ON filestore.id=context.filestore_id GROUP BY filestore.id ORDER BY num ASC");
            rs = stmt.executeQuery();

            if (!rs.next()) {
                // None found
                throw new StorageException("No filestore found");
            }

            // Load potential candidates
            class Candidate {
                final int id;
                final int maxNumberOfContexts;
                final int numberOfContexts;

                Candidate(final int id, final int maxNumberOfContexts, final int numberOfContexts) {
                    super();
                    this.id = id;
                    this.maxNumberOfContexts = maxNumberOfContexts;
                    this.numberOfContexts = numberOfContexts;
                }
            }

            final List<Candidate> candidates = new LinkedList<Candidate>();
            do {
                candidates.add(new Candidate(rs.getInt(1), rs.getInt(2), rs.getInt(3)));
            } while (rs.next());

            // Close resources as no more needed
            DBUtils.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            // Find a suitable one from ordered list of candidates
            for (final Candidate candidate : candidates) {
                if (candidate.maxNumberOfContexts > 0 && candidate.numberOfContexts < candidate.maxNumberOfContexts) {
                    // Get filestore
                    final Filestore filestore = getFilestore(candidate.id, false, con);
                    if (enoughSpaceForContext(filestore)) {
                        return filestore;
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
    public boolean hasSpaceForAnotherContext(final Filestore filestore) throws StorageException {
        return !isContextLimitReached(filestore) && enoughSpaceForContext(filestore);
    }

    private boolean isContextLimitReached(final Filestore filestore) {
        // 0 is the special value for not adding contexts to this filestore.
        // and check if the current contexts stored in the filestore reach the maximum context value for the filestore.
        return 0 == filestore.getMaxContexts().intValue() || filestore.getCurrentContexts().intValue() >= filestore.getMaxContexts().intValue();
    }

    private boolean enoughSpaceForContext(final Filestore filestore) throws StorageException {
        final long averageSize = getAverageFilestoreSpace();
        return filestore.getReserved().longValue() + averageSize <= filestore.getSize().longValue();
    }

    /**
     * @return the configured average file store space per context in mega bytes (MB).
     * @throws StorageException if parsing the configuration option fails.
     */
    private long getAverageFilestoreSpace() throws StorageException {
        final String value = prop.getProp("AVERAGE_CONTEXT_SIZE", "100");
        long average_size;
        try {
            average_size = Long.parseLong(value);
        } catch (final NumberFormatException e) {
            LOG.error("Unable to parse average context filestore space from configuration file.", e);
            throw new StorageException("Unable to parse average context filestore space from configuration file.", e);
        }
        return average_size;
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

    private static final long toMB(final long value) {
        return value / 0x100000;
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
    public Filestore getFilestore(final int id, final boolean loadRealUsage) throws StorageException {
        Connection con = null;
        try {
            con = cache.getConnectionForConfigDB();
            return getFilestore(id, loadRealUsage, con);
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            if (null != con) {
                try {
                    cache.pushConnectionForConfigDB(con);
                } catch (final PoolException e) {
                    LOG.error("Error pushing configdb connection to pool!", e);
                }
            }
        }
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
    private Filestore getFilestore(final int id, final boolean loadRealUsage, final Connection con) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        final Filestore fs;
        try {
            stmt = con.prepareStatement("SELECT uri,size,max_context FROM filestore WHERE id=?");
            stmt.setInt(1, id);
            result = stmt.executeQuery();
            if (!result.next()) {
                throw new StorageException("unable to get filestore data");
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
        final FilestoreUsage usage = getUsage(id, loadRealUsage);
        fs.setUsed(L(toMB(usage.getUsage())));
        fs.setCurrentContexts(I(usage.getCtxCount()));
        fs.setReserved(L(getAverageFilestoreSpace() * usage.getCtxCount()));
        return fs;
    }

    private void updateFilestoresWithRealUsage(final List<Filestore> stores) throws StorageException {
        final Collection<FilestoreContextBlock> blocks = makeBlocksFromFilestoreContexts();
        // Sort by database.
        Map<Integer, Collection<FilestoreContextBlock>> dbMap = new HashMap<Integer, Collection<FilestoreContextBlock>>();
        for (FilestoreContextBlock block : blocks) {
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
        final Connection con;
        try {
            con = cache.getConnectionForConfigDB();
        } catch (final PoolException e) {
            throw new StorageException(e);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT d.cid,d.write_db_pool_id,d.db_schema,c.filestore_id FROM context_server2db_pool d JOIN context c ON d.cid=c.cid WHERE d.server_id=?");
            stmt.setInt(1, cache.getServerId());
            result = stmt.executeQuery();
            final Map<MultiKey, FilestoreContextBlock> blocks = new HashMap<MultiKey, FilestoreContextBlock>();
            while (result.next()) {
                final FilestoreInfo temp = new FilestoreInfo(result.getInt(1), result.getInt(2), result.getString(3), result.getInt(4));
                final MultiKey key = new MultiKey(I(temp.writeDBPoolID), temp.dbSchema, I(temp.filestoreID));
                FilestoreContextBlock block = blocks.get(key);
                if (null == block) {
                    block = new FilestoreContextBlock(temp.writeDBPoolID, temp.filestoreID);
                    blocks.put(key, block);
                }
                block.add(temp);
            }
            return blocks.values();
        } catch (final PoolException e) {
            throw new StorageException(e);
        } catch (final SQLException e) {
            throw new StorageException(e);
        } finally {
            closeSQLStuff(result, stmt);
            try {
                cache.pushConnectionForConfigDB(con);
            } catch (final PoolException e) {
                throw new StorageException(e);
            }
        }
    }

    protected static void updateBlocksInSameDBWithFilestoreUsage(Collection<FilestoreContextBlock> blocks) throws StorageException {
        for (FilestoreContextBlock block : blocks) {
            updateBlockWithFilestoreUsage(block);
        }
    }

    private static void updateBlockWithFilestoreUsage(final FilestoreContextBlock block) throws StorageException {
        if (block.size() == 0) {
            return;
        }
        Connection con = null;
        int representativeContextID = Integer.MIN_VALUE;
        for (Integer cid : block.filestores.keySet()) {
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
            // If none is available anymore, we can not add any usage data and we should just return.
            return;
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT cid,used FROM filestore_usage");
            result = stmt.executeQuery();
            while (result.next()) {
                final int cid = result.getInt(1);
                final long usage = result.getLong(2);
                block.update(cid, usage);
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

    private void updateFilestoresWithUsageFromBlocks(final List<Filestore> stores, final Collection<FilestoreContextBlock> blocks) throws StorageException {
        final Map<Integer, Long> filestore2usage = new HashMap<Integer, Long>();
        final Map<Integer, Integer> filestore2ctxUsage = new HashMap<Integer, Integer>();
        for (final FilestoreContextBlock block : blocks) {
            final Collection<FilestoreInfo> infos = block.filestores.values();
            for (final FilestoreInfo info : infos) {
                final Integer fid = I(info.filestoreID);
                final long usage = filestore2usage.containsKey(fid) ? l(filestore2usage.get(fid)) : 0;
                filestore2usage.put(fid, L(usage + info.usage));
                filestore2ctxUsage.put(fid, I(filestore2ctxUsage.containsKey(fid) ? i(filestore2ctxUsage.get(fid)) + 1 : 1));
            }
        }

        for (final Filestore store : stores) {
            Long usedBytes = filestore2usage.get(store.getId());
            if (null == usedBytes) {
                usedBytes = L(0);
            }
            store.setUsed(usedBytes);
            Integer numContexts = filestore2ctxUsage.get(store.getId());
            if (null == numContexts) {
                numContexts = I(0);
            }
            store.setCurrentContexts(numContexts);
            store.setReserved(L(getAverageFilestoreSpace() * i(numContexts)));
        }
    }

    /* ----------------------- */

    private final long getContextUsedQuota(final int cid) throws StorageException {
        final Connection con;
        try {
            con = cache.getConnectionForContext(cid);
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        long quotaUsed = 0;
        try {
            stmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE cid=?");
            stmt.setInt(1, cid);
            result = stmt.executeQuery();
            // One line per context in that table. cid is PRIMARY KEY.
            if (result.next()) {
                quotaUsed = result.getLong(1);
            }
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            closeSQLStuff(result, stmt);
            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(cid, con);
                } catch (final PoolException e) {
                    LOG.error("Error pushing ox connection to pool!", e);
                }
            }
        }
        return quotaUsed;
    }

    /**
     * @param filestoreId
     * @return
     * @throws StorageException
     */
    private int[] getContextsInFilestore(final int filestoreId) throws StorageException {
        final Connection con;
        try {
            con = cache.getConnectionForConfigDB();
        } catch (final PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        final List<Integer> retval = new ArrayList<Integer>();
        try {
            stmt = con.prepareStatement("SELECT cid FROM context WHERE filestore_id=?");
            stmt.setInt(1, filestoreId);
            result = stmt.executeQuery();
            while (result.next()) {
                retval.add(I(result.getInt(1)));
            }
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            closeSQLStuff(result, stmt);
            try {
                cache.pushConnectionForConfigDB(con);
            } catch (final PoolException e) {
                LOG.error("Error pushing configdb connection to pool!", e);
            }
        }
        return Collections.toArray(retval);
    }

    /**
     * Loads the usage information for a filestore.
     *
     * @param filestoreId the unique identifier of the filestore.
     * @param loadRealUsage <code>true</code> to load the filestore usage from every context in it. BEWARE! This is a slow operation.
     * @return The {@link FilestoreUsage} object for the filestore.
     * @throws StorageException if some problem occurs loading the information.
     */
    private final FilestoreUsage getUsage(final int filestoreId, final boolean loadRealUsage) throws StorageException {
        final FilestoreUsage usage = new FilestoreUsage();
        for (final int cid : getContextsInFilestore(filestoreId)) {
            usage.addContextUsage(loadRealUsage ? getContextUsedQuota(cid) : 0);
        }
        return usage;
    }
}
