package com.openexchange.ajax.contact;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.contact.action.GetResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.groupware.container.ContactObject;

public class GetTest extends ContactTest {
	
	private static final Log LOG = LogFactory.getLog(ContactTest.class);
	
	public GetTest(final String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testGet() throws Exception {
		final ContactObject contactObj = createContactObject("testGet");
		final int objectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		
		loadContact(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testGetWithAllFields() throws Exception {
		final ContactObject contactObject = createCompleteContactObject();
		
		final int objectId = insertContact(getWebConversation(), contactObject, PROTOCOL + getHostName(), getSessionId());
		
		final ContactObject loadContact = loadContact(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
		
		contactObject.setObjectID(objectId);
		compareObject(contactObject, loadContact);
	}
	
	public void testGetWithAllFieldsOnUpdate() throws Exception {
		ContactObject contactObject = new ContactObject();
		contactObject.setSurName("testGetWithAllFieldsOnUpdate");
		contactObject.setParentFolderID(contactFolderId);
		
		final int objectId = insertContact(getWebConversation(), contactObject, PROTOCOL + getHostName(), getSessionId());
		
		contactObject = createCompleteContactObject();
		
		updateContact(getWebConversation(), contactObject, objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
		
		final ContactObject loadContact = loadContact(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
		
		contactObject.setObjectID(objectId);
		compareObject(contactObject, loadContact);
	}
	
	public void testGetUser() throws Exception {
		ContactObject loadContact = loadUser(getWebConversation(), userId, getHostName(), getSessionId());
		assertNotNull("contact object is null", loadContact);
		assertEquals("user id is not equals", userId, loadContact.getInternalUserId());
		assertTrue("object id not set", loadContact.getObjectID() > 0);
		final GetResponse response = new AJAXClient(new AJAXSession(getWebConversation(), getSessionId())).execute(
		    new com.openexchange.ajax.user.actions.GetRequest(userId));
		loadContact = response.getContact();
		assertNotNull("contact object is null", loadContact);
        assertEquals("user id is not equals", userId, loadContact.getInternalUserId());
        assertTrue("object id not set", loadContact.getObjectID() > 0);
	}

    // Node 2652
    public void testLastModifiedUTC() throws Exception {
        final AJAXClient client = new AJAXClient(new AJAXSession(getWebConversation(), getSessionId()));

        final ContactObject contactObj = createContactObject("testNew");
		final int objectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
        try {
            final GetRequest req = new GetRequest(contactFolderId, objectId);

            final AbstractAJAXResponse response = Executor.execute(client, req);
            final JSONObject contact = (JSONObject) response.getResponse().getData();
            assertTrue(contact.has("last_modified_utc"));

        } finally {
            deleteContact(getWebConversation(), objectId, contactFolderId, getHostName(), getSessionId());
        }
    }
}