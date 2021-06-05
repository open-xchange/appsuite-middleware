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

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentUpdatesResponse;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link UpdatesForModifiedAndDeletedTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class UpdatesForModifiedAndDeletedTest extends AbstractAppointmentTest {

    /**
     * Initializes a new {@link UpdatesForModifiedAndDeletedTest}.
     * 
     * @param name
     */
    public UpdatesForModifiedAndDeletedTest() {
        super();
    }

    /**
     * Test http://oxpedia.org/wiki/index.php?title=HTTP_API#Get_updated_appointments
     * 
     * @throws Exception
     */
    @Test
    public void testUpdatesForModifiedAndDeleted() throws Exception {
        // insert some
        final int numberOfAppointments = 8;
        List<Appointment> newAppointments = createAndPersistSeveral("testAppointment", numberOfAppointments);

        // update 2
        List<Appointment> updatedAppointments = new ArrayList<Appointment>(2);
        List<Integer> expectUpdatedAppointmentIds = new ArrayList<Integer>(2);
        updatedAppointments.add(newAppointments.get(0));
        expectUpdatedAppointmentIds.add(I(newAppointments.get(0).getObjectID()));
        updatedAppointments.add(newAppointments.get(1));
        expectUpdatedAppointmentIds.add(I(newAppointments.get(1).getObjectID()));
        updateAppointments(updatedAppointments);

        // delete 2
        List<Appointment> deletedAppointments = new ArrayList<Appointment>(2);
        List<Integer> expectDeletedAppointmentIds = new ArrayList<Integer>(2);
        deletedAppointments.add(newAppointments.get(2));
        expectDeletedAppointmentIds.add(I(newAppointments.get(2).getObjectID()));
        deletedAppointments.add(newAppointments.get(3));
        expectDeletedAppointmentIds.add(I(newAppointments.get(3).getObjectID()));
        deleteAppointments(deletedAppointments);

        // check modified with timestamp from last 
        Date lastModified = newAppointments.get(numberOfAppointments - 1).getLastModified();
        int[] cols = new int[] { Appointment.OBJECT_ID, Appointment.TITLE };
        AppointmentUpdatesResponse modifiedAppointmentsResponse = listModifiedAppointments(appointmentFolderId, cols, lastModified);
        assertTrue(modifiedAppointmentsResponse.getNewOrModifiedIds().containsAll(expectUpdatedAppointmentIds));
        assertTrue(modifiedAppointmentsResponse.getDeletedIds().containsAll(expectDeletedAppointmentIds));

        // cleanup: delete all remaining
        newAppointments.removeAll(deletedAppointments);
        deleteAppointments(newAppointments);

    }
}
