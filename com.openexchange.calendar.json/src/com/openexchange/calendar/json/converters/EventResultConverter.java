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

import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.chronos.UserizedEvent;
import com.openexchange.exception.OXException;
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
    private static final String INPUT_FORMAT = "event";

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
        TimeZone clientTimeZone = getClientTimeZone(requestData, session);
        if (UserizedEvent.class.isInstance(resultObject)) {
            try {
                result.setResultObject(convertEvent((UserizedEvent) resultObject, clientTimeZone), getOutputFormat());
            } catch (JSONException e) {
                throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            }
        }
    }

    private static TimeZone getClientTimeZone(AJAXRequestData requestData, ServerSession session) {
        String timeZoneID = requestData.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
        if (Strings.isEmpty(timeZoneID)) {
            timeZoneID = session.getUser().getTimeZone();
        }
        return TimeZone.getTimeZone(timeZoneID);
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

        return jsonObject;
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

    private static long addTimeZoneOffset(long date, TimeZone timeZone) {
        return null == timeZone ? date : date + timeZone.getOffset(date);
    }

}
