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
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.fields.SearchFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.calendar.json.actions.chronos.ChronosAction;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.UserizedEvent;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.collections.PropertizedList;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * {@link SearchAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@Action(method = RequestMethod.PUT, name = "search", description = "Search appointments.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "columns", description = "The requested fields.")
}, requestBody = "An Object as described in Search appointments.",
responseDescription = "An array with appointment data. Each array element describes one contact and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter.")
@OAuthAction(AppointmentActionFactory.OAUTH_READ_SCOPE)
public final class SearchAction extends ChronosAction {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(SearchAction.class);

    /**
     * Initializes a new {@link SearchAction}.
     * @param services
     */
    public SearchAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final AppointmentAJAXRequest req) throws OXException, JSONException {
        Date timestamp = new Date(0);

        final JSONObject jData = req.getData();
        final AppointmentSearchObject searchObj = new AppointmentSearchObject();

        if (jData.has(AJAXServlet.PARAMETER_INFOLDER)) {
            final int inFolder = DataParser.parseInt(jData, AJAXServlet.PARAMETER_INFOLDER);
            searchObj.setFolderIDs(Collections.singleton(Integer.valueOf(inFolder)));
        }

        if (jData.has(SearchFields.PATTERN)) {
            searchObj.setQueries(Collections.singleton(DataParser.parseString(jData, SearchFields.PATTERN)));
        }

        final int orderBy = req.optInt(AJAXServlet.PARAMETER_SORT);
        final String orderDirString = req.getParameter(AJAXServlet.PARAMETER_ORDER);
        final Order orderDir = OrderFields.parse(orderDirString);

        final AppointmentSqlFactoryService factoryService = getService();
        if (null == factoryService) {
            throw ServiceExceptionCode.absentService(AppointmentSqlFactoryService.class);
        }
        final AppointmentSQLInterface appointmentsql = factoryService.createAppointmentSql(req.getSession());
        final SearchIterator<Appointment> it = appointmentsql.searchAppointments(searchObj, orderBy, orderDir, _appointmentFields);


        try {
            final CalendarCollectionService recColl = getService(CalendarCollectionService.class);
            List<Appointment> appointmentList = new ArrayList<Appointment>();
            while (it.hasNext()) {
                final Appointment appointment = it.next();

                if (appointment.getRecurrenceType() != CalendarObject.NONE && appointment.getRecurrencePosition() == 0) {

                    // If this is an recurring appointment, add the first occurrence to the result object
                    RecurringResultsInterface recuResults = null;
                    try {
                        recuResults = recColl.calculateFirstRecurring(appointment);
                    } catch (final OXException x) {
                        LOG.error("Can not calculate recurrence for appointment {} in context {}", appointment.getObjectID(), req.getSession().getContextId(), x);
                        appointmentList.add(appointment);
                    }
                    if (recuResults != null && recuResults.size() != 1) {
                        LOG.warn("Can not load first recurring appointment from appointment object {}", appointment.getObjectID());
                        appointmentList.add(appointment);
                    } else if (recuResults != null) {
                        appointment.setStartDate(new Date(recuResults.getRecurringResult(0).getStart()));
                        appointment.setEndDate(new Date(recuResults.getRecurringResult(0).getEnd()));

                        appointmentList.add(appointment);
                    }
                } else {
                    appointmentList.add(appointment);
                }

                if (appointment.getLastModified() != null && timestamp.before(appointment.getLastModified())) {
                    timestamp = appointment.getLastModified();
                }
            }

            final int leftHandLimit = req.optInt(AJAXServlet.LEFT_HAND_LIMIT);
            final int rightHandLimit = req.optInt(AJAXServlet.RIGHT_HAND_LIMIT);

            if (leftHandLimit >= 0 || rightHandLimit > 0) {
                final int size = appointmentList.size();
                final int fromIndex = leftHandLimit > 0 ? leftHandLimit : 0;
                final int toIndex = rightHandLimit > 0 ? (rightHandLimit > size ? size : rightHandLimit) : size;
                if ((fromIndex) > size) {
                    appointmentList = Collections.<Appointment> emptyList();
                } else if (fromIndex >= toIndex) {
                    appointmentList = Collections.<Appointment> emptyList();
                } else {
                    /*
                     * Check if end index is out of range
                     */
                    if (toIndex < size) {
                        appointmentList = appointmentList.subList(fromIndex, toIndex);
                    } else if (fromIndex > 0) {
                        appointmentList = appointmentList.subList(fromIndex, size);
                    }
                }
                appointmentList = new PropertizedList<Appointment>(appointmentList).setProperty("more", Integer.valueOf(size));
            }

            return new AJAXRequestResult(appointmentList, timestamp, "appointment");
        } finally {
            if (it != null) {
                it.close();
            }
        }
    }

    private static final Set<String> REQUIRED_PARAMETERS = com.openexchange.tools.arrays.Collections.unmodifiableSet(
        AJAXServlet.PARAMETER_COLUMNS
    );

    private static final Set<String> OPTIONAL_PARAMETERS = com.openexchange.tools.arrays.Collections.unmodifiableSet(
        AJAXServlet.PARAMETER_TIMEZONE, AJAXServlet.PARAMETER_SORT, AJAXServlet.PARAMETER_ORDER, AJAXServlet.LEFT_HAND_LIMIT, AJAXServlet.RIGHT_HAND_LIMIT
    );

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
        JSONObject jsonObject = request.getData();
        int[] folderIDs = jsonObject.has(AJAXServlet.PARAMETER_INFOLDER) ? new int[] { jsonObject.getInt(AJAXServlet.PARAMETER_INFOLDER) } : null;
        String pattern = jsonObject.optString(SearchFields.PATTERN);
        List<UserizedEvent> events = session.getCalendarService().searchEvents(session, folderIDs, pattern);
        return getAppointmentResultWithTimestamp(session, events);
    }

}
