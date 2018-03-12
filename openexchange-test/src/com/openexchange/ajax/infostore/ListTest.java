
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

public class ListTest extends InfostoreAJAXTest {

    @Test
    public void testBasic() throws Exception {
        final String[][] bothEntries = new String[2][2];
        final String origId1 = Iterables.get(itm.getCreatedEntities(), 0).getId();
        bothEntries[0][1] = origId1;
        final String origId2 = Iterables.get(itm.getCreatedEntities(), 1).getId();
        bothEntries[1][1] = origId2;

        bothEntries[0][0] = String.valueOf(folderId);
        bothEntries[1][0] = String.valueOf(folderId);

        checkEntries(bothEntries);

    }

    @Test
    public void testSkipsMissingIds() throws Exception {
        final String fantasyID = getFantasyID();

        final String[][] entries = new String[4][2];
        entries[0][1] = Iterables.get(itm.getCreatedEntities(), 0).getId();
        entries[1][1] = String.valueOf(fantasyID);
        entries[2][1] = Iterables.get(itm.getCreatedEntities(), 1).getId();
        entries[3][1] = Iterables.get(itm.getCreatedEntities(), 1).getId();

        entries[0][0] = String.valueOf(folderId);
        entries[1][0] = String.valueOf(folderId);
        entries[2][0] = String.valueOf(folderId);
        entries[3][0] = fantasyID;

        checkEntries(entries);

    }

    // Node 2652    @Test
    @Test
    public void testLastModifiedUTC() throws JSONException, IOException, OXException {
        final String[][] bothEntries = new String[2][2];
        final String origId1 = Iterables.get(itm.getCreatedEntities(), 0).getId();
        bothEntries[0][1] = origId1;
        final String origId2 = Iterables.get(itm.getCreatedEntities(), 1).getId();
        bothEntries[1][1] = origId2;

        bothEntries[0][0] = String.valueOf(folderId);
        bothEntries[1][0] = String.valueOf(folderId);

        itm.list(bothEntries, new int[] { Metadata.LAST_MODIFIED_UTC });
        assertFalse(itm.getLastResponse().hasError());

        final JSONArray arr = (JSONArray) itm.getLastResponse().getData();
        final int size = arr.length();
        assertTrue(size > 0);

        for (int i = 0; i < size; i++) {
            final JSONArray row = arr.optJSONArray(i);
            assertTrue(row.length() == 1);
            assertNotNull(row.optLong(0));
        }
    }

    // Bug 12427
    @Test
    public void testNumberOfVersions() throws JSONException, IOException, OXException {
        final String[][] entries = new String[1][2];
        entries[0][0] = String.valueOf(folderId);
        String origId = Iterables.get(itm.getCreatedEntities(), 0).getId();
        entries[0][1] = origId;

        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        com.openexchange.file.storage.File org = itm.getAction(origId);
        org.setDescription("New description");
        itm.updateAction(org, upload, new com.openexchange.file.storage.File.Field[] {}, new Date(Long.MAX_VALUE));
        {
            OXException exception = itm.getLastResponse().getException();
            if (null != exception) {
                fail("An unexpected exception occurred: " + exception.getMessage());
            }
        }

        itm.list(entries, new int[] { Metadata.ID, Metadata.NUMBER_OF_VERSIONS });

        JSONArray rows = (JSONArray) itm.getLastResponse().getData();
        boolean found = false;
        for (int i = 0, size = rows.length(); i < size; i++) {
            JSONArray row = rows.getJSONArray(i);
            String id = row.getString(0);
            int numberOfVersions = row.getInt(1);

            if (id.equals(origId)) {
                assertEquals(2, numberOfVersions);
                found = true;
            }
        }
        assertTrue(found);
    }

    // Find a non-existing ID
    public String getFantasyID() throws JSONException, IOException, OXException {
        String id = "20000";
        com.openexchange.file.storage.File file = itm.getAction(id);
        assertTrue(itm.getLastResponse().hasError());
        return id;
    }

    public void checkEntries(final String[][] infostore_ids) throws Exception {
        List<com.openexchange.file.storage.File> list = itm.list(infostore_ids, new int[] { Metadata.ID, Metadata.TITLE, Metadata.DESCRIPTION, Metadata.URL });
        assertFalse(itm.getLastResponse().hasError());

        final Set<String> ids = itm.getCreatedEntitiesIds();
        final Set<String> descriptions = new HashSet<String>(Arrays.asList("test knowledge description", "test url description"));
        final Set<String> urls = new HashSet<String>(Arrays.asList("http://www.open-xchange.com"));
        final Set<String> titles = new HashSet<String>(Arrays.asList("test knowledge", "test url"));

        final JSONArray entries = (JSONArray) itm.getLastResponse().getData();

        for (int i = 0; i < entries.length(); i++) {
            final JSONArray entry = entries.getJSONArray(i);

            assertTrue(ids.remove(entry.getString(0)));
            assertTrue(titles.remove(entry.getString(1)));
            assertTrue(descriptions.remove(entry.getString(2)));
            urls.remove(entry.getString(3));
        }

        assertTrue(ids.isEmpty());
        assertTrue(descriptions.isEmpty());
        assertTrue(urls.isEmpty());
        assertTrue(titles.isEmpty());
    }

}
