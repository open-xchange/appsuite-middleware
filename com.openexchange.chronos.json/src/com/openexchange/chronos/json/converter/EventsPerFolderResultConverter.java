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
                ResponseWriter.addException(error, eventsResult.getError());
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
