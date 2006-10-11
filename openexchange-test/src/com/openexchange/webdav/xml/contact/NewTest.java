package com.openexchange.webdav.xml.contact;

import com.openexchange.groupware.container.ContactObject;
import com.openexchange.webdav.xml.ContactTest;

public class NewTest extends ContactTest {
	
	public void testNewContact() throws Exception {
		ContactObject contactObj = createContactObject("testNewContact");
		insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
	}
	
}

