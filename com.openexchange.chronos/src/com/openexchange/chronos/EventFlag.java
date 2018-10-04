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
