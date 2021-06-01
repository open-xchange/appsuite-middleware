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

package com.openexchange.chronos.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
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
     * the current session user's access rights.
     * <p/>
     * If an event series with change exceptions is matched, the identifier of the recurring <i>master</i> event is returned.
     *
     * @param session The calendar session
     * @param uid The UID to resolve
     * @return The identifier of the resolved event, or <code>null</code> if not found
     */
    String resolveByUID(CalendarSession session, String uid) throws OXException;

    /**
     * Resolves an UID to the identifier of an existing event. The lookup is performed case-sensitive and context-wise, independently of
     * the current session user's access rights, within the scope of a specific calendar user. I.e., the unique identifier is resolved to
     * events residing in the user's <i>personal</i>, as well as <i>public</i> calendar folders.
     * <p/>
     * If an event series with change exceptions is matched, the identifier of the recurring <i>master</i> event is returned.
     *
     * @param session The calendar session
     * @param uid The UID to resolve
     * @param calendarUserId The identifier of the calendar user the unique identifier should be resolved for
     * @return The identifier of the resolved event, or <code>null</code> if not found
     */
    String resolveByUID(CalendarSession session, String uid, int calendarUserId) throws OXException;

    /**
     * Resolves an UID to all events belonging to the corresponding calendar object resource. The lookup is performed case-sensitive,
     * within the scope of a specific calendar user. I.e., the unique identifier is resolved to events residing in the user's
     * <i>personal</i>, as well as <i>public</i> calendar folders.
     * <p/>
     * The events will be <i>userized</i> to reflect the view of the calendar user on the events.
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_FIELDS}</li>
     * </ul>
     * 
     * @param session The calendar session
     * @param uid The UID to resolve
     * @param calendarUserId The identifier of the calendar user the unique identifier should be resolved for
     * @return The <i>userized</i> events, or an empty list if no events were found
     */
    List<Event> resolveEventsByUID(CalendarSession session, String uid, int calendarUserId) throws OXException;

    /**
     * Resolves an UID to the identifier of the folder where the corresponding calendar object resource is located in. The lookup is
     * performed case-sensitive, within the scope of a specific calendar user. I.e., the unique identifier is resolved to the folder view
     * on the events residing in the user's <i>personal</i>, as well as <i>public</i> calendar folders.
     * 
     * @param session The calendar session
     * @param uid The UID to resolve
     * @param calendarUserId The identifier of the calendar user the unique identifier should be resolved for
     * @param fallbackToDefault <code>true</code> to fall back to the calendar user's default folder, <code>false</code>, otherwise
     * @return The folder id where the events are located in, or <code>null</code> respectively the default folder as fallback if no such events were found
     */
    String resolveFolderIdByUID(CalendarSession session, String uid, int calendarUserId, boolean fallbackToDefault) throws OXException;

    /**
     * Resolves an UID and optional recurrence identifier pair to the identifier of an existing event. The lookup is performed case-
     * sensitive and context-wise, independently of the current session user's access rights, within the scope of a specific calendar user.
     * I.e., the unique identifier and recurrence identifier are resolved to events residing in the user's <i>personal</i>, as well as
     * <i>public</i> calendar folders.
     * <p/>
     * If no recurrence identifier is given and an event series with change exceptions is matched, the identifier of the recurring
     * <i>master</i> event is returned.
     *
     * @param session The calendar session
     * @param uid The UID to resolve
     * @param recurrenceId The recurrence identifier to match, or <code>null</code> to resolve to non-recurring or series master events only
     * @param calendarUserId The identifier of the calendar user the unique identifier should be resolved for
     * @return The identifier of the resolved event, or <code>null</code> if not found
     */
    String resolveByUID(CalendarSession session, String uid, RecurrenceId recurrenceId, int calendarUserId) throws OXException;

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
     * @param sequence The expected sequence number to match, or <code>null</code> to resolve independently of the event's sequence number
     * @return The resolved event from the user's point of view, or <code>null</code> if not found
     */
    Event resolveByID(CalendarSession session, String id, Integer sequence) throws OXException;
    
    /**
     * Resolves an UID to the identifier of an existing event. The lookup is performed case-sensitive and context-wise, independently of
     * the current session user's access rights.
     * <p/>
     * If an event series with change exceptions is matched, the identifier of the recurring <i>master</i> event is returned.
     *
     * @param session The calendar session
     * @param id The identifier of the event to resolve
     * @param sequence The expected sequence number to match, or <code>null</code> to resolve independently of the event's sequence number
     * @param calendarUserId The identifier of the calendar user the unique identifier should be resolved for
     * @return The identifier of the resolved event, or <code>null</code> if not found
     * @throws OXException If permissions are missing
     */
    Event resolveByID(CalendarSession session, String id, Integer sequence, int calendarUserId) throws OXException;

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

    /**
     * Gets the registered calendar service interceptors.
     * 
     * @return The calendar service interceptors, or an emoty set if there are none.
     */
    Set<CalendarInterceptor> getInterceptors();

}
