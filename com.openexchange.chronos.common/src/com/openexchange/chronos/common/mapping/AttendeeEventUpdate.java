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

package com.openexchange.chronos.common.mapping;

import static com.openexchange.chronos.common.CalendarUtils.matches;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Conference;
import com.openexchange.chronos.ConferenceField;
import com.openexchange.chronos.DelegatingEvent;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.chronos.service.SimpleCollectionUpdate;

/**
 * {@link AttendeeEventUpdate}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class AttendeeEventUpdate implements EventUpdate {

    private final Event event;
    private final Attendee originalAttendee;
    private final Attendee updatedAttendee;

    /**
     * Initializes a new {@link AttendeeEventUpdate}, indicating a single updated attendee as the only change within the passed event.
     *
     * @param event The event to construct the attendee event update for
     * @param originalAttendee The original attendee to indicate
     * @param updatedAttendee The updated attendee to indicate
     */
    public AttendeeEventUpdate(Event event, Attendee originalAttendee, Attendee updatedAttendee) {
        super();
        this.event = event;
        this.originalAttendee = originalAttendee;
        this.updatedAttendee = updatedAttendee;
    }

    @Override
    public Event getOriginal() {
        return event;
    }

    @Override
    public Event getUpdate() {
        List<Attendee> attendees = event.getAttendees();
        if (null != attendees && 0 < attendees.size()) {
            List<Attendee> modifiedAttendees = new ArrayList<Attendee>(attendees.size());
            for (Attendee attendee : attendees) {
                modifiedAttendees.add(matches(originalAttendee, attendee) ? updatedAttendee : attendee);
            }
            return new DelegatingEvent(event) {

                @Override
                public List<Attendee> getAttendees() {
                    return modifiedAttendees;
                }
            };
        }
        return event;
    }

    @Override
    public Set<EventField> getUpdatedFields() {
        return Collections.singleton(EventField.ATTENDEES);
    }

    @Override
    public boolean containsAnyChangeOf(EventField[] fields) {
        return com.openexchange.tools.arrays.Arrays.contains(fields, EventField.ATTENDEES);
    }

    @Override
    public CollectionUpdate<Attendee, AttendeeField> getAttendeeUpdates() {
        ItemUpdate<Attendee, AttendeeField> attendeeUpdate = new DefaultItemUpdate<Attendee, AttendeeField>(AttendeeMapper.getInstance(), originalAttendee, updatedAttendee);
        return new DefaultCollectionUpdate<Attendee, AttendeeField>(null, null, Collections.singletonList(attendeeUpdate));
    }

    @Override
    public CollectionUpdate<Alarm, AlarmField> getAlarmUpdates() {
        return AbstractCollectionUpdate.emptyUpdate();
    }

    @Override
    public SimpleCollectionUpdate<Attachment> getAttachmentUpdates() {
        return AbstractCollectionUpdate.emptyUpdate();
    }

    @Override
    public CollectionUpdate<Conference, ConferenceField> getConferenceUpdates() {
        return AbstractCollectionUpdate.emptyUpdate();
    }

    @Override
    public String toString() {
        return "ItemUpdate [originalItem=" + getOriginal() + ", updatedItem=" + getUpdate() + ", updatedFields=" + getUpdatedFields() + "]";
    }

}
