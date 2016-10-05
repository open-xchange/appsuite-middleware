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

package com.openexchange.cluster.lock.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.cluster.lock.ClusterTask;
import com.openexchange.cluster.lock.policies.RetryPolicy;
import com.openexchange.cluster.lock.policies.RunOnceRetryPolicy;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link ClusterLockServiceDatabaseImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ClusterLockServiceDatabaseImpl extends AbstractClusterLockServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterLockServiceDatabaseImpl.class);

    // On duplicate key simply retain the same values
    private static final String ACQUIRE_LOCK = "INSERT INTO clusterLock (cid, user, name, timestamp) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE `cid` = `cid` AND `user` = `user` AND `name` = `name` AND `timestamp` = `timestamp`";
    private static final String GET_TIMESTAMP = "SELECT timestamp FROM clusterLock WHERE cid=? AND user=? AND name=?";
    private static final String UPDATE_X_TIMESTAMP = "UPDATE clusterLock SET timestamp=? WHERE cid=? AND user=? AND name=? AND timestamp=?";
    private static final String DELETE_TIMESTAMP = "DELETE FROM clusterLock WHERE cid=? AND user=? AND name=?";

    /**
     * Initialises a new {@link ClusterLockServiceDatabaseImpl}.
     * 
     * @param services The {@link ServiceLookup} instance
     */
    public ClusterLockServiceDatabaseImpl(ServiceLookup services) {
        super(services);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cluster.lock.ClusterLockService#acquireClusterLock(com.openexchange.cluster.lock.ClusterTask)
     */
    @Override
    public <T> boolean acquireClusterLock(ClusterTask<T> clusterTask) throws OXException {
        int contextId = clusterTask.getContextId();

        DatabaseService databaseService = services.getService(DatabaseService.class);

        Connection connection = databaseService.getWritable(contextId);
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            long timeNow = System.nanoTime();
            if (acquireClusterLock(clusterTask, timeNow, connection)) {
                return true;
            }

            long timeThen = getTimestamp(clusterTask, connection);
            if (!leaseExpired(timeNow, timeThen)) {
                return false;
            }

            return updateTimestamp(clusterTask, timeNow, timeThen, connection);
        } catch (SQLException e) {
            throw ClusterLockExceptionCodes.SQL_ERROR.create(e.getMessage(), e);
        } finally {
            DBUtils.closeResources(resultSet, statement, connection, false, contextId);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cluster.lock.ClusterLockService#releaseClusterLock(com.openexchange.cluster.lock.ClusterTask)
     */
    @Override
    public <T> void releaseClusterLock(ClusterTask<T> clusterTask) throws OXException {
        deleteTimestamp(clusterTask);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cluster.lock.ClusterLockService#runClusterTask(com.openexchange.cluster.lock.ClusterTask)
     */
    @Override
    public <T> T runClusterTask(ClusterTask<T> clusterTask) throws OXException {
        return runClusterTask(clusterTask, new RunOnceRetryPolicy());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cluster.lock.ClusterLockService#runClusterTask(com.openexchange.cluster.lock.ClusterTask, com.openexchange.cluster.lock.policies.RetryPolicy)
     */
    @Override
    public <T> T runClusterTask(ClusterTask<T> clusterTask, RetryPolicy retryPolicy) throws OXException {
        return runClusterTask(clusterTask, retryPolicy, new RefreshLockTask<T>(clusterTask));
    }

    //////////////////////////////////// HELPERS ///////////////////////////////////

    /**
     * {@link RefreshLockTask} - Refreshes the lock expiration timestamp for the specified task
     */
    private class RefreshLockTask<T> implements Runnable {

        private static final String UPDATE_TIMESTAMP = "UPDATE clusterLock SET timestamp=? WHERE cid=? AND user=? AND name=?";
        private final ClusterTask<T> clusterTask;

        /**
         * Initialises a new {@link RefreshLockTask}.
         * 
         * @param clusterTask The {@link ClusterTask} to refresh
         */
        public RefreshLockTask(ClusterTask<T> clusterTask) {
            super();
            this.clusterTask = clusterTask;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            int contextId = clusterTask.getContextId();
            int userId = clusterTask.getUserId();

            DatabaseService databaseService = services.getService(DatabaseService.class);

            Connection connection = null;
            PreparedStatement statement = null;

            try {
                connection = databaseService.getWritable(contextId);
                statement = connection.prepareStatement(UPDATE_TIMESTAMP);

                int pos = 1;
                statement.setString(pos++, Long.toString(System.nanoTime()));
                statement.setInt(pos++, contextId);
                statement.setInt(pos++, userId);
                statement.setString(pos++, clusterTask.getTaskName());

                statement.executeUpdate();
            } catch (SQLException e) {
                LOGGER.error("The lock for cluster task '{}' was not refreshed. {}", clusterTask.getTaskName(), e.getMessage(), e);
            } catch (OXException e) {
                LOGGER.error("Cannot get a writable connection for cluster task '{}'. The cluster task lock was not refreshed. {}", clusterTask.getTaskName(), e.getMessage(), e);
            } finally {
                DBUtils.closeResources(null, statement, connection, false, contextId);
            }
        }
    }

    /**
     * Inserts the specified timestamp in the database using the specified writable {@link Connection}
     * 
     * @param clusterTask The {@link ClusterTask}
     * @param now The timestamp to insert
     * @param connection The writable {@link Connection}
     * @return <code>true</code> if the timestamp was successfully inserted (indicating that the lock was acquired);
     *         <code>false</code> otherwise
     * @throws SQLException if an SQL error is occurred
     */
    private <T> boolean acquireClusterLock(ClusterTask<T> clusterTask, long now, Connection connection) throws SQLException {
        int userId = clusterTask.getUserId();
        int contextId = clusterTask.getContextId();

        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(ACQUIRE_LOCK);

            int index = 1;
            statement.setInt(index++, contextId);
            statement.setInt(index++, userId);
            statement.setString(index++, clusterTask.getTaskName());
            statement.setLong(index++, now);
            statement.setInt(index++, contextId);
            statement.setInt(index++, userId);
            statement.setString(index++, clusterTask.getTaskName());

            return statement.executeUpdate() > 0;
        } finally {
            DBUtils.closeSQLStuff(statement);
        }
    }

    /**
     * Returns the already existing timestamp from the database
     * 
     * @param clusterTask The {@link ClusterTask}
     * @param connection The {@link Connection}
     * @return The timestamp
     * @throws SQLException if an SQL exception is occurred
     */
    private <T> long getTimestamp(ClusterTask<T> clusterTask, Connection connection) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(GET_TIMESTAMP);

            int index = 1;
            statement.setInt(index++, clusterTask.getContextId());
            statement.setInt(index++, clusterTask.getUserId());
            statement.setString(index++, clusterTask.getTaskName());
            resultSet = statement.executeQuery();

            return Long.parseLong(resultSet.getString(index));
        } finally {
            DBUtils.closeSQLStuff(statement);
        }
    }

    /**
     * Updates the timestamp for the specified {@link ClusterTask}
     * 
     * @param clusterTask The {@link ClusterTask}
     * @param timeNow The time now
     * @param timeThen The time then
     * @param connection The writable {@link Connection}
     * @return <code>true</code> if the timestamp was successfully updated; <code>false</code> otherwise
     * @throws SQLException if an SQL exception is occurred
     */
    private <T> boolean updateTimestamp(ClusterTask<T> clusterTask, long timeNow, long timeThen, Connection connection) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(UPDATE_X_TIMESTAMP);

            int index = 1;
            statement.setLong(index++, timeNow);
            statement.setInt(index++, clusterTask.getContextId());
            statement.setInt(index++, clusterTask.getUserId());
            statement.setString(index++, clusterTask.getTaskName());
            statement.setLong(index++, timeThen);

            return statement.executeUpdate() > 0;
        } finally {
            DBUtils.closeSQLStuff(statement);
        }
    }

    /**
     * Deletes the timestamp for the specified {@link ClusterTask}
     * 
     * @param clusterTask The {@link ClusterTask}
     * @throws OXException if an error is occurred
     */
    private <T> void deleteTimestamp(ClusterTask<T> clusterTask) throws OXException {
        int contextId = clusterTask.getContextId();

        DatabaseService databaseService = services.getService(DatabaseService.class);

        Connection connection = databaseService.getWritable(contextId);
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(DELETE_TIMESTAMP);

            int index = 1;
            statement.setInt(index++, contextId);
            statement.setInt(index++, clusterTask.getUserId());
            statement.setString(index++, clusterTask.getTaskName());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw ClusterLockExceptionCodes.SQL_ERROR.create(e.getMessage(), e);
        } finally {
            DBUtils.closeSQLStuff(statement);
        }
    }
}
