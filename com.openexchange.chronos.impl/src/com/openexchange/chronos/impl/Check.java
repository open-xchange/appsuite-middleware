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

import static com.openexchange.chronos.impl.Utils.getSearchTerm;
import static com.openexchange.chronos.impl.Utils.i;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.SortOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.groupware.tools.mappings.Mapping;
import com.openexchange.java.Strings;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ColumnFieldOperand;

/**
 * {@link CalendarService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Check {

    /**
     * Checks that the required permissions are fulfilled in a specific userized folder.
     *
     * @param folder The folder to check the permissions for
     * @param requiredFolderPermission The required folder permission, or {@link Permission#NO_PERMISSIONS} if none required
     * @param requiredReadPermission The required read object permission, or {@link Permission#NO_PERMISSIONS} if none required
     * @param requiredWritePermission The required write object permission, or {@link Permission#NO_PERMISSIONS} if none required
     * @param requiredDeletePermission The required delete object permission, or {@link Permission#NO_PERMISSIONS} if none required
     * @throws OXException {@link CalendarExceptionCodes#NO_READ_PERMISSION}, {@link CalendarExceptionCodes#NO_WRITE_PERMISSION}, {@link CalendarExceptionCodes#NO_DELETE_PERMISSION}
     */
    public static void requireCalendarPermission(UserizedFolder folder, int requiredFolderPermission, int requiredReadPermission, int requiredWritePermission, int requiredDeletePermission) throws OXException {
        if (false == CalendarContentType.class.isInstance(folder.getContentType())) {
            throw CalendarExceptionCodes.NO_READ_PERMISSION.create(I(folder.getContext().getContextId()), I(folder.getUser().getId()), I(i(folder)));
        }
        Permission ownPermission = folder.getOwnPermission();
        if (ownPermission.getFolderPermission() < requiredFolderPermission) {
            throw CalendarExceptionCodes.NO_READ_PERMISSION.create(I(folder.getContext().getContextId()), I(folder.getUser().getId()), I(i(folder)));
        }
        if (ownPermission.getReadPermission() < requiredReadPermission) {
            throw CalendarExceptionCodes.NO_READ_PERMISSION.create(I(folder.getContext().getContextId()), I(folder.getUser().getId()), I(i(folder)));
        }
        if (ownPermission.getWritePermission() < requiredWritePermission) {
            throw CalendarExceptionCodes.NO_WRITE_PERMISSION.create(I(folder.getContext().getContextId()), I(folder.getUser().getId()), I(i(folder)));
        }
        if (ownPermission.getDeletePermission() < requiredDeletePermission) {
            throw CalendarExceptionCodes.NO_DELETE_PERMISSION.create(I(folder.getContext().getContextId()), I(folder.getUser().getId()), I(i(folder)));
        }
    }

    public static void allowedOrganizerSchedulingObjectChange(Event originalEvent, Event udpatedEvent) throws OXException {

    }

    public static void allowedAttendeeSchedulingObjectChange(Event originalEvent, Event udpatedEvent) throws OXException {

    }

    public static void requireMinimumSearchPatternLength(String pattern) throws OXException {

    }

    /**
     * Checks that the supplied client timestamp is equal to or greater than the last modification time of the event.
     *
     * @param event The event to check the timestamp against
     * @param clientTimestampp The client timestamp
     * @throws OXException {@link CalendarExceptionCodes#CONCURRENT_MODIFICATION}
     */
    public static void requireUpToDateTimestamp(Event event, long clientTimestampp) throws OXException {
        if (event.getLastModified().getTime() > clientTimestampp) {
            throw CalendarExceptionCodes.CONCURRENT_MODIFICATION.create(I(event.getId()), L(clientTimestampp), L(event.getLastModified().getTime()));
        }
    }

    /**
     * Checks that a specific event is actually present in the supplied folder. Based on the folder type, the event's public folder
     * identifier or the attendee's personal calendar folder is checked.
     *
     * @param event The event to check
     * @param folder The folder where the event should appear in
     * @throws OXException {@link CalendarExceptionCodes#EVENT_NOT_FOUND_IN_FOLDER}
     */
    public static void eventIsInFolder(Event event, UserizedFolder folder) throws OXException {
        if (PublicType.getInstance().equals(folder.getType())) {
            if (event.getPublicFolderId() != i(folder)) {
                throw CalendarExceptionCodes.EVENT_NOT_FOUND_IN_FOLDER.create(I(i(folder)), I(event.getId()));
            }
        } else {
            Attendee userAttendee = CalendarUtils.find(event.getAttendees(), folder.getCreatedBy());
            if (null == userAttendee || userAttendee.getFolderID() != i(folder)) {
                throw CalendarExceptionCodes.EVENT_NOT_FOUND_IN_FOLDER.create(I(i(folder)), I(event.getId()));
            }
        }
    }

    /**
     * Checks that all specified mandatory fields are <i>set</i> and not <code>null</code> in the event.
     *
     * @param event The event to check
     * @param fields The mandatory fields
     * @throws OXException {@link CalendarExceptionCodes#MANDATORY_FIELD}
     */
    public static void mandatoryFields(Event event, EventField... fields) throws OXException {
        if (null != fields) {
            for (EventField field : fields) {
                Mapping<? extends Object, Event> mapping = EventMapper.getInstance().get(field);
                if (false == mapping.isSet(event) || null == mapping.get(event)) {
                    String readableName = String.valueOf(field); //TODO i18n
                    throw CalendarExceptionCodes.MANDATORY_FIELD.create(readableName, String.valueOf(field));
                }
            }
        }
    }

    /**
     * Checks that the start- and enddate properties are set in an event, and ensures that the end date does is not before the start date.
     *
     * @param event The event to check
     * @see Check#mandatoryFields(Event, EventField...)
     * @throws OXException {@link CalendarExceptionCodes#MANDATORY_FIELD}, {@link CalendarExceptionCodes#END_BEFORE_START}
     */
    public static void startAndEndDate(Event event) throws OXException {
        mandatoryFields(event, EventField.START_DATE, EventField.END_DATE);
        if (event.getStartDate().after(event.getEndDate())) {
            throw CalendarExceptionCodes.END_BEFORE_START.create(L(event.getStartDate().getTime()), L(event.getEndDate().getTime()));
        }
    }

    /**
     * Checks that the classification is supported based on the given folder's type, if it is different from {@link Classification#PUBLIC}.
     *
     * @param classification The classification to check
     * @param folder The target folder for the event
     * @return The passed classification, after it was checked for validity
     * @throws OXException {@link CalendarExceptionCodes#UNSUPPORTED_CLASSIFICATION}
     */
    public static Classification classificationIsValid(Classification classification, UserizedFolder folder) throws OXException {
        if (false == Classification.PUBLIC.equals(classification) && PublicType.getInstance().equals(folder.getType())) {
            throw CalendarExceptionCodes.UNSUPPORTED_CLASSIFICATION.create(String.valueOf(classification), I(i(folder)), PublicType.getInstance());
        }
        return classification;
    }

    /**
     * Checks an event's recurrence rule for validity.
     *
     * @param event The event to check
     * @return The passed event's recurrence rule, after it was checked for validity
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RRULE}
     */
    public static String recurrenceRuleIsValid(Event event) throws OXException {
        String recurrenceRule = event.getRecurrenceRule();
        if (event.containsRecurrenceRule() && null != recurrenceRule) {
            try {
                new RecurrenceRule(event.getRecurrenceRule());
            } catch (InvalidRecurrenceRuleException e) {
                throw CalendarExceptionCodes.INVALID_RRULE.create(e, event.getRecurrenceRule());
            }
        }
        return recurrenceRule;
    }

    /**
     * Ensures that all recurrence identifiers are valid for a specific recurring event series, i.e. the targeted occurrences
     * are actually part of the series.
     *
     * @param seriesMaster The series master event providing the recurrence information
     * @param recurrenceID The recurrence identifier
     * @return The passed list of recurrence identifiers, after their existence was checked
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RECURRENCE_ID}
     */
    public static List<Date> recurrenceIdsExist(Event seriesMaster, List<Date> recurrenceIDs) throws OXException {
        if (null != recurrenceIDs) {
            for (Date recurrenceID : recurrenceIDs) {
                recurrenceIdExists(seriesMaster, recurrenceID);
            }
        }
        return recurrenceIDs;
    }

    /**
     * Ensures that a specific recurrence identifier is valid for a specific recurring event series, i.e. the targeted occurrence
     * is actually part of the series.
     *
     * @param seriesMaster The series master event providing the recurrence information
     * @param recurrenceID The recurrence identifier
     * @return The passed recurrence identifier, after it was checked for validity
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RECURRENCE_ID}
     */
    public static Date recurrenceIdExists(Event seriesMaster, Date recurrenceID) throws OXException {
        RecurrenceRule rule;
        try {
            rule = new RecurrenceRule(seriesMaster.getRecurrenceRule());
        } catch (InvalidRecurrenceRuleException e) {
            throw CalendarExceptionCodes.INVALID_RRULE.create(e, seriesMaster.getRecurrenceRule());
        }
        DateTime start;
        if (seriesMaster.isAllDay()) {
            start = new DateTime(seriesMaster.getStartDate().getTime()).toAllDay();
        } else {
            start = new DateTime(TimeZone.getTimeZone(seriesMaster.getStartTimeZone()), seriesMaster.getStartDate().getTime());
        }
        RecurrenceRuleIterator iterator = rule.iterator(start);
        iterator.fastForward(recurrenceID.getTime());
        if (false == iterator.hasNext() || recurrenceID.getTime() != iterator.nextMillis()) {
            throw CalendarExceptionCodes.INVALID_RECURRENCE_ID.create(L(recurrenceID.getTime()), seriesMaster.getRecurrenceRule());
        }
        return recurrenceID;
    }

    /**
     * Checks that the supplied event's unique identifier (UID) is not already used for another event within the same context.
     *
     * @param storage A reference to the calendar storage
     * @param event The event to check
     * @return The passed event's unique identifier, after it was checked for uniqueness
     * @throws OXException {@link CalendarExceptionCodes#UID_CONFLICT}
     */
    public static String uidIsUnique(CalendarStorage storage, Event event) throws OXException {
        String uid = event.getUid();
        if (Strings.isNotEmpty(uid)) {
            CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
                .addSearchTerm(getSearchTerm(EventField.UID, SingleOperation.EQUALS, uid))
                .addSearchTerm(new CompositeSearchTerm(CompositeOperation.OR)
                    .addSearchTerm(getSearchTerm(EventField.SERIES_ID, SingleOperation.ISNULL))
                    .addSearchTerm(getSearchTerm(EventField.ID, SingleOperation.EQUALS, new ColumnFieldOperand<EventField>(EventField.SERIES_ID)))
                )
            ;
            List<Event> events = storage.getEventStorage().searchEvents(searchTerm, new SortOptions().setLimits(0, 1), new EventField[] { EventField.ID });
            if (0 < events.size()) {
                throw CalendarExceptionCodes.UID_CONFLICT.create(uid, I(events.get(0).getId()));
            }
        }
        return uid;
    }

    /**
     * Checks that a particular attendee exists in an event.
     *
     * @param event The event to check
     * @param attendee The attendee to lookup
     * @return The successfully looked up attendee
     * @see CalendarUtils#find(List, Attendee)
     * @throws OXException {@link CalendarExceptionCodes#ATTENDEE_NOT_FOUND}
     */
    public static Attendee attendeeExists(Event event, Attendee attendee) throws OXException {
        Attendee matchingAttendee = CalendarUtils.find(event.getAttendees(), attendee);
        if (null == matchingAttendee) {
            throw CalendarExceptionCodes.ATTENDEE_NOT_FOUND.create(attendee, I(event.getId()));
        }
        return matchingAttendee;
    }

    /**
     * Initializes a new {@link Check}.
     */
    private Check() {
        super();
    }

}
