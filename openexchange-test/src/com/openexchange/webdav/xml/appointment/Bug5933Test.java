package com.openexchange.webdav.xml.appointment;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.FolderTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Bug5933Test extends AppointmentTest {
	
	private static final Log LOG = LogFactory.getLog(Bug5933Test.class);
	
	public Bug5933Test(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testBug5933() throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("Bug5933Test" + System.currentTimeMillis());
		folderObj.setParentFolderID(FolderObject.PUBLIC);
		folderObj.setModule(FolderObject.CALENDAR);
		folderObj.setType(FolderObject.PUBLIC);
		
		OCLPermission[] permission = new OCLPermission[] {
			FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int newFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostName(), getLogin(), getPassword());
		
		AppointmentObject appointmentObj = createAppointmentObject("Bug5933Test");
		appointmentObj.setParentFolderID(newFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		folderObj = FolderTest.getAppointmentDefaultFolder(getSecondWebConversation(), getHostName(), getSecondLogin(), getPassword());
		int secondUserId = folderObj.getCreatedBy();
				
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[1];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(secondUserId);
		
		appointmentObj.setParticipants(participants);
		
		int objectId = insertAppointment(getWebConversation(), appointmentObj, getHostName(), getLogin(), getPassword());
		appointmentObj.setObjectID(objectId);
		
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, newFolderId, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
	
		deleteAppointment(getWebConversation(), objectId, newFolderId, getHostName(), getLogin(), getPassword());
		FolderTest.deleteFolder(getWebConversation(), new int[] { newFolderId }, getHostName(), getLogin(), getPassword());
	}
}