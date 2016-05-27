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

package com.openexchange.drive.events.subscribe.rdb;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.exception.OXException;

/**
 * {@link SQL}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SQL {

    public static String getCreateDriveEventSubscriptionsTableStmt() {
        return "CREATE TABLE driveEventSubscriptions (" +
            "uuid BINARY(16) DEFAULT NULL," +
            "cid INT4 UNSIGNED NOT NULL," +
            "service VARCHAR(64) NOT NULL," +
            "token VARCHAR(255) NOT NULL," +
            "user INT4 UNSIGNED NOT NULL," +
            "folder VARCHAR(512)," +
            "timestamp BIGINT(20) NOT NULL," +
            "PRIMARY KEY (cid,service,token)," +
            "INDEX (cid,service,folder)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=ascii;";
        //TODO: change to the following for next release
//        return "CREATE TABLE driveEventSubscriptions (" +
//            "uuid BINARY(16) NOT NULL," +
//            "cid INT4 UNSIGNED NOT NULL," +
//            "service VARCHAR(64) NOT NULL," +
//            "token VARCHAR(255) NOT NULL," +
//            "user INT4 UNSIGNED NOT NULL," +
//            "folder VARCHAR(512)," +
//            "timestamp BIGINT(20) NOT NULL," +
//            "PRIMARY KEY (cid,uuid)," +
//            "INDEX (cid,service,folder)," +
//            "INDEX (cid,service,token)" +
//        ") ENGINE=InnoDB DEFAULT CHARSET=ascii;";
    }

    public static final String REPLACE_SUBSCRIPTION_STMT =
        "REPLACE INTO driveEventSubscriptions (uuid,cid,service,token,user,folder,timestamp) " +
        "VALUES (UNHEX(?),?,?,?,?,REVERSE(?),?);";

    public static final String DELETE_SUBSCRIPTION_STMT =
        "DELETE FROM driveEventSubscriptions " +
        "WHERE cid=? AND service=? AND token=?;";

    public static final String DELETE_SUBSCRIPTION_FOR_FOLDER_STMT =
        "DELETE FROM driveEventSubscriptions " +
        "WHERE cid=? AND service=? AND token=? AND folder=REVERSE(?);";

    public static final String EXISTS_TOKEN_STMT =
        "SELECT 1 FROM driveEventSubscriptions " +
        "WHERE cid=? AND service=? AND token=?;";

    public static final String UPDATE_TOKEN_STMT =
        "UPDATE driveEventSubscriptions SET token=? " +
        "WHERE cid=? AND service=? AND token=?;";

    public static final String EXISTS_TOKEN_WITHOUT_SERVICE_STMT =
        "SELECT 1 FROM driveEventSubscriptions " +
        "WHERE cid=? AND token=?;";

    public static final String UPDATE_TOKEN_WITHOUT_SERVICE_STMT =
        "UPDATE driveEventSubscriptions SET token=? " +
        "WHERE cid=? AND token=?;";

    /**
     * SELECT LOWER(HEX(uuid)),service,token,user,REVERSE(folder),timestamp FROM driveEventSubscriptions
     * WHERE cid=? AND service IN (?,?,...) AND folder IN (?,?,...);"
     */
    public static final String SELECT_SUBSCRIPTIONS_STMT(int serviceCount, int folderCount) throws OXException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT LOWER(HEX(uuid)),service,token,user,REVERSE(folder),timestamp FROM driveEventSubscriptions ");
        stringBuilder.append("WHERE cid=? AND service");
        appendPlaceholders(stringBuilder, serviceCount);
        stringBuilder.append(" AND folder");
        appendPlaceholders(stringBuilder, folderCount);
        return stringBuilder.append(';').toString();
    }

    /**
     * SELECT LOWER(HEX(uuid)),service,token,user,REVERSE(folder),timestamp FROM driveEventSubscriptions
     * WHERE service IN (?,?,...);"
     */
    public static final String SELECT_SUBSCRIPTIONS_STMT(int serviceCount) throws OXException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT LOWER(HEX(uuid)),cid,service,token,user,REVERSE(folder),timestamp FROM driveEventSubscriptions ");
        stringBuilder.append("WHERE service");
        appendPlaceholders(stringBuilder, serviceCount);
        return stringBuilder.append(';').toString();
    }

    public static final String DELETE_SUBSCRIPTIONS_FOR_TOKEN_STMT =
        "DELETE FROM driveEventSubscriptions " +
        "WHERE service=? AND token=? AND timestamp<?;";

    public static final String DELETE_SUBSCRIPTIONS_IN_CONTEXT_STMT =
        "DELETE FROM driveEventSubscriptions " +
        "WHERE cid=?;";

    public static final String DELETE_SUBSCRIPTIONS_FOR_USER_STMT =
        "DELETE FROM driveEventSubscriptions " +
        "WHERE cid=? AND user=?;";

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

    public static String escape(String value) throws OXException {
        if (null == value) {
            return null;
        }
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
    }

    public static String unescape(String value) throws OXException {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
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

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SQL.class);

    private SQL() {
        super();
    }

}

