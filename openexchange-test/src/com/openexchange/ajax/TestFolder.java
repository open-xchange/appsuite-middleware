package com.openexchange.ajax;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import org.xml.sax.SAXException;

import junit.framework.TestCase;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class TestFolder extends TestCase {
	
	private String sessionId = null;
	
	private WebConversation wc = null;
	
	private WebRequest req = null;
	
	private WebResponse resp = null;
	
	protected void setUp() throws Exception {
		super.setUp();
		wc = new WebConversation();
		req = new GetMethodWebRequest("http://127.0.0.1/ajax/login?name=thorben&password=netline");
		resp = wc.getResponse(req);
		sessionId = resp.getText();
		int pos = sessionId.indexOf('\"');
		sessionId = sessionId.substring(pos + 1, sessionId.indexOf('\"', pos + 1));
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testGetRootFolders() {
		req = new GetMethodWebRequest("http://127.0.0.1/ajax/folder?session=" + sessionId);
		String errorPrefix = "{\"error\":";
		try {
			resp = wc.getResponse(req);
			System.out.println(resp.getText());
			assertFalse(resp.getText().startsWith(errorPrefix));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testGetSubfolders() {
		req = new GetMethodWebRequest("http://127.0.0.1/ajax/folder?session=" + sessionId + "&gparent=127");
		String errorPrefix = "{\"error\":";
		try {
			resp = wc.getResponse(req);
			System.out.println(resp.getText());
			assertFalse(resp.getText().startsWith(errorPrefix));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testGetFolder() {
		req = new GetMethodWebRequest("http://127.0.0.1/ajax/folder?session=" + sessionId + "&id=290");
		String errorPrefix = "{\"error\":";
		try {
			resp = wc.getResponse(req);
			System.out.println(resp.getText());
			assertFalse(resp.getText().startsWith(errorPrefix));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testCreateUpdateDeleteFolder() {
		String folderName = "CalendarFolder07";
		String newFolderObj = "{\"folder_id\":65,\"permissions\":[{\"group\":false,\"bits\":403710016,\"entity\":140}],\"title\":\""+folderName+"\",\"module\":2}";
		byte[] bytes;
		String expectedInsertPrefix = "{OK: Folder successfully inserted";
		int fuid = -1;
		try {
			bytes = newFolderObj.getBytes("UTF-8");
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			req = new PutMethodWebRequest("http://127.0.0.1/ajax/folder?session=" + sessionId + "&folder=65", bais, "text/javascript; charset=UTF-8");
			System.out.println("Starting Put-Request...");
			long start = System.currentTimeMillis();
			resp = wc.getResponse(req);
			System.out.println("Response:\n" + resp.getText() + "\tDuration: " + (System.currentTimeMillis() - start) + "msec");
			if (resp.getText().indexOf('(') > -1) {
				fuid = Integer.parseInt(resp.getText().substring(resp.getText().indexOf('(') + 1,
						resp.getText().indexOf(')')));
			}
			assertTrue(resp.getText().startsWith(expectedInsertPrefix));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (SAXException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		String updateFolderObj = "{\"folder_id\":65,\"permissions\":[{\"group\":false,\"bits\":403710016,\"entity\":140}],\"title\":\""+folderName+"_Changed\",\"module\":2}";
		String expectedUpdatePrefix = "{OK: Folder successfully updated";
		try {
			bytes = updateFolderObj.getBytes("UTF-8");
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			req = new PutMethodWebRequest("http://127.0.0.1/ajax/folder?session=" + sessionId + "&id=" + fuid, bais, "text/javascript; charset=UTF-8");
			System.out.println("Starting Put-Request...");
			long start = System.currentTimeMillis();
			resp = wc.getResponse(req);
			System.out.println("Response:\n" + resp.getText() + "\tDuration: " + (System.currentTimeMillis() - start) + "msec");
			assertTrue(resp.getText().startsWith(expectedUpdatePrefix));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (SAXException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		String expectedDeletePrefix = "{OK: Folder successfully deleted";
		try {
			bytes = "".getBytes("UTF-8");
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			req = new PostMethodWebRequest("http://127.0.0.1/ajax/folder?session=" + sessionId + "&delete=" + fuid, bais, "text/javascript; charset=UTF-8");
			System.out.println("Starting Post-Request...");
			long start = System.currentTimeMillis();
			resp = wc.getResponse(req);
			System.out.println("Response:\n>" + resp.getText() + "<\tDuration: " + (System.currentTimeMillis() - start) + "msec");
			assertTrue(resp.getText().startsWith(expectedDeletePrefix));
		}  catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (SAXException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
