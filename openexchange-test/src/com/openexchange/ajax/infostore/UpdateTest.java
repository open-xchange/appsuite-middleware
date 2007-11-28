package com.openexchange.ajax.infostore;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.json.JSONObject;

import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.test.TestInit;
import com.openexchange.test.OXTestToolkit;


public class UpdateTest extends InfostoreAJAXTest {

	public static final int SIZE = 15; // Size of the large file in Megabytes
	
	private static final byte[] megabyte = new byte[1000000];

	String LOREM_IPSUM = "[32] Sed ut perspiciatis, unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam eaque ipsa, quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt, explicabo. Nemo enim ipsam voluptatem, quia voluptas sit, aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos, qui ratione voluptatem sequi nesciunt, neque porro quisquam est, qui dolorem ipsum, quia dolor sit, amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt, ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit, qui in ea voluptate velit esse, quam nihil molestiae consequatur, vel illum, qui dolorem eum fugiat, quo voluptas nulla pariatur? [33] At vero eos et accusamus et iusto odio dignissimos ducimus, qui blanditiis praesentium voluptatum deleniti atque corrupti, quos dolores et quas molestias excepturi sint, obcaecati cupiditate non provident, similique sunt in culpa, qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio, cumque nihil impedit, quo minus id, quod maxime placeat, facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet, ut et voluptates repudiandae sint et molestiae non recusandae. Itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat.";

	
	public UpdateTest(String name) {
		super(name);
	}
	
	public void testBasic() throws Exception{
		Response res = this.update(getWebConversation(), getHostName(),sessionId,clean.get(0), System.currentTimeMillis(), m(
				"title" , "test knowledge updated",
				"color_label" , "1"
		));
		assertNoError(res);
		
		
		res = get(getWebConversation(), getHostName(), sessionId, clean.get(0));
		assertNoError(res);
		
		JSONObject object = (JSONObject) res.getData();
		
		
		assertEquals("test knowledge updated", object.getString("title"));
		assertEquals("test knowledge description", object.getString("description"));
		assertEquals(1, object.getInt("color_label"));
		
		assertEquals(0, object.getInt("version"));
		
	}
	
	public void testLongDescription() throws Exception {
		descriptionRoundtrip(LOREM_IPSUM);
	}
	
	public void testCharset() throws Exception {
		descriptionRoundtrip("Hölööööööö");
	}
	
	private void descriptionRoundtrip(String desc) throws Exception{
		Response res = this.update(getWebConversation(), getHostName(),sessionId,clean.get(0), System.currentTimeMillis(), m(
				"description" , desc
		));
		assertNoError(res);
		res = get(getWebConversation(), getHostName(), sessionId, clean.get(0));
		assertNoError(res);
		
		res = get(getWebConversation(), getHostName(), sessionId, clean.get(0));
		assertNoError(res);
		JSONObject object = (JSONObject) res.getData();
		assertEquals(desc, object.getString("description"));
	}
	
	public void testConflict() throws Exception{
		Response res = this.get(getWebConversation(),getHostName(), sessionId, clean.get(0));
		Response res2 = this.update(getWebConversation(), getHostName(),sessionId,clean.get(0), res.getTimestamp().getTime()-2000, m(
					"title" , "test knowledge updated"
			));
		assertNotNull(res2.getErrorMessage());
		assertFalse("".equals(res2.getErrorMessage()));
		
	}
	
	public void testUpload() throws Exception{
		File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
		
		int id = clean.get(0);
		
		Response res = update(getWebConversation(),getHostName(),sessionId,id,System.currentTimeMillis(),m(), upload, "text/plain");
		assertNoError(res);
		
		res = get(getWebConversation(),getHostName(), sessionId, id);
		JSONObject obj = (JSONObject) res.getData();
		
		assertEquals(1,obj.getInt("version"));
		
		assertEquals("text/plain",obj.getString("file_mimetype"));
		assertEquals(upload.getName(),obj.getString("filename"));

		InputStream is = null;
		InputStream is2 = null;
		try {
			is = new FileInputStream(upload);
			is2 = document(getWebConversation(),getHostName(),sessionId, id, 1);
			OXTestToolkit.assertSameContent(is,is2);
		} finally {
			if(is!=null)
				is.close();
			if(is2!=null)
				is2.close();
		}
	}
	
	//Bug 4120
	public void testUniqueFilenamesOnUpload() throws Exception {
		File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
		
		int id = clean.get(0);
		
		Response res = update(getWebConversation(),getHostName(),sessionId,id,System.currentTimeMillis(),m(), upload, "text/plain");
		assertNoError(res);
		
		int id2 = createNew(getWebConversation(), getHostName(), sessionId, m("title" , "otherFile", "description","other_desc", "folder_id" ,	((Integer)folderId).toString()));
		
		clean.add(id2);
		
		res = update(getWebConversation(),getHostName(),sessionId,id2,System.currentTimeMillis(),m(), upload, "text/plain");
		assertTrue(res.hasError());
	}
	
	//Bug 4120
	public void testUniqueFilenamesOnSwitchVersions() throws Exception {
		File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
		
		int id = clean.get(0);
		
		Response res = update(getWebConversation(),getHostName(),sessionId,id,System.currentTimeMillis(),m("filename" , "theFile.txt"), upload, "text/plain");
		assertNoError(res);
		
		res = update(getWebConversation(),getHostName(),sessionId,id,System.currentTimeMillis(),m(), upload, "text/plain");
		assertNoError(res);
		
		int id2 = createNew(getWebConversation(), getHostName(), sessionId, m("title" , "otherFile", "description","other_desc","filename","theFile.txt","folder_id" ,	((Integer)folderId).toString()),upload,"text/plain");
		
		clean.add(id2);
		
		res = update(getWebConversation(),getHostName(),sessionId,id,System.currentTimeMillis(),m("title","otherTitle","version","1"));
		assertTrue(res.hasError());
		
	}
	
	public void notestLargeFileUpload() throws Exception{
		File largeFile = File.createTempFile("test","bin");
		largeFile.deleteOnExit();
		
		BufferedOutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(largeFile),1000000);
			for(int i = 0; i < SIZE; i++) {
				out.write(megabyte);
				out.flush();
			}
		} finally {
			if(out != null)
				out.close();
		}
		
		try {
			int id = createNew(
					getWebConversation(),
					getHostName(),
					sessionId,
					m(
							"folder_id" 		,	((Integer)folderId).toString(),
							"title"  		,  	"test large upload",
							"description" 	, 	"test large upload description"
					), largeFile, "text/plain"
			);
			clean.add(id);
			fail("Uploaded Large File and got no error");
		} catch (Exception x) {
			assertTrue(true);
		}
	}
	
	public void testSwitchVersion() throws Exception{
		File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
		
		int id = clean.get(0);
		
		Response res = update(getWebConversation(),getHostName(),sessionId,id,System.currentTimeMillis(),m(), upload, "text/plain"); // V1
		assertNoError(res);
		
		res = update(getWebConversation(),getHostName(),sessionId,id,System.currentTimeMillis(),m(), upload, "text/plain");// V2
		assertNoError(res);
		
		res = update(getWebConversation(),getHostName(),sessionId,id,System.currentTimeMillis(),m(), upload, "text/plain");// V3
		assertNoError(res);
		
		res = update(getWebConversation(),getHostName(),sessionId,id, System.currentTimeMillis(), m("version" , "2"));
		assertNoError(res);
		
		res = get(getWebConversation(),getHostName(), sessionId, id);
		JSONObject obj = (JSONObject) res.getData();
		assertEquals(2,obj.get("version"));
		
		res = versions(getWebConversation(),getHostName(), sessionId, id, new int[]{Metadata.VERSION, Metadata.CURRENT_VERSION});
		assertNoError(res);
		
		VersionsTest.assureVersions(new Integer[]{1,2,3},res,2);
	}
	
	public void testUpdateCurrentVersionByDefault() throws Exception{
		File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
		
		int id = clean.get(0);
		
		Response res = update(getWebConversation(),getHostName(),sessionId,id,System.currentTimeMillis(),m(), upload, "text/plain"); // V1
		assertNoError(res);
		
		res = update(getWebConversation(),getHostName(),sessionId,id,System.currentTimeMillis(),m(), upload, "text/plain");// V2
		assertNoError(res);
		
		res = update(getWebConversation(),getHostName(),sessionId,id,System.currentTimeMillis(),m("description","New Description"));
		assertNoError(res);
		
		res = get(getWebConversation(), getHostName(), sessionId, id);
		assertNoError(res);
		
		JSONObject document = (JSONObject) res.getData();
		assertEquals("New Description", document.get("description"));
	}
	
	// Bug 3928
	public void testVersionCommentForNewVersion() throws Exception {
		File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
		
		int id = clean.get(0);
		
		Response res = update(getWebConversation(),getHostName(),sessionId,id,System.currentTimeMillis(),m("version_comment","Version Comment"), upload, "text/plain"); // V1
		assertNoError(res);
		
		res = get(getWebConversation(), getHostName(), sessionId, id);
		assertNoError(res);
		
		JSONObject document = (JSONObject)res.getData();
		assertEquals("Version Comment",document.get("version_comment"));
	}
	
	// Bug 4269
	public void testVirtualFolder() throws Exception {
		Response res = update(getWebConversation(), getHostName(), sessionId, clean.get(0), System.currentTimeMillis(), m("folder_id", ""+FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID));
		assertTrue(res.hasError());
		assertTrue(res.getErrorMessage(), res.getErrorMessage().contains("virt"));
	}
		
}
