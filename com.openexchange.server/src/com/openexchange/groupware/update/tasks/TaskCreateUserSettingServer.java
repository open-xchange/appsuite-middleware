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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.TaskExceptionCode;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.tools.update.Tools;

/**
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 *
 */
public class TaskCreateUserSettingServer implements UpdateTask {

    private static final String TABLE_NAME = "user_setting_server";
    private static final String CREATE_STATEMENT = "CREATE TABLE user_setting_server (" +
            "cid INT4 UNSIGNED NOT NULL," +
            "user INT4 UNSIGNED NOT NULL," +
            "contact_collect_folder INT4 UNSIGNED," +
            "contact_collect_enabled BOOL," +
            "FOREIGN KEY(cid, user) REFERENCES user(cid, id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

    @Override
    public int addedWithVersion() {
        return 27;
    }

    @Override
    public int getPriority() {
        return UpdateTaskPriority.NORMAL.priority;
    }

    @Override
    public void perform(final Schema schema, final int contextId) throws OXException {
        Connection con = null;
        try {
            con = Database.getNoTimeout(contextId, true);
        } catch (final OXException e) {
            throw TaskExceptionCode.NO_CONNECTION.create(e);
        }

        try {
            if(!Tools.tableExists(con, TABLE_NAME)) {
                createTable(con);
            }
        } catch (final SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        } finally {
                Database.backNoTimeout(contextId, true, con);
        }
    }

    private void createTable(final Connection con) throws SQLException {
        final Statement stmt = con.createStatement();
        try {
            stmt.execute(CREATE_STATEMENT);
        } finally {
            stmt.close();
        }
    }
}
