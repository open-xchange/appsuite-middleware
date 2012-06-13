package com.openexchange.ajax.contact;

import java.util.Date;
import org.json.JSONArray;
import com.openexchange.ajax.contact.action.UpdatesRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.groupware.container.Contact;

public class UpdatesTest extends AbstractContactTest {

	public UpdatesTest(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testUpdates() throws Exception {
		listModifiedAppointment(contactFolderId, new Date(0));
	}

    // Node 2652
    public void testLastModifiedUTC() throws Exception {
        final int cols[] = new int[]{ Contact.OBJECT_ID, Contact.FOLDER_ID, Contact.LAST_MODIFIED_UTC};

        final Contact contactObj = createContactObject("testLastModifiedUTC");
		final int objectId = insertContact(contactObj);
        try {
            final UpdatesRequest updatesRequest = new UpdatesRequest(contactFolderId, cols, -1, null, new Date(0));
            final AbstractAJAXResponse response = client.execute(updatesRequest);
            final JSONArray arr = (JSONArray) response.getResponse().getData();

            assertNotNull(arr);
            final int size = arr.length();
            assertTrue(size > 0);
            for(int i = 0; i < size; i++ ){
                final JSONArray objectData = arr.optJSONArray(i);
                assertNotNull(objectData);
                assertNotNull(objectData.opt(2));
            }
        } finally {
            try {
                deleteContact(objectId, contactFolderId, true);
            } catch (final Exception e) {
                // ignore
            }
        }
    }
}

