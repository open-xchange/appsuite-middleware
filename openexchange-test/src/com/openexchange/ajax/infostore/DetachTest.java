package com.openexchange.ajax.infostore;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.infostore.utils.Metadata;

public class DetachTest extends InfostoreAJAXTest {

	public void setUp() throws Exception {
		super.setUp();
		File upload = new File(Init.getTestProperty("ajaxPropertiesFile"));
		Response res = update(sessionId,clean.get(0),System.currentTimeMillis(),m(),upload,"text/plain");
		assertNoError(res);
		res = update(sessionId,clean.get(0),System.currentTimeMillis(),m(),upload,"text/plain");
		assertNoError(res);
		res = update(sessionId,clean.get(0),System.currentTimeMillis(),m(),upload,"text/plain");
		assertNoError(res);
		res = update(sessionId,clean.get(0),System.currentTimeMillis(),m(),upload,"text/plain");
		assertNoError(res);
		res = update(sessionId,clean.get(0),System.currentTimeMillis(),m(),upload,"text/plain");
		assertNoError(res);
	}
	
	public void testBasic() throws Exception {
		int[] notDetached = detach(sessionId, System.currentTimeMillis(), clean.get(0), new int[]{1,2,3,4,5});
		assertEquals(0, notDetached.length);
		
		checkNoVersions();
	}
	
	public void testRevert() throws Exception {
		revert(sessionId,System.currentTimeMillis(), clean.get(0));
		
		checkNoVersions();
	}
	
	public void checkNoVersions() throws Exception {
//		 Version magically reverts to 0
		Response res = get(sessionId, clean.get(0));
		assertNoError(res);
		
		JSONObject obj = (JSONObject) res.getData();
		
		assertEquals(0, obj.getInt("version"));
		
		int[] notDetached = detach(sessionId, System.currentTimeMillis(), clean.get(0), new int[]{1,2,3});
		
		Set<Integer> versions = new HashSet<Integer>(Arrays.asList(new Integer[]{1,2,3}));
		
		assertEquals(versions.size(), notDetached.length);
		for(int id : notDetached) {
			assertTrue(versions.remove(id));
		}
		assertTrue(versions.isEmpty());
		
		res = get(sessionId, clean.get(0));
		assertNoError(res);
		
		obj = (JSONObject) res.getData();
		
		assertEquals("",obj.get("filename"));
		assertEquals("",obj.get("file_mimetype"));
		//assertEquals(0, obj.get("file_size")); FIXME
	}
	
	public void testSpotted() throws Exception {
		int[] notDetached = detach(sessionId, System.currentTimeMillis(), clean.get(0), new int[]{1,3,5});
		assertEquals(0, notDetached.length);
		
		Response res = versions(sessionId,clean.get(0),new int[]{Metadata.VERSION,Metadata.CURRENT_VERSION});
		assertNoError(res);
		// Current Version reverts to 4 (being the newest available version
		VersionsTest.assureVersions(new Integer[]{2,4},res,4);
		
		res = get(sessionId,clean.get(0));
		assertNoError(res);
		
		JSONObject obj = (JSONObject)res.getData();
		
		assertEquals(4,obj.getInt("version"));
	}
	
	public void testDetachVersion0() throws Exception {
		int[] notDetached = detach(sessionId, System.currentTimeMillis(), clean.get(0), new int[]{0});
		assertEquals(1, notDetached.length);
		assertEquals(0,notDetached[0]);
	}
}
