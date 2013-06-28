package com.openexchange.ajax.infostore;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.attach.SimpleAttachmentTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.test.OXTestToolkit;

public class ZipDocumentsTest extends InfostoreAJAXTest {

	private final SimpleAttachmentTest attachmentTest = new SimpleAttachmentTest("InfoStoreAttachmentTest");

	public ZipDocumentsTest(final String name) {
		super(name);
	}

	public void testBasic() throws Exception {
		final AttachmentMetadata attachment = attachmentTest.getAttachment(0);
		final int id = saveAs(getWebConversation(), getHostName(), sessionId,attachment.getFolderId(), attachment.getAttachedId(),attachment.getModuleId(), attachment.getId(), m(
				"folder_id"			,		""+folderId,
				"title"				,		"My Attachment",
				"description"		,		"An attachment cum InfoItem"
		));

		clean.add(id);

		final Response res = get(getWebConversation(),getHostName(), sessionId, id);
		assertNotNull(res.getTimestamp());
        final JSONObject obj = (JSONObject) res.getData();

		final File upload = attachmentTest.getTestFile();

		assertEquals("My Attachment",obj.getString("title"));
		assertEquals("An attachment cum InfoItem",obj.getString("description"));
		assertEquals(1,obj.getInt("version"));
		assertEquals(upload.getName(),obj.getString("filename"));


		InputStream is = null;
		InputStream is2 = null;
		try {
			is = new FileInputStream(upload);
			is2 = document(getWebConversation(),getHostName(),sessionId, id, 1);
			OXTestToolkit.assertSameContent(is,is2);
		} finally {
			if(is!=null) {
				is.close();
			}
			if(is2!=null) {
				is2.close();
			}
		}
	}

    //Bug 4269
	public void testVirtualFolder() throws Exception{
        for(int folderId : virtualFolders) {
            virtualFolder( folderId );
        }
	}

    //Bug 4269
	public void virtualFolder(int folderId) throws Exception {
		final AttachmentMetadata attachment = attachmentTest.getAttachment(0);
		try {
			final int id = saveAs(getWebConversation(), getHostName(), sessionId,attachment.getFolderId(), attachment.getAttachedId(),attachment.getModuleId(), attachment.getId(), m(
					"folder_id"			,		""+folderId,
					"title"				,		"My Attachment",
					"description"		,		"An attachment cum InfoItem"
			));

			clean.add(id);
			fail("Expected IOException when trying to save attachment in virtual infostore folder");
		} catch (final JSONException x) {
			assertTrue(x.getMessage(), x.getMessage().contains("virt"));
		}

	}

	@Override
	public void setUp() throws Exception{
		attachmentTest.setUp();
		attachmentTest.upload();
		super.setUp();
	}

	@Override
	public void tearDown() throws Exception {
		attachmentTest.tearDown();
		super.tearDown();
	}
}
