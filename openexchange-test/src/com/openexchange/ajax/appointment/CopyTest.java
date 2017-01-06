
package com.openexchange.ajax.appointment;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.util.Date;
import java.util.UUID;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.FolderTestManager;

public class CopyTest extends AppointmentTest {

    @Test
    public void testCopy() throws Exception {
        final Appointment appointmentObj = new Appointment();
        final String date = String.valueOf(System.currentTimeMillis());
        appointmentObj.setTitle("testCopy" + date);
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.FREE);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId1 = catm.insert(appointmentObj).getObjectID();

        final FolderObject folderObj = FolderTestManager.createNewFolderObject("testCopy" + UUID.randomUUID().toString(), FolderObject.CALENDAR, FolderObject.PRIVATE, userId, 1);
        int targetFolderId = ftm.insertFolderOnServer(folderObj).getObjectID();

        final JSONObject jsonObj = new JSONObject();
        jsonObj.put(FolderChildFields.FOLDER_ID, targetFolderId);
        Appointment copy = catm.copy(appointmentFolderId, objectId1, jsonObj, true);
        assertNotNull(copy);

        final Response response = catm.getLastResponse().getResponse();

        if (response.hasError()) {
            fail("json error: " + response.getErrorMessage());
        }

        final JSONObject data = (JSONObject) response.getData();
        if (data.has("conflicts")) {
            fail("conflicts found!");
        }
    }
}
