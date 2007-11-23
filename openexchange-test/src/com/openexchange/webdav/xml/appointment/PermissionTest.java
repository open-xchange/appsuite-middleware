package com.openexchange.webdav.xml.appointment;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.XmlServlet;

public class PermissionTest extends AppointmentTest {
	
	public PermissionTest(String name) {
		super(name);
	}
	
	public void testDummy() {
		
	}
	
	public void testInsertAppointmentInPrivateFolderWithoutPermission() throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testInsertAppointmentInPrivateFolderWithoutPermission" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.CALENDAR);
		folderObj.setType(FolderObject.PRIVATE);
		folderObj.setParentFolderID(1);
		
		OCLPermission[] permission = new OCLPermission[] { 
			FolderTest.createPermission( userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS),
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testInsertAppointmentInPrivateFolderWithoutPermission");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(parentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		try {
			final int appointmentObjectId = insertAppointment(getSecondWebConversation(), appointmentObj, PROTOCOL + getHostName(), getSecondLogin(), getPassword());
			fail("permission exception expected!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
		}
		
		FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testInsertAppointmentInPublicFolderWithoutPermission() throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testInsertAppointmentInPublicFolderWithoutPermission" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.CALENDAR);
		folderObj.setType(FolderObject.PUBLIC);
		folderObj.setParentFolderID(2);
		
		OCLPermission[] permission = new OCLPermission[] { 
			FolderTest.createPermission( userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS)
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testInsertAppointmentInPublicFolderWithoutPermission");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(parentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		try {
			final int appointmentObjectId = insertAppointment(getSecondWebConversation(), appointmentObj, PROTOCOL + getHostName(), getSecondLogin(), getPassword());
			fail("permission exception expected!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
		}
		
		FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testUpdateAppointmentInPrivateFolderWithoutPermission() throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testInsertAppointmentInPrivateFolderWithoutPermission" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.CALENDAR);
		folderObj.setType(FolderObject.PRIVATE);
		folderObj.setParentFolderID(1);
		
		OCLPermission[] permission = new OCLPermission[] { 
			FolderTest.createPermission( userId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS),
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testInsertAppointmentInPrivateFolderWithoutPermission");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(parentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		final int appointmentObjectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		appointmentObj.setObjectID(appointmentObjectId);
		
		try {
			updateAppointment(getSecondWebConversation(), appointmentObj, appointmentObjectId, parentFolderId, PROTOCOL + getHostName(), getSecondLogin(), getPassword());
			fail("permission exception expected!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
		}
		
		FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testUpdateAppointmentInPublicFolderWithoutPermission() throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testUpdateAppointmentInPublicFolderWithoutPermission" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.CALENDAR);
		folderObj.setType(FolderObject.PUBLIC);
		folderObj.setParentFolderID(2);
		
		OCLPermission[] permission = new OCLPermission[] { 
			FolderTest.createPermission( userId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS)
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testUpdateAppointmentInPublicFolderWithoutPermission");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(parentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		final int appointmentObjectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		appointmentObj.setObjectID(appointmentObjectId);
		
		try {
			updateAppointment(getSecondWebConversation(), appointmentObj, appointmentObjectId, parentFolderId, PROTOCOL + getHostName(), getSecondLogin(), getPassword());
			fail("permission exception expected!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
		}
		
		FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testDeleteAppointmentInPrivateFolderWithoutPermission() throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testDeleteAppointmentInPrivateFolderWithoutPermission" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.CALENDAR);
		folderObj.setType(FolderObject.PRIVATE);
		folderObj.setParentFolderID(1);
		
		OCLPermission[] permission = new OCLPermission[] { 
			FolderTest.createPermission( userId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS),
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testDeleteAppointmentInPrivateFolderWithoutPermission");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(parentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		final int appointmentObjectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		appointmentObj.setObjectID(appointmentObjectId);
		
		try {
			deleteAppointment(getSecondWebConversation(), appointmentObjectId, parentFolderId, PROTOCOL + getHostName(), getSecondLogin(), getPassword());
			fail("permission exception expected!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
		}
		
		FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testDeleteAppointmentInPublicFolderWithoutPermission() throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testDeleteAppointmentInPublicFolderWithoutPermission" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.CALENDAR);
		folderObj.setType(FolderObject.PUBLIC);
		folderObj.setParentFolderID(2);
		
		OCLPermission[] permission = new OCLPermission[] { 
			FolderTest.createPermission( userId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS)
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testDeleteAppointmentInPublicFolderWithoutPermission");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(parentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		final int appointmentObjectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		appointmentObj.setObjectID(appointmentObjectId);
		
		try {
			deleteAppointment(getSecondWebConversation(), appointmentObjectId, parentFolderId, PROTOCOL + getHostName(), getSecondLogin(), getPassword());
			fail("permission exception expected!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
		}
		
		FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
}

