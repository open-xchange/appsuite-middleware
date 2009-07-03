package com.openexchange.webdav.xml.folder;

import java.util.Date;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.XmlServlet;

public class ListTest extends FolderTest {
	
	public ListTest(final String name) {
		super(name);
	}
	
	public void testDummy() {
		
	}
	
	public void testPropFindWithModified() throws Exception {
		FolderObject folderObj = createFolderObject(userId, "testPropFindWithModified1", FolderObject.CONTACT, false);
		final int objectId1 = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
		folderObj = createFolderObject(userId, "testPropFindWithModified2", FolderObject.TASK, false);
		final int objectId2 = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
		
		// prevent master/slave problem
		Thread.sleep(1000);
		
		final FolderObject loadFolder = loadFolder(webCon, objectId1, getHostName(), getLogin(), getPassword());
		final Date modified = loadFolder.getLastModified();
		
		final FolderObject[] folderArray = listFolder(webCon, decrementDate(modified), true, false, PROTOCOL + hostName, login, password);
		
		assertTrue("expected response size is < 2", folderArray.length >= 2);
	}
	
	public void testPropFindWithDeleted() throws Exception {
		FolderObject folderObj = createFolderObject(userId, "testPropFindWithDeleted1", FolderObject.CALENDAR, false);
		final int objectId1 = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
		folderObj = createFolderObject(userId, "testPropFindWithDeleted2", FolderObject.CONTACT, false);
		final int objectId2 = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
		
		// prevent master/slave problem
		Thread.sleep(1000);
		
		final FolderObject loadFolder = loadFolder(webCon, objectId1, getHostName(), getLogin(), getPassword());
		final Date modified = loadFolder.getLastModified();
		
		final int[] id = { objectId1, objectId2 };
		
		final int[] failed = deleteFolder(webCon, id, PROTOCOL + hostName, login, password);
		
		final FolderObject[] folderArray = listFolder(webCon, decrementDate(modified), false, true, PROTOCOL + hostName, login, password);
		
		assertTrue("expected response size is < 2", folderArray.length >= 2);
	}
	
	public void testPropFindWithObjectIdOnPrivateFolder() throws Exception {
		final FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testPropFindWithObjectIdOnPrivateFolder" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.TASK);
		folderObj.setType(FolderObject.PRIVATE);
		folderObj.setParentFolderID(1);
		
		final OCLPermission[] permission = new OCLPermission[] {
			createPermission( userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS)
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int objectId = insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		final FolderObject loadFolder = loadFolder(getWebConversation(), objectId, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		deleteFolder(getWebConversation(), new int[] { objectId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testPropFindWithObjectIdOnPublicFolder() throws Exception {
		final FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testPropFindWithObjectIdOnPublicFolder" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.TASK);
		folderObj.setType(FolderObject.PRIVATE);
		folderObj.setParentFolderID(1);
		
		final OCLPermission[] permission = new OCLPermission[] {
			createPermission( userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS)
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int objectId = insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		final FolderObject loadFolder = loadFolder(getWebConversation(), objectId, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		deleteFolder(getWebConversation(), new int[] { objectId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testObjectNotFound() throws Exception {
		final FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testObjectNotFound" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.TASK);
		folderObj.setType(FolderObject.PRIVATE);
		folderObj.setParentFolderID(1);
		
		final OCLPermission[] permission = new OCLPermission[] {
			createPermission( userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS)
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int objectId = insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		try {
			final FolderObject loadFolder = loadFolder(getWebConversation(), (objectId+1000), PROTOCOL + getHostName(), getLogin(), getPassword());
			fail("object not found exception expected!");
		} catch (final TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.OBJECT_NOT_FOUND_STATUS);
		}
		
		deleteFolder(getWebConversation(), new int[] { objectId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testList() throws Exception {
		final FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testList" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.TASK);
		folderObj.setType(FolderObject.PRIVATE);
		folderObj.setParentFolderID(1);
		
		final OCLPermission[] permission = new OCLPermission[] {
			createPermission( userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS)
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int objectId = insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		final int[] idArray = listFolder(getWebConversation(), getHostName(), getLogin(), getPassword());
		
		boolean found = false;
		for (int a = 0; a < idArray.length; a++) {
			if (idArray[a] == objectId) {
				found = true;
				break;
			}
		}
		
		assertTrue("id " + objectId + " not found in response", found);
		deleteFolder(getWebConversation(), new int[] { objectId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}	
}

