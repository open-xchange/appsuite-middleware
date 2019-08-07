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

import static com.openexchange.chronos.common.CalendarUtils.hasExternalOrganizer;
import static com.openexchange.chronos.common.CalendarUtils.initRecurrenceRule;
import static com.openexchange.chronos.common.CalendarUtils.isAttendeeSchedulingResource;
import static com.openexchange.chronos.common.CalendarUtils.isInternal;
import static com.openexchange.chronos.common.CalendarUtils.isOrganizerSchedulingResource;
import static com.openexchange.chronos.common.CalendarUtils.matches;
import static com.openexchange.chronos.impl.Utils.getCalendarUser;
import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.SchedulingControl;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultCalendarObjectResource;
import com.openexchange.chronos.common.mapping.AttendeeEventUpdate;
import com.openexchange.chronos.common.mapping.AttendeeMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.scheduling.AttachmentDataProvider;
import com.openexchange.chronos.impl.scheduling.ChangeBuilder;
import com.openexchange.chronos.impl.scheduling.MessageBuilder;
import com.openexchange.chronos.impl.scheduling.NotificationBuilder;
import com.openexchange.chronos.scheduling.ChangeNotification;
import com.openexchange.chronos.scheduling.SchedulingMessage;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.chronos.scheduling.changes.Change;
import com.openexchange.chronos.scheduling.changes.ChangeAction;
import com.openexchange.chronos.scheduling.changes.DescriptionService;
import com.openexchange.chronos.scheduling.changes.ScheduleChange;
import com.openexchange.chronos.scheduling.changes.SchedulingChangeService;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link SchedulingHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class SchedulingHelper {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SchedulingHelper.class);

    private final ServiceLookup services;
    private final CalendarSession session;
    private final CalendarUser calendarUser;
    private final CalendarFolder folder;
    private final ResultTracker tracker;
    
    /**
     * Initializes a new {@link SchedulingHelper}.
     *
     * @param services A service lookup reference
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     * @param tracker The underlying result tracker
     */
    public SchedulingHelper(ServiceLookup services, CalendarSession session, CalendarFolder folder, ResultTracker tracker) throws OXException {
        super();
        this.services = services;
        this.session = session;
        this.folder = folder;
        this.calendarUser = getCalendarUser(session, folder);
        this.tracker = tracker;
    }
    
    /**
     * Tracks notifications and scheduling messages for a newly created calendar object resource in the underlying calendar folder.
     * <p/>
     * This includes:
     * <ul>
     * <li>a {@link ChangeAction#CREATE} notification to the calendar owner if the current user acts on behalf of him</li>
     * <li>{@link ChangeAction#CREATE} notifications to internal attendees for an organizer scheduling resource</li>
     * <li>{@link SchedulingMethod#REQUEST} messages to external attendees for an organizer scheduling resource</li>
     * </ul>
     * 
     * @param createdResource The newly created calendar object resource
     */
    public void trackCreation(CalendarObjectResource createdResource) throws OXException {
        trackCreation(createdResource, null);
    }

    /**
     * Tracks notifications and scheduling messages for a newly created calendar object resource in the underlying calendar folder.
     * <p/>
     * This includes:
     * <ul>
     * <li>a {@link ChangeAction#CREATE} notification to the calendar owner if the current user acts on behalf of him</li>
     * <li>{@link ChangeAction#CREATE} notifications to internal attendees for an organizer scheduling resource</li>
     * <li>{@link SchedulingMethod#REQUEST} messages to external attendees for an organizer scheduling resource</li>
     * </ul>
     * 
     * @param createdResource The newly created calendar object resource
     * @param consideredRecipients The recipients to consider, or <code>null</code> to consider all possible recipients
     */
    public void trackCreation(CalendarObjectResource createdResource, List<? extends CalendarUser> consideredRecipients) throws OXException {
        if (false == shouldTrack(createdResource)) {
            return;
        }
        /*
         * prepare notification to calendar owner of newly created resource when acting on behalf, if enabled
         */
        if (isCalendarOwner(calendarUser) && false == isActing(calendarUser) && isNotifyOnCreate(calendarUser) && shouldTrack(calendarUser, consideredRecipients)) {
            tracker.trackChangeNotification(buildCreateNotification(createdResource, getOriginator(), calendarUser));
        }
        /*
         * prepare notifications and scheduling messages from organizer to attendees
         */
        if (isOrganizerSchedulingResource(createdResource, calendarUser.getEntity())) {
            for (Entry<Attendee, CalendarObjectResource> entry : getResourcesPerAttendee(createdResource, consideredRecipients).entrySet()) {
                Attendee recipient = entry.getKey();
                if (isInternal(recipient)) {
                    /*
                     * prepare notifications for each individual internal attendee, if enabled
                     */
                    if (CalendarUserType.INDIVIDUAL.matches(recipient.getCuType()) && false == isActing(recipient) && 
                        false == isCalendarOwner(recipient) && isNotifyOnCreate(recipient) && shouldTrack(recipient, consideredRecipients)) { 
                        tracker.trackChangeNotification(buildCreateNotification(entry.getValue(), getOriginator(), recipient));
                    }
                } else {
                    /*
                     * prepare scheduling messages for each external attendee
                     */
                    if (shouldTrack(recipient, consideredRecipients)) {
                        tracker.trackSchedulingMessage(buildCreateMessage(entry.getValue(), getOriginator(), recipient));
                    }
                }
            }
        }
    }

    /**
     * Tracks notifications and scheduling messages for an updated calendar object resource in the underlying calendar folder.
     * <p/>
     * This includes:
     * <ul>
     * <li>a {@link ChangeAction#UPDATE} notification to the calendar owner if the current user acts on behalf of him</li>
     * <li>{@link ChangeAction#UPDATE} notifications to internal attendees for an organizer scheduling resource</li>
     * <li>{@link SchedulingMethod#REQUEST} messages to external attendees for an organizer scheduling resource</li>
     * <li>{@link ChangeAction#UPDATE} notifications to internal attendees if the current user implicitly acts on behalf of the organizer</li>
     * <li>{@link SchedulingMethod#REQUEST} messages to external attendees if the current user implicitly acts on behalf of the organizer</li>
     * </ul>
     * 
     * @param updatedResource The updated calendar object resource
     * @param eventUpdate The performed event update
     */
    public void trackUpdate(CalendarObjectResource updatedResource, EventUpdate eventUpdate) throws OXException {
        trackUpdate(updatedResource, eventUpdate, null);
    }

    /**
     * Tracks notifications and scheduling messages for an updated calendar object resource in the underlying calendar folder.
     * <p/>
     * This includes:
     * <ul>
     * <li>a {@link ChangeAction#UPDATE} notification to the calendar owner if the current user acts on behalf of him</li>
     * <li>{@link ChangeAction#UPDATE} notifications to internal attendees for an organizer scheduling resource</li>
     * <li>{@link SchedulingMethod#REQUEST} messages to external attendees for an organizer scheduling resource</li>
     * <li>{@link ChangeAction#UPDATE} notifications to internal attendees if the current user implicitly acts on behalf of the organizer</li>
     * <li>{@link SchedulingMethod#REQUEST} messages to external attendees if the current user implicitly acts on behalf of the organizer</li>
     * </ul>
     * 
     * @param updatedResource The updated calendar object resource
     * @param eventUpdate The performed event update
     * @param consideredRecipients The recipients to consider, or <code>null</code> to consider all possible recipients
     */
    public void trackUpdate(CalendarObjectResource updatedResource, EventUpdate eventUpdate, List<? extends CalendarUser> consideredRecipients) throws OXException {
        trackUpdate(updatedResource, Collections.singletonList(eventUpdate), consideredRecipients);
    }

    /**
     * Tracks notifications and scheduling messages for an updated calendar object resource in the underlying calendar folder.
     * <p/>
     * This includes:
     * <ul>
     * <li>a {@link ChangeAction#UPDATE} notification to the calendar owner if the current user acts on behalf of him</li>
     * <li>{@link ChangeAction#UPDATE} notifications to internal attendees for an organizer scheduling resource</li>
     * <li>{@link SchedulingMethod#REQUEST} messages to external attendees for an organizer scheduling resource</li>
     * <li>{@link ChangeAction#UPDATE} notifications to internal attendees if the current user implicitly acts on behalf of the organizer</li>
     * <li>{@link SchedulingMethod#REQUEST} messages to external attendees if the current user implicitly acts on behalf of the organizer</li>
     * </ul>
     * 
     * @param updatedResource The updated calendar object resource
     * @param eventUpdates The list of performed event updates
     * @param consideredRecipients The recipients to consider, or <code>null</code> to consider all possible recipients
     */
    private void trackUpdate(CalendarObjectResource updatedResource, List<EventUpdate> eventUpdates, List<? extends CalendarUser> consideredRecipients) throws OXException {
        if (false == shouldTrack(updatedResource, eventUpdates)) {
            return;
        }
        /*
         * prepare notification to calendar owner of updated resource when acting on behalf, if enabled
         */
        if (isCalendarOwner(calendarUser) && false == isActing(calendarUser) && isNotifyOnUpdate(calendarUser) && shouldTrack(calendarUser, consideredRecipients)) {
            tracker.trackChangeNotification(buildUpdateNotification(updatedResource, eventUpdates, getOriginator(), calendarUser));
        }
        if (isOrganizerSchedulingResource(updatedResource, calendarUser.getEntity())) {
            /*
             * prepare notifications and scheduling messages from organizer to attendees
             */
            for (Entry<Attendee, CalendarObjectResource> entry : getResourcesPerAttendee(updatedResource, consideredRecipients).entrySet()) {
                Attendee recipient = entry.getKey();
                if (isInternal(recipient)) {
                    /*
                     * prepare notifications for each individual internal attendee, if enabled
                     */
                    if (CalendarUserType.INDIVIDUAL.matches(recipient.getCuType()) && false == isActing(recipient) && 
                        false == isCalendarOwner(recipient) && isNotifyOnUpdate(recipient) && shouldTrack(recipient, consideredRecipients)) {
                        tracker.trackChangeNotification(buildUpdateNotification(entry.getValue(), eventUpdates, getOriginator(), recipient));
                    }
                } else {
                    /*
                     * prepare scheduling messages for each external attendee
                     */
                    if (shouldTrack(recipient, consideredRecipients)) {
                        tracker.trackSchedulingMessage(buildUpdateMessage(entry.getValue(), eventUpdates, getOriginator(), recipient));
                    }
                }
            }
        } else if (hasExternalOrganizer(updatedResource)) {
            /*
             * prepare counter proposal to external organizer once this is allowed, for now this path should not be possible
             */
            throw new UnsupportedOperationException("COUNTER not implemented");
        } else {
            /*
             * prepare notifications and scheduling messages from attendee acting on behalf of the organizer to attendees
             */
            CalendarUser originator = session.getEntityResolver().applyEntityData(new Organizer(), updatedResource.getOrganizer().getEntity());
            originator.setSentBy(getOriginator());
            for (Entry<Attendee, CalendarObjectResource> entry : getResourcesPerAttendee(updatedResource, consideredRecipients).entrySet()) {
                Attendee recipient = entry.getKey();
                if (isInternal(recipient)) {
                    /*
                     * prepare notifications for each individual internal attendee, if enabled
                     */
                    if (CalendarUserType.INDIVIDUAL.matches(recipient.getCuType()) && false == isActing(recipient) && 
                        false == isCalendarOwner(recipient) && isNotifyOnUpdate(recipient) && shouldTrack(recipient, consideredRecipients)) {
                        tracker.trackChangeNotification(buildUpdateNotification(entry.getValue(), eventUpdates, originator, recipient));
                    }
                } else {
                    /*
                     * prepare scheduling messages for each external attendee
                     */
                    if (shouldTrack(recipient, consideredRecipients)) {
                        tracker.trackSchedulingMessage(buildUpdateMessage(entry.getValue(), eventUpdates, originator, recipient));
                    }
                }
            }
        }
    }

    /**
     * Tracks notifications and scheduling messages for a deleted calendar object resource in the underlying calendar folder, handling both
     * attendee- and organizer scheduling resources.
     * <p/>
     * This includes:
     * <ul>
     * <li>a {@link ChangeAction#CANCEL} notification to the calendar owner if the current user acts on behalf of him</li>
     * <li>{@link ChangeAction#CANCEL} notifications to internal attendees for an organizer scheduling resource</li>
     * <li>{@link SchedulingMethod#CANCEL} messages to external attendees for an organizer scheduling resource</li>
     * <li>a {@link ChangeAction#REPLY} notification to an internal organizer for an attendee scheduling resource</li>
     * <li>a {@link SchedulingMethod#REPLY} message to an external organizer for an attendee scheduling resource</li>
     * <li>{@link ChangeAction#CANCEL} notifications to other internal attendees for an attendee scheduling resource</li>
     * </ul>
     * 
     * @param deletedResource The deleted calendar object resource
     */
    public void trackDeletion(CalendarObjectResource deletedResource) throws OXException {
        trackDeletion(deletedResource, null);
    }

    /**
     * Tracks notifications and scheduling messages for a deleted calendar object resource in the underlying calendar folder, handling both
     * attendee- and organizer scheduling resources.
     * <p/>
     * This includes:
     * <ul>
     * <li>a {@link ChangeAction#CANCEL} notification to the calendar owner if the current user acts on behalf of him</li>
     * <li>{@link ChangeAction#CANCEL} notifications to internal attendees for an organizer scheduling resource</li>
     * <li>{@link SchedulingMethod#CANCEL} messages to external attendees for an organizer scheduling resource</li>
     * <li>a {@link ChangeAction#REPLY} notification to an internal organizer for an attendee scheduling resource</li>
     * <li>a {@link SchedulingMethod#REPLY} message to an external organizer for an attendee scheduling resource</li>
     * <li>{@link ChangeAction#CANCEL} notifications to other internal attendees for an attendee scheduling resource</li>
     * </ul>
     * 
     * @param deletedResource The deleted calendar object resource
     * @param consideredRecipients The recipients to consider, or <code>null</code> to consider all possible recipients
     */
    public void trackDeletion(CalendarObjectResource deletedResource, List<? extends CalendarUser> consideredRecipients) throws OXException {
        if (false == shouldTrack(deletedResource)) {
            return;
        }
        /*
         * prepare notification to calendar owner of deleted resource when acting on behalf, if enabled
         */
        if (isCalendarOwner(calendarUser) && false == isActing(calendarUser) && isNotifyOnDelete(calendarUser) && shouldTrack(calendarUser, consideredRecipients)) {
            tracker.trackChangeNotification(buildCancelNotification(deletedResource, getOriginator(), calendarUser));
        }            
        /*
         * prepare notifications and scheduling messages from organizer to attendees
         */
        if (isOrganizerSchedulingResource(deletedResource, calendarUser.getEntity())) {
            for (Entry<Attendee, CalendarObjectResource> entry : getResourcesPerAttendee(deletedResource, consideredRecipients).entrySet()) {
                Attendee recipient = entry.getKey();
                if (isInternal(recipient)) {
                    /*
                     * prepare notifications for each individual internal attendee, if enabled
                     */
                    if (CalendarUserType.INDIVIDUAL.matches(recipient.getCuType()) && false == isActing(recipient) && 
                        false == isCalendarOwner(recipient) && isNotifyOnDelete(recipient) && shouldTrack(recipient, consideredRecipients)) {
                        tracker.trackChangeNotification(buildCancelNotification(entry.getValue(), getOriginator(), recipient));
                    }
                } else {
                    /*
                     * prepare scheduling messages for each external attendee
                     */
                    if (shouldTrack(recipient, consideredRecipients)) {
                        tracker.trackSchedulingMessage(buildCancelMessage(entry.getValue(), getOriginator(), recipient));
                    }
                }
            }
        }
        /*
         * prepare notifications and scheduling messages from attendee to organizer
         */
        if (isAttendeeSchedulingResource(deletedResource, calendarUser.getEntity())) {
            CalendarUser recipient = deletedResource.getOrganizer();
            if (isInternal(recipient, CalendarUserType.INDIVIDUAL)) {
                /*
                 * prepare reply notification to internal organizer, if enabled
                 */
                if (false == isActing(calendarUser) && isNotifyOnReply(recipient) && shouldTrack(recipient, consideredRecipients)) {
                    tracker.trackChangeNotification(buildReplyNotification(deletedResource, getOriginator(), recipient, ParticipationStatus.DECLINED, optSchedulingComment()));
                }
            } else {
                /*
                 * prepare scheduling reply to external organizer
                 */
                if (shouldTrack(recipient, consideredRecipients)) {
                    tracker.trackSchedulingMessage(buildReplyMessage(deletedResource, getOriginator(), recipient, ParticipationStatus.DECLINED, optSchedulingComment()));
                }
            }
        }
        if (isAttendeeSchedulingResource(deletedResource, calendarUser.getEntity())) {
            /*
             * prepare notifications for each individual internal attendee, if enabled
             */            
            for (Entry<Attendee, CalendarObjectResource> entry : getResourcesPerAttendee(deletedResource, true).entrySet()) {
                Attendee recipient = entry.getKey();
                if (CalendarUserType.INDIVIDUAL.matches(recipient.getCuType()) && 
                    false == isActing(recipient) && false == isCalendarOwner(recipient) && isNotifyOnReplyAsAttendee(recipient) && shouldTrack(recipient, consideredRecipients)) {
                    tracker.trackChangeNotification(buildReplyNotification(deletedResource, getOriginator(), recipient, ParticipationStatus.DECLINED, optSchedulingComment()));
                }
            }
        }
    }

    /**
     * Tracks notifications and scheduling messages for a single updated event in the underlying calendar folder, after the participation
     * status was changed.
     * <p/>
     * This includes:
     * <ul>
     * <li>a {@link ChangeAction#REPLY} notification to the calendar owner if the current user acts on behalf of him</li>
     * <li>a {@link ChangeAction#REPLY} notification to an internal organizer for an attendee scheduling resource</li>
     * <li>a {@link SchedulingMethod#REPLY} message to an external organizer for an attendee scheduling resource</li>
     * <li>{@link ChangeAction#REPLY} notifications to other internal attendees for an attendee scheduling resource</li>
     * </ul>
     * 
     * @param updatedEvent The updated event
     * @param originalAttendee The original attendee
     * @param updatedAttendee The updated attendee
     */
    public void trackReply(Event updatedEvent, Attendee originalAttendee, Attendee updatedAttendee) throws OXException {
        trackReply(new DefaultCalendarObjectResource(updatedEvent), new AttendeeEventUpdate(updatedEvent, originalAttendee, updatedAttendee));
    }

    /**
     * Tracks notifications and scheduling messages for an updated calendar object resource in the underlying calendar folder, after the
     * participation status was changed.
     * <p/>
     * This includes:
     * <ul>
     * <li>a {@link ChangeAction#REPLY} notification to the calendar owner if the current user acts on behalf of him</li>
     * <li>a {@link ChangeAction#REPLY} notification to an internal organizer for an attendee scheduling resource</li>
     * <li>a {@link SchedulingMethod#REPLY} message to an external organizer for an attendee scheduling resource</li>
     * <li>{@link ChangeAction#REPLY} notifications to other internal attendees for an attendee scheduling resource</li>
     * </ul>
     * 
     * @param updatedResource The updated calendar object resource
     * @param attendeeEventUpdate The performed attendee event update
     */
    public void trackReply(CalendarObjectResource updatedResource, EventUpdate attendeeEventUpdate) throws OXException {
        trackReply(updatedResource, Collections.singletonList(attendeeEventUpdate));
    }
    
    /**
     * Tracks notifications and scheduling messages for an updated calendar object resource in the underlying calendar folder, after the
     * participation status was changed.
     * <p/>
     * This includes:
     * <ul>
     * <li>a {@link ChangeAction#REPLY} notification to the calendar owner if the current user acts on behalf of him</li>
     * <li>a {@link ChangeAction#REPLY} notification to an internal organizer for an attendee scheduling resource</li>
     * <li>a {@link SchedulingMethod#REPLY} message to an external organizer for an attendee scheduling resource</li>
     * <li>{@link ChangeAction#REPLY} notifications to other internal attendees for an attendee scheduling resource</li>
     * </ul>
     * 
     * @param updatedResource The updated calendar object resource
     * @param attendeeEventUpdates The list of performed attendee event updates
     */
    public void trackReply(CalendarObjectResource updatedResource, List<EventUpdate> attendeeEventUpdates) throws OXException {
        if (false == shouldTrack(updatedResource, attendeeEventUpdates)) {
            return;
        }
        /*
         * prepare notification to calendar owner of updated resource when acting on behalf, if enabled
         */
        if (isCalendarOwner(calendarUser) && false == isActing(calendarUser) && isNotifyOnReply(calendarUser)) {
            tracker.trackChangeNotification(buildReplyNotification(updatedResource, attendeeEventUpdates, getOriginator(), calendarUser));
        }            
        /*
         * prepare notifications and scheduling messages from attendee to organizer
         */
        if (isAttendeeSchedulingResource(updatedResource, calendarUser.getEntity())) {
            CalendarUser recipient = updatedResource.getOrganizer();
            if (isInternal(recipient, CalendarUserType.INDIVIDUAL)) {
                /*
                 * prepare reply notification to internal organizer, if enabled
                 */
                if (false == isActing(recipient) && isNotifyOnReply(recipient)) {
                    tracker.trackChangeNotification(buildReplyNotification(updatedResource, attendeeEventUpdates, getOriginator(), recipient));
                }
            } else {
                /*
                 * prepare scheduling reply to external organizer
                 */
                tracker.trackSchedulingMessage(buildReplyMessage(updatedResource, attendeeEventUpdates, getOriginator(), recipient));
            }
        }
        if (isAttendeeSchedulingResource(updatedResource, calendarUser.getEntity())) {
            /*
             * prepare notifications for each individual internal attendee, if enabled
             */            
            for (Entry<Attendee, CalendarObjectResource> entry : getResourcesPerAttendee(updatedResource, true).entrySet()) {
                Attendee recipient = entry.getKey();
                if (CalendarUserType.INDIVIDUAL.matches(recipient.getCuType()) && false == matches(updatedResource.getOrganizer(), recipient) &&
                    false == isActing(recipient) && false == isCalendarOwner(recipient) && isNotifyOnReplyAsAttendee(recipient)) {
                    tracker.trackChangeNotification(buildReplyNotification(updatedResource, attendeeEventUpdates, getOriginator(), recipient));
                }
            }
        }
    }

    private ChangeNotification buildCreateNotification(CalendarObjectResource createdResource, CalendarUser originator, CalendarUser recipient) throws OXException {
        //@formatter:off
        ScheduleChange scheduleChange = getSchedulingChangeService().describeCreationRequest(
            session.getContextId(), originator, recipient, optSchedulingComment(), createdResource.getEvents());
        return new NotificationBuilder()
            .setMethod(ChangeAction.CREATE)
            .setOriginator(originator)
            .setRecipient(recipient)
            .setResource(createdResource)
            .setScheduleChange(scheduleChange)
        .build();
        //@formatter:on
    }

    private ChangeNotification buildUpdateNotification(CalendarObjectResource updatedResource, List<EventUpdate> eventUpdates, CalendarUser originator, CalendarUser recipient) throws OXException {
        //@formatter:off
        List<Change> changeDescriptions = getChangeDescriptions(eventUpdates, originator, recipient);
        ScheduleChange scheduleChange = getSchedulingChangeService().describeUpdateRequest(
            session.getContextId(), originator, recipient, optSchedulingComment(), updatedResource.getEvents(), changeDescriptions);
        return new NotificationBuilder()
            .setMethod(ChangeAction.UPDATE)
            .setOriginator(originator)
            .setRecipient(recipient)
            .setResource(updatedResource)
            .setScheduleChange(scheduleChange)
        .build();
        //@formatter:on
    }

    private ChangeNotification buildCancelNotification(CalendarObjectResource deletedResource, CalendarUser originator, CalendarUser recipient) throws OXException {
        //@formatter:off
        ScheduleChange scheduleChange = getSchedulingChangeService().describeCancel(
            session.getContextId(), originator, recipient, optSchedulingComment(), deletedResource.getEvents());
        return new NotificationBuilder()
            .setMethod(ChangeAction.CANCEL)
            .setOriginator(originator)
            .setRecipient(recipient)
            .setResource(deletedResource)
            .setScheduleChange(scheduleChange)
        .build();
        //@formatter:on
    }

    private ChangeNotification buildReplyNotification(CalendarObjectResource resource, CalendarUser originator, CalendarUser recipient, ParticipationStatus partStat, String comment) throws OXException {
        //@formatter:off
        List<Event> updatedEvents = new ArrayList<Event>();
        List<Change> changeDescriptions = new ArrayList<Change>();
        for (Event event : resource.getEvents()) {
            EventUpdate eventUpdate = overridePartStat(event, originator, partStat, comment);
            changeDescriptions.add(getChangeDescriptionsFor(eventUpdate, originator, recipient, EventField.ATTENDEES));
            updatedEvents.add(eventUpdate.getUpdate());
        }
        ScheduleChange scheduleChange = getSchedulingChangeService().describeReply(
            session.getContextId(), originator, recipient, comment, updatedEvents, changeDescriptions, partStat);
        return new NotificationBuilder()
            .setMethod(ChangeAction.REPLY)
            .setOriginator(originator)
            .setRecipient(recipient)
            .setResource(new DefaultCalendarObjectResource(updatedEvents))
            .setScheduleChange(scheduleChange)
        .build();
        //@formatter:on
    }

    private ChangeNotification buildReplyNotification(CalendarObjectResource updatedResource, List<EventUpdate> attendeeEventUpdates, CalendarUser originator, CalendarUser recipient) throws OXException {
        //@formatter:off
        List<Change> changeDescriptions = getChangeDescriptionsFor(attendeeEventUpdates, originator, recipient, EventField.ATTENDEES);
        Attendee matchingAttendee = extractAttendee(updatedResource, originator);
        String comment = null != matchingAttendee ? matchingAttendee.getComment() : null;
        ScheduleChange scheduleChange = getSchedulingChangeService().describeReply(
            session.getContextId(), originator, recipient, comment, updatedResource.getEvents(), changeDescriptions, 
            null != matchingAttendee ? matchingAttendee.getPartStat() : null);
        return new NotificationBuilder()
            .setMethod(ChangeAction.REPLY)
            .setOriginator(originator)
            .setRecipient(recipient)
            .setResource(updatedResource)
            .setScheduleChange(scheduleChange)
        .build();
        //@formatter:on
    }

    private SchedulingMessage buildCreateMessage(CalendarObjectResource createdResource, CalendarUser originator, CalendarUser recipient) throws OXException {
        //@formatter:off
        ScheduleChange scheduleChange = getSchedulingChangeService().describeCreationRequest(
            session.getContextId(), originator, recipient, optSchedulingComment(), createdResource.getEvents());
        return new MessageBuilder()
            .setMethod(SchedulingMethod.REQUEST)
            .setOriginator(originator)
            .setRecipient(recipient)
            .setResource(createdResource)
            .setScheduleChange(scheduleChange)
            .setAttachmentDataProvider(getAttachmentDataProvider())
        .build();
        //@formatter:on
    }
    
    private SchedulingMessage buildUpdateMessage(CalendarObjectResource updatedResource, List<EventUpdate> eventUpdates, CalendarUser originator, CalendarUser recipient) throws OXException {
        //@formatter:off
        List<Change> changeDescriptions = getChangeDescriptions(eventUpdates, originator, recipient);
        ScheduleChange scheduleChange = getSchedulingChangeService().describeUpdateRequest(
            session.getContextId(), originator, recipient, optSchedulingComment(), updatedResource.getEvents(), changeDescriptions);
        return new MessageBuilder()
            .setMethod(SchedulingMethod.REQUEST)
            .setOriginator(originator)
            .setRecipient(recipient)
            .setResource(updatedResource)
            .setScheduleChange(scheduleChange)
            .setAttachmentDataProvider(getAttachmentDataProvider())
        .build();
        //@formatter:on
    }

    private SchedulingMessage buildCancelMessage(CalendarObjectResource deletedResource, CalendarUser originator, CalendarUser recipient) throws OXException {
        //@formatter:off
        ScheduleChange scheduleChange = getSchedulingChangeService().describeCancel(
            session.getContextId(), originator, recipient, optSchedulingComment(), deletedResource.getEvents());
        return new MessageBuilder()
            .setMethod(SchedulingMethod.CANCEL)
            .setOriginator(originator)
            .setRecipient(recipient)
            .setResource(deletedResource)
            .setScheduleChange(scheduleChange)
        .build();
        //@formatter:on
    }

    private SchedulingMessage buildReplyMessage(CalendarObjectResource resource, CalendarUser originator, CalendarUser recipient, ParticipationStatus partStat, String comment) throws OXException {
        //@formatter:off
        List<Event> updatedEvents = new ArrayList<Event>();
        List<Change> changeDescriptions = new ArrayList<Change>();
        for (Event event : resource.getEvents()) {
            EventUpdate eventUpdate = overridePartStat(event, originator, partStat, comment);
            changeDescriptions.add(getChangeDescriptionsFor(eventUpdate, originator, recipient, EventField.ATTENDEES));
            updatedEvents.add(eventUpdate.getUpdate());
        }
        ScheduleChange scheduleChange = getSchedulingChangeService().describeReply(
            session.getContextId(), originator, recipient, comment, updatedEvents, changeDescriptions, partStat);
        return new MessageBuilder()
            .setMethod(SchedulingMethod.REPLY)
            .setOriginator(originator)
            .setRecipient(recipient)
            .setResource(new DefaultCalendarObjectResource(updatedEvents))
            .setScheduleChange(scheduleChange)
        .build();
        //@formatter:on
    }

    private SchedulingMessage buildReplyMessage(CalendarObjectResource updatedResource, List<EventUpdate> attendeeEventUpdates, CalendarUser originator, CalendarUser recipient) throws OXException {
        //@formatter:off
        List<Change> changeDescriptions = getChangeDescriptionsFor(attendeeEventUpdates, originator, recipient, EventField.ATTENDEES);
        Attendee matchingAttendee = extractAttendee(updatedResource, originator);
        String comment = null != matchingAttendee ? matchingAttendee.getComment() : null;
        ScheduleChange scheduleChange = getSchedulingChangeService().describeReply(
            session.getContextId(), originator, recipient, comment, updatedResource.getEvents(), changeDescriptions, 
            null != matchingAttendee ? matchingAttendee.getPartStat() : null);
        return new MessageBuilder()
            .setMethod(SchedulingMethod.REPLY)
            .setOriginator(originator)
            .setRecipient(recipient)
            .setResource(updatedResource)
            .setScheduleChange(scheduleChange)
        .build();
        //@formatter:on
    }

    /**
     * Associates attendees of a calendar object resource to those events within the resource they are actually attending, resulting in
     * individual views of the calendar object resource.
     * 
     * @param resource The calendar object resource to get the individual views for
     * @param internalOnly <code>true</code> to only consider internal attendees, <code>false</code>, otherwise
     * @return The individual views on the calendar object resource per attendee
     */
    private Map<Attendee, CalendarObjectResource> getResourcesPerAttendee(CalendarObjectResource resource, boolean internalOnly) {
        return getResourcesPerAttendee(resource, internalOnly, null);
    }

    /**
     * Associates attendees of a calendar object resource to those events within the resource they are actually attending, resulting in
     * individual views of the calendar object resource.
     * 
     * @param resource The calendar object resource to get the individual views for
     * @param consideredRecipients The recipients to consider, or <code>null</code> to consider all possible recipients
     * @return The individual views on the calendar object resource per attendee
     */
    private Map<Attendee, CalendarObjectResource> getResourcesPerAttendee(CalendarObjectResource resource, Collection<? extends CalendarUser> consideredRecipients) {
        return getResourcesPerAttendee(resource, false, consideredRecipients);
    }
    
    /**
     * Associates attendees of a calendar object resource to those events within the resource they are actually attending, resulting in
     * individual views of the calendar object resource.
     * 
     * @param resource The calendar object resource to get the individual views for
     * @param internalOnly <code>true</code> to only consider internal attendees, <code>false</code>, otherwise
     * @param consideredRecipients The recipients to consider, or <code>null</code> to consider all possible recipients
     * @return The individual views on the calendar object resource per attendee
     */
    private Map<Attendee, CalendarObjectResource> getResourcesPerAttendee(CalendarObjectResource resource, boolean internalOnly, Collection<? extends CalendarUser> consideredRecipients) {
        Map<Integer, List<Event>> eventsPerEntity = new HashMap<Integer, List<Event>>();
        Map<Integer, Attendee> attendeesPerEntity = new HashMap<Integer, Attendee>();
        Map<String, List<Event>> eventsPerUri = new HashMap<String, List<Event>>();
        Map<String, Attendee> attendeesPerUri = new HashMap<String, Attendee>();
        for (Event event : resource.getEvents()) {
            if (null == event.getAttendees()) {
                continue;
            }
            for (Attendee attendee : event.getAttendees()) {
                if (null == consideredRecipients || CalendarUtils.contains(consideredRecipients, attendee)) {
                    if (isInternal(attendee) && false == attendee.isHidden()) {
                        com.openexchange.tools.arrays.Collections.put(eventsPerEntity, I(attendee.getEntity()), event);
                        attendeesPerEntity.put(I(attendee.getEntity()), attendee);
                    } else if (false == internalOnly) {
                        com.openexchange.tools.arrays.Collections.put(eventsPerUri, attendee.getUri(), event);
                        attendeesPerUri.put(attendee.getUri(), attendee);
                    }
                }
            }
        }
        Map<Attendee, CalendarObjectResource> resourcesPerUserAttendee = new HashMap<Attendee, CalendarObjectResource>(eventsPerEntity.size() + eventsPerUri.size());
        for (Entry<Integer, List<Event>> entry : eventsPerEntity.entrySet()) {
            resourcesPerUserAttendee.put(attendeesPerEntity.get(entry.getKey()), new DefaultCalendarObjectResource(entry.getValue()));
        }
        for (Entry<String, List<Event>> entry : eventsPerUri.entrySet()) {
            resourcesPerUserAttendee.put(attendeesPerUri.get(entry.getKey()), new DefaultCalendarObjectResource(entry.getValue()));
        }
        return resourcesPerUserAttendee;
    }

    private String optSchedulingComment() {
        return session.get(CalendarParameters.PARAMETER_COMMENT, String.class);
    }

    private AttachmentDataProvider getAttachmentDataProvider() {
        return new AttachmentDataProvider(services, session.getContextId());
    }

    private SchedulingChangeService getSchedulingChangeService() {
        return services.getService(SchedulingChangeService.class);
    }

    private DescriptionService getDescriptionService() {
        return services.getService(DescriptionService.class);
    }

    private CalendarUser getOriginator() throws OXException {
        CalendarUser originator = getCalendarUser(session, folder);
        if (session.getUserId() != originator.getEntity()) {
            originator.setSentBy(session.getEntityResolver().applyEntityData(new CalendarUser(), session.getUserId()));
        }
        return originator;
    }
    
    private boolean shouldTrack(CalendarObjectResource resource) {
        return shouldTrack(resource, null);
    }

    /**
     * Gets a value indicating whether scheduling messages and notification to a particular calendar user should be tracked or not, based
     * on the optional {@link CalendarParameters#PARAMETER_SCHEDULING} parameter, or an explicitly supplied whitelist of recipients.
     * 
     * @param calendarUser The calendar user to check
     * @param consideredRecipients The recipients to consider, or <code>null</code> to consider all possible recipients
     * @return <code>true</code> if scheduling messages and notifications should be tracked, <code>false</code>, otherwise
     */
    private boolean shouldTrack(CalendarUser calendarUser, Collection<? extends CalendarUser> consideredRecipients) {
        /*
         * don't track if not considered explicitly
         */
        if (null != consideredRecipients && false == CalendarUtils.contains(consideredRecipients, calendarUser)) {
            LOG.trace("Recipient not considered explicitly, skip tracking of notifications and scheduling messages for {}.", calendarUser);
            return false;
        }
        /*
         * don't track if scheduling is forcibly suppressed
         */
        SchedulingControl schedulingControl = session.get(CalendarParameters.PARAMETER_SCHEDULING, SchedulingControl.class);
        if (SchedulingControl.NONE.matches(schedulingControl)) {
            LOG.trace("Scheduling is forcibly suppressed via {}, skip tracking of notifications and scheduling messages.", schedulingControl);
            return false;
        }
        if (SchedulingControl.INTERNAL_ONLY.matches(schedulingControl) && false == isInternal(calendarUser, CalendarUserType.INDIVIDUAL)) {
            LOG.trace("Scheduling to non-internal recipients is forcibly suppressed via {}, skip tracking of notifications and scheduling messages for {}.", schedulingControl, calendarUser);
            return false;
        }
        if (SchedulingControl.EXTERNAL_ONLY.matches(schedulingControl) && isInternal(calendarUser, CalendarUserType.INDIVIDUAL)) {
            LOG.trace("Scheduling to non-external recipients is forcibly suppressed via {}, skip tracking of notifications and scheduling messages for {}.", schedulingControl, calendarUser);
            return false;
        }
        /*
         * do track, otherwise
         */
        return true;
    }

    /**
     * Gets a value indicating whether scheduling messages and notifications should be tracked or not, based on the underlying events, and
     * the optional parameter {@link CalendarParameters#PARAMETER_SCHEDULING}.
     * 
     * @param resource The event resource where scheduling messages are tracked for
     * @param eventUpdates The corresponding event update, or <code>null</code> if not applicable
     * @return <code>true</code> if scheduling messages and notifications should be tracked, <code>false</code>, otherwise
     */
    private boolean shouldTrack(CalendarObjectResource resource, List<EventUpdate> eventUpdates) {
        /*
         * don't track if affected events end in the past
         */
        try {
            if (endsInPast(resource) && (null != eventUpdates && endsInPast(eventUpdates))) {
                LOG.trace("{} ends in past, skip tracking of notifications and scheduling messages.", resource);
                return false;
            }
        } catch (Exception e) {
            LOG.error("Unexpected error checking if events end in the past, assuming they do not.", e);
        }
        /*
         * don't track if scheduling is forcibly suppressed
         */
        SchedulingControl schedulingControl = session.get(CalendarParameters.PARAMETER_SCHEDULING, SchedulingControl.class);
        if (SchedulingControl.NONE.matches(schedulingControl)) {
            LOG.trace("Scheduling is forcibly suppressed via {}, skip tracking of notifications and scheduling messages.", schedulingControl);
            return false;
        }
        /*
         * do track, otherwise
         */
        return true;
    }

    private boolean endsInPast(CalendarObjectResource resource) throws OXException {
        DateTime now = new DateTime(System.currentTimeMillis());
        TimeZone timeZone = session.getEntityResolver().getTimeZone(calendarUser.getEntity());
        for (Event event : resource.getEvents()) {
            if (false == endsInPast(event, now, timeZone)) {
                return false;
            }
        }
        return true;
    }

    private boolean endsInPast(List<EventUpdate> eventUpdates) throws OXException {
        DateTime now = new DateTime(System.currentTimeMillis());
        TimeZone timeZone = session.getEntityResolver().getTimeZone(calendarUser.getEntity());
        for (EventUpdate eventUpdate : eventUpdates) {
            if (false == endsInPast(eventUpdate, now, timeZone)) {
                return false;
            }
        }
        return true;
    }

    private boolean endsInPast(EventUpdate eventUpdate, DateTime now, TimeZone timeZone) throws OXException {
        if (null != eventUpdate.getOriginal() && false == endsInPast(eventUpdate.getOriginal(), now, timeZone)) {
            return false;
        }
        if (null != eventUpdate.getUpdate() && false == endsInPast(eventUpdate.getUpdate(), now, timeZone)) {
            return false;
        }
        return true;
    }

    private boolean endsInPast(Event event, DateTime now, TimeZone timeZone) throws OXException {
        /*
         * check event end time (in given timezone if floating)
         */
        DateTime dtNow = null != now ? now : new DateTime(System.currentTimeMillis());
        DateTime eventEnd = getEndDate(event);
        /*
         * if series master event, check end of recurrence
         */
        if (null != event.getRecurrenceRule()) {
            RecurrenceRule rule = initRecurrenceRule(event.getRecurrenceRule());
            if (null != rule.getUntil()) {
                eventEnd = rule.getUntil(); // fixed until
            } else if (null != rule.getCount()) {
                RecurrenceIterator<Event> iterator = session.getRecurrenceService().iterateEventOccurrences(event, null, null);
                while (iterator.hasNext()) {
                    eventEnd = getEndDate(iterator.next());
                    if (eventEnd.after(dtNow) && (false == eventEnd.isFloating() || null == timeZone || eventEnd.swapTimeZone(timeZone).after(dtNow))) {
                        break; // this occurrence already ends after now
                    }
                }
            } else {
                return false; // infinite recurrence
            }
        }
        if (eventEnd.isFloating() && null != timeZone) {
            eventEnd = eventEnd.swapTimeZone(timeZone);
        }
        return eventEnd.before(dtNow);
    }

    private boolean isNotifyOnCreate(CalendarUser calendarUser) {
        return session.getConfig().isNotifyOnCreate(calendarUser.getEntity());
    }

    private boolean isNotifyOnUpdate(CalendarUser calendarUser) {
        return session.getConfig().isNotifyOnUpdate(calendarUser.getEntity());
    }

    private boolean isNotifyOnDelete(CalendarUser calendarUser) {
        return session.getConfig().isNotifyOnDelete(calendarUser.getEntity());
    }

    private boolean isNotifyOnReply(CalendarUser calendarUser) {
        return session.getConfig().isNotifyOnReply(calendarUser.getEntity());
    }

    private boolean isNotifyOnReplyAsAttendee(CalendarUser calendarUser) {
        return session.getConfig().isNotifyOnReplyAsAttendee(calendarUser.getEntity());
    }

    private boolean isActing(CalendarUser calendarUser) {
        return isActing(calendarUser.getEntity());
    }

    private boolean isActing(int userId) {
        return session.getUserId() == userId;
    }

    private boolean isCalendarOwner(CalendarUser calendarUser) {
        return isCalendarOwner(calendarUser.getEntity());
    }

    private boolean isCalendarOwner(int userId) {
        return calendarUser.getEntity() == userId;
    }

    /**
     * Initializes an event update that indicates an updated participation status of the attendee matching a specific calendar user.
     * 
     * @param event The event to override the participation status update in
     * @param calendarUser The calendar user to override the participation status for
     * @param partStat The participation status to indicate for the matching attendee
     * @param comment An optional comment from the attendee to indicate
     * @return An event update that indicates the updated participation status accordingly
     */
    private static AttendeeEventUpdate overridePartStat(Event event, CalendarUser calendarUser, ParticipationStatus partStat, String comment) throws OXException {
        Attendee originalAttendee = CalendarUtils.find(event.getAttendees(), calendarUser);
        if (null == originalAttendee) {
            throw CalendarExceptionCodes.ATTENDEE_NOT_FOUND.create(I(calendarUser.getEntity()), event.getId());
        }
        Attendee updatedAttendee = AttendeeMapper.getInstance().copy(originalAttendee, null, AttendeeField.ENTITY, AttendeeField.MEMBER, AttendeeField.CU_TYPE, AttendeeField.URI);
        updatedAttendee.setPartStat(partStat);
        updatedAttendee.setComment(comment);
        return new AttendeeEventUpdate(event, originalAttendee, updatedAttendee);
    }

    /**
     * Gets the (first) attendee matching a specific calendar user found in the supplied calendar object resource.
     * 
     * @param resource The calendar object resource to get the attendee comment from
     * @param calendarUser The calendar user to lookup the attendee for
     * @return The matching attendee, or <code>null</code> if not set or found
     */
    private static Attendee extractAttendee(CalendarObjectResource resource, CalendarUser calendarUser) {
        for (Event event : resource.getEvents()) {
            Attendee attende = CalendarUtils.find(event.getAttendees(), calendarUser);
            if (null != attende) {
                return attende;
            }
        }
        return null;
    }

    private static DateTime getEndDate(Event event) {
        DateTime endDate = event.getEndDate();
        if (null == endDate) {
            endDate = event.getStartDate();
            if (endDate.isAllDay()) {
                endDate.addDuration(new Duration(1, 1, 0));
            }
        }
        return endDate;
    }

    /**
     * Builds change descriptions for a list of event updates.
     * 
     * @param eventUpdates The event updates to get the descriptions for
     * @param originator The originator of the message
     * @param recipient The recipient of the message
     * @param fields The event fields to include in the descriptions
     * @return The change descriptions
     * @see DescriptionService#describe(EventUpdate, TimeZone, Locale, EventField...)
     */
    private List<Change> getChangeDescriptions(List<EventUpdate> eventUpdates, CalendarUser originator, CalendarUser recipient) {
        List<Change> changes = new ArrayList<Change>(eventUpdates.size());
        for (EventUpdate eventUpdate : eventUpdates) {
            changes.add(getChangeDescription(eventUpdate, originator, recipient));
        }
        return changes;
    }

    /**
     * Builds change descriptions for an event update.
     * 
     * @param eventUpdate The event update to get the description for
     * @param originator The originator of the message
     * @param recipient The recipient of the message
     * @param fields The event fields to include in the description
     * @return The change description
     * @see DescriptionService#describe(EventUpdate, TimeZone, Locale, EventField...)
     */
    private Change getChangeDescription(EventUpdate eventUpdate, CalendarUser originator, CalendarUser recipient) {
        TimeZone timeZone = selectTimeZone(originator, recipient, eventUpdate);
        Locale locale = selectLocale(originator, recipient);
        return new ChangeBuilder()
            .setDescriptions(getDescriptionService().describe(eventUpdate, timeZone, locale, (EventField[]) null))
            .setRecurrenceId(eventUpdate.getUpdate().getRecurrenceId())
        .build();
    }

    /**
     * Builds change descriptions for specific event fields of multiple event updates.
     * 
     * @param eventUpdates The event updates to get the descriptions for 
     * @param originator The originator of the message
     * @param recipient The recipient of the message
     * @param fields The event fields to include in the descriptions
     * @return The change description
     * @see DescriptionService#describeOnly(EventUpdate, TimeZone, Locale, EventField...)
     */
    private List<Change> getChangeDescriptionsFor(List<EventUpdate> eventUpdates, CalendarUser originator, CalendarUser recipient, EventField...fields) {
        List<Change> changes = new ArrayList<Change>(eventUpdates.size());
        for (EventUpdate eventUpdate : eventUpdates) {
            changes.add(getChangeDescriptionsFor(eventUpdate, originator, recipient, fields));
        }
        return changes;
    }
    
    /**
     * Builds change descriptions for specific event fields of an event update.
     * 
     * @param eventUpdate The event update to get the description for 
     * @param originator The originator of the message
     * @param recipient The recipient of the message
     * @param fields The event fields to include in the description
     * @return The change description
     * @see DescriptionService#describeOnly(EventUpdate, TimeZone, Locale, EventField...)
     */
    private Change getChangeDescriptionsFor(EventUpdate eventUpdate, CalendarUser originator, CalendarUser recipient, EventField...fields) {
        TimeZone timeZone = selectTimeZone(originator, recipient, eventUpdate);
        Locale locale = selectLocale(originator, recipient);
        return new ChangeBuilder()
            .setDescriptions(getDescriptionService().describeOnly(eventUpdate, timeZone, locale, fields))
            .setRecurrenceId(eventUpdate.getUpdate().getRecurrenceId())
        .build();
    }

    private Locale selectLocale(CalendarUser originator, CalendarUser recipient) {
        try {
            if (isInternal(recipient, CalendarUserType.INDIVIDUAL)) {
                return session.getEntityResolver().getLocale(recipient.getEntity());
            }
            if (isInternal(originator, CalendarUserType.INDIVIDUAL)) {
                return session.getEntityResolver().getLocale(originator.getEntity());
            }
        } catch (OXException e) {
            LOG.warn("Unexpected error determining target locale, falling back to defaults.", e);
        }
        return Locale.getDefault();
    }

    private TimeZone selectTimeZone(CalendarUser originator, CalendarUser recipient, EventUpdate eventUpdate) {
        try {
            if (isInternal(recipient, CalendarUserType.INDIVIDUAL)) {
                return session.getEntityResolver().getTimeZone(recipient.getEntity());
            }
            if (isInternal(originator, CalendarUserType.INDIVIDUAL)) {
                return session.getEntityResolver().getTimeZone(originator.getEntity());
            }
        } catch (OXException e) {
            LOG.warn("Unexpected error determining target timezone, falling back to defaults.", e);
        }
        if (null != eventUpdate && null != eventUpdate.getUpdate()) {
            DateTime startDate = eventUpdate.getUpdate().getStartDate();
            if (null != startDate && false == startDate.isFloating()) {
                return startDate.getTimeZone();
            }
        }
        return TimeZone.getDefault();
    }

}
