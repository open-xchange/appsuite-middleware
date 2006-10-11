package com.openexchange.webdav.xml.attachment;

import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.webdav.xml.AttachmentTest;
import com.openexchange.webdav.xml.ContactTest;
import com.openexchange.webdav.xml.FolderTest;

public class ListTest extends AttachmentTest {

	public void testLoadAttachment() throws Exception {
		FolderObject folderObj = FolderTest.getContactDefaultFolder(webCon, PROTOCOL + hostName, login, password);
		int contactFolderId = folderObj.getObjectID();
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testLoadAttachment");
		contactObj.setParentFolderID(contactFolderId);
		int objectId = ContactTest.insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		int attachmentId = insertAttachment(System.currentTimeMillis() + "test.txt", Types.CONTACT, objectId, contactFolderId, false);
		loadAttachment(attachmentId, Types.CONTACT, objectId, contactFolderId, false);
	}
	
	public void testLoadAttachmentWithRtf() throws Exception {
		FolderObject folderObj = FolderTest.getContactDefaultFolder(webCon, PROTOCOL + hostName, login, password);
		int contactFolderId = folderObj.getObjectID();
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testLoadAttachmentWithRtf");
		contactObj.setParentFolderID(contactFolderId);
		int objectId = ContactTest.insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		int attachmentId = insertAttachment(System.currentTimeMillis() + "test.txt", Types.CONTACT, objectId, contactFolderId, true);
		loadAttachment(attachmentId, Types.CONTACT, objectId, contactFolderId, true);
	}
	
}
