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
 *    trademarks of the OX Software GmbH. group of companies.
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

import static com.openexchange.tools.TimeZoneUtils.getTimeZone;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.calendar.json.actions.chronos.ChronosAction;
import com.openexchange.chronos.CalendarParameters;
import com.openexchange.chronos.CalendarService;
import com.openexchange.chronos.CalendarSession;
import com.openexchange.chronos.UserizedEvent;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * {@link NewAppointmentsSearchAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@Action(method = RequestMethod.GET, name = "newappointments", description = "Get new appointments.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "columns", description = "The requested fields."),
    @Parameter(name = "start", description = "Lower inclusive limit of the queried range as a Date. Only appointments which end on or after this date are returned."),
    @Parameter(name = "end", description = "Upper exclusive limit of the queried range as a Date. Only appointments which start before this date are returned."),
    @Parameter(name = "sort", optional=true, description = "The identifier of a column which determines the sort order of the response. If this parameter is specified and holds a column number, then the parameter order must be also specified."),
    @Parameter(name = "order", optional=true, description = "\"asc\" if the response entires should be sorted in the ascending order, \"desc\" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified."),
    @Parameter(name = "limit", description = "limits the number of returned object to the given value.")
}, responseDescription = "An array with appointment data. Each array element describes one appointment and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter.")
@OAuthAction(AppointmentActionFactory.OAUTH_READ_SCOPE)
public final class NewAppointmentsSearchAction extends ChronosAction {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(NewAppointmentsSearchAction.class);

    /**
     * Initializes a new {@link NewAppointmentsSearchAction}.
     * @param services
     */
    public NewAppointmentsSearchAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final AppointmentAJAXRequest req) throws OXException, JSONException {
        final TimeZone timeZone;
        {
            final String timeZoneId = req.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? req.getTimeZone() : getTimeZone(timeZoneId);
        }
        final Date start = req.checkTime(AJAXServlet.PARAMETER_START, timeZone);
        final Date end = req.checkTime(AJAXServlet.PARAMETER_END, timeZone);

        final Date startUTC = req.checkDate(AJAXServlet.PARAMETER_START);
        final Date endUTC = req.checkDate(AJAXServlet.PARAMETER_END);

        int orderBy = req.optInt(AJAXServlet.PARAMETER_SORT);

        if (orderBy == 0) {
            orderBy = CalendarObject.START_DATE;
        }

        String orderDirString = req.getParameter(AJAXServlet.PARAMETER_ORDER);
        if (orderDirString == null) {
            orderDirString = "asc";
        }
        final Order orderDir = OrderFields.parse(orderDirString);

        final int limit = req.checkInt("limit");

        Date timestamp = new Date(0);

        final AppointmentSearchObject searchObj = new AppointmentSearchObject();
        searchObj.setMinimumEndDate(start);
        searchObj.setMaximumStartDate(end);
        searchObj.setUserIDs(Collections.singleton(Integer.valueOf(req.getSession().getUserId())));
        searchObj.setOnlyPrivateAppointments(true);

        final LinkedList<Appointment> linkedAppointmentList = new LinkedList<Appointment>();


        SearchIterator<Appointment> searchIterator = null;
        try {
            final AppointmentSqlFactoryService factoryService = getService();
            if (null == factoryService) {
                throw ServiceExceptionCode.absentService(AppointmentSqlFactoryService.class);
            }
            final AppointmentSQLInterface appointmentsql = factoryService.createAppointmentSql(req.getSession());
            final CalendarCollectionService recColl = getService(CalendarCollectionService.class);
            searchIterator = appointmentsql.searchAppointments(searchObj, orderBy, orderDir, _appointmentFields);

            final List<Appointment> appointmentList = new ArrayList<Appointment>();
            while (searchIterator.hasNext()) {
                final Appointment appointmentobject = searchIterator.next();
                boolean processed = false;
                if (appointmentobject.getRecurrenceType() != CalendarObject.NONE && appointmentobject.getRecurrencePosition() == 0) {
                    // Commented this because this is done in CalendarOperation.next():726 that calls extractRecurringInformation()
                    // appointmentobject.calculateRecurrence();
                    RecurringResultsInterface recuResults = null;
                    try {
                        recuResults = recColl.calculateRecurring(appointmentobject, startUTC.getTime(), endUTC.getTime(), 0);
                        processed = true;
                    } catch (final OXException x) {
                        LOG.error("Can not calculate recurrence {}:{}", appointmentobject.getObjectID(), req.getSession().getContextId(), x);
                    }
                    if (recuResults != null && recuResults.size() > 0) {
                        final RecurringResultInterface result = recuResults.getRecurringResult(0);
                        appointmentobject.setStartDate(new Date(result.getStart()));
                        appointmentobject.setEndDate(new Date(result.getEnd()));
                        appointmentobject.setRecurrencePosition(result.getPosition());

                        if (appointmentobject.getFullTime()) {
                            if (recColl.inBetween(appointmentobject.getStartDate().getTime(), appointmentobject.getEndDate().getTime(), startUTC.getTime(), endUTC.getTime())) {
                                compareStartDateForList(linkedAppointmentList, appointmentobject, limit);
                            }
                        } else {
                            compareStartDateForList(linkedAppointmentList, appointmentobject, limit);
                        }
                    }
                }
                if (!processed) {
                    if (appointmentobject.getFullTime() && (startUTC != null && endUTC != null)) {
                        if (recColl.inBetween(appointmentobject.getStartDate().getTime(), appointmentobject.getEndDate().getTime(), startUTC.getTime(), endUTC.getTime())) {
                            compareStartDateForList(linkedAppointmentList, appointmentobject, limit);
                        }
                    } else {
                        compareStartDateForList(linkedAppointmentList, appointmentobject, limit);
                    }
                }

                if (timestamp.before(appointmentobject.getLastModified())) {
                    timestamp = appointmentobject.getLastModified();
                }
            }

            for (int a = 0; a < linkedAppointmentList.size(); a++) {
                final Appointment appointmentObj = linkedAppointmentList.get(a);
                if (appointmentObj.getFullTime()) {
                    checkAndAddAppointment(appointmentList, appointmentObj, startUTC, endUTC, recColl);
                } else {
                    appointmentList.add(appointmentObj);
                }
            }

            return new AJAXRequestResult(appointmentList, timestamp, "appointment");
        } finally {
            if (searchIterator != null) {
                searchIterator.close();
            }
        }
    }

    private static final String[] REQUIRED_PARAMETERS = { 
        CalendarParameters.PARAMETER_RANGE_START, CalendarParameters.PARAMETER_RANGE_END
    };

    @Override
    protected AJAXRequestResult perform(CalendarService calendarService, AppointmentAJAXRequest request) throws OXException, JSONException {
        CalendarSession calendarSession = initSession(request, REQUIRED_PARAMETERS);
        List<UserizedEvent> events = calendarService.getEventsOfUser(calendarSession);
        return getAppointmentResultWithTimestamp(events);
    }

}
