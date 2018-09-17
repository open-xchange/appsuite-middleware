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

package com.openexchange.chronos.common;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.tools.arrays.Collections.isNullOrEmpty;
import static org.slf4j.LoggerFactory.getLogger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.mail.internet.idn.IDNA;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventFlag;
import com.openexchange.chronos.EventStatus;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.Period;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.common.mapping.AbstractCollectionUpdate;
import com.openexchange.chronos.common.mapping.AbstractEventUpdates;
import com.openexchange.chronos.common.mapping.AbstractSimpleCollectionUpdate;
import com.openexchange.chronos.common.mapping.AttendeeMapper;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.EventUpdates;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.service.SimpleCollectionUpdate;
import com.openexchange.chronos.service.SortOrder;
import com.openexchange.chronos.service.TimestampedResult;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.ProblematicAttribute;
import com.openexchange.groupware.tools.mappings.Mapping;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;
import com.openexchange.search.Operand;
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

    /**
     * The default calendar account prefix.
     */
    public static final String DEFAULT_ACCOUNT_PREFIX = "cal://0/";

    /** A timestamp in the distant future as substitute for the client timestamp when circumventing concurrent modification checks */
    public static final long DISTANT_FUTURE = Long.MAX_VALUE;

    /** A comparator for (usually numerical) string identifiers */
    public static final Comparator<String> ID_COMPARATOR = new Comparator<String>() {

        @Override
        public int compare(String id1, String id2) {
            if (null == id1) {
                return null == id2 ? 0 : 1;
            }
            if (null == id2) {
                return -1;
            }
            try {
                return Integer.compare(Integer.parseInt(id1), Integer.parseInt(id2));
            } catch (NumberFormatException e) {
                // fall back to common string comparator
                return id1.compareTo(id2);
            }
        }
    };

    /** A collection of fields that are always included when querying events from the storage */
    private static final Set<EventField> DEFAULT_FIELDS = Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(
        EventField.ID, EventField.SERIES_ID, EventField.FOLDER_ID, EventField.RECURRENCE_ID, EventField.TIMESTAMP, EventField.CREATED_BY,
        EventField.CALENDAR_USER, EventField.CLASSIFICATION, EventField.START_DATE, EventField.END_DATE, EventField.RECURRENCE_RULE,
        EventField.CHANGE_EXCEPTION_DATES, EventField.DELETE_EXCEPTION_DATES, EventField.RECURRENCE_DATES, EventField.ORGANIZER
    )));

    /** A collection of identifying meta fields */
    private static final Set<EventField> IDENTIFYING_FIELDS = Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(
        EventField.ID, EventField.SERIES_ID, EventField.FOLDER_ID, EventField.RECURRENCE_ID, EventField.UID, EventField.FILENAME,
        EventField.TIMESTAMP, EventField.CREATED, EventField.LAST_MODIFIED, EventField.CREATED_BY
    )));

    /** A collection of fields that need to be queried to construct the special event flags field properly afterwards */
    private static final List<EventField> FLAG_FIELDS = Arrays.asList(
        EventField.ID, EventField.SERIES_ID, EventField.FOLDER_ID, EventField.RECURRENCE_ID,  EventField.STATUS, EventField.TRANSP,
        EventField.CLASSIFICATION, EventField.ORGANIZER, EventField.ATTACHMENTS, EventField.ALARMS, EventField.ATTENDEES
    );

    private static final ConcurrentMap<String, TimeZone> KNOWN_TIMEZONES = new ConcurrentHashMap<String, TimeZone>();

    /**
     * Gets a value indicating whether a specific recurrence id is present in a collection of recurrence identifiers, based on its value.
     *
     * @param recurrenceIds The recurrence id's to search
     * @param recurrenceId The recurrence id to lookup
     * @return <code>true</code> if a matching recurrence identifier is contained in the collection, <code>false</code>, otherwise
     */
    public static boolean contains(Collection<RecurrenceId> recurrenceIds, RecurrenceId recurrenceId) {
        return null != recurrenceIds && recurrenceIds.contains(recurrenceId);
    }

    /**
     * Looks up a specific calendar user in a collection of attendees, utilizing the {@link CalendarUtils#matches} routine.
     *
     * @param attendees The attendees to search
     * @param calendarUser The calendar user to lookup
     * @return The matching calendar user, or <code>null</code> if not found
     * @see CalendarUtils#matches
     */
    public static Attendee find(List<Attendee> attendees, CalendarUser calendarUser) {
        if (null != attendees && 0 < attendees.size()) {
            for (Attendee candidateAttendee : attendees) {
                if (matches(calendarUser, candidateAttendee)) {
                    return candidateAttendee;
                }
            }
        }
        return null;
    }

    /**
     * Gets a value indicating whether a specific calendar user is present in a collection of attendees, utilizing the
     * {@link CalendarUtils#matches} routine.
     *
     * @param attendees The attendees to search
     * @param calendarUser The calendar user to lookup
     * @return <code>true</code> if the calendar user is contained in the collection of attendees, <code>false</code>, otherwise
     * @see CalendarUtils#matches
     */
    public static boolean contains(List<Attendee> attendees, CalendarUser calendarUser) {
        return null != find(attendees, calendarUser);
    }

    /**
     * Finds a specific event identified by its object-identifier in a collection.
     *
     * @param events The events to search in
     * @param objectID The object identifier of the event to search
     * @return The event, or <code>null</code> if not found
     */
    public static Event find(Collection<Event> events, String objectID) {
        if (null != events) {
            for (Event event : events) {
                if (objectID.equals(event.getId())) {
                    return event;
                }
            }
        }
        return null;
    }

    /**
     * Finds all overridden instances (<i>change exceptions</i>) of a specific recurring event series in a collection.
     *
     * @param events The events to search in
     * @param seriesId The series identifier to search the overridden instances for
     * @return All matching overridden instances (<i>change exceptions</i>) of the series, or an empty list if there are none
     */
    public static List<Event> findExceptions(Collection<Event> events, String seriesId) {
        List<Event> changeExceptions = new ArrayList<Event>();
        if (null != events) {
            for (Event event : events) {
                if (seriesId.equals(event.getSeriesId()) && isSeriesException(event)) {
                    changeExceptions.add(event);
                }
            }
        }
        return changeExceptions;
    }

    /**
     * Finds a specific event identified by its object-identifier and an optional recurrence identifier in a collection.
     *
     * @param events The events to search in
     * @param objectID The object identifier of the event to search
     * @param recurrenceID The rcurrence identifier of the event to search
     * @return The event, or <code>null</code> if not found
     */
    public static Event find(Collection<Event> events, String objectID, RecurrenceId recurrenceID) {
        if (null != events) {
            for (Event event : events) {
                if (objectID.equals(event.getId())) {
                    if (null == recurrenceID || recurrenceID.equals(event.getRecurrenceId())) {
                        return event;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets a value indicating whether one calendar user matches another, by comparing the entity identifier for internal calendar users,
     * or trying to match the calendar user's URI for external ones.
     *
     * @param user1 The first calendar user to check
     * @param user2 The second calendar user to check
     * @return <code>true</code> if the objects <i>match</i>, i.e. are targeting the same calendar user, <code>false</code>, otherwise
     */
    public static boolean matches(CalendarUser user1, CalendarUser user2) {
        if (null == user1) {
            return null == user2;
        } else if (null != user2) {
            if (0 < user1.getEntity() && user1.getEntity() == user2.getEntity()) {
                return true;
            }
            if (null != user1.getUri() && user1.getUri().equalsIgnoreCase(user2.getUri())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a value indicating whether a calendar user matches a specific internal user entity.
     *
     * @param calendarUser The calendar user to check
     * @param userID The entity identifier of the user to check
     * @return <code>true</code> if the objects <i>match</i>, i.e. are targeting the same calendar user, <code>false</code>, otherwise
     */
    public static boolean matches(CalendarUser calendarUser, int userID) {
        return null != calendarUser && calendarUser.getEntity() == userID;
    }

    /**
     * Gets a value indicating whether one calendar user equals another, by comparing all properties of the calendar users, after the
     * identifying properties (<i>entity</i> and <i>uri</i>) do match based on {@link #matches(CalendarUser, CalendarUser)}.
     *
     * @param user1 The first calendar user to check
     * @param user2 The second calendar user to check
     * @return <code>true</code> if the objects are <i>equal</i>, i.e. the users match and all further properties are equal in both user references, <code>false</code>, otherwise
     */
    public static boolean equals(CalendarUser user1, CalendarUser user2) {
        if (null == user1) {
            return null == user2;
        }
        if (null == user2) {
            return false;
        }
        if (false == matches(user1, user2)) {
            return false;
        }
        if (user1.getCn() == null) {
            if (user2.getCn() != null) {
                return false;
            }
        } else if (!user1.getCn().equals(user2.getCn())) {
            return false;
        }
        if (user1.getEMail() == null) {
            if (user2.getEMail() != null) {
                return false;
            }
        } else if (!user1.getEMail().equals(user2.getEMail())) {
            return false;
        }
        if (user1.getSentBy() == null) {
            if (user2.getSentBy() != null) {
                return false;
            }
        } else if (!user1.getSentBy().equals(user2.getSentBy())) {
            return false;
        }
        return true;
    }

    /**
     * Looks up a specific internal attendee in a collection of attendees based on its entity identifier.
     *
     * @param attendees The attendees to search
     * @param entity The entity identifier to lookup
     * @return The matching attendee, or <code>null</code> if not found
     */
    public static Attendee find(List<Attendee> attendees, int entity) {
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
     * Looks up a specific (managed) attachment in a collection of attachments based on its managed identifier.
     *
     * @param attachments The attachments to search
     * @param managedId The managed identifier to lookup
     * @return The matching attachment, or <code>null</code> if not found
     */
    public static Attachment findAttachment(List<Attachment> attachments, int managedId) {
        if (null != attachments && 0 < attachments.size()) {
            for (Attachment attachment : attachments) {
                if (managedId == attachment.getManagedId()) {
                    return attachment;
                }
            }
        }
        return null;
    }

    /**
     * Gets a value indicating whether an attendee represents an <i>internal</i> entity, i.e. an internal user, group or resource, or not.
     *
     * @param attendee The attendee to check
     * @return <code>true</code> if the attendee is internal, <code>false</code>, otherwise
     */
    public static boolean isInternal(Attendee attendee) {
        return isInternal(attendee, attendee.getCuType());
    }

    /**
     * Gets a value indicating whether an attendee represents an <i>internal</i> user entity or not.
     *
     * @param attendee The attendee to check
     * @return <code>true</code> if the attendee is internal, <code>false</code>, otherwise
     */
    public static boolean isInternalUser(Attendee attendee) {
        return null != attendee && CalendarUserType.INDIVIDUAL.matches(attendee.getCuType()) && attendee.getEntity() > 0;
    }

    /**
     * Gets a value indicating whether an attendee represents an <i>external</i> user entity or not.
     *
     * @param attendee The attendee to check
     * @return <code>true</code> if the attendee is internal, <code>false</code>, otherwise
     */
    public static boolean isExternalUser(Attendee attendee) {
        return null != attendee && CalendarUserType.INDIVIDUAL.matches(attendee.getCuType()) && attendee.getEntity() <= 0;
    }

    /**
     * Gets a value indicating whether a calendar user represents an <i>internal</i> entity, i.e. an internal user, group or resource, or not.
     *
     * @param calendarUser The calendar user to check
     * @param cuType The calendar user type to assume
     * @return <code>true</code> if the calendar user is internal, <code>false</code>, otherwise
     */
    public static boolean isInternal(CalendarUser calendarUser, CalendarUserType cuType) {
        return 0 < calendarUser.getEntity() || 0 == calendarUser.getEntity() && CalendarUserType.GROUP.equals(cuType);
    }

    /**
     * Gets a value indicating whether a collection of attendees contains a specific internal attendee based on its entity identifier or
     * not.
     *
     * @param attendees The attendees to search
     * @param entity The entity identifier to lookup
     * @return <code>true</code> if the attendee was found, <code>false</code>, otherwise
     */
    public static boolean contains(List<Attendee> attendees, int entity) {
        return null != find(attendees, entity);
    }

    /**
     * Gets a value indicating whether a specific user is the organizer of an event or not.
     *
     * @param event The event
     * @param userId The identifier of the user to check
     * @return <code>true</code> if the user with the supplied identifier is the organizer, <code>false</code>, otherwise
     */
    public static boolean isOrganizer(Event event, int userId) {
        return null != event.getOrganizer() && userId == event.getOrganizer().getEntity();
    }

    /**
     * Gets a value indicating whether a specific event is organized externally, i.e. no internal organizer entity is responsible.
     *
     * @param event The event to check
     * @return <code>true</code> if the event has an <i>external</i> organizer, <code>false</code>, otherwise
     */
    public static boolean hasExternalOrganizer(Event event) {
        return null != event.getOrganizer() && 0 >= event.getOrganizer().getEntity();
    }

    /**
     * Gets a value indicating whether a specific user is an attendee of an event or not.
     *
     * @param event The event
     * @param userId The identifier of the user to check
     * @return <code>true</code> if the user with the supplied identifier is an attendee, <code>false</code>, otherwise
     */
    public static boolean isAttendee(Event event, int userId) {
        return contains(event.getAttendees(), userId);
    }

    /**
     * Gets a value indicating whether a specific user is the only / the last internal user attendee in an attendee list.
     *
     * @param attendees The attendees to check
     * @param userID The identifier of the user to lookup in the attendee list
     * @return <code>true</code> if there are no other internal user attendees despite the specified one, <code>false</code>, otherwise
     */
    public static boolean isLastUserAttendee(List<Attendee> attendees, int userID) {
        List<Attendee> userAttendees = filter(attendees, Boolean.TRUE, CalendarUserType.INDIVIDUAL);
        return null != userAttendees && 1 == userAttendees.size() && userID == userAttendees.get(0).getEntity();
    }

    /**
     * Gets a date representing the supplied date-time's value.
     *
     * @param dateTime The date-time to get the corresponding date for
     * @return The date, or <code>null</code> if passed date-time reference was null
     */
    public static Date asDate(DateTime dateTime) {
        return null == dateTime ? null : new Date(dateTime.getTimestamp());
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
     * Converts a so-called <i>floating</i> date into a date in a concrete timezone by applying the actual timezone offset on that date.
     *
     * @param floatingDate The floating date to convert (usually the raw date in <code>UTC</code>)
     * @param timeZone The target timezone
     * @return The date in the target timezone, with the corresponding timezone offset applied
     */
    public static Date getDateInTimeZone(Date floatingDate, TimeZone timeZone) {
        return new Date(floatingDate.getTime() - timeZone.getOffset(floatingDate.getTime()));
    }

    /**
     * Converts a so-called <i>floating</i> date into a date in a concrete timezone by applying the actual timezone offset on that date.
     *
     * @param floatingDate The floating date to convert (usually the raw date in <code>UTC</code>)
     * @param timeZone The target timezone
     * @return The date in the target timezone, with the corresponding timezone offset applied
     */
    public static long getDateInTimeZone(DateTime floatingDate, TimeZone timeZone) {
        if (false == floatingDate.isFloating() || null == timeZone) {
            return floatingDate.getTimestamp();
        }
        return floatingDate.getTimestamp() - timeZone.getOffset(floatingDate.getTimestamp());
    }

    /**
     * Adds or subtracts the specified amount of time of the given calendar field to the supplied date.<p/>
     * The calendar is initialized with <code>UTC</code> timezone implicitly.
     *
     * @param date The date to add or subtract the time to/from
     * @param field The calendar field
     * @param amount The amount of date or time to be added to the field
     * @return A new date derived from the calendar after adding the amount of time
     */
    public static Date add(Date date, int field, int amount) {
        return add(date, field, amount, TimeZones.UTC);
    }

    /**
     * Adds or subtracts the specified amount of time of the given calendar field to the supplied date.
     *
     * @param date The initial time to set, or <code>null</code> to intialize with the default time
     * @param field The calendar field
     * @param amount The amount of date or time to be added to the field
     * @param timeZone The timezone to perform the add/substract operation in
     * @return A new date derived from the calendar after adding the amount of time
     */
    public static Date add(Date date, int field, int amount, TimeZone timeZone) {
        Calendar calendar = initCalendar(timeZone, date);
        calendar.add(field, amount);
        return calendar.getTime();
    }

    /**
     * Gets the maximum timestamp of a collection of events.
     *
     * @param events The events to get the maximum timestamp from
     * @return The maximum timestamp as {@link Date}, or <code>null</code> if the supplied collection was <code>null</code> or empty
     */
    public static Date getMaximumTimestamp(Collection<Event> events) {
        if (null == events || events.isEmpty()) {
            return null;
        }
        long maximumTimestamp = Integer.MIN_VALUE;
        for (Event event : events) {
            if (null != event) {
                maximumTimestamp = Math.max(maximumTimestamp, event.getTimestamp());
            }
        }
        return Integer.MIN_VALUE == maximumTimestamp ? null : new Date(maximumTimestamp);
    }

    /**
     * Gets the maximum timestamp of a map containing multiple timestamped results.
     *
     * @param results The results to get the maximum timestamp from
     * @return The maximum timestamp as {@link Date}, or <code>null</code> if all values in the supplied map were <code>null</code> or empty
     */
    public static <K, V extends TimestampedResult> Date getMaximumTimestamp(Map<K, V> results) {
        long maximumTimestamp = Integer.MIN_VALUE;
        if (null != results && 0 < results.size()) {
            for (TimestampedResult result : results.values()) {
                maximumTimestamp = Math.max(maximumTimestamp, result.getTimestamp());
            }
        }
        return Integer.MIN_VALUE == maximumTimestamp ? null : new Date(maximumTimestamp);
    }

    /**
     * Gets the identifiers of the supplied events in an array.
     *
     * @param events The events to get the identifiers for
     * @return The object identifiers
     */
    public static String[] getObjectIDs(List<Event> events) {
        if (null == events || 0 == events.size()) {
            return new String[0];
        }
        Set<String> objectIDs = new HashSet<String>(events.size());
        for (Event event : events) {
            objectIDs.add(event.getId());
        }
        return objectIDs.toArray(new String[objectIDs.size()]);
    }

    /**
     * Maps a collection of events by their identifier.
     *
     * @param events The events to map
     * @return The mapped events
     */
    public static Map<String, Event> getEventsByID(Collection<Event> events) {
        if (null == events) {
            return null;
        }
        Map<String, Event> eventsByID = new HashMap<String, Event>(events.size());
        for (Event event : events) {
            eventsByID.put(event.getId(), event);
        }
        return eventsByID;
    }

    /**
     * Gets a value indicating whether the supplied event is considered as the <i>master</i> event of a recurring series or not, based
     * on checking the properties {@link EventField#ID} and {@link EventField#SERIES_ID} for equality and the absence of an assigned
     * {@link EventField#RECURRENCE_ID}.
     *
     * @param event The event to check
     * @return <code>true</code> if the event is the series master, <code>false</code>, otherwise
     */
    public static boolean isSeriesMaster(Event event) {
        return null != event && null != event.getId() && event.getId().equals(event.getSeriesId()) && null == event.getRecurrenceId();
    }

    /**
     * Gets a value indicating whether the supplied event is considered as an exceptional event of a recurring series or not, based on
     * the properties {@link EventField#ID} and {@link EventField#SERIES_ID}.
     *
     * @param event The event to check
     * @return <code>true</code> if the event is the series master, <code>false</code>, otherwise
     */
    public static boolean isSeriesException(Event event) {
        return null != event && null != event.getSeriesId() && false == event.getSeriesId().equals(event.getId());
    }

    /**
     * Initializes a new recurrence rule for the supplied recurrence rule string.
     *
     * @param rrule The recurrence rule string
     * @return The recurrence rule
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RRULE}
     */
    public static RecurrenceRule initRecurrenceRule(String rrule) throws OXException {
        try {
            return new RecurrenceRule(rrule);
        } catch (InvalidRecurrenceRuleException | IllegalArgumentException e) {
            throw CalendarExceptionCodes.INVALID_RRULE.create(e, rrule);
        }
    }

    /**
     * Gets a value indicating whether an updated recurrence rule would produce further, additional occurrences compared to the original
     * rule.
     *
     * @param originalRRule The original recurrence rule, or <code>null</code> if there was none
     * @param updatedRRule The original recurrence rule, or <code>null</code> if there is none
     * @return <code>true</code> if the updated rule yields further occurrences or unsure, <code>false</code>, otherwise
     */
    public static boolean hasFurtherOccurrences(String originalRRule, String updatedRRule) throws OXException {
        if (null == originalRRule) {
            return null != updatedRRule;
        }
        if (null == updatedRRule) {
            return false;
        }
        RecurrenceRule originalRule = initRecurrenceRule(originalRRule);
        RecurrenceRule updatedRule = initRecurrenceRule(updatedRRule);
        /*
         * check if only UNTIL was changed
         */
        RecurrenceRule checkedRule = initRecurrenceRule(updatedRule.toString());
        checkedRule.setUntil(originalRule.getUntil());
        if (checkedRule.toString().equals(originalRule.toString())) {
            return 1 == CalendarUtils.compare(originalRule.getUntil(), updatedRule.getUntil(), null);
        }
        /*
         * check if only COUNT was changed
         */
        checkedRule = initRecurrenceRule(updatedRule.toString());
        if (null == originalRule.getCount()) {
            checkedRule.setUntil(null);
        } else {
            checkedRule.setCount(i(originalRule.getCount()));
        }
        if (checkedRule.toString().equals(originalRule.toString())) {
            int originalCount = null == originalRule.getCount() ? Integer.MAX_VALUE : i(originalRule.getCount());
            int updatedCount = null == updatedRule.getCount() ? Integer.MAX_VALUE : i(updatedRule.getCount());
            return updatedCount > originalCount;
        }
        /*
         * check if only the INTERVAL was extended
         */
        checkedRule = initRecurrenceRule(updatedRule.toString());
        checkedRule.setInterval(originalRule.getInterval());
        if (checkedRule.toString().equals(originalRule.toString())) {
            return 0 != updatedRule.getInterval() % originalRule.getInterval();
        }
        /*
         * check if each BY... part is equally or more restrictive
         */
        //TODO
        return true;
    }

    /**
     * Initializes a new calendar in a specific timezone and sets the initial time.
     *
     * @param timeZone The timezone to use for the calendar
     * @param time The initial time to set, or <code>null</code> to intialize with the default time
     * @return A new calendar instance
     */
    public static Calendar initCalendar(TimeZone timeZone, Date time) {
        Calendar calendar = GregorianCalendar.getInstance(timeZone);
        if (null != time) {
            calendar.setTime(time);
        }
        return calendar;
    }

    /**
     * Initializes a new calendar in a specific timezone and sets the initial time.
     *
     * @param timeZone The timezone to use for the calendar
     * @param time The initial time in UTC milliseconds from the epoch
     * @return A new calendar instance
     */
    public static Calendar initCalendar(TimeZone timeZone, long time) {
        Calendar calendar = GregorianCalendar.getInstance(timeZone);
        calendar.setTimeInMillis(time);
        return calendar;
    }

    /**
     * Initializes a new calendar in a specific timezone and sets the initial time.
     * <p/>
     * The field {@link Calendar#MILLISECOND} is set to <code>0</code> explicitly.
     *
     * @param timeZone The timezone to use for the calendar
     * @param year The value used to set the {@link Calendar#YEAR} field
     * @param month The value used to set the {@link Calendar#MONTH} field
     * @param date The value used to set the {@link Calendar#DATE} field
     * @return A new calendar instance
     */
    public static Calendar initCalendar(TimeZone timeZone, int year, int month, int date) {
        return initCalendar(timeZone, year, month, date, 0, 0, 0);
    }

    /**
     * Initializes a new calendar in a specific timezone and sets the initial time.
     * <p/>
     * The field {@link Calendar#MILLISECOND} is set to <code>0</code> explicitly.
     *
     * @param timeZone The timezone to use for the calendar
     * @param year The value used to set the {@link Calendar#YEAR} field
     * @param month The value used to set the {@link Calendar#MONTH} field
     * @param date The value used to set the {@link Calendar#DATE} field
     * @param hourOfDay The value used to set the {@link Calendar#HOUR_OF_DAY} field
     * @param minute The value used to set the {@link Calendar#MINUTE} field
     * @param second The value used to set the {@link Calendar#SECOND} field
     * @return A new calendar instance
     */
    public static Calendar initCalendar(TimeZone timeZone, int year, int month, int date, int hourOfDay, int minute, int second) {
        Calendar calendar = GregorianCalendar.getInstance(timeZone);
        calendar.set(year, month, date, hourOfDay, minute, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**
     * Gets a value indicating whether a specific event falls (at least partly) into a time range.
     * <p/>
     * According to RFC 4791, an event overlaps a given time range if the condition for the corresponding component state specified in
     * the table below is satisfied:
     * <pre>
     * +---------------------------------------------------------------+
     * | VEVENT has the DTEND property?                                |
     * |   +-----------------------------------------------------------+
     * |   |   VEVENT has the DURATION property?                       |
     * |   |   +-------------------------------------------------------+
     * |   |   |   DURATION property value is greater than 0 seconds?  |
     * |   |   |   +---------------------------------------------------+
     * |   |   |   |   DTSTART property is a DATE-TIME value?          |
     * |   |   |   |   +-----------------------------------------------+
     * |   |   |   |   | Condition to evaluate                         |
     * +---+---+---+---+-----------------------------------------------+
     * | Y | N | N | * | (start <  DTEND AND end > DTSTART)            |
     * +---+---+---+---+-----------------------------------------------+
     * | N | Y | Y | * | (start <  DTSTART+DURATION AND end > DTSTART) |
     * |   |   +---+---+-----------------------------------------------+
     * |   |   | N | * | (start <= DTSTART AND end > DTSTART)          |
     * +---+---+---+---+-----------------------------------------------+
     * | N | N | N | Y | (start <= DTSTART AND end > DTSTART)          |
     * +---+---+---+---+-----------------------------------------------+
     * | N | N | N | N | (start <  DTSTART+P1D AND end > DTSTART)      |
     * +---+---+---+---+-----------------------------------------------+
     * </pre>
     *
     * @param event The event to check
     * @param from The lower inclusive limit of the range, i.e. the event should start on or after this date, or <code>null</code> for no limit
     * @param until The upper exclusive limit of the range, i.e. the event should end before this date, or <code>null</code> for no limit
     * @param timeZone The timezone to consider if the event has <i>floating</i> dates
     * @return <code>true</code> if the event falls into the time range, <code>false</code>, otherwise
     * @see <a href="https://tools.ietf.org/html/rfc4791#section-9.9">RFC 4791, section 9.9</a>
     */
    public static boolean isInRange(Event event, Date from, Date until, TimeZone timeZone) {
        /*
         * determine effective timestamps for check
         */
        long start = null == from ? Long.MIN_VALUE : from.getTime();
        long end = null == until ? Long.MAX_VALUE : until.getTime();
        long dtStart = event.getStartDate().getTimestamp();
        if (isFloating(event)) {
            dtStart -= timeZone.getOffset(dtStart);
        }
        /*
         * check if a 'real' end date is set in event
         */
        boolean hasDtEnd;
        long dtEnd;
        if (null == event.getEndDate()) {
            dtEnd = 0;
            hasDtEnd = false;
        } else {
            dtEnd = event.getEndDate().getTimestamp();
            if (isFloating(event)) {
                dtEnd -= timeZone.getOffset(dtEnd);
            }
            if (event.getStartDate().isAllDay()) {
                Calendar calendar = initCalendar(timeZone, dtStart);
                calendar.add(Calendar.DATE, 1);
                hasDtEnd = calendar.getTimeInMillis() != dtEnd;
            } else {
                hasDtEnd = dtStart != dtEnd;
            }
        }
        /*
         * perform checks
         */
        if (hasDtEnd) {
            // VEVENT has the DTEND property? Y
            // (start < DTEND AND end > DTSTART)
            return start < dtEnd && end > dtStart;
        } else {
            // VEVENT has the DTEND property? N
            if (false == event.getStartDate().isAllDay()) {
                // DTSTART property is a DATE-TIME value? Y
                // (start <= DTSTART AND end > DTSTART)
                return start <= dtStart && end > dtStart;
            } else {
                // DTSTART property is a DATE-TIME value? N
                // (start < DTSTART+P1D AND end > DTSTART)
                if (end > dtStart) {
                    Calendar calendar = initCalendar(timeZone, dtStart);
                    calendar.add(Calendar.DATE, 1);
                    return start < calendar.getTimeInMillis();
                } else {
                    return false;
                }
            }
        }
    }

    /**
     * Gets a value indicating whether a specific event falls (at least partly) into the time range of another event.
     *
     * @param event The event to check
     * @param event2 The second event to check against
     * @param timeZone The timezone to consider if one or the other event has <i>floating</i> dates
     * @return <code>true</code> if the event falls into the time range of the other, <code>false</code>, otherwise
     */
    public static boolean isInRange(Event event, Event event2, TimeZone timeZone) {
        long from = isFloating(event2) ? getDateInTimeZone(event2.getStartDate(), timeZone) : event2.getStartDate().getTimestamp();
        long until = isFloating(event2) ? getDateInTimeZone(event2.getEndDate(), timeZone) : event2.getEndDate().getTimestamp();
        return isInRange(event, new Date(from), new Date(until), timeZone);
    }

    /**
     * Gets a value indicating whether a specific period falls (at least partly) into a time range.
     *
     * @param period The period to check
     * @param from The lower inclusive limit of the range, i.e. the event should start on or after this date, or <code>null</code> for no limit
     * @param until The upper exclusive limit of the range, i.e. the event should end before this date, or <code>null</code> for no limit
     * @param timeZone The timezone to consider if the period is <i>all-day</i> (so has <i>floating</i> dates)
     * @return <code>true</code> if the event falls into the time range, <code>false</code>, otherwise
     */
    public static boolean isInRange(Period period, Date from, Date until, TimeZone timeZone) {
        Date startDate = period.isAllDay() ? getDateInTimeZone(period.getStartDate(), timeZone) : period.getStartDate();
        Date endDate = period.isAllDay() ? getDateInTimeZone(period.getEndDate(), timeZone) : period.getEndDate();
        return (null == until || startDate.before(until)) && (null == from || endDate.after(from));
    }

    /**
     * Gets a value indicating whether the supplied event contains so-called <i>floating</i> dates, i.e. the event doesn't start- and end
     * at a fixed date and time, but is always rendered in the view of the user's current timezone.
     * <p/>
     * Especially, <i>all-day</i> events are usually floating.
     *
     * @param event The event to check
     * @return <code>true</code> if the event is <i>floating</i>, <code>false</code>, otherwise
     */
    public static boolean isFloating(Event event) {
        return null != event.getStartDate() && event.getStartDate().isFloating();
    }

    /**
     * Gets a value indicating whether the supplied event has <i>all-day</i> character or not, i.e. its start- and endtime do not contain
     * a time of day or not. This implicitly implies that the event is also <i>floating</i>.
     *
     * @param event The event to check
     * @return <code>true</code> if the event has <i>all-day</i> character, <code>false</code>, otherwise
     */
    public static boolean isAllDay(Event event) {
        return null != event.getStartDate() && event.getStartDate().isAllDay();
    }

    /**
     * Compares the supplied objects for order, from the perspective of the given timezone for <i>floating</i> dates.
     *
     * @param dateTime1 The first date-time to compare
     * @param dateTime2 The second date-time to compare
     * @param timeZone The timezone to consider for <i>floating</i> dates, i.e. the actual 'perspective' of the comparison, or
     *            <code>null</code> to fall back to UTC
     * @return A negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
     */
    public static int compare(DateTime dateTime1, DateTime dateTime2, TimeZone timeZone) {
        if (null == dateTime1) {
            return null == dateTime2 ? 0 : -1;
        }
        if (null == dateTime2) {
            return 1;
        }
        long timestamp1 = dateTime1.isFloating() ? getDateInTimeZone(dateTime1, timeZone) : dateTime1.getTimestamp();
        long timestamp2 = dateTime2.isFloating() ? getDateInTimeZone(dateTime2, timeZone) : dateTime2.getTimestamp();
        return Long.compare(timestamp1, timestamp2);
    }

    /**
     * Calculates the duration between the supplied dates or date-times, from the perspective of the given timezone for <i>floating</i>
     * dates.
     * <p/>
     * If one of the supplied dates is <i>floating</i>, and the other one is not (i.e. it represents a <i>date with local time
     * timezone</i>), the difference is determined by transferring the floating date into this local timezone first.
     *
     * @param dateTime1 The first date-time
     * @param dateTime2 The second date-time
     * @return The duration between the dates
     */
    public static Duration getDuration(DateTime dateTime1, DateTime dateTime2) {
        if (dateTime1.isAllDay() && dateTime2.isAllDay()) {
            int days = 0;
            if (dateTime1.before(dateTime2)) {
                Duration ONE_DAY_MORE = new Duration(1, 1, 0);
                for (DateTime current = dateTime1; false == current.equals(dateTime2); current = current.addDuration(ONE_DAY_MORE), days++) {
                    ;
                }
            }
            if (dateTime1.after(dateTime2)) {
                Duration ONE_DAY_LESS = new Duration(-1, 1, 0);
                for (DateTime current = dateTime1; false == current.equals(dateTime2); current = current.addDuration(ONE_DAY_LESS), days--) {
                    ;
                }
            }
            return new Duration(days >= 0 ? 1 : -1, Math.abs(days), 0);
        }
        long timestamp1 = dateTime1.isFloating() && null != dateTime2.getTimeZone() ? getDateInTimeZone(dateTime1, dateTime2.getTimeZone()) : dateTime1.getTimestamp();
        long timestamp2 = dateTime2.isFloating() && null != dateTime1.getTimeZone() ? getDateInTimeZone(dateTime2, dateTime1.getTimeZone()) : dateTime2.getTimestamp();
        return Duration.parse(AlarmUtils.getDuration(timestamp2 - timestamp1, TimeUnit.MILLISECONDS));
    }

    /**
     * Adjusts a collection of recurrence identifiers by applying an offset based on the difference of an original and updated series
     * start date.
     *
     * @param originalRecurrenceIds The original recurrence identifiers
     * @param updatedRecurrenceIds The updated recurrence identifiers to adjust
     * @param originalSeriesStart The original start date of the underlying series event
     * @param updatedSeriesStart The original start date of the underlying series event
     * @return A set of new recurrence identifier instance whose values are shifted accordingly
     */
    public static SortedSet<RecurrenceId> shiftRecurrenceIds(SortedSet<RecurrenceId> originalRecurrenceIds, SortedSet<RecurrenceId> updatedRecurrenceIds, DateTime originalSeriesStart, DateTime updatedSeriesStart) {
        if (isNullOrEmpty(updatedRecurrenceIds) || isNullOrEmpty(originalRecurrenceIds)) {
            return updatedRecurrenceIds;
        }
        TreeSet<RecurrenceId> changedRecurrenceIds = new TreeSet<RecurrenceId>(updatedRecurrenceIds);
        for (RecurrenceId originalRecurrenceId : originalRecurrenceIds) {
            if (changedRecurrenceIds.remove(originalRecurrenceId)) {
                changedRecurrenceIds.add(shiftRecurrenceId(originalRecurrenceId, originalSeriesStart, updatedSeriesStart));
            }
        }
        return changedRecurrenceIds;
    }

    /**
     * Adjusts an recurrence identifier by applying an offset based on the difference of an original and updated series start date.
     *
     * @param originalRecurrenceId The original recurrence identifier to adjust
     * @param originalSeriesStart The original start date of the underlying series event
     * @param updatedSeriesStart The original start date of the underlying series event
     * @return A new recurrence identifier instance whose value is shifted accordingly
     */
    public static RecurrenceId shiftRecurrenceId(RecurrenceId originalRecurrenceId, DateTime originalSeriesStart, DateTime updatedSeriesStart) {
        if (originalSeriesStart.isAllDay() && updatedSeriesStart.isAllDay()) {
            /*
             * both 'all-day', apply offset day-wise & return an 'all-day' recurrence id
             */
            DateTime value = originalRecurrenceId.getValue();
            if (originalSeriesStart.before(updatedSeriesStart)) {
                Duration ONE_DAY_MORE = new Duration(1, 1, 0);
                for (DateTime current = originalSeriesStart; false == current.equals(updatedSeriesStart); current = current.addDuration(ONE_DAY_MORE), value = value.addDuration(ONE_DAY_MORE)) {
                    ;
                }
            }
            if (originalSeriesStart.after(updatedSeriesStart)) {
                Duration ONE_DAY_LESS = new Duration(-1, 1, 0);
                for (DateTime current = originalSeriesStart; false == current.equals(updatedSeriesStart); current = current.addDuration(ONE_DAY_LESS), value = value.addDuration(ONE_DAY_LESS)) {
                    ;
                }
            }
            return new DefaultRecurrenceId(value);
        }
        if (originalSeriesStart.isFloating() && updatedSeriesStart.isFloating()) {
            /*
             * both 'floating', apply relative offset & return a 'floating' or 'all-day' recurrence id
             */
            long offset = updatedSeriesStart.getTimestamp() - originalSeriesStart.getTimestamp();
            DateTime value = new DateTime(null, originalRecurrenceId.getValue().getTimestamp() + offset);
            return new DefaultRecurrenceId(updatedSeriesStart.isAllDay() ? value.toAllDay() : value);
        }
        if (originalSeriesStart.isFloating()) {
            /*
             * from 'floating' to 'non-floating', apply offset in timezone of updated series start & return a fixed recurrence id
             */
            long offset = updatedSeriesStart.getTimestamp() - getDateInTimeZone(originalSeriesStart, updatedSeriesStart.getTimeZone());
            return new DefaultRecurrenceId(new DateTime(originalRecurrenceId.getValue().getTimestamp() + offset));
        }
        if (updatedSeriesStart.isFloating()) {
            /*
             * from 'non-floating' to 'floating', apply offset in timezone of original series start & return a 'floating' or 'all-day' recurrence id
             */
            long offset = getDateInTimeZone(updatedSeriesStart, originalSeriesStart.getTimeZone()) - originalSeriesStart.getTimestamp();
            DateTime value = new DateTime(originalRecurrenceId.getValue().getTimestamp() + offset);
            return new DefaultRecurrenceId(updatedSeriesStart.isAllDay() ? value.toAllDay() : value);
        }
        /*
         * both 'non-floating', apply relative offset & return a fixed recurrence id
         */
        long offset = updatedSeriesStart.getTimestamp() - originalSeriesStart.getTimestamp();
        return new DefaultRecurrenceId(new DateTime(originalRecurrenceId.getValue().getTimestamp() + offset));
    }

    /**
     * Filters a list of attendees based on their calendaruser type, and whether they represent "internal" attendees or not.
     *
     * @param attendees The attendees to filter
     * @param internal {@link Boolean#TRUE} to only consider internal entities, {@link Boolean#FALSE} for non-internal ones,
     *            or <code>null</code> to not filter by internal/external
     * @param cuTypes The {@link CalendarUserType}s to consider, or <code>null</code> to not filter by calendar user type
     * @return The filtered attendees, or an empty list if there were no matching attendees
     */
    public static List<Attendee> filter(List<Attendee> attendees, Boolean internal, CalendarUserType... cuTypes) {
        if (null == attendees) {
            return Collections.emptyList();
        }
        List<Attendee> filteredAttendees = new ArrayList<Attendee>(attendees.size());
        for (Attendee attendee : attendees) {
            if (null == cuTypes || com.openexchange.tools.arrays.Arrays.contains(cuTypes, attendee.getCuType())) {
                if (null == internal || internal.booleanValue() == isInternal(attendee)) {
                    filteredAttendees.add(attendee);
                }
            }
        }
        return filteredAttendees;
    }

    /**
     * Filters a list of attendees based on their group membership, i.e. gets all attendees that are associated with a specific group.
     *
     * @param attendees The attendees to filter
     * @param groupUri The group uri to filter the attendees by
     * @return The filtered attendees, or an empty list if there were no matching attendees
     */
    public static List<Attendee> filterByMembership(List<Attendee> attendees, String groupUri) {
        if (null == attendees) {
            return Collections.emptyList();
        }
        List<Attendee> filteredAttendees = new ArrayList<Attendee>(attendees.size());
        for (Attendee attendee : attendees) {
            List<String> groupMemberships = attendee.getMember();
            if (null == groupMemberships || 0 == groupMemberships.size()) {
                continue;
            }
            for (String groupMembership : groupMemberships) {
                if (groupMembership.equals(groupUri)) {
                    filteredAttendees.add(attendee);
                    break;
                }
            }
        }
        return filteredAttendees;
    }

    /**
     * Gets the entity identifiers of all attendees representing internal users.
     *
     * @param attendees The attendees to extract the user identifiers for
     * @return The user identifiers, or an empty array if there are none
     */
    public static int[] getUserIDs(List<Attendee> attendees) {
        if (null == attendees || 0 == attendees.size()) {
            return new int[0];
        }
        List<Integer> userIDs = new ArrayList<Integer>(attendees.size());
        for (Attendee attendee : attendees) {
            if (CalendarUserType.INDIVIDUAL.equals(attendee.getCuType()) && isInternal(attendee)) {
                userIDs.add(I(attendee.getEntity()));
            }
        }
        return I2i(userIDs);
    }

    /**
     * Gets the identifiers of all alarms in the supplied alarm collection.
     *
     * @param alarms The alarms to extract the user identifiers for
     * @return The alarm identifiers, or an empty array if there are none
     */
    public static int[] getAlarmIDs(List<Alarm> alarms) {
        if (null == alarms || 0 == alarms.size()) {
            return new int[0];
        }
        int[] alarmIds = new int[alarms.size()];
        for (int i = 0; i < alarmIds.length; i++) {
            alarmIds[i] = alarms.get(i).getId();
        }
        return alarmIds;
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
     * @return A single search term
     */
    public static <E extends Enum<?>> SingleSearchTerm getSearchTerm(E field, SingleOperation operation) {
        return new SingleSearchTerm(operation).addOperand(new ColumnFieldOperand<E>(field));
    }

    /**
     * Extracts all event conflicts from the problematic attributes of the supplied conflict exception
     * ({@link CalendarExceptionCodes#EVENT_CONFLICTS} or {@link CalendarExceptionCodes#HARD_EVENT_CONFLICTS}).
     *
     * @param conflictException The conflict exception
     * @return The extracted event conflicts, or an empty list if there are none
     */
    public static List<EventConflict> extractEventConflicts(OXException conflictException) {
        if (null != conflictException) {
            ProblematicAttribute[] problematics = conflictException.getProblematics();
            if (null != problematics && 0 < problematics.length) {
                List<EventConflict> eventConflicts = new ArrayList<EventConflict>(problematics.length);
                for (ProblematicAttribute problematic : problematics) {
                    if (EventConflict.class.isInstance(problematic)) {
                        eventConflicts.add((EventConflict) problematic);
                    }
                }
                return eventConflicts;
            }
        }
        return Collections.emptyList();
    }

    /**
     * Optionally gets the Java timezone for a given identifier.
     *
     * @param id The timezone identifier
     * @return The matching Java timezone, or <code>null</code> if not found
     */
    public static TimeZone optTimeZone(String id) {
        return optTimeZone(id, null);
    }

    /**
     * Optionally gets the Java timezone for a given identifier.
     *
     * @param id The timezone identifier
     * @param fallback The fallback timezone to return if no matching timezone was found
     * @return The matching Java timezone, or the fallback if not found
     */
    public static TimeZone optTimeZone(String id, TimeZone fallback) {
        if (null == id) {
            return fallback;
        }
        TimeZone timeZone = KNOWN_TIMEZONES.get(id);
        if (null == timeZone) {
            TimeZone timeZoneForId = TimeZone.getTimeZone(id);
            if ("GMT".equals(timeZoneForId.getID()) && false == "GMT".equalsIgnoreCase(id)) {
                return fallback;
            }
            timeZone = KNOWN_TIMEZONES.putIfAbsent(id, timeZoneForId);
            if (null == timeZone) {
                timeZone = timeZoneForId;
            }
        }
        return timeZone;
    }

    /**
     * Extracts an e-mail address from the supplied URI string. Decoding of sequences of escaped octets is performed implicitly, which
     * includes decoding of percent-encoded scheme-specific parts. Additionally, any ASCII-encoded parts of the address string are decoded
     * back to their unicode representation.
     * <p/>
     * Examples:<br/>
     * <ul>
     * <li>For input string <code>horst@xn--mller-kva.de</code>, the mail address <code>horst@m&uuml;ller.de</code> is extracted</li>
     * <li>For input string <code>mailto:horst@m%C3%BCller.de</code>, the mail address <code>horst@m&uuml;ller</code> is extracted</li>
     * </ul>
     *
     * @param value The URI address string to extract the e-mail address from
     * @return The extracted e-mail address, or the value as-is if no further extraction/decoding was possible or necessary
     */
    public static String extractEMailAddress(String value) {
        if (Strings.isEmpty(value)) {
            return value;
        }
        URI uri = null;
        try {
            uri = new URI(value);
        } catch (URISyntaxException e) {
            getLogger(CalendarUtils.class).debug("Error interpreting \"{}\" as URI, assuming \"mailto:\" protocol as fallback.", value, e);
            try {
                uri = new URI("mailto", value, null);
            } catch (URISyntaxException e2) {
                getLogger(CalendarUtils.class).debug("Error constructing \"mailto:\" URI for \"{}\", interpreting directly as fallback.", value, e2);
            }
        }
        /*
         * prefer scheme-specific part from "mailto:"-URI if possible
         */
        if (null != uri && "mailto".equalsIgnoreCase(uri.getScheme())) {
            value = uri.getSchemeSpecificPart();
        }
        /*
         * decode any punycoded names, too
         */
        return IDNA.toIDN(value);
    }

    /**
     * Gets a string representation of the <code>mailto</code>-URI for the supplied e-mail address.
     * <p/>
     * Non-ASCII characters are encoded implicitly as per {@link URI#toASCIIString()}.
     *
     * @param emailAddress The e-mail address to get the URI for
     * @return The <code>mailto</code>-URI, or <code>null</code> if no address was passed
     * @see{@link URI#toASCIIString()}
     */
    public static String getURI(String emailAddress) {
        if (Strings.isNotEmpty(emailAddress)) {
            try {
                return new URI("mailto", CalendarUtils.extractEMailAddress(emailAddress), null).toASCIIString();
            } catch (URISyntaxException e) {
                getLogger(CalendarUtils.class).debug("Error constructing \"mailto:\" URI for \"{}\", passign value as-is as fallback.", emailAddress, e);
            }
        }
        return emailAddress;
    }

    /**
     * Gets a value indicating whether an event series lies in the past or not, i.e. the end-time of its last occurrence is before the
     * <i>current</i> time.
     * <p/>
     * Therefore, the recurrence rule's <code>UNTIL</code>- and <code>COUNT</code>-parameters are evaluated accordingly; for
     * <i>never-ending</i> event series, this method always returns <code>false</code>;
     *
     * @param recurrenceService A reference to the recurrence service
     * @param seriesMaster The series master event to check
     * @param now The date to consider as <i>now</i> in the comparison
     * @param timeZone The timezone to consider if the event has <i>floating</i> dates
     * @return <code>true</code> if the event series is in the past, <code>false</code>, otherwise
     */
    public static boolean isInPast(RecurrenceService recurrenceService, Event seriesMaster, Date now, TimeZone timeZone) throws OXException {
        RecurrenceData recurrenceData = new DefaultRecurrenceData(seriesMaster.getRecurrenceRule(), seriesMaster.getStartDate(), null);
        return false == recurrenceService.iterateRecurrenceIds(recurrenceData, now, null).hasNext();
    }

    /**
     * Gets a specific occurrence from an event series by iterating the recurrence set.
     *
     * @param recurrenceService A reference to the recurrence service
     * @param seriesMaster The series master event to get the occurrence from
     * @param recurrenceId The recurrence identifier to get the occurrence for
     * @return The event occurrence, or <code>null</code> if not found in the recurrence set
     */
    public static Event getOccurrence(RecurrenceService recurrenceService, Event seriesMaster, RecurrenceId recurrenceId) throws OXException {
        long recurrenceTimestamp = recurrenceId.getValue().getTimestamp();
        RecurrenceIterator<Event> iterator = recurrenceService.iterateEventOccurrences(seriesMaster, new Date(recurrenceTimestamp), null);
        while (iterator.hasNext()) {
            Event occurrence = iterator.next();
            if (recurrenceId.equals(occurrence.getRecurrenceId())) {
                return occurrence;
            }
            if (occurrence.getRecurrenceId().getValue().getTimestamp() > recurrenceTimestamp) {
                break;
            }
        }
        return null;
    }

    /**
     * Combines multiple collections of recurrence identifiers within a sorted set.
     *
     * @param recurrenceIds1 The first collection of recurrence identifiers, or <code>null</code> if not defined
     * @param recurrenceIds2 The second collection of recurrence identifiers, or <code>null</code> if not defined
     * @return A sorted set of all recurrence identifiers found in the supplied collections
     */
    public static SortedSet<RecurrenceId> combine(Collection<RecurrenceId> recurrenceIds1, Collection<RecurrenceId> recurrenceIds2) {
        SortedSet<RecurrenceId> recurrenceIds = new TreeSet<RecurrenceId>(RecurrenceIdComparator.DEFAULT_COMPARATOR);
        if (null != recurrenceIds1) {
            recurrenceIds.addAll(recurrenceIds1);
        }
        if (null != recurrenceIds2) {
            recurrenceIds.addAll(recurrenceIds2);
        }
        return recurrenceIds;
    }

    /**
     * Maps a list of events based on their UID property, so that each event series including any change exceptions are grouped separately.
     *
     * @param events The events to map
     * @param assignIfEmpty <code>true</code> to assign a new unique identifier in case it's missing from an event, <code>false</code>, otherwise
     * @return The events, mapped by their unique identifier
     */
    public static Map<String, List<Event>> getEventsByUID(List<Event> events, boolean assignIfEmpty) {
        if (null == events) {
            return Collections.emptyMap();
        }
        Map<String, List<Event>> eventsByUID = new LinkedHashMap<String, List<Event>>();
        for (Event event : events) {
            String uid = event.getUid();
            if (null == uid && assignIfEmpty) {
                uid = UUID.randomUUID().toString();
                event.setUid(uid);
            }
            List<Event> list = eventsByUID.get(uid);
            if (null == list) {
                list = new ArrayList<Event>();
                eventsByUID.put(uid, list);
            }
            list.add(event);
        }
        return eventsByUID;
    }

    /**
     * Sorts a list of events and change exceptions so that the <i>series master</i> event will be the first element in the list, and any
     * change exceptions are sorted afterwards based on their recurrence identifier.
     *
     * @param events The events to sort
     * @return The sorted events
     */
    public static List<Event> sortSeriesMasterFirst(List<Event> events) {
        if (null != events && 1 < events.size()) {
            Collections.sort(events, new Comparator<Event>() {

                @Override
                public int compare(Event event1, Event event2) {
                    RecurrenceId recurrenceId1 = event1.getRecurrenceId();
                    RecurrenceId recurrenceId2 = event2.getRecurrenceId();
                    if (null == recurrenceId1) {
                        return null == recurrenceId2 ? 0 : -1;
                    }
                    if (null == recurrenceId2) {
                        return 1;
                    }
                    return recurrenceId1.compareTo(recurrenceId2);
                }
            });
        }
        return events;
    }

    /**
     * Gets a value indicating whether a specific event can be considered as <i>opaque</i> during free/busy lookups or conflict checks,
     * i.e. it is either explicitly marked different from {@link Transp#TRANSPARENT}, or its transparency is set to <code>null</code>.
     *
     * @param event The event to check
     * @return <code>true</code> if the event is considered as <i>public</i>, <code>false</code>, otherwise
     */
    public static boolean isOpaqueTransparency(Event event) {
        return null == event.getTransp() || false == Transp.TRANSPARENT.equals(event.getTransp().getValue());
    }

    /**
     * Gets a value indicating whether a specific event can be considered as <i>public</i> or not, i.e. it is either explicitly marked
     * with the default value of {@link Classification#PUBLIC}, or its classification is set to <code>null</code>.
     *
     * @param event The event to check
     * @return <code>true</code> if the event is considered as <i>public</i>, <code>false</code>, otherwise
     */
    public static boolean isPublicClassification(Event event) {
        return null == event.getClassification() || Classification.PUBLIC.equals(event.getClassification());
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
        if (isPublicClassification(event)) {
            return false;
        }
        if (matches(event.getCreatedBy(), userID) || matches(event.getCalendarUser(), userID) || contains(event.getAttendees(), userID)) {
            return false;
        }
        return true;
    }

    /**
     * Gets a value indicating whether an event is a so-called <i>group-scheduled</i> event or not.
     * <p/>
     * Group-scheduled events are <i>meetings</i> with a defined organizer and one or more attendees, while not group-scheduled ones are
     * simple <i>published</i> events, or events in a single user's calendar only.
     *
     * @param event The event to check
     * @return <code>true</code> if the event is group-scheduled, <code>false</code>, otherwise
     * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.8.4.1">RFC 5545, section 3.8.4.1</a>
     *      and
     *      <a href="https://tools.ietf.org/html/rfc5545#section-3.8.4.3">RFC 5545, section 3.8.4.3</a>
     */
    public static boolean isGroupScheduled(Event event) {
        return null != event.getOrganizer() && null != event.getAttendees() && 0 < event.getAttendees().size();
    }

    /**
     * Gets a value indicating whether an event is a so-called <i>group-scheduled</i> event, but the only attendee is the organizer itself.
     * <p/>
     * While pseudo-group-scheduled events are formally <i>group-scheduled</i> events, they should rather not be treated as such, since no
     * actual scheduling is required here. The legacy calendar implementation used to assign an organizer and attendee even for personal
     * events in a single user's calendar only.
     *
     * @param event The event to check
     * @return <code>true</code> if the event is pseudo-group-scheduled, <code>false</code>, otherwise
     * @see CalendarUtils#isGroupScheduled
     */
    public static boolean isPseudoGroupScheduled(Event event) {
        return isGroupScheduled(event) && 1 == event.getAttendees().size() && matches(event.getOrganizer(), event.getAttendees().get(0));
    }

    /**
     * Removes the implicitly added attendee for pseudo-<i>group-scheduled</i> events, along with any organizer information, in case there
     * are no additional attendees present in the event.
     * <p/>
     * This effectively makes the event to not appear as <i>meeting</i> in clients, as well as allowing modifications on it.
     *
     * @param event The event to adjust
     * @return <code>true</code> if the event was pseudo-group-scheduled and the attendee and organizer were removed, <code>false</code> otherwise
     */
    public static boolean removeImplicitAttendee(Event event) {
        if (isPseudoGroupScheduled(event)) {
            event.removeAttendees();
            event.removeOrganizer();
            return true;
        }
        return false;
    }

    /**
     * Gets a value indicating whether an event represents an <i>attendee scheduling object resource</i> or not, i.e. a group-scheduled
     * event the calendar user attends, organized by a different entity.
     *
     * @param event The event to check
     * @param calendarUserId The identifier of the calendar user representing the view on the event
     * @return <code>true</code> if the event is an attendee scheduling object resource, <code>false</code>, otherwise
     * @see <a href="https://tools.ietf.org/html/rfc6638#section-3.1">RFC 6638, section 3.1</a>
     */
    public static boolean isAttendeeSchedulingResource(Event event, int calendarUserId) {
        if (isGroupScheduled(event)) {
            Attendee matchingAttendee = find(event.getAttendees(), calendarUserId);
            return null != matchingAttendee && false == matches(matchingAttendee, event.getOrganizer());
        }
        return false;
    }

    /**
     * Gets a value indicating whether an event represents an <i>organizer scheduling object resource</i> or not, i.e. a group-scheduled
     * event where the calendar user matches the organizer.
     *
     * @param event The event to check
     * @param calendarUserId The identifier of the calendar user representing the view on the event
     * @return <code>true</code> if the event is an organizer scheduling object resource, <code>false</code>, otherwise
     * @see <a href="https://tools.ietf.org/html/rfc6638#section-3.1">RFC 6638, section 3.1</a>
     */
    public static boolean isOrganizerSchedulingResource(Event event, int calendarUserId) {
        return isGroupScheduled(event) && null != event.getOrganizer() && event.getOrganizer().getEntity() == calendarUserId;
    }

    /**
     * Calculates the effective start date of a specific occurrence of an event series.
     *
     * @param seriesMaster The series master event
     * @param recurrenceId The recurrence identifier of the occurrence
     * @return The start date of the occurrence
     */
    public static DateTime calculateStart(Event seriesMaster, RecurrenceId recurrenceId) {
        //TODO check
        //        return recurrenceId.getValue();
        if (seriesMaster.getStartDate().isAllDay()) {
            return new DateTime(recurrenceId.getValue().getTimestamp()).toAllDay();
        }
        return new DateTime(seriesMaster.getStartDate().getTimeZone(), recurrenceId.getValue().getTimestamp());
    }

    /**
     * Calculates the effective end date of a specific occurrence of an event series.
     *
     * @param seriesMaster The series master event
     * @param recurrenceId The recurrence identifier of the occurrence
     * @return The end date of the occurrence
     */
    public static DateTime calculateEnd(Event seriesMaster, RecurrenceId recurrenceId) {
        //TODO check
        long startMillis = seriesMaster.getStartDate().getTimestamp();
        long endMillis = seriesMaster.getEndDate().getTimestamp();
        long duration = endMillis - startMillis;
        long occurrenceEnd = recurrenceId.getValue().getTimestamp() + duration;
        if (seriesMaster.getEndDate().isAllDay()) {
            return new DateTime(occurrenceEnd).toAllDay();
        }
        return new DateTime(seriesMaster.getEndDate().getTimeZone(), occurrenceEnd);
    }

    /**
     * Adds an extended property to an event, initializing the event's extended properties collection as needed.
     *
     * @param event The event to add the property to
     * @param extendedProperty The extended property to add
     */
    public static void addExtendedProperty(Event event, ExtendedProperty extendedProperty) {
        ExtendedProperties extendedProperties = event.getExtendedProperties();
        if (null == extendedProperties) {
            extendedProperties = new ExtendedProperties();
            event.setExtendedProperties(extendedProperties);
        }
        extendedProperties.add(extendedProperty);
    }

    /**
     * Adds an extended property to an extended properties container, optionally initializing a new one automatically.
     *
     * @param extendedProperties The extended properties container to add the property to, or <code>null</code> to initialize a new one
     * @param extendedProperty The extended property to add
     * @param removeExisting <code>true</code> to remove any existing extended property with the same name, <code>false</code>, otherwise
     * @return The adjusted extended properties container, or a new extended properties instance as needed
     */
    public static ExtendedProperties addExtendedProperty(ExtendedProperties extendedProperties, ExtendedProperty extendedProperty, boolean removeExisting) {
        if (null == extendedProperties) {
            extendedProperties = new ExtendedProperties();
        } else if (removeExisting) {
            extendedProperties.removeAll(extendedProperty.getName());
        }
        extendedProperties.add(extendedProperty);
        return extendedProperties;
    }

    /**
     * Optionally gets (the first) extended property of a specific name of a calendar.
     *
     * @param calendar The calendar to get the extended property from
     * @param name The name of the extended property to get
     * @return The extended property, or <code>null</code> if not set
     */
    public static ExtendedProperty optExtendedProperty(com.openexchange.chronos.Calendar calendar, String name) {
        return optExtendedProperty(calendar.getExtendedProperties(), name);
    }

    /**
     * Optionally gets (the first) extended property of a specific name of an event.
     *
     * @param event The event to get the extended property from
     * @param name The name of the extended property to get
     * @return The extended property, or <code>null</code> if not set
     */
    public static ExtendedProperty optExtendedProperty(Event event, String name) {
        return optExtendedProperty(event.getExtendedProperties(), name);
    }

    /**
     * Gets all extended properties with a specific name. Wildcards are supported in the name, e.g. <code>X-MOZ-SNOOZE-TIME*</code>.
     * 
     * @param extendedProperties The extended properties to check
     * @param name The property name to match
     * @return All matching properties, or an empty list if there are none
     */
    public static List<ExtendedProperty> findExtendedProperties(ExtendedProperties extendedProperties, String name) {
        if (null == extendedProperties || extendedProperties.isEmpty()) {
            return Collections.emptyList();
        }
        if (-1 == name.indexOf('*') && -1 == name.indexOf('?')) {
            return extendedProperties.getAll(name);
        }
        List<ExtendedProperty> matchingProperties = new ArrayList<ExtendedProperty>();
        Pattern pattern = Pattern.compile(Strings.wildcardToRegex(name));
        for (ExtendedProperty property : extendedProperties) {
            if (null != property.getName() && pattern.matcher(property.getName()).matches()) {
                matchingProperties.add(property);
            }
        }
        return matchingProperties;
    }

    protected static ExtendedProperty optExtendedProperty(ExtendedProperties extendedProperties, String name) {
        return null != extendedProperties ? extendedProperties.get(name) : null;
    }

    /**
     * Initializes a new attachment collection update based on the supplied original and updated attachment lists.
     *
     * @param originalAttachments The original attachments
     * @param updatedAttachments The updated attachments
     * @return The collection update
     */
    public static SimpleCollectionUpdate<Attachment> getAttachmentUpdates(List<Attachment> originalAttachments, List<Attachment> updatedAttachments) {
        return new AbstractSimpleCollectionUpdate<Attachment>(originalAttachments, updatedAttachments) {

            @Override
            protected boolean matches(Attachment item1, Attachment item2) {
                if (0 < item1.getManagedId() && 0 < item2.getManagedId()) {
                    return item1.getManagedId() == item2.getManagedId();
                }
                return false;
            }
        };
    }

    /**
     * Initializes a new exception date collection update based on the supplied original and updated exception date lists.
     *
     * @param originalDates The original dates
     * @param updatedDates The updated dates
     * @return The collection update
     */
    public static SimpleCollectionUpdate<RecurrenceId> getExceptionDateUpdates(Collection<RecurrenceId> originalDates, Collection<RecurrenceId> updatedDates) {
        return new AbstractSimpleCollectionUpdate<RecurrenceId>(originalDates, updatedDates) {

            @Override
            protected boolean matches(RecurrenceId item1, RecurrenceId item2) {
                if (null != item1 && null != item2) {
                    return item1.equals(item2);
                }
                return false;
            }
        };
    }

    /**
     * Initializes a new attendee collection update based on the supplied original and updated attendee lists.
     *
     * @param originalAttendees The original attendees
     * @param updatedAttendees The updated attendees
     * @return The collection update
     */
    public static AbstractCollectionUpdate<Attendee, AttendeeField> getAttendeeUpdates(List<Attendee> originalAttendees, List<Attendee> updatedAttendees) throws OXException {
        return getAttendeeUpdates(originalAttendees, updatedAttendees, true, (AttendeeField[]) null);
    }

    /**
     * Initializes a new attendee collection update based on the supplied original and updated attendee lists.
     *
     * @param originalAttendees The original attendees
     * @param updatedAttendees The updated attendees
     * @param considerUnset <code>true</code> to also consider comparison with not <i>set</i> fields of the original, <code>false</code>, otherwise
     * @param ignoredFields Fields to ignore when determining the differences between updated items
     * @return The collection update
     * @throws OXException In case mapping fails
     */
    public static AbstractCollectionUpdate<Attendee, AttendeeField> getAttendeeUpdates(List<Attendee> originalAttendees, List<Attendee> updatedAttendees, boolean considerUnset, AttendeeField... ignoredFields) throws OXException {
        return new AbstractCollectionUpdate<Attendee, AttendeeField>(AttendeeMapper.getInstance(), originalAttendees, updatedAttendees, considerUnset, ignoredFields) {

            @Override
            protected boolean matches(Attendee item1, Attendee item2) {
                return CalendarUtils.matches(item1, item2);
            }
        };
    }

    /**
     * Initializes a new event updates collection based on the supplied original and updated event lists.
     * <p/>
     * Event matching is performed on one or more event fields.
     *
     * @param originalEvents The original events
     * @param updatedEvents The updated events
     * @param fieldsToMatch The event fields to consider when checking events for equality
     * @return The event updates
     * @see EventMapper#equalsByFields(Event, Event, EventField...)
     */
    public static EventUpdates getEventUpdates(List<Event> originalEvents, List<Event> updatedEvents, EventField... fieldsToMatch) throws OXException {
        return getEventUpdates(originalEvents, updatedEvents, true, (EventField[]) null, fieldsToMatch);
    }

    /**
     * Initializes a new event updates collection based on the supplied original and updated event lists.
     * <p/>
     * Event matching is performed on one or more event fields.
     *
     * @param originalEvents The original events
     * @param updatedEvents The updated events
     * @param fieldsToMatch The event fields to consider when checking events for equality
     * @param considerUnset <code>true</code> to also consider comparison with not <i>set</i> fields of the original, <code>false</code>, otherwise
     * @param ignoredFields Fields to ignore when determining the differences between updated items
     * @return The event updates
     * @see EventMapper#equalsByFields(Event, Event, EventField...)
     */
    public static EventUpdates getEventUpdates(List<Event> originalEvents, List<Event> updatedEvents, boolean considerUnset, EventField[] ignoredFields, final EventField... fieldsToMatch) throws OXException {
        return new AbstractEventUpdates(originalEvents, updatedEvents, considerUnset, ignoredFields) {

            @Override
            protected boolean matches(Event item1, Event item2) {
                try {
                    return EventMapper.getInstance().equalsByFields(item1, item2, fieldsToMatch);
                } catch (OXException e) {
                    throw new UnsupportedOperationException(e);
                }
            }
        };
    }

    /**
     * Creates a sorted set of the recurrence identifiers for a list of change exception events.
     *
     * @param changeExceptions The change exceptions to get the recurrence identifiers from
     * @return The recurrence identifiers, or an empty set if there are none
     */
    public static SortedSet<RecurrenceId> getRecurrenceIds(Collection<Event> changeExceptions) {
        SortedSet<RecurrenceId> recurrenceIds = new TreeSet<RecurrenceId>();
        if (null != changeExceptions) {
            for (Event changeException : changeExceptions) {
                RecurrenceId recurrenceId = changeException.getRecurrenceId();
                if (null != recurrenceId) {
                    recurrenceIds.add(recurrenceId);
                }
            }
        }
        return recurrenceIds;
    }

    /**
     * Creates an array of timestamps (milliseconds since epoch) of the supplied sorted set of recurrence identifiers, ready to be used
     * as exception dates in recurrence data.
     *
     * @param recurrenceIds The recurrence identifiers to get the timestamps for
     * @return The timestamps in a sorted array of <code>long</code>s, or <code>null</code> if no recurrence identifiers are passed
     * @see RecurrenceData#getExceptionDates
     */
    public static long[] getExceptionDates(SortedSet<RecurrenceId> recurrenceIds) {
        if (null == recurrenceIds || recurrenceIds.isEmpty()) {
            return null;
        }
        long[] exceptionDates = new long[recurrenceIds.size()];
        int position = 0;
        for (RecurrenceId recurrenceId : recurrenceIds) {
            exceptionDates[position++] = recurrenceId.getValue().getTimestamp();
        }
        return exceptionDates;
    }

    /**
     * Splits a set of exception dates at a certain split point, resulting in one set with exception dates <i>prior</i> the split point,
     * and another one with exception dates <i>on or after</i> this split point.
     *
     * @param exceptionDates The set of exception dates to split
     * @param splitPoint The split point
     * @return A map entry, where the key holds the exception dates prior the split point, and the value the dates on or after it
     */
    public static Entry<SortedSet<RecurrenceId>, SortedSet<RecurrenceId>> splitExceptionDates(SortedSet<RecurrenceId> exceptionDates, DateTime splitPoint) {
        SortedSet<RecurrenceId> leftExceptionDates = new TreeSet<RecurrenceId>();
        SortedSet<RecurrenceId> rightExceptionDates = new TreeSet<RecurrenceId>();
        if (null != exceptionDates && 0 < exceptionDates.size()) {
            for (RecurrenceId exceptionDate : exceptionDates) {
                if (0 > compare(exceptionDate.getValue(), splitPoint, null)) {
                    leftExceptionDates.add(exceptionDate);
                } else {
                    rightExceptionDates.add(exceptionDate);
                }
            }
        }
        return new AbstractMap.SimpleEntry<SortedSet<RecurrenceId>, SortedSet<RecurrenceId>>(leftExceptionDates, rightExceptionDates);
    }

    /**
     * Gets the full identifier for the supplied event, based on the property values for {@link EventField#FOLDER_ID},
     * {@link EventField#ID} and {@link EventField#RECURRENCE_ID}.
     *
     * @param event The event to get the full identifier for
     * @return The full identifier for the event
     */
    public static EventID getEventID(Event event) {
        return new EventID(event.getFolderId(), event.getId(), event.getRecurrenceId());
    }

    /**
     * Gets the maximum timestamp in a list of timestamped results.
     *
     * @param results The results to determine the maximum timestamp for
     * @return The maximum timestamp, or <code>0</code> if the supplied list is <code>null</code> or empty
     */
    public static long getMaximumTimestamp(List<? extends TimestampedResult> results) {
        long timestamp = 0L;
        if (null != results) {
            for (TimestampedResult result : results) {
                timestamp = Math.max(timestamp, result.getTimestamp());
            }
        }
        return timestamp;
    }

    /**
     * Sorts a list of events.
     *
     * @param events The events to sort
     * @param sortOrders The sort orders to use
     * @param timeZone The timezone to consider for comparing <i>floating</i> date properties, i.e. the actual 'perspective' of the
     *            comparison, or <code>null</code> to fall back to UTC
     * @return The sorted events
     */
    public static List<Event> sortEvents(List<Event> events, final SortOrder[] sortOrders, final TimeZone timeZone) {
        if (null == events || 2 > events.size() || null == sortOrders || 0 == sortOrders.length) {
            return events;
        }
        Collections.sort(events, new Comparator<Event>() {

            @Override
            public int compare(Event event1, Event event2) {
                if (null == event1) {
                    return null == event2 ? 0 : -1;
                }
                if (null == event2) {
                    return 1;
                }
                int comparison = 0;
                for (SortOrder sortOrder : sortOrders) {
                    Mapping<? extends Object, Event> mapping = EventMapper.getInstance().opt(sortOrder.getBy());
                    if (null == mapping) {
                        org.slf4j.LoggerFactory.getLogger(CalendarUtils.class).warn("Can't compare by {} due to missing mapping", sortOrder.getBy());
                        continue;
                    }
                    comparison = mapping.compare(event1, event2, null, timeZone);
                    if (0 != comparison) {
                        return sortOrder.isDescending() ? -1 * comparison : comparison;
                    }
                }
                return comparison;
            }
        });
        return events;
    }

    /**
     * Gets the identifier of the folder representing a specific calendar user's view on an event. For events in <i>public</i> folders or
     * not <i>group-scheduled</i> events, this is always the common folder identifier of the event as per {@link Event#getFolderId}.
     * Otherwise, the corresponding attendee's parent folder identifier is returned.
     *
     * @param event The event to get the folder view for
     * @param calendarUser The identifier of the calendar user to get the folder view for
     * @return The folder identifier
     * @throws OXException - {@link CalendarExceptionCodes#ATTENDEE_NOT_FOUND} in case there's no static parent folder and the supplied user is no attendee
     */
    public static String getFolderView(Event event, int calendarUser) throws OXException {
        if (null != event.getFolderId()) {
            return event.getFolderId();
        }
        Attendee userAttendee = find(event.getAttendees(), calendarUser);
        if (null == userAttendee || null == userAttendee.getFolderId()) {
            throw CalendarExceptionCodes.ATTENDEE_NOT_FOUND.create(I(calendarUser), event.getId());
        }
        return userAttendee.getFolderId();
    }

    /**
     * Gets the event fields to include when querying events from the storage based on the client-requested fields defined in the
     * supplied calendar parameters.
     * <p/>
     * Specific {@link #DEFAULT_FIELDS} are included implicitly, further required ones may be defined explicitly, too.
     *
     * @param parameters The calendar parameters to get the requested fields from
     * @param requiredFields Additionally required fields to add, or <code>null</code> if not defined
     * @return The fields to use when querying events from the storage
     * @see CalendarParameters#PARAMETER_FIELDS
     * @see #DEFAULT_FIELDS
     */
    public static EventField[] getFields(CalendarParameters parameters, EventField... requiredFields) {
        return getFields(parameters.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class), requiredFields);
    }

    /**
     * Gets the event fields to include when querying events from the storage based on the supplied client-requested fields.
     * <p/>
     * Specific {@link #DEFAULT_FIELDS} are included implicitly, further required ones may be defined explicitly, too. If the special
     * field {@link EventField#FLAGS} is requested, further fields (as listed in {@link #FLAG_FIELDS}) are also added to be able to
     * derive the actual flags afterwards.
     *
     * @param requestedFields The fields requested by the client, or <code>null</code> to retrieve all fields
     * @param requiredFields Additionally required fields to add, or <code>null</code> if not defined
     * @return The fields to use when querying events from the storage
     * @see #DEFAULT_FIELDS
     * @see #FLAG_FIELDS
     */
    public static EventField[] getFields(EventField[] requestedFields, EventField... requiredFields) {
        if (null == requestedFields) {
            return EventField.values();
        }
        Set<EventField> fields = new HashSet<EventField>();
        fields.addAll(DEFAULT_FIELDS);
        fields.addAll(Arrays.asList(requestedFields));
        if (null != requiredFields && 0 < requiredFields.length) {
            fields.addAll(Arrays.asList(requiredFields));
        }
        if (fields.contains(EventField.FLAGS)) {
            fields.addAll(FLAG_FIELDS);
        }
        return fields.toArray(new EventField[fields.size()]);
    }

    /**
     * Gets a value indicating whether the supplied array of event fields contains no other than identifying meta fields, in which case
     * some self-protection checks can possibly be relaxed to allow larger result lists in responses to clients.
     *
     * @param requestedFields The requested fields, or <code>null</code> if all event fields were requested
     * @return <code>true</code> if only identifying meta fields were queried, <code>false</code>, otherwise
     * @see #IDENTIFYING_FIELDS
     */
    public static boolean containsOnlyIdentifyingFields(EventField[] requestedFields) {
        if (null == requestedFields) {
            return false;
        }
        for (EventField eventField : requestedFields) {
            if (false == IDENTIFYING_FIELDS.contains(eventField)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Prepends the default account to the given folder id if this is a plain numeric folder id.
     *
     * @param folderId The folder identifier
     * @return The folder id with the default account as absolute identifier.
     */
    public static String prependDefaultAccount(String folderId) {
        try {
            Integer.parseInt(folderId);
        } catch (NumberFormatException nfe) {
            return folderId;
        }
        return DEFAULT_ACCOUNT_PREFIX + folderId;
    }

    /**
     * Generates the flags for a specific event (see {@link EventFlag}).
     *
     * @param event The event to get the flags for
     * @param calendarUser The identifier of the calendar user to get flags for
     * @return The event flags
     */
    public static EnumSet<EventFlag> getFlags(Event event, int calendarUser) {
        return getFlags(event, calendarUser, calendarUser);
    }

    /**
     * Generates the flags for a specific event (see {@link EventFlag}).
     *
     * @param event The event to get the flags for
     * @param calendarUser The identifier of the calendar user to get flags for
     * @param user The identifier of the current user, in case he is different from the calendar user
     * @return The event flags
     */
    public static EnumSet<EventFlag> getFlags(Event event, int calendarUser, int user) {
        EnumSet<EventFlag> flags = EnumSet.noneOf(EventFlag.class);
        if (null != event.getAttachments() && 0 < event.getAttachments().size()) {
            flags.add(EventFlag.ATTACHMENTS);
        }
        if (null != event.getAlarms() && 0 < event.getAlarms().size()) {
            flags.add(EventFlag.ALARMS);
        }
        if (isGroupScheduled(event) && false == isPseudoGroupScheduled(event)) {
            flags.add(EventFlag.SCHEDULED);
        }
        if (isOrganizerSchedulingResource(event, calendarUser)) {
            flags.add(EventFlag.ORGANIZER);
            flags.add(EventFlag.ATTENDEE);
        } else if (isAttendeeSchedulingResource(event, calendarUser)) {
            flags.add(EventFlag.ATTENDEE);
        }
        if (calendarUser != user) {
            flags.add(EventFlag.ON_BEHALF);
        }
        if (Classification.CONFIDENTIAL.equals(event.getClassification())) {
            flags.add(EventFlag.CONFIDENTIAL);
        } else if (Classification.PRIVATE.equals(event.getClassification())) {
            flags.add(EventFlag.PRIVATE);
        }
        if (false == isOpaqueTransparency(event)) {
            flags.add(EventFlag.TRANSPARENT);
        }
        if (EventStatus.CONFIRMED.equals(event.getStatus())) {
            flags.add(EventFlag.EVENT_CONFIRMED);
        } else if (EventStatus.CANCELLED.equals(event.getStatus())) {
            flags.add(EventFlag.EVENT_CANCELLED);
        } else if (EventStatus.TENTATIVE.equals(event.getStatus())) {
            flags.add(EventFlag.EVENT_TENTATIVE);
        }
        Attendee attendee = find(event.getAttendees(), calendarUser);
        if (null != attendee) {
            if (ParticipationStatus.ACCEPTED.equals(attendee.getPartStat())) {
                flags.add(EventFlag.ACCEPTED);
            } else if (ParticipationStatus.DECLINED.equals(attendee.getPartStat())) {
                flags.add(EventFlag.DECLINED);
            } else if (ParticipationStatus.DELEGATED.equals(attendee.getPartStat())) {
                flags.add(EventFlag.DELEGATED);
            } else if (ParticipationStatus.NEEDS_ACTION.equals(attendee.getPartStat())) {
                flags.add(EventFlag.NEEDS_ACTION);
            } else if (ParticipationStatus.TENTATIVE.equals(attendee.getPartStat())) {
                flags.add(EventFlag.TENTATIVE);
            }
        }
        if (isSeriesMaster(event)) {
            flags.add(EventFlag.SERIES);
        } else if (isSeriesException(event)) {
            flags.add(EventFlag.OVERRIDDEN);
        }
        return flags;
    }

}