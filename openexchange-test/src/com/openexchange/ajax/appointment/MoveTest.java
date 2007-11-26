package com.openexchange.ajax.appointment;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.FolderObject;

public class MoveTest extends AppointmentTest {

	private static final Log LOG = LogFactory.getLog(MoveTest.class);
	
	public MoveTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testMove2PrivateFolder() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		String date = String.valueOf(System.currentTimeMillis());
		appointmentObj.setTitle("testMove2PrivateFolder" + date);
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		appointmentObj.setShownAs(AppointmentObject.RESERVED);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		String login = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "login", "");
		String password = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "password", "");
		
		FolderObject folderObj = com.openexchange.webdav.xml.FolderTest.createFolderObject(userId, "testMove2PrivateFolder" + System.currentTimeMillis(), FolderObject.CALENDAR, false);
		int targetFolder = com.openexchange.webdav.xml.FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), login, password);

		appointmentObj.setParentFolderID(targetFolder);
		updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, targetFolder, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());
		
		deleteAppointment(getWebConversation(), objectId, targetFolder, PROTOCOL + getHostName(), getSessionId());
		com.openexchange.webdav.xml.FolderTest.deleteFolder(getWebConversation(), new int[] { targetFolder }, PROTOCOL + getHostName(), login, password);
	}	
	
	public void testMove2PublicFolder() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		String date = String.valueOf(System.currentTimeMillis());
		appointmentObj.setTitle("testMove2PublicFolder" + date);
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		appointmentObj.setShownAs(AppointmentObject.RESERVED);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		String login = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "login", "");
		String password = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "password", "");
		
		FolderObject folderObj = com.openexchange.webdav.xml.FolderTest.createFolderObject(userId, "testMove2PublicFolder" + System.currentTimeMillis(), FolderObject.CALENDAR, true);
		int targetFolder = com.openexchange.webdav.xml.FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), login, password);

		appointmentObj.setParentFolderID(targetFolder);
		updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, targetFolder, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());
		
		deleteAppointment(getWebConversation(), objectId, targetFolder, PROTOCOL + getHostName(), getSessionId());
		com.openexchange.webdav.xml.FolderTest.deleteFolder(getWebConversation(), new int[] { targetFolder }, PROTOCOL + getHostName(), login, password);
	}
}

