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
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.SchemaStore;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.groupware.update.Updater;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link LastVersionedUpdateTask} is the last update task defining a database schema version number. After this task every task should use
 * the new {@link UpdateTaskV2} interface.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class LastVersionedUpdateTask extends UpdateTaskAdapter {

    public LastVersionedUpdateTask() {
        super();
    }

    @Override
    public int addedWithVersion() {
        return 200;
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        final Schema schema = params.getSchema();
        List<String> executed = determineExecuted(schema.getDBVersion(), Updater.getInstance().getAvailableUpdateTasks());
        final int contextId = params.getContextId();
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class, true);
        final int poolId = dbService.getWritablePool(contextId);
        final Connection con = dbService.getForUpdateTask(contextId);
        try {
            con.setAutoCommit(false);
            executed = excludeAlreadyListed(con, executed);
            insertTasks(con, executed, poolId, schema.getSchema());
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (final OXException e) {
            rollback(con);
            throw e;
        } finally {
            autocommit(con);
            dbService.backForUpdateTask(contextId, con);
        }
    }

    public static void insertTasks(final Connection con, final List<String> executed, final int poolId, final String schema) throws OXException {
        final SchemaStore store = SchemaStore.getInstance();
        for (final String taskName : executed) {
            store.addExecutedTask(con, taskName, true, poolId, schema);
        }
    }

    private List<String> determineExecuted(final int version, final UpdateTask[] tasks) {
        final List<String> retval = new ArrayList<String>();
        for (final UpdateTask task : tasks) {
            if (task.addedWithVersion() != Schema.NO_VERSION && task.addedWithVersion() <= version) {
                retval.add(task.getClass().getName());
            }
        }
        return retval;
    }

    private List<String> excludeAlreadyListed(final Connection con, final List<String> executed) throws OXException {
        Statement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.createStatement();
            result = stmt.executeQuery("SELECT taskName FROM updateTask WHERE cid=0");
            while (result.next()) {
                final int pos = executed.indexOf(result.getString(1));
                if (pos != -1) {
                    executed.remove(pos);
                }
            }
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        return executed;
    }
}
