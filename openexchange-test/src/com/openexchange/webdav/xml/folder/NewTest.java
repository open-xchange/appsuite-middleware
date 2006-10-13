package com.openexchange.webdav.xml.folder;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.OCLPermission;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.GroupUserTest;

public class NewTest extends FolderTest {
	
	public void testInsertPrivateFolderCalendar() throws Exception {
		FolderObject folderObj = createFolderObject(userId, "testInsertPrivateFolderCalendar", FolderObject.CALENDAR, false);
		int objectId = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
		folderObj.setObjectID(objectId);
		
		FolderObject loadFolder = loadFolder(webCon, objectId, PROTOCOL + hostName, login, password);
		compareFolder(folderObj, loadFolder);
	}
	
	public void testInsertPrivateFolderContact() throws Exception {
		FolderObject folderObj = createFolderObject(userId, "testInsertPrivateFolderContact", FolderObject.CONTACT, false);
		int objectId = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
		folderObj.setObjectID(objectId);
		
		FolderObject loadFolder = loadFolder(webCon, objectId, PROTOCOL + hostName, login, password);
		compareFolder(folderObj, loadFolder);
	}
	
	public void testInsertPrivateFolderTask() throws Exception {
		FolderObject folderObj = createFolderObject(userId, "testInsertPrivateFolderTask", FolderObject.TASK, false);
		int objectId = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
		folderObj.setObjectID(objectId);
		
		FolderObject loadFolder = loadFolder(webCon, objectId, PROTOCOL + hostName, login, password);
		compareFolder(folderObj, loadFolder);
	}
	
	public void testInsertPublicFolderCalendar() throws Exception {
		FolderObject folderObj = createFolderObject(userId, "testInsertPublicFolderCalendar", FolderObject.CALENDAR, true);
		int objectId = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
		folderObj.setObjectID(objectId);
		
		FolderObject loadFolder = loadFolder(webCon, objectId, PROTOCOL + hostName, login, password);
		compareFolder(folderObj, loadFolder);
	}
	
	public void testInsertPublicFolderContact() throws Exception {
		FolderObject folderObj = createFolderObject(userId, "testInsertPublicFolderContact", FolderObject.CONTACT, true);
		int objectId = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
		folderObj.setObjectID(objectId);
		
		FolderObject loadFolder = loadFolder(webCon, objectId, PROTOCOL + hostName, login, password);
		compareFolder(folderObj, loadFolder);
	}
	
	public void testInsertPublicFolderTask() throws Exception {
		FolderObject folderObj = createFolderObject(userId, "testInsertPublicFolderTask", FolderObject.TASK, true);
		int objectId = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
		folderObj.setObjectID(objectId);
		
		FolderObject loadFolder = loadFolder(webCon, objectId, PROTOCOL + hostName, login, password);
		compareFolder(folderObj, loadFolder);
	}
	
	public void _notestInsertPrivateFolderWithoutPermission() throws Exception {
		final int secondUserId = GroupUserTest.getUserId(getSecondWebConversation(), PROTOCOL + getHostName(), getSecondLogin(), getPassword());
		
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testInsertPrivateFolderWithoutPermission" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.TASK);
		folderObj.setType(FolderObject.PRIVATE);
		folderObj.setParentFolderID(1);
		
		OCLPermission[] permission = new OCLPermission[] { 
			createPermission( userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS)
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int objectId = insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		folderObj = createFolderObject(userId, "testInsertPrivateFolderWithoutPermission", FolderObject.TASK, false);
		folderObj.setParentFolderID(objectId);
		try {
			int subFolderId1 = insertFolder(getSecondWebConversation(), folderObj, PROTOCOL + getHostName(), getSecondLogin(), getPassword());
			fail("conflict permission expected!");
		} catch (OXException exc) {
			assertExceptionMessage(exc.getMessage(), 1002);
		}

		deleteFolder(getWebConversation(), new int[] { objectId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void _notestInsertPublicFolderWithoutPermission() throws Exception {
		final int secondUserId = GroupUserTest.getUserId(getSecondWebConversation(), PROTOCOL + getHostName(), getSecondLogin(), getPassword());
		
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testInsertPublicFolderWithoutPermission" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.TASK);
		folderObj.setType(FolderObject.PUBLIC);
		folderObj.setParentFolderID(2);
		
		OCLPermission[] permission = new OCLPermission[] { 
			createPermission( userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS)
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int objectId = insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		folderObj = createFolderObject(userId, "testInsertPrivateFolderWithoutPermission", FolderObject.TASK, true);
		folderObj.setParentFolderID(objectId);
		try {
			int subFolderId1 = insertFolder(getSecondWebConversation(), folderObj, PROTOCOL + getHostName(), getSecondLogin(), getPassword());
			fail("conflict permission expected!");
		} catch (OXException exc) {
			assertExceptionMessage(exc.getMessage(), 1002);
		}

		deleteFolder(getWebConversation(), new int[] { objectId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
}
