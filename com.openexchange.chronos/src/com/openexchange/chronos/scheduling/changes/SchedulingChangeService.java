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

package com.openexchange.chronos.scheduling.changes;

import java.util.List;
import com.openexchange.chronos.CalendarObjectResource;
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
     * @param originator The originator of the message
     * @param comment An optional comment for ScheduleChange to the recipient
     * @param resource The cancel calendar object resource
     * @return A {@link ScheduleChange} for a {@link SchedulingMethod#CANCEL}
     * @throws OXException In case data is invalid or missing
     */
    ScheduleChange describeCancel(CalendarUser originator, String comment, CalendarObjectResource resource) throws OXException;

    /**
     * Generates the schedule change for a canceled instance of a recurring event series.
     * 
     * @param originator The originator of the message
     * @param comment The optional scheduling comment left by the originator for the recipient
     * @param resource The canceled calendar object resource
     * @param seriesMaster The series master event the canceled instance belongs to, or <code>null</code> if not available
     * @return The generated schedule change
     */
    ScheduleChange describeCancelInstance(CalendarUser originator, String comment, CalendarObjectResource resource, Event seriesMaster);

    /**
     * Gets a {@link ScheduleChange} for a {@link SchedulingMethod#COUNTER}
     * 
     * @param originator The originator of the message
     * @param comment An optional comment for ScheduleChange to the recipient
     * @param resource The countered calendar object resource
     * @param changes The changes done to the events, that triggered the counter
     * @param isExceptionCreate <code>true</code> if the update contains the master event as original event as per {@link EventUpdate#getOriginal()} and a new
     *            change exception as per {@link EventUpdate#getUpdate()}, <code>false</code> otherwise
     * @return A {@link ScheduleChange} for a {@link SchedulingMethod#CANCEL}
     * @throws OXException In case data is invalid or missing
     */
    ScheduleChange describeCounter(CalendarUser originator, String comment, CalendarObjectResource resource, List<Change> changes, boolean isExceptionCreate) throws OXException;

    /**
     * Gets a {@link ScheduleChange} for a {@link SchedulingMethod#DECLINE_COUNTER}
     *
     * @param originator The originator of the message
     * @param comment An optional comment for ScheduleChange to the recipient
     * @param resource The declined resource
     * @return A {@link ScheduleChange} for a {@link SchedulingMethod#DECLINE_COUNTER}
     * @throws OXException In case data is invalid or missing
     */
    ScheduleChange describeDeclineCounter(CalendarUser originator, String comment, CalendarObjectResource resource) throws OXException;

    /**
     * Gets a {@link ScheduleChange} for a {@link SchedulingMethod#REPLY}
     *
     * @param originator The originator of the message
     * @param comment An optional comment for ScheduleChange to the recipient
     * @param resource The calendar object resource being replied
     * @param change The changes done to the event
     * @param partStat The participant status of the <b>originator</b>
     * @return A {@link ScheduleChange} for a {@link SchedulingMethod#REPLY}
     * @throws OXException In case data is invalid or missing
     */
    ScheduleChange describeReply(CalendarUser originator, String comment, CalendarObjectResource resource, List<Change> change, ParticipationStatus partStat) throws OXException;

    /**
     * Generates the schedule change for a reply to an instance of a recurring event series.
     * 
     * @param originator The originator of the message
     * @param comment The optional scheduling comment left by the originator for the recipient
     * @param resource The calendar object resource holding containing the reply
     * @param seriesMaster The series master event the replied instance belongs to, or <code>null</code> if not available
     * @param changes The descriptions of the particular changes
     * @param partStat The new participation status of the reply
     * @return The generated schedule change
     */
    ScheduleChange describeReplyInstance(CalendarUser originator, String comment, CalendarObjectResource resource, Event seriesMaster, List<Change> changes, ParticipationStatus partStat);

    /**
     * Gets a {@link ScheduleChange} for a {@link SchedulingMethod#REQUEST} in case of a new created event
     * 
     * @param originator The originator of the message
     * @param comment An optional comment for ScheduleChange to the recipient
     * @param resource The updated calendar object resource
     * @return A {@link ScheduleChange} for a {@link SchedulingMethod#REQUEST}
     * @throws OXException In case data is invalid or missing
     */
    ScheduleChange describeCreationRequest(CalendarUser originator, String comment, CalendarObjectResource resource) throws OXException;

    /**
     * Gets a {@link ScheduleChange} for a {@link SchedulingMethod#REQUEST} in case of an updated event
     * 
     * @param originator The originator of the message
     * @param comment An optional comment for ScheduleChange to the recipient
     * @param resource The updated calendar object resource
     * @param changes The changes done to the events
     * @return A {@link ScheduleChange} for a {@link SchedulingMethod#REQUEST}
     * @throws OXException In case data is invalid or missing
     */
    ScheduleChange describeUpdateRequest(CalendarUser originator, String comment, CalendarObjectResource resource, List<Change> changes) throws OXException;

    /**
     * Generates the schedule change for an updated instance of a recurring event series.
     * 
     * @param originator The originator of the message
     * @param comment The optional scheduling comment left by the originator for the recipient
     * @param resource The updated calendar object resource
     * @param seriesMaster The series master event the updated instance belongs to, or <code>null</code> if not available
     * @param changes The descriptions of the particular changes
     * @return The generated schedule change
     */
    ScheduleChange describeUpdateInstance(CalendarUser originator, String comment, CalendarObjectResource resource, Event seriesMaster, List<Change> changes);

    /**
     * Gets a {@link ScheduleChange} for a {@link SchedulingMethod#REQUEST} in case of an new change exception for an existing event
     * 
     * @param originator The originator of the message
     * @param comment An optional comment for ScheduleChange to the recipient
     * @param resource The updated calendar object resource
     * @param changes The changes done (compared to the master event) that caused the new exceptions
     * @param isExceptionCreate <code>true</code> if an exception has been created
     * @return A {@link ScheduleChange} for a {@link SchedulingMethod#REQUEST}
     * @throws OXException In case data is invalid or missing
     */
    ScheduleChange describeNewException(CalendarUser originator, String comment, CalendarObjectResource resource, List<Change> changes) throws OXException;

}
