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

package com.openexchange.chronos.itip.json.action;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.json.converter.mapper.EventMapper;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataSource;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * 
 * {@link Utils}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
class Utils {

    private Utils() {}

    /**
     * Initializes a {@link CalendarSession} for iMIP handling
     *
     * @param services The {@link ServiceLookup} to get the {@link CalendarService} from
     * @param session The session
     * @return A new {@link CalendarSession}
     * @throws OXException If service is missing
     */
    public static CalendarSession initCalendarSession(ServiceLookup services, ServerSession session) throws OXException {
        CalendarSession calendarSession = services.getService(CalendarService.class).init(session);
        calendarSession.set(CalendarParameters.PARAMETER_IGNORE_STORAGE_WARNINGS, Boolean.TRUE);
        calendarSession.set(CalendarParameters.PARAMETER_SKIP_EXTERNAL_ATTENDEE_URI_CHECKS, Boolean.TRUE);
        return calendarSession;
    }

    /**
     * Converts the given list of events to a {@link AJAXRequestResult}
     *
     * @param session The session
     * @param tz The timezone to consider for the client
     * @param events The list of events to convert
     * @return A {@link AJAXRequestResult}
     * @throws OXException In case serialization fails
     * @throws JSONException In case serialization fails
     */
    public static AJAXRequestResult convertToResult(Session session, TimeZone tz, List<Event> events) throws OXException, JSONException {
        List<Event> sorted = CalendarUtils.sortSeriesMasterFirst(events);
        if (sorted != null) {
            JSONArray array = new JSONArray(sorted.size());
            for (Event event : sorted) {
                event = EventMapper.getInstance().copy(event, null, (EventField[]) null);
                event.setFolderId(CalendarUtils.prependDefaultAccount(event.getFolderId()));
                JSONObject object = EventMapper.getInstance().serialize(event, EventMapper.getInstance().getAssignedFields(event), tz, session);
                array.put(object);
            }
            return new AJAXRequestResult(array, new Date(), "json");
        }

        JSONObject object = new JSONObject();
        object.put("msg", "Done");
        return new AJAXRequestResult(object, new Date(), "json");
    }

    /**
     * Get the timezone for the user
     *
     * @param request The request optional containing the timezone
     * @param session The users session to get the timezone from
     * @return The timezone to use
     */
    public static TimeZone getTimeZone(AJAXRequestData request, ServerSession session) {
        TimeZone tz = TimeZone.getTimeZone(session.getUser().getTimeZone());
        String timezoneParameter = request.getParameter("timezone");
        return timezoneParameter == null ? tz : TimeZone.getTimeZone(timezoneParameter);
    }

    private static final String DATA_SOURCE_PARAM = "dataSource";
    private static final String SUPPORTED_DATA_SOURCE = "com.openexchange.mail.ical";

    /**
     * Checks if the correct data source for the request was transmitted.
     * <p>
     * Only value that is allowed is {@value #SUPPORTED_DATA_SOURCE}
     *
     * @param request
     * @throws OXException
     */
    private static void checkDataSource(AJAXRequestData request) throws OXException {
        String dataSourceParam = request.getParameter(DATA_SOURCE_PARAM);
        if (false == SUPPORTED_DATA_SOURCE.equals(dataSourceParam)) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(DATA_SOURCE_PARAM, dataSourceParam);
        }
    }

    /**
     * Get the iCAL file as {@link InputStream} from a mail
     *
     * @param session The {@link Session}
     * @param request The request that contains the mail references from
     * @param conversionService The {@link ConversionService} to get the iCAL from
     * @return The iCAL as {@link InputStream}
     * @throws OXException If {@link InputStream} can't be get
     */
    public static InputStream getIcalFromMail(Session session, AJAXRequestData request, ConversionService conversionService) throws OXException {
        checkDataSource(request);

        DataArguments dataArguments = getDataArguments(request);

        DataSource dataSource = conversionService.getDataSource(SUPPORTED_DATA_SOURCE);
        if (null == dataSource) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DataSource.class.getName());
        }
        final Data<InputStream> dsData = dataSource.getData(InputStream.class, dataArguments, session);
        return dsData.getData();
    }

    /**
     * Transforms request parameters to a {@link DataArguments} object
     *
     * @param request The request to get parameter from
     * @return {@link DataArguments} with the parameters set
     * @throws OXException In case of malformed JSON
     */
    public static DataArguments getDataArguments(AJAXRequestData request) throws OXException {
        DataArguments dataArguments = new DataArguments();
        Object data = request.getData();
        if (data != null) {
            if (false == data instanceof JSONObject) {
                throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
            }
            JSONObject body = (JSONObject) data;
            body.entrySet().forEach((e) -> dataArguments.put(e.getKey(), e.getValue().toString()));
        } else {
            request.getParameters().forEach((k, v) -> dataArguments.put(k, v));
        }
        return dataArguments;
    }
}
