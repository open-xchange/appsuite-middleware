package com.openexchange.webdav.xml.appointment;

import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.FolderTest;

public class Bug4395Test extends AppointmentTest {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Bug4395Test.class);

	private FolderObject folderObj = null;

    private int parentFolderId;

	public Bug4395Test(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		folderObj = null;
		parentFolderId = 0;
	}

	@Override
    protected void tearDown() throws Exception {
	    if (null != folderObj) {
	        if (0 != parentFolderId) {
	            FolderTest.deleteFolder(getSecondWebConversation(), new int[] { parentFolderId }, PROTOCOL + getHostName(), getSecondLogin(), getPassword(), context);
	        }
	    }
        super.tearDown();
    }

	public void testBug4395() throws Exception {
		final FolderObject sharedFolderObject = FolderTest.getAppointmentDefaultFolder(getSecondWebConversation(), PROTOCOL + getHostName(), getSecondLogin(), getPassword(), context);
		final int secondUserId = sharedFolderObject.getCreatedBy();

		folderObj = new FolderObject();
		folderObj.setFolderName("testBug4395" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.CALENDAR);
		folderObj.setType(FolderObject.PRIVATE);
		folderObj.setParentFolderID(1);

		OCLPermission[] permission = new OCLPermission[] {
			// FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
			FolderTest.createPermission( secondUserId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, true),
		};

		folderObj.setPermissionsAsArray( permission );

		parentFolderId = FolderTest.insertFolder(getSecondWebConversation(), folderObj, PROTOCOL + getHostName(), getSecondLogin(), getPassword(), context);

		permission = new OCLPermission[] {
			FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, false),
			FolderTest.createPermission( secondUserId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, true),
		};

		folderObj.setPermissionsAsArray( permission );
		folderObj.setObjectID(parentFolderId);

		FolderTest.updateFolder(getSecondWebConversation(), folderObj, getHostName(), getSecondLogin(), getPassword(), context);

		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testBug4395");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(parentFolderId);
		appointmentObj.setIgnoreConflicts(true);

		final int appointmentObjectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
		appointmentObj.setObjectID(appointmentObjectId);

		final Appointment loadAppointment = loadAppointment(getWebConversation(), appointmentObjectId, parentFolderId, getHostName(), getLogin(), getPassword(), context);
		compareObject(appointmentObj, loadAppointment);

		FolderTest.deleteFolder(getSecondWebConversation(), new int[] { parentFolderId }, PROTOCOL + getHostName(), getSecondLogin(), getPassword(), context);
		folderObj = null;
		parentFolderId = 0;
	}
}
