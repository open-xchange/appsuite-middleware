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

package com.openexchange.calendar.json.actions;

import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.parser.ParticipantParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.calendar.json.actions.chronos.EventConverter;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.server.ServiceLookup;


/**
 * {@link ConfirmAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@RestrictedAction(module = AppointmentAction.MODULE, type = RestrictedAction.Type.WRITE)
public final class ConfirmAction extends AppointmentAction {

    /**
     * Initializes a new {@link ConfirmAction}.
     *
     * @param services A service lookup reference
     */
    public ConfirmAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(CalendarSession session, AppointmentAJAXRequest request) throws OXException, JSONException {
        String folderId = request.checkParameter(AJAXServlet.PARAMETER_FOLDERID);
        String objectId = request.checkParameter(AJAXServlet.PARAMETER_ID);
        int recurrencePosition = request.optInt(AJAXServlet.PARAMETER_OCCURRENCE);
        EventID eventID;
        if (AppointmentAJAXRequest.NOT_FOUND == recurrencePosition) {
            eventID = new EventID(folderId, objectId);
        } else {
            eventID = getEventConverter(session).getEventID(folderId, objectId, recurrencePosition);
        }
        long clientTimestamp = optClientTimestamp(request, CalendarUtils.DISTANT_FUTURE);
        JSONObject jsonObject = request.getData();
        ConfirmableParticipant participant = new ParticipantParser().parseConfirmation(true, jsonObject);
        Attendee attendee = EventConverter.getAttendee(participant);
        if ((0 == participant.getType() || Participant.USER == participant.getType()) && 0 == participant.getIdentifier()) {
            attendee.setEntity(jsonObject.has(AJAXServlet.PARAMETER_ID) ? jsonObject.getInt(AJAXServlet.PARAMETER_ID) : session.getUserId());
        }
        CalendarResult result = session.getCalendarService().updateAttendee(session, eventID, attendee, null, clientTimestamp);
        return new AJAXRequestResult(new JSONObject(0), new Date(result.getTimestamp()), "json");
    }

}
