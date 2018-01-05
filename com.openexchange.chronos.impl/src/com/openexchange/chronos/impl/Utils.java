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

import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.getFolderView;
import static com.openexchange.chronos.common.CalendarUtils.isClassifiedFor;
import static com.openexchange.chronos.common.CalendarUtils.isGroupScheduled;
import static com.openexchange.chronos.common.CalendarUtils.isInRange;
import static com.openexchange.chronos.common.CalendarUtils.isLastUserAttendee;
import static com.openexchange.chronos.common.CalendarUtils.isOrganizer;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.impl.AbstractStorageOperation.PARAM_CONNECTION;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i2I;
import java.sql.Connection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarStrings;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.DelegatingEvent;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventFlag;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.compat.Event2Appointment;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
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
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.modules.Module;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaType;
import com.openexchange.quota.groupware.AmountQuotas;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.Operand;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ColumnFieldOperand;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;

/**
 * {@link Utils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Utils {

    /** The fixed identifier for an account of the internal calendar provider */
    public static final int ACCOUNT_ID = CalendarAccount.DEFAULT_ACCOUNT.getAccountId();

    /** The fixed identifier for the internal calendar provider */
    public static final String PROVIDER_ID = CalendarAccount.DEFAULT_ACCOUNT.getProviderId();

    /** A collection of fields that are always included when querying events from the storage */
    public static final List<EventField> DEFAULT_FIELDS = Arrays.asList(
        EventField.ID, EventField.SERIES_ID, EventField.FOLDER_ID, EventField.RECURRENCE_ID, EventField.TIMESTAMP, EventField.CREATED_BY,
        EventField.CALENDAR_USER, EventField.CLASSIFICATION, EventField.START_DATE, EventField.END_DATE, EventField.RECURRENCE_RULE,
        EventField.CHANGE_EXCEPTION_DATES, EventField.DELETE_EXCEPTION_DATES, EventField.ORGANIZER
    );

    /** The event fields that are also available if an event's classification is not {@link Classification#PUBLIC} */
    public static final EventField[] NON_CLASSIFIED_FIELDS = {
        EventField.CLASSIFICATION, EventField.CREATED, EventField.UID, EventField.FILENAME, EventField.CREATED_BY,
        EventField.CALENDAR_USER, EventField.CHANGE_EXCEPTION_DATES, EventField.DELETE_EXCEPTION_DATES, EventField.END_DATE,
        EventField.ID, EventField.TIMESTAMP, EventField.MODIFIED_BY, EventField.FOLDER_ID, EventField.SERIES_ID,
        EventField.RECURRENCE_RULE, EventField.SEQUENCE, EventField.START_DATE, EventField.TRANSP
    };

    /** A collection of fields that need to be queried to construct the special event flags field properly afterwards */
    public static final List<EventField> FLAG_FIELDS = Arrays.asList(
        EventField.ID, EventField.SERIES_ID, EventField.FOLDER_ID, EventField.RECURRENCE_ID,  EventField.STATUS, EventField.TRANSP,
        EventField.CLASSIFICATION, EventField.ORGANIZER, EventField.ATTACHMENTS, EventField.ALARMS, EventField.ATTENDEES
    );

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
     */
    public static boolean isEnforceDefaultAttendee(CalendarSession session) {
        // enabled by default for now (as legacy storage still in use)
        return session.get(CalendarParameters.PARAMETER_DEFAULT_ATTENDEE, Boolean.class, Boolean.TRUE).booleanValue();
    }

    /**
     * Gets a value indicating whether a recurring event series should be resolved to individual occurrences or not, based on the value
     * of {@link CalendarParameters#PARAMETER_EXPAND_OCCURRENCES} in the supplied parameters.
     *
     * @param parameters The calendar parameters to evaluate
     * @return <code>true</code> if individual occurrences should be resolved, <code>false</code>, otherwise
     * @see CalendarParameters#PARAMETER_EXPAND_OCCURRENCES
     */
    public static boolean isResolveOccurrences(CalendarParameters parameters) {
        return parameters.get(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, Boolean.class, Boolean.FALSE).booleanValue();
    }

    /**
     * Gets a value indicating whether (soft) conflicts of internal attendees should be checked during event creation or update or not,
     * based on the value of {@link CalendarParameters#PARAMETER_CHECK_CONFLICTS} in the supplied parameters.
     *
     * @param parameters The calendar parameters to evaluate
     * @return <code>true</code> if (soft) conflicts should be checked, <code>false</code>, otherwise
     * @see CalendarParameters#PARAMETER_CHECK_CONFLICTS
     */
    public static boolean isCheckConflicts(CalendarParameters parameters) {
        return parameters.get(CalendarParameters.PARAMETER_CHECK_CONFLICTS, Boolean.class, Boolean.FALSE).booleanValue();
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
     * Gets the event fields to include when querying events from the storage based on the supplied client-requested fields.
     * <p/>
     * Specific {@link Utils#DEFAULT_FIELDS} are included implicitly, further required ones may be defined explicitly, too. If the special
     * field {@link EventField#FLAGS} is requested, further fields (as listed in {@link Utils#FLAG_FIELDS}) are also added to be able to
     * derive the actual flags afterwards.
     *
     * @param requestedFields The fields requested by the client, or <code>null</code> to retrieve all fields
     * @param requiredFields Additionally required fields to add, or <code>null</code> if not defined
     * @return The fields to use when querying events from the storage
     * @see Utils#DEFAULT_FIELDS
     * @see Utils#FLAG_FIELDS
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
        if (fields.contains(EventFlag.class)) {
            fields.addAll(FLAG_FIELDS);
        }
        return fields.toArray(new EventField[fields.size()]);
    }

    /**
     * Constructs a search term to match events located in a specific folder. Depending on the folder's type, either a search term for
     * the {@link EventField#FOLDER_ID} and/or for the {@link AttendeeField#FOLDER_ID} is built.
     * <p/>
     * The session user's read permissions in the folder (<i>own</i> vs <i>all</i>) are considered automatically, too, by restricting via
     * {@link EventField#CREATED_BY} if needed.
     *
     * @param session The calendar session
     * @param folder The folder to construct the search term for
     * @return The search term
     */
    public static SearchTerm<?> getFolderIdTerm(CalendarSession session, UserizedFolder folder) {
        SearchTerm<?> searchTerm;
        if (PublicType.getInstance().equals(folder.getType())) {
            /*
             * match the event's common folder identifier
             */
            searchTerm = getSearchTerm(EventField.FOLDER_ID, SingleOperation.EQUALS, folder.getID());
        } else {
            /*
             * for personal folders, match against the corresponding attendee's folder
             */
            searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
                .addSearchTerm(getSearchTerm(AttendeeField.ENTITY, SingleOperation.EQUALS, I(folder.getCreatedBy())))
                .addSearchTerm(getSearchTerm(AttendeeField.FOLDER_ID, SingleOperation.EQUALS, folder.getID()));
            if (false == isEnforceDefaultAttendee(session)) {
                /*
                 * also match the event's common folder identifier if no default attendee is enforced
                 */
                searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
                    .addSearchTerm(getSearchTerm(EventField.FOLDER_ID, SingleOperation.EQUALS, folder.getID()))
                    .addSearchTerm(searchTerm);
            }
        }
        if (folder.getOwnPermission().getReadPermission() < Permission.READ_ALL_OBJECTS) {
            /*
             * if only access to "own" objects, restrict to events created by the current session's user
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
     * Creates a <i>userized</i> version of an event, representing a specific user's point of view on the event data. This includes
     * <ul>
     * <li><i>anonymization</i> of restricted event data in case the event it is not marked as {@link Classification#PUBLIC}, and the
     * current session's user is neither creator, nor attendee of the event.</li>
     * <li>selecting the appropriate parent folder identifier for the specific user</li>
     * <li>apply <i>userized</i> versions of change- and delete-exception dates in the series master event based on the user's actual
     * attendance</li>
     * <li>taking over the user's personal list of alarms for the event</li>
     * </ul>
     *
     * @param storage The calendar storage to use
     * @param session The calendar session
     * @param event The event to userize
     * @param forUser The identifier of the user in whose point of view the event should be adjusted
     * @return The <i>userized</i> event
     * @see Utils#applyExceptionDates
     * @see Utils#anonymizeIfNeeded
     */
    public static Event userize(CalendarSession session, CalendarStorage storage, Event event, int forUser) throws OXException {
        if (isSeriesMaster(event)) {
            event = applyExceptionDates(storage, event, forUser);
        }
        final List<Alarm> alarms = storage.getAlarmStorage().loadAlarms(event, forUser);
        final String folderView = getFolderView(event, forUser);
        if (null != alarms || false == folderView.equals(event.getFolderId())) {
            event = new DelegatingEvent(event) {

                @Override
                public String getFolderId() {
                    return folderView;
                }

                @Override
                public boolean containsFolderId() {
                    return true;
                }

                @Override
                public List<Alarm> getAlarms() {
                    return alarms;
                }

                @Override
                public boolean containsAlarms() {
                    return true;
                }
            };
        }
        return anonymizeIfNeeded(session, event);
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
                return false == session.getRecurrenceService().iterateEventOccurrences(event, from, until).hasNext();
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
     * Adds one or more warnings in the calendar session.
     *
     * @param session The calendar session
     * @param warnings The warnings to add, or <code>null</code> to ignore
     */
    public static void addWarnings(CalendarSession session, Collection<OXException> warnings) {
        if (null != warnings && 0 < warnings.size()) {
            for (OXException warning : warnings) {
                session.addWarning(warning);
            }
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
     * Gets the actual target calendar user for a specific folder. This is either the current session's user for "private" or "public"
     * folders, or the folder owner for "shared" calendar folders.
     *
     * @param session The calendar session
     * @param folder The folder to get the calendar user for
     * @return The calendar user
     */
    public static CalendarUser getCalendarUser(CalendarSession session, UserizedFolder folder) throws OXException {
        int calendarUserId = getCalendarUserId(folder);
        return session.getEntityResolver().applyEntityData(new CalendarUser(), calendarUserId);
    }

    /**
     * Gets the identifier of the actual target calendar user for a specific folder. This is either the current session's user for
     * "private" or "public" folders, or the folder owner for "shared" calendar folders.
     *
     * @param folder The folder to get the calendar user for
     * @return The identifier of the calendar user
     */
    public static int getCalendarUserId(UserizedFolder folder) {
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
            return null != userAttendee && folder.getID().equals(userAttendee.getFolderId());
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
            if ("FLD-1004".equals(e.getErrorCode())) {
                // com.openexchange.folderstorage.FolderExceptionErrorMessage.NO_STORAGE_FOR_ID
                throw CalendarExceptionCodes.UNSUPPORTED_FOLDER.create(e, folderID, "");
            }
            throw e;
        }
    }

    /**
     * Maps the corresponding event occurrences from two collections based on their common object- and recurrence identifiers.
     *
     * @param originalOccurrences The original event occurrences
     * @param updatedOccurrences The updated event occurrences
     * @return A list of entries holding each of the matching original and updated event occurrences, with one of them possibly <code>null</code>
     */
    public static List<Entry<Event, Event>> mapEventOccurrences(List<Event> originalOccurrences, List<Event> updatedOccurrences) {
        List<Entry<Event, Event>> mappedEvents = new ArrayList<Entry<Event, Event>>(Math.max(originalOccurrences.size(), updatedOccurrences.size()));
        for (Event originalOccurrence : originalOccurrences) {
            Event updatedOccurrence = find(updatedOccurrences, originalOccurrence.getId(), originalOccurrence.getRecurrenceId());
            mappedEvents.add(new AbstractMap.SimpleEntry<Event, Event>(originalOccurrence, updatedOccurrence));
        }
        for (Event updatedOccurrence : updatedOccurrences) {
            if (null == find(originalOccurrences, updatedOccurrence.getId(), updatedOccurrence.getRecurrenceId())) {
                mappedEvents.add(new AbstractMap.SimpleEntry<Event, Event>(null, updatedOccurrence));
            }
        }
        return mappedEvents;
    }

    /**
     * Get the configured quota and the actual usage of the underlying calendar account.
     *
     * @param session The calendar session
     * @param storage The calendar storage to use
     * @return The quota
     * @throws OXException In case of an error
     */
    public static Quota getQuota(CalendarSession session, CalendarStorage storage) throws OXException {
        /*
         * get configured amount quota limit
         */
        ConfigViewFactory configViewFactory = Services.getService(ConfigViewFactory.class, true);
        Connection connection = session.get(PARAM_CONNECTION, Connection.class);
        long limit;
        if (null != connection) {
            limit = AmountQuotas.getLimit(session.getSession(), Module.CALENDAR.getName(), configViewFactory, connection);
        } else {
            DatabaseService databaseService = Services.getService(DatabaseService.class, true);
            limit = AmountQuotas.getLimit(session.getSession(), Module.CALENDAR.getName(), configViewFactory, databaseService);
        }
        if (Quota.UNLIMITED == limit) {
            return Quota.UNLIMITED_AMOUNT;
        }
        /*
         * get actual usage & wrap in quota structure appropriately
         */
        long usage = storage.getEventStorage().countEvents();
        return new Quota(QuotaType.AMOUNT, limit, usage);
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
        if (false == isSeriesMaster(seriesMaster) || false == isGroupScheduled(seriesMaster) || isOrganizer(seriesMaster, forUser) ||
            isLastUserAttendee(seriesMaster.getAttendees(), forUser)) {
            /*
             * "real" delete exceptions for all attendees, take over as-is
             */
            return seriesMaster;
        }
        /*
         * check which change exceptions exist where the user is not attending
         */
        SortedSet<RecurrenceId> changeExceptionDates = seriesMaster.getChangeExceptionDates();
        if (null == changeExceptionDates || 0 == changeExceptionDates.size()) {
            return seriesMaster;
        }
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
            .addSearchTerm(getSearchTerm(EventField.SERIES_ID, SingleOperation.EQUALS, seriesMaster.getSeriesId()))
            .addSearchTerm(getSearchTerm(EventField.ID, SingleOperation.NOT_EQUALS, new ColumnFieldOperand<EventField>(EventField.SERIES_ID)))
            .addSearchTerm(getSearchTerm(AttendeeField.ENTITY, SingleOperation.NOT_EQUALS, I(forUser)))
        ;
        EventField[] fields = new EventField[] { EventField.ID, EventField.SERIES_ID, EventField.RECURRENCE_ID };
        List<Event> unattendedChangeExceptions = storage.getEventStorage().searchEvents(searchTerm, null, fields);
        if (null == unattendedChangeExceptions || 0 == unattendedChangeExceptions.size()) {
            return seriesMaster;
        }
        /*
         * apply a 'userized' version of exception dates by moving exception date from change- to delete-exceptions
         */
        SortedSet<RecurrenceId> userizedChangeExceptions = new TreeSet<RecurrenceId>(changeExceptionDates);
        SortedSet<RecurrenceId> userizedDeleteExceptions = new TreeSet<RecurrenceId>();
        if (null != seriesMaster.getDeleteExceptionDates()) {
            userizedDeleteExceptions.addAll(seriesMaster.getDeleteExceptionDates());
        }
        for (Event unattendedChangeException : unattendedChangeExceptions) {
            userizedChangeExceptions.remove(unattendedChangeException.getRecurrenceId());
            userizedDeleteExceptions.add(unattendedChangeException.getRecurrenceId());
        }
        return new DelegatingEvent(seriesMaster) {

            @Override
            public SortedSet<RecurrenceId> getDeleteExceptionDates() {
                return userizedDeleteExceptions;
            }

            @Override
            public boolean containsDeleteExceptionDates() {
                return true;
            }

            @Override
            public SortedSet<RecurrenceId> getChangeExceptionDates() {
                return userizedChangeExceptions;
            }

            @Override
            public boolean containsChangeExceptionDates() {
                return true;
            }
        };
    }

    /**
     * Gets a list containing all elements provided by the supplied iterator.
     *
     * @param itrerator The iterator to get the list for
     * @return The list
     */
    public static <T> List<T> asList(Iterator<T> iterator) {
        List<T> list = new ArrayList<T>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    /**
     * Gets a list containing all elements provided by the supplied iterator.
     *
     * @param itrerator The iterator to get the list for
     * @param limit The maximum number of items to include
     * @return The list
     */
    public static <T> List<T> asList(Iterator<T> iterator, int limit) {
        List<T> list = new ArrayList<T>();
        while (iterator.hasNext() && list.size() < limit) {
            list.add(iterator.next());
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

    /**
     * Calculates a map holding the identifiers of all folders a user is able to access, based on the supplied collection of folder
     * identifiers.
     *
     * @param session The calendar session
     * @param folderIds The identifiers of all folders to determine the users with access for
     * @return The identifiers of the affected folders for each user
     */
    public static Map<Integer, List<String>> getAffectedFoldersPerUser(CalendarSession session, Set<String> folderIds) {
        Map<Integer, List<String>> affectedFoldersPerUser = new HashMap<Integer, List<String>>();
        OXFolderAccess folderAccess;
        try {
            Context context = ServerSessionAdapter.valueOf(session.getSession()).getContext();
            Connection connection = optConnection(session);
            folderAccess = null != connection ? new OXFolderAccess(connection, context) : new OXFolderAccess(context);
        } catch (OXException e) {
            LoggerFactory.getLogger(Utils.class).warn("Error collecting affected folders", e);
            return affectedFoldersPerUser;
        }
        for (String folderId : folderIds) {
            try {
                FolderObject folder = folderAccess.getFolderObject(Event2Appointment.asInt(folderId));
                for (Integer userId : getAffectedUsers(folder, session.getEntityResolver())) {
                    com.openexchange.tools.arrays.Collections.put(affectedFoldersPerUser, userId, folderId);
                }
            } catch (Exception e) {
                LoggerFactory.getLogger(Utils.class).warn("Error collecting affected users for folder {}; skipping.", folderId, e);
            }
        }
        return affectedFoldersPerUser;
    }

    /**
     * Gets a list of the personal folder identifiers representing all internal user attendee's view for the supplied collection of
     * attendees.
     *
     * @param attendees The attendees to collect the folder identifiers for
     * @return The personal folder identifiers of the internal user attendees, or an empty list if there are none
     */
    public static List<String> getPersonalFolderIds(List<Attendee> attendees) {
        List<String> folderIds = new ArrayList<String>();
        for (Attendee attendee : CalendarUtils.filter(attendees, Boolean.TRUE, CalendarUserType.INDIVIDUAL)) {
            String folderId = attendee.getFolderId();
            if (Strings.isNotEmpty(folderId)) {
                folderIds.add(folderId);
            }
        }
        return folderIds;
    }

    /**
     * Gets a collection of all user identifiers for whom a specific folder is visible, i.e. a list of user identifiers who'd be affected
     * by a change in this folder.
     *
     * @param folder The folder to get the affected user identifiers for
     * @param entityResolver The entity resolver to use
     * @return The identifiers of the affected folders for each user
     */
    public static Set<Integer> getAffectedUsers(FolderObject folder, EntityResolver entityResolver) {
        List<OCLPermission> permissions = folder.getPermissions();
        if (null == permissions || 0 == permissions.size()) {
            return Collections.emptySet();
        }
        Set<Integer> affectedUsers = new HashSet<Integer>();
        for (OCLPermission permission : permissions) {
            if (permission.isFolderVisible()) {
                if (permission.isGroupPermission()) {
                    try {
                        int[] groupMembers = entityResolver.getGroupMembers(permission.getEntity());
                        affectedUsers.addAll(Arrays.asList(i2I(groupMembers)));
                    } catch (OXException e) {
                        LoggerFactory.getLogger(Utils.class).warn("Error resolving members of group {} for for folder {}; skipping.",
                            I(permission.getEntity()), I(folder.getObjectID()), e);
                    }
                } else {
                    affectedUsers.add(I(permission.getEntity()));
                }
            }
        }
        return affectedUsers;
    }

    /**
     * Gets a collection of all user identifiers for whom a specific folder is visible, i.e. a list of user identifiers who'd be affected
     * by a change in this folder.
     *
     * @param folder The folder to get the affected user identifiers for
     * @param entityResolver The entity resolver to use
     * @return The identifiers of the affected folders for each user
     */
    public static Set<Integer> getAffectedUsers(UserizedFolder folder, EntityResolver entityResolver) {
        Permission[] permissions = folder.getPermissions();
        if (null == permissions || 0 == permissions.length) {
            return Collections.emptySet();
        }
        Set<Integer> affectedUsers = new HashSet<Integer>();
        for (Permission permission : permissions) {
            if (permission.isVisible()) {
                if (permission.isGroup()) {
                    try {
                        int[] groupMembers = entityResolver.getGroupMembers(permission.getEntity());
                        affectedUsers.addAll(Arrays.asList(i2I(groupMembers)));
                    } catch (OXException e) {
                        LoggerFactory.getLogger(Utils.class).warn("Error resolving members of group {} for for folder {}; skipping.",
                            I(permission.getEntity()), folder.getID(), e);
                    }
                } else {
                    affectedUsers.add(I(permission.getEntity()));
                }
            }
        }
        return affectedUsers;
    }

    /**
     * Gets an iterator for the recurrence set of the supplied series master event, iterating over the occurrences of the event series.
     * <p/>
     * Any exception dates (as per {@link Event#getDeleteExceptionDates()}) and overridden instances (as per {@link
     * Event#getChangeExceptionDates()}) are skipped implicitly, so that those occurrences won't be included in the resulting iterator.
     * <p/>
     * Start- and end of the considered range are taken from the corresponding parameters in the supplied session.
     *
     * @param session The calendar session
     * @param masterEvent The recurring event master
     * @return The recurrence iterator
     */
    public static Iterator<Event> resolveOccurrences(CalendarSession session, Event masterEvent) throws OXException {
        return resolveOccurrences(session, masterEvent, getFrom(session), getUntil(session));
    }

    /**
     * Gets an iterator for the recurrence set of the supplied series master event, iterating over the occurrences of the event series.
     * <p/>
     * Any exception dates (as per {@link Event#getDeleteExceptionDates()}) and overridden instances (as per {@link
     * Event#getChangeExceptionDates()}) are skipped implicitly, so that those occurrences won't be included in the resulting iterator.
     *
     * @param session The calendar session
     * @param masterEvent The recurring event master
     * @param from The start of the iteration interval, or <code>null</code> to start with the first occurrence
     * @param until The end of the iteration interval, or <code>null</code> to iterate until the last occurrence
     * @return The recurrence iterator
     */
    public static Iterator<Event> resolveOccurrences(CalendarSession session, Event masterEvent, Date from, Date until) throws OXException {
        return session.getRecurrenceService().iterateEventOccurrences(masterEvent, from, until);
    }

    /**
     * Gets an iterator for the recurrence set of the supplied series master event, iterating over the recurrence identifiers of the event.
     * <p/>
     * Any exception dates (as per {@link Event#getDeleteExceptionDates()}) and overridden instances (as per {@link
     * Event#getChangeExceptionDates()}) are skipped implicitly, so that those occurrences won't be included in the resulting iterator.
     * <p/>
     * Start- and end of the considered range are taken from the corresponding parameters in the supplied session.
     *
     * @param session The calendar session
     * @param masterEvent The recurring event master
     * @return The recurrence iterator
     */
    public static Iterator<RecurrenceId> getRecurrenceIterator(CalendarSession session, Event masterEvent) throws OXException {
        return getRecurrenceIterator(session, masterEvent, getFrom(session), getUntil(session));
    }

    /**
     * Gets a recurrence iterator for the supplied series master event, iterating over the recurrence identifiers of the event.
     * <p/>
     * Any exception dates (as per {@link Event#getDeleteExceptionDates()}) and overridden instances (as per {@link
     * Event#getChangeExceptionDates()}) are skipped implicitly, so that those occurrences won't be included in the resulting iterator.
     *
     * @param session The calendar session
     * @param masterEvent The recurring event master
     * @param from The start of the iteration interval, or <code>null</code> to start with the first occurrence
     * @param until The end of the iteration interval, or <code>null</code> to iterate until the last occurrence
     * @return The recurrence iterator
     */
    public static Iterator<RecurrenceId> getRecurrenceIterator(CalendarSession session, Event masterEvent, Date from, Date until) throws OXException {
        return session.getRecurrenceService().iterateRecurrenceIds(new DefaultRecurrenceData(masterEvent), from, until);
    }

    private static FolderServiceDecorator initDecorator(CalendarSession session) throws OXException {
        FolderServiceDecorator decorator = new FolderServiceDecorator();
        Connection connection = optConnection(session);
        if (null != connection) {
            decorator.put(Connection.class.getName(), connection);
        }
        decorator.setLocale(session.getEntityResolver().getLocale(session.getUserId()));
        decorator.setTimeZone(Utils.getTimeZone(session));
        return decorator;
    }

    /**
     * Optionally gets a database connection set in a specific calendar session.
     *
     * @param session The calendar session to get the connection from
     * @return The connection, or <code>null</code> if not defined
     */
    public static Connection optConnection(CalendarSession session) {
        return session.get(AbstractStorageOperation.PARAM_CONNECTION, Connection.class, null);
    }

}
