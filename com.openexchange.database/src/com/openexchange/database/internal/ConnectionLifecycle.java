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

package com.openexchange.database.internal;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.Databases;
import com.openexchange.database.internal.reloadable.GenericReloadable;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.pooling.PoolableLifecycle;
import com.openexchange.pooling.PooledData;

/**
 * Life cycle for database connections.
 */
class ConnectionLifecycle implements PoolableLifecycle<Connection> {

    /**
     * SQL command for checking the connection.
     */
    private static final String TEST_SELECT = "SELECT 1 AS test";

    private static final AtomicReference<Field> openStatementsField = new AtomicReference<Field>(null);

    private static Field getOpenStatementsField() {
        Field openStatementsField = ConnectionLifecycle.openStatementsField.get();
        if (null == openStatementsField) {
            synchronized (ConnectionLifecycle.class) {
                openStatementsField = ConnectionLifecycle.openStatementsField.get();
                if (null == openStatementsField) {
                    try {
                        openStatementsField = com.mysql.jdbc.ConnectionImpl.class.getDeclaredField("openStatements");
                        openStatementsField.setAccessible(true);
                        ConnectionLifecycle.openStatementsField.set(openStatementsField);
                    } catch (NoSuchFieldException e) {
                        // Unable to retrieve openStatements content.
                        return null;
                    } catch (SecurityException e) {
                        // Unable to retrieve openStatements content.
                        return null;
                    }
                }
            }
        }
        return openStatementsField;
    }

    static final class FastThrowable extends Throwable {

        FastThrowable() {
            super("tracked closed connection");
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    private static class UrlAndConnectionArgs {

        final String url;
        final Properties connectionArguments;

        UrlAndConnectionArgs(String url, Properties connectionArguments) {
            super();
            this.url = url;
            this.connectionArguments = connectionArguments;
        }
    }

    // ----------------------------------------------------------------------------------------------

    /**
     * Time between checks if a connection still works.
     */
    private static final long DEFAULT_CHECK_TIME = 120000;

    private final AtomicReference<UrlAndConnectionArgs> urlAndConnectionReference;

    /**
     * Initializes a new {@link ConnectionLifecycle}.
     *
     * @param url A database URL of the form <code> jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * @param connectionArguments A list of arbitrary string tag/value pairs as connection arguments; normally at least a "user" and "password" property should be included
     */
    public ConnectionLifecycle(final String url, final Properties connectionArguments) {
        super();
        urlAndConnectionReference = new AtomicReference<>(new UrlAndConnectionArgs(url, connectionArguments));
    }

    /**
     * Sets the JDBC URL and connection arguments to use.
     *
     * @param url A database URL of the form <code> jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * @param connectionArguments A list of arbitrary string tag/value pairs as connection arguments; normally at least a "user" and "password" property should be included
     */
    public void setUrlAndConnectionArgs(String url, Properties connectionArguments) {
        urlAndConnectionReference.set(new UrlAndConnectionArgs(url, connectionArguments));
    }

    @Override
    public boolean activate(final PooledData<Connection> data) {
        final Connection con = data.getPooled();
        boolean retval;
        Statement stmt = null;
        ResultSet result = null;
        try {
            retval = MysqlUtils.ClosedState.OPEN == MysqlUtils.isClosed(con, true);
            if (retval && data.getLastPacketDiffFallbackToTimeDiff() > DEFAULT_CHECK_TIME) {
                stmt = con.createStatement();
                result = stmt.executeQuery(TEST_SELECT);
                if (result.next()) {
                    retval = result.getInt(1) == 1;
                } else {
                    retval = false;
                }
            }
        } catch (final SQLException e) {
            retval = false;
        } finally {
            Databases.closeSQLStuff(result, stmt);
        }
        return retval;
    }

    @Override
    public Connection create() throws SQLException {
        UrlAndConnectionArgs urlAndConnectionArgs = urlAndConnectionReference.get();
        return DriverManager.getConnection(urlAndConnectionArgs.url, urlAndConnectionArgs.connectionArguments);
    }

    public Connection createWithoutTimeout() throws SQLException {
        UrlAndConnectionArgs urlAndConnectionArgs = urlAndConnectionReference.get();
        Properties withoutTimeout = new Properties();
        withoutTimeout.putAll(urlAndConnectionArgs.connectionArguments);
        for (Iterator<Object> iter = withoutTimeout.keySet().iterator(); iter.hasNext();) {
            final Object test = iter.next();
            if (String.class.isAssignableFrom(test.getClass()) && Strings.asciiLowerCase(((String) test)).endsWith("timeout")) {
                iter.remove();
            }
        }
        return DriverManager.getConnection(urlAndConnectionArgs.url, withoutTimeout);
    }

    @Override
    public boolean deactivate(final PooledData<Connection> data) {
        boolean retval = true;
        try {
            retval = MysqlUtils.ClosedState.OPEN == MysqlUtils.isClosed(data.getPooled(), true);
        } catch (final SQLException e) {
            retval = false;
        }
        return retval;
    }

    @Override
    public void destroy(final Connection obj) {
        Databases.close(obj);
    }

    private static void addTrace(final OXException dbe, final PooledData<Connection> data) {
        if (null != data.getTrace()) {
            dbe.setStackTrace(data.getTrace());
        }
    }

    private static volatile Integer usageThreshold;

    private static int usageThreshold() {
        Integer tmp = usageThreshold;
        if (null == tmp) {
            synchronized (ConnectionLifecycle.class) {
                tmp = usageThreshold;
                if (null == tmp) {
                    final int defaultValue = 2000;
                    final ConfigurationService confService = Initialization.getConfigurationService();
                    if (null == confService) {
                        return defaultValue;
                    }

                    tmp = Integer.valueOf(confService.getIntProperty("com.openexchange.database.usageThreshold", defaultValue));
                    usageThreshold = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    static {
        GenericReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                usageThreshold = null;
            }

            @Override
            public Interests getInterests() {
                return DefaultInterests.builder().propertiesOfInterest("com.openexchange.database.usageThreshold").build();
            }
        });
    }

    @Override
    public boolean validate(final PooledData<Connection> data) {
        final Connection con = data.getPooled();
        boolean retval = true;
        try {
            MysqlUtils.ClosedState closedState = MysqlUtils.isClosed(con, true);
            if (MysqlUtils.ClosedState.OPEN != closedState) {
                if (MysqlUtils.ClosedState.EXPLICITLY_CLOSED == closedState) {
                    ConnectionPool.LOG.error("Found closed connection.", new FastThrowable());
                } else {
                    ConnectionPool.LOG.error("Found internally closed connection.", new FastThrowable());
                }
                retval = false;
            } else if (!con.getAutoCommit()) {
                final OXException dbe = DBPoolingExceptionCodes.NO_AUTOCOMMIT.create();
                addTrace(dbe, data);
                ConnectionPool.LOG.error("", dbe);
                con.rollback();
                con.setAutoCommit(true);
            }
            // Getting number of open statements.
            try {
                int active = 0;
                if (con instanceof com.mysql.jdbc.Connection) {
                    active = ((com.mysql.jdbc.Connection) con).getActiveStatementCount();
                }
                if (active > 0) {
                    final OXException dbe = DBPoolingExceptionCodes.ACTIVE_STATEMENTS.create(I(active));
                    addTrace(dbe, data);
                    String openStatement = "";
                    if (con instanceof com.mysql.jdbc.ConnectionImpl) {
                        Field openStatementsField = getOpenStatementsField();
                        if (null == openStatementsField) {
                            // Unable to retrieve openStatements content. Just log that there is an open statement...
                        } else {
                            CopyOnWriteArrayList<Statement> open = (CopyOnWriteArrayList<Statement>) openStatementsField.get(con);
                            for (Statement statement : open) {
                                openStatement = statement.toString();
                            }
                        }
                    }
                    ConnectionPool.LOG.error(openStatement, dbe);
                    retval = false;
                }
            } catch (final Exception e) {
                ConnectionPool.LOG.error("", e);
            }
            // Write warning if using this connection was longer than 2 seconds.
            if (data.getTimeDiff() > usageThreshold()) {
                final OXException dbe = DBPoolingExceptionCodes.TOO_LONG.create(L(data.getTimeDiff()));
                addTrace(dbe, data);
                ConnectionPool.LOG.warn("", dbe);
            }
        } catch (final SQLException e) {
            retval = false;
        }
        return retval;
    }

    @Override
    public String getObjectName() {
        return "Database connection";
    }
}
