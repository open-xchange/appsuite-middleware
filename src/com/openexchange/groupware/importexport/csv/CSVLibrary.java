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

package com.openexchange.groupware.importexport.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.openexchange.api2.OXException;
import com.openexchange.cache.FolderCacheManager;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionClasses;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionFactory;
import com.openexchange.sessiond.SessionObject;
/**
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
@OXExceptionSource(
	classId=ImportExportExceptionClasses.CSVLIBRARY, 
	component=Component.IMPORT_EXPORT)
@OXThrowsMultiple(
	category={
		Category.SUBSYSTEM_OR_SERVICE_DOWN,
		Category.CODE_ERROR,
		Category.CODE_ERROR,
		Category.CODE_ERROR}, 
	desc={"","","", ""}, 
	exceptionId={0,1,2,3}, 
	msg={
		"Could not load folder %s",
		"Could not create folder id from string %s",
		"Could not read InputStream as string",
		"Missing ability to encode or decode UTF-8 on server, cannot read file."})
public class CSVLibrary {
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(CSVLibrary.class);
	
	protected static final ImportExportExceptionFactory EXCEPTIONS = new ImportExportExceptionFactory(CSVLibrary.class);
	public static final char CELL_DELIMITER = ',';
	public static final char ROW_DELIMITER = '\n';

	public static FolderObject getFolderObject(final SessionObject sessObj, final String folder) throws ImportExportException {
		final int folderId = getFolderId(folder);
		FolderObject fo = null;
		try {
			if(FolderCacheManager.isEnabled()){
				fo = FolderCacheManager.getInstance().getFolderObject(folderId, true, sessObj.getContext(), null);
			} else {
				fo = FolderObject.loadFolderObjectFromDB(folderId, sessObj.getContext());
			}
		} catch (OXException e) {
			throw EXCEPTIONS.create(0, folder);
		}
		return fo;
	}

	public static int getFolderId(final String folderString) throws ImportExportException {
		try{
			return Integer.parseInt(folderString);
		} catch (NumberFormatException e) {
			throw EXCEPTIONS.create(1, folderString);
		}
	}

	public static List<String> convertToList(final int[] cols) {
		final List<String> l = new LinkedList<String>();
		for(int col : cols){
			l.add( Contacts.mapping[col].getReadableTitle() );
		}
		return l;
	}

	/**
	 * ...because Java5, basic data types and Arrays don't mix
	 */
	public static Set<Integer> transformIntArrayToSet(final int[] arr) {
		final LinkedHashSet<Integer> s = new LinkedHashSet<Integer>();
		for(int val : arr){
			s.add(Integer.valueOf(val));
		}
		return s;
	}

	/**
	 * ...because Java5, basic data types and Arrays don't mix
	 */
	public static int[] transformSetToIntArray(final Set<Integer> s) {
		int[] ret = new int[s.size()];
		int i = 0;
		for(Integer val : s){
			ret[i++] = val.intValue();
		}
		return ret;
	}
	
	public static String transformInputStreamToString(final InputStream is, final String encoding) throws ImportExportException{
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is, encoding));
		} catch (UnsupportedEncodingException e1) {
			LOG.fatal(e1);
			throw EXCEPTIONS.create(3);
		}
		final StringBuilder bob = new StringBuilder();
		String buffer;
		try {
			while( (buffer = br.readLine()) != null){
				bob.append(buffer);
				bob.append('\n');
			}
		} catch (IOException e) {
			throw EXCEPTIONS.create(2);
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				/* 
				 * already closed
				 */
				LOG.error(e.getMessage(), e);
			}
		}
		return bob.toString();
	}

}