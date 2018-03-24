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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.json.fields.ChronosCalendarResultJsonFields;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MergingCalendarResultConverter} merges a list of {@link CalendarResult} objects and converts the final result to json.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class MergingCalendarResultConverter extends CalendarResultConverter {

    public static final String INPUT_FORMAT = "mergingCalendarResults";

    /**
     * Initializes a new {@link MergingCalendarResultConverter}.
     *
     * @param services A service lookup reference
     */
    public MergingCalendarResultConverter(ServiceLookup services) {
        super(services);
    }

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
        if (resultObject instanceof List) {
            resultObject = convertCalendarResult((List<CalendarResult>) resultObject, getTimeZoneID(requestData, session), requestData.getSession(), getFields(requestData), isExtendedEntities(requestData));
        } else {
            throw new UnsupportedOperationException();
        }
        result.setResultObject(resultObject, getOutputFormat());
    }

    private JSONObject convertCalendarResult(List<CalendarResult> calendarResults, String timeZoneID, ServerSession session, Set<EventField> requestedFields, boolean extendedEntities) throws OXException {
        JSONObject result = new JSONObject(1);
        try {
            List<Event> creates = new ArrayList<Event>();
            List<Event> updates = new ArrayList<Event>();
            List<Event> deletes = new ArrayList<Event>();

            for (CalendarResult calendarResult : calendarResults) {
                if (calendarResult instanceof ErrorAwareCalendarResult && ((ErrorAwareCalendarResult) calendarResult).hasError()) {
                    return toJSON((com.openexchange.chronos.json.converter.ErrorAwareCalendarResult) calendarResult, session.getUser().getLocale());
                }
                for (CreateResult createResult : calendarResult.getCreations()) {
                    creates.add(createResult.getCreatedEvent());
                }
                for (UpdateResult updatedResult : calendarResult.getUpdates()) {
                    updates.add(updatedResult.getUpdate());
                }
                for (DeleteResult deleteResult : calendarResult.getDeletions()) {
                    deletes.add(deleteResult.getOriginal());
                }
            }

            result.put(ChronosCalendarResultJsonFields.Result.CREATED, convertEvents(creates, timeZoneID, session, requestedFields, extendedEntities));
            result.put(ChronosCalendarResultJsonFields.Result.UPDATED, convertEvents(updates, timeZoneID, session, requestedFields, extendedEntities));
            result.put(ChronosCalendarResultJsonFields.Result.DELETED, convertEvents(deletes, timeZoneID, session, requestedFields, extendedEntities));
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
        return result;
    }

    public static JSONObject toJSON(ErrorAwareCalendarResult result, Locale locale) throws JSONException {
        JSONObject json = new JSONObject();
        OXException exception = result.getError();
        json.put(ChronosCalendarResultJsonFields.Error.FOLDER_ID, result.getFolderID());
        json.put(ChronosCalendarResultJsonFields.Error.ID, result.getId().getObjectID());
        if (result.getId().getRecurrenceID() != null) {
            json.put(ChronosCalendarResultJsonFields.Error.RECURRENCE_ID, result.getId().getRecurrenceID().getValue().toString());
        }
        JSONObject jsonException = new JSONObject();
        ResponseWriter.addException(jsonException, exception, locale);
        json.put(ChronosCalendarResultJsonFields.Error.ERROR, jsonException);

        return json;
    }

}
