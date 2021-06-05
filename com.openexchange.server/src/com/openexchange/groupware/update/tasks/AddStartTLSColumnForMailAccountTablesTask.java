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

package com.openexchange.groupware.update.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerServiceRegistry;
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

    private final String[] tables;
    private final String[] secureProtocols;

    /**
     * Initializes a new {@link AddStartTLSColumnForMailAccountTablesTask}.
     */
    public AddStartTLSColumnForMailAccountTablesTask() {
        super();
        tables = new String[] { "user_mail_account", "user_transport_account" };
        secureProtocols = new String[] { "imaps", "pop3s", "pops", "smtps" };
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        ConfigurationService configService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        boolean force = configService.getBoolProperty("com.openexchange.mail.enforceSecureConnection", false);

        Connection con = params.getConnection();
        int rollback = 0;
        try {
            con.setAutoCommit(false);
            rollback = 1;

            Column column = new Column("starttls", "TINYINT UNSIGNED NOT NULL DEFAULT 0");
            for (String table : tables) {
                if (false == Tools.columnExists(con, table, "starttls")) {
                    Tools.addColumns(con, table, new Column[] { column });
                }
            }
            if (force) {
                activateStartTLS(con, force);
            }

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { com.openexchange.groupware.update.tasks.Release781UpdateTask.class.getName() };
    }

    private void activateStartTLS(Connection con, boolean forceSecure) throws SQLException {
        for (String table : tables) {
            PreparedStatement stmt = null;
            PreparedStatement stmt2 = null;
            ResultSet rs = null;
            try {
                stmt = con.prepareStatement("SELECT id, cid, user, url FROM " + table + " WHERE id <> 0 FOR UPDATE");
                rs = stmt.executeQuery();

                if (rs.next()) {
                    stmt2 = con.prepareStatement("UPDATE " + table + " SET starttls = ? WHERE id = ? AND cid = ? AND user = ?");
                    do {
                        int id = rs.getInt(1);
                        int cid = rs.getInt(2);
                        int user = rs.getInt(3);
                        String url = rs.getString(4);
                        boolean secure = checkSecureUrl(url) || forceSecure;

                        stmt2.setBoolean(1, secure);
                        stmt2.setInt(2, id);
                        stmt2.setInt(3, cid);
                        stmt2.setInt(4, user);
                        stmt2.addBatch();
                    } while (rs.next());
                    stmt2.executeBatch();
                }
            } finally {
                Databases.closeSQLStuff(stmt2);
                Databases.closeSQLStuff(rs, stmt);
            }
        }
    }

    private boolean checkSecureUrl(String url) {
        if (Strings.isEmpty(url)) {
            return false;
        }
        for (String protocol : secureProtocols) {
            if (url.toLowerCase().startsWith(protocol)) {
                return true;
            }
        }
        return false;
    }

}
