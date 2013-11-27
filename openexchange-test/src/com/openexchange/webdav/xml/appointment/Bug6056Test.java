package com.openexchange.webdav.xml.appointment;

import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.FolderTest;

public class Bug6056Test extends AppointmentTest {

	private static final Log LOG = com.openexchange.log.Log.loggerFor(Bug6056Test.class);

	public Bug6056Test(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testBug6065() throws Exception {
		final FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("Bug6065Test" + System.currentTimeMillis());
		folderObj.setParentFolderID(FolderObject.PUBLIC);
		folderObj.setModule(FolderObject.CALENDAR);
		folderObj.setType(FolderObject.PUBLIC);

		final OCLPermission[] permission = new OCLPermission[] {
			FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
		};

		folderObj.setPermissionsAsArray( permission );

		final int newFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostName(), getLogin(), getPassword(), context);

		final Appointment appointmentObj = createAppointmentObject("Bug6065Test");
		appointmentObj.setParentFolderID(newFolderId);
		appointmentObj.setIgnoreConflicts(true);

		final int objectId = insertAppointment(getWebConversation(), appointmentObj, getHostName(), getLogin(), getPassword(), context);
		appointmentObj.setObjectID(objectId);

		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, newFolderId, getHostName(), getLogin(), getPassword(), context);
		final Date lastModified = loadAppointment.getLastModified();

		deleteAppointment(getWebConversation(), objectId, newFolderId, getHostName(), getLogin(), getPassword(), context);

		boolean found = false;
		final Appointment[] appointmentArray = AppointmentTest.listAppointment(getWebConversation(), newFolderId, lastModified, false, true, getHostName(), getLogin(), getPassword(), context);
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId) {
				found = true;
			}
		}

		assertTrue("object not found in delete response", found);


		FolderTest.deleteFolder(getWebConversation(), new int[] { newFolderId }, getHostName(), getLogin(), getPassword(), context);
	}
}
