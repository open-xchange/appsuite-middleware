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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.tools.sql.DBUtils;

/**
 * This update task tries to replace folder 0 for task reminder with correct
 * folder identifier and removes reminder for that the correct folder can't be
 * determined.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TaskReminderFolderZero implements UpdateTask {

    private static final String SELECT_REMINDER = "SELECT cid,object_id,"
        + "target_id,userid,folder FROM reminder WHERE module=4";

    private static final String SELECT_FOLDER = "SELECT folder FROM task_folder"
        + " WHERE cid=? AND id=? AND user=?";

    private static final String UPDATE_REMINDER = "UPDATE reminder SET folder=?"
        + " WHERE cid=? AND object_id=?";

    private static final String DELETE_REMINDER = "DELETE FROM reminder "
        + "WHERE cid=? AND object_id=?";

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(TaskReminderFolderZero.class));

    public TaskReminderFolderZero() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int addedWithVersion() {
        return 6;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return UpdateTaskPriority.LOW.priority;
    }

    private class ReminderData {
        private int cid;
        private int reminderId;
        private int targetId;
        private int userid;
        private int folder;
    }

    @Override
    public void perform(final Schema schema, final int contextId) throws OXException {
        LOG.info("Performing update task TaskReminderFolderZero.");
        final List<ReminderData> reminders = getReminder(contextId);
        final List<ReminderData> update = new ArrayList<ReminderData>();
        final List<ReminderData> remove = new ArrayList<ReminderData>();
        for (final ReminderData remind : reminders) {
             final int folder = findFolder(remind.cid, remind.targetId,
                 remind.userid);
             if (-1 == folder) {
                 remove.add(remind);
             } else if (remind.folder != folder) {
                 remind.folder = folder;
                 update.add(remind);
             }
        }
        LOG.info("Fixing " + update.size() + " reminder and removing " + remove.size() + " not fixable reminder.");
        final Connection con = Database.get(contextId, true);
        try {
            con.setAutoCommit(false);
            update(con, update);
            delete(con, remove);
            con.commit();
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (final SQLException e) {
                LOG.error("Problem setting autocommit to true.", e);
            }
            Database.back(contextId, true, con);
        }
        LOG.info("Update task TaskReminderFolderZero all DONE.");
    }

    private List<ReminderData> getReminder(final int contextId)
        throws OXException {
        final List<ReminderData> retval = new ArrayList<ReminderData>();
        final Connection con = Database.get(contextId, false);
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT_REMINDER);
            result = stmt.executeQuery();
            while (result.next()) {
                final ReminderData remind = new ReminderData();
                int pos = 1;
                remind.cid = result.getInt(pos++);
                remind.reminderId = result.getInt(pos++);
                remind.targetId = result.getInt(pos++);
                remind.userid = result.getInt(pos++);
                remind.folder = result.getInt(pos++);
                retval.add(remind);
            }
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(result, stmt);
            Database.back(contextId, false, con);
        }
        return retval;
    }

    private int findFolder(final int contextId, final int taskId,
        final int userId) throws OXException {
        final Connection con = Database.get(contextId, false);
        int retval = -1;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT_FOLDER);
            stmt.setInt(1, contextId);
            stmt.setInt(2, taskId);
            stmt.setInt(3, userId);
            result = stmt.executeQuery();
            if (result.next()) {
                retval = result.getInt(1);
            }
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(result, stmt);
            Database.back(contextId, false, con);
        }
        return retval;
    }

    private int update(final Connection con, final List<ReminderData> update)
        throws OXException {
        int retval = 0;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(UPDATE_REMINDER);
            for (final ReminderData remind : update) {
                stmt.setInt(1, remind.folder);
                stmt.setInt(2, remind.cid);
                stmt.setInt(3, remind.reminderId);
                stmt.addBatch();
            }
            final int[] updated = stmt.executeBatch();
            for (final int i : updated) {
                retval += i;
            }
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(null, stmt);
        }
        if (retval != update.size()) {
            LOG.error(String.valueOf(update.size())
                + " reminder should be changed, but only " + retval
                + " have been changed.");
        }
        return retval;
    }

    private int delete(final Connection con, final List<ReminderData> remove)
        throws OXException {
        int retval = 0;
        PreparedStatement stmt = null;
        try {
            // DELETE FROM reminder WHERE cid=? AND object_id=?
            stmt = con.prepareStatement(DELETE_REMINDER);
            for (final ReminderData remind : remove) {
                stmt.setInt(1, remind.cid);
                stmt.setInt(2, remind.reminderId);
                stmt.addBatch();
            }
            final int[] removed = stmt.executeBatch();
            for (final int i : removed) {
                retval += i;
            }
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(null, stmt);
        }
        if (retval != remove.size()) {
            LOG.error(String.valueOf(remove.size())
                + " reminder should be removed, but only " + retval
                + " have been removed.");
        }
        return retval;

    }
}
