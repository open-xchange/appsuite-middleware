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
}