package com.openexchange.ajax;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.groupware.container.FolderObject;

public class TestFolder extends TestCase {
	
	private String sessionId = null;
	
	private WebConversation wc = null;
	
	private WebRequest req = null;
	
	private WebResponse resp = null;
	
	protected void setUp() throws Exception {
		super.setUp();
		wc = new WebConversation();
        sessionId = LoginTest.getSessionId(wc, "127.0.0.1", "marcus", "netline");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testGetRootFolders() {
		req = new GetMethodWebRequest("http://127.0.0.1/ajax/folders?session=" + sessionId);
		try {
			long start = System.currentTimeMillis();
			resp = wc.getResponse(req);
			System.out.println("Response:\n" + resp.getText() + "\tDuration: " + (System.currentTimeMillis() - start)
					+ "msec");
			assertFalse(resp.getText().indexOf("error") > -1);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testGetSubfolders() {
		req = new GetMethodWebRequest("http://127.0.0.1/ajax/folders?session=" + sessionId + "&gparent=127");
		try {
			long start = System.currentTimeMillis();
			resp = wc.getResponse(req);
			System.out.println("Response:\n" + resp.getText() + "\tDuration: " + (System.currentTimeMillis() - start)
					+ "msec");
			assertFalse(resp.getText().indexOf("error") > -1);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		req = new GetMethodWebRequest("http://127.0.0.1/ajax/folders?session=" + sessionId + "&gparent=INBOX");
		try {
			long start = System.currentTimeMillis();
			resp = wc.getResponse(req);
			System.out.println("Response:\n" + resp.getText() + "\tDuration: " + (System.currentTimeMillis() - start)
					+ "msec");
			assertFalse(resp.getText().indexOf("error") > -1);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testGetFolder() {
		req = new GetMethodWebRequest("http://127.0.0.1/ajax/folders?session=" + sessionId + "&id=290");
		try {
			long start = System.currentTimeMillis();
			resp = wc.getResponse(req);
			System.out.println("Response:\n" + resp.getText() + "\tDuration: " + (System.currentTimeMillis() - start)
					+ "msec");
			assertFalse(resp.getText().indexOf("error") > -1);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		req = new GetMethodWebRequest("http://127.0.0.1/ajax/folders?session=" + sessionId + "&id=INBOX");
		try {
			long start = System.currentTimeMillis();
			resp = wc.getResponse(req);
			System.out.println("Response:\n" + resp.getText() + "\tDuration: " + (System.currentTimeMillis() - start)
					+ "msec");
			assertFalse(resp.getText().indexOf("error") > -1);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testCreateUpdateDeleteFolder() {
		String oxFolderName = "CalendarFolder07";
		String mailFolderName = "MyMailFolder25";
		String newOXFolderObj = "{\"folder_id\":65,\"permissions\":[{\"group\":false,\"bits\":403710016,\"entity\":140}],\"title\":\""+oxFolderName+"\",\"module\":2}";
		String newMailFolderObj = "{\"folder_id\":\"INBOX\",\"permissions\":[{\"group\":false,\"bits\":\"lrswipcda\",\"entity\":140}],\"title\":\""+mailFolderName+"\",\"module\":"+FolderObject.MAIL+"}";
		byte[] bytes;
		String expectedInsertPrefix = "{OK: Folder successfully inserted";
		int oxFolderId = -1;
		String mailFolderFullName = null;
		/*
		 * Insert OX Folder
		 */
		try {
			bytes = newOXFolderObj.getBytes("UTF-8");
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			req = new PutMethodWebRequest("http://127.0.0.1/ajax/folders?session=" + sessionId + "&folder=65", bais, "text/javascript; charset=UTF-8");
			System.out.println("Starting Put-Request to insert a OX folder...");
			long start = System.currentTimeMillis();
			resp = wc.getResponse(req);
			System.out.println("Response:\n" + resp.getText() + "\tDuration: " + (System.currentTimeMillis() - start) + "msec");
			if (resp.getText().indexOf('(') > -1) {
				oxFolderId = Integer.parseInt(resp.getText().substring(resp.getText().indexOf('(') + 1,
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
		/*
		 * Insert Mail Folder
		 */
		try {
			bytes = newMailFolderObj.getBytes("UTF-8");
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			req = new PutMethodWebRequest("http://127.0.0.1/ajax/folders?session=" + sessionId + "&folder=INBOX", bais, "text/javascript; charset=UTF-8");
			System.out.println("Starting Put-Request to insert a IMAP folder...");
			long start = System.currentTimeMillis();
			resp = wc.getResponse(req);
			System.out.println("Response:\n" + resp.getText() + "\tDuration: " + (System.currentTimeMillis() - start) + "msec");
			if (resp.getText().indexOf('(') > -1) {
				mailFolderFullName = resp.getText().substring(resp.getText().indexOf('(') + 1, resp.getText().indexOf(')'));
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
		String updateOXFolderObj = "{\"folder_id\":65,\"permissions\":[{\"group\":false,\"bits\":403710016,\"entity\":140}],\"title\":\""+oxFolderName+"_Changed\",\"module\":2}";
		String updateMailFolderObj = "{\"folder_id\":\"INBOX\",\"permissions\":[{\"group\":false,\"bits\":\"lrswipcda\",\"entity\":140}],\"title\":\""+mailFolderName+"Changed\",\"module\":"+FolderObject.MAIL+"}";
		String expectedUpdatePrefix = "{OK: Folder successfully updated";
		/*
		 * Update OX folder
		 */
		try {
			bytes = updateOXFolderObj.getBytes("UTF-8");
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			req = new PutMethodWebRequest("http://127.0.0.1/ajax/folders?session=" + sessionId + "&id=" + oxFolderId, bais, "text/javascript; charset=UTF-8");
			System.out.println("Starting Put-Request to update a OX folder...");
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
		/*
		 * Update IMAP Folder
		 */
		try {
			bytes = updateMailFolderObj.getBytes("UTF-8");
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			req = new PutMethodWebRequest("http://127.0.0.1/ajax/folders?session=" + sessionId + "&id=" + mailFolderFullName, bais, "text/javascript; charset=UTF-8");
			System.out.println("Starting Put-Request to update a IMAP folder...");
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
		/*
		 * Delete OX folder
		 */
		String expectedDeletePrefix = "{OK: Folder successfully deleted";
		try {
			bytes = "".getBytes("UTF-8");
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			req = new PostMethodWebRequest("http://127.0.0.1/ajax/folders?session=" + sessionId + "&delete=" + oxFolderId, bais, "text/javascript; charset=UTF-8");
			System.out.println("Starting Post-Request to delete a OX folder...");
			long start = System.currentTimeMillis();
			resp = wc.getResponse(req);
			System.out.println("Response:\n" + resp.getText() + "\t\tDuration: " + (System.currentTimeMillis() - start) + "msec");
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
		/*
		 * Delete IMAP folder
		 */
		mailFolderFullName += "Changed";
		try {
			bytes = "".getBytes("UTF-8");
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			req = new PostMethodWebRequest("http://127.0.0.1/ajax/folders?session=" + sessionId + "&delete=" + mailFolderFullName, bais, "text/javascript; charset=UTF-8");
			System.out.println("Starting Post-Request to delete a IMAP folder...");
			long start = System.currentTimeMillis();
			resp = wc.getResponse(req);
			System.out.println("Response:\n" + resp.getText() + "\t\tDuration: " + (System.currentTimeMillis() - start) + "msec");
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
