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
     * Gets the user identifiers of the members of a specific internal group.
     *
     * @param groupID The identifier of the group to get the members for
     * @return The group members
     */
    int[] getGroupMembers(int groupID) throws OXException;;

    /**
     * Gets the default timezone configured for a specific user.
     *
     * @param userID The identifier of the user to get the timezone for
     * @return The timezone
     */
    TimeZone getTimeZone(int userID) throws OXException;;

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
