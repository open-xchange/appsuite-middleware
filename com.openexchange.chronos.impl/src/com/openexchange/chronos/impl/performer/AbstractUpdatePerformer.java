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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.AlarmUtils.filterRelativeTriggers;
import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.getAlarmIDs;
import static com.openexchange.chronos.common.CalendarUtils.getExceptionDates;
import static com.openexchange.chronos.common.CalendarUtils.getFolderView;
import static com.openexchange.chronos.common.CalendarUtils.isGroupScheduled;
import static com.openexchange.chronos.common.CalendarUtils.isLastUserAttendee;
import static com.openexchange.chronos.common.CalendarUtils.isOrganizer;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.matches;
import static com.openexchange.chronos.common.SearchUtils.getSearchTerm;
import static com.openexchange.chronos.impl.Check.classificationAllowsUpdate;
import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Utils.getCalendarUser;
import static com.openexchange.folderstorage.Permission.DELETE_ALL_OBJECTS;
import static com.openexchange.folderstorage.Permission.DELETE_OWN_OBJECTS;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.READ_ALL_OBJECTS;
import static com.openexchange.folderstorage.Permission.READ_FOLDER;
import static com.openexchange.folderstorage.Permission.READ_OWN_OBJECTS;
import static com.openexchange.folderstorage.Permission.WRITE_ALL_OBJECTS;
import static com.openexchange.folderstorage.Permission.WRITE_OWN_OBJECTS;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.DelegatingEvent;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.UnmodifiableEvent;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.common.mapping.AlarmMapper;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.Consistency;
import com.openexchange.chronos.impl.Role;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.java.Strings;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link AbstractUpdatePerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class AbstractUpdatePerformer extends AbstractQueryPerformer {

    protected final CalendarUser calendarUser;
    protected final int calendarUserId;
    protected final CalendarFolder folder;
    protected final Date timestamp;

    protected final ResultTracker resultTracker;
    protected EnumSet<Role> roles;

    /**
     * Initializes a new {@link AbstractUpdatePerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     * @param roles The {@link Role}s a user acts as.
     */
    protected AbstractUpdatePerformer(CalendarStorage storage, CalendarSession session, CalendarFolder folder, EnumSet<Role> roles) throws OXException {
        super(session, storage);
        this.folder = folder;
        this.calendarUser = getCalendarUser(session, folder);
        this.calendarUserId = calendarUser.getEntity();
        this.timestamp = new Date();
        this.resultTracker = new ResultTracker(storage, session, folder, timestamp.getTime(), getSelfProtection());
        this.roles = roles;
    }

    /**
     * Initializes a new {@link AbstractUpdatePerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    protected AbstractUpdatePerformer(CalendarStorage storage, CalendarSession session, CalendarFolder folder) throws OXException {
        this(storage, session, folder, EnumSet.noneOf(Role.class));
    }

    /**
     * Initializes a new {@link AbstractUpdatePerformer}, taking over the settings from another update performer.
     *
     * @param updatePerformer The update performer to take over the settings from
     */
    protected AbstractUpdatePerformer(AbstractUpdatePerformer updatePerformer) {
        super(updatePerformer.session, updatePerformer.storage);
        this.folder = updatePerformer.folder;
        this.calendarUser = updatePerformer.calendarUser;
        this.calendarUserId = updatePerformer.calendarUserId;
        this.timestamp = updatePerformer.timestamp;
        this.resultTracker = updatePerformer.resultTracker;
        this.roles = updatePerformer.roles;
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
        exceptionEvent.setRecurrenceRule(null);
        exceptionEvent.setDeleteExceptionDates(null);
        exceptionEvent.setChangeExceptionDates(new TreeSet<RecurrenceId>(Collections.singleton(recurrenceID)));
        exceptionEvent.setStartDate(CalendarUtils.calculateStart(originalMasterEvent, recurrenceID));
        exceptionEvent.setEndDate(CalendarUtils.calculateEnd(originalMasterEvent, recurrenceID));
        Consistency.setCreated(timestamp, exceptionEvent, originalMasterEvent.getCreatedBy());
        Consistency.setModified(session, timestamp, exceptionEvent, session.getUserId());
        return exceptionEvent;
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
        Consistency.setModified(session, timestamp, eventUpdate, session.getUserId());
        storage.getEventStorage().updateEvent(eventUpdate);
    }

    /**
     * Adds a specific recurrence identifier to the series master's change exception array and updates the series master event in the
     * storage.
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
        Consistency.setModified(session, timestamp, eventUpdate, session.getUserId());
        storage.getEventStorage().updateEvent(eventUpdate);
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
     * <li>tracking the deletion within the result tracker instance</li>
     * </ul>
     *
     * @param originalEvent The original event to delete
     */
    protected void delete(Event originalEvent) throws OXException {
        /*
         * recursively delete any existing event exceptions
         */
        if (isSeriesMaster(originalEvent)) {
            for (Event changeException : loadExceptionData(originalEvent.getSeriesId())) {
                delete(changeException);
            }
        }
        /*
         * delete event data from storage
         */
        resultTracker.rememberOriginalEvent(originalEvent);
        String id = originalEvent.getId();
        Event tombstone = storage.getUtilities().getTombstone(originalEvent, timestamp, calendarUser);
        tombstone.setAttendees(storage.getUtilities().getTombstones(originalEvent.getAttendees()));
        storage.getEventStorage().insertEventTombstone(tombstone);
        storage.getAttendeeStorage().insertAttendeeTombstones(id, tombstone.getAttendees());
        storage.getAlarmStorage().deleteAlarms(id);
        storage.getAttachmentStorage().deleteAttachments(session.getSession(), folder.getId(), id, originalEvent.getAttachments());
        storage.getEventStorage().deleteEvent(id);
        storage.getAttendeeStorage().deleteAttendees(id);
        storage.getAlarmTriggerStorage().deleteTriggers(id);
        /*
         * track deletion in result
         */
        resultTracker.trackDeletion(originalEvent);
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
        int userId = originalAttendee.getEntity();
        if (isSeriesMaster(originalEvent)) {
            deleteExceptions(originalEvent.getSeriesId(), originalEvent.getChangeExceptionDates(), userId);
        }
        /*
         * delete event data from storage for this attendee
         */
        String id = originalEvent.getId();
        Event tombstone = storage.getUtilities().getTombstone(originalEvent, timestamp, calendarUser);
        tombstone.setAttendees(Collections.singletonList(storage.getUtilities().getTombstone(originalAttendee)));
        storage.getEventStorage().insertEventTombstone(tombstone);
        storage.getAttendeeStorage().insertAttendeeTombstones(id, originalEvent.getAttendees());
        storage.getAttendeeStorage().deleteAttendees(id, Collections.singletonList(originalAttendee));
        storage.getAlarmStorage().deleteAlarms(id, userId);
        /*
         * 'touch' event & track calendar results
         */
        touch(id);
        Event updatedEvent = loadEventData(id);
        resultTracker.trackUpdate(originalEvent, updatedEvent);

        // Update alarm trigger
        Map<Integer, List<Alarm>> alarms = storage.getAlarmStorage().loadAlarms(updatedEvent);
        storage.getAlarmTriggerStorage().deleteTriggers(updatedEvent.getId());
        storage.getAlarmTriggerStorage().insertTriggers(updatedEvent, alarms);
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
     * @param recurrenceId The recurrence identifier of the occurrence to remove the attendee for
     * @param originalAttendee The original attendee to delete from the recurrence
     */
    protected void deleteFromRecurrence(Event originalSeriesMaster, RecurrenceId recurrenceId, Attendee originalAttendee) throws OXException {
        /*
         * prepare & insert a plain exception first, based on the original data from the master, leaving out the removed attendee
         */
        Map<Integer, List<Alarm>> seriesMasterAlarms = storage.getAlarmStorage().loadAlarms(originalSeriesMaster);
        Event newExceptionEvent = prepareException(originalSeriesMaster, recurrenceId);
        Check.quotaNotExceeded(storage, session);
        storage.getEventStorage().insertEvent(newExceptionEvent);
        List<Attendee> attendees = new ArrayList<Attendee>(originalSeriesMaster.getAttendees());
        attendees.remove(originalAttendee);
        storage.getAttendeeStorage().insertAttendees(newExceptionEvent.getId(), attendees);
        storage.getAttachmentStorage().insertAttachments(session.getSession(), folder.getId(), newExceptionEvent.getId(), originalSeriesMaster.getAttachments());
        for (Entry<Integer, List<Alarm>> entry : seriesMasterAlarms.entrySet()) {
            if (originalAttendee.getEntity() != i(entry.getKey())) {
                insertAlarms(newExceptionEvent, i(entry.getKey()), filterRelativeTriggers(entry.getValue()), true);
            }
        }
        /*
         * add change exception date to series master & track results
         */
        Event updatedExceptionEvent = loadEventData(newExceptionEvent.getId());
        resultTracker.trackCreation(updatedExceptionEvent, originalSeriesMaster);
        resultTracker.rememberOriginalEvent(originalSeriesMaster);
        addChangeExceptionDate(originalSeriesMaster, recurrenceId);
        Event updatedMasterEvent = loadEventData(originalSeriesMaster.getId());
        resultTracker.trackUpdate(originalSeriesMaster, updatedMasterEvent);
        /*
         * reset alarm triggers for series master event and new change exception
         */
        storage.getAlarmTriggerStorage().deleteTriggers(updatedMasterEvent.getId());
        storage.getAlarmTriggerStorage().insertTriggers(updatedMasterEvent, seriesMasterAlarms);
        storage.getAlarmTriggerStorage().deleteTriggers(updatedExceptionEvent.getId());
        storage.getAlarmTriggerStorage().insertTriggers(updatedExceptionEvent, storage.getAlarmStorage().loadAlarms(updatedExceptionEvent));
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
     * @return The inserted alarms
     */
    protected List<Alarm> insertAlarms(Event event, int userId, List<Alarm> alarms, boolean forceNewUids) throws OXException {
        if (null == alarms || 0 == alarms.size()) {
            return Collections.emptyList();
        }
        return insertAlarms(event, Collections.singletonMap(I(userId), alarms), forceNewUids).get(I(userId));
    }

    /**
     * Inserts alarm data for an event of a specific user, optionally assigning new alarm UIDs in case the alarms are copied over from
     * another event. A new unique alarm identifier is always assigned, and the event is passed from the calendar user's folder view to the
     * storage implicitly (based on {@link Utils#getFolderView}.
     *
     * @param event The event the alarms are associated with
     * @param alarmsByUserId The alarms to insert, mapped to the corresponding user identifier
     * @param alarms The alarms to insert
     * @param forceNewUids <code>true</code> if new UIDs should be assigned even if already set in the supplied alarms, <code>false</code>, otherwise
     * @return The inserted alarms, mapped to the corresponding user identifier
     */
    protected Map<Integer, List<Alarm>> insertAlarms(Event event, Map<Integer, List<Alarm>> alarmsByUserId, boolean forceNewUids) throws OXException {
        if (null == alarmsByUserId || 0 == alarmsByUserId.size()) {
            return Collections.emptyMap();
        }
        Map<Integer, List<Alarm>> newAlarmsByUserId = new HashMap<Integer, List<Alarm>>(alarmsByUserId.size());
        for (Entry<Integer, List<Alarm>> entry : alarmsByUserId.entrySet()) {
            List<Alarm> newAlarms = new ArrayList<Alarm>(entry.getValue().size());
            for (Alarm alarm : entry.getValue()) {
                Alarm newAlarm = AlarmMapper.getInstance().copy(alarm, null, (AlarmField[]) null);
                newAlarm.setId(storage.getAlarmStorage().nextId());
                if (forceNewUids || false == newAlarm.containsUid() || Strings.isEmpty(newAlarm.getUid())) {
                    newAlarm.setUid(UUID.randomUUID().toString());
                }
                newAlarms.add(newAlarm);
            }
            String folderView = getFolderView(event, i(entry.getKey()));
            if (false == folderView.equals(event.getFolderId())) {
                event = new DelegatingEvent(event) {

                    @Override
                    public String getFolderId() {
                        return folderView;
                    }

                    @Override
                    public boolean containsFolderId() {
                        return true;
                    }
                };
            }
            storage.getAlarmStorage().insertAlarms(event, i(entry.getKey()), newAlarms);
            newAlarmsByUserId.put(entry.getKey(), newAlarms);
        }
        return newAlarmsByUserId;
    }

    /**
     * Updates a calendar user's alarms for a specific event.
     *
     * @param event The event to update the alarms in
     * @param userId The identifier of the calendar user whose alarms are updated
     * @param originalAlarms The original alarms, or <code>null</code> if there are none
     * @param updatedAlarms The updated alarms
     * @return <code>true</code> if there were any updates, <code>false</code>, otherwise
     */
    protected boolean updateAlarms(Event event, int userId, List<Alarm> originalAlarms, List<Alarm> updatedAlarms) throws OXException {
        CollectionUpdate<Alarm, AlarmField> alarmUpdates = AlarmUtils.getAlarmUpdates(originalAlarms, updatedAlarms);
        if (alarmUpdates.isEmpty()) {
            return false;
        }
        requireWritePermissions(event, Collections.singletonList(session.getEntityResolver().prepareUserAttendee(userId)));
        /*
         * delete removed alarms
         */
        List<Alarm> removedItems = alarmUpdates.getRemovedItems();
        if (0 < removedItems.size()) {
            storage.getAlarmStorage().deleteAlarms(event.getId(), userId, getAlarmIDs(removedItems));
        }
        /*
         * save updated alarms
         */
        List<? extends ItemUpdate<Alarm, AlarmField>> updatedItems = alarmUpdates.getUpdatedItems();
        if (0 < updatedItems.size()) {
            List<Alarm> alarms = new ArrayList<Alarm>(updatedItems.size());
            for (ItemUpdate<Alarm, AlarmField> itemUpdate : updatedItems) {
                Alarm alarm = AlarmMapper.getInstance().copy(itemUpdate.getOriginal(), null, (AlarmField[]) null);
                AlarmMapper.getInstance().copy(itemUpdate.getUpdate(), alarm, AlarmField.values());
                alarm.setId(itemUpdate.getOriginal().getId());
                alarm.setUid(itemUpdate.getOriginal().getUid());
                alarms.add(Check.alarmIsValid(alarm));
            }
            final String folderView = getFolderView(event, userId);
            if (false == folderView.equals(event.getFolderId())) {
                Event userizedEvent = new DelegatingEvent(event) {

                    @Override
                    public String getFolderId() {
                        return folderView;
                    }

                    @Override
                    public boolean containsFolderId() {
                        return true;
                    }
                };
                storage.getAlarmStorage().updateAlarms(userizedEvent, userId, alarms);
            } else {
                storage.getAlarmStorage().updateAlarms(event, userId, alarms);
            }
        }
        /*
         * insert new alarms
         */
        insertAlarms(event, userId, alarmUpdates.getAddedItems(), false);
        Map<Integer, List<Alarm>> loadAlarms = storage.getAlarmStorage().loadAlarms(event);
        storage.getAlarmTriggerStorage().deleteTriggers(event.getId());
        storage.getAlarmTriggerStorage().insertTriggers(event, loadAlarms);

        return true;
    }

    /**
     * Loads all non user-specific data for a specific event, including attendees and attachments.
     * <p/>
     * No <i>userization</i> of the event is performed and no alarm data is fetched for a specific attendee, i.e. only the plain/vanilla
     * event data is loaded from the storage.
     *
     * @param id The identifier of the event to load
     * @return The event data
     * @throws OXException {@link CalendarExceptionCodes#EVENT_NOT_FOUND}
     */
    protected Event loadEventData(String id) throws OXException {
        Event event = storage.getEventStorage().loadEvent(id, null);
        if (null == event) {
            throw CalendarExceptionCodes.EVENT_NOT_FOUND.create(id);
        }
        return new UnmodifiableEvent(storage.getUtilities().loadAdditionalEventData(-1, event, null));
    }

    /**
     * Loads all non user-specific data for multiple events, including attendees and attachments.
     * <p/>
     * No <i>userization</i> of the events is performed and no alarm data is fetched for a specific attendee, i.e. only the plain/vanilla
     * event data is loaded from the storage.
     *
     * @param ids The identifiers of the event to load
     * @return The event data
     * @throws OXException {@link CalendarExceptionCodes#EVENT_NOT_FOUND}
     */
    protected List<Event> loadEventData(List<String> ids) throws OXException {
        if (null == ids || 0 == ids.size()) {
            return Collections.emptyList();
        }
        if (1 == ids.size()) {
            return Collections.singletonList(loadEventData(ids.get(0)));
        }
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.OR);
        for (String id : ids) {
            searchTerm.addSearchTerm(getSearchTerm(EventField.ID, SingleOperation.EQUALS, id));
        }

        List<Event> foundEvents = storage.getEventStorage().searchEvents(searchTerm, null, null);
        foundEvents = storage.getUtilities().loadAdditionalEventData(-1, foundEvents, null);
        List<Event> events = new ArrayList<Event>(ids.size());
        for (String id : ids) {
            Event event = find(foundEvents, id);
            if (null == event) {
                throw CalendarExceptionCodes.EVENT_NOT_FOUND.create(id);
            }
            events.add(new UnmodifiableEvent(event));
        }
        return events;
    }

    /**
     * Loads all non user-specific data for a all exceptions of an event series, including attendees and attachments.
     * <p/>
     * No <i>userization</i> of the exception events is performed and no alarm data is fetched for a specific attendee, i.e. only the
     * plain/vanilla event data is loaded from the storage.
     *
     * @param seriesId The identifier of the event series to load the exceptions from
     * @return The event exception data
     */
    protected List<Event> loadExceptionData(String seriesId) throws OXException {
        List<Event> exceptions = storage.getEventStorage().loadExceptions(seriesId, null);
        if (0 < exceptions.size()) {
            exceptions = storage.getUtilities().loadAdditionalEventData(-1, exceptions, null);
            List<Event> unmodifiableExceptions = new ArrayList<Event>(exceptions.size());
            for (Event exception : exceptions) {
                unmodifiableExceptions.add(new UnmodifiableEvent(exception));
            }
            exceptions = unmodifiableExceptions;
        }
        return exceptions;
    }

    /**
     * Loads all non user-specific data for a collection of exceptions of an event series, including attendees and attachments.
     * <p/>
     * No <i>userization</i> of the exception events is performed and no alarm data is fetched for a specific attendee, i.e. only the
     * plain/vanilla event data is loaded from the storage.
     *
     * @param seriesId The identifier of the event series to load the exceptions from
     * @param recurrenceIds The recurrence identifiers of the exceptions to load
     * @return The event exception data
     * @throws OXException {@link CalendarExceptionCodes#EVENT_RECURRENCE_NOT_FOUND}
     */
    protected List<Event> loadExceptionData(String seriesId, Collection<RecurrenceId> recurrenceIds) throws OXException {
        List<Event> exceptions = new ArrayList<Event>();
        if (null != recurrenceIds && 0 < recurrenceIds.size()) {
            for (RecurrenceId recurrenceId : recurrenceIds) {
                Event exception = storage.getEventStorage().loadException(seriesId, recurrenceId, null);
                if (null == exception) {
                    throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(seriesId, String.valueOf(recurrenceId));
                }
                exceptions.add(exception);
            }
        }
        return storage.getUtilities().loadAdditionalEventData(-1, exceptions, null);
    }

    /**
     * Loads all non user-specific data for a specific exception of an event series, including attendees and attachments.
     * <p/>
     * No <i>userization</i> of the exception event is performed and no alarm data is fetched for a specific attendee, i.e. only the
     * plain/vanilla event data is loaded from the storage.
     *
     * @param seriesId The identifier of the event series to load the exception from
     * @param recurrenceId The recurrence identifier of the exception to load
     * @return The event exception data
     * @throws OXException {@link CalendarExceptionCodes#EVENT_RECURRENCE_NOT_FOUND}
     */
    protected Event loadExceptionData(String seriesId, RecurrenceId recurrenceId) throws OXException {
        Event changeException = optExceptionData(seriesId, recurrenceId);
        if (null == changeException) {
            throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(seriesId, recurrenceId);
        }
        return changeException;
    }

    /**
     * Optionally loads all non user-specific data for a specific exception of an event series, including attendees and attachments.
     * <p/>
     * No <i>userization</i> of the exception event is performed and no alarm data is fetched for a specific attendee, i.e. only the
     * plain/vanilla event data is loaded from the storage.
     *
     * @param seriesId The identifier of the event series to load the exception from
     * @param recurrenceId The recurrence identifier of the exception to load
     * @return The event exception data, or <code>null</code> if not found
     */
    protected Event optExceptionData(String seriesId, RecurrenceId recurrenceId) throws OXException {
        Event changeException = storage.getEventStorage().loadException(seriesId, recurrenceId, null);
        if (null != changeException) {
            changeException = storage.getUtilities().loadAdditionalEventData(-1, changeException, null);
        }
        return changeException;
    }

    /**
     * Checks that the current session's user is able to delete a specific event, by either requiring delete access for <i>own</i> or
     * <i>all</i> objects, based on the user being the creator of the event or not.
     * <p/>
     * Additionally, the event's classification is checked.
     *
     * @param originalEvent The event to check the user's delete permissions for
     * @throws OXException {@link CalendarExceptionCodes#NO_DELETE_PERMISSION}, {@link CalendarExceptionCodes#RESTRICTED_BY_CLASSIFICATION}
     * @see Check#requireCalendarPermission
     * @see Check#classificationAllowsUpdate
     */
    protected void requireDeletePermissions(Event originalEvent) throws OXException {
        if (roles.contains(Role.ORGANIZER)) {
            return;
        }
        if (matches(originalEvent.getCreatedBy(), session.getUserId())) {
            requireCalendarPermission(folder, READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, DELETE_OWN_OBJECTS);
        } else {
            requireCalendarPermission(folder, READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, DELETE_ALL_OBJECTS);
        }
        classificationAllowsUpdate(folder, originalEvent);
    }

    /**
     * Checks that a specific event can be updated by the current session's user under the perspective of the current folder, by either
     * requiring write access for <i>own</i> or <i>all</i> objects, based on the user being the creator of the event or not.
     * <p/>
     * Additionally, the event's classification is checked.
     *
     * @param originalEvent The original event being updated
     * @throws OXException {@link CalendarExceptionCodes#UNSUPPORTED_FOLDER}, {@link CalendarExceptionCodes#NO_READ_PERMISSION},
     *             {@link CalendarExceptionCodes#NO_WRITE_PERMISSION}, {@link CalendarExceptionCodes#NOT_ORGANIZER},
     *             {@link CalendarExceptionCodes#RESTRICTED_BY_CLASSIFICATION}
     */
    protected void requireWritePermissions(Event originalEvent) throws OXException {
        requireWritePermissions(originalEvent, false);
    }

    /**
     * Checks that a specific event can be updated by the current session's user under the perspective of the current folder, by either
     * requiring write access for <i>own</i> or <i>all</i> objects, based on the user being the creator of the event or not.
     * <p/>
     * Additionally, the event's classification is checked.
     *
     * @param originalEvent The original event being updated
     * @param assumeExternalOrganizerUpdate <code>true</code> if an external organizer update can be assumed, <code>false</code>, otherwise
     * @throws OXException {@link CalendarExceptionCodes#UNSUPPORTED_FOLDER}, {@link CalendarExceptionCodes#NO_READ_PERMISSION},
     *             {@link CalendarExceptionCodes#NO_WRITE_PERMISSION}, {@link CalendarExceptionCodes#NOT_ORGANIZER},
     *             {@link CalendarExceptionCodes#RESTRICTED_BY_CLASSIFICATION}
     */
    protected void requireWritePermissions(Event originalEvent, boolean assumeExternalOrganizerUpdate) throws OXException {
        if (roles.contains(Role.ORGANIZER)) {
            return;
        }
        if (matches(originalEvent.getCreatedBy(), session.getUserId())) {
            requireCalendarPermission(folder, READ_FOLDER, READ_OWN_OBJECTS, WRITE_OWN_OBJECTS, NO_PERMISSIONS);
        } else {
            requireCalendarPermission(folder, READ_FOLDER, READ_ALL_OBJECTS, WRITE_ALL_OBJECTS, NO_PERMISSIONS);
        }
        Check.classificationAllowsUpdate(folder, originalEvent);
        if (isGroupScheduled(originalEvent) && false == isOrganizer(originalEvent, calendarUserId) && false == assumeExternalOrganizerUpdate) {
            /*
             * not allowed attendee change in group scheduled resource, throw error if configured
             */
            OXException e = CalendarExceptionCodes.NOT_ORGANIZER.create(
                folder.getId(), originalEvent.getId(), originalEvent.getOrganizer().getUri(), originalEvent.getOrganizer().getSentBy());
            if (PublicType.getInstance().equals(folder.getType())) {
                if (session.getConfig().isRestrictAllowedAttendeeChangesPublic()) {
                    throw e;
                }
            } else if (session.getConfig().isRestrictAllowedAttendeeChanges()) {
                throw e;
            }
            session.addWarning(e);
        }
    }

    /**
     * Checks that data of a specific attendee of an event can be updated by the current session's user under the perspective of the
     * current folder.
     *
     * @param originalEvent The original event being updated
     * @param updatedAttendee The attendee whose data is updated
     * @throws OXException {@link CalendarExceptionCodes#UNSUPPORTED_FOLDER}, {@link CalendarExceptionCodes#NO_READ_PERMISSION},
     *             {@link CalendarExceptionCodes#NO_WRITE_PERMISSION}, {@link CalendarExceptionCodes#NOT_ORGANIZER},
     *             {@link CalendarExceptionCodes#RESTRICTED_BY_CLASSIFICATION}
     */
    protected void requireWritePermissions(Event originalEvent, Attendee updatedAttendee) throws OXException {
        requireWritePermissions(originalEvent, updatedAttendee, false);
    }

    /**
     * Checks that data of a specific attendee of an event can be updated by the current session's user under the perspective of the
     * current folder.
     *
     * @param originalEvent The original event being updated
     * @param updatedAttendee The attendee whose data is updated
     * @param assumeExternalOrganizerUpdate <code>true</code> if an external organizer update can be assumed, <code>false</code>, otherwise
     * @throws OXException {@link CalendarExceptionCodes#UNSUPPORTED_FOLDER}, {@link CalendarExceptionCodes#NO_READ_PERMISSION},
     *             {@link CalendarExceptionCodes#NO_WRITE_PERMISSION}, {@link CalendarExceptionCodes#NOT_ORGANIZER},
     *             {@link CalendarExceptionCodes#RESTRICTED_BY_CLASSIFICATION}
     */
    protected void requireWritePermissions(Event originalEvent, Attendee updatedAttendee, boolean assumeExternalOrganizerUpdate) throws OXException {
        if (false == matches(updatedAttendee, calendarUserId)) {
            /*
             * always require permissions for whole event in case an attendee different from the calendar user updated
             */
            requireWritePermissions(originalEvent, assumeExternalOrganizerUpdate);
        }
        if (session.getUserId() != calendarUserId) {
            /*
             * user acts on behalf of other calendar user, allow attendee update based on permissions in underlying folder
             */
            if (matches(originalEvent.getCreatedBy(), session.getUserId())) {
                requireCalendarPermission(folder, READ_FOLDER, READ_OWN_OBJECTS, WRITE_OWN_OBJECTS, NO_PERMISSIONS);
            } else {
                requireCalendarPermission(folder, READ_FOLDER, READ_ALL_OBJECTS, WRITE_ALL_OBJECTS, NO_PERMISSIONS);
            }
            Check.classificationAllowsUpdate(folder, originalEvent);
        }
    }

    /**
     * Checks that data of one or more attendees of an event can be updated by the current session's user under the perspective of the
     * current folder.
     *
     * @param originalEvent The original event being updated
     * @param updatedAttendees The attendees whose data is updated
     * @throws OXException {@link CalendarExceptionCodes#UNSUPPORTED_FOLDER}, {@link CalendarExceptionCodes#NO_READ_PERMISSION},
     *             {@link CalendarExceptionCodes#NO_WRITE_PERMISSION}, {@link CalendarExceptionCodes#NOT_ORGANIZER},
     *             {@link CalendarExceptionCodes#RESTRICTED_BY_CLASSIFICATION}
     */
    protected void requireWritePermissions(Event originalEvent, List<Attendee> updatedAttendees) throws OXException {
        requireWritePermissions(originalEvent, updatedAttendees, false);
    }

    /**
     * Checks that data of one or more attendees of an event can be updated by the current session's user under the perspective of the
     * current folder.
     *
     * @param originalEvent The original event being updated
     * @param updatedAttendees The attendees whose data is updated
     * @param assumeExternalOrganizerUpdate <code>true</code> if an external organizer update can be assumed, <code>false</code>, otherwise
     * @throws OXException {@link CalendarExceptionCodes#UNSUPPORTED_FOLDER}, {@link CalendarExceptionCodes#NO_READ_PERMISSION},
     *             {@link CalendarExceptionCodes#NO_WRITE_PERMISSION}, {@link CalendarExceptionCodes#NOT_ORGANIZER},
     *             {@link CalendarExceptionCodes#RESTRICTED_BY_CLASSIFICATION}
     */
    protected void requireWritePermissions(Event originalEvent, List<Attendee> updatedAttendees, boolean assumeExternalOrganizerUpdate) throws OXException {
        for (Attendee updatedAttendee : updatedAttendees) {
            requireWritePermissions(originalEvent, updatedAttendee, assumeExternalOrganizerUpdate);
        }
    }

    /**
     * Gets a value indicating whether a delete operation performed in the current folder from the calendar user's perspective would lead
     * to a <i>real</i> deletion of the event from the storage, or if only the calendar user is removed from the attendee list, hence
     * rather an update is performed.
     * <p/>
     * A deletion leads to a complete removal if
     * <ul>
     * <li>the event is located in a <i>public folder</i></li>
     * <li>or the event is not <i>group-scheduled</i></li>
     * <li>or the calendar user is the organizer of the event</li>
     * <li>or the calendar user is the last internal user attendee in the event</li>
     * </ul>
     *
     * @param originalEvent The original event to check
     * @return <code>true</code> if a deletion would lead to a removal of the event, <code>false</code>, otherwise
     */
    protected boolean deleteRemovesEvent(Event originalEvent) {
        return PublicType.getInstance().equals(folder.getType()) ||
            false == isGroupScheduled(originalEvent) ||
            isOrganizer(originalEvent, calendarUserId) ||
            isLastUserAttendee(originalEvent.getAttendees(), calendarUserId)
        ;
    }

    /**
     * Gets a value indicating whether an event series has further occurrences besides the occurrences identified by the supplied
     * recurrence identifiers or not.
     *
     * @param seriesMaster The series master event
     * @param recurrenceIds The recurrence identifiers to skip, or <code>null</code> to just check the series itself
     * @return <code>true</code> if the event's recurrence set yields at least one further occurrence, <code>false</code>, otherwise
     */
    protected boolean hasFurtherOccurrences(Event seriesMaster, SortedSet<RecurrenceId> recurrenceIds) throws OXException {
        RecurrenceData recurrenceData = new DefaultRecurrenceData(
            seriesMaster.getRecurrenceRule(), seriesMaster.getStartDate(), getExceptionDates(recurrenceIds), getExceptionDates(seriesMaster.getRecurrenceDates()));
        try {
            return session.getRecurrenceService().iterateRecurrenceIds(recurrenceData).hasNext();
        } catch (OXException e) {
            if ("CAL-4061".equals(e.getErrorCode())) {
                // "Invalid recurrence id [id ..., rule ...]", so outside recurrence set
                return false;
            }
            throw e;
        }
    }

}
