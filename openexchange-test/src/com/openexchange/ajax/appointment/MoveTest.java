package com.openexchange.ajax.appointment;

import java.util.Date;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;

public class MoveTest extends AppointmentTest {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MoveTest.class);
    private String login;
    private String password;
    private String context;
    private int targetFolder;
    private int objectId;

	public MoveTest(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
	    super.setUp();
	    login = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "login", "");
	    context = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "contextName", "defaultcontext");
	    password = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "password", "");
	    targetFolder = 0;
	}

	@Override
	protected void tearDown() throws Exception {
        if (0 != objectId) {
            deleteAppointment(getWebConversation(), objectId, targetFolder, PROTOCOL + getHostName(), getSessionId(), false);
        }
	    if (0 != targetFolder) {
	        com.openexchange.webdav.xml.FolderTest.deleteFolder(getWebConversation(), new int[] { targetFolder }, PROTOCOL + getHostName(), login, password, context);
	    }
	    super.tearDown();
	}

	public void testMove2PrivateFolder() throws Exception {
		final Appointment appointmentObj = new Appointment();
		final String date = String.valueOf(System.currentTimeMillis());
		appointmentObj.setTitle("testMove2PrivateFolder" + date);
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setOrganizer(User.User1.name());
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		appointmentObj.setShownAs(Appointment.RESERVED);
		objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());

		final FolderObject folderObj = com.openexchange.webdav.xml.FolderTest.createFolderObject(userId, "testMove2PrivateFolder" + System.currentTimeMillis(), FolderObject.CALENDAR, false);
		targetFolder = com.openexchange.webdav.xml.FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), login, password, context);

		appointmentObj.setParentFolderID(targetFolder);
		updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, targetFolder, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());
	}

	public void testMove2PublicFolder() throws Exception {
		final Appointment appointmentObj = new Appointment();
		final String date = String.valueOf(System.currentTimeMillis());
		appointmentObj.setTitle("testMove2PublicFolder" + date);
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setOrganizer(User.User1.name());
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		appointmentObj.setShownAs(Appointment.RESERVED);
		objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());

		final FolderObject folderObj = com.openexchange.webdav.xml.FolderTest.createFolderObject(userId, "testMove2PublicFolder" + System.currentTimeMillis(), FolderObject.CALENDAR, true);
		targetFolder = com.openexchange.webdav.xml.FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), login, password, context);

		appointmentObj.setParentFolderID(targetFolder);
		updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, targetFolder, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());
	}
}

