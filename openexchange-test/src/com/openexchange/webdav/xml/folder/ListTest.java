package com.openexchange.webdav.xml.folder;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.OCLPermission;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.XmlServlet;
import java.util.Date;

public class ListTest extends FolderTest {
	
	public void testDummy() {
		
	}
	
	public void _notestPropFindWithModified() throws Exception {
		Date modified = new Date();
		
		FolderObject folderObj = createFolderObject(userId, "testPropFindWithModified1", FolderObject.CONTACT, false);
		insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
		folderObj = createFolderObject(userId, "testPropFindWithModified2", FolderObject.TASK, false);
		insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
		
		FolderObject[] folderArray = listFolder(webCon, modified, true, false, PROTOCOL + hostName, login, password);
		
		assertTrue("expected response size is < 2", folderArray.length >= 2);
	}
	
	public void _notestPropFindWithDeleted() throws Exception {
		Date modified = new Date();
		
		FolderObject folderObj = createFolderObject(userId, "testPropFindWithDeleted1", FolderObject.CALENDAR, false);
		int objectId1 = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
		folderObj = createFolderObject(userId, "testPropFindWithDeleted2", FolderObject.CONTACT, false);
		int objectId2 = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
		
		int[] id = { objectId1, objectId2 };
		
		int[] failed = deleteFolder(webCon, id, PROTOCOL + hostName, login, password);
		
		FolderObject[] folderArray = listFolder(webCon, modified, false, true, PROTOCOL + hostName, login, password);
		
		assertTrue("expected response size is < 2", folderArray.length >= 2);
	}
	
	public void _notestPropFindWithObjectIdOnPrivateFolder() throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testPropFindWithObjectIdOnPrivateFolderWithoutPermission" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.TASK);
		folderObj.setType(FolderObject.PRIVATE);
		folderObj.setParentFolderID(1);
		
		OCLPermission[] permission = new OCLPermission[] {
			createPermission( userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS)
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		int objectId = insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		FolderObject loadFolder = loadFolder(getWebConversation(), objectId, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		deleteFolder(getWebConversation(), new int[] { objectId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void _notestPropFindWithObjectIdOnPublicFolder() throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testPropFindWithObjectIdOnPublicFolder" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.TASK);
		folderObj.setType(FolderObject.PRIVATE);
		folderObj.setParentFolderID(1);
		
		OCLPermission[] permission = new OCLPermission[] {
			createPermission( userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS)
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		int objectId = insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		FolderObject loadFolder = loadFolder(getWebConversation(), objectId, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		deleteFolder(getWebConversation(), new int[] { objectId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void _notestObjectNotFound() throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testObjectNotFound" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.TASK);
		folderObj.setType(FolderObject.PRIVATE);
		folderObj.setParentFolderID(1);
		
		OCLPermission[] permission = new OCLPermission[] {
			createPermission( userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS)
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		int objectId = insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		try {
			FolderObject loadFolder = loadFolder(getWebConversation(), (objectId+1000), PROTOCOL + getHostName(), getLogin(), getPassword());
			fail("object not found exception expected!");
		} catch (OXException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.OBJECT_NOT_FOUND_STATUS);
		}
		
		deleteFolder(getWebConversation(), new int[] { objectId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
}

