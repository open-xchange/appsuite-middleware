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

import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * This class contains test methods of calendar problems described by Funambol.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class FunambolTest extends AbstractAJAXSession {

    private int folderId;
    private TimeZone timeZone;
    private List<Appointment> toDelete;

    public FunambolTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folderId = getClient().getValues().getPrivateAppointmentFolder();
        timeZone = getClient().getValues().getTimeZone();
        toDelete = new ArrayList<Appointment>();
    }

    private Appointment getAppointment() {
        Appointment appointment = new Appointment();
        appointment.setParentFolderID(folderId);
        appointment.setTitle("TestCreationTime");
        appointment.setStartDate(new Date(TimeTools.getHour(0, timeZone)));
        appointment.setEndDate(new Date(TimeTools.getHour(1, timeZone)));
        appointment.setIgnoreConflicts(true);
        return appointment;
    }

    @Test
    public void testAppointmentCreationTime() throws Throwable {
        Date lastModified = null;
        Date timeAfterCreation = null;
        Date timeBeforeCreation = null;
        Appointment reload = null;

        boolean potentialTimestamp = false;
        while (!potentialTimestamp) {
            Appointment appointment = getAppointment();
            // Sometimes requests are really, really fast and time of first insert is same as this time. Maybe it is more a problem of XEN and a
            // frozen clock there.
            timeBeforeCreation = new Date(getClient().getValues().getServerTime().getTime() - 1);

            final CommonInsertResponse insertResponse = getClient().execute(new InsertRequest(appointment, timeZone));
            insertResponse.fillObject(appointment);
            final GetResponse response = getClient().execute(new GetRequest(appointment));
            reload = response.getAppointment(timeZone);
            // reload.getCreationDate() does not have milliseconds.
            lastModified = reload.getLastModified();

            // This request is responded even faster than creating an appointment. Therefore this must be the second request.
            timeAfterCreation = new Date(getClient().getValues().getServerTime().getTime() + 1);

            System.out.println(lastModified.getTime());
            if (lastModified.getTime() % 1000 >= 500) {
                potentialTimestamp = true;
            }
            toDelete.add(appointment);
        }

        assertNotNull("Missing last modified", lastModified);
        assertTrue("Appointment creation time is not after time request before creation.", lastModified.after(timeBeforeCreation));
        assertTrue("Appointment creation time is not before time request after creation.", lastModified.before(timeAfterCreation));
        assertNotNull("Missing appointment", reload);
        assertEquals("Last modified and creation date are different.", L(lastModified.getTime() / 1000), L(reload.getCreationDate().getTime() / 1000));
    }
}
