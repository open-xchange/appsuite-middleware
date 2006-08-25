package com.openexchange.webdav.xml;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.api.OXAttachment;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import java.io.ByteArrayInputStream;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

public class AttachmentTest extends AbstractWebdavTest {
	
	public static final String ATTACHMENT_URL = "/servlet/webdav.attachments";

	protected StringBuffer data = new StringBuffer();	

	protected void setUp() throws Exception {
		super.setUp();

		data.append("DATA MIT VIEL TEST\n");
		data.append("123465678901223456\n");
	}
	
	public void testInsertAttachment() throws Exception {
		FolderObject folderObj = FolderTest.getContactDefaultFolder(webCon, PROTOCOL + hostName, login, password);
		int contactFolderId = folderObj.getObjectID();
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testInsertAttachment");
		contactObj.setParentFolderID(contactFolderId);
		int objectId = ContactTest.insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		insertAttachment(System.currentTimeMillis() + "test.txt", Types.CONTACT, objectId, contactFolderId,  false);
	}

	public void testLoadAttachment() throws Exception {
		FolderObject folderObj = FolderTest.getContactDefaultFolder(webCon, PROTOCOL + hostName, login, password);
		int contactFolderId = folderObj.getObjectID();
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testLoadAttachment");
		contactObj.setParentFolderID(contactFolderId);
		int objectId = ContactTest.insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		int attachmentId = insertAttachment(System.currentTimeMillis() + "test.txt", Types.CONTACT, objectId, contactFolderId, false);
		loadAttachment(attachmentId, Types.CONTACT, objectId, contactFolderId, false);
	}
	
	public void testLoadAttachmentWithRtf() throws Exception {
		FolderObject folderObj = FolderTest.getContactDefaultFolder(webCon, PROTOCOL + hostName, login, password);
		int contactFolderId = folderObj.getObjectID();
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testLoadAttachmentWithRtf");
		contactObj.setParentFolderID(contactFolderId);
		int objectId = ContactTest.insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		int attachmentId = insertAttachment(System.currentTimeMillis() + "test.txt", Types.CONTACT, objectId, contactFolderId, true);
		loadAttachment(objectId, Types.CONTACT, objectId, contactFolderId, true);
	}
	
	public void testDeleteAttachment() throws Exception {
		FolderObject folderObj = FolderTest.getContactDefaultFolder(webCon, PROTOCOL + hostName, login, password);
		int contactFolderId = folderObj.getObjectID();
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testDeleteAttachment");
		contactObj.setParentFolderID(contactFolderId);
		int objectId = ContactTest.insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		int attachmentId = insertAttachment(System.currentTimeMillis() + "test.txt", Types.CONTACT, objectId, contactFolderId, false);
		deleteAttachment(attachmentId, Types.TASK, objectId, contactFolderId);
	}
	
	protected int insertAttachment(String filename, int module, int targetId, int targetFolderId, boolean rtf) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(data.toString().getBytes());
		req = new PutMethodWebRequest(hostName + ATTACHMENT_URL, bais, "text/plain");
		req.setHeaderField("Authorization", "Basic " + authData);
		req.setHeaderField(OXAttachment.FILENAME, filename);
		req.setHeaderField(OXAttachment.MODULE, String.valueOf(module));
		req.setHeaderField(OXAttachment.TARGET_ID, String.valueOf(targetId));
		req.setHeaderField(OXAttachment.TARGET_FOLDER_ID, String.valueOf(targetId));
		
		if (rtf) {
			req.setHeaderField(OXAttachment.RTF_FLAG, String.valueOf(rtf));
		}
		
		resp = webCon.getResponse(req);
		
		bais = new ByteArrayInputStream(resp.getText().getBytes("UTF-8"));
		
		Document doc = new SAXBuilder().build(bais);
		return parseResponse(doc, false);
	}
	
	protected void loadAttachment(int objectId, int module, int targetId, int targetFolderId, boolean rtf) throws Exception {
		WebRequest req = new GetMethodWebRequest(hostName + ATTACHMENT_URL);
		WebResponse resp = webCon.getResponse(req);
		req.setHeaderField(OXAttachment.MODULE, String.valueOf(module));
		req.setHeaderField(OXAttachment.TARGET_ID, String.valueOf(targetId));
		req.setHeaderField(OXAttachment.OBJECT_ID, String.valueOf(objectId));
		
		assertEquals(200, resp.getResponseCode());
		
		if (rtf) {
			assertEquals("check rtf flag", "true", resp.getHeaderField(OXAttachment.RTF_FLAG));
		} 
		
		assertEquals("check response body size", data.length(), resp.getText().length());
		assertEquals("check response body", data.toString(), resp.getText());
	}
	
	protected void deleteAttachment(int objectId, int module, int targetId, int targetFolderId) throws Exception {
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(null, new UsernamePasswordCredentials(login, password));
		DeleteMethod deleteMethod = new DeleteMethod(hostName + ATTACHMENT_URL);
		deleteMethod.setDoAuthentication( true );
		deleteMethod.setRequestHeader(OXAttachment.MODULE, String.valueOf(module));
		deleteMethod.setRequestHeader(OXAttachment.TARGET_ID, String.valueOf(targetId));
		deleteMethod.setRequestHeader(OXAttachment.OBJECT_ID, String.valueOf(objectId));
		deleteMethod.setRequestHeader(OXAttachment.TARGET_FOLDER_ID, String.valueOf(objectId));
		
		assertEquals(200, resp.getResponseCode());
	}
}

