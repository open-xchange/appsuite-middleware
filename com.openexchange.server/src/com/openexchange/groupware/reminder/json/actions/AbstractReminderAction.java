/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.groupware.reminder.json.actions;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.TimeZoneUtils.getTimeZone;
import java.util.ArrayList;
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
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
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
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.reminder.ReminderService;
import com.openexchange.groupware.reminder.json.ReminderAJAXRequest;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractReminderAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractReminderAction.MODULE, type = RestrictedAction.Type.READ)  // Minimum reminders requirement
public abstract class AbstractReminderAction implements AJAXActionService {

    private static final AJAXRequestResult RESULT_JSON_NULL = new AJAXRequestResult(JSONObject.NULL, "json");

    private static final String MODULES_PARAMETER = "modules";

    protected static final String MODULE = "reminder";

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
            final ReminderAJAXRequest reminderRequest = new ReminderAJAXRequest(requestData, session, getModules(requestData));
            final String sTimeZone = requestData.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
            if (null != sTimeZone) {
                reminderRequest.setTimeZone(getTimeZone(sTimeZone));
            }
            return perform(reminderRequest);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private List<Integer> getModules(AJAXRequestData req) throws OXException {
        String parameter = req.getParameter(MODULES_PARAMETER);
        if (Strings.isEmpty(parameter)) {
            return null;
        }
        String[] splitByCommaNotInQuotes = Strings.splitByCommaNotInQuotes(parameter);
        List<Integer> result = new ArrayList<>(splitByCommaNotInQuotes.length);
        for (String str : splitByCommaNotInQuotes) {
            try {
                int module = Integer.parseInt(str);
                if (module >= 0) {
                    result.add(I(module));
                }
            } catch (@SuppressWarnings("unused") NumberFormatException e) {
                int module = AJAXServlet.getModuleInteger(str);
                if (module >= 0) {
                    int typesConstant = getTypesConstant(module);
                    if (typesConstant >= 0) {
                        result.add(I(typesConstant));
                        continue;
                    }
                }
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(MODULES_PARAMETER, parameter);
            }
        }
        return result.isEmpty() ? null : result;
    }

    /**
     * Translates a FolderObject value to a Types value.
     */
    public static int getTypesConstant(final int folderObjectConstant) {
        switch (folderObjectConstant) {
            case FolderObject.CONTACT:
                return Types.CONTACT;
            case FolderObject.INFOSTORE:
                return Types.INFOSTORE;
            case FolderObject.MAIL:
                return Types.EMAIL;
            case FolderObject.TASK:
                return Types.TASK;
            case FolderObject.CALENDAR:
                return Types.APPOINTMENT;
            default:
                return -1;
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
        } catch (@SuppressWarnings("unused") Exception e) {
            // Ignore
        }
    }

    protected void convertAlarmTrigger2Reminder(CalendarSession calendarSession, AlarmTrigger trigger, ReminderWriter reminderWriter, JSONArray jsonResponseArray) throws OXException, JSONException {
        ReminderObject reminder = new ReminderObject();
        reminder.setDate(new Date(trigger.getTime().longValue()));
        EventID eventId = null;
        if (trigger.containsRecurrenceId()) {
            eventId = new EventID(trigger.getFolder(), trigger.getEventId(), trigger.getRecurrenceId());
        } else {
            eventId = new EventID(trigger.getFolder(), trigger.getEventId());
        }
        Event event = calendarSession.getCalendarService().getEvent(calendarSession, trigger.getFolder(), eventId);
        reminder.setLastModified(event.getLastModified());
        reminder.setFolder(Integer.parseInt(trigger.getFolder()));
        reminder.setModule(Types.APPOINTMENT);
        reminder.setUser(trigger.getUserId().intValue());
        reminder.setObjectId(trigger.getAlarm().intValue()); // Store the alarm id instead of the reminder id
        reminder.setTargetId(Integer.parseInt(event.getId()));

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

    /**
     * @param req The {@link ReminderAJAXRequest}
     * @param reminder The {@link ReminderObject}
     * @return true if the reminder type is requested, false otherwise
     */
    protected boolean isRequested(ReminderAJAXRequest req, ReminderObject reminder) {
        List<Integer> optModules = req.getOptModules();
        if (optModules == null || optModules.isEmpty()) {
            return true;
        }
        for (int module : optModules) {
            if (reminder.getModule() == module) {
                return true;
            }
        }
        return false;
    }

}
