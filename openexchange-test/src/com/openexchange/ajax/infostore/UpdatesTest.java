package com.openexchange.ajax.infostore;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;

import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.test.TestInit;

public class UpdatesTest extends InfostoreAJAXTest{

	public UpdatesTest(String name) {
		super(name);
	}
	
	public void testBasic() throws Exception{
		Response res = all(getWebConversation(),getHostName(),sessionId, folderId, new int[]{Metadata.ID});
		assertNoError(res);
		long ts = res.getTimestamp().getTime()+2;
		
		this.update(getWebConversation(), getHostName(),sessionId,clean.get(0), ts, m(
				"title" , "test knowledge updated"
		));
		
		res = updates(getWebConversation(),getHostName(),sessionId,folderId, new int[]{Metadata.TITLE, Metadata.DESCRIPTION}, ts);
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
		
		res = updates(getWebConversation(),getHostName(),sessionId,folderId, new int[]{Metadata.TITLE,Metadata.DESCRIPTION}, ts);
		
		assertNoError(res);
		
		modAndDel = (JSONArray) res.getData();
		
		assertEquals(2,modAndDel.length());
		
		for(int i = 0; i < 2; i++) {
			assertTrue(ids.remove(modAndDel.getInt(i)));
		}
		
		assertTrue(ids.isEmpty());
	}
	
	public void testRemovedVersionForcesUpdate() throws Exception{
		Response res = all(getWebConversation(),getHostName(),sessionId, folderId, new int[]{Metadata.ID});
		assertNoError(res);
		long ts = res.getTimestamp().getTime()+2;
		
		File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
		res = update(getWebConversation(),getHostName(),sessionId,clean.get(0),System.currentTimeMillis(),m("version_comment" , "Comment 1"), upload, "text/plain");
		assertNoError(res);
		res = update(getWebConversation(),getHostName(),sessionId,clean.get(0),System.currentTimeMillis(),m("version_comment" , "Comment 2"), upload, "text/plain");
		assertNoError(res);
		res = update(getWebConversation(),getHostName(),sessionId,clean.get(0),System.currentTimeMillis(),m("version_comment" , "Comment 3"), upload, "text/plain");
		assertNoError(res);
		
		int[] nd = detach(getWebConversation(), getHostName(), sessionId, ts, clean.get(0), new int[]{3});
		assertEquals(0, nd.length);
		
		res = updates(getWebConversation(),getHostName(),sessionId,folderId, new int[]{Metadata.TITLE, Metadata.DESCRIPTION}, ts);
		assertNoError(res);
		
		JSONArray modAndDel = (JSONArray) res.getData();
		
		
		assertEquals(1, modAndDel.length());
		
	}

	//Bug 4269
	public void testVirtualFolder() throws Exception {
		Response res = updates(getWebConversation(), getHostName(), sessionId, FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID, new int[]{Metadata.ID}, 0);
		assertNoError(res);
		JSONArray modAndDel = (JSONArray) res.getData();
		assertEquals(0, modAndDel.length());
	}
}
