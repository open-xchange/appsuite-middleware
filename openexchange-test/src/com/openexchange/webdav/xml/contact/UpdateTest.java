package com.openexchange.webdav.xml.contact;

import com.openexchange.groupware.container.ContactObject;
import com.openexchange.webdav.xml.ContactTest;

public class UpdateTest extends ContactTest {
	
	public void testUpdateContact() throws Exception {
		ContactObject contactObj = createContactObject("testUpdateContact");
		int objectId = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		
		contactObj = createContactObject("testUpdateContact2");
		contactObj.setEmail1(null);
		
		updateContact(webCon, contactObj, objectId, contactFolderId, PROTOCOL + hostName, login, password);
	}
	
}

