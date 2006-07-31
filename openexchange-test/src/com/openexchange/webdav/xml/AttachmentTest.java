package com.openexchange.webdav.xml;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.groupware.Types;
import java.io.ByteArrayInputStream;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

public class AttachmentTest extends AbstractWebdavTest {

	protected StringBuffer data = new StringBuffer();	

	protected void setUp() throws Exception {
		super.setUp();

		data.append("DATA MIT VIEL TEST\n");
		data.append("123465678901223456\n");
	}
	
	public void testInsertAttachment() throws Exception {
		insertAttachment(System.currentTimeMillis() + "test.txt", Types.APPOINTMENT, 12345);
	}

	public void testLoadAttachment() throws Exception {
		int objectId = insertAttachment(System.currentTimeMillis() + "test.txt", Types.CONTACT, 112233);
		loadAttachment(objectId, Types.CONTACT, 112233);
	}
	public void testDeleteAttachment() throws Exception {
		int objectId = insertAttachment(System.currentTimeMillis() + "test.txt", Types.TASK, 22334455);
		deleteAttachment(objectId, Types.TASK, 22334455);
	}
	
	protected int insertAttachment(String filename, int module, int targetId) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(data.toString().getBytes());
		req = new PutMethodWebRequest(PROTOCOL + hostName + getURL(), bais, "text/javascript");
		req.setHeaderField("Authorization", "Basic " + authData);
		req.setHeaderField("filename", filename);
		req.setHeaderField("module", String.valueOf(module));
		req.setHeaderField("target_id", String.valueOf(targetId));
		resp = webCon.getResponse(req);
		
		bais = new ByteArrayInputStream(resp.getText().getBytes("UTF-8"));
		
		Document doc = new SAXBuilder().build(bais);
		return parseResponse(doc, false);
	}
	
	protected void loadAttachment(int objectId, int module, int targetId) throws Exception {
		WebRequest req = new GetMethodWebRequest(PROTOCOL + hostName + getURL());
		WebResponse resp = webCon.getResponse(req);
		req.setHeaderField("module", String.valueOf(module));
		req.setHeaderField("target_id", String.valueOf(targetId));
		req.setHeaderField("object_id", String.valueOf(objectId));
		
		assertEquals(200, resp.getResponseCode());
		assertEquals("check response body size", data.length(), resp.getText().length());
		assertEquals("check response body", data.toString(), resp.getText());
	}
	
	protected void deleteAttachment(int objectId, int module, int targetId) throws Exception {
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(null, new UsernamePasswordCredentials(login, password));
		DeleteMethod deleteMethod = new DeleteMethod(PROTOCOL + hostName + getURL());
		deleteMethod.setDoAuthentication( true );
		deleteMethod.setRequestHeader("module", String.valueOf(module));
		deleteMethod.setRequestHeader("target_id", String.valueOf(targetId));
		deleteMethod.setRequestHeader("object_id", String.valueOf(objectId));
		
		assertEquals(200, resp.getResponseCode());
		assertEquals("check response body size", data.length(), resp.getText().length());
		assertEquals("check response body", data.toString(), resp.getText());
	}
	

	protected String getURL() {
		return attachmentUrl;
	}
}

