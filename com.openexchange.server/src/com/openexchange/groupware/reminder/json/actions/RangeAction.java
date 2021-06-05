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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.ajax.writer.ReminderWriter;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.reminder.ReminderService;
import com.openexchange.groupware.reminder.json.ReminderAJAXRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;


/**
 * {@link RangeAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractReminderAction.MODULE, type = RestrictedAction.Type.READ)
public final class RangeAction extends AbstractReminderAction {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(RangeAction.class);

    /**
     * Initializes a new {@link RangeAction}.
     * @param services
     */
    public RangeAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final ReminderAJAXRequest req) throws OXException, JSONException {
        final Date end = req.checkDate(AJAXServlet.PARAMETER_END);
        final TimeZone tz = req.getTimeZone();
        final TimeZone timeZone;
        {
            final String timeZoneId = req.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? tz : getTimeZone(timeZoneId);
        }


        final ReminderWriter reminderWriter = new ReminderWriter(timeZone);
        try {
            final ServerSession session = req.getSession();
            final ReminderService reminderService = ServerServiceRegistry.getInstance().getService(ReminderService.class, true);
            final User user = session.getUser();
            final List<ReminderObject> reminders = reminderService.getArisingReminder(session, session.getContext(), user, end);
            final JSONArray jsonResponseArray = new JSONArray();
            for (ReminderObject reminder : reminders) {
                try {
                    if (isRequested(req, reminder) && hasModulePermission(reminder, session) && stillAccepted(reminder, session)) {
                        final JSONObject jsonReminderObj = new JSONObject(12);
                        reminderWriter.writeObject(reminder, jsonReminderObj);
                        jsonResponseArray.put(jsonReminderObj);
                    }
                } catch (OXException e) {
                    if (!OXFolderExceptionCode.NOT_EXISTS.equals(e)) {
                        throw e;
                    }
                    LOG.warn("Cannot load target object of this reminder.", e);
                    deleteReminderSafe(req.getSession(), reminder, user.getId(), reminderService);
                }
            }

            if (req.getOptModules() == null || req.getOptModules().isEmpty() || req.getOptModules().contains(I(Types.APPOINTMENT))) {
                addEventReminder(session, jsonResponseArray, reminderWriter, end);
            }

            return new AJAXRequestResult(jsonResponseArray, "json");
        } catch (OXException e) {
            throw e;
        }
    }

    /**
     * Adds event reminder to the response array
     *
     * @param session The user session
     * @param jsonResponseArray The response array
     * @param reminderWriter The {@link ReminderWriter} to use
     * @param until The upper limit of query
     * @throws JSONException
     * @throws OXException
     */
    private void addEventReminder(Session session, JSONArray jsonResponseArray, ReminderWriter reminderWriter, Date until) throws JSONException, OXException {
        CalendarService calService = ServerServiceRegistry.getInstance().getService(CalendarService.class);
        CalendarSession calSession = calService.init(session);
        calSession.set(CalendarParameters.PARAMETER_RANGE_END, until);

        List<AlarmTrigger> alarmTrigger = calService.getAlarmTriggers(calSession, Collections.singleton("DISPLAY"));
        for (AlarmTrigger trigger : alarmTrigger) {
            convertAlarmTrigger2Reminder(calSession, trigger, reminderWriter, jsonResponseArray);
        }
    }

}
