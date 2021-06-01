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

package com.openexchange.chronos.itip.json.action;

import static com.openexchange.chronos.itip.json.action.Utils.convertToResult;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.scheduling.IncomingSchedulingMessage;
import com.openexchange.chronos.scheduling.SchedulingBroker;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.chronos.scheduling.SchedulingSource;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link IncomingSchedulingAction}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class IncomingSchedulingAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(IncomingSchedulingAction.class);

    private final ServiceLookup services;
    private final SchedulingMethod method;

    /**
     * Initializes a new {@link IncomingSchedulingAction}.
     * 
     * @param method The scheduling method to handle
     * @param services The service lookup
     */
    public IncomingSchedulingAction(SchedulingMethod method, ServiceLookup services) {
        super();
        this.method = method;
        this.services = services;
    }

    /**
     * Gets a value indicating whether this instance can handle a specific message or not
     *
     * @param message The incoming message to get the method from
     * @return <code>true</code> if this instance can handle the update, <code>false</code> otherwise
     */
    public boolean canPerform(IncomingSchedulingMessage message) {
        return method.equals(message.getMethod());
    }

    /**
     * Tries to apply the designated method by applying updates to the calendar.
     *
     * @param request The request
     * @param message The incoming message
     * @param session The session
     * @param tz The timezone for the user
     * @return Either an result for the client or
     *         <code>null</code> if the request could not be served, e.g. when the method
     *         in the calendar doesn't match the expected method to handle
     * @throws OXException In case of an error while updating
     */
    public AJAXRequestResult perform(AJAXRequestData request, IncomingSchedulingMessage message, CalendarSession session, TimeZone tz) throws OXException {
        /*
         * patch imported calendar & perform scheduling operation
         */
        CalendarResult result = perform(request, message, session);
        if (null == result) {
            return null;
        }
        /*
         * Transform results to align with expected API output
         */
        List<Event> updatedEvents = new ArrayList<>();
        for (CreateResult createResult : result.getCreations()) {
            updatedEvents.add(createResult.getCreatedEvent());
        }
        for (UpdateResult updateResult : result.getUpdates()) {
            updatedEvents.add(updateResult.getUpdate());
        }
        try {
            return convertToResult(session.getSession(), tz, updatedEvents);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e);
        }
    }

    /**
     * Performs the designated method announced set for this instance and performs
     * the corresponding update in the calendar.
     *
     * @param request The request to get the information from
     * @param message The incoming message
     * @param session The session
     * @return A {@link CalendarResult} of the update or
     *         <code>null</code> to indicate that no processing has been performed
     * @throws OXException In case of error
     * @see <a href="https://tools.ietf.org/html/rfc5546">RFC 5546</a>
     */
    private CalendarResult perform(AJAXRequestData request, IncomingSchedulingMessage message, CalendarSession session) throws OXException {
        setCalendarParameters(request, session);

        SchedulingBroker schedulingBroker = services.getServiceSafe(SchedulingBroker.class);
        return schedulingBroker.handleIncomingScheduling(session, SchedulingSource.API, message, getAttendee(request, message));
    }

    /**
     * Set required calendar parameters for the further operations
     *
     * @param request The request
     * @param session The calendar session to modify
     */
    private void setCalendarParameters(AJAXRequestData request, CalendarSession session) {
        if ("accept_and_ignore_conflicts".equalsIgnoreCase(request.getAction())) {
            session.set(CalendarParameters.PARAMETER_CHECK_CONFLICTS, Boolean.FALSE);
        }
    }

    private Attendee getAttendee(AJAXRequestData request, IncomingSchedulingMessage message) {
        if (SchedulingMethod.ADD.equals(message.getMethod()) || SchedulingMethod.REQUEST.equals(message.getMethod())) {
            Attendee update = new Attendee();
            update.setEntity(message.getTargetUser());
            update.setPartStat(getPartStat(request.getAction()));
            update.setComment(getComment(request));
            return update;
        }
        return null;
    }

    private static ParticipationStatus getPartStat(String action) {
        switch (action.toLowerCase()) {
            case "accept_and_ignore_conflicts":
            case "accept":
                return ParticipationStatus.ACCEPTED;
            case "tentative":
                return ParticipationStatus.TENTATIVE;
            case "decline":
                return ParticipationStatus.DECLINED;
            default:
                return null;
        }
    }

    private static String getComment(AJAXRequestData request) {
        try {
            return request.getParameter("message", String.class);
        } catch (OXException e) {
            LOGGER.debug("Unable to get comment", e);
        }
        return null;
    }

}
