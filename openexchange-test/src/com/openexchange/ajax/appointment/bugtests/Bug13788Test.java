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

import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertTrue;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug13788Test extends AbstractAJAXSession {

    private Appointment appointment, update;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        appointment = new Appointment();
        appointment.setTitle("Bug 13788 Test");
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setStartDate(D("01.10.2009 00:00", TimeZone.getTimeZone("UTC")));
        appointment.setEndDate(D("02.10.2009 00:00", TimeZone.getTimeZone("UTC")));
        appointment.setFullTime(true);
        appointment.setIgnoreConflicts(true);

        InsertRequest appointmentInsertRequest = new InsertRequest(appointment, getClient().getValues().getTimeZone());
        AppointmentInsertResponse appointmentInsertResponse = getClient().execute(appointmentInsertRequest);
        appointmentInsertResponse.fillAppointment(appointment);

        update = new Appointment();
        update.setObjectID(appointment.getObjectID());
        update.setParentFolderID(appointment.getParentFolderID());
        update.setLastModified(appointment.getLastModified());
        update.setIgnoreConflicts(true);
        update.setStartDate(D("03.10.2009 00:00", TimeZone.getTimeZone("UTC")));
        update.setEndDate(D("04.10.2009 00:00", TimeZone.getTimeZone("UTC")));
    }

    @Test
    public void testBug13788() throws Exception {
        UpdateRequest updateRequest = new UpdateRequest(update, getClient().getValues().getTimeZone());
        UpdateResponse updateResponse = getClient().execute(updateRequest);
        appointment.setLastModified(updateResponse.getTimestamp());

        GetRequest getRequest = new GetRequest(appointment.getParentFolderID(), appointment.getObjectID());
        GetResponse getResponse = getClient().execute(getRequest);

        Appointment loadedAppointment = getResponse.getAppointment(getClient().getValues().getTimeZone());
        assertTrue("Lost fulltime flag.", loadedAppointment.getFullTime());
    }

}
