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
import com.openexchange.session.Session;
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

    protected JSONObject convertCalendarResult(CalendarResult calendarResult, String timeZoneID, Session session, Set<EventField> requestedFields, boolean extendedEntities) throws OXException {
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

    private JSONObject convertCalendarResult(UpdatesResult calendarResult, String timeZoneID, Session session, Set<EventField> fields, boolean extendedEntities) throws OXException {
        JSONObject result = new JSONObject(2);
        try {
            result.put(ChronosCalendarResultJsonFields.Updates.NEW, convertEvents(calendarResult.getNewAndModifiedEvents(), timeZoneID, session, fields, extendedEntities));
            result.put(ChronosCalendarResultJsonFields.Updates.DELETED, convertEvents(calendarResult.getDeletedEvents(), timeZoneID, session, fields, extendedEntities));
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
        return result;
    }

    protected JSONArray convertCreateResults(List<CreateResult> results, String timeZoneID, Session session, Set<EventField> fields, boolean extendedEntities) throws OXException {
        JSONArray events = new JSONArray(results.size());
        for (CreateResult createResult : results) {
            events.put(convertEvent(createResult.getCreatedEvent(), timeZoneID, session, fields, extendedEntities));
        }
        return events;
    }

    protected JSONArray convertUpdateResults(List<UpdateResult> results, String timeZoneID, Session session, Set<EventField> fields, boolean extendedEntities) throws OXException {
        JSONArray events = new JSONArray(results.size());
        for (UpdateResult updateResult : results) {
            events.put(convertEvent(updateResult.getUpdate(), timeZoneID, session, fields, extendedEntities));
        }
        return events;
    }

    protected JSONArray convertDeleteResults(List<DeleteResult> results, String timeZoneID, Session session, Set<EventField> fields, boolean extendedEntities) throws OXException {
        JSONArray events = new JSONArray(results.size());
        for (DeleteResult deleteResult : results) {
            events.put(convertEvent(deleteResult.getOriginal(), timeZoneID, session, fields, extendedEntities));
        }
        return events;
    }

}
