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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.database.Database;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.tasks.Mapping;
import com.openexchange.groupware.tasks.SQL;
import com.openexchange.groupware.tasks.StorageType;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TaskException;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.update.Tools;

/**
 * This class implements a database update task that sets the modified by
 * attribute of every task to NOT NULL and fills it with the created by
 * attribute.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TaskModifiedByNotNull implements UpdateTask {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(TaskModifiedByNotNull
        .class);

    /**
     * Default constructor.
     */
    public TaskModifiedByNotNull() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public int addedWithVersion() {
        return 2;
    }

    /**
     * {@inheritDoc}
     */
    public int getPriority() {
        return UpdateTaskPriority.NORMAL.priority;
    }

    /**
     * {@inheritDoc}
     */
    public void perform(final Schema schema, final int contextId)
        throws AbstractOXException {
        LOG.info("Performing update task TaskModifiedByNotNull.");
        Connection con = null;
        try {
            con = Database.get(contextId, true);
        } catch (DBPoolingException e) {
            throw new TaskException(TaskException.Code.NO_CONNECTION, e);
        }
        try {
            if (Tools.isNullable(con, task_table, changed_from)) {
                setModifiedBy(con, task_table);
                alterModifiedBy(con, task_table);
            }
            if (Tools.isNullable(con, del_task_table, changed_from)) {
                setModifiedBy(con, del_task_table);
                alterModifiedBy(con, del_task_table);
            }
        } catch (SQLException e) {
            throw new TaskException(TaskException.Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            Database.back(contextId, true, con);
        }
    }

    private void setModifiedBy(final Connection con, final String table)
        throws SQLException {
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            final int updated = stmt.executeUpdate("UPDATE " + table
                + " SET " + changed_from + '=' + created_from + " WHERE "
                + changed_from + " IS NULL");
            LOG.info("Updated in " + updated + " rows " + changed_from + " to "
                + created_from + " in table " + table);
        } finally {
            DBUtils.closeSQLStuff(null, stmt);
        }
    }

    private void alterModifiedBy(final Connection con, final String table)
        throws SQLException {
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute("ALTER TABLE " + table + " MODIFY " + changed_from
                + " INT4 UNSIGNED NOT NULL");
            LOG.info("Altered table " + table + " changed " + changed_from
                + " to NOT NULL.");
        } finally {
            DBUtils.closeSQLStuff(null, stmt);
        }
    }

    private final String task_table = SQL.TASK_TABLES.get(StorageType.ACTIVE);

    private final String del_task_table = SQL.TASK_TABLES.get(StorageType
        .DELETED);

    private final String changed_from = Mapping.getMapping(Task.MODIFIED_BY)
        .getDBColumnName();

    private final String created_from = Mapping.getMapping(Task.CREATED_BY)
        .getDBColumnName();
}
