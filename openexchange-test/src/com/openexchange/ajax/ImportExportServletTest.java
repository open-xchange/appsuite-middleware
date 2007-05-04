/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ajax;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.contact.ContactConfig;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.AbstractCSVContactTest;
import com.openexchange.groupware.importexport.CSVContactImportTest;
import com.openexchange.groupware.importexport.Format;
import com.openexchange.groupware.importexport.csv.CSVParser;
import com.openexchange.server.OCLPermission;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.webdav.xml.FolderTest;


/**
 * Test of the ImporterExporter servlet. 
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class ImportExportServletTest extends AbstractAJAXTest {
	//private SessionObject sessObj;
	public String FOLDER_NAME = "csv-contact-roundtrip-ajax-test";
	public String IMPORTED_CSV = CSVContactImportTest.IMPORT_MULTIPLE;
	public String EXPORT_SERVLET = "export";
	public String IMPORT_SERVLET = "import";
	public String IMPORT_VCARD = "BEGIN:VCARD\nVERSION:3.0\nPRODID:OPEN-XCHANGE\nFN:Prinz\\, Tobias\nN:Prinz;Tobias;;;\nNICKNAME:Tierlieb\nBDAY:19810501\nADR;TYPE=work:;;;Meinerzhagen;NRW;58540;DE\nTEL;TYPE=home,voice:+49 2358 7192\nEMAIL:tobias.prinz@open-xchange.com\nORG:- deactivated -\nREV:20061204T160750.018Z\nURL:www.tobias-prinz.de\nUID:80@ox6.netline.de\nEND:VCARD\n";
	public String[] IMPORT_VCARD_AWAITED_ELEMENTS = "PRODID:OPEN-XCHANGE\nFN:Prinz\\, Tobias\nN:Prinz;Tobias;;;\nBDAY:19810501\nADR;TYPE=work:;;;Meinerzhagen;NRW;58540;DE\nTEL;TYPE=home,voice:+49 2358 7192\nEMAIL:tobias.prinz@open-xchange.com".split("\n");
	
	public ImportExportServletTest(String name){
		super(name);
	}
	
	public void setUp() throws Exception{
		super.setUp();
		Init.initDB();
		ContactConfig.init();
	//	final UserStorage uStorage = UserStorage.getInstance(new ContextImpl(1));
	//  final int userId = uStorage.getUserId( Init.getAJAXProperty("login") );
	//	sessObj = SessionObjectWrapper.createSessionObject(userId, 1, "csv-roundtrip-test");
	}
	
	public void tearDown() throws Exception{
		super.tearDown();
	}
	
	public void testCSVRoundtrip() throws Exception{
		//preparations
		final String insertedCSV = IMPORTED_CSV;
		final Format format = Format.CSV;
		final int folderId = createFolder("csv-contact-roundtrip-" + System.currentTimeMillis(),FolderObject.CONTACT);
		
		
		//test: import
		InputStream is = new ByteArrayInputStream(insertedCSV.getBytes());
		WebConversation webconv = getWebConversation();
		WebRequest req = new PostMethodWebRequest(
				getCSVColumnUrl(IMPORT_SERVLET, folderId, format)
				);
		((PostMethodWebRequest)req).setMimeEncoded(true);
		req.selectFile("file", "contacts.csv", is, format.getMimeType());
		WebResponse webRes = webconv.getResource(req);
		
		JSONObject response = extractFromCallback( webRes.getText() );
		
		//test: export
		webconv =  getWebConversation();
		req = new GetMethodWebRequest( getCSVColumnUrl(EXPORT_SERVLET, folderId, format) );
		webRes = webconv.sendRequest(req);
		is = webRes.getInputStream();
		String resultingCSV = AbstractCSVContactTest.readStreamAsString(is);
		//finally: checking
		CSVParser parser1 = new CSVParser(insertedCSV);
		CSVParser parser2 = new CSVParser(resultingCSV);
		assertEquals("input == output ?" , parser1.parse() , parser2.parse());
		
		//clean up
		removeFolder(folderId);
	}

	public void testVCardRoundtrip() throws Exception{
		//preparations
		final String insertedCSV = IMPORT_VCARD;
		final Format format = Format.VCARD;
		final int folderId = createFolder("vcard-contact-roundtrip-" + System.currentTimeMillis(),FolderObject.CONTACT);
		
		
		//test: import
		InputStream is = new ByteArrayInputStream(insertedCSV.getBytes());
		WebConversation webconv = getWebConversation();
		WebRequest req = new PostMethodWebRequest(
				getUrl(IMPORT_SERVLET, folderId, format)
				);
		((PostMethodWebRequest)req).setMimeEncoded(true);
		req.selectFile("file", "contact.vcf", is, format.getMimeType());
		WebResponse webRes = webconv.getResource(req);
		
		JSONObject response = extractFromCallback( webRes.getText() );
		
		//test: export
		webconv =  getWebConversation();
		req = new GetMethodWebRequest( getUrl(EXPORT_SERVLET, folderId, format) );
		webRes = webconv.sendRequest(req);
		is = webRes.getInputStream();
		String resultingVCard = AbstractCSVContactTest.readStreamAsString(is);
		//finally: checking
		for(String test: IMPORT_VCARD_AWAITED_ELEMENTS){
			assertTrue("VCard contains " + test + "?", resultingVCard.contains(test));
		}
		
		//clean up
		removeFolder(folderId);
	}

	public void testCSVBrokenFile() throws Exception{
		//preparations
		final String insertedCSV = "bla\nbla,bla";
		final Format format = Format.CSV;
		final int folderId = createFolder("csv-contact-roundtrip-" + System.currentTimeMillis(),FolderObject.CONTACT);
		
		//test: import
		InputStream is = new ByteArrayInputStream(insertedCSV.getBytes());
		WebConversation webconv = getWebConversation();
		WebRequest req = new PostMethodWebRequest(
				getCSVColumnUrl(IMPORT_SERVLET, folderId, format)
				);
		((PostMethodWebRequest)req).setMimeEncoded(true);
		req.selectFile("file", "contacts.csv", is, format.getMimeType());
		WebResponse webRes = webconv.getResource(req);
		JSONObject response = extractFromCallback( webRes.getText() );
		assertEquals("Must contain error ", "I_E-1000", response.optString("code"));
	}
	
	public void testUnknownCSVFormat() throws Exception{
		//preparations
		final String insertedCSV = "bla\nbla\nbla";
		final Format format = Format.CSV;
		final int folderId = createFolder("csv-contact-roundtrip-" + System.currentTimeMillis(),FolderObject.CONTACT);
		
		//test: import
		InputStream is = new ByteArrayInputStream(insertedCSV.getBytes());
		WebConversation webconv = getWebConversation();
		WebRequest req = new PostMethodWebRequest(
				getCSVColumnUrl(IMPORT_SERVLET, folderId, format)
				);
		((PostMethodWebRequest)req).setMimeEncoded(true);
		req.selectFile("file", "contacts.csv", is, format.getMimeType());
		WebResponse webRes = webconv.getResource(req);
		JSONObject response = extractFromCallback( webRes.getText() );
		assertEquals("Must contain error ", "I_E-0804", response.optString("code"));
	}
	
	public void testEmptyFileUploaded() throws Exception{
		InputStream is = new ByteArrayInputStream("".getBytes());
		WebConversation webconv = getWebConversation();
		final Format format = Format.CSV;
		final int folderId = createFolder("csv-empty-file-" + System.currentTimeMillis(),FolderObject.CONTACT);
		
		WebRequest req = new PostMethodWebRequest(
				getCSVColumnUrl(IMPORT_SERVLET, folderId, format)
				);
		((PostMethodWebRequest)req).setMimeEncoded(true);
		req.selectFile("file", "empty.vcs", is, format.getMimeType());
		WebResponse webRes = webconv.getResource(req);
		JSONObject response = extractFromCallback( webRes.getText() );
		assertEquals("Must contain error ", "I_E-1303", response.optString("code"));
	}
	
	public void testIcalMessage() throws Exception{
		InputStream is = new ByteArrayInputStream("BEGIN:VCALENDAR".getBytes());
		WebConversation webconv = getWebConversation();
		final Format format = Format.ICAL;
		final int folderId = createFolder("ical-empty-file-" + System.currentTimeMillis(),FolderObject.CONTACT);
		
		WebRequest req = new PostMethodWebRequest(
				getCSVColumnUrl(IMPORT_SERVLET, folderId, format)
				);
		((PostMethodWebRequest)req).setMimeEncoded(true);
		req.selectFile("file", "empty.ics", is, format.getMimeType());
		WebResponse webRes = webconv.getResource(req);
		JSONObject response = extractFromCallback( webRes.getText() );
		assertEquals("Must contain error ", "I_E-1303", response.optString("code"));
	}
	
	
	
	
	
	
	
	private int getUserId_FIXME() throws MalformedURLException, OXException, IOException, SAXException, JSONException {
		final FolderObject folderObj = com.openexchange.ajax.FolderTest
		.getStandardCalendarFolder(getWebConversation(),
		getHostName(), getSessionId());

		return folderObj.getCreatedBy();
	}

	public String getUrl(String servlet, int folderId, Format format) throws IOException, SAXException, JSONException{
		StringBuilder bob = new StringBuilder("http://");
		bob.append(getHostName());
		bob.append("/ajax/");
		bob.append(servlet);
		bob.append("?session=");
		bob.append(getSessionId());
		addParam(bob, ImportExport.PARAMETER_FOLDERID, folderId ) ;
		addParam(bob, ImportExport.PARAMETER_ACTION, format.getConstantName());
		return bob.toString();
	}
	
	public String getCSVColumnUrl(String servlet, int folderId, Format format) throws IOException, SAXException, JSONException{
		StringBuilder bob = new StringBuilder(getUrl(servlet, folderId, format));
		addParam(bob, ImportExport.PARAMETER_COLUMNS, ContactField.GIVEN_NAME.getNumber());
		addParam(bob, ImportExport.PARAMETER_COLUMNS, ContactField.EMAIL1.getNumber());
		return bob.toString();		
	}
	
	private void addParam(StringBuilder bob, String param, String value){
		bob.append("&");
		bob.append(param);
		bob.append("=");
		bob.append(value);
	}
	
	private void addParam(StringBuilder bob, String param, int value){
		addParam(bob, param, Integer.toString(value));
	}
	
	private int createFolder(String title, int folderObjectModuleID) throws Exception{
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName(title);
		folderObj.setParentFolderID(FolderObject.PRIVATE);
		folderObj.setModule(folderObjectModuleID);
		folderObj.setType(FolderObject.PRIVATE);
		
		OCLPermission[] permission = new OCLPermission[] {
			FolderTest.createPermission( getUserId_FIXME(), false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
		};
		
		folderObj.setPermissionsAsArray( permission );
		try{
			return FolderTest.insertFolder(getWebConversation(), folderObj, getHostName(), getLogin(), getPassword());
		} catch(OXFolderException e){
			return -1;
		}
	}
	
	private void removeFolder( int folderId) throws OXException, Exception{
		if(folderId == -1){
			return;
		}
		FolderTest.deleteFolder(getWebConversation(), new int[] { folderId }, getHostName(), getLogin(), getPassword());
	}
	

	
	
	
}
