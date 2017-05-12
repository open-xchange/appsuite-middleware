
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
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.google.common.collect.Iterables;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.search.Order;
import com.openexchange.test.FolderTestManager;
import com.openexchange.test.TestInit;

public class SearchTest extends AbstractAJAXSession {

    protected String[] all = null;

    protected int folderId;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        this.folderId = createFolderForTest();

        all = new String[26];

        final char[] alphabet = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
        for (int i = 0; i < 26; i++) {
            com.openexchange.file.storage.File tempFile = InfostoreTestManager.createFile(folderId, "Test " + i, "text/javascript");
            tempFile.setDescription("this is document " + alphabet[i]);
            itm.newAction(tempFile);
            all[i] = "Test " + i;
        }
    }

    private int createFolderForTest() throws JSONException, OXException, IOException {
        final int parent = getClient().getValues().getPrivateInfostoreFolder();
        FolderObject folder = FolderTestManager.createNewFolderObject("NewInfostoreFolder" + UUID.randomUUID().toString(), Module.INFOSTORE.getFolderConstant(), FolderObject.PUBLIC, getClient().getValues().getUserId(), parent);
        return ftm.insertFolderOnServer(folder).getObjectID();
    }

    @Test
    public void testBasic() throws Exception {
        List<com.openexchange.file.storage.File> files = itm.search("5", folderId);
        assertFalse(itm.getLastResponse().hasError());

        assertTitles(files, "Test 5", "Test 15", "Test 25");
    }

    @Test
    public void testPattern() throws Exception {
        List<com.openexchange.file.storage.File> files = itm.search("*", folderId);
        assertFalse(itm.getLastResponse().hasError());
        assertTitles(files, all);

        files = itm.search("Test ?5", folderId);
        assertFalse(itm.getLastResponse().hasError());
        assertTitles(files, "Test 15", "Test 25");
    }

    @Test
    public void testAll() throws Exception {
        List<com.openexchange.file.storage.File> files = itm.search("", folderId);
        assertFalse(itm.getLastResponse().hasError());
        assertTitles(files, all);
    }

    @Test
    public void testCaseInsensitive() throws Exception {
        List<com.openexchange.file.storage.File> files = itm.search("test", folderId);
        assertFalse(itm.getLastResponse().hasError());
        assertTitles(files, all);
    }

    @Test
    public void testStartAndStop() throws Exception {
        List<com.openexchange.file.storage.File> files = itm.search("5", folderId, Metadata.DESCRIPTION, Order.ASCENDING, -1, 0, 1);
        assertFalse(itm.getLastResponse().hasError());

        JSONArray arrayOfarrays = (JSONArray) itm.getLastResponse().getData();
        assertEquals(2, arrayOfarrays.length());
        assertTitle(0, arrayOfarrays, "Test 5");
        assertTitle(1, arrayOfarrays, "Test 15");

        files = itm.search("5", folderId, Metadata.DESCRIPTION, Order.DESCENDING, -1, 0, 1);
        assertFalse(itm.getLastResponse().hasError());

        arrayOfarrays = (JSONArray) itm.getLastResponse().getData();

        assertEquals(2, arrayOfarrays.length());
        assertTitle(0, arrayOfarrays, "Test 25");
        assertTitle(1, arrayOfarrays, "Test 15");

        files = itm.search("5", folderId, Metadata.DESCRIPTION, Order.DESCENDING, -1, 1, 2);
        assertFalse(itm.getLastResponse().hasError());

        arrayOfarrays = (JSONArray) itm.getLastResponse().getData();

        assertEquals(2, arrayOfarrays.length());
        assertTitle(0, arrayOfarrays, "Test 15");
        assertTitle(1, arrayOfarrays, "Test 5");

        files = itm.search("5", folderId, Metadata.DESCRIPTION, Order.DESCENDING, -1, 1, 5);
        assertFalse(itm.getLastResponse().hasError());

        arrayOfarrays = (JSONArray) itm.getLastResponse().getData();

        assertEquals(2, arrayOfarrays.length());
        assertTitle(0, arrayOfarrays, "Test 15");
        assertTitle(1, arrayOfarrays, "Test 5");

    }

    @Test
    public void testLimit() throws Exception {
        List<com.openexchange.file.storage.File> files = itm.search("5", folderId, Metadata.DESCRIPTION, Order.ASCENDING, 1);
        assertFalse(itm.getLastResponse().hasError());

        final JSONArray arrayOfarrays = (JSONArray) itm.getLastResponse().getData();
        assertEquals(1, arrayOfarrays.length());
        assertTitle(0, arrayOfarrays, "Test 5");
    }

    @Test
    public void testSort() throws Exception {
        List<com.openexchange.file.storage.File> files = itm.search("5", folderId, Metadata.DESCRIPTION, Order.ASCENDING, -1);
        assertFalse(itm.getLastResponse().hasError());

        JSONArray arrayOfarrays = (JSONArray) itm.getLastResponse().getData();

        assertTitle(0, arrayOfarrays, "Test 5");
        assertTitle(1, arrayOfarrays, "Test 15");
        assertTitle(2, arrayOfarrays, "Test 25");

        files = itm.search("5", folderId, Metadata.DESCRIPTION, Order.DESCENDING, -1);
        assertFalse(itm.getLastResponse().hasError());

        arrayOfarrays = (JSONArray) itm.getLastResponse().getData();

        assertTitle(0, arrayOfarrays, "Test 25");
        assertTitle(1, arrayOfarrays, "Test 15");
        assertTitle(2, arrayOfarrays, "Test 5");

    }

    @Test
    public void testVersions() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        String id = null;
        for(com.openexchange.file.storage.File file: itm.getCreatedEntities()){
            if(file.getTitle().equals(all[1])){
                id=file.getId();
            }
        }
        com.openexchange.file.storage.File org = itm.getAction(id);
        org.setTitle("File");
        itm.updateAction(org, upload, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.TITLE }, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        List<com.openexchange.file.storage.File> found = itm.search("File", folderId);
        assertFalse(itm.getLastResponse().hasError());

        assertTitles(found, "File");

        itm.revert(id);
        assertFalse(itm.getLastResponse().hasError());

        List<com.openexchange.file.storage.File> files = itm.search("1", folderId);
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

        List<com.openexchange.file.storage.File> files = itm.search("\\?", folderId);
        assertFalse(itm.getLastResponse().hasError());

        assertTitles(files, "The mysterious ?");

        file = itm.getAction(id);
        file.setTitle("The * of all trades");
        itm.updateAction(file, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.TITLE }, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        files = itm.search("\\*", folderId);
        assertFalse(itm.getLastResponse().hasError());

        assertTitles(files, "The * of all trades");

    }

    @Test
    public void testPermissions() throws Exception {
        itm.setClient(getClient2());
        List<com.openexchange.file.storage.File> found = itm.search("*", folderId);
        assertEquals(0, found.size());
        assertEquals("IFO-0400", itm.getLastResponse().getException().getErrorCode());
    }

    @Test
    public void testCategories() throws Exception {
        final String id = Iterables.get(itm.getCreatedEntities(), 0).getId();
        com.openexchange.file.storage.File file = itm.getAction(id);
        file.setCategories("[\"curiosity\", \"cat\", \"danger\"]");
        itm.updateAction(file, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.CATEGORIES }, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        List<com.openexchange.file.storage.File> files = itm.search("curiosity", folderId);
        assertFalse(itm.getLastResponse().hasError());

        assertTitles(files, file.getTitle());

    }

    // Node 2652
    @Test
    public void testLastModifiedUTC() throws JSONException, IOException, OXException {
        itm.search("*", folderId);
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
        com.openexchange.file.storage.File tempFile = itm.createFileOnServer(folderId, title, "text/javascript");
        assertTrue(itm.getLastResponse().hasError());
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
            assertTrue(error, titlesSet.remove(entry.getTitle()));
        }
    }

}
