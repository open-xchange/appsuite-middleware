
package com.openexchange.webdav.xml.appointment;

import org.junit.Test;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.FolderTest;

public class Bug5933Test extends AppointmentTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Bug5933Test.class);

    public Bug5933Test() {
        super();
    }

    @Test
    public void testBug5933() throws Exception {
        FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("Bug5933Test" + System.currentTimeMillis());
        folderObj.setParentFolderID(FolderObject.PUBLIC);
        folderObj.setModule(FolderObject.CALENDAR);
        folderObj.setType(FolderObject.PUBLIC);

        final OCLPermission[] permission = new OCLPermission[] { FolderTest.createPermission(userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
        };

        folderObj.setPermissionsAsArray(permission);

        final int newFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());

        final Appointment appointmentObj = createAppointmentObject("Bug5933Test");
        appointmentObj.setParentFolderID(newFolderId);
        appointmentObj.setIgnoreConflicts(true);

        folderObj = FolderTest.getAppointmentDefaultFolder(getSecondWebConversation(), getHostURI(), getSecondLogin(), getPassword());
        final int secondUserId = folderObj.getCreatedBy();

        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[1];
        participants[0] = new UserParticipant();
        participants[0].setIdentifier(secondUserId);

        appointmentObj.setParticipants(participants);

        final int objectId = insertAppointment(getWebConversation(), appointmentObj, getHostURI(), getLogin(), getPassword());
        appointmentObj.setObjectID(objectId);

        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, newFolderId, getHostURI(), getLogin(), getPassword());
        compareObject(appointmentObj, loadAppointment);

        deleteAppointment(getWebConversation(), objectId, newFolderId, getHostURI(), getLogin(), getPassword());
        FolderTest.deleteFolder(getWebConversation(), new int[] { newFolderId }, getHostURI(), getLogin(), getPassword());
    }
}
