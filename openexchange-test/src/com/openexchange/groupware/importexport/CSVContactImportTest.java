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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.groupware.importexport.importers.CSVContactImporter;

/**
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class CSVContactImportTest extends AbstractCSVContactTest {
	protected static final Importer imp = new CSVContactImporter();
	public static final String IMPORT_ONE = ContactField.GIVEN_NAME.getReadableName()+","+ContactField.EMAIL1.getReadableName()+"\nTobias Prinz, tobias.prinz@open-xchange.com";
	public static final String IMPORT_MULTIPLE = IMPORT_ONE + "\nLaguna, francisco.laguna@open-xchange.com\n";
	public static final String IMPORT_DUPLICATE = IMPORT_MULTIPLE + "Laguna, francisco.laguna@open-xchange.com\n";
	
	//workaround for JUnit 3 runner
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(CSVContactImportTest.class);
	}
	
	@Test public void canImport() throws ImportExportException{
		//normal case
		Map <String, Integer>folderMappings = new HashMap<String, Integer>();
		folderMappings.put(folderId + "", new Integer(Types.CONTACT) );
		assertTrue("Can import?", imp.canImport(sessObj, Format.CSV, folderMappings, null));
		
		//too many 
		try{
			folderMappings.put("blaFolder", new Integer(Types.CONTACT));
			imp.canImport(sessObj, Format.ICAL, folderMappings, null);
			fail("Could import ICAL, but should not");
		} catch (ImportExportException e){
			assertTrue("Cannot import ICAL", true);
		}
		
		//wrong export type
		try{
			imp.canImport(sessObj, Format.ICAL, folderMappings, null);
			fail("Could import ICAL, but should not");
		} catch (ImportExportException e){
			assertTrue("Cannot import ICAL", true);
		}
		
		//wrong folder
		try{
			folderMappings.remove(folderId+"");
			folderMappings.put(folderId + "" , new Integer(Types.APPOINTMENT) );
			imp.canImport(sessObj, Format.CSV, folderMappings, null);
			fail("Could import CSV into designated appointment folder, but should not");
		} catch (ImportExportException e){
			assertTrue("Cannot import CSV into designated appointment folder", true);
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
		Map <String, Integer>folderMappings = new HashMap<String, Integer>();
		folderMappings.put(folderId + "", new Integer(Types.CONTACT) );
		InputStream is = new ByteArrayInputStream( csv.getBytes() );
		return imp.importData(sessObj, Format.CSV, is, folderMappings, null);

	}
}
