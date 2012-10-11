package com.openexchange.ajax.appointment;

import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;

public class MoveTest extends AppointmentTest {

	private static final Log LOG = LogFactory.getLog(MoveTest.class);

	public MoveTest(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
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
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());

		final String login = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "login", "");
		final String context = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "contextName", "defaultcontext");
		final String password = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "password", "");

		final FolderObject folderObj = com.openexchange.webdav.xml.FolderTest.createFolderObject(userId, "testMove2PrivateFolder" + System.currentTimeMillis(), FolderObject.CALENDAR, false);
		final int targetFolder = com.openexchange.webdav.xml.FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), login, password, context);

		appointmentObj.setParentFolderID(targetFolder);
		updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, targetFolder, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());

		deleteAppointment(getWebConversation(), objectId, targetFolder, PROTOCOL + getHostName(), getSessionId(), false);
		com.openexchange.webdav.xml.FolderTest.deleteFolder(getWebConversation(), new int[] { targetFolder }, PROTOCOL + getHostName(), login, password, context);
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
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());

		final String login = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "login", "");
		final String context = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "contextName", "defaultcontext");
		final String password = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "password", "");

		final FolderObject folderObj = com.openexchange.webdav.xml.FolderTest.createFolderObject(userId, "testMove2PublicFolder" + System.currentTimeMillis(), FolderObject.CALENDAR, true);
		final int targetFolder = com.openexchange.webdav.xml.FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), login, password, context);

		appointmentObj.setParentFolderID(targetFolder);
		updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, targetFolder, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());

		deleteAppointment(getWebConversation(), objectId, targetFolder, PROTOCOL + getHostName(), getSessionId(), false);
		com.openexchange.webdav.xml.FolderTest.deleteFolder(getWebConversation(), new int[] { targetFolder }, PROTOCOL + getHostName(), login, password, context);
	}
}

