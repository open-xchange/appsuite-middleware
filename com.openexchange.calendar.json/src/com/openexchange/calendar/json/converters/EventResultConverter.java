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

package com.openexchange.calendar.json.converters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.calendar.json.actions.chronos.DefaultEventConverter;
import com.openexchange.calendar.json.actions.chronos.EventConverter;
import com.openexchange.calendar.json.compat.Appointment;
import com.openexchange.calendar.json.compat.CalendarDataObject;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link EventResultConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventResultConverter extends AbstractCalendarJSONResultConverter {

    private final ServiceLookup services;
    private final AppointmentResultConverter delegate;

    /**
     * Initializes a new {@link EventResultConverter}.
     */
    public EventResultConverter(ServiceLookup services) {
        super();
        this.services = services;
        this.delegate = new AppointmentResultConverter(services);
    }

    @Override
    public String getInputFormat() {
        return "eventDocument";
    }

    @Override
    protected void convertCalendar(AppointmentAJAXRequest request, AJAXRequestResult result, ServerSession session, Converter converter, TimeZone userTimeZone) throws OXException {
        Object resultObject = result.getResultObject();
        if (null == resultObject) {
            return;
        }
        CalendarSession calendarSession = services.getServiceSafe(CalendarService.class).init(request.getSession());
        EventConverter eventConverter = new DefaultEventConverter(services, calendarSession);
        if (Event.class.isInstance(resultObject)) {
            CalendarDataObject appointment = eventConverter.getAppointment((Event) resultObject);
            result.setResultObject(appointment, delegate.getInputFormat());
            delegate.convertCalendar(request, result, session, converter, userTimeZone);
        } else if (Collections.class.isInstance(resultObject)) {
            Collection<?> collection = (Collection<?>) resultObject;
            List<Appointment> appointments = new ArrayList<Appointment>(collection.size());
            for (Object object : collection) {
                appointments.add(eventConverter.getAppointment((Event) object));
            }
            delegate.convert(appointments, request, result, userTimeZone);
        } else {
            throw new UnsupportedOperationException();
        }
    }

}
