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

public class AllTest extends InfostoreAJAXTest {

	public AllTest(final String name) {
		super(name);
	}

	public void testBasic() throws Exception{

		final Response res = this.all(getWebConversation(),getHostName(),sessionId, folderId, new int[]{Metadata.ID, Metadata.TITLE, Metadata.DESCRIPTION, Metadata.URL, Metadata.FOLDER_ID});

		final Set<String> descriptions = new HashSet<String>(Arrays.asList("test knowledge description", "test url description"));
		final Set<String> urls = new HashSet<String>(Arrays.asList("http://www.open-xchange.com"));
		final Set<String> titles = new HashSet<String>(Arrays.asList("test knowledge", "test url"));

		assertNoError(res);

		final JSONArray entries = (JSONArray) res.getData();

		for(int i = 0; i < entries.length(); i++) {
			final JSONArray entry = entries.getJSONArray(i);

			assertTrue(titles.remove(entry.getString(1)));
			assertTrue(descriptions.remove(entry.getString(2)));
			urls.remove(entry.getString(3));
		}

		assertTrue(descriptions.isEmpty());
		assertTrue(urls.isEmpty());
		assertTrue(titles.isEmpty());

	}

	//Bug 4269
	public void testVirtualFolder() throws Exception{

        for(int folderId : virtualFolders) {
            virtualFolderTest( folderId );
        }
	}

    public void virtualFolderTest(int folderid) throws JSONException, IOException, SAXException {
        final Response res = all(getWebConversation(), getHostName(), sessionId, folderid, new int[]{Metadata.ID});
		assertNoError(res);
		assertEquals(0, ((JSONArray) res.getData()).length());
    }

    // Node 2652
    public void testLastModifiedUTC() throws Exception {
        final Response res = all(getWebConversation(), getHostName(), sessionId, folderId, new int[] {Metadata.LAST_MODIFIED_UTC});
        assertNoError(res);

        final JSONArray entries = (JSONArray) res.getData();
		final int size = entries.length();
        assertTrue(size > 0);

        for(int i = 0; i < size; i++) {
			final JSONArray entry = entries.getJSONArray(i);
			assertNotNull(entry.get(0));
		}
    }

    // Bug 12427

    public void testNumberOfVersions() throws JSONException, IOException, SAXException {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        Response res = update(getWebConversation(),getHostName(),sessionId,clean.get(0),Long.MAX_VALUE,m("description","New description"), upload, "text/plain");
        assertNoError(res);

        res = all(getWebConversation(), getHostName(), sessionId, folderId, new int[]{Metadata.ID, Metadata.NUMBER_OF_VERSIONS});

        JSONArray rows = (JSONArray) res.getData();
        boolean found = false;
        for(int i = 0, size = rows.length(); i < size; i++) {
            JSONArray row = rows.getJSONArray(i);
            String id = row.getString(0);
            int numberOfVersions = row.getInt(1);

            if(id.equals(clean.get(0))) {
                assertEquals(1, numberOfVersions);
                found = true;
            }
        }
        assertTrue(found);
    }
}
