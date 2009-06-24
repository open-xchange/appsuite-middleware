package com.openexchange.ajax.contact;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;

import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.contact.action.UpdatesRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.groupware.container.Contact;

public class UpdatesTest extends ContactTest {

	private static final Log LOG = LogFactory.getLog(UpdateTest.class);
	
	public UpdatesTest(final String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testUpdates() throws Exception {
		listModifiedAppointment(getWebConversation(), contactFolderId, new Date(0), PROTOCOL + getHostName(), getSessionId());
	}

    // Node 2652
    public void testLastModifiedUTC() throws Exception {
        final AJAXClient client = new AJAXClient(new AJAXSession(getWebConversation(), getSessionId()));
        final int cols[] = new int[]{ Contact.OBJECT_ID, Contact.FOLDER_ID, Contact.LAST_MODIFIED_UTC};

        final Contact contactObj = createContactObject("testLastModifiedUTC");
		final int objectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
        try {
            final UpdatesRequest updatesRequest = new UpdatesRequest(contactFolderId, cols, -1, null, new Date(0));
            final AbstractAJAXResponse response = Executor.execute(client, updatesRequest);
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
            deleteContact(getWebConversation(), objectId, contactFolderId, getHostName(), getSessionId());
        }
    }
}

