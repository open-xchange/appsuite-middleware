package com.openexchange.ajax.contact;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;

import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.contact.action.AllRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.groupware.container.ContactObject;

public class AllTest extends ContactTest {

	private static final Log LOG = LogFactory.getLog(AllTest.class);
	
	public AllTest(final String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}	

    //FIXME: This is no test!
    public void testAll() throws Exception {
		final int cols[] = new int[]{ ContactObject.OBJECT_ID };
		
		final ContactObject[] contactArray = listContact(getWebConversation(), contactFolderId, cols, PROTOCOL + getHostName(), getSessionId());
	}

    // Node 2652
    public void testLastModifiedUTC() throws Exception {
        AJAXClient client = new AJAXClient(new AJAXSession(getWebConversation(), getSessionId()));
        final int cols[] = new int[]{ ContactObject.OBJECT_ID, ContactObject.FOLDER_ID, ContactObject.LAST_MODIFIED_UTC};

        final ContactObject contactObj = createContactObject("testLastModifiedUTC");
		final int objectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
        try {
            AllRequest req = new AllRequest(contactFolderId, cols);
            
            CommonAllResponse response = Executor.execute(client, req);
            JSONArray arr = (JSONArray) response.getResponse().getData();

            assertNotNull(arr);
            int size = arr.length();
            assertTrue(size > 0);
            for(int i = 0; i < size; i++ ){
                JSONArray objectData = arr.optJSONArray(i);
                assertNotNull(objectData);
                assertNotNull(objectData.opt(2));
            }
        } finally {
            deleteContact(getWebConversation(), objectId, contactFolderId, getHostName(), getSessionId());
        }
    }
    
}