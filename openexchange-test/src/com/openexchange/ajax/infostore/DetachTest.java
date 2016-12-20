
package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.test.TestInit;

public class DetachTest extends InfostoreAJAXTest {

    public DetachTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        final long FAR_FUTURE = Long.MAX_VALUE;

        com.openexchange.file.storage.File org = itm.getAction(clean.get(0));
        itm.updateAction(org, upload, new com.openexchange.file.storage.File.Field[] {}, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        itm.updateAction(org, upload, new com.openexchange.file.storage.File.Field[] {}, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        itm.updateAction(org, upload, new com.openexchange.file.storage.File.Field[] {}, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        itm.updateAction(org, upload, new com.openexchange.file.storage.File.Field[] {}, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        itm.updateAction(org, upload, new com.openexchange.file.storage.File.Field[] {}, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        itm.updateAction(org, upload, new com.openexchange.file.storage.File.Field[] {}, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());
    }

    @Test
    public void testBasic() throws Exception {
        ftm.detach(clean.get(0), new Date(System.currentTimeMillis()), new int[] { 1, 2, 3, 4, 5 });

        checkNoVersions();
    }

    @Test
    public void testRevert() throws Exception {
        itm.revert(clean.get(0));
        assertFalse(itm.getLastResponse().hasError());
        assertNotNull(itm.getLastResponse().getTimestamp());
        checkNoVersions();
    }

    public void checkNoVersions() throws Exception {
        //		 Version magically reverts to 0
        com.openexchange.file.storage.File obj = itm.getAction(clean.get(0));

        assertEquals(0, obj.getVersion());

        ftm.detach(clean.get(0), new Date(System.currentTimeMillis()), new int[] { 1, 2, 3 });

        final Set<Integer> versions = new HashSet<Integer>(Arrays.asList(new Integer[] { 1, 2, 3 }));

        //TODO 
        //        final int[] notDetached = detach(getWebConversation(), getHostName(), sessionId, System.currentTimeMillis(), clean.get(0), new int[] { 1, 2, 3 });
        //        assertEquals(versions.size(), notDetached.length);
        //        for (final int id : notDetached) {
        //            assertTrue(versions.remove(id));
        //        }
        //        assertTrue(versions.isEmpty());

        com.openexchange.file.storage.File file = itm.getAction(clean.get(0));

        assertEquals(null, file.getFileName());
        assertEquals("", file.getFileMIMEType());
        assertEquals(0, file.getFileSize());
    }

//    TODO MS re-add
//    @Test
//        public void testSpotted() throws Exception {
//            ftm.detach(clean.get(0), new Date(Long.MAX_VALUE), new int[] { 1, 3, 5 });
//
//        final int[] notDetached = detach(getWebConversation(), getHostName(), sessionId, Long.MAX_VALUE, clean.get(0), new int[] { 1, 3, 5 });
//            assertEquals(0, notDetached.length);
//    
//            Response res = versions(getWebConversation(), getHostName(), sessionId, clean.get(0), new int[] { Metadata.VERSION, Metadata.CURRENT_VERSION });
//            assertNoError(res);
//            // Current Version reverts to 4 (being the newest available version
//            VersionsTest.assureVersions(new Integer[] { 2, 4 }, res, 4);
//            asser
//    
//            com.openexchange.file.storage.File obj = itm.getAction(clean.get(0));
//    
//            assertEquals(4, obj.getVersion());
//        }

//  TODO MS re-add
//    @Test
//    public void testDetachVersion0() throws Exception {
//        final int[] notDetached = detach(getWebConversation(), getHostName(), sessionId, Long.MAX_VALUE, clean.get(0), new int[] { 0 });
//        assertEquals(1, notDetached.length);
//        assertEquals(0, notDetached[0]);
//    }

    // Bug 3818
//  TODO MS re-add
//    @Test
//    public void testCopyComments() throws Exception {
//        com.openexchange.file.storage.File file = itm.getAction(clean.get(0));
//        file.setDescription("current_description");
//        itm.updateAction(file, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.DESCRIPTION }, new Date(Long.MAX_VALUE));
//        assertFalse(itm.getLastResponse().hasError());
//
//        final int[] notDetached = detach(getWebConversation(), getHostName(), sessionId, Long.MAX_VALUE, clean.get(0), new int[] { 5, 4, 3 });
//        assertEquals(0, notDetached.length);
//
//        com.openexchange.file.storage.File document = itm.getAction(clean.get(0));
//        assertEquals("current_description", document.getDescription());
//    }

    //	Bug 4120
//  TODO MS re-add
//    @Test
//    public void testUniqueFilenames() throws Exception {
//        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
//
//        com.openexchange.file.storage.File file = itm.getAction(clean.get(0));
//        file.setFileName("blubb.properties");
//        itm.updateAction(file, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.FILENAME }, new Date(Long.MAX_VALUE));
//        assertFalse(itm.getLastResponse().hasError());
//
//        com.openexchange.file.storage.File data = createFile(folderId, "otherFile");
//        data.setFileMIMEType("text/plain");
//        data.setDescription("other_desc");
//        itm.newAction(data, upload);
//
//        clean.add(data.getId());
//
//        detach(getWebConversation(), getHostName(), sessionId, Long.MAX_VALUE, clean.get(0), new int[] { 5 });
//
//    }
}
