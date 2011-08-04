package com.openexchange.ajax.importexport;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.container.Contact;
import com.openexchange.webdav.xml.ContactTest;

public class VCardExportTest extends AbstractVCardTest {
	
	final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	private static final Log LOG = LogFactory.getLog(ICalImportTest.class);
	
	public VCardExportTest(final String name) {
		super(name);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testDummy() throws Exception {
		
	}
	
	public void testExportVCard() throws Exception {
		final String surname = "testImportVCard" + System.currentTimeMillis();
		
		final Contact contactObj = new Contact();
		contactObj.setSurName(surname);
		contactObj.setGivenName("givenName");
		contactObj.setBirthday(simpleDateFormat.parse("2007-04-04"));
		contactObj.setParentFolderID(contactFolderId);
		
		final int objectId = ContactTest.insertContact(getWebConversation(), contactObj, getHostName(), getLogin(), getPassword());

		final Contact[] contactArray = exportContact(getWebConversation(), contactFolderId, emailaddress, timeZone, getHostName(), getSessionId());
		
		boolean found = false;
		for (int a = 0; a < contactArray.length; a++) {
			if (contactArray[a].getSurName() != null && contactArray[a].getSurName().equals(surname)) {
				found = true;
				ContactTest.compareObject(contactObj, contactArray[a]);
			}
		}
		
		assertTrue("contact with surname: " + surname + " not found", found);
		
		ContactTest.deleteContact(getWebConversation(), objectId, contactFolderId, getHostName(), getLogin(), getPassword());
	}
}