package com.openexchange.webdav.xml.contact;

import java.util.Date;
import java.util.Locale;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.webdav.xml.ContactTest;
import com.openexchange.webdav.xml.XmlServlet;

public class ListTest extends ContactTest {

	public ListTest(final String name) {
		super(name);
	}

	public void testPropFindWithModified() throws Exception {
		final Contact contactObj = createContactObject("testPropFindWithModified");
		final int objectId1 = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password, context);
		final int objectId2 = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password, context);

		// prevent master/slave problem
		Thread.sleep(1000);

		final Contact loadContact = loadContact(getWebConversation(), objectId1, contactFolderId, getHostName(), getLogin(), getPassword(), context);
		final Date modified = loadContact.getLastModified();

		final Contact[] contactArray = listContact(webCon, contactFolderId, decrementDate(modified), true, false, PROTOCOL + hostName, login, password, context);

		assertTrue("check response", contactArray.length >= 2);
		deleteContact(getWebConversation(), objectId1, contactFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
		deleteContact(getWebConversation(), objectId2, contactFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
	}

	public void testPropFindWithDelete() throws Exception {
		final Contact contactObj = createContactObject("testPropFindWithModified");
		final int objectId1 = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password, context);
		final int objectId2 = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password, context);

		final int[][] objectIdAndFolderId = { { objectId1, contactFolderId }, { objectId2, contactFolderId } };

		final Contact loadContact = loadContact(getWebConversation(), objectId1, contactFolderId, getHostName(), getLogin(), getPassword(), context);
		final Date modified = loadContact.getLastModified();

		deleteContact(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context);

		// prevent master/slave problem
		Thread.sleep(1000);

		final Contact[] appointmentArray = listContact(webCon, contactFolderId, decrementDate(modified), false, true, PROTOCOL + hostName, login, password, context);

		assertTrue("wrong response array length", appointmentArray.length >= 2);
	}

	public void testPropFindWithObjectId() throws Exception {
		final Contact contactObj = createContactObject("testPropFindWithObjectId");
		final int objectId = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password, context);

		final Contact loadContact = loadContact(webCon, objectId, contactFolderId, PROTOCOL + hostName, login, password, context);

		contactObj.setObjectID(objectId);
		compareObject(contactObj, loadContact);
		deleteContact(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
	}

	public void testObjectNotFound() throws Exception {
		final Contact contactObj = createContactObject("testObjectNotFound");
		final int objectId = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password, context);

		try {
			loadContact(webCon, (objectId+1000), contactFolderId, PROTOCOL + hostName, login, password, context);
			fail("object not found exception expected!");
		} catch (final OXException exc) {
			assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.OBJECT_NOT_FOUND_STATUS);
		}

		final int[][] objectIdAndFolderId = { { objectId ,contactFolderId } };
		deleteContact(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context );
	}

	public void testListWithAllFields() throws Exception {
		final Contact contactObj = createCompleteContactObject();

		final int objectId = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password, context);

		// prevent master/slave problem
		Thread.sleep(1000);

		Contact loadContact = loadContact(getWebConversation(), objectId, contactFolderId, getHostName(), getLogin(), getPassword(), context);
		final Date modified = loadContact.getLastModified();

		final Contact[] appointmentArray = listContact(webCon, contactFolderId, decrementDate(modified), true, false, PROTOCOL + hostName, login, password, context);

		assertTrue("wrong response array length", appointmentArray.length >= 1);

		loadContact = appointmentArray[0];
		contactObj.setObjectID(objectId);

		compareObject(contactObj, loadContact);
		deleteContact(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
	}

	public void testList() throws Exception {
		final Contact contactObj = createContactObject("testObjectNotFound");
		final int objectId = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password, context);

		final int[] idArray = listContact(getWebConversation(), contactFolderId, getHostName(), getLogin(), getPassword(), context);

		boolean found = false;
		for (int a = 0; a < idArray.length; a++) {
			if (idArray[a] == objectId) {
				found = true;
				break;
			}
		}

		assertTrue("id " + objectId + " not found in response", found);
		deleteContact(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
	}
}
