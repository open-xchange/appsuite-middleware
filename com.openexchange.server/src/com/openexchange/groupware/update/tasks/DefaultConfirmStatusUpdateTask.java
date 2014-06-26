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
import java.util.HashMap;
import java.util.Map;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class DefaultConfirmStatusUpdateTask implements UpdateTask {

    private static final String TABLE = "user_setting_server";

    private static final String COLUMN_PRIVATE = "defaultStatusPrivate";

    private static final String COLUMN_PUBLIC = "defaultStatusPublic";

    private static final String ALTER_PRIVATE = "ALTER TABLE " + TABLE + " ADD " + COLUMN_PRIVATE + " INT4 UNSIGNED DEFAULT 0";

    private static final String ALTER_PUBLIC = "ALTER TABLE " + TABLE + " ADD " + COLUMN_PUBLIC + " INT4 UNSIGNED DEFAULT 0";

    private static final Map<String, String> STATEMENTS = new HashMap<String, String>() {

        private static final long serialVersionUID = 1L;

        {
            put(COLUMN_PRIVATE, ALTER_PRIVATE);
            put(COLUMN_PUBLIC, ALTER_PUBLIC);
        }
    };

    @Override
    public int addedWithVersion() {
        return 70;
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
            for (String column : STATEMENTS.keySet()) {
                if (columnExists(con, TABLE, column)) {
                    continue;
                }
                PreparedStatement stmt = con.prepareStatement(STATEMENTS.get(column));
                stmt.execute();
                stmt.close();
            }
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw OXCalendarExceptionCodes.UPDATE_EXCEPTION.create(e.getMessage());
        } finally {
            autocommit(con);
            Database.backNoTimeout(contextId, true, con);
        }
    }

}
