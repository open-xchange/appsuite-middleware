package com.openexchange.webdav.xml.attachment;

import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.webdav.xml.AttachmentTest;
import com.openexchange.webdav.xml.ContactTest;
import com.openexchange.webdav.xml.FolderTest;

public class NewTest extends AttachmentTest {
	
	public void testInsertAttachment() throws Exception {
		FolderObject folderObj = FolderTest.getContactDefaultFolder(webCon, PROTOCOL + hostName, login, password);
		int contactFolderId = folderObj.getObjectID();
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testInsertAttachment");
		contactObj.setParentFolderID(contactFolderId);
		int objectId = ContactTest.insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		insertAttachment(System.currentTimeMillis() + "test.txt", Types.CONTACT, objectId, contactFolderId,  false);
	}
	
}

