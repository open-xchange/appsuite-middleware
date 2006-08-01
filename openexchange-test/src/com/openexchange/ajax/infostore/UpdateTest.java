package com.openexchange.ajax.infostore;

import org.json.JSONObject;

import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.types.Response;
import com.openexchange.groupware.infostore.ajax.ResponseWithTimestamp;


public class UpdateTest extends InfostoreAJAXTest {

	public UpdateTest() {
		super();
	}
	
	public void testBasic() throws Exception{
		this.update(sessionId, clean.get(0),System.currentTimeMillis(),m(
				"title" , "test knowledge updated"
		));
		
		Response res = get(sessionId, clean.get(0));
		assertNoError(res);
		
		JSONObject object = (JSONObject) res.getData();
		
		
		assertEquals("test knowledge updated", object.getString("title"));
		assertEquals("test knowledge description", object.getString("description"));
		assertEquals(0, object.getInt("version"));
		
	}

}
