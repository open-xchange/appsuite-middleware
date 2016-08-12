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
import com.openexchange.exception.OXException;

/**
 * {@link AttendeeDiff}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class AttendeeDiff {

    private final List<Attendee> removedAttendees;
    private final List<Attendee> addedAttendees;
    private final List<Attendee[]> updatedAttendees;

    public AttendeeDiff(List<Attendee> originalAttendees, List<Attendee> newAttendees) throws OXException {
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
            updatedAttendees = new ArrayList<Attendee[]>();
            removedAttendees = new ArrayList<Attendee>();
            for (Attendee newAttendee : newAttendees) {
                Attendee originalAttendee = CalendarUtils.find(originalAttendees, newAttendee);
                if (null == originalAttendee) {
                    addedAttendees.add(newAttendee);
                } else if (isUpdated(originalAttendee, newAttendee)) {
                    updatedAttendees.add(new Attendee[] { originalAttendee, newAttendee });
                } else {
                    // not changed
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

    private boolean isUpdated(Attendee originalAttendee, Attendee newAttendee) throws OXException {
        Attendee deltaAttendee = AttendeeMapper.getInstance().getDifferences(originalAttendee, newAttendee);
        AttendeeField[] updatedFields = AttendeeMapper.getInstance().getAssignedFields(deltaAttendee);
        return 0 < updatedFields.length;
    }

    public List<Attendee> getRemovedAttendees() {
        return removedAttendees;
    }

    public List<Attendee> getAddedAttendees() {
        return addedAttendees;
    }

    public List<Attendee[]> getUpdatedAttendees() {
        return updatedAttendees;
    }

}
