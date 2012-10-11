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

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.reminder.ReminderExceptionCode;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.reminder.internal.SQL;
import com.openexchange.groupware.tasks.TaskExceptionCode;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class RemoveBrokenReminder implements UpdateTask {

    /**
     * Logger.
     */
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(RemoveBrokenReminder.class));

    /**
     * Default constructor.
     */
    public RemoveBrokenReminder() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int addedWithVersion() {
        return 20;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return UpdateTaskPriority.NORMAL.priority;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void perform(final Schema schema, final int contextId)
        throws OXException {
        LOG.info("Performing update task TaskModifiedByNotNull.");
        Connection con = null;
        try {
            con = Database.get(contextId, true);
        } catch (final OXException e) {
            throw TaskExceptionCode.NO_CONNECTION.create(e);
        }
        try {
            con.setAutoCommit(false);
            final ReminderObject[] broken = getBroken(con);
            deleteBroken(con, broken);
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw ReminderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            rollback(con);
            throw e;
        } finally {
            autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    private void deleteBroken(final Connection con, final ReminderObject[] brokens) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL.DELETE_WITH_ID);
            for (final ReminderObject broken : brokens) {
                // Abuse target identifier for context identifier.
                stmt.setInt(1, broken.getTargetId());
                stmt.setInt(2, broken.getObjectId());
                stmt.addBatch();
            }
            final int[] mRows = stmt.executeBatch();
            int rows = 0;
            for (final int mRow : mRows) {
                rows += mRow;
            }
            if (brokens.length != rows) {
                throw ReminderExceptionCode.SQL_ERROR.create("Strangely deleted "
                    + rows + " instead of " + brokens.length);
            }
        } catch (final SQLException e) {
            throw ReminderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    private ReminderObject[] getBroken(final Connection con) throws OXException {
        final List<ReminderObject> tmp = new ArrayList<ReminderObject>();
        Statement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.createStatement();
            result = stmt.executeQuery("SELECT r.cid,r.object_id FROM reminder r "
                + "JOIN oxfolder_tree f ON r.cid=f.cid AND r.folder=f.fuid WHERE "
                + "(r.module=4 AND f.module!=1) OR (r.module=1 AND f.module!=2)");
            while (result.next()) {
                final ReminderObject broken = new ReminderObject();
                // Abuse target identifier for context identifier.
                broken.setTargetId(result.getInt(1));
                broken.setObjectId(result.getInt(2));
            }
        } catch (final SQLException e) {
            throw ReminderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        return tmp.toArray(new ReminderObject[tmp.size()]);
    }

}
