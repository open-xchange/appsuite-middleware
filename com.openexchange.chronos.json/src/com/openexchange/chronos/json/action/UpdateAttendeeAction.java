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

package com.openexchange.chronos.json.action;

import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_FIELDS;
import static com.openexchange.tools.arrays.Collections.unmodifiableSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.json.converter.CalendarResultConverter;
import com.openexchange.chronos.json.converter.MultipleCalendarResultConverter;
import com.openexchange.chronos.json.converter.mapper.EventMapper;
import com.openexchange.chronos.json.converter.mapper.ListItemMapping;
import com.openexchange.chronos.json.exception.CalendarExceptionCodes;
import com.openexchange.chronos.json.oauth.ChronosOAuthScope;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.json.ListMapping;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 *
 * {@link UpdateAttendeeAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
@OAuthAction(ChronosOAuthScope.OAUTH_WRITE_SCOPE)
public class UpdateAttendeeAction extends ChronosAction {

    /**
     * Initializes a new {@link UpdateAttendeeAction}.
     *
     * @param services
     */
    protected UpdateAttendeeAction(ServiceLookup services) {
        super(services);
    }

    private static final Set<String> OPTIONAL_PARAMETERS = unmodifiableSet(PARAM_SEND_INTERNAL_NOTIFICATIONS, PARAM_CHECK_CONFLICTS, PARAM_RANGE_START, PARAM_RANGE_END, PARAM_EXPAND, PARAMETER_FIELDS);

    private static final String ATTENDEE = "attendee";
    private static final String ALARMS_FIELD = "alarms";

    @Override
    protected Set<String> getOptionalParameters() {
        return OPTIONAL_PARAMETERS;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected AJAXRequestResult perform(IDBasedCalendarAccess calendarAccess, AJAXRequestData requestData) throws OXException {
        long clientTimestamp = parseClientTimestamp(requestData);
        Object data = requestData.getData();
        if (data instanceof JSONObject) {
            Attendee attendee = null;
            try {
                JSONObject attendeeJSON = ((JSONObject) data).getJSONObject(ATTENDEE);
                ListItemMapping<Attendee, Event, JSONObject> mapping = (ListItemMapping<Attendee, Event, JSONObject>) ((ListMapping<Attendee, Event>) EventMapper.getInstance().opt(EventField.ATTENDEES));
                Entry<String, ?> timezone = parseParameter(requestData, "timezone", false);
                if (timezone != null && timezone.getValue() != null) {
                    attendee = mapping.deserialize(attendeeJSON, TimeZone.getTimeZone((String) timezone.getValue()));
                } else {
                    attendee = mapping.deserialize(attendeeJSON, TimeZone.getTimeZone(requestData.getSession().getUser().getTimeZone()));
                }
                if (!attendee.containsUri() && !attendee.containsEntity()) {
                    attendee.setEntity(requestData.getSession().getUserId());
                }
            } catch (JSONException e) {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e.getMessage(), e);
            }

            EventID eventID = parseIdParameter(requestData);
            CalendarResult updateAttendeeResult;
            try {
                updateAttendeeResult = calendarAccess.updateAttendee(eventID, attendee, clientTimestamp);
                clientTimestamp = updateAttendeeResult.getTimestamp() == 0l ? clientTimestamp : updateAttendeeResult.getTimestamp();
            } catch (OXException e) {
                return handleConflictException(e);
            }

            List<OXException> warnings = null;
            List<CalendarResult> results = null;
            if (((JSONObject) data).has(ALARMS_FIELD)) {
                Event toUpdate = new Event();
                Entry<String, ?> parseParameter = parseParameter(requestData, "timezone", false);
                try {
                    if (parseParameter == null) {
                        EventMapper.getInstance().get(EventField.ALARMS).deserialize((JSONObject) data, toUpdate, TimeZone.getTimeZone(requestData.getSession().getUser().getTimeZone()));
                    } else {
                        TimeZone zone = (TimeZone) parseParameter.getValue();
                        EventMapper.getInstance().get(EventField.ALARMS).deserialize((JSONObject) data, toUpdate, zone);
                    }
                    try {
                        // Update calendar session with new timestamp
                        CalendarResult updateAlarmResult = calendarAccess.updateAlarms(eventID, toUpdate.getAlarms(), clientTimestamp);
                        results = new ArrayList<>(2);
                        results.add(updateAttendeeResult);
                        results.add(updateAlarmResult);
                    } catch (OXException e) {
                        warnings = Collections.singletonList(CalendarExceptionCodes.UNABLE_TO_ADD_ALARMS.create(e, e.getMessage()));
                    }
                } catch (JSONException e) {
                    warnings = Collections.singletonList(CalendarExceptionCodes.UNABLE_TO_ADD_ALARMS.create(e, e.getMessage()));
                }

            }
            if (results != null) {
                long timestamp = 0L;
                for (CalendarResult result : results) {
                    timestamp = Math.max(timestamp, result.getTimestamp());
                }
                return new AJAXRequestResult(results, new Date(timestamp), MultipleCalendarResultConverter.INPUT_FORMAT);
            } else {
                AJAXRequestResult ajaxRequestResult = new AJAXRequestResult(updateAttendeeResult, new Date(updateAttendeeResult.getTimestamp()), CalendarResultConverter.INPUT_FORMAT);
                if (warnings != null) {
                    ajaxRequestResult.addWarnings(warnings);
                }
                return ajaxRequestResult;
            }

        } else {
            throw AjaxExceptionCodes.ILLEGAL_REQUEST_BODY.create();
        }
    }

}
