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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.rollback;
import static com.openexchange.tools.update.Tools.columnExists;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class ContactCollectOnIncomingAndOutgoingMailUpdateTask implements UpdateTask {

    private static final String TABLE = "user_setting_server";

    private static final String COLUMN_TRANSPORT = "contactCollectOnMailTransport";

    private static final String COLUMN_ACCESS = "contactCollectOnMailAccess";

    private static final String COLUMN_DEFINITION_TRANSPORT = COLUMN_TRANSPORT + " BOOL DEFAULT TRUE";

    private static final String COLUMN_DEFINITION_ACCESS = COLUMN_ACCESS + " BOOL DEFAULT TRUE";

    private static final String ALTER_TABLE = "ALTER TABLE " + TABLE + " ADD (";

    @Override
    public int addedWithVersion() {
        return 92;
    }

    @Override
    public int getPriority() {
        return UpdateTaskPriority.NORMAL.priority;
    }

    @Override
    public void perform(Schema schema, int contextId) throws OXException {
        Connection con = Database.getNoTimeout(contextId, true);
        try {
            con.setAutoCommit(false);

            StringBuilder sb = new StringBuilder(ALTER_TABLE);

            if (!columnExists(con, TABLE, COLUMN_TRANSPORT)) {
                sb.append(COLUMN_DEFINITION_TRANSPORT);
                sb.append(", ");
            }

            if (!columnExists(con, TABLE, COLUMN_ACCESS)) {
                sb.append(COLUMN_DEFINITION_ACCESS);
                sb.append(", ");
            }

            String stmt = sb.toString();

            if (stmt.equals(ALTER_TABLE)) {
                return;
            }

            stmt = stmt.substring(0, stmt.length() - 2);
            stmt = stmt + ")";

            PreparedStatement pstmt = con.prepareStatement(stmt);
            pstmt.execute();
            pstmt.close();
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.backNoTimeout(contextId, true, con);
        }
    }
}
