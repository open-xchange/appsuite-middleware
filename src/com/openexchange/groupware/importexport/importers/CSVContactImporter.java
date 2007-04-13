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

package com.openexchange.groupware.importexport.importers;

import static com.openexchange.groupware.importexport.csv.CSVLibrary.getFolderObject;
import static com.openexchange.groupware.importexport.csv.CSVLibrary.transformInputStreamToString;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.ContactSetter;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.Format;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.importexport.Importer;
import com.openexchange.groupware.importexport.csv.CSVParser;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionClasses;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionFactory;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.EffectivePermission;
import com.openexchange.sessiond.SessionObject;

@OXExceptionSource(
	classId=ImportExportExceptionClasses.CSVCONTACTIMPORTER, 
	component=Component.IMPORT_EXPORT)
@OXThrowsMultiple(
	category={
		Category.USER_INPUT,
		Category.PROGRAMMING_ERROR,
		Category.PROGRAMMING_ERROR,
		Category.USER_INPUT,},
	desc={"","","", ""}, 
	exceptionId={0,1,2,3}, 
	msg={
		"Can only import into one folder at a time.",
		"Cannot import this kind of data. Use method canImport() first.",
		"Cannot read given InputStream.",
		"Could not find a field named %s"})
		
public class CSVContactImporter implements Importer {

	protected static ImportExportExceptionFactory EXCEPTIONS = new ImportExportExceptionFactory(CSVContactImporter.class);
	
	public boolean canImport(SessionObject sessObj, Format format,
			List<String> folders,
			Map<String, String[]> optionalParams) throws ImportExportException {
		String folder;
		if(folders.size() != 1){
			throw EXCEPTIONS.create(0);
		} else {
			folder = folders.get(0);
		}
		if(! format.equals(Format.CSV) ){
			return false;
		}
		FolderObject fo = null;
		try {
			fo = getFolderObject(sessObj, folder);
		} catch (ImportExportException e) {
			return false;
		} 
		if(fo == null){
			System.out.println("Folder does not exist: " + folder);
			return false;
		}
		//check format of folder
		if ( fo.getModule() != FolderObject.CONTACT ){
			return false;
		}
		//check read access to folder
		EffectivePermission perm;
		try {
			perm = fo.getEffectiveUserPermission(sessObj.getUserObject().getId(), sessObj.getUserConfiguration());
		} catch (DBPoolingException e) {
			return false;
		} catch (SQLException e) {
			return false;
		}
		return perm.canCreateObjects();
	}
	

	public List<ImportResult> importData(SessionObject sessObj, Format format,
			InputStream is, List<String> folders,
			Map<String, String[]> optionalParams) throws ImportExportException {
		if(! canImport(sessObj, format, folders, optionalParams)){
			throw EXCEPTIONS.create(1);
		}
		String folder = folders.get(0);
		String csvStr = transformInputStreamToString(is);
		List <List <String> >csv = new CSVParser().parse(csvStr);
		Iterator< List<String> > iter = csv.iterator();
		//get header fields
		List<String> fields = (List<String>) iter.next();
		
		//reading entries...
		List<ImportResult> results = new LinkedList<ImportResult>();
		ContactSetter conSet = new ContactSetter();
		ContactSQLInterface contactsql = new RdbContactSQLInterface(sessObj);
		while(iter.hasNext()){
			//...and writing them
			results.add( writeEntry(fields, iter.next(), folder, contactsql, conSet));
		}
		return results;
	}
	
	/**
	 * 
	 * @param fields Headers of the table; column title
	 * @param entry A list of row cells.
	 * @param folder The folder this is line meant to be written into
	 * @param contactsql The interface to store data in the OX
	 * @param conSet The ContactSetter used for translating the given data 
	 * @return a report containing either the object ID of the entry created OR an error message
	 */
	protected ImportResult writeEntry(List<String> fields, List<String> entry, String folder, ContactSQLInterface contactsql, ContactSetter conSet){
		final ImportResult result = new ImportResult();
		final ContactObject contactObj = new ContactObject();
		result.setFolder( folder );
		try{
			for(int i = 0; i < fields.size(); i ++){
				final ContactField currField = ContactField.getByDisplayName(fields.get(i));
				if(currField == null){
					result.setException(EXCEPTIONS.create(3, fields.get(i)));
				} else {
					currField.doSwitch(conSet, contactObj, entry.get(i));
				}
			}
			contactObj.setParentFolderID(Integer.parseInt( folder.trim() ));
			contactsql.insertContactObject(contactObj);
			result.setDate( contactObj.getLastModified() );
			result.setObjectId( Integer.toString( contactObj.getObjectID() ) );
		} catch (ContactException e) {
			result.setException(e);
		} catch (OXException e) {
			result.setException(e);
		}
		return result;
	}
}
