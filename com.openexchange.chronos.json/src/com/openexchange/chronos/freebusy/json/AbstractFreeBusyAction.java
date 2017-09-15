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

package com.openexchange.chronos.freebusy.json;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.json.action.AbstractChronosAction;
import com.openexchange.chronos.json.converter.mapper.EventMapper;
import com.openexchange.chronos.json.converter.mapper.ListItemMapping;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccessFactory;
import com.openexchange.chronos.provider.composition.IDBasedFreeBusyAccess;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.json.ListMapping;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 *
 * {@link AbstractFreeBusyAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public abstract class AbstractFreeBusyAction extends AbstractChronosAction {

    private static final String ATTENDEES = "attendees";
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractFreeBusyAction.class);

    /**
     * Initializes a new {@link AbstractDriveShareAction}.
     *
     * @param services A service lookup reference
     */
    protected AbstractFreeBusyAction(ServiceLookup services) {
        super(services);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ajax.requesthandler.AJAXActionService#perform(com.openexchange.ajax.requesthandler.AJAXRequestData, com.openexchange.tools.session.ServerSession)
     */
    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        IDBasedFreeBusyAccess freeBusyAccess = initFreeBusyAccess(requestData);
        AJAXRequestResult result = perform(freeBusyAccess, requestData);
        return result;
    }

    /**
     * Performs a request.
     *
     * @param freeBusyAccess The initialized free-busy access to use
     * @param requestData The underlying request data
     * @return The request result
     */
    protected abstract AJAXRequestResult perform(IDBasedFreeBusyAccess freeBusyAccess, AJAXRequestData requestData) throws OXException;

    /**
     * Initializes the free-busy access for a request and parses all known parameters supplied by the client, throwing an appropriate
     * exception in case a required parameters is missing.
     *
     * @param requestData The underlying request data
     * @return The initialized free-busy access
     */
    protected IDBasedFreeBusyAccess initFreeBusyAccess(AJAXRequestData requestData) throws OXException {
        IDBasedFreeBusyAccess calendarAccess = requireService(IDBasedCalendarAccessFactory.class).createFreeBusyAccess(requestData.getSession());
        Set<String> requiredParameters = getRequiredParameters();
        Set<String> optionalParameters = getOptionalParameters();
        Set<String> parameters = new HashSet<String>();
        parameters.addAll(requiredParameters);
        parameters.addAll(optionalParameters);
        for (String parameter : parameters) {
            Entry<String, ?> entry = parseParameter(requestData, parameter, requiredParameters.contains(parameter));
            if (null != entry) {
                calendarAccess.set(entry.getKey(), entry.getValue());
            }
        }
        return calendarAccess;
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
            attendee.setEntity(Integer.valueOf(attendeeId));
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
            ListItemMapping<Attendee, Event, JSONObject> mapping = (ListItemMapping<Attendee, Event, JSONObject>) ((ListMapping<Attendee, Event>) EventMapper.getInstance().opt(EventField.ATTENDEES));
            TimeZone timeZone = TimeZone.getTimeZone(requestData.getSession().getUser().getTimeZone());
            JSONArray attendeesArray = requestBody.getJSONArray(ATTENDEES);
            for (int index = 0; index < attendeesArray.length(); index++) {
                JSONObject attendeeJSON = attendeesArray.getJSONObject(index);
                Attendee attendee = mapping.deserialize(attendeeJSON, timeZone);
                if (!attendee.containsUri() && !attendee.containsEntity()) {
                    attendee.setEntity(requestData.getSession().getUserId());
                }
                attendees.add(attendee);
            }
            return attendees;
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e.getMessage(), e);
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
