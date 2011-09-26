package com.openexchange.webdav.xml.attachment;

import java.io.ByteArrayInputStream;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.webdav.xml.AttachmentTest;
import com.openexchange.webdav.xml.ContactTest;
import com.openexchange.webdav.xml.FolderTest;

public class NewTest extends AttachmentTest {

	public NewTest(final String name) {
		super(name);
	}

	public void testInsertAttachment() throws Exception {
		final FolderObject folderObj = FolderTest.getContactDefaultFolder(webCon, PROTOCOL + hostName, login, password, context);
		final int contactFolderId = folderObj.getObjectID();
		final Contact contactObj = new Contact();
		contactObj.setSurName("testInsertAttachment");
		contactObj.setParentFolderID(contactFolderId);
		final int objectId = ContactTest.insertContact(webCon, contactObj, PROTOCOL + hostName, login, password, context);

		final AttachmentMetadata attachmentObj = new AttachmentImpl();
		attachmentObj.setFilename(System.currentTimeMillis() + "test.txt");
		attachmentObj.setModuleId(Types.CONTACT);
		attachmentObj.setAttachedId(objectId);
		attachmentObj.setFolderId(contactFolderId);
		attachmentObj.setRtfFlag(false);
		attachmentObj.setFileMIMEType(CONTENT_TYPE);

		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);

		final int attachmentId = insertAttachment(webCon, attachmentObj, byteArrayInputStream, getHostName(), getLogin(), getPassword(), context);
		assertTrue("attachment is 0", attachmentId > 0);
	}
}

