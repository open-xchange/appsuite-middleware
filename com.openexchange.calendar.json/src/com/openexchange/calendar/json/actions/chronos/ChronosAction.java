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
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.writer.AppointmentWriter;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.calendar.json.AppointmentAJAXRequestFactory;
import com.openexchange.calendar.json.actions.AppointmentAction;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.UserizedEvent;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
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

    private final EventConverter eventConverter;

    /**
     * Initializes a new {@link ChronosAction}.
     *
     * @param services A service lookup reference
     */
    protected ChronosAction(ServiceLookup services) {
        super(services);
        this.eventConverter = new EventConverter(services);
    }

    /**
     * Gets the event converter.
     *
     * @return The event converter
     */
    protected EventConverter getEventConverter() {
        return eventConverter;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        if (false == session.getUserPermissionBits().hasCalendar()) {
            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create("calendar");
        }
        AppointmentAJAXRequest request = AppointmentAJAXRequestFactory.createAppointmentAJAXRequest(requestData, session);
        boolean legacy;
        switch (requestData.getAction().toLowerCase()) {
            case "all":
                legacy = false;
                break;
            case "list":
                legacy = false;
                break;
            case "get":
                legacy = false;
                break;
            case "updates":
                legacy = false;
                break;
            case "search":
                legacy = false;
                break;
            case "newappointments":
                legacy = false;
                break;
            case "has":
                legacy = false;
                break;
            case "resolveuid":
                legacy = false;
                break;
            case "getchangeexceptions":
                legacy = false;
                break;
            case "freebusy":
                legacy = true;
                break;
            case "new":
                legacy = false;
                break;
            case "update":
                legacy = false;
                break;
            case "confirm":
                legacy = false;
                break;
            case "delete":
                legacy = false;
                break;
            case "copy":
                legacy = false;
                break;
            default:
                legacy = false;
                break;
        }
        String legacyValue = request.getParameter("legacy");
        if (null != legacyValue) {
            legacy = Boolean.parseBoolean(legacyValue);
        }
        //        legacy = true;

        if (legacy) {
            try {
                return perform(request);
            } catch (final JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            }
        } else {
            try {
                return perform(initSession(request), request);
            } catch (OXException e) {
                throw EventConverter.wrapCalendarException(e);
            } catch (JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            }
        }
    }

    protected abstract AJAXRequestResult perform(CalendarSession session, AppointmentAJAXRequest request) throws OXException, JSONException;

    protected AJAXRequestResult getAppointmentResultWithTimestamp(CalendarSession session, List<UserizedEvent> events) throws OXException {
        Date timestamp = new Date(0L);
        List<Appointment> appointments = new ArrayList<Appointment>(events.size());
        for (UserizedEvent event : events) {
            appointments.add(getEventConverter().getAppointment(session, event));
            timestamp = getLatestModified(timestamp, event);
        }
        return new AJAXRequestResult(appointments, timestamp, "appointment");
    }

    protected AJAXRequestResult getAppointmentConflictResult(CalendarSession session, List<EventConflict> conflicts) throws OXException, JSONException {
        TimeZone timeZone = session.get(CalendarParameters.PARAMETER_TIMEZONE, TimeZone.class, TimeZone.getTimeZone(session.getUser().getTimeZone()));
        AppointmentWriter appointmentWriter = new AppointmentWriter(timeZone).setSession(session.getSession());
        JSONArray jsonArray = new JSONArray(conflicts.size());
        for (EventConflict conflict : conflicts) {
            JSONObject jsonObject = new JSONObject();
            CalendarDataObject appointment = getEventConverter().getAppointment(session, conflict.getConflictingEvent());
            if (conflict.isHardConflict()) {
                appointment.setHardConflict();
            }
            appointmentWriter.writeAppointment(appointment, jsonObject);
            jsonArray.put(jsonObject);
        }
        return new AJAXRequestResult(new JSONObject().put("conflicts", jsonArray), null, "json");
    }

    protected static Date getLatestModified(Date lastModified, UserizedEvent event) {
        return getLatestModified(lastModified, event.getEvent().getLastModified());
    }

    protected static Date getLatestModified(Date lastModified1, Date lastModified2) {
        return null != lastModified2 && lastModified2.after(lastModified1) ? lastModified2 : lastModified1;
    }

    protected AJAXRequestResult getAppointmentDeltaResultWithTimestamp(CalendarSession session, List<UserizedEvent> newAndModifiedEvents, List<UserizedEvent> deletedEvents) throws OXException {
        Date timestamp = new Date(0L);
        CollectionDelta<Appointment> delta = new CollectionDelta<Appointment>();
        if (null != newAndModifiedEvents) {
            for (UserizedEvent event : newAndModifiedEvents) {
                delta.addNewOrModified(getEventConverter().getAppointment(session, event));
                timestamp = getLatestModified(timestamp, event);
            }
        }
        if (null != deletedEvents) {
            for (UserizedEvent event : deletedEvents) {
                Appointment appointment = getEventConverter().getAppointment(session, event);
                appointment.setMarker(Marker.ID_ONLY);
                delta.addDeleted(appointment);
                timestamp = getLatestModified(timestamp, event);
            }
        }
        return new AJAXRequestResult(delta, timestamp, "appointment");
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
        int folderID = DataParser.checkInt(jsonObject, AJAXServlet.PARAMETER_FOLDERID);
        int objectID = DataParser.checkInt(jsonObject, AJAXServlet.PARAMETER_ID);
        if (jsonObject.hasAndNotNull(CalendarFields.RECURRENCE_POSITION)) {
            return eventConverter.getEventID(session, folderID, objectID, DataParser.checkInt(jsonObject, CalendarFields.RECURRENCE_POSITION));
        }
        if (jsonObject.hasAndNotNull(CalendarFields.OLD_RECURRENCE_POSITION)) {
            return eventConverter.getEventID(session, folderID, objectID, DataParser.checkInt(jsonObject, CalendarFields.OLD_RECURRENCE_POSITION));
        }
        if (jsonObject.hasAndNotNull(CalendarFields.RECURRENCE_DATE_POSITION)) {
            return eventConverter.getEventID(session, folderID, objectID, DataParser.checkDate(jsonObject, CalendarFields.RECURRENCE_DATE_POSITION));
        }
        return new EventID(folderID, objectID);
    }

    protected Set<String> getRequiredParameters() {
        return Collections.emptySet();
    }

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
        CalendarSession session = getService(CalendarService.class).init(request.getSession());
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

    private static Entry<String, ?> parseParameter(AppointmentAJAXRequest request, String parameter, boolean required) throws OXException {
        String value = request.getParameter(parameter);
        if (Strings.isEmpty(value)) {
            if (false == required) {
                return null;
            }
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(parameter);
        }
        try {
            switch (parameter) {
                case AJAXServlet.PARAMETER_TIMESTAMP:
                    return new AbstractMap.SimpleEntry<String, Long>(CalendarParameters.PARAMETER_TIMESTAMP, Long.valueOf(value));
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
                    return new AbstractMap.SimpleEntry<String, String>(CalendarParameters.PARAMETER_ORDER, "desc".equalsIgnoreCase(value) ? "DESC" : "ASC");
                case AJAXServlet.PARAMETER_SHOW_PRIVATE_APPOINTMENTS:
                    return new AbstractMap.SimpleEntry<String, Boolean>(CalendarParameters.PARAMETER_INCLUDE_PRIVATE, Boolean.valueOf(value));
                case AJAXServlet.PARAMETER_RECURRENCE_MASTER:
                    return new AbstractMap.SimpleEntry<String, Boolean>(CalendarParameters.PARAMETER_RECURRENCE_MASTER, Boolean.valueOf(value));
                case AJAXServlet.PARAMETER_TIMEZONE:
                    return new AbstractMap.SimpleEntry<String, TimeZone>(CalendarParameters.PARAMETER_TIMEZONE, getTimeZone(value));
                case AJAXServlet.PARAMETER_COLUMNS:
                    return new AbstractMap.SimpleEntry<String, EventField[]>(CalendarParameters.PARAMETER_FIELDS, parseColumns(value));
                case AJAXServlet.PARAMETER_LIMIT:
                case AJAXServlet.RIGHT_HAND_LIMIT:
                    return new AbstractMap.SimpleEntry<String, Integer>(CalendarParameters.PARAMETER_RIGHT_HAND_LIMIT, Integer.valueOf(value));
                case AJAXServlet.LEFT_HAND_LIMIT:
                    return new AbstractMap.SimpleEntry<String, Integer>(CalendarParameters.PARAMETER_LEFT_HAND_LIMIT, Integer.valueOf(value));
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
        return EventConverter.getFields(columnIDs);
    }

}
