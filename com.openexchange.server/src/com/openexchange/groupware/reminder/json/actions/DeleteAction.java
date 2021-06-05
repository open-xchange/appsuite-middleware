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

import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarServiceUtilities;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.reminder.ReminderExceptionCode;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.reminder.ReminderService;
import com.openexchange.groupware.reminder.json.ReminderAJAXRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link DeleteAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractReminderAction.MODULE, type = RestrictedAction.Type.WRITE)
public final class DeleteAction extends AbstractReminderAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DeleteAction.class);

    /**
     * Initializes a new {@link DeleteAction}.
     *
     * @param services
     */
    public DeleteAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final ReminderAJAXRequest req) throws OXException, JSONException {
        final JSONArray response = new JSONArray();
        if (req.getData() instanceof JSONObject) {

            final JSONObject jData = req.getData();
            final long longId = DataParser.checkLong(jData, AJAXServlet.PARAMETER_ID);

            if (longId > Integer.MAX_VALUE) {
                try {
                    // reminder is an event reminder
                    int alarmId = (int) (longId >> 32);
                    int eventIdInt = (int) longId;
                    CalendarService calendarService = ServerServiceRegistry.getInstance().getService(CalendarService.class);
                    CalendarSession calendarSession = calendarService.init(req.getSession());
                    CalendarServiceUtilities calendarServiceUtilities = calendarService.getUtilities();
                    Event event = calendarServiceUtilities.resolveByID(calendarSession, String.valueOf(eventIdInt), null);
                    List<Alarm> alarms = event.getAlarms();
                    for (Alarm alarm : alarms) {
                        if (alarm.getId() == alarmId) {
                            alarm.setAcknowledged(new Date());
                        }
                    }
                    EventID eventId = null;
                    if (event.containsRecurrenceId()) {
                        eventId = new EventID(event.getFolderId(), event.getId(), event.getRecurrenceId());
                    } else {
                        eventId = new EventID(event.getFolderId(), event.getId());
                    }
                    CalendarResult updateAlarms = calendarService.updateAlarms(calendarSession, eventId, alarms, event.getTimestamp());
                    if (updateAlarms.getUpdates() != null && updateAlarms.getUpdates().size() == 1) {
                        response.put(longId);
                    }
                } catch (OXException oxe) {
                    LOG.debug("", oxe);
                    // TODO Eventually wrap the OXException into a reminder exception ?
                    throw oxe;
                }
            } else {

                final int id = (int) longId;
                try {
                    final ReminderService reminderService = ServerServiceRegistry.getInstance().getService(ReminderService.class, true);
                    final ReminderObject reminder = reminderService.loadReminder(req.getSession(), id);
                    reminderService.deleteReminder(req.getSession(), reminder);
                } catch (OXException oxe) {
                    LOG.debug("", oxe);
                    if (ReminderExceptionCode.NOT_FOUND.equals(oxe)) {
                        response.put(id);
                        return new AJAXRequestResult(response, "json");
                    }
                    throw oxe;
                }
            }
        } else {
            JSONArray jsonArray = req.getData();
            for (int i = 0; i < jsonArray.length(); i++) {
                final JSONObject jData = jsonArray.getJSONObject(i);
                final long longId = DataParser.checkLong(jData, AJAXServlet.PARAMETER_ID);

                if (longId > Integer.MAX_VALUE) {
                    try {
                        // reminder is an event reminder
                        int eventIdInt = (int) (longId >> 32);
                        int alarmId = (int) longId;
                        CalendarService calendarService = ServerServiceRegistry.getInstance().getService(CalendarService.class);
                        CalendarSession calendarSession = calendarService.init(req.getSession());
                        CalendarServiceUtilities calendarServiceUtilities = calendarService.getUtilities();
                        Event event = calendarServiceUtilities.resolveByID(calendarSession, String.valueOf(eventIdInt), null);
                        List<Alarm> alarms = event.getAlarms();
                        for (Alarm alarm : alarms) {
                            if (alarm.getId() == alarmId) {
                                alarm.setAcknowledged(new Date());
                            }
                        }
                        EventID eventId = null;
                        if (event.containsRecurrenceId()) {
                            eventId = new EventID(event.getFolderId(), event.getId(), event.getRecurrenceId());
                        } else {
                            eventId = new EventID(event.getFolderId(), event.getId());
                        }
                        CalendarResult updateAlarms = calendarService.updateAlarms(calendarSession, eventId, alarms, event.getTimestamp());
                        if (updateAlarms.getUpdates() != null && updateAlarms.getUpdates().size() == 1) {
                            response.put(longId);
                        }
                    } catch (OXException oxe) {
                        LOG.debug("", oxe);
                        // TODO Eventually wrap the OXException into a reminder exception ?
                        throw oxe;
                    }
                } else {
                    final int id = (int) longId;
                    try {
                        final ReminderService reminderService = ServerServiceRegistry.getInstance().getService(ReminderService.class, true);
                        final ReminderObject reminder = reminderService.loadReminder(req.getSession(), id);
                        reminderService.deleteReminder(req.getSession(), reminder);
                    } catch (OXException oxe) {
                        LOG.debug("", oxe);
                        if (ReminderExceptionCode.NOT_FOUND.equals(oxe)) {
                            response.put(id);
                            return new AJAXRequestResult(response, "json");
                        }
                        throw oxe;
                    }
                }
            }
        }
        return new AJAXRequestResult(response, "json");
    }

}
