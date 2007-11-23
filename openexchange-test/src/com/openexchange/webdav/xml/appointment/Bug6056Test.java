package com.openexchange.webdav.xml.appointment;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.FolderTest;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Bug6056Test extends AppointmentTest {
	
	private static final Log LOG = LogFactory.getLog(Bug6056Test.class);
	
	public Bug6056Test(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testBug6065() throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("Bug6065Test" + System.currentTimeMillis());
		folderObj.setParentFolderID(FolderObject.PUBLIC);
		folderObj.setModule(FolderObject.CALENDAR);
		folderObj.setType(FolderObject.PUBLIC);
		
		OCLPermission[] permission = new OCLPermission[] {
			FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int newFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostName(), getLogin(), getPassword());
		
		AppointmentObject appointmentObj = createAppointmentObject("Bug6065Test");
		appointmentObj.setParentFolderID(newFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		int objectId = insertAppointment(getWebConversation(), appointmentObj, getHostName(), getLogin(), getPassword());
		appointmentObj.setObjectID(objectId);
		
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, newFolderId, getHostName(), getLogin(), getPassword());
		Date lastModified = loadAppointment.getLastModified();
		
		deleteAppointment(getWebConversation(), objectId, newFolderId, getHostName(), getLogin(), getPassword());
		
		boolean found = false;
		AppointmentObject[] appointmentArray = AppointmentTest.listAppointment(getWebConversation(), newFolderId, lastModified, false, true, getHostName(), getLogin(), getPassword());
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId) {
				found = true;
			}
		}
		
		assertTrue("object not found in delete response", found);
		
	
		FolderTest.deleteFolder(getWebConversation(), new int[] { newFolderId }, getHostName(), getLogin(), getPassword());
	}
}