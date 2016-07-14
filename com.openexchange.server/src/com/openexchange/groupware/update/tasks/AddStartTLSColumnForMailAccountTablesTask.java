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
 *     Copyright (C) 2016-2016 OX Software GmbH
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;


/**
 * {@link AddStartTLSColumnForMailAccountTablesTask} - Adds "starttls" column to "user_mail_account" and "user_transport_account" tables and
 * attempts to set a reasonable default value for that column dependent on mail account data
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.2
 */
public class AddStartTLSColumnForMailAccountTablesTask extends UpdateTaskAdapter {

    private final String[] TABLES = { "user_mail_account", "user_transport_account" };
    private final String[] SECURE_PROTOCOLS = { "imaps", "pop3s", "pops", "smtps" };

    /**
     * Initializes a new {@link AddStartTLSColumnForMailAccountTablesTask}.
     */
    public AddStartTLSColumnForMailAccountTablesTask() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        int contextId = params.getContextId();
        DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        ConfigurationService configService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        boolean force = configService.getBoolProperty("com.openexchange.mail.enforceSecureConnection", false);
        Connection con = null;
        try {
            con = dbService.getForUpdateTask(contextId);
            con.setAutoCommit(false);
            Column column = new Column("starttls", "TINYINT UNSIGNED NOT NULL DEFAULT 0");
            for (String table : TABLES) {
                Tools.addColumns(con, table, new Column[] { column });
            }
            if (force) {
                activateStartTLS(con, force);
            }
            con.commit();
        } catch (SQLException e) {
            DBUtils.rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            DBUtils.rollback(con);
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(con);
            dbService.backForUpdateTask(contextId, con);
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { com.openexchange.groupware.update.tasks.Release781UpdateTask.class.getName() };
    }

    private void activateStartTLS(Connection con, boolean forceSecure) throws SQLException {
        for (String table : TABLES) {
            PreparedStatement stmt = null;
            PreparedStatement stmt2 = null;
            ResultSet rs = null;
            try {
                stmt = con.prepareStatement("SELECT id, cid, user, url FROM " + table + " WHERE id <> 0 FOR UPDATE");
                rs = stmt.executeQuery();
                stmt2 = con.prepareStatement("UPDATE " + table + " SET starttls = ? WHERE id = ? AND cid = ? AND user = ?");
                while (rs.next()) {
                    int id = rs.getInt(1);
                    int cid = rs.getInt(2);
                    int user = rs.getInt(3);
                    String url = rs.getString(4);
                    boolean secure = checkSecureUrl(url) || forceSecure;
                    stmt2 = con.prepareStatement("UPDATE " + table + " SET starttls = ? WHERE id = ? AND cid = ? AND user = ?");
                    stmt2.setBoolean(1, secure);
                    stmt2.setInt(2, id);
                    stmt2.setInt(3, cid);
                    stmt2.setInt(4, user);
                    stmt2.addBatch();
                }
                stmt2.executeBatch();
            } finally {
                DBUtils.closeSQLStuff(stmt2);
                DBUtils.closeSQLStuff(rs, stmt);
            }
        }
    }

    private boolean checkSecureUrl(String url) {
        if (Strings.isEmpty(url)) {
            return false;
        }
        for (String protocol : SECURE_PROTOCOLS) {
            if (url.toLowerCase().startsWith(protocol)) {
                return true;
            }
        }
        return false;
    }

}
