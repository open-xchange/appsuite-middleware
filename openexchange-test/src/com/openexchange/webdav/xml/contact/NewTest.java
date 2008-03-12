package com.openexchange.webdav.xml.contact;

import java.io.ByteArrayInputStream;

import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.AttachmentTest;
import com.openexchange.webdav.xml.ContactTest;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.XmlServlet;

public class NewTest extends ContactTest {
	
	public NewTest(String name) {
		super(name);
	}
	
	public void testNewContact() throws Exception {
		ContactObject contactObj = createContactObject("testNewContact");
		int objectId = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		contactObj.setObjectID(objectId);
		ContactObject loadContact = ContactTest.loadContact(getWebConversation(), objectId, contactFolderId, getHostName(), getLogin(), getPassword());
		compareObject(contactObj, loadContact);		
	}
	
	public void _noestNewContactWithAttachment() throws Exception {
		ContactObject contactObj = createContactObject("testNewContactWithAttachment");
		int objectId = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		contactObj.setNumberOfAttachments(2);
		contactObj.setObjectID(objectId);
		
		AttachmentMetadata attachmentObj = new AttachmentImpl();
		attachmentObj.setFilename(System.currentTimeMillis() + "test1.txt");
		attachmentObj.setModuleId(Types.CONTACT);
		attachmentObj.setAttachedId(objectId);
		attachmentObj.setFolderId(contactFolderId);
		attachmentObj.setRtfFlag(false);
		attachmentObj.setFileMIMEType("plain/text");
		
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("t1".getBytes());
		
		int attachmentId1 = AttachmentTest.insertAttachment(webCon, attachmentObj, byteArrayInputStream, getHostName(), getLogin(), getPassword());
		
		byteArrayInputStream = new ByteArrayInputStream("t2".getBytes());
		int attachmentId2 = AttachmentTest.insertAttachment(webCon, attachmentObj, byteArrayInputStream, getHostName(), getLogin(), getPassword());
		
		ContactObject loadContact = ContactTest.loadContact(getWebConversation(), objectId, contactFolderId, getHostName(), getLogin(), getPassword());
		compareObject(contactObj, loadContact);	
	}
	
	public void _notestContactInPrivateFlagInPublicFolder() throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testContactInPrivateFlagInPublicFolder" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.CONTACT);
		folderObj.setType(FolderObject.PUBLIC);
		folderObj.setParentFolderID(2);
		
		OCLPermission[] permission = new OCLPermission[] { 
			FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION)
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testContactInPrivateFlagInPublicFolder");
		contactObj.setPrivateFlag(true);
		contactObj.setParentFolderID(parentFolderId);

		try {
			int objectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getLogin(), getPassword());
			deleteContact(getWebConversation(), objectId, parentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
			fail("conflict exception expected!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.CONFLICT_STATUS);
		}
	}	
	
	public void testContactWithAttachment() throws Exception {
		ContactObject contactObj = createContactObject("testContactWithAttachment");
		final int objectId = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		contactObj.setObjectID(objectId);
		contactObj.setNumberOfAttachments(1);
		
		final AttachmentMetadata attachmentMeta = new AttachmentImpl();
		attachmentMeta.setAttachedId(objectId);
		attachmentMeta.setFolderId(contactFolderId);
		attachmentMeta.setFileMIMEType("text/plain");
		attachmentMeta.setModuleId(Types.CONTACT);
		attachmentMeta.setFilename("test.txt");
		
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("test".getBytes());
		AttachmentTest.insertAttachment(webCon, attachmentMeta, byteArrayInputStream, getHostName(), getLogin(), getPassword());
		
		final ContactObject loadContact = loadContact(getWebConversation(), objectId, contactFolderId, getHostName(), getLogin(), getPassword());
		final ContactObject[] contactArray = listContact(getWebConversation(), contactFolderId, loadContact.getLastModified(), true, false, getHostName(), getLogin(), getPassword());
		
		boolean found = false;
		for (int a = 0; a < contactArray.length; a++) {
			if (contactArray[a].getObjectID() == objectId) {
				compareObject(contactObj, contactArray[a]);
				found = true;
			}
		}
		
		assertTrue("task not found" , found);
	}
}

