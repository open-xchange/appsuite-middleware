package com.openexchange.ajax.infostore;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;

import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.infostore.utils.Metadata;

public class UpdatesTest extends InfostoreAJAXTest{

	public UpdatesTest() {
		super();
	}
	
	public void testBasic() throws Exception{
		Response res = all(sessionId,folderId,new int[]{Metadata.ID});
		assertNoError(res);
		long ts = res.getTimestamp().getTime()+2;
		
		this.update(sessionId, clean.get(0),ts,m(
				"title" , "test knowledge updated"
		));
		
		res = updates(sessionId,folderId,new int[]{Metadata.TITLE, Metadata.DESCRIPTION},ts);
		assertNoError(res);
		
		JSONArray modAndDel = (JSONArray) res.getData();
		
		
		assertEquals(1, modAndDel.length());
		
		JSONArray fields = modAndDel.getJSONArray(0);
		
		assertEquals("test knowledge updated", fields.getString(0));
		assertEquals("test knowledge description", fields.getString(1));
		
		Set<Integer> ids = new HashSet<Integer>(clean);
		
		ts = res.getTimestamp().getTime();
		
		tearDown();
		clean.clear();
		
		res = updates(sessionId,folderId,new int[]{Metadata.TITLE,Metadata.DESCRIPTION},ts);
		
		assertNoError(res);
		
		modAndDel = (JSONArray) res.getData();
		
		assertEquals(2,modAndDel.length());
		
		for(int i = 0; i < 2; i++) {
			assertTrue(ids.remove(modAndDel.getInt(i)));
		}
		
		assertTrue(ids.isEmpty());
	}

}
