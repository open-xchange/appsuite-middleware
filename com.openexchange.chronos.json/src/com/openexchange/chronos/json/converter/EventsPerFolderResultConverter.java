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
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link EventsPerFolderResultConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventsPerFolderResultConverter extends EventResultConverter {

    @SuppressWarnings("hiding")
    public static final String INPUT_FORMAT = "eventsResults";

    /**
     * Initializes a new {@link EventsPerFolderResultConverter}.
     *
     * @param services A service lookup reference
     */
    public EventsPerFolderResultConverter(ServiceLookup services) {
        super(services);
    }

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
        if (null != resultObject) {
            Map<String, EventsResult> eventsResults;
            try {
                eventsResults = (Map<String, EventsResult>) resultObject;
            } catch (ClassCastException e) {
                throw new UnsupportedOperationException(e);
            }
            resultObject = convertEventsResults(eventsResults, getTimeZoneID(requestData, session), session, getFields(requestData), isExtendedEntities(requestData));
        }
        result.setResultObject(resultObject, getOutputFormat());
    }

    private JSONValue convertEventsResults(Map<String, EventsResult> eventsResults, String timeZoneID, ServerSession session, Set<EventField> requestedFields, boolean extendedEntities) throws OXException {
        if (null == eventsResults) {
            return null;
        }
        JSONArray jsonArray = new JSONArray(eventsResults.size());
        for (Map.Entry<String, EventsResult> entry : eventsResults.entrySet()) {
            jsonArray.put(convertEventsResult(entry.getKey(), entry.getValue(), timeZoneID, session, requestedFields, extendedEntities));
        }
        return jsonArray;
    }

    private JSONObject convertEventsResult(String folderId, EventsResult eventsResult, String timeZoneID, ServerSession session, Set<EventField> requestedFields, boolean extendedEntities) throws OXException {
        if (null == eventsResult) {
            return null;
        }
        JSONObject jsonObject = new JSONObject(3);
        try {
            jsonObject.put("folder", folderId);
            if (null != eventsResult.getError()) {
                JSONObject error = new JSONObject();
                ResponseWriter.addException(error, eventsResult.getError(), session.getUser().getLocale());
                jsonObject.put("error", error);
            } else {
                jsonObject.putOpt("events", convertEvents(eventsResult.getEvents(), timeZoneID, session, requestedFields, extendedEntities));
                jsonObject.put("timestamp", eventsResult.getTimestamp());
            }
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
        return jsonObject;
    }

}
