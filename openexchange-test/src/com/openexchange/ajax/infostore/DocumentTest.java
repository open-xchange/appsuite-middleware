package com.openexchange.ajax.infostore;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.groupware.Init;

public class DocumentTest extends InfostoreAJAXTest {
	
	protected File upload;
	protected int id;
	
	public void setUp() throws Exception{
		super.setUp();
		upload = new File(Init.getTestProperty("ajaxPropertiesFile"));
		id = createNew(
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
	}
	
	public void testCurrentVersion() throws Exception{
		InputStream is = null;
		InputStream is2 = null;
		try {
			is = new FileInputStream(upload);
			is2 = document(sessionId,id,-1);
			
			assertSameContent(is,is2);
		} finally {
			if(is!=null)
				is.close();
			if(is2!=null)
				is2.close();
		}
	}
}
