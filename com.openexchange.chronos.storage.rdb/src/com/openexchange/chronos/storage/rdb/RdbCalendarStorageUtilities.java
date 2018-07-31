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

package com.openexchange.chronos.storage.rdb;

import static com.openexchange.chronos.common.CalendarUtils.getObjectIDs;
import static com.openexchange.tools.arrays.Arrays.contains;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.mapping.AttendeeMapper;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageUtilities;
import com.openexchange.exception.OXException;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RdbCalendarStorageUtilities implements CalendarStorageUtilities {

    /** The event fields that are preserved for reference in <i>tombstone</i> events */
    private static final EventField[] EVENT_TOMBSTONE_FIELDS = {
        EventField.FOLDER_ID, EventField.ID, EventField.SERIES_ID, EventField.CALENDAR_USER, EventField.UID, EventField.FILENAME,
        EventField.TIMESTAMP, EventField.CREATED, EventField.CREATED_BY, EventField.LAST_MODIFIED, EventField.MODIFIED_BY,
        EventField.SEQUENCE, EventField.START_DATE, EventField.END_DATE, EventField.RECURRENCE_RULE, EventField.RECURRENCE_ID,
        EventField.DELETE_EXCEPTION_DATES, EventField.CHANGE_EXCEPTION_DATES, EventField.RECURRENCE_DATES,
        EventField.CLASSIFICATION, EventField.TRANSP
    };

    /** The attendee fields that are preserved for reference in <i>tombstone</i> attendees */
    private static final AttendeeField[] ATTENDEE_TOMBSTONE_FIELDS = {
        AttendeeField.CU_TYPE, AttendeeField.ENTITY, AttendeeField.FOLDER_ID, AttendeeField.MEMBER, AttendeeField.PARTSTAT, AttendeeField.URI
    };

    private final CalendarStorage storage;

    /**
     * Initializes a new {@link RdbCalendarStorageUtilities}.
     *
     * @param storage The underlying calendar storage
     */
    public RdbCalendarStorageUtilities(CalendarStorage storage) {
        super();
        this.storage = storage;
    }

    @Override
    public Attendee getTombstone(Attendee attendee) throws OXException {
        return AttendeeMapper.getInstance().copy(attendee, null, ATTENDEE_TOMBSTONE_FIELDS);
    }

    @Override
    public List<Attendee> getTombstones(List<Attendee> attendees) throws OXException {
        if (null == attendees) {
            return null;
        }
        List<Attendee> tombstoneAttendees = new ArrayList<Attendee>(attendees.size());
        for (Attendee attendee : attendees) {
            tombstoneAttendees.add(getTombstone(attendee));
        }
        return tombstoneAttendees;
    }

    @Override
    public Event getTombstone(Event event, Date lastModified, CalendarUser modifiedBy) throws OXException {
        Event tombstone = EventMapper.getInstance().copy(event, new Event(), true, EVENT_TOMBSTONE_FIELDS);
        tombstone.setLastModified(lastModified);
        tombstone.setModifiedBy(modifiedBy);
        tombstone.setTimestamp(lastModified.getTime());
        return tombstone;
    }

    @Override
    public Event loadAdditionalEventData(int userId, Event event, EventField[] fields) throws OXException {
        if (null != event && (null == fields || contains(fields, EventField.ATTENDEES))) {
            event.setAttendees(storage.getAttendeeStorage().loadAttendees(event.getId()));
        }
        if (null != event && (null == fields || contains(fields, EventField.ATTACHMENTS))) {
            try {
                event.setAttachments(storage.getAttachmentStorage().loadAttachments(event.getId()));
            } catch (UnsupportedOperationException e) {
                // attachment storage not supported in account, skip
            }
        }
        if (null != event && 0 < userId && (null == fields || contains(fields, EventField.ALARMS))) {
            event.setAlarms(storage.getAlarmStorage().loadAlarms(event, userId));
        }
        return event;
    }

    @Override
    public List<Event> loadAdditionalEventData(int userId, List<Event> events, EventField[] fields) throws OXException {
        if (null != events && 0 < events.size() && (null == fields ||
            contains(fields, EventField.ATTENDEES) || contains(fields, EventField.ATTACHMENTS) || contains(fields, EventField.ALARMS))) {
            String[] objectIDs = getObjectIDs(events);
            if (null == fields || contains(fields, EventField.ATTENDEES)) {
                Map<String, List<Attendee>> attendeesById = storage.getAttendeeStorage().loadAttendees(objectIDs);
                for (Event event : events) {
                    event.setAttendees(attendeesById.get(event.getId()));
                }
            }
            if (null == fields || contains(fields, EventField.ATTACHMENTS)) {
                try {
                    Map<String, List<Attachment>> attachmentsById = storage.getAttachmentStorage().loadAttachments(objectIDs);
                    for (Event event : events) {
                        event.setAttachments(attachmentsById.get(event.getId()));
                    }
                } catch (UnsupportedOperationException e) {
                    // attachment storage not supported in account, skip
                }
            }
            if (0 < userId && (null == fields || contains(fields, EventField.ALARMS))) {
                Map<String, List<Alarm>> alarmsById = storage.getAlarmStorage().loadAlarms(events, userId);
                for (Event event : events) {
                    event.setAlarms(alarmsById.get(event.getId()));
                }
            }
        }
        return events;
    }

    @Override
    public List<Event> loadAdditionalEventTombstoneData(List<Event> events, EventField[] fields) throws OXException {
        if (null != events && 0 < events.size() && (null == fields || contains(fields, EventField.ATTENDEES))) {
            Map<String, List<Attendee>> attendeesById = storage.getAttendeeStorage().loadAttendees(getObjectIDs(events));
            for (Event event : events) {
                event.setAttendees(attendeesById.get(event.getId()));
            }
        }
        return events;
    }

    @Override
    public void deleteAllData() throws OXException {
        storage.getEventStorage().deleteAllEvents();
        storage.getAlarmStorage().deleteAllAlarms();
        storage.getAlarmTriggerStorage().deleteAllTriggers();
        storage.getAttendeeStorage().deleteAllAttendees();
    }

}
