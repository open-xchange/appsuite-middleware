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

package com.openexchange.chronos.scheduling.changes;

import java.util.List;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.exception.OXException;

/**
 * {@link SchedulingChangeService}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public interface SchedulingChangeService {

    /**
     * Gets a {@link ScheduleChange} for a {@link SchedulingMethod#CANCEL}
     * 
     * @param contextId The context identifier
     * @param originator The originator of the message
     * @param recipient The recipient of the message
     * @param comment An optional comment for ScheduleChange to the recipient
     * @param removedEvents The removed events to cancel
     * @return A {@link ScheduleChange} for a {@link SchedulingMethod#CANCEL}
     * @throws OXException In case data is invalid or missing
     */
    ScheduleChange describeCancel(int contextId, CalendarUser originator, CalendarUser recipient, String comment, List<Event> removedEvents) throws OXException;

    /**
     * Gets a {@link ScheduleChange} for a {@link SchedulingMethod#COUNTER}
     * 
     * @param contextId The context identifier
     * @param originator The originator of the message
     * @param recipient The recipient of the message
     * @param comment An optional comment for ScheduleChange to the recipient
     * @param countered A list of event to counter
     * @param changes The changes done to the events, that triggered the counter
     * @param isExceptionCreate <code>true</code> if the update contains the master event as original event as per {@link EventUpdate#getOriginal()} and a new
     *            change exception as per {@link EventUpdate#getUpdate()}, <code>false</code> otherwise
     * @return A {@link ScheduleChange} for a {@link SchedulingMethod#CANCEL}
     * @throws OXException In case data is invalid or missing
     */
    ScheduleChange describeCounter(int contextId, CalendarUser originator, CalendarUser recipient, String comment, List<Event> countered, List<Change> changes, boolean isExceptionCreate) throws OXException;

    /**
     * Gets a {@link ScheduleChange} for a {@link SchedulingMethod#DECLINE_COUNTER}
     *
     * @param contextId The context identifier
     * @param originator The originator of the message
     * @param recipient The recipient of the message
     * @param comment An optional comment for ScheduleChange to the recipient
     * @param declinedEvent The declined event
     * @return A {@link ScheduleChange} for a {@link SchedulingMethod#DECLINE_COUNTER}
     * @throws OXException In case data is invalid or missing
     */
    ScheduleChange describeDeclineCounter(int contextId, CalendarUser originator, CalendarUser recipient, String comment, List<Event> declinedEvent) throws OXException;

    /**
     * 
     * Gets a {@link ScheduleChange} for a {@link SchedulingMethod#REPLY}
     *
     * @param contextId The context identifier
     * @param originator The originator of the message
     * @param recipient The recipient of the message
     * @param comment An optional comment for ScheduleChange to the recipient
     * @param updated The events to reply to
     * @param change The changes done to the event
     * @param partStat The participant status of the <b>originator</b>
     * @return A {@link ScheduleChange} for a {@link SchedulingMethod#REPLY}
     * @throws OXException In case data is invalid or missing
     */
    ScheduleChange describeReply(int contextId, CalendarUser originator, CalendarUser recipient, String comment, List<Event> updated, List<Change> change, ParticipationStatus partStat) throws OXException;

    /**
     * 
     * Gets a {@link ScheduleChange} for a {@link SchedulingMethod#REQUEST} in case of a new created event
     * 
     * @param contextId The context identifier
     * @param originator The originator of the message
     * @param recipient The recipient of the message
     * @param comment An optional comment for ScheduleChange to the recipient
     * @param created The newly created event
     * @return A {@link ScheduleChange} for a {@link SchedulingMethod#REQUEST}
     * @throws OXException In case data is invalid or missing
     */
    ScheduleChange describeCreationRequest(int contextId, CalendarUser originator, CalendarUser recipient, String comment, List<Event> created) throws OXException;

    /**
     * 
     * Gets a {@link ScheduleChange} for a {@link SchedulingMethod#REQUEST} in case of an updated event
     * 
     * @param contextId The context identifier
     * @param originator The originator of the message
     * @param recipient The recipient of the message
     * @param comment An optional comment for ScheduleChange to the recipient
     * @param updated The updated events
     * @param changes The changes done to the events
     * @return A {@link ScheduleChange} for a {@link SchedulingMethod#REQUEST}
     * @throws OXException In case data is invalid or missing
     */
    ScheduleChange describeUpdateRequest(int contextId, CalendarUser originator, CalendarUser recipient, String comment, List<Event> updated, List<Change> changes) throws OXException;

    /**
     * 
     * Gets a {@link ScheduleChange} for a {@link SchedulingMethod#REQUEST} in case of an new change exception for an existing event
     * 
     * @param contextId The context identifier
     * @param originator The originator of the message
     * @param recipient The recipient of the message
     * @param comment An optional comment for ScheduleChange to the recipient
     * @param updated The new change exceptions
     * @param changes The changes done (compared to the master event) that caused the new exceptions
     * @param isExceptionCreate <code>true</code> if an exception has been created
     * @return A {@link ScheduleChange} for a {@link SchedulingMethod#REQUEST}
     * @throws OXException In case data is invalid or missing
     */
    ScheduleChange describeNewException(int contextId, CalendarUser originator, CalendarUser recipient, String comment, List<Event> updated, List<Change> changes) throws OXException;

}
