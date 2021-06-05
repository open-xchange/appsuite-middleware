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
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalExportRequest;
import com.openexchange.ajax.importexport.actions.ICalExportResponse;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug19089Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug19089Test extends ManagedAppointmentTest {

    private Appointment appointment;

    private final String tzid = "Europe/Berlin";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        appointment = new Appointment();
        appointment.setStartDate(D("01.04.2011 08:00"));
        appointment.setEndDate(D("01.04.2011 09:00"));
        appointment.setTimezone(tzid);
        appointment.setTitle("Test Bug 19089");
        appointment.setParentFolderID(folder.getObjectID());
        appointment.setIgnoreConflicts(true);

        catm.insert(appointment);
    }

    @Test
    public void testBug19089() throws Exception {
        ICalExportRequest request = new ICalExportRequest(folder.getObjectID());
        ICalExportResponse response = getClient().execute(request);
        String ical = (String) response.getData();
        // System.out.println(ical);
        assertTrue("Export should contain a VTIMEZONE Object" + System.getProperty("line.separator") + ical, ical.contains("BEGIN:VTIMEZONE"));
    }

}
