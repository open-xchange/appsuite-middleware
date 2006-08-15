package com.openexchange.ajax.infostore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONObject;

import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.Init;


public class NewTest extends InfostoreAJAXTest {

	public NewTest() {
		super();
	}
	
	public void notestUpload() throws Exception{
		File upload = new File(Init.getTestProperty("ajaxPropertiesFile"));
		int id = createNew(
				sessionId,
				m(
						"folder_id" 		,	((Integer)folderId).toString(),
						"title"  		,  	"test upload",
						"description" 	, 	"test upload description"
				),
				upload,
				"text/plain"
		);
		clean.add(id);
		
		Response res = get(sessionId,id);
		JSONObject obj = (JSONObject) res.getData();
		
		assertEquals("test upload",obj.getString("title"));
		assertEquals("test upload description",obj.getString("description"));
		assertEquals(1,obj.getInt("version"));
		assertEquals("text/plain",obj.getString("file_mimetype"));
		assertEquals(upload.getName(),obj.getString("filename"));

		
		InputStream is = null;
		InputStream is2 = null;
		try {
			//is = new FileInputStream(upload);
			is2 = document(sessionId,id,1);
			
			//BufferedReader r = new BufferedReader(new InputStreamReader(is2));
			//String line = null;
			//while((line=r.readLine())!=null){
			//	System.out.println(line);
			//}
			
			//assertSameContent(is,is2);
		} finally {
			if(is!=null)
				is.close();
			if(is2!=null)
				is2.close();
		}
		
		id = createNew(
			sessionId,
			m(
					"folder_id" 		,	((Integer)folderId).toString(),
					"title"  		,  	"test no upload",
					"description" 	, 	"test no upload description"
			),
			null,
			""
		);
		clean.add(id);
		
		res = get(sessionId,id);
		obj = (JSONObject) res.getData();
		
		assertEquals("test no upload",obj.getString("title"));
		assertEquals("test no upload description",obj.getString("description"));
		
	}

}
