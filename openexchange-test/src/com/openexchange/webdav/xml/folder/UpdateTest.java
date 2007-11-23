package com.openexchange.webdav.xml.folder;

import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.GroupUserTest;
import java.util.Date;

public class UpdateTest extends FolderTest {
	
	public UpdateTest(String name) {
		super(name);
	}
	
	public void testRenameFolder() throws Exception {
		FolderObject folderObj = createFolderObject(userId, "testInsertRenameFolder", FolderObject.TASK, true);
		int objectId = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
		
		folderObj = new FolderObject();
		folderObj.setFolderName("testRenameFolder" + System.currentTimeMillis());
		folderObj.setObjectID(objectId);
		folderObj.setParentFolderID(2);		
		
		updateFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
	 
		FolderObject loadFolder = loadFolder(webCon, objectId, PROTOCOL + hostName, login, password);
		compareFolder(folderObj, loadFolder);
	}
	 
	public void testMoveFolder() throws Exception {
		FolderObject folderObj = createFolderObject(userId, "testMoveFolder1", FolderObject.TASK, true);
		int parentFolderId = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
	 
		folderObj = createFolderObject(userId, "testMoveFolder2", FolderObject.TASK, true);
		int objectId = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
		folderObj.setObjectID(objectId);
		folderObj.setParentFolderID(parentFolderId);
		updateFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
	 
		FolderObject loadFolder = loadFolder(webCon, objectId, PROTOCOL + hostName, login, password);
		compareFolder(folderObj, loadFolder);
	}
	
	public void testChangePermissionsOfPrivateFolder() throws Exception {
		int userParticipantId = GroupUserTest.searchUser(webCon, userParticipant2, new Date(0), PROTOCOL + hostName, login, password)[0].getInternalUserId();
		int groupParticipantId = GroupUserTest.searchGroup(webCon, groupParticipant, new Date(0), PROTOCOL + hostName, login, password)[0].getIdentifier();
		
		FolderObject folderObj = createFolderObject(userId, "testChangePermissionOfPrivateFolder", FolderObject.TASK, false);
		int objectId = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
		folderObj.setObjectID(objectId);
		
		OCLPermission oclp[] = new OCLPermission[3];
		oclp[0] = createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, true);
		oclp[1] = createPermission( userParticipantId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_OWN_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, false);
		oclp[2] = createPermission( groupParticipantId, true, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, false);
		
		folderObj.setPermissionsAsArray( oclp );
		
		updateFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
		
		FolderObject loadFolder = loadFolder(webCon, objectId, PROTOCOL + hostName, login, password);
		compareFolder(folderObj, loadFolder);
	}
	
	public void testChangePermissionsOfPublicFolder() throws Exception {
		int userParticipantId = GroupUserTest.searchUser(webCon, userParticipant2, new Date(0), PROTOCOL + hostName, login, password)[0].getInternalUserId();
		int groupParticipantId = GroupUserTest.searchGroup(webCon, groupParticipant, new Date(0), PROTOCOL + hostName, login, password)[0].getIdentifier();
		
		FolderObject folderObj = createFolderObject(userId, "testChangePermissionOfPublicFolder", FolderObject.TASK, true);
		int objectId = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
		folderObj.setObjectID(objectId);
		
		OCLPermission oclp[] = new OCLPermission[3];
		oclp[0] = createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
		oclp[1] = createPermission( userParticipantId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_OWN_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS);
		oclp[2] = createPermission( groupParticipantId, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
		
		folderObj.setPermissionsAsArray( oclp );
		
		updateFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
		
		FolderObject loadFolder = loadFolder(webCon, objectId, PROTOCOL + hostName, login, password);
		compareFolder(folderObj, loadFolder);
	}
}

