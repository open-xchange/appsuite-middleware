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

import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.json.fields.ChronosCalendarResultJsonFields;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CalendarResultConverter}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class CalendarResultConverter extends EventResultConverter {

    /**
     * Initializes a new {@link CalendarResultConverter}.
     *
     * @param services A service lookup reference
     */
    public CalendarResultConverter(ServiceLookup services) {
        super(services);
    }

    @SuppressWarnings("hiding")
    public static final String INPUT_FORMAT = "calendarResult";

    @Override
    public String getInputFormat() {
        return INPUT_FORMAT;
    }

    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        /*
         * check and convert result object
         */
        Object resultObject = result.getResultObject();
        if (resultObject instanceof CalendarResult) {
            resultObject = convertCalendarResult((CalendarResult) resultObject, getTimeZoneID(requestData, session), session, getFields(requestData), isExtendedEntities(requestData));
        } else if (resultObject instanceof UpdatesResult) {
            resultObject = convertCalendarResult((UpdatesResult) resultObject, getTimeZoneID(requestData, session), session, getFields(requestData), isExtendedEntities(requestData));
        } else {
            throw new UnsupportedOperationException();
        }
        result.setResultObject(resultObject, getOutputFormat());
    }

    protected JSONObject convertCalendarResult(CalendarResult calendarResult, String timeZoneID, ServerSession session, Set<EventField> requestedFields, boolean extendedEntities) throws OXException {
        JSONObject result = new JSONObject(3);
        try {
            result.put(ChronosCalendarResultJsonFields.Result.CREATED, convertCreateResults(calendarResult.getCreations(), timeZoneID, session, requestedFields, extendedEntities));
            result.put(ChronosCalendarResultJsonFields.Result.UPDATED, convertUpdateResults(calendarResult.getUpdates(), timeZoneID, session, requestedFields, extendedEntities));
            result.put(ChronosCalendarResultJsonFields.Result.DELETED, convertDeleteResults(calendarResult.getDeletions(), timeZoneID, session, requestedFields, extendedEntities));
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
        return result;
    }

    private JSONObject convertCalendarResult(UpdatesResult calendarResult, String timeZoneID, ServerSession session, Set<EventField> fields, boolean extendedEntities) throws OXException {
        JSONObject result = new JSONObject(2);
        try {
            result.put(ChronosCalendarResultJsonFields.Updates.NEW, convertEvents(calendarResult.getNewAndModifiedEvents(), timeZoneID, session, fields, extendedEntities));
            result.put(ChronosCalendarResultJsonFields.Updates.DELETED, convertEvents(calendarResult.getDeletedEvents(), timeZoneID, session, fields, extendedEntities));
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
        return result;
    }

    protected JSONArray convertCreateResults(List<CreateResult> results, String timeZoneID, ServerSession session, Set<EventField> fields, boolean extendedEntities) throws OXException {
        JSONArray events = new JSONArray(results.size());
        for (CreateResult createResult : results) {
            events.put(convertEvent(createResult.getCreatedEvent(), timeZoneID, session, fields, extendedEntities));
        }
        return events;
    }

    protected JSONArray convertUpdateResults(List<UpdateResult> results, String timeZoneID, ServerSession session, Set<EventField> fields, boolean extendedEntities) throws OXException {
        JSONArray events = new JSONArray(results.size());
        for (UpdateResult updateResult : results) {
            events.put(convertEvent(updateResult.getUpdate(), timeZoneID, session, fields, extendedEntities));
        }
        return events;
    }

    protected JSONArray convertDeleteResults(List<DeleteResult> results, String timeZoneID, ServerSession session, Set<EventField> fields, boolean extendedEntities) throws OXException {
        JSONArray events = new JSONArray(results.size());
        for (DeleteResult deleteResult : results) {
            events.put(convertEvent(deleteResult.getOriginal(), timeZoneID, session, fields, extendedEntities));
        }
        return events;
    }

}
