package com.openexchange.ajax.infostore;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;

import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.infostore.utils.Metadata;


public class UpdateTest extends InfostoreAJAXTest {

	public static final int SIZE = 15; // Size of the large file in Megabytes
	
	private static final byte[] megabyte = new byte[1000000];

	
	public UpdateTest() {
		super();
	}
	
	public void testBasic() throws Exception{
		Response res = this.update(sessionId, clean.get(0),System.currentTimeMillis(),m(
				"title" , "test knowledge updated",
				"color_label" , "1"
		));
		assertNoError(res);
		
		
		res = get(sessionId, clean.get(0));
		assertNoError(res);
		
		JSONObject object = (JSONObject) res.getData();
		
		
		assertEquals("test knowledge updated", object.getString("title"));
		assertEquals("test knowledge description", object.getString("description"));
		assertEquals(1, object.getInt("color_label"));
		
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
	
	public void testUpload() throws Exception{
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
	
	public void testLargeFileUpload() throws Exception{
		File largeFile = File.createTempFile("test","bin");
		largeFile.deleteOnExit();
		
		BufferedOutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(largeFile),1000000);
			for(int i = 0; i < SIZE; i++) {
				out.write(megabyte);
				out.flush();
			}
		} finally {
			if(out != null)
				out.close();
		}
		
		try {
			int id = createNew(
					sessionId,
					m(
							"folder_id" 		,	((Integer)folderId).toString(),
							"title"  		,  	"test large upload",
							"description" 	, 	"test large upload description"
					),
					largeFile,
					"text/plain"
			);
			clean.add(id);
			fail("Uploaded Large File and got no error");
		} catch (Exception x) {
			assertTrue(x.getMessage().startsWith("the request was rejected because its size"));
		}
	}
	
	public void testSwitchVersion() throws Exception{
		File upload = new File(Init.getTestProperty("ajaxPropertiesFile"));
		
		int id = clean.get(0);
		
		Response res = update(sessionId,id,System.currentTimeMillis(),m(),upload,"text/plain"); // V1
		assertNoError(res);
		
		res = update(sessionId,id,System.currentTimeMillis(),m(),upload,"text/plain");// V2
		assertNoError(res);
		
		res = update(sessionId,id,System.currentTimeMillis(),m(),upload,"text/plain");// V3
		assertNoError(res);
		
		res = update(sessionId,id,System.currentTimeMillis(),m("version" , "2"));
		assertNoError(res);
		
		res = get(sessionId,id);
		JSONObject obj = (JSONObject) res.getData();
		assertEquals(2,obj.get("version"));
		
		res = versions(sessionId,id, new int[]{Metadata.VERSION, Metadata.CURRENT_VERSION});
		assertNoError(res);
		
		VersionsTest.assureVersions(new Integer[]{1,2,3},res,2);
	}
	
	

}
