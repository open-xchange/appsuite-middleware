package com.openexchange.ajax.infostore;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.groupware.infostore.utils.Metadata;

public class DeleteTest extends InfostoreAJAXTest {

	public DeleteTest(final String name) {
		super(name);
	}

	public void testBasic() throws Exception{
        super.removeAll();

        final Response res = this.all(getWebConversation(),getHostName(),sessionId, folderId, new int[]{Metadata.ID});

		assertNoError(res);

		final JSONArray a = (JSONArray) res.getData();

		assertEquals(0, a.length());

		clean.clear();
	}

	public void testConflict() throws Exception{

		final String[][] toDelete = new String[clean.size()][2];

		for(int i = 0; i < toDelete.length; i++) {
			toDelete[i][0] = String.valueOf(folderId);
			toDelete[i][1] = clean.get(i);
		}

		String[] notDeleted = delete(getWebConversation(),getHostName(),sessionId, 0, toDelete);
		assertEquals(toDelete.length,notDeleted.length);

		Set<String> notDeletedExpect = new HashSet<String>(clean);

		for(final String i : notDeleted) {
			assertTrue(notDeletedExpect.remove(i));
		}
		assertTrue(notDeletedExpect.isEmpty());

		removeDocumentsAndFolders();
		clean.clear();

		final AJAXClient client = new AJAXClient(new AJAXSession(getWebConversation(), getHostName(), sessionId), false);
		String infostoreTrashFolder = String.valueOf(client.getValues().getInfostoreTrashFolder());
		notDeletedExpect = new HashSet<String>();
		for(int i = 0; i < toDelete.length; i++) {
            FileID fileID = new FileID(toDelete[i][1]);
            fileID.setFolderId(infostoreTrashFolder);
            String uniqueID = fileID.toUniqueID();

            toDelete[i][0] = infostoreTrashFolder;
            toDelete[i][1] = uniqueID;
            notDeletedExpect.add(uniqueID);
        }
		notDeleted = delete(getWebConversation(),getHostName(),sessionId, 0, toDelete);
		assertEquals(toDelete.length,notDeleted.length);

		for(final String i : notDeleted) {
			assertTrue(notDeletedExpect.remove(i));
		}
		assertTrue(notDeletedExpect.isEmpty());

	}


    public void testDeleteSingle() throws JSONException, IOException, SAXException {
        final String[] notDeleted = deleteSingle(getWebConversation(), getHostName(), sessionId, Long.MAX_VALUE, folderId, clean.get(clean.size()-1));
        assertEquals(0, notDeleted.length);

    }

}
