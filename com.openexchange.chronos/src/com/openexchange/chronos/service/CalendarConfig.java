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

package com.openexchange.chronos.service;

import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.exception.OXException;

/**
 * {@link CalendarConfig}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface CalendarConfig {

    /**
     * Gets the identifier of a specific user's default personal calendar folder.
     *
     * @param userId The identifier of the user to retrieve the default calendar identifier for
     * @return The default calendar folder identifier
     */
    String getDefaultFolderId(int userId) throws OXException;

    /**
     * Gets the initial participation status to use for new events in a specific folder.
     *
     * @param userId The identifier of the user to get the participation status for
     * @param inPublicFolder <code>true</code> if the event is located in a <i>public</i> folder, <code>false</code>, otherwise
     * @return The initial participation status, or {@link ParticipationStatus#NEEDS_ACTION} if not defined
     */
    ParticipationStatus getInitialPartStat(int userId, boolean inPublicFolder);

    /**
     * Gets the default alarms to be applied to events whose start-date is of type <i>date</i> from the underlying user configuration.
     *
     * @param userId The identifier of the user to get the default alarm for
     * @return The default alarms, or <code>null</code> if not defined
     */
    List<Alarm> getDefaultAlarmDate(int userId) throws OXException;

    /**
     * Gets the default alarms to be applied to events whose start-date is of type <i>date-time</i> from the underlying user configuration.
     *
     * @param userId The identifier of the user to get the default alarm for
     * @return The default alarms, or <code>null</code> if not defined
     */
    List<Alarm> getDefaultAlarmDateTime(int userId) throws OXException;

    /**
     * Gets the defined availability (in form of one or more available definitions) from the underlying user configuration.
     *
     * @param userId The identifier of the user to get the availability for
     * @return The availability, or <code>null</code> if not defined
     */
    Available[] getAvailability(int userId) throws OXException;

    /**
     * Gets a value indicating whether newly added group attendees should be resolved to their individual members, without preserving the
     * group reference, or not.
     *
     * @return <code>true</code> if group attendees should be resolved, <code>false</code>, otherwise
     */
    boolean isResolveGroupAttendees();

    /**
     * Gets the configured minimum search pattern length.
     *
     * @return The minimum search pattern length, or <code>0</code> for no limitation
     */
    int getMinimumSearchPatternLength() throws OXException;

    /**
     * Gets the configured maximum number of conflicts between two recurring event series.
     *
     * @return The maximum conflicts per recurrence
     */
    int getMaxConflictsPerRecurrence();

    /**
     * Gets the configured maximum number of attendees to indicate per conflict.
     *
     * @return The the maximum number of attendees to indicate per conflict
     */
    int getMaxAttendeesPerConflict();

    /**
     * Gets the overall maximum number of conflicts to return.
     *
     * @return The the maximum number of conflicts to return
     */
    int getMaxConflicts();

    /**
     * Gets a value indicating whether the checks of (external) attendee URIs are generally disabled or not.
     *
     * @return <code>true</code> if the URI checks are disabled, <code>false</code>, otherwise
     */
    boolean isSkipExternalAttendeeURIChecks();

    /**
     * Gets a value indicating whether attendee changes to events located in <i>private</i> or <i>shared</i> folders should be restricted
     * according to <a href="https://tools.ietf.org/html/rfc6638#section-3.2.2.1">RFC 6638, section 3.2.2.1</a> or not, which effectively
     * restricts any changes to the calendar scheduling resource to be performed by the organizer only.
     *
     * @return <code>true</code> if allowed attendee changes should be restricted, <code>false</code>, otherwise
     */
    boolean isRestrictAllowedAttendeeChanges();

    /**
     * Gets a value indicating whether attendee changes to events located in <i>public</i> folders should be restricted according to
     * <a href="https://tools.ietf.org/html/rfc6638#section-3.2.2.1">RFC 6638, section 3.2.2.1</a> or not, which effectively restricts
     * any changes to the calendar scheduling resource to be performed by the organizer only.
     *
     * @return <code>true</code> if allowed attendee changes should be restricted, <code>false</code>, otherwise
     */
    boolean isRestrictAllowedAttendeeChangesPublic();

}
