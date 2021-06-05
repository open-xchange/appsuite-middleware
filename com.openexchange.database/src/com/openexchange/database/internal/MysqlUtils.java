/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.database.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import com.mysql.jdbc.ConnectionImpl;

/**
 * {@link MysqlUtils}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class MysqlUtils {

    /**
     * Initializes a new {@link MysqlUtils}.
     */
    private MysqlUtils() {
        super();
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    /** Simple holder to delay initialization when needed */
    private static class LastPacketTimeMethodHolder {

        static final Object[] EMPTY_ARGS = new Object[0];

        static final Method getLastPacketReceivedTimeMsMethod;
        static final Method getLastPacketSentTimeMsMethod;
        static {
            Method m;
            try {
                m = com.mysql.jdbc.MysqlIO.class.getDeclaredMethod("getLastPacketReceivedTimeMs", new Class<?>[0]);
                m.setAccessible(true);
            } catch (SecurityException e) {
                m = null;
            } catch (NoSuchMethodException e) {
                m = null;
            }
            getLastPacketReceivedTimeMsMethod = m;

            try {
                m = com.mysql.jdbc.MysqlIO.class.getDeclaredMethod("getLastPacketSentTimeMs", new Class<?>[0]);
                m.setAccessible(true);
            } catch (SecurityException e) {
                m = null;
            } catch (NoSuchMethodException e) {
                m = null;
            }
            getLastPacketSentTimeMsMethod = m;
        }

    }

    /**
     * Gets the time stamp (the number of milliseconds since January 1, 1970, 00:00:00 GMT) when the last packet has been received through given connection.
     *
     * @param con The connection to get the "last packet received" time from
     * @return The time stamp or <code>null</code>
     */
    public static Long getOLastPacketReceivedTime(Object con) {
        if (con instanceof com.mysql.jdbc.ConnectionImpl) {
            return getLastPacketReceivedTime0((com.mysql.jdbc.ConnectionImpl) con);
        }
        return null;
    }

    /**
     * Gets the time stamp (the number of milliseconds since January 1, 1970, 00:00:00 GMT) when the last packet has been received through given connection.
     *
     * @param con The connection to get the "last packet received" time from
     * @return The time stamp or <code>null</code>
     */
    public static Long getLastPacketReceivedTime(Connection con) {
        if (con instanceof com.mysql.jdbc.ConnectionImpl) {
            return getLastPacketReceivedTime0((com.mysql.jdbc.ConnectionImpl) con);
        }
        return null;
    }

    private static Long getLastPacketReceivedTime0(com.mysql.jdbc.ConnectionImpl mysqlConnectionImpl) {
        Method getLastPacketReceivedTimeMsMethod = LastPacketTimeMethodHolder.getLastPacketReceivedTimeMsMethod;
        if (null != getLastPacketReceivedTimeMsMethod) {
            try {
                Long lastPacketReceivedTime = (Long) getLastPacketReceivedTimeMsMethod.invoke(mysqlConnectionImpl.getIO(), LastPacketTimeMethodHolder.EMPTY_ARGS);
                return lastPacketReceivedTime.longValue() == 0 ? null : lastPacketReceivedTime;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Gets the time stamp (the number of milliseconds since January 1, 1970, 00:00:00 GMT) when the last packet has been sent through given connection.
     *
     * @param con The connection to get the "last packet sent" time from
     * @return The time stamp or <code>null</code>
     */
    public static Long getOLastPacketSentTime(Object con) {
        if (con instanceof com.mysql.jdbc.ConnectionImpl) {
            return getLastPacketSentTime0((com.mysql.jdbc.ConnectionImpl) con);
        }
        return null;
    }

    /**
     * Gets the time stamp (the number of milliseconds since January 1, 1970, 00:00:00 GMT) when the last packet has been sent through given connection.
     *
     * @param con The connection to get the "last packet sent" time from
     * @return The time stamp or <code>null</code>
     */
    public static Long getLastPacketSentTime(Connection con) {
        if (con instanceof com.mysql.jdbc.ConnectionImpl) {
            return getLastPacketSentTime0((com.mysql.jdbc.ConnectionImpl) con);
        }
        return null;
    }

    private static Long getLastPacketSentTime0(com.mysql.jdbc.ConnectionImpl mysqlConnectionImpl) {
        Method getLastPacketSentTimeMsMethod = LastPacketTimeMethodHolder.getLastPacketSentTimeMsMethod;
        if (null != getLastPacketSentTimeMsMethod) {
            try {
                Long lastPacketSentTime = (Long) getLastPacketSentTimeMsMethod.invoke(mysqlConnectionImpl.getIO(), LastPacketTimeMethodHolder.EMPTY_ARGS);
                return lastPacketSentTime.longValue() == 0 ? null : lastPacketSentTime;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Gets the time stamp (the number of milliseconds since January 1, 1970, 00:00:00 GMT) when the last packet has been either received or sent through given connection.
     *
     * @param con The connection to get the "last packet either received or sent" time from
     * @return The time stamp or <code>null</code>
     */
    public static Long getOLastPacketTime(Object con) {
        if (con instanceof com.mysql.jdbc.ConnectionImpl) {
            return getLastPacketTime0((com.mysql.jdbc.ConnectionImpl) con);
        }
        return null;
    }

    /**
     * Gets the time stamp (the number of milliseconds since January 1, 1970, 00:00:00 GMT) when the last packet has been either received or sent through given connection.
     *
     * @param con The connection to get the "last packet either received or sent" time from
     * @return The time stamp or <code>null</code>
     */
    public static Long getLastPacketTime(Connection con) {
        if (con instanceof com.mysql.jdbc.ConnectionImpl) {
            return getLastPacketTime0((com.mysql.jdbc.ConnectionImpl) con);
        }
        return null;
    }

    private static Long getLastPacketTime0(com.mysql.jdbc.ConnectionImpl mysqlConnectionImpl) {
        Long lastPacketReceivedTime = getLastPacketReceivedTime0(mysqlConnectionImpl);
        Long lastPacketSentTime     = getLastPacketSentTime0(mysqlConnectionImpl);

        if (null == lastPacketReceivedTime) {
            // No "last packet received" time available. Prefer "last packet sent" time (if any)
            return lastPacketSentTime;
        }
        if (null == lastPacketSentTime) {
            // Neither nor...
            return null;
        }

        return lastPacketReceivedTime.longValue() > lastPacketSentTime.longValue() ? lastPacketReceivedTime : lastPacketSentTime;
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    /** Simple holder to delay initialization when needed */
    private static class MysqlConnectionFieldHolder {
        static final Field mysqlConnectionField;
        static {
            Field f;
            try {
                f = com.mysql.jdbc.MysqlIO.class.getDeclaredField("mysqlConnection");
                f.setAccessible(true);
            } catch (SecurityException e) {
                f = null;
            } catch (NoSuchFieldException e) {
                f = null;
            }
            mysqlConnectionField = f;
        }

    }

    /** The closed state for a connection */
    public static enum ClosedState {
        /** Connection appears to be open */
        OPEN,
        /** Connection has been explicitly closed since {@link Connection#isClosed()} signaled <code>true</code> */
        EXPLICITLY_CLOSED,
        /** Connection seems to be internally closed; meaning necessary resources were closed rendering connection unusable */
        INTERNALLY_CLOSED;
    }

    /**
     * Checks whether specified connection appears to be closed. This is connection has been explicitly closed or lost its internal network resources.
     *
     * @param con The connection to check
     * @return The determined closed status for specified connection
     * @throws SQLException If closed status cannot be returned
     */
    public static ClosedState isClosed(Connection con, boolean closeOnInternallyClosed) throws SQLException {
        if (con.isClosed()) {
            return ClosedState.EXPLICITLY_CLOSED;
        }

        if (isInternallyClosed(con, closeOnInternallyClosed)) {
            return ClosedState.INTERNALLY_CLOSED;
        }

        return ClosedState.OPEN;
    }

    /**
     * Checks whether specified connection appears to be internally closed. This is connection lost its internal network resources.
     *
     * @param con The connection to check
     * @param closeOnInternallyClosed Whether to perform an explicit close in case considered as internally closed
     * @return The determined closed status for specified connection
     */
    public static boolean isInternallyClosed(Connection con, boolean closeOnInternallyClosed) {
        if (con instanceof com.mysql.jdbc.ConnectionImpl) {
            com.mysql.jdbc.ConnectionImpl mysqlConnectionImpl = (com.mysql.jdbc.ConnectionImpl) con;
            if (seemsClosed(mysqlConnectionImpl)) {
                if (closeOnInternallyClosed) {
                    closeSafe(mysqlConnectionImpl);
                }
                return true;
            }
        }

        return false;
    }

    private static boolean seemsClosed(com.mysql.jdbc.ConnectionImpl mysqlConnectionImpl) {
        try {
            Field mysqlConnectionField = MysqlConnectionFieldHolder.mysqlConnectionField;
            if (null != mysqlConnectionField) {
                return null == mysqlConnectionField.get(mysqlConnectionImpl.getIO());
            }
        } catch (Exception e) {
            // Ignore
        }

        // Not definitely known
        return false;
    }

    private static void closeSafe(ConnectionImpl mysqlConnection) {
        if (null != mysqlConnection) {
            try {
                mysqlConnection.realClose(false, false, false, null);
            } catch (Exception e) {
                // ignore, we're going away.
            }
        }
    }

    /**
     * Code is correct and will not leave a connection in CLOSED_WAIT state. See CloseWaitTest.java.
     */
    public static void close(Connection con) {
        if (null != con) {
            try {
                con.close();
            } catch (Exception e) {
                // ignore, we're going away.
            }
        }
    }

    /**
     * Gets the connection identifier.
     *
     * @param con The connection
     * @return The connection identifier or <code>0</code> (zero)
     */
    public static long getConnectionId(Connection con) {
        return (con instanceof com.mysql.jdbc.MySQLConnection) ? ((com.mysql.jdbc.MySQLConnection) con).getId() : 0L;
    }

}
