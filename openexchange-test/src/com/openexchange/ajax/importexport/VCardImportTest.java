package com.openexchange.ajax.importexport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.xml.sax.SAXException;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.ContactTest;

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
			//System.out.println("surname: " + contactArray[a].getSurName() + " == " + contactObj.getSurName());
			if (contactObj.getSurName().equals(contactArray[a].getSurName()) ) {
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
		
		//ContactObject[] contactArray = exportContact(getWebConversation(), contactFolderId, emailaddress, timeZone, getHostName(), getSessionId());
		
		ContactTest.deleteContact(getWebConversation(), Integer.parseInt(importResult[0].getObjectId()), contactFolderId, getHostName(), getLogin(), getPassword());
		ContactTest.deleteContact(getWebConversation(), Integer.parseInt(importResult[2].getObjectId()), contactFolderId, getHostName(), getLogin(), getPassword());
	}
	
	public void test6823() throws TestException, IOException, SAXException, JSONException, Exception{
		//final String vcard = "BEGIN:VCARD\nVERSION:3.0\nPRODID:OPEN-XCHANGE\nFN:Prinz\\, Tobias\nN:Prinz;Tobias;;;\nNICKNAME:Tierlieb\nBDAY:19810501\nADR;TYPE=work:;;;Meinerzhagen;NRW;58540;DE\nTEL;TYPE=home,voice:+49 2358 7192\nEMAIL:tobias.prinz@open-xchange.com\nORG:- deactivated -\nREV:20061204T160750.018Z\nURL:www.tobias-prinz.de\nUID:80@ox6.netline.de\nEND:VCARD\n";
		final String vcard ="BEGIN:VCARD\nVERSION:3.0\nN:;Svetlana;;;\nFN:Svetlana\nTEL;type=CELL;type=pref:6670373\nCATEGORIES:Nicht abgelegt\nX-ABUID:CBC739E8-694E-4589-8651-8C30E1A6E724\\:ABPerson\nEND:VCARD";
		ImportResult[] importResult = importVCard(getWebConversation(), new ByteArrayInputStream(vcard.getBytes()), contactFolderId, timeZone, emailaddress, getHostName(), getSessionId());
		
		assertTrue("Only one import" , importResult.length == 1);
		assertFalse("No error?", importResult[0].hasError());
		
		ContactTest.deleteContact(getWebConversation(), Integer.parseInt(importResult[0].getObjectId()), contactFolderId, getHostName(), getLogin(), getPassword());
	}
	
	public void test6962followup() throws TestException, IOException, SAXException, JSONException, Exception{
		final String vcard ="BEGIN:VCARD\nVERSION:3.0\nN:;Svetlana;;;\nFN:Svetlana\nTEL;type=CELL;type=pref:673730\nEND:VCARD\nBEGIN:VCARD\nVERSION:666\nN:;Svetlana;;;\nFN:Svetlana\nTEL;type=CELL;type=pref:6670373\nEND:VCARD";
		ImportResult[] importResult = importVCard(getWebConversation(), new ByteArrayInputStream(vcard.getBytes()), contactFolderId, timeZone, emailaddress, getHostName(), getSessionId());
		
		assertTrue("Two import attempts" , importResult.length == 2);
		assertFalse("No error on first attempt?", importResult[0].hasError());
		assertTrue("Error on second attempt?", importResult[1].hasError());
		OXException ex = importResult[1].getException();

		//following line was removed since test environment cannot relay correct error messages from server
		//assertEquals("Correct error code?", "I_E-0605",ex.getErrorCode());
		ContactTest.deleteContact(getWebConversation(), Integer.parseInt(importResult[0].getObjectId()), contactFolderId, getHostName(), getLogin(), getPassword());
	}
	
	public void test7106() throws TestException, IOException, SAXException, JSONException, Exception{
		final String vcard ="BEGIN:VCARD\nVERSION:3.0\nN:;Hübört;;;\nFN:Hübört Sönderzeichön\nTEL;type=CELL;type=pref:6670373\nEND:VCARD\n";
		ImportResult[] importResult = importVCard(getWebConversation(), new ByteArrayInputStream(vcard.getBytes()), contactFolderId, timeZone, emailaddress, getHostName(), getSessionId());
		
		assertFalse("Worked?", importResult[0].hasError());
		int contactId = Integer.parseInt(importResult[0].getObjectId());
		ContactObject myImport = ContactTest.loadContact(getWebConversation(), contactId, contactFolderId, getHostName(), getLogin(), getPassword());
		assertEquals("Checking surname:" , "Hübört Sönderzeichön" , myImport.getSurName());
	
		ContactTest.deleteContact(getWebConversation(), contactId, contactFolderId, getHostName(), getLogin(), getPassword());
	}
	
	/**
	 * Deals with N and ADR properties and different amount of semicola used. 
	 * Also tests an input stream with no terminating newline
	 */
	public void test7248() throws TestException, IOException, SAXException, JSONException, Exception{
		final String vcard ="BEGIN:VCARD\nVERSION:2.1\nN:Colombara;Robert\nFN:Robert Colombara\nADR;WORK:;;;;;;DE\nADR;HOME:;;;;;- / -\nEND:VCARD";
		ImportResult[] importResult = importVCard(getWebConversation(), new ByteArrayInputStream(vcard.getBytes()), contactFolderId, timeZone, emailaddress, getHostName(), getSessionId());
		assertTrue("Only one import?", importResult.length == 1 );
		assertFalse("Import worked?", importResult[0].hasError());
		
		int contactId = Integer.parseInt(importResult[0].getObjectId());
		ContactObject myImport = ContactTest.loadContact(getWebConversation(), contactId, contactFolderId, getHostName(), getLogin(), getPassword());
		assertEquals("Checking surname:" , "Colombara" , myImport.getSurName());
	
		ContactTest.deleteContact(getWebConversation(), contactId, contactFolderId, getHostName(), getLogin(), getPassword());
	}
	
	/**
	 * Deals with broken E-Mail adresses as encountered in Resources in the example file
	 */
	public void test7249() throws TestException, IOException, SAXException, JSONException, Exception{
		final String vcard ="BEGIN:VCARD\nVERSION:2.1\nFN:Conference_Room_Olpe\nEMAIL;PREF;INTERNET:Conference_Room_Olpe_EMAIL\nEND:VCARD";
		ImportResult[] importResult = importVCard(getWebConversation(), new ByteArrayInputStream(vcard.getBytes()), contactFolderId, timeZone, emailaddress, getHostName(), getSessionId());
		
		assertFalse("Worked?", importResult[0].hasError());
		int contactId = Integer.parseInt(importResult[0].getObjectId());
		ContactObject myImport = ContactTest.loadContact(getWebConversation(), contactId, contactFolderId, getHostName(), getLogin(), getPassword());
		assertEquals("Checking surname:" , "Conference_Room_Olpe" , myImport.getDisplayName());
		assertEquals("Checking email1 (must be null):" , null , myImport.getEmail1());
		assertEquals("Checking email2 (must be null):" , null , myImport.getEmail2());
		assertEquals("Checking email3 (must be null):" , null , myImport.getEmail3());
	
		ContactTest.deleteContact(getWebConversation(), contactId, contactFolderId, getHostName(), getLogin(), getPassword());
	}
	
	/**
	 * Deals with umlauts
	 */
	public void test7250() throws TestException, IOException, SAXException, JSONException, Exception{
		final String vcard ="BEGIN:VCARD\nVERSION:2.1\nN;CHARSET=Windows-1252:Börnig;Anke;;;\nFN;CHARSET=Windows-1252:Anke  Börnig\nEND:VCARD";
		ImportResult[] importResult = importVCard(getWebConversation(), new ByteArrayInputStream(vcard.getBytes("Cp1252")), contactFolderId, timeZone, emailaddress, getHostName(), getSessionId());
		
		assertFalse("Worked?", importResult[0].hasError());
		int contactId = Integer.parseInt(importResult[0].getObjectId());
		ContactObject myImport = ContactTest.loadContact(getWebConversation(), contactId, contactFolderId, getHostName(), getLogin(), getPassword());
		assertEquals("Checking surname:" , "Börnig" , myImport.getSurName());
	
		ContactTest.deleteContact(getWebConversation(), contactId, contactFolderId, getHostName(), getLogin(), getPassword());
	}
}