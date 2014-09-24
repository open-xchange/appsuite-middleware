package com.openexchange.ajax.appointment;

import java.io.ByteArrayInputStream;
import java.util.Date;
import org.json.JSONObject;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.URLParameter;

public class CopyTest extends AppointmentTest {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CopyTest.class);
    private int targetFolderId;
    private String login;
    private String context;
    private String password;

	public CopyTest(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
	    super.setUp();
	    targetFolderId = 0;
	    login = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "login", "");
	    context = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "contextName", "defaultContext");
	    password = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "password", "");
	}

    @Override
    protected void tearDown() throws Exception {
        if (0 != targetFolderId) {
            com.openexchange.webdav.xml.FolderTest.deleteFolder(getWebConversation(), new int[] { targetFolderId }, PROTOCOL + getHostName(), login, password, context);
        }
        super.tearDown();
    }

	public void testCopy() throws Exception {
		final Appointment appointmentObj = new Appointment();
		final String date = String.valueOf(System.currentTimeMillis());
		appointmentObj.setTitle("testCopy" + date);
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.FREE);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId1 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());



		final FolderObject folderObj = com.openexchange.webdav.xml.FolderTest.createFolderObject(userId, "testCopy" + System.currentTimeMillis(), FolderObject.CALENDAR, false);
		targetFolderId = com.openexchange.webdav.xml.FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), login, password, context);

		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, getSessionId());
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_COPY);
		parameter.setParameter(AJAXServlet.PARAMETER_ID, objectId1);
		parameter.setParameter(AJAXServlet.PARAMETER_FOLDERID, appointmentFolderId);
		parameter.setParameter(AppointmentFields.IGNORE_CONFLICTS, true);

		final JSONObject jsonObj = new JSONObject();
		jsonObj.put(FolderChildFields.FOLDER_ID, targetFolderId);
		final ByteArrayInputStream bais = new ByteArrayInputStream(jsonObj.toString().getBytes(com.openexchange.java.Charsets.UTF_8));
		final WebRequest req = new PutMethodWebRequest(PROTOCOL + getHostName() + APPOINTMENT_URL + parameter.getURLParameters(), bais, "text/javascript");
		final WebResponse resp = getWebConversation().getResponse(req);

		assertEquals(200, resp.getResponseCode());

		final Response response = Response.parse(resp.getText());

		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}

		int objectId2 = 0;

		final JSONObject data = (JSONObject)response.getData();
		if (data.has(DataFields.ID)) {
			objectId2 = data.getInt(DataFields.ID);
		}

		if (data.has("conflicts")) {
			fail("conflicts found!");
		}

		deleteAppointment(getWebConversation(), objectId1, appointmentFolderId, PROTOCOL + getHostName(), getSessionId(), false);
		if (objectId2 > 0) {
			deleteAppointment(getWebConversation(), objectId2, targetFolderId, PROTOCOL + getHostName(), getSessionId(), false);
		}
	}
}
