package com.openexchange.webdav.xml.folder;

import com.openexchange.groupware.container.FolderObject;
import com.openexchange.webdav.xml.FolderTest;

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
}
