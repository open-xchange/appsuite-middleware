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
import java.util.Map;
import com.openexchange.chronos.Event;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.quota.Quota;

/**
 * {@link CalendarServiceUtilities}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
@SingletonService
public interface CalendarServiceUtilities {

    /**
     * Resolves an UID to the identifier of an existing event. The lookup is performed case-sensitive and context-wise, independently of
     * the current session user's access rights. If an event series with change exceptions is matched, the identifier of the recurring
     * <i>master</i> event is returned.
     *
     * @param session The calendar session
     * @param uid The UID to resolve
     * @return The identifier of the resolved event, or <code>null</code> if not found
     */
    String resolveByUID(CalendarSession session, String uid) throws OXException;

    /**
     * Resolves a resource filename to the identifier of an existing event. The lookup is performed context-wise, independently of the
     * current session user's access rights. If an event series with change exceptions is matched, the identifier of the recurring
     * <i>master</i> event is returned.
     *
     * @param session The calendar session
     * @param filename The filename to resolve
     * @return The identifier of the resolved event, or <code>null</code> if not found
     */
    String resolveByFilename(CalendarSession session, String filename) throws OXException;

    /**
     * Resolves an event identifier to an event, and returns it in the perspective of the current session's user, i.e. having an
     * appropriate parent folder identifier assigned.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_FIELDS}</li>
     * </ul>
     *
     * @param session The calendar session
     * @param id The identifier of the event to resolve
     * @return The resolved event from the user's point of view, or <code>null</code> if not found
     */
    Event resolveByID(CalendarSession session, String id) throws OXException;

    /**
     * Resolves a specific event (and any overridden instances or <i>change exceptions</i>) by its externally used resource name, which
     * typically matches the event's UID or filename property. The lookup is performed within a specific folder in a case-sensitive way.
     * If an event series with overridden instances is matched, the series master event will be the first event in the returned list.
     * <p/>
     * It is also possible that only overridden instances of an event series are returned, which may be the case for <i>detached</i>
     * instances where the user has no access to the corresponding series master event.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_FIELDS}</li>
     * </ul>
     *
     * @param session The calendar session
     * @param folderId The identifier of the folder to resolve the resource name in
     * @param resourceName The resource name to resolve
     * @return The resolved event(s), or <code>null</code> if no matching event was found
     * @see <a href="https://tools.ietf.org/html/rfc4791#section-4.1">RFC 4791, section 4.1</a>
     */
    List<Event> resolveResource(CalendarSession session, String folderId, String resourceName) throws OXException;

    /**
     * Resolves multiple events (and any overridden instances or <i>change exceptions</i>) by their externally used resource name, which
     * typically matches the event's UID or filename property. The lookup is performed within a specific folder in a case-sensitive way.
     * If an event series with overridden instances is matched, the series master event will be the first event in the returned list of
     * the corresponding events result.
     * <p/>
     * It is also possible that only overridden instances of an event series are returned, which may be the case for <i>detached</i>
     * instances where the user has no access to the corresponding series master event.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_FIELDS}</li>
     * </ul>
     *
     * @param session The calendar session
     * @param folderId The identifier of the folder to resolve the resource names in
     * @param resourceNames The resource names to resolve
     * @return The resolved event(s), mapped to their corresponding resource name
     * @see <a href="https://tools.ietf.org/html/rfc4791#section-4.1">RFC 4791, section 4.1</a>
     */
    Map<String, EventsResult> resolveResources(CalendarSession session, String folderId, List<String> resourceNames) throws OXException;

    /**
     * Gets a value indicating whether a specific folder contains events that were not created by the current session's user.
     *
     * @param session The calendar session
     * @param folderId The identifier of the folder to check the contained events in
     * @return <code>true</code> if there's at least one event located in the folder that is not created by the user, <code>false</code>, otherwise
     */
    boolean containsForeignEvents(CalendarSession session, String folderId) throws OXException;

    /**
     * Gets the number of events in a folder, which includes the sum of all non-recurring events, the series master events, and the
     * overridden exceptional occurrences from event series. Distinct object access permissions (e.g. <i>read own</i>) are not considered.
     *
     * @param session The calendar session
     * @param folderId The identifier of the folder to count the events in
     * @return The number of events contained in the folder, or <code>0</code> if there are none
     */
    long countEvents(CalendarSession session, String folderId) throws OXException;

    /**
     * Get the configured quotas and their actual usages of the underlying calendar account.
     *
     * @param session The calendar session
     * @return The configured quotas and the actual usages
     */
    Quota[] getQuotas(CalendarSession session) throws OXException;

    /**
     * Loads the recurrence data for an existing recurring event series.
     * <p/>
     * No permissions checks are performed, i.e. the recurrence data is also loaded for event series the current session's user cannot
     * access.
     *
     * @param session The calendar session
     * @param seriesId The identifier of the event series to load the recurrence data for
     * @return The recurrence data
     */
    RecurrenceData loadRecurrenceData(CalendarSession session, String seriesId) throws OXException;

}
