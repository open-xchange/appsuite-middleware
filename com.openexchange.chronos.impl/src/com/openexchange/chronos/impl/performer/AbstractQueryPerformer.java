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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.getFields;
import static com.openexchange.chronos.common.CalendarUtils.isAttendee;
import static com.openexchange.chronos.common.CalendarUtils.isOrganizer;
import static com.openexchange.chronos.common.CalendarUtils.matches;
import static com.openexchange.tools.arrays.Arrays.contains;
import static com.openexchange.tools.arrays.Arrays.remove;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.SelfProtectionFactory;
import com.openexchange.chronos.common.SelfProtectionFactory.SelfProtection;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.impl.Consistency;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.quota.Quota;

/**
 * {@link AbstractQueryPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class AbstractQueryPerformer {

    /** <i>Meta</i>-fields of events that are always skipped when applying updated event data */
    protected static final EventField[] SKIPPED_FIELDS = {
        EventField.CREATED, EventField.CREATED_BY, EventField.LAST_MODIFIED, EventField.TIMESTAMP, EventField.MODIFIED_BY, EventField.FLAGS
    };

    protected final CalendarSession session;
    protected final CalendarStorage storage;

    private SelfProtection selfProtection;

    /**
     * Initializes a new {@link AbstractQueryPerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     */
    protected AbstractQueryPerformer(CalendarSession session, CalendarStorage storage) {
        super();
        this.session = session;
        this.storage = storage;
    }

    protected SelfProtection getSelfProtection() {
        if (selfProtection==null){
            LeanConfigurationService leanConfigurationService = Services.getService(LeanConfigurationService.class);
            selfProtection = SelfProtectionFactory.createSelfProtection(leanConfigurationService);
        }
        return selfProtection;
    }

    /**
     * Gets a value indicating whether a specific event is visible for the current session's user, independently of the underlying folder
     * permissions, i.e. only considering if the user participates in the event in any form (organizer, attendees, creator, calendar user).
     *
     * @param event The event to check
     * @return <code>true</code> if the event can be read by the current session's user, <code>false</code>, otherwise
     */
    protected boolean hasReadPermission(Event event) {
        int userId = session.getUserId();
        return matches(event.getCalendarUser(), userId) || matches(event.getCreatedBy(), userId) || isAttendee(event, userId) || isOrganizer(event, userId);
    }

    /**
     * Get the configured quota and the actual usage of the underlying calendar account.
     *
     * @return The quota
     */
    protected Quota getQuota() throws OXException {
        return Utils.getQuota(session, storage);
    }

    /**
     * Sorts a list of events if requested, based on the search options set in the underlying calendar parameters.
     *
     * @param events The events to sort
     * @return The sorted events
     */
    protected List<Event> sortEvents(List<Event> events) throws OXException {
        return CalendarUtils.sortEvents(events, new SearchOptions(session).getSortOrders(), Utils.getTimeZone(session));
    }

    /**
     * Initializes a new event post processor for this performed.
     *
     * @return The event post processor
     */
    protected EventPostProcessor postProcessor() {
        return new EventPostProcessor(session, storage, getSelfProtection());
    }

    /**
     * Initializes a new event post processor, implicitly supplying further data for the calendar user attendee and event flags as needed.
     * <p/>
     * <b>Note:</b> Should only be used if different fields were used before when querying the event storage. In case all attendee data is
     * not explicitly requested, the post-processor is enriched with data for the calendar user attendee implicitly.
     *
     * @param eventIds The identifiers of the events being processed
     * @param calendarUserId The identifier of the underlying calendar user
     * @param requestedFields The fields as requested by the client
     * @param queriedFields The fields loaded from the storage
     * @return The event post processor, enriched with further data as needed
     * @see {@link AbstractQueryPerformer#getFieldsForStorage(EventField[])}
     */
    protected EventPostProcessor postProcessor(String[] eventIds, int calendarUserId, EventField[] requestedFields, EventField[] queriedFields) throws OXException {
        EventPostProcessor postProcessor = postProcessor();
        /*
         * always supply essential data for actual calendar user attendee, unless already requested explicitly
         */
        if (false == contains(queriedFields, EventField.ATTENDEES)) {
            Attendee attendee = new Attendee();
            attendee.setEntity(calendarUserId);
            AttendeeField[] fields = {
                AttendeeField.ENTITY, AttendeeField.CU_TYPE, AttendeeField.FOLDER_ID,
                AttendeeField.PARTSTAT, AttendeeField.HIDDEN, AttendeeField.TIMESTAMP
            };
            postProcessor.setUserAttendeeInfo(storage.getAttendeeStorage().loadAttendee(eventIds, attendee, fields));
        }
        /*
         * supply info for event flag generation as needed
         */
        if (contains(requestedFields, EventField.FLAGS)) {
            if (false == contains(queriedFields, EventField.ATTACHMENTS)) {
                postProcessor.setAttachmentsFlagInfo(storage.getAttachmentStorage().hasAttachments(eventIds));
            }
            if (false == contains(queriedFields, EventField.CONFERENCES)) {
                postProcessor.setConferencesFlagInfo(storage.getConferenceStorage().hasConferences(eventIds));
            }
            if (false == contains(queriedFields, EventField.ALARMS)) {
                postProcessor.setAlarmsFlagInfo(storage.getAlarmTriggerStorage().hasTriggers(calendarUserId, eventIds));
            }
            if (false == contains(queriedFields, EventField.ATTENDEES)) {
                postProcessor.setScheduledFlagInfo(storage.getAttendeeStorage().loadAttendeeCounts(eventIds, null));
            }
        }
        return postProcessor;
    }

    /**
     * Gets the event fields to pass down to the storage in case a subsequent <i>post-processing</i> of the events
     * will take place, based on the supplied calendar parameters.
     * <p/>
     * <b>Note:</b> Only in case the resulting events are <i>post-processed</i>, and information for event flag generation will be
     * fetched separately, a different set of fields may be queried from the storage.
     *
     * @param requestedFields The event fields as requested from the client
     * @return The event fields to hand down to the storage when querying event data
     */
    protected static EventField[] getFieldsForStorage(EventField[] requestedFields) {
        if (null == requestedFields || contains(requestedFields, EventField.ATTENDEES) || false == contains(requestedFields, EventField.FLAGS)) {
            /*
             * all attendees, or no event flags requested, no special handling needed
             */
            return getFields(requestedFields);
        }
        /*
         * event flags are requested, but not all attendees; temporary remove flags field to supply info for event flag generation
         * afterwards, also ensure to include further fields relevant for event flag generation
         */
        return getFields(remove(requestedFields, EventField.FLAGS), EventField.STATUS, EventField.TRANSP);
    }

    /**
     * Prepares a new change exception for a recurring event series.
     *
     * @param originalMasterEvent The original master event
     * @param recurrenceId The recurrence identifier
     * @param objectId The object identifier to take over for the prepared exception
     * @param timestamp The timestamp of the change
     * @return The prepared exception event
     */
    protected Event prepareException(Event originalMasterEvent, RecurrenceId recurrenceId, String objectId, Date timestamp) throws OXException {
        RecurrenceId normalizedRecurrenceId = CalendarUtils.normalizeRecurrenceID(originalMasterEvent.getStartDate(), recurrenceId);
        Event exceptionEvent = EventMapper.getInstance().copy(originalMasterEvent, new Event(), true, (EventField[]) null);
        exceptionEvent.setId(objectId);
        exceptionEvent.setRecurrenceId(normalizedRecurrenceId);
        exceptionEvent.setRecurrenceRule(null);
        exceptionEvent.setDeleteExceptionDates(null);
        exceptionEvent.setChangeExceptionDates(new TreeSet<RecurrenceId>(Collections.singleton(normalizedRecurrenceId)));
        exceptionEvent.setStartDate(CalendarUtils.calculateStart(originalMasterEvent, normalizedRecurrenceId));
        exceptionEvent.setEndDate(CalendarUtils.calculateEnd(originalMasterEvent, normalizedRecurrenceId));
        Consistency.setCreated(timestamp, exceptionEvent, originalMasterEvent.getCreatedBy());
        Consistency.setModified(session, timestamp, exceptionEvent, session.getUserId());
        return exceptionEvent;
    }

}
