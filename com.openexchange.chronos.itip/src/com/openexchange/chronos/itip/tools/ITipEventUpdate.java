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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.itip.tools;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.mapping.DefaultEventUpdate;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.chronos.service.SimpleCollectionUpdate;
import com.openexchange.exception.OXException;

/**
 * {@link ITipEventUpdate}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class ITipEventUpdate implements EventUpdate {

    private final static Set<AttendeeField> IGNOREES = new HashSet<>(4);
    {
        IGNOREES.add(AttendeeField.EXTENDED_PARAMETERS);
        IGNOREES.add(AttendeeField.CN);
        IGNOREES.add(AttendeeField.CU_TYPE);
        IGNOREES.add(AttendeeField.ENTITY);
        IGNOREES.add(AttendeeField.ROLE);
        IGNOREES.add(AttendeeField.RSVP);
    }

    private EventUpdate delegate;

    public ITipEventUpdate(Event originalEvent, Event updatedEvent, boolean considerUnset, EventField... ignoredFields) throws OXException {
        // Make sure EventField.EXTENDED_PROPERTIES is contained in ignordeFields.
        if (ignoredFields == null || ignoredFields.length == 0) {
            this.delegate = new DefaultEventUpdate(originalEvent, updatedEvent, considerUnset, IGNOREES, EventField.EXTENDED_PROPERTIES);
        } else {
            if (Arrays.stream(ignoredFields).anyMatch(x -> x == EventField.EXTENDED_PROPERTIES)) {
                this.delegate = new DefaultEventUpdate(originalEvent, updatedEvent, considerUnset, IGNOREES, ignoredFields);
            } else {
                EventField[] fields = new EventField[ignoredFields.length + 1];
                System.arraycopy(ignoredFields, 0, fields, 0, ignoredFields.length);
                fields[fields.length - 1] = EventField.EXTENDED_PROPERTIES;
                this.delegate = new DefaultEventUpdate(originalEvent, updatedEvent, considerUnset, IGNOREES, fields);
            }
        }
    }

    @Override
    public Event getOriginal() {
        return delegate.getOriginal();
    }

    @Override
    public Event getUpdate() {
        return delegate.getUpdate();
    }

    @Override
    public Set<EventField> getUpdatedFields() {
        return delegate.getUpdatedFields();
    }

    @Override
    public CollectionUpdate<Attendee, AttendeeField> getAttendeeUpdates() {
        return delegate.getAttendeeUpdates();
    }

    @Override
    public boolean containsAnyChangeOf(EventField[] fields) {
        return delegate.containsAnyChangeOf(fields);
    }

    @Override
    public CollectionUpdate<Alarm, AlarmField> getAlarmUpdates() {
        return delegate.getAlarmUpdates();
    }

    @Override
    public SimpleCollectionUpdate<Attachment> getAttachmentUpdates() {
        return delegate.getAttachmentUpdates();
    }

    public boolean containsAllChangesOf(EventField[] fields) {
        return getUpdatedFields().containsAll(Arrays.asList(fields));
    }

    public boolean containsAnyChangesBeside(EventField[] fields) {
        Set<EventField> temp = new HashSet<>(getUpdatedFields());
        temp.removeAll(Arrays.asList(fields));
        return !temp.isEmpty();
    }

    public boolean containsExactTheseChanges(EventField[] fields) {
        return containsAllChangesOf(fields) && containsOnlyChangeOf(fields);
    }

    public boolean containsOnlyChangeOf(EventField[] fields) {
        return !containsAnyChangesBeside(fields);
    }

    /**
     * Checks if the event diff contains <b>only</b> state changes
     *
     * @return <code>true</code> for <b>only</b> state changes; otherwise <code>false</code>
     */
    public boolean isAboutStateChangesOnly() {
        // First, let's see if any fields besides the state tracking fields have changed
        Set<EventField> differing = new HashSet<>(getUpdatedFields());
        differing.remove(EventField.ATTENDEES);
        if (!differing.isEmpty()) {
            return false;
        }

        return isAboutStateChanges();
    }

    /**
     * Checks if the event diff contains <b>only</b> state changes beside relevant fields.
     *
     * @return <code>true</code> for <b>only</b> state changes; otherwise <code>false</code>
     */
    public boolean isAboutStateChangesOnly(EventField[] relevant) {
        // First, let's see if any fields besides the state tracking fields have changed
        Set<EventField> differing = new HashSet<>(getUpdatedFields());
        differing.remove(EventField.ATTENDEES);
        if (differing.removeAll(Arrays.asList(relevant))) {
            return false; // There is at least one relevant change left.
        }

        return isAboutStateChanges();
    }

    /**
     * Checks if the event diff contains any state changes
     *
     * @return <code>true</code> for any state changes; otherwise <code>false</code>
     */
    public boolean isAboutStateChanges() {
        // Hm, okay, so now let's see if any participants were added or removed. That also means this mail is not only about state changes.
        if (getAttendeeUpdates().getAddedItems() != null && !getAttendeeUpdates().getAddedItems().isEmpty()) {
            return false;
        }

        if (getAttendeeUpdates().getRemovedItems() != null && !getAttendeeUpdates().getRemovedItems().isEmpty()) {
            return false;
        }

        List<? extends ItemUpdate<Attendee, AttendeeField>> updatedItems = getAttendeeUpdates().getUpdatedItems();
        for (ItemUpdate<Attendee, AttendeeField> updatedItem : updatedItems) {
            Set<AttendeeField> temp = new HashSet<>(updatedItem.getUpdatedFields());
            temp.remove(AttendeeField.PARTSTAT);
            temp.remove(AttendeeField.COMMENT);
            if (!temp.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if the event diff contains only changes besides attendee update
     * 
     * @return <code>false</code> if an attendee status was changes,<code>true</code> otherwise
     */
    public boolean isAboutDetailChangesOnly() {
        HashSet<EventField> differing = new HashSet<>(getUpdatedFields());
        differing.remove(EventField.ATTENDEES);

        // If any other field than the participants fields as changed and the participant fields were not changed, we're done, as no state changes could have occurred
        if (!differing.isEmpty() && !containsAnyChangeOf(new EventField[] { EventField.ATTENDEES })) {
            return true;
        }

        // Hm, okay, so now let's see if any participants state has changed. That means, that something other than a detail field has changed
        if (getAttendeeUpdates() != null && !getAttendeeUpdates().isEmpty()) {
            if (getAttendeeUpdates().getUpdatedItems() != null && !getAttendeeUpdates().getUpdatedItems().isEmpty()) {
                for (ItemUpdate<Attendee, AttendeeField> item : getAttendeeUpdates().getUpdatedItems()) {
                    if (item.getUpdatedFields().contains(AttendeeField.PARTSTAT)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public boolean isAboutCertainParticipantsStateChangeOnly(String identifier) {
        if (!isAboutStateChangesOnly()) {
            return false;
        }

        if (getAttendeeUpdates() == null || getAttendeeUpdates().isEmpty()) {
            return false;
        }

        CollectionUpdate<Attendee, AttendeeField> attendeeUpdates = getAttendeeUpdates();

        if (attendeeUpdates.getAddedItems() != null && !attendeeUpdates.getAddedItems().isEmpty()) {
            return false;
        }

        if (attendeeUpdates.getRemovedItems() != null && !attendeeUpdates.getRemovedItems().isEmpty()) {
            return false;
        }

        if (attendeeUpdates.getUpdatedItems() == null || attendeeUpdates.getUpdatedItems().size() != 1) {
            return false;
        }

        ItemUpdate<Attendee, AttendeeField> attendeeUpdate = attendeeUpdates.getUpdatedItems().get(0);
        if (!(attendeeUpdate.getOriginal().getEMail().equals(identifier) || Integer.toString(attendeeUpdate.getOriginal().getEntity()).equals(identifier))) {
            return false;
        }
        Set<AttendeeField> updatedFields = new HashSet<>(attendeeUpdate.getUpdatedFields());
        updatedFields.remove(AttendeeField.PARTSTAT);
        if (!updatedFields.isEmpty()) {
            return false;
        }

        return true;
    }

    public boolean isAboutCertainParticipantsRemoval(int userId) {
        if (getAttendeeUpdates() == null || getAttendeeUpdates().isEmpty()) {
            return false;
        }

        CollectionUpdate<Attendee, AttendeeField> attendeeUpdates = getAttendeeUpdates();

        if (attendeeUpdates.getAddedItems() != null && !attendeeUpdates.getAddedItems().isEmpty()) {
            return false;
        }

        if (attendeeUpdates.getUpdatedItems() != null && !attendeeUpdates.getUpdatedItems().isEmpty()) {
            return false;
        }

        if (attendeeUpdates.getRemovedItems() == null || attendeeUpdates.getRemovedItems().size() != 1) {
            return false;
        }

        if (attendeeUpdates.getRemovedItems().get(0).getEntity() != userId) {
            return false;
        }

        return true;
    }

}
