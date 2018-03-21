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
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.writer.ReminderWriter;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.compat.Event2Appointment;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.reminder.ReminderService;
import com.openexchange.groupware.reminder.json.ReminderAJAXRequest;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractReminderAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractReminderAction implements AJAXActionService {

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
     * Checks if session-associated user has appropriate module access granted.
     *
     * @param reminder The reminder
     * @param session The associated session
     * @return <code>true</code> if module permission is granted; otherwise <code>false</code>
     */
    protected static boolean hasModulePermission(final ReminderObject reminder, final ServerSession session) {
        switch (reminder.getModule()) {
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
            case Types.TASK:
                final UserParticipant[] userParticipants = new TasksSQLImpl(session).getTaskById(reminder.getTargetId(), reminder.getFolder()).getUsers();
                if (null != userParticipants) {
                    final int userId = session.getUserId();
                    for (final UserParticipant userParticipant : userParticipants) {
                        if (userParticipant.getIdentifier() == userId) {
                            return userParticipant.getConfirm() != Task.DECLINE;
                        }
                    }
                }
                break;
            default:
                return true;
        }
        return true;
    }

    /**
     * Safely deletes given reminder.
     *
     * @param reminder The reminder
     * @param userId The associated user
     * @param reminderSql The reminder SQL
     */
    protected static void deleteReminderSafe(Session session, ReminderObject reminder, int userId, ReminderService reminderService) {
        try {
            reminderService.deleteReminder(session, reminder.getTargetId(), userId, reminder.getModule());
        } catch (Exception e) {
            // Ignore
        }
    }

    protected void convertAlarmTrigger2Reminder(CalendarSession calendarSession, AlarmTrigger trigger, ReminderWriter reminderWriter, JSONArray jsonResponseArray) throws OXException, JSONException {
        ReminderObject reminder = new ReminderObject();
        reminder.setDate(new Date(trigger.getTime()));
        EventID eventId = null;
        if(trigger.containsRecurrenceId()){
            eventId = new EventID(trigger.getFolder(), trigger.getEventId(), trigger.getRecurrenceId());
        } else {
            eventId = new EventID(trigger.getFolder(), trigger.getEventId());
        }
        Event event = calendarSession.getCalendarService().getEvent(calendarSession, trigger.getFolder(), eventId);
        reminder.setLastModified(event.getLastModified());
        reminder.setFolder(Integer.valueOf(trigger.getFolder()));
        reminder.setModule(Types.APPOINTMENT);
        reminder.setUser(trigger.getUserId());
        reminder.setObjectId(trigger.getAlarm()); // Store the alarm id instead of the reminder id
        reminder.setTargetId(Integer.valueOf(event.getId()));

        if (CalendarUtils.isSeriesMaster(event) && null != eventId.getRecurrenceID()) {
            int pos = Event2Appointment.getRecurrencePosition(calendarSession.getRecurrenceService(), new DefaultRecurrenceData(event), eventId.getRecurrenceID());
            reminder.setRecurrencePosition(pos);
            reminder.setRecurrenceAppointment(true);
        } else {
            reminder.setRecurrenceAppointment(false);
        }

        List<Alarm> alarms = event.getAlarms();
        if (null != alarms) {
            for (Alarm alarm : alarms) {
                if (alarm.getId() == trigger.getAlarm().intValue()) {
                    reminder.setDescription(alarm.getDescription());
                    break;
                }
            }
        }

        JSONObject jsonReminderObj = new JSONObject(12);
        reminderWriter.writeObject(reminder, jsonReminderObj);
        jsonResponseArray.put(jsonReminderObj);
    }

}
