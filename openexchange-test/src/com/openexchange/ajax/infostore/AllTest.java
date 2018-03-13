
package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import com.google.common.collect.Iterables;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.test.TestInit;

public class AllTest extends InfostoreAJAXTest {

    @Test
    public void testBasic() throws Exception {
        List<com.openexchange.file.storage.File> all = itm.getAll(folderId);

        final Set<String> descriptions = new HashSet<String>(Arrays.asList("test knowledge description", "test url description"));
        final Set<String> titles = new HashSet<String>(Arrays.asList("test knowledge", "test url"));

        assertFalse(itm.getLastResponse().hasError());

        final JSONArray entries = (JSONArray) itm.getLastResponse().getData();

        for(int i = 0; i < entries.length(); i++) {
            final JSONArray entry = entries.getJSONArray(i);

            assertTrue(titles.remove(entry.getString(1)));
            assertTrue(descriptions.remove(entry.getString(2)));
        }
    }

    //Bug 4269
    @Test
    public void testVirtualFolder() throws Exception {
        for (int folderId : virtualFolders) {
            virtualFolderTest(folderId);
        }
    }

    public void virtualFolderTest(int folderid) throws JSONException, IOException, OXException {
        List<com.openexchange.file.storage.File> all = itm.getAll(folderid);
        assertNotNull(all);
        assertFalse(itm.getLastResponse().hasError());
        assertEquals(0, ((JSONArray) itm.getLastResponse().getData()).length());
    }

    // Node 2652
    @Test
    public void testLastModifiedUTC() throws Exception {
        List<com.openexchange.file.storage.File> all = itm.getAll(folderId);
        assertFalse(itm.getLastResponse().hasError());

        final int size = all.size();
        assertTrue(size > 0);

        for (int i = 0; i < size; i++) {
            final com.openexchange.file.storage.File entry = all.get(i);
            assertNotNull(entry);
        }
    }

    // Bug 12427
    @Test
    public void testNumberOfVersions() throws JSONException, IOException, OXException {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        final String id = Iterables.get(itm.getCreatedEntities(), 0).getId();

        com.openexchange.file.storage.File org = itm.getAction(id);
        org.setDescription("New description");
        itm.updateAction(org, upload, new com.openexchange.file.storage.File.Field[] {com.openexchange.file.storage.File.Field.DESCRIPTION}, new Date(Long.MAX_VALUE)); // V1
        {
            OXException exception = itm.getLastResponse().getException();
            if (null != exception) {
                fail("An unexpected exception occurred: " + exception.getMessage());
            }
        }

        int[] columns = new int[] { Metadata.ID, Metadata.NUMBER_OF_VERSIONS };
        itm.getAll(folderId, columns);

        JSONArray rows = (JSONArray) itm.getLastResponse().getData();
        boolean found = false;
        for(int i = 0, size = rows.length(); i < size; i++) {
            JSONArray row = rows.getJSONArray(i);
            String objectId = row.getString(0);
            int numberOfVersions = row.getInt(1);

            if(objectId.equals(id)) {
                assertEquals(2, numberOfVersions);
                found = true;
            }
        }
        assertTrue(found);
    }
}
