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
import static com.openexchange.chronos.common.CalendarUtils.matches;
import static com.openexchange.chronos.impl.Utils.getCalendarUserId;
import static com.openexchange.java.Autoboxing.L;
import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.RecurrenceRange;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.SelfProtectionFactory.SelfProtection;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.performer.ConflictCheckPerformer;
import com.openexchange.chronos.impl.performer.ResolveUidPerformer;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.java.Strings;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CalendarService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Check extends com.openexchange.chronos.common.Check {

    /**
     * Checks that the session's user has permissions for the <i>calendar</i> module.
     *
     * @param session The session to check
     * @return The passed session, after the capability was checked
     * @throws OXException {@link CalendarExceptionCodes#MISSING_CAPABILITY}
     */
    public static ServerSession hasCalendar(ServerSession session) throws OXException {
        if (false == session.getUserPermissionBits().hasCalendar()) {
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
        if (false == session.getUserPermissionBits().hasFreeBusy()) {
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

    /**
     * Checks that the supplied client timestamp is equal to or greater than the timestamp of the event.
     *
     * @param event The event to check the timestamp against
     * @param clientTimestamp The client timestamp
     * @return The passed event, after the timestamp was checked
     * @throws OXException {@link CalendarExceptionCodes#CONCURRENT_MODIFICATION}
     */
    public static Event requireUpToDateTimestamp(Event event, long clientTimestamp) throws OXException {
        if (event.getTimestamp() > clientTimestamp) {
            throw CalendarExceptionCodes.CONCURRENT_MODIFICATION.create(event.getId(), L(clientTimestamp), L(event.getTimestamp()));
        }
        return event;
    }

    /**
     * Checks that a specific event is actually present in the supplied folder. Based on the folder type, the event's public folder
     * identifier or the attendee's personal calendar folder is checked.
     *
     * @param event The event to check
     * @param folder The folder where the event should appear in
     * @return The identifier of the passed folder, after it was checked that it is a valid parent folder of the event
     * @throws OXException {@link CalendarExceptionCodes#EVENT_NOT_FOUND_IN_FOLDER}
     */
    public static String eventIsInFolder(Event event, UserizedFolder folder) throws OXException {
        if (false == Utils.isInFolder(event, folder)) {
            throw CalendarExceptionCodes.EVENT_NOT_FOUND_IN_FOLDER.create(folder.getID(), event.getId());
        }
        return folder.getID();
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
            if (false == matches(originalEvent.getCreatedBy(), userID) && false == contains(originalEvent.getAttendees(), userID)) {
                throw CalendarExceptionCodes.RESTRICTED_BY_CLASSIFICATION.create(folder.getID(), originalEvent.getId(), String.valueOf(originalEvent.getClassification()));
            }
        }
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
     * Checks that a specific unique identifier (UID) is not already used for another event within the same context.
     *
     * @param storage A reference to the calendar storage
     * @param uid The unique identifier to check
     * @return The passed unique identifier, after it was checked for uniqueness
     * @throws OXException {@link CalendarExceptionCodes#UID_CONFLICT}
     */
    public static String uidIsUnique(CalendarStorage storage, String uid) throws OXException {
        String existingId = new ResolveUidPerformer(storage).perform(uid);
        if (null != existingId) {
            throw CalendarExceptionCodes.UID_CONFLICT.create(uid, existingId);
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
     * Checks that the account's quota is not exceeded prior inserting new event data.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @throws OXException {@link QuotaExceptionCodes#QUOTA_EXCEEDED_CALENDAR}
     */
    public static void quotaNotExceeded(CalendarStorage storage, CalendarSession session) throws OXException {
        Quota quota = Utils.getQuota(session, storage);
        if (null != quota && quota.isExceeded()) {
            throw QuotaExceptionCodes.QUOTA_EXCEEDED_CALENDAR.create(String.valueOf(quota.getUsage()), String.valueOf(quota.getLimit()));
        }
    }

    /**
     * Checks that the maximum number of attendees is not exceeded prior inserting or updating the attendee collection of an event.
     *
     * @param selfProtection A reference to the self-protection helper
     * @param attendees The attendees to check
     * @return The passed attendees, after they were checked
     * @throws OXException {@link CalendarExceptionCodes#TOO_MANY_ATTENDEES}
     * @see <a href="https://tools.ietf.org/html/rfc4791#section-5.2.9">RFC 4791, section 5.2.9</a>
     */
    public static List<Attendee> maxAttendees(SelfProtection selfProtection, List<Attendee> attendees) throws OXException {
        if (null != attendees) {
            selfProtection.checkAttendeeCollection(attendees);
        }
        return attendees;
    }

    /**
     * Checks that the maximum number of alarms is not exceeded prior inserting or updating the alarm collection of an event for a user.
     *
     * @param selfProtection A reference to the self-protection helper
     * @param alarms The alarms to check
     * @return The passed alarms, after they were checked
     * @throws OXException {@link CalendarExceptionCodes#TOO_MANY_ALARMS}
     */
    public static List<Alarm> maxAlarms(SelfProtection selfProtection, List<Alarm> alarms) throws OXException {
        if (null != alarms) {
            Event event = new Event();
            event.setAlarms(alarms);
            selfProtection.checkEvent(event);
        }
        return alarms;
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
     * Checks that the range specific within a recurrence identifier matches an expected recurrence range value.
     *
     * @param recurrenceId The recurrence identifier to check
     * @param expectedRange The expected recurrence range, or <code>null</code> to ensure no range is set
     * @return The recurrence identifier, after its range parameter was checked
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RECURRENCE_ID}
     */
    public static RecurrenceId recurrenceRangeMatches(RecurrenceId recurrenceId, RecurrenceRange expectedRange) throws OXException {
        if (null == expectedRange && null != recurrenceId.getRange() || null != expectedRange && false == expectedRange.equals(recurrenceId.getRange())) {
            throw CalendarExceptionCodes.INVALID_RECURRENCE_ID.create(new Exception("Expected range " + expectedRange), recurrenceId.getValue(), "");
        }
        return recurrenceId;
    }

}
