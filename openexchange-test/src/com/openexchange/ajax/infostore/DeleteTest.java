package com.openexchange.ajax.infostore;

import org.json.JSONArray;

import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.types.Response;
import com.openexchange.groupware.infostore.utils.Metadata;

public class DeleteTest extends InfostoreAJAXTest {

	public DeleteTest() {
		super();
	}
	
	public void testBasic() throws Exception{
		tearDown();
		
		Response res = this.all(sessionId,folderId,new int[]{Metadata.ID});
		
		assertNoError(res);
		
		JSONArray a = (JSONArray) res.getData();
		
		assertEquals(0, a.length());
		
		clean.clear();
	}

}
