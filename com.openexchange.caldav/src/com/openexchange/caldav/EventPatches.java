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

import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.initCalendar;
import static com.openexchange.chronos.common.CalendarUtils.isOrganizer;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.openexchange.caldav.resources.EventResource;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.RelatedTo;
import com.openexchange.chronos.Trigger;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.ical.DefaultICalProperty;
import com.openexchange.chronos.ical.ICalProperty;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.dav.AttachmentUtils;
import com.openexchange.dav.DAVUserAgent;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;

/**
 * {@link EventPatches}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventPatches {

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(EventPatches.class);

    /** The external representation of the prefix used for private comments denoting a new time proposal */
    private static final String COMMENT_PROPOSAL_PREFIX_EXTERNAL = "\u200B\uD83D\uDDD3\u200B ";

    /** The internal representation of the prefix used for private comments denoting a new time proposal */
    private static final String COMMENT_PROPOSAL_PREFIX_INTERNAL = "\u200B\u0e4f\u200B ";

    /** The "empty" trigger used in Apple default alarms (<code>19760401T005545Z</code>) */
    private static final Trigger EMPTY_ALARM_TRIGGER = new Trigger(new Date(197168145000L));  // 19760401T005545Z

    /**
     * Initializes a new {@link EventPatches}.
     */
    private EventPatches() {
    	// prevent instantiation
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
     * {@link Incoming}
     *
     * Patches for incoming iCal files.
     *
     * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
     */
    public static final class Incoming {

        /**
         * Initializes a new {@link Incoming}.
         */
        private Incoming() {
        	// prevent instantiation
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
        private static void adjustAttendeeComments(EventResource eventResource, Event importedEvent) {
            /*
             * evaluate imported extra properties
             */
            ICalProperty iCalProperty = Tools.optICalProperty(importedEvent, "X-CALENDARSERVER-PRIVATE-COMMENT");
            if (null != iCalProperty) {
                /*
                 * take over the user's attendee comment if set
                 */
                try {
                    eventResource.getCalendarSession().getEntityResolver().prepare(importedEvent.getAttendees());
                } catch (OXException e) {
                    LOG.warn("Error preparing attendees for imported event", e);
                }
                Attendee attendee = find(importedEvent.getAttendees(), eventResource.getFactory().getUser().getId());
                if (null != attendee && false == attendee.containsComment()) {
                    attendee.setComment(iCalProperty.getValue());
                }
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
        private static void adjustSnoozeExceptions(EventResource resource, Event importedEvent, List<Event> importedChangeExceptions) {
            if (DAVUserAgent.MAC_CALENDAR.equals(resource.getUserAgent()) && isSeriesMaster(resource.getEvent()) &&
                null != importedEvent && null != importedChangeExceptions && 0 < importedChangeExceptions.size()) {
                /*
                 * check if there's a single new change exception holding only the snoozed alarm (and no further changes)
                 */
                List<Event> newChangeExceptions = getNewChangeExceptions(resource.getEvent(), importedChangeExceptions);
                if (null == newChangeExceptions || 1 != newChangeExceptions.size()) {
                    return;
                }
                Event newChangeException = newChangeExceptions.get(0);
                Alarm snoozedAlarm = null;
                Alarm snoozeAlarm = null;
                if (null != newChangeException && null != newChangeException.getAlarms()) {
                    for (Alarm alarm : newChangeException.getAlarms()) {
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
                try {
                    RecurrenceId recurrenceId = newChangeException.getRecurrenceId();
                    CalendarSession calendarSession = resource.getCalendarSession();
                    calendarSession.set(CalendarParameters.PARAMETER_FIELDS, null);
                    Event masterEvent = calendarSession.getCalendarService().getEvent(calendarSession, resource.getEvent().getFolderId(), resource.getEvent().getId());
                    Event originalOccurrence = null;
                    Iterator<Event> iterator = resource.getCalendarSession().getRecurrenceService().calculateInstances(
                        masterEvent, initCalendar(resource.getParent().getTimeZone(), recurrenceId.getValue()), null, null);
                    if (iterator.hasNext()) {
                        originalOccurrence = iterator.next();
                    }
                    if (null != originalOccurrence) {
                        if (null != originalOccurrence.getAlarms() && 1 == originalOccurrence.getAlarms().size() && snoozedAlarm.getTrigger().equals(originalOccurrence.getAlarms().get(0).getTrigger())) {
                            Alarm originalAlarm = originalOccurrence.getAlarms().get(0);
                            originalOccurrence = EventPatches.Outgoing.applyAll(resource, originalOccurrence);
                            EventUpdate eventUpdate = resource.getCalendarSession().getUtilities().compare(
                                originalOccurrence, newChangeException, true, EventField.LAST_MODIFIED, EventField.RECURRENCE_RULE, EventField.CREATED, EventField.ALARMS);
                            if (eventUpdate.getUpdatedFields().isEmpty() && eventUpdate.getAttendeeUpdates().isEmpty() && false == eventUpdate.getAlarmUpdates().isEmpty()) {
                                List<Alarm> patchedAlarms = new ArrayList<Alarm>(2);
                                snoozedAlarm.setUid(originalAlarm.getUid());
                                snoozeAlarm.setRelatedTo(new RelatedTo("SNOOZE", originalAlarm.getUid()));
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
         * Extracts those change exceptions that are considered as "new", i.e. change exceptions that do not already exist based on the
         * change exception dates of the original recurring event master.
         *
         * @param originalEvent The original recurring master event
         * @param importedChangeExceptions The imported change exceptions as supplied by the client
         * @return The new exceptions, or an empty list if there are none
         */
        private static List<Event> getNewChangeExceptions(Event originalEvent, List<Event> importedChangeExceptions) {
            if (null == importedChangeExceptions || 0 == importedChangeExceptions.size()) {
                return Collections.emptyList();
            }
            List<Event> newChangeExceptions = new ArrayList<Event>(importedChangeExceptions.size());
            List<Date> changeExceptionDates = originalEvent.getChangeExceptionDates();
            if (null == changeExceptionDates || 0 == changeExceptionDates.size()) {
                newChangeExceptions.addAll(importedChangeExceptions);
            } else {
                for (Event importedChangeException : importedChangeExceptions) {
                    RecurrenceId recurrenceId = importedChangeException.getRecurrenceId();
                    if (null != recurrenceId && false == changeExceptionDates.contains(new Date(recurrenceId.getValue()))) {
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
            Date mozSnoozeDate = Tools.optICalDateProperty(importedEvent, "X-MOZ-SNOOZE");
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
            Date mozSnoozeTime = Tools.optICalDateProperty(importedEvent, "X-MOZ-SNOOZE-TIME*");
            if (null == mozSnoozeTime || null == importedEvent.getAlarms()) {
                return;
            }
            /*
             * insert corresponding snooze alarm & establish snooze relationship
             */
            Alarm snoozeAlarm = new Alarm(new Trigger(mozSnoozeTime), AlarmAction.DISPLAY);
            snoozeAlarm.setAcknowledged(Tools.optICalDateProperty(importedEvent, "X-MOZ-LASTACK"));
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
            Date parentAcknowledged = Tools.optICalDateProperty(importedEvent, "X-MOZ-LASTACK");
            parentAcknowledged = Tools.getLatestModified(parentAcknowledged, Tools.optICalDateProperty(importedSeriesMaster, "X-MOZ-LASTACK"));
            for (Alarm alarm : importedEvent.getAlarms()) {
                Date acknowledged = Tools.getLatestModified(Tools.optICalDateProperty(alarm, "X-MOZ-LASTACK"), alarm.getAcknowledged());
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
                if (name.toLowerCase().endsWith(com.openexchange.caldav.resources.CalDAVResource.EXTENSION_ICS)) {
                    name = name.substring(0, name.length() - com.openexchange.caldav.resources.CalDAVResource.EXTENSION_ICS.length());
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
         * Applies all known patches to an event after importing.
         *
         * @param resource The parent event resource
         * @param caldavImport The CalDAV import
         * @return The patched CalDAV import
         */
        public static CalDAVImport applyAll(EventResource resource, CalDAVImport caldavImport) {
            Event importedEvent = caldavImport.getEvent();
            List<Event> importedChangeExceptions = caldavImport.getChangeExceptions();
            if (null != importedEvent) {
                /*
                 * patch the 'master' event
                 */
                applyFilename(resource, importedEvent);
                adjustAttendeeComments(resource, importedEvent);
                adjustProposedTimePrefixes(importedEvent);
                adjustSnoozeExceptions(resource, importedEvent, importedChangeExceptions);
                adjustAlarms(resource, importedEvent, null);
                applyManagedAttachments(importedEvent);
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
                }
            }
            return new CalDAVImport(resource.getUrl(), importedEvent, importedChangeExceptions);
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

        /**
         * Initializes a new {@link Outgoing}.
         */
        private Outgoing() {
        	// prevent instantiation
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
                            Map<String, String> parameters = Collections.singletonMap("X-CALENDARSERVER-ATTENDEE-REF", attendee.getUri());
                            exportedEvent = Tools.addProperty(exportedEvent, new DefaultICalProperty("X-CALENDARSERVER-ATTENDEE-COMMENT", attendee.getComment(), parameters));
                        }
                    }
                } else {
                    /*
                     * provide the current user's confirmation message
                     */
                    Attendee attendee = find(exportedEvent.getAttendees(), eventResource.getFactory().getUser().getId());
                    if (null != attendee && Strings.isNotEmpty(attendee.getComment())) {
                        exportedEvent = Tools.addProperty(exportedEvent, new DefaultICalProperty("X-CALENDARSERVER-PRIVATE-COMMENT", attendee.getComment()));
                    }
                }
            }
            return exportedEvent;
        }

        private static Alarm getEmptyDefaultAlarm() {
            Alarm alarm = new Alarm(EMPTY_ALARM_TRIGGER, AlarmAction.NONE);
            alarm.setUid(UUID.randomUUID().toString().toUpperCase());
            alarm = Tools.addProperty(alarm, new DefaultICalProperty("X-WR-ALARMUID", alarm.getUid()));
            alarm = Tools.addProperty(alarm, new DefaultICalProperty("X-APPLE-LOCAL-DEFAULT-ALARM", "TRUE"));
            alarm = Tools.addProperty(alarm, new DefaultICalProperty("X-APPLE-DEFAULT-ALARM", "TRUE"));
            return alarm;
        }

        private static Event adjustAlarms(EventResource resource, Event exportedEvent) {
            List<Alarm> exportedAlarms = exportedEvent.getAlarms();
            if (resource.getFactory().getUser().getId() != resource.getParent().getCalendarUser()) {
                /*
                 * remove alarms if event in shared folders is loaded is loaded by another user
                 */
                exportedAlarms = null;
            }
            if (null == exportedAlarms || 0 == exportedAlarms.size()) {
                /*
                 * insert a dummy alarm to prevent Apple clients from adding their own default alarms
                 */
                if (DAVUserAgent.IOS.equals(resource.getUserAgent()) || DAVUserAgent.MAC_CALENDAR.equals(resource.getUserAgent())) {
                    exportedEvent.setAlarms(Collections.singletonList(getEmptyDefaultAlarm()));
                }
            } else {
                List<Alarm> patchedAlarms = new ArrayList<Alarm>(exportedAlarms.size());
                for (Alarm exportedAlarm : exportedAlarms) {
                    Alarm alarm = exportedAlarm;
                    /*
                     * also supply the acknowledged date via X-MOZ-LASTACK
                     */
                    if (null != exportedAlarm.getAcknowledged()) {
                        DefaultICalProperty mozLastAckProperty = new DefaultICalProperty("X-MOZ-LASTACK", Tools.formatAsUTC(exportedAlarm.getAcknowledged()));
                        alarm = Tools.addProperty(exportedAlarm, mozLastAckProperty);
                        /*
                         * also store X-MOZ-LASTACK in parent component for recurring events
                         */
                        if (isSeriesMaster(exportedEvent)) {
                            exportedEvent = Tools.addProperty(exportedEvent, mozLastAckProperty);
                        }
                    }
                    Alarm snoozedAlarm = AlarmUtils.getSnoozedAlarm(alarm, exportedAlarms);
                    if (null != snoozedAlarm) {
                        /*
                         * use relative triggers for snoozed alarms in event series for apple clients
                         */
                        if (isSeriesMaster(exportedEvent) && null != alarm.getTrigger() && null != alarm.getTrigger().getDateTime() &&
                            (DAVUserAgent.IOS.equals(resource.getUserAgent()) || DAVUserAgent.MAC_CALENDAR.equals(resource.getUserAgent()))) {
                            try {
                                Trigger trigger = alarm.getTrigger();
                                trigger.setDuration(AlarmUtils.getTriggerDuration(alarm.getTrigger(), exportedEvent,
                                    resource.getFactory().requireService(RecurrenceService.class)));
                                trigger.setDateTime(null);
                            } catch (OXException e) {
                                LOG.warn("Error converting snoozed alarm trigger", e);
                            }
                        }
                        /*
                         * Thunderbird/Lightning likes to have a custom "X-MOZ-SNOOZE-TIME-<timestamp_of_recurrence>" property for recurring
                         * events, and a custom "X-MOZ-SNOOZE-TIME" property for non-recurring ones
                         */
                        if (DAVUserAgent.THUNDERBIRD_LIGHTNING.equals(resource.getUserAgent())) {
                            Date snoozeTime = AlarmUtils.getTriggerTime(alarm.getTrigger(), exportedEvent, resource.getParent().getTimeZone());
                            if (isSeriesMaster(exportedEvent) && null != snoozedAlarm.getAcknowledged()) {
                                try {
                                    RecurrenceService recurrenceService = resource.getFactory().requireService(RecurrenceService.class);
                                    Iterator<Event> iterator = recurrenceService.calculateInstancesRespectExceptions(
                                        exportedEvent, initCalendar(TimeZones.UTC, snoozedAlarm.getAcknowledged()), null, null, null);
                                    if (iterator.hasNext()) {
                                        Date relatedDate = AlarmUtils.getRelatedDate(alarm.getTrigger().getRelated(), iterator.next());
                                        String propertyName = "X-MOZ-SNOOZE-TIME-" + String.valueOf(relatedDate.getTime()) + "000";
                                        exportedEvent = Tools.addProperty(exportedEvent, new DefaultICalProperty(propertyName, Tools.formatAsUTC(snoozeTime)));
                                    }
                                } catch (OXException e) {
                                    LOG.warn("Error converting snoozed alarm trigger", e);
                                }
                            } else {
                                exportedEvent = Tools.addProperty(exportedEvent, new DefaultICalProperty("X-MOZ-SNOOZE-TIME", Tools.formatAsUTC(snoozeTime)));
                            }
                        }
                    }
                    patchedAlarms.add(alarm);
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
            List<Attendee> attendees = exportedEvent.getAttendees();
            if (null != attendees && 1 == attendees.size() && resource.getParent().getCalendarUser() == attendees.get(0).getEntity() &&
                false == PublicType.getInstance().equals(resource.getParent().getFolder().getType())) {
                exportedEvent.removeAttendees();
                exportedEvent.removeOrganizer();
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
        public static Event applyAll(EventResource resource, Event exportedEvent) {
            exportedEvent = removeImplicitAttendee(resource, exportedEvent);
            exportedEvent = adjustProposedTimePrefixes(exportedEvent);
            exportedEvent = applyAttendeeComments(resource, exportedEvent);
            exportedEvent = adjustAlarms(resource, exportedEvent);
            exportedEvent = prepareManagedAttachments(resource, exportedEvent);
            exportedEvent = removeAttachmentsFromExceptions(resource, exportedEvent);
            return exportedEvent;
        }

    }

}
