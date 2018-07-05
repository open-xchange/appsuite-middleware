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

import static com.openexchange.chronos.compat.Event2Appointment.asInt;
import static com.openexchange.tools.TimeZoneUtils.getTimeZone;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.calendar.json.AppointmentAJAXRequestFactory;
import com.openexchange.calendar.json.actions.chronos.DefaultEventConverter;
import com.openexchange.calendar.json.actions.chronos.EventConverter;
import com.openexchange.calendar.json.compat.Appointment;
import com.openexchange.calendar.json.compat.AppointmentWriter;
import com.openexchange.calendar.json.compat.CalendarDataObject;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.compat.Event2Appointment;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.SortOrder;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.CommonObject.Marker;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.groupware.results.CollectionDelta;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link AppointmentAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class AppointmentAction implements AJAXActionService {

    /** The columns alias for the columns included in the response of a typical <code>all</code> request */
    public static final int[] COLUMNS_ALL_ALIAS = new int[] { 1, 20, 207, 206, 2 };

    /** The columns alias for the columns included in the response of a typical <code>list</code> request */
    public static final int[] COLUMNS_LIST_ALIAS = new int[] { 1, 20, 207, 206, 2, 200, 201, 202, 203, 209, 221, 401, 402, 102, 400, 101, 220, 215, 100 };

    protected final ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link AppointmentAction}.
     *
     * @param services A service lookup reference
     */
    protected AppointmentAction(ServiceLookup services) {
        super();
        this.serviceLookup = services;
    }

    /**
     * Gets the event converter.
     *
     * @return The event converter
     */
    protected EventConverter getEventConverter(CalendarSession session) {
        return new DefaultEventConverter(serviceLookup, session);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        if (false == session.getUserPermissionBits().hasCalendar()) {
            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create("calendar");
        }
        AppointmentAJAXRequest request = AppointmentAJAXRequestFactory.createAppointmentAJAXRequest(requestData, session);
        CalendarSession calendarSession = initSession(request);
        /*
         * mark request to avoid duplicate attempts to resolve occurrences of event series at
         * com.openexchange.calendar.json.converters.AppointmentResultConverter
         */
        request.getRequest().setProperty("com.openexchange.calendar.resolveOccurrences", Boolean.FALSE);
        try {
            return perform(calendarSession, request);
        } catch (OXException e) {
            throw EventConverter.wrapCalendarException(e);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Performs an appointment request.
     *
     * @param session The calendar session
     * @param request The request
     * @return The request result
     */
    protected abstract AJAXRequestResult perform(CalendarSession session, AppointmentAJAXRequest request) throws OXException, JSONException;

    protected static AJAXRequestResult getAppointmentResultWithTimestamp(EventConverter converter, List<Event> events) throws OXException {
        long timestamp = 0L;
        List<Appointment> appointments = new ArrayList<Appointment>(events.size());
        for (Event event : events) {
            appointments.add(converter.getAppointment(event));
            timestamp = getLatestTimestamp(timestamp, event);
        }
        return new AJAXRequestResult(appointments, new Date(timestamp), "appointment");
    }

    protected static AJAXRequestResult getAppointmentResultWithTimestamp(EventConverter converter, List<Event> events, List<EventID> requestedIDs) throws OXException {
        long timestamp = 0L;
        if (requestedIDs.size() != events.size()) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(new IllegalStateException("requestedIDs.size() != events.size()"), Autoboxing.I(50933));
        }
        List<Appointment> appointments = new ArrayList<Appointment>(requestedIDs.size());
        for (int i = 0; i < requestedIDs.size(); i++) {
            Event event = events.get(i);
            if (null == event) {
                getLogger(AppointmentAction.class).info("Requested object {} not found in results; skipping silently.", Autoboxing.I(i));
                continue;
            }
            appointments.add(converter.getAppointment(event));
            timestamp = getLatestTimestamp(timestamp, event);
        }
        return new AJAXRequestResult(appointments, new Date(timestamp), "appointment");
    }

    protected static AJAXRequestResult getAppointmentConflictResult(EventConverter converter, List<EventConflict> conflicts) throws OXException, JSONException {
        TimeZone timeZone = converter.getDefaultTimeZone();
        AppointmentWriter appointmentWriter = new AppointmentWriter(timeZone).setSession(ServerSessionAdapter.valueOf(converter.getSession()));
        JSONArray jsonArray = new JSONArray(conflicts.size());
        for (EventConflict conflict : conflicts) {
            JSONObject jsonObject = new JSONObject();
            CalendarDataObject appointment = converter.getAppointment(conflict.getConflictingEvent());
            if (conflict.isHardConflict()) {
                appointment.setHardConflict();
            }
            if (null != conflict.getConflictingAttendees()) {
                List<Participant> participants = new ArrayList<Participant>();
                for (Attendee attendee : conflict.getConflictingAttendees()) {
                    if (CalendarUserType.INDIVIDUAL.equals(attendee.getCuType())) {
                        UserParticipant userParticipant = new UserParticipant(attendee.getEntity());
                        userParticipant.setConfirm(Event2Appointment.getConfirm(attendee.getPartStat()));
                        participants.add(userParticipant);
                    } else if (CalendarUserType.RESOURCE.equals(attendee.getCuType()) || CalendarUserType.ROOM.equals(attendee.getCuType())) {
                        ResourceParticipant resourceParticipant = new ResourceParticipant(attendee.getEntity());
                        participants.add(resourceParticipant);
                    }
                }
                appointment.setParticipants(participants);
                appointment.setConfirmations(new ConfirmableParticipant[0]);
            }
            appointmentWriter.writeAppointment(appointment, jsonObject);
            jsonArray.put(jsonObject);
        }
        return new AJAXRequestResult(new JSONObject().put("conflicts", jsonArray), null, "json");
    }

    protected static long getLatestTimestamp(long timestamp, Event event) {
        return getLatestTimestamp(timestamp, event.getTimestamp());
    }

    protected static long getLatestTimestamp(long lastModified1, long lastModified2) {
        return Math.max(lastModified1, lastModified2);
    }

    protected static AJAXRequestResult getAppointmentDeltaResultWithTimestamp(EventConverter converter, List<Event> newAndModifiedEvents, List<Event> deletedEvents) throws OXException {
        long timestamp = 0L;
        CollectionDelta<Appointment> delta = new CollectionDelta<Appointment>();
        if (null != newAndModifiedEvents) {
            for (Event event : newAndModifiedEvents) {
                delta.addNewOrModified(converter.getAppointment(event));
                timestamp = getLatestTimestamp(timestamp, event);
            }
        }
        if (null != deletedEvents) {
            for (Event event : deletedEvents) {
                Appointment appointment = new Appointment();
                appointment.setMarker(Marker.ID_ONLY);
                appointment.setObjectID(asInt(event.getId()));
                delta.addDeleted(appointment);
                timestamp = getLatestTimestamp(timestamp, event);
            }
        }
        return new AJAXRequestResult(delta, new Date(timestamp), "appointment");
    }

    protected List<EventID> parseRequestedIDs(CalendarSession session, AppointmentAJAXRequest request) throws OXException, JSONException {
        Object data = request.getData();
        if (JSONArray.class.isInstance(data)) {
            return parseEventIDs(session, (JSONArray) data);
        } else if (JSONObject.class.isInstance(data)) {
            return Collections.singletonList(parseEventID(session, (JSONObject) data));
        }
        throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
    }

    protected List<EventID> parseEventIDs(CalendarSession session, JSONArray jsonArray) throws OXException, JSONException {
        List<EventID> eventIDs = new ArrayList<EventID>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            eventIDs.add(parseEventID(session, jsonArray.getJSONObject(i)));
        }
        return eventIDs;
    }

    protected EventID parseEventID(CalendarSession session, JSONObject jsonObject) throws OXException {
        String folderId = DataParser.checkString(jsonObject, AJAXServlet.PARAMETER_FOLDERID);
        String objectId = DataParser.checkString(jsonObject, AJAXServlet.PARAMETER_ID);
        if (jsonObject.hasAndNotNull(CalendarFields.RECURRENCE_POSITION)) {
            return getEventConverter(session).getEventID(folderId, objectId, DataParser.checkInt(jsonObject, CalendarFields.RECURRENCE_POSITION));
        }
        if (jsonObject.hasAndNotNull(CalendarFields.OLD_RECURRENCE_POSITION)) {
            return getEventConverter(session).getEventID(folderId, objectId, DataParser.checkInt(jsonObject, CalendarFields.OLD_RECURRENCE_POSITION));
        }
        if (jsonObject.hasAndNotNull(CalendarFields.RECURRENCE_DATE_POSITION)) {
            return getEventConverter(session).getEventID(folderId, objectId, DataParser.checkDate(jsonObject, CalendarFields.RECURRENCE_DATE_POSITION));
        }
        return new EventID(folderId, objectId);
    }

    protected long parseClientTimestamp(AppointmentAJAXRequest request) throws OXException {
        String parameter = request.checkParameter(AJAXServlet.PARAMETER_TIMESTAMP);
        try {
            return Long.parseLong(parameter.trim());
        } catch (NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(AJAXServlet.PARAMETER_TIMESTAMP, parameter);
        }
    }

    protected long optClientTimestamp(AppointmentAJAXRequest request, long fallbackTimestamp) throws OXException {
        String parameter = request.getParameter(AJAXServlet.PARAMETER_TIMESTAMP);
        if (null == parameter) {
            return fallbackTimestamp;
        }
        try {
            return Long.parseLong(parameter.trim());
        } catch (NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(AJAXServlet.PARAMETER_TIMESTAMP, parameter);
        }
    }

    /**
     * Gets a list of required parameter names that will be evaluated. If missing in the request, an appropriate exception is thrown. By
     * default, an empty list is returned.
     *
     * @return The list of required parameters
     */
    protected Set<String> getRequiredParameters() {
        return Collections.emptySet();
    }

    /**
     * Gets a list of parameter names that will be evaluated if set, but are not required to fulfill the request. By default, an empty
     * list is returned.
     *
     * @return The list of optional parameters
     */
    protected Set<String> getOptionalParameters() {
        return Collections.emptySet();
    }

    /**
     * Initializes a calendar session for a request and parses all known parameters supplied by the client, throwing an appropriate
     * exception in case a required parameters is missing.
     *
     * @param request The underlying appointment request
     * @return The calendar session
     */
    protected CalendarSession initSession(AppointmentAJAXRequest request) throws OXException {
        CalendarSession session = serviceLookup.getService(CalendarService.class).init(request.getSession());
        Set<String> requiredParameters = getRequiredParameters();
        Set<String> optionalParameters = getOptionalParameters();
        Set<String> parameters = new HashSet<String>();
        parameters.addAll(requiredParameters);
        parameters.addAll(optionalParameters);
        for (String parameter : parameters) {
            Entry<String, ?> entry = parseParameter(request, parameter, requiredParameters.contains(parameter));
            if (null != entry) {
                session.set(entry.getKey(), entry.getValue());
            }
        }
        return session;
    }

    protected static Entry<String, ?> parseParameter(AppointmentAJAXRequest request, String parameter, boolean required) throws OXException {
        String value = request.getParameter(parameter);
        if (Strings.isEmpty(value)) {
            if (false == required) {
                return null;
            }
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(parameter);
        }
        try {
            switch (parameter) {
                case AJAXServlet.PARAMETER_START:
                    long longValue = Long.parseLong(value);
                    Date date = Long.MIN_VALUE == longValue ? new Date(longValue) : request.applyTimeZone2Date(longValue);
                    return new AbstractMap.SimpleEntry<String, Date>(CalendarParameters.PARAMETER_RANGE_START, date);
                case AJAXServlet.PARAMETER_END:
                    longValue = Long.parseLong(value);
                    date = Long.MAX_VALUE == longValue ? new Date(longValue) : request.applyTimeZone2Date(longValue);
                    return new AbstractMap.SimpleEntry<String, Date>(CalendarParameters.PARAMETER_RANGE_END, date);
                case AJAXServlet.PARAMETER_SORT:
                    return new AbstractMap.SimpleEntry<String, EventField>(CalendarParameters.PARAMETER_ORDER_BY, EventConverter.getField(Integer.parseInt(value)));
                case AJAXServlet.PARAMETER_ORDER:
                    return new AbstractMap.SimpleEntry<String, SortOrder.Order>(CalendarParameters.PARAMETER_ORDER, SortOrder.Order.parse(value, SortOrder.Order.ASC));
                case AJAXServlet.PARAMETER_SHOW_PRIVATE_APPOINTMENTS:
                    return new AbstractMap.SimpleEntry<String, Boolean>(CalendarParameters.PARAMETER_SKIP_CLASSIFIED, Boolean.valueOf(false == Boolean.parseBoolean(value)));
                case AJAXServlet.PARAMETER_RECURRENCE_MASTER:
                    return new AbstractMap.SimpleEntry<String, Boolean>(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, Boolean.valueOf(false == Boolean.parseBoolean(value)));
                case AJAXServlet.PARAMETER_TIMEZONE:
                    return new AbstractMap.SimpleEntry<String, TimeZone>(CalendarParameters.PARAMETER_TIMEZONE, getTimeZone(value));
                case AJAXServlet.PARAMETER_COLUMNS:
                    return new AbstractMap.SimpleEntry<String, EventField[]>(CalendarParameters.PARAMETER_FIELDS, parseColumns(value));
                case AJAXServlet.PARAMETER_LIMIT:
                case AJAXServlet.RIGHT_HAND_LIMIT:
                    return new AbstractMap.SimpleEntry<String, Integer>(CalendarParameters.PARAMETER_RIGHT_HAND_LIMIT, Integer.valueOf(value));
                case AJAXServlet.LEFT_HAND_LIMIT:
                    return new AbstractMap.SimpleEntry<String, Integer>(CalendarParameters.PARAMETER_LEFT_HAND_LIMIT, Integer.valueOf(value));
                case AJAXServlet.PARAMETER_IGNORE:
                    return new AbstractMap.SimpleEntry<String, String[]>(CalendarParameters.PARAMETER_IGNORE, Strings.splitByComma(value));
                default:
                    throw new IllegalArgumentException("unknown paramter: " + parameter);
            }
        } catch (IllegalArgumentException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, parameter, value);
        }
    }

    private static EventField[] parseColumns(String columns) throws OXException {
        if (null == columns) {
            return null;
        }
        int[] columnIDs;
        if ("all".equals(columns)) {
            columnIDs = COLUMNS_ALL_ALIAS;
        } else if ("list".equals(columns)) {
            columnIDs = COLUMNS_LIST_ALIAS;
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
        return EventConverter.getFields(columnIDs);
    }

}
