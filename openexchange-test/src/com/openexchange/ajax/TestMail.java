package com.openexchange.ajax;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class TestMail extends TestCase {
	
	private String sessionId = null;
	
	private WebConversation wc = null;
	
	private WebRequest req = null;
	
	private WebResponse resp = null;
	
	public void setUp() throws Exception {
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
	
	public void testGetNumOfMailsInDefaultFolder() {
		/*
		 * Default folder "inbox"
		 */
		req = new GetMethodWebRequest("http://127.0.0.1/ajax/mail?session=" + sessionId);
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
	
	public void testGetListOfMailsInDefaultFolder() {
		/*
		 * Default folder "inbox"
		 */
		req = new GetMethodWebRequest("http://127.0.0.1/ajax/mail?session=" + sessionId + "&from=0&to=20&sort=date&order=DESC");
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
	
	public void testGetMessage() {
		/*
		 * Default folder "inbox"
		 */
		req = new GetMethodWebRequest("http://127.0.0.1/ajax/mail?session=" + sessionId + "&id=4");
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
	
	public void testCreateMessage() {
		String newMail = "{\"from\":\"Wichser Schweinearsch <hans@example.org>\",\"recipient_to\":[\"Thorben Betten <thorben@example.org>\"],\"recipient_cc\":[],\"recipient_bcc\":[],\"subject\":\"Goh kecken, Aller!\",\"date\":1150282129000,\"size\":1872,\"content-type\":\"TEXT/PLAIN; charset=us-ascii\",\"content\":\"Ober fieser Mailtext! Ober fieser Mailtext! Ober fieser Mailtext! Ober\\r\\nfieser Mailtext! Ober fieser Mailtext! Ober fieser Mailtext! Ober fieser\\r\\nMailtext! Ober fieser Mailtext! Ober fieser Mailtext! Ober fieser\\r\\nMailtext! Ober fieser Mailtext! Ober fieser Mailtext! Ober fieser\\r\\nMailtext! Ober fieser Mailtext! Ober fieser Mailtext! Ober fieser\\r\\nMailtext! Ober fieser Mailtext! Ober fieser Mailtext! Ober fieser\\r\\nMailtext! Ober fieser Mailtext! Ober fieser Mailtext! Ober fieser\\r\\nMailtext! Ober fieser Mailtext! Ober fieser Mailtext! Ober fieser\\r\\nMailtext! Ober fieser Mailtext! Ober fieser Mailtext! Ober fieser\\r\\nMailtext! Ober fieser Mailtext! Ober fieser Mailtext! Ober fieser\\r\\nMailtext! Ober fieser Mailtext! Ober fieser Mailtext! Ober fieser\\r\\nMailtext!\\r\\n\\r\\n\"}";
		byte[] bytes = null;
		try {
			bytes = newMail.getBytes("UTF-8");
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			req = new PutMethodWebRequest("http://127.0.0.1/ajax/mail?session=" + sessionId + "&folder=SENT", bais, "text/javascript");
			System.out.println("Starting Put-Request...");
			long start = System.currentTimeMillis();
			resp = wc.getResponse(req);
			System.out.println("Response:\n" + resp.getText() + "\tDuration: " + (System.currentTimeMillis() - start) + "msec");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testDeleteMessage() {
		byte[] bytes = null;
		try {
			bytes = "".getBytes("UTF-8");
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			req = new PostMethodWebRequest("http://127.0.0.1/ajax/mail?session=" + sessionId + "&delete=6&folder=INBOX", bais, "text/javascript");
			System.out.println("Starting Put-Request...");
			long start = System.currentTimeMillis();
			resp = wc.getResponse(req);
			System.out.println("Response:\n" + resp.getText() + "\tDuration: " + (System.currentTimeMillis() - start) + "msec");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
