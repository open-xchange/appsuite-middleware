package com.openexchange.ajax.contact;

import com.openexchange.ajax.ContactTest;
import com.openexchange.groupware.container.ContactObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NewTest extends ContactTest {
	
	private static final Log LOG = LogFactory.getLog(NewTest.class);
	
	public NewTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testNew() throws Exception {
		ContactObject contactObj = createContactObject("testNew");
		int objectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testNewWithDistributionList() throws Exception {
		ContactObject contactEntry = createContactObject("internal contact");
		contactEntry.setEmail1("internalcontact@x.de");
		int contactId = insertContact(getWebConversation(), contactEntry, PROTOCOL + getHostName(), getSessionId());
		contactEntry.setObjectID(contactId);
		
		int objectId = createContactWithDistributionList("testNewWithDistributionList", contactEntry);
	}
	
	public void testNewWithLinks() throws Exception {
		ContactObject link1 = createContactObject("link1");
		ContactObject link2 = createContactObject("link2");
		int linkId1 = insertContact(getWebConversation(), link1, PROTOCOL + getHostName(), getSessionId());
		link1.setObjectID(linkId1);
		int linkId2 = insertContact(getWebConversation(), link2, PROTOCOL + getHostName(), getSessionId());
		link2.setObjectID(linkId2);
		
		int objectId = createContactWithLinks("testNewWithLinks", link1, link2);
	}
}

