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

package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.junit.Test;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalExportRequest;
import com.openexchange.ajax.importexport.actions.ICalExportResponse;
import com.openexchange.groupware.container.Appointment;

public class ICalAppointmentExportTest extends ManagedAppointmentTest {

    @Test
    public void testExportICalAppointment() throws Exception {
        final String title = "testExportICalAppointment" + System.currentTimeMillis();
        int folderID = folder.getObjectID();
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle(title);
        appointmentObj.setStartDate(new Date());
        appointmentObj.setEndDate(new Date());
        appointmentObj.setShownAs(Appointment.RESERVED);
        appointmentObj.setParentFolderID(folderID);
        appointmentObj.setIgnoreConflicts(true);

        catm.insert(appointmentObj);
        ICalExportRequest exportRequest = new ICalExportRequest(folderID);
        ICalExportResponse response = getClient().execute(exportRequest);

        String iCal = response.getICal();

        assertTrue(iCal.contains(title));
    }

}
