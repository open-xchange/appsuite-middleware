package com.openexchange.ajax.infostore;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.httpclient.HttpException;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.FolderTest;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.utils.Metadata;

public class LockTest extends InfostoreAJAXTest {
	
	protected File testFile;
	
	public void setUp() throws Exception{
		testFile = new File(Init.getTestProperty("ajaxPropertiesFile"));
		sessionId = getSessionId();
		// Copied-without-thinking from FolderTest
		FolderObject myInfostore = FolderTest.getMyInfostoreFolder(getWebConversation(), getHostName(), getSessionId());
		folderId = FolderTest.insertFolder(getWebConversation(), getHostName(), getSessionId(), getLogin(), false,
			myInfostore.getObjectID(), "NewInfostoreFolder", "infostore", FolderObject.PUBLIC, null, true);
		FolderTest.updateFolder(getWebConversation(),getHostName(),sessionId,getLogin(),getSeconduser(),folderId,System.currentTimeMillis(),false);
		//folderId=229;
		Map<String,String> create = m(
				"folder_id" 		,	((Integer)folderId).toString(),
				"title"  		,  	"test knowledge",
				"description" 	, 	"test knowledge description"
		);
		
		int c = this.createNew(sessionId,create,testFile,"text/plain");
		
		clean.add(c);
		
		Response res = this.update(sessionId,c,System.currentTimeMillis(),m(),testFile,"text/plain");
		assertNoError(res);
		
		res = this.update(sessionId,c,System.currentTimeMillis(),m(),testFile,"text/plain");
		assertNoError(res);
		
		res = this.update(sessionId,c,System.currentTimeMillis(),m(),testFile,"text/plain");
		assertNoError(res);
		
		res = this.update(sessionId,c,System.currentTimeMillis(),m(),testFile,"text/plain");
		assertNoError(res);
		
	}
	
	public void tearDown() throws Exception{
		super.tearDown();
		
		FolderTest.deleteFolders(getWebConversation(),getHostName(),sessionId,new int[]{folderId},System.currentTimeMillis(),false);
	}
	
	public void testLock() throws Exception{
		Response res = lock(sessionId,clean.get(0));
		assertNoError(res);
		
		res = get(sessionId,clean.get(0));
		assertNoError(res);
		assertLocked((JSONObject)res.getData());
		
		String sessionId2 = this.getSecondSessionId();
		
		// Object may not be modified
		res = update(sessionId2,clean.get(0),System.currentTimeMillis(), m("title" , "Hallo"));
		assertTrue(res.hasError());
		
		
		// Object may not be removed
		int[] notDeleted = delete(sessionId2,System.currentTimeMillis(),new int[][]{{folderId, clean.get(0)}});
		assertEquals(1, notDeleted.length);
		assertEquals(clean.get(0),(Object) notDeleted[0]);
		
		// Versions may not be removed
		int[] notDetached = detach(sessionId2,System.currentTimeMillis(),clean.get(0),new int[]{4});
		assertEquals(1,notDetached.length);
		assertEquals(4,notDetached[0]);
		
		// Object may not be locked
		res = lock(sessionId2,clean.get(0));
		assertTrue(res.hasError());
		
		
		// Object may not be unlocked
		
		res = unlock(sessionId2,clean.get(0));
		assertTrue(res.hasError());
		
		// Lock owner may update
		
		res = update(sessionId,clean.get(0),System.currentTimeMillis(), m("title" , "Hallo"));
		assertNoError(res);
		
		res = get(sessionId, clean.get(0));
		JSONObject o = (JSONObject) res.getData();
		
		assertEquals("Hallo",o.get("title"));
		
		//Lock owner may detach
		notDetached = detach(sessionId,System.currentTimeMillis(),clean.get(0),new int[]{4});
		assertEquals(0,notDetached.length);
		
		//Lock owner may remove
		notDeleted = delete(sessionId,System.currentTimeMillis(),new int[][]{{folderId, clean.get(0)}});
		assertEquals(0, notDeleted.length);
		clean.remove(0);
		
	}
	
	public void testUnlock() throws Exception{
		Response res = lock(sessionId,clean.get(0));
		assertNoError(res);
		
		res = get(sessionId,clean.get(0));
		assertNoError(res);
		assertLocked((JSONObject)res.getData());
		
		// Lock owner may relock
		res = lock(sessionId,clean.get(0));
		assertNoError(res);
		
		// Lock owner may unlock (duh!)
		res = unlock(sessionId,clean.get(0));
		assertNoError(res);
		
		res = get(sessionId,clean.get(0));
		assertNoError(res);
		assertUnlocked((JSONObject)res.getData());
		
		String sessionId2 = getSecondSessionId();
		
		res = lock(sessionId2,clean.get(0));
		assertNoError(res);
		
		// Owner may unlock
		res = unlock(sessionId,clean.get(0));
		assertNoError(res);
		
		res = get(sessionId,clean.get(0));
		assertNoError(res);
		assertUnlocked((JSONObject)res.getData());
		
	}
	
	public static void assertLocked(JSONObject o) throws JSONException{
		long locked = o.getInt(Metadata.LOCKED_UNTIL_LITERAL.getName());
		assertFalse("This may not me 0: "+locked, 0 == locked);
	}
	
	public static void assertUnlocked(JSONObject o) throws JSONException{
		assertEquals(0, o.getInt(Metadata.LOCKED_UNTIL_LITERAL.getName()));
	}
}
