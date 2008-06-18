package com.openexchange.webdav.xml.contact;

import java.util.Date;

import com.openexchange.groupware.container.ContactObject;
import com.openexchange.webdav.xml.ContactTest;

public class Bug8182Test extends ContactTest {
	
	public Bug8182Test(final String name) {
		super(name);
	}

	public void testBug8182() throws Exception {
		final ContactObject contactObj = createContactObject("testPropFindWithModified");
		final int objectId = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		final ContactObject loadContact = loadContact(getWebConversation(), objectId, contactFolderId, getHostName(), getLogin(), getPassword());
		final Date modified = loadContact.getLastModified();
		
		deleteContact(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		// prevent master/slave problem
		Thread.sleep(1000);
		
		final ContactObject[] contactArray = listContact(webCon, contactFolderId, modified, true, false, PROTOCOL + hostName, login, password);
		
		boolean found = true;
		if (contactArray.length == 0) {
			found = false; 
		} else {
			for (int a = 0; a < contactArray.length; a++) {
				if (contactArray[a].getObjectID() == objectId) {
					found = true;
					break;
				}
			}
		}
		
		assertFalse("unexpected object id " + objectId + " in response", found);
	}
}
