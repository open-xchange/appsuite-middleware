
package com.openexchange.ajax.appointment.bugtests;

import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.container.Appointment;

public class Bug8836Test extends AppointmentTest {

    @Test
    public void testBug8836() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testBug8836");
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();

        appointmentObj.setObjectID(objectId);
        appointmentObj.setPrivateFlag(true);

        Appointment loadAppointment = catm.get(appointmentFolderId, objectId);

        catm.update(appointmentFolderId, appointmentObj);

        loadAppointment = catm.get(appointmentFolderId, objectId);

        compareObject(appointmentObj, loadAppointment);
    }
}
