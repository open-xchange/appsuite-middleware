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

package com.openexchange.mobilepush.events.storage.rdb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {@link Statements}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class Statements {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Statements.class);

    public static final String TABLE_NAME = "mobileEventSubscriptions";

    public static final String CREATE_TABLE =
        "CREATE TABLE mobileEventSubscriptions ( " +
            "cid INT4 UNSIGNED NOT NULL, " +
            "service VARCHAR(64) NOT NULL, " +
            "token VARCHAR(255) NOT NULL, " +
            "provider VARCHAR(64) NOT NULL, " +
            "user INT4 UNSIGNED NOT NULL, " +
            "timestamp BIGINT(20) NOT NULL, " +
            "blockLoginPushUntil BIGINT(20), " +
            "PRIMARY KEY (cid,service,token,provider), " +
            "INDEX (cid,service,token,provider) " +
            ") ENGINE=InnoDB DEFAULT CHARSET=ascii; ";

    public static final String DELETE_SUBSCRIPTION_BY_USER = "DELETE FROM mobileEventSubscriptions WHERE cid=? AND user=?;";

    public static final String DELETE_SUBSCRIPTION_FOR_CONTEXT = "DELETE FROM mobileEventSubscriptions WHERE cid=?";

    public static final String REPLACE_OR_ADD_SUBSCRIPTION = "REPLACE INTO mobileEventSubscriptions (cid,service,token,provider,user,timestamp) "
        + "VALUES (?,?,?,?,?,?);";

    public static final String UPDATE_TOKENS = "UPDATE mobileEventSubscriptions "
        + "SET token=?, timestamp=? "
        + "WHERE cid=? AND service=? AND token=? ";

    public static final String DELETE_TOKEN_BY_PROVIDER = "DELETE FROM mobileEventSubscriptions "
        + "WHERE cid=? AND service=? AND provider=? AND token=?";

    public static final String DELETE_TOKEN_BY_SERVICE_ID = "DELETE FROM mobileEventSubscriptions "
        + "WHERE cid=? AND service=? AND token=?";

    public static final String SELECT_SUBSCRIPTIONS = "SELECT cid, service, token, provider, user, timestamp "
        + "FROM mobileEventSubscriptions "
        + "WHERE cid=? AND user=? AND service=? AND provider=?;";

    public static final String SELECT_TOKENS = "SELECT token "
        + "FROM mobileEventSubscriptions WHERE cid=? AND user=? AND provider=? AND service=? GROUP BY user;";


    /**
     * Returns the following SQL statement based on parameter blockLoginPush.
     * <br>
     *  <code>true: </code><br>
     *   <code>"SELECT cid, user, token FROM mobileEventSubscriptions WHERE provider=? AND lastLoginPush &lt;= System.currentTimeMillis() GROUP BY cid, user;</code>
     *  <br><br>
     *  <code>false: </code><br>
     *   <code>"SELECT cid, user, token FROM mobileEventSubscriptions WHERE provider=? GROUP BY cid, user;</code>
     *
     * @param blockLoginPush <code>true</code> if push notification should be blocked until b limit is exceeded.
     * @return string which represents the statement
     *
     */
    public static String SELECT_SUBSCRIPTIONS(boolean blockLoginPush) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT cid, user, token FROM mobileEventSubscriptions WHERE provider=? ");
        if(blockLoginPush) {
            stringBuilder.append(" AND (blockLoginPushUntil IS NULL");
            stringBuilder.append(" OR blockLoginPushUntil <= ").append(System.currentTimeMillis()).append(')');
        }
        stringBuilder.append(" GROUP BY user, cid;");
        return stringBuilder.toString();
    }


    private static final String UPDATE_LAST_LOGIN_PUSH_TIMESTAMP = "UPDATE mobileEventSubscriptions "
        + "SET blockLoginPushUntil=? "
        + "WHERE cid=? AND user";

    /**
     * Resulting SQL statement
     *  <br><code>UPDATE mobileEventSubscriptions SET blockLoginPushUntil=? WHERE cid=? AND user;</code><br>
     * Count of IN arguments depends on userSize
     * @param userSize
     * @return
     */
    public static String UPDATE_LAST_PUSH_LOGIN_TIMESTAMP(int userSize) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(UPDATE_LAST_LOGIN_PUSH_TIMESTAMP);
        appendPlaceholders(stringBuilder, userSize);
        return stringBuilder.append(';').toString();
    }

    /**
     * Appends a SQL clause for the given number of placeholders, i.e. either <code>=?</code> if <code>count</code> is <code>1</code>, or
     * an <code>IN</code> clause like <code>IN (?,?,?,?)</code> in case <code>count</code> is greater than <code>1</code>.
     *
     * @param stringBuilder The string builder to append the clause
     * @param count The number of placeholders to append
     * @return The string builder
     */
    private static StringBuilder appendPlaceholders(StringBuilder stringBuilder, int count) {
        if (0 >= count) {
            throw new IllegalArgumentException("count");
        }
        if (1 == count) {
            stringBuilder.append("=?");
        } else {
            stringBuilder.append(" IN (?");
            for (int i = 1; i < count; i++) {
                stringBuilder.append(",?");
            }
            stringBuilder.append(')');
        }
        return stringBuilder;
    }

    public static ResultSet logExecuteQuery(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeQuery();
        } else {
            long start = System.currentTimeMillis();
            ResultSet resultSet = stmt.executeQuery();
            LOG.debug("executeQuery: {} - {} ms elapsed.", stmt.toString(), (System.currentTimeMillis() - start));
            return resultSet;
        }
    }

    public static int logExecuteUpdate(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeUpdate();
        } else {
            long start = System.currentTimeMillis();
            int rowCount = stmt.executeUpdate();
            LOG.debug("executeUpdate: {} - {} rows affected, {} ms elapsed.", stmt.toString(), rowCount, (System.currentTimeMillis() - start));
            return rowCount;
        }
    }
}
