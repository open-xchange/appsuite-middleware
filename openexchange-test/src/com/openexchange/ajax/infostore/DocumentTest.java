package com.openexchange.ajax.infostore;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.test.TestInit;
import com.openexchange.test.OXTestToolkit;

public class DocumentTest extends InfostoreAJAXTest {
	
	protected File upload;
	protected int id;
	
	public DocumentTest(String name){
		super(name);
	}
	
	public void setUp() throws Exception{
		super.setUp();
		upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
		id = createNew(
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
	}
	
	public void testCurrentVersion() throws Exception{
		InputStream is = null;
		InputStream is2 = null;
		try {
			is = new FileInputStream(upload);
			is2 = document(getWebConversation(),getHostName(),sessionId, id, -1);
			
			OXTestToolkit.assertSameContent(is,is2);
		} finally {
			if(is!=null)
				is.close();
			if(is2!=null)
				is2.close();
		}
	}
	
	public void testContentType() throws Exception {
		GetMethodWebRequest req = documentRequest(sessionId, getHostName(), id, -1, "application/octet-stream");
		WebResponse resp = getWebConversation().getResource(req);
		assertEquals("application/octet-stream", resp.getContentType());
		
		req = documentRequest(sessionId, getHostName(), id, -1, null);
		resp = getWebConversation().getResource(req);
		assertEquals("text/plain", resp.getContentType());
		
	}
}
