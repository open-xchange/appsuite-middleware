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

package com.openexchange.share.storage.internal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.share.storage.mapping.ShareMapper;

/**
 * {@link SQL}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SQL {

    /**
     * The DB mapper for the "share" table
     */
    public static final ShareMapper SHARE_MAPPER = new ShareMapper();

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SQL.class);

    public static String getCreateShareTableStmt() {
        return "CREATE TABLE share (" +
            "cid int(10) unsigned NOT NULL," +
            "guest int(10) unsigned NOT NULL," +
            "module tinyint(3) unsigned NOT NULL," +
            "folder varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
            "item varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
            "owner int(10) unsigned NOT NULL," +
            "expires bigint(64) DEFAULT NULL," +
            "created bigint(64) NOT NULL," +
            "created_by int(10) unsigned NOT NULL," +
            "modified bigint(64) NOT NULL," +
            "modified_by int(10) unsigned NOT NULL," +
            "meta BLOB DEFAULT NULL," +
            "PRIMARY KEY (cid,guest,module,folder,item)," +
            "KEY owner_index (cid,owner)," +
            "KEY created_by_index (cid,created_by)," +
            "KEY expires_index (cid,expires)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    }

    /**
     * Gets all known update tasks.
     *
     * @return The update tasks
     */
    public static UpdateTaskV2[] getUpdateTasks() {
        return new UpdateTaskV2[] { new ShareCreateTableTask() };
    };

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

    public static int[] logExecuteBatch(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeBatch();
        } else {
            long start = System.currentTimeMillis();
            int[] result = stmt.executeBatch();
            LOG.debug("executeBatch: {} - {} rows affected, {} ms elapsed.", stmt.toString(), Arrays.toString(result), (System.currentTimeMillis() - start));
            return result;
        }
    }

    public static int sumUpdateCount(int[] result) {
        int sum = 0;
        if (null != result) {
            for (int i : result) {
                sum += i;
            }
        }
        return sum;
    }

    private SQL() {
        super();
    }

}

