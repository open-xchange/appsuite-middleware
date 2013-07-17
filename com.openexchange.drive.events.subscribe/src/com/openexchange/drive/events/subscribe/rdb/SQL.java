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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.StringAllocator;
import com.openexchange.log.LogFactory;

/**
 * {@link SQL}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SQL {

    public static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SQL.class));

    public static String getCreateDriveEventSubscriptionsTableStmt() {
        return "CREATE TABLE driveEventSubscriptions (" +
            "uuid BINARY(16) NOT NULL," +
            "cid INT4 UNSIGNED NOT NULL," +
            "service VARCHAR(64) NOT NULL," +
            "token VARCHAR(255) NOT NULL," +
            "folder VARCHAR(512)," +
            "PRIMARY KEY (cid, uuid)," +
            "INDEX (cid,service,folder)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=ascii;";
    }

    // no UNIQUE constraint possible due to combined length of required columns
    public static final String INSERT_SUBSCRIPTION_STMT =
        "INSERT INTO driveEventSubscriptions (uuid,cid,service,token,folder) " +
        "SELECT UNHEX(?),?,?,?,REVERSE(?) FROM DUAL WHERE NOT EXISTS " +
        "(SELECT uuid FROM driveEventSubscriptions WHERE cid=? AND service=? AND token=? AND folder=REVERSE(?));";


//        "VALUES (UNHEX(?),?,?,?,REVERSE(?));";


//        INSERT INTO driveEventSubscriptions (uuid,cid,service,token,folder)
//    SELECT UNHEX('8e60a72b560849ec819321bb918855af'),424242669,'apn','28919862989a1b5ba59c11d5f7cb7ba2b9678be9dd18b033184d04f682013677',REVERSE('65841')
//    FROM dual WHERE NOT EXISTS
//        (SELECT uuid FROM driveEventSubscriptions
//        WHERE cid=424242669 AND service='apn' AND token='28919862989a1b5ba59c11d5f7cb7ba2b9678be9dd18b033184d04f682013677' AND folder=REVERSE('65841'))



    public static final String DELETE_SUBSCRIPTION_STMT =
        "DELETE FROM driveEventSubscriptions " +
        "WHERE cid=? AND service=? AND token=? AND folder=?;";

    public static final String UPDATE_TOKEN_STMT =
        "UPDATE driveEventSubscriptions SET token=? " +
        "WHERE cid=? AND service=? AND token=?;";

    public static final String DELETE_SUBSCRIPTIONS_IN_CONTEXT_STMT =
        "DELETE FROM driveEventSubscriptions " +
        "WHERE cid=?;";

    /**
     * SELECT LOWER(HEX(uuid)),token,REVERSE(folder) FROM driveEventSubscriptions
     * WHERE cid=? AND service=? AND REVERSE(folder) IN (...);"
     */
    public static final String SELECT_SUBSCRIPTIONS_STMT(Collection<String> rootFolderIDs) throws OXException {
        if (null == rootFolderIDs || 0 == rootFolderIDs.size()) {
            throw new IllegalArgumentException("folderIDs");
        }
        StringAllocator allocator = new StringAllocator();
        allocator.append("SELECT LOWER(HEX(uuid)),token,REVERSE(folder) FROM driveEventSubscriptions ");
        allocator.append("WHERE cid=? AND service=? AND REVERSE(folder)");
        Iterator<String> iterator = rootFolderIDs.iterator();
        if (1 == rootFolderIDs.size()) {
            allocator.append("='").append(escape(iterator.next())).append("';");
        } else {
            allocator.append(" IN ('").append(escape(iterator.next()));
            while (iterator.hasNext()) {
                allocator.append("','").append(escape(iterator.next()));
            }
            allocator.append("');");
        }
        return allocator.toString();
    }

    public static ResultSet logExecuteQuery(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeQuery();
        } else {
            long start = System.currentTimeMillis();
            ResultSet resultSet = stmt.executeQuery();
            LOG.debug("executeQuery: " + stmt.toString() + " - " + (System.currentTimeMillis() - start) + " ms elapsed.");
            return resultSet;
        }
    }

    public static int logExecuteUpdate(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeUpdate();
        } else {
            long start = System.currentTimeMillis();
            int rowCount = stmt.executeUpdate();
            LOG.debug("executeUpdate: " + stmt.toString() + " - " + rowCount + " rows affected, " +
                (System.currentTimeMillis() - start) + " ms elapsed.");
            return rowCount;
        }
    }

    public static String escape(String value) throws OXException {
        if (null == value) {
            System.out.println(value);
        }
        try {
            return URLEncoder.encode(value, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
    }

    public static String unescape(String value) throws OXException {
        try {
            return URLDecoder.decode(value, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
    }

    private SQL() {
        super();
    }

}

