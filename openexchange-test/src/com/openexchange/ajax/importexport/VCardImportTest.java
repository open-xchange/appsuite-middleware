package com.openexchange.ajax.importexport;

import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.webdav.xml.ContactTest;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VCardImportTest extends AbstractVCardTest {
	
	final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	private static final Log LOG = LogFactory.getLog(VCardImportTest.class);
	
	public VCardImportTest(String name) {
		super(name);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testDummy() throws Exception {
		
	}
	
	public void testImportVCard() throws Exception {
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testImportVCard" + System.currentTimeMillis());
		contactObj.setGivenName("givenName");
		contactObj.setBirthday(simpleDateFormat.parse("2007-04-04"));

		ImportResult[] importResult = importVCard(getWebConversation(), new ContactObject[]  { contactObj }, contactFolderId, timeZone, emailaddress, getHostName(), getSessionId());
		
		assertEquals("import result size is not 1", 1, importResult.length);
		assertTrue("server errors of server", importResult[0].isCorrect());
		
		int objectId = Integer.parseInt(importResult[0].getObjectId());
		
		assertTrue("object id is 0", objectId > 0);
		
		ContactObject[] contactArray = exportContact(getWebConversation(), contactFolderId, emailaddress, timeZone, getHostName(), getSessionId());
		
		boolean found = false;
		
		for (int a = 0; a < contactArray.length; a++) {
			System.out.println("surname: " + contactArray[a].getSurName() + " == " + contactObj.getSurName());
			if (contactArray[a].getSurName().equals(contactObj.getSurName())) {
				contactObj.setParentFolderID(contactFolderId);
				ContactTest.compareObject(contactObj, contactArray[a]);
				
				found = true;
			}
		}
		
		assertTrue("inserted object not found in response", found);
		
		ContactTest.deleteContact(getWebConversation(), Integer.parseInt(importResult[0].getObjectId()), contactFolderId, getHostName(), getLogin(), getPassword());
	}

	public void testImportVCardWithBrokenContact() throws Exception {
		final StringBuffer stringBuffer = new StringBuffer();

		// cont1
		stringBuffer.append("BEGIN:VCARD").append('\n');
		stringBuffer.append("VERSION:3.0").append('\n');
		stringBuffer.append("FN:testImportVCardWithBrokenContact1").append('\n');
		stringBuffer.append("N:testImportVCardWithBrokenContact1;givenName;;;").append('\n');
		stringBuffer.append("BDAY:20070404").append('\n');
		stringBuffer.append("END:VCARD").append('\n');

		// cont2
		stringBuffer.append("BEGIN:VCARD").append('\n');
		stringBuffer.append("VERSION:3.0").append('\n');
		stringBuffer.append("FN:testImportVCardWithBrokenContact2").append('\n');
		stringBuffer.append("N:testImportVCardWithBrokenContact2;givenName;;;").append('\n');
		stringBuffer.append("BDAY:INVALID_DATE").append('\n');
		stringBuffer.append("END:VCARD").append('\n');
		
		// cont3
		stringBuffer.append("BEGIN:VCARD").append('\n');
		stringBuffer.append("VERSION:3.0").append('\n');
		stringBuffer.append("FN:testImportVCardWithBrokenContact3").append('\n');
		stringBuffer.append("N:testImportVCardWithBrokenContact3;givenName;;;").append('\n');
		stringBuffer.append("BDAY:20070404").append('\n');
		stringBuffer.append("END:VCARD").append('\n');
		
		ImportResult[] importResult = importVCard(getWebConversation(), new ByteArrayInputStream(stringBuffer.toString().getBytes()), contactFolderId, timeZone, emailaddress, getHostName(), getSessionId());
		
		assertEquals("invalid import result array size", 3, importResult.length);
		
		assertTrue("server errors of server", importResult[0].isCorrect());
		assertTrue("server errors of server", importResult[1].hasError());
		assertTrue("server errors of server", importResult[2].isCorrect());
		
		ContactObject[] contactArray = exportContact(getWebConversation(), contactFolderId, emailaddress, timeZone, getHostName(), getSessionId());
		
		ContactTest.deleteContact(getWebConversation(), Integer.parseInt(importResult[0].getObjectId()), contactFolderId, getHostName(), getLogin(), getPassword());
		ContactTest.deleteContact(getWebConversation(), Integer.parseInt(importResult[2].getObjectId()), contactFolderId, getHostName(), getLogin(), getPassword());
	}
}