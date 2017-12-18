
package com.openexchange.ajax.infostore.apiclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import org.junit.Test;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.junit.Assert;
import com.openexchange.testing.httpclient.models.InfoItemData;
import com.openexchange.tools.io.IOTools;

public class UploadActionTest extends InfostoreApiClientTest {

    public static final int SIZE = 15; // Size of the large file in Megabytes

    private static final byte[] megabyte = new byte[1000000];

    /*
     * Create tests ---------------------------------------------------------------------------------------------------------------------
     */

    @Test
    public void testUpload() throws Exception {
        final File file = new File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_INFOSTORE_DIR), "ox.jpg");
        String id = uploadInfoItem(file, "image/jpeg");

        InfoItemData item = getItem(id);
        assertEquals(item.getTitle(), file.getName());
        assertEquals(item.getDescription(), file.getName());
        assertEquals("1", item.getVersion());
        assertEquals("image/jpeg", item.getFileMimetype());
    }

    @Test
    public void testUploadEmptyFile_considerEmptyUpdate() throws Exception {

        final File file = File.createTempFile("infostore-new-test", ".txt");
        String id = uploadInfoItem(file, "text/plain");

        InfoItemData item = getItem(id);
        assertEquals(item.getTitle(), file.getName());
        assertEquals(item.getDescription(), file.getName());
        assertEquals("1", item.getVersion());
        assertEquals("text/plain", item.getFileMimetype());
        assertTrue(file.delete());
    }

    @Test
    public void testLargeFileUpload() throws Exception {

        final File file = File.createTempFile("test", "bin");
        file.deleteOnExit();
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file), 1000000);
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
            uploadInfoItem(file, "text/plain");
            fail();
        } catch (Throwable e) {
            assertTrue(e instanceof AssertionError);
        }
    }

    // Bug 3928
    @Test
    public void testVersionCommentForNewDocument() throws Exception {

        final File file = new File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_INFOSTORE_DIR), "ox.jpg");
        String id = uploadInfoItem(null, file, "image/jpeg", "my comment");

        InfoItemData item = getItem(id);
        assertEquals(item.getTitle(), file.getName());
        assertEquals(item.getDescription(), file.getName());
        assertEquals("1", item.getVersion());
        assertEquals(Integer.valueOf(1), item.getNumberOfVersions());
        assertEquals("my comment", item.getVersionComment());
        assertEquals("image/jpeg", item.getFileMimetype());
    }

    // Bug 4120
    @Test
    public void testUniqueFilenamesOnUpload() throws Exception {

        final File file = new File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_INFOSTORE_DIR), "ox.jpg");
        String id = uploadInfoItem(file, "image/jpeg");

        InfoItemData item = getItem(id);
        assertEquals(item.getTitle(), file.getName());
        assertEquals(item.getDescription(), file.getName());
        assertEquals("1", item.getVersion());
        assertEquals("image/jpeg", item.getFileMimetype());

        String id2 = uploadInfoItem(file, "image/jpeg");
        InfoItemData item2 = getItem(id2);
        assertNotEquals(file.getName(), item2.getFilename());
    }

    @Test
    public void testChunkWiseUpload() throws Exception {
        final File file = new File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_INFOSTORE_DIR), "ox.jpg");
        byte[] all = IOTools.getBytes(new FileInputStream(file));
        int numOfChunks = 3;
        byte[][] chunks = new byte[numOfChunks][];
        int chunkSize = (int) Math.floor(chunks.length / numOfChunks);
        int from = 0, to = chunkSize;
        for (int x = 0; x < numOfChunks; x++) {
            chunks[x] = Arrays.copyOfRange(all, from, to);
            from = to;
            to = to + chunkSize;
        }

        // upload chunks
        String id = null;
        for (int x = 0; x < numOfChunks; x++) {
            id = uploadInfoItem(id, file, "image/jpeg", null, chunks[x], Long.valueOf(x * chunkSize));
        }

        InfoItemData item = getItem(id);
        assertEquals(item.getTitle(), file.getName());
        assertEquals(item.getDescription(), file.getName());
        assertEquals("1", item.getVersion());
        assertEquals("image/jpeg", item.getFileMimetype());
    }

    /*
     * Update tests ---------------------------------------------------------------------------------------------------------------------
     */

    @Test
    public void testBasicUpdate() throws Exception {

        final File file = new File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_INFOSTORE_DIR), "ox.jpg");
        String id = uploadInfoItem(file, "image/jpeg");

        InfoItemData item = getItem(id);
        assertEquals(item.getTitle(), file.getName());
        assertEquals(item.getDescription(), file.getName());
        assertEquals("1", item.getVersion());
        assertEquals("image/jpeg", item.getFileMimetype());

        final File file2 = File.createTempFile("infostore-new-test", ".txt");
        String id2 = uploadInfoItem(id, file2, "text/plain", "version number 2");
        Assert.assertEquals(id, id2);

        item = getItem(id2);
        assertEquals(file2.getName(), item.getTitle());
        assertEquals(file2.getName(), item.getDescription());
        assertEquals("2", item.getVersion());
        assertEquals(Integer.valueOf(2), item.getNumberOfVersions());
        assertEquals("text/plain", item.getFileMimetype());
    }

}
