
package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import org.junit.Test;
import com.google.common.collect.Iterables;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.test.TestInit;

public class NewTest extends InfostoreAJAXTest {

    public static final int SIZE = 15; // Size of the large file in Megabytes

    private static final byte[] megabyte = new byte[1000000];

    @Test
    public void testUpload() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        com.openexchange.file.storage.File data = createFile(folderId, "test upload");
        data.setFileMIMEType("text/plain");
        data.setDescription("test upload description");
        itm.newAction(data, upload);

        String id = data.getId();

        com.openexchange.file.storage.File obj = itm.getAction(id);
        assertEquals(data.getTitle(), obj.getTitle());
        assertEquals(data.getDescription(), obj.getDescription());
        assertEquals("1", obj.getVersion());
        assertEquals("text/plain", obj.getFileMIMEType());

        InputStream is = null;
        InputStream is2 = null;
        try {
            is = new FileInputStream(upload);
            is2 = itm.document(Integer.toString(folderId), id, "1");

        } finally {
            if (is != null) {
                is.close();
            }
            if (is2 != null) {
                is2.close();
            }
        }

        com.openexchange.file.storage.File dataNoUpload = createFile(folderId, "test no upload");
        dataNoUpload.setFileMIMEType("");
        dataNoUpload.setDescription("test no upload description");
        itm.newAction(dataNoUpload);

        id = dataNoUpload.getId();

        obj = itm.getAction(id);

        assertEquals("test no upload", obj.getTitle());
        assertEquals("test no upload description", obj.getDescription());
    }

    @Test
    public void testUploadEmptyFile_considerEmptyUpdate() throws Exception {
        File emptyFile = File.createTempFile("infostore-new-test", ".txt");

        com.openexchange.file.storage.File file = new DefaultFile();
        file.setFolderId(String.valueOf(folderId));
        file.setTitle("test upload");
        file.setDescription("test upload description");
        file.setFileMIMEType("text/plain");

        itm.newAction(file, emptyFile);

        String id = file.getId();

        com.openexchange.file.storage.File obj = itm.getAction(id);
        assertEquals(file.getTitle(), obj.getTitle());
        assertEquals(file.getDescription(), obj.getDescription());
        assertEquals("0", obj.getVersion());
        assertEquals("application/octet-stream", obj.getFileMIMEType());
        assertTrue(emptyFile.delete());
    }

    @Test
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

        com.openexchange.file.storage.File data = createFile(folderId, "test large upload");
        data.setFileMIMEType("text/plain");
        itm.setFailOnError(false);
        try {
            itm.newAction(data, largeFile);
            fail();
        } catch (Throwable e) {
            assertTrue(e instanceof AssertionError);
        }

    }

    // Bug 3928 
    @Test
    public void testVersionCommentForNewDocument() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        com.openexchange.file.storage.File data = createFile(folderId, "test upload");
        data.setFileMIMEType("text/plain");
        data.setDescription("test upload description");
        data.setVersionComment("Version Comment");
        itm.newAction(data, upload);

        String id = data.getId();

        com.openexchange.file.storage.File obj = itm.getAction(id);
        assertEquals(1, obj.getNumberOfVersions());
        assertEquals("Version Comment", obj.getVersionComment());
    }

    // Bug 4120 
    @Test
    public void testUniqueFilenamesOnUpload() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        final String id = Iterables.get(itm.getCreatedEntities(), 0).getId();

        com.openexchange.file.storage.File org = itm.getAction(id);
        itm.updateAction(org, upload, new com.openexchange.file.storage.File.Field[] {}, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        com.openexchange.file.storage.File data = createFile(folderId, "otherFile");
        data.setFileMIMEType("text/plain");
        data.setDescription("other_desc");
        itm.newAction(data, upload);

        com.openexchange.file.storage.File obj = itm.getAction(data.getId());
        assertFalse(upload.getName().equals(obj.getFileName()));
    }

    @Test
    public void testTitleFromFilename() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        com.openexchange.file.storage.File data = createFile(folderId, null);
        data.setFileMIMEType("text/plain");
        itm.newAction(data, upload);

        com.openexchange.file.storage.File obj = itm.getAction(data.getId());
        assertEquals(data.getTitle(), obj.getTitle());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testTitleFromFilenameTheSameEvenIfFilenameIsChangedOnCollision() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        com.openexchange.file.storage.File data = createFile(folderId, "test upload");
        data.setFileMIMEType("text/plain");
        itm.newAction(data, upload);

        com.openexchange.file.storage.File data2 = createFile(folderId, "test upload");
        data.setFileMIMEType("text/plain");
        itm.newAction(data2, upload);

        com.openexchange.file.storage.File action = itm.getAction(data2.getId());

        assertEquals(action.getFileName(), action.getTitle());
    }

}
