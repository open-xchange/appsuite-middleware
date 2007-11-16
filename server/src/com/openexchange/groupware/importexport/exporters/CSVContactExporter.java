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

package com.openexchange.groupware.importexport.exporters;

import static com.openexchange.groupware.importexport.csv.CSVLibrary.getFolderId;
import static com.openexchange.groupware.importexport.csv.CSVLibrary.getFolderObject;
import static com.openexchange.groupware.importexport.csv.CSVLibrary.transformIntArrayToSet;
import static com.openexchange.groupware.importexport.csv.CSVLibrary.transformSetToIntArray;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.ContactGetter;
import com.openexchange.groupware.contact.helpers.ContactStringGetter;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.Exporter;
import com.openexchange.groupware.importexport.Format;
import com.openexchange.groupware.importexport.SizedInputStream;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionClasses;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionFactory;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.EffectivePermission;
import com.openexchange.sessiond.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
@OXExceptionSource(
	classId=ImportExportExceptionClasses.CSVCONTACTEXPORTER, 
	component=Component.IMPORT_EXPORT)
@OXThrowsMultiple(
	category={
		Category.PERMISSION, 
		Category.SUBSYSTEM_OR_SERVICE_DOWN,
		Category.SUBSYSTEM_OR_SERVICE_DOWN,
		Category.CODE_ERROR,
		Category.INTERNAL_ERROR}, 
	desc={"","","","",""}, 
	exceptionId={0,1,2,3,4}, 
	msg={
		"Could not export the folder %s in the format %s.",
		"Could not load folder %s",
		"Could not load contacts",
		"Could not create folder id from string %s",
		"Could not encode as UTF-8"})

public class CSVContactExporter implements Exporter {
	
	protected final static int[] POSSIBLE_FIELDS = {
		DataObject.OBJECT_ID,
		DataObject.CREATED_BY,
		DataObject.CREATION_DATE,
		DataObject.LAST_MODIFIED,
		DataObject.MODIFIED_BY,
//		CommonObject.PRIVATE_FLAG,
//		CommonObject.CATEGORIES,
		ContactObject.SUR_NAME,
		ContactObject.ANNIVERSARY,
		ContactObject.ASSISTANT_NAME,
		ContactObject.BIRTHDAY,
		ContactObject.BRANCHES,
		ContactObject.BUSINESS_CATEGORY,
		ContactObject.CATEGORIES,
		ContactObject.CELLULAR_TELEPHONE1,
		ContactObject.CELLULAR_TELEPHONE2,
		ContactObject.CITY_BUSINESS,
		ContactObject.CITY_HOME,
		ContactObject.CITY_OTHER,
		ContactObject.COMMERCIAL_REGISTER,
		ContactObject.COMPANY,
		ContactObject.COUNTRY_BUSINESS,
		ContactObject.COUNTRY_HOME,
		ContactObject.COUNTRY_OTHER,
		ContactObject.DEPARTMENT,
		ContactObject.DISPLAY_NAME,
//		ContactObject.DISTRIBUTIONLIST,
		ContactObject.EMAIL1,
		ContactObject.EMAIL2,
		ContactObject.EMAIL3,
		ContactObject.EMPLOYEE_TYPE,
		ContactObject.FAX_BUSINESS,
		ContactObject.FAX_HOME,
		ContactObject.FAX_OTHER,
//		ContactObject.FILE_AS,
		ContactObject.FOLDER_ID,
		ContactObject.GIVEN_NAME,
//		ContactObject.IMAGE1,
//		ContactObject.IMAGE1_CONTENT_TYPE,
		ContactObject.INFO,
		ContactObject.INSTANT_MESSENGER1,
		ContactObject.INSTANT_MESSENGER2,
//		ContactObject.LINKS,
		ContactObject.MANAGER_NAME,
		ContactObject.MARITAL_STATUS,
		ContactObject.MIDDLE_NAME,
		ContactObject.NICKNAME,
		ContactObject.NOTE,
		ContactObject.NUMBER_OF_CHILDREN,
		ContactObject.NUMBER_OF_EMPLOYEE,
		ContactObject.POSITION,
		ContactObject.POSTAL_CODE_BUSINESS,
		ContactObject.POSTAL_CODE_HOME,
		ContactObject.POSTAL_CODE_OTHER,
//		ContactObject.PRIVATE_FLAG,
		ContactObject.PROFESSION,
		ContactObject.ROOM_NUMBER,
		ContactObject.SALES_VOLUME,
		ContactObject.SPOUSE_NAME,
		ContactObject.STATE_BUSINESS,
		ContactObject.STATE_HOME,
		ContactObject.STATE_OTHER,
		ContactObject.STREET_BUSINESS,
		ContactObject.STREET_HOME,
		ContactObject.STREET_OTHER,
		ContactObject.SUFFIX,
		ContactObject.TAX_ID,
		ContactObject.TELEPHONE_ASSISTANT,
		ContactObject.TELEPHONE_BUSINESS1,
		ContactObject.TELEPHONE_BUSINESS2,
		ContactObject.TELEPHONE_CALLBACK,
		ContactObject.TELEPHONE_CAR,
		ContactObject.TELEPHONE_COMPANY,
		ContactObject.TELEPHONE_HOME1,
		ContactObject.TELEPHONE_HOME2,
		ContactObject.TELEPHONE_IP,
		ContactObject.TELEPHONE_ISDN,
		ContactObject.TELEPHONE_OTHER,
		ContactObject.TELEPHONE_PAGER,
		ContactObject.TELEPHONE_PRIMARY,
		ContactObject.TELEPHONE_RADIO,
		ContactObject.TELEPHONE_TELEX,
		ContactObject.TELEPHONE_TTYTDD,
		ContactObject.TITLE,
		ContactObject.URL,
		ContactObject.USERFIELD01,
		ContactObject.USERFIELD02,
		ContactObject.USERFIELD03,
		ContactObject.USERFIELD04,
		ContactObject.USERFIELD05,
		ContactObject.USERFIELD06,
		ContactObject.USERFIELD07,
		ContactObject.USERFIELD08,
		ContactObject.USERFIELD09,
		ContactObject.USERFIELD10,
		ContactObject.USERFIELD11,
		ContactObject.USERFIELD12,
		ContactObject.USERFIELD13,
		ContactObject.USERFIELD14,
		ContactObject.USERFIELD15,
		ContactObject.USERFIELD16,
		ContactObject.USERFIELD17,
		ContactObject.USERFIELD18,
		ContactObject.USERFIELD19,
		ContactObject.USERFIELD20,
		ContactObject.DEFAULT_ADDRESS};

	private static final ImportExportExceptionFactory EXCEPTIONS = new ImportExportExceptionFactory(CSVContactExporter.class);
	private static final Log LOG = LogFactory.getLog(CSVContactExporter.class);
	
	public boolean canExport(final Session sessObj, final Format format, final String folder, final Map <String, String[]> optionalParams)  throws ImportExportException {
		if( !format.equals(Format.CSV) ){
			return false;
		}
		FolderObject fo;
		try {
			fo = getFolderObject(sessObj, folder);
		} catch (final ImportExportException e) {
			return false;
		}
		//check format of folder
		if ( fo.getModule() != FolderObject.CONTACT ){
			return false;
		}
		//check read access to folder
		EffectivePermission perm;
		try {
			perm = fo.getEffectiveUserPermission(sessObj.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessObj.getUserId(), sessObj.getContext()));
		} catch (final DBPoolingException e) {
			return false;
		} catch (final SQLException e) {
			return false;
		}
		return perm.canReadAllObjects();
	}

	
	public SizedInputStream exportData(final Session sessObj, final Format format, final String folder, 
			final int[] fieldsToBeExported, final Map <String, String[]> optionalParams) throws ImportExportException {
		if(! canExport(sessObj, format, folder, optionalParams)){
			EXCEPTIONS.create(0, folder, format);
		}
		final int folderId = getFolderId(folder);
		final ContactSQLInterface contactSql = new RdbContactSQLInterface(sessObj);
		int[] cols = null;
		if( fieldsToBeExported == null || fieldsToBeExported.length == 0){
			cols = POSSIBLE_FIELDS;
		} else {
			final Set <Integer> s1 = transformIntArrayToSet(fieldsToBeExported);
			final Set <Integer> s2 = transformIntArrayToSet(POSSIBLE_FIELDS);
			s1.retainAll(s2);
			cols = transformSetToIntArray( s1 );
		}
		SearchIterator conIter;
		try {
			conIter = contactSql.getContactsInFolder(folderId, 0, contactSql.getNumberOfContacts(folderId), 0, "ASC", cols);
		} catch (final OXException e) {
			throw EXCEPTIONS.create(2, e);
		}
		final StringBuilder ret = new StringBuilder();
		ret.append( convertToLine( com.openexchange.groupware.importexport.csv.CSVLibrary.convertToList(cols) ) );
		
		while(conIter.hasNext()){
			ContactObject current;
			try {
				current = (ContactObject) conIter.next();
				ret.append( convertToLine( convertToList(current, cols) ) );
			} catch (final SearchIteratorException e) {
				LOG.error("Could not retrieve contact from folder " + folder + " using a FolderIterator, exception was: ", e);
			} catch (final OXException e) {
				LOG.error("Could not retrieve contact from folder " + folder + ", OXException was: ", e);
			}
			
		}
		try {
			return new SizedInputStream(
					new ByteArrayInputStream ( ret.toString().getBytes("UTF-8")) ,  
					ret.toString().getBytes("UTF-8").length,
					Format.CSV);
		} catch (final UnsupportedEncodingException e) {
			throw EXCEPTIONS.create(4);
		}
	}

	
	public SizedInputStream exportData(final Session sessObj, final Format format, final String folder, 
			final int objectId,	final int[] fieldsToBeExported, final Map <String, String[]> optionalParams) throws ImportExportException {
		if(! canExport(sessObj, format, folder, optionalParams)){
			EXCEPTIONS.create(0, folder, format);
		}
		final int folderId = getFolderId(folder);
		final ContactSQLInterface contactSql = new RdbContactSQLInterface(sessObj);
		int[] cols;
		if( fieldsToBeExported == null || fieldsToBeExported.length == 0){
			cols = POSSIBLE_FIELDS;
		} else {
			cols = fieldsToBeExported;
		}
		ContactObject conObj;
		try {
			conObj = contactSql.getObjectById(objectId, folderId);
		} catch (final OXException e) {
			throw EXCEPTIONS.create(2, e);
		}

		final StringBuilder ret = new StringBuilder();
		ret.append( convertToLine( com.openexchange.groupware.importexport.csv.CSVLibrary.convertToList(cols) ) );
		ret.append( convertToLine( convertToList(conObj, cols) ) );
		
		try {
			return new SizedInputStream(
					new ByteArrayInputStream ( ret.toString().getBytes("UTF-8")) ,  
					ret.toString().getBytes().length,
					Format.CSV);
		} catch (UnsupportedEncodingException e) {
			LOG.fatal(e);
			throw EXCEPTIONS.create(4);
		}
	}
	
	protected List<String> convertToList(final ContactObject conObj, final int[] cols){
		final List<String> l = new LinkedList<String>();
		final ContactStringGetter getter = new ContactStringGetter();
		getter.setDelegate( new ContactGetter() );
		ContactField tempField;
		for(final int col : cols){
			tempField = ContactField.getByValue(col);
			try {
				l.add( (String) tempField.doSwitch(getter, conObj) );
			} catch (final ContactException e) {
				l.add("");
			}
		}
		return l;
	}
	
	protected String convertToLine(final List<String> line){
		final StringBuilder bob = new StringBuilder();
		for(String str : line){
			bob.append("\"");
			str = str.replace("\"", "\"\"");
			bob.append(str);
			bob.append("\"");
			bob.append(com.openexchange.groupware.importexport.csv.CSVLibrary.CELL_DELIMITER);
		}
		bob.setCharAt(bob.length() - 1, com.openexchange.groupware.importexport.csv.CSVLibrary.ROW_DELIMITER);
		return bob.toString();
	}

}
