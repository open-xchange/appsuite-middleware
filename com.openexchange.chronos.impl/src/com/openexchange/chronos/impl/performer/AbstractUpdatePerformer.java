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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.impl.Utils.getCalendarUserId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.DelegatingEvent;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.AlarmMapper;
import com.openexchange.chronos.impl.AttendeeMapper;
import com.openexchange.chronos.impl.CalendarResultImpl;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.Consistency;
import com.openexchange.chronos.impl.CreateResultImpl;
import com.openexchange.chronos.impl.DeleteResultImpl;
import com.openexchange.chronos.impl.EventMapper;
import com.openexchange.chronos.impl.UpdateResultImpl;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.Strings;

/**
 * {@link AbstractUpdatePerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class AbstractUpdatePerformer {

    /** The event fields that are preserved for reference in <i>tombstone</i> events */
    private static final EventField[] EVENT_TOMBSTONE_FIELDS = {
        EventField.CHANGE_EXCEPTION_DATES, EventField.CLASSIFICATION, EventField.CREATED, EventField.CREATED_BY,
        EventField.DELETE_EXCEPTION_DATES, EventField.END_DATE, EventField.ID, EventField.LAST_MODIFIED,
        EventField.MODIFIED_BY, EventField.CALENDAR_USER, EventField.FOLDER_ID, EventField.SERIES_ID, EventField.RECURRENCE_RULE,
        EventField.SEQUENCE, EventField.START_DATE, EventField.TRANSP, EventField.UID, EventField.FILENAME, EventField.SEQUENCE
    };

    /** The attendee fields that are preserved for reference in <i>tombstone</i> attendees */
    private static final AttendeeField[] ATTENDEE_TOMBSTONE_FIELDS = {
        AttendeeField.CU_TYPE, AttendeeField.ENTITY, AttendeeField.FOLDER_ID, AttendeeField.MEMBER, AttendeeField.PARTSTAT, AttendeeField.URI
    };

    protected final CalendarSession session;
    protected final CalendarStorage storage;
    protected final int calendarUserId;
    protected final UserizedFolder folder;
    protected final Date timestamp;
    protected final CalendarResultImpl result;

    /**
     * Initializes a new {@link AbstractUpdatePerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    protected AbstractUpdatePerformer(CalendarStorage storage, CalendarSession session, UserizedFolder folder) throws OXException {
        super();
        this.folder = folder;
        this.calendarUserId = getCalendarUserId(folder);
        this.session = session;
        this.timestamp = new Date();
        this.storage = storage;
        this.result = new CalendarResultImpl(session, calendarUserId, folder.getID()).applyTimestamp(timestamp);
    }

    /**
     * Prepares a new change exception for a recurring event series.
     *
     * @param originalMasterEvent The original master event
     * @param recurrenceID The recurrence identifier
     * @return The prepared exception event
     */
    protected Event prepareException(Event originalMasterEvent, RecurrenceId recurrenceID) throws OXException {
        Event exceptionEvent = EventMapper.getInstance().copy(originalMasterEvent, new Event(), true, (EventField[]) null);
        exceptionEvent.setId(storage.getEventStorage().nextId());
        exceptionEvent.setRecurrenceId(recurrenceID);
        exceptionEvent.setChangeExceptionDates(new TreeSet<RecurrenceId>(Collections.singleton(recurrenceID)));
        exceptionEvent.setDeleteExceptionDates(null);
        exceptionEvent.setStartDate(CalendarUtils.calculateStart(originalMasterEvent, recurrenceID));
        exceptionEvent.setEndDate(CalendarUtils.calculateEnd(originalMasterEvent, recurrenceID));
        Consistency.setCreated(timestamp, exceptionEvent, originalMasterEvent.getCreatedBy());
        Consistency.setModified(timestamp, exceptionEvent, session.getUserId());
        return exceptionEvent;
    }

    /**
     * Generates a <i>tombstone</i> event object based on the supplied event, as used to track the deletion in the storage.
     *
     * @param event The event to create the <i>tombstone</i> for
     * @param lastModified The last modification time to take over
     * @param modifiedBy The identifier of the modifying user to take over
     * @return The <i>tombstone</i> event
     */
    protected Event getTombstone(Event event, Date lastModified, int modifiedBy) throws OXException {
        Event tombstone = EventMapper.getInstance().copy(event, new Event(), true, EVENT_TOMBSTONE_FIELDS);
        Consistency.setModified(lastModified, tombstone, modifiedBy);
        return tombstone;
    }

    /**
     * Generates <i>tombstone</i> attendee objects based on the supplied attendees, as used to track the deletion in the storage.
     *
     * @param attendees The attendees to create the <i>tombstone</i> for
     * @return The <i>tombstone</i> attendees
     */
    protected List<Attendee> getTombstones(List<Attendee> attendees) throws OXException {
        if (null == attendees) {
            return null;
        }
        List<Attendee> tombstoneAttendees = new ArrayList<Attendee>(attendees.size());
        for (Attendee attendee : attendees) {
            tombstoneAttendees.add(getTombstone(attendee));
        }
        return tombstoneAttendees;
    }

    /**
     * Generates a <i>tombstone</i> attendee object based on the supplied attendee, as used to track the deletion in the storage.
     *
     * @param attendee The attendee to create the <i>tombstone</i> for
     * @return The <i>tombstone</i> attendee
     */
    protected Attendee getTombstone(Attendee attendee) throws OXException {
        return AttendeeMapper.getInstance().copy(attendee, null, ATTENDEE_TOMBSTONE_FIELDS);
    }

    /**
     * <i>Touches</i> an event in the storage by setting it's last modification timestamp and modified-by property to the current
     * timestamp and calendar user.
     *
     * @param id The identifier of the event to <i>touch</i>
     */
    protected void touch(String id) throws OXException {
        Event eventUpdate = new Event();
        eventUpdate.setId(id);
        Consistency.setModified(timestamp, eventUpdate, session.getUserId());
        storage.getEventStorage().updateEvent(eventUpdate);
    }

    /**
     * Adds a specific recurrence identifier to the series master's change exception array and updates the series master event in the
     * storage. Also, an appropriate update result is added.
     *
     * @param originalMasterEvent The original series master event
     * @param recurrenceID The recurrence identifier of the occurrence to add
     */
    protected void addChangeExceptionDate(Event originalMasterEvent, RecurrenceId recurrenceID) throws OXException {
        SortedSet<RecurrenceId> changeExceptionDates = new TreeSet<RecurrenceId>();
        if (null != originalMasterEvent.getChangeExceptionDates()) {
            changeExceptionDates.addAll(originalMasterEvent.getChangeExceptionDates());
        }
        if (false == changeExceptionDates.add(recurrenceID)) {
            // TODO throw/log?
        }
        Event eventUpdate = new Event();
        eventUpdate.setId(originalMasterEvent.getId());
        eventUpdate.setChangeExceptionDates(changeExceptionDates);
        Consistency.setModified(timestamp, eventUpdate, calendarUserId);
        storage.getEventStorage().updateEvent(eventUpdate);
        result.addUpdate(new UpdateResultImpl(originalMasterEvent, loadEventData(originalMasterEvent.getId())));
    }

    /**
     * Deletes a single event from the storage. This can be used for any kind of event, i.e. a single, non-recurring event, an existing
     * exception of an event series, or an event series. For the latter one, any existing event exceptions are deleted as well.
     * <p/>
     * The event's attendees are loaded on demand if not yet present in the passed <code>originalEvent</code> {@code originalEvent}.
     * <p/>
     * The deletion includes:
     * <ul>
     * <li>insertion of a <i>tombstone</i> record for the original event</li>
     * <li>insertion of <i>tombstone</i> records for the original event's attendees</li>
     * <li>deletion of any alarms associated with the event</li>
     * <li>deletion of any attachments associated with the event</li>
     * <li>deletion of the event</li>
     * <li>deletion of the event's attendees</li>
     * </ul>
     *
     * @param originalEvent The original event to delete
     */
    protected void delete(Event originalEvent) throws OXException {
        /*
         * recursively delete any existing event exceptions
         */
        if (isSeriesMaster(originalEvent)) {
            deleteExceptions(originalEvent.getSeriesId(), originalEvent.getChangeExceptionDates());
        }
        /*
         * delete event data from storage
         */
        String id = originalEvent.getId();
        storage.getEventStorage().insertEventTombstone(getTombstone(originalEvent, timestamp, calendarUserId));
        storage.getAttendeeStorage().insertAttendeeTombstones(id, getTombstones(originalEvent.getAttendees()));
        storage.getAlarmStorage().deleteAlarms(id);
        storage.getAttachmentStorage().deleteAttachments(session.getSession(), folder.getID(), id, originalEvent.getAttachments());
        storage.getEventStorage().deleteEvent(id);
        storage.getAttendeeStorage().deleteAttendees(id, originalEvent.getAttendees());
        result.addDeletion(new DeleteResultImpl(originalEvent));
    }

    /**
     * Deletes a specific internal user attendee from a single event from the storage. This can be used for any kind of event, i.e. a
     * single, non-recurring event, an existing exception of an event series, or an event series. For the latter one, the attendee is deleted from
     * any existing event exceptions as well.
     * <p/>
     * The deletion includes:
     * <ul>
     * <li>insertion of a <i>tombstone</i> record for the original event</li>
     * <li>insertion of <i>tombstone</i> records for the original attendee</li>
     * <li>deletion of any alarms of the attendee associated with the event</li>
     * <li>deletion of the attendee from the event</li>
     * <li>update of the last-modification timestamp of the original event</li>
     * </ul>
     *
     * @param originalEvent The original event to delete
     * @param originalAttendee The original attendee to delete
     */
    protected void delete(Event originalEvent, Attendee originalAttendee) throws OXException {
        /*
         * recursively delete any existing event exceptions for this attendee
         */
        int userID = originalAttendee.getEntity();
        if (isSeriesMaster(originalEvent)) {
            deleteExceptions(originalEvent.getSeriesId(), originalEvent.getChangeExceptionDates(), userID);
        }
        /*
         * delete event data from storage for this attendee
         */
        String objectID = originalEvent.getId();
        storage.getEventStorage().insertEventTombstone(getTombstone(originalEvent, timestamp, calendarUserId));
        storage.getAttendeeStorage().insertAttendeeTombstone(objectID, originalAttendee);
        storage.getAttendeeStorage().deleteAttendees(objectID, Collections.singletonList(originalAttendee));
        storage.getAlarmStorage().deleteAlarms(objectID, userID);
        /*
         * 'touch' event & add track update result
         */
        touch(objectID);
        result.addUpdate(new UpdateResultImpl(originalEvent, loadEventData(objectID)));
    }

    /**
     * Deletes change exception events from the storage.
     * <p/>
     * For each change exception, the data is removed by invoking {@link #delete(Event)} for the exception.
     *
     * @param seriesID The series identifier
     * @param exceptionDates The recurrence identifiers of the change exceptions to delete
     */
    protected void deleteExceptions(String seriesID, Collection<RecurrenceId> exceptionDates) throws OXException {
        for (Event originalExceptionEvent : loadExceptionData(seriesID, exceptionDates)) {
            delete(originalExceptionEvent);
        }
    }

    /**
     * Deletes a specific internal user attendee from change exception events from the storage.
     * <p/>
     * For each change exception, the data is removed by invoking {@link #delete(Event, Attendee)} for the exception, in case the
     * user is found the exception's attendee list.
     *
     * @param seriesID The series identifier
     * @param exceptionDates The recurrence identifiers of the change exceptions to delete
     * @param userID The identifier of the user attendee to delete
     */
    protected void deleteExceptions(String seriesID, Collection<RecurrenceId> exceptionDates, int userID) throws OXException {
        for (Event originalExceptionEvent : loadExceptionData(seriesID, exceptionDates)) {
            Attendee originalUserAttendee = find(originalExceptionEvent.getAttendees(), userID);
            if (null != originalUserAttendee) {
                delete(originalExceptionEvent, originalUserAttendee);
            }
        }
    }

    /**
     * Deletes a specific internal user attendee from a specific occurrence of a series event that does not yet exist as change exception.
     * This includes the creation of the corresponding change exception, and the removal of the user attendee from this exception's
     * attendee list.
     *
     * @param originalMasterEvent The original series master event
     * @param recurrenceID The recurrence identifier of the occurrence to remove the attendee for
     * @param originalAttendee The original attendee to delete from the recurrence
     */
    protected void deleteFromRecurrence(Event originalMasterEvent, RecurrenceId recurrenceID, Attendee originalAttendee) throws OXException {
        /*
         * create new exception event
         */
        Event exceptionEvent = prepareException(originalMasterEvent, recurrenceID);
        storage.getEventStorage().insertEvent(exceptionEvent);
        /*
         * take over all other original attendees
         */
        List<Attendee> excpetionAttendees = new ArrayList<Attendee>(originalMasterEvent.getAttendees());
        excpetionAttendees.remove(originalAttendee);
        storage.getAttendeeStorage().insertAttendees(exceptionEvent.getId(), excpetionAttendees);
        /*
         * take over all other original alarms
         */
        for (Entry<Integer, List<Alarm>> entry : storage.getAlarmStorage().loadAlarms(originalMasterEvent).entrySet()) {
            int userID = entry.getKey().intValue();
            if (userID != originalAttendee.getEntity()) {
                insertAlarms(exceptionEvent, userID, entry.getValue(), true);
            }
        }
        /*
         * take over all original attachments
         */
        storage.getAttachmentStorage().insertAttachments(session.getSession(), folder.getID(), exceptionEvent.getId(), originalMasterEvent.getAttachments());
        result.addCreation(new CreateResultImpl(loadEventData(exceptionEvent.getId())));
        /*
         * track new change exception date in master
         */
        addChangeExceptionDate(originalMasterEvent, recurrenceID);
    }

    /**
     * Inserts alarm data for an event of a specific user, optionally assigning new alarm UIDs in case the alarms are copied over from
     * another event. A new unique alarm identifier is always assigned, and the event is passed from the calendar user's folder view to the
     * storage implicitly (based on {@link Utils#getFolderView}.
     *
     * @param event The event the alarms are associated with
     * @param userId The identifier of the user the alarms should be inserted for
     * @param alarms The alarms to insert
     * @param forceNewUids <code>true</code> if new UIDs should be assigned even if already set in the supplied alarms, <code>false</code>, otherwise
     */
    protected void insertAlarms(Event event, int userId, List<Alarm> alarms, boolean forceNewUids) throws OXException {
        if (null == alarms || 0 == alarms.size()) {
            return;
        }
        List<Alarm> newAlarms = new ArrayList<Alarm>(alarms.size());
        for (Alarm alarm : alarms) {
            Alarm newAlarm = AlarmMapper.getInstance().copy(alarm, null, (AlarmField[]) null);
            newAlarm.setId(storage.getAlarmStorage().nextId());
            if (forceNewUids || false == newAlarm.containsUid() || Strings.isEmpty(newAlarm.getUid())) {
                newAlarm.setUid(UUID.randomUUID().toString());
            }
            newAlarms.add(newAlarm);
        }
        final String folderView = Utils.getFolderView(storage, event, userId);
        if (false == folderView.equals(event.getFolderId())) {
            event = new DelegatingEvent(event) {

                @Override
                public String getFolderId() {
                    return folderView;
                }
            };
        }
        storage.getAlarmStorage().insertAlarms(event, userId, newAlarms);
    }

    /**
     * Loads all data for a specific event, including attendees, attachments and alarms.
     * <p/>
     * The parent folder identifier is set based on {@link AbstractUpdatePerformer#folder}
     *
     * @param id The identifier of the event to load
     * @return The event data
     * @throws OXException {@link CalendarExceptionCodes#EVENT_NOT_FOUND}
     */
    protected Event loadEventData(String id) throws OXException {
        return loadEventData(id, true);
    }

    /**
     * Loads all data for a specific event, including attendees, attachments and alarms.
     * <p/>
     * The parent folder identifier is optionally set based on {@link AbstractUpdatePerformer#folder}
     *
     * @param id The identifier of the event to load
     * @param applyFolderId <code>true</code> to take over the parent folder identifier representing the view on the event, <code>false</code>, otherwise
     * @return The event data
     * @throws OXException {@link CalendarExceptionCodes#EVENT_NOT_FOUND}
     */
    protected Event loadEventData(String id, boolean applyFolderId) throws OXException {
        Event event = storage.getEventStorage().loadEvent(id, null);
        if (null == event) {
            throw CalendarExceptionCodes.EVENT_NOT_FOUND.create(id);
        }
        event = Utils.loadAdditionalEventData(storage, calendarUserId, event, EventField.values());
        if (applyFolderId) {
            event.setFolderId(folder.getID());
        }
        return event;
    }

    protected List<Event> loadExceptionData(String seriesID, Collection<RecurrenceId> recurrenceIDs) throws OXException {
        List<Event> exceptions = new ArrayList<Event>();
        if (null != recurrenceIDs && 0 < recurrenceIDs.size()) {
            for (RecurrenceId recurrenceID : recurrenceIDs) {
                Event exception = storage.getEventStorage().loadException(seriesID, recurrenceID, null);
                if (null == exception) {
                    throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(seriesID, String.valueOf(recurrenceID));
                }
                exception.setFolderId(folder.getID());
                exceptions.add(exception);
            }
        }
        return Utils.loadAdditionalEventData(storage, false, calendarUserId, exceptions, EventField.values());
    }

    protected Event loadExceptionData(String seriesID, RecurrenceId recurrenceID) throws OXException {
        Event exception = storage.getEventStorage().loadException(seriesID, recurrenceID, null);
        if (null == exception) {
            throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(seriesID, String.valueOf(recurrenceID));
        }
        exception = Utils.loadAdditionalEventData(storage, calendarUserId, exception, EventField.values());
        exception.setFolderId(folder.getID());
        return exception;
    }

    /**
     * Gets the identifier of a specific user's default personal calendar folder.
     *
     * @param userID The identifier of the user to retrieve the default calendar identifier for
     * @return The default calendar folder identifier
     */
    protected String getDefaultCalendarID(int userID) throws OXException {
        return session.getConfig().getDefaultFolderID(userID);
    }

    /**
     * Prepares the organizer for an event, taking over an external organizer if specified.
     *
     * @param organizerData The organizer as defined by the client, or <code>null</code> to prepare the current calendar user as organizer
     * @return The prepared organizer
     */
    protected Organizer prepareOrganizer(Organizer organizerData) throws OXException {
        Organizer organizer;
        if (null != organizerData) {
            organizer = session.getEntityResolver().prepare(organizerData, CalendarUserType.INDIVIDUAL);
            if (0 < organizer.getEntity()) {
                /*
                 * internal organizer must match the actual calendar user if specified
                 */
                if (organizer.getEntity() != calendarUserId) {
                    throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(organizer.getUri(), Autoboxing.I(organizer.getEntity()), CalendarUserType.INDIVIDUAL);
                }
            } else {
                /*
                 * take over external organizer as-is
                 */
                return session.getConfig().isSkipExternalAttendeeURIChecks() ? organizer : Check.requireValidEMail(organizer);
            }
        } else {
            /*
             * prepare a default organizer for calendar user
             */
            organizer = session.getEntityResolver().applyEntityData(new Organizer(), calendarUserId);
        }
        /*
         * apply "sent-by" property if someone is acting on behalf of the calendar user
         */
        if (null != organizer && calendarUserId != session.getUserId()) {
            organizer.setSentBy(session.getEntityResolver().applyEntityData(new CalendarUser(), session.getUserId()));
        }
        return organizer;
    }

}
