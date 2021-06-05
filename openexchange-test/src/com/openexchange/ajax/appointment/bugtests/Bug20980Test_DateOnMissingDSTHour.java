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

import static org.junit.Assert.assertNull;
import java.util.Date;
import org.junit.Test;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.groupware.container.Appointment;

public class Bug20980Test_DateOnMissingDSTHour extends ManagedAppointmentTest {

    public Bug20980Test_DateOnMissingDSTHour() {
        super();
    }

    @Test
    public void testBugWithDST() {
        int fid = folder.getObjectID();
        Appointment series = generateDailyAppointment();
        series.setStartDate(D("30/3/2008 01:00", utc));
        series.setEndDate(D("30/3/2008 02:00", utc));
        series.setTitle("A daily series");
        series.setParentFolderID(fid);
        catm.insert(series);

        Date lastMod = series.getLastModified();
        for (int i = 1; i < 3; i++) {
            Appointment changeEx = new Appointment();
            changeEx.setParentFolderID(series.getParentFolderID());
            changeEx.setObjectID(series.getObjectID());
            changeEx.setLastModified(lastMod);
            changeEx.setRecurrencePosition(i);
            changeEx.setTitle("Element # " + i + " of series that has different name");
            catm.update(changeEx);
            assertNull("Problem with update #" + i, catm.getLastException());
            lastMod = new Date(catm.getLastModification().getTime() + 1);
        }
    }

}
