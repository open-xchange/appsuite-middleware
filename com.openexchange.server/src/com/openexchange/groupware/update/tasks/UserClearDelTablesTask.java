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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.server.services.ServerServiceRegistry;
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

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.update.UpdateTaskV2#perform(com.openexchange.groupware.update.PerformParameters)
     */
    @Override
    public void perform(PerformParameters params) throws OXException {
        int ctxId = params.getContextId();
        DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        Connection con = dbService.getForUpdateTask(ctxId);
        PreparedStatement stmt = null;
        try {
            con.setAutoCommit(false);
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
        } catch (SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            rollback(con);
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(con);
            closeSQLStuff(stmt);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.update.UpdateTaskV2#getDependencies()
     */
    @Override
    public String[] getDependencies() {
        return new String[0];
    }

}
