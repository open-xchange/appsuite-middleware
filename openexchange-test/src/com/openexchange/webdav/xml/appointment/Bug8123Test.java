
package com.openexchange.webdav.xml.appointment;

import static org.junit.Assert.fail;
import java.util.Date;
import org.junit.Test;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.resource.Resource;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.GroupUserTest;

public class Bug8123Test extends AppointmentTest {

    @Test
    public void testBug8123() throws Exception {
        final Resource[] resource = GroupUserTest.searchResource(getWebConversation(), "*", new Date(0), getHostURI(), getLogin(), getPassword());

        if (resource.length == 0) {
            fail("no resource found for this test");
        }

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testBug8123");
        appointmentObj.setStartDate(startTime);
        appointmentObj.setEndDate(endTime);
        appointmentObj.setShownAs(Appointment.FREE);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        final Participant[] participant = new Participant[2];
        participant[0] = new UserParticipant();
        participant[0].setIdentifier(userId);
        participant[1] = new ResourceParticipant();
        participant[1].setIdentifier(resource[0].getIdentifier());

        appointmentObj.setParticipants(participant);

        final int objectId = insertAppointment(getWebConversation(), appointmentObj, getHostURI(), getLogin(), getPassword());

        appointmentObj.setObjectID(objectId);
        Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());
        compareObject(appointmentObj, loadAppointment);

        final Date modified = new Date(loadAppointment.getCreationDate().getTime() - 1000);

        loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostURI(), getLogin(), getPassword());
        compareObject(appointmentObj, loadAppointment);

        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());
    }
}
