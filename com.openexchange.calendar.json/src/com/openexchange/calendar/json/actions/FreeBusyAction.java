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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.FreeBusyTime;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.FreeBusyResult;
import com.openexchange.chronos.service.FreeBusyService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link FreeBusyAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@RestrictedAction(module = AppointmentAction.MODULE, type = RestrictedAction.Type.READ)
public final class FreeBusyAction extends AppointmentAction {

    private static final Set<String> REQUIRED_PARAMETERS = com.openexchange.tools.arrays.Collections.unmodifiableSet(
        AJAXServlet.PARAMETER_START, AJAXServlet.PARAMETER_END
    );

    private static final Set<String> OPTIONAL_PARAMETERS = com.openexchange.tools.arrays.Collections.unmodifiableSet(
        AJAXServlet.PARAMETER_TIMEZONE
    );

    /**
     * Initializes a new {@link FreeBusyAction}.
     *
     * @param services A service lookup reference
     */
    public FreeBusyAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected Set<String> getRequiredParameters() {
        return REQUIRED_PARAMETERS;
    }

    @Override
    protected Set<String> getOptionalParameters() {
        return OPTIONAL_PARAMETERS;
    }


    @Override
    protected AJAXRequestResult perform(CalendarSession session, AppointmentAJAXRequest request) throws OXException, JSONException {
        Date from = session.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
        Date until = session.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
        Attendee attendee = new Attendee();
        attendee.setEntity(request.checkInt(AJAXServlet.PARAMETER_ID));
        attendee.setCuType(Appointment2Event.getCalendarUserType(request.checkInt("type")));
        FreeBusyService freeBusyService = session.getFreeBusyService();
        if (null == freeBusyService) {
            throw ServiceExceptionCode.absentService(FreeBusyService.class);
        }
        FreeBusyResult freeBusyResult = freeBusyService.getFreeBusy(session, Collections.singletonList(attendee), from, until, false).get(attendee);
        return getAppointmentResultWithTimestamp(getEventConverter(session), extractEvents(freeBusyResult));
    }

    private static List<Event> extractEvents(FreeBusyResult freeBusyResult) {
        if (null == freeBusyResult || null == freeBusyResult.getFreeBusyTimes()) {
            return Collections.emptyList();
        }
        List<Event> events = new ArrayList<Event>(freeBusyResult.getFreeBusyTimes().size());
        for (FreeBusyTime freeBusyTime : freeBusyResult.getFreeBusyTimes()) {
            if (null != freeBusyTime.getEvent()) {
                events.add(freeBusyTime.getEvent());
            }
        }
        return events;
    }

}
