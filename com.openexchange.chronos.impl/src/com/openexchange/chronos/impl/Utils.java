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

import static com.openexchange.chronos.common.CalendarUtils.contains;
import static com.openexchange.chronos.common.CalendarUtils.getObjectIDs;
import static com.openexchange.chronos.common.CalendarUtils.isInRange;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.arrays.Arrays.contains;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Period;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.compat.Recurrence;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.SortOptions;
import com.openexchange.chronos.service.SortOrder;
import com.openexchange.chronos.service.UserizedEvent;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tools.mappings.Mapping;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.Operand;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ColumnFieldOperand;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.user.UserService;

/**
 * {@link Utils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Utils {

    /** A collection of fields that are always included when querying events from the storage */
    public static final List<EventField> DEFAULT_FIELDS = Arrays.asList(
        EventField.ID, EventField.SERIES_ID, EventField.PUBLIC_FOLDER_ID, EventField.LAST_MODIFIED, EventField.CREATED_BY,
        EventField.CLASSIFICATION, EventField.PUBLIC_FOLDER_ID, EventField.ALL_DAY, EventField.START_DATE, EventField.END_DATE,
        EventField.START_TIMEZONE, EventField.RECURRENCE_RULE, EventField.CHANGE_EXCEPTION_DATES, EventField.DELETE_EXCEPTION_DATES
    );

    /** The event fields that are also available if an event's classification is not {@link Classification#PUBLIC} */
    public static final EventField[] NON_CLASSIFIED_FIELDS = {
        EventField.ALL_DAY, EventField.CHANGE_EXCEPTION_DATES, EventField.CLASSIFICATION, EventField.CREATED, EventField.CREATED_BY,
        EventField.DELETE_EXCEPTION_DATES, EventField.END_DATE, EventField.END_TIMEZONE, EventField.ID, EventField.LAST_MODIFIED,
        EventField.MODIFIED_BY, EventField.PUBLIC_FOLDER_ID, EventField.SERIES_ID, EventField.RECURRENCE_RULE, EventField.SEQUENCE,
        EventField.START_DATE, EventField.START_TIMEZONE, EventField.TRANSP, EventField.UID
    };

    /**
     * Gets the event fields to include when querying events from the storage based on the client-requested fields defined in the
     * supplied calendar parameters. <p/>
     * Specific {@link Utils#DEFAULT_FIELDS} are included implicitly, further required ones may be defined explicitly, too.
     *
     * @param parameters The calendar parameters to get the requested fields from
     * @param requiredFields Additionally required fields to add, or <code>null</code> if not defined
     * @return The fields to use when querying events from the storage
     * @see CalendarParameters#PARAMETER_FIELDS
     * @see Utils#DEFAULT_FIELDS
     */
    public static EventField[] getFields(CalendarParameters parameters, EventField... requiredFields) {
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
    public static boolean isResolveOccurrences(CalendarParameters parameters) {
        return false == parameters.get(CalendarParameters.PARAMETER_RECURRENCE_MASTER, Boolean.class, Boolean.FALSE).booleanValue();
    }

    /**
     * Gets a value indicating whether (soft) conflicts of internal attendees should be ignored during event creation or update or not,
     * based on the value of {@link CalendarParameters#PARAMETER_IGNORE_CONFLICTS} in the supplied parameters.
     *
     * @param parameters The calendar parameters to evaluate
     * @return <code>true</code> if (soft) conflicts should be ignored, <code>false</code>, otherwise
     * @see CalendarParameters#PARAMETER_IGNORE_CONFLICTS
     */
    public static boolean isIgnoreConflicts(CalendarParameters parameters) {
        return parameters.get(CalendarParameters.PARAMETER_IGNORE_CONFLICTS, Boolean.class, Boolean.FALSE).booleanValue();
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
    public static TimeZone getTimeZone(CalendarSession session) {
        return session.get(CalendarParameters.PARAMETER_TIMEZONE, TimeZone.class, TimeZone.getTimeZone(session.getUser().getTimeZone()));
    }

    /**
     * Extracts the "from" date used for range-queries from the parameter {@link CalendarParameters#PARAMETER_RANGE_START}.
     *
     * @param parameters The calendar parameters to evaluate
     * @return The "from" date, or <code>null</code> if not set
     */
    public static Date getFrom(CalendarParameters parameters) {
        return parameters.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
    }

    /**
     * Extracts the "until" date used for range-queries from the parameter {@link CalendarParameters#PARAMETER_RANGE_END}.
     *
     * @param parameters The calendar parameters to evaluate
     * @return The "until" date, or <code>null</code> if not set
     */
    public static Date getUntil(CalendarParameters parameters) {
        return parameters.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
    }

    /**
     * Gets the event fields to include when querying events from the storage based on the supplied client-requested fields. <p/>
     * Specific {@link Utils#DEFAULT_FIELDS} are included implicitly, further required ones may be defined explicitly, too.
     *
     * @param requestedFields The fields requested by the client, or <code>null</code> to retrieve all fields
     * @param requiredFields Additionally required fields to add, or <code>null</code> if not defined
     * @return The fields to use when querying events from the storage
     * @see Utils#DEFAULT_FIELDS
     */
    public static EventField[] getFields(EventField[] requestedFields, EventField... requiredFields) {
        if (null == requestedFields) {
            return EventField.values();
        }
        Set<EventField> fields = new HashSet<EventField>();
        fields.addAll(DEFAULT_FIELDS);
        if (null != requiredFields && 0 < requestedFields.length) {
            fields.addAll(Arrays.asList(requiredFields));
        }
        fields.addAll(Arrays.asList(requestedFields));
        return fields.toArray(new EventField[fields.size()]);
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
    public static SearchTerm<?> getFolderIdTerm(UserizedFolder folder) {
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
    public static CompositeSearchTerm appendCommonTerms(CompositeSearchTerm searchTerm, Date from, Date until, Date updatedSince) {
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
     * Parses a folder's numerical folder identifier.
     *
     * @param folder The folder to get the identifier for
     * @return The folder identifier
     */
    public static int i(UserizedFolder folder) throws OXException {
        try {
            return Integer.parseInt(folder.getID());
        } catch (NumberFormatException e) {
            throw OXException.general("unsupported folder id: " + folder.getID());//TODO
        }
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
    public static UserizedEvent anonymizeIfNeeded(UserizedEvent userizedEvent) throws OXException {
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
        return new UserizedEvent(userizedEvent.getSession(), anonymizedEvent, userizedEvent.getFolderId());
    }

    /**
     * "Anonymizes" an event in case it is not marked as {@link Classification#PUBLIC}, and the session's user is neither creator, nor
     * attendee of the event.
     * <p/>
     * After anonymization, the event will only contain those properties defined in {@link #NON_CLASSIFIED_FIELDS}, as well as the
     * generic summary "Private".
     *
     * @param event The event to anonymize
     * @param userID The identifier of the user requesting the event data
     * @return The potentially anonymized event
     */
    public static Event anonymizeIfNeeded(Event event, int userID) throws OXException {
        if (false == isClassifiedFor(event, userID)) {
            return event;
        }
        Event anonymizedEvent = new Event();
        EventMapper.getInstance().copy(event, anonymizedEvent, NON_CLASSIFIED_FIELDS);
        anonymizedEvent.setSummary("Private"); // TODO i18n?
        return anonymizedEvent;
    }

    /**
     * Gets an event comparator based on the supplied sort order of event fields.
     *
     * @param sortOrders The sort orders to get the comparator for
     * @return The comparator
     */
    public static Comparator<Event> getComparator(final SortOrder[] sortOrders) {
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
     * <li>it is classified as private or confidential for the accessing user and such events are configured to be excluded</li>
     * <li>it's start-date is behind the range requested via parameters</li>
     * <li>it's end-date is before the range requested via parameters</li>
     * </ul>
     *
     * @param event The event to check
     * @param session The calendar session
     * @param includePrivate <code>true</code> to include private or confidential events in non-private folders, <code>false</code>, otherwise
     * @return <code>true</code> if the event should be excluded, <code>false</code>, otherwise
     */
    public static boolean isExcluded(Event event, CalendarSession session, boolean includePrivate) throws OXException {
        if (false == includePrivate && isClassifiedFor(event, session.getUser().getId())) {
            return true;
        }
        Date from = getFrom(session);
        Date until = getUntil(session);
        if (null != from || null != until) {
            Period period = new Period(event);
            if (isSeriesMaster(event)) {
                //TODO: really consider "implicit" series period also if isResolveOccurrences(session) == false?
                //      (needed for com.openexchange.ajax.appointment.bugtests.Bug16107Test)
                period = Recurrence.getImplicitSeriesPeriod(new DefaultRecurrenceData(event), period);
            }
            if (false == isInRange(period, from, until, getTimeZone(session))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isIncludePrivate(CalendarParameters parameters) {
        return parameters.get(CalendarParameters.PARAMETER_INCLUDE_PRIVATE, Boolean.class, Boolean.FALSE).booleanValue();
    }

    /**
     * Gets a value indicating whether event data is classified as confidential or private for a specific accessing user entity and
     * therefore should be anonymized or not.
     *
     * @param event The event to check
     * @param userID The identifier of the accessing user to check
     * @return <code>true</code> if the event is classified for the supplied user, <code>false</code>, otherwise
     */
    public static boolean isClassifiedFor(Event event, int userID) {
        if (null == event.getClassification() || Classification.PUBLIC.equals(event.getClassification())) {
            return false;
        }
        if (event.getCreatedBy() == userID || contains(event.getAttendees(), userID)) {
            return false;
        }
        return true;
    }

    /**
     * Finds a specific event identified by its event identifier in a collection.
     *
     * @param events The events to search in
     * @param eventID The identifier of the event to search
     * @return The event, or <code>null</code> if not found
     */
    public static UserizedEvent find(Collection<UserizedEvent> events, EventID eventID) {
        if (null != events) {
            for (UserizedEvent event : events) {
                if (event.getFolderId() == eventID.getFolderID() && event.getEvent().getId() == eventID.getObjectID()) {
                    if (null == eventID.getRecurrenceID() || eventID.getRecurrenceID().equals(event.getEvent().getRecurrenceId())) {
                        return event;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Finds a specific event identified by its object-identifier in a collection.
     *
     * @param events The events to search in
     * @param objectID The object identifier of the event to search
     * @return The event, or <code>null</code> if not found
     */
    public static Event find(Collection<Event> events, int objectID) {
        if (null != events) {
            for (Event event : events) {
                if (event.getId() == objectID) {
                    return event;
                }
            }
        }
        return null;
    }

    /**
     * Finds a specific event identified by its object-identifier and an optional recurrence identifier in a collection.
     *
     * @param events The events to search in
     * @param objectID The object identifier of the event to search
     * @param recurrenceID The rcurrence identifier of the event to search
     * @return The event, or <code>null</code> if not found
     */
    public static Event find(Collection<Event> events, int objectID, RecurrenceId recurrenceID) {
        if (null != events) {
            for (Event event : events) {
                if (event.getId() == objectID) {
                    if (null == recurrenceID || recurrenceID.equals(event.getRecurrenceId())) {
                        return event;
                    }
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
    public static List<UserizedEvent> sort(List<UserizedEvent> events, SortOptions sortOptions) {
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
     * Sorts a list of events.
     *
     * @param events The events to sort
     * @param sortOptions The sort options to use
     * @return The sorted events
     */
    public static List<Event> sortEvents(List<Event> events, SortOptions sortOptions) {
        if (null == events || 2 > events.size() || null == sortOptions || SortOptions.EMPTY.equals(sortOptions) || null == sortOptions.getSortOrders() || 0 == sortOptions.getSortOrders().length) {
            return events;
        }
        Collections.sort(events, getComparator(sortOptions.getSortOrders()));
        return events;
    }

    /**
     * Gets the actual target calendar user for a specific folder. This is either the current session's user for "private" or "public"
     * folders, or the folder owner for "shared" calendar folders.
     *
     * @param folder The folder to get the calendar user for
     * @return The calendar user
     */
    public static User getCalendarUser(UserizedFolder folder) throws OXException {
        if (SharedType.getInstance().equals(folder.getType())) {
            return Services.getService(UserService.class).getUser(folder.getCreatedBy(), folder.getContext());
        }
        return folder.getUser();
    }

    /**
     * Gets the "acting" calendar user for a specific folder, i.e. the proxy user who is acting on behalf of the calendar owner, which is
     * the current session's user in case the folder is a "shared" calendar, otherwise <code>null</code> for "private" or "public" folders.
     *
     * @param folder The folder to determine the proxy user for
     * @return The proxy calendar user, or <code>null</code> if the current session's user is acting on behalf of it's own
     */
    public static User getProxyUser(UserizedFolder folder) throws OXException {
        return SharedType.getInstance().equals(folder.getType()) ? folder.getUser() : null;
    }

    /**
     * Gets a value indicating whether an event lies in the past or not, i.e. it's end-time is before the <i>current</i> time.
     * <p/>
     * For event series, the recurrence rule's <code>UNTIL</code>- and <code>COUNT</code>-parameters are evaluated accordingly; for
     * <i>never-ending</i> event series, this method always returns <code>false</code>.
     *
     * @param event The event to check
     * @param now The date to consider as <i>now</i> in the comparison
     * @param timeZone The timezone to consider if the event has <i>floating</i> dates
     * @return <code>true</code> if the event is in the past, <code>false</code>, otherwise
     */
    public static boolean isInPast(Event event, Date now, TimeZone timeZone) throws OXException {
        if (false == isSeriesMaster(event)) {
            return false == isInRange(event, now, null, timeZone);
        }
        return Recurrence.isInPast(new DefaultRecurrenceData(event), now, timeZone);
    }

    /**
     * Gets a value indicating whether an event lies in the past or not, i.e. it's end-time is before the current system time.
     * <p/>
     * For event series, the recurrence rule's <code>UNTIL</code>- and <code>COUNT</code>-parameters are evaluated accordingly; for
     * <i>never-ending</i> event series, this method always returns <code>false</code>;
     *
     * @param event The event to check
     * @param timeZone The timezone to consider if the event has <i>floating</i> dates
     * @return <code>true</code> if the event is in the past, <code>false</code>, otherwise
     */
    public static boolean isInPast(Event event, TimeZone timeZone) throws OXException {
        return isInPast(event, new Date(), timeZone);
    }

    /**
     * Gets a <i>userized</i> folder by its identifier.
     *
     * @param session The calendar session
     * @param folderID The identifier of the folder to get
     * @return The folder
     */
    public static UserizedFolder getFolder(CalendarSession session, int folderID) throws OXException {
        try {
            return Services.getService(FolderService.class).getFolder(FolderStorage.REAL_TREE_ID, String.valueOf(folderID), session.getSession(), initDecorator(session));
        } catch (OXException e) {
            if ("FLD-0003".equals(e.getErrorCode())) {
                // com.openexchange.tools.oxfolder.OXFolderExceptionCode.NOT_VISIBLE
                throw CalendarExceptionCodes.NO_READ_PERMISSION.create(I(folderID));
            }
            throw e;
        }
    }

    /**
     * Loads additional event data from the storage, based on the requested fields. This currently includes
     * <ul>
     * <li>{@link EventField#ATTENDEES}</li>
     * <li>{@link EventField#ATTACHMENTS}</li>
     * <li>{@link EventField#ALARMS}</li> (of the calendar user)
     * </ul>
     *
     * @param storage A reference to the calendar storage to use
     * @param events The events to load additional data for
     * @param userID The identifier of the calendar user to load additional data for, or <code>-1</code> to not load user-sensitive data
     * @param fields The requested fields, or <code>null</code> to assume all fields are requested
     * @return The events, enriched by the additionally loaded data
     */
    public static List<Event> loadAdditionalEventData(CalendarStorage storage, int userID, List<Event> events, EventField[] fields) throws OXException {
        if (null != events && 0 < events.size() && (null == fields || contains(fields, EventField.ATTENDEES) || contains(fields, EventField.ATTACHMENTS) || contains(fields, EventField.ALARMS))) {
            int[] objectIDs = getObjectIDs(events);
            if (null == fields || contains(fields, EventField.ATTENDEES)) {
                Map<Integer, List<Attendee>> attendeesById = storage.getAttendeeStorage().loadAttendees(getObjectIDs(events));
                for (Event event : events) {
                    event.setAttendees(attendeesById.get(I(event.getId())));
                }
            }
            if (null == fields || contains(fields, EventField.ATTACHMENTS)) {
                Map<Integer, List<Attachment>> attachmentsById = storage.getAttachmentStorage().loadAttachments(objectIDs);
                for (Event event : events) {
                    event.setAttachments(attachmentsById.get(I(event.getId())));
                }
            }
            if (0 < userID && (null == fields || contains(fields, EventField.ALARMS))) {
                Map<Integer, List<Alarm>> alarmsById = storage.getAlarmStorage().loadAlarms(objectIDs, userID);
                for (Event event : events) {
                    event.setAlarms(alarmsById.get(I(event.getId())));
                }
            }
        }
        return events;
    }

    /**
     * Loads additional event data from the storage, based on the requested fields. This currently includes
     * <ul>
     * <li>{@link EventField#ATTENDEES}</li>
     * <li>{@link EventField#ATTACHMENTS}</li>
     * <li>{@link EventField#ALARMS}</li> (of the calendar user)
     * </ul>
     *
     * @param storage A reference to the calendar storage to use
     * @param event The event to load additional data for
     * @param userID The identifier of the calendar user to load additional data for, or <code>-1</code> to not load user-sensitive data
     * @param fields The requested fields, or <code>null</code> to assume all fields are requested
     * @return The event, enriched by the additionally loaded data
     */
    public static Event loadAdditionalEventData(CalendarStorage storage, int userID, Event event, EventField[] fields) throws OXException {
        if (null != event && (null == fields || contains(fields, EventField.ATTENDEES))) {
            event.setAttendees(storage.getAttendeeStorage().loadAttendees(event.getId()));
        }
        if (null != event && (null == fields || contains(fields, EventField.ATTACHMENTS))) {
            event.setAttachments(storage.getAttachmentStorage().loadAttachments(event.getId()));
        }
        if (null != event && 0 < userID && (null == fields || contains(fields, EventField.ALARMS))) {
            event.setAlarms(storage.getAlarmStorage().loadAlarms(event.getId(), userID));
        }
        return event;
    }

    /**
     * Gets a list containing all elements provided by the supplied iterator.
     *
     * @param itrerator The iterator to get the list for
     * @return The list
     */
    public static <T> List<T> asList(Iterator<T> itrerator) {
        List<T> list = new ArrayList<T>();
        while (itrerator.hasNext()) {
            list.add(itrerator.next());
        }
        return list;
    }

    public static List<UserizedFolder> getVisibleFolders(CalendarSession session) throws OXException {
        return getVisibleFolders(session, PrivateType.getInstance(), SharedType.getInstance(), PublicType.getInstance());
    }

    public static List<UserizedFolder> getVisibleFolders(CalendarSession session, Type... types) throws OXException {
        List<UserizedFolder> visibleFolders = new ArrayList<UserizedFolder>();
        FolderService folderService = Services.getService(FolderService.class);
        for (Type type : types) {
            FolderResponse<UserizedFolder[]> response = folderService.getVisibleFolders(FolderStorage.REAL_TREE_ID, CalendarContentType.getInstance(), type, false, session.getSession(), initDecorator(session));
            UserizedFolder[] folders = response.getResponse();
            if (null != folders && 0 < folders.length) {
                visibleFolders.addAll(Arrays.asList(folders));
            }
        }
        return visibleFolders;
    }

    private static FolderServiceDecorator initDecorator(CalendarSession session) {
        FolderServiceDecorator decorator = new FolderServiceDecorator();
        Connection connection = session.get(StorageOperation.PARAM_CONNECTION, Connection.class, null);
        if (null != connection) {
            decorator.put(Connection.class.getName(), connection);
        }
        decorator.setLocale(session.getUser().getLocale());
        decorator.setTimeZone(Utils.getTimeZone(session));
        return decorator;
    }

}
