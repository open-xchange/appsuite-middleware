
package com.openexchange.ajax.appointment.bugtests;

import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.FolderTestManager;

public class Bug9089Test extends AppointmentTest {

    /**
     * Creates a normal appointment in a public folder and then try to modify the private flag.
     * Expected result is that an error message is thrown
     */
    @Test
    public void testBug9089() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testBug9089" + System.currentTimeMillis());
        folderObj.setParentFolderID(FolderObject.PUBLIC);
        folderObj.setModule(FolderObject.CALENDAR);
        folderObj.setType(FolderObject.PUBLIC);

        final OCLPermission[] permission = new OCLPermission[] { FolderTestManager.createPermission(userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
        };

        folderObj.setPermissionsAsArray(permission);

        final int newFolderId = ftm.insertFolderOnServer(folderObj).getObjectID();

        final Appointment appointmentObj = createAppointmentObject("testBug9089");
        appointmentObj.setParentFolderID(newFolderId);
        appointmentObj.setIgnoreConflicts(true);

        final int objectId = catm.insert(appointmentObj).getObjectID();
        catm.get(newFolderId, objectId);

        appointmentObj.setPrivateFlag(true);

        catm.update(newFolderId, appointmentObj);

        catm.get(newFolderId, objectId);
    }
}
