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
import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.exception.OXException;

/**
 * {@link EntityResolver}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface EntityResolver {

    /**
     * Prepares a client-supplied calendar user. This includes:
     * <ul>
     * <li>Resolving external entities to their corresponding internal entities, if a matching calendar user is found by the URI value</li>
     * <li>Verifying the existence of internal calendar user entities</li>
     * <li>Applying further static properties of internal calendar users, which typically includes the calendar user's common name and the
     * actual calendar user address</li>
     * </ul>
     *
     * @param calendarUser The calendar user to prepare
     * @param cuType The expected calendar user type, or <code>null</code> if not specified
     * @return The passed calendar user reference, possibly enriched by the resolved static entity data if a matching internal calendar user was found
     */
    <T extends CalendarUser> T prepare(T calendarUser, CalendarUserType cuType) throws OXException;

    /**
     * Prepares a client-supplied calendar user. This includes:
     * <ul>
     * <li>Resolving external entities to their corresponding internal entities, if a matching calendar user is found by the URI value,
     * and it is listed as <i>resolvable</i></li>
     * <li>Verifying the existence of internal calendar user entities</li>
     * <li>Applying further static properties of internal calendar users, which typically includes the calendar user's common name and the
     * actual calendar user address</li>
     * </ul>
     *
     * @param calendarUser The calendar user to prepare
     * @param cuType The expected calendar user type, or <code>null</code> if not specified
     * @param resolvableEntities A whitelist of identifiers of those entities that should be resolved by their URI value, or
     *            <code>null</code> to resolve all resolvable entities
     * @return The passed calendar user reference, possibly enriched by the resolved static entity data if a matching internal calendar user was found
     */
    <T extends CalendarUser> T prepare(T calendarUser, CalendarUserType cuType, int[] resolvableEntities) throws OXException;

    /**
     * Prepares a list of client-supplied attendees; (internal) resource identifiers are <b>not</b> resolved. This includes:
     * <ul>
     * <li>Resolving external entities to their corresponding internal entities, if a matching calendar user is found by the URI value</li>
     * <li>Verifying the existence of internal calendar user entities</li>
     * <li>Applying further static properties of internal calendar users, which typically includes the calendar user's common name and the
     * actual calendar user address</li>
     * </ul>
     *
     * @param attendees The attendees to prepare
     * @return The passed attendee list, with each entry being possibly enriched by the resolved static entity data if a matching internal attendee was found
     */
    List<Attendee> prepare(List<Attendee> attendees) throws OXException;

    /**
     * Prepares a list of client-supplied attendees. This includes:
     * <ul>
     * <li>Resolving external entities to their corresponding internal entities, if a matching calendar user is found by the URI value</li>
     * <li>Verifying the existence of internal calendar user entities</li>
     * <li>Applying further static properties of internal calendar users, which typically includes the calendar user's common name and the
     * actual calendar user address</li>
     * </ul>
     *
     * @param attendees The attendees to prepare
     * @param resolveResourceIds <code>true</code> to resolve (internal) resource identifiers, <code>false</code>, otherwise
     * @return The passed attendee list, with each entry being possibly enriched by the resolved static entity data if a matching internal attendee was found
     */
    List<Attendee> prepare(List<Attendee> attendees, boolean resolveResourceIds) throws OXException;

    /**
     * Generates a copy from a client-supplied attendee and prepares it. This includes:
     * <ul>
     * <li>Resolving external entities to their corresponding internal entities, if a matching calendar user is found by the URI value</li>
     * <li>Verifying the existence of internal calendar user entities</li>
     * <li>Applying further static properties of internal calendar users, which typically includes the calendar user's common name and the
     * actual calendar user address</li>
     * </ul>
     *
     * @param attendee The attendee to prepare
     * @param resolveResourceIds <code>true</code> to resolve (internal) resource identifiers, <code>false</code>, otherwise
     * @return A copied attendee reference, being possibly enriched by the resolved static entity data if a matching internal attendee was found
     */
    Attendee prepare(Attendee attendees, boolean resolveResourceIds) throws OXException;

    /**
     * Prepares a list of client-supplied attendees. This includes:
     * <ul>
     * <li>Resolving external entities to their corresponding internal entities, if a matching calendar user is found by the URI value,
     * and it is listed as <i>resolvable</i></li>
     * <li>Verifying the existence of internal calendar user entities</li>
     * <li>Applying further static properties of internal calendar users, which typically includes the calendar user's common name and the
     * actual calendar user address</li>
     * </ul>
     *
     * @param attendees The attendees to prepare
     * @param resolvableEntities A whitelist of identifiers of those entities that should be resolved by their URI value, or
     *            <code>null</code> to resolve all resolvable entities
     * @return The passed attendee list, with each entry being possibly enriched by the resolved static entity data if a matching internal attendee was found
     */
    List<Attendee> prepare(List<Attendee> attendees, int[] resolvableEntities) throws OXException;

    /**
     * Gets the user identifiers of the members of a specific internal group.
     *
     * @param groupID The identifier of the group to get the members for
     * @return The group members
     */
    int[] getGroupMembers(int groupID) throws OXException;

    /**
     * Gets the default timezone configured for a specific user.
     *
     * @param userID The identifier of the user to get the timezone for
     * @return The timezone
     */
    TimeZone getTimeZone(int userID) throws OXException;

    /**
     * Gets the locale configured for a specific user.
     *
     * @param userID The identifier of the user to get the locale for
     * @return The locale
     */
    Locale getLocale(int userID) throws OXException;

    /**
     * Gets the identifier of the contact associated with a specific user.
     *
     * @param userID The identifier of the user to get the contact identifier for
     * @return The contact identifier
     */
    int getContactId(int userID) throws OXException;

    /**
     * Prepares a new attendee representing the internal user with the supplied identifier.
     *
     * @param userID The identifier of the user to prepare the attendee for
     * @return The prepared attendee
     */
    Attendee prepareUserAttendee(int userID) throws OXException;

    /**
     * Prepares a new attendee representing the internal group with the supplied identifier.
     *
     * @param groupID The identifier of the group to prepare the attendee for
     * @return The prepared attendee
     */
    Attendee prepareGroupAttendee(int groupID) throws OXException;

    /**
     * Prepares a new attendee representing the internal resource with the supplied identifier.
     *
     * @param resourceID The identifier of the resource to prepare the attendee for
     * @return The prepared attendee
     */
    Attendee prepareResourceAttendee(int resourceID) throws OXException;

    /**
     * Probes the actual calendar user type for a specific internal entity identifier based on the existence of a corresponding user,
     * group or resource.
     *
     * @param entity The entity identifier to probe the calendar user type for
     * @return The calendar user type, or <code>null</code> if no matching entity exixts
     */
    CalendarUserType probeCUType(int entity) throws OXException;

    /**
     * Applies the default set of static properties for the supplied internal calendar user based on the underlying groupware object.
     * This typically includes the calendar user's common name and calendar user address for the supplied internal entity.
     *
     * @param calendarUser The calendar user to apply the static entity data for
     * @param userID The identifier of the user to prepare the attendee for
     * @return The passed calendar user reference, enriched by the resolved static entity data
     */
    <T extends CalendarUser> T applyEntityData(T calendarUser, int userID) throws OXException;

    /**
     * Applies the default set of static properties for the supplied attendee entity based on the underlying groupware object.
     * This typically includes the attendee's common name or calendar user address for the supplied internal attendee entity.
     *
     * @param attendee The attendee to apply the static entity data for
     * @return The passed attendee reference, enriched by the resolved static entity data
     */
    Attendee applyEntityData(Attendee attendee) throws OXException;

    /**
     * Applies specific properties for the supplied calendar user based on the underlying groupware object.
     *
     * @param calendarUser The calendar user to apply the static entity data for
     * @param cuType The corresponding calendar user type
     * @return The passed calendar user reference, enriched by the resolved static entity data
     */
    <T extends CalendarUser> T applyEntityData(T calendarUser, CalendarUserType cuType) throws OXException;

    /**
     * Prefetches information about the underlying groupware objects that are targeted by the supplied list of attendees.
     *
     * @param attendees The attendees to prefetch information for
     */
    void prefetch(List<Attendee> attendees);

    /**
     * Gets the identifier of the context this entity resolver operates on.
     *
     * @return The context identifier
     */
    int getContextID();

    /**
     * Invalidates any cached entities.
     */
    void invalidate();

}
