package com.openexchange.ajax.infostore;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;

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
		
		res = versions(sessionId,clean.get(0), new int[]{Metadata.VERSION});
		assertNoError(res);

		Set<Integer> versions = new HashSet<Integer>(Arrays.asList(new Integer[]{0,1,2,3}));
		JSONArray arrayOfarrays = (JSONArray) res.getData();
		
		for(int i = 0; i < arrayOfarrays.length(); i++) {
			JSONArray comp = arrayOfarrays.getJSONArray(i);
			assertTrue(versions.remove(comp.getInt(0)));
		}
		assertTrue(versions.isEmpty());
	}
}
