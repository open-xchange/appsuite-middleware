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

package com.openexchange.chronos.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.service.AttendeeDiff;
import com.openexchange.chronos.service.AttendeeUpdate;
import com.openexchange.exception.OXException;

/**
 * {@link AttendeeDiffImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class AttendeeDiffImpl implements AttendeeDiff {

    private final List<Attendee> removedAttendees;
    private final List<Attendee> addedAttendees;
    private final List<AttendeeUpdate> updatedAttendees;

    /**
     * Initializes a new {@link AttendeeDiffImpl}.
     *
     * @param originalAttendees The attendees of the original event, or <code>null</code> for new events
     * @param newAttendees The attendees of the updated event, or <code>null</code> for event deletions
     */
    public AttendeeDiffImpl(List<Attendee> originalAttendees, List<Attendee> newAttendees) throws OXException {
        super();
        if (null == originalAttendees || 0 == originalAttendees.size()) {
            removedAttendees = Collections.emptyList();
            updatedAttendees = Collections.emptyList();
            if (null == newAttendees || 0 == newAttendees.size()) {
                addedAttendees = Collections.emptyList();
            } else {
                addedAttendees = new ArrayList<Attendee>(newAttendees);
            }
        } else if (null == newAttendees || 0 == newAttendees.size()) {
            removedAttendees = new ArrayList<Attendee>(originalAttendees);
            updatedAttendees = Collections.emptyList();
            addedAttendees = Collections.emptyList();
        } else {
            addedAttendees = new ArrayList<Attendee>();
            updatedAttendees = new ArrayList<AttendeeUpdate>();
            removedAttendees = new ArrayList<Attendee>();
            for (Attendee newAttendee : newAttendees) {
                Attendee originalAttendee = CalendarUtils.find(originalAttendees, newAttendee);
                if (null == originalAttendee) {
                    addedAttendees.add(newAttendee);
                } else {
                    Attendee deltaAttendee = AttendeeMapper.getInstance().getDifferences(originalAttendee, newAttendee);
                    AttendeeField[] updatedFields = AttendeeMapper.getInstance().getAssignedFields(deltaAttendee);
                    if (0 < updatedFields.length) {
                        updatedAttendees.add(new AttendeeUpdate(originalAttendee, newAttendee, updatedFields));
                    } else {
                        // not changed
                    }
                }
            }
            for (Attendee originalAttendee : originalAttendees) {
                Attendee newAttendee = CalendarUtils.find(newAttendees, originalAttendee);
                if (null == newAttendee) {
                    removedAttendees.add(originalAttendee);
                }
            }
        }
    }

    @Override
    public List<Attendee> getRemovedAttendees() {
        return removedAttendees;
    }

    @Override
    public List<Attendee> getRemovedAttendees(Boolean internal, CalendarUserType cuType) {
        return CalendarUtils.filter(removedAttendees, internal, cuType);
    }

    @Override
    public List<Attendee> getAddedAttendees() {
        return addedAttendees;
    }

    @Override
    public List<Attendee> getAddedAttendees(Boolean internal, CalendarUserType cuType) {
        return CalendarUtils.filter(addedAttendees, internal, cuType);
    }

    @Override
    public List<AttendeeUpdate> getUpdatedAttendees() {
        return updatedAttendees;
    }

    @Override
    public List<AttendeeUpdate> getUpdatedAttendees(Boolean internal, CalendarUserType cuType) {
        List<AttendeeUpdate> filteredUpdates = new ArrayList<AttendeeUpdate>(updatedAttendees.size());
        for (AttendeeUpdate attendeeUpdate : updatedAttendees) {
            if (null == cuType || cuType.equals(attendeeUpdate.getOriginalAttendee().getCuType())) {
                if (null == internal || internal.equals(Boolean.valueOf(0 < attendeeUpdate.getOriginalAttendee().getEntity()))) {
                    filteredUpdates.add(attendeeUpdate);
                }
            }
        }
        return filteredUpdates;
    }

    @Override
    public boolean isEmpty() {
        return 0 == removedAttendees.size() && 0 == addedAttendees.size() && 0 == updatedAttendees.size();
    }

    @Override
    public String toString() {
        return "AttendeeDiff [" + removedAttendees.size() + " removed, " + addedAttendees.size() + " added, " + updatedAttendees.size() + " updated]";
    }

}
