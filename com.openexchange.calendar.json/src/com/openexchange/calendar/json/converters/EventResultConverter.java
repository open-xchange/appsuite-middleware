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

package com.openexchange.calendar.json.converters;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.ajax.fields.ParticipantsFields;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.calendar.CalendarField;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.UserizedEvent;
import com.openexchange.chronos.compat.Event2Appointment;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Participant;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link EventResultConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventResultConverter implements ResultConverter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(EventResultConverter.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link EventResultConverter}.
     * 
     * @param services A service lookup reference
     */
    public EventResultConverter(final ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String getInputFormat() {
        return "event";
    }

    @Override
    public String getOutputFormat() {
        return "json";
    }

    @Override
    public Quality getQuality() {
        return Quality.BAD;
    }

    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        Object resultObject = result.getResultObject();
        try {
            if (UserizedEvent.class.isInstance(resultObject)) {
                result.setResultObject(convertEvent((UserizedEvent) resultObject, getClientTimeZone(requestData, session)), getOutputFormat());
            } else if (Collection.class.isInstance(resultObject)) {
                Collection<UserizedEvent> events = (Collection<UserizedEvent>) resultObject;
                int[] columns = new AppointmentAJAXRequest(requestData, session).checkIntArray(AJAXServlet.PARAMETER_COLUMNS);
                result.setResultObject(convertEvents(events, getClientTimeZone(requestData, session), columns), getOutputFormat());
            }
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
    }

    private JSONArray convertEvents(Collection<UserizedEvent> events, TimeZone clientTimeZone, int[] columns) throws JSONException {
        JSONArray jsonArray = new JSONArray(events.size());
        for (UserizedEvent event : events) {
            jsonArray.put(convertEventColumns(event, clientTimeZone, columns));
        }
        return jsonArray;
    }

    private JSONArray convertEventColumns(UserizedEvent event, TimeZone clientTimeZone, int[] columns) throws JSONException {
        JSONArray jsonArray = new JSONArray(columns.length);
        JSONObject convertedEvent = convertEvent(event, clientTimeZone);
        for (int i = 0; i < columns.length; i++) {
            CalendarField field = CalendarField.getByColumn(columns[i]);
            if (null == field) {
                LOG.warn("Unknown column requested: {}", Autoboxing.I(columns[i]));
                jsonArray.put(JSONObject.NULL);
            } else {
                jsonArray.put(convertedEvent.opt(field.getJsonName()));
            }
        }
        return jsonArray;
    }

    private JSONObject convertEvent(UserizedEvent event, TimeZone clientTimeZone) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt(AppointmentFields.TITLE, event.getSummary());
        jsonObject.putOpt(AppointmentFields.LOCATION, event.getLocation());
        jsonObject.putOpt(AppointmentFields.NOTE, event.getDescription());
        if (event.isAllDay()) {
            jsonObject.put(AppointmentFields.FULL_TIME, true);
            jsonObject.putOpt(AppointmentFields.START_DATE, toJsonDate(event.getStartDate()));
            jsonObject.putOpt(AppointmentFields.END_DATE, toJsonDate(event.getEndDate()));
        } else {
            jsonObject.putOpt(AppointmentFields.START_DATE, toJsonTime(event.getStartDate(), clientTimeZone));
            jsonObject.putOpt(AppointmentFields.END_DATE, toJsonTime(event.getEndDate(), clientTimeZone));
        }
        jsonObject.putOpt(AppointmentFields.TIMEZONE, event.getStartTimezone());
        jsonObject.put(AppointmentFields.ID, event.getId());
        jsonObject.put(AppointmentFields.FOLDER_ID, event.getFolderId());
        jsonObject.putOpt(AppointmentFields.UID, event.getUid());
        jsonObject.putOpt(AppointmentFields.CREATION_DATE, toJsonTime(event.getCreated(), clientTimeZone));
        jsonObject.put(AppointmentFields.CREATED_BY, event.getCreatedBy());
        jsonObject.putOpt(AppointmentFields.LAST_MODIFIED, toJsonTime(event.getLastModified(), clientTimeZone));
        jsonObject.put(AppointmentFields.MODIFIED_BY, event.getModifiedBy());
        jsonObject.putOpt(AppointmentFields.LAST_MODIFIED_UTC, toJsonTime(event.getLastModified(), TimeZone.getTimeZone("UTC")));
        jsonObject.putOpt(AppointmentFields.SEQUENCE, event.getSequence());
        jsonObject.put(AppointmentFields.PRIVATE_FLAG, Event2Appointment.getPrivateFlag(event.getClassification()));
        jsonObject.putOpt(AppointmentFields.SHOW_AS, Event2Appointment.getShownAs(event.getStatus()));
        jsonObject.putOpt(AppointmentFields.COLORLABEL, Event2Appointment.getColorLabel(event.getColor()));
        jsonObject.putOpt(AppointmentFields.ORGANIZER, Event2Appointment.getEMailAddress(event.getOrganizer().getUri()));
        if (null != event.getOrganizer() && 0 < event.getOrganizer().getEntity()) {
            jsonObject.putOpt(AppointmentFields.ORGANIZER_ID, event.getOrganizer().getEntity());
        }
        jsonObject.putOpt(AppointmentFields.ALARM, Event2Appointment.getReminder(event.getAlarms()));
        List<Attendee> attendees = event.getAttendees();
        if (null != attendees && 0 < attendees.size()) {
            JSONArray participantsArray = new JSONArray(attendees.size());
            JSONArray usersArray = new JSONArray();
            JSONArray confirmationsArray = new JSONArray();
            for (Attendee attendee : attendees) {
                convertAttendee(attendee, participantsArray, usersArray, confirmationsArray);
            }
            if (0 < participantsArray.length()) {
                jsonObject.put(AppointmentFields.PARTICIPANTS, participantsArray);
            }
            if (0 < usersArray.length()) {
                jsonObject.put(AppointmentFields.USERS, usersArray);
            }
            if (0 < confirmationsArray.length()) {
                jsonObject.put(AppointmentFields.CONFIRMATIONS, confirmationsArray);
            }
        }

        return jsonObject;
    }

    private void convertAttendee(Attendee attendee, JSONArray participantsArray, JSONArray usersArray, JSONArray confirmationsArray) throws JSONException {
        if (null == attendee.getCuType()) {
            return;
        }
        switch (attendee.getCuType()) {
            case GROUP:
                participantsArray.put(new JSONObject()
                    .put(ParticipantsFields.ID, attendee.getEntity())
                    .put(ParticipantsFields.TYPE, Participant.GROUP));
                //TODO: confirm status of group members into "users" array?
                break;
            case INDIVIDUAL:
                if (0 < attendee.getEntity()) {
                    participantsArray.put(new JSONObject()
                        .put(ParticipantsFields.ID, attendee.getEntity())
                        .put(ParticipantsFields.TYPE, Participant.USER));
                    usersArray.put(new JSONObject()
                        .put(ParticipantsFields.ID, attendee.getEntity())
                        .put(ParticipantsFields.CONFIRMATION, Event2Appointment.getConfirm(attendee.getPartStat())));
                } else {
                    participantsArray.put(new JSONObject()
                        .put(ParticipantsFields.ID, attendee.getEntity())
                        .putOpt(ParticipantsFields.DISPLAY_NAME, attendee.getCommonName())
                        .putOpt(ParticipantsFields.MAIL, Event2Appointment.getEMailAddress(attendee.getUri()))
                        .put(ParticipantsFields.TYPE, Participant.EXTERNAL_USER));
                    confirmationsArray.put(new JSONObject()
                        .put(ParticipantsFields.TYPE, Participant.EXTERNAL_USER)
                        .putOpt(ParticipantsFields.MAIL, Event2Appointment.getEMailAddress(attendee.getUri()))
                        .putOpt(ParticipantsFields.DISPLAY_NAME, attendee.getCommonName())
                        .put(ParticipantsFields.STATUS, Event2Appointment.getConfirm(attendee.getPartStat())));
                }
                break;
            case RESOURCE:
            case ROOM:
                participantsArray.put(new JSONObject()
                    .put(ParticipantsFields.ID, attendee.getEntity())
                    .put(ParticipantsFields.TYPE, Participant.RESOURCE));
                break;
            case UNKNOWN:
                break;
            default:
                break;
        }
    }

    private static Long toJsonTime(Date date, TimeZone clientTimeZone) {
        if (null != date) {
            long time = date.getTime();
            return Long.valueOf(null == clientTimeZone ? time : time + clientTimeZone.getOffset(time));
        }
        return null;
    }

    private static Long toJsonDate(Date date) {
        return null != date ? Long.valueOf(date.getTime()) : null;
    }

    private static TimeZone getClientTimeZone(AJAXRequestData requestData, ServerSession session) {
        String timeZoneID = requestData.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
        if (Strings.isEmpty(timeZoneID)) {
            timeZoneID = session.getUser().getTimeZone();
        }
        return TimeZone.getTimeZone(timeZoneID);
    }

}
