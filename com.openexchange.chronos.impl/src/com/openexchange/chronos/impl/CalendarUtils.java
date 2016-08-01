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

import static com.openexchange.java.Autoboxing.I;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarParameters;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ResourceId;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.group.Group;
import com.openexchange.groupware.ldap.User;
import com.openexchange.resource.Resource;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.Operand;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ColumnFieldOperand;
import com.openexchange.search.internal.operands.ConstantOperand;

/**
 * {@link CalendarUtils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarUtils {

    /** A collection of fields that are always included when querying events from the storage */
    private static final Collection<EventField> MANDATORY_FIELDS = Arrays.asList(
        EventField.ID, EventField.RECURRENCE_ID, EventField.LAST_MODIFIED, EventField.CREATED_BY, EventField.CLASSIFICATION,
        EventField.PUBLIC_FOLDER_ID, EventField.ALL_DAY, EventField.START_DATE, EventField.END_DATE, EventField.START_TIMEZONE,
        EventField.RECURRENCE_RULE
    );

    /**
     * Gets the event fields to include when querying events from the storage based on the client-requested fields defined in the
     * supplied calendar parameters. <p/>
     * Specific mandatory fields are included implicitly.
     *
     * @param parameters The calendar parameters to get the requested fields from
     * @return The fields to use when querying events from the storage
     */
    static EventField[] getFields(CalendarParameters parameters) {
        return getFields(parameters.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class));
    }

    /**
     * Gets the event fields to include when querying events from the storage based on the supplied client-requested fields. <p/>
     * Specific mandatory fields are included implicitly.
     *
     * @param requestedFields The fields requested by the client, or <code>null</code> to retrieve all fields
     * @return The fields to use when querying events from the storage
     */
    static EventField[] getFields(EventField[] requestedFields) {
        if (null == requestedFields) {
            return EventField.values();
        }
        Set<EventField> fields = new HashSet<EventField>();
        fields.addAll(MANDATORY_FIELDS);
        fields.addAll(Arrays.asList(requestedFields));
        return fields.toArray(new EventField[fields.size()]);
    }

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

    /**
     * Truncates the time part of the supplied date, i.e. sets the fields {@link Calendar#HOUR_OF_DAY}, {@link Calendar#MINUTE},
     * {@link Calendar#SECOND} and {@link Calendar#MILLISECOND} to <code>0</code>.
     *
     * @param date The date to truncate the time part for
     * @return A new date instance based on the supplied date with the time fraction truncated
     */
    public static Date truncateTime(Date date) {
        Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Constructs a search term to match events located in a specific folder. Depending on the folder's type, either a search term for
     * the {@link EventField#PUBLIC_FOLDER_ID} or for the {@link AttendeeField#FOLDER_ID} is built.
     *
     * @param folder The folder to construct the search term for
     * @return The search term
     */
    static SearchTerm<?> getFolderIdTerm(UserizedFolder folder) {
        if (PublicType.getInstance().equals(folder.getType())) {
            if (folder.getOwnPermission().getReadPermission() < Permission.READ_ALL_OBJECTS) {
                return new CompositeSearchTerm(CompositeOperation.AND)
                    .addSearchTerm(getSearchTerm(EventField.PUBLIC_FOLDER_ID, SingleOperation.EQUALS, folder.getID()))
                    .addSearchTerm(getSearchTerm(EventField.CREATED_BY, SingleOperation.EQUALS, folder.getSession().getUserId()));
            }
            return getSearchTerm(EventField.PUBLIC_FOLDER_ID, SingleOperation.EQUALS, folder.getID());
        }
        if (PrivateType.getInstance().equals(folder.getType())) {
            return new CompositeSearchTerm(CompositeOperation.AND)
                .addSearchTerm(getSearchTerm(EventField.PUBLIC_FOLDER_ID, SingleOperation.EQUALS, I(0)))
                .addSearchTerm(getSearchTerm(AttendeeField.ENTITY, SingleOperation.EQUALS, I(folder.getCreatedBy())))
                .addSearchTerm(getSearchTerm(AttendeeField.FOLDER_ID, SingleOperation.EQUALS, folder.getID()));
        }
        if (SharedType.getInstance().equals(folder.getType())) {
            CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
                .addSearchTerm(getSearchTerm(EventField.PUBLIC_FOLDER_ID, SingleOperation.EQUALS, I(0)))
                .addSearchTerm(getSearchTerm(AttendeeField.ENTITY, SingleOperation.EQUALS, I(folder.getCreatedBy())))
                .addSearchTerm(getSearchTerm(AttendeeField.FOLDER_ID, SingleOperation.EQUALS, folder.getID()));
            if (folder.getOwnPermission().getReadPermission() < Permission.READ_ALL_OBJECTS) {
                searchTerm.addSearchTerm(getSearchTerm(EventField.CREATED_BY, SingleOperation.EQUALS, folder.getSession().getUserId()));
            }
            return searchTerm;
        }
        throw new UnsupportedOperationException("Unknown folder type: " + folder.getType());

    }

    /**
     * Appends search terms for commonly used restriction.
     *
     * @param searchTerm The search term to append the search terms for
     * @param from The minimum (inclusive) end time of the events, or <code>null</code> for no restrictions
     * @param until The maximum (exclusive) start time of the events, or <code>null</code> for no restrictions
     * @param updatedSince The minimum (exclusive) last modification time of the events, or <code>null</code> for no restrictions
     * @return The passed search term reference
     */
    static CompositeSearchTerm appendCommonTerms(CompositeSearchTerm searchTerm, Date from, Date until, Date updatedSince) {
        if (null != from) {
            searchTerm.addSearchTerm(getSearchTerm(EventField.END_DATE, SingleOperation.GREATER_OR_EQUAL, from));
        }
        if (null != until) {
            searchTerm.addSearchTerm(getSearchTerm(EventField.START_DATE, SingleOperation.LESS_THAN, until));
        }
        if (null != updatedSince) {
            searchTerm.addSearchTerm(getSearchTerm(EventField.LAST_MODIFIED, SingleOperation.GREATER_THAN, updatedSince));
        }
        return searchTerm;
    }

    /**
     * Gets a single search term using the field itself as column operand and a second operand.
     *
     * @param <V> The operand's type
     * @param <E> The field type
     * @param operation The operation to use
     * @param operand The second operand
     * @return A single search term
     */
    public static <V, E extends Enum<?>> SingleSearchTerm getSearchTerm(E field, SingleOperation operation, Operand<V> operand) {
        return getSearchTerm(field, operation).addOperand(operand);
    }

    /**
     * Gets a single search term using the field itself as column operand and adds the supplied value as constant operand.
     *
     * @param <V> The operand's type
     * @param <E> The field type
     * @param operation The operation to use
     * @param operand The value to use as constant operand
     * @return A single search term
     */
    public static <V, E extends Enum<?>> SingleSearchTerm getSearchTerm(E field, SingleOperation operation, V operand) {
        return getSearchTerm(field, operation, new ConstantOperand<V>(operand));
    }

    /**
     * Gets a single search term using the field itself as single column operand.
     *
     * @param <E> The field type
     * @param operation The operation to use
     * @param operand The value to use as constant operand
     * @return A single search term
     */
    public static <E extends Enum<?>> SingleSearchTerm getSearchTerm(E field, SingleOperation operation) {
        return new SingleSearchTerm(operation).addOperand(new ColumnFieldOperand<E>(field));
    }

    /**
     * Gets the identifiers of the supplied events in an array.
     *
     * @param events The events to get the identifiers for
     * @return The object identifiers
     */
    static int[] getObjectIDs(List<Event> events) {
        int[] objectIDs = new int[events.size()];
        for (int i = 0; i < events.size(); i++) {
            objectIDs[i] = events.get(i).getId();
        }
        return objectIDs;
    }

}
