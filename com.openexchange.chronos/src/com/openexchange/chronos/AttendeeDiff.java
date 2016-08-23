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

package com.openexchange.chronos;

import java.util.List;

/**
 * {@link AttendeeDiff}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface AttendeeDiff {

    /**
     * Gets the list of newly added attendees.
     *
     * @return The added attendees, or an empty list if there are none
     */
    List<Attendee> getAddedAttendees();

    /**
     * Gets a filtered list of newly added attendees.
     *
     * @param internal {@link Boolean#TRUE} to only consider internal entities, {@link Boolean#FALSE} for non-internal ones,
     *            or <code>null</code> to not filter by internal/external
     * @param cuType The {@link CalendarUserType} to consider, or <code>null</code> to not filter by calendar user type
     * @return The added attendees, or an empty list if there are none
     */
    List<Attendee> getAddedAttendees(Boolean internal, CalendarUserType cuType);

    /**
     * Gets the list of removed attendees.
     *
     * @return The removed attendees, or an empty list if there are none
     */
    List<Attendee> getRemovedAttendees();

    /**
     * Gets a filtered list of removed attendees.
     *
     * @param internal {@link Boolean#TRUE} to only consider internal entities, {@link Boolean#FALSE} for non-internal ones,
     *            or <code>null</code> to not filter by internal/external
     * @param cuType The {@link CalendarUserType} to consider, or <code>null</code> to not filter by calendar user type
     * @return The removed attendees, or an empty list if there are none
     */
    List<Attendee> getRemovedAttendees(Boolean internal, CalendarUserType cuType);

    /**
     * Gets the list of updated attendees.
     *
     * @return The updated attendees, or an empty list if there are none
     */
    List<AttendeeUpdate> getUpdatedAttendees();

    /**
     * Gets a filtered list of updated attendees.
     *
     * @param internal {@link Boolean#TRUE} to only consider internal entities, {@link Boolean#FALSE} for non-internal ones,
     *            or <code>null</code> to not filter by internal/external
     * @param cuType The {@link CalendarUserType} to consider, or <code>null</code> to not filter by calendar user type
     * @return The updated attendees, or an empty list if there are none
     */
    List<AttendeeUpdate> getUpdatedAttendees(Boolean internal, CalendarUserType cuType);

    /**
     * Gets a value indicating whether the attendee diff is empty, if there are changes of any kind or not.
     *
     * @return <code>true</code> if there were no changes at all, <code>false</code>, otherwise
     */
    boolean isEmpty();

}
