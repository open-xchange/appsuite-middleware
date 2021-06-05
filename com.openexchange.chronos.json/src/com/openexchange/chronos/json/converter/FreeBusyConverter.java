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
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.FreeBusyTime;
import com.openexchange.chronos.json.converter.mapper.AttendeesMapping;
import com.openexchange.chronos.json.converter.mapper.EventMapper;
import com.openexchange.chronos.json.fields.ChronosFreeBusyJsonFields;
import com.openexchange.chronos.json.fields.ChronosGeneralJsonFields;
import com.openexchange.chronos.service.FreeBusyResult;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link FreeBusyConverter}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class FreeBusyConverter implements ResultConverter {

    public static final String INPUT_FORMAT = "freeBusy";

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
         * Determine timezone
         */
        String timeZoneID = requestData.getParameter(ChronosGeneralJsonFields.TIMEZONE);
        if (null == timeZoneID) {
            timeZoneID = session.getUser().getTimeZone();
        }
        /*
         * check and convert result object
         */
        Object resultObject = result.getResultObject();
        if (Map.class.isInstance(resultObject)) {
            try {
                Map<Attendee, FreeBusyResult> map = (Map<Attendee, FreeBusyResult>) resultObject;
                JSONArray array = new JSONArray(map.size());
                for (Entry<Attendee, FreeBusyResult> attendee : map.entrySet()) {
                    JSONObject json = new JSONObject(2);
                    json.put(ChronosFreeBusyJsonFields.ATTENDEE, ((AttendeesMapping<?>) EventMapper.getInstance().get(EventField.ATTENDEES)).serialize(attendee.getKey(), TimeZone.getTimeZone(timeZoneID)));
                    FreeBusyResult freeBusyResult = attendee.getValue();
                    json.put(ChronosFreeBusyJsonFields.FREE_BUSY_TIME, parseFreeBusyTime(freeBusyResult, session, timeZoneID));
                    if (freeBusyResult.getWarnings() != null && freeBusyResult.getWarnings().size()!=0){
                        JSONArray warningsArray = new JSONArray(freeBusyResult.getWarnings().size());
                        for(OXException ex : freeBusyResult.getWarnings()){
                            JSONObject warning = new JSONObject();
                            ResponseWriter.addException(warning, ex, session.getUser().getLocale(), false);
                            warningsArray.put(warning);
                        }
                        json.put(ChronosFreeBusyJsonFields.WARNINGS, warningsArray);
                    }
                    array.put(json);
                }
                resultObject = array;
            } catch (JSONException e) {
                throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            }
        } else {
            throw new UnsupportedOperationException();
        }
        result.setResultObject(resultObject, getOutputFormat());
    }

    /**
     * Parse a {@link FreeBusyResult}s to a {@link JSONArray}.
     *
     * @param freeBusyResult The {@link FreeBusyResult}
     * @param session The user session
     * @param timeZoneID The id of the time-zone
     * @return The {@link JSONArray}
     * @throws JSONException
     * @throws OXException
     */
    private JSONArray parseFreeBusyTime(FreeBusyResult freeBusyResult, Session session, String timeZoneID) throws JSONException, OXException {
        List<FreeBusyTime> freeBusyTimes = freeBusyResult.getFreeBusyTimes();
        if (freeBusyTimes==null){
            return new JSONArray();
        }
        JSONArray result = new JSONArray(freeBusyTimes.size());
        for (FreeBusyTime time : freeBusyTimes) {
            JSONObject json = new JSONObject(3);
            json.put(ChronosFreeBusyJsonFields.FreeBusyTime.START_TIME, time.getStartTime().getTime());
            json.put(ChronosFreeBusyJsonFields.FreeBusyTime.END_TIME, time.getEndTime().getTime());
            json.put(ChronosFreeBusyJsonFields.FreeBusyTime.FB_TYPE, time.getFbType().getValue());
            Event event = time.getEvent();
            if (event!=null){
              json.put(ChronosFreeBusyJsonFields.FreeBusyTime.EVENT, EventMapper.getInstance().serialize(event, EventMapper.getInstance().getAssignedFields(event), timeZoneID, session));
            }
            result.put(json);
        }
        return result;
    }

}
