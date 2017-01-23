
package com.openexchange.ajax.appointment.bugtests;

import java.util.Date;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.container.Appointment;

public class Bug8836Test extends AppointmentTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Bug8836Test.class);

    @Test
    public void testBug8836() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testBug8836");
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();

        appointmentObj.setObjectID(objectId);
        appointmentObj.setPrivateFlag(true);

        Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        Date modified = new Date(loadAppointment.getLastModified().getTime() + 1);

        catm.update(appointmentFolderId, appointmentObj);

        loadAppointment = catm.get(appointmentFolderId, objectId);
        modified = new Date(loadAppointment.getLastModified().getTime() + 1);

        compareObject(appointmentObj, loadAppointment);
    }
}
