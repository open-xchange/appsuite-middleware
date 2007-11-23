package com.openexchange.webdav.xml.appointment;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.XmlServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Bug4395Test extends AppointmentTest {
	
	private static final Log LOG = LogFactory.getLog(Bug4395Test.class);
	
	public Bug4395Test(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testBug4395() throws Exception {
		final FolderObject sharedFolderObject = FolderTest.getAppointmentDefaultFolder(getSecondWebConversation(), PROTOCOL + getHostName(), getSecondLogin(), getPassword());
		int secondUserId = sharedFolderObject.getCreatedBy();
		
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testBug4395" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.CALENDAR);
		folderObj.setType(FolderObject.PRIVATE);
		folderObj.setParentFolderID(1);
		
		OCLPermission[] permission = new OCLPermission[] { 
			// FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
			FolderTest.createPermission( secondUserId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, true),
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int parentFolderId = FolderTest.insertFolder(getSecondWebConversation(), folderObj, PROTOCOL + getHostName(), getSecondLogin(), getPassword());
		
		permission = new OCLPermission[] { 
			FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, false),
			FolderTest.createPermission( secondUserId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, true),
		};
		
		folderObj.setPermissionsAsArray( permission );
		folderObj.setObjectID(parentFolderId);
		
		FolderTest.updateFolder(getSecondWebConversation(), folderObj, getHostName(), getSecondLogin(), getPassword());
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testBug4395");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(parentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		final int appointmentObjectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		appointmentObj.setObjectID(appointmentObjectId);

		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), appointmentObjectId, parentFolderId, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		FolderTest.deleteFolder(getSecondWebConversation(), new int[] { parentFolderId }, PROTOCOL + getHostName(), getSecondLogin(), getPassword());
	}
}