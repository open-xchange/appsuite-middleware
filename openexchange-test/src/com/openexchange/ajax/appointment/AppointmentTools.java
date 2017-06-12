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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.ajax.appointment;

import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link AppointmentTools}. Utility class that contains all help methods for comparing
 * appointment objects
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class AppointmentTools extends ParticipantTools {

    /**
     * Compares the specified objects
     * 
     * @param appointmentObj1 The expected {@link Appointment}
     * @param appointmentObj2 The actual {@link Appointment}
     * @throws Exception if an error is occurred
     */
    public static void compareObject(final Appointment appointmentObj1, final Appointment appointmentObj2) throws Exception {
        assertEquals("id is not equals", appointmentObj1.getObjectID(), appointmentObj2.getObjectID());
        assertEqualsAndNotNull("title is not equals", appointmentObj1.getTitle(), appointmentObj2.getTitle());
        assertEqualsAndNotNull("start is not equals", appointmentObj1.getStartDate(), appointmentObj2.getStartDate());
        assertEqualsAndNotNull("end is not equals", appointmentObj1.getEndDate(), appointmentObj2.getEndDate());
        assertEqualsAndNotNull("location is not equals", appointmentObj1.getLocation(), appointmentObj2.getLocation());
        assertEquals("shown_as is not equals", appointmentObj1.getShownAs(), appointmentObj2.getShownAs());
        assertEquals("folder id is not equals", appointmentObj1.getParentFolderID(), appointmentObj2.getParentFolderID());
        assertEquals("private flag is not equals", appointmentObj1.getPrivateFlag(), appointmentObj2.getPrivateFlag());
        assertEquals("full time is not equals", appointmentObj1.getFullTime(), appointmentObj2.getFullTime());
        assertEquals("label is not equals", appointmentObj1.getLabel(), appointmentObj2.getLabel());
        assertEquals("alarm is not equals", appointmentObj1.getAlarm(), appointmentObj2.getAlarm());
        assertEquals("alarm flag is not equals", appointmentObj1.getAlarmFlag(), appointmentObj2.getAlarmFlag());
        assertEquals("recurrence_type", appointmentObj1.getRecurrenceType(), appointmentObj2.getRecurrenceType());
        assertEquals("interval", appointmentObj1.getInterval(), appointmentObj2.getInterval());
        assertEquals("days", appointmentObj1.getDays(), appointmentObj2.getDays());
        assertEquals("month", appointmentObj1.getMonth(), appointmentObj2.getMonth());
        assertEquals("day_in_month", appointmentObj1.getDayInMonth(), appointmentObj2.getDayInMonth());
        assertEquals("until", appointmentObj1.getUntil(), appointmentObj2.getUntil());
        assertEqualsAndNotNull("note is not equals", appointmentObj1.getNote(), appointmentObj2.getNote());
        assertEqualsAndNotNull("categories is not equals", appointmentObj1.getCategories(), appointmentObj2.getCategories());
        assertEqualsAndNotNull("delete exception is not equals", appointmentObj1.getDeleteException(), appointmentObj2.getDeleteException());

        assertEqualsAndNotNull("participants are not equals", participants2String(appointmentObj1.getParticipants()), participants2String(appointmentObj2.getParticipants()));
        assertEqualsAndNotNull("users are not equals", users2String(appointmentObj1.getUsers()), users2String(appointmentObj2.getUsers()));
    }
}
