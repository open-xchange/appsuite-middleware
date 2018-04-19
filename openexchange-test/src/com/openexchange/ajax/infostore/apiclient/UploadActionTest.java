
package com.openexchange.ajax.infostore.apiclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

/**
 *
 * {@link UploadActionTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class UploadActionTest extends InfostoreApiClientTest {

    public static final int SIZE = 15; // Size of the large file in Megabytes

    private static final byte[] megabyte = new byte[1000000];

    /*
     * Create tests ---------------------------------------------------------------------------------------------------------------------
     */

    @Test
    public void testUpload() throws Exception {
        final File file = new File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_DIR), "ox.jpg");
        String id = uploadInfoItem(file, MIME_IMAGE_JPG);

        InfoItemData item = getItem(id);
        checkFile(file, item, "1", MIME_IMAGE_JPG);
    }

    @Test
    public void testUploadEmptyFile_considerEmptyUpdate() throws Exception {

        final File file = File.createTempFile("infostore-new-test", ".txt");
        String id = uploadInfoItem(file, MIME_TEXT_PLAIN);

        InfoItemData item = getItem(id);
        checkFile(file, item, "1", MIME_TEXT_PLAIN);
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
            uploadInfoItem(file, MIME_TEXT_PLAIN);
            fail();
        } catch (Throwable e) {
            assertFalse(e instanceof AssertionError);
        }
    }

    // Bug 3928
    @Test
    public void testVersionCommentForNewDocument() throws Exception {

        final File file = new File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_DIR), "ox.jpg");
        String id = uploadInfoItem(null, file, MIME_IMAGE_JPG, "my comment");

        InfoItemData item = getItem(id);
        checkFile(file, item, "1", MIME_IMAGE_JPG);
        assertEquals(Integer.valueOf(1), item.getNumberOfVersions());
        assertEquals("my comment", item.getVersionComment());
    }

    // Bug 4120
    @Test
    public void testUniqueFilenamesOnUpload() throws Exception {

        final File file = new File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_DIR), "ox.jpg");
        String id = uploadInfoItem(file, MIME_IMAGE_JPG);

        InfoItemData item = getItem(id);
        checkFile(file, item, "1", MIME_IMAGE_JPG);

        String id2 = uploadInfoItem(file, MIME_IMAGE_JPG);
        InfoItemData item2 = getItem(id2);
        assertNotEquals(file.getName(), item2.getFilename());
    }

    @Test
    public void testChunkWiseUpload() throws Exception {
        String testDir = AJAXConfig.getProperty(AJAXConfig.Property.TEST_DIR);
        System.out.println("Going to use file from directory '" + testDir + "'.");
        final File file = new File(testDir, "ox.jpg");
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
            id = uploadInfoItem(id, file, MIME_IMAGE_JPG, null, chunks[x], Long.valueOf(x * chunkSize), Long.valueOf(all.length));
        }

        InfoItemData item = getItem(id);
        checkFile(file, item, "1", MIME_IMAGE_JPG);
    }

    /*
     * Update tests ---------------------------------------------------------------------------------------------------------------------
     */

    @Test
    public void testBasicUpdate() throws Exception {

        final File file = new File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_DIR), "ox.jpg");
        String id = uploadInfoItem(file, MIME_IMAGE_JPG);

        InfoItemData item = getItem(id);
        checkFile(file, item, "1", MIME_IMAGE_JPG);

        final File file2 = File.createTempFile("infostore-new-test", ".txt");
        String id2 = uploadInfoItem(id, file2, MIME_TEXT_PLAIN, "version number 2");
        Assert.assertEquals(id, id2);

        item = getItem(id2);
        checkFile(file2, item, "2", MIME_TEXT_PLAIN);
        assertEquals(Integer.valueOf(2), item.getNumberOfVersions());
    }

    private void checkFile(File file, InfoItemData item, String version, String mimetype) {
        assertEquals(file.getName(), item.getTitle());
        assertEquals(file.getName(), item.getFilename());
        assertEquals(version, item.getVersion());
        assertEquals(mimetype, item.getFileMimetype());
    }

}
