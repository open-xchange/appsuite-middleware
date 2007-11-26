package com.openexchange.ajax.contact;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.ContactTest;
import com.openexchange.groupware.container.ContactObject;

public class DeleteTest extends ContactTest {

	private static final Log LOG = LogFactory.getLog(DeleteTest.class);
	
	public DeleteTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	
	public void testDelete() throws Exception {
		ContactObject contactObj = createContactObject("testDelete");
		int id = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		
		deleteContact(getWebConversation(), id, contactFolderId, PROTOCOL + getHostName(), getSessionId());
	}
}

