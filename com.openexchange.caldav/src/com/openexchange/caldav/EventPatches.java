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

package com.openexchange.caldav;

import static com.openexchange.chronos.common.CalendarUtils.addExtendedProperty;
import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.getEventID;
import static com.openexchange.chronos.common.CalendarUtils.isOrganizer;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.optExtendedProperty;
import static com.openexchange.tools.arrays.Collections.isNullOrEmpty;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.UUID;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.caldav.resources.CalendarAccessOperation;
import com.openexchange.caldav.resources.EventResource;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.ExtendedPropertyParameter;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.RelatedTo;
import com.openexchange.chronos.Trigger;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.DefaultEventUpdate;
import com.openexchange.chronos.ical.CalendarExport;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.dav.AttachmentUtils;
import com.openexchange.dav.DAVUserAgent;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;

/**
 * {@link EventPatches}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventPatches {
    
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(EventPatches.class);

    /**
     * Initializes a new {@link Incoming}.
     *
     * @param factory The factory
     */
    public static Incoming Incoming(GroupwareCaldavFactory factory) {
        return new Incoming(factory);
    }

    /**
     * Initializes a new {@link Outgoing}.
     *
     * @param factory The factory
     */
    public static Outgoing Outgoing(GroupwareCaldavFactory factory) {
        return new Outgoing(factory);
    }

    /** The external representation of the prefix used for private comments denoting a new time proposal */
    private static final String COMMENT_PROPOSAL_PREFIX_EXTERNAL = "\u200B\uD83D\uDDD3\u200B ";

    /** The internal representation of the prefix used for private comments denoting a new time proposal */
    private static final String COMMENT_PROPOSAL_PREFIX_INTERNAL = "\u200B\u0e4f\u200B ";

    /** The "empty" trigger used in Apple default alarms (<code>19760401T005545Z</code>) */
    private static final Trigger EMPTY_ALARM_TRIGGER = new Trigger(new Date(197168145000L));  // 19760401T005545Z

    /** Extended properties that are derived from others and injected on a per-client basis */
    private static final String[] DERIVED_X_PROPERTIES = {
        "X-MICROSOFT-CDO-ALLDAYEVENT", "X-MICROSOFT-CDO-BUSYSTATUS",
        "X-CALENDARSERVER-PRIVATE-COMMENT", "X-CALENDARSERVER-ATTENDEE-COMMENT",
        "X-MOZ-SNOOZE", "X-MOZ-SNOOZE-TIME*", "X-MOZ-LASTACK"
    };

    /**
     * Initializes a new {@link EventPatches}.
     */
    private EventPatches() {
    	// prevent instantiation
    }

    /**
     * Configures the {@link ICalParameters#IGNORED_PROPERTIES} parameter as needed prior exporting/importing iCal data.
     *
     * @param resource The underlying event resource
     * @param parameters The iCal parameters to configure the ignored properties in
     * @return The passed iCal paramters instance
     */
    public static ICalParameters applyIgnoredProperties(EventResource resource, ICalParameters parameters) {
        if (null != parameters) {
            DAVUserAgent userAgent = resource.getUserAgent();
            if (false == DAVUserAgent.EM_CLIENT.equals(userAgent)) {
                /*
                 * forcibly ignore X-MICROSOFT-CDO-ALLDAYEVENT and X-MICROSOFT-CDO-BUSYSTATUS for clients other than em client
                 */
                parameters.set(ICalParameters.IGNORED_PROPERTIES, new String[] { "X-MICROSOFT-CDO-ALLDAYEVENT", "X-MICROSOFT-CDO-BUSYSTATUS" });
            }
        }
        return parameters;
    }

    /**
     * Replaces the representation of the prefix used for private comments denoting a new time proposal with another one.
     *
     * @param attendees The attendees to patch
     * @param The prefix to replace
     * @param The replacement prefix
     */
    private static void patchPrivateComments(List<Attendee> attendees, String prefix, String replacement) {
        if (null == attendees || 0 == attendees.size()) {
            return;
        }
        for (Attendee attendee : attendees) {
            String comment = attendee.getComment();
            if (null != comment && comment.startsWith(prefix)) {
                attendee.setComment(replacement + comment.substring(prefix.length()));
            }
        }
    }

    /**
     * Removes all attachments from the supplied event in case it represents a change exception and the current user agent is the
     * Mac OS client, as he cannot handle them properly (the server is effectively behaving like
     * <code>calendar-managed-attachments-no-recurrence</code> in this case).
     *
     * @param resource The parent event resource
     * @param event The event being imported/exported
     * @return The patched event
     */
    private static Event removeAttachmentsFromExceptions(EventResource resource, Event event) {
        if (null != event.getAttachments() && DAVUserAgent.MAC_CALENDAR.equals(resource.getUserAgent()) && (isSeriesException(event) || null != event.getRecurrenceId())) {
            event.removeAttachments();
        }
        return event;
    }

    /**
     * Removes all extended properties from an event that are derived from others.
     * 
     * @param event The event to strip the derived extended properties from
     * @return The passed event reference
     */
    private static Event stripDerivedProperties(Event event) {
        ExtendedProperties extendedProperties = event.getExtendedProperties();
        if (null != extendedProperties && 0 < extendedProperties.size()) {
            List<ExtendedProperty> propertiesToRemove = new ArrayList<ExtendedProperty>();
            for (String name : DERIVED_X_PROPERTIES) {
                propertiesToRemove.addAll(CalendarUtils.findExtendedProperties(extendedProperties, name));
            }
            if (false == propertiesToRemove.isEmpty()) {
                extendedProperties.removeAll(propertiesToRemove);
            }
        }
        return event;
    }

    /**
     * {@link Incoming}
     *
     * Patches for incoming iCal files.
     *
     * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
     */
    public static final class Incoming {

        private final GroupwareCaldavFactory factory;

        /**
         * Initializes a new {@link Incoming}.
         *
         * @param factory The factory
         */
        private Incoming(GroupwareCaldavFactory factory) {
            super();
            this.factory = factory;
        }

        /**
         * Replaces the external representation of the prefix used for private comments denoting a new time proposal to the internal one.
         *
         * @param event The event to adjust prior saving
         */
        private static void adjustProposedTimePrefixes(Event event) {
            patchPrivateComments(event.getAttendees(), COMMENT_PROPOSAL_PREFIX_EXTERNAL, COMMENT_PROPOSAL_PREFIX_INTERNAL);
        }

        /**
         * Takes over the current user's attendee comment if set from an Apple client via the custom
         * <code>X-CALENDARSERVER-PRIVATE-COMMENT</code> property.
         *
         * @param eventResource The event resource
         * @param importedEvent The event
         */
        private void adjustAttendeeComments(EventResource eventResource, Event importedEvent) {
            /*
             * evaluate imported extra properties
             */
            ExtendedProperty extendedProperty = optExtendedProperty(importedEvent, "X-CALENDARSERVER-PRIVATE-COMMENT");
            if (null != extendedProperty) {
                /*
                 * take over the user's attendee comment if set
                 */
                try {
                    EntityResolver entityResolver = factory.requireService(CalendarUtilities.class).getEntityResolver(factory.getContext().getContextId());
                    entityResolver.prepare(importedEvent.getAttendees());
                } catch (OXException e) {
                    LOG.warn("Error preparing attendees for imported event", e);
                }
                Attendee attendee = find(importedEvent.getAttendees(), eventResource.getFactory().getUser().getId());
                if (null != attendee && false == attendee.containsComment()) {
                    attendee.setComment(String.valueOf(extendedProperty.getValue()));
                }
            }
        }

        /**
         * Strips any extended properties in case an update is performed on an <i>attendee scheduling object resource</i>, from the 
         * calendar user's point of view.
         * <p/>
         * Otherwise, all known and handled extended properties are removed to avoid ambigiouties, as they will be derived from other 
         * properties upon the next export. 
         *
         * @param eventResource The event resource
         * @param importedEvent The event
         */
        private void stripExtendedPropertiesFromAttendeeSchedulingResource(EventResource eventResource, Event importedEvent) {
            ExtendedProperties extendedProperties = importedEvent.getExtendedProperties();
            if (null == extendedProperties) {
                return;
            }
            int calendarUserId = -1;
            try {
                calendarUserId = eventResource.getParent().getCalendarUser().getId();
            } catch (OXException e) {
                LOG.warn("Error deriving calendar user from collection", e);
            }
            if (null != eventResource.getEvent() && CalendarUtils.isAttendeeSchedulingResource(eventResource.getEvent(), calendarUserId)) {
                /*
                 * strip all extended properties from attendee scheduling resources
                 */
                importedEvent.removeExtendedProperties();
            }
        }

        private static void adjustAlarms(EventResource resource, Event importedEvent, Event importedSeriesMaster) {
            List<Alarm> alarms = importedEvent.getAlarms();
            if (null == alarms || 0 == alarms.size()) {
                return;
            }
            /*
             * remove any empty default alarms
             */
            for (Iterator<Alarm> iterator = alarms.iterator(); iterator.hasNext();) {
                if (EMPTY_ALARM_TRIGGER.equals(iterator.next().getTrigger())) {
                    iterator.remove();
                }
            }
            if (DAVUserAgent.THUNDERBIRD_LIGHTNING.equals(resource.getUserAgent())) {
                /*
                 * handle acknowledged alarms via X-MOZ-LASTACK
                 */
                adjustMozillaLastAcknowledged(importedEvent, importedSeriesMaster);
            }
            if (DAVUserAgent.THUNDERBIRD_LIGHTNING.equals(resource.getUserAgent()) || DAVUserAgent.EM_CLIENT.equals(resource.getUserAgent())) {

                /*
                 * handle snoozing via X-MOZ-SNOOZE and X-MOZ-SNOOZE-TIME-...
                 */
                adjustMozillaSnoozeTime(importedEvent);
                adjustMozillaSnooze(importedEvent);
            }
        }

        /**
         * Adjusts any new change exceptions inserted by the Mac OS client when snoozing the alarm of an event series.
         *
         * @param resource The event resource
         * @param importedEvent The imported series master event as supplied by the client
         * @param importedChangeExceptions The imported change exceptions as supplied by the client
         */
        private void adjustSnoozeExceptions(EventResource resource, Event importedEvent, List<Event> importedChangeExceptions) {
            if (DAVUserAgent.MAC_CALENDAR.equals(resource.getUserAgent()) && isSeriesMaster(resource.getEvent()) &&
                null != importedEvent && null != importedChangeExceptions && 0 < importedChangeExceptions.size()) {
                /*
                 * check if there's a single new change exception holding only the snoozed alarm (and no further changes)
                 */
                try {
                    List<Event> newChangeExceptions = getNewChangeExceptions(resource, importedChangeExceptions);
                    if (null == newChangeExceptions || 1 != newChangeExceptions.size()) {
                        return;
                    }
                    Event newChangeException = newChangeExceptions.get(0);
                    Alarm snoozedAlarm = null;
                    Alarm snoozeAlarm = null;
                    if (null != newChangeException && null != newChangeException.getAlarms()) {
                        for (Alarm alarm : newChangeException.getAlarms()) {
                            /*
                             * XXX: If the original event did contain more than one alarm the snooze alarm of e.g. the second alarm
                             * will contain the wrong UID in 'RELATE_TO' field, letting the function below return the wrong snoozed alarm.
                             * This alarm will be updated instead, efficiently deleting one snooze alarm.
                             */
                            snoozedAlarm = AlarmUtils.getSnoozedAlarm(alarm, newChangeException.getAlarms());
                            if (null != snoozedAlarm) {
                                snoozeAlarm = alarm;
                                break;
                            }
                        }
                    }
                    if (null == snoozedAlarm || null == snoozeAlarm) {
                        return;
                    }
                    Event masterEvent = new CalendarAccessOperation<Event>(factory) {

                        @Override
                        protected Event perform(IDBasedCalendarAccess access) throws OXException {
                            access.set(CalendarParameters.PARAMETER_FIELDS, null);
                            EventID eventId = new EventID(resource.getEvent().getFolderId(), resource.getEvent().getId(), resource.getEvent().getRecurrenceId());
                            return access.getEvent(eventId);
                        }
                    }.execute(factory.getSession());
                    RecurrenceId recurrenceId = newChangeException.getRecurrenceId();
                    Iterator<Event> iterator = factory.requireService(RecurrenceService.class).iterateEventOccurrences(masterEvent, new Date(recurrenceId.getValue().getTimestamp()), null);
                    Event originalOccurrence = iterator.hasNext() ? iterator.next() : null;
                    if (null != originalOccurrence) {
                        Alarm originalAlarm = getSnoozedAlarm(originalOccurrence, snoozedAlarm);
                        if (null != originalAlarm) {
                            originalOccurrence = exportAndImport(resource, originalOccurrence);
                            EventUpdate eventUpdate = DefaultEventUpdate.builder()
                                .originalEvent(originalOccurrence)
                                .updatedEvent(newChangeException)
                                .ignoredEventFields(EventField.TIMESTAMP, EventField.LAST_MODIFIED, EventField.SEQUENCE, EventField.RECURRENCE_RULE, EventField.CREATED, EventField.ALARMS, EventField.EXTENDED_PROPERTIES)
                                .considerUnset(true)
                                .ignoreDefaults(true)
                            .build();
                            if (false == eventUpdate.getAlarmUpdates().isEmpty() && eventUpdate.getUpdatedFields().size() == 1) {
                                snoozedAlarm.setUid(originalAlarm.getUid());
                                snoozeAlarm.setRelatedTo(new RelatedTo("SNOOZE", originalAlarm.getUid()));
                                List<Alarm> patchedAlarms = new ArrayList<Alarm>(originalOccurrence.getAlarms().size());
                                for (Alarm a : originalOccurrence.getAlarms()) {
                                    if (!snoozedAlarm.getTrigger().matches(a.getTrigger()) && !snoozeAlarm.getRelatedTo().equals(a.getRelatedTo())) {
                                        patchedAlarms.add(a);
                                    }
                                }
                                patchedAlarms.add(snoozeAlarm);
                                patchedAlarms.add(snoozedAlarm);
                                importedEvent.setAlarms(patchedAlarms);
                                for (Iterator<Event> iter = importedChangeExceptions.iterator(); iter.hasNext();) {
                                    if (newChangeException.getRecurrenceId().equals(iter.next().getRecurrenceId())) {
                                        iter.remove();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } catch (OXException e) {
                    LOG.warn("Error adjusting snooze exceptions", e);
                }
            }
        }

        /**
         * Get the original {@link Alarm} that is equal to the snoozed alarm
         *
         * @param originalOccurrence The original occurrence of the exception
         * @param snoozedAlarm The alarm that has been snoozed
         * @return The {@link Alarm} that matches the snoozedAlarm or <code>null</code>
         */
        private Alarm getSnoozedAlarm(Event originalOccurrence, Alarm snoozedAlarm) {
            if (null != originalOccurrence.getAlarms()) {
                for (Alarm a : originalOccurrence.getAlarms()) {
                    if (snoozedAlarm.getTrigger().matches(a.getTrigger())) {
                        return a;
                    }
                }
            }
            return null;
        }

        private Event exportAndImport(EventResource resource, Event event) throws OXException {
            /*
             * prepare exported event
             */
            CalendarUtilities calendarUtilities = factory.requireService(CalendarUtilities.class);
            event = calendarUtilities.copyEvent(event, (EventField[]) null);
            EventPatches.Outgoing(factory).applyAll(resource, event);
            /*
             * export to ical & re-import the event
             */
            ICalService iCalService = factory.requireService(ICalService.class);
            CalendarExport calendarExport = iCalService.exportICal(null);
            calendarExport.add(event);
            IFileHolder fileHolder = null;
            try {
                fileHolder = calendarExport.getVCalendar();
                ImportedCalendar calendar = iCalService.importICal(fileHolder.getStream(), null);
                if (null != calendar.getEvents() && 0 < calendar.getEvents().size()) {
                    return calendar.getEvents().get(0);
                }
                throw OXException.notFound("export");
            } finally {
                Streams.close(fileHolder);
            }
        }

        /**
         * Extracts those change exceptions that are considered as "new", i.e. change exceptions that do not already exist based on the
         * change exception dates of the original recurring event master.
         *
         * @param resource The event resource
         * @param importedChangeExceptions The imported change exceptions as supplied by the client
         * @return The new exceptions, or an empty list if there are none
         */
        private List<Event> getNewChangeExceptions(EventResource resource, List<Event> importedChangeExceptions) throws OXException {
            if (null == importedChangeExceptions || 0 == importedChangeExceptions.size()) {
                return Collections.emptyList();
            }
            Event originalEvent = resource.getEvent();
            List<Event> changeExceptions = new CalendarAccessOperation<List<Event>>(factory) {

                @Override
                protected List<Event> perform(IDBasedCalendarAccess access) throws OXException {
                    return access.getChangeExceptions(originalEvent.getFolderId(), originalEvent.getSeriesId());
                }
            }.execute(factory.getSession());
            SortedSet<RecurrenceId> changeExceptionDates = CalendarUtils.getRecurrenceIds(changeExceptions);
            List<Event> newChangeExceptions = new ArrayList<Event>(importedChangeExceptions.size());
            if (null == changeExceptionDates || 0 == changeExceptionDates.size()) {
                newChangeExceptions.addAll(importedChangeExceptions);
            } else {
                for (Event importedChangeException : importedChangeExceptions) {
                    RecurrenceId recurrenceId = importedChangeException.getRecurrenceId();
                    if (null != recurrenceId && false == CalendarUtils.contains(changeExceptionDates, recurrenceId)) {
                        newChangeExceptions.add(importedChangeException);
                    }
                }
            }
            return newChangeExceptions;
        }

        /**
         * Adjusts a "snoozed" alarm as indicated by the Mozilla Lightning client via <code>X-MOZ-SNOOZE</code> by establishing a corresponding
         * snooze-relationship to another alarm if missing.
         *
         * @param importedEvent The imported event as sent by the client
         */
        private static void adjustMozillaSnooze(Event importedEvent) {
            Date mozSnoozeDate = Tools.optExtendedPropertyAsDate(importedEvent.getExtendedProperties(), "X-MOZ-SNOOZE");
            if (null == mozSnoozeDate || null == importedEvent.getAlarms()) {
                return;
            }
            for (Alarm alarm : importedEvent.getAlarms()) {
                if (null != alarm.getTrigger() && null != alarm.getTrigger().getDateTime() && alarm.getTrigger().getDateTime().equals(mozSnoozeDate)) {
                    /*
                     * this is the snooze alarm; establish corresponding snooze-relationship if missing
                     */
                    addSnoozeRelationship(alarm, importedEvent.getAlarms());
                }
            }
        }

        /**
         * Adds a "SNOOZE" relationship to the supplied "snooze" alarm pointing to a suitable alarm based on an equal "acknowledged" value.
         *
         * @param snoozeAlarm The snooze alarm
         * @param allAlarms All alarms associated with the event
         */
        private static void addSnoozeRelationship(Alarm snoozeAlarm, List<Alarm> allAlarms) {
            if (null != snoozeAlarm.getAcknowledged() && null == snoozeAlarm.getRelatedTo()) {
                Alarm snoozedAlarm = null;
                for (Alarm alarmCandidate : allAlarms) {
                    if (alarmCandidate != snoozeAlarm && null != alarmCandidate.getAcknowledged() &&
                        alarmCandidate.getAcknowledged().equals(snoozeAlarm.getAcknowledged())) {
                        snoozedAlarm = alarmCandidate;
                        break;
                    }
                }
                if (null != snoozedAlarm) {
                    String uid = snoozedAlarm.getUid();
                    if (Strings.isEmpty(uid)) {
                        uid = UUID.randomUUID().toString().toUpperCase();
                        snoozedAlarm.setUid(uid);
                    }
                    snoozeAlarm.setRelatedTo(new RelatedTo("SNOOZE", uid));
                }
            }
        }

        /**
         * Adjusts a "snoozed" alarm as indicated by the Mozilla Lightning client via <code>X-MOZ-SNOOZE-TIME...</code> for an event series by
         * establishing a corresponding snooze-relationship to another alarm if missing.
         *
         * @param importedEvent The imported event as sent by the client
         */
        private static void adjustMozillaSnoozeTime(Event importedEvent) {
            Date mozSnoozeTime = Tools.optExtendedPropertyAsDate(importedEvent.getExtendedProperties(), "X-MOZ-SNOOZE-TIME*");
            if (null == mozSnoozeTime || null == importedEvent.getAlarms()) {
                return;
            }
            /*
             * insert corresponding snooze alarm & establish snooze relationship
             */
            Alarm snoozeAlarm = new Alarm(new Trigger(mozSnoozeTime), AlarmAction.DISPLAY);
            snoozeAlarm.setDescription("Reminder");
            snoozeAlarm.setAcknowledged(Tools.optExtendedPropertyAsDate(importedEvent.getExtendedProperties(), "X-MOZ-LASTACK"));
            importedEvent.getAlarms().add(snoozeAlarm);
            addSnoozeRelationship(snoozeAlarm, importedEvent.getAlarms());
        }

        /**
         * Adjusts an "acknowledged" alarm as indicated by the Mozilla Lightning via <code>X-MOZ-LASTACK</code> for an event by copying
         * the value over to the regular "acknowledged" property of the alarm.
         *
         * @param importedEvent The imported event as sent by the client
         * @param importedSeriesMaster The imported series master event as sent by the client
         */
        private static void adjustMozillaLastAcknowledged(Event importedEvent, Event importedSeriesMaster) {
            if (null == importedEvent.getAlarms() || 0 == importedEvent.getAlarms().size()) {
                return;
            }
            /*
             * take over latest "X-MOZ-LASTACK" from alarms and parent event component
             */
            Date parentAcknowledged = Tools.optExtendedPropertyAsDate(importedEvent.getExtendedProperties(), "X-MOZ-LASTACK");
            if (null != importedSeriesMaster) {
                parentAcknowledged = Tools.getLatestModified(parentAcknowledged, Tools.optExtendedPropertyAsDate(importedSeriesMaster.getExtendedProperties(), "X-MOZ-LASTACK"));
            }
            for (Alarm alarm : importedEvent.getAlarms()) {
                Date acknowledged = Tools.getLatestModified(Tools.optExtendedPropertyAsDate(alarm.getExtendedProperties(), "X-MOZ-LASTACK"), alarm.getAcknowledged());
                acknowledged = Tools.getLatestModified(parentAcknowledged, acknowledged);
                alarm.setAcknowledged(acknowledged);
            }
        }

        /**
         * Takes over a filename from the parent resource's WebDAV path in case it differs from the event's UID.
         *
         * @param resource The parent event resource
         * @param importedEvent The event being imported
         */
        private static void applyFilename(EventResource resource, Event importedEvent) {
            String name = resource.getUrl().name();
            if (Strings.isNotEmpty(name)) {
                if (name.toLowerCase().endsWith(Tools.EXTENSION_ICS)) {
                    name = name.substring(0, name.length() - Tools.EXTENSION_ICS.length());
                }
                if (false == name.equals(importedEvent.getUid())) {
                    importedEvent.setFilename(name);
                }
            }
        }

        private static void applyManagedAttachments(Event importedEvent) {
            List<Attachment> attachments = importedEvent.getAttachments();
            if (null != attachments && 0 < attachments.size()) {
                for (Attachment attachment : attachments) {
                    if (0 < attachment.getManagedId()) {
                        continue; // already set
                    }
                    if (null != attachment.getUri()) {
                        try {
                            AttachmentMetadata metadata = AttachmentUtils.decodeURI(new URI(attachment.getUri()));
                            attachment.setManagedId(metadata.getId());
                        } catch (URISyntaxException e) {
                            LOG.warn("Error decoding attachment URI", e);
                        } catch (IllegalArgumentException e) {
                            LOG.debug("Error interpreting as attachment URI; skipping.", e);
                        }
                    }
                }
            }
        }

        /**
         * Takes over any <code>X-CALENDARSERVER-ACCESS</code> restrictions found in the imported calendar into the imported events.
         *
         * @param eventResource The event resource
         * @param caldavImport The CalDAV import
         */
        private static void applyCalendarserverAccess(EventResource eventResource, CalDAVImport caldavImport) {
            if (DAVUserAgent.IOS.equals(eventResource.getUserAgent()) || DAVUserAgent.MAC_CALENDAR.equals(eventResource.getUserAgent())) {
                ExtendedProperty property = optExtendedProperty(caldavImport.getCalender(), "X-CALENDARSERVER-ACCESS");
                if (null == property || false == String.class.isInstance(property.getValue()) || Strings.isEmpty((String) property.getValue())) {
                    return;
                }
                if (null == property.getValue()) {
                    LOG.warn("Ignoring unknown X-CALENDARSERVER-ACCESS '{}'", property.getValue());
                    return;
                }
                Classification classification = new Classification((String) property.getValue());
                if (null != caldavImport.getEvent()) {
                    caldavImport.getEvent().setClassification(classification);
                }
                if (null != caldavImport.getChangeExceptions()) {
                    for (Event changeException : caldavImport.getChangeExceptions()) {
                        changeException.setClassification(classification);
                    }
                }
            }
        }

        /**
         * Restores deleted change exception events from the past where the calendar user attendee's participation status was 
         * previously set to 'declined'. This is done for the iOS client who sometimes decides to send such updates without user 
         * interaction. 
         *
         * @param resource The event resource
         * @param importedEvent The imported series master event as supplied by the client
         * @param importedChangeExceptions The imported change exceptions as supplied by the client
         */
        private void restoreDeclinedDeleteExceptions(EventResource resource, Event importedEvent, List<Event> importedChangeExceptions) {
            if (false == resource.exists() || false == isSeriesMaster(resource.getEvent()) || null == importedEvent || false == DAVUserAgent.IOS.equals(resource.getUserAgent())) {
                return; // not applicable
            }
            SortedSet<RecurrenceId> deleteExceptionDates = importedEvent.getDeleteExceptionDates();
            DateTime now = new DateTime(System.currentTimeMillis());
            if (isNullOrEmpty(deleteExceptionDates) || now.before(importedEvent.getDeleteExceptionDates().first().getValue())) {
                return; // not applicable
            }
            try {
                new CalendarAccessOperation<Void>(factory) {

                    @Override
                    protected Void perform(IDBasedCalendarAccess access) throws OXException {
                        /*
                         * check for new delete exception dates for previous change exceptions
                         */
                        Event originalSeriesMaster = access.getEvent(getEventID(resource.getEvent()));
                        for (RecurrenceId exceptionDate : originalSeriesMaster.getChangeExceptionDates()) {
                            if (deleteExceptionDates.contains(exceptionDate) && now.after(exceptionDate.getValue())) {
                                Event originalChangeException = access.getEvent(
                                    new EventID(resource.getEvent().getFolderId(), resource.getEvent().getId(), exceptionDate));
                                Attendee attendee = find(originalChangeException.getAttendees(), resource.getParent().getCalendarUser().getId());
                                if (null != attendee && ParticipationStatus.DECLINED.matches(attendee.getPartStat())) {
                                    /*
                                     * restore original change exception; remove delete exception date
                                     */
                                    deleteExceptionDates.remove(exceptionDate);
                                    importedChangeExceptions.add(exportAndImport(resource, originalChangeException));
                                }
                            }
                        }
                        return null;
                    }
                }.execute(factory.getSession());                
            } catch (OXException e) {
                LOG.warn("Error restoring declined delete exceptions", e);
            }
        }

        /**
         * Applies all known patches to an event after importing.
         *
         * @param resource The parent event resource
         * @param caldavImport The CalDAV import
         * @return The patched CalDAV import
         */
        public CalDAVImport applyAll(EventResource resource, CalDAVImport caldavImport) {
            /*
             * patch the calendar
             */
            applyCalendarserverAccess(resource, caldavImport);
            Event importedEvent = caldavImport.getEvent();
            List<Event> importedChangeExceptions = caldavImport.getChangeExceptions();
            if (null != importedEvent) {
                /*
                 * patch the 'master' event
                 */
                applyFilename(resource, importedEvent);
                adjustAttendeeComments(resource, importedEvent);
                adjustProposedTimePrefixes(importedEvent);
                restoreDeclinedDeleteExceptions(resource, importedEvent, importedChangeExceptions);
                adjustSnoozeExceptions(resource, importedEvent, importedChangeExceptions);
                adjustAlarms(resource, importedEvent, null);
                applyManagedAttachments(importedEvent);
                stripExtendedPropertiesFromAttendeeSchedulingResource(resource, importedEvent);
            }
            if (null != importedChangeExceptions && 0 < importedChangeExceptions.size()) {
                /*
                 * patch the change exceptions
                 */
                for (Event importedChangeException : importedChangeExceptions) {
                    adjustAttendeeComments(resource, importedChangeException);
                    adjustProposedTimePrefixes(importedChangeException);
                    adjustAlarms(resource, importedChangeException, importedEvent);
                    applyManagedAttachments(importedChangeException);
                    removeAttachmentsFromExceptions(resource, importedChangeException);
                    stripExtendedPropertiesFromAttendeeSchedulingResource(resource, importedChangeException);
                }
            }
            /*
             * strip not wanted, derived extended properties in a 2nd step afterwards
             */
            if (null != importedEvent) {
                stripDerivedProperties(importedEvent);
            }
            if (null != importedChangeExceptions && 0 < importedChangeExceptions.size()) {
                for (Event importedChangeException : importedChangeExceptions) {
                    stripDerivedProperties(importedChangeException);
                }
            }
            return new CalDAVImport(resource.getUrl(), caldavImport.getCalender(), importedEvent, importedChangeExceptions);
        }

    }

    /**
     * {@link Outgoing}
     *
     * Patches for outgoing iCal files.
     *
     * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
     * @since v7.10.0
     */
    public static final class Outgoing {

        private final GroupwareCaldavFactory factory;

        /**
         * Initializes a new {@link Outgoing}.
         *
         * @param factory The factory
         */
        private Outgoing(GroupwareCaldavFactory factory) {
            super();
            this.factory = factory;
        }

        /**
         * Replaces the internal representation of the prefix used for private comments denoting a new time proposal to the external one.
         *
         * @param event The event to adjust prior serialization
         * @return The passed event reference
         */
        private static Event adjustProposedTimePrefixes(Event event) {
            patchPrivateComments(event.getAttendees(), COMMENT_PROPOSAL_PREFIX_INTERNAL, COMMENT_PROPOSAL_PREFIX_EXTERNAL);
            return event;
        }

        /**
         * Takes over attendee comments as custom iCal parameters for Apple clients, which includes comments meant for the organizer (via
         * <code>X-CALENDARSERVER-ATTENDEE-COMMENT</code>, as well as the calendar user's own comment via
         * <code>X-CALENDARSERVER-PRIVATE-COMMENT</code>.
         *
         * @param resource The parent event resource
         * @param exportedEvent The event being exported
         * @return The patched event
         */
        private static Event applyAttendeeComments(EventResource eventResource, Event exportedEvent) {
            /*
             * "caldav-privatecomments" for Apple clients
             * https://github.com/apple/ccs-calendarserver/blob/master/doc/Extensions/caldav-privatecomments.txt
             */
            if (null != exportedEvent.getAttendees() &&
                (DAVUserAgent.IOS.equals(eventResource.getUserAgent()) || DAVUserAgent.MAC_CALENDAR.equals(eventResource.getUserAgent()))) {
                if (isOrganizer(exportedEvent, eventResource.getFactory().getUser().getId())) {
                    /*
                     * provide all attendee comments for organizer
                     */
                    for (Attendee attendee : exportedEvent.getAttendees()) {
                        if (Strings.isNotEmpty(attendee.getComment())) {
                            List<ExtendedPropertyParameter> parameters = new ArrayList<ExtendedPropertyParameter>();
                            parameters.add(new ExtendedPropertyParameter("X-CALENDARSERVER-ATTENDEE-REF", attendee.getUri()));
                            addExtendedProperty(exportedEvent, new ExtendedProperty("X-CALENDARSERVER-ATTENDEE-COMMENT", attendee.getComment(), parameters));
                        }
                    }
                } else {
                    /*
                     * provide the current user's confirmation message
                     */
                    Attendee attendee = find(exportedEvent.getAttendees(), eventResource.getFactory().getUser().getId());
                    if (null != attendee && Strings.isNotEmpty(attendee.getComment())) {
                        addExtendedProperty(exportedEvent, new ExtendedProperty("X-CALENDARSERVER-PRIVATE-COMMENT", attendee.getComment()));
                    }
                }
            }
            return exportedEvent;
        }

        private static Alarm getEmptyDefaultAlarm() {
            Alarm alarm = new Alarm(EMPTY_ALARM_TRIGGER, AlarmAction.NONE);
            alarm.setUid(UUID.randomUUID().toString().toUpperCase());
            List<ExtendedProperty> extendedProperties = new ArrayList<ExtendedProperty>(3);
            extendedProperties.add(new ExtendedProperty("X-WR-ALARMUID", alarm.getUid()));
            extendedProperties.add(new ExtendedProperty("X-APPLE-LOCAL-DEFAULT-ALARM", "TRUE"));
            extendedProperties.add(new ExtendedProperty("X-APPLE-DEFAULT-ALARM", "TRUE"));
            alarm.setExtendedProperties(new ExtendedProperties(extendedProperties));
            return alarm;
        }

        private Event adjustAlarms(EventResource resource, Event exportedEvent) {
            List<Alarm> exportedAlarms = exportedEvent.getAlarms();
            try {
                if (resource.getFactory().getUser().getId() != resource.getParent().getCalendarUser().getId()) {
                    /*
                     * remove alarms if event in shared folders is loaded is loaded by another user
                     */
                    exportedAlarms = null;
                }
            } catch (OXException e) {
                LOG.warn("Error deriving calendar user from collection", e);
            }
            if (null == exportedAlarms || 0 == exportedAlarms.size()) {
                /*
                 * insert a dummy alarm to prevent Apple clients from adding their own default alarms
                 */
                if (DAVUserAgent.IOS.equals(resource.getUserAgent()) || DAVUserAgent.MAC_CALENDAR.equals(resource.getUserAgent())) {
                    exportedEvent.setAlarms(Collections.singletonList(getEmptyDefaultAlarm()));
                } else {
                    exportedEvent.removeAlarms();
                }
            } else {
                List<Alarm> patchedAlarms = new ArrayList<Alarm>(exportedAlarms.size());
                for (Alarm exportedAlarm : exportedAlarms) {
                    Alarm alarm = exportedAlarm;
                    /*
                     * also supply the acknowledged date via X-MOZ-LASTACK, both in alarm and parent event component
                     */
                    if (null != exportedAlarm.getAcknowledged()) {
                        ExtendedProperty mozLastAckProperty = new ExtendedProperty("X-MOZ-LASTACK", Tools.formatAsUTC(exportedAlarm.getAcknowledged()));
                        alarm.setExtendedProperties(addExtendedProperty(alarm.getExtendedProperties(), mozLastAckProperty, true));
                        exportedEvent.setExtendedProperties(addExtendedProperty(exportedEvent.getExtendedProperties(), mozLastAckProperty, true));
                    }
                    Alarm snoozedAlarm = AlarmUtils.getSnoozedAlarm(alarm, exportedAlarms);
                    if (null != snoozedAlarm) {
                        if (DAVUserAgent.THUNDERBIRD_LIGHTNING.equals(resource.getUserAgent())) {
                            Date snoozeTime = null;
                            try {
                                TimeZone timeZone = TimeZone.getTimeZone(resource.getParent().getCalendarUser().getTimeZone());
                                snoozeTime = AlarmUtils.getTriggerTime(alarm.getTrigger(), exportedEvent, timeZone);
                            } catch (OXException e) {
                                LOG.warn("Error determining snooze time", e);
                                continue;
                            }
                            if (isSeriesMaster(exportedEvent) && null != snoozedAlarm.getAcknowledged()) {
                                try {
                                    Iterator<Event> iterator = factory.requireService(RecurrenceService.class).iterateEventOccurrences(
                                        exportedEvent, snoozedAlarm.getAcknowledged(), null);
                                    if (iterator.hasNext()) {
                                        DateTime relatedDate = AlarmUtils.getRelatedDate(alarm.getTrigger().getRelated(), iterator.next());
                                        String propertyName = "X-MOZ-SNOOZE-TIME-" + String.valueOf(relatedDate.getTimestamp()) + "000";
                                        addExtendedProperty(exportedEvent, new ExtendedProperty(propertyName, Tools.formatAsUTC(snoozeTime)));
                                    }
                                } catch (OXException e) {
                                    LOG.warn("Error converting snoozed alarm trigger", e);
                                }
                            } else {
                                if (null != snoozeTime) {
                                    addExtendedProperty(exportedEvent, new ExtendedProperty("X-MOZ-SNOOZE-TIME", Tools.formatAsUTC(snoozeTime)));
                                }
                            }
                        }
                    }
                    patchedAlarms.add(alarm);
                }
                /*
                 * finally remove default "ACKNOWLEDGED" properties from alarms for lightning (results in parse error in client)
                 */
                if (DAVUserAgent.THUNDERBIRD_LIGHTNING.equals(resource.getUserAgent())) {
                    for (Alarm alarm : patchedAlarms) {
                        alarm.removeAcknowledged();
                    }
                }
                exportedEvent.setAlarms(patchedAlarms);
            }
            return exportedEvent;
        }

        /**
         * Removes the implicitly added folder owner attendee for events in personal calendar folders in case no further attendees were
         * added, along with any organizer information.
         * <p/>
         * This effectively makes the event to not appear as <i>meeting</i> in clients, as well as allowing modifications on it.
         *
         * @param resource The parent event resource
         * @param exportedEvent The event being exported
         * @return The patched event
         */
        private static Event removeImplicitAttendee(EventResource resource, Event exportedEvent) {
            if (false == PublicType.getInstance().equals(resource.getParent().getFolder().getType())) {
                CalendarUtils.removeImplicitAttendee(exportedEvent);
            }
            return exportedEvent;
        }

        /**
         * Prepares the URIs for all managed attachments of the event.
         *
         * @param resource The parent event resource
         * @param exportedEvent The event being exported
         * @return The patched event
         */
        private static Event prepareManagedAttachments(EventResource resource, Event exportedEvent) {
            List<Attachment> attachments = exportedEvent.getAttachments();
            if (null != attachments && 0 < attachments.size()) {
                for (Attachment attachment : attachments) {
                    if (0 < attachment.getManagedId() && null == attachment.getUri()) {
                        try {
                            AttachmentMetadata metadata = Tools.getAttachmentMetadata(attachment, resource, exportedEvent);
                            URI uri = AttachmentUtils.buildURI(resource.getHostData(), metadata);
                            attachment.setUri(uri.toString());
                        } catch (OXException | URISyntaxException e) {
                            LOG.warn("Error preparing managed attachment", e);
                        }
                    }
                }
            }
            return exportedEvent;
        }

        /**
         * Applies all known patches to an event prior exporting.
         *
         * @param resource The parent event resource
         * @param exportedEvent The event being exported
         * @return The patched event
         */
        public Event applyAll(EventResource resource, Event exportedEvent) {
            exportedEvent = stripDerivedProperties(exportedEvent);
            exportedEvent = removeImplicitAttendee(resource, exportedEvent);
            exportedEvent = adjustProposedTimePrefixes(exportedEvent);
            exportedEvent = applyAttendeeComments(resource, exportedEvent);
            exportedEvent = adjustAlarms(resource, exportedEvent);
            exportedEvent = prepareManagedAttachments(resource, exportedEvent);
            exportedEvent = removeAttachmentsFromExceptions(resource, exportedEvent);
            return exportedEvent;
        }

        public static CalendarExport applyExport(EventResource eventResource, CalendarExport calendarExport) {
            if (DAVUserAgent.IOS.equals(eventResource.getUserAgent()) || DAVUserAgent.MAC_CALENDAR.equals(eventResource.getUserAgent())) {
                Event event = eventResource.getEvent();
                if (null != event && false == CalendarUtils.isPublicClassification(event)) {
                    calendarExport.add(new ExtendedProperty("X-CALENDARSERVER-ACCESS", String.valueOf(event.getClassification())));
                }
            }
            return calendarExport;
        }

    }

}
