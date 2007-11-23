package com.openexchange.ajax.infostore;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Folder;
import com.openexchange.ajax.FolderTest;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.URLParameter;

public class LockTest extends InfostoreAJAXTest {
	
	protected File testFile;
	
	public LockTest(String name){
		super(name);
	}
	
	public void setUp() throws Exception{
		testFile = new File(Init.getTestProperty("ajaxPropertiesFile"));
		sessionId = getSessionId();
		// Copied-without-thinking from FolderTest
		final int userId = FolderTest.getUserId(getWebConversation(), getHostName(), getLogin(), getPassword());
		FolderObject myInfostore = FolderTest.getMyInfostoreFolder(getWebConversation(), getHostName(), getSessionId(), userId);
		folderId = FolderTest.insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false,
		myInfostore.getObjectID(), "NewInfostoreFolder"+System.currentTimeMillis(), "infostore", FolderObject.PUBLIC, -1, true);
		updateFolder(getWebConversation(),getHostName(),sessionId,getLogin(),getSeconduser(),folderId,System.currentTimeMillis(),false);
		
		//folderId=228;
		Map<String,String> create = m(
				"folder_id" 		,	((Integer)folderId).toString(),
				"title"  		,  	"test knowledge",
				"description" 	, 	"test knowledge description"
		);
		
		int c = this.createNew(getWebConversation(),getHostName(),sessionId,create, testFile, "text/plain");
		
		clean.add(c);
		
		Response res = this.update(getWebConversation(),getHostName(),sessionId,c,System.currentTimeMillis(),m(), testFile, "text/plain");
		assertNoError(res);
		
		res = this.update(getWebConversation(),getHostName(),sessionId,c,System.currentTimeMillis(),m(), testFile, "text/plain");
		assertNoError(res);
		
		res = this.update(getWebConversation(),getHostName(),sessionId,c,System.currentTimeMillis(),m(), testFile, "text/plain");
		assertNoError(res);
		
		res = this.update(getWebConversation(),getHostName(),sessionId,c,System.currentTimeMillis(),m(), testFile, "text/plain");
		assertNoError(res);
		
	}
	
	public void tearDown() throws Exception{
		super.tearDown();
		
		FolderTest.deleteFolders(getWebConversation(),getHostName(),sessionId,new int[]{folderId},System.currentTimeMillis(),false);
	}
	
	public void testLock() throws Exception{
		
		Response res = get(getWebConversation(), getHostName(), sessionId, clean.get(0));
		Date ts = res.getTimestamp();
		
		res = lock(getWebConversation(),getHostName(), sessionId, clean.get(0));
		assertNoError(res);
		
		res = get(getWebConversation(),getHostName(), sessionId, clean.get(0));
		assertNoError(res);
		assertLocked((JSONObject)res.getData());
		
		// BUG 4232
		
		res = updates(getWebConversation(), getHostName(), sessionId, folderId, new int[]{Metadata.ID}, ts.getTime());
		
		JSONArray modAndDel = (JSONArray) res.getData();
		JSONArray mod = modAndDel.getJSONArray(0);
		
		assertEquals(1, mod.length());
		System.out.println(mod);
		assertEquals(clean.get(0), (Integer) mod.getInt(0));
		
		
		String sessionId2 = this.getSecondSessionId();
		
		// Object may not be modified
		res = update(getSecondWebConversation(),getHostName(),sessionId2, clean.get(0), System.currentTimeMillis(), m("title" , "Hallo"));
		assertTrue(res.hasError());
		
		// Bug #????
		// Object may not be moved
		
		int userId2 = FolderTest.getUserId(getSecondWebConversation(), getHostName(), getSeconduser(), getPassword());
		int folderId2 = FolderTest.getMyInfostoreFolder(getSecondWebConversation(),getHostName(),sessionId2,userId2).getObjectID();
		
		res = update(getSecondWebConversation(),getHostName(),sessionId2, clean.get(0), System.currentTimeMillis(), m("folder_id" , ""+folderId2));
		assertTrue(res.hasError());
		
		// Object may not be removed
		int[] notDeleted = delete(getSecondWebConversation(),getHostName(),sessionId2, System.currentTimeMillis(), new int[][]{{folderId, clean.get(0)}});
		assertEquals(1, notDeleted.length);
		assertEquals(clean.get(0),(Integer) notDeleted[0]);
		
		
		// Versions may not be removed
		int[] notDetached = detach(getSecondWebConversation(),getHostName(),sessionId2,System.currentTimeMillis(), clean.get(0), new int[]{4});
		assertEquals(1,notDetached.length);
		assertEquals(4,notDetached[0]);
		
		// Object may not be locked
		res = lock(getSecondWebConversation(),getHostName(), sessionId2, clean.get(0));
		assertTrue(res.hasError());
		
		
		// Object may not be unlocked
		
		res = unlock(getSecondWebConversation(),getHostName(), sessionId2, clean.get(0));
		assertTrue(res.hasError());
		
		// Lock owner may update
		res = update(getWebConversation(),getHostName(),sessionId, clean.get(0), System.currentTimeMillis(), m("title" , "Hallo"));
		assertNoError(res);
		
		res = get(getWebConversation(), getHostName(), sessionId, clean.get(0));
		JSONObject o = (JSONObject) res.getData();
		
		assertEquals("Hallo",o.get("title"));
		
		//Lock owner may detach
		notDetached = detach(getWebConversation(),getHostName(),sessionId,System.currentTimeMillis(), clean.get(0), new int[]{4});
		assertEquals(0,notDetached.length);
		
		//Lock owner may remove
		notDeleted = delete(getWebConversation(),getHostName(),sessionId, System.currentTimeMillis(), new int[][]{{folderId, clean.get(0)}});
		assertEquals(0, notDeleted.length);
		clean.remove(0);
		
	}
	
	public void testUnlock() throws Exception{
		Response res = lock(getWebConversation(),getHostName(), sessionId, clean.get(0));
		assertNoError(res);
		
		res = get(getWebConversation(),getHostName(), sessionId, clean.get(0));
		assertNoError(res);
		assertLocked((JSONObject)res.getData());
		
		// Lock owner may relock
		res = lock(getWebConversation(),getHostName(), sessionId, clean.get(0));
		assertNoError(res);
		
		// Lock owner may unlock (duh!)
		res = unlock(getWebConversation(),getHostName(), sessionId, clean.get(0));
		assertNoError(res);
		
		res = get(getWebConversation(),getHostName(), sessionId, clean.get(0));
		assertNoError(res);
		assertUnlocked((JSONObject)res.getData());
		
		String sessionId2 = getSecondSessionId();
		
		res = lock(getSecondWebConversation(),getHostName(), sessionId2, clean.get(0));
		assertNoError(res);
		
		// Owner may not edit
		res = update(getWebConversation(),getHostName(),sessionId, clean.get(0), System.currentTimeMillis(), m("title" , "Hallo"));
		assertTrue(res.hasError());
		
		// Owner may unlock
		res = unlock(getWebConversation(),getHostName(), sessionId, clean.get(0));
		assertNoError(res);
		
		res = get(getWebConversation(),getHostName(), sessionId, clean.get(0));
		assertNoError(res);
		assertUnlocked((JSONObject)res.getData());
		
	}
	
	public static void assertLocked(JSONObject o) throws JSONException{
		long locked = o.getInt(Metadata.LOCKED_UNTIL_LITERAL.getName());
		assertFalse("This must be != 0: "+locked, 0 == locked);
	}
	
	public static void assertUnlocked(JSONObject o) throws JSONException{
		assertEquals(0, o.getInt(Metadata.LOCKED_UNTIL_LITERAL.getName()));
	}
	
	// Copied from FolderTest
	
	
	public static boolean updateFolder(final WebConversation conversation, final String hostname, final String sessionId,
			final String entity, final String secondEntity, final int folderId, final long timestamp, final boolean printOutput) throws JSONException, MalformedURLException, IOException, SAXException {
			JSONObject jsonFolder = new JSONObject();
			jsonFolder.put("id", folderId);
			JSONArray perms = new JSONArray();
			JSONObject jsonPermission = new JSONObject();
			jsonPermission.put("entity", entity);
			jsonPermission.put("group", false);
			jsonPermission.put("bits", createPermissionBits(8, 8, 8, 8, true));
			perms.put(jsonPermission);
			jsonPermission = new JSONObject();
			jsonPermission.put("entity", secondEntity);
			jsonPermission.put("group", false);
			jsonPermission.put("bits", createPermissionBits(8, 8, 8, 8, true));
			perms.put(jsonPermission);
			jsonFolder.put("permissions", perms);
			URLParameter urlParam = new URLParameter();
			urlParam.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE);
			urlParam.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
			urlParam.setParameter(AJAXServlet.PARAMETER_ID, String.valueOf(folderId));
			urlParam.setParameter("timestamp", String.valueOf(timestamp));
			byte[] bytes = jsonFolder.toString().getBytes("UTF-8");
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostname + FOLDER_URL + urlParam.getURLParameters(), bais,
				"text/javascript; charset=UTF-8");
			final WebResponse resp = conversation.getResponse(req);
			JSONObject respObj = new JSONObject(resp.getText());
			if (printOutput)
				System.out.println(respObj.toString());
			if (respObj.has("error"))
				return false;
			return true;
		}
	
	private static int createPermissionBits(int fp, int orp, int owp, int odp, boolean adminFlag) {
		int[] perms = new int[5];
		perms[0] = fp;
		perms[1] = orp;
		perms[2] = owp;
		perms[3] = odp;
		perms[4] = adminFlag ? 1 : 0;
		return createPermissionBits(perms);
	}
	
	private static int createPermissionBits(int[] permission) {
		int retval = 0;
		boolean first = true;
		for (int i = permission.length - 1; i >= 0; i--) {
			int exponent = (i * 7); // Number of bits to be shifted
			if (first) {
				retval += permission[i] << exponent;
				first = false;
			} else {
				if (permission[i] == OCLPermission.ADMIN_PERMISSION) {
					retval += Folder.MAX_PERMISSION << exponent;
				} else {
					retval += mapping[permission[i]] << exponent;
				}
			}
		}
		return retval;
	}
	private static final int[] mapping = { 0, -1, 1, -1, 2, -1, -1, -1, 4 };
	
	private static final String FOLDER_URL = "/ajax/folders";

}
