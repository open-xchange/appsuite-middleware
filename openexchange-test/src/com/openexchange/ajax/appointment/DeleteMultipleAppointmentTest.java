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

import static org.junit.Assert.assertFalse;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link DeleteMultipleAppointmentTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class DeleteMultipleAppointmentTest extends AppointmentTest {

    private Appointment appointment1, appointment2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        appointment1 = new Appointment();
        appointment1.setIgnoreConflicts(true);
        appointment1.setTitle("Test 1");
        appointment1.setTimezone(timeZone.getID());
        appointment1.setStartDate(new Date());
        appointment1.setEndDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2));
        appointment1.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        InsertRequest insReq1 = new InsertRequest(appointment1, timeZone);
        AppointmentInsertResponse insRes1 = getClient().execute(insReq1);
        insRes1.fillAppointment(appointment1);

        appointment2 = new Appointment();
        appointment2.setIgnoreConflicts(true);
        appointment2.setTitle("Test 2");
        appointment2.setTimezone(timeZone.getID());
        appointment2.setStartDate(new Date());
        appointment2.setEndDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2));
        appointment2.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        InsertRequest insReq2 = new InsertRequest(appointment2, timeZone);
        AppointmentInsertResponse insRes2 = getClient().execute(insReq2);
        insRes2.fillAppointment(appointment2);
    }

    @Test
    public void testDeleteMultiple() throws Exception {
        int[] ids = new int[] { appointment1.getObjectID(), appointment2.getObjectID() };
        DeleteRequest delReq = new DeleteRequest(ids, getClient().getValues().getPrivateAppointmentFolder(), new Date(System.currentTimeMillis() + 300000L), true);
        CommonDeleteResponse delRes = getClient().execute(delReq);
        assertFalse("Multiple delete failed: " + delRes.getErrorMessage(), delRes.hasError());
    }
}
