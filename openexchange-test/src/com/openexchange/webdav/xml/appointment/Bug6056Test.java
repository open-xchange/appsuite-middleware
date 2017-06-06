
package com.openexchange.webdav.xml.appointment;

import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.junit.Test;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.FolderTest;

public class Bug6056Test extends AppointmentTest {

    @Test
    public void testBug6065() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("Bug6065Test" + System.currentTimeMillis());
        folderObj.setParentFolderID(FolderObject.PUBLIC);
        folderObj.setModule(FolderObject.CALENDAR);
        folderObj.setType(FolderObject.PUBLIC);

        final OCLPermission[] permission = new OCLPermission[] { FolderTest.createPermission(userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
        };

        folderObj.setPermissionsAsArray(permission);

        final int newFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());

        final Appointment appointmentObj = createAppointmentObject("Bug6065Test");
        appointmentObj.setParentFolderID(newFolderId);
        appointmentObj.setIgnoreConflicts(true);

        final int objectId = insertAppointment(getWebConversation(), appointmentObj, getHostURI(), getLogin(), getPassword());
        appointmentObj.setObjectID(objectId);

        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, newFolderId, getHostURI(), getLogin(), getPassword());
        final Date lastModified = loadAppointment.getLastModified();

        deleteAppointment(getWebConversation(), objectId, newFolderId, getHostURI(), getLogin(), getPassword());

        boolean found = false;
        final Appointment[] appointmentArray = listAppointment(getWebConversation(), newFolderId, lastModified, false, true, getHostURI(), getLogin(), getPassword());
        for (int a = 0; a < appointmentArray.length; a++) {
            if (appointmentArray[a].getObjectID() == objectId) {
                found = true;
            }
        }

        assertTrue("object not found in delete response", found);

        FolderTest.deleteFolder(getWebConversation(), new int[] { newFolderId }, getHostURI(), getLogin(), getPassword());
    }
}
