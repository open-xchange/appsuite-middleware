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

package com.openexchange.calendar.json.actions;

import java.util.Date;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.calendar.json.compat.AppointmentParser;
import com.openexchange.calendar.json.compat.CalendarDataObject;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.SchedulingControl;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link NewAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@RestrictedAction(module = AppointmentAction.MODULE, type = RestrictedAction.Type.WRITE)
public final class NewAction extends AppointmentAction {

    private static final Set<String> OPTIONAL_PARAMETERS = com.openexchange.tools.arrays.Collections.unmodifiableSet(AJAXServlet.PARAMETER_TIMEZONE);

    /**
     * Initializes a new {@link NewAction}.
     *
     * @param services A service lookup reference
     */
    public NewAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected Set<String> getOptionalParameters() {
        return OPTIONAL_PARAMETERS;
    }

    @Override
    protected AJAXRequestResult perform(CalendarSession session, AppointmentAJAXRequest request) throws OXException, JSONException {
        JSONObject jsonObject = request.getData();
        CalendarDataObject appointment = new CalendarDataObject();
        appointment.setContext(request.getSession().getContext());
        new AppointmentParser(request.getTimeZone()).parse(appointment, jsonObject);
        if (false == appointment.containsParentFolderID()) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(AJAXServlet.PARAMETER_FOLDERID);
        }
        if (appointment.containsNotification()) {

            session.set(CalendarParameters.PARAMETER_SCHEDULING, appointment.getNotification() ? SchedulingControl.ALL : SchedulingControl.NONE);
        }
        if (false == appointment.getIgnoreConflicts()) {
            session.set(CalendarParameters.PARAMETER_CHECK_CONFLICTS, Boolean.TRUE);
        }
        if (false == session.contains(CalendarParameters.PARAMETER_TRACK_ATTENDEE_USAGE)) {
            session.set(CalendarParameters.PARAMETER_TRACK_ATTENDEE_USAGE, Boolean.TRUE);
        }
        Event event = getEventConverter(session).getEvent(appointment, null);
        String folderID = String.valueOf(appointment.getParentFolderID());
        CalendarResult result;
        try {
            result = session.getCalendarService().createEvent(session, folderID, event);
        } catch (OXException e) {
            if (CalendarExceptionCodes.EVENT_CONFLICTS.equals(e) || CalendarExceptionCodes.HARD_EVENT_CONFLICTS.equals(e)) {
                return getAppointmentConflictResult(getEventConverter(session), CalendarUtils.extractEventConflicts(e));
            }
            throw e;
        }
        JSONObject resultObject = new JSONObject(1);
        if (0 < result.getCreations().size()) {
            resultObject.put(DataFields.ID, result.getCreations().get(0).getCreatedEvent().getId());
        }
        return new AJAXRequestResult(resultObject, new Date(result.getTimestamp()), "json");
    }

}
