
package com.openexchange.ajax.appointment;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.util.Date;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.FolderTestManager;
import com.openexchange.tools.URLParameter;

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

        final FolderObject folderObj = FolderTestManager.createNewFolderObject("testCopy" + System.currentTimeMillis(), FolderObject.CALENDAR, FolderObject.PRIVATE, userId, 1);
        int targetFolderId = ftm.insertFolderOnServer(folderObj).getObjectID();

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, getSessionId());
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_COPY);
        parameter.setParameter(AJAXServlet.PARAMETER_ID, objectId1);
        parameter.setParameter(AJAXServlet.PARAMETER_FOLDERID, appointmentFolderId);
        parameter.setParameter(AppointmentFields.IGNORE_CONFLICTS, true);

        final JSONObject jsonObj = new JSONObject();
        jsonObj.put(FolderChildFields.FOLDER_ID, targetFolderId);
        Appointment copy = catm.copy(appointmentFolderId, objectId1, new ByteArrayInputStream(jsonObj.toString().getBytes(com.openexchange.java.Charsets.UTF_8)));
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
