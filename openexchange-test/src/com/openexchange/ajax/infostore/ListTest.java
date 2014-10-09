package com.openexchange.ajax.infostore;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.test.TestInit;

public class ListTest extends InfostoreAJAXTest {

	public ListTest(final String name) {
		super(name);
	}

	public void testBasic() throws Exception {
		final String[][] bothEntries = new String[2][2];
		bothEntries[0][1] = clean.get(0);
		bothEntries[1][1] = clean.get(1);

		bothEntries[0][0] = String.valueOf(folderId);
		bothEntries[1][0] = String.valueOf(folderId);


		checkEntries(bothEntries);


	}


    public void testSkipsMissingIds() throws Exception {
        final String fantasyID = getFantasyID();

        final String[][] entries = new String[4][2];
        entries[0][1] = clean.get(0);
        entries[1][1] = String.valueOf(fantasyID);
        entries[2][1] = clean.get(1);
        entries[3][1] = clean.get(1);

        entries[0][0] = String.valueOf(folderId);
        entries[1][0] = String.valueOf(folderId);
        entries[2][0] = String.valueOf(folderId);
        entries[3][0] = fantasyID;


        checkEntries(entries);



    }

    // Node 2652
    public void testLastModifiedUTC() throws JSONException, IOException, SAXException {
        final String[][] bothEntries = new String[2][2];
		bothEntries[0][1] = clean.get(0);
		bothEntries[1][1] = clean.get(1);

		bothEntries[0][0] = String.valueOf(folderId);
		bothEntries[1][0] = String.valueOf(folderId);

        final Response res = list(getWebConversation(), getHostName(),sessionId, new int[]{Metadata.LAST_MODIFIED_UTC}, bothEntries);

        assertNoError(res);

        final JSONArray arr = (JSONArray) res.getData();
        final int size = arr.length();
        assertTrue(size > 0);

        for(int i = 0; i < size; i++) {
            final JSONArray row = arr.optJSONArray(i);
            assertTrue(row.length() == 1);
            assertNotNull(row.optLong(0));
        }

    }
    // Bug 12427

    public void testNumberOfVersions() throws JSONException, IOException, SAXException {
        final String[][] entries = new String[1][2];
        entries[0][0] = String.valueOf(folderId);
        entries[0][1] = clean.get(0);

        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        Response res = update(getWebConversation(),getHostName(),sessionId,clean.get(0),Long.MAX_VALUE,m("description","New description"), upload, "text/plain");
        assertNoError(res);

        res = list(getWebConversation(), getHostName(),sessionId, new int[]{Metadata.ID , Metadata.NUMBER_OF_VERSIONS}, entries);

        JSONArray rows = (JSONArray) res.getData();
        boolean found = false;
        for(int i = 0, size = rows.length(); i < size; i++) {
            JSONArray row = rows.getJSONArray(i);
            String id = row.getString(0);
            int numberOfVersions = row.getInt(1);

            if (id.equals(clean.get(0))) {
                assertEquals(1, numberOfVersions);
                found = true;
            }
        }
        assertTrue(found);
    }

    // Find a non-existing ID
    public String getFantasyID() throws JSONException, IOException, SAXException {
        String id = "20000";
        Response res = this.get(getWebConversation(), getHostName(), sessionId, id);
        while(!(res.getErrorMessage().contains("IFO-0300") || res.getErrorMessage().contains("IFO-0438"))) {
            id += 10000;
            res = this.get(getWebConversation(), getHostName(), sessionId, id);
        }
        return id;
    }


    public void checkEntries(final String[][] infostore_ids) throws Exception{
        final Response res = list(getWebConversation(), getHostName(),sessionId, new int[]{Metadata.ID,Metadata.TITLE, Metadata.DESCRIPTION, Metadata.URL}, infostore_ids);

		assertNoError(res);

		final Set<String> ids = new HashSet<String>(clean);
		final Set<String> descriptions = new HashSet<String>(Arrays.asList("test knowledge description", "test url description"));
		final Set<String> urls = new HashSet<String>(Arrays.asList("http://www.open-xchange.com"));
		final Set<String> titles = new HashSet<String>(Arrays.asList("test knowledge", "test url"));


		final JSONArray entries = (JSONArray) res.getData();

		for(int i = 0; i < entries.length(); i++) {
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
