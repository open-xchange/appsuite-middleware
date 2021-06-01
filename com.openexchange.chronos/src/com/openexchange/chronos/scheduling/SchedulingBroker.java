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

package com.openexchange.chronos.scheduling;

import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link SchedulingBroker} - A broker handling all kind of messages regarding changes of the calendar data. This includes
 * <li>incoming</li>
 * and
 * <li>outgoing</li>
 * messages from or to calendar users.
 * <p>
 * Efficiently this broker provides methods to realize the iTIP protocol. Please note, that it is just iTIP and not iMIP.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @see <a href="https://tools.ietf.org/html/rfc5546">RFC 5546</a>
 * @since v7.10.3
 */
@SingletonService
public interface SchedulingBroker {

    /**
     * Handles a scheduling event for external calendar users by sending a message containing the
     * calendar object resource as iCAL file to a designated external calendar user based on each
     * given message.
     * 
     * @param session A {@link Session}
     * @param messages A {@link List} of {@link SchedulingMessage} to send to the external recipients
     * @return A list containing the {@link ScheduleStatus} of delivery in order of the given messages
     */
    List<ScheduleStatus> handleScheduling(Session session, List<SchedulingMessage> messages);

    /**
     * Handles a scheduling event for internal calendar users by sending notifications to internal
     * recipients based on the given notifications.
     * 
     * @param session A {@link Session}
     * @param notifications A {@link List} of {@link ChangeNotification} to send to the recipients
     * @return A list containing the {@link ScheduleStatus} of delivery in order of the given messages
     */
    List<ScheduleStatus> handleNotifications(Session session, List<ChangeNotification> notifications);

    /**
     * Handles incoming calendar changes triggered by external attendees. Updates the calendar data
     * based on the transmitted calendar object resources.
     * <p>
     * Will check for sanity and consistency of the data as well as permissions of the user applying
     * the changes.
     *
     * @param session The calendar session
     * @param source The source from which the scheduling has been triggered
     * @param incomingScheduling The incoming change(s)
     * @return A {@link CalendarResult} containing the updates
     * @throws OXException In case the change could not be applied, e.g. the transmitted data can't be
     *             parsed or is outdated
     */
    default CalendarResult handleIncomingScheduling(CalendarSession session, SchedulingSource source, IncomingSchedulingMessage incomingScheduling) throws OXException {
        return handleIncomingScheduling(session, source, incomingScheduling, null);
    }

    /**
     * Handles incoming calendar changes triggered by external attendees. Updates the calendar data
     * based on the transmitted calendar object resources.
     * <p>
     * Will check for sanity and consistency of the data as well as permissions of the user applying
     * the changes.
     *
     * @param session The calendar session
     * @param source The source from which the scheduling has been triggered
     * @param incomingScheduling The incoming change(s)
     * @param attendee The attendee to update, can be <code>null</code> e.g. when handling a CANCEL
     * @return A {@link CalendarResult} containing the updates
     * @throws OXException In case the change could not be applied, e.g. the transmitted data can't be
     *             parsed or is outdated
     */
    CalendarResult handleIncomingScheduling(CalendarSession session, SchedulingSource source, IncomingSchedulingMessage incomingScheduling, Attendee attendee) throws OXException;

}
