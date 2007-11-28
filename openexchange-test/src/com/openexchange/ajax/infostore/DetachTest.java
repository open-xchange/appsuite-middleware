package com.openexchange.ajax.infostore;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;

import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.test.TestInit;

public class DetachTest extends InfostoreAJAXTest {

	public DetachTest(String name){
		super(name);
	}
	
	public void setUp() throws Exception {
		super.setUp();
		File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
		Response res = update(getWebConversation(),getHostName(),sessionId,clean.get(0),System.currentTimeMillis(),m(), upload, "text/plain");
		assertNoError(res);
		res = update(getWebConversation(),getHostName(),sessionId,clean.get(0),System.currentTimeMillis(),m(), upload, "text/plain");
		assertNoError(res);
		res = update(getWebConversation(),getHostName(),sessionId,clean.get(0),System.currentTimeMillis(),m(), upload, "text/plain");
		assertNoError(res);
		res = update(getWebConversation(),getHostName(),sessionId,clean.get(0),System.currentTimeMillis(),m(), upload, "text/plain");
		assertNoError(res);
		res = update(getWebConversation(),getHostName(),sessionId,clean.get(0),System.currentTimeMillis(),m(), upload, "text/plain");
		assertNoError(res);
	}
	
	public void testBasic() throws Exception {
		int[] notDetached = detach(getWebConversation(), getHostName(), sessionId, System.currentTimeMillis(), clean.get(0), new int[]{1,2,3,4,5});
		assertEquals(0, notDetached.length);
		
		checkNoVersions();
	}
	
	public void testRevert() throws Exception {
		Response res = revert(getWebConversation(),getHostName(), sessionId, System.currentTimeMillis(), clean.get(0));
		assertNoError(res);
		checkNoVersions();
	}
	
	public void checkNoVersions() throws Exception {
//		 Version magically reverts to 0
		Response res = get(getWebConversation(), getHostName(), sessionId, clean.get(0));
		assertNoError(res);
		
		JSONObject obj = (JSONObject) res.getData();
		
		assertEquals(0, obj.getInt("version"));
		
		int[] notDetached = detach(getWebConversation(), getHostName(), sessionId, System.currentTimeMillis(), clean.get(0), new int[]{1,2,3});
		
		Set<Integer> versions = new HashSet<Integer>(Arrays.asList(new Integer[]{1,2,3}));
		
		assertEquals(versions.size(), notDetached.length);
		for(int id : notDetached) {
			assertTrue(versions.remove(id));
		}
		assertTrue(versions.isEmpty());
		
		res = get(getWebConversation(), getHostName(), sessionId, clean.get(0));
		assertNoError(res);
		
		obj = (JSONObject) res.getData();
		
		assertEquals("",obj.get("filename"));
		assertEquals("",obj.get("file_mimetype"));
		//assertEquals(0, obj.get("file_size")); FIXME
	}
	
	public void testSpotted() throws Exception {
		int[] notDetached = detach(getWebConversation(), getHostName(), sessionId, System.currentTimeMillis(), clean.get(0), new int[]{1,3,5});
		assertEquals(0, notDetached.length);
		
		Response res = versions(getWebConversation(),getHostName(),sessionId, clean.get(0), new int[]{Metadata.VERSION,Metadata.CURRENT_VERSION});
		assertNoError(res);
		// Current Version reverts to 4 (being the newest available version
		VersionsTest.assureVersions(new Integer[]{2,4},res,4);
		
		res = get(getWebConversation(),getHostName(), sessionId, clean.get(0));
		assertNoError(res);
		
		JSONObject obj = (JSONObject)res.getData();
		
		assertEquals(4,obj.getInt("version"));
	}
	
	public void testDetachVersion0() throws Exception {
		int[] notDetached = detach(getWebConversation(), getHostName(), sessionId, System.currentTimeMillis(), clean.get(0), new int[]{0});
		assertEquals(1, notDetached.length);
		assertEquals(0,notDetached[0]);
	}
	
	// Bug 3818
	public void testCopyComments() throws Exception{
		Response res = update(getWebConversation(), getHostName(), sessionId, clean.get(0), Long.MAX_VALUE, m("description","current_description"));
		assertNoError(res);
		
		int[] notDetached = detach(getWebConversation(), getHostName(), sessionId, Long.MAX_VALUE, clean.get(0), new int[]{5,4,3});
		assertEquals(0, notDetached.length);
		
		res = get(getWebConversation(), getHostName(),sessionId, clean.get(0), 0);
		assertNoError(res);
		
		JSONObject document = (JSONObject) res.getData();
		assertEquals("current_description", document.get("description"));
	}
	
//	Bug 4120
	public void testUniqueFilenames() throws Exception {
		File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
		
		int id = clean.get(0);
		
		
		Response res = update(getWebConversation(),getHostName(),sessionId,id,System.currentTimeMillis(),m("filename" , "blupp.properties"));
		assertNoError(res);
		
		int id2 = createNew(getWebConversation(), getHostName(), sessionId, m("title" , "otherFile", "description","other_desc", "folder_id" ,	((Integer)folderId).toString()), upload, "text/plain");
		clean.add(id2);
		
		try {
			int[] nd = detach(getWebConversation(), getHostName(), sessionId, Long.MAX_VALUE, clean.get(0), new int[]{5});
			fail("Expected Exception.");
		} catch (IOException x) {
			assertTrue(true);
		}
	}
}
