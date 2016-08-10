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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarParameters;
import com.openexchange.chronos.CalendarSession;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ResourceId;
import com.openexchange.chronos.SortOptions;
import com.openexchange.chronos.SortOrder;
import com.openexchange.chronos.UserizedEvent;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.group.Group;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tools.mappings.Mapping;
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
    static final List<EventField> MANDATORY_FIELDS = Arrays.asList(
        EventField.ID, EventField.SERIES_ID, EventField.PUBLIC_FOLDER_ID, EventField.LAST_MODIFIED, EventField.CREATED_BY,
        EventField.CLASSIFICATION, EventField.PUBLIC_FOLDER_ID, EventField.ALL_DAY, EventField.START_DATE, EventField.END_DATE,
        EventField.START_TIMEZONE, EventField.RECURRENCE_RULE
    );

    /** The event fields that are also available if an event's classification is not {@link Classification#PUBLIC} */
    static final EventField[] NON_CLASSIFIED_FIELDS = {
        EventField.ALL_DAY, EventField.CHANGE_EXCEPTION_DATES, EventField.CLASSIFICATION, EventField.CREATED, EventField.CREATED_BY,
        EventField.DELETE_EXCEPTION_DATES, EventField.END_DATE, EventField.END_TIMEZONE, EventField.ID, EventField.LAST_MODIFIED,
        EventField.MODIFIED_BY, EventField.PUBLIC_FOLDER_ID, EventField.SERIES_ID, EventField.RECURRENCE_RULE, EventField.SEQUENCE,
        EventField.START_DATE, EventField.START_TIMEZONE, EventField.TRANSP, EventField.UID
    };

    /**
     * Gets the event fields to include when querying events from the storage based on the client-requested fields defined in the
     * supplied calendar parameters. <p/>
     * Specific {@link CalendarUtils#MANDATORY_FIELDS} are included implicitly, further required ones may be defined explicitly, too.
     *
     * @param parameters The calendar parameters to get the requested fields from
     * @param requiredFields Additionally required fields to add, or <code>null</code> if not defined
     * @return The fields to use when querying events from the storage
     * @see CalendarParameters#PARAMETER_FIELDS
     * @see CalendarUtils#MANDATORY_FIELDS
     */
    static EventField[] getFields(CalendarParameters parameters, EventField... requiredFields) {
        return getFields(parameters.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class), requiredFields);
    }

    /**
     * Gets a value indicating whether a recurring event series should be resolved to individual occurrences or not, based on the value
     * of {@link CalendarParameters#PARAMETER_RECURRENCE_MASTER} in the supplied parameters.
     *
     * @param parameters The calendar parameters to evaluate
     * @return <code>true</code> if individual occurrences should be resolved, <code>false</code>, otherwise
     * @see CalendarParameters#PARAMETER_RECURRENCE_MASTER
     */
    static boolean isResolveOccurrences(CalendarParameters parameters) {
        return false == parameters.get(CalendarParameters.PARAMETER_RECURRENCE_MASTER, Boolean.class, Boolean.FALSE).booleanValue();
    }

    /**
     * Gets the timezone valid for the supplied calendar session, which is either the (possibly overridden) timezone defined via
     * {@link CalendarParameters#PARAMETER_TIMEZONE}, or as fallback, the session user's default timezone.
     *
     * @param session The calendar session to get the timezone for
     * @return The timezone
     * @see CalendarParameters#PARAMETER_TIMEZONE
     * @see User#getTimeZone()
     */
    static TimeZone getTimeZone(CalendarSession session) {
        return session.get(CalendarParameters.PARAMETER_TIMEZONE, TimeZone.class, TimeZone.getTimeZone(session.getUser().getTimeZone()));
    }

    /**
     * Extracts the "from" date used for range-queries from the parameter {@link CalendarParameters#PARAMETER_RANGE_START}.
     *
     * @param parameters The calendar parameters to evaluate
     * @return The "from" date, or <code>null</code> if not set
     */
    static Date getFrom(CalendarParameters parameters) {
        return parameters.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
    }

    /**
     * Extracts the "until" date used for range-queries from the parameter {@link CalendarParameters#PARAMETER_RANGE_END}.
     *
     * @param parameters The calendar parameters to evaluate
     * @return The "until" date, or <code>null</code> if not set
     */
    static Date getUntil(CalendarParameters parameters) {
        return parameters.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
    }

    /**
     * Gets the event fields to include when querying events from the storage based on the supplied client-requested fields. <p/>
     * Specific {@link CalendarUtils#MANDATORY_FIELDS} are included implicitly, further required ones may be defined explicitly, too.
     *
     * @param requestedFields The fields requested by the client, or <code>null</code> to retrieve all fields
     * @param requiredFields Additionally required fields to add, or <code>null</code> if not defined
     * @return The fields to use when querying events from the storage
     * @see CalendarUtils#MANDATORY_FIELDS
     */
    static EventField[] getFields(EventField[] requestedFields, EventField... requiredFields) {
        if (null == requestedFields) {
            return EventField.values();
        }
        Set<EventField> fields = new HashSet<EventField>();
        fields.addAll(MANDATORY_FIELDS);
        if (null != requiredFields && 0 < requestedFields.length) {
            fields.addAll(Arrays.asList(requiredFields));
        }
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
        calendarUser.setCn(user.getDisplayName());
        calendarUser.setUri(getCalAddress(user));
        return calendarUser;
    }

    /**
     * Truncates the time part of the supplied date, i.e. sets the fields {@link Calendar#HOUR_OF_DAY}, {@link Calendar#MINUTE},
     * {@link Calendar#SECOND} and {@link Calendar#MILLISECOND} to <code>0</code>.
     *
     * @param date The date to truncate the time part for
     * @param timeZone The timezone to consider
     * @return A new date instance based on the supplied date with the time fraction truncated
     */
    public static Date truncateTime(Date date, TimeZone timeZone) {
        return truncateTime(initCalendar(timeZone, date)).getTime();
    }

    /**
     * Truncates the time part in the supplied calendar reference, i.e. sets the fields {@link Calendar#HOUR_OF_DAY},
     * {@link Calendar#MINUTE}, {@link Calendar#SECOND} and {@link Calendar#MILLISECOND} to <code>0</code>.
     *
     * @param calendar The calendar reference to truncate the time part in
     * @param timeZone The timezone to consider
     * @return The calendar reference
     */
    public static Calendar truncateTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**
     * Constructs a search term to match events located in a specific folder. Depending on the folder's type, either a search term for
     * the {@link EventField#PUBLIC_FOLDER_ID} or for the {@link AttendeeField#FOLDER_ID} is built.
     * <p/>
     * The session user's read permissions in the folder ("own" vs "all") are considered automatically, too, by restricting via
     * {@link EventField#CREATED_BY} if needed.
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
     * Converts a so-called <i>floating</i> date into a date in a concrete timezone by applying the actual timezone offset on that date.
     *
     * @param floatingDate The floating date to convert (usually the raw date in <code>UTC</code>)
     * @param timeZone The target timezone
     * @return The date in the target timezone, with the corresponding timezone offset applied
     */
    static Date getDateInTimeZone(Date floatingDate, TimeZone timeZone) {
        return new Date(floatingDate.getTime() - timeZone.getOffset(floatingDate.getTime()));
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

    /**
     * Gets a value indicating whether the supplied event is considered as the <i>master</i> event of a recurring series or not, based
     * on the properties {@link EventField#ID} and {@link EventField#SERIES_ID} for equality.
     *
     * @param event The event to check
     * @return <code>true</code> if the event is the series master, <code>false</code>, otherwise
     */
    static boolean isSeriesMaster(Event event) {
        return null != event && event.getId() == event.getSeriesId();
    }

    /**
     * Gets a value indicating whether the supplied event is considered as an exceptional event of a recurring series or not, based on
     * the properties {@link EventField#ID} and {@link EventField#SERIES_ID}.
     *
     * @param event The event to check
     * @return <code>true</code> if the event is the series master, <code>false</code>, otherwise
     */
    static boolean isSeriesException(Event event) {
        return null != event && 0 < event.getSeriesId() && event.getSeriesId() != event.getId();
    }

    /**
     * Initializes a new calendar in a specific timezone and sets the initial time.
     *
     * @param timeZone The timezone to use for the calendar
     * @param time The initial time to set
     * @return A new calendar instance
     */
    static Calendar initCalendar(TimeZone timeZone, Date time) {
        Calendar calendar = GregorianCalendar.getInstance(timeZone);
        calendar.setTime(time);
        return calendar;
    }

    /**
     * Gets a value indicating whether a specific event falls (at least partly) into a time range.
     *
     * @param event The event to check
     * @param from The lower inclusive limit of the range, i.e. the event should start on or after this date, or <code>null</code> for no limit
     * @param until The upper exclusive limit of the range, i.e. the event should end before this date, or <code>null</code> for no limit
     * @param timeZone The timezone to consider if the event has <i>floating</i> dates
     * @return <code>true</code> if the event falls into the time range, <code>false</code>, otherwise
     */
    static boolean isInRange(Event event, Date from, Date until, TimeZone timeZone) {
        // TODO floating events that are not "all-day"
        Date startDate = event.isAllDay() ? getDateInTimeZone(event.getStartDate(), timeZone) : event.getStartDate();
        Date endDate = event.isAllDay() ? getDateInTimeZone(event.getEndDate(), timeZone) : event.getEndDate();
        return (null == until || startDate.before(until)) && (null == from || endDate.after(from));
    }

    /**
     * "Anonymizes" an event in case it is not marked as {@link Classification#PUBLIC}, and the session's user is neither creator, nor
     * attendee of the event.
     * <p/>
     * After anonymization, the event will only contain those properties defined in {@link #NON_CLASSIFIED_FIELDS}, as well as the
     * generic summary "Private".
     *
     * @param userizedEvent The event to anonymize
     * @param userID The identifier of the user requesting the event data
     * @return The potentially anonymized event
     */
    static UserizedEvent anonymizeIfNeeded(UserizedEvent userizedEvent) throws OXException {
        Event event = userizedEvent.getEvent();
        int userID = userizedEvent.getSession().getUserId();
        if (false == isClassifiedFor(event, userID)) {
            return userizedEvent;
        }
        Event anonymizedEvent = new Event();
        for (EventField field : NON_CLASSIFIED_FIELDS) {
            Mapping<? extends Object, Event> mapping = EventMapper.getInstance().opt(field);
            if (null != mapping && mapping.isSet(event)) {
                mapping.copy(event, anonymizedEvent);
            }
        }
        anonymizedEvent.setSummary("Private"); // TODO i18n?
        return new UserizedEvent(userizedEvent.getSession(), anonymizedEvent, userizedEvent.getFolderId(), null);
    }

    /**
     * Gets an event comparator based on the supplied sort order of event fields.
     *
     * @param sortOrders The sort orders to get the comparator for
     * @return The comparator
     */
    static Comparator<Event> getComparator(final SortOrder[] sortOrders) {
        return new Comparator<Event>() {

            @Override
            public int compare(Event event1, Event event2) {
                if (null == event1) {
                    return null == event2 ? 0 : -1;
                }
                if (null == event2) {
                    return 1;
                }
                if (null == sortOrders || 0 == sortOrders.length) {
                    return 0;
                }
                int comparison = 0;
                if (null != sortOrders && 0 < sortOrders.length) {
                    for (SortOrder sortOrder : sortOrders) {
                        try {
                            comparison = EventMapper.getInstance().get(sortOrder.getBy()).compare(event1, event2);
                        } catch (OXException e) {
                            throw new RuntimeException(e);
                        }
                        if (0 != comparison) {
                            return sortOrder.isDescending() ? -1 * comparison : comparison;
                        }
                    }
                }
                return comparison;
            }
        };
    }

    /**
     * Gets a value indicating whether a specific event should be excluded from results based on the configured calendar parameters,
     * e.g. because ...
     * <ul>
     * <li>it is classified as private or confidential for the accessing user and such events are configured to be excluded via parameters</li>
     * <li>it's start-date is behind the range requested via parameters</li>
     * <li>it's end-date is before the range requested via parameters</li>
     * </ul>
     *
     * @param event The event to check
     * @param session The calendar session
     * @return <code>true</code> if the event should be excluded, <code>false</code>, otherwise
     */
    static boolean isExcluded(Event event, CalendarSession session) {
        if (isClassifiedFor(event, session.getUser().getId()) &&
            Boolean.FALSE.equals(session.get(CalendarParameters.PARAMETER_INCLUDE_PRIVATE, Boolean.class))) {
            return true;
        }
        if (false == isInRange(event, getFrom(session), getUntil(session), getTimeZone(session))) {
            return true;
        }
        return false;
    }

    /**
     * Gets a value indicating whether event data is classified as confidential or private for a specific accessing user entity and
     * therefore should be anonymized or not.
     *
     * @param event The event to check
     * @param userID The identifier of the accessing user to check
     * @return <code>true</code> if the event is classified for the supplied user, <code>false</code>, otherwise
     */
    static boolean isClassifiedFor(Event event, int userID) {
        if (null == event.getClassification() || Classification.PUBLIC.equals(event.getClassification())) {
            return false;
        }
        if (event.getCreatedBy() == userID || contains(event.getAttendees(), userID)) {
            return false;
        }
        return true;
    }

    /**
     * Finds a specific event identified by its folder- and object-identifier in a collection.
     *
     * @param events The events to search in
     * @param folderID The identifier of the parent folder to search
     * @param objectID The object identifier of the event to search
     * @return The event, or <code>null</code> if not found
     */
    static UserizedEvent find(Collection<UserizedEvent> events, int folderID, int objectID) {
        if (null != events) {
            for (UserizedEvent event : events) {
                if (event.getFolderId() == folderID && event.getEvent().getId() == objectID) {
                    return event;
                }
            }
        }
        return null;
    }

    /**
     * Sorts a list of events.
     *
     * @param events The events to sort
     * @param sortOptions The sort options to use
     * @return The sorted events
     */
    static List<UserizedEvent> sort(List<UserizedEvent> events, SortOptions sortOptions) {
        if (null == events || 2 > events.size() || null == sortOptions || SortOptions.EMPTY.equals(sortOptions) ||
            null == sortOptions.getSortOrders() || 0 == sortOptions.getSortOrders().length) {
            return events;
        }
        final Comparator<Event> eventComparator = getComparator(sortOptions.getSortOrders());
        Collections.sort(events, new Comparator<UserizedEvent>() {

            @Override
            public int compare(UserizedEvent userizedEvent1, UserizedEvent userizedEvent2) {
                return eventComparator.compare(userizedEvent1.getEvent(), userizedEvent2.getEvent());
            }
        });
        return events;
    }

    /**
     * Filters a list of attendees based on their calendaruser type, and whether they represent "internal" attendees or not.
     *
     * @param attendees The attendees to filter
     * @param internal {@link Boolean#TRUE} to only consider internal entities, {@link Boolean#FALSE} for non-internal ones,
     *            or <code>null</code> to not filter by internal/external
     * @param cuType The {@link CalendarUserType} to consider, or <code>null</code> to not filter by calender user type
     * @return The filtered attendees
     */
    static List<Attendee> filter(List<Attendee> attendees, Boolean internal, CalendarUserType cuType) {
        if (null == attendees) {
            return null;
        }
        List<Attendee> filteredAttendees = new ArrayList<Attendee>(attendees.size());
        for (Attendee attendee : attendees) {
            if (null == cuType || cuType.equals(attendee.getCuType())) {
                if (null == internal || internal.equals(Boolean.valueOf(0 < attendee.getEntity()))) {
                    filteredAttendees.add(attendee);
                }
            }
        }
        return filteredAttendees;
    }

}
