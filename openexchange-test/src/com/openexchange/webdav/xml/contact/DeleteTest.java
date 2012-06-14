package com.openexchange.webdav.xml.contact;

import java.util.Date;
import java.util.Locale;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.webdav.xml.ContactTest;
import com.openexchange.webdav.xml.XmlServlet;

public class DeleteTest extends ContactTest {

	public DeleteTest(final String name) {
		super(name);
	}

	public void testDelete() throws Exception {
		final Contact contactObj = createContactObject("testDelete");
		final int objectId1 = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password, context);
		final int objectId2 = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password, context);

		final int[][] objectIdAndFolderId = { { objectId1, contactFolderId }, { objectId2, contactFolderId } };

		deleteContact(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context);
	}

	public void testDeleteConcurentConflict() throws Exception {
		final Contact contactObj = createContactObject("testDeleteConcurentConflict");
		final int objectId = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password, context);

		try {
			deleteContact(webCon, objectId, contactFolderId, new Date(1), PROTOCOL + hostName, login, password, context );
			fail("expected concurent modification exception!");
		} catch (final OXException exc) {
			assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.MODIFICATION_STATUS);
		}

		deleteContact(webCon, objectId, contactFolderId, PROTOCOL + hostName, login, password, context);
	}

	public void testDeleteNotFound() throws Exception {
		final Contact contactObj = createContactObject("testUpdateContactNotFound");
		final int objectId = insertContact(webCon, contactObj, PROTOCOL + hostName, login, password, context);

		try {
			deleteContact(webCon, (objectId + 1000), contactFolderId, PROTOCOL + hostName, login, password, context );
			fail("expected object not found exception!");
		} catch (final OXException exc) {
			assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.OBJECT_NOT_FOUND_STATUS);
		}

		deleteContact(webCon, objectId, contactFolderId, PROTOCOL + hostName, login, password, context );
	}
}

