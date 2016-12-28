
package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.attach.SimpleAttachmentTest;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.test.OXTestToolkit;

public class SaveAsTest extends InfostoreAJAXTest {

    private final SimpleAttachmentTest attachmentTest = new SimpleAttachmentTest();

    public SaveAsTest() {
        super();
    }

    @Test
    public void testBasic() throws Exception {
        final AttachmentMetadata attachment = attachmentTest.getAttachment(0);
        
        final String id = itm.saveAs(attachment.getFolderId(), attachment.getAttachedId(), attachment.getModuleId(), attachment.getId(), m("folder_id", "" + folderId, "title", "My Attachment", "description", "An attachment cum InfoItem"));

        com.openexchange.file.storage.File obj = itm.getAction(id);

        final File upload = attachmentTest.getTestFile();

        assertEquals("My Attachment", obj.getTitle());
        assertEquals("An attachment cum InfoItem", obj.getDescription());
        assertEquals(1, obj.getVersion());
        assertEquals(upload.getName(), obj.getFileName());

        InputStream is = null;
        InputStream is2 = null;
        try {
            is = new FileInputStream(upload);
            is2 = itm.document(Integer.toString(folderId), id, "1");
            OXTestToolkit.assertSameContent(is, is2);
        } finally {
            if (is != null) {
                is.close();
            }
            if (is2 != null) {
                is2.close();
            }
        }
    }

    //Bug 4269
    @Test
    public void testVirtualFolder() throws Exception {
        for (int folderId : virtualFolders) {
            virtualFolder(folderId);
        }
    }

    //Bug 4269
    public void virtualFolder(int folderId) throws Exception {
        final AttachmentMetadata attachment = attachmentTest.getAttachment(0);
        try {
            final String id = itm.saveAs(attachment.getFolderId(), attachment.getAttachedId(), attachment.getModuleId(), attachment.getId(), m("folder_id", "" + folderId, "title", "My Attachment", "description", "An attachment cum InfoItem"));
            fail("Expected IOException when trying to save attachment in virtual infostore folder");
        } catch (final JSONException x) {
            assertTrue(x.getMessage(), x.getMessage().contains("IFO-1700"));
        }

    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        attachmentTest.setUp();
        attachmentTest.upload();
    }

    @After
    public void tearDown() throws Exception {
        try {
            attachmentTest.tearDown();
        } finally {
            super.tearDown();
        }
    }
}
