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

package com.openexchange.chronos.json.action;

import static com.openexchange.tools.arrays.Collections.unmodifiableSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.json.converter.FreeBusyConverter;
import com.openexchange.chronos.json.converter.mapper.EventMapper;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.service.FreeBusyResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.json.ListItemMapping;
import com.openexchange.groupware.tools.mappings.json.ListMapping;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link FreeBusyAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
@RestrictedAction(module = ChronosAction.MODULE, type = RestrictedAction.Type.READ)
public class FreeBusyAction extends ChronosAction {

    private static final Set<String> OPTIONAL_PARAMETERS = unmodifiableSet(PARAM_FIELDS, PARAM_MASK_ID);

    private static final String ATTENDEES = "attendees";

    /**
     * Initializes a new {@link FreeBusyAction}.
     *
     * @param services A service lookup reference
     */
    protected FreeBusyAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected Set<String> getOptionalParameters() {
        return OPTIONAL_PARAMETERS;
    }

    @Override
    protected AJAXRequestResult perform(IDBasedCalendarAccess calendarAccess, AJAXRequestData requestData) throws OXException {
        List<Attendee> attendees = parseAttendeesObject(requestData);
        Date from = parseDate(requestData, "from");
        Date until = parseDate(requestData, "until");
        boolean merge = Boolean.TRUE.equals(requestData.getParameter("merge", Boolean.class, true));
        Map<Attendee, FreeBusyResult> result = calendarAccess.queryFreeBusy(attendees, from, until, merge);
        return new AJAXRequestResult(result, FreeBusyConverter.INPUT_FORMAT);
    }

    /**
     * Parses the request parameter 'attendees' from the specified {@link AJAXRequestData}
     *
     * @param request The {@link AJAXRequestData}
     * @return A {@link List} with {@link Attendee} objects
     * @throws OXException if a parsing error occurs
     */
    protected static List<Attendee> parseAttendeesParameter(AJAXRequestData request) throws OXException {

        String parameter = request.getParameter(ATTENDEES, String.class, false);
        String[] splitByComma = Strings.splitByComma(parameter);
        List<Attendee> attendees = new ArrayList<>(splitByComma.length);
        for (String attendeeId : splitByComma) {
            Attendee attendee = new Attendee();
            attendee.setEntity(Integer.parseInt(attendeeId));
            attendees.add(attendee);
        }

        return attendees;
    }

    /**
     * Parses the request body of the specified {@link AJAXRequestData} and extracts the {@link Attendee}s
     *
     * @param request The {@link AJAXRequestData}
     * @return A {@link List} with {@link Attendee} objects
     * @throws OXException if a JSON parsing error is occurred, or if the request body is not a {@link JSONObject},
     *             or if the 'attendees' field is missing from the request body.
     */
    protected static List<Attendee> parseAttendeesObject(AJAXRequestData requestData) throws OXException {
        List<Attendee> attendees = new ArrayList<>();
        Object objectData = requestData.getData();
        if (!(objectData instanceof JSONObject)) {
            throw AjaxExceptionCodes.ILLEGAL_REQUEST_BODY.create();
        }

        JSONObject requestBody = (JSONObject) objectData;
        if (!requestBody.hasAndNotNull(ATTENDEES)) {
            throw AjaxExceptionCodes.MISSING_FIELD.create(ATTENDEES);
        }

        try {
            @SuppressWarnings("unchecked") ListItemMapping<Attendee, Event, JSONObject> mapping = (ListItemMapping<Attendee, Event, JSONObject>) ((ListMapping<Attendee, Event>) EventMapper.getInstance().opt(EventField.ATTENDEES));
            ServerSession session = requestData.getSession();
            TimeZone timeZone = session == null ? TimeZone.getTimeZone("UTC") :  TimeZone.getTimeZone(session.getUser().getTimeZone());
            JSONArray attendeesArray = requestBody.getJSONArray(ATTENDEES);
            for (int index = 0; index < attendeesArray.length(); index++) {
                JSONObject attendeeJSON = attendeesArray.getJSONObject(index);
                Attendee attendee = mapping.deserialize(attendeeJSON, timeZone);
                attendees.add(attendee);
            }
            return attendees;
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Parses the specified request parameter from the specified {@link AJAXRequestData} as a {@link Date}
     *
     * @param request The {@link AJAXRequestData}
     * @param param The parameter's name
     * @return The parsed {@link Date}
     * @throws OXException if a parsing error occurs
     */
    protected static Date parseDate(AJAXRequestData request, String param) throws OXException {
        String parameter = request.getParameter(param, String.class, false);
        return new Date(DateTime.parse(TimeZone.getTimeZone("UTC"), parameter).getTimestamp());
    }
}
