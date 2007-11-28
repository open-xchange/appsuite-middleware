package com.openexchange.ajax.infostore;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.test.TestInit;
import com.openexchange.test.OXTestToolkit;

public class CopyTest extends InfostoreAJAXTest {
	
	public CopyTest(String name){
		super(name);
	}
	
	private Set<String> skipKeys = new HashSet<String>(Arrays.asList(
			Metadata.ID_LITERAL.getName(),
			Metadata.CREATION_DATE_LITERAL.getName(),
			Metadata.LAST_MODIFIED_LITERAL.getName(),
			Metadata.VERSION_LITERAL.getName(),
			Metadata.CURRENT_VERSION_LITERAL.getName(),
			Metadata.SEQUENCE_NUMBER_LITERAL.getName(),
			Metadata.CONTENT_LITERAL.getName()
	));
	
	public void testCopy() throws Exception {
		int id = copy(getWebConversation(), getHostName(),sessionId,clean.get(0), System.currentTimeMillis(), m());
		clean.add(id);
		
		Response res = get(getWebConversation(), getHostName(), sessionId, clean.get(0));
		assertNoError(res);
		
		JSONObject orig = (JSONObject) res.getData();
		
		res = get(getWebConversation(), getHostName(), sessionId, id);
		assertNoError(res);
		
		JSONObject copy = (JSONObject) res.getData();
		
		assertEquals(orig.length(), copy.length());
		
		for(Iterator keys = orig.keys(); keys.hasNext();) {
			String key = keys.next().toString();
			if(!skipKeys.contains(key)) {
				assertEquals(orig.get(key).toString(), copy.get(key).toString());
			}
		}
	}
	
	public void testCopyFile() throws Exception {
		File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
		int id = createNew(
				getWebConversation(),
				getHostName(),
				sessionId,
				m(
						"folder_id" 		,	((Integer)folderId).toString(),
						"title"  		,  	"test upload",
						"description" 	, 	"test upload description"
				), upload, "text/plain"
		);
		clean.add(id);
		//FIXME Bug 4120
		int copyId = copy(getWebConversation(),getHostName(),sessionId,id, System.currentTimeMillis(), m("filename" , "other.properties"));
		clean.add(copyId);
		
		Response res = get(getWebConversation(),getHostName(), sessionId, id);
		assertNoError(res);
		JSONObject orig = (JSONObject) res.getData();
		
		res = get(getWebConversation(),getHostName(), sessionId, copyId);
		assertNoError(res);
		JSONObject copy = (JSONObject) res.getData();
		
		assertEquals("other.properties", copy.get("filename"));
		assertEquals(orig.get("file_size"), copy.get("file_size"));
		assertEquals(orig.get("file_mimetype"), copy.get("file_mimetype"));
		
		InputStream is = null;
		InputStream is2 = null;
		try {
			is = new FileInputStream(upload);
			is2 = document(getWebConversation(),getHostName(),sessionId, copyId, 1);
			
			//BufferedReader r = new BufferedReader(new InputStreamReader(is2));
			//String line = null;
			//while((line=r.readLine())!=null){
			//	System.out.println(line);
			//}
			
			OXTestToolkit.assertSameContent(is,is2);
		} finally {
			if(is!=null)
				is.close();
			if(is2!=null)
				is2.close();
		}
	}
	
	public void testModifyingCopy() throws Exception {
		int id = copy(getWebConversation(), getHostName(),sessionId,clean.get(0), System.currentTimeMillis(), m("title" , "copy"));
		clean.add(id);
		
		Response res = get(getWebConversation(), getHostName(), sessionId, clean.get(0));
		assertNoError(res);
		
		JSONObject orig = (JSONObject) res.getData();
		
		res = get(getWebConversation(), getHostName(), sessionId, id);
		assertNoError(res);
		
		JSONObject copy = (JSONObject) res.getData();
		
		assertEquals(orig.length(), copy.length());
		
		for(Iterator keys = orig.keys(); keys.hasNext();) {
			String key = keys.next().toString();
			if(!skipKeys.contains(key) && !key.equals("title")) {
				assertEquals(orig.get(key).toString(), copy.get(key).toString());
			} else if (key.equals("title")) {
				assertEquals("copy",copy.get(key));
			}
		}
	}
	
	public void testUploadCopy() throws Exception {
		File upload = new File(TestInit.getTestProperty("webdavPropertiesFile"));
		int id = copy(getWebConversation(), getHostName(),sessionId,clean.get(0),System.currentTimeMillis(),m("title" , "copy"), upload, "text/plain");
		clean.add(id);
		
		Response res = get(getWebConversation(), getHostName(), sessionId, id);
		assertNoError(res);
		
		JSONObject copy = (JSONObject) res.getData();
		
		assertEquals(upload.getName(),copy.get("filename"));
		assertEquals("text/plain", copy.get("file_mimetype"));
	}

	//Bug 4269
	public void testVirtualFolder() throws Exception{
		try {
			int id = copy(getWebConversation(), getHostName(),sessionId,clean.get(0), System.currentTimeMillis(), m("folder_id" , ""+FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID));
			clean.add(id);	
			fail("Expected IOException");
		} catch (JSONException x) {
			assertTrue(x.getMessage(), x.getMessage().contains("virt"));
		}
		
	}
}
