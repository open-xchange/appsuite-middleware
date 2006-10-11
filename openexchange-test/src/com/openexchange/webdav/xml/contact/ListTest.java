package com.openexchange.webdav.xml.contact;

import com.openexchange.groupware.container.ContactObject;
import com.openexchange.webdav.xml.ContactTest;
import java.util.Date;

public class ListTest extends ContactTest {

	public void testPropFindWithModified() throws Exception {
		Date modified = new Date();
		
		ContactObject contactObj = createContactObject("testPropFindWithModified");
		insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		
		ContactObject[] contactArray = listContact(webCon, contactFolderId, modified, "NEW_AND_MODIFIED", PROTOCOL + hostName, login, password);
		
		assertTrue("check response", contactArray.length >= 2);
	}
	
	public void testPropFindWithDelete() throws Exception {
		Date modified = new Date();
		
		ContactObject contactObj = createContactObject("testPropFindWithModified");
		int objectId1 = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		int objectId2 = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		
		int[][] objectIdAndFolderId = { { objectId1, contactFolderId }, { objectId2, contactFolderId } };
		
		deleteContact(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password);
		
		ContactObject[] appointmentArray = listContact(webCon, contactFolderId, modified, "DELETED", PROTOCOL + hostName, login, password);
		
		assertTrue("wrong response array length", appointmentArray.length >= 2);
	}
	
	public void testPropFindWithObjectId() throws Exception {
		ContactObject contactObj = createContactObject("testPropFindWithObjectId");
		int objectId = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		
		ContactObject loadContact = loadContact(webCon, objectId, contactFolderId, PROTOCOL + hostName, login, password);
	}
	
	public void testListWithAllFields() throws Exception {
		ContactObject contactObj = createCompleteContactObject();
		
		Date modified = new Date();
		
		int objectId = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		
		ContactObject[] appointmentArray = listContact(webCon, contactFolderId, modified, "NEW_AND_MODIFIED", PROTOCOL + hostName, login, password);
		
		assertEquals("wrong response array length", 1, appointmentArray.length);
		
		ContactObject loadContact = appointmentArray[0];
		contactObj.setObjectID(objectId);
		
		compareObject(contactObj, loadContact);
	}
	
}
