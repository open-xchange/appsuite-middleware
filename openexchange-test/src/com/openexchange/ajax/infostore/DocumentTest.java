
package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.junit.Before;
import org.junit.Test;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.test.OXTestToolkit;
import com.openexchange.test.TestInit;

public class DocumentTest extends InfostoreAJAXTest {

    protected File upload;
    protected String id;

    public DocumentTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        com.openexchange.file.storage.File data = createFile(folderId, "test upload");
        itm.newAction(data, upload);
        clean.add(data.getId());
    }

    @Test
    public void testCurrentVersion() throws Exception {
        InputStream is = null;
        InputStream is2 = null;
        try {
            is = new FileInputStream(upload);
            is2 = document(getWebConversation(), getHostName(), sessionId, id, -1);

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
        GetMethodWebRequest req = documentRequest(sessionId, getHostName(), id, -1, "application/octet-stream");
        WebResponse resp = getWebConversation().getResource(req);
        assertEquals("application/octet-stream", resp.getContentType());

        req = documentRequest(sessionId, getHostName(), id, -1, null);
        resp = getWebConversation().getResource(req);
        assertEquals("text/plain", resp.getContentType());

    }
}
