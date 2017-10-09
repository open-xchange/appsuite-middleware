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

import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.folderstorage.Permission.CREATE_OBJECTS_IN_FOLDER;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.WRITE_OWN_OBJECTS;
import java.util.List;
import java.util.UUID;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.impl.AttendeeHelper;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.Consistency;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.java.Strings;

/**
 * {@link CreatePerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CreatePerformer extends AbstractUpdatePerformer {

    /**
     * Initializes a new {@link CreatePerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    public CreatePerformer(CalendarStorage storage, CalendarSession session, UserizedFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Performs the creation of an event.
     *
     * @param event The event to create
     * @return The result
     */
    public InternalCalendarResult perform(Event event) throws OXException {
        getSelfProctection().checkEvent(event);
        /*
         * check current session user's permissions
         */
        requireCalendarPermission(folder, CREATE_OBJECTS_IN_FOLDER, NO_PERMISSIONS, WRITE_OWN_OBJECTS, NO_PERMISSIONS);
        /*
         * prepare event & attendee data for insert, assign parent folder
         */
        Event newEvent = prepareEvent(event);
        List<Attendee> newAttendees = prepareAttendees(event.getAttendees());
        getSelfProctection().checkAttendeeCollection(newAttendees);
        if (null == newAttendees || 0 == newAttendees.size()) {
            /*
             * not group-scheduled event (only on a single user's calendar), apply parent folder identifier
             */
            newEvent.setFolderId(folder.getID());
        } else {
            /*
             * group-scheduled event, assign organizer and dynamic parent-folder identifier (for non-public folders)
             */
            newEvent.setOrganizer(prepareOrganizer(event.getOrganizer()));
            newEvent.setFolderId(PublicType.getInstance().equals(folder.getType()) ? folder.getID() : null);
        }
        /*
         * check for conflicts & quota restrictions
         */
        Check.quotaNotExceeded(storage, session);
        Check.noConflicts(storage, session, newEvent, newAttendees);
        /*
         * insert event, attendees, attachments & alarms of user
         */
        storage.getEventStorage().insertEvent(newEvent);
        if (null != newAttendees && 0 < newAttendees.size()) {
            storage.getAttendeeStorage().insertAttendees(newEvent.getId(), newAttendees);
        }
        if (null != event.getAlarms() && 0 < event.getAlarms().size()) {
            newEvent.setFolderId(folder.getID());
            insertAlarms(newEvent, calendarUserId, Check.alarmsAreValid(event.getAlarms()), false);
        }
        if (null != event.getAttachments() && 0 < event.getAttachments().size()) {
            storage.getAttachmentStorage().insertAttachments(session.getSession(), folder.getID(), newEvent.getId(), event.getAttachments());
        }
        /*
         * track creation, insert alarm triggers & return result
         */
        Event createdEvent = loadEventData(newEvent.getId());
        resultTracker.trackCreation(createdEvent);
        storage.getAlarmTriggerStorage().insertTriggers(newEvent, newEvent.getDeleteExceptionDates());
        return resultTracker.getResult();
    }

    private List<Attendee> prepareAttendees(List<Attendee> attendeeData) throws OXException {
        return AttendeeHelper.onNewEvent(session, folder, attendeeData).getAttendeesToInsert();
    }

    private Event prepareEvent(Event eventData) throws OXException {
        Event event = new Event();
        /*
         * identifiers
         */
        event.setId(storage.getEventStorage().nextId());
        event.setSequence(0);
        if (false == eventData.containsUid() || Strings.isEmpty(eventData.getUid())) {
            event.setUid(UUID.randomUUID().toString());
        } else {
            event.setUid(Check.uidIsUnique(storage, eventData));
        }
        /*
         * creation/modification/calendaruser metadata
         */
        Consistency.setCreated(session, timestamp, event, calendarUserId);
        Consistency.setModified(session, timestamp, event, calendarUserId);
        Consistency.setCalenderUser(session, folder, event);
        /*
         * date/time related properties
         */
        Check.startAndEndDate(eventData);
        EventMapper.getInstance().copy(eventData, event, EventField.START_DATE, EventField.END_DATE);
        Consistency.adjustAllDayDates(event);
        //        Consistency.setTimeZone(session, event, calendarUserId);
        /*
         * classification, transparency, color, geo
         */
        if (eventData.containsClassification() && null != eventData.getClassification()) {
            event.setClassification(Check.classificationIsValid(eventData.getClassification(), folder));
        } else {
            event.setClassification(Classification.PUBLIC);
        }
        event.setTransp(eventData.containsTransp() && null != eventData.getTransp() ? eventData.getTransp() : TimeTransparency.OPAQUE);
        event.setColor(eventData.containsColor() ? eventData.getColor() : null);
        if (eventData.containsGeo()) {
            event.setGeo(Check.geoLocationIsValid(eventData));
        }
        /*
         * recurrence related fields
         */
        if (eventData.containsRecurrenceRule() && null != eventData.getRecurrenceRule()) {
            event.setRecurrenceRule(Check.recurrenceRuleIsValid(session.getRecurrenceService(), eventData));
            event.setSeriesId(event.getId());
            if (eventData.containsDeleteExceptionDates()) {
                event.setDeleteExceptionDates(Check.recurrenceIdsExist(session.getRecurrenceService(), eventData, eventData.getDeleteExceptionDates()));
            }
        }
        /*
         * copy over further (unchecked) event fields
         */
        return EventMapper.getInstance().copy(eventData, event, EventField.SUMMARY, EventField.LOCATION, EventField.DESCRIPTION, EventField.CATEGORIES, EventField.FILENAME, EventField.URL, EventField.STATUS, EventField.EXTENDED_PROPERTIES);
    }

}
