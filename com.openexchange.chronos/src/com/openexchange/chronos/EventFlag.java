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

package com.openexchange.chronos;

/**
 * {@link EventFlag}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public enum EventFlag {

    /**
     * The event contains at least one attachment.
     */
    ATTACHMENTS,
    /**
     * The event contains at least one conference.
     */
    CONFERENCES,
    /**
     * The calendar user has at least one alarm associated with the event.
     */
    ALARMS,
    /**
     * Event is a <i>group-scheduled</i> meeting with an organizer.
     */
    SCHEDULED,
    /**
     * The calendar user is the <i>organizer<i> of the meeting.
     */
    ORGANIZER,
    /**
     * The calendar user is the <i>organizer<i> of the meeting, and the current user acts on behalf of him.
     */
    ORGANIZER_ON_BEHALF,
    /**
     * The calendar user is <i>attendee<i> of the meeting.
     */
    ATTENDEE,
    /**
     * The calendar user is <i>attendee<i> of the meeting, and the current user acts on behalf of him.
     */
    ATTENDEE_ON_BEHALF,
    /**
     * Event is classified <i>private</i>, so is invisible for others.
     */
    PRIVATE,
    /**
     * Event is classified as <i>confidential</i>, so only start and end time are visible for others.
     */
    CONFIDENTIAL,
    /**
     * Event is <i>transparent</i> for the calendar user, i.e. invisible to free/busy time searches.
     */
    TRANSPARENT,
    /**
     * Indicates that the event's overall status is <i>tentative</i>.
     */
    EVENT_TENTATIVE,
    /**
     * Indicates that the event's overall status is <i>definite</i>.
     */
    EVENT_CONFIRMED,
    /**
     * Indicates that the event's overall status is <i>cancelled</i>.
     */
    EVENT_CANCELLED,
    /**
     * The calendar user's participation status is <i>needs action</i>.
     */
    NEEDS_ACTION,
    /**
     * The calendar user's participation status is <i>accepted</i>.
     */
    ACCEPTED,
    /**
     * The calendar user's participation status is <i>declined</i>.
     */
    DECLINED,
    /**
     * The calendar user's participation status is <i>tentative</i>.
     */
    TENTATIVE,
    /**
     * The calendar user's participation status is <i>delegated</i>.
     */
    DELEGATED,
    /**
     * The event represents the <i>master</i> of a recurring event series, or an expanded (regular) occurrence of a series.
     */
    SERIES,
    /**
     * The event represents an exception / overridden instance of a recurring event series.
     */
    OVERRIDDEN,
    /**
     * The event represents the <i>first</i> occurrence of a recurring event series.
     */
    FIRST_OCCURRENCE,
    /**
     * The event represents the <i>last</i> occurrence of a recurring event series.
     */
    LAST_OCCURRENCE,

    ;
}
