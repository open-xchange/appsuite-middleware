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
import static com.openexchange.chronos.common.CalendarUtils.isPublicClassification;
import static com.openexchange.chronos.impl.Utils.getCalendarUserId;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;
import javax.mail.internet.AddressException;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.performer.ConflictCheckPerformer;
import com.openexchange.chronos.impl.performer.ResolveUidPerformer;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.groupware.tools.mappings.Mapping;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CalendarService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Check {

    /**
     * Checks that the session's user has permissions for the <i>calendar</i> module.
     *
     * @param session The session to check
     * @return The passed session, after the capability was checked
     * @throws OXException {@link CalendarExceptionCodes#MISSING_CAPABILITY}
     */
    public static ServerSession hasCalendar(ServerSession session) throws OXException {
        if (false == session.getUserConfiguration().hasCalendar()) {
            throw CalendarExceptionCodes.MISSING_CAPABILITY.create(com.openexchange.groupware.userconfiguration.Permission.CALENDAR.getCapabilityName());
        }
        return session;
    }

    /**
     * Checks that the session's user has permissions for the <i>calendar_freebusy</i> module.
     *
     * @param session The session to check
     * @return The passed session, after the capability was checked
     * @throws OXException {@link CalendarExceptionCodes#MISSING_CAPABILITY}
     */
    public static ServerSession hasFreeBusy(ServerSession session) throws OXException {
        if (false == session.getUserConfiguration().hasFreeBusy()) {
            throw CalendarExceptionCodes.MISSING_CAPABILITY.create("calendar_freebusy");
        }
        return session;
    }

    /**
     * Checks that the required permissions are fulfilled in a specific userized folder.
     *
     * @param folder The folder to check the permissions for
     * @param requiredFolderPermission The required folder permission, or {@link Permission#NO_PERMISSIONS} if none required
     * @param requiredReadPermission The required read object permission, or {@link Permission#NO_PERMISSIONS} if none required
     * @param requiredWritePermission The required write object permission, or {@link Permission#NO_PERMISSIONS} if none required
     * @param requiredDeletePermission The required delete object permission, or {@link Permission#NO_PERMISSIONS} if none required
     * @throws OXException {@link CalendarExceptionCodes#UNSUPPORTED_FOLDER}, {@link CalendarExceptionCodes#NO_READ_PERMISSION}, {@link CalendarExceptionCodes#NO_WRITE_PERMISSION}, {@link CalendarExceptionCodes#NO_DELETE_PERMISSION}
     */
    public static void requireCalendarPermission(UserizedFolder folder, int requiredFolderPermission, int requiredReadPermission, int requiredWritePermission, int requiredDeletePermission) throws OXException {
        if (false == CalendarContentType.class.isInstance(folder.getContentType())) {
            throw CalendarExceptionCodes.UNSUPPORTED_FOLDER.create(folder.getID(), String.valueOf(folder.getContentType()));
        }
        Permission ownPermission = folder.getOwnPermission();
        if (ownPermission.getFolderPermission() < requiredFolderPermission) {
            throw CalendarExceptionCodes.NO_READ_PERMISSION.create(folder.getID());
        }
        if (ownPermission.getReadPermission() < requiredReadPermission) {
            throw CalendarExceptionCodes.NO_READ_PERMISSION.create(folder.getID());
        }
        if (ownPermission.getWritePermission() < requiredWritePermission) {
            throw CalendarExceptionCodes.NO_WRITE_PERMISSION.create(folder.getID());
        }
        if (ownPermission.getDeletePermission() < requiredDeletePermission) {
            throw CalendarExceptionCodes.NO_DELETE_PERMISSION.create(folder.getID());
        }
    }

    public static void allowedOrganizerSchedulingObjectChange(Event originalEvent, Event udpatedEvent) throws OXException {

    }

    public static void allowedAttendeeSchedulingObjectChange(Event originalEvent, Event udpatedEvent) throws OXException {

    }

    /**
     * Checks that the supplied search pattern length is equal to or greater than a configured minimum.
     *
     * @param minimumPatternLength, The minimum search pattern length, or <code>0</code> for no limitation
     * @param pattern The pattern to check
     * @return The passed pattern, after the length was checked
     * @throws OXException {@link CalendarExceptionCodes#QUERY_TOO_SHORT}
     */
    public static String minimumSearchPatternLength(String pattern, int minimumPatternLength) throws OXException {
        if (null != pattern && 0 < minimumPatternLength && pattern.length() < minimumPatternLength) {
            throw CalendarExceptionCodes.QUERY_TOO_SHORT.create(I(minimumPatternLength), pattern);
        }
        return pattern;
    }

    /**
     * Checks that the supplied client timestamp is equal to or greater than the last modification time of the event.
     *
     * @param event The event to check the timestamp against
     * @param clientTimestamp The client timestamp
     * @return The passed event, after the timestamp was checked
     * @throws OXException {@link CalendarExceptionCodes#CONCURRENT_MODIFICATION}
     */
    public static Event requireUpToDateTimestamp(Event event, long clientTimestamp) throws OXException {
        if (null != event.getLastModified() && event.getLastModified().getTime() > clientTimestamp) {
            throw CalendarExceptionCodes.CONCURRENT_MODIFICATION.create(event.getId(), L(clientTimestamp), L(event.getLastModified().getTime()));
        }
        return event;
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
        if (false == Utils.isInFolder(event, folder)) {
            throw CalendarExceptionCodes.EVENT_NOT_FOUND_IN_FOLDER.create(folder.getID(), event.getId());
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
     * Checks
     * <ul>
     * <li>that the start- and enddate properties are set in the event</li>
     * <li>that the end date does is not before the start date</li>
     * <li>that both start and enddate are either both <i>all-day</i> or not</li>
     * <li>that both start and enddate are either both <i>floating</i> or not</li>
     * </ul>
     *
     * @param event The event to check
     * @see Check#mandatoryFields(Event, EventField...)
     * @throws OXException {@link CalendarExceptionCodes#MANDATORY_FIELD}, {@link CalendarExceptionCodes#END_BEFORE_START}
     */
    public static void startAndEndDate(Event event) throws OXException {
        mandatoryFields(event, EventField.START_DATE, EventField.END_DATE);
        if (event.getStartDate().after(event.getEndDate())) {
            throw CalendarExceptionCodes.END_BEFORE_START.create(L(event.getStartDate().getTimestamp()), L(event.getEndDate().getTimestamp()));
        }
        if (event.getStartDate().isAllDay() != event.getEndDate().isAllDay()) {
            throw new OXException(); //TODO
        }
        if (event.getStartDate().isFloating() != event.getEndDate().isFloating()) {
            throw new OXException(); //TODO
        }
    }

    /**
     * Checks that a list of alarms are valid, i.e. they all contain all mandatory properties.
     *
     * @param alarms The alarms to check
     * @return The passed alarms, after they were checked for validity
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RRULE}
     */
    public static List<Alarm> alarmsAreValid(List<Alarm> alarms) throws OXException {
        if (null != alarms && 0 < alarms.size()) {
            for (Alarm alarm : alarms) {
                alarmIsValid(alarm);
            }
        }
        return alarms;
    }

    /**
     * Checks that the supplied alarm is valid, i.e. it contains all mandatory properties.
     *
     * @param alarm The alarm to check
     * @return The passed alarm, after it was checked for validity
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RRULE}
     */
    public static Alarm alarmIsValid(Alarm alarm) throws OXException {
        /*
         * action and trigger are both required for any type of alarm
         */
        if (null == alarm.getAction()) {
            throw CalendarExceptionCodes.MANDATORY_FIELD.create(AlarmField.ACTION.toString());
        }
        if (null == alarm.getTrigger() || null == alarm.getTrigger().getDateTime() && null == alarm.getTrigger().getDuration()) {
            throw CalendarExceptionCodes.MANDATORY_FIELD.create(AlarmField.TRIGGER.toString());
        }
        /*
         * check further properties based on alarm type
         */
        if (AlarmAction.DISPLAY.equals(alarm.getAction())) {
            if (!alarm.containsDescription()) {
                throw CalendarExceptionCodes.MANDATORY_FIELD.create(AlarmField.DESCRIPTION.toString());
            }
            return alarm;
        } else if (AlarmAction.EMAIL.equals(alarm.getAction())) {
            if ((!alarm.containsDescription()) || alarm.getDescription().isEmpty()) {
                throw CalendarExceptionCodes.MANDATORY_FIELD.create(AlarmField.DESCRIPTION.toString());
            }
            if ((!alarm.containsSummary()) || alarm.getSummary().isEmpty()) {
                throw CalendarExceptionCodes.MANDATORY_FIELD.create(AlarmField.SUMMARY.toString());
            }

            if ((!alarm.containsAttendees()) || alarm.getAttendees().isEmpty()) {
                throw CalendarExceptionCodes.MANDATORY_FIELD.create(AlarmField.ATTENDEES.toString());
            }

            for (Attendee attendee : alarm.getAttendees()) {
                if (attendee.getEMail() == null || attendee.getEMail().isEmpty()) {
                    throw CalendarExceptionCodes.MANDATORY_FIELD.create(AlarmField.ATTENDEES.toString());
                }
                try {
                    new QuotedInternetAddress(attendee.getEMail());
                } catch (AddressException e) {
                    throw CalendarExceptionCodes.MANDATORY_FIELD.create(AlarmField.ATTENDEES.toString());
                }
            }
        }
        return alarm;
    }

    /**
     * Checks that the classification is supported based on the given folder's type, if it is not <code>null</code> and different from
     * {@link Classification#PUBLIC}.
     *
     * @param classification The classification to check, or <code>null</code> to skip the check
     * @param folder The target folder for the event
     * @return The passed classification, after it was checked for validity
     * @throws OXException {@link CalendarExceptionCodes#UNSUPPORTED_CLASSIFICATION}
     */
    public static Classification classificationIsValid(Classification classification, UserizedFolder folder) throws OXException {
        if (null != classification && false == Classification.PUBLIC.equals(classification) && PublicType.getInstance().equals(folder.getType())) {
            throw CalendarExceptionCodes.UNSUPPORTED_CLASSIFICATION.create(String.valueOf(classification), folder.getID(), PublicType.getInstance());
        }
        return classification;
    }

    /**
     * Checks that the classification is supported during move operations based on the given source- and target folder's type, if it is
     * not <code>null</code> and different from {@link Classification#PUBLIC}.
     * <p/>
     * A move of a confidentially- or private-classified event will be denied in case the target folder is <i>public</i>, or the target
     * folder's calendar user is different from the sourcefolder's calendar user.
     *
     * @param classification The classification to check, or <code>null</code> to skip the check
     * @param folder The source folder of the event
     * @param targetFolder The target folder of the event
     * @return The passed classification, after it was checked for validity
     * @throws OXException {@link CalendarExceptionCodes#UNSUPPORTED_CLASSIFICATION_FOR_MOVE}
     */
    public static Classification classificationIsValidOnMove(Classification classification, UserizedFolder folder, UserizedFolder targetFolder) throws OXException {
        if (null != classification && false == Classification.PUBLIC.equals(classification)) {
            if (PublicType.getInstance().equals(targetFolder.getType()) || getCalendarUserId(folder) != getCalendarUserId(targetFolder)) {
                throw CalendarExceptionCodes.UNSUPPORTED_CLASSIFICATION_FOR_MOVE.create(
                    String.valueOf(classification), folder.getID(), folder.getType(), targetFolder.getID(), targetFolder.getType());
            }
        }
        return classification;
    }

    /**
     * Checks that an update or delete operation is allowed based on the original event's classification.
     *
     * @param folder The parent folder of the event
     * @param originalEvent The original event to check
     * @throws OXException {@link CalendarExceptionCodes#RESTRICTED_BY_CLASSIFICATION}
     */
    public static void classificationAllowsUpdate(UserizedFolder folder, Event originalEvent) throws OXException {
        if (false == isPublicClassification(originalEvent)) {
            int userID = folder.getUser().getId();
            if (originalEvent.getCreatedBy() != userID && false == contains(originalEvent.getAttendees(), userID)) {
                throw CalendarExceptionCodes.RESTRICTED_BY_CLASSIFICATION.create(folder.getID(), originalEvent.getId(), String.valueOf(originalEvent.getClassification()));
            }
        }
    }

    /**
     * Checks an event's recurrence rule for validity.
     *
     * @param recurrenceService A reference to the recurrence service
     * @param event The event to check
     * @return The passed event's recurrence rule, after it was checked for validity
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RRULE}
     */
    public static String recurrenceRuleIsValid(RecurrenceService recurrenceService, Event event) throws OXException {
        String recurrenceRule = event.getRecurrenceRule();
        if (event.containsRecurrenceRule() && null != recurrenceRule) {
            recurrenceService.validate(new DefaultRecurrenceData(event));
        }
        return recurrenceRule;
    }

    /**
     * Ensures that all recurrence identifiers are valid for a specific recurring event series, i.e. the targeted occurrences
     * are actually part of the series.
     *
     * @param recurrenceService A reference to the recurrence service
     * @param seriesMaster The series master event providing the recurrence information
     * @param recurrenceID The recurrence identifier
     * @return The passed list of recurrence identifiers, after their existence was checked
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RECURRENCE_ID}
     */
    public static SortedSet<RecurrenceId> recurrenceIdsExist(RecurrenceService recurrenceService, Event seriesMaster, SortedSet<RecurrenceId> recurrenceIDs) throws OXException {
        if (null != recurrenceIDs) {
            for (RecurrenceId recurrenceID : recurrenceIDs) {
                recurrenceIdExists(recurrenceService, seriesMaster, recurrenceID);
            }
        }
        return recurrenceIDs;
    }

    /**
     * Ensures that a specific recurrence identifier is valid for a specific recurring event series, i.e. the targeted occurrence
     * is actually part of the series.
     *
     * @param recurrenceService A reference to the recurrence service
     * @param seriesMaster The series master event providing the recurrence information
     * @param recurrenceID The recurrence identifier
     * @return The passed recurrence identifier, after it was checked for validity
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RECURRENCE_ID}
     */
    public static RecurrenceId recurrenceIdExists(RecurrenceService recurrenceService, Event seriesMaster, RecurrenceId recurrenceID) throws OXException {
        Iterator<RecurrenceId> iterator = recurrenceService.iterateRecurrenceIds(seriesMaster, new Date(recurrenceID.getValue()), null);
        if (false == iterator.hasNext()) {
            throw CalendarExceptionCodes.INVALID_RECURRENCE_ID.create(String.valueOf(recurrenceID), seriesMaster.getRecurrenceRule());
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
            String existingId = new ResolveUidPerformer(storage).perform(uid);
            if (null != existingId) {
                throw CalendarExceptionCodes.UID_CONFLICT.create(uid, existingId);
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
            throw CalendarExceptionCodes.ATTENDEE_NOT_FOUND.create(attendee, event.getId());
        }
        return matchingAttendee;
    }

    /**
     * Checks that an event being inserted/updated does not conflict with other existing events.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param event The event being inserted/updated
     * @param attendees The event's list of attendees, or <code>null</code> in case of a not group-scheduled event
     * @throws OXException {@link CalendarExceptionCodes#EVENT_CONFLICTS}, {@link CalendarExceptionCodes#HARD_EVENT_CONFLICTS}
     */
    public static void noConflicts(CalendarStorage storage, CalendarSession session, Event event, List<Attendee> attendees) throws OXException {
        noConflicts(new ConflictCheckPerformer(session, storage).perform(event, attendees));
    }

    /**
     * Checks that the supplied list of conflicts is empty.
     *
     * @param conflicts The conflict check result
     * @throws OXException {@link CalendarExceptionCodes#EVENT_CONFLICTS}, {@link CalendarExceptionCodes#HARD_EVENT_CONFLICTS}
     */
    public static void noConflicts(List<EventConflict> conflicts) throws OXException {
        if (null != conflicts && 0 < conflicts.size()) {
            // derive "hard conflict" from first event as list should be ordered
            OXException conflictException = conflicts.get(0).isHardConflict() ?
                CalendarExceptionCodes.HARD_EVENT_CONFLICTS.create() : CalendarExceptionCodes.EVENT_CONFLICTS.create();
            for (EventConflict eventConflict : conflicts) {
                conflictException.addProblematic(eventConflict);
            }
            throw conflictException;
        }
    }

    /**
     * Checks that the supplied timezone identifier is valid, i.e. a corresponding timezone exists.
     *
     * @param timeZoneID The timezone identifier to check, or <code>null</code> to skip the check
     * @return The identifier of the matching timezone
     * @throws OXException {@link CalendarExceptionCodes#INVALID_TIMEZONE}
     */
    public static String timeZoneExists(String timeZoneID) throws OXException {
        TimeZone timeZone = CalendarUtils.optTimeZone(timeZoneID, null);
        if (null == timeZone) {
            throw CalendarExceptionCodes.INVALID_TIMEZONE.create(timeZoneID);
        }
        return timeZone.getID();
    }

    /**
     * Checks that the supplied calendar user's URI denotes a valid e-mail address.
     * <p/>
     * This method should only be invoked for <i>external</i> calendar users.
     *
     * @param calendarUser The (external) calendar user to check
     * @return The calendar user, after its URI has been checked for validity
     * @throws OXException {@link CalendarExceptionCodes#INVALID_CALENDAR_USER}
     */
    public static <T extends CalendarUser> T requireValidEMail(T calendarUser) throws OXException {
        String address = CalendarUtils.extractEMailAddress(calendarUser.getUri());
        if (null == address) {
            throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(calendarUser.getUri(), I(calendarUser.getEntity()), "");
        }
        try {
            new QuotedInternetAddress(address);
        } catch (AddressException e) {
            throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(e, calendarUser.getUri(), I(calendarUser.getEntity()), "");
        }
        return calendarUser;
    }

    /**
     * Initializes a new {@link Check}.
     */
    private Check() {
        super();
    }

}
