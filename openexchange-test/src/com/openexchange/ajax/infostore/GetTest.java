package com.openexchange.ajax.infostore;

import org.json.JSONObject;

import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;


public class GetTest extends InfostoreAJAXTest {

	public GetTest(String name) {
		super(name);
	}
	
	public void testBasic() throws Exception{
		
		Response res = this.get(getWebConversation(), sessionId, clean.get(0));
		
		assertNoError(res);
		
		JSONObject obj = (JSONObject) res.getData();
		
		assertEquals("test knowledge",obj.getString("title"));
		assertEquals("test knowledge description",obj.getString("description"));
		
	}

}
