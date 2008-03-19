package com.openexchange.ajax.contact;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.contact.action.GetResponse;
import com.openexchange.ajax.contact.action.UserGetRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.groupware.container.ContactObject;

public class GetTest extends ContactTest {
	
	private static final Log LOG = LogFactory.getLog(ContactTest.class);
	
	public GetTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testGet() throws Exception {
		ContactObject contactObj = createContactObject("testGet");
		int objectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		
		loadContact(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testGetWithAllFields() throws Exception {
		ContactObject contactObject = createCompleteContactObject();
		
		int objectId = insertContact(getWebConversation(), contactObject, PROTOCOL + getHostName(), getSessionId());
		
		ContactObject loadContact = loadContact(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
		
		contactObject.setObjectID(objectId);
		compareObject(contactObject, loadContact);
	}
	
	public void testGetWithAllFieldsOnUpdate() throws Exception {
		ContactObject contactObject = new ContactObject();
		contactObject.setSurName("testGetWithAllFieldsOnUpdate");
		contactObject.setParentFolderID(contactFolderId);
		
		int objectId = insertContact(getWebConversation(), contactObject, PROTOCOL + getHostName(), getSessionId());
		
		contactObject = createCompleteContactObject();
		
		updateContact(getWebConversation(), contactObject, objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
		
		ContactObject loadContact = loadContact(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
		
		contactObject.setObjectID(objectId);
		compareObject(contactObject, loadContact);
	}
	
	public void testGetUser() throws Exception {
		ContactObject loadContact = loadUser(getWebConversation(), userId, getHostName(), getSessionId());
		assertNotNull("contact object is null", loadContact);
		assertEquals("user id is not equals", userId, loadContact.getInternalUserId());
		assertTrue("object id not set", loadContact.getObjectID() > 0);
		final GetResponse response = (GetResponse) Executor.execute(
		    new AJAXClient(new AJAXSession(getWebConversation(), getSessionId())),
		    new UserGetRequest(userId));
		loadContact = response.getContact();
		assertNotNull("contact object is null", loadContact);
        assertEquals("user id is not equals", userId, loadContact.getInternalUserId());
        assertTrue("object id not set", loadContact.getObjectID() > 0);
	}
}