package com.openexchange.webdav.xml.contact;

import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.ContactTest;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.XmlServlet;

public class PermissionTest extends ContactTest {
	
	public PermissionTest(String name) {
		super(name);
	}
	
	public void testDummy() {
		
	}
	
	public void testInsertContactInPrivateFolderWithoutPermission() throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testInsertContactInPrivateFolderWithoutPermission" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.CONTACT);
		folderObj.setType(FolderObject.PRIVATE);
		folderObj.setParentFolderID(1);
		
		OCLPermission[] permission = new OCLPermission[] { 
			FolderTest.createPermission( userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS),
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testInsertContactInPrivateFolderWithoutPermission");
		contactObj.setParentFolderID(parentFolderId);
		
		try {
			final int contactObjectId = insertContact(getSecondWebConversation(), contactObj, PROTOCOL + getHostName(), getSecondLogin(), getPassword());
			fail("permission exception expected!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
		}
		
		FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testInsertContactInPublicFolderWithoutPermission() throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testInsertContactInPublicFolderWithoutPermission" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.CONTACT);
		folderObj.setType(FolderObject.PUBLIC);
		folderObj.setParentFolderID(2);
		
		OCLPermission[] permission = new OCLPermission[] { 
			FolderTest.createPermission( userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS)
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testInsertContactInPublicFolderWithoutPermission");
		contactObj.setParentFolderID(parentFolderId);
		
		try {
			final int contactObjectId = insertContact(getSecondWebConversation(), contactObj, PROTOCOL + getHostName(), getSecondLogin(), getPassword());
			fail("permission exception expected!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
		}
		
		FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testUpdateContactInPrivateFolderWithoutPermission() throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testInsertContactInPrivateFolderWithoutPermission" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.CONTACT);
		folderObj.setType(FolderObject.PRIVATE);
		folderObj.setParentFolderID(1);
		
		OCLPermission[] permission = new OCLPermission[] { 
			FolderTest.createPermission( userId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS),
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testUpdateContactInPrivateFolderWithoutPermission");
		contactObj.setParentFolderID(parentFolderId);
		
		final int contactObjectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		contactObj.setObjectID(contactObjectId);
		
		try {
			updateContact(getSecondWebConversation(), contactObj, contactObjectId, parentFolderId, PROTOCOL + getHostName(), getSecondLogin(), getPassword());
			fail("permission exception expected!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
		}
		
		FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testUpdateContactInPublicFolderWithoutPermission() throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testUpdateContactInPublicFolderWithoutPermission" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.CONTACT);
		folderObj.setType(FolderObject.PUBLIC);
		folderObj.setParentFolderID(2);
		
		OCLPermission[] permission = new OCLPermission[] { 
			FolderTest.createPermission( userId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS)
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testUpdateContactInPublicFolderWithoutPermission");
		contactObj.setParentFolderID(parentFolderId);
		
		final int contactObjectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		contactObj.setObjectID(contactObjectId);
		
		try {
			updateContact(getSecondWebConversation(), contactObj, contactObjectId, parentFolderId, PROTOCOL + getHostName(), getSecondLogin(), getPassword());
			fail("permission exception expected!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
		}
		
		FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testDeleteContactInPrivateFolderWithoutPermission() throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testDeleteContactInPrivateFolderWithoutPermission" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.CONTACT);
		folderObj.setType(FolderObject.PRIVATE);
		folderObj.setParentFolderID(1);
		
		OCLPermission[] permission = new OCLPermission[] { 
			FolderTest.createPermission( userId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS),
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testDeleteContactInPrivateFolderWithoutPermission");
		contactObj.setParentFolderID(parentFolderId);
		
		final int contactObjectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		contactObj.setObjectID(contactObjectId);
		
		try {
			deleteContact(getSecondWebConversation(), contactObjectId, parentFolderId, PROTOCOL + getHostName(), getSecondLogin(), getPassword());
			fail("permission exception expected!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
		}
		
		FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testDeleteContactInPublicFolderWithoutPermission() throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testDeleteContactInPublicFolderWithoutPermission" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.CONTACT);
		folderObj.setType(FolderObject.PUBLIC);
		folderObj.setParentFolderID(2);
		
		OCLPermission[] permission = new OCLPermission[] { 
			FolderTest.createPermission( userId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS)
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testDeleteContactInPublicFolderWithoutPermission");
		contactObj.setParentFolderID(parentFolderId);
		
		final int contactObjectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		contactObj.setObjectID(contactObjectId);
		
		try {
			deleteContact(getSecondWebConversation(), contactObjectId, parentFolderId, PROTOCOL + getHostName(), getSecondLogin(), getPassword());
			fail("permission exception expected!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
		}
		
		FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
}

