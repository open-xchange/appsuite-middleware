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

package com.openexchange.ajax.request;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.writer.ReminderWriter;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.OXException;
import com.openexchange.api2.ReminderSQLInterface;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarRecurringCollection;
import com.openexchange.groupware.calendar.CalendarReminderDelete;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.calendar.OXCalendarException;
import com.openexchange.groupware.calendar.RecurringResult;
import com.openexchange.groupware.calendar.RecurringResults;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.reminder.EmptyReminderDeleteImpl;
import com.openexchange.groupware.reminder.ReminderDeleteInterface;
import com.openexchange.groupware.reminder.ReminderHandler;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.tasks.ModifyThroughDependant;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;

/**
 * {@link ReminderRequest} - Handles request to reminder servlet.
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public final class ReminderRequest {

    private final Session sessionObj;

    private final Context ctx;

    private final User userObj;

    private Date timestamp;

    private static final Log LOG = LogFactory.getLog(ReminderRequest.class);

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
     * @param ctx The context
     */
    public ReminderRequest(final Session session, final Context ctx) {
        super();
        this.sessionObj = session;
        this.ctx = ctx;
        userObj = UserStorage.getStorageUser(session.getUserId(), ctx);
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
     * @throws AjaxException If an AJAX error occurs
     * @throws OXJSONException If a JSON error occurs
     */
    public JSONValue action(final String action, final JSONObject jsonObject) throws OXMandatoryFieldException, OXException, JSONException, SearchIteratorException, AjaxException, OXJSONException {
        if (action.equalsIgnoreCase(AJAXServlet.ACTION_DELETE)) {
            return actionDelete(jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATES)) {
            return actionUpdates(jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_RANGE)) {
            return actionRange(jsonObject);
        } else {
            throw new AjaxException(AjaxException.Code.UnknownAction, action);
        }
    }

    private JSONArray actionDelete(final JSONObject jsonObject) throws JSONException, OXException, OXJSONException, AjaxException {
        final JSONObject jData = DataParser.checkJSONObject(jsonObject, "data");
        final int id = DataParser.checkInt(jData, AJAXServlet.PARAMETER_ID);
        final TimeZone tz = TimeZone.getTimeZone(userObj.getTimeZone());
        final JSONArray jsonArray = new JSONArray();
        try {
            final Context ctx = ContextStorage.getInstance().getContext(sessionObj.getContextId());
            final ReminderSQLInterface reminderSql = new ReminderHandler(ctx);

            final ReminderObject reminder = reminderSql.loadReminder(id);
            final ReminderDeleteInterface reminderDeleteInterface;
            final int module = reminder.getModule();
            switch (module) {
            case Types.APPOINTMENT:
                reminderDeleteInterface = new CalendarReminderDelete();
                break;
            case Types.TASK:
                reminderDeleteInterface = new ModifyThroughDependant();
                break;
            default:
                reminderDeleteInterface = new EmptyReminderDeleteImpl();
            }
            reminderSql.setReminderDeleteInterface(reminderDeleteInterface);

            if (reminder.isRecurrenceAppointment()) {
                final ReminderObject nextReminder = getNextRecurringReminder(sessionObj, tz, reminder);
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
            LOG.debug(oxe.getMessage(), oxe);
            if (oxe.getComponent().equals(EnumComponent.REMINDER) && oxe.getDetailNumber() == 9) {
                jsonArray.put(id);
                return jsonArray;
            }
            throw oxe;
        } catch (final AbstractOXException exc) {
            throw new OXException(exc);
        }
        return jsonArray;
    }

    private JSONArray actionUpdates(final JSONObject jsonObject) throws OXMandatoryFieldException, JSONException, OXException, SearchIteratorException, OXJSONException, AjaxException {
        timestamp = DataParser.checkDate(jsonObject, AJAXServlet.PARAMETER_TIMESTAMP);

        final JSONArray jsonResponseArray = new JSONArray();
        SearchIterator it = null;

        try {
            final Context ctx = ContextStorage.getInstance().getContext(sessionObj.getContextId());
            final ReminderSQLInterface reminderSql = new ReminderHandler(ctx);
            it = reminderSql.listModifiedReminder(userObj.getId(), timestamp);

            while (it.hasNext()) {
                final ReminderWriter reminderWriter = new ReminderWriter(TimeZone.getTimeZone(userObj.getTimeZone()));
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
        } catch (final AbstractOXException e) {
            throw new OXException(e);
        } finally {
            if (null != it) {
                it.close();
            }
        }
    }

    private JSONArray actionRange(final JSONObject jsonObject) throws OXMandatoryFieldException, JSONException, OXException, OXJSONException, AjaxException {
        final Date end = DataParser.checkDate(jsonObject, AJAXServlet.PARAMETER_END);
        final TimeZone tz = TimeZone.getTimeZone(userObj.getTimeZone());
        final ReminderWriter reminderWriter = new ReminderWriter(tz);
        try {
            final Context ctx = ContextStorage.getInstance().getContext(sessionObj.getContextId());

            final ReminderSQLInterface reminderSql = new ReminderHandler(ctx);
            final JSONArray jsonResponseArray = new JSONArray();
            final SearchIterator<ReminderObject> it = reminderSql.listReminder(userObj.getId(), end);
            try {
                while (it.hasNext()) {
                    final ReminderObject reminder = it.next();
                    if (reminder.isRecurrenceAppointment()) {
                        try {
                            if (!getLatestRecurringReminder(sessionObj, tz, end, reminder)) {
                                final ReminderObject nextReminder = getNextRecurringReminder(sessionObj, tz, reminder);
                                if (nextReminder != null) {
                                    reminderSql.updateReminder(nextReminder);
                                } else {
                                    reminderSql.deleteReminder(reminder);
                                }
                                continue;
                            }
                        } catch (final OXObjectNotFoundException e) {
                            LOG.warn("Cannot load target object of this reminder.", e);
                            reminderSql.deleteReminder(reminder.getTargetId(), userObj.getId(), reminder.getModule());
                        } catch (final OXException e) {
                            LOG.error(
                                "Can not calculate recurrence of appointment " + reminder.getTargetId() + ':' + sessionObj.getContextId(),
                                e);
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
        } catch (final AbstractOXException e) {
            throw new OXException(e);
        }
    }

    protected boolean hasModulePermission(final ReminderObject reminderObj) {
        switch (reminderObj.getModule()) {
        case Types.APPOINTMENT:
            return UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(), ctx).hasCalendar();
        case Types.TASK:
            return UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(), ctx).hasTask();
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
        final CalendarSql calendarSql = new CalendarSql(sessionObj);
        final CalendarDataObject calendarDataObject;
        try {
            calendarDataObject = calendarSql.getObjectById(reminder.getTargetId(), reminder.getFolder());
        } catch (final SQLException e) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, e);
        }

        final Calendar calendar = Calendar.getInstance(tz);
        calendar.add(Calendar.MONTH, -3);

        final RecurringResults recurringResults = CalendarRecurringCollection.calculateRecurring(
            calendarDataObject,
            calendar.getTimeInMillis(),
            endRange.getTime(),
            0);
        boolean retval = false;
        if (recurringResults != null && recurringResults.size() > 0) {
            final RecurringResult recurringResult = recurringResults.getRecurringResult(recurringResults.size() - 1);
            calendar.setTimeInMillis(recurringResult.getStart());
            calendar.add(Calendar.MINUTE, -calendarDataObject.getAlarm());
            if (calendar.getTimeInMillis() >= reminder.getDate().getTime()) {
                reminder.setDate(calendar.getTime());
                reminder.setRecurrencePosition(recurringResult.getPosition());
                retval = true;
            }
        }
        return retval;
    }

    private static final ReminderObject getNextRecurringReminder(final Session sessionObj, final TimeZone tz, final ReminderObject reminder) throws OXException {
        final CalendarSql calendarSql = new CalendarSql(sessionObj);
        final CalendarDataObject calendarDataObject;
        try {
            calendarDataObject = calendarSql.getObjectById(reminder.getTargetId(), reminder.getFolder());
        } catch (final SQLException e) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, e);
        }
        final RecurringResults recurringResults;
        try {
            recurringResults = CalendarRecurringCollection.calculateRecurring(
                calendarDataObject,
                reminder.getDate().getTime(),
                calendarDataObject.getUntil().getTime(),
                0);
        } catch (final OXException e) {
            LOG.error(
                "Can't calculate next recurrence for appointment " + reminder.getTargetId() + " in context " + sessionObj.getContextId(),
                e);
            return null;
        }
        if (null == recurringResults || recurringResults.size() == 0) {
            return null;
        }
        ReminderObject nextReminder = null;
        final Date now = new Date();
        for (int i = 0; i < recurringResults.size(); i++) {
            final RecurringResult recurringResult = recurringResults.getRecurringResult(i);
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
