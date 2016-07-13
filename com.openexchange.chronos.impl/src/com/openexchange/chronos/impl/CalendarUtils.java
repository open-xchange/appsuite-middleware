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

package com.openexchange.chronos.impl;

import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ResourceId;
import com.openexchange.group.Group;
import com.openexchange.groupware.ldap.User;
import com.openexchange.resource.Resource;

/**
 * {@link CalendarUtils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarUtils {

    /**
     * Looks up a specific internal attendee in a collection of attendees, utilizing the
     * {@link CalendarUtils#matches(Attendee, Attendee)} routine.
     *
     * @param attendees The attendees to search
     * @param attendee The attendee to lookup
     * @return The matching attendee, or <code>null</code> if not found
     * @see CalendarUtils#matches(Attendee, Attendee)
     */
    static Attendee find(List<Attendee> attendees, Attendee attendee) {
        if (null != attendees && 0 < attendees.size()) {
            for (Attendee candidateAttendee : attendees) {
                if (matches(attendee, candidateAttendee)) {
                    return candidateAttendee;
                }
            }
        }
        return null;
    }

    /**
     * Gets a value indicating whether a specific attendee is present in a collection of attendees, utilizing the
     * {@link CalendarUtils#matches(Attendee, Attendee)} routine.
     *
     * @param attendees The attendees to search
     * @param attendee The attendee to lookup
     * @return <code>true</code> if the attendee is contained in the collection of attendees, <code>false</code>, otherwise
     * @see CalendarUtils#matches(Attendee, Attendee)
     */
    static boolean contains(List<Attendee> attendees, Attendee attendee) {
        return null != find(attendees, attendee);
    }

    /**
     * Gets a value indicating whether one attendee matches another, by comparing the entity identifier for internal attendees,
     * or trying to match the attendee's URI for external ones.
     *
     * @param attendee1 The first attendee to check
     * @param attendee2 The second attendee to check
     * @return <code>true</code> if the attendees match, i.e. are targeting the same calendar user, <code>false</code>, otherwise
     */
    static boolean matches(Attendee attendee1, Attendee attendee2) {
        if (null == attendee1) {
            return null == attendee2;
        } else if (null != attendee2) {
            if (0 < attendee1.getEntity() && attendee1.getEntity() == attendee2.getEntity()) {
                return true;
            }
            if (null != attendee1.getUri() && attendee1.getUri().equals(attendee2.getUri())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Looks up a specific internal attendee in a collection of attendees based on its entity identifier.
     *
     * @param attendees The attendees to search
     * @param entity The entity identifier to lookup
     * @return The matching attendee, or <code>null</code> if not found
     */
    static Attendee find(List<Attendee> attendees, int entity) {
        if (null != attendees && 0 < attendees.size()) {
            for (Attendee attendee : attendees) {
                if (entity == attendee.getEntity()) {
                    return attendee;
                }
            }
        }
        return null;
    }

    /**
     * Gets a value indicating whether a collection of attendees contains a specific internal attendee based on its entity identifier or
     * not.
     *
     * @param attendees The attendees to search
     * @param entity The entity identifier to lookup
     * @return <code>true</code> if the attendee was found, <code>false</code>, otherwise
     */
    static boolean contains(List<Attendee> attendees, int entity) {
        return null != find(attendees, entity);
    }

    /**
     * Gets a value indicating whether a specific user is the organizer of an event or not.
     *
     * @param event The event
     * @param userId The identifier of the user to check
     * @return <code>true</code> if the user with the supplied identifier is the organizer, <code>false</code>, otherwise
     */
    static boolean isOrganizer(Event event, int userId) {
        return null != event.getOrganizer() && userId == event.getOrganizer().getEntity();
    }

    /**
     * Gets a value indicating whether a specific user is an attendee of an event or not.
     *
     * @param event The event
     * @param userId The identifier of the user to check
     * @return <code>true</code> if the user with the supplied identifier is an attendee, <code>false</code>, otherwise
     */
    static boolean isAttendee(Event event, int userId) {
        return contains(event.getAttendees(), userId);
    }

    /**
     * Gets the calendar address for a user (as mailto URI).
     *
     * @param contextID The context identifier
     * @param user The user
     * @return The calendar address
     */
    public static String getCalAddress(User user) {
        return "mailto:" + user.getMail();
    }

    /**
     * Gets the calendar address for a resource (as uniform resource name URI).
     *
     * @param contextID The context identifier
     * @param resource The resource
     * @return The calendar address
     */
    public static String getCalAddress(int contextID, Resource resource) {
        return ResourceId.forResource(contextID, resource.getIdentifier());
    }

    /**
     * Gets the calendar address for a group (as uniform resource name URI).
     *
     * @param contextID The context identifier
     * @param group The group
     * @return The calendar address
     */
    public static String getCalAddress(int contextID, Group group) {
        return ResourceId.forGroup(contextID, group.getIdentifier());
    }

    /**
     * Applies common properties of a specific user to a calendar user instance.
     *
     * @param calendarUser The calendar user to apply the properties to
     * @param user The user to get the properties from
     * @return The passed calendar user reference
     */
    public static <T extends CalendarUser> T applyProperties(T calendarUser, User user) {
        calendarUser.setEntity(user.getId());
        calendarUser.setCommonName(user.getDisplayName());
        calendarUser.setUri(getCalAddress(user));
        return calendarUser;
    }

}
