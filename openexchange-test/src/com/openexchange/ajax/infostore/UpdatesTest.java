package com.openexchange.ajax.infostore;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.test.TestInit;

public class UpdatesTest extends InfostoreAJAXTest{

	public UpdatesTest(final String name) {
		super(name);
	}

	public void testBasic() throws Exception{
		Response res = all(getWebConversation(),getHostName(),sessionId, folderId, new int[]{Metadata.ID});
		assertNoError(res);
		long ts = res.getTimestamp().getTime()+2;

		this.update(getWebConversation(), getHostName(),sessionId,clean.get(0), ts, m(
				"title" , "test knowledge updated"
		));

		res = updates(getWebConversation(),getHostName(),sessionId,folderId, new int[]{Metadata.TITLE, Metadata.DESCRIPTION}, ts);
		assertNoError(res);

		JSONArray modAndDel = (JSONArray) res.getData();


		assertEquals(1, modAndDel.length());

		final JSONArray fields = modAndDel.getJSONArray(0);

		assertEquals("test knowledge updated", fields.getString(0));
		assertEquals("test knowledge description", fields.getString(1));

		final Set<Integer> ids = new HashSet<Integer>(clean);

		ts = res.getTimestamp().getTime();

		removeAll();

		res = updates(getWebConversation(),getHostName(),sessionId,folderId, new int[]{Metadata.TITLE,Metadata.DESCRIPTION}, ts);

		assertNoError(res);

		modAndDel = (JSONArray) res.getData();

		assertEquals(2,modAndDel.length());

		for(int i = 0; i < 2; i++) {
			assertTrue(ids.remove(modAndDel.getInt(i)));
		}

		assertTrue(ids.isEmpty());
	}

	public void testRemovedVersionForcesUpdate() throws Exception{
		Response res = all(getWebConversation(),getHostName(),sessionId, folderId, new int[]{Metadata.ID});
		assertNoError(res);
		final long ts = res.getTimestamp().getTime()+2;

		final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
		res = update(getWebConversation(),getHostName(),sessionId,clean.get(0),Long.MAX_VALUE,m("version_comment" , "Comment 1"), upload, "text/plain");
		assertNoError(res);
		res = update(getWebConversation(),getHostName(),sessionId,clean.get(0),Long.MAX_VALUE,m("version_comment" , "Comment 2"), upload, "text/plain");
		assertNoError(res);
		res = update(getWebConversation(),getHostName(),sessionId,clean.get(0),Long.MAX_VALUE,m("version_comment" , "Comment 3"), upload, "text/plain");
		assertNoError(res);

		final int[] nd = detach(getWebConversation(), getHostName(), sessionId, ts, clean.get(0), new int[]{3});
		assertEquals(0, nd.length);

		res = updates(getWebConversation(),getHostName(),sessionId,folderId, new int[]{Metadata.TITLE, Metadata.DESCRIPTION}, ts);
		assertNoError(res);

		final JSONArray modAndDel = (JSONArray) res.getData();


		assertEquals(1, modAndDel.length());

	}

    //Bug 4269
	public void testVirtualFolder() throws Exception{

        for(int folderId : virtualFolders) {
            virtualFolderTest( folderId );
        }
	}

    //Bug 4269
	public void virtualFolderTest(int folderId) throws Exception {
		final Response res = updates(getWebConversation(), getHostName(), sessionId, folderId, new int[]{Metadata.ID}, 0);
		assertNoError(res);
		final JSONArray modAndDel = (JSONArray) res.getData();
		assertEquals(0, modAndDel.length());
	}

    // Node 2652
    public void testLastModifiedUTC() throws JSONException, IOException, SAXException {
        final Response res = updates(getWebConversation(),getHostName(),sessionId,folderId, new int[]{Metadata.LAST_MODIFIED_UTC}, 0);
		assertNoError(res);

		final JSONArray modAndDel = (JSONArray) res.getData();

		assertTrue(modAndDel.length() > 0);

		final JSONArray fields = modAndDel.getJSONArray(0);
        assertNotNull(fields.optLong(0));
    }

    // Bug 12427

    public void testNumberOfVersions() throws JSONException, IOException, SAXException {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        Response res = update(getWebConversation(),getHostName(),sessionId,clean.get(0),Long.MAX_VALUE,m("description","New description"), upload, "text/plain");
        assertNoError(res);

        res = updates(getWebConversation(),getHostName(),sessionId,folderId, new int[]{Metadata.ID, Metadata.NUMBER_OF_VERSIONS}, 0);

        JSONArray rows = (JSONArray) res.getData();
        boolean found = false;
        for(int i = 0, size = rows.length(); i < size; i++) {
            JSONArray row = rows.getJSONArray(i);
            int id = row.getInt(0);
            int numberOfVersions = row.getInt(1);

            if(id == clean.get(0)) {
                assertEquals(1, numberOfVersions);
                found = true;
            }
        }
        assertTrue(found);
    }
}
