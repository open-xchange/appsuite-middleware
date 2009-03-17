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
import com.openexchange.test.OXTestToolkit;
import com.openexchange.test.TestInit;

public class CopyTest extends InfostoreAJAXTest {
	
	public CopyTest(final String name){
		super(name);
	}
	
	private final Set<String> skipKeys = new HashSet<String>(Arrays.asList(
			Metadata.ID_LITERAL.getName(),
			Metadata.CREATION_DATE_LITERAL.getName(),
			Metadata.LAST_MODIFIED_LITERAL.getName(),
            Metadata.LAST_MODIFIED_UTC_LITERAL.getName(),
            Metadata.VERSION_LITERAL.getName(),
			Metadata.CURRENT_VERSION_LITERAL.getName(),
			Metadata.SEQUENCE_NUMBER_LITERAL.getName(),
			Metadata.CONTENT_LITERAL.getName()
	));
	
	public void testCopy() throws Exception {
		final int id = copy(getWebConversation(), getHostName(),sessionId,clean.get(0), Long.MAX_VALUE, m());
		clean.add(id);
		
		Response res = get(getWebConversation(), getHostName(), sessionId, clean.get(0));
		assertNoError(res);
		
		final JSONObject orig = (JSONObject) res.getData();
		
		res = get(getWebConversation(), getHostName(), sessionId, id);
		assertNoError(res);
		
		final JSONObject copy = (JSONObject) res.getData();
		
		assertEquals(orig.length(), copy.length());
		
		for(final Iterator keys = orig.keys(); keys.hasNext();) {
			final String key = keys.next().toString();
			if(!skipKeys.contains(key)) {
				assertEquals(orig.get(key).toString(), copy.get(key).toString());
			}
		}

        assertNotNull(res.getTimestamp());
    }
	
	public void testCopyFile() throws Exception {
		final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
		final int id = createNew(
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
		final int copyId = copy(getWebConversation(),getHostName(),sessionId,id, Long.MAX_VALUE, m("filename" , "other.properties"));
		clean.add(copyId);
		
		Response res = get(getWebConversation(),getHostName(), sessionId, id);
		assertNoError(res);
		final JSONObject orig = (JSONObject) res.getData();
		
		res = get(getWebConversation(),getHostName(), sessionId, copyId);
		assertNoError(res);
		final JSONObject copy = (JSONObject) res.getData();
		
		assertEquals("other.properties", copy.get("filename"));
		assertEquals(orig.get("file_size"), copy.get("file_size"));
		assertEquals(orig.get("file_mimetype"), copy.get("file_mimetype"));
		
		InputStream is = null;
		InputStream is2 = null;
		try {
			is = new FileInputStream(upload);
			is2 = document(getWebConversation(),getHostName(),sessionId, copyId, 1);
			
			OXTestToolkit.assertSameContent(is,is2);
		} finally {
			if(is!=null) {
				is.close();
			}
			if(is2!=null) {
				is2.close();
			}
		}
	}
	
	public void testModifyingCopy() throws Exception {
		final int id = copy(getWebConversation(), getHostName(),sessionId,clean.get(0), Long.MAX_VALUE, m("title" , "copy"));
		clean.add(id);
		
		Response res = get(getWebConversation(), getHostName(), sessionId, clean.get(0));
		assertNoError(res);
		
		final JSONObject orig = (JSONObject) res.getData();
		
		res = get(getWebConversation(), getHostName(), sessionId, id);
		assertNoError(res);
		
		final JSONObject copy = (JSONObject) res.getData();
		
		assertEquals(orig.length(), copy.length());
		
		for(final Iterator keys = orig.keys(); keys.hasNext();) {
			final String key = keys.next().toString();
			if(!skipKeys.contains(key) && !key.equals("title")) {
				assertEquals(orig.get(key).toString(), copy.get(key).toString());
			} else if (key.equals("title")) {
				assertEquals("copy",copy.get(key));
			}
		}
	}
	
	public void testUploadCopy() throws Exception {
		final File upload = new File(TestInit.getTestProperty("webdavPropertiesFile"));
		final int id = copy(getWebConversation(), getHostName(),sessionId,clean.get(0),Long.MAX_VALUE,m("title" , "copy"), upload, "text/plain");
		clean.add(id);
		
		final Response res = get(getWebConversation(), getHostName(), sessionId, id);
		assertNoError(res);
		
		final JSONObject copy = (JSONObject) res.getData();
		
		assertEquals(upload.getName(),copy.get("filename"));
		assertEquals("text/plain", copy.get("file_mimetype"));
	}

    //Bug 4269
	public void testVirtualFolder() throws Exception{

        for(int folderId : virtualFolders) {
            virtualFolderTest( folderId );
        }
	}

    //Bug 4269
	public void virtualFolderTest(int folderId) throws Exception{
		try {
			final int id = copy(getWebConversation(), getHostName(),sessionId,clean.get(0), Long.MAX_VALUE, m("folder_id" , ""+folderId));
			clean.add(id);	
			fail("Expected IOException");
		} catch (final JSONException x) {
			assertTrue(x.getMessage(), x.getMessage().contains("virt"));
		}
		
	}
}
