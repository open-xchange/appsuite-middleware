
package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.test.TestInit;

public class SearchTest extends InfostoreAJAXTest {

    protected String[] all = null;
    private static final int[] COLS = new int[] { Metadata.TITLE, Metadata.DESCRIPTION };

    public SearchTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        all = new String[26];

        final char[] alphabet = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

        for (int i = 0; i < 26; i++) {
            com.openexchange.file.storage.File createdFile = createFileOnServer(folderId, "Test " + i, "text/javascript");
            all[i] = "Test " + i;
            clean.add(createdFile.getId());
        }
    }

    @Test
    public void testBasic() throws Exception {
        final Response res = search(getWebConversation(), getHostName(), sessionId, "5", COLS, folderId, -1, null, -1, -1);
        assertNoError(res);

        assertTitles(res, "Test 5", "Test 15", "Test 25");

    }

    @Test
    public void testPattern() throws Exception {
        Response res = search(getWebConversation(), getHostName(), sessionId, "*", COLS, folderId);
        assertNoError(res);
        assertTitles(res, all);

        res = search(getWebConversation(), getHostName(), sessionId, "Test ?5", COLS, folderId);
        assertNoError(res);
        assertTitles(res, "Test 15", "Test 25");

    }

    @Test
    public void testAll() throws Exception {
        final Response res = search(getWebConversation(), getHostName(), sessionId, "", COLS, folderId);
        assertNoError(res);
        assertTitles(res, all);
    }

    @Test
    public void testCaseInsensitive() throws Exception {
        final Response res = search(getWebConversation(), getHostName(), sessionId, "test", COLS, folderId);
        assertNoError(res);
        assertTitles(res, all);
    }

    @Test
    public void testStartAndStop() throws Exception {
        Response res = search(getWebConversation(), getHostName(), sessionId, "5", COLS, folderId, Metadata.DESCRIPTION, "ASC", 0, 1);
        assertNoError(res);

        JSONArray arrayOfarrays = (JSONArray) res.getData();
        assertEquals(2, arrayOfarrays.length());
        assertTitle(0, arrayOfarrays, "Test 5");
        assertTitle(1, arrayOfarrays, "Test 15");

        res = search(getWebConversation(), getHostName(), sessionId, "5", COLS, folderId, Metadata.DESCRIPTION, "DESC", 0, 1);
        assertNoError(res);

        arrayOfarrays = (JSONArray) res.getData();

        assertEquals(2, arrayOfarrays.length());
        assertTitle(0, arrayOfarrays, "Test 25");
        assertTitle(1, arrayOfarrays, "Test 15");

        res = search(getWebConversation(), getHostName(), sessionId, "5", COLS, folderId, Metadata.DESCRIPTION, "DESC", 1, 2);
        assertNoError(res);

        arrayOfarrays = (JSONArray) res.getData();

        assertEquals(2, arrayOfarrays.length());
        assertTitle(0, arrayOfarrays, "Test 15");
        assertTitle(1, arrayOfarrays, "Test 5");

        res = search(getWebConversation(), getHostName(), sessionId, "5", COLS, folderId, Metadata.DESCRIPTION, "DESC", 1, 5);
        assertNoError(res);

        arrayOfarrays = (JSONArray) res.getData();

        assertEquals(2, arrayOfarrays.length());
        assertTitle(0, arrayOfarrays, "Test 15");
        assertTitle(1, arrayOfarrays, "Test 5");

    }

    @Test
    public void testLimit() throws Exception {
        final Response res = search(getWebConversation(), getHostName(), sessionId, "5", COLS, folderId, Metadata.DESCRIPTION, "ASC", 1);
        assertNoError(res);

        final JSONArray arrayOfarrays = (JSONArray) res.getData();
        assertEquals(1, arrayOfarrays.length());
        assertTitle(0, arrayOfarrays, "Test 5");

    }

    @Test
    public void testSort() throws Exception {
        Response res = search(getWebConversation(), getHostName(), sessionId, "5", COLS, folderId, Metadata.DESCRIPTION, "ASC", -1, -1);
        assertNoError(res);

        JSONArray arrayOfarrays = (JSONArray) res.getData();

        assertTitle(0, arrayOfarrays, "Test 5");
        assertTitle(1, arrayOfarrays, "Test 15");
        assertTitle(2, arrayOfarrays, "Test 25");

        res = search(getWebConversation(), getHostName(), sessionId, "5", COLS, folderId, Metadata.DESCRIPTION, "DESC", -1, -1);
        assertNoError(res);

        arrayOfarrays = (JSONArray) res.getData();

        assertTitle(0, arrayOfarrays, "Test 25");
        assertTitle(1, arrayOfarrays, "Test 15");
        assertTitle(2, arrayOfarrays, "Test 5");

    }

    @Test
    public void testVersions() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        final String id = clean.get(0);

        Response res = update(getWebConversation(), getHostName(), sessionId, id, Long.MAX_VALUE, m("title", "File"), upload, "text/plain");
        assertNoError(res);

        res = search(getWebConversation(), getHostName(), sessionId, "File", COLS, folderId);
        assertNoError(res);

        assertTitles(res, "File");

        res = revert(getWebConversation(), getHostName(), sessionId, res.getTimestamp().getTime(), id);
        assertNoError(res);

        res = search(getWebConversation(), getHostName(), sessionId, "1", COLS, folderId);
        assertNoError(res);

        assertTitles(res, "Test 1", "Test 10", "Test 11", "Test 12", "Test 13", "Test 14", "Test 15", "Test 16", "Test 17", "Test 18", "Test 19", "Test 21");
    }

    //FIXME MS re-add
//    // Tests functionality that no one requested yet
//    public void notestEscape() throws Exception {
//        final String id = clean.get(0);
//        com.openexchange.file.storage.File file = itm.getAction(id);
//        file.setTitle("The mysterious ?");
//        UpdateInfostoreResponse result = update(file, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.TITLE }, Long.MAX_VALUE);
//        assertFalse(result.hasError());
//
//        res = search(getWebConversation(), getHostName(), sessionId, "\\?", COLS, folderId);
//        assertNoError(res);
//
//        assertTitles(res, "The mysterious ?");
//
//        file = itm.getAction(id);
//        file.setTitle("The * of all trades");
//        result = update(file, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.TITLE }, Long.MAX_VALUE);
//        assertFalse(result.hasError());
//
//        res = search(getWebConversation(), getHostName(), sessionId, "\\*", COLS, folderId);
//        assertNoError(res);
//
//        assertTitles(res, "The * of all trades");
//
//    }

    @Test
    public void testPermissions() throws Exception {
        final String sessionId2 = this.getSecondSessionId();
        final Response res = search(getSecondWebConversation(), getHostName(), sessionId2, "*", COLS, folderId);
        assertEquals("IFO-0400", res.getException().getErrorCode());
    }

    //FIXME MS re-add
//    @Test
//    public void testCategories() throws Exception {
//        final String id = clean.get(0);
//        com.openexchange.file.storage.File file = itm.getAction(id);
//        file.setCategories("[\"curiosity\", \"cat\", \"danger\"]");
//        UpdateInfostoreResponse result = update(file, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.CATEGORIES }, Long.MAX_VALUE);
//        assertFalse(result.hasError());
//
//        res = search(getWebConversation(), getHostName(), sessionId, "curiosity", COLS);
//        assertNoError(res);
//
//        assertTitles(res, "Test 0");
//
//    }

    // Node 2652
    @Test
    public void testLastModifiedUTC() throws JSONException, IOException, SAXException {
        final Response res = search(getWebConversation(), getHostName(), sessionId, "*", new int[] { Metadata.LAST_MODIFIED_UTC }, folderId);
        assertNoError(res);
        final JSONArray results = (JSONArray) res.getData();
        final int size = results.length();
        assertTrue(size > 0);

        for (int i = 0; i < size; i++) {
            final JSONArray row = results.optJSONArray(i);
            assertNotNull(row);
            assertTrue(row.length() > 0);
            assertNotNull(row.optLong(0));
        }
    }

    // Bug 12427

    public void notestNumberOfVersions() throws JSONException, IOException, SAXException, OXException {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        Response res;
        com.openexchange.file.storage.File toUpdate = itm.getAction(clean.get(0));
        for (int i = 0; i < clean.size(); i++) {
            itm.updateAction(toUpdate, new com.openexchange.file.storage.File.Field[] { }, new Date(Long.MAX_VALUE));
        }

        res = search(getWebConversation(), getHostName(), sessionId, "*", new int[] { Metadata.ID, Metadata.NUMBER_OF_VERSIONS }, folderId);

        final JSONArray rows = (JSONArray) res.getData();
        for (int i = 0, size = rows.length(); i < size; i++) {
            final JSONArray row = rows.getJSONArray(i);
            row.getInt(0);
            final int numberOfVersions = row.getInt(1);

            assertEquals(1, numberOfVersions);

        }
    }

    // Bug 18124
    @Test
    public void testBackslashFound() throws Exception {
        String title = "Test\\WithBackslash";
        com.openexchange.file.storage.File createdFile = createFileOnServer(folderId, title, "text/javascript");
        clean.add(createdFile.getId());

        Response res = search(getWebConversation(), getHostName(), sessionId, title, new int[] { Metadata.TITLE, Metadata.ID }, folderId);

        assertTitles(res, title);

    }

    public static void assertTitle(final int index, final JSONArray results, final String title) throws JSONException {
        final JSONArray entry = results.getJSONArray(index);
        assertEquals(title, entry.getString(0));
    }

    public static void assertTitles(final Response res, final String... titles) throws JSONException {
        final JSONArray arrayOfarrays = (JSONArray) res.getData();
        final Set<String> titlesSet = new HashSet<String>(Arrays.asList(titles));

        final String error = "Expected: " + titlesSet + " but got " + arrayOfarrays;
        assertEquals(error, titles.length, arrayOfarrays.length());
        for (int i = 0; i < arrayOfarrays.length(); i++) {
            final JSONArray entry = arrayOfarrays.getJSONArray(i);
            assertTrue(error, titlesSet.remove(entry.getString(0)));
        }
    }

}
