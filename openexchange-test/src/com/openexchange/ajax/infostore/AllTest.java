package com.openexchange.ajax.infostore;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;

import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.utils.Metadata;

public class AllTest extends InfostoreAJAXTest {

	public AllTest(String name) {
		super(name);
	}
		
	public void testBasic() throws Exception{
		
		Response res = this.all(getWebConversation(),getHostName(),sessionId, folderId, new int[]{Metadata.ID, Metadata.TITLE, Metadata.DESCRIPTION, Metadata.URL, Metadata.FOLDER_ID});
		
		Set<String> descriptions = new HashSet<String>(Arrays.asList("test knowledge description", "test url description"));
		Set<String> urls = new HashSet<String>(Arrays.asList("http://www.open-xchange.com"));
		Set<String> titles = new HashSet<String>(Arrays.asList("test knowledge", "test url"));
		
		assertNoError(res);
		
		JSONArray entries = (JSONArray) res.getData();
		
		for(int i = 0; i < entries.length(); i++) {
			JSONArray entry = entries.getJSONArray(i);
			
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
		Response res = all(getWebConversation(), getHostName(), sessionId, FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID, new int[]{Metadata.ID});
		assertNoError(res);
		assertEquals(0, ((JSONArray) res.getData()).length());
	}
}
