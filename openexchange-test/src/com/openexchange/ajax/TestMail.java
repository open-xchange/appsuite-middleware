package com.openexchange.ajax;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.groupware.container.mail.JSONMessageObject;
import com.openexchange.tools.URLParameter;

public class TestMail extends TestCase {
	
	private String sessionId = null;
	
	private WebConversation wc = null;
	
	private WebRequest req = null;
	
	private WebResponse resp = null;
	
	public void setUp() throws Exception {
		wc = new WebConversation();
		req = new GetMethodWebRequest("http://127.0.0.1/ajax/login?action=login&name=thorben&password=netline");
		resp = wc.getResponse(req);
		JSONObject respObj = new JSONObject(resp.getText());
		sessionId = respObj.getString("session");
	}
	
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void notestSpellCheck() {
		try {
			String txt = "First line wioth soem incorrect worsd<br>Some texte for spell checker<br>Next linee fuckesr<br>Bye!";
			ByteArrayInputStream bais = new ByteArrayInputStream(txt.getBytes("utf-8"));
			URLParameter urlParam = new URLParameter();
			urlParam.setParameter(AJAXServlet.PARAMETER_ACTION, "spellcheck");
			urlParam.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
			req = new PostMethodWebRequest("http://127.0.0.1/ajax/spellcheck" + urlParam.getURLParameters(), bais,
					"text/javascript; charset=UTF-8");
			resp = wc.getResponse(req);
			String s = resp.getText();
			JSONObject respObj = new JSONObject(s);
			System.out.println("Response: " + respObj.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void notestGetAllMailsInInbox() {
		/*
		 * Default folder "inbox"
		 */
		String columns = JSONMessageObject.FIELD_ID + "";
		req = new GetMethodWebRequest("http://127.0.0.1/ajax/mail?session=" + sessionId
				+ "&action=all&folder=Inbox&sort="+JSONMessageObject.FIELD_SENT_DATE+"&order=ASC&columns=" + columns);
		try {
			long start = System.currentTimeMillis();
			resp = wc.getResponse(req);
			String s = resp.getText();
			JSONObject respObj = new JSONObject(s);
			System.out.println("Response:\n" + respObj.toString());
			System.out.println("\n\tDuration: " + (System.currentTimeMillis() - start)
					+ "msec");
			assertFalse(resp.getText().indexOf("error") > -1);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testNewMailsInInbox() {
		/*
		 * Default folder "inbox"
		 */
		String columns = JSONMessageObject.FIELD_ID + "";
		req = new GetMethodWebRequest("http://127.0.0.1/ajax/mail?session=" + sessionId
				+ "&action=newmsgs&folder=Inbox&sort="+JSONMessageObject.FIELD_SENT_DATE+"&order=ASC&columns=" + columns);
		try {
			long start = System.currentTimeMillis();
			resp = wc.getResponse(req);
			String s = resp.getText();
			JSONObject respObj = new JSONObject(s);
			System.out.println("Response:\n" + respObj.toString());
			System.out.println("\n\tDuration: " + (System.currentTimeMillis() - start)
					+ "msec");
			assertFalse(resp.getText().indexOf("error") > -1);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testGetMails() {
		try {
			String columns = JSONMessageObject.FIELD_ID+","+JSONMessageObject.FIELD_FROM;
//			req = new GetMethodWebRequest("http://127.0.0.1/ajax/mail?session=" + sessionId
//					+ "&action=all&folder=Inbox&sort="+MessageObject.FIELD_SENT_DATE+"&order=ASC&columns=" + columns);
			req = new GetMethodWebRequest("http://127.0.0.1/ajax/mail?session=" + sessionId
					+ "&action=all&folder=Inbox&sort=thread&columns=" + columns);
			resp = wc.getResponse(req);
			String s = resp.getText();
			JSONObject respObj = new JSONObject(s);
			System.out.println("Thread-Sorted Messages: " + respObj.getJSONArray("data").toString());
			JSONArray arr = respObj.getJSONArray("data");
			for (int i = 0; i < arr.length(); i++) {
				JSONArray nestedArr = arr.getJSONArray(i);
				req = new GetMethodWebRequest("http://127.0.0.1/ajax/mail?session=" + sessionId
						+ "&action=get&id=" + nestedArr.getString(0));
				resp = wc.getResponse(req);
				JSONObject mailObj = new JSONObject(resp.getText());
				System.out.println("Mail >"+nestedArr.getString(0)+"<: " + mailObj.getString("data"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testReplyMail() {
		try {
			String columns = JSONMessageObject.FIELD_ID+","+JSONMessageObject.FIELD_FROM;
			req = new GetMethodWebRequest("http://127.0.0.1/ajax/mail?session=" + sessionId
					+ "&action=all&folder=Inbox&sort=thread&columns=" + columns);
			resp = wc.getResponse(req);
			String s = resp.getText();
			JSONObject respObj = new JSONObject(s);
			System.out.println("Thread-Sorted Messages: " + respObj.getJSONArray("data").toString());
			JSONArray arr = respObj.getJSONArray("data");
			for (int i = 0; i < arr.length(); i++) {
				JSONArray nestedArr = arr.getJSONArray(i);
				req = new GetMethodWebRequest("http://127.0.0.1/ajax/mail?session=" + sessionId
						+ "&action=reply&reply2all=1&id=" + nestedArr.getString(0));
				resp = wc.getResponse(req);
				JSONObject mailObj = new JSONObject(resp.getText());
				System.out.println("Reply-Mail >"+nestedArr.getString(0)+"<: " + mailObj.getString("data"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testForwardMail() {
		try {
			String columns = JSONMessageObject.FIELD_ID+","+JSONMessageObject.FIELD_FROM;
			req = new GetMethodWebRequest("http://127.0.0.1/ajax/mail?session=" + sessionId
					+ "&action=all&folder=Inbox&sort=thread&columns=" + columns);
			resp = wc.getResponse(req);
			String s = resp.getText();
			JSONObject respObj = new JSONObject(s);
			System.out.println("Thread-Sorted Messages: " + respObj.getJSONArray("data").toString());
			JSONArray arr = respObj.getJSONArray("data");
			for (int i = 0; i < arr.length(); i++) {
				JSONArray nestedArr = arr.getJSONArray(i);
				req = new GetMethodWebRequest("http://127.0.0.1/ajax/mail?session=" + sessionId
						+ "&action=forward&id=" + nestedArr.getString(0));
				resp = wc.getResponse(req);
				JSONObject mailObj = new JSONObject(resp.getText());
				System.out.println("Forward-Mail >"+nestedArr.getString(0)+"<: " + mailObj.getString("data"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
//	public void testGetMessage() {
//		/*
//		 * Default folder "inbox"
//		 */
//		int id = 12;
//		req = new GetMethodWebRequest("http://127.0.0.1/ajax/mail?session=" + sessionId + "&action=get&id=" + id);
//		try {
//			long start = System.currentTimeMillis();
//			resp = wc.getResponse(req);
//			System.out.println("Response:\n" + resp.getText() + "\tDuration: " + (System.currentTimeMillis() - start)
//					+ "msec");
//			assertFalse(resp.getText().indexOf("error") > -1);
//			/*
//			 * Attachment
//			 */
//			start = System.currentTimeMillis();
//			req = new GetMethodWebRequest("http://127.0.0.1/ajax/mail?session=" + sessionId + "&action=attachment&id="+id+"&attachment=1.2");
//			resp = wc.getResponse(req);
//			System.out.println("Attachment's Content-Type: " + resp.getContentType());
//			System.out.println("Attachment's Content-Length: " + resp.getContentLength());
//			InputStream is = resp.getInputStream();
//			System.out.print("Response:\n");
//			byte[] bytes = null;
//			byte[] tmpBytes = new byte[8192];
//			int bytesRead = -1;
//			while ((bytesRead = is.read(tmpBytes, 0, tmpBytes.length)) >= 0) {
//				if (bytes == null) {
//					bytes = new byte[bytesRead];
//					System.arraycopy(tmpBytes, 0, bytes, 0, bytesRead);
//				} else {
//					byte[] prevBytes = bytes;
//					bytes = new byte[prevBytes.length + bytesRead];
//					System.arraycopy(prevBytes, 0, bytes, 0, prevBytes.length);
//					System.arraycopy(tmpBytes, 0, bytes, prevBytes.length - 1, bytesRead);
//				}
//			}
//			System.out.print(new String(bytes, resp.getCharacterSet()));
//			System.out.println("\tDuration: " + (System.currentTimeMillis() - start)
//					+ "msec");
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//	}
	
//	public void testSendMessage() {
//		try {
//			String jsonMsg = "{\"from\":[\"thorben@leo.org\"],\"to\":[\"offspring@leo.org\"],\"cc\":[],\"bcc\":[],\"subject\":\"Mail vom Thorben\",\"size\":-1,\"sent_date\":1152647545000,\"received_date\":1152647547000,\"flags\":0,\"level\":0,\"user\":{\"X-Mailer\":\"OPEN-XCHANGE 5072 - WebMail\"},\"attachments\":[{\"content_type\":\"TEXT/PLAIN; charset=UTF-8\",\"content\":\"Fieser Mailtext vom Thorben. Fist Fucker united! Mit Attachment!\r\n\",\"disk\":null,\"size\":-1,\"id\":\"1.1\"},{\"content_type\":\"APPLICATION/OCTET-STREAM; name=test.txt\",\"disp\":\"ATTACHMENT\",\"content\":null,\"disk\":null,\"size\":1805,\"filename\":\"test.txt\",\"id\":\"1.2\"}],\"nested_msgs\":[]}";
//			WebForm webForm = null;
//			
//			req = new PostMethodWebRequest("http://127.0.0.1/ajax/mail?session=" + sessionId + "&action=send");
//			req.selectFile("test.txt", new File("/home/thorben/test.txt"), "text/plain");
//			
//			resp = wc.getResponse(req);
//			System.out.println(resp.getText());
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
//	public void testCreateMessage() {
//		String newMail = \"{\\"from\\":\\"Wichser Schweinearsch <hans@example.org>\\",\"recipient_to\":[\"Thorben Betten <thorben@example.org>\"],\"recipient_cc\":[],\"recipient_bcc\":[],\"subject\":\"Goh kecken, Aller!\",\"date\":1150282129000,\"size\":1872,\"content-type\":\"TEXT/PLAIN; charset=us-ascii\",\"content\":\"Ober fieser Mailtext! Ober fieser Mailtext! Ober fieser Mailtext! Ober\\r\\nfieser Mailtext! Ober fieser Mailtext! Ober fieser Mailtext! Ober fieser\\r\\nMailtext! Ober fieser Mailtext! Ober fieser Mailtext! Ober fieser\\r\\nMailtext! Ober fieser Mailtext! Ober fieser Mailtext! Ober fieser\\r\\nMailtext! Ober fieser Mailtext! Ober fieser Mailtext! Ober fieser\\r\\nMailtext! Ober fieser Mailtext! Ober fieser Mailtext! Ober fieser\\r\\nMailtext! Ober fieser Mailtext! Ober fieser Mailtext! Ober fieser\\r\\nMailtext! Ober fieser Mailtext! Ober fieser Mailtext! Ober fieser\\r\\nMailtext! Ober fieser Mailtext! Ober fieser Mailtext! Ober fieser\\r\\nMailtext! Ober fieser Mailtext! Ober fieser Mailtext! Ober fieser\\r\\nMailtext! Ober fieser Mailtext! Ober fieser Mailtext! Ober fieser\\r\\nMailtext!\\r\\n\\r\\n\"}";
//		byte[] bytes = null;
//		try {
//			bytes = newMail.getBytes("UTF-8");
//			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
//			req = new PutMethodWebRequest("http://127.0.0.1/ajax/mail?session=" + sessionId + "&folder=SENT", bais, "text/javascript");
//			System.out.println("Starting Put-Request...");
//			long start = System.currentTimeMillis();
//			resp = wc.getResponse(req);
//			System.out.println("Response:\n" + resp.getText() + "\tDuration: " + (System.currentTimeMillis() - start) + "msec");
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//	}
//	
//	public void testDeleteMessage() {
//		byte[] bytes = null;
//		try {
//			bytes = "".getBytes("UTF-8");
//			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
//			req = new PostMethodWebRequest("http://127.0.0.1/ajax/mail?session=" + sessionId + "&delete=6&folder=INBOX", bais, "text/javascript");
//			System.out.println("Starting Put-Request...");
//			long start = System.currentTimeMillis();
//			resp = wc.getResponse(req);
//			System.out.println("Response:\n" + resp.getText() + "\tDuration: " + (System.currentTimeMillis() - start) + "msec");
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//	}
}
