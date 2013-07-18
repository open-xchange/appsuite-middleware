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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;

/**
 * {@link MailAccountCreateTablesTask} - Inserts necessary tables to support multiple mail accounts.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailAccountCreateTablesTask implements UpdateTask {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MailAccountCreateTablesTask.class));

    @Override
    public int addedWithVersion() {
        return 40;
    }

    @Override
    public int getPriority() {
        return UpdateTaskPriority.HIGHEST.priority;
    }

    private static final String getCreateMailAccount() {
        return "CREATE TABLE `user_mail_account` (" +
            "id INT4 unsigned NOT NULL," +
            "cid INT4 unsigned NOT NULL," +
            "user INT4 unsigned NOT NULL," +
            "name VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
            "url VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
            "login varchar(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
            "password VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL," +
            "primary_addr VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
            "personal VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL," +
            "replyTo VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL," +
            "default_flag TINYINT unsigned NOT NULL default 0," +
            "spam_handler VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
            "trash VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
            "sent VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
            "drafts VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
            "spam VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
            "confirmed_spam VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
            "confirmed_ham VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
            "archive VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT ''," +
            "unified_inbox TINYINT unsigned default 0," +
            "trash_fullname VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
            "sent_fullname VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
            "drafts_fullname VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
            "spam_fullname VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
            "confirmed_spam_fullname VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
            "confirmed_ham_fullname VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
            "archive_fullname VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT ''," +
            "PRIMARY KEY  (cid, id, user)," +
            "INDEX (cid, user)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";
    }

    private static final String getCreateTransportAccount() {
        return "CREATE TABLE `user_transport_account` (" +
                "id INT4 unsigned NOT NULL," +
                "cid INT4 unsigned NOT NULL," +
                "user INT4 unsigned NOT NULL," +
                "name VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
                "url VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
                "login varchar(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
                "password VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL," +
                "send_addr VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
                "personal VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL," +
                "replyTo VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL," +
                "default_flag TINYINT unsigned NOT NULL default 0," +
                "unified_inbox TINYINT unsigned default 0," +
                "PRIMARY KEY  (cid, id, user)," +
                "INDEX (cid, user)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";
    }

    private static final String getCreateMailAccountProperties() {
        return "CREATE TABLE user_mail_account_properties (" +
                "id INT4 unsigned NOT NULL," +
                "cid INT4 unsigned NOT NULL," +
                "user INT4 unsigned NOT NULL," +
                "name VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
                "value VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
                "PRIMARY KEY  (cid, id, user, name)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    }

    private static final String getCreateTransportAccountProperties() {
        return "CREATE TABLE user_transport_account_properties (" +
                "id INT4 unsigned NOT NULL," +
                "cid INT4 unsigned NOT NULL," +
                "user INT4 unsigned NOT NULL," +
                "name VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
                "value VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
                "PRIMARY KEY  (cid, id, user, name)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    }

    private static final String getCreateSequence() {
        return "CREATE TABLE sequence_mail_service (" +
                "cid INT4 unsigned NOT NULL," +
                "id INT4 unsigned NOT NULL," +
                "PRIMARY KEY (cid)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";
    }

    @Override
    public void perform(final Schema schema, final int contextId) throws OXException {
        createTable("sequence_mail_service", getCreateSequence(), contextId);
        createTable("user_mail_account", getCreateMailAccount(), contextId);
        createTable("user_mail_account_properties", getCreateMailAccountProperties(), contextId);
        createTable("user_transport_account", getCreateTransportAccount(), contextId);
        createTable("user_transport_account_properties", getCreateTransportAccountProperties(), contextId);
        if (LOG.isInfoEnabled()) {
            LOG.info("UpdateTask 'MailAccountCreateTablesTask' successfully performed!");
        }
    }

    private static void createTable(final String tablename, final String sqlCreate, final int contextId) throws OXException {
        final Connection writeCon = Database.get(contextId, true);
        PreparedStatement stmt = null;
        try {
            try {
                if (tableExists(tablename, writeCon.getMetaData())) {
                    return;
                }
                stmt = writeCon.prepareStatement(sqlCreate);
                stmt.executeUpdate();
            } catch (final SQLException e) {
                throw createSQLError(e);
            }
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(contextId, true, writeCon);
        }
    }

    /**
     * The object type "TABLE"
     */
    private static final String[] types = { "TABLE" };

    /**
     * Check a table's existence
     *
     * @param tableName The table name to check
     * @param dbmd The database's meta data
     * @return <code>true</code> if table exists; otherwise <code>false</code>
     * @throws SQLException If a SQL error occurs
     */
    private static boolean tableExists(final String tableName, final DatabaseMetaData dbmd) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = dbmd.getTables(null, null, tableName, types);
            return resultSet.next();
        } finally {
            closeSQLStuff(resultSet, null);
        }
    }

    private static OXException createSQLError(final SQLException e) {
        return UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
    }
}
