
package com.openexchange.webdav.xml.appointment;

import java.util.Date;
import org.junit.Test;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.FolderTest;

public class Bug8196Test extends AppointmentTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Bug8196Test.class);

    public Bug8196Test() {
        super();
    }

    @Test
    public void testBug8196() throws Exception {
        final FolderObject folderObj = FolderTest.getAppointmentDefaultFolder(getSecondWebConversation(), getHostURI(), getSecondLogin(), getPassword());
        final int secondAppointmentFolderId = folderObj.getObjectID();
        final int secondUserId = folderObj.getCreatedBy();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testBug8196");
        appointmentObj.setStartDate(startTime);
        appointmentObj.setEndDate(endTime);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setAlarmFlag(true);
        appointmentObj.setAlarm(15);

        final Participant[] userParticipant = new UserParticipant[2];
        userParticipant[0] = new UserParticipant();
        userParticipant[0].setIdentifier(userId);

        userParticipant[1] = new UserParticipant();
        userParticipant[1].setIdentifier(secondUserId);

        appointmentObj.setParticipants(userParticipant);

        final int objectId = insertAppointment(getWebConversation(), appointmentObj, getHostURI(), getLogin(), getPassword());
        appointmentObj.setObjectID(objectId);

        appointmentObj.removeAlarm();

        updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());

        appointmentObj.removeAlarm();
        appointmentObj.setAlarmFlag(false);
        appointmentObj.setParentFolderID(secondAppointmentFolderId);

        Appointment loadAppointment = loadAppointment(getSecondWebConversation(), objectId, secondAppointmentFolderId, getHostURI(), getSecondLogin(), getPassword());
        compareObject(appointmentObj, loadAppointment);

        final Date modified = new Date(loadAppointment.getLastModified().getTime() - 1000);

        loadAppointment = loadAppointment(getSecondWebConversation(), objectId, secondAppointmentFolderId, modified, getHostURI(), getSecondLogin(), getPassword());
        compareObject(appointmentObj, loadAppointment);

        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());
    }
}
