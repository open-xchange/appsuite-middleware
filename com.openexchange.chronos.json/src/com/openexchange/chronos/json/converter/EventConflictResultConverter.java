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
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.json.converter.mapper.EventMapper;
import com.openexchange.chronos.json.fields.ChronosEventConflictJsonFields;
import com.openexchange.chronos.json.fields.ChronosGeneralJsonFields;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.ProblematicAttribute;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link EventConflictResultConverter}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class EventConflictResultConverter implements ResultConverter {

    public static final String INPUT_FORMAT = "eventConflict";

    /**
     * Initializes a new {@link EventConflictResultConverter}.
     */
    public EventConflictResultConverter() {
        super();
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
         * Determine time zone
         */
        String timeZoneID = requestData.getParameter(ChronosGeneralJsonFields.TIMEZONE);
        if (null == timeZoneID) {
            timeZoneID = session.getUser().getTimeZone();
        }
        /*
         * check and convert result object
         */
        Object resultObject = result.getResultObject();
        if (resultObject instanceof ProblematicAttribute[]) {
            resultObject = convertProblematics((ProblematicAttribute[]) resultObject, timeZoneID, session);
        } else if (resultObject instanceof List) {
            ProblematicAttribute[] array = new ProblematicAttribute[((List<?>) resultObject).size()];
            ((List<ProblematicAttribute>) resultObject).toArray(array);
            resultObject = convertProblematics(array, timeZoneID, session);
        } else {
            throw new UnsupportedOperationException();
        }
        result.setResultObject(resultObject, getOutputFormat());
    }

    private JSONObject convertProblematics(ProblematicAttribute[] problematics, String timeZoneID, Session session) throws OXException {
        try {
            JSONObject result = new JSONObject(1);
            JSONArray conflicts = new JSONArray(problematics.length);
            for (ProblematicAttribute problematic : problematics) {
                if (problematic instanceof EventConflict) {
                    EventConflict conflict = (EventConflict) problematic;
                    JSONObject jsonConflict = new JSONObject(3);
                    jsonConflict.put(ChronosEventConflictJsonFields.EventConflict.HARD_CONFLICT, conflict.isHardConflict());
                    jsonConflict.put(ChronosEventConflictJsonFields.EventConflict.CONFLICTING_ATTENDEES, parseAttendees(conflict.getConflictingAttendees()));
                    jsonConflict.put(ChronosEventConflictJsonFields.EventConflict.EVENT, EventMapper.getInstance().serialize(conflict.getConflictingEvent(), EventMapper.getInstance().getAssignedFields(conflict.getConflictingEvent()), timeZoneID, session));
                    conflicts.put(jsonConflict);
                }
            }
            result.put(ChronosEventConflictJsonFields.CONFLICTS, conflicts);
            return result;
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
    }

    private JSONArray parseAttendees(List<Attendee> attendees) throws JSONException {
        JSONArray result = new JSONArray(attendees.size());
        for (Attendee attendee : attendees) {
            result.put(EventMapper.serializeCalendarUser(attendee));
        }
        return result;
    }

}
