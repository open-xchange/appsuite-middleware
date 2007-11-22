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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.database;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.pooling.PoolableLifecycle;
import com.openexchange.pooling.PooledData;
import com.openexchange.pooling.ReentrantLockPool;
import com.openexchange.server.impl.DBPoolingException;

/**
 * Extends the pool API especially for database connections.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ConnectionPool extends ReentrantLockPool<Connection> implements
    ConnectionPoolMBean {

    /**
     * The close method of the Connection interface.
     */
//    private static final Method CLOSE;

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(ConnectionPool.class);

    /**
     * Default time between checks if a connection still works.
     */
    public static final long DEFAULT_CHECK_TIME = 120000;

    /**
     * Reference to the lifecycle object.
     */
    private final ConnectionLifecycle lifecycle;

    /**
     * Full constructor with all parameters.
     * @param url JDBC url to the database.
     * @param info Properties for the connections.
     * @param config pool configuration parameters.
     */
    public ConnectionPool(final String url, final Properties info,
        final ReentrantLockPool.Config config) {
        super(new ConnectionLifecycle(url, info), config);
        lifecycle = (ConnectionLifecycle) super.getLifecycle();
    }

//    /**
//     * {@inheritDoc}
//     */
//    public Connection getNew() throws PoolingException {
//        final Connection con = super.get();
//        return (Connection) Proxy.newProxyInstance(
//            this.getClass().getClassLoader(),
//            new Class[] { Connection.class },
//            new InvocationHandler() {
//                public Object invoke(final Object proxy, final Method method,
//                    final Object[] args) throws Throwable {
//                    if (CLOSE.equals(method)) {
//                        back((Connection) proxy);
//                        return null;
//                    } else {
//                        return method.invoke(con, args);
//                    }
//                }
//            }
//        );
//    }

    /**
     * {@inheritDoc}
     */
    public int getNumBrokenConnections() {
        return getNumBroken();
    }

    /**
     * {@inheritDoc}
     */
    public int getNumberOfDBConnections() {
    	return Database.getNumConnections();
    }
    
    /**
     * Sets a new check time used for activating connections. If check time is
     * exhausted since last use of the connection a select statement is sent to
     * the database to check if the connection still works.
     * @param checkTime new check time.
     */
    public void setCheckTime(final long checkTime) {
        lifecycle.checkTime = checkTime;
    }

    /**
     * Life cycle for database connections.
     */
    private static class ConnectionLifecycle implements
        PoolableLifecycle<Connection> {
        /**
         * SQL command for checking the connection.
         */
        private static final String TEST_SELECT = "SELECT 1 AS test";
        /**
         * URL to the database for creating new connections.
         */
        private final transient String url;
        /**
         * Properties for new connections.
         */
        private final transient Properties info;

        /**
         * Time between checks if a connection still works.
         */
        private long checkTime = DEFAULT_CHECK_TIME;

        /**
         * Default constructor.
         * @param url URL to the database for creating new connections.
         * @param info Properties for new connections.
         */
        public ConnectionLifecycle(final String url, final Properties info) {
            this.url = url;
            this.info = info;
        }
        /**
         * {@inheritDoc}
         */
        public boolean activate(final PooledData<Connection> data) {
            final Connection con = data.getPooled();
            boolean retval;
            Statement stmt = null;
            ResultSet result = null;
            try {
                retval = !con.isClosed();
                if (data.getTimeDiff() > checkTime) {
                    stmt = con.createStatement();
                    result = stmt.executeQuery(TEST_SELECT);
                    if (result.next()) {
                        retval = result.getInt(1) == 1;
                    } else {
                        retval = false;
                    }
                }
            } catch (SQLException e) {
                retval = false;
            } finally {
                closeSQLStuff(result, stmt);
            }
            return retval;
        }
        /**
         * {@inheritDoc}
         */
        public Connection create() throws SQLException {
            return DriverManager.getConnection(url, info);
        }
        /**
         * {@inheritDoc}
         */
        public boolean deactivate(final PooledData<Connection> data) {
            boolean retval = true;
            try {
                retval = !data.getPooled().isClosed();
            } catch (SQLException e) {
                retval = false;
            }
            return retval;
        }
        /**
         * {@inheritDoc}
         */
        public void destroy(final Connection obj) {
            try {
                obj.close();
            } catch (SQLException e) {
                LOG.debug("Problem while closing connection.", e);
            }
        }
        private static void addTrace(final DBPoolingException dbe,
            final PooledData<Connection> data) {
            if (null != data.getTrace()) {
                dbe.setStackTrace(data.getTrace());
            }
        }
        /**
         * {@inheritDoc}
         */
        public boolean validate(final PooledData<Connection> data) {
            final Connection con = data.getPooled();
            boolean retval = true;
            try {
                if (con.isClosed()) {
                    LOG.error("Found closed connection.");
                    retval = false;
                } else if (!con.getAutoCommit()) {
                    final DBPoolingException dbe = new DBPoolingException(
                        DBPoolingException.Code.IN_TRANSACTION);
                    addTrace(dbe, data);
                    LOG.error(dbe.getMessage(), dbe);
                    con.rollback();
                    con.setAutoCommit(true);
                }
                final Class< ? extends Connection> connectionClass = con
                    .getClass();
                try {
                    final Method method = connectionClass.getMethod(
                        "getActiveStatementCount");
                    final int active = ((Integer) method.invoke(con,
                        new Object[0])).intValue();
                    if (active > 0) {
                        final DBPoolingException dbe = new DBPoolingException(
                            DBPoolingException.Code.ACTIVE_STATEMENTS, Integer
                            .valueOf(active));
                        addTrace(dbe, data);
                        LOG.error(dbe.getMessage(), dbe);
                        retval = false;
                    }
                } catch (RuntimeException e) {
                    LOG.error(e.getMessage(), e);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
                if (data.getTimeDiff() > 2000) {
                    final DBPoolingException dbe = new DBPoolingException(
                        DBPoolingException.Code.TOO_LONG, Long.valueOf(data
                        .getTimeDiff()));
                    addTrace(dbe, data);
                    if (LOG.isWarnEnabled()) {
                    	LOG.warn(dbe.getMessage(), dbe);
                    }
                }
            } catch (SQLException e) {
                retval = false;
            }
            return retval;
        }
    }

    static final ReentrantLockPool.Config DEFAULT_CONFIG;

    static {
        DEFAULT_CONFIG = new ReentrantLockPool.Config();
        DEFAULT_CONFIG.minIdle = 0;
        DEFAULT_CONFIG.maxIdle = -1;
        DEFAULT_CONFIG.maxIdleTime = 60000;
        DEFAULT_CONFIG.maxActive = -1;
        DEFAULT_CONFIG.maxWait = 10000;
        DEFAULT_CONFIG.maxLifeTime = -1;
        DEFAULT_CONFIG.exhaustedAction = ReentrantLockPool.ExhaustedActions
            .BLOCK;
        DEFAULT_CONFIG.testOnActivate = false;
        DEFAULT_CONFIG.testOnDeactivate = true;
        DEFAULT_CONFIG.testOnIdle = false;
        DEFAULT_CONFIG.testThreads = false;
//        try {
//            CLOSE = Connection.class.getMethod("close", new Class[] {});
//        } catch (SecurityException e) {
//            throw new RuntimeException(e);
//        } catch (NoSuchMethodException e) {
//            throw new RuntimeException(e);
//        }
    }
}
