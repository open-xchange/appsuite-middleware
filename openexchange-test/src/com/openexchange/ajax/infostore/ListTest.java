package com.openexchange.ajax.infostore;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.net.MalformedURLException;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.xml.sax.SAXException;

import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.infostore.utils.Metadata;

public class ListTest extends InfostoreAJAXTest {

	public ListTest(String name) {
		super(name);
	}
	
	public void testBasic() throws Exception {
		int[][] bothEntries = new int[2][2];
		bothEntries[0][1] = clean.get(0);
		bothEntries[1][1] = clean.get(1);
		
		bothEntries[0][0] = folderId;
		bothEntries[1][0] = folderId;
		
		
		checkEntries(bothEntries);
		
		
	}


    public void testSkipsMissingIds() throws Exception {
        int fantasyID = getFantasyID();

        int[][] entries = new int[3][2];
        entries[0][1] = clean.get(0);
        entries[1][1] = fantasyID;
        entries[1][1] = clean.get(1);

        entries[0][0] = folderId;
        entries[1][0] = folderId;
        entries[2][0] = folderId;


        checkEntries(entries);


        
    }

    // Find a non-existing ID 
    public int getFantasyID() throws JSONException, IOException, SAXException {
        int id = 20000;
        Response res = this.get(getWebConversation(), getHostName(), sessionId, id);
        while(!res.getErrorMessage().contains("IFO-0300")) {
            id += 10000;
            res = this.get(getWebConversation(), getHostName(), sessionId, id);
        }
        return id;
    }


    public void checkEntries(int[][] infostore_ids) throws Exception{
        Response res = list(getWebConversation(), getHostName(),sessionId, new int[]{Metadata.ID,Metadata.TITLE, Metadata.DESCRIPTION, Metadata.URL}, infostore_ids);

		assertNoError(res);

		Set<Integer> ids = new HashSet<Integer>(clean);
		Set<String> descriptions = new HashSet<String>(Arrays.asList("test knowledge description", "test url description"));
		Set<String> urls = new HashSet<String>(Arrays.asList("http://www.open-xchange.com"));
		Set<String> titles = new HashSet<String>(Arrays.asList("test knowledge", "test url"));


		JSONArray entries = (JSONArray) res.getData();

		for(int i = 0; i < entries.length(); i++) {
			JSONArray entry = entries.getJSONArray(i);

			assertTrue(ids.remove(entry.getInt(0)));
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
