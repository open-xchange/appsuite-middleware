
package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import org.json.JSONException;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.exception.OXException;
import com.openexchange.test.TestInit;

public class VersionsTest extends InfostoreAJAXTest {

    public VersionsTest() {
        super();
    }

    @Test
    public void testVersions() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        Response res = update(getWebConversation(), getHostName(), sessionId, clean.get(0), Long.MAX_VALUE, m("version_comment", "Comment 1"), upload, "text/plain");
        assertNoError(res);
        res = update(getWebConversation(), getHostName(), sessionId, clean.get(0), Long.MAX_VALUE, m("version_comment", "Comment 2"), upload, "text/plain");
        assertNoError(res);
        res = update(getWebConversation(), getHostName(), sessionId, clean.get(0), Long.MAX_VALUE, m("version_comment", "Comment 3"), upload, "text/plain");
        assertNoError(res);

        com.openexchange.file.storage.File file = itm.getAction(clean.get(0));
        assertEquals(3,  file.getNumberOfVersions());
    }

    //FIXME MS re-add
    // Bug 13627
//    @Test
//    public void testVersionSorting() throws Exception {
//        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
//        Response res = update(getWebConversation(), getHostName(), sessionId, clean.get(0), Long.MAX_VALUE, m("version_comment", "Comment 1"), upload, "text/plain");
//        assertNoError(res);
//        res = update(getWebConversation(), getHostName(), sessionId, clean.get(0), Long.MAX_VALUE, m("version_comment", "Comment 2"), upload, "text/plain");
//        assertNoError(res);
//        res = update(getWebConversation(), getHostName(), sessionId, clean.get(0), Long.MAX_VALUE, m("version_comment", "Comment 3"), upload, "text/plain");
//        assertNoError(res);
//
//        itm.getAction(clean.get(0))
//        res = versions(getWebConversation(), getHostName(), sessionId, clean.get(0), new int[] { Metadata.VERSION, Metadata.CURRENT_VERSION }, Metadata.VERSION, "desc");
//        assertNoError(res);
//
//        assureVersions(new Integer[] { 3, 2, 1 }, res, 3);
//    }

    //FIXME MS re-add
//    @Test
//    public void testUniqueVersions() throws Exception {
//        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
//        Response res = update(getWebConversation(), getHostName(), sessionId, clean.get(0), Long.MAX_VALUE, m("version_comment", "Comment 1"), upload, "text/plain");
//        assertNoError(res);
//        res = update(getWebConversation(), getHostName(), sessionId, clean.get(0), Long.MAX_VALUE, m("version_comment", "Comment 2"), upload, "text/plain");
//        assertNoError(res);
//        res = update(getWebConversation(), getHostName(), sessionId, clean.get(0), Long.MAX_VALUE, m("version_comment", "Comment 3"), upload, "text/plain");
//        assertNoError(res);
//
//        res = versions(getWebConversation(), getHostName(), sessionId, clean.get(0), new int[] { Metadata.VERSION, Metadata.CURRENT_VERSION });
//        assertNoError(res);
//
//        assureVersions(new Integer[] { 1, 2, 3 }, res, 3);
//
//        final int[] nd = detach(getWebConversation(), getHostName(), sessionId, res.getTimestamp().getTime(), clean.get(0), new int[] { 3 });
//        assertEquals(0, nd.length);
//
//        res = update(getWebConversation(), getHostName(), sessionId, clean.get(0), Long.MAX_VALUE, m("version_comment", "Comment 3"), upload, "text/plain");
//        assertNoError(res);
//
//        res = versions(getWebConversation(), getHostName(), sessionId, clean.get(0), new int[] { Metadata.VERSION, Metadata.CURRENT_VERSION });
//        assertNoError(res);
//
//        assureVersions(new Integer[] { 1, 2, 4 }, res, 4);
//
//    }

    @Test
    public void testLastModifiedUTC() throws JSONException, IOException, SAXException, OXException {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        
        com.openexchange.file.storage.File toUpdate = itm.getAction(clean.get(0));
        toUpdate.setVersionComment("Comment 1");
        itm.updateAction(toUpdate, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.VERSION_COMMENT }, new Date(Long.MAX_VALUE));

        toUpdate.setVersionComment("Comment 2");
        itm.updateAction(toUpdate, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.VERSION_COMMENT }, new Date(Long.MAX_VALUE));

        toUpdate.setVersionComment("Comment 3");
        itm.updateAction(toUpdate, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.VERSION_COMMENT }, new Date(Long.MAX_VALUE));

        com.openexchange.file.storage.File fileWithVersions = itm.getAction(clean.get(0));
        final int size = fileWithVersions.getNumberOfVersions();
        assertTrue(size > 0);
    }
}
