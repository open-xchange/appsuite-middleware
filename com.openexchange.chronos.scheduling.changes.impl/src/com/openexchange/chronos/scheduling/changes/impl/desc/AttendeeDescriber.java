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

package com.openexchange.chronos.scheduling.changes.impl.desc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.annotation.NonNull;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.generators.ArgumentType;
import com.openexchange.chronos.scheduling.changes.Description;
import com.openexchange.chronos.scheduling.changes.impl.ChangeDescriber;
import com.openexchange.chronos.scheduling.changes.impl.ChangesUtils;
import com.openexchange.chronos.scheduling.changes.impl.SentenceImpl;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.java.Strings;

/**
 * {@link AttendeeDescriber}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class AttendeeDescriber implements ChangeDescriber {

    private static final Map<CalendarUserType, String> ADD_MESSAGE_MAP = new HashMap<>(4, 0.9f);
    static {
        ADD_MESSAGE_MAP.put(CalendarUserType.INDIVIDUAL, Messages.HAS_ADDED_PARTICIPANT);
        ADD_MESSAGE_MAP.put(CalendarUserType.GROUP, Messages.HAS_INVITED_GROUP);
        ADD_MESSAGE_MAP.put(CalendarUserType.RESOURCE, Messages.HAS_ADDED_RESOURCE);
    }

    private static final Map<CalendarUserType, String> DELETE_MESSAGE_MAP = new HashMap<>(4, 0.9f);
    static {
        DELETE_MESSAGE_MAP.put(CalendarUserType.INDIVIDUAL, Messages.HAS_REMOVED_PARTICIPANT);
        DELETE_MESSAGE_MAP.put(CalendarUserType.GROUP, Messages.HAS_REMOVED_GROUP);
        DELETE_MESSAGE_MAP.put(CalendarUserType.RESOURCE, Messages.HAS_REMOVED_RESOURCE);
    }

    private static final AttendeeField[] STATUS = { AttendeeField.PARTSTAT };

    private static final CalendarUserType[] DESCRIBABLE_TYPES = { CalendarUserType.INDIVIDUAL, CalendarUserType.GROUP, CalendarUserType.RESOURCE };

    @Override
    @NonNull
    public EventField[] getFields() {
        return new EventField[] { EventField.ATTENDEES };
    }

    @Override
    public Description describe(EventUpdate eventUpdate) {
        List<SentenceImpl> sentences = new LinkedList<>();
        CollectionUpdate<Attendee, AttendeeField> attendeeChanges = eventUpdate.getAttendeeUpdates();
        if (null == attendeeChanges || attendeeChanges.isEmpty()) {
            return null;
        }
        describeChange(sentences, attendeeChanges.getAddedItems(), ADD_MESSAGE_MAP);

        for (ItemUpdate<Attendee, AttendeeField> updatedItems : attendeeChanges.getUpdatedItems()) {
            if (updatedItems.containsAnyChangeOf(STATUS)) {
                Attendee attendee = updatedItems.getUpdate();
                if (CalendarUserType.INDIVIDUAL.matches(attendee.getCuType()) && false == ParticipationStatus.NEEDS_ACTION.matches(attendee.getPartStat())) {
                    // Avoid generating messages with empty status
                    SentenceImpl sentence = new SentenceImpl(Messages.HAS_CHANGED_STATE).add(getReferenceName(attendee), ArgumentType.PARTICIPANT);
                    sentence.addStatus(attendee.getPartStat());
                    sentences.add(sentence);
                }
            }
        }

        describeChange(sentences, attendeeChanges.getRemovedItems(), DELETE_MESSAGE_MAP);
        return new DefaultDescription(sentences, EventField.ATTENDEES);
    }

    /*
     * --------------------- Helpers ---------------------
     */

    private String getReferenceName(Attendee attendee) {
        return Strings.isEmpty(attendee.getCn()) ? attendee.getUri() : attendee.getCn();
    }

    private void describeChange(List<SentenceImpl> sentences, List<Attendee> attendees, Map<CalendarUserType, String> messages) {
        for (CalendarUserType cuType : DESCRIBABLE_TYPES) {
            boolean isIndividual = CalendarUserType.INDIVIDUAL.matches(cuType);
            String message = messages.get(cuType);
            for (Attendee attendee : ChangesUtils.sortAttendees(attendees, cuType)) {
                SentenceImpl sentence = new SentenceImpl(message).add(getReferenceName(attendee), ArgumentType.PARTICIPANT);
                if (isIndividual) {
                    sentence.addStatus(attendee.getPartStat());
                }
                sentences.add(sentence);
            }
        }
    }

}
