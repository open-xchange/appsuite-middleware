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

import java.util.HashSet;
import java.util.Set;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Conference;
import com.openexchange.chronos.ConferenceField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.groupware.tools.mappings.common.AbstractCollectionUpdate;
import com.openexchange.groupware.tools.mappings.common.CollectionUpdate;
import com.openexchange.groupware.tools.mappings.common.DefaultItemUpdate;
import com.openexchange.groupware.tools.mappings.common.SimpleCollectionUpdate;
import com.openexchange.tools.arrays.Arrays;

/**
 * {@link DefaultEventUpdate}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultEventUpdate extends DefaultItemUpdate<Event, EventField> implements EventUpdate {

    /**
     * Creates a new builder instance
     *
     * @return The new builder instance
     */
    public static DefaultEventUpdate.Builder builder() {
        return new DefaultEventUpdate.Builder();
    }

    /**
     * {@link DefaultEventUpdate.Builder}.
     *
     * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
     * @since v7.10.0
     */
    public static class Builder {

        private Event originalEvent;
        private Event updatedEvent;
        private EventField[] ignoredEventFields;
        private AttendeeField[] ignoredAttendeeFields;
        private boolean considerUnset;
        private boolean ignoreDefaults;

        /**
         * Initializes a new {@link DefaultEventUpdate.Builder}.
         */
        Builder() {
            super();
        }

        /**
         * Sets the original event.
         *
         * @param originalEvent The original event
         * @return A self reference
         */
        public Builder originalEvent(Event originalEvent) {
            this.originalEvent = originalEvent;
            return this;
        }

        /**
         * Sets the updated event.
         *
         * @param updatedEvent The updated event
         * @return A self reference
         */
        public Builder updatedEvent(Event updatedEvent) {
            this.updatedEvent = updatedEvent;
            return this;
        }

        /**
         * Sets the event fields to ignore when determining the differences.
         *
         * @param ignoredEventFields The event fields to ignore
         * @return A self reference
         */
        public Builder ignoredEventFields(EventField... ignoredEventFields) {
            this.ignoredEventFields = ignoredEventFields;
            return this;
        }

        /**
         * Sets the attendee fields to ignore when determining the differences.
         *
         * @param ignoredAttendeeFields The attendee fields to ignore
         * @return A self reference
         */
        public Builder ignoredEventFields(AttendeeField... ignoredAttendeeFields) {
            this.ignoredAttendeeFields = ignoredAttendeeFields;
            return this;
        }

        /**
         * Sets whether comparison with not <i>set</i> fields of the original should take place or not.
         *
         * @param considerUnset <code>true</code> to also consider comparison with not <i>set</i> fields of the original, <code>false</code>, otherwise
         * @return A self reference
         */
        public Builder considerUnset(boolean considerUnset) {
            this.considerUnset = considerUnset;
            return this;
        }

        /**
         * Sets whether default values of enumerated properties should also be considered for not <i>set</i> fields of the update or not.
         *
         * @param ignoreDefaults <code>true</code> to also consider default values of enumerated properties for not <i>set</i> fields of the update, <code>false</code>, otherwise
         * @return A self reference
         */
        public Builder ignoreDefaults(boolean ignoreDefaults) {
            this.ignoreDefaults = ignoreDefaults;
            return this;
        }

        /**
         * Builds the event update using the previously defined properties.
         *
         * @return The new event update instance
         */
        public EventUpdate build() {
            return new DefaultEventUpdate(originalEvent, updatedEvent, ignoredEventFields, ignoredAttendeeFields, considerUnset, ignoreDefaults);
        }
    }

    private final CollectionUpdate<Alarm, AlarmField> alarmUpdates;
    private final CollectionUpdate<Attendee, AttendeeField> attendeeUpdates;
    private final CollectionUpdate<Conference, ConferenceField> conferenceUpdates;
    private final SimpleCollectionUpdate<Attachment> attachmentUpdates;

    /**
     * Initializes a new {@link DefaultEventUpdate} containing the event update providing the differences.
     * <p/>
     * <i>Unset</i> fields of the original are considered, and no fields are ignored.
     *
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     */
    public DefaultEventUpdate(Event originalEvent, Event updatedEvent) {
        this(originalEvent, updatedEvent, true, (EventField[]) null);
    }

    /**
     * Initializes a new {@link DefaultEventUpdate} containing the event update providing the differences.
     *
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     * @param considerUnset <code>true</code> to also consider comparison with not <i>set</i> fields of the original, <code>false</code>, otherwise
     * @param ignoredFields Fields to ignore when determining the differences
     */
    public DefaultEventUpdate(Event originalEvent, Event updatedEvent, boolean considerUnset, EventField... ignoredFields) {
        this(originalEvent, updatedEvent, ignoredFields, null, considerUnset, false);
    }

    /**
     * Initializes a new {@link DefaultEventUpdate} containing the event update providing the differences.
     *
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     * @param considerUnset <code>true</code> to also consider comparison with not <i>set</i> fields of the original, <code>false</code>, otherwise
     * @param ignoredAttendeeFields The {@link AttendeeField}s to ignore when determining the differences
     * @param ignoredFields Fields to ignore when determining the differences
     */
    public DefaultEventUpdate(Event originalEvent, Event updatedEvent, boolean considerUnset, Set<AttendeeField> ignoredAttendeeFields, EventField... ignoredFields) {
        this(originalEvent, updatedEvent, ignoredFields, null != ignoredAttendeeFields ? ignoredAttendeeFields.toArray(new AttendeeField[ignoredAttendeeFields.size()]) : null, considerUnset, false);
    }

    /**
     * Initializes a new {@link DefaultEventUpdate}.
     *
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     * @param ignoredEventFields The event fields to ignore when determining the differences
     * @param ignoredAttendeeFields The event attendee fields to ignore when determining the differences
     * @param ignoreDefaults <code>true</code> to also consider default values of enumerated properties for not <i>set</i> fields of the update, <code>false</code>, otherwise
     * @param considerUnset <code>true</code> to also consider comparison with not <i>set</i> fields of the original, <code>false</code>, otherwise
     */
    protected DefaultEventUpdate(Event originalEvent, Event updatedEvent, EventField[] ignoredEventFields, AttendeeField[] ignoredAttendeeFields, boolean considerUnset, boolean ignoreDefaults) {
        super(originalEvent, updatedEvent, getDifferentFields(EventMapper.getInstance(), originalEvent, updatedEvent, considerUnset, ignoreDefaults,
            extendIgnoredFields(ignoredEventFields, EventField.ALARMS, EventField.ATTACHMENTS, EventField.ATTENDEES)));
        if (considerCollectionUpdate(EventField.ALARMS, ignoredEventFields)) {
            alarmUpdates = AlarmUtils.getAlarmUpdates(
                null != originalEvent ? originalEvent.getAlarms() : null, null != updatedEvent ? updatedEvent.getAlarms() : null);
        } else {
            alarmUpdates = AbstractCollectionUpdate.emptyUpdate();
        }
        if (considerCollectionUpdate(EventField.ATTENDEES, ignoredEventFields)) {
            attendeeUpdates = CalendarUtils.getAttendeeUpdates(
                null != originalEvent ? originalEvent.getAttendees() : null, null != updatedEvent ? updatedEvent.getAttendees() : null, considerUnset, ignoredAttendeeFields);
        } else {
            attendeeUpdates = AbstractCollectionUpdate.emptyUpdate();
        }
        if (considerCollectionUpdate(EventField.ATTACHMENTS, ignoredEventFields)) {
            attachmentUpdates = CalendarUtils.getAttachmentUpdates(
                null != originalEvent ? originalEvent.getAttachments() : null, null != updatedEvent ? updatedEvent.getAttachments() : null);
        } else {
            attachmentUpdates = AbstractCollectionUpdate.emptyUpdate();
        }
        if (considerCollectionUpdate(EventField.CONFERENCES, ignoredEventFields)) {
            conferenceUpdates = CalendarUtils.getConferenceUpdates(
                null != originalEvent ? originalEvent.getConferences() : null, null != updatedEvent ? updatedEvent.getConferences() : null);
        } else {
            conferenceUpdates = AbstractCollectionUpdate.emptyUpdate();
        }
    }

    private static EventField[] extendIgnoredFields(EventField[] ignoredFields, EventField...additionals) {
        if (null == ignoredFields) {
            return additionals;
        }
        if (null == additionals || 0 == additionals.length) {
            return ignoredFields;
        }
        return Arrays.add(ignoredFields, additionals);
    }

    private static boolean considerCollectionUpdate(EventField field, EventField[] ignoredFields) {
        if (null == ignoredFields || false == Arrays.contains(ignoredFields, field)) {
            return true;
        }
        return false;
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

    @Override
    public CollectionUpdate<Conference, ConferenceField> getConferenceUpdates() {
        return conferenceUpdates;
    }

    @Override
    public Set<EventField> getUpdatedFields() {
        Set<EventField> updatedFields = new HashSet<EventField>(super.getUpdatedFields());
        if (false == attendeeUpdates.isEmpty()) {
            updatedFields.add(EventField.ATTENDEES);
        }
        if (false == attachmentUpdates.isEmpty()) {
            updatedFields.add(EventField.ATTACHMENTS);
        }
        if (false == alarmUpdates.isEmpty()) {
            updatedFields.add(EventField.ALARMS);
        }
        if (false == conferenceUpdates.isEmpty()) {
            updatedFields.add(EventField.CONFERENCES);
        }
        return updatedFields;
    }

}
