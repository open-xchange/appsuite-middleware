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

package com.openexchange.chronos.common.mapping;

import java.util.Set;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.SimpleCollectionUpdate;
import com.openexchange.exception.OXException;

/**
 * {@link EventUpdateImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventUpdateImpl extends DefaultItemUpdate<Event, EventField> implements EventUpdate {

    private final CollectionUpdate<Alarm, AlarmField> alarmUpdates;
    private final CollectionUpdate<Attendee, AttendeeField> attendeeUpdates;
    private final SimpleCollectionUpdate<Attachment> attachmentUpdates;

    /**
     * Initializes a new {@link EventUpdateImpl} containing the event update providing the differences.
     *
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     * @param considerUnset <code>true</code> to also consider comparison with not <i>set</i> fields of the original, <code>false</code>, otherwise
     * @param ignoredFields Fields to ignore when determining the differences
     */
    public EventUpdateImpl(Event originalEvent, Event updatedEvent, boolean considerUnset, EventField... ignoredFields) throws OXException {
        this(originalEvent, updatedEvent, getDifferentFields(EventMapper.getInstance(), originalEvent, updatedEvent, considerUnset, ignoredFields), null);
    }

    /**
     * Initializes a new {@link EventUpdateImpl} containing the event update providing the differences.
     *
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     * @param considerUnset <code>true</code> to also consider comparison with not <i>set</i> fields of the original, <code>false</code>, otherwise
     * @param ignoredAttendeeFields The {@link AttendeeField}s to ignore when determining the differences
     * @param ignoredFields Fields to ignore when determining the differences
     * @throws OXException If building differences fails
     */
    public EventUpdateImpl(Event originalEvent, Event updatedEvent, boolean considerUnset, Set<AttendeeField> ignoredAttendeeFields, EventField... ignoredFields) throws OXException {
        this(originalEvent, updatedEvent, getDifferentFields(EventMapper.getInstance(), originalEvent, updatedEvent, considerUnset, ignoredFields), ignoredAttendeeFields);
    }

    EventUpdateImpl(Event originalEvent, Event updatedEvent, Set<EventField> updatedFields, Set<AttendeeField> ignoredAttendeeFields) throws OXException {
        super(originalEvent, updatedEvent, updatedFields);
        alarmUpdates = AlarmUtils.getAlarmUpdates(
            null != originalEvent ? originalEvent.getAlarms() : null, null != updatedEvent ? updatedEvent.getAlarms() : null);
        attendeeUpdates = CalendarUtils.getAttendeeUpdates(
            null != originalEvent ? originalEvent.getAttendees() : null, null != updatedEvent ? updatedEvent.getAttendees() : null, true, ignoredAttendeeFields.toArray(new AttendeeField[] {}));
        attachmentUpdates = CalendarUtils.getAttachmentUpdates(
            null != originalEvent ? originalEvent.getAttachments() : null, null != updatedEvent ? updatedEvent.getAttachments() : null);
    }

    @Override
    public CollectionUpdate<Attendee, AttendeeField> getAttendeeUpdates() {
        return attendeeUpdates;
    }

    @Override
    public CollectionUpdate<Alarm, AlarmField> getAlarmUpdates() {
        return alarmUpdates;
    }

    @Override
    public SimpleCollectionUpdate<Attachment> getAttachmentUpdates() {
        return attachmentUpdates;
    }

}
