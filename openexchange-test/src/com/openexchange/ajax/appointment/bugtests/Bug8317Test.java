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

package com.openexchange.ajax.appointment.bugtests;

import java.util.Calendar;
import java.util.Date;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.java.util.TimeZones;

public class Bug8317Test extends AppointmentTest {

    /**
     * INFO: This test case must be done at least today + 1 days because otherwise
     * no conflict resolution is made because past appointments do not conflict!
     *
     * Therefore I changed this to a future date and I fixed the test case.
     *
     * TODO: Create a dynamic date/time in the future for testing.
     */
    @Test
    public void testBug8317() {
        final Calendar calendar = Calendar.getInstance(TimeZones.UTC);
        calendar.setTimeInMillis(startTime);
        calendar.add(Calendar.DATE, 5);

        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.set(year, month, day, 0, 0, 0);
        Date startDate = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date endDate = calendar.getTime();

        Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testBug8317");
        appointmentObj.setStartDate(startDate);
        appointmentObj.setEndDate(endDate);
        appointmentObj.setFullTime(true);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setIgnoreConflicts(true);
        catm.insert(appointmentObj).getObjectID();

        calendar.setTimeZone(timeZone);
        calendar.set(year, month, day, 0, 30, 0);
        startDate = calendar.getTime();

        calendar.set(year, month, day, 1, 0, 0);
        endDate = calendar.getTime();

        appointmentObj = new Appointment();
        appointmentObj.setTitle("testBug8317 II");
        appointmentObj.setStartDate(startDate);
        appointmentObj.setEndDate(endDate);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setIgnoreConflicts(false);

        final int objectId2 = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId2);
        catm.delete(appointmentObj);
    }
}
