/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.impl;

import static com.openexchange.chronos.common.CalendarUtils.contains;
import static com.openexchange.chronos.common.CalendarUtils.extractEMailAddress;
import static com.openexchange.chronos.common.CalendarUtils.isInternal;
import static com.openexchange.chronos.common.CalendarUtils.isPublicClassification;
import static com.openexchange.chronos.common.CalendarUtils.matches;
import static com.openexchange.chronos.impl.Utils.getCalendarUserId;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeePrivileges;
import com.openexchange.chronos.CalendarStrings;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Conference;
import com.openexchange.chronos.DefaultAttendeePrivileges;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.RecurrenceRange;
import com.openexchange.chronos.ResourceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.SelfProtectionFactory.SelfProtection;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.performer.ConflictCheckPerformer;
import com.openexchange.chronos.impl.performer.ResolvePerformer;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.groupware.tools.alias.UserAliasUtility;
import com.openexchange.groupware.tools.mappings.Mapping;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.resource.Resource;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

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
     * Checks that an event can be <i>read</i> by the current user, either based on the user's permissions in the calendar folder
     * representing the actual view on the event, or based on the user participating in the event as organizer or attendee.
     *
     * @param folder The calendar folder the event is read in
     * @param event The event to check
     * @return The event, after the check for sufficient read permissions took place
     * @see Utils#isVisible(CalendarFolder, Event)
     * @throws OXException {@link CalendarExceptionCodes#NO_READ_PERMISSION}
     */
    public static Event eventIsVisible(CalendarFolder folder, Event event) throws OXException {
        if (Utils.isVisible(folder, event)) {
            return event;
        }
        throw CalendarExceptionCodes.NO_READ_PERMISSION.create(folder.getId());
    }

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
    public static void requireCalendarPermission(CalendarFolder folder, int requiredFolderPermission, int requiredReadPermission, int requiredWritePermission, int requiredDeletePermission) throws OXException {
        Permission ownPermission = folder.getOwnPermission();
        if (ownPermission.getFolderPermission() < requiredFolderPermission) {
            throw CalendarExceptionCodes.NO_READ_PERMISSION.create(folder.getId());
        }
        if (ownPermission.getReadPermission() < requiredReadPermission) {
            throw CalendarExceptionCodes.NO_READ_PERMISSION.create(folder.getId());
        }
        if (ownPermission.getWritePermission() < requiredWritePermission) {
            throw CalendarExceptionCodes.NO_WRITE_PERMISSION.create(folder.getId());
        }
        if (ownPermission.getDeletePermission() < requiredDeletePermission) {
            throw CalendarExceptionCodes.NO_DELETE_PERMISSION.create(folder.getId());
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
     * Checks that the incoming update does not contain outdated participant status
     * <p>
     * Note: Due the fact that some clients will receive the timestamp in DateTime format <code>ISO.8601.2004</code>,
     * see {@link <a href="https://tools.ietf.org/html/rfc5545#section-3.3.5">RFC 5545 Section 3.3.5</a>}, the timestamp
     * can only be evaluated in seconds.
     *
     * @param original The original attendee from the DB
     * @param updated The updated attendee
     * @throws OXException {@link CalendarExceptionCodes#CONCURRENT_MODIFICATION}
     */
    public static void requireUpToDateTimestamp(Attendee original, Attendee updated) throws OXException {
        if (original.getTimestamp() > 0 && updated.getTimestamp() > 0
            && original.getTimestamp() > updated.getTimestamp()
            && original.getTimestamp() - updated.getTimestamp() >= 1000) {
            throw CalendarExceptionCodes.CONCURRENT_MODIFICATION.create(I(original.getEntity()), L(updated.getTimestamp()), L(original.getTimestamp()));
        }
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
    public static String eventIsInFolder(Event event, CalendarFolder folder) throws OXException {
        if (false == Utils.isInFolder(event, folder)) {
            throw CalendarExceptionCodes.EVENT_NOT_FOUND_IN_FOLDER.create(folder.getId(), event.getId());
        }
        return folder.getId();
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
     * @param session The calendar session
     * @param event The event to check
     * @see Check#mandatoryFields(Event, EventField...)
     * @throws OXException {@link CalendarExceptionCodes#MANDATORY_FIELD}, {@link CalendarExceptionCodes#END_BEFORE_START}
     */
    public static void startAndEndDate(CalendarSession session, Event event) throws OXException {
        DateTime startDate = event.getStartDate();
        if (null == startDate) {
            String fieldName = StringHelper.valueOf(session.getEntityResolver().getLocale(session.getUserId())).getString(CalendarStrings.FIELD_START_DATE);
            throw CalendarExceptionCodes.MANDATORY_FIELD.create(fieldName);
        }
        DateTime endDate = event.getEndDate();
        if (null == endDate) {
            String fieldName = StringHelper.valueOf(session.getEntityResolver().getLocale(session.getUserId())).getString(CalendarStrings.FIELD_END_DATE);
            throw CalendarExceptionCodes.MANDATORY_FIELD.create(fieldName);
        }
        if (startDate.after(endDate)) {
            throw CalendarExceptionCodes.END_BEFORE_START.create(String.valueOf(startDate), String.valueOf(endDate));
        }
        if (startDate.isAllDay() != endDate.isAllDay()) {
            throw CalendarExceptionCodes.INCOMPATIBLE_DATE_TYPES.create(String.valueOf(startDate), String.valueOf(endDate));
        }
        if (startDate.isFloating() != endDate.isFloating()) {
            throw CalendarExceptionCodes.INCOMPATIBLE_DATE_TYPES.create(String.valueOf(startDate), String.valueOf(endDate));
        }
    }

    /**
     * Checks that the attendee privileges are supported based on the given folder's type and event organizer, if it's not <code>null</code>
     * and different from {@link DefaultAttendeePrivileges#DEFAULT}.
     *
     * @param privileges The attendee privileges to check, or <code>null</code> to skip the check
     * @param folder The target folder for the event
     * @param organizer The event's organizer
     * @return The passed privileges, after they were checked for validity
     * @throws OXException {@link CalendarExceptionCodes#INVALID_DATA}
     */
    public static AttendeePrivileges attendeePrivilegesAreValid(AttendeePrivileges privileges, CalendarFolder folder, Organizer organizer) throws OXException {
        if (null != privileges && DefaultAttendeePrivileges.MODIFY.getValue().equalsIgnoreCase(privileges.getValue())) {
            /*
             * 'modify' privilege only allowed in non-public folders, with internal organizer
             */
            if (PublicType.getInstance().equals(folder.getType())) {
                throw CalendarExceptionCodes.INVALID_DATA.create(EventField.ATTENDEE_PRIVILEGES, "Incompatible folder type");
            }
            if (null != organizer && false == isInternal(organizer, CalendarUserType.INDIVIDUAL)) {
                throw CalendarExceptionCodes.INVALID_DATA.create(EventField.ATTENDEE_PRIVILEGES, "Not allowed for externally organized events");
            }
        }
        return privileges;
    }

    /**
     * Checks that the classification is supported based on the given folder's type and list of attendees, if it is not <code>null</code>
     * and different from {@link Classification#PUBLIC}.
     *
     * @param classification The classification to check, or <code>null</code> to skip the check
     * @param folder The target folder for the event
     * @param attendees The attendees participating in the event
     * @return The passed classification, after it was checked for validity
     * @throws OXException {@link CalendarExceptionCodes#UNSUPPORTED_CLASSIFICATION_FOR_FOLDER}, {@link CalendarExceptionCodes#UNSUPPORTED_CLASSIFICATION_FOR_RESOURCE}
     */
    public static Classification classificationIsValid(Classification classification, CalendarFolder folder, List<Attendee> attendees) throws OXException {
        if (null != classification && false == Classification.PUBLIC.equals(classification)) {
            if (false == PrivateType.getInstance().equals(folder.getType())) {
                throw CalendarExceptionCodes.UNSUPPORTED_CLASSIFICATION_FOR_FOLDER.create(classification, folder.getId(), folder.getType());
            }
            if (Classification.PRIVATE.equals(classification)) {
                List<Attendee> resourceAttendees = CalendarUtils.filter(attendees, Boolean.TRUE, CalendarUserType.RESOURCE, CalendarUserType.ROOM);
                if (0 < resourceAttendees.size()) {
                    throw CalendarExceptionCodes.UNSUPPORTED_CLASSIFICATION_FOR_RESOURCE.create(classification, resourceAttendees.get(0));
                }
            }
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
    public static Classification classificationIsValidOnMove(Classification classification, CalendarFolder folder, CalendarFolder targetFolder) throws OXException {
        if (null != classification && false == Classification.PUBLIC.equals(classification)) {
            if (PublicType.getInstance().equals(targetFolder.getType()) || getCalendarUserId(folder) != getCalendarUserId(targetFolder)) {
                throw CalendarExceptionCodes.UNSUPPORTED_CLASSIFICATION_FOR_MOVE.create(
                    String.valueOf(classification), folder.getId(), folder.getType(), targetFolder.getId(), targetFolder.getType());
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
    public static void classificationAllowsUpdate(CalendarFolder folder, Event originalEvent) throws OXException {
        if (false == isPublicClassification(originalEvent)) {
            int userID = folder.getSession().getUserId();
            if (false == matches(originalEvent.getCreatedBy(), userID) && false == contains(originalEvent.getAttendees(), userID)) {
                throw CalendarExceptionCodes.RESTRICTED_BY_CLASSIFICATION.create(folder.getId(), originalEvent.getId(), String.valueOf(originalEvent.getClassification()));
            }
        }
    }

    /**
     * Checks that the supplied event's unique identifier (UID) is not already used for another event within the same context.
     *
     * @param session The calendar session
     * @param storage A reference to the calendar storage
     * @param event The event to check
     * @return The passed event's unique identifier, after it was checked for uniqueness
     * @throws OXException {@link CalendarExceptionCodes#UID_CONFLICT}
     */
    public static String uidIsUnique(CalendarSession session, CalendarStorage storage, Event event) throws OXException {
        String uid = event.getUid();
        if (Strings.isNotEmpty(uid)) {
            String existingId = new ResolvePerformer(session, storage).resolveByUid(uid);
            if (null != existingId) {
                throw CalendarExceptionCodes.UID_CONFLICT.create(uid, existingId);
            }
        }
        return uid;
    }

    /**
     * Checks that the supplied event's unique identifier (UID) is not already used for another event within the scope of a specific
     * calendar user, i.e. the unique identifier is resolved to events residing in the user's <i>personal</i>, as well as <i>public</i>
     * calendar folders.
     *
     * @param session The calendar session
     * @param storage A reference to the calendar storage
     * @param event The event to check
     * @param calendarUserId The identifier of the calendar user the unique identifier should be resolved in
     * @return The passed event's unique identifier, after it was checked for uniqueness
     * @throws OXException {@link CalendarExceptionCodes#UID_CONFLICT}
     */
    public static String uidIsUnique(CalendarSession session, CalendarStorage storage, Event event, int calendarUserId) throws OXException {
        String uid = event.getUid();
        if (Strings.isNotEmpty(uid)) {
            EventID existingId = new ResolvePerformer(session, storage).resolveByUid(uid, calendarUserId);
            if (null != existingId) {
                throw CalendarExceptionCodes.UID_CONFLICT.create(uid, existingId.getObjectID());
            }
        }
        return uid;
    }

    /**
     * Checks that a specific unique identifier (UID) is not already used for another event within the same context.
     *
     * @param session The calendar session
     * @param storage A reference to the calendar storage
     * @param uid The unique identifier to check
     * @return The passed unique identifier, after it was checked for uniqueness
     * @throws OXException {@link CalendarExceptionCodes#UID_CONFLICT}
     */
    public static String uidIsUnique(CalendarSession session, CalendarStorage storage, String uid) throws OXException {
        String existingId = new ResolvePerformer(session, storage).resolveByUid(uid);
        if (null != existingId) {
            throw CalendarExceptionCodes.UID_CONFLICT.create(uid, existingId);
        }
        return uid;
    }

    /**
     * Checks that the unique identifier (UID) matches in all events within the supplied collection, i.e. it is either undefined, or
     * equal in all events.
     *
     * @param events The events to check the UID for equality
     * @return The event's common unique identifier, after it was checked to be equal in all events, or <code>null</code> if not assigned
     * @throws OXException {@link CalendarExceptionCodes#INVALID_DATA}
     */
    public static String uidMatches(Collection<Event> events) throws OXException {
        return Check.fieldMatches(events, EventField.UID, String.class);
    }

    /**
     * Checks that the filename property matches in all events within the supplied collection, i.e. it is either undefined, or
     * equal in all events.
     *
     * @param events The events to check the filename for equality
     * @return The event's common filename, after it was checked to be equal in all events, or <code>null</code> if not assigned
     * @throws OXException {@link CalendarExceptionCodes#INVALID_DATA}
     */
    public static String filenameMatches(Collection<Event> events) throws OXException {
        return Check.fieldMatches(events, EventField.FILENAME, String.class);
    }

    /**
     * Checks that a specific property matches in all events within the supplied collection, i.e. it is either undefined, or equal in
     * all events.
     *
     * @param events The events to check the property for equality
     * @param field The event field denoting the property to check
     * @return The property value, after it was checked to be equal in all events, or <code>null</code> if not assigned
     * @throws OXException {@link CalendarExceptionCodes#INVALID_DATA}
     */
    private static <T> T fieldMatches(Collection<Event> events, EventField field, Class<T> clazz) throws OXException {
        if (null == events || events.isEmpty()) {
            return null;
        }
        Iterator<Event> iterator = events.iterator();
        Mapping<? extends Object, Event> mapping = EventMapper.getInstance().get(field);
        Event firstEvent = iterator.next();
        while (iterator.hasNext()) {
            Event nextEvent = iterator.next();
            if (false == mapping.equals(firstEvent, nextEvent)) {
                throw CalendarExceptionCodes.INVALID_DATA.create(field, "Mismatching values: \"" + mapping.get(firstEvent) + "\" vs. \"" + mapping.get(nextEvent) + '"');
            }
        }
        return clazz.cast(mapping.get(firstEvent));
    }

    /**
     * Checks that the organizer matches in all events from a calendar object resource, i.e. it is either undefined, or equal in all
     * events.
     *
     * @param event The primary event to check the organizer in, or <code>null</code> to just check the further events
     * @param events Further events to check the organizer for equality, or <code>null</code> to just check the first event
     * @return The event's common organizer, after it was checked to be equal in all events, or <code>null</code> if not assigned
     * @throws OXException {@link CalendarExceptionCodes#DIFFERENT_ORGANIZER}
     * @see CalendarUtils#matches(CalendarUser, CalendarUser)
     */
    public static Organizer organizerMatches(Event event, List<Event> events) throws OXException {
        Organizer organizer = null != event ? event.getOrganizer() : null != events && 0 < events.size() ? events.get(0).getOrganizer() : null;
        if (null != events) {
            for (Event e : events) {
                if (false == CalendarUtils.matches(organizer, e.getOrganizer())) {
                    String id = null != event ? event.getId() : 0 < events.size() ? events.get(0).getId() : null;
                    throw CalendarExceptionCodes.DIFFERENT_ORGANIZER.create(id, organizer, e.getOrganizer());
                }
            }
        }
        return organizer;
    }

    /**
     * Checks that all attachments referenced by the supplied attachment collection are visible for the current session owner.
     *
     * @param session The calendar session
     * @param storage The calendar storage
     * @param attachments The attachments to check
     * @return The passed attachments, after each one was checked successfully
     * @throws OXException {@link CalendarExceptionCodes#ATTACHMENT_NOT_FOUND}
     */
    public static List<Attachment> attachmentsAreVisible(CalendarSession session, CalendarStorage storage, List<Attachment> attachments) throws OXException {
        if (null != attachments) {
            for (Attachment attachment : attachments) {
                if (0 < attachment.getManagedId()) {
                    Event event = null;
                    try {
                        String eventId = storage.getAttachmentStorage().resolveAttachmentId(attachment.getManagedId());
                        if (null != eventId) {
                            event = new ResolvePerformer(session, storage).resolveById(eventId, null);
                        }
                    } catch (OXException e) {
                        throw CalendarExceptionCodes.ATTACHMENT_NOT_FOUND.create(e, I(attachment.getManagedId()), null, null);
                    }
                    if (null == event || null == CalendarUtils.findAttachment(event.getAttachments(), attachment.getManagedId())) {
                        throw CalendarExceptionCodes.ATTACHMENT_NOT_FOUND.create(I(attachment.getManagedId()), null, null);
                    }
                }
            }
        }
        return attachments;
    }

    /**
     * Checks that a specific attachment is contained in one of the supplied events, based on the attachment's managed identifier.
     *
     * @param events The events to search
     * @param managedId The managed identifier to lookup
     * @return The matching attachment
     * @throws OXException {@link CalendarExceptionCodes#ATTACHMENT_NOT_FOUND}
     * @see CalendarUtils#findAttachment(Collection, int)
     */
    public static Attachment containsAttachment(Collection<Event> events, int managedId) throws OXException {
        Attachment attachment = CalendarUtils.findAttachment(events, managedId);
        if (null == attachment) {
            throw CalendarExceptionCodes.ATTACHMENT_NOT_FOUND.create(I(managedId), null, null);
        }
        return attachment;
    }

    /**
     * Checks that a particular attendee exists in an event.
     *
     * @param event The event to check
     * @param attendee The attendee to lookup
     * @return The successfully looked up attendee
     * @see CalendarUtils#find(Collection, CalendarUser)
     * @throws OXException {@link CalendarExceptionCodes#ATTENDEE_NOT_FOUND}
     */
    public static Attendee attendeeExists(Event event, Attendee attendee) throws OXException {
        Attendee matchingAttendee = CalendarUtils.find(event.getAttendees(), attendee);
        if (null == matchingAttendee) {
            throw CalendarExceptionCodes.ATTENDEE_NOT_FOUND.create(I(attendee.getEntity()), event.getId());
        }
        return matchingAttendee;
    }

    /**
     * Checks that the event's organizer is also contained in the list of attendees, in case it is an <i>internal</i> user.
     *
     * @param event The event to check
     * @param folder
     * @throws OXException {@link CalendarExceptionCodes#MISSING_ORGANIZER}
     */
    public static void internalOrganizerIsAttendee(Event event, CalendarFolder folder) throws OXException {
        Organizer organizer = event.getOrganizer();
        if (PublicType.getInstance().equals(folder.getType())) {
            return;
        }
        if (null != organizer && CalendarUtils.isInternal(organizer, CalendarUserType.INDIVIDUAL) && false == contains(event.getAttendees(), organizer)) {
            throw CalendarExceptionCodes.MISSING_ORGANIZER.create();
        }
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
     * Checks that the maximum number of conferences is not exceeded prior inserting or updating the conference collection of an event.
     *
     * @param selfProtection A reference to the self-protection helper
     * @param conferences The conferences to check
     * @return The passed conferences, after they were checked
     * @throws OXException {@link CalendarExceptionCodes#TOO_MANY_CONFERENCES}
     * @see <a href="https://tools.ietf.org/html/rfc4791#section-5.2.9">RFC 4791, section 5.2.9</a>
     */
    public static List<Conference> maxConferences(SelfProtection selfProtection, List<Conference> conferences) throws OXException {
        if (null != conferences) {
            selfProtection.checkConferenceCollection(conferences);
        }
        return conferences;
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
            throw CalendarExceptionCodes.INVALID_RECURRENCE_ID.create(new Exception("Expected range " + expectedRange), recurrenceId.getValue(), null);
        }
        return recurrenceId;
    }

    /**
     * Checks that a specific resource identifier exists.
     *
     * @param entityResolver The entity resolver to use for checking
     * @param resourceId The resource identifier to check
     * @return The resource identifier, after it was checked for existence
     * @throws OXException {@link CalendarExceptionCodes#INVALID_CALENDAR_USER}
     */
    public static ResourceId exists(EntityResolver entityResolver, ResourceId resourceId) throws OXException {
        /*
         * implcitly check existence by applying internal entity data
         */
        CalendarUser calendarUser = new CalendarUser();
        calendarUser.setEntity(resourceId.getEntity());
        calendarUser.setUri(resourceId.getURI());
        entityResolver.applyEntityData(calendarUser, resourceId.getCalendarUserType());
        return resourceId;
    }

    /**
     * Checks that a calendar user address URI matches a specific user, i.e. it either matches the user's resource identifier, or
     * references one of the user's e-mail addresses.
     *
     * @param uri The calendar user address string to check
     * @param contextId The context identifier
     * @param user The internal user to match against
     * @return The passed calendar address, after it was checked to match the referenced user
     * @throws OXException {@link CalendarExceptionCodes#INVALID_CALENDAR_USER}
     */
    public static String calendarAddressMatches(String uri, int contextId, User user) throws OXException {
        if (null == uri) {
            throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(uri, I(user.getId()), CalendarUserType.INDIVIDUAL);
        }
        ResourceId resourceId = ResourceId.parse(uri);
        if (null != resourceId && resourceId.getContextID() == contextId && resourceId.getEntity() == user.getId()) {
            /*
             * resource id address matches referenced user
             */
            return uri;
        }
        Set<String> aliases = new HashSet<String>();
        if (null != user.getMail()) {
            aliases.add(user.getMail());
        }
        if (null != user.getAliases()) {
            aliases.addAll(Arrays.asList(user.getAliases()));
        }
        if (UserAliasUtility.isAlias(extractEMailAddress(uri), aliases)) {
            /*
             * e-mail address matches referenced user
             */
            return uri;
        }
        /*
         * mismatch, otherwise
         */
        throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(uri, I(user.getId()), CalendarUserType.INDIVIDUAL);
    }

    /**
     * Checks that a calendar user address URI matches a specific resource, i.e. it either matches the resource's resource identifier, or
     * references the resource's e-mail addresses.
     *
     * @param uri The calendar user address string to check
     * @param contextId The context identifier
     * @param resource The internal resource to match against
     * @return The passed calendar address, after it was checked to match the referenced user
     * @throws OXException {@link CalendarExceptionCodes#INVALID_CALENDAR_USER}
     */
    public static String calendarAddressMatches(String uri, int contextId, Resource resource) throws OXException {
        if (null == uri) {
            throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(uri, I(resource.getIdentifier()), CalendarUserType.INDIVIDUAL);
        }
        ResourceId resourceId = ResourceId.parse(uri);
        if (null != resourceId && resourceId.getContextID() == contextId && resourceId.getEntity() == resource.getIdentifier()) {
            /*
             * resource id address matches referenced resource
             */
            return uri;
        }
        String mailAddress = extractEMailAddress(uri);
        if (Strings.isNotEmpty(mailAddress) && mailAddress.equalsIgnoreCase(resource.getMail())) {
            /*
             * e-mail address matches referenced resource
             */
            return uri;
        }
        /*
         * mismatch, otherwise
         */
        throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(uri, I(resource.getIdentifier()), CalendarUserType.INDIVIDUAL);
    }

}
