
package com.openexchange.webdav.xml.attachment;

import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.junit.Test;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.webdav.xml.AttachmentTest;
import com.openexchange.webdav.xml.ContactTest;
import com.openexchange.webdav.xml.FolderTest;

public class ListTest extends AttachmentTest {

    public ListTest() {
        super();
    }

    @Test
    public void testLoadAttachment() throws Exception {
        final FolderObject folderObj = FolderTest.getContactDefaultFolder(webCon, getHostURI(), login, password);
        final int contactFolderId = folderObj.getObjectID();
        final Contact contactObj = new Contact();
        contactObj.setSurName("testLoadAttachment");
        contactObj.setParentFolderID(contactFolderId);

        final int objectId = ContactTest.insertContact(webCon, contactObj, getHostURI(), login, password);

        final AttachmentMetadata attachmentObj = new AttachmentImpl();
        attachmentObj.setFilename(System.currentTimeMillis() + "test.txt");
        attachmentObj.setModuleId(Types.CONTACT);
        attachmentObj.setAttachedId(objectId);
        attachmentObj.setFolderId(contactFolderId);
        attachmentObj.setRtfFlag(false);
        attachmentObj.setFileMIMEType(CONTENT_TYPE);

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);

        final int attachmentId = insertAttachment(webCon, attachmentObj, byteArrayInputStream, getHostURI(), getLogin(), getPassword());
        assertTrue("attachment is 0", attachmentId > 0);

        attachmentObj.setId(attachmentId);
        final InputStream is = loadAttachment(webCon, attachmentObj, getHostURI(), getLogin(), getPassword());
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final byte b[] = new byte[512];
        int len = 0;
        while ((len = is.read(b)) != -1) {
            byteArrayOutputStream.write(b, 0, len);
        }
        assertEqualsAndNotNull("byte[] are not equals", data, byteArrayOutputStream.toByteArray());
    }

    @Test
    public void testLoadAttachmentWithRtf() throws Exception {
        final FolderObject folderObj = FolderTest.getContactDefaultFolder(webCon, getHostURI(), login, password);
        final int contactFolderId = folderObj.getObjectID();
        final Contact contactObj = new Contact();
        contactObj.setSurName("testLoadAttachmentWithRtf");
        contactObj.setParentFolderID(contactFolderId);

        final int objectId = ContactTest.insertContact(webCon, contactObj, getHostURI(), login, password);

        final AttachmentMetadata attachmentObj = new AttachmentImpl();
        attachmentObj.setFilename(System.currentTimeMillis() + "test.txt");
        attachmentObj.setModuleId(Types.CONTACT);
        attachmentObj.setAttachedId(objectId);
        attachmentObj.setFolderId(contactFolderId);
        attachmentObj.setRtfFlag(true);
        attachmentObj.setFileMIMEType(CONTENT_TYPE);

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);

        final int attachmentId = insertAttachment(webCon, attachmentObj, byteArrayInputStream, getHostURI(), getLogin(), getPassword());
        assertTrue("attachment is 0", attachmentId > 0);

        attachmentObj.setId(attachmentId);
        final InputStream is = loadAttachment(webCon, attachmentObj, getHostURI(), getLogin(), getPassword());
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final byte b[] = new byte[512];
        int len = 0;
        while ((len = is.read(b)) != -1) {
            byteArrayOutputStream.write(b, 0, len);
        }
        assertEqualsAndNotNull("byte[] are not equals", data, byteArrayOutputStream.toByteArray());
    }

}
