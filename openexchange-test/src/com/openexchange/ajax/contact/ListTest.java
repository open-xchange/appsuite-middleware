package com.openexchange.ajax.contact;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.ContactTest;
import com.openexchange.groupware.container.ContactObject;

public class ListTest extends ContactTest {
	
	private static final Log LOG = LogFactory.getLog(ListTest.class);
	
	public ListTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testList() throws Exception {
		ContactObject contactObj = createContactObject("testList");
		int id1 = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		int id2 = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		int id3 = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		
		final int[][] objectIdAndFolderId = { { id1, contactFolderId }, { id2, contactFolderId }, { id3, contactFolderId } };
		
		final int cols[] = new int[]{ ContactObject.OBJECT_ID, ContactObject.SUR_NAME, ContactObject.DISPLAY_NAME } ;
		
		ContactObject[] contactArray = listContact(getWebConversation(), objectIdAndFolderId, cols, PROTOCOL + getHostName(), getSessionId());
		
		assertEquals("check response array", 3, contactArray.length);
	}
	
	public void testListWithAllFields() throws Exception {
		ContactObject contactObject = createCompleteContactObject();
		
		int objectId = insertContact(getWebConversation(), contactObject, PROTOCOL + getHostName(), getSessionId());
		
		final int[][] objectIdAndFolderId = { { objectId, contactFolderId } };
		
		ContactObject[] contactArray = listContact(getWebConversation(), objectIdAndFolderId, CONTACT_FIELDS, PROTOCOL + getHostName(), getSessionId());
		
		assertEquals("check response array", 1, contactArray.length);
		
		ContactObject loadContact = contactArray[0];
		
		contactObject.setObjectID(objectId);
		compareObject(contactObject, loadContact);
	}
}