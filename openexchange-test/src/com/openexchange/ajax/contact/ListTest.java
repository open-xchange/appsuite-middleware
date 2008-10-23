package com.openexchange.ajax.contact;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;

import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.contact.action.ListRequest;
import com.openexchange.ajax.framework.*;
import com.openexchange.groupware.container.ContactObject;

public class ListTest extends ContactTest {
	
	private static final Log LOG = LogFactory.getLog(ListTest.class);
	
	public ListTest(final String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testList() throws Exception {
		final ContactObject contactObj = createContactObject("testList");
		final int id1 = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		final int id2 = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		final int id3 = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		
		// prevent problems with master/slave
		Thread.sleep(1000);
		
		final int[][] objectIdAndFolderId = { { id1, contactFolderId }, { id2, contactFolderId }, { id3, contactFolderId } };
		
		final int cols[] = new int[]{ ContactObject.OBJECT_ID, ContactObject.SUR_NAME, ContactObject.DISPLAY_NAME } ;
		
		final ContactObject[] contactArray = listContact(getWebConversation(), objectIdAndFolderId, cols, PROTOCOL + getHostName(), getSessionId());
		
		assertEquals("check response array", 3, contactArray.length);
	}
	
	public void testListWithAllFields() throws Exception {
		final ContactObject contactObject = createCompleteContactObject();

		final int objectId = insertContact(getWebConversation(), contactObject, PROTOCOL + getHostName(), getSessionId());
		
		final int[][] objectIdAndFolderId = { { objectId, contactFolderId } };
		
		final ContactObject[] contactArray = listContact(getWebConversation(), objectIdAndFolderId, CONTACT_FIELDS, PROTOCOL + getHostName(), getSessionId());
		
		assertEquals("check response array", 1, contactArray.length);
		
		final ContactObject loadContact = contactArray[0];
		
		contactObject.setObjectID(objectId);
		compareObject(contactObject, loadContact);
	}

	public void testListWithNotExistingEntries() throws Exception {
		final ContactObject contactObject = createCompleteContactObject();
		
		final int objectId = insertContact(getWebConversation(), contactObject, PROTOCOL + getHostName(), getSessionId());
		final int objectId2 = insertContact(getWebConversation(), contactObject, PROTOCOL + getHostName(), getSessionId());
		
		final int cols[] = new int[]{ ContactObject.OBJECT_ID, ContactObject.SUR_NAME, ContactObject.DISPLAY_NAME } ;
		
		// not existing object last
		final int[][] objectIdAndFolderId1 = { { objectId, contactFolderId }, { objectId+100, contactFolderId } };
		ContactObject[] contactArray = listContact(getWebConversation(), objectIdAndFolderId1, cols, getHostName(), getSessionId());		
		assertEquals("check response array", 1, contactArray.length);

		// not existing object first
		final int[][] objectIdAndFolderId2 = { { objectId+100, contactFolderId }, { objectId, contactFolderId } };
		contactArray = listContact(getWebConversation(), objectIdAndFolderId2, cols, getHostName(), getSessionId());		
		assertEquals("check response array", 1, contactArray.length);
		
		// not existing object first
		final int[][] objectIdAndFolderId3 = { { objectId+100, contactFolderId }, { objectId, contactFolderId }, { objectId2, contactFolderId } };
		contactArray = listContact(getWebConversation(), objectIdAndFolderId3, cols, getHostName(), getSessionId());		
		assertEquals("check response array", 2, contactArray.length);
		
		deleteContact(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testListUser() throws Exception {
		final int[] userIdArray = new int[] { userId };
		final int cols[] = new int[]{ ContactObject.OBJECT_ID, ContactObject.SUR_NAME, ContactObject.DISPLAY_NAME } ;
		
		final ContactObject[] contactArray = listUser(getWebConversation(), userIdArray, cols, PROTOCOL + getHostName(), getSessionId());
		assertEquals("check response array", 1, contactArray.length);
	}

    // Node 2652
    public void testLastModifiedUTC() throws Exception {
        AJAXClient client = new AJAXClient(new AJAXSession(getWebConversation(), getSessionId()));
        final int cols[] = new int[]{ ContactObject.OBJECT_ID, ContactObject.FOLDER_ID, ContactObject.LAST_MODIFIED_UTC};

        final ContactObject contactObj = createContactObject("testLastModifiedUTC");
		final int objectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
        try {
            ListRequest listRequest = new ListRequest(ListIDs.l(new int[]{contactFolderId, objectId}), cols, true);
            CommonListResponse response = Executor.execute(client, listRequest);
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