package com.openexchange.ajax.attach;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AttachmentTest;
import com.openexchange.ajax.LoginTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.test.OXTestToolkit;

public abstract class AbstractAttachmentTest extends AttachmentTest {

	public AbstractAttachmentTest(String name) {
		super(name);
	}

	
	protected int attachedId = -1;
	protected int folderId = -1;
	
	
	protected int moduleId = -1;
	
	protected String sessionId;
	
	public void setUp() throws Exception{
		super.setUp();
		
		sessionId = getSessionId();
			
		folderId = getExclusiveWritableFolder(sessionId);
		attachedId = createExclusiveWritableAttachable(sessionId,folderId);
		
		moduleId = getModule();
	}
	
	public void tearDown() throws Exception {
		removeAttachments();
		removeAttachable(folderId, attachedId, sessionId);
	}
	
	public void removeAttachments() throws Exception{
		super.tearDown();
	}

	public abstract int createExclusiveWritableAttachable(String sessionId, int folderId) throws Exception;
	
	public abstract int getExclusiveWritableFolder(String sessionId) throws Exception;
	
	public abstract void removeAttachable(int folder, int id, String sessionId) throws Exception;
	
	public abstract int getModule() throws Exception;
	
	
	protected void doDetach() throws Exception {
		doGet();
		int id = clean.get(0).getId();
		removeAttachments();
		
		Response res = get(getWebConversation(),sessionId,folderId,attachedId,moduleId, id);
		assertTrue(res.hasError());
	}
	
	protected void doUpdates() throws Exception {
		upload();
		Response res = get(getWebConversation(), sessionId, folderId, attachedId, moduleId, clean.get(0).getId());
		assertNoError(res);
		long timestamp = res.getTimestamp().getTime();
		upload();
		upload();
		upload();
		upload();
		
		List<AttachmentMetadata> createdLater = new ArrayList<AttachmentMetadata>(clean.subList(1,clean.size()));
		
		res = updates(getWebConversation(),sessionId,folderId,attachedId, moduleId, timestamp, new int[]{AttachmentField.ID, AttachmentField.FILENAME}, AttachmentField.CREATION_DATE, "ASC");
		
		assertNoError(res);
		JSONArray arrayOfArrays = (JSONArray) res.getData();
		
		assertEquals(createdLater.size(), arrayOfArrays.length());
		
		for(int i = 0; i < arrayOfArrays.length(); i++) {
			JSONArray values = arrayOfArrays.getJSONArray(i);
			AttachmentMetadata attachment = createdLater.get(i);
			
			assertEquals(values.getInt(0),attachment.getId());
			assertEquals(testFile.getName(), values.getString(1));
		}
		
		List<AttachmentMetadata> copy = new ArrayList<AttachmentMetadata>(clean);
		removeAttachments();
		
		res = updates(getWebConversation(),sessionId,folderId,attachedId, moduleId, timestamp, new int[]{AttachmentField.ID, AttachmentField.FILENAME}, AttachmentField.CREATION_DATE, "ASC");
		
		JSONArray arrayOfIds = (JSONArray) res.getData();
		
		assertEquals(copy.size(), arrayOfIds.length());
		
		for(int i = 0; i < arrayOfIds.length(); i++) {
			int id = arrayOfIds.getInt(i);
			AttachmentMetadata attachment = copy.get(i);
			
			assertEquals(id,attachment.getId());
		}
	}
	
	protected void doAll() throws Exception {
		upload();
		upload();
		upload();
		upload();
		upload();
		
		Response res = all(getWebConversation(),sessionId,folderId,attachedId, moduleId, new int[]{AttachmentField.ID, AttachmentField.FILENAME}, AttachmentField.CREATION_DATE, "ASC");
		assertNoError(res);
		JSONArray arrayOfArrays = (JSONArray) res.getData();
		
		assertEquals(clean.size(), arrayOfArrays.length());
		
		for(int i = 0; i < arrayOfArrays.length(); i++) {
			JSONArray values = arrayOfArrays.getJSONArray(i);
			AttachmentMetadata attachment = clean.get(i);
			
			assertEquals(values.getInt(0),attachment.getId());
			assertEquals(testFile.getName(), values.getString(1));
		}
	}
	
	protected void doList() throws Exception {
		upload();
		upload();
		upload();
		upload();
		upload();
		
		int[] ids = new int[]{
				clean.get(0).getId(),
				clean.get(2).getId(),
				clean.get(4).getId()
		};
		
		Response res = list(getWebConversation(),sessionId,folderId,attachedId, moduleId, ids, new int[]{AttachmentField.ID, AttachmentField.FILENAME});
		assertNoError(res);
		JSONArray arrayOfArrays = (JSONArray) res.getData();
		
		assertEquals(ids.length, arrayOfArrays.length());
		
		for(int i = 0; i < arrayOfArrays.length(); i++) {
			JSONArray values = arrayOfArrays.getJSONArray(i);
			
			assertEquals(ids[i], values.getInt(0));
			assertEquals(testFile.getName(), values.getString(1));
		}
	}
	
	protected void doGet() throws Exception {
		upload();
		Response res = get(getWebConversation(),sessionId,folderId,attachedId,moduleId, clean.get(0).getId());
		assertNoError(res);
		
		JSONObject data = (JSONObject) res.getData();
		
		assertEquals(folderId, data.getInt("folder"));
		assertEquals(attachedId, data.getInt("attached"));
		assertEquals(moduleId, data.getInt("module"));
		assertEquals(testFile.getName(),data.getString("filename"));
		assertEquals("text/plain", data.getString("file_mimetype"));
		assertEquals(testFile.length(), data.getLong("file_size"));
		assertEquals(clean.get(0).getId(), data.getInt("id"));
	}
	
	protected void doMultiple() throws Exception {
		AttachmentMetadata attachment = new AttachmentImpl();
		AttachmentMetadata attachment2 = new AttachmentImpl();
		
		attachment.setFolderId(folderId);
		attachment.setAttachedId(attachedId);
		attachment.setModuleId(moduleId);
		
		attachment2.setFolderId(folderId);
		attachment2.setAttachedId(attachedId);
		attachment2.setModuleId(moduleId);
		
		
		Response res = attach(getWebConversation(),sessionId,folderId,attachedId,moduleId, Arrays.asList(testFile, testFile2));
		assertNoError(res);
		
		attachment.setId(((JSONArray)res.getData()).getInt(0));
		attachment2.setId(((JSONArray)res.getData()).getInt(1));
		clean.add(attachment);
		clean.add(attachment2);
		
		assertFilename(clean.get(0), testFile.getName());
		assertFilename(clean.get(1), testFile2.getName());
	}
	
	protected final void assertFilename(AttachmentMetadata att, String filename) throws MalformedURLException, JSONException, IOException, SAXException {
		Response res = get(getWebConversation(),sessionId,folderId,attachedId,moduleId, att.getId());
		assertNoError(res);
		
		JSONObject data = (JSONObject) res.getData();
		
		assertEquals(filename,data.getString("filename"));
	}
		
	protected void doDocument() throws Exception {
		upload();
		
		InputStream data = null;
		InputStream local = null;
		
		try {
			data = document(getWebConversation(),sessionId,folderId,attachedId,moduleId, clean.get(0).getId());
			OXTestToolkit.assertSameContent(local = new FileInputStream(testFile),data);
		} finally {
			if(data != null)
				data.close();
			if(local != null)
				local.close();
		}
		
		GetMethodWebRequest req = documentRequest(sessionId, folderId, attachedId, moduleId, clean.get(0).getId(), null);
		WebResponse resp = getWebConversation().getResource(req);
		assertEquals("text/plain",resp.getContentType());
		
		req = documentRequest(sessionId, folderId, attachedId, moduleId, clean.get(0).getId(), "application/octet-stream");
		resp = getWebConversation().getResource(req);
		assertEquals("application/octet-stream",resp.getContentType());
		
	}
	
	protected void doNotExists() throws Exception {
		int imaginaryFolderFriend = Integer.MAX_VALUE;
		int imaginaryObjectFriend = Integer.MAX_VALUE;
		
		AttachmentMetadata attachment = new AttachmentImpl();
		attachment.setFolderId(imaginaryFolderFriend);
		attachment.setAttachedId(attachedId);
		attachment.setModuleId(moduleId);
		
		Response res = attach(getWebConversation(),sessionId,imaginaryFolderFriend,attachedId,moduleId, testFile);
		assertTrue(res.hasError());
		
		attachment.setFolderId(folderId);
		attachment.setAttachedId(imaginaryObjectFriend);
		
		res = attach(getWebConversation(),sessionId,imaginaryFolderFriend,attachedId,moduleId, testFile);
		assertTrue(res.hasError());	
	}
	
	protected void doForbidden() throws Exception {
		AttachmentMetadata attachment = new AttachmentImpl();
		attachment.setFolderId(folderId);
		attachment.setAttachedId(attachedId);
		attachment.setModuleId(moduleId);
		
		Response res = attach(getSecondWebConversation(),getSecondSessionId(),folderId,attachedId,moduleId, testFile);
		assertTrue(res.hasError());
		
	}
	
	public void refreshSessionId() throws IOException, SAXException, JSONException{
		sessionId = LoginTest.getSessionId(getWebConversation(), getHostName(),
                getLogin(), getPassword());
	}
	
	protected void doQuota() throws Exception {
		Response res = quota(getWebConversation(), sessionId);
		assertNoError(res);
		JSONObject quota = (JSONObject)res.getData(); 
		int use = quota.getInt("use");
		
		upload();
		
		res = quota(getWebConversation(), sessionId);
		assertNoError(res);
		quota = (JSONObject)res.getData();
		int useAfter = quota.getInt("use");
		
		res = get(getWebConversation(),sessionId, clean.get(0).getFolderId(), clean.get(0).getAttachedId(), clean.get(0).getAttachedId(), clean.get(0).getId());
		assertNoError(res);
		
		assertEquals(useAfter-use,((JSONObject)res.getData()).get("file_size"));
	}
	
	public void upload() throws Exception {
		AttachmentMetadata attachment = new AttachmentImpl();
		attachment.setFolderId(folderId);
		attachment.setAttachedId(attachedId);
		attachment.setModuleId(moduleId);
		
		Response res = attach(getWebConversation(),sessionId,folderId,attachedId,moduleId, testFile);
		assertNoError(res);
		
		attachment.setId(((JSONArray)res.getData()).getInt(0));
		clean.add(attachment);
	}
	
	public AttachmentMetadata getAttachment(int index) {
		return clean.get(index);
	}
	
	public File getTestFile(){
		return testFile;
	}
	
}
