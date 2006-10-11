package com.openexchange.webdav.xml.contact;

import com.openexchange.groupware.container.ContactObject;
import com.openexchange.webdav.xml.ContactTest;

public class DeleteTest extends ContactTest {
	
	public void testDelete() throws Exception {
		ContactObject contactObj = createContactObject("testDelete");
		int objectId1 = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		int objectId2 = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		
		int[][] objectIdAndFolderId = { { objectId1, contactFolderId }, { objectId2, contactFolderId } };
		
		deleteContact(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password);
	}
	
}

