
package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.google.common.collect.Iterables;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.search.Order;
import com.openexchange.test.TestInit;

public class SearchTest extends InfostoreAJAXTest {

    protected String[] all = null;
    private static final int[] COLS = new int[] { Metadata.TITLE, Metadata.DESCRIPTION };

    @Before
    public void setUp() throws Exception {
        super.setUp();

        all = new String[26];

        final char[] alphabet = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
        for (int i = 0; i < 26; i++) {
            com.openexchange.file.storage.File tempFile = InfostoreTestManager.createFile(folderId, "Test " + i, "text/javascript");
            tempFile.setDescription("this is document "+alphabet[i]);
            itm.newAction(tempFile);
            all[i] = "Test " + i;
        }
    }

    @Test
    public void testBasic() throws Exception {
        List<com.openexchange.file.storage.File> files = itm.search("5", COLS, folderId, Metadata.TITLE, Order.NO_ORDER);
        assertFalse(itm.getLastResponse().hasError());

        assertTitles(files, "Test 5", "Test 15", "Test 25");

    }

    @Test
    public void testPattern() throws Exception {
        List<com.openexchange.file.storage.File> files = itm.search("*", COLS, folderId, Metadata.TITLE, Order.NO_ORDER);
        assertFalse(itm.getLastResponse().hasError());
        assertTitles(files, all);

        files = itm.search("Test ?5", COLS, folderId, Metadata.TITLE, Order.NO_ORDER);
        assertFalse(itm.getLastResponse().hasError());
        assertTitles(files, "Test 15", "Test 25");
    }

    @Test
    public void testAll() throws Exception {
        List<com.openexchange.file.storage.File> files = itm.search("", COLS, folderId, Metadata.TITLE, Order.NO_ORDER);
        assertFalse(itm.getLastResponse().hasError());
        assertTitles(files, all);
    }

    @Test
    public void testCaseInsensitive() throws Exception {
        List<com.openexchange.file.storage.File> files = itm.search("test", COLS, folderId, Metadata.TITLE, Order.NO_ORDER);
        assertFalse(itm.getLastResponse().hasError());
        assertTitles(files, all);
    }

    @Test
    public void testStartAndStop() throws Exception {
        List<com.openexchange.file.storage.File> files = itm.search("5", COLS, folderId, Metadata.DESCRIPTION, Order.ASCENDING);
        assertFalse(itm.getLastResponse().hasError());
        assertTitles(files, all);

        JSONArray arrayOfarrays = (JSONArray) itm.getLastResponse().getData();
        assertEquals(2, arrayOfarrays.length());
        assertTitle(0, arrayOfarrays, "Test 5");
        assertTitle(1, arrayOfarrays, "Test 15");

        files = itm.search("5", COLS, folderId, Metadata.DESCRIPTION, Order.DESCENDING);
        assertFalse(itm.getLastResponse().hasError());

        arrayOfarrays = (JSONArray) itm.getLastResponse().getData();

        assertEquals(2, arrayOfarrays.length());
        assertTitle(0, arrayOfarrays, "Test 25");
        assertTitle(1, arrayOfarrays, "Test 15");

        files = itm.search("5", COLS, folderId, Metadata.DESCRIPTION, Order.DESCENDING);
        assertFalse(itm.getLastResponse().hasError());

        arrayOfarrays = (JSONArray) itm.getLastResponse().getData();

        assertEquals(2, arrayOfarrays.length());
        assertTitle(0, arrayOfarrays, "Test 15");
        assertTitle(1, arrayOfarrays, "Test 5");

        files = itm.search("5", COLS, folderId, Metadata.DESCRIPTION, Order.DESCENDING);
        assertFalse(itm.getLastResponse().hasError());

        arrayOfarrays = (JSONArray) itm.getLastResponse().getData();

        assertEquals(2, arrayOfarrays.length());
        assertTitle(0, arrayOfarrays, "Test 15");
        assertTitle(1, arrayOfarrays, "Test 5");

    }

    @Test
    public void testLimit() throws Exception {
        List<com.openexchange.file.storage.File> files = itm.search("5", COLS, folderId, Metadata.DESCRIPTION, Order.ASCENDING);
        assertFalse(itm.getLastResponse().hasError());
        assertTitles(files, all);

        final JSONArray arrayOfarrays = (JSONArray) itm.getLastResponse().getData();
        assertEquals(1, arrayOfarrays.length());
        assertTitle(0, arrayOfarrays, "Test 5");

    }

    @Test
    public void testSort() throws Exception {
        List<com.openexchange.file.storage.File> files = itm.search("5", COLS, folderId, Metadata.DESCRIPTION, Order.ASCENDING);
        assertFalse(itm.getLastResponse().hasError());
        assertTitles(files, all);

        JSONArray arrayOfarrays = (JSONArray) itm.getLastResponse().getData();

        assertTitle(0, arrayOfarrays, "Test 5");
        assertTitle(1, arrayOfarrays, "Test 15");
        assertTitle(2, arrayOfarrays, "Test 25");

        files = itm.search("5", COLS, folderId, Metadata.DESCRIPTION, Order.DESCENDING);
        assertFalse(itm.getLastResponse().hasError());

        arrayOfarrays = (JSONArray) itm.getLastResponse().getData();

        assertTitle(0, arrayOfarrays, "Test 25");
        assertTitle(1, arrayOfarrays, "Test 15");
        assertTitle(2, arrayOfarrays, "Test 5");

    }

    @Test
    public void testVersions() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        final String id = Iterables.get(itm.getCreatedEntities(), 0).getId();
        com.openexchange.file.storage.File org = itm.getAction(id);
        org.setTitle("File");
        itm.updateAction(org, upload, new com.openexchange.file.storage.File.Field[] {}, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        List<com.openexchange.file.storage.File> found = itm.search("File", COLS, folderId, Metadata.TITLE, Order.NO_ORDER);
        assertFalse(itm.getLastResponse().hasError());

        assertTitles(found, "File");

        itm.revert(id);
        assertFalse(itm.getLastResponse().hasError());

        List<com.openexchange.file.storage.File> files = itm.search("1", COLS, folderId, Metadata.ID, Order.NO_ORDER);
        assertFalse(itm.getLastResponse().hasError());

        assertTitles(files, "Test 1", "Test 10", "Test 11", "Test 12", "Test 13", "Test 14", "Test 15", "Test 16", "Test 17", "Test 18", "Test 19", "Test 21");
    }

    // Tests functionality that no one requested yet
    public void notestEscape() throws Exception {
        final String id = Iterables.get(itm.getCreatedEntities(), 0).getId();
        com.openexchange.file.storage.File file = itm.getAction(id);
        file.setTitle("The mysterious ?");
        itm.updateAction(file, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.TITLE }, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        List<com.openexchange.file.storage.File> files = itm.search("\\?", COLS, folderId, Metadata.TITLE, Order.NO_ORDER);
        assertFalse(itm.getLastResponse().hasError());

        assertTitles(files, "The mysterious ?");

        file = itm.getAction(id);
        file.setTitle("The * of all trades");
        itm.updateAction(file, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.TITLE }, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        files = itm.search("\\*", COLS, folderId, Metadata.TITLE, Order.NO_ORDER);
        assertFalse(itm.getLastResponse().hasError());

        assertTitles(files, "The * of all trades");

    }

    @Test
    public void testPermissions() throws Exception {
        final String sessionId2 = this.getSecondSessionId();

        itm.setClient(getClient2());
        itm.search("*", COLS, folderId, Metadata.LAST_MODIFIED_UTC, Order.NO_ORDER);

        assertEquals("IFO-0400", itm.getLastResponse().getException().getErrorCode());

        itm.setClient(getClient());
    }

    @Test
    public void testCategories() throws Exception {
        final String id = Iterables.get(itm.getCreatedEntities(), 0).getId();
        com.openexchange.file.storage.File file = itm.getAction(id);
        file.setCategories("[\"curiosity\", \"cat\", \"danger\"]");
        itm.updateAction(file, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.CATEGORIES }, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        List<com.openexchange.file.storage.File> files = itm.search("curiosity", COLS, folderId, Metadata.TITLE, Order.DESCENDING);
        assertFalse(itm.getLastResponse().hasError());

        assertTitles(files, "Test 0");

    }

    // Node 2652
    @Test
    public void testLastModifiedUTC() throws JSONException, IOException, OXException {
        itm.search("*", new int[] { Metadata.LAST_MODIFIED_UTC }, folderId, Metadata.LAST_MODIFIED_UTC, Order.NO_ORDER);
        assertFalse(itm.getLastResponse().hasError());
        final JSONArray results = (JSONArray) itm.getLastResponse().getData();
        final int size = results.length();
        assertTrue(size > 0);

        for (int i = 0; i < size; i++) {
            final JSONArray row = results.optJSONArray(i);
            assertNotNull(row);
            assertTrue(row.length() > 0);
            assertNotNull(row.optLong(0));
        }
    }

    // Bug 18124
    @Test
    public void testBackslashFound() throws Exception {
        String title = "Test\\WithBackslash";
        com.openexchange.file.storage.File createdFile = createFileOnServer(folderId, title, "text/javascript");

        List<com.openexchange.file.storage.File> files = itm.search(title, new int[] { Metadata.TITLE, Metadata.ID }, folderId, Metadata.ID, Order.NO_ORDER);

        assertTitles(files, title);
    }

    public static void assertTitle(final int index, final JSONArray results, final String title) throws JSONException {
        final JSONArray entry = results.getJSONArray(index);
        assertEquals(title, entry.getString(0));
    }

    public static void assertTitles(final List<com.openexchange.file.storage.File> files, final String... titles) throws JSONException {
        final Set<String> titlesSet = new HashSet<String>(Arrays.asList(titles));

        final String error = "Expected: " + titlesSet + " but got " + files;
        assertEquals(error, titles.length, files.size());
        for (int i = 0; i < files.size(); i++) {
            final com.openexchange.file.storage.File entry = files.get(i);
            assertTrue(error, titlesSet.remove(entry.getFileName()));
        }
    }

}
