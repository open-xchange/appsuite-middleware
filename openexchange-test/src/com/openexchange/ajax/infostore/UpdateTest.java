package com.openexchange.ajax.infostore;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.json.JSONObject;

import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.Init;


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
	
	public void testConflict() throws Exception{
		Response res = this.get(sessionId,clean.get(0));
		Response res2 = this.update(sessionId, clean.get(0),res.getTimestamp().getTime()-2000,m(
					"title" , "test knowledge updated"
			));
		assertNotNull(res2.getErrorMessage());
		assertFalse("".equals(res2.getErrorMessage()));
		
	}
	
	public void notestUpload() throws Exception{
		File upload = new File(Init.getTestProperty("ajaxPropertiesFile"));
		
		int id = clean.get(0);
		
		Response res = update(sessionId,id,System.currentTimeMillis(),m(),upload,"text/plain");
		assertNoError(res);
		
		res = get(sessionId,id);
		JSONObject obj = (JSONObject) res.getData();
		
		assertEquals(1,obj.getInt("version"));
		
		assertEquals("text/plain",obj.getString("file_mimetype"));
		assertEquals(upload.getName(),obj.getString("filename"));

		InputStream is = null;
		InputStream is2 = null;
		try {
			is = new FileInputStream(upload);
			is2 = document(sessionId,id,1);
			assertSameContent(is,is2);
		} finally {
			if(is!=null)
				is.close();
			if(is2!=null)
				is2.close();
		}
	}
	
	public void testSwitchVersion() throws Exception{
		File upload = new File(Init.getTestProperty("ajaxPropertiesFile"));
		
		int id = clean.get(0);
		
		Response res = update(sessionId,id,System.currentTimeMillis(),m(),upload,"text/plain");
		assertNoError(res);
		
		res = update(sessionId,id,System.currentTimeMillis(),m(),upload,"text/plain");
		assertNoError(res);
		
		res = update(sessionId,id,System.currentTimeMillis(),m(),upload,"text/plain");
		assertNoError(res);
		
		update(sessionId,id,System.currentTimeMillis(),m("version" , "2"));
		
		res = get(sessionId,id);
		JSONObject obj = (JSONObject) res.getData();
		assertEquals(2,obj.get("version"));
	}

}
