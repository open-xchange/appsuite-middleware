
package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.test.FolderTestManager;
import com.openexchange.test.common.test.OXTestToolkit;
import com.openexchange.test.common.test.TestInit;

public class SaveAsTest extends InfostoreAJAXTest {

    @Test
    public void testBasic() throws Exception {
        FolderObject newFolder = FolderTestManager.createNewFolderObject("test", Module.INFOSTORE.getFolderConstant(), FolderObject.PRIVATE, getClient().getValues().getUserId(), folderId);
        final AttachmentMetadata attachment = new AttachmentImpl();
        attachment.setFolderId(folderId);
        attachment.setAttachedId(newFolder.getObjectID());
        attachment.setModuleId(Module.INFOSTORE.getFolderConstant());
        File testFile = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        atm.attach(attachment, testFile.getName(), FileUtils.openInputStream(testFile), "text/plain");

        final String id = itm.saveAs(attachment.getFolderId(), attachment.getAttachedId(), attachment.getModuleId(), attachment.getId(), m("folder_id", "" + folderId, "title", "My Attachment", "description", "An attachment cum InfoItem"));

        itm.setFailOnError(true);
        com.openexchange.file.storage.File obj = itm.getAction(id);

        assertEquals("Unexpected title", "My Attachment", obj.getTitle());
        assertEquals("Unexpected description", "An attachment cum InfoItem", obj.getDescription());
        assertEquals("Unexpected version", "1", obj.getVersion());
        assertEquals("Unexpected filename", testFile.getName(), obj.getFileName());

        InputStream is = null;
        InputStream is2 = null;
        try {
            is = new FileInputStream(testFile);
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
        for (int fid : virtualFolders) {
            virtualFolder(fid);
        }
    }

    //Bug 4269
    public void virtualFolder(int fid) throws Exception {
        FolderObject newFolder = FolderTestManager.createNewFolderObject("test", Module.INFOSTORE.getFolderConstant(), FolderObject.PRIVATE, getClient().getValues().getUserId(), fid);
        final AttachmentMetadata attachment = new AttachmentImpl();
        attachment.setFolderId(fid);
        attachment.setAttachedId(newFolder.getObjectID());
        attachment.setModuleId(Module.INFOSTORE.getFolderConstant());
        File testFile = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        atm.attach(attachment, testFile.getName(), FileUtils.openInputStream(testFile), "text/plain");
        itm.saveAs(attachment.getFolderId(), attachment.getAttachedId(), attachment.getModuleId(), attachment.getId(), m("folder_id", "" + fid, "title", "My Attachment", "description", "An attachment cum InfoItem"));
        assertTrue(itm.getLastResponse().hasError());
    }

    private Map<String, String> m(final String... pairs) {
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Must contain matching pairs");
        }

        final Map<String, String> m = new HashMap<String, String>();

        for (int i = 0; i < pairs.length; i++) {
            m.put(pairs[i], pairs[++i]);
        }

        return m;

    }

}
