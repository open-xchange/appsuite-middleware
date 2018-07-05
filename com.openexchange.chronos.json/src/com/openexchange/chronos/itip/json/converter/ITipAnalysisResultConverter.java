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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.itip.json.converter;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.itip.ITipAction;
import com.openexchange.chronos.itip.ITipAnalysis;
import com.openexchange.chronos.itip.ITipAnnotation;
import com.openexchange.chronos.itip.ITipChange;
import com.openexchange.chronos.json.converter.mapper.EventMapper;
import com.openexchange.chronos.json.fields.ChronosEventConflictJsonFields;
import com.openexchange.chronos.json.fields.ChronosGeneralJsonFields;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ITipAnalysisResultConverter}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class ITipAnalysisResultConverter implements ResultConverter {

    public static final String INPUT_FORMAT = "iTipAnalysis";

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
        Object resultObject = result.getResultObject();
        String tz = requestData.getParameter(ChronosGeneralJsonFields.TIMEZONE);
        if (ITipAnalysis.class.isInstance(resultObject)) {
            ITipAnalysis analysis = (ITipAnalysis) resultObject;
            result.setResultObject(convertAnalysis(analysis, tz, session), getOutputFormat());
        } else if (List.class.isInstance(resultObject)) {
            List<ITipAnalysis> analysis = (List<ITipAnalysis>) resultObject;
            result.setResultObject(convertAnalysis(analysis, tz, session), getOutputFormat());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private JSONArray convertAnalysis(List<ITipAnalysis> analysis, String tz, ServerSession session) throws OXException {
        JSONArray array = new JSONArray();
        for (ITipAnalysis a : analysis) {
            array.put(convertAnalysis(a, tz, session));
        }
        return array;
    }

    private JSONObject convertAnalysis(ITipAnalysis analysis, String tz, ServerSession session) throws OXException {
        JSONObject json = new JSONObject();
        try {
            convertAnnotations(analysis.getAnnotations(), json, tz, session);
            convertChanges(analysis.getChanges(), json, tz, session);
            convertActions(analysis.getActions(), json);
            convertAttributes(analysis.getAttributes(), json);
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
        return json;
    }

    private void convertAttributes(Map<String, Object> attributes, JSONObject json) throws JSONException {
        if (attributes == null || attributes.isEmpty()) {
            return;
        }

        JSONObject jsonObject = new JSONObject();
        for (Entry<String, Object> entry : attributes.entrySet()) {
            jsonObject.put(entry.getKey(), entry.getValue());
        }

        json.putOpt("attributes", attributes);
    }

    private void convertActions(Set<ITipAction> actions, JSONObject json) throws JSONException {
        if (actions == null || actions.isEmpty()) {
            return;
        }

        JSONArray actionsArray = new JSONArray();
        for (ITipAction action : actions) {
            actionsArray.put(action.name().toLowerCase());
        }

        json.put("actions", actionsArray);
    }

    private void convertChanges(List<ITipChange> changes, JSONObject json, String tz, ServerSession session) throws JSONException, OXException {
        if (changes == null || changes.isEmpty()) {
            return;
        }

        JSONArray changesArray = new JSONArray();
        for (ITipChange change : changes) {
            JSONObject changeObject = new JSONObject();
            convertChange(change, changeObject, tz, session);
            changesArray.put(changeObject);
        }

        json.put("changes", changesArray);
    }

    private void convertChange(ITipChange change, JSONObject changeObject, String tz, ServerSession session) throws JSONException, OXException {
        if (change == null) {
            return;
        }

        if (change.getIntroduction() != null) {
            changeObject.put("introduction", change.getIntroduction());
        }
        changeObject.put("type", change.getType().name().toLowerCase());
        changeObject.put("exception", change.isException());

        Event newEvent = change.getNewEvent();
        adjustFolderIdForClient(newEvent);
        if (newEvent != null) {
            changeObject.put("newEvent", EventMapper.getInstance().serialize(newEvent, EventMapper.getInstance().getAssignedFields(newEvent), tz, session));
        }

        Event currentEvent = change.getCurrentEvent();
        adjustFolderIdForClient(currentEvent);
        if (currentEvent != null) {
            changeObject.put("currentEvent", EventMapper.getInstance().serialize(currentEvent, EventMapper.getInstance().getAssignedFields(currentEvent), tz, session));
        }

        Event masterEvent = change.getMasterEvent();
        adjustFolderIdForClient(masterEvent);
        if (masterEvent != null) {
            changeObject.put("masterEvent", EventMapper.getInstance().serialize(masterEvent, EventMapper.getInstance().getAssignedFields(masterEvent), tz, session));
        }

        Event deletedEvent = change.getDeletedEvent();
        adjustFolderIdForClient(deletedEvent);
        if (deletedEvent != null) {
            changeObject.put("deletedEvent", EventMapper.getInstance().serialize(deletedEvent, EventMapper.getInstance().getAssignedFields(deletedEvent), tz, session));
        }

        List<EventConflict> conflicts = change.getConflicts();
        if (conflicts != null && !conflicts.isEmpty()) {
            JSONArray array = new JSONArray(conflicts.size());
            for (EventConflict conflict : conflicts) {
                JSONObject jsonConflict = new JSONObject(3);
                jsonConflict.put(ChronosEventConflictJsonFields.EventConflict.HARD_CONFLICT, conflict.isHardConflict());
                jsonConflict.put(ChronosEventConflictJsonFields.EventConflict.CONFLICTING_ATTENDEES, convertAttendees(conflict.getConflictingAttendees()));
                jsonConflict.put(ChronosEventConflictJsonFields.EventConflict.EVENT, EventMapper.getInstance().serialize(conflict.getConflictingEvent(), EventMapper.getInstance().getAssignedFields(conflict.getConflictingEvent()), tz, session));
                array.put(jsonConflict);
            }
            changeObject.put("conflicts", array);
        }

        List<String> diffDescription = change.getDiffDescription();
        if (change.getDiff() != null && diffDescription != null && !diffDescription.isEmpty()) {
            JSONArray array = new JSONArray();
            for (String description : diffDescription) {
                array.put(description);
            }
            changeObject.put("diffDescription", array);
        }
    }

    private void adjustFolderIdForClient(Event event) {
        if (event != null) {
            event.setFolderId(CalendarUtils.prependDefaultAccount(event.getFolderId()));
        }
    }

    private JSONArray convertAttendees(List<Attendee> attendees) throws JSONException {
        JSONArray result = new JSONArray(attendees.size());
        for (Attendee attendee : attendees) {
            result.put(EventMapper.serializeCalendarUser(attendee));
        }
        return result;
    }

    private void convertAnnotations(List<ITipAnnotation> annotations, JSONObject json, String tz, ServerSession session) throws JSONException, OXException {
        if (annotations == null || annotations.isEmpty()) {
            return;
        }

        JSONArray array = new JSONArray();
        for (ITipAnnotation annotation : annotations) {
            JSONObject annotationObject = new JSONObject();
            annotationObject.put("message", annotation.getMessage());
            Event event = annotation.getEvent();
            if (event != null) {
                annotationObject.put("event", EventMapper.getInstance().serialize(event, EventMapper.getInstance().getAssignedFields(event), tz, session));
            }
            array.put(annotationObject);
        }

        json.put("annotations", array);
    }

}
