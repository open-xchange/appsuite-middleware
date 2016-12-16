
package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.json.JSONException;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.infostore.actions.UpdateInfostoreResponse;
import com.openexchange.exception.OXException;
import com.openexchange.test.OXTestToolkit;
import com.openexchange.test.TestInit;

public class UpdateTest extends InfostoreAJAXTest {

    public static final int SIZE = 15; // Size of the large file in Megabytes

    private static final byte[] megabyte = new byte[1000000];

    String LOREM_IPSUM = "[32] Sed ut perspiciatis, unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam eaque ipsa, quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt, explicabo. Nemo enim ipsam voluptatem, quia voluptas sit, aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos, qui ratione voluptatem sequi nesciunt, neque porro quisquam est, qui dolorem ipsum, quia dolor sit, amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt, ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit, qui in ea voluptate velit esse, quam nihil molestiae consequatur, vel illum, qui dolorem eum fugiat, quo voluptas nulla pariatur? [33] At vero eos et accusamus et iusto odio dignissimos ducimus, qui blanditiis praesentium voluptatum deleniti atque corrupti, quos dolores et quas molestias excepturi sint, obcaecati cupiditate non provident, similique sunt in culpa, qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio, cumque nihil impedit, quo minus id, quod maxime placeat, facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet, ut et voluptates repudiandae sint et molestiae non recusandae. Itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat.";

    public UpdateTest() {
        super();
    }

    @Test
    public void testBasic() throws Exception {
        final String id = clean.get(0);
        com.openexchange.file.storage.File file = itm.getAction(id);
        file.setTitle("test knowledge updated");
        file.setColorLabel(1);
        UpdateInfostoreResponse result = update(file, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.TITLE, com.openexchange.file.storage.File.Field.COLOR_LABEL }, Long.MAX_VALUE);
        assertFalse(result.hasError());
        assertNotNull(itm.getLastResponse().getTimestamp());

        com.openexchange.file.storage.File object = itm.getAction(clean.get(0));

        assertEquals("test knowledge updated", object.getTitle());
        assertEquals("test knowledge description", object.getDescription());
        assertEquals(1, object.getColorLabel());
        assertEquals(0, object.getVersion());

    }

    @Test
    public void testLongDescription() throws Exception {
        descriptionRoundtrip(LOREM_IPSUM);
    }

    @Test
    public void testCharset() throws Exception {
        descriptionRoundtrip("H\u00f6l\u00f6\u00f6\u00f6\u00f6\u00f6\u00f6\u00f6");
    }

    private void descriptionRoundtrip(final String desc) throws Exception {
        com.openexchange.file.storage.File file = itm.getAction(clean.get(0));
        file.setDescription(desc);
        UpdateInfostoreResponse result = update(file, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.DESCRIPTION }, Long.MAX_VALUE);
        assertFalse(result.hasError());
        assertNotNull(itm.getLastResponse().getTimestamp());

        com.openexchange.file.storage.File obj = itm.getAction(clean.get(0));
        assertEquals(desc, obj.getDescription());
    }

    @Test
    public void testConflict() throws Exception {
        com.openexchange.file.storage.File file = itm.getAction(clean.get(0));
        file.setTitle("test knowledge updated");
        UpdateInfostoreResponse result = update(file, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.TITLE }, file.getLastModified().getTime() - 2000);
        assertTrue(result.hasConflicts());
        assertTrue(result.hasError());

    }

    @Test
    public void testUpload() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        final String id = clean.get(0);

        Response res = update(getWebConversation(), getHostName(), sessionId, id, Long.MAX_VALUE, m(), upload, "text/plain");
        assertNoError(res);

        com.openexchange.file.storage.File obj = itm.getAction(id);

        assertEquals(1, obj.getVersion());
        assertEquals("text/plain", obj.getFileMIMEType());
        assertEquals(upload.getName(), obj.getFileName());

        InputStream is = null;
        InputStream is2 = null;
        try {
            is = new FileInputStream(upload);
            is2 = document(getWebConversation(), getHostName(), sessionId, id, 1);
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
    public void testUploadEmptyFile() throws IOException, JSONException, SAXException, OXException {
        final File emptyFile = File.createTempFile("infostore-new-test", ".txt");

        final String id = clean.get(0);

        Response res = update(getWebConversation(), getHostName(), sessionId, id, Long.MAX_VALUE, m(), emptyFile, "text/plain");
        assertNoError(res);

        com.openexchange.file.storage.File obj = itm.getAction(id);
        assertEquals(1, obj.getVersion());
        assertEquals("text/plain", obj.getFileMIMEType());
        assertEquals(emptyFile.getName(), obj.getFileMD5Sum());
        assertTrue(emptyFile.delete());
    }

    //Bug 4120
    @Test
    public void testUniqueFilenamesOnUpload() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        final String id = clean.get(0);

        Response res = update(getWebConversation(), getHostName(), sessionId, id, Long.MAX_VALUE, m(), upload, "text/plain");
        assertNoError(res);

        final String id2 = createFileOnServer(folderId, "otherFile", "text/javascript").getId();
        clean.add(id2);

        res = update(getWebConversation(), getHostName(), sessionId, id2, Long.MAX_VALUE, m(), upload, "text/plain");

        com.openexchange.file.storage.File obj = itm.getAction(clean.get(0));
        assertFalse(upload.getName().equals(obj.getFileName()));
    }

    //Bug 4120
    @Test
    public void testUniqueFilenamesOnSwitchVersions() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        final String id = clean.get(0);

        Response res = update(getWebConversation(), getHostName(), sessionId, id, Long.MAX_VALUE, m("filename", "theFile.txt"), upload, "text/plain");
        assertNoError(res);

        res = update(getWebConversation(), getHostName(), sessionId, id, Long.MAX_VALUE, m(), upload, "text/plain");
        assertNoError(res);

        com.openexchange.file.storage.File data = createFile(folderId, "otherFile");
        data.setFileMIMEType("text/plain");
        data.setDescription("other_desc");
        data.setFileName("theFile.txt");
        itm.newAction(data, upload);

        clean.add(data.getId());

        com.openexchange.file.storage.File file = itm.getAction(id);
        file.setTitle("otherTitle");
        file.setVersion("1");
        UpdateInfostoreResponse result = update(file, new com.openexchange.file.storage.File.Field[] {com.openexchange.file.storage.File.Field.VERSION, com.openexchange.file.storage.File.Field.TITLE}, Long.MAX_VALUE);
        assertFalse(result.hasConflicts());

        com.openexchange.file.storage.File reloaded = itm.getAction(id);

        assertEquals("theFile (1).txt", reloaded.getFileName());
    }

    // FIXME MS re-add
    //    @Test
    //    public void testSwitchVersion() throws Exception {
    //        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
    //
    //        final String id = clean.get(0);
    //
    //        Response res = update(getWebConversation(), getHostName(), sessionId, id, Long.MAX_VALUE, m(), upload, "text/plain"); // V1
    //        assertNoError(res);
    //
    //        res = update(getWebConversation(), getHostName(), sessionId, id, Long.MAX_VALUE, m(), upload, "text/plain");// V2
    //        assertNoError(res);
    //
    //        res = update(getWebConversation(), getHostName(), sessionId, id, Long.MAX_VALUE, m(), upload, "text/plain");// V3
    //        assertNoError(res);
    //
    //        res = update(getWebConversation(), getHostName(), sessionId, id, Long.MAX_VALUE, m("version", "2"));
    //        assertNoError(res);
    //
    //        com.openexchange.file.storage.File obj = itm.getAction(id);
    //        assertEquals("Version does not match.", "2", obj.getVersion());
    //
    //        res = versions(getWebConversation(), getHostName(), sessionId, id, new int[] { Metadata.VERSION, Metadata.CURRENT_VERSION });
    //        assertNoError(res);
    //
    //        VersionsTest.assureVersions(new Integer[] { 1, 2, 3 }, res, 2);
    //    }

    @Test
    public void testUpdateCurrentVersionByDefault() throws Exception {
        final String id = clean.get(0);

        com.openexchange.file.storage.File file = itm.getAction(id);
        UpdateInfostoreResponse result = update(file, new com.openexchange.file.storage.File.Field[] {}, Long.MAX_VALUE);
        assertFalse(result.hasConflicts());
        assertFalse(result.hasError());

        result = update(file, new com.openexchange.file.storage.File.Field[] {}, Long.MAX_VALUE);
        assertFalse(result.hasConflicts());
        assertFalse(result.hasError());

        file.setDescription("New Description");
        result = update(file, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.DESCRIPTION }, Long.MAX_VALUE);

        com.openexchange.file.storage.File obj = itm.getAction(id);

        assertEquals("New Description", obj.getDescription());
    }

    // Bug 3928
    @Test
    public void testVersionCommentForNewVersion() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        final String id = clean.get(0);

        Response res = update(getWebConversation(), getHostName(), sessionId, id, Long.MAX_VALUE, m("version_comment", "Version Comment"), upload, "text/plain"); // V1
        assertNoError(res);

        com.openexchange.file.storage.File obj = itm.getAction(id);
        assertEquals("Version Comment", obj.getVersionComment());
    }

    //Bug 4269
    @Test
    public void testVirtualFolder() throws Exception {
        for (int folderId : virtualFolders) {
            virtualFolderTest(folderId);
        }
    }

    // Bug 4269
    public void virtualFolderTest(int folderId) throws Exception {
        com.openexchange.file.storage.File file = itm.getAction(clean.get(0));
        file.setFolderId(String.valueOf(folderId));
        UpdateInfostoreResponse result = update(file, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.FOLDER_ID }, Long.MAX_VALUE);
        assertTrue(result.hasError());
        assertTrue(result.getErrorMessage(), result.getErrorMessage().contains("IFO-1700"));
    }

}
