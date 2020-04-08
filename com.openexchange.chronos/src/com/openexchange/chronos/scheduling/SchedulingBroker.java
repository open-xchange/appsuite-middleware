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

package com.openexchange.chronos.scheduling;

import java.util.List;
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
    CalendarResult handleIncomingScheduling(CalendarSession session, SchedulingSource source, IncomingSchedulingMessage incomingScheduling) throws OXException;

}
