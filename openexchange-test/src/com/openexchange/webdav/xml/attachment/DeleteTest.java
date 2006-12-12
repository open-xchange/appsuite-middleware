package com.openexchange.webdav.xml.attachment;

import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.webdav.xml.AttachmentTest;
import com.openexchange.webdav.xml.ContactTest;
import com.openexchange.webdav.xml.FolderTest;
import java.io.ByteArrayInputStream;

public class DeleteTest extends AttachmentTest {
	
	public void testDeleteAttachment() throws Exception {
		FolderObject folderObj = FolderTest.getContactDefaultFolder(webCon, PROTOCOL + hostName, login, password);
		int contactFolderId = folderObj.getObjectID();
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testDeleteAttachment");
		contactObj.setParentFolderID(contactFolderId);
		
		int objectId = ContactTest.insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		
		AttachmentMetadata attachmentObj = new AttachmentImpl();
		attachmentObj.setFilename(System.currentTimeMillis() + "test.txt");
		attachmentObj.setModuleId(Types.CONTACT);
		attachmentObj.setAttachedId(objectId);
		attachmentObj.setFolderId(contactFolderId);
		attachmentObj.setRtfFlag(false);
		attachmentObj.setFileMIMEType(CONTENT_TYPE);
		
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
		
		int attachmentId = insertAttachment(webCon, attachmentObj, byteArrayInputStream, getHostName(), getLogin(), getPassword());
		assertTrue("attachment is 0", attachmentId > 0);
		
		attachmentObj.setId(attachmentId);
		
		deleteAttachment(webCon, attachmentObj, getHostName(), getLogin(), getPassword());
	}	
}

