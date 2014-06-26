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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.TestServiceRegistry;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;

public class VCardImportTest extends AbstractVCardTest {
	//workaround for JUnit 3 runner
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(VCardImportTest.class);
	}

	@Test public void test6825_tooMuchInformation() throws OXException, NumberFormatException, OXException, UnsupportedEncodingException, OXException, OXException {
		//setup: building an VCard file with a summary longer than 255 characters.
		folderId = createTestFolder(FolderObject.CONTACT, sessObj,ctx, "vcard6825Folder");
		final String stringTooLong = "aaaaaaaaaabbbbbbbbbbccccccccccddddddddddeeeeeeeeeeffffffffffgggggggggghhhhhhhhhhiiiiiiiiiijjjjjjjjjjkkkkkkkkkkllllllllllmmmmmmmmmmnnnnnnnnnnooooooooooppppppppppqqqqqqqqqqrrrrrrrrrrttttttttttuuuuuuuuuvvvvvvvvvwwwwwwwwwwxxxxxxxxxxyyyyyyyyyyzzzzzzzzzz00000000001111111111222222222233333333334444444444455555555556666666666777777777788888888889999999999";
		final String expected = "aaaaaaaaaabbbbbbbbbbccccccccccddddddddddeeeeeeeeeeffffffffffgggggggggghhhhhhhhhhiiiiiiiiiijjjjjjjjjjkkkkkkkkkkllllllllllmmmmmmmm";
		final String vcard = "BEGIN:VCARD\nVERSION:3.0\n\nN:"+stringTooLong+";givenName;;;\nEND:VCARD\n";
		final List <String> folders = Arrays.asList( Integer.toString(folderId) );
		//import and tests
		final List<ImportResult> results = imp.importData(sessObj, format, new ByteArrayInputStream(vcard.getBytes(com.openexchange.java.Charsets.UTF_8)), folders, null);
		assertTrue("One import?", 1 == results.size());
		assertFalse("Should have no error" , results.get(0).hasError() );

		ImportResult res = results.get(0);
        ContactService contactService = TestServiceRegistry.getInstance().getService(ContactService.class);
        Contact co = contactService.getContact(sessObj, res.getFolder(), res.getObjectId()); 
	    assertEquals("Should have truncated name", expected, co.getSurName());
	}

	/*
	 * TELEX is not read.
	 */
	@Test public void test7719() throws OXException, NumberFormatException, OXException, UnsupportedEncodingException, OXException, OXException {
		//setup
		folderId = createTestFolder(FolderObject.CONTACT, sessObj,ctx, "vcard7719Folder");
		final String telex = "7787987897897897897";
		final String vcard = "BEGIN:VCARD\nVERSION:2.1\nN:Schmitz;Hansi;;Dr.;\nFN:Dr. Hansi Schmitz\nEMAIL;PREF;INTERNET;CHARSET=Windows-1252:Hansi@Schmitz.super\nEMAIL;TLX:"+telex+"\nEND:VCARD";
		final List <String> folders = Arrays.asList( Integer.toString(folderId) );

		//import and tests
		final List<ImportResult> results = imp.importData(sessObj, format, new ByteArrayInputStream(vcard.getBytes(com.openexchange.java.Charsets.UTF_8)), folders, null);
		assertTrue("One import?" , 1 == results.size());
		final ImportResult res = results.get(0);
		assertEquals("Should have no error" , null, res.getException() );

		ContactService contactService = TestServiceRegistry.getInstance().getService(ContactService.class);
		Contact co = contactService.getContact(sessObj, res.getFolder(), res.getObjectId()); 
		assertEquals("Has telex" , telex , co.getTelephoneTelex());
	}

	@Test public void testEmpty() throws UnsupportedEncodingException, NumberFormatException, OXException, OXException, OXException {
		folderId = createTestFolder(FolderObject.CONTACT, sessObj,ctx, "vcard7719Folder");
		final String vcard = "BEGIN:VCARD\nVERSION:2.1\nN:;;;;\nEND:VCARD\n";
		final List <String> folders = Arrays.asList( Integer.toString(folderId) );

		//import and tests
		final List<ImportResult> results = imp.importData(sessObj, format, new ByteArrayInputStream(vcard.getBytes(com.openexchange.java.Charsets.UTF_8)), folders, null);
		assertTrue("One import?" , 1 == results.size());
		final ImportResult res = results.get(0);
		assertEquals("Should have no error" , null, res.getException() );

		ContactService contactService = TestServiceRegistry.getInstance().getService(ContactService.class);
		contactService.getContact(sessObj, res.getFolder(), res.getObjectId()); 
	}
}
