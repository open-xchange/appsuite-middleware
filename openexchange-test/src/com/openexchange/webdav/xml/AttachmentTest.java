package com.openexchange.webdav.xml;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.webdav.attachments;
import com.openexchange.webdav.xml.fields.DataFields;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

public class AttachmentTest extends AbstractWebdavXMLTest {
	
	public static final String ATTACHMENT_URL = "/servlet/webdav.attachments";
	
	public static final String CONTENT_TYPE = "image/png";
	
	public static final byte[] data = { -119, 80, 78, 71, 13, 10, 26, 10, 0,
	0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 1, 0, 0, 0, 1, 1, 3, 0, 0, 0,
	37, -37, 86, -54, 0, 0, 0, 6, 80, 76, 84, 69, -1, -1, -1, -1, -1,
	-1, 85, 124, -11, 108, 0, 0, 0, 1, 116, 82, 78, 83, 0, 64, -26,
	-40, 102, 0, 0, 0, 1, 98, 75, 71, 68, 0, -120, 5, 29, 72, 0, 0, 0,
	9, 112, 72, 89, 115, 0, 0, 11, 18, 0, 0, 11, 18, 1, -46, -35, 126,
	-4, 0, 0, 0, 10, 73, 68, 65, 84, 120, -38, 99, 96, 0, 0, 0, 2, 0,
	1, -27, 39, -34, -4, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126 };
	
	public AttachmentTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public static int insertAttachment(WebConversation webCon, AttachmentMetadata attachmentObj, InputStream is, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		WebRequest webRequest = new PutMethodWebRequest(host + ATTACHMENT_URL, is, attachmentObj.getFileMIMEType());
		webRequest.setHeaderField("Authorization", "Basic " + getAuthData(login, password));
		webRequest.setHeaderField(attachments.FILENAME, attachmentObj.getFilename());
		webRequest.setHeaderField(attachments.MODULE, String.valueOf(attachmentObj.getModuleId()));
		webRequest.setHeaderField(attachments.TARGET_ID, String.valueOf(attachmentObj.getAttachedId()));
		webRequest.setHeaderField(attachments.TARGET_FOLDER_ID, String.valueOf(attachmentObj.getFolderId()));
		
		if (attachmentObj.getRtfFlag()) {
			webRequest.setHeaderField(attachments.RTF_FLAG, String.valueOf(attachmentObj.getRtfFlag()));
		}
		
		WebResponse webResponse = webCon.getResponse(webRequest);
		assertEquals(207, webResponse.getResponseCode());
		
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(webResponse.getText().getBytes("UTF-8"));
		
		Document doc = new SAXBuilder().build(byteArrayInputStream);
		return parseResponse(doc, false);
	}
	
	public static InputStream loadAttachment(WebConversation webCon, AttachmentMetadata attachmentObj, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		WebRequest webRequest = new GetMethodWebRequest(host + ATTACHMENT_URL);
		webRequest.setHeaderField("Authorization", "Basic " + getAuthData(login, password));
		webRequest.setHeaderField(attachments.MODULE, String.valueOf(attachmentObj.getModuleId()));
		webRequest.setHeaderField(attachments.TARGET_ID, String.valueOf(attachmentObj.getAttachedId()));
		webRequest.setHeaderField(attachments.TARGET_FOLDER_ID, String.valueOf(attachmentObj.getFolderId()));
		webRequest.setHeaderField(DataFields.OBJECT_ID, String.valueOf(attachmentObj.getId()));
		
		WebResponse webResponse = webCon.getResponse(webRequest);

		assertEquals(200, webResponse.getResponseCode());
		
		return webResponse.getInputStream();
	}
	
	protected void deleteAttachment(WebConversation webCon, AttachmentMetadata attachmentObj, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, password));
		DeleteMethod deleteMethod = new DeleteMethod(host + ATTACHMENT_URL);
		deleteMethod.setDoAuthentication( true );
		deleteMethod.setRequestHeader(attachments.MODULE, String.valueOf(attachmentObj.getModuleId()));
		deleteMethod.setRequestHeader(attachments.TARGET_ID, String.valueOf(attachmentObj.getAttachedId()));
		deleteMethod.setRequestHeader(DataFields.OBJECT_ID, String.valueOf(attachmentObj.getId()));
		deleteMethod.setRequestHeader(attachments.TARGET_FOLDER_ID, String.valueOf(attachmentObj.getFolderId()));
		
		httpclient.executeMethod(deleteMethod);
		
		assertEquals(200, deleteMethod.getStatusCode());
	}
	
	public void compareAttachments(AttachmentMetadata attachmentObj1, AttachmentMetadata attachmentObj2) throws Exception {
		assertEquals("filename is not equals", attachmentObj1.getFilename(), attachmentObj2.getFilename());
		assertEquals("module is not equals", attachmentObj1.getModuleId(), attachmentObj2.getModuleId());
		assertEquals("target id is not equals", attachmentObj1.getAttachedId(), attachmentObj2.getAttachedId());
		assertEquals("target folder id is not equals", attachmentObj1.getFolderId(), attachmentObj2.getFolderId());
		assertEquals("rtf flag is not equals", attachmentObj1.getRtfFlag(), attachmentObj2.getRtfFlag());
	}
}

