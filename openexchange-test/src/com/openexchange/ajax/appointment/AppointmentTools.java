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
     */
    public static void compareObject(final Appointment appointmentObj1, final Appointment appointmentObj2) {
        assertEquals("id is not equals", appointmentObj1.getObjectID(), appointmentObj2.getObjectID());
        assertEqualsAndNotNull("title is not equals", appointmentObj1.getTitle(), appointmentObj2.getTitle());
        assertEqualsAndNotNull("start is not equals", appointmentObj1.getStartDate(), appointmentObj2.getStartDate());
        assertEqualsAndNotNull("end is not equals", appointmentObj1.getEndDate(), appointmentObj2.getEndDate());
        assertEqualsAndNotNull("location is not equals", appointmentObj1.getLocation(), appointmentObj2.getLocation());
        assertEquals("shown_as is not equals", appointmentObj1.getShownAs(), appointmentObj2.getShownAs());
        assertEquals("folder id is not equals", appointmentObj1.getParentFolderID(), appointmentObj2.getParentFolderID());
        assertTrue("private flag is not equals", appointmentObj1.getPrivateFlag() == appointmentObj2.getPrivateFlag());
        assertTrue("full time is not equals", appointmentObj1.getFullTime() == appointmentObj2.getFullTime());
        assertEquals("label is not equals", appointmentObj1.getLabel(), appointmentObj2.getLabel());
        assertEquals("alarm is not equals", appointmentObj1.getAlarm(), appointmentObj2.getAlarm());
        assertTrue("alarm flag is not equals", appointmentObj1.getAlarmFlag() == appointmentObj2.getAlarmFlag());
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
