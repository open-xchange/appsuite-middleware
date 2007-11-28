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
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedList;
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
import com.openexchange.groupware.importexport.importers.CSVContactImporter;

/**
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class CSVContactImportTest extends AbstractContactTest {
	public String IMPORT_HEADERS = ContactTestData.IMPORT_HEADERS;
	public String IMPORT_ONE = ContactTestData.IMPORT_ONE;
	public String IMPORT_MULTIPLE = ContactTestData.IMPORT_MULTIPLE;
	public String IMPORT_DUPLICATE = IMPORT_MULTIPLE + "Laguna, francisco.laguna@open-xchange.com, Francisco Laguna\n";
	public String IMPORT_EMPTY = IMPORT_HEADERS+",,";
	public boolean doDebugging = false;
	
	public String notASingleImport = "I_E-0804";
	public String malformedCSV = "I_E-1000";
	public String malformedDate = "CON-0600";
	
	//workaround for JUnit 3 runner
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(CSVContactImportTest.class);
	}
	
	public CSVContactImportTest(){
		super();
		imp = new CSVContactImporter();
		defaultFormat = Format.CSV;
	}
	
	@Test public void canImport() throws ImportExportException{
		List <String> folders = new LinkedList<String>();
		folders.add(Integer.toString(folderId));
		//normal case
		assertTrue("Can import?", imp.canImport(sessObj, defaultFormat, folders, null));
		
		//too many
		folders.add("blaFolder");
		try{
			imp.canImport(sessObj, Format.CSV, folders, null);
			fail("Could import two folders, but should not");
		} catch (ImportExportException e){
			assertTrue("Cannot import more than one folder", true);
		}
		
		//wrong export type
		folders.remove("blaFolder");
		try{
			assertTrue("Cannot import ICAL" , !imp.canImport(sessObj, Format.ICAL, folders, null) );
		} catch (ImportExportException e){
			fail("Exception caught, but only 'false' value expected");
		}

	}
	
	@Test public void importOneContact() throws NumberFormatException, Exception{
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
		
		//cleaning up
		contactSql.deleteContactObject(Integer.parseInt(res.getObjectId()), Integer.parseInt(res.getFolder()), res.getDate());
	}
	
	@Test public void importEmpty() throws NumberFormatException, Exception{
		List<ImportResult> results = importStuff(IMPORT_EMPTY); 
		assertEquals("One result?" , 1, results.size());
		ImportResult res = results.get(0);
		if(res.hasError()){
			res.getException().printStackTrace();
		}
		assertTrue( res.isCorrect() );

		//basic check: 1 entry in folder
		final ContactSQLInterface contactSql = new RdbContactSQLInterface(sessObj);
		assertEquals("One contact in folder?", 1, contactSql.getNumberOfContacts(folderId));

		//cleaning up
		contactSql.deleteContactObject(Integer.parseInt(res.getObjectId()), Integer.parseInt(res.getFolder()), res.getDate());
	}

	
	@Test public void importListOfContacts() throws NumberFormatException, Exception{
		List<ImportResult> results = importStuff(IMPORT_MULTIPLE); 
		assertEquals("Two results?" , results.size(), 2);
		for(ImportResult res : results){
			if(res.hasError()){
				res.getException().printStackTrace();
			}
			assertTrue( res.isCorrect() );
		}
		
		//basic check
		final ContactSQLInterface contactSql = new RdbContactSQLInterface(sessObj);
		assertEquals("Two contacts in folder?", 2, contactSql.getNumberOfContacts(folderId));
		
		//cleaning up
		for(ImportResult res : results){
			contactSql.deleteContactObject(Integer.parseInt(res.getObjectId()), Integer.parseInt(res.getFolder()), res.getDate());
		}
	}
	
	@Test public void importBullshit(){
		List <String> folders = Arrays.asList( Integer.toString(folderId) );
		InputStream is = new ByteArrayInputStream( "Bla\nbla\nbla".getBytes() );

		try {
			imp.importData(sessObj, defaultFormat, is, folders, null);
		} catch (ImportExportException e) {
			assertEquals("Checking correct file with wrong header" , notASingleImport, e.getErrorCode());
			return;
		}
		fail("Should throw exception");
	}
	
	@Test public void importBullshit2(){
		List <String> folders = Arrays.asList( Integer.toString(folderId) );
		InputStream is = new ByteArrayInputStream( "Bla\nbla,bla".getBytes() );

		try {
			imp.importData(sessObj, defaultFormat, is, folders, null);
		} catch (ImportExportException e) {
			assertEquals("Checking malformed file with wrong header" , malformedCSV, e.getErrorCode());
			return;
		}
		fail("Should throw exception");
	}
	
	/*
	 * Currently, the API allows for duplicate entries...
	 */
	@Test public void importOfDuplicates() throws NumberFormatException, Exception{
		List<ImportResult> results = importStuff(IMPORT_DUPLICATE); 
		assertEquals("Three results?" , 3, results.size());
		for(ImportResult res : results){
			if(res.hasError()){
				res.getException().printStackTrace();
			}
			assertTrue( res.isCorrect() );
		}

		final ContactSQLInterface contactSql = new RdbContactSQLInterface(sessObj);
		assertEquals("Three contacts in folder?", 3, contactSql.getNumberOfContacts(folderId));
		
		//cleaning up
		for(ImportResult res : results){
			contactSql.deleteContactObject(Integer.parseInt(res.getObjectId()), Integer.parseInt(res.getFolder()), res.getDate());
		}
	}
	
	@Test public void importIllegalDate() throws NumberFormatException, Exception{
		List<ImportResult> results = importStuff(ContactField.GIVEN_NAME.getReadableName() + " , " + ContactField.BIRTHDAY.getReadableName() + "\n" + "Tobias Prinz , 1981/04/01"); 
		assertEquals("One result?" , results.size(), 1);
		ImportResult res = results.get(0);
		assertTrue("Got bug?" , res.hasError() );

		assertEquals("Caught class cast exception", malformedDate , res.getException().getErrorCode() );
	}
	
	/*
	 * Counting the TIMEZONE element? 
	 */
	@Test public void bug7109() throws ImportExportException, UnsupportedEncodingException{
		List<ImportResult> results1 = importStuff(ContactField.DISPLAY_NAME.getReadableName()+", "+ContactField.GIVEN_NAME.getReadableName() + " , " + ContactField.BIRTHDAY.getReadableName() + "\n" + "Tobias Prinz , "+ "Tobias Prinz , "+System.currentTimeMillis());
		List<ImportResult> results2 = importStuff(ContactField.DISPLAY_NAME.getReadableName()+", "+ContactField.GIVEN_NAME.getReadableName() + " , " + ContactField.BIRTHDAY.getReadableName() + "\n" + "Tobias Prinz , "+ "Tobias Prinz , 1981/04/01");
		List<ImportResult> results3 = importStuff(ContactField.DISPLAY_NAME.getReadableName()+", "+ContactField.GIVEN_NAME.getReadableName() + " , " + "stupidColumnName\n" + "Tobias Prinz , "+ "Tobias Prinz , 1981/04/01");
		List<ImportResult> results4 = importStuff(ContactField.DISPLAY_NAME.getReadableName()+", "+ContactField.BIRTHDAY.getReadableName() + "\nTobias Prinz, 1981/04/01");
		assertEquals("One result for first attempt?" , results1.size(), 1);
		assertEquals("One result for second attempt?" , results2.size(), 1);
		assertEquals("One result for third attempt?" , results3.size(), 1);
		assertEquals("One result for fourth attempt?" , results4.size(), 1);
		
		ImportResult tempRes = results1.get(0);
		assertTrue("Attempt 1 has no error", tempRes.isCorrect());
		assertTrue("Entry after attempt 1 exists?", existsEntry(Integer.parseInt(tempRes.getObjectId())));
		
		tempRes = results2.get(0);
		assertTrue("Attempt 2 has error", tempRes.hasError());
		OXException exc = tempRes.getException();
		assertEquals("Malformed date?" , malformedDate, exc.getErrorCode());
		
		tempRes = results3.get(0);
		assertTrue("Attempt 3 has error", tempRes.hasError());
		exc = tempRes.getException();
		assertEquals("Only a warning" , Category.WARNING , exc.getCategory());
		assertTrue("Entry after attempt 3 exists?", existsEntry(Integer.parseInt(tempRes.getObjectId())));
		
		tempRes = results4.get(0);
		assertTrue("Attempt 4 has error", tempRes.hasError());
		exc = tempRes.getException();
		assertEquals("Malformed date?" , malformedDate, exc.getErrorCode());
		
		try	{
			importStuff("stupidColumnName, yet another stupid column name\n" + "Tobias Prinz , 1981/04/01");
			fail("Importing without any useful column titles should fail.");
		} catch (ImportExportException exc1){
			assertEquals("Could not translate any column title", notASingleImport, exc1.getErrorCode());
		}
	}
	
	/*
	 * This was listed as 6825, 7107 or 7386
	 */
	@Test public void bugTooMuchInformation() throws ImportExportException, UnsupportedEncodingException{
		final List<ImportResult> results = importStuff(ContactField.DISPLAY_NAME.getReadableName()+", "+ContactField.GIVEN_NAME.getReadableName() + "," + ContactField.SUFFIX.getReadableName() + "\nAli, Hadschi Halef Omar, Ben Hadschi Abul Abbas Ibn Hadschi Dawuhd Ben Hadschi Abul Abbas Ibn Hadschi Dawuhd Ben Hadschi Abul Abbas Ibn Hadschi Dawuhd Ben Hadschi Abul Abbas Ibn Hadschi Dawuhd al Gossarah");
		assertEquals("One result?" , 1, results.size());
		ImportResult res = results.get(0);
		OXException exc = res.getException();
		assertEquals("Category correct?" , exc.getCategory(), Category.TRUNCATED);
		assertEquals("Fields correct?" ,  ContactField.SUFFIX.getReadableName() , exc.getMessageArgs()[0]);
	}
	
	/*
	 * "private" flag is being set
	 */
	@Test public void bug7710() throws UnsupportedEncodingException, NumberFormatException, OXException{
		final String file = ContactField.DISPLAY_NAME.getReadableName()+", "+ContactField.GIVEN_NAME.getReadableName() + " , " + ContactField.PRIVATE_FLAG.getReadableName() + "\nTobias Prinz, Tobias Prinz,true";
		final List<ImportResult> results = importStuff(file);
		assertEquals("Only one result", 1, results.size());
		ImportResult res = results.get(0);
		ContactObject conObj = getEntry( Integer.parseInt( res.getObjectId() ) );
		assertTrue("Is private?", conObj.getPrivateFlag());
	}
	
	
	protected void checkFirstResult(int objectID ) throws OXException{
		final ContactObject co = new RdbContactSQLInterface(sessObj).getObjectById(objectID, folderId);
		assertEquals("Checking name" ,  NAME1 , co.getGivenName());
		assertEquals("Checking e-Mail" ,  EMAIL1 , co.getEmail1());
	}
}
