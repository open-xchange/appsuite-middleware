
package com.openexchange.ajax.appointment.action;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AbstractAJAXResponse;

/**
 * Provides functionality for appointment parsing.
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 *
 */
public class AppointmentParserTools {

    public static void parseConflicts(JSONObject data, AbstractAJAXResponse response) throws JSONException {
        if (!data.has("conflicts")) {
            return;
        }

        JSONArray conflicts = (JSONArray) data.get("conflicts");
        List<ConflictObject> conflictObjects = new ArrayList<ConflictObject>();
        for (int i = 0; i < conflicts.length(); i++) {
            JSONObject conflict = conflicts.getJSONObject(i);
            ConflictObject conflictObject = new ConflictObject();
            if (conflict.has("participants")) {
                parseConflictParticipants(conflict.getJSONArray("participants"), conflictObject);
            }
            if (conflict.has("title")) {
                conflictObject.setTitle(conflict.getString("title"));
            }
            if (conflict.has("shown_as")) {
                conflictObject.setShownAs(conflict.getInt("shown_as"));
            }
            if (conflict.has("start_date")) {
                conflictObject.setStartDate(conflict.getLong("start_date"));
            }
            if (conflict.has("end_date")) {
                conflictObject.setEndDate(conflict.getLong("end_date"));
            }
            if (conflict.has("id")) {
                conflictObject.setId(conflict.getInt("id"));
            }
            if (conflict.has("created_by")) {
                conflictObject.setCreatedBy(conflict.getInt("created_by"));
            }
            conflictObjects.add(conflictObject);
        }
        response.setConflicts(conflictObjects);
    }

    private static void parseConflictParticipants(JSONArray participants, ConflictObject conflictObject) throws JSONException {
        List<Participant> participantObjects = new ArrayList<Participant>();
        for (int i = 0; i < participants.length(); i++) {
            Participant participantObject = new Participant();
            JSONObject participant = participants.getJSONObject(i);
            if (participant.has("confirmation")) {
                participantObject.setConfirmation(participant.getInt("confirmation"));
            }
            if (participant.has("type")) {
                participantObject.setType(participant.getInt("type"));
            }
            if (participant.has("id")) {
                participantObject.setId(participant.getInt("id"));
            }
            participantObjects.add(participantObject);
        }
    }

}
