package com.openexchange.ajax.contact;

import com.openexchange.ajax.ContactTest;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.ContactSearchObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SearchTest extends ContactTest {
	
	private static final Log LOG = LogFactory.getLog(SearchTest.class);
	
	public SearchTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testSearchLoginUser() throws Exception {
		String username = getAJAXProperty("username");
		
		if (username == null) {
			username = getLogin();
		}
		
		ContactObject[] contactArray = searchContact(getWebConversation(), username, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { ContactObject.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId());
		assertTrue("contact array size is 0", contactArray.length > 0);
		assertEquals("user id is not equals", userId, contactArray[0].getInternalUserId());
	}
	
	public void testSearchStartCharacter() throws Exception {
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("Meier");
		contactObj.setParentFolderID(contactFolderId);
		int objectId1 = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		int objectId2 = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		
		ContactObject[] contactArray = searchContact(getWebConversation(), "M", contactFolderId, new int[] { ContactObject.INTERNAL_USERID }, true, PROTOCOL + getHostName(), getSessionId());
		assertTrue("contact array size < 2", contactArray.length >= 2);
	}
	
	public void testSearchEmailComplete() throws Exception {
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("Mustermann");
		contactObj.setGivenName("Tom");
		contactObj.setEmail1("tom.mustermann@email.com");
		contactObj.setParentFolderID(contactFolderId);
		
		ContactObject contactObj2 = new ContactObject();
		contactObj2.setSurName("Mustermann");
		contactObj2.setGivenName("Ute");
		contactObj2.setEmail1("ute.mustermann@email.com");
		contactObj2.setParentFolderID(contactFolderId);

		ContactObject contactObj3 = new ContactObject();
		contactObj3.setSurName("Gloreich");
		contactObj3.setGivenName("Guenter");
		contactObj3.setEmail1("g.gloreich@email.com");
		contactObj3.setParentFolderID(contactFolderId);
		
		int objectId1 = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		int objectId2 = insertContact(getWebConversation(), contactObj2, PROTOCOL + getHostName(), getSessionId());
		
		ContactSearchObject cso = new ContactSearchObject();
		cso.setSurname("Must*");
		cso.setEmailAutoComplete(true);
		
		ContactObject[] contactArray = searchContactAdvanced(getWebConversation(), cso,contactFolderId, new int[] { ContactObject.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId());
		assertTrue("contact array size >= 2", contactArray.length >= 2);
		
		cso = new ContactSearchObject();
		cso.setEmail1("*email.com");
		cso.setEmailAutoComplete(true);
		
		ContactObject[] contactArray2 = searchContactAdvanced(getWebConversation(), cso,contactFolderId, new int[] { ContactObject.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId());
		assertTrue("contact array size >= 3", contactArray2.length >= 3);
		
	}
	
}