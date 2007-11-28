package com.openexchange.ajax.infostore;

import java.io.File;

import org.json.JSONObject;

import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.test.TestInit;


public class GetTest extends InfostoreAJAXTest {

	public GetTest(String name) {
		super(name);
	}
	
	public void testBasic() throws Exception{
		
		Response res = this.get(getWebConversation(), getHostName(), sessionId, clean.get(0));
		
		assertNoError(res);
		
		JSONObject obj = (JSONObject) res.getData();
		
		assertEquals("test knowledge",obj.getString("title"));
		assertEquals("test knowledge description",obj.getString("description"));
		
	}
	
	public void getVersion() throws Exception {
		File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
		Response res = update(getWebConversation(),getHostName(),sessionId,clean.get(0),System.currentTimeMillis(),m("description","New description"), upload, "text/plain");
		assertNoError(res);
		
		res = this.get(getWebConversation(), getHostName(), sessionId, clean.get(0),0);
		
		assertNoError(res);
		
		JSONObject obj = (JSONObject) res.getData();
		
		assertEquals("test knowledge",obj.getString("title"));
		assertEquals("test knowledge description",obj.getString("description"));
		
		res = this.get(getWebConversation(), getHostName(), sessionId, clean.get(0),1);
		
		assertNoError(res);
		
		obj = (JSONObject) res.getData();
		
		assertEquals("test knowledge description",obj.getString("New description"));
		
	}

}
