package com.openexchange.ajax.infostore;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.test.TestInit;


public class VersionsTest extends InfostoreAJAXTest {

	
	public VersionsTest(String name) {
		super(name);
	}
	
	public void testVersions() throws Exception{
		File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
		Response res = update(getWebConversation(),getHostName(),sessionId,clean.get(0),System.currentTimeMillis(),m("version_comment" , "Comment 1"), upload, "text/plain");
		assertNoError(res);
		res = update(getWebConversation(),getHostName(),sessionId,clean.get(0),System.currentTimeMillis(),m("version_comment" , "Comment 2"), upload, "text/plain");
		assertNoError(res);
		res = update(getWebConversation(),getHostName(),sessionId,clean.get(0),System.currentTimeMillis(),m("version_comment" , "Comment 3"), upload, "text/plain");
		assertNoError(res);
		
		res = versions(getWebConversation(),getHostName(), sessionId, clean.get(0), new int[]{Metadata.VERSION, Metadata.CURRENT_VERSION});
		assertNoError(res);

		assureVersions(new Integer[]{1,2,3},res,3);
		
		res = versions(getWebConversation(), getHostName(), sessionId, clean.get(0), new int[]{Metadata.VERSION, Metadata.VERSION_COMMENT});
		assertNoError(res);
		
		Map<Integer, String> comments = new HashMap<Integer, String>();
		comments.put(1,"Comment 1");
		comments.put(2,"Comment 2");
		comments.put(3,"Comment 3");
		
		JSONArray arrayOfarrays = (JSONArray) res.getData();
		
		for(int i = 0; i < arrayOfarrays.length(); i++) {
			JSONArray payload = arrayOfarrays.getJSONArray(i);
			assertEquals(comments.remove(payload.getInt(0)),payload.getString(1));
		}
		
	}
	
	public void testUniqueVersions() throws Exception{
		File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
		Response res = update(getWebConversation(),getHostName(),sessionId,clean.get(0),System.currentTimeMillis(),m("version_comment" , "Comment 1"), upload, "text/plain");
		assertNoError(res);
		res = update(getWebConversation(),getHostName(),sessionId,clean.get(0),System.currentTimeMillis(),m("version_comment" , "Comment 2"), upload, "text/plain");
		assertNoError(res);
		res = update(getWebConversation(),getHostName(),sessionId,clean.get(0),System.currentTimeMillis(),m("version_comment" , "Comment 3"), upload, "text/plain");
		assertNoError(res);
		
		res = versions(getWebConversation(),getHostName(), sessionId, clean.get(0), new int[]{Metadata.VERSION, Metadata.CURRENT_VERSION});
		assertNoError(res);

		assureVersions(new Integer[]{1,2,3},res,3);
		
		int[] nd = detach(getWebConversation(), getHostName(), sessionId, res.getTimestamp().getTime(), clean.get(0), new int[]{3});
		assertEquals(0, nd.length);
		
		res = update(getWebConversation(),getHostName(),sessionId,clean.get(0),System.currentTimeMillis(),m("version_comment" , "Comment 3"), upload, "text/plain");
		assertNoError(res);
		
		res = versions(getWebConversation(),getHostName(), sessionId, clean.get(0), new int[]{Metadata.VERSION, Metadata.CURRENT_VERSION});
		assertNoError(res);

		assureVersions(new Integer[]{1,2,4},res,4);

		
	}
	
	public static final void assureVersions(Integer[] ids, Response res, Integer current) throws JSONException{
		Set<Integer> versions = new HashSet<Integer>(Arrays.asList(ids));
		JSONArray arrayOfarrays = (JSONArray) res.getData();
		
		assertEquals(versions.size(), arrayOfarrays.length());
		for(int i = 0; i < arrayOfarrays.length(); i++) {
			JSONArray comp = arrayOfarrays.getJSONArray(i);
			assertTrue("Didn't expect "+comp.getInt(0), versions.remove(comp.getInt(0)));
			if(current != null && comp.getInt(0) != current) {
				assertFalse(comp.getBoolean(1));
			} else if(current != null){
				assertTrue(comp.getBoolean(1));
			}
		}
		assertTrue(versions.isEmpty());
	}
}
