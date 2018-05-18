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

package com.openexchange.calendar.json.actions;

import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.parser.ParticipantParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.calendar.json.actions.chronos.EventConverter;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceLookup;


/**
 * {@link ConfirmAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@OAuthAction(AppointmentActionFactory.OAUTH_WRITE_SCOPE)
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
