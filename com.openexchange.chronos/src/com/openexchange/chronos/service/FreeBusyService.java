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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.exception.OXException;

/**
 * {@link FreeBusyService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface FreeBusyService {

    /**
     * Gets an array of <code>boolean</code> values representing the days where the current session's user has events at.
     *
     * @param session The calendar session
     * @param from The start date of the period to consider
     * @param until The end date of the period to consider
     * @return The "has" result, i.e. an array of <code>boolean</code> values representing the days where the user has events at
     */
    boolean[] hasEventsBetween(CalendarSession session, Date from, Date until) throws OXException;

    /**
     * Gets free/busy information in a certain interval for one ore more attendees. Only <i>internal</i> attendees of types
     * {@link CalendarUserType#INDIVIDUAL} and {@link CalendarUserType#RESOURCE} are considered.
     *
     * @param session The calendar session
     * @param attendees The attendees to get the free/busy data for
     * @param from The start of the requested time range
     * @param until The end of the requested time range
     * @return The free/busy data for the attendees, which are stripped down event objects based on the current session user's access permissions for the events
     */
    Map<Attendee, List<UserizedEvent>> getFreeBusy(CalendarSession session, List<Attendee> attendees, Date from, Date until) throws OXException;

    /**
     * Checks for potential conflicting events of the attendees with another event, typically prior event creation or update.
     *
     * @param session The calendar session
     * @param event The event to check (usually the event being created/updated)
     * @param attendees The attendees to check
     * @return The conflicts, or an empty list if there are none
     */
    List<EventConflict> checkForConflicts(CalendarSession session, Event event, List<Attendee> attendees) throws OXException;

}
