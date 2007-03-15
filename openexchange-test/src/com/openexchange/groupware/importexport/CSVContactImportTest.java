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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.groupware.importexport.importers.CSVContactImporter;

/**
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class CSVContactImportTest extends AbstractCSVContactTest {
	protected Importer imp = new CSVContactImporter();
	public static String NAME1 = "Prinz";
	public static String EMAIL1 = "tobias.prinz@open-xchange.com";
	public static String NAME2 = "Laguna";
	public static String EMAIL2 = "francisco.laguna@open-xchange.com";
	public static String IMPORT_ONE = ContactField.GIVEN_NAME.getReadableName()+","+ContactField.EMAIL1.getReadableName()+"\n"+NAME1+", "+EMAIL1;
	public static String IMPORT_MULTIPLE = IMPORT_ONE + "\n"+NAME2+", "+EMAIL2+"\n";
	public static String IMPORT_DUPLICATE = IMPORT_MULTIPLE + "Laguna, francisco.laguna@open-xchange.com\n";
	public boolean doDebugging = false;
	
	//workaround for JUnit 3 runner
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(CSVContactImportTest.class);
	}
	
	@Test public void canImport() throws ImportExportException{
		List <String> folders = new LinkedList<String>();
		folders.add(Integer.toString(folderId));
		//normal case
		assertTrue("Can import?", imp.canImport(sessObj, Format.CSV, folders, null));
		
		//too many
		folders.add("blaFolder");
		try{
			imp.canImport(sessObj, Format.ICAL, folders, null);
			fail("Could import two foldersL, but should not");
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
	
	/*
	 * Currently, the API allows for duplicate entries...
	 */
	@Test public void testImportOfDuplicates() throws NumberFormatException, Exception{
		List<ImportResult> results = importStuff(IMPORT_DUPLICATE); 
		assertEquals("Three results?" , results.size(), 3);
		for(ImportResult res : results){
			if(res.hasError()){
				res.getException().printStackTrace();
			}
			assertTrue( res.isCorrect() );
		}

		final ContactSQLInterface contactSql = new RdbContactSQLInterface(sessObj);
		assertEquals("Three contacts in folder?", contactSql.getNumberOfContacts(folderId) , 3);
		
		//cleaning up
		for(ImportResult res : results){
			contactSql.deleteContactObject(Integer.parseInt(res.getObjectId()), Integer.parseInt(res.getFolder()), res.getDate());
		}
	}
	
	protected List<ImportResult> importStuff(String csv) throws ImportExportException{
		List <String> folders = Arrays.asList( Integer.toString(folderId) );
		InputStream is = new ByteArrayInputStream( debug(csv).getBytes() );
		return debug(imp.importData(sessObj, Format.CSV, is, folders, null));

	}

	protected String debug(String str){
		if(doDebugging){
			System.out.println(str);
		}
		return str;
	}
	
	protected List<ImportResult> debug(List<ImportResult> results){
		if(doDebugging){
			System.out.println("Result---BEGIN");
			for(ImportResult res: results){
				if(res.hasError()){
					System.out.println("Error: BEGIN---");
					res.getException().printStackTrace();
					System.out.println("---END");
				} else {
					System.out.println("Worked: id = " + res.getObjectId());
				}
			}
			System.out.println("Result---END\n");
		}
		return results;
	}
	
	protected void checkFirstResult(int objectID ) throws OXException{
		final ContactObject co = new RdbContactSQLInterface(sessObj).getObjectById(objectID, folderId);
		assertEquals("Checking name" ,  NAME1 , co.getGivenName());
		assertEquals("Checking e-Mail" ,  EMAIL1 , co.getEmail1());
	}
}
