package com.openexchange.webdav.xml.folder;

import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.GroupUserTest;

public class PermissionTest extends FolderTest {
	
	public PermissionTest(String name) {
		super(name);
	}
	
	public void testDummy() {
		
	}
	
	public void testInsertPrivateFolderWithoutPermission() throws Exception {
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
			int subFolderId1 = insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
			deleteFolder(getWebConversation(), new int[] { subFolderId1 }, PROTOCOL + getHostName(), getLogin(), getPassword());
			fail("conflict permission expected!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), 1002);
		}

		deleteFolder(getWebConversation(), new int[] { objectId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testInsertPublicFolderWithoutPermission() throws Exception {
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
		folderObj = createFolderObject(userId, "testInsertPublicFolderWithoutPermission", FolderObject.TASK, true);
		folderObj.setParentFolderID(objectId);
		try {
			int subFolderId1 = insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
			deleteFolder(getWebConversation(), new int[] { subFolderId1 }, PROTOCOL + getHostName(), getLogin(), getPassword());
			fail("conflict permission expected!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), 1002);
		}

		deleteFolder(getWebConversation(), new int[] { objectId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
}

