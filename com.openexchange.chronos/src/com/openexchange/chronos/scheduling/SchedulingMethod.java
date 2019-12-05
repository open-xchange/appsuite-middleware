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
