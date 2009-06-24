package com.openexchange.webdav.xml.contact;

import java.util.Date;

import com.openexchange.groupware.container.Contact;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.ContactTest;
import com.openexchange.webdav.xml.XmlServlet;

public class UpdateTest extends ContactTest {
	
	public static final String CONTENT_TYPE = "image/png";
	
	public static final byte[] image = { -119, 80, 78, 71, 13, 10, 26, 10, 0,
	0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 1, 0, 0, 0, 1, 1, 3, 0, 0, 0,
	37, -37, 86, -54, 0, 0, 0, 6, 80, 76, 84, 69, -1, -1, -1, -1, -1,
	-1, 85, 124, -11, 108, 0, 0, 0, 1, 116, 82, 78, 83, 0, 64, -26,
	-40, 102, 0, 0, 0, 1, 98, 75, 71, 68, 0, -120, 5, 29, 72, 0, 0, 0,
	9, 112, 72, 89, 115, 0, 0, 11, 18, 0, 0, 11, 18, 1, -46, -35, 126,
	-4, 0, 0, 0, 10, 73, 68, 65, 84, 120, -38, 99, 96, 0, 0, 0, 2, 0,
	1, -27, 39, -34, -4, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126 };
	
	public UpdateTest(final String name) {
		super(name);
	}
	
	public void testUpdateContact() throws Exception {
		Contact contactObj = createContactObject("testUpdateContact");
		final int objectId = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		
		contactObj = createContactObject("testUpdateContact");
		contactObj.setEmail1(null);
		
		updateContact(webCon, contactObj, objectId, contactFolderId, PROTOCOL + hostName, login, password);
		final Contact loadContact = loadContact(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(contactObj, loadContact);
		deleteContact(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testUpdateContactWithImage() throws Exception {
		Contact contactObj = createContactObject("testUpdateContactWithImage");
		final int objectId = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		
		contactObj = createContactObject("testUpdateContactWithImage");
		contactObj.setEmail1(null);
		contactObj.setImageContentType(CONTENT_TYPE);
		contactObj.setImage1(image);
		
		updateContact(webCon, contactObj, objectId, contactFolderId, PROTOCOL + hostName, login, password);
		final Contact loadContact = loadContact(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		contactObj.removeImage1();
		compareObject(contactObj, loadContact);
		deleteContact(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testUpdateConcurentConflict() throws Exception {
		Contact contactObj = createContactObject("testUpdateContactConcurentConflict");
		final int objectId = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		
		contactObj = createContactObject("testUpdateContactConcurentConflict2");
		
		try {
			updateContact(webCon, contactObj, objectId, contactFolderId, new Date(0), PROTOCOL + hostName, login, password);
			fail("expected concurent modification exception!");
		} catch (final TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.MODIFICATION_STATUS);
		}
		
		final int[][] objectIdAndFolderId = { {objectId, contactFolderId } };
		deleteContact(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
	
	public void testUpdateNotFound() throws Exception {
		Contact contactObj = createContactObject("testUpdateContactNotFound");
		final int objectId = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password);
		
		contactObj = createContactObject("testUpdateContactNotFound");
		
		try {
			updateContact(webCon, contactObj, (objectId + 1000), contactFolderId, new Date(0), PROTOCOL + hostName, login, password);
			fail("expected object not found exception!");
		} catch (final TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.OBJECT_NOT_FOUND_STATUS);
		}
		
		final int[][] objectIdAndFolderId = { {objectId, contactFolderId } };
		deleteContact(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
	
}

