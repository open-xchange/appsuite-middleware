
package com.openexchange.webdav.xml.attachment;

import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import org.junit.Test;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.webdav.xml.AttachmentTest;
import com.openexchange.webdav.xml.ContactTest;
import com.openexchange.webdav.xml.FolderTest;

public class DeleteTest extends AttachmentTest {

    @Test
    public void testDeleteAttachment() throws Exception {
        final FolderObject folderObj = FolderTest.getContactDefaultFolder(webCon, getHostURI(), getLogin(), getPassword());
        final int contactFolderId = folderObj.getObjectID();
        final Contact contactObj = new Contact();
        contactObj.setSurName("testDeleteAttachment");
        contactObj.setParentFolderID(contactFolderId);

        final int objectId = ContactTest.insertContact(webCon, contactObj, getHostURI(), getLogin(), getPassword());

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

        deleteAttachment(webCon, attachmentObj, getHostURI(), getLogin(), getPassword());
    }
}
