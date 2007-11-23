package com.openexchange.webdav.xml.appointment;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.Group;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.GroupUserTest;
import com.openexchange.webdav.xml.XmlServlet;
import java.util.Date;

public class ListTest extends AppointmentTest {
	
	public ListTest(String name) {
		super(name);
	}
	
	public void testPropFindWithModified() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testPropFindWithModified");
		appointmentObj.setIgnoreConflicts(true);
		int objectId1 = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		int objectId2 = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		final AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId1, appointmentFolderId, getHostName(), getLogin(), getPassword());
		final Date modified = loadAppointment.getCreationDate();
		
		AppointmentObject[] appointmentArray = listAppointment(webCon, appointmentFolderId, modified, true, false, PROTOCOL + hostName, login, password);
		
		assertTrue("check response", appointmentArray.length >= 2);
		
		int[][] objectIdAndFolderId = { {objectId1, appointmentFolderId }, { objectId2, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
		
	}
	
	public void testPropFindInPublicFolder() throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testPropFindInPublicFolder" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.CALENDAR);
		folderObj.setType(FolderObject.PRIVATE);
		folderObj.setParentFolderID(1);
		
		OCLPermission[] permission = new OCLPermission[] { 
			FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testPropFindInPublicFolder");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(parentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		int objectId = insertAppointment(getWebConversation(), appointmentObj, getHostName(), getLogin(), getPassword());
		
		final AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, parentFolderId, getHostName(), getLogin(), getPassword());
		final Date modified = loadAppointment.getCreationDate();
		
		AppointmentObject[] appointmentArray = listAppointment(webCon, appointmentFolderId, modified, true, false, PROTOCOL + hostName, login, password);
		
		boolean found = true;
		
		for (int a = 0; a < appointmentArray.length; a++) {
			if (objectId == appointmentArray[a].getObjectID()) {
				found = true;
				break;
			}
		}
		
		assertTrue("object not found in response", found);
		
		deleteAppointment(getWebConversation(), objectId, parentFolderId, getHostName(), getLogin(), getPassword());
		FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostName(), getLogin(), getPassword());
	}
	
	public void testPropFindInPublicFolderWithGroupPermission() throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testPropFindInPublicFolderWithGroupPermission" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.CALENDAR);
		folderObj.setType(FolderObject.PRIVATE);
		folderObj.setParentFolderID(1);
		
		
		int usersGroupId = 1; // Users
		
		OCLPermission[] permission = new OCLPermission[] { 
			FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
			FolderTest.createPermission( usersGroupId, true, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, false),
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testPropFindInPublicFolderWithGroupPermission");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(parentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		int objectId = insertAppointment(getWebConversation(), appointmentObj, getHostName(), getLogin(), getPassword());
		
		final AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, parentFolderId, getHostName(), getLogin(), getPassword());
		final Date modified = loadAppointment.getCreationDate();

		AppointmentObject[] appointmentArray = listAppointment(getSecondWebConversation(), appointmentFolderId, modified, true, false, PROTOCOL + hostName, getSecondLogin(), getPassword());
		
		boolean found = true;
		
		for (int a = 0; a < appointmentArray.length; a++) {
			if (objectId == appointmentArray[a].getObjectID()) {
				found = true;
				break;
			}
		}
		
		assertTrue("object not found in response", found);
		
		deleteAppointment(getWebConversation(), objectId, parentFolderId, getHostName(), getLogin(), getPassword());
		FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostName(), getLogin(), getPassword());
	}
	
	public void testPropFindWithDelete() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testPropFindWithDelete");
		appointmentObj.setIgnoreConflicts(true);
		int objectId1 = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		int objectId2 = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		final AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId1, appointmentFolderId, getHostName(), getLogin(), getPassword());
		final Date modified = loadAppointment.getCreationDate();
		
		int[][] objectIdAndFolderId = { { objectId1, appointmentFolderId }, { objectId2, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password);
		
		AppointmentObject[] appointmentArray = listAppointment(webCon, appointmentFolderId, modified, false, true, PROTOCOL + hostName, login, password);
		
		boolean found = true;
		
		for (int a = 0; a < appointmentArray.length; a++) {
			if (objectId1 == appointmentArray[a].getObjectID()) {
				found = true;
				break;
			}
		}
		
		assertTrue("object not found in response", found);
	}
	
	public void testPropFindWithObjectId() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testPropFindWithObjectId");
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		AppointmentObject loadAppointment = loadAppointment(webCon, objectId, appointmentFolderId, PROTOCOL + hostName, login, password);
		
		int[][] objectIdAndFolderId = { { objectId ,appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
	
	public void testObjectNotFound() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testObjectNotFound");
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		try {
			AppointmentObject loadAppointment = loadAppointment(webCon, (objectId+1000), appointmentFolderId, PROTOCOL + hostName, login, password);
			fail("object not found exception expected!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.OBJECT_NOT_FOUND_STATUS);
		}
		
		int[][] objectIdAndFolderId = { { objectId ,appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
	
	public void testListWithAllFields() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testListWithAllFields");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setLocation("Location");
		appointmentObj.setShownAs(AppointmentObject.FREE);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setPrivateFlag(true);
		appointmentObj.setLabel(2);
		appointmentObj.setNote("note");
		appointmentObj.setCategories("testcat1,testcat2,testcat3");
		appointmentObj.setIgnoreConflicts(true);
		
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		final AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getLogin(), getPassword());
		final Date modified = loadAppointment.getCreationDate();
	
		final AppointmentObject[] appointmentArray = listAppointment(webCon, appointmentFolderId, modified, true, false, PROTOCOL + hostName, login, password);
		
		assertTrue("wrong response array length", appointmentArray.length >= 1);
		
		boolean found = false;
		for (int a = 0; a < appointmentArray.length; a++) {
			AppointmentObject checkAppointment = appointmentArray[a];
			
			if (checkAppointment.getObjectID() == objectId) {
				found = true;
				appointmentObj.setObjectID(objectId);
				compareObject(appointmentObj, checkAppointment);
			}
		}
		
		assertTrue("object not found in response", found);
		
		int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
	
	public void testList() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testObjectNotFound");
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		final int[] idArray = listAppointment(getWebConversation(), appointmentFolderId, getHostName(), getLogin(), getPassword());
		
		boolean found = false;
		for (int a = 0; a < idArray.length; a++) {
			if (idArray[a] == objectId) {
				found = true;
				break;
			}
		}
		
		assertTrue("id " + objectId + " not found in response", found);
		
		int[][] objectIdAndFolderId = { { objectId ,appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
}

