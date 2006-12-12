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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ListTest extends AttachmentTest {

	public void testLoadAttachment() throws Exception {
		FolderObject folderObj = FolderTest.getContactDefaultFolder(webCon, PROTOCOL + hostName, login, password);
		int contactFolderId = folderObj.getObjectID();
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testLoadAttachment");
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
		InputStream is = loadAttachment(webCon, attachmentObj, getHostName(), getLogin(), getPassword());
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte b[] = new byte[512];
		int len = 0;
		while ((len = is.read(b)) != -1) {
			byteArrayOutputStream.write(b, 0, len);
		}
		assertEqualsAndNotNull("byte[] are not equals", data, byteArrayOutputStream.toByteArray());
	}
	
	public void testLoadAttachmentWithRtf() throws Exception {
		FolderObject folderObj = FolderTest.getContactDefaultFolder(webCon, PROTOCOL + hostName, login, password);
		int contactFolderId = folderObj.getObjectID();
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testLoadAttachmentWithRtf");
		contactObj.setParentFolderID(contactFolderId);
		
		int objectId = ContactTest.insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		
		AttachmentMetadata attachmentObj = new AttachmentImpl();
		attachmentObj.setFilename(System.currentTimeMillis() + "test.txt");
		attachmentObj.setModuleId(Types.CONTACT);
		attachmentObj.setAttachedId(objectId);
		attachmentObj.setFolderId(contactFolderId);
		attachmentObj.setRtfFlag(true);
		attachmentObj.setFileMIMEType(CONTENT_TYPE);
		
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
		
		int attachmentId = insertAttachment(webCon, attachmentObj, byteArrayInputStream, getHostName(), getLogin(), getPassword());
		assertTrue("attachment is 0", attachmentId > 0);
		
		attachmentObj.setId(attachmentId);
		InputStream is = loadAttachment(webCon, attachmentObj, getHostName(), getLogin(), getPassword());
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte b[] = new byte[512];
		int len = 0;
		while ((len = is.read(b)) != -1) {
			byteArrayOutputStream.write(b, 0, len);
		}
		assertEqualsAndNotNull("byte[] are not equals", data, byteArrayOutputStream.toByteArray());
	}
	
}
