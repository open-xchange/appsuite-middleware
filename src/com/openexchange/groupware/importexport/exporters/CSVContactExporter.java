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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.cache.FolderCacheManager;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.Exporter;
import com.openexchange.groupware.importexport.Format;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionClasses;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionFactory;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.EffectivePermission;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;

@OXExceptionSource(
	classId=ImportExportExceptionClasses.CSVCONTACTEXPORTER, 
	component=Component.IMPORT_EXPORT)
@OXThrowsMultiple(
	category={
		Category.PERMISSION, 
		Category.SUBSYSTEM_OR_SERVICE_DOWN,
		Category.SUBSYSTEM_OR_SERVICE_DOWN,
		Category.PROGRAMMING_ERROR}, 
	desc={"","","",""}, 
	exceptionId={0,1,2,3}, 
	msg={
		"Could not export the folder %s in the format %s.",
		"Could not load folder %s",
		"Could not load contacts",
		"Could not create folderId from String %s"})

public class CSVContactExporter implements Exporter {
	
	protected final static char CELL_DELIMITER = ',';
	protected final static char ROW_DELIMITER = '\n';
	
	protected final static int[] POSSIBLE_FIELDS = {
		DataObject.OBJECT_ID,
		DataObject.CREATED_BY,
		DataObject.CREATION_DATE,
		DataObject.LAST_MODIFIED,
		DataObject.MODIFIED_BY,
		FolderChildObject.FOLDER_ID,
		CommonObject.PRIVATE_FLAG,
		CommonObject.CATEGORIES,
		ContactObject.GIVEN_NAME,
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
		ContactObject.DISTRIBUTIONLIST,
		ContactObject.EMAIL1,
		ContactObject.EMAIL2,
		ContactObject.EMAIL3,
		ContactObject.EMPLOYEE_TYPE,
		ContactObject.FAX_BUSINESS,
		ContactObject.FAX_HOME,
		ContactObject.FAX_OTHER,
		ContactObject.FILE_AS,
		ContactObject.FOLDER_ID,
		ContactObject.GIVEN_NAME,
		ContactObject.IMAGE1,
		ContactObject.IMAGE1_CONTENT_TYPE,
		ContactObject.INFO,
		ContactObject.INSTANT_MESSENGER1,
		ContactObject.INSTANT_MESSENGER2,
		ContactObject.LINKS,
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
		ContactObject.PRIVATE_FLAG,
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

	
	public boolean canExport(final SessionObject sessObj, final Format format, final String folder, final int type, Map <String, String[]> optionalParams)  throws ImportExportException {
		if(type != Types.CONTACT){
			System.out.println("DBG:TIER:: Type != contact: " + type);
			return false;
		}
		FolderObject fo;
		try {
			fo = getFolderObject(sessObj, folder);
		} catch (ImportExportException e) {
			System.out.println("DBG:TIER:: Could not get folder object");
			return false;
		}
		//check format of folder
		if ( fo.getModule() != type){
			System.out.println("DBG:TIER:: module has type = " + fo.getModule() + ", requested was " + type);
			return false;
		}
		//check read access to folder
		EffectivePermission perm;
		try {
			perm = fo.getEffectiveUserPermission(sessObj.getUserObject().getId(), sessObj.getUserConfiguration());
		} catch (DBPoolingException e) {
			System.out.println("DBG:TIER:: Could not get permission: DBPOOLING Exception");
			return false;
		} catch (SQLException e) {
			System.out.println("DBG:TIER:: Could not get permission: SQL Exception");
			return false;
		}
		return perm.canReadAllObjects();
	}

	
	public InputStream exportData(final SessionObject sessObj, final Format format, final String folder, final int type,
			final int[] fieldsToBeExported, Map <String, String[]> optionalParams) throws ImportExportException {
		if(! canExport(sessObj, format, folder, type, optionalParams)){
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
		SearchIterator conIter;
		try {
			conIter = contactSql.getContactsInFolder(folderId, 0, contactSql.getNumberOfContacts(folderId), 0, "ASC", cols);
		} catch (OXException e) {
			throw EXCEPTIONS.create(2, e);
		}
		StringBuilder ret = new StringBuilder();
		ret.append( convertToLine( convertToList(cols) ) );
		while(conIter.hasNext()){
			ContactObject current;
			try {
				current = (ContactObject) conIter.next();
				ret.append( convertToLine( convertToList(current, cols) ) );
			} catch (SearchIteratorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return new ByteArrayInputStream ( ret.toString().getBytes() );
	}

	
	public InputStream exportData(final SessionObject sessObj, final Format format, final String folder, 
			final int type, final int objectId,	final int[] fieldsToBeExported, Map <String, String[]> optionalParams) throws ImportExportException {
		if(! canExport(sessObj, format, folder, type, optionalParams)){
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
		} catch (OXException e) {
			throw EXCEPTIONS.create(2, e);
		}

		StringBuilder ret = new StringBuilder();
		ret.append( convertToLine( convertToList(cols) ) );
		ret.append( convertToLine( convertToList(conObj, cols) ) );
		
		return new ByteArrayInputStream ( ret.toString().getBytes() );
	}
	
	protected FolderObject getFolderObject(SessionObject sessObj, String folder) throws ImportExportException{
		final int folderId = getFolderId(folder);
		FolderObject fo = null;
		try {
			if(FolderCacheManager.isEnabled()){
				fo = FolderCacheManager.getInstance().getFolderObject(folderId, sessObj.getContext());
			} else {
				fo = FolderObject.loadFolderObjectFromDB(folderId, sessObj.getContext());
			}
		} catch (OXException e) {
			throw EXCEPTIONS.create(1, folder);
		}
		return fo;
	}
	
	protected int getFolderId(String folderString) throws ImportExportException{
		try{
			return new Integer(folderString).intValue();
		} catch (NumberFormatException e) {
			throw EXCEPTIONS.create(3, folderString);
		}
	}
	
	protected List<String> convertToList(ContactObject conObj, int[] cols){
		List<String> l = new LinkedList<String>();
		for(int col : cols){
			l.add( Contacts.mapping[col].toString() );
		}
		return l;
	}
	
	protected List<String> convertToList(int[] cols){
		List<String> l = new LinkedList<String>();
		for(int col : cols){
			l.add( Contacts.mapping[col].getReadableTitle() );
		}
		return l;
	}
	
	protected String convertToLine(List<String> line){
		StringBuilder bob = new StringBuilder();
		for(String str : line){
			bob.append("\"");
			str = str.replace("\"", "\"\"");
			bob.append(str);
			bob.append("\"");
			bob.append(CELL_DELIMITER);
		}
		bob.setCharAt(bob.length() - 1, ROW_DELIMITER);
		return bob.toString();
	}

}
