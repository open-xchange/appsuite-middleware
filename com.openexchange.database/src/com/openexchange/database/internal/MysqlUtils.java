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
 *    trademarks of the OX Software GmbH. group of companies.
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

import java.lang.reflect.Field;
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

    private static final Field mysqlConnectionField;
    static {
        Field f;
        try {
            f = com.mysql.jdbc.MysqlIO.class.getDeclaredField("mysqlConnection");
            f.setAccessible(true);
        } catch (final SecurityException e) {
            f = null;
        } catch (final NoSuchFieldException e) {
            f = null;
        }
        mysqlConnectionField = f;
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
            Field mysqlConnectionField = MysqlUtils.mysqlConnectionField;
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

}
