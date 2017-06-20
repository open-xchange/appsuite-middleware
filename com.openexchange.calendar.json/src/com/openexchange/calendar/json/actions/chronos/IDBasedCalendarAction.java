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

import static org.slf4j.LoggerFactory.getLogger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.calendar.json.AppointmentAJAXRequestFactory;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.provider.composition.CompositeEventID;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccessFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.java.Autoboxing;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link IDBasedCalendarAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class IDBasedCalendarAction extends ChronosAction {

    protected IDBasedCalendarAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        if (false == session.getUserPermissionBits().hasCalendar()) {
            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create("calendar");
        }
        AppointmentAJAXRequest request = AppointmentAJAXRequestFactory.createAppointmentAJAXRequest(requestData, session);
        IDBasedCalendarAccess calendarAccess = initCalendarAccess(request);
        if (1 == 1 || false == super.initSession(request).getConfig().isUseIDBasedAccess()) {
            return super.perform(requestData, session);
        }
        try {
            return perform(calendarAccess, request);
        } catch (OXException e) {
            throw EventConverter.wrapCalendarException(e);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Initializes a calendar session for a request and parses all known parameters supplied by the client, throwing an appropriate
     * exception in case a required parameters is missing.
     *
     * @param request The underlying appointment request
     * @return The calendar session
     */
    protected IDBasedCalendarAccess initCalendarAccess(AppointmentAJAXRequest request) throws OXException {
        IDBasedCalendarAccess calendarAccess = getService(IDBasedCalendarAccessFactory.class).createAccess(request.getSession());
        Set<String> requiredParameters = getRequiredParameters();
        Set<String> optionalParameters = getOptionalParameters();
        Set<String> parameters = new HashSet<String>();
        parameters.addAll(requiredParameters);
        parameters.addAll(optionalParameters);
        for (String parameter : parameters) {
            Entry<String, ?> entry = parseParameter(request, parameter, requiredParameters.contains(parameter));
            if (null != entry) {
                calendarAccess.set(entry.getKey(), entry.getValue());
            }
        }
        return calendarAccess;
    }

    /**
     * Gets the event converter.
     *
     * @return The event converter
     */
    protected IDBasedEventConverter getEventConverter(IDBasedCalendarAccess access) {
        return new IDBasedEventConverter(serviceLookup, access);
    }

    protected List<CompositeEventID> parseRequestedIDs(IDBasedCalendarAccess access, AppointmentAJAXRequest request) throws OXException, JSONException {
        Object data = request.getData();
        if (JSONArray.class.isInstance(data)) {
            return parseEventIDs(access, (JSONArray) data);
        } else if (JSONObject.class.isInstance(data)) {
            return Collections.singletonList(parseEventID(access, (JSONObject) data));
        }
        throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
    }

    protected List<CompositeEventID> parseEventIDs(IDBasedCalendarAccess access, JSONArray jsonArray) throws OXException, JSONException {
        List<CompositeEventID> eventIDs = new ArrayList<CompositeEventID>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            eventIDs.add(parseEventID(access, jsonArray.getJSONObject(i)));
        }
        return eventIDs;
    }

    protected CompositeEventID parseEventID(IDBasedCalendarAccess access, JSONObject jsonObject) throws OXException {
        //        String folderId = DataParser.checkString(jsonObject, AJAXServlet.PARAMETER_FOLDERID);
        String objectId = DataParser.checkString(jsonObject, AJAXServlet.PARAMETER_ID);
        if (jsonObject.hasAndNotNull(CalendarFields.RECURRENCE_POSITION)) {
            return getEventConverter(access).getEventID(objectId, DataParser.checkInt(jsonObject, CalendarFields.RECURRENCE_POSITION));
        }
        if (jsonObject.hasAndNotNull(CalendarFields.OLD_RECURRENCE_POSITION)) {
            return getEventConverter(access).getEventID(objectId, DataParser.checkInt(jsonObject, CalendarFields.OLD_RECURRENCE_POSITION));
        }
        if (jsonObject.hasAndNotNull(CalendarFields.RECURRENCE_DATE_POSITION)) {
            return getEventConverter(access).getEventID(objectId, DataParser.checkDate(jsonObject, CalendarFields.RECURRENCE_DATE_POSITION));
        }
        return CompositeEventID.parse(objectId);
    }

    protected static AJAXRequestResult getAppointmentResultWithTimestamp(IDBasedEventConverter converter, List<Event> events, List<CompositeEventID> requestedIDs) throws OXException {
        Date timestamp = new Date(0L);
        if (requestedIDs.size() != events.size()) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(new IllegalStateException("requestedIDs.size() != events.size()"), Autoboxing.I(50933));
        }
        List<Appointment> appointments = new ArrayList<Appointment>(requestedIDs.size());
        for (int i = 0; i < requestedIDs.size(); i++) {
            Event event = events.get(i);
            if (null == event) {
                getLogger(ChronosAction.class).info("Requested object {} not found in results; skipping silently.", Autoboxing.I(i));
                continue;
            }
            appointments.add(converter.getAppointment(event));
            timestamp = getLatestModified(timestamp, event);
        }
        return new AJAXRequestResult(appointments, timestamp, "appointment");
    }

    protected abstract AJAXRequestResult perform(IDBasedCalendarAccess access, AppointmentAJAXRequest request) throws OXException, JSONException;

}
