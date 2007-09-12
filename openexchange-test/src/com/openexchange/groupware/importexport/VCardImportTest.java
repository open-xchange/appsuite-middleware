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

package com.openexchange.groupware.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.server.DBPoolingException;

public class VCardImportTest extends AbstractVCardTest {
	//workaround for JUnit 3 runner
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(VCardImportTest.class);
	}
	
	@Test public void test6825_tooMuchInformation() throws DBPoolingException, SQLException, OXObjectNotFoundException, NumberFormatException, OXException, UnsupportedEncodingException{
		//setup: building an VCard file with a summary longer than 255 characters.
		folderId = createTestFolder(FolderObject.CONTACT, sessObj, "vcard6825Folder");
		final String stringTooLong = "zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... ";
		final String vcard = "BEGIN:VCARD\nVERSION:3.0\n\nN:"+stringTooLong+";givenName;;;\nEND:VCARD\n";
		final List <String> folders = Arrays.asList( Integer.toString(folderId) );
		//import and tests
		final List<ImportResult> results = imp.importData(sessObj, format, new ByteArrayInputStream(vcard.getBytes("UTF-8")), folders, null);
		assertEquals("One import?" , 1 , results.size());
		assertTrue("Should have an error" , results.get(0).hasError() );
		final OXException e = results.get(0).getException();
		assertEquals("Should be truncation error" , Category.TRUNCATED , e.getCategory());
		assertEquals("GIVEN NAME was too long" , ContactField.SUR_NAME.getVCardElementName() , e.getMessageArgs()[0]);
	}
	
	/*
	 * TELEX is not read.
	 */
	@Test public void test7719() throws DBPoolingException, SQLException, OXObjectNotFoundException, NumberFormatException, OXException, UnsupportedEncodingException{
		//setup
		folderId = createTestFolder(FolderObject.CONTACT, sessObj, "vcard7719Folder");
		final String telex = "7787987897897897897";
		final String vcard = "BEGIN:VCARD\nVERSION:2.1\nN:Schmitz;Hansi;;Dr.;\nFN:Dr. Hansi Schmitz\nEMAIL;PREF;INTERNET;CHARSET=Windows-1252:Hansi@Schmitz.super\nEMAIL;TLX:"+telex+"\nEND:VCARD";
		final List <String> folders = Arrays.asList( Integer.toString(folderId) );

		//import and tests
		final List<ImportResult> results = imp.importData(sessObj, format, new ByteArrayInputStream(vcard.getBytes("UTF-8")), folders, null);
		assertEquals("One import?" , 1 , results.size());
		final ImportResult res = results.get(0);
		assertEquals("Should have no error" , null, res.getException() );

		ContactSQLInterface contacts = new RdbContactSQLInterface(sessObj);
		ContactObject co = contacts.getObjectById(Integer.parseInt( res.getObjectId()), Integer.parseInt( res.getFolder() ) );
		assertEquals("Has telex" , telex , co.getTelephoneTelex());
	}
	
	@Test public void testEmpty() throws DBPoolingException, SQLException, UnsupportedEncodingException, NumberFormatException, OXException {
		folderId = createTestFolder(FolderObject.CONTACT, sessObj, "vcard7719Folder");
		final String vcard = "BEGIN:VCARD\nVERSION:2.1\nN:;;;;\nEND:VCARD\n";
		final List <String> folders = Arrays.asList( Integer.toString(folderId) );

		//import and tests
		final List<ImportResult> results = imp.importData(sessObj, format, new ByteArrayInputStream(vcard.getBytes("UTF-8")), folders, null);
		assertEquals("One import?" , 1 , results.size());
		final ImportResult res = results.get(0);
		assertEquals("Should have no error" , null, res.getException() );

		ContactSQLInterface contacts = new RdbContactSQLInterface(sessObj);
		ContactObject co = contacts.getObjectById(Integer.parseInt( res.getObjectId()), Integer.parseInt( res.getFolder() ) );

	}
}