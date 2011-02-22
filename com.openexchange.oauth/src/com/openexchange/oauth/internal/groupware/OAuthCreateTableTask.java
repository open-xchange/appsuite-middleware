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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.oauth.internal.groupware;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.DBPoolingException;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateException;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.oauth.services.ServiceRegistry;
import com.openexchange.server.ServiceException;

/**
 * {@link OAuthCreateTableTask} - Inserts necessary tables.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OAuthCreateTableTask extends UpdateTaskAdapter {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(OAuthCreateTableTask.class);

    /**
     * Initializes a new {@link OAuthCreateTableTask}.
     */
    public OAuthCreateTableTask() {
        super();
    }

    public String[] getDependencies() {
        return new String[] {};
    }

    private static final String getCreate() {
        return "CREATE TABLE oauthAccounts ("
        + "cid INT4 UNSIGNED NOT NULL,"
        + "user INT4 UNSIGNED NOT NULL,"
        + "id INT4 UNSIGNED NOT NULL,"
        + "displayName VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,"
        + "accessToken VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,"
        + "accessSecret VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,"
        + "serviceId VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,"
        + "PRIMARY KEY (cid, id),"
        + "INDEX (cid, user),"
        + "FOREIGN KEY (cid, user) REFERENCES user (cid, id)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";
    }

    public void perform(final PerformParameters params) throws AbstractOXException {
        createTable("oauthAccounts", getCreate(), params.getContextId());
        if (LOG.isInfoEnabled()) {
            LOG.info("UpdateTask 'OAuthCreateTableTask' successfully performed!");
        }
    }

    private static void createTable(final String tablename, final String sqlCreate, final int contextId) throws UpdateException {
        final DatabaseService dbService;
        try {
            dbService = ServiceRegistry.getInstance().getService(DatabaseService.class, true);
        } catch (final ServiceException e) {
            throw new UpdateException(e);
        }
        final Connection writeCon;
        try {
            writeCon = dbService.getWritable(contextId);
        } catch (final DBPoolingException e) {
            throw new UpdateException(e);
        }
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
            dbService.backWritable(contextId, writeCon);
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

    private static UpdateException createSQLError(final SQLException e) {
        return UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
    }
}
