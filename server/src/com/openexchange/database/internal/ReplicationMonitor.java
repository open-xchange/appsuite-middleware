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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.database.internal;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.database.DBPoolingException;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.internal.wrapping.ConnectionReturnerFactory;
import com.openexchange.pooling.PoolingException;

/**
 * {@link ReplicationMonitor}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class ReplicationMonitor {

    static final Log LOG = LogFactory.getLog(ReplicationMonitor.class);

    private static long masterConnectionsFetched;

    private static long slaveConnectionsFetched;

    private static long masterInsteadOfSlaveFetched;

    private ReplicationMonitor() {
        super();
    }

    interface FetchAndSchema {
        Connection get(Pools pools, Assignment assign, boolean write, boolean usedAsRead) throws PoolingException, DBPoolingException;
    }

    static final FetchAndSchema TIMEOUT = new FetchAndSchema() {
        public Connection get(Pools pools, Assignment assign, boolean write, boolean usedAsRead) throws PoolingException, DBPoolingException {
            final int poolId;
            if (write) {
                poolId = assign.getWritePoolId();
            } else {
                poolId = assign.getReadPoolId();
            }
            ConnectionPool pool = pools.getPool(poolId);
            Connection retval = pool.get();
            try {
                String schema = assign.getSchema();
                if (null != schema && !retval.getCatalog().equals(schema)) {
                    retval.setCatalog(schema);
                }
            } catch (SQLException e) {
                try {
                    pool.back(retval);
                } catch (PoolingException e1) {
                    LOG.error(e1.getMessage(), e1);
                }
                throw DBPoolingExceptionCodes.SCHEMA_FAILED.create(e);
            }
            return ConnectionReturnerFactory.createConnection(pools, assign, retval, false, write, usedAsRead);
        }
    };

    static final FetchAndSchema NOTIMEOUT = new FetchAndSchema() {
        public Connection get(Pools pools, Assignment assign, boolean write, boolean usedAsRead) throws PoolingException, DBPoolingException {
            final int poolId;
            if (write) {
                poolId = assign.getWritePoolId();
            } else {
                poolId = assign.getReadPoolId();
            }
            ConnectionPool pool = pools.getPool(poolId);
            final Connection retval = pool.getWithoutTimeout();
            try {
                String schema = assign.getSchema();
                if (null != schema && !retval.getCatalog().equals(schema)) {
                    retval.setCatalog(schema);
                }
            } catch (SQLException e) {
                pool.backWithoutTimeout(retval);
                throw DBPoolingExceptionCodes.SCHEMA_FAILED.create(e);
            }
            return ConnectionReturnerFactory.createConnection(pools, assign, retval, true, write, usedAsRead);
        }
    };

    static Connection checkActualAndFallback(Pools pools, Assignment assign, boolean noTimeout, boolean write) throws DBPoolingException {
        return checkActualAndFallback(pools, assign, noTimeout ? NOTIMEOUT : TIMEOUT, write);
    }

    static Connection checkActualAndFallback(Pools pools, Assignment assign, FetchAndSchema fetch, boolean write) throws DBPoolingException {
        Connection retval;
        try {
            retval = fetch.get(pools, assign, write, false);
            incrementFetched(assign, write);
        } catch (PoolingException e) {
            DBPoolingException e1 = createException(assign, write, e);
            // Immediately fail if connection to master is wanted or no fallback is there.
            if (write || assign.getWritePoolId() == assign.getReadPoolId()) {
                throw e1;
            }
            // Try fallback to master.
            LOG.warn(e1.getMessage(), e1);
            try {
                retval = fetch.get(pools, assign, true, true);
                incrementInstead();
            } catch (PoolingException e2) {
                throw createException(assign, true, e2);
            }
        }
        if (!write && assign.isTransactionInitialized() && !isUpToDate(assign.getTransaction(), readTransaction(retval, assign.getContextId()))) {
            LOG.debug("Slave " + assign.getReadPoolId() + " is not actual. Using master " + assign.getWritePoolId() + " instead.");
            Connection toReturn = retval;
            try {
                retval = fetch.get(pools, assign, true, true);
                incrementInstead();
                try {
                    toReturn.close();
                } catch (SQLException e) {
                    DBPoolingException e1 = DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
                    LOG.error(e1.getMessage(), e1);
                }
            } catch (PoolingException e) {
                // Use not actual slave if master connection cannot be obtained.
                DBPoolingException e1 = createException(assign, true, e);
                LOG.warn(e1.getMessage(), e1);
            }
        }
        return retval;
    }

    private static DBPoolingException createException(Assignment assign, boolean write, Throwable cause) {
        int poolId = write ? assign.getWritePoolId() : assign.getReadPoolId();
        return assign.getReadPoolId() == Constants.CONFIGDB_READ_ID ? DBPoolingExceptionCodes.NO_CONFIG_DB.create(cause) : DBPoolingExceptionCodes.NO_CONNECTION.create(cause, I(poolId));
    }

    private static boolean isUpToDate(long masterTransaction, long slaveTransaction) {
        // TODO handle overflow
        return slaveTransaction >= masterTransaction;
    }

    public static void backAndIncrementTransaction(Pools pools, Assignment assign, Connection con, boolean noTimeout, boolean write, boolean usedAsRead) {
        final int poolId;
        if (write) {
            poolId = assign.getWritePoolId();
            if (poolId != assign.getReadPoolId() && !usedAsRead) {
                ReplicationMonitor.increaseTransactionCounter(assign, con);
            }
        } else {
            poolId = assign.getReadPoolId();
        }
        final ConnectionPool pool;
        try {
            pool = pools.getPool(poolId);
        } catch (DBPoolingException e) {
            LOG.error(e.getMessage(), e);
            return;
        }
        if (noTimeout) {
            pool.backWithoutTimeout(con);
        } else {
            try {
                pool.back(con);
            } catch (PoolingException e) {
                DBPoolingException e1 = DBPoolingExceptionCodes.RETURN_FAILED.create(e, I(poolId));
                LOG.error(e1.getMessage(), e1);
            }
        }
    }

    private static long readTransaction(Connection con, int ctxId) throws DBPoolingException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        final long retval;
        try {
            stmt = con.prepareStatement("SELECT transaction FROM replicationMonitor WHERE cid=?");
            stmt.setInt(1, ctxId);
            result = stmt.executeQuery();
            if (result.next()) {
                retval = result.getLong(1);
            } else {
                throw DBPoolingExceptionCodes.TRANSACTION_MISSING.create(I(ctxId));
            }
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        return retval;
    }

    private static long lastLogged = 0;

    static void increaseTransactionCounter(Assignment assign, Connection con) {
        try {
            if (con.isClosed()) {
                return;
            }
        } catch (SQLException e) {
            DBPoolingException e1 = DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            LOG.error(e1.getMessage(), e1);
            return;
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            con.setAutoCommit(false);
            stmt = con.prepareStatement("UPDATE replicationMonitor SET transaction=transaction+1 WHERE cid=?");
            stmt.setInt(1, assign.getContextId());
            stmt.execute();
            stmt.close();
            stmt = con.prepareStatement("SELECT transaction FROM replicationMonitor WHERE cid=?");
            stmt.setInt(1, assign.getContextId());
            result = stmt.executeQuery();
            if (result.next()) {
                assign.setTransaction(result.getLong(1));
            } else {
                LOG.error("Updating transaction for replication monitor failed for context " + assign.getContextId() + ".");
            }
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            if (1146 == e.getErrorCode()) {
                if (lastLogged + 300000 < System.currentTimeMillis()) {
                    lastLogged = System.currentTimeMillis();
                    DBPoolingException e1 = DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
                    LOG.error(e1.getMessage(), e1);
                }
            } else {
                DBPoolingException e1 = DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
                LOG.error(e1.getMessage(), e1);
            }
        } finally {
            autocommit(con);
            closeSQLStuff(result, stmt);
        }
    }

    public static void incrementFetched(Assignment assign, boolean write) {
        if (assign.getWritePoolId() == assign.getReadPoolId() || write) {
            masterConnectionsFetched++;
        } else {
            slaveConnectionsFetched++;
        }
    }

    private static void incrementInstead() {
        masterInsteadOfSlaveFetched++;
    }

    public static long getMasterConnectionsFetched() {
        return masterConnectionsFetched;
    }

    public static long getSlaveConnectionsFetched() {
        return slaveConnectionsFetched;
    }

    public static long getMasterInsteadOfSlave() {
        return masterInsteadOfSlaveFetched;
    }
}
