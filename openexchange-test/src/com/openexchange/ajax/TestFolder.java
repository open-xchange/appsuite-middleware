package com.openexchange.ajax;

import junit.framework.TestCase;

import com.meterware.httpunit.GetMethodWebRequest;
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
		System.out.println("Obtained Session Id: " + sessionId);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testGetFolder() {
		req = new GetMethodWebRequest("http://127.0.0.1/ajax/folder?session=" + sessionId);
		try {
			resp = wc.getResponse(req);
			System.out.println(resp.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
