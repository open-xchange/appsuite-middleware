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

package com.openexchange.ajax.request;

import static com.openexchange.tools.TimeZoneUtils.getTimeZone;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.parser.ReminderParser;
import com.openexchange.ajax.writer.ReminderWriter;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.ReminderService;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.reminder.ReminderExceptionCode;
import com.openexchange.groupware.reminder.ReminderHandler;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ReminderRequest} - Handles request to reminder servlet.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public final class ReminderRequest {

    private final ServerSession session;

    private final User userObj;

    private Date timestamp;

    private final AppointmentSqlFactoryService appointmentFactory;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ReminderRequest.class);

    /**
     * Gets the time stamp.
     *
     * @return The time stamp.
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Initializes a new {@link ReminderRequest}.
     *
     * @param session The session
     */
    public ReminderRequest(final ServerSession session) {
        super();
        this.session = session;
        appointmentFactory = ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class);
        userObj = session.getUser();
    }

    /**
     * Handles the request dependent on specified action string.
     *
     * @param action The action string
     * @param jsonObject The JSON object containing request's data & parameters
     * @return A JSON result object dependent on triggered action method
     * @throws OXMandatoryFieldException If a mandatory field is missing in passed JSON request object
     * @throws OXException If a server-related error occurs
     * @throws JSONException If a JSON error occurs
     * @throws SearchIteratorException If a search-iterator error occurs
     * @throws OXException If an AJAX error occurs
     * @throws OXException If a JSON error occurs
     */
    public JSONValue action(final String action, final JSONObject jsonObject) throws JSONException, OXException{
        if (action.equalsIgnoreCase(AJAXServlet.ACTION_DELETE)) {
            return actionDelete(jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATES)) {
            return actionUpdates(jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_RANGE)) {
            return actionRange(jsonObject);
        } else if (action.equalsIgnoreCase("remindAgain")) {
            return actionRemindAgain(jsonObject);
        } else {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create( action);
        }
    }

    private JSONArray actionDelete(final JSONObject jsonObject) throws JSONException, OXException, OXException, OXException {
        final JSONObject jData = DataParser.checkJSONObject(jsonObject, "data");
        final int id = DataParser.checkInt(jData, AJAXServlet.PARAMETER_ID);
        final TimeZone tz = TimeZoneUtils.getTimeZone(userObj.getTimeZone());
        final JSONArray jsonArray = new JSONArray();
        try {
            final ReminderService reminderSql = new ReminderHandler(session.getContext());
            final ReminderObject reminder = reminderSql.loadReminder(id);

            if (reminder.isRecurrenceAppointment()) {
                final ReminderObject nextReminder = getNextRecurringReminder(session, tz, reminder);
                if (nextReminder != null) {
                    reminderSql.updateReminder(nextReminder);
                    jsonArray.put(nextReminder.getObjectId());
                } else {
                    reminderSql.deleteReminder(reminder);
                }
            } else {
                reminderSql.deleteReminder(reminder);
            }
        } catch (final OXException oxe) {
            LOG.debug("", oxe);
            if (ReminderExceptionCode.NOT_FOUND.equals(oxe)) {
                jsonArray.put(id);
                return jsonArray;
            }
            throw oxe;
        }
        return jsonArray;
    }

    private JSONArray actionUpdates(final JSONObject jsonObject) throws JSONException, OXException {
        timestamp = DataParser.checkDate(jsonObject, AJAXServlet.PARAMETER_TIMESTAMP);
        final TimeZone timeZone;
        {
            final String timeZoneId = DataParser.parseString(jsonObject, AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? getTimeZone(userObj.getTimeZone()) : getTimeZone(timeZoneId);
        }

        final JSONArray jsonResponseArray = new JSONArray();
        SearchIterator<?> it = null;

        try {
            final ReminderService reminderSql = new ReminderHandler(session.getContext());
            it = reminderSql.listModifiedReminder(userObj.getId(), timestamp);

            while (it.hasNext()) {
                final ReminderWriter reminderWriter = new ReminderWriter(timeZone);
                final ReminderObject reminderObj = (ReminderObject) it.next();

                if (reminderObj.isRecurrenceAppointment()) {
                    final int targetId = reminderObj.getTargetId();
                    final int inFolder = reminderObj.getFolder();

                    // currently disabled because not used by the UI
                    // final ReminderObject latestReminder = getLatestReminder(targetId, inFolder, sessionObj, end);
                    //
                    // if (latestReminder == null) {
                    // continue;
                    // } else {
                    // reminderObj.setDate(latestReminder.getDate());
                    // reminderObj.setRecurrencePosition(latestReminder.getRecurrencePosition());
                    // }
                }

                if (hasModulePermission(reminderObj)) {
                    final JSONObject jsonReminderObj = new JSONObject();
                    reminderWriter.writeObject(reminderObj, jsonReminderObj);
                    jsonResponseArray.put(jsonReminderObj);
                }
            }

            return jsonResponseArray;
        } catch (final OXException e) {
            throw e;
        } finally {
            if (null != it) {
                it.close();
            }
        }
    }

    private JSONObject actionRemindAgain(final JSONObject jsonObject) throws JSONException, OXException {
        // timestamp = DataParser.checkDate(jsonObject, AJAXServlet.PARAMETER_TIMESTAMP);
        final int reminderId = DataParser.checkInt(jsonObject, AJAXServlet.PARAMETER_ID);
        final TimeZone tz = TimeZoneUtils.getTimeZone(userObj.getTimeZone());
        final TimeZone timeZone;
        {
            final String timeZoneId = DataParser.parseString(jsonObject, AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? tz : getTimeZone(timeZoneId);
        }
        /*
         * Parse reminder from JSON
         */
        final JSONObject jreminder = jsonObject.getJSONObject(AJAXServlet.PARAMETER_DATA);
        final ReminderObject reminder = new ReminderObject();
        new ReminderParser(tz).parse(reminder, jreminder);
        if (null == reminder.getDate()) {
            throw ReminderExceptionCode.MANDATORY_FIELD_ALARM.create();
        }
        reminder.setObjectId(reminderId);
        /*
         * Load storage version and check permission
         */
        final ReminderService reminderSql = new ReminderHandler(session.getContext());
        {
            final ReminderObject storageReminder = reminderSql.loadReminder(reminder.getObjectId());
            /*
             * Check module permission
             */
            if (!hasModulePermission(storageReminder)) {
                throw ReminderExceptionCode.UNEXPECTED_ERROR.create("No module permission.");
            }
            /*
             * Set other fields
             */
            reminder.setModule(storageReminder.getModule());
            reminder.setDescription(storageReminder.getDescription());
            reminder.setFolder(storageReminder.getFolder());
            reminder.setTargetId(storageReminder.getTargetId());
            reminder.setUser(storageReminder.getUser());
        }
        /*
         * Trigger action
         */
        reminderSql.remindAgain(reminder, session, session.getContext());
        timestamp = reminder.getLastModified();
        /*
         * Write updated reminder
         */
        final ReminderWriter reminderWriter = new ReminderWriter(timeZone);
        final JSONObject jsonReminderObj = new JSONObject();
        reminderWriter.writeObject(reminder, jsonReminderObj);
        return jsonReminderObj;
    }

    private JSONArray actionRange(final JSONObject jsonObject) throws JSONException, OXException {
        final Date end = DataParser.checkDate(jsonObject, AJAXServlet.PARAMETER_END);
        final TimeZone tz = TimeZoneUtils.getTimeZone(userObj.getTimeZone());
        final TimeZone timeZone;
        {
            final String timeZoneId = DataParser.parseString(jsonObject, AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? tz : getTimeZone(timeZoneId);
        }

        final ReminderWriter reminderWriter = new ReminderWriter(timeZone);
        try {
            final ReminderService reminderSql = new ReminderHandler(session.getContext());
            final JSONArray jsonResponseArray = new JSONArray();
            final SearchIterator<ReminderObject> it = reminderSql.getArisingReminder(session, session.getContext(), userObj, end);
            try {
                while (it.hasNext()) {
                    final ReminderObject reminder = it.next();
                    if (reminder.isRecurrenceAppointment()) {
                        try {
                            if (!getLatestRecurringReminder(session, tz, end, reminder)) {
                                final ReminderObject nextReminder = getNextRecurringReminder(session, tz, reminder);
                                if (nextReminder != null) {
                                    reminderSql.updateReminder(nextReminder);
                                } else {
                                    reminderSql.deleteReminder(reminder);
                                }
                                continue;
                            }
                        } catch (final OXException e) {
                            if (e.isGeneric(Generic.NOT_FOUND)) {
                                LOG.warn("Cannot load target object of this reminder.", e);
                                reminderSql.deleteReminder(reminder.getTargetId(), userObj.getId(), reminder.getModule());
                            } else {
                                LOG.error("Can not calculate recurrence of appointment {}{}{}", reminder.getTargetId(), ':', session.getContextId(), e);
                            }
                        }
                    }
                    if (hasModulePermission(reminder)) {
                        final JSONObject jsonReminderObj = new JSONObject();
                        reminderWriter.writeObject(reminder, jsonReminderObj);
                        jsonResponseArray.put(jsonReminderObj);
                    }
                }
            } finally {
                it.close();
            }
            return jsonResponseArray;
        } catch (final OXException e) {
            throw e;
        }
    }

    protected boolean hasModulePermission(final ReminderObject reminderObj) {
        switch (reminderObj.getModule()) {
        case Types.APPOINTMENT:
            return session.getUserPermissionBits().hasCalendar();
        case Types.TASK:
            return session.getUserPermissionBits().hasTask();
        default:
            return true;
        }
    }

    /**
     * This method returns the lastest reminder object of the recurrence appointment. The reminder object contains only the alarm attribute
     * and the recurrence position.
     *
     * @return <code>true</code> if a latest reminder was found.
     */
    protected boolean getLatestRecurringReminder(final Session sessionObj, final TimeZone tz, final Date endRange, final ReminderObject reminder) throws OXException {
        final AppointmentSQLInterface calendarSql = appointmentFactory.createAppointmentSql(sessionObj);
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
            endRange.getTime(),
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

    private static final ReminderObject getNextRecurringReminder(final Session sessionObj, final TimeZone tz, final ReminderObject reminder) throws OXException {
        final AppointmentSQLInterface calendarSql = ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class).createAppointmentSql(sessionObj);
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
            LOG.error("Can't calculate next recurrence for appointment {} in context {}", reminder.getTargetId(), sessionObj.getContextId(), e);
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
}
