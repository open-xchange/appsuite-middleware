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

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.groupware.importexport.importers.OutlookCSVContactImporter;

public class OutlookCSVContactImportTest extends AbstractContactTest{
	public String IMPORT_HEADERS = ContactField.GIVEN_NAME.getEnglishOutlookName()+","+ContactField.EMAIL1.getEnglishOutlookName()+","+ContactField.BIRTHDAY.getEnglishOutlookName()+"\n";
	public String IMPORT_ONE = IMPORT_HEADERS + NAME1+", "+EMAIL1+", "+DATE1;
	public static String DATE1 = "4/1/1981";

	public OutlookCSVContactImportTest(){
		super();
		defaultFormat = Format.OUTLOOK_CSV;
		imp = new OutlookCSVContactImporter();
	}
	
	//workaround for JUnit 3 runner
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(OutlookCSVContactImportTest.class);
	}

	protected void checkFirstResult(int objectID ) throws OXException{
		final ContactObject co = new RdbContactSQLInterface(sessObj).getObjectById(objectID, folderId);
		assertEquals("Checking name" ,  NAME1 , co.getGivenName());
		assertEquals("Checking e-Mail" ,  EMAIL1 , co.getEmail1());
		
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		Date compDate = null;
		try {
			compDate = sdf.parse(DATE1);
		} catch (ParseException e) {
			System.out.println("Setup error: Date format used for comparison sucks.");
		}
		assertDateEquals(compDate, co.getBirthday());
	}
	
	@Test
	public void importOneContact() throws NumberFormatException, Exception {
		List<ImportResult> results = importStuff(IMPORT_ONE); 
		assertEquals("One result?" , results.size(), 1);
		ImportResult res = results.get(0);
		if(res.hasError()){
			res.getException().printStackTrace();
		}
		assertTrue( res.isCorrect() );

		//basic check: 1 entry in folder
		final ContactSQLInterface contactSql = new RdbContactSQLInterface(sessObj);
		assertEquals("One contact in folder?", 1, contactSql.getNumberOfContacts(folderId));

		//detailed check:
		checkFirstResult(
			Integer.parseInt(
				res.getObjectId()));
		
	}
	
	@Test
	public void bug7105() throws NumberFormatException, Exception {
		List<ImportResult> results = importStuff(IMPORT_ONE+"\n"+NAME2); 
		assertEquals("Two results?" , 2 , results.size());

		int i = 0;
		for(ImportResult res : results){
			assertEquals("Entry " + (i++) + " is correct?" , null, res.getException());
		}
		
	}
	
	@Test
	public void bug7552() throws NumberFormatException, Exception {
		List<ImportResult> results = importStuff(IMPORT_HEADERS + NAME1+", "+EMAIL1+", 1.4.1981"); 
		assertEquals("One result?" , 1, results.size());
		ImportResult res = results.get(0);
		if(res.hasError()){
			res.getException().printStackTrace();
		}

		//check date set correctly though German style
		final ContactSQLInterface contactSql = new RdbContactSQLInterface(sessObj);
		Date birthday = contactSql.getObjectById( Integer.parseInt(res.getObjectId()) , Integer.parseInt(res.getFolder()) ).getBirthday();
		assertDateEquals( new SimpleDateFormat("dd.MM.yyyy").parse("1.4.1981") , birthday);

		//cleaning up
		contactSql.deleteContactObject(Integer.parseInt(res.getObjectId()), Integer.parseInt(res.getFolder()), res.getDate());
	}
	
	@Test
	public void bug6825_tooMuchInformation() throws ImportExportException, UnsupportedEncodingException {
		List<ImportResult> results = importStuff(
				IMPORT_HEADERS + 
				"my name is definately too long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long-long"+
				", "
				+EMAIL1+
				", 1.4.1981"); 
		assertEquals("One result?" , 1, results.size());
		ImportResult res = results.get(0);
		assertTrue("Has error" , res.hasError());
		final OXException dirk = res.getException();
		assertEquals("Is truncation error?" , Category.TRUNCATED , dirk.getCategory());
		assertEquals("GIVEN_NAME is too long?" , ContactField.GIVEN_NAME.getEnglishOutlookName() , dirk.getMessageArgs()[0]);
	}

	/*
	 * "private" flag is being set
	 */
	@Test public void bug7710() throws UnsupportedEncodingException, NumberFormatException, OXException{
		String file = ContactField.SUR_NAME.getGermanOutlookName() + ", " + ContactField.PRIVATE_FLAG.getGermanOutlookName() + "\nTobias Prinz,PRIVAT";
		List<ImportResult> results = importStuff(file);
		assertEquals("Only one result", 1, results.size());
		ImportResult res = results.get(0);
		ContactObject conObj = getEntry( Integer.parseInt( res.getObjectId() ) );
		assertTrue("Is private?", conObj.getPrivateFlag());
		
		file = ContactField.SUR_NAME.getGermanOutlookName() + ", " + ContactField.PRIVATE_FLAG.getGermanOutlookName() + "\nTobias Prinz,ÖFFENTLICH";
		results = importStuff(file);
		assertEquals("Only one result", 1, results.size());
		res = results.get(0);
		conObj = getEntry( Integer.parseInt( res.getObjectId() ) );
		assertTrue("Is private?", !conObj.getPrivateFlag());
	}
	
	public void assertDateEquals(Date date1 , Date date2){
		Calendar c1 = new GregorianCalendar(), c2 = new GregorianCalendar();
		c1.setTime(date1);
		c2.setTime(date2);
		assertEquals("Day", c1.get(Calendar.DAY_OF_MONTH),c2.get(Calendar.DAY_OF_MONTH));
		assertEquals("Month", c1.get(Calendar.MONTH),c2.get(Calendar.MONTH));
		assertEquals("Year", c1.get(Calendar.YEAR),c2.get(Calendar.YEAR));
	}

	
}
