package com.openexchange.ajax.appointment;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.OCLPermission;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.FolderTest;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Bug9089Test extends AppointmentTest {

	private static final Log LOG = LogFactory.getLog(Bug9089Test.class);
	
	public Bug9089Test(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	/**
	 * Creates a normal appointment in a public folder and then try to modify the private flag.
	 * Expected result is that an error message is thrown
	 */
	public void testBug9089() throws Exception {
		final FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testBug9089" + System.currentTimeMillis());
		folderObj.setParentFolderID(FolderObject.PUBLIC);
		folderObj.setModule(FolderObject.CALENDAR);
		folderObj.setType(FolderObject.PUBLIC);
		
		final OCLPermission[] permission = new OCLPermission[] {
			FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int newFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostName(), getLogin(), getPassword());
		
		final AppointmentObject appointmentObj = createAppointmentObject("testBug9089");
		appointmentObj.setParentFolderID(newFolderId);
		appointmentObj.setIgnoreConflicts(true);

		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, newFolderId, timeZone, getHostName(), getSessionId());
		Date modified = loadAppointment.getLastModified();
		
		appointmentObj.setPrivateFlag(true);
		
		try {
			updateAppointment(getWebConversation(), appointmentObj, objectId, newFolderId, modified, timeZone, getHostName(), getSessionId());
			fail("exception expected");
		} catch (TestException exc) {
			assertTrue(true);
		}
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, newFolderId, timeZone, getHostName(), getSessionId());
		modified = loadAppointment.getLastModified();
		
		deleteAppointment(getWebConversation(), objectId, newFolderId, modified, getHostName(), getSessionId());
		FolderTest.deleteFolder(getWebConversation(), new int[] { newFolderId }, modified, getHostName(), getLogin(), getPassword());
	}
}
