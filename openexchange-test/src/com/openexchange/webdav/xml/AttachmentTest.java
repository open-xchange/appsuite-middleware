package com.openexchange.webdav.xml;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.webdav.attachments;
import com.openexchange.webdav.xml.fields.DataFields;
import java.io.ByteArrayInputStream;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

public class AttachmentTest extends AbstractWebdavXMLTest {
	
	public static final String ATTACHMENT_URL = "/servlet/webdav.attachments";

	protected StringBuffer data = new StringBuffer();	

	protected void setUp() throws Exception {
		super.setUp();

		data.append("DATA MIT VIEL TEST\n");
		data.append("123465678901223456\n");
	}
	
	protected int insertAttachment(String filename, int module, int targetId, int targetFolderId, boolean rtf) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(data.toString().getBytes());
		req = new PutMethodWebRequest(PROTOCOL + hostName + ATTACHMENT_URL, bais, "text/plain");
		req.setHeaderField("Authorization", "Basic " + authData);
		req.setHeaderField(attachments.FILENAME, filename);
		req.setHeaderField(attachments.MODULE, String.valueOf(module));
		req.setHeaderField(attachments.TARGET_ID, String.valueOf(targetId));
		req.setHeaderField(attachments.TARGET_FOLDER_ID, String.valueOf(targetFolderId));
		
		if (rtf) {
			req.setHeaderField(attachments.RTF_FLAG, String.valueOf(rtf));
		}
		
		resp = webCon.getResponse(req);
		assertEquals(207, resp.getResponseCode());
		
		bais = new ByteArrayInputStream(resp.getText().getBytes("UTF-8"));
		
		Document doc = new SAXBuilder().build(bais);
		return parseResponse(doc, false);
	}
	
	protected void loadAttachment(int objectId, int module, int targetId, int targetFolderId, boolean rtf) throws Exception {
		WebRequest req = new GetMethodWebRequest(PROTOCOL + hostName + ATTACHMENT_URL);
		req.setHeaderField("Authorization", "Basic " + authData);
		req.setHeaderField(attachments.MODULE, String.valueOf(module));
		req.setHeaderField(attachments.TARGET_ID, String.valueOf(targetId));
		req.setHeaderField(attachments.TARGET_FOLDER_ID, String.valueOf(targetFolderId));
		req.setHeaderField(DataFields.OBJECT_ID, String.valueOf(objectId));
		
		WebResponse resp = webCon.getResponse(req);

		assertEquals(200, resp.getResponseCode());
		
		assertEquals("check response body size", data.length(), resp.getText().length());
		assertEquals("check response body", data.toString(), resp.getText());
	}
	
	protected void deleteAttachment(int objectId, int module, int targetId, int targetFolderId) throws Exception {
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(null, new UsernamePasswordCredentials(login, password));
		DeleteMethod deleteMethod = new DeleteMethod(PROTOCOL + hostName + ATTACHMENT_URL);
		deleteMethod.setDoAuthentication( true );
		deleteMethod.setRequestHeader(attachments.MODULE, String.valueOf(module));
		deleteMethod.setRequestHeader(attachments.TARGET_ID, String.valueOf(targetId));
		deleteMethod.setRequestHeader(DataFields.OBJECT_ID, String.valueOf(objectId));
		deleteMethod.setRequestHeader(attachments.TARGET_FOLDER_ID, String.valueOf(targetFolderId));
		
		assertEquals(207, resp.getResponseCode());
	}
}

