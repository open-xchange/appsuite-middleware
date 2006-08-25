package com.openexchange.ajax.infostore;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.infostore.utils.Metadata;


public class VersionsTest extends InfostoreAJAXTest {

	
	public VersionsTest() {
		super();
	}
	
	public void testVersions() throws Exception{
		File upload = new File(Init.getTestProperty("ajaxPropertiesFile"));
		Response res = update(sessionId,clean.get(0),System.currentTimeMillis(),m(),upload,"text/plain");
		assertNoError(res);
		res = update(sessionId,clean.get(0),System.currentTimeMillis(),m(),upload,"text/plain");
		assertNoError(res);
		res = update(sessionId,clean.get(0),System.currentTimeMillis(),m(),upload,"text/plain");
		assertNoError(res);
		
		res = versions(sessionId,clean.get(0), new int[]{Metadata.VERSION, Metadata.CURRENT_VERSION});
		assertNoError(res);

		assureVersions(new Integer[]{1,2,3},res,3);
	}
	
	public static final void assureVersions(Integer[] ids, Response res, Integer current) throws JSONException{
		Set<Integer> versions = new HashSet<Integer>(Arrays.asList(ids));
		JSONArray arrayOfarrays = (JSONArray) res.getData();
		
		assertEquals(versions.size(), arrayOfarrays.length());
		for(int i = 0; i < arrayOfarrays.length(); i++) {
			JSONArray comp = arrayOfarrays.getJSONArray(i);
			assertTrue(versions.remove(comp.getInt(0)));
			if(current != null && comp.getInt(0) != current) {
				assertFalse(comp.getBoolean(1));
			} else if(current != null){
				assertTrue(comp.getBoolean(1));
			}
		}
		assertTrue(versions.isEmpty());
	}
}
