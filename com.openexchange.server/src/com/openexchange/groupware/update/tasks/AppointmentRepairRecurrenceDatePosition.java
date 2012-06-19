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

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.sql.DBUtils;

/**
 * Bug 12495 caused some appointment change exceptions to have the recurrence
 * data position missing. This task tries to restore the recurrence date
 * position out of the appointment.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class AppointmentRepairRecurrenceDatePosition implements UpdateTask {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(AppointmentRepairRecurrenceDatePosition.class));

    public AppointmentRepairRecurrenceDatePosition() {
        super();
    }

    @Override
    public int addedWithVersion() {
        return 23;
    }

    @Override
    public int getPriority() {
        return UpdateTaskPriority.NORMAL.priority;
    }

    private static final String findSQL = "SELECT cid,timestampfield01,timestampfield02,timezone,intfield01,intfield02,intfield04,intfield05,field06 FROM prg_dates WHERE intfield01!=intfield02 AND field08 IS NULL ORDER BY cid ASC";

    @Override
    public void perform(final Schema schema, final int contextId) throws OXException, OXException {
        if (LOG.isInfoEnabled()) {
            LOG.info("Performing update task to repair the recurrence date position of appointment change exceptions.");
        }
        final Connection con = Database.get(contextId, true);
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            con.setAutoCommit(false);
            stmt = con.prepareStatement(findSQL);
            result = stmt.executeQuery();
            while (result.next()) {
                final CalendarDataObject appointment = fillAppointment(result);
                LOG.info("Repairing in context " + appointment.getContextID() + " appointment " + appointment.getObjectID() + ".");
                try {
                    if (!appointment.containsRecurrencePosition()) {
                        LOG.info("Unable to repair appointment " + appointment.getObjectID() + " in context " + appointment.getContextID() + ". Recurrence position is missing.");
                        continue;
                    }
                    final long recurrence_date_position;
                    if (null != appointment.getRecurrence()) {
                        calculateRecurrenceDatePosition(appointment, appointment.getRecurrencePosition());
                        recurrence_date_position = appointment.getRecurrenceDatePosition().getTime();
                    } else {
                        final CalendarDataObject series = loadAppointment(con, appointment.getContext(), appointment.getRecurrenceID());
                        calculateRecurrenceDatePosition(series, appointment.getRecurrencePosition());
                        recurrence_date_position = series.getRecurrenceDatePosition().getTime();
                    }
                    writeRecurrenceDatePosition(con, appointment.getContextID(), appointment.getObjectID(), recurrence_date_position);
                } catch (final UnsupportedOperationException e) {
                    LOG.info(
                        "Unable to repair appointment " + appointment.getObjectID() + " in context " + appointment.getContextID() + ".",
                        e);
                    continue;
                } catch (final OXException e) {
                    LOG.info(
                        "Unable to repair appointment " + appointment.getObjectID() + " in context " + appointment.getContextID() + ".",
                        e);
                    continue;
                }
            }
            con.commit();
        } catch (final SQLException e) {
            DBUtils.rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(con);
            closeSQLStuff(result, stmt);
            if (con != null) {
                Database.back(contextId, true, con);
            }
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Update task to repair the recurrence date position of appointment change exceptions performed.");
        }
    }

    private void writeRecurrenceDatePosition(final Connection con, final int cid, final int id, final long recurrenceDatePosition) {
        final String sql = "UPDATE prg_dates SET field08=? WHERE cid=? AND intfield01=?";
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(sql);
            stmt.setString(1, Long.toString(recurrenceDatePosition));
            stmt.setInt(2, cid);
            stmt.setInt(3, id);
            final int changed = stmt.executeUpdate();
            if (1 != changed) {
                LOG.error("Updated " + changed + " appointments instead of 1.");
            }
        } catch (final SQLException e) {
            LOG.error("SQL psroblem while repairing recurrence date position.", e);
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    private CalendarDataObject loadAppointment(final Connection con, final Context ctx, final int id) throws OXException {
        final String sql = "SELECT cid,timestampfield01,timestampfield02,timezone,intfield01,intfield02,intfield04,intfield05,field06 FROM prg_dates WHERE cid=? AND intfield01=?";
        PreparedStatement stmt = null;
        ResultSet result = null;
        CalendarDataObject retval = null;
        try {
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, id);
            result = stmt.executeQuery();
            if (result.next()) {
                retval = fillAppointment(result);
            } else {
                throw OXException.notFound(String.valueOf(id));
            }
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e);
        } finally {
            DBUtils.closeSQLStuff(result, stmt);
        }
        return retval;
    }

    private CalendarDataObject fillAppointment(final ResultSet result) throws SQLException {
        final CalendarDataObject retval = new CalendarDataObject();
        int pos = 1;
        retval.setContext(new SimpleContext(result.getInt(pos++)));
        retval.setStartDate(result.getTimestamp(pos++));
        retval.setEndDate(result.getTimestamp(pos++));
        String tmpString = result.getString(pos++);
        if (!result.wasNull()) {
            retval.setTimezone(tmpString);
        }
        retval.setObjectID(result.getInt(pos++));
        int tmpInt = result.getInt(pos++);
        if (!result.wasNull()) {
            retval.setRecurrenceID(tmpInt);
        }
        tmpInt = result.getInt(pos++);
        if (!result.wasNull()) {
            retval.setRecurrenceCalculator(tmpInt);
        }
        tmpInt = result.getInt(pos++);
        if (!result.wasNull()) {
            retval.setRecurrencePosition(tmpInt);
        }
        tmpString = result.getString(pos++);
        if (!result.wasNull()) {
            retval.setRecurrence(tmpString);
        }
        return retval;
    }

    private static final void calculateRecurrenceDatePosition(final CalendarDataObject appointment, final int recurrencePosition) throws OXException {
        final CalendarCollectionService recColl = ServerServiceRegistry.getInstance().getService(
            CalendarCollectionService.class);
        recColl.fillDAO(appointment);
        final RecurringResultsInterface rrs = recColl.calculateRecurring(appointment, 0, 0, recurrencePosition, CalendarCollectionService.MAX_OCCURRENCESE, true);
        if (null == rrs) {
            throw OXCalendarExceptionCodes.UNABLE_TO_CALCULATE_RECURRING_POSITION_NO_INPUT.create();
        }
        final RecurringResultInterface rs = rrs.getRecurringResult(0);
        if (null == rs) {
            throw OXCalendarExceptionCodes.UNABLE_TO_CALCULATE_RECURRING_POSITION_NO_INPUT.create();
        }
        appointment.setRecurrenceDatePosition(new Date(rs.getNormalized()));
    }

    private static final class SimpleContext implements Context {
        private static final long serialVersionUID = -3564750621953002511L;
        private final int cid;
        SimpleContext(final int cid) {
            super();
            this.cid = cid;
        }
        @Override
        public int getContextId() {
            return cid;
        }
        @Override
        public String[] getFileStorageAuth() {
            throw new UnsupportedOperationException("getFileStorageAuth");
        }
        @Override
        public long getFileStorageQuota() {
            throw new UnsupportedOperationException("getFileStorageQuota");
        }
        @Override
        public int getFilestoreId() {
            throw new UnsupportedOperationException("getFilestoreId");
        }
        @Override
        public String getFilestoreName() {
            throw new UnsupportedOperationException("getFilestoreName");
        }
        @Override
        public String[] getLoginInfo() {
            throw new UnsupportedOperationException("getLoginInfo");
        }
        @Override
        public int getMailadmin() {
            throw new UnsupportedOperationException("getMailadmin");
        }
        @Override
        public String getName() {
            throw new UnsupportedOperationException("getName");
        }
        @Override
        public boolean isEnabled() {
            throw new UnsupportedOperationException("isEnabled");
        }
        @Override
        public boolean isUpdating() {
            throw new UnsupportedOperationException("isUpdating");
        }
        @Override
        public boolean isReadOnly() {
            throw new UnsupportedOperationException("isReadOnly");
        }

        @Override
        public Map<String, Set<String>> getAttributes() {
            throw new UnsupportedOperationException("getAttributes");
        }
    }
}
