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

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.OXException;
import com.openexchange.database.Database;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.OXCalendarException;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.calendar.OXCalendarException.Code;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.UpdateException;
import com.openexchange.groupware.update.exception.UpdateExceptionFactory;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.sql.DBUtils;

/**
 * Bug 12495 caused some appointment change exceptions to have the recurrence
 * data position missing. This task tries to restore the recurrence date
 * position out of the appointment.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
@OXExceptionSource(classId = Classes.UPDATE_TASK, component = EnumComponent.UPDATE)
public final class AppointmentRepairRecurrenceDatePosition implements UpdateTask {

    private static final Log LOG = LogFactory.getLog(
        AppointmentRepairRecurrenceDatePosition.class);

    private static final UpdateExceptionFactory EXCEPTION = new UpdateExceptionFactory(
        AppointmentRepairRecurrenceDatePosition.class);

    /**
     * Default constructor.
     */
    public AppointmentRepairRecurrenceDatePosition() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public int addedWithVersion() {
        return 23;
    }

    /**
     * {@inheritDoc}
     */
    public int getPriority() {
        return UpdateTaskPriority.NORMAL.priority;
    }

    private static final String findSQL = "SELECT cid,timestampfield01,"
        + "timestampfield02,timezone,intfield01,intfield02,intfield04,"
        + "intfield05,field06 FROM prg_dates WHERE intfield01!=intfield02 "
        + "AND field08 IS NULL ORDER BY cid ASC";

    /**
     * {@inheritDoc}
     */
    @OXThrowsMultiple(category = { Category.CODE_ERROR },
        desc = { "" },
        exceptionId = { 1 },
        msg = { "An SQL error occurred: %1$s." }
    )
    public void perform(final Schema schema, final int contextId)
        throws DBPoolingException, UpdateException {
        if (LOG.isInfoEnabled()) {
            LOG.info("Performing update task to repair the recurrence date "
                + "position of appointment change exceptions.");
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
                LOG.info("Repairing in context " + appointment.getContextID()
                    + " appointment " + appointment.getObjectID() + ".");
                try {
                    if (!appointment.containsRecurrencePosition()) {
                        LOG.info("Unable to repair appointment " + appointment
                            .getObjectID() + " in context " + appointment
                            .getContextID() + ". Recurrence position is missing.");
                        continue;
                    }
                    final long recurrence_date_position;
                    if (null != appointment.getRecurrence()) {
                        calculateRecurrenceDatePosition(appointment, appointment
                            .getRecurrencePosition());
                        recurrence_date_position = appointment
                            .getRecurrenceDatePosition().getTime();
                    } else {
                        final CalendarDataObject series = loadAppointment(con,
                            appointment.getContext(), appointment
                            .getRecurrenceID());
                        calculateRecurrenceDatePosition(series, appointment
                            .getRecurrencePosition());
                        recurrence_date_position = series
                            .getRecurrenceDatePosition().getTime();
                    }
                    writeRecurrenceDatePosition(con, appointment.getContextID(),
                        appointment.getObjectID(), recurrence_date_position);
                } catch (final UnsupportedOperationException e) {
                    LOG.info("Unable to repair appointment " + appointment
                        .getObjectID() + " in context " + appointment
                        .getContextID() + ".", e);
                    continue;
                } catch (final OXException e) {
                    LOG.info("Unable to repair appointment " + appointment
                        .getObjectID() + " in context " + appointment
                        .getContextID() + ".", e);
                    continue;
                }
            }
            con.commit();
        } catch (final SQLException e) {
            DBUtils.rollback(con);
            throw EXCEPTION.create(1, e, e.getMessage());
        } finally {
            DBUtils.autocommit(con);
            closeSQLStuff(result, stmt);
            if (con != null) {
                Database.back(contextId, true, con);
            }
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Update task to repair the recurrence date position of "
                + "appointment change exceptions performed.");
        }
    }

    private void writeRecurrenceDatePosition(final Connection con, final int cid,
        final int id, final long recurrenceDatePosition) {
        final String sql = "UPDATE prg_dates SET field08=? WHERE cid=? AND intfield01=?";
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(sql);
            stmt.setString(1, String.valueOf(recurrenceDatePosition));
            stmt.setInt(2, cid);
            stmt.setInt(3, id);
            int changed = stmt.executeUpdate();
            if (1 != changed) {
                LOG.error("Updated " + changed + " appointments instead of 1.");
            }
        } catch (final SQLException e) {
            LOG.error("SQL psroblem while repairing recurrence date position.", e);
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    private CalendarDataObject loadAppointment(final Connection con,
        final Context ctx, final int id) throws OXObjectNotFoundException,
        OXCalendarException {
        final String sql = "SELECT cid,timestampfield01,timestampfield02,"
            + "timezone,intfield01,intfield02,intfield04,intfield05,field06 "
            + "FROM prg_dates WHERE cid=? AND intfield01=?";
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
                throw new OXObjectNotFoundException(OXObjectNotFoundException
                    .Code.OBJECT_NOT_FOUND, EnumComponent.UPDATE, Integer.valueOf(id));
            }
        } catch (final SQLException e) {
            throw new OXCalendarException(Code.CALENDAR_SQL_ERROR, e);
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
        CalendarCollectionService recColl = ServerServiceRegistry.getInstance().getService(
            CalendarCollectionService.class);
        recColl.fillDAO(appointment);
        final RecurringResultsInterface rrs = recColl.calculateRecurring(appointment, 0, 0, recurrencePosition, recColl.MAXTC, true);
        if (null == rrs) {
            throw new OXCalendarException(OXCalendarException.Code.UNABLE_TO_CALCULATE_RECURRING_POSITION_NO_INPUT);
        }
        final RecurringResultInterface rs = rrs.getRecurringResult(0);
        if (null == rs) {
            throw new OXCalendarException(OXCalendarException.Code.UNABLE_TO_CALCULATE_RECURRING_POSITION_NO_INPUT);
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
        public int getContextId() {
            return cid;
        }
        public String[] getFileStorageAuth() {
            throw new UnsupportedOperationException("getFileStorageAuth");
        }
        public long getFileStorageQuota() {
            throw new UnsupportedOperationException("getFileStorageQuota");
        }
        public int getFilestoreId() {
            throw new UnsupportedOperationException("getFilestoreId");
        }
        public String getFilestoreName() {
            throw new UnsupportedOperationException("getFilestoreName");
        }
        public String[] getLoginInfo() {
            throw new UnsupportedOperationException("getLoginInfo");
        }
        public int getMailadmin() {
            throw new UnsupportedOperationException("getMailadmin");
        }
        public String getName() {
            throw new UnsupportedOperationException("getName");
        }
        public boolean isEnabled() {
            throw new UnsupportedOperationException("isEnabled");
        }
        public boolean isUpdating() {
            throw new UnsupportedOperationException("isUpdating");
        }
    }
}
