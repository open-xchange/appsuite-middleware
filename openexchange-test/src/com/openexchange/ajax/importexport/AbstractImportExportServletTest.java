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

package com.openexchange.ajax.importexport;

import com.openexchange.ajax.AbstractAJAXTest;
import com.openexchange.ajax.ImportExport;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.ContactTestData;
import com.openexchange.groupware.importexport.Format;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.webdav.xml.FolderTest;
import org.json.JSONException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Test of the ImporterExporter servlet. This class serves as library for all
 * derived tests. 
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public abstract class AbstractImportExportServletTest extends AbstractAJAXTest {
	//private SessionObject sessObj;
	public String FOLDER_NAME = "csv-contact-roundtrip-ajax-test";

    public String IMPORTED_CSV = ContactTestData.IMPORT_MULTIPLE;
	public String EXPORT_SERVLET = "export";
	public String IMPORT_SERVLET = "import";
	public String IMPORT_VCARD = "BEGIN:VCARD\nVERSION:3.0\nPRODID:OPEN-XCHANGE\nFN:Prinz\\, Tobias\nN:Prinz;Tobias;;;\nNICKNAME:Tierlieb\nBDAY:19810501\nADR;TYPE=work:;;;Meinerzhagen;NRW;58540;DE\nTEL;TYPE=home,voice:+49 2358 7192\nEMAIL:tobias.prinz@open-xchange.com\nORG:- deactivated -\nREV:20061204T160750.018Z\nURL:www.tobias-prinz.de\nUID:80@ox6.netline.de\nEND:VCARD\n";
	public String[] IMPORT_VCARD_AWAITED_ELEMENTS = "PRODID:OPEN-XCHANGE\nFN:Prinz\\, Tobias\nN:Prinz;Tobias;;;\nBDAY:19810501\nADR;TYPE=work:;;;Meinerzhagen;NRW;58540;DE\nTEL;TYPE=home,voice:+49 2358 7192\nEMAIL:tobias.prinz@open-xchange.com".split("\n");
	
	public AbstractImportExportServletTest(String name){
		super(name);
	}
	
	public void setUp() throws Exception{
		super.setUp();
	//	final UserStorage uStorage = UserStorage.getInstance(new ContextImpl(1));
	//  final int userId = uStorage.getUserId( Init.getAJAXProperty("login") );
	//	sessObj = SessionObjectWrapper.createSessionObject(userId, 1, "csv-roundtrip-test");
	}
	
	public void tearDown() throws Exception{
        super.tearDown();
	}
	
	protected int getUserId_FIXME() throws MalformedURLException, OXException, IOException, SAXException, JSONException {
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
		addParam(bob, ImportExport.PARAMETER_COLUMNS, ContactField.DISPLAY_NAME.getNumber());
		return bob.toString();		
	}
	
	protected void addParam(StringBuilder bob, String param, String value){
		bob.append("&");
		bob.append(param);
		bob.append("=");
		bob.append(value);
	}
	
	protected void addParam(StringBuilder bob, String param, int value){
		addParam(bob, param, Integer.toString(value));
	}
	
	protected int createFolder(String title, int folderObjectModuleID) throws Exception{
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
	
	protected void removeFolder( int folderId) throws OXException, Exception{
		if(folderId == -1){
			return;
		}
		FolderTest.deleteFolder(getWebConversation(), new int[] { folderId }, getHostName(), getLogin(), getPassword());
	}
	

	
	public static void assertEquals(String message, List l1, List l2){
		if(l1.size() != l2.size()) {
			fail(message);
		}
		Set s = new HashSet(l1);
		for(Object o : l2) {
			assertTrue(message,s.remove(o));
		}
	}
	
	
}
