package com.openexchange.webdav.xml.folder;

import com.openexchange.groupware.container.FolderObject;
import com.openexchange.webdav.xml.FolderTest;
import java.util.Date;

public class ListTest extends FolderTest {
	
	public void testPropFindWithModified() throws Exception {
		Date modified = new Date();
	 
		FolderObject folderObj = createFolderObject(userId, "testPropFindWithModified1", FolderObject.CONTACT, false);
		insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
		folderObj = createFolderObject(userId, "testPropFindWithModified2", FolderObject.TASK, false);
		insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password);
	 
		FolderObject[] folderArray = listFolder(webCon, modified, true, false, PROTOCOL + hostName, login, password);
	 
		assertTrue("check response", folderArray.length == 2);
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
	 
		assertTrue("check response", folderArray.length == 2);
	}
}

