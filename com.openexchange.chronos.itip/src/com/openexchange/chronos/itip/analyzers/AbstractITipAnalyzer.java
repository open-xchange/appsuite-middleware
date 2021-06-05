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

package com.openexchange.chronos.itip.analyzers;

import static com.openexchange.chronos.common.CalendarUtils.getOccurrence;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.Consistency;
import com.openexchange.chronos.common.DefaultCalendarObjectResource;
import com.openexchange.chronos.common.IncomingCalendarObjectResource;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.itip.ITipAnalysis;
import com.openexchange.chronos.itip.ITipAnalyzer;
import com.openexchange.chronos.itip.ITipChange;
import com.openexchange.chronos.itip.ITipChange.Type;
import com.openexchange.chronos.itip.ITipExceptions;
import com.openexchange.chronos.itip.ITipIntegrationUtility;
import com.openexchange.chronos.itip.ITipMessage;
import com.openexchange.chronos.itip.ITipMethod;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.generators.ArgumentType;
import com.openexchange.chronos.itip.generators.HTMLWrapper;
import com.openexchange.chronos.itip.generators.PassthroughWrapper;
import com.openexchange.chronos.itip.generators.Sentence;
import com.openexchange.chronos.itip.generators.TypeWrapper;
import com.openexchange.chronos.itip.osgi.Services;
import com.openexchange.chronos.itip.tools.ITipEventUpdate;
import com.openexchange.chronos.scheduling.changes.Description;
import com.openexchange.chronos.scheduling.changes.DescriptionService;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tools.mappings.common.AbstractSimpleCollectionUpdate;
import com.openexchange.groupware.tools.mappings.common.CollectionUpdate;
import com.openexchange.groupware.tools.mappings.common.ItemUpdate;
import com.openexchange.regional.RegionalSettings;
import com.openexchange.regional.RegionalSettingsService;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link AbstractITipAnalyzer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractITipAnalyzer implements ITipAnalyzer {

    public static final EventField[] SKIP = new EventField[] { EventField.FOLDER_ID, EventField.ID, EventField.SERIES_ID, EventField.CREATED_BY, EventField.CREATED, EventField.TIMESTAMP, EventField.LAST_MODIFIED, EventField.MODIFIED_BY, EventField.SEQUENCE,
        EventField.ALARMS, EventField.FLAGS, EventField.ATTENDEE_PRIVILEGES };
    
    private final static Logger LOG = LoggerFactory.getLogger(AbstractITipAnalyzer.class);

    protected ITipIntegrationUtility util;

    @Override
    public ITipAnalysis analyze(final ITipMessage message, Map<String, String> header, final String style, final CalendarSession session) throws OXException {

        final ContextService contexts = Services.getService(ContextService.class);
        final UserService users = Services.getService(UserService.class);

        final Context ctx = contexts.getContext(session.getContextId());
        final User user = users.getUser(session.getUserId(), ctx);

        if (null == header) {
            return analyze(message, new HashMap<String, String>(), wrapperFor(style), user.getLocale(), user, ctx, session);
        }
        return analyze(message, lowercase(header), wrapperFor(style), user.getLocale(), user, ctx, session);
    }

    private Map<String, String> lowercase(final Map<String, String> header) {
        final Map<String, String> copy = new HashMap<String, String>();
        for (final Map.Entry<String, String> entry : header.entrySet()) {
            copy.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        return copy;
    }

    protected abstract ITipAnalysis analyze(ITipMessage message, Map<String, String> header, TypeWrapper wrapper, Locale locale, User user, Context ctx, CalendarSession session) throws OXException;

    public AbstractITipAnalyzer(final ITipIntegrationUtility util) {
        this.util = util;
    }

    protected TypeWrapper wrapperFor(final String style) {
        TypeWrapper w = new PassthroughWrapper();
        if (style != null && style.equalsIgnoreCase("html")) {
            w = new HTMLWrapper();
        }
        return w;
    }

    public void describeDiff(final ITipChange change, final TypeWrapper wrapper, final CalendarSession session, ITipMessage message) throws OXException {
        final ContextService contexts = Services.getService(ContextService.class);
        final UserService users = Services.getService(UserService.class);
        RegionalSettingsService regionalSettingsService = Services.getService(RegionalSettingsService.class);

        final Context ctx = contexts.getContext(session.getContextId());
        final User user = users.getUser(session.getUserId(), ctx);
        RegionalSettings regionalSettings = regionalSettingsService.get(session.getContextId(), session.getUserId());

        switch (change.getType()) {
            case CREATE:
                createIntro(change, wrapper, user.getLocale());
                break;
            case UPDATE:
                updateIntro(change, wrapper, user.getLocale(), message);
                break;
            case CREATE_DELETE_EXCEPTION:
            case DELETE:
                deleteIntro(change, wrapper, user.getLocale());
                break;
            default:
                // Nothing to do
                break;
        }

        final Event currentEvent = change.getCurrentEvent();
        final Event newEvent = change.getNewEvent();

        if (currentEvent == null || newEvent == null) {
            change.setDiffDescription(new ArrayList<String>());
            return;
        }
        
        final List<String> descriptions = new LinkedList<String>();
        DescriptionService descriptionService = Services.getOptionalService(DescriptionService.class);
        if (null != descriptionService) {
            List<Description> descs;
            if (ITipMethod.REPLY.equals(message.getMethod())) {
                descs = descriptionService.describeOnly(change.getDiff(), EventField.ATTENDEES);
            } else {
                descs = descriptionService.describe(change.getDiff());
            }
            for (Description desc : descs) {
                for (com.openexchange.chronos.scheduling.changes.Sentence sentence : desc.getSentences()) {
                    descriptions.add(sentence.getMessage(wrapper.getFormat(), user.getLocale(), TimeZone.getTimeZone(user.getTimeZone()), regionalSettings));
                }
            }
        }

        change.setDiffDescription(descriptions);

        // Now let's choose an introduction sentence
        switch (change.getType()) {
            case CREATE:
                if (!change.isException()) {
                    createIntro(change, wrapper, user.getLocale());
                    break;
                } // $FALL-THROUGH$ Else Fall Through, creating change exceptions is more similar to updates
            case UPDATE:
                updateIntro(change, wrapper, user.getLocale(), message);
                break;
            case CREATE_DELETE_EXCEPTION:
            case DELETE:
                deleteIntro(change, wrapper, user.getLocale());
                break;
            default:
                // Nothing to do
                break;
        }
    }

    private void deleteIntro(final ITipChange change, final TypeWrapper wrapper, final Locale locale) {
        final String displayName = displayNameForOrganizer(change.getDeletedEvent());
        change.setIntroduction(new Sentence(Messages.DELETE_INTRO).add(displayName, ArgumentType.PARTICIPANT).getMessage(wrapper, locale));

    }

    private void updateIntro(final ITipChange change, final TypeWrapper wrapper, final Locale locale, ITipMessage message) throws OXException {
        String displayName = displayNameForOrganizer(change.getCurrentEvent());
        if (onlyStateChanged(change.getDiff())) {
            // External Participant

            ParticipationStatus newStatus = null;

            outer: for (ItemUpdate<Attendee, AttendeeField> attendeeUpdate : change.getDiff().getAttendeeUpdates().getUpdatedItems()) {
                for (AttendeeField attendeeField : attendeeUpdate.getUpdatedFields()) {
                    if (attendeeField.equals(AttendeeField.PARTSTAT)) {
                        newStatus = attendeeUpdate.getUpdate().getPartStat();
                        displayName = attendeeUpdate.getOriginal().getCn() == null ? attendeeUpdate.getOriginal().getEMail() : attendeeUpdate.getOriginal().getCn();
                        break outer;
                    }
                }
            }

            if (ParticipationStatus.ACCEPTED.equals(newStatus) || ParticipationStatus.TENTATIVE.equals(newStatus) || ParticipationStatus.DECLINED.equals(newStatus)) {
                change.setIntroduction(new Sentence(Messages.STATUS_CHANGED_INTRO).add(displayName, ArgumentType.PARTICIPANT).add("", ArgumentType.STATUS, newStatus).getMessage(wrapper, locale));
            }
        } else {
            if (message.getMethod() != ITipMethod.COUNTER) {
                change.setIntroduction(new Sentence(Messages.UPDATE_INTRO).add(displayName, ArgumentType.PARTICIPANT).getMessage(wrapper, locale));
            }
        }
    }

    AttendeeField[] ALL_BUT_CONFIRMATION = new AttendeeField[] { AttendeeField.CN, AttendeeField.CU_TYPE, AttendeeField.EMAIL, AttendeeField.ENTITY, AttendeeField.FOLDER_ID, AttendeeField.MEMBER, AttendeeField.ROLE, AttendeeField.RSVP, AttendeeField.SENT_BY, AttendeeField.URI };

    private boolean onlyStateChanged(ITipEventUpdate diff) {
        if (null == diff) {
            return false;
        }
        if (diff.containsAnyChangesBeside(new EventField[] { EventField.ATTENDEES })) {
            return false;
        }

        CollectionUpdate<Attendee, AttendeeField> attendeeUpdates = diff.getAttendeeUpdates();

        if (attendeeUpdates == null || attendeeUpdates.isEmpty()) {
            return true;
        }

        if (attendeeUpdates.getAddedItems() != null && !attendeeUpdates.getAddedItems().isEmpty()) {
            return false;
        }

        if (attendeeUpdates.getRemovedItems() != null && !attendeeUpdates.getRemovedItems().isEmpty()) {
            return false;
        }

        List<? extends ItemUpdate<Attendee, AttendeeField>> updatedItems = attendeeUpdates.getUpdatedItems();
        for (ItemUpdate<Attendee, AttendeeField> attendeeUpdate : updatedItems) {
            if (attendeeUpdate.containsAnyChangeOf(ALL_BUT_CONFIRMATION)) {
                return false;
            }
        }

        return true;
    }

    private void createIntro(final ITipChange change, final TypeWrapper wrapper, final Locale locale) {
        final String displayName = displayNameForOrganizer(change.getNewEvent());
        change.setIntroduction(new Sentence(Messages.CREATE_INTRO).add(displayName, ArgumentType.PARTICIPANT).getMessage(wrapper, locale));
    }

    protected String displayNameForOrganizer(Event event) {
        Organizer organizer;
        if (null == event || (organizer = event.getOrganizer()) == null) {
            return "unknown";
        }

        if (organizer.getCn() != null) {
            return organizer.getCn();
        }

        if (organizer.getEMail() != null) {
            return organizer.getEMail();
        }

        return "unknown";
    }

    protected Event findAndRemoveMatchingException(final Event exception, final List<Event> exceptions) {
        for (Iterator<Event> iterator = exceptions.iterator(); iterator.hasNext();) {
            Event existingException = iterator.next();
            if (existingException.getRecurrenceId().compareTo(exception.getRecurrenceId()) == 0) {
                iterator.remove();
                return existingException;
            }
        }
        return null;
    }

    public boolean doAppointmentsDiffer(final Event update, final Event original, final CalendarSession session) {
        if (original == update) {
            // Can be the same object .. so avoid roundtrip of diff
            return false;
        }
        final ITipEventUpdate diff = new ITipEventUpdate(original, update, true, AbstractITipAnalyzer.SKIP);
        if (diff.getUpdatedFields().isEmpty()) {
            return false;
        }
        /*
         * Check if events do only differ because of the participant status of the user, that has already been updated
         */
        if (diff.isAboutCertainParticipantsStateChangeOnly(String.valueOf(session.getUserId()))) {
            /*
             * Get the updated attendee and check if the status was "reseted" by the incoming event
             */
            ItemUpdate<Attendee, AttendeeField> itemUpdate = diff.getAttendeeUpdates().getUpdatedItems().get(0);
            if (false == ParticipationStatus.NEEDS_ACTION.matches(itemUpdate.getOriginal().getPartStat())//
                && ParticipationStatus.NEEDS_ACTION.matches(itemUpdate.getUpdate().getPartStat())) {
                return false;
            }
        }

        return true;
    }

    public boolean hasConflicts(final ITipAnalysis analysis) {
        for (final ITipChange change : analysis.getChanges()) {
            if (change.getConflicts() != null && !change.getConflicts().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void purgeConflicts(final ITipAnalysis analysis) throws OXException {
        final Map<String, ITipChange> knownEvents = new HashMap<>();
        for (final ITipChange change : analysis.getChanges()) {
            final Event currentAppointment = change.getCurrentEvent();
            if (currentAppointment != null) {
                knownEvents.put(currentAppointment.getId(), change);
            }
            final Event deletedAppointment = change.getDeletedEvent();
            if (deletedAppointment != null) {
                knownEvents.put(deletedAppointment.getId(), change);
            }
        }

        for (final ITipChange change : analysis.getChanges()) {
            final List<EventConflict> conflicts = change.getConflicts();
            if (conflicts == null) {
                continue;
            }
            final Event newEvent = change.getNewEvent();
            if (newEvent == null) {
                continue;
            }
            final Event currentEvent = change.getCurrentEvent();
            final Event masterEvent = change.getMasterEvent();
            for (final Iterator<EventConflict> iterator = conflicts.iterator(); iterator.hasNext();) {
                final EventConflict conflict = iterator.next();
                if (null != conflict.getConflictingEvent()) {
                    continue;
                }
                if (currentEvent != null && currentEvent.getId() != null && (currentEvent.getId().equals(conflict.getConflictingEvent().getId()))) {
                    iterator.remove();
                    continue;
                }
                if (masterEvent != null && masterEvent.getId() != null && (masterEvent.getId().equals(conflict.getConflictingEvent().getId()))) {
                    iterator.remove();
                    continue;
                }
                final ITipChange changeToConflict = knownEvents.get(conflict.getConflictingEvent().getId());
                if (changeToConflict == null) {
                    continue;
                }
                if (changeToConflict.getType() == ITipChange.Type.DELETE) {
                    iterator.remove();
                } else {
                    final Event changedAppointment = changeToConflict.getNewEvent();
                    if (changedAppointment == null) {
                        continue;
                    }
                    if (!overlaps(changedAppointment, newEvent)) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    public boolean overlaps(final Event event1, final Event event2) {
        if (event2.getStartDate().after(event1.getEndDate())) {
            return false;
        }

        if (event1.getStartDate().after(event2.getEndDate())) {
            return false;
        }

        return true;
    }

    public boolean isCreate(final ITipAnalysis analysis) {
        for (final ITipChange change : analysis.getChanges()) {
            if (change.getType() == Type.CREATE) {
                return true;
            }
        }
        return false;
    }

    public boolean rescheduling(final ITipAnalysis analysis) throws OXException {
        for (final ITipChange change : analysis.getChanges()) {
            if (change.getType() == Type.CREATE && change.isException()) {
                final ITipEventUpdate diff = new ITipEventUpdate(change.getCurrentEvent(), change.getNewEvent(), true, (EventField[]) null);
                if (diff.containsAnyChangeOf(new EventField[] { EventField.START_DATE, EventField.END_DATE })) {
                    return true;
                }
                return false;
            }
            if (change.getType() != Type.UPDATE) {
                return true;
            }
            final ITipEventUpdate diff = change.getDiff();
            if (diff != null && diff.containsAnyChangeOf(new EventField[] { EventField.START_DATE, EventField.END_DATE })) {
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to restore (obviously) unchanged attachments in an incoming updated event compared to its stored representation. Matching
     * is performed based on attachment metadata (filesize, format-type, filename).
     * 
     * @param original The original event
     * @param update The updated event
     * @return The updated event, possibly with restored attachments
     */
    protected static Event restoreAttachments(Event original, Event update) {
        if (null == original) {
            /*
             * New event, skip processing
             */
            return update;
        }
        AbstractSimpleCollectionUpdate<Attachment> attachmentUpdates = new AbstractSimpleCollectionUpdate<Attachment>(original.getAttachments(), update.getAttachments()) {

            @Override
            protected boolean matches(Attachment item1, Attachment item2) {
                /*
                 * match via managed id, URI or checksum
                 */
                if (0 < item1.getManagedId() && 0 < item2.getManagedId()) {
                    return item1.getManagedId() == item2.getManagedId();
                }
                if (nonNull(item1.getUri(), item2.getUri())) {
                    return item1.getUri().equals(item2.getUri());
                }
                if (nonNull(item1.getChecksum(), item2.getChecksum())) {
                    return item1.getChecksum().equals(item2.getChecksum());
                }
                /*
                 * match via metadata
                 */
                if (nonNullAwareEquals(item1.getFilename(), item2.getFilename())//
                    && item1.getSize() == item2.getSize()//
                    && nonNullAwareEquals(item1.getFormatType(), item2.getFormatType())) {
                    return true;
                }
                return false;
            }
        };
        List<Attachment> newAttachments = new ArrayList<Attachment>();
        if (null != original.getAttachments()) {
            newAttachments.addAll(original.getAttachments());
        }
        newAttachments.removeAll(attachmentUpdates.getRemovedItems());
        newAttachments.addAll(attachmentUpdates.getAddedItems());
        update.setAttachments(newAttachments);
        return update;
    }

    protected void ensureParticipant(final Event original, final Event event, final CalendarSession session, int owner) throws OXException {
        if (null == CalendarUtils.find(event.getAttendees(), session.getEntityResolver().prepareUserAttendee(owner))) {
            // Owner is a party crasher..
            Attendee attendee = new Attendee();
            attendee.setEntity(owner);
            attendee.setPartStat(ParticipationStatus.NEEDS_ACTION);
            attendee.setCuType(CalendarUserType.INDIVIDUAL);

            List<Attendee> attendees;
            if (null != original && original.containsAttendees() && null != original.getAttendees()) {
                attendees = new LinkedList<>(original.getAttendees());
            } else if (event.containsAttendees() && null != event.getAttendees()) {
                attendees = new LinkedList<>(event.getAttendees());
            } else {
                attendees = new LinkedList<>();
            }
            attendees.add(attendee);
            event.setAttendees(attendees);
        }
    }

    /**
     * Checks if a exception was already deleted
     *
     * @param original The original or rather the master event
     * @param exception The exception to check
     * @return <code>true</code> if the given exception was already deleted
     */
    protected boolean isDeleteException(Event original, Event exception) {
        if (null != original && original.containsDeleteExceptionDates()) {
            SortedSet<RecurrenceId> deleteExceptionDates = original.getDeleteExceptionDates();
            if (null != deleteExceptionDates) {
                return deleteExceptionDates.stream().anyMatch(r -> 0 == r.compareTo(exception.getRecurrenceId()));
            }
        }
        return false;
    }

    protected static boolean nonNull(Object... o) {
        if (null == o) {
            return false;
        }
        for (int i = 0; i < o.length; i++) {
            if (null == o[i]) {
                return false;
            }

        }
        return true;
    }

    protected static boolean nonNullAwareEquals(Object o1, Object o2) {
        return nonNull(o1, o2) && Objects.equals(o1, o2);
    }
    
    /**
     * Resolves an UID to all events belonging to the corresponding calendar object resource. The lookup is performed case-sensitive,
     * within the scope of a specific calendar user. I.e., the unique identifier is resolved to events residing in the user's
     * <i>personal</i>, as well as <i>public</i> calendar folders.
     * <p/>
     * The events will be <i>userized</i> to reflect the view of the calendar user on the events.
     * 
     * @param session The calendar session
     * @param uid The UID to resolve
     * @param calendarUserId The identifier of the calendar user the unique identifier should be resolved for
     * @return The <i>userized</i> events, or an empty list if no events were found
     */
    protected static CalendarObjectResource getOriginalResource(CalendarSession session, String uid, int calendarUserId) throws OXException {
        EventField[] oldParameterFields = session.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class);
        try {
            session.set(CalendarParameters.PARAMETER_FIELDS, null);
            List<Event> events = session.getCalendarService().getUtilities().resolveEventsByUID(session, uid, calendarUserId);
            return events.isEmpty() ? null : new DefaultCalendarObjectResource(events);
        } finally {
            session.set(CalendarParameters.PARAMETER_FIELDS, oldParameterFields);
        }
    }

    protected static Event optEventOccurrence(CalendarSession session, Event seriesMaster, RecurrenceId recurrenceId) {
        if (null == seriesMaster) {
            return null;
        }
        try {
            return getOccurrence(session.getRecurrenceService(), seriesMaster, recurrenceId);
        } catch (OXException e) {
            session.addWarning(e);
            LOG.warn("Unexpected error preparing event occurrence for {} of {}", recurrenceId, seriesMaster, e);
            return null;
        }
    }

    /**
     * Constructs a calendar object resource from the events of an iTIP message.
     * 
     * @param message The iTIP message to get the calendar object resource from
     * @return The calendar object resource representig the events in the iTIP message
     */
    protected static CalendarObjectResource getResource(ITipMessage message) {
        Iterable<Event> exceptions = message.exceptions();
        if (null == exceptions) {
            return new IncomingCalendarObjectResource(message.getEvent());
        }
        return new IncomingCalendarObjectResource(message.getEvent(), Lists.newArrayList(exceptions));
    }

    /**
     * Patches a previously imported event so that changes can be derived properly when comparing with a currently stored event.
     * 
     * @param session The calendar session
     * @param event The event to patch
     * @param originalEvent The original event, or <code>null</code> if not applicable
     * @param calendarUserId The calendar user id
     * @return The patched event
     */
    protected static Event patchEvent(CalendarSession session, Event event, Event originalEvent, int calendarUserId) {
        try {
            Event patchedEvent = session.getUtilities().copyEvent(event, (EventField[]) null);
            Consistency.adjustAllDayDates(patchedEvent);
            session.getUtilities().adjustTimeZones(session.getSession(), calendarUserId, patchedEvent, originalEvent);
            patchedEvent = restoreAttachments(originalEvent, patchedEvent);
            Consistency.normalizeRecurrenceIDs(patchedEvent.getStartDate(), patchedEvent);
            return patchedEvent;
        } catch (OXException e) {
            session.addWarning(e);
            LOG.warn("Unexpected error patching {}, falling back to original representation", event, e);
            return event;
        }
    }

    /**
     * Constructs a calendar object resource from the events of an iTIP message.
     * <p/>
     * Events not fulfilling the constraints of https://tools.ietf.org/html/rfc5546#section-3.2 are filtered if desired.
     * 
     * @param session The calendar session
     * @param message The iTIP message to get the calendar object resource from
     * @param filterInvalid <code>true</code> to filter invalid occurrences from the iTip message, <code>false</code>, otherwise
     * @return The calendar object resource representing the events in the iTIP message
     */
    protected static CalendarObjectResource getResource(CalendarSession session, ITipMessage message, boolean filterInvalid) throws OXException {
        List<Event> events = new ArrayList<Event>();
        if (null != message.getEvent()) {
            events.add(message.getEvent());
        }
        Iterable<Event> exceptions = message.exceptions();
        if (null != exceptions) {
            for (Event exception : exceptions) {
                if (filterInvalid) {
                    try {
                        checkValidity(message, exception);
                    } catch (OXException e) {
                        LOG.debug("Filtering invalid exception {}: {}", exception, e.getMessage(), e);
                        session.addWarning(e);
                        continue;
                    }
                }
                events.add(exception);
            }
        }
        try {
            return new IncomingCalendarObjectResource(events);
        } catch (Exception e) {
            throw ITipExceptions.NOT_CONFORM.create(e, e.getMessage());
        }
    }

    private static void checkValidity(ITipMessage message, Event event) throws OXException {
        if (ITipMethod.REQUEST.equals(message.getMethod()) || ITipMethod.CANCEL.equals(message.getMethod())) {
            /*
             * require UID, ORGANIZER and at least one ATTENDEE
             */
            if (null == event.getUid()) {
                throw CalendarExceptionCodes.MANDATORY_FIELD.create(EventField.UID);
            }
            if (null == event.getOrganizer()) {
                throw CalendarExceptionCodes.MANDATORY_FIELD.create(EventField.ORGANIZER);
            }
            if (null == event.getAttendees() || event.getAttendees().isEmpty()) {
                throw CalendarExceptionCodes.MANDATORY_FIELD.create(EventField.ATTENDEES);
            }
        }
    }
}
