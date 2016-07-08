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

package com.openexchange.calendar.json.actions.chronos;

import static com.openexchange.tools.TimeZoneUtils.getTimeZone;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.calendar.json.AppointmentAJAXRequestFactory;
import com.openexchange.calendar.json.actions.AppointmentAction;
import com.openexchange.chronos.CalendarParameters;
import com.openexchange.chronos.CalendarService;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventID;
import com.openexchange.chronos.UserizedEvent;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CommonObject.Marker;
import com.openexchange.groupware.results.CollectionDelta;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ChronosAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class ChronosAction extends AppointmentAction {

    /**
     * Initializes a new {@link ChronosAction}.
     *
     * @param services A service lookup reference
     */
    protected ChronosAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        if (false == session.getUserPermissionBits().hasCalendar()) {
            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create("calendar");
        }
        AppointmentAJAXRequest request = AppointmentAJAXRequestFactory.createAppointmentAJAXRequest(requestData, session);
        boolean performNew = true;
        try {
            return performNew ? perform(getService(CalendarService.class), request) : perform(request);
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    protected abstract AJAXRequestResult perform(CalendarService calendarService, AppointmentAJAXRequest request) throws OXException, JSONException;

    protected static AJAXRequestResult getAppointmentResultWithTimestamp(List<UserizedEvent> events) {
        Date timestamp = new Date(0L);
        List<Appointment> appointments = new ArrayList<Appointment>(events.size());
        for (UserizedEvent event : events) {
            appointments.add(EventMapper.getAppointment(event));
            timestamp = getLatestModified(timestamp, event);
        }
        return new AJAXRequestResult(appointments, timestamp, "appointment");
    }

    private static Date getLatestModified(Date lastModified, UserizedEvent event) {
        Date eventLastModified = event.getEvent().getLastModified();
        return null != eventLastModified && eventLastModified.after(lastModified) ? eventLastModified : lastModified;
    }

    protected static AJAXRequestResult getAppointmentDeltaResultWithTimestamp(List<UserizedEvent> newAndModifiedEvents, List<UserizedEvent> deletedEvents) {
        Date timestamp = new Date(0L);
        CollectionDelta<Appointment> delta = new CollectionDelta<Appointment>();
        if (null != newAndModifiedEvents) {
            for (UserizedEvent event : newAndModifiedEvents) {
                delta.addNewOrModified(EventMapper.getAppointment(event));
                timestamp = getLatestModified(timestamp, event);
            }
        }
        if (null != deletedEvents) {
            for (UserizedEvent event : deletedEvents) {
                Appointment appointment = EventMapper.getAppointment(event);
                appointment.setMarker(Marker.ID_ONLY);
                delta.addDeleted(appointment);
                timestamp = getLatestModified(timestamp, event);
            }
        }
        return new AJAXRequestResult(delta, timestamp, "appointment");
    }

    protected static List<EventID> parseRequestedIDs(AppointmentAJAXRequest request) throws OXException, JSONException {
        Object data = request.getData();
        if (JSONArray.class.isInstance(data)) {
            return parseEventIDs((JSONArray) data);
        } else if (JSONObject.class.isInstance(data)) {
            return Collections.singletonList(parseEventID((JSONObject) data));
        }
        throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
    }

    protected static List<EventID> parseEventIDs(JSONArray jsonArray) throws OXException, JSONException {
        List<EventID> eventIDs = new ArrayList<EventID>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            eventIDs.add(parseEventID(jsonArray.getJSONObject(i)));
        }
        return eventIDs;
    }

    protected static EventID parseEventID(JSONObject jsonObject) throws OXException {
        return new EventID(DataParser.checkInt(jsonObject, AJAXServlet.PARAMETER_FOLDERID), DataParser.checkInt(jsonObject, AJAXServlet.PARAMETER_ID));
    }

    protected static CalendarParameters parseParameters(AppointmentAJAXRequest request, String... requiredParameters) throws OXException {
        CalendarParameters parameters = new CalendarParameters();
        String timestampParameter = request.getParameter(AJAXServlet.PARAMETER_TIMESTAMP);
        if (null != timestampParameter) {
            try {
                parameters.set(CalendarParameters.PARAMETER_TIMESTAMP, Long.valueOf(timestampParameter));
            } catch (NumberFormatException e) {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(AJAXServlet.PARAMETER_TIMESTAMP, timestampParameter);
            }
        }
        Date start = request.optDate(AJAXServlet.PARAMETER_START);
        if (null != start) {
            parameters.set(CalendarParameters.PARAMETER_RANGE_START, request.applyTimeZone2Date(start.getTime()));
        }
        Date end = request.optDate(AJAXServlet.PARAMETER_END);
        if (null != end) {
            parameters.set(CalendarParameters.PARAMETER_RANGE_END, request.applyTimeZone2Date(end.getTime()));
        }
        EventField orderBy = EventMapper.getField(request.optInt(AJAXServlet.PARAMETER_SORT));
        parameters.set(CalendarParameters.PARAMETER_ORDER_BY, null == orderBy ? EventField.START_DATE : orderBy);
        String order = request.getParameter(AJAXServlet.PARAMETER_ORDER);
        if (null != order) {
            parameters.set(CalendarParameters.PARAMETER_ORDER, "desc".equalsIgnoreCase(order) ? "DESC" : "ASC");
        }
        String showPrivate = request.getParameter(AJAXServlet.PARAMETER_SHOW_PRIVATE_APPOINTMENTS);
        if (null != showPrivate) {
            parameters.set(CalendarParameters.PARAMETER_INCLUDE_PRIVATE, Boolean.valueOf(showPrivate));
        }
        String recurrenceMaster = request.getParameter(AJAXServlet.PARAMETER_RECURRENCE_MASTER);
        if (null != recurrenceMaster) {
            parameters.set(CalendarParameters.PARAMETER_RECURRENCE_MASTER, Boolean.valueOf(recurrenceMaster));
        }
        String timeZoneID = request.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
        parameters.set(CalendarParameters.PARAMETER_TIMEZONE, null == timeZoneID ? request.getTimeZone() : getTimeZone(timeZoneID));

        String columns = request.getParameter(AJAXServlet.PARAMETER_COLUMNS);
        if (null != columns) {
            parameters.set(CalendarParameters.PARAMETER_FIELDS, parseColumns(columns));
        }
        return requireParameters(parameters, requiredParameters);
    }

    private static EventField[] parseColumns(String columns) throws OXException {
        if (null == columns) {
            return null;
        }
        int[] columnIDs;
        if ("all".equals(columns)) {
            columnIDs = AppointmentAction.COLUMNS_ALL_ALIAS;
        } else if ("list".equals(columns)) {
            columnIDs = AppointmentAction.COLUMNS_LIST_ALIAS;
        } else {
            String[] splitted = Strings.splitByComma(columns);
            columnIDs = new int[splitted.length];
            for (int i = 0; i < splitted.length; i++) {
                try {
                    columnIDs[i] = Integer.parseInt(splitted[i].trim());
                } catch (NumberFormatException e) {
                    throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(AJAXServlet.PARAMETER_COLUMNS, columns);
                }
            }
        }
        return EventMapper.getFields(columnIDs);
    }

    private static CalendarParameters requireParameters(CalendarParameters parameters, String... requiredParameters) throws OXException {
        if (null != requiredParameters) {
            for (String requiredParameter : requiredParameters) {
                if (false == parameters.contains(requiredParameter)) {
                    throw AjaxExceptionCodes.MISSING_PARAMETER.create(requiredParameter);
                }
            }
        }
        return parameters;
    }

}
