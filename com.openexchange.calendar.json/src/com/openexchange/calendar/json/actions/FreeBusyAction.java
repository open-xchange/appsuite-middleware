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

package com.openexchange.calendar.json.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.FreeBusyTime;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.FreeBusyResult;
import com.openexchange.chronos.service.FreeBusyService;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link FreeBusyAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@OAuthAction(AppointmentActionFactory.OAUTH_READ_SCOPE)
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
