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

import com.openexchange.chronos.scheduling.annotations.AttendeeMethod;
import com.openexchange.chronos.scheduling.annotations.AttendeeMethod.ROLE;
import com.openexchange.chronos.scheduling.annotations.OrganizerMethod;

/**
 * {@link SchedulingMethod}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public enum SchedulingMethod {

    /**
     * The <code>ADD</code> method. This method is used to add new instances to an existing event,
     * in other words, adding additional event exceptions to a series.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5546#section-3.2.4">RFC5546 Section 3.2.4</a>
     */
    @OrganizerMethod
    ADD,

    /**
     * The <code>CANCLE</code> method. This method is used to cancel or delete on or more event
     * occurrences.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5546#section-3.2.5">RFC5546 Section 3.2.5</a>
     */
    @OrganizerMethod
    CANCEL,

    /**
     * The <code>COUNTER</code> method. This method is used to propose changes to the organizer.
     * It can be refused by the organizer with a {@link #DECLINE_COUNTER}
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5546#section-3.2.7">RFC5546 Section 3.2.7</a>
     */
    @AttendeeMethod
    COUNTER,

    /**
     * The <code>DECLINE_COUNTER</code> method. This method is used to refuse a proposal made
     * via the {@link #COUNTER} method
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5546#section-3.2.8">RFC5546 Section 3.2.8</a>
     */
    @OrganizerMethod
    DECLINE_COUNTER,

    /**
     * The <code>REFRESH</code> method. The method is used to get the current state of an event.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5546#section-3.2.6">RFC5546 Section 3.2.6</a>
     */
    @AttendeeMethod
    REFRESH,

    /**
     * The <code>REPLY</code> method. This method is used to answer to an {@link #REQUEST}. Used
     * to set the status of the acting attendee.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5546#section-3.2.3">RFC5546 Section 3.2.3</a>
     */
    @AttendeeMethod
    REPLY,

    /**
     * The <code>REQUEST</code> method. This method is used to create and update all information
     * about an event or a series.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5546#section-3.2.2">RFC5546 Section 3.2.2</a>
     */
    @OrganizerMethod
    @AttendeeMethod(role = ROLE.ON_BEHALF_OF)
    REQUEST,

    /**
     * The <code>PUBLISH</code> method. This method is used to publish a specific event without
     * the need to add attendees to the event.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5546#section-3.2.1">RFC5546 Section 3.2.1</a>
     */
    @OrganizerMethod
    PUBLISH;
}
