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
import com.openexchange.chronos.EventField;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link CalendarService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface CalendarService {

    /**
     * Initializes a new calendar session.
     *
     * @param session The underlying server session
     * @return A new calendar session
     */
    CalendarSession init(Session session) throws OXException;

    /**
     * Resolves an UID to the identifier of an existing event. The lookup is performed context-wise, independently of the current session
     * user's access rights. If an event series with change exceptions is matched, the identifier of the recurring <i>master</i> event is
     * returned.
     *
     * @param session The calendar session
     * @param uid The UID to resolve
     * @return The identifier of the resolved event, or <code>0</code> if not found
     */
    int resolveByUID(CalendarSession session, String uid) throws OXException;

    /**
     * Gets the sequence number of a calendar folder, which is the highest last-modification timestamp of the folder itself and his
     * contents. Distinct object access permissions (e.g. "read own") are not considered.
     *
     * @param session The calendar session
     * @param folderID The identifier of the folder to get the sequence number for
     * @return The sequence number
     */
    long getSequenceNumber(CalendarSession session, int folderID) throws OXException;

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
     * Searches for events by pattern in the fields {@link EventField#SUMMARY}, {@link EventField#DESCRIPTION} and
     * {@link EventField#CATEGORIES}. The pattern is surrounded by wildcards implicitly to follow a <i>contains</i> semantic.
     *
     * @param session The calendar session
     * @param folderIDs The identifiers of the folders to perform the search in, or <code>null</code> to search across all visible folders
     * @param pattern The pattern to search for
     * @return The found events, or an empty list if there are none
     */
    List<UserizedEvent> searchEvents(CalendarSession session, int[] folderIDs, String pattern) throws OXException;

    /**
     * Gets all change exceptions of a recurring event series.
     *
     * @param session The calendar session
     * @param folderID The identifier of the folder representing the current user's calendar view
     * @param seriesID The identifier of the series to get the change exceptions for
     * @return The change exceptions, or an empty list if there are none
     */
    List<UserizedEvent> getChangeExceptions(CalendarSession session, int folderID, int seriesID) throws OXException;

    /**
     * Gets a specific event.
     *
     * @param session The calendar session
     * @param folderID The identifier of the folder representing the current user's calendar view
     * @param objectID The identifier of the event to get
     * @return The event
     */
    UserizedEvent getEvent(CalendarSession session, int folderID, int objectID) throws OXException;

    /**
     * Gets a list of events.
     *
     * @param session The calendar session
     * @param folderID The identifier of the folder representing the current user's calendar view
     * @param eventIDs A list of the identifiers of the events to get
     * @return The events
     */
    List<UserizedEvent> getEvents(CalendarSession session, List<EventID> eventIDs) throws OXException;

    /**
     * Gets all events in a specific calendar folder.
     *
     * @param session The calendar session
     * @param folderID The identifier of the folder to get the events from
     * @return The events
     */
    List<UserizedEvent> getEventsInFolder(CalendarSession session, int folderID) throws OXException;

    /**
     * Gets all events of the session's user.
     *
     * @param session The calendar session
     * @return The events
     */
    List<UserizedEvent> getEventsOfUser(CalendarSession session) throws OXException;

    UpdatesResult getUpdatedEventsInFolder(CalendarSession session, int folderID, Date updatedSince) throws OXException;

    UpdatesResult getUpdatedEventsOfUser(CalendarSession session, Date updatedSince) throws OXException;

    CalendarResult createEvent(CalendarSession session, UserizedEvent event) throws OXException;

    CalendarResult updateEvent(CalendarSession session, EventID eventID, UserizedEvent event) throws OXException;

    CalendarResult updateAttendee(CalendarSession session, EventID eventID, Attendee attendee) throws OXException;

    Map<EventID, CalendarResult> deleteEvents(CalendarSession session, List<EventID> eventIDs) throws OXException;

}
