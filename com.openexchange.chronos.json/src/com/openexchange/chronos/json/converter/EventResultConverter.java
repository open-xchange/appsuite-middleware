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

package com.openexchange.chronos.json.converter;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.json.converter.mapper.EventMapper;
import com.openexchange.chronos.json.fields.ChronosGeneralJsonFields;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link EventResultConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventResultConverter implements ResultConverter {

    public static final String INPUT_FORMAT = "event";

    @Override
    public String getInputFormat() {
        return INPUT_FORMAT;
    }

    @Override
    public String getOutputFormat() {
        return "json";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        /*
         * check and convert result object
         */
        Object resultObject = result.getResultObject();
        if (Event.class.isInstance(resultObject)) {
            /*
             * only one event to convert
             */
            resultObject = convertEvent((Event) resultObject, getTimeZoneID(requestData, session), session, getFields(requestData));
        } else if (List.class.isInstance(resultObject)) {
            /*
             * convert list of events
             */
            resultObject = convertEvents((List<Event>) resultObject, getTimeZoneID(requestData, session), session, getFields(requestData));
        } else {
            throw new UnsupportedOperationException();
        }
        result.setResultObject(resultObject, getOutputFormat());
    }

    protected JSONObject convertEvent(Event event, String timeZoneID, Session session, EventField[] fields) throws OXException {
        if (null == event) {
            return null;
        }
        if (null == fields) {
            fields = EventMapper.getInstance().getAssignedFields(event);
        }
        try {
            return EventMapper.getInstance().serialize(event, fields, timeZoneID, session);
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
    }

    protected JSONArray convertEvents(List<Event> events, String timeZoneID, Session session, EventField[] fields) throws OXException {
        if (null == events) {
            return null;
        }
        JSONArray jsonArray = new JSONArray(events.size());
        for (Event event : events) {
            jsonArray.put(convertEvent(event, timeZoneID, session, fields));
        }
        return jsonArray;
    }

    protected static String getTimeZoneID(AJAXRequestData requestData, ServerSession session) {
        String timeZoneID = requestData.getParameter(ChronosGeneralJsonFields.TIMEZONE);
        return null == timeZoneID ? session.getUser().getTimeZone() : timeZoneID;
    }

    protected static EventField[] getFields(AJAXRequestData requestData) throws OXException {
        return EventMapper.getInstance().parseFields(requestData.getParameter(CalendarParameters.PARAMETER_FIELDS));
    }

}
