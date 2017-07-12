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

import static com.openexchange.chronos.common.CalendarUtils.getObjectIDs;
import static com.openexchange.chronos.common.CalendarUtils.isClassifiedFor;
import static com.openexchange.chronos.common.CalendarUtils.isGroupScheduled;
import static com.openexchange.chronos.common.CalendarUtils.isInRange;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.arrays.Arrays.contains;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarStrings;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarConfig;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.SimpleCollectionUpdate;
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
import com.openexchange.i18n.tools.StringHelper;
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
        EventField.ID, EventField.SERIES_ID, EventField.FOLDER_ID, EventField.LAST_MODIFIED, EventField.CREATED_BY,
        EventField.CALENDAR_USER, EventField.CLASSIFICATION, EventField.START_DATE, EventField.END_DATE, EventField.RECURRENCE_RULE,
        EventField.CHANGE_EXCEPTION_DATES, EventField.DELETE_EXCEPTION_DATES, EventField.ORGANIZER
    );

    /** The event fields that are also available if an event's classification is not {@link Classification#PUBLIC} */
    public static final EventField[] NON_CLASSIFIED_FIELDS = {
        EventField.CHANGE_EXCEPTION_DATES, EventField.CLASSIFICATION, EventField.CREATED, EventField.CREATED_BY,
        EventField.CALENDAR_USER, EventField.DELETE_EXCEPTION_DATES, EventField.END_DATE, EventField.ID,
        EventField.LAST_MODIFIED, EventField.MODIFIED_BY, EventField.FOLDER_ID, EventField.SERIES_ID,
        EventField.RECURRENCE_RULE, EventField.SEQUENCE, EventField.START_DATE, EventField.TRANSP,
        EventField.UID, EventField.FILENAME
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
     * Gets a value indicating whether the current calendar user should be added as default attendee to events implicitly or not,
     * independently of the event being group-scheduled or not, based on the value of {@link CalendarParameters#PARAMETER_DEFAULT_ATTENDEE}
     * in the supplied parameters.
     * <p/>
     * If the <i>legacy</i> storage is in use, the default attendee is enforced statically.
     *
     * @param session The calendar session to evaluate
     * @return <code>true</code> the current calendar user should be added as default attendee to events implicitly, <code>false</code>, otherwise
     * @see CalendarParameters#PARAMETER_DEFAULT_ATTENDEE
     * @see CalendarConfig#isUseLegacyStorage
     * @see CalendarConfig#isReplayToLegacyStorage
     */
    public static boolean isEnforceDefaultAttendee(CalendarSession session) {
        return 1 == 1 || session.getConfig().isUseLegacyStorage() || session.getConfig().isReplayToLegacyStorage() ||
            session.get(CalendarParameters.PARAMETER_DEFAULT_ATTENDEE, Boolean.class, Boolean.FALSE).booleanValue();
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
    public static TimeZone getTimeZone(CalendarSession session) throws OXException {
        TimeZone timeZone = session.get(CalendarParameters.PARAMETER_TIMEZONE, TimeZone.class);
        return null != timeZone ? timeZone : session.getEntityResolver().getTimeZone(session.getUserId());
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
     * the {@link EventField#FOLDER_ID} and/or for the {@link AttendeeField#FOLDER_ID} is built.
     * <p/>
     * The session user's read permissions in the folder ("own" vs "all") are considered automatically, too, by restricting via
     * {@link EventField#CREATED_BY} if needed.
     *
     * @param folder The folder to construct the search term for
     * @return The search term
     */
    public static SearchTerm<?> getFolderIdTerm(UserizedFolder folder) {
        /*
         * match the event's common folder identifier
         */
        SearchTerm<?> searchTerm = getSearchTerm(EventField.FOLDER_ID, SingleOperation.EQUALS, folder.getID());
        if (false == PublicType.getInstance().equals(folder.getType())) {
            /*
             * for personal folders, also match against the corresponding attendee's folder
             */
            searchTerm = new CompositeSearchTerm(CompositeOperation.OR)
                .addSearchTerm(searchTerm)
                .addSearchTerm(new CompositeSearchTerm(CompositeOperation.AND)
                    .addSearchTerm(getSearchTerm(AttendeeField.ENTITY, SingleOperation.EQUALS, I(folder.getCreatedBy())))
                    .addSearchTerm(getSearchTerm(AttendeeField.FOLDER_ID, SingleOperation.EQUALS, folder.getID())));
        }
        if (folder.getOwnPermission().getReadPermission() < Permission.READ_ALL_OBJECTS) {
            /*
             * if only access to "own" objects; restrict to events created by the current session's user
             */
            searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
                .addSearchTerm(searchTerm)
                .addSearchTerm(getSearchTerm(EventField.CREATED_BY, SingleOperation.EQUALS, folder.getSession().getUserId()));
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
     * <i>Anonymizes</i> an event in case it is not marked as {@link Classification#PUBLIC}, and the session's user is neither creator, nor
     * attendee of the event.
     * <p/>
     * After anonymization, the event will only contain those properties defined in {@link #NON_CLASSIFIED_FIELDS}, as well as the
     * generic summary "Private".
     *
     * @param session The calendar session
     * @param event The event to anonymize
     * @return The potentially anonymized event
     */
    public static Event anonymizeIfNeeded(CalendarSession session, Event event) throws OXException {
        if (false == isClassifiedFor(event, session.getUserId())) {
            return event;
        }
        Event anonymizedEvent = EventMapper.getInstance().copy(event, new Event(), NON_CLASSIFIED_FIELDS);
        anonymizedEvent.setSummary(StringHelper.valueOf(session.getEntityResolver().getLocale(session.getUserId())).getString(CalendarStrings.SUMMARY_PRIVATE));
        return anonymizedEvent;
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
     * @param includeClassified <code>true</code> to include <i>confidential</i> events in shared folders, <code>false</code>, otherwise
     * @return <code>true</code> if the event should be excluded, <code>false</code>, otherwise
     */
    public static boolean isExcluded(Event event, CalendarSession session, boolean includeClassified) throws OXException {
        /*
         * excluded if "classified" for user (and such events are requested to be excluded)
         */
        if (isClassifiedFor(event, session.getUserId())) {
            if (false == includeClassified || false == Classification.CONFIDENTIAL.equals(event.getClassification())) {
                // only include 'confidential' events if requested
                return true;
            }
        }
        Date from = getFrom(session);
        Date until = getUntil(session);
        if ((null != from || null != until) && null != event.getStartDate()) {
            if (isSeriesMaster(event)) {
                /*
                 * excluded if there are no actual occurrences in range
                 */
                Iterator<Event> iterator = session.getRecurrenceService().iterateEventOccurrences(event, from, until);
                return false == iterator.hasNext();
            } else {
                /*
                 * excluded if event period not in range
                 */
                TimeZone timeZone = getTimeZone(session);
                if (false == isInRange(event, from, until, timeZone)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets a value indicating whether events in foreign folders classified as {@link Classification#CONFIDENTIAL} are to be included in
     * the results or not. <p/>
     * <b>Note:</b>Events the marked as {@link Classification#PRIVATE} are always excluded in shared folders (in case the user is not
     * attending itself).
     *
     * @param parameters The calendar parameters to evaluate
     * @return <code>true</code> if classified events should be included, <code>false</code>, otherwise
     * @see CalendarParameters#PARAMETER_INCLUDE_PRIVATE
     */
    public static boolean isIncludeClassifiedEvents(CalendarParameters parameters) {
        return parameters.get(CalendarParameters.PARAMETER_INCLUDE_PRIVATE, Boolean.class, Boolean.FALSE).booleanValue();
    }

    /**
     * Gets the identifier of the folder representing a specific calendar user's view on an event. For events in <i>public</i> folders or
     * not <i>group-scheduled</i> events, this is always the common folder identifier of the event. Otherwise, the corresponding
     * attendee's parent folder identifier is returned.
     *
     * @param storage A reference to the calendar storage to query the event's attendees not set
     * @param event The event to get the folder view for
     * @param calendarUser The identifier of the user to get the folder view for
     * @return The folder identifier
     * @throws OXException - {@link CalendarExceptionCodes#ATTENDEE_NOT_FOUND} in case there's no static parent folder and the supplied user is no attendee
     */
    public static String getFolderView(CalendarStorage storage, Event event, int calendarUser) throws OXException {
        if (null != event.getFolderId() || false == isGroupScheduled(event)) {
            return event.getFolderId();
        } else {
            Attendee userAttendee = CalendarUtils.find(
                event.containsAttendees() ? event.getAttendees() : storage.getAttendeeStorage().loadAttendees(event.getId()), calendarUser);
            if (null == userAttendee || null == userAttendee.getFolderID()) {
                throw CalendarExceptionCodes.ATTENDEE_NOT_FOUND.create(I(calendarUser), event.getId());
            }
            return userAttendee.getFolderID();
        }
    }

    /**
     * Gets a user.
     *
     * @param session The calendar session
     * @param userId The identifier of the user to get
     * @return The user
     */
    public static User getUser(CalendarSession session, int userId) throws OXException {
        return Services.getService(UserService.class).getUser(userId, session.getContextId());
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
     * Gets the identifier of the actual target calendar user for a specific folder. This is either the current session's user for
     * "private" or "public" folders, or the folder owner for "shared" calendar folders.
     *
     * @param folder The folder to get the calendar user for
     * @return The identifier of the calendar user
     */
    public static int getCalendarUserId(UserizedFolder folder) throws OXException {
        if (SharedType.getInstance().equals(folder.getType())) {
            return folder.getCreatedBy();
        }
        return folder.getUser().getId();
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
     * Gets a value indicating whether a specific event is actually present in the supplied folder. Based on the folder type, the
     * event's public folder identifier or the attendee's personal calendar folder is checked.
     *
     * @param event The event to check
     * @param folder The folder where the event should appear in
     * @return <code>true</code> if the event <i>is</i> in the folder, <code>false</code>, otherwise
     */
    public static boolean isInFolder(Event event, UserizedFolder folder) throws OXException {
        if (PublicType.getInstance().equals(folder.getType()) || false == isGroupScheduled(event)) {
            return folder.getID().equals(event.getFolderId());
        } else {
            Attendee userAttendee = CalendarUtils.find(event.getAttendees(), folder.getCreatedBy());
            return null != userAttendee && folder.getID().equals(userAttendee.getFolderID());
        }
    }

    /**
     * Gets a <i>userized</i> folder by its identifier.
     *
     * @param session The calendar session
     * @param folderID The identifier of the folder to get
     * @return The folder
     */
    public static UserizedFolder getFolder(CalendarSession session, String folderID) throws OXException {
        try {
            return Services.getService(FolderService.class).getFolder(FolderStorage.REAL_TREE_ID, folderID, session.getSession(), initDecorator(session));
        } catch (OXException e) {
            if ("FLD-0003".equals(e.getErrorCode())) {
                // com.openexchange.tools.oxfolder.OXFolderExceptionCode.NOT_VISIBLE
                throw CalendarExceptionCodes.NO_READ_PERMISSION.create(e, folderID);
            }
            throw e;
        }
    }

    /**
     * Loads additional event data from the storage, based on the requested fields. This currently includes
     * <ul>
     * <li>{@link EventField#ATTENDEES}</li>
     * <li>{@link EventField#ATTACHMENTS}</li> (not for <i>tombstones</i>)
     * <li>{@link EventField#ALARMS}</li> (of the calendar user; not for <i>tombstones</i>)
     * </ul>
     *
     * @param storage A reference to the calendar storage to use
     * @param tombstones <code>true</code> if tombstone data is being read, <code>false</code>, otherwise
     * @param events The events to load additional data for
     * @param userID The identifier of the calendar user to load additional data for, or <code>-1</code> to not load user-sensitive data
     * @param fields The requested fields, or <code>null</code> to assume all fields are requested
     * @return The events, enriched by the additionally loaded data
     */
    public static List<Event> loadAdditionalEventData(CalendarStorage storage, boolean tombstones, int userID, List<Event> events, EventField[] fields) throws OXException {
        if (null == events || 0 == events.size()) {
            return events;
        }
        if (tombstones) {
            /*
             * only attendee data available for tombstone events
             */
            if (null == fields || contains(fields, EventField.ATTENDEES)) {
                Map<String, List<Attendee>> attendeesById = storage.getAttendeeStorage().loadAttendees(getObjectIDs(events));
                for (Event event : events) {
                    event.setAttendees(attendeesById.get(event.getId()));
                }
            }
        } else {
            /*
             * read attendees, attachments & alarms for non-tombstone events
             */
            if (null == fields || contains(fields, EventField.ATTENDEES) || contains(fields, EventField.ATTACHMENTS) || contains(fields, EventField.ALARMS)) {
                String[] objectIDs = getObjectIDs(events);
                if (null == fields || contains(fields, EventField.ATTENDEES)) {
                    Map<String, List<Attendee>> attendeesById = storage.getAttendeeStorage().loadAttendees(objectIDs);
                    for (Event event : events) {
                        event.setAttendees(attendeesById.get(event.getId()));
                    }
                }
                if (null == fields || contains(fields, EventField.ATTACHMENTS)) {
                    Map<String, List<Attachment>> attachmentsById = storage.getAttachmentStorage().loadAttachments(objectIDs);
                    for (Event event : events) {
                        event.setAttachments(attachmentsById.get(event.getId()));
                    }
                }
                if (0 < userID && (null == fields || contains(fields, EventField.ALARMS))) {
                    Map<String, List<Alarm>> alarmsById = storage.getAlarmStorage().loadAlarms(events, userID);
                    for (Event event : events) {
                        event.setAlarms(alarmsById.get(event.getId()));
                    }
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
            event.setAlarms(storage.getAlarmStorage().loadAlarms(event, userID));
        }
        return event;
    }

    /**
     * Applies <i>userized</i> versions of change- and delete-exception dates in the series master event based on the user's actual
     * attendance.
     *
     * @param storage A reference to the calendar storage to use
     * @param seriesMaster The series master event
     * @param forUser The identifier of the user to apply the exception dates for
     * @return The passed event reference, with possibly adjusted exception dates
     * @see <a href="https://tools.ietf.org/html/rfc6638#section-3.2.6">RFC 6638, section 3.2.6</a>
     */
    public static Event applyExceptionDates(CalendarStorage storage, Event seriesMaster, int forUser) throws OXException {
        if (false == isSeriesMaster(seriesMaster)) {
            return seriesMaster;
        }
        /*
         * lookup all known change exceptions
         */
        SortedSet<RecurrenceId> changeExceptionDates = seriesMaster.getChangeExceptionDates();
        if (null == changeExceptionDates || 0 == changeExceptionDates.size()) {
            return seriesMaster;
        }
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
            .addSearchTerm(getSearchTerm(EventField.SERIES_ID, SingleOperation.EQUALS, seriesMaster.getSeriesId()))
            .addSearchTerm(getSearchTerm(EventField.ID, SingleOperation.NOT_EQUALS, new ColumnFieldOperand<EventField>(EventField.SERIES_ID)))
        ;
        List<Event> changeExceptions = storage.getEventStorage().searchEvents(searchTerm, null, getFields((EventField[]) null));
        changeExceptions = loadAdditionalEventData(storage, false, forUser, changeExceptions, new EventField[] { EventField.ATTENDEES });
        /*
         * check which change exception the user attends
         */
        SortedSet<RecurrenceId> userizedChangeExceptions = new TreeSet<RecurrenceId>();
        SortedSet<RecurrenceId> userizedDeleteExceptions = new TreeSet<RecurrenceId>();
        if (null != seriesMaster.getDeleteExceptionDates()) {
            userizedDeleteExceptions.addAll(seriesMaster.getDeleteExceptionDates());
        }
        for (Event changeException : changeExceptions) {
            RecurrenceId exceptionDate = changeException.getRecurrenceId();
            if (CalendarUtils.contains(changeException.getAttendees(), forUser)) {
                userizedChangeExceptions.add(exceptionDate);
            } else {
                userizedDeleteExceptions.add(exceptionDate);
            }
        }
        /*
         * apply 'userized' versions of exception dates
         */
        seriesMaster.setChangeExceptionDates(userizedChangeExceptions);
        seriesMaster.setDeleteExceptionDates(userizedDeleteExceptions);
        return seriesMaster;
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

    /**
     * Gets all calendar folders accessible by the current sesssion's user.
     *
     * @param session The underlying calendar session
     * @return The folders, or an empty list if there are none
     */
    public static List<UserizedFolder> getVisibleFolders(CalendarSession session) throws OXException {
        return getVisibleFolders(session, PrivateType.getInstance(), SharedType.getInstance(), PublicType.getInstance());
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
                    return item1.getValue() == item2.getValue();
                }
                return false;
            }
        };
    }

    /**
     * Initializes a new alarm collection update based on the supplied original and updated alarm lists.
     *
     * @param originalAlarms The original alarms
     * @param updatedAlarms The updated alarms
     * @return The collection update
     */
    public static AbstractCollectionUpdate<Alarm, AlarmField> getAlarmUpdates(List<Alarm> originalAlarms, List<Alarm> updatedAlarms) throws OXException {
        /*
         * special handling to detect change of single reminder (as used in legacy storage)
         */
        if (null != originalAlarms && 1 == originalAlarms.size() && null != updatedAlarms && 1 == updatedAlarms.size()) {
            Alarm originalAlarm = originalAlarms.get(0);
            Alarm updatedAlarm = updatedAlarms.get(0);
            Set<AlarmField> differentFields = AlarmMapper.getInstance().getDifferentFields(
                originalAlarm, updatedAlarm, true, AlarmField.TRIGGER, AlarmField.UID, AlarmField.DESCRIPTION, AlarmField.EXTENDED_PROPERTIES);
            if (differentFields.isEmpty()) {
                return new AbstractCollectionUpdate<Alarm, AlarmField>(AlarmMapper.getInstance(), originalAlarms, updatedAlarms) {

                    @Override
                    protected boolean matches(Alarm alarm1, Alarm alarm2) {
                        return true;
                    }
                };
            }
        }
        /*
         * default collection update, otherwise
         */
        return new AbstractCollectionUpdate<Alarm, AlarmField>(AlarmMapper.getInstance(), originalAlarms, updatedAlarms) {

            @Override
            protected boolean matches(Alarm alarm1, Alarm alarm2) {
                if (null == alarm1) {
                    return null == alarm2;
                } else if (null != alarm2) {
                    if (0 < alarm1.getId() && alarm1.getId() == alarm2.getId()) {
                        return true;
                    }
                    if (null != alarm1.getUid() && alarm1.getUid().equals(alarm2.getUid())) {
                        return true;
                    }
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
        return new AbstractCollectionUpdate<Attendee, AttendeeField>(AttendeeMapper.getInstance(), originalAttendees, updatedAttendees) {

            @Override
            protected boolean matches(Attendee item1, Attendee item2) {
                return CalendarUtils.matches(item1, item2);
            }
        };
    }

    /**
     * Gets all calendar folders of certain types  accessible by the current sesssion's user.
     *
     * @param session The underlying calendar session
     * @param types The folder types to include
     * @return The folders, or an empty list if there are none
     */
    public static List<UserizedFolder> getVisibleFolders(CalendarSession session, Type... types) throws OXException {
        List<UserizedFolder> visibleFolders = new ArrayList<UserizedFolder>();
        FolderService folderService = Services.getService(FolderService.class);
        for (Type type : types) {
            FolderResponse<UserizedFolder[]> response = folderService.getVisibleFolders(
                FolderStorage.REAL_TREE_ID, CalendarContentType.getInstance(), type, false, session.getSession(), initDecorator(session));
            UserizedFolder[] folders = response.getResponse();
            if (null != folders && 0 < folders.length) {
                visibleFolders.addAll(Arrays.asList(folders));
            }
        }
        return visibleFolders;
    }

    private static FolderServiceDecorator initDecorator(CalendarSession session) throws OXException {
        FolderServiceDecorator decorator = new FolderServiceDecorator();
        Connection connection = session.get(AbstractStorageOperation.PARAM_CONNECTION, Connection.class, null);
        if (null != connection) {
            decorator.put(Connection.class.getName(), connection);
        }
        decorator.setLocale(session.getEntityResolver().getLocale(session.getUserId()));
        decorator.setTimeZone(Utils.getTimeZone(session));
        return decorator;
    }

}
