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

package com.openexchange.chronos.json.converter;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.json.fields.ChronosCalendarResultJsonFields;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.ErrorAwareCalendarResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CalendarResultsPerEventIdConverter}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class CalendarResultsPerEventIdConverter extends CalendarResultConverter {

    /**
     * Initializes a new {@link CalendarResultsPerEventIdConverter}.
     *
     * @param services A service lookup reference
     */
    public CalendarResultsPerEventIdConverter(ServiceLookup services) {
        super(services);
    }

    @SuppressWarnings("hiding")
    public static final String INPUT_FORMAT = "calendarResultsPerEventId";

    @Override
    public String getInputFormat() {
        return INPUT_FORMAT;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        /*
         * check and convert result object
         */
        Object resultObject = result.getResultObject();
        if (null != resultObject) {
            Map<EventID, CalendarResult> resultsPerId;
            try {
                resultsPerId = (Map<EventID, CalendarResult>) resultObject;
            } catch (ClassCastException e) {
                throw new UnsupportedOperationException(e);
            }
            resultObject = convertCalendarResults(resultsPerId, getTimeZoneID(requestData, session), session, getFields(requestData), isExtendedEntities(requestData));
        }
        result.setResultObject(resultObject, getOutputFormat());
    }

    private JSONValue convertCalendarResults(Map<EventID, CalendarResult> resultsPerId, String timeZoneID, ServerSession session, Set<EventField> requestedFields, boolean extendedEntities) throws OXException {
        if (null == resultsPerId) {
            return null;
        }
        JSONArray jsonArray = new JSONArray(resultsPerId.size());
        for (Entry<EventID, CalendarResult> entry : resultsPerId.entrySet()) {
            jsonArray.put(convertCalendarResult(entry.getKey(), entry.getValue(), timeZoneID, session, requestedFields, extendedEntities));
        }
        return jsonArray;
    }

    private JSONObject convertCalendarResult(EventID eventID, CalendarResult calendarResult, String timeZoneID, ServerSession session, Set<EventField> requestedFields, boolean extendedEntities) throws OXException {
        if (null == calendarResult) {
            return null;
        }
        JSONObject jsonObject = new JSONObject(8);
        try {
            jsonObject.put(ChronosCalendarResultJsonFields.ErrorAwareResult.FOLDER_ID, eventID.getFolderID());
            jsonObject.put(ChronosCalendarResultJsonFields.ErrorAwareResult.ID, eventID.getObjectID());
            jsonObject.putOpt(ChronosCalendarResultJsonFields.ErrorAwareResult.RECURRENCE_ID, eventID.getRecurrenceID());
            jsonObject.put("timestamp", calendarResult.getTimestamp());
            if (ErrorAwareCalendarResult.class.isInstance(calendarResult) && null != ((ErrorAwareCalendarResult) calendarResult).getError()) {
                JSONObject error = new JSONObject();
                ResponseWriter.addException(error, ((ErrorAwareCalendarResult) calendarResult).getError(), session.getUser().getLocale());
                jsonObject.put(ChronosCalendarResultJsonFields.ErrorAwareResult.ERROR, error);
            } else {
                jsonObject.put(ChronosCalendarResultJsonFields.ErrorAwareResult.CREATED, convertCreateResults(calendarResult.getCreations(), timeZoneID, session, requestedFields, extendedEntities));
                jsonObject.put(ChronosCalendarResultJsonFields.ErrorAwareResult.UPDATED, convertUpdateResults(calendarResult.getUpdates(), timeZoneID, session, requestedFields, extendedEntities));
                jsonObject.put(ChronosCalendarResultJsonFields.ErrorAwareResult.DELETED, convertDeleteResults(calendarResult.getDeletions(), timeZoneID, session, requestedFields, extendedEntities));
            }
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
        return jsonObject;
    }

}
