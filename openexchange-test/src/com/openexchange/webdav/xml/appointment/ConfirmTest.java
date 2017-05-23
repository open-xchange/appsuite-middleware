
package com.openexchange.webdav.xml.appointment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.FolderTest;

public class ConfirmTest extends AppointmentTest {

    @Test
    public void testConfirm() throws Exception {
        final FolderObject sharedFolderObject = FolderTest.getAppointmentDefaultFolder(getSecondWebConversation(), getHostURI(), getSecondLogin(), getPassword());
        final int secondUserId = sharedFolderObject.getCreatedBy();

        final Appointment appointmentObj = createAppointmentObject("testConfirm");
        appointmentObj.setIgnoreConflicts(true);
        UserParticipant[] participants = new UserParticipant[2];
        participants[0] = new UserParticipant();
        participants[0].setIdentifier(userId);
        participants[1] = new UserParticipant();
        participants[1].setIdentifier(secondUserId);

        appointmentObj.setParticipants(participants);

        final int objectId = insertAppointment(getWebConversation(), appointmentObj, getHostURI(), getLogin(), getPassword());

        confirmAppointment(getSecondWebConversation(), objectId, Appointment.ACCEPT, null, getHostURI(), getSecondLogin(), getPassword());

        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());

        boolean found = false;

        participants = loadAppointment.getUsers();
        for (int a = 0; a < participants.length; a++) {
            if (participants[a].getIdentifier() == secondUserId) {
                found = true;
                assertEquals("wrong confirm status", Appointment.ACCEPT, participants[a].getConfirm());
            }
        }

        assertTrue("user participant with id " + secondUserId + " not found", found);

        deleteAppointment(getWebConversation(), new int[][] { { objectId, appointmentFolderId } }, getHostURI(), getLogin(), getPassword());
    }
}
