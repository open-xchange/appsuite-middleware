
package com.openexchange.ajax.infostore;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.test.TestInit;

public class NewTest extends InfostoreAJAXTest {

    public static final int SIZE = 15; // Size of the large file in Megabytes

    private static final byte[] megabyte = new byte[1000000];

    public NewTest(final String name) {
        super(name);
    }

    public void testNothing() {
        assertTrue(true);
    }

    public void testUpload() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        String id = createNew(getWebConversation(), getHostName(), sessionId, m(
            "folder_id",
            ((Integer) folderId).toString(),
            "title",
            "test upload",
            "description",
            "test upload description"), upload, "text/plain");
        clean.add(id);

        Response res = get(getWebConversation(), getHostName(), sessionId, id);
        assertNotNull(res.getTimestamp());
        JSONObject obj = (JSONObject) res.getData();

        assertEquals("test upload", obj.getString("title"));
        assertEquals("test upload description", obj.getString("description"));
        assertEquals(1, obj.getInt("version"));
        assertEquals("text/plain", obj.getString("file_mimetype"));
        assertEquals(upload.getName(), obj.getString("filename"));

        InputStream is = null;
        InputStream is2 = null;
        try {
            is = new FileInputStream(upload);
            is2 = document(getWebConversation(), getHostName(), sessionId, id, 1);

        } finally {
            if (is != null) {
                is.close();
            }
            if (is2 != null) {
                is2.close();
            }
        }

        id = createNew(getWebConversation(), getHostName(), sessionId, m(
            "folder_id",
            ((Integer) folderId).toString(),
            "title",
            "test no upload",
            "description",
            "test no upload description"), null, "");
        clean.add(id);

        res = get(getWebConversation(), getHostName(), sessionId, id);
        obj = (JSONObject) res.getData();

        assertEquals("test no upload", obj.getString("title"));
        assertEquals("test no upload description", obj.getString("description"));

    }

    public void testUploadEmptyFile() throws IOException, JSONException, SAXException {
        File emptyFile = File.createTempFile("infostore-new-test", ".txt");
        String id = createNew(getWebConversation(), getHostName(), sessionId, m(
            "folder_id",
            ((Integer) folderId).toString(),
            "title",
            "test upload",
            "description",
            "test upload description"), emptyFile, "text/plain");
        clean.add(id);

        Response res = get(getWebConversation(), getHostName(), sessionId, id);
        assertNotNull(res.getTimestamp());
        JSONObject obj = (JSONObject) res.getData();

        assertEquals("test upload", obj.getString("title"));
        assertEquals("test upload description", obj.getString("description"));
        assertEquals(1, obj.getInt("version"));
        assertEquals("text/plain", obj.getString("file_mimetype"));
        assertEquals(emptyFile.getName(), obj.getString("filename"));
        assertTrue(emptyFile.delete());
    }

    public void testLargeFileUpload() throws Exception {
        final File largeFile = File.createTempFile("test", "bin");
        largeFile.deleteOnExit();

        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(largeFile), 1000000);
            for (int i = 0; i < SIZE; i++) {
                out.write(megabyte);
                out.flush();
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }

        try {
            final String id = createNew(getWebConversation(), getHostName(), sessionId, m(
                "folder_id",
                ((Integer) folderId).toString(),
                "title",
                "test large upload",
                "description",
                "test large upload description"), largeFile, "text/plain");
            clean.add(id);
            fail("Uploaded Large File and got no error");
        } catch (final Exception x) {
            // Exception is expected
        }
    }

    // Bug 3877
    /*
     * public void testEnforceFolderType() throws Exception { final int folderId =
     * FolderTest.getStandardCalendarFolder(getWebConversation(), getHostName(), sessionId).getObjectID(); try { final int id = createNew(
     * getWebConversation(), getHostName(), sessionId, m( "folder_id" , ((Integer)folderId).toString(), "title" , "Save to Calendar Folder",
     * "description" , "This shouldn't work" ), null, "" ); clean.add(id); fail("Could save infoitem in calendar folder"); } catch (final
     * Exception x) { assertTrue(true); } }
     */

    // Bug 3928
    public void testVersionCommentForNewDocument() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        final String id = createNew(getWebConversation(), getHostName(), sessionId, m(
            "folder_id",
            ((Integer) folderId).toString(),
            "title",
            "test upload",
            "description",
            "test upload description",
            "version_comment",
            "Version Comment"), upload, "text/plain");
        clean.add(id);

        final Response res = get(getWebConversation(), getHostName(), sessionId, id);
        final JSONObject obj = (JSONObject) res.getData();
        assertEquals(1, obj.getInt("version"));
        assertEquals("Version Comment", obj.getString("version_comment"));
    }

    // Bug 4120
    public void testUniqueFilenamesOnUpload() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        final String id = clean.get(0);

        Response res = update(getWebConversation(), getHostName(), sessionId, id, Long.MAX_VALUE, m(), upload, "text/plain");
        assertNoError(res);

        final String id2 = createNew(getWebConversation(), getHostName(), sessionId, m(
            "title",
            "otherFile",
            "description",
            "other_desc",
            "folder_id",
            ((Integer) folderId).toString()), upload, "text/plain");
        clean.add(id2);

        res = get(getWebConversation(), getHostName(), sessionId, id2);

        JSONObject obj = (JSONObject) res.getData();
        assertFalse(upload.getName().equals(obj.get("filename")));

    }

    // Bug 4269 and Bug 7143
    public void testVirtualFolder() throws Exception {
        for (int folderId : virtualFolders) {
            virtualFolder(folderId);
        }
    }

    public void virtualFolder(final int folderId) throws Exception {
        try {
            createNew(getWebConversation(), getHostName(), sessionId, m("folder_id", "" + folderId));
            fail("Expected exception because we can't create a document in this virtual folder");
        } catch (final JSONException x) {
            assertTrue(x.getMessage(), x.getMessage().contains("virt"));
        }
    }

    public void testTitleFromFilename() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        final String id = createNew(
            getWebConversation(),
            getHostName(),
            sessionId,
            m("folder_id", ((Integer) folderId).toString()),
            upload,
            "text/plain");
        clean.add(id);

        final Response res = get(getWebConversation(), getHostName(), sessionId, id);
        final JSONObject obj = (JSONObject) res.getData();

        assertEquals(upload.getName(), obj.getString("title"));
    }

    public void testTitleFromFilenameTheSameEvenIfFilenameIsChangedOnCollision() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        final String id = createNew(
            getWebConversation(),
            getHostName(),
            sessionId,
            m("folder_id", ((Integer) folderId).toString()),
            upload,
            "text/plain");
        clean.add(id);


        final String id2 = createNew(
            getWebConversation(),
            getHostName(),
            sessionId,
            m("folder_id", ((Integer) folderId).toString()),
            upload,
            "text/plain");
        clean.add(id2);

        Response res = get(getWebConversation(), getHostName(), sessionId, id2);
        JSONObject obj = (JSONObject) res.getData();

        assertEquals(obj.getString("filename"), obj.getString("title"));
    }

}
