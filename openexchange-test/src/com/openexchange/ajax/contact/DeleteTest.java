package com.openexchange.ajax.contact;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.ajax.ContactTest;
import com.openexchange.groupware.container.Contact;

public class DeleteTest extends ContactTest {

	private static final Log LOG = com.openexchange.log.Log.loggerFor(DeleteTest.class);

	public DeleteTest(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}


	public void testDelete() throws Exception {
		final Contact contactObj = createContactObject("testDelete");
		final int id = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());

		deleteContact(getWebConversation(), id, contactFolderId, PROTOCOL + getHostName(), getSessionId());
	}
}

