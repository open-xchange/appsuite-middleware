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
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.groupware.tools.mappings.common.AbstractCollectionUpdate;
import com.openexchange.groupware.tools.mappings.common.CollectionUpdate;
import com.openexchange.groupware.tools.mappings.common.DefaultItemUpdate;
import com.openexchange.groupware.tools.mappings.common.ItemUpdate;
import com.openexchange.groupware.tools.mappings.common.SimpleCollectionUpdate;

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
