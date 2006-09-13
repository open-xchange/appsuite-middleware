package com.openexchange.ajax.infostore;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.json.JSONObject;

import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.attach.TaskAttachmentTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.attach.AttachmentMetadata;

public class SaveAsTest extends InfostoreAJAXTest {
	
	private TaskAttachmentTest attachmentTest = new TaskAttachmentTest("TaskAttachmentTest");
	
	
	public void testBasic() throws Exception {
		AttachmentMetadata attachment = attachmentTest.getAttachment(0);
		int id = saveAs(getWebConversation(), sessionId, attachment.getFolderId(),attachment.getAttachedId(), attachment.getModuleId(),attachment.getId(), m(
				"folder_id"			,		""+folderId,
				"title"				,		"My Attachment",
				"description"		,		"An attachment cum InfoItem"
		));
		
		clean.add(id);
		
		Response res = get(getWebConversation(),sessionId, id);
		JSONObject obj = (JSONObject) res.getData();
		
		File upload = attachmentTest.getTestFile();
		
		assertEquals("My Attachment",obj.getString("title"));
		assertEquals("An attachment cum InfoItem",obj.getString("description"));
		assertEquals(1,obj.getInt("version"));
		assertEquals(upload.getName(),obj.getString("filename"));

		
		InputStream is = null;
		InputStream is2 = null;
		try {
			is = new FileInputStream(upload);
			is2 = document(getWebConversation(),sessionId,id, 1);
			assertSameContent(is,is2);
		} finally {
			if(is!=null)
				is.close();
			if(is2!=null)
				is2.close();
		}
	}
	
	
	public void setUp() throws Exception{
		attachmentTest.setUp();
		attachmentTest.upload();
		super.setUp();
	}
	
	public void tearDown() throws Exception {
		attachmentTest.tearDown();
		super.tearDown();
	}
}
