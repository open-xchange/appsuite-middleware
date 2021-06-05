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

package com.openexchange.chronos.itip.generators;

import java.util.List;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * {@link NotificationParticipantResolver}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface NotificationParticipantResolver {

    /**
     * Resolves {@link com.openexchange.chronos.Attendee}s to {@link NotificationParticipant}s
     * 
     * @param original The original {@link Event}. Can be <code>null</code>
     * @param update The updated or new {@link Event}
     * @param user The current {@link User}
     * @param onBehalfOf The {@link User} to act and send mails on its behalf
     * @param ctx The {@link Context} of the user
     * @param session The users {@link Session}
     * @param principal The principal
     * @return A {@link List} of {@link NotificationParticipant}s for the given event
     * @throws OXException If user, organizer, folder ID, etc. can't be resolved
     */
    List<NotificationParticipant> resolveAllRecipients(Event original, Event update, User user, User onBehalfOf, Context ctx, Session session, CalendarUser principal) throws OXException;

    /**
     * Get all internal and external participants that attendee the given event. Efficiently removes
     * resources and groups from the given list of {@link NotificationParticipant}s.
     * 
     * @param allRecipients The {@link NotificationParticipant} to filter
     * @param event The event to get the attendees from
     * @return A filtered List of {@link NotificationParticipant}s that attend the event
     */
    List<NotificationParticipant> getAllParticipants(List<NotificationParticipant> allRecipients, Event event);

    /**
     * Get all resources of the given event and converts them to {@link NotificationParticipant}s
     * 
     * @param event The {@link Event}
     * @return A {@link List} of {@link NotificationParticipant}
     */
    List<NotificationParticipant> getResources(Event event);
}
