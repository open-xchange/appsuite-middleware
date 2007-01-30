package com.openexchange.ajax.contact;

import com.openexchange.ajax.ContactTest;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
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
		ContactObject[] contactArray = searchContact(getWebConversation(), getLogin(), FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { ContactObject.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId());
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
}