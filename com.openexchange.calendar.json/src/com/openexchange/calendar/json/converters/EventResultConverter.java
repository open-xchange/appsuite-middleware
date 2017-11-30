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
