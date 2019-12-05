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

package com.openexchange.chronos.scheduling.common;

import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.scheduling.changes.Change;
import com.openexchange.java.Strings;

/**
 * {@link Utils}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class Utils {

    private Utils() {}

    /**
     * Selects a specific event from a calendar object resource that is referenced by the supplied list of changes.
     * 
     * @param resource The calendar object resource to select the event from
     * @param changes The changes for which to get the event for
     * @return The described event, or the resource's first event if no better match could be selected
     */
    public static Event selectDescribedEvent(CalendarObjectResource resource, List<Change> changes) {
        if (null != changes && 0 < changes.size()) {
            RecurrenceId recurrenceId = changes.get(0).getRecurrenceId();
            if (null != recurrenceId) {
                Event event = resource.getChangeException(recurrenceId);
                if (null != event) {
                    return event;
                }
            }
        }
        return resource.getFirstEvent();
    }

    /**
     * Gets a calendar user's display name, falling back to his e-mail or URI properties as needed.
     * 
     * @param calendarUser The calendar user to get the display name from
     * @return The display name
     */
    public static String getDisplayName(CalendarUser calendarUser) {
        if (Strings.isNotEmpty(calendarUser.getCn())) {
            return calendarUser.getCn();
        }
        if (Strings.isNotEmpty(calendarUser.getEMail())) {
            return calendarUser.getEMail();
        }
        return CalendarUtils.extractEMailAddress(calendarUser.getUri());
    }

    /**
     * Gets a value indicating whether a calendar user represents an <i>internal</i> entity, an internal user, group or resource , or not.
     *
     * @param calendarUser The calendar user to check
     * @return <code>true</code> if the calendar user is internal, <code>false</code>, otherwise
     */
    public static boolean isInternalCalendarUser(CalendarUser calendarUser) {
        if (Attendee.class.isAssignableFrom(calendarUser.getClass())) {
            Attendee attendee = (Attendee) calendarUser;
            return CalendarUtils.isInternal(attendee);
        }
        return CalendarUtils.isInternal(calendarUser, CalendarUserType.INDIVIDUAL);
    }
}
