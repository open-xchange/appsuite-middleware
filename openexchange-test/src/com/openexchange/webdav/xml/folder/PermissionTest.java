package com.openexchange.webdav.xml.folder;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.GroupUserTest;

public class PermissionTest extends FolderTest {

	public PermissionTest(final String name) {
		super(name);
	}

	public void testDummy() {

	}

	public void testInsertPrivateFolderWithoutPermission() throws Exception {
		GroupUserTest.getUserId(getSecondWebConversation(), PROTOCOL + getHostName(), getSecondLogin(), getPassword(), context);

		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testInsertPrivateFolderWithoutPermission" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.TASK);
		folderObj.setType(FolderObject.PRIVATE);
		folderObj.setParentFolderID(1);

		final OCLPermission[] permission = new OCLPermission[] {
			createPermission( userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS)
		};

		folderObj.setPermissionsAsArray( permission );

		final int objectId = insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
		folderObj = createFolderObject(userId, "testInsertPrivateFolderWithoutPermission", FolderObject.TASK, false);
		folderObj.setParentFolderID(objectId);
		try {
			final int subFolderId1 = insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
			deleteFolder(getWebConversation(), new int[] { subFolderId1 }, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
			fail("conflict permission expected!");
		} catch (final OXException exc) {
			assertExceptionMessage(exc.getMessage(), 1002);
		}

		deleteFolder(getWebConversation(), new int[] { objectId }, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
	}

	public void testInsertPublicFolderWithoutPermission() throws Exception {
		GroupUserTest.getUserId(getSecondWebConversation(), PROTOCOL + getHostName(), getSecondLogin(), getPassword(), context);

		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testInsertPublicFolderWithoutPermission" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.TASK);
		folderObj.setType(FolderObject.PUBLIC);
		folderObj.setParentFolderID(2);

		final OCLPermission[] permission = new OCLPermission[] {
			createPermission( userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS)
		};

		folderObj.setPermissionsAsArray( permission );

		final int objectId = insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
		folderObj = createFolderObject(userId, "testInsertPublicFolderWithoutPermission", FolderObject.TASK, true);
		folderObj.setParentFolderID(objectId);
		try {
			final int subFolderId1 = insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
			deleteFolder(getWebConversation(), new int[] { subFolderId1 }, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
			fail("conflict permission expected!");
		} catch (final OXException exc) {
			assertExceptionMessage(exc.getMessage(), 1002);
		}

		deleteFolder(getWebConversation(), new int[] { objectId }, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
	}
}

