package com.openexchange.webdav.xml.contact;

import com.openexchange.groupware.container.ContactObject;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.ContactTest;
import com.openexchange.webdav.xml.XmlServlet;
import java.util.Date;

public class Bug8182Test extends ContactTest {
	
	public Bug8182Test(String name) {
		super(name);
	}

	public void testBug8182() throws Exception {
		Date modified = new Date();
		
		ContactObject contactObj = createContactObject("testPropFindWithModified");
		int objectId = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		deleteContact(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		ContactObject[] contactArray = listContact(webCon, contactFolderId, modified, true, false, PROTOCOL + hostName, login, password);
		
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
