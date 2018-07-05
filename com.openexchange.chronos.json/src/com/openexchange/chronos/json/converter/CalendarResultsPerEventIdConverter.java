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
import com.openexchange.session.Session;
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

    private JSONValue convertCalendarResults(Map<EventID, CalendarResult> resultsPerId, String timeZoneID, Session session, Set<EventField> requestedFields, boolean extendedEntities) throws OXException {
        if (null == resultsPerId) {
            return null;
        }
        JSONArray jsonArray = new JSONArray(resultsPerId.size());
        for (Entry<EventID, CalendarResult> entry : resultsPerId.entrySet()) {
            jsonArray.put(convertCalendarResult(entry.getKey(), entry.getValue(), timeZoneID, session, requestedFields, extendedEntities));
        }
        return jsonArray;
    }

    private JSONObject convertCalendarResult(EventID eventID, CalendarResult calendarResult, String timeZoneID, Session session, Set<EventField> requestedFields, boolean extendedEntities) throws OXException {
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
                ResponseWriter.addException(error, ((ErrorAwareCalendarResult) calendarResult).getError());
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
