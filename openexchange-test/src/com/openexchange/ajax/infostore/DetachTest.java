
package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import com.google.common.collect.Iterables;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.search.Order;
import com.openexchange.test.TestInit;

public class DetachTest extends InfostoreAJAXTest {

    private String origId;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        final long FAR_FUTURE = Long.MAX_VALUE;

        origId = Iterables.get(itm.getCreatedEntities(), 0).getId();
        com.openexchange.file.storage.File org = itm.getAction(origId);
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
        com.openexchange.file.storage.File file = itm.getAction(origId);
        ftm.detach(file.getId(), file.getLastModified(), new int[] { 1, 2, 3, 4, 5 });

        checkVersions(origId, "6", 1);
    }

    @Test
    public void testRevert() throws Exception {
        com.openexchange.file.storage.File file = itm.getAction(origId);

        itm.revert(file.getId());
        assertFalse(itm.getLastResponse().hasError());
        assertNotNull(itm.getLastResponse().getTimestamp());
        checkVersions(origId, "0", 0);;
    }

    public void checkVersions(String objectId, String version, int numberoOfVersions) throws Exception {
        //       Version magically reverts to 0
        com.openexchange.file.storage.File obj = itm.getAction(objectId);

        assertEquals(version, obj.getVersion());
        assertEquals(numberoOfVersions, obj.getNumberOfVersions());

        ftm.detach(objectId, obj.getLastModified(), new int[] { 1, 2, 3 });

        com.openexchange.file.storage.File obj2 = itm.getAction(objectId);

        final Set<Integer> versions = new HashSet<Integer>(Arrays.asList(new Integer[] { 1, 2, 3 }));

        final int[] notDetached = ftm.detach(objectId, obj2.getLastModified(), new int[] { 1, 2, 3 });
        assertEquals(versions.size(), notDetached.length);
        for (final int lId : notDetached) {
            assertTrue(versions.remove(lId));
        }
        assertTrue(versions.isEmpty());
    }

    @Test
    public void testSpotted() throws Exception {
        final String origId = Iterables.get(itm.getCreatedEntities(), 0).getId();

        final int[] notDetached = ftm.detach(origId, new Date(Long.MAX_VALUE), new int[] { 1, 3, 5, 6 });
        assertEquals(0, notDetached.length);

        List<com.openexchange.file.storage.File> versions = itm.versions(origId, new int[] { Metadata.VERSION, Metadata.CURRENT_VERSION }, Metadata.VERSION, Order.DESCENDING);
        AbstractAJAXResponse lastResponse = itm.getLastResponse();
        assertFalse(lastResponse.hasError());
        // Current Version reverts to 4 (being the newest available version
        VersionsTest.assureVersions(new Integer[] { 2, 4 }, lastResponse, 4);

        com.openexchange.file.storage.File obj = itm.getAction(origId);

        assertEquals("4", obj.getVersion());
    }

    @Test
    public void testDetachVersion0() throws Exception {
        final String origId = Iterables.get(itm.getCreatedEntities(), 0).getId();

        final int[] notDetached = ftm.detach(origId, new Date(Long.MAX_VALUE), new int[] { 0 });
        assertEquals(1, notDetached.length);
        assertEquals(0, notDetached[0]);
    }

    // Bug 3818
    @Test
    public void testCopyComments() throws Exception {
        final String origId = Iterables.get(itm.getCreatedEntities(), 0).getId();
        com.openexchange.file.storage.File file = itm.getAction(origId);
        file.setDescription("current_description");
        itm.updateAction(file, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.DESCRIPTION }, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        final int[] notDetached = ftm.detach(origId, new Date(Long.MAX_VALUE), new int[] { 5, 4, 3 });
        assertEquals(0, notDetached.length);

        com.openexchange.file.storage.File document = itm.getAction(origId);
        assertEquals("current_description", document.getDescription());
    }

    //	Bug 4120
    @Test
    public void testUniqueFilenames() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        final String origId = Iterables.get(itm.getCreatedEntities(), 0).getId();

        com.openexchange.file.storage.File file = itm.getAction(origId);
        file.setFileName("blubb.properties");
        itm.updateAction(file, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.FILENAME }, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        com.openexchange.file.storage.File data = createFile(folderId, "otherFile");
        data.setFileMIMEType("text/plain");
        data.setDescription("other_desc");
        itm.newAction(data, upload);

        final int[] notDetached = ftm.detach(origId, new Date(Long.MAX_VALUE), new int[] { 5 });

    }
}
