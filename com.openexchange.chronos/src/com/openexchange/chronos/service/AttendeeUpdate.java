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

package com.openexchange.chronos.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;

/**
 * {@link AttendeeUpdate}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class AttendeeUpdate {

    private final Attendee originalAttendee;
    private final Attendee updatedAttendee;
    private final Set<AttendeeField> updatedFields;

    /**
     * Initializes a new {@link AttendeeUpdate}.
     *
     * @param originalAttendee The attendee in the original event, or <code>null</code> for a newly added attendee
     * @param updatedAttendee The attendee in the updated event, or <code>null</code> for a removed attendee
     * @param updatedFields The attendee fields that were modified through the update
     */
    public AttendeeUpdate(Attendee originalAttendee, Attendee updatedAttendee, AttendeeField[] updatedFields) {
        this(originalAttendee, updatedAttendee, Collections.unmodifiableSet(new HashSet<AttendeeField>(Arrays.asList(updatedFields))));
    }

    /**
     * Initializes a new {@link AttendeeUpdate}.
     *
     * @param originalAttendee The attendee in the original event, or <code>null</code> for a newly added attendee
     * @param updatedAttendee The attendee in the updated event, or <code>null</code> for a removed attendee
     * @param updatedFields The attendee fields that were modified through the update
     */
    public AttendeeUpdate(Attendee originalAttendee, Attendee updatedAttendee, Set<AttendeeField> updatedFields) {
        super();
        this.originalAttendee = originalAttendee;
        this.updatedAttendee = updatedAttendee;
        this.updatedFields = updatedFields;
    }

    /**
     * Gets the attendee in the original event.
     *
     * @return The attendee in the original event, or <code>null</code> for a newly added attendee
     */
    public Attendee getOriginalAttendee() {
        return originalAttendee;
    }

    /**
     * Gets the attendee in the updated event.
     *
     * @return The attendee in the updated event, or <code>null</code> for a removed attendee
     */
    public Attendee getUpdatedAttendee() {
        return updatedAttendee;
    }

    /**
     * Gets the attendee fields that were modified through the update
     *
     * @return The updated fields
     */
    public Set<AttendeeField> getUpdatedFields() {
        return updatedFields;
    }

}

