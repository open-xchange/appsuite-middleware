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

package com.openexchange.chronos.itip.generators;

import java.util.List;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;

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
