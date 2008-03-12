package com.openexchange.webdav.xml.contact;

import java.util.Date;

import com.openexchange.groupware.container.ContactObject;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.ContactTest;
import com.openexchange.webdav.xml.XmlServlet;

public class DeleteTest extends ContactTest {
	
	public DeleteTest(String name) {
		super(name);
	}
	
	public void testDelete() throws Exception {
		ContactObject contactObj = createContactObject("testDelete");
		int objectId1 = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		int objectId2 = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		
		int[][] objectIdAndFolderId = { { objectId1, contactFolderId }, { objectId2, contactFolderId } };
		
		deleteContact(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password);
	}
	
	public void _notestDeleteConcurentConflict() throws Exception {
		ContactObject contactObj = createContactObject("testDeleteConcurentConflict");
		int objectId = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		
		try {
			deleteContact(webCon, objectId, contactFolderId, new Date(0), PROTOCOL + hostName, login, password );
			fail("expected concurent modification exception!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.MODIFICATION_STATUS);
		}
		
		deleteContact(webCon, objectId, contactFolderId, PROTOCOL + hostName, login, password );
	}
	
	public void testDeleteNotFound() throws Exception {
		ContactObject contactObj = createContactObject("testUpdateContactNotFound");
		int objectId = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		
		try {
			deleteContact(webCon, (objectId + 1000), contactFolderId, PROTOCOL + hostName, login, password );
			fail("expected object not found exception!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.OBJECT_NOT_FOUND_STATUS);
		}
		
		deleteContact(webCon, objectId, contactFolderId, PROTOCOL + hostName, login, password );
	}	
}

