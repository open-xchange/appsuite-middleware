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

package com.openexchange.groupware.reminder.json.actions;

import static com.openexchange.tools.TimeZoneUtils.getTimeZone;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.ReminderService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.reminder.json.ReminderAJAXRequest;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractReminderAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractReminderAction implements AJAXActionService {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(AbstractReminderAction.class);

    private static final AJAXRequestResult RESULT_JSON_NULL = new AJAXRequestResult(JSONObject.NULL, "json");

    private final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractReminderAction}.
     */
    protected AbstractReminderAction(final ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Gets the {@link AppointmentSqlFactoryService} instance.
     *
     * @return The service
     */
    protected AppointmentSqlFactoryService getService() {
        return services.getService(AppointmentSqlFactoryService.class);
    }

    /**
     * Gets the service of specified type
     *
     * @param clazz The service's class
     * @return The service or <code>null</code> if absent
     */
    protected <S> S getService(final Class<? extends S> clazz) {
        return services.getService(clazz);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        try {
            final ReminderAJAXRequest reminderRequest = new ReminderAJAXRequest(requestData, session);
            final String sTimeZone = requestData.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
            if (null != sTimeZone) {
                reminderRequest.setTimeZone(getTimeZone(sTimeZone));
            }
            return perform(reminderRequest);
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Performs specified reminder request.
     *
     * @param req The reminder request
     * @return The result
     * @throws OXException If an error occurs
     * @throws JSONException If a JSON error occurs
     */
    protected abstract AJAXRequestResult perform(ReminderAJAXRequest req) throws OXException, JSONException;

    /**
     * Gets the result filled with JSON <code>NULL</code>.
     *
     * @return The result with JSON <code>NULL</code>.
     */
    protected static AJAXRequestResult getJSONNullResult() {
        return RESULT_JSON_NULL;
    }

    /**
     * This method returns the lastest reminder object of the recurrence appointment. The reminder object contains only the alarm attribute
     * and the recurrence position.
     *
     * @return <code>true</code> if a latest reminder was found.
     */
    protected boolean getLatestRecurringReminder(final Session session, final TimeZone tz, final Date endRange, final ReminderObject reminder) throws OXException {
        final AppointmentSQLInterface calendarSql = getService().createAppointmentSql(session);
        final CalendarCollectionService recColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
        final Appointment calendarDataObject;
        try {
            calendarDataObject = calendarSql.getObjectById(reminder.getTargetId(), reminder.getFolder());
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e);
        }

        final Calendar calendar = Calendar.getInstance(tz);
        calendar.add(Calendar.MONTH, -3);

        final RecurringResultsInterface recurringResults = recColl.calculateRecurring(
            calendarDataObject,
            calendar.getTimeInMillis(),
            endRange.getTime() + calendarDataObject.getAlarm() * 60 * 1000,
            0);
        boolean retval = false;
        if (recurringResults != null && recurringResults.size() > 0) {
            final RecurringResultInterface recurringResult = recurringResults.getRecurringResult(recurringResults.size() - 1);
            calendar.setTimeInMillis(recurringResult.getStart());
            calendar.add(Calendar.MINUTE, -calendarDataObject.getAlarm());
            if (calendar.getTimeInMillis() >= reminder.getDate().getTime()) {
                reminder.setDate(calendar.getTime());
                reminder.setRecurrencePosition(recurringResult.getPosition());
                retval = true;
            }
        } else if (calendarDataObject.getRecurrenceID() != calendarDataObject.getObjectID()) {
            // If the appointment is an exception return true as a reminder exists
            retval = true;
        }
        return retval;
    }

    /**
     * Checks if session-associated user has appropriate module access granted.
     *
     * @param reminder The reminder
     * @param session The associated session
     * @return <code>true</code> if module permission is granted; otherwise <code>false</code>
     */
    protected static boolean hasModulePermission(final ReminderObject reminder, final ServerSession session) {
        switch (reminder.getModule()) {
        case Types.APPOINTMENT:
            return session.getUserPermissionBits().hasCalendar();
        case Types.TASK:
            return session.getUserPermissionBits().hasTask();
        default:
            return true;
        }
    }

    /**
     * Checks if associated calendar event is still accepted by session-associated user.
     *
     * @param reminder The reminder
     * @param session The associated session
     * @return <code>true</code> if still accepted; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    protected static boolean stillAccepted(final ReminderObject reminder, final ServerSession session) throws OXException {
        switch (reminder.getModule()) {
        case Types.APPOINTMENT:
            {
                try {
                    final AppointmentSqlFactoryService factoryService = ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class);
                    if (null != factoryService) {
                        final UserParticipant[] userParticipants = factoryService.createAppointmentSql(session).getObjectById(reminder.getTargetId(), reminder.getFolder()).getUsers();
                        if (null != userParticipants) {
                            final int userId = session.getUserId();
                            for (final UserParticipant userParticipant : userParticipants) {
                                if (userParticipant.getIdentifier() == userId) {
                                    return userParticipant.getConfirm() != Appointment.DECLINE;
                                }
                            }
                        }
                    }
                } catch (final SQLException e) {
                    throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e);
                }
            }
            break;
        case Types.TASK:
            {
                final UserParticipant[] userParticipants = new TasksSQLImpl(session).getTaskById(reminder.getTargetId(), reminder.getFolder()).getUsers();
                if (null != userParticipants) {
                    final int userId = session.getUserId();
                    for (final UserParticipant userParticipant : userParticipants) {
                        if (userParticipant.getIdentifier() == userId) {
                            return userParticipant.getConfirm() != Appointment.DECLINE;
                        }
                    }
                }
            }
            break;
        default:
            return true;
        }
        return true;
    }

    protected static final ReminderObject getNextRecurringReminder(final Session session, final TimeZone tz, final ReminderObject reminder) throws OXException {
        final AppointmentSQLInterface calendarSql = ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class).createAppointmentSql(session);
        final CalendarCollectionService recColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
        final Appointment calendarDataObject;

        try {
            calendarDataObject = calendarSql.getObjectById(reminder.getTargetId(), reminder.getFolder());
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e);
        }

        final RecurringResultsInterface recurringResults;
        try {
            // Until is always set to 00:00:00 UTC so we have to recalculate it to get the last occurrence too.
            final long end_mod = calendarDataObject.getEndDate().getTime() % Constants.MILLI_DAY;
            Date until = null;
            until = new Date(calendarDataObject.getUntil().getTime() + end_mod + tz.getOffset(calendarDataObject.getUntil().getTime()));

            recurringResults = recColl.calculateRecurring(calendarDataObject, reminder.getDate().getTime(), until.getTime(), 0);
        } catch (final OXException e) {
            LOG.error("Can't calculate next recurrence for appointment {} in context {}", reminder.getTargetId(), session.getContextId(), e);
            return null;
        }
        if (null == recurringResults || recurringResults.size() == 0) {
            return null;
        }
        ReminderObject nextReminder = null;
        final Date now = new Date();
        for (int i = 0; i < recurringResults.size(); i++) {
            final RecurringResultInterface recurringResult = recurringResults.getRecurringResult(i);
            final Calendar calendar = Calendar.getInstance(tz);
            calendar.setTimeInMillis(recurringResult.getStart());
            calendar.add(Calendar.MINUTE, -calendarDataObject.getAlarm());
            if (calendar.getTime().after(reminder.getDate()) && calendar.getTime().after(now)) {
                nextReminder = reminder.clone();
                nextReminder.setRecurrenceAppointment(true);
                nextReminder.setRecurrencePosition(recurringResult.getPosition());
                nextReminder.setDate(calendar.getTime());
                break;
            }
        }
        return nextReminder;
    }

    /**
     * Safely deletes given reminder.
     *
     * @param reminder The reminder
     * @param userId The associated user
     * @param reminderSql The reminder SQL
     */
    protected static void deleteReminderSafe(ReminderObject reminder, int userId, ReminderService reminderSql) {
        try {
            reminderSql.deleteReminder(reminder.getTargetId(), userId, reminder.getModule());
        } catch (Exception e) {
            // Ignore
        }
    }

}
