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
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.parser.ParticipantParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.calendar.json.actions.chronos.ChronosAction;
import com.openexchange.calendar.json.actions.chronos.EventConverter;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ConfirmAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@OAuthAction(AppointmentActionFactory.OAUTH_WRITE_SCOPE)
public final class ConfirmAction extends ChronosAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfirmAction.class);
    /**
     * Initializes a new {@link ConfirmAction}.
     * @param services
     */
    public ConfirmAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final AppointmentAJAXRequest req) throws OXException, JSONException {
        // Get parameters
        final int objectId = req.checkInt(DataFields.ID);
        final int folderId = req.checkInt(AJAXServlet.PARAMETER_FOLDERID);
        Date timestamp = null;
        final int optOccurrenceId = req.optInt(AJAXServlet.PARAMETER_OCCURRENCE);

        // Get request body
        final JSONObject jData = req.getData();

        final ConfirmableParticipant participant = new ParticipantParser().parseConfirmation(true, jData);
        final String confirmMessage = participant.getMessage();
        final int confirmStatus = participant.getConfirm();

        final ServerSession session = req.getSession();
        int userId = session.getUserId();
        if (jData.has(AJAXServlet.PARAMETER_ID)) {
            userId = DataParser.checkInt(jData, AJAXServlet.PARAMETER_ID);
        }

        final AppointmentSqlFactoryService factoryService = getService();
        if (null == factoryService) {
            throw ServiceExceptionCode.absentService(AppointmentSqlFactoryService.class);
        }
        final AppointmentSQLInterface appointmentSql = factoryService.createAppointmentSql(session);

        boolean isUser = (participant.getType() == Participant.USER) || (participant.getType() == 0);
        boolean isExternal = participant.getType() == Participant.EXTERNAL_USER;
        boolean isOccurrenceChange = (optOccurrenceId != AppointmentAJAXRequest.NOT_FOUND) && (optOccurrenceId > 0);

        if (isOccurrenceChange) {
            if (isUser) {
                timestamp = appointmentSql.setUserConfirmation(objectId, folderId, optOccurrenceId, userId, confirmStatus, confirmMessage).getLastModified();
            } else if (isExternal) {
                timestamp = appointmentSql.setExternalConfirmation(objectId, folderId, optOccurrenceId, participant.getEmailAddress(), confirmStatus, confirmMessage).getLastModified();
            } else {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(AJAXServlet.PARAMETER_TYPE, jData.get(AJAXServlet.PARAMETER_TYPE));
            }
            return new AJAXRequestResult(new JSONObject(0), timestamp, "json");
        }

        if (isUser) {
            timestamp = appointmentSql.setUserConfirmation(objectId, folderId, userId, confirmStatus, confirmMessage);
        } else if (isExternal) {
            timestamp = appointmentSql.setExternalConfirmation(objectId, folderId, participant.getEmailAddress(), confirmStatus, confirmMessage);
        } else {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create( AJAXServlet.PARAMETER_TYPE, jData.get(AJAXServlet.PARAMETER_TYPE));
        }

        return new AJAXRequestResult(new JSONObject(0), timestamp, "json");
    }

    private static final Set<String> OPTIONAL_PARAMETERS = com.openexchange.tools.arrays.Collections.unmodifiableSet(AJAXServlet.PARAMETER_TIMESTAMP);

    @Override
    protected Set<String> getOptionalParameters() {
        return OPTIONAL_PARAMETERS;
    }

    @Override
    protected AJAXRequestResult perform(CalendarSession session, AppointmentAJAXRequest request) throws OXException, JSONException {
        int objectID = request.checkInt(AJAXServlet.PARAMETER_ID);
        int folderID = request.checkInt(AJAXServlet.PARAMETER_FOLDERID);
        int recurrencePosition = request.optInt(AJAXServlet.PARAMETER_OCCURRENCE);
        EventID eventID;
        if (AppointmentAJAXRequest.NOT_FOUND == recurrencePosition) {
            eventID = new EventID(folderID, objectID);
        } else {
            eventID = getEventConverter().getEventID(session, folderID, objectID, recurrencePosition);
        }
        JSONObject jsonObject = request.getData();
        ConfirmableParticipant participant = new ParticipantParser().parseConfirmation(true, jsonObject);
        Attendee attendee = EventConverter.getAttendee(participant);
        if ((0 == participant.getType() || Participant.USER == participant.getType()) && 0 == participant.getIdentifier()) {
            attendee.setEntity(jsonObject.has(AJAXServlet.PARAMETER_ID) ? jsonObject.getInt(AJAXServlet.PARAMETER_ID) : session.getUser().getId());
        }
        CalendarResult result = session.getCalendarService().updateAttendee(session, eventID, attendee);
        return new AJAXRequestResult(new JSONObject(0), result.getTimestamp(), "json");
    }

}
