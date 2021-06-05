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

import static com.openexchange.database.Databases.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Tools;

/**
 * {@link UserClearDelTablesTask}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class UserClearDelTablesTask extends UpdateTaskAdapter {

    private static final String[] OBSOLETE_COLUMNS = new String[] {
        "imapServer", "imapLogin", "mail", "mailEnabled", "mailDomain", "preferredLanguage", "shadowLastChange", "smtpServer", "timeZone", "userPassword",
        "passwordMech", "homeDirectory", "loginShell" };

    private static final String TABLE = "del_user";

    /**
     * Initializes a new {@link UserClearDelTablesTask}.
     */
    public UserClearDelTablesTask() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        PreparedStatement stmt = null;
        try {
            con.setAutoCommit(false);
            rollback = 1;

            for (String column : OBSOLETE_COLUMNS) {
                int type = Tools.getColumnType(con, TABLE, column);
                if (!Tools.hasDefaultValue(con, TABLE, column)) {
                    stmt = con.prepareStatement("ALTER TABLE " + TABLE + " ALTER " + column + " SET DEFAULT ?");
                    switch (type) {
                    case java.sql.Types.CHAR:
                    case java.sql.Types.VARCHAR:
                        stmt.setString(1, "");
                        break;
                    case java.sql.Types.DATE:
                    case java.sql.Types.TIMESTAMP:
                        stmt.setDate(1, new java.sql.Date(0));
                        break;
                    case java.sql.Types.TINYINT:
                    case java.sql.Types.BOOLEAN:
                        stmt.setInt(1, 0);
                        break;
                    case java.sql.Types.BLOB:
                    case -1:
                        stmt.cancel();
                        stmt.close();
                        continue;
                    default:
                        stmt.setInt(1, -1);
                        break;
                    }
                    stmt.executeUpdate();
                    stmt.close();
                }
                if (Tools.isNullable(con, TABLE, column)) {
                    stmt = con.prepareStatement("UPDATE " + TABLE + " SET " + column + " = NULL");
                    stmt.executeUpdate();
                    stmt.close();
                } else {
                    stmt = con.prepareStatement("UPDATE " + TABLE + " SET " + column + " = ?");
                    switch (type) {
                    case java.sql.Types.CHAR:
                    case java.sql.Types.VARCHAR:
                        stmt.setString(1, "");
                        break;
                    case java.sql.Types.DATE:
                    case java.sql.Types.TIMESTAMP:
                        stmt.setDate(1, new java.sql.Date(0));
                        break;
                    case java.sql.Types.TINYINT:
                    case java.sql.Types.BOOLEAN:
                        stmt.setInt(1, 0);
                        break;
                    default:
                        stmt.setInt(1, -1);
                        break;
                    }
                    stmt.executeUpdate();
                    stmt.close();
                }
            }

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
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
        return new String[0];
    }

}
