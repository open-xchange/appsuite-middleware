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

package com.openexchange.chronos.storage;

import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.exception.OXException;

/**
 * {@link AttendeeStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface AttendeeStorage {

    /**
     * Loads all attendees for a specific event.
     *
     * @param objectID The identifier of the event to load the attendees for
     * @return The attendees
     */
    List<Attendee> loadAttendees(String objectID) throws OXException;

    /**
     * Loads the attendees for specific events.
     *
     * @param objectIDs The identifiers of the events to load the attendees for
     * @return The attendees, mapped to the identifiers of the corresponding events
     */
    Map<String, List<Attendee>> loadAttendees(String[] objectIDs) throws OXException;

    /**
     * Loads the attendees for specific events.
     *
     * @param objectIDs The identifiers of the events to load the attendees for
     * @param internal {@link Boolean#TRUE} to only consider internal entities, {@link Boolean#FALSE} for non-internal ones,
     *            or <code>null</code> to not filter by internal/external
     * @return The attendees, mapped to the identifiers of the corresponding events
     */
    Map<String, List<Attendee>> loadAttendees(String[] objectIDs, Boolean internal) throws OXException;

    void deleteAttendees(String objectID) throws OXException;

    void deleteAttendees(String objectID, List<Attendee> attendees) throws OXException;

    void insertAttendees(String objectID, List<Attendee> attendees) throws OXException;

    void updateAttendees(String objectID, List<Attendee> attendees) throws OXException;

    void updateAttendee(String objectID, Attendee attendee) throws OXException;

    void insertTombstoneAttendees(String objectID, List<Attendee> attendees) throws OXException;

    void insertTombstoneAttendee(String objectID, Attendee attendee) throws OXException;

}
