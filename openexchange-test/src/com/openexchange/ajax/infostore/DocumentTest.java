
package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.infostore.actions.GetDocumentResponse;
import com.openexchange.test.OXTestToolkit;
import com.openexchange.test.TestInit;

public class DocumentTest extends InfostoreAJAXTest {

    protected File upload;
    protected String id;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        com.openexchange.file.storage.File data = createFile(folderId, "test upload" + UUID.randomUUID().toString());
        data.setFileMIMEType("text/plain");
        itm.newAction(data, upload);
        id = data.getId();
    }

    @Test
    public void testCurrentVersion() throws Exception {
        InputStream is = null;
        InputStream is2 = null;
        try {
            is = new FileInputStream(upload);
            is2 = itm.document(Integer.toString(folderId), id, "-1");

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

    @Test
    public void testContentType() throws Exception {
        itm.document(Integer.toString(folderId), id, "-1", "application/octet-stream");
        String contentType = ((GetDocumentResponse) itm.getLastResponse()).getContentType();
        assertEquals("application/octet-stream;charset=UTF-8", contentType);

        itm.document(Integer.toString(folderId), id, "-1", null);

        contentType = ((GetDocumentResponse) itm.getLastResponse()).getContentType();
        assertTrue(contentType.startsWith("text/plain"));
    }
}
