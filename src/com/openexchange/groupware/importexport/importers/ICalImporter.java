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

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.AbstractImporter;
import com.openexchange.groupware.importexport.Format;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.importexport.Importer;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionClasses;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionFactory;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLInterfaceImpl;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.EffectivePermission;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.versit.ICalendar;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterPrivacyException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

@OXExceptionSource(
	classId=ImportExportExceptionClasses.ICALIMPORTER, 
	component=Component.IMPORT_EXPORT)
@OXThrowsMultiple(
	category={
		Category.PERMISSION, 
		Category.SUBSYSTEM_OR_SERVICE_DOWN,
		Category.USER_INPUT,
		Category.CODE_ERROR,
		Category.CODE_ERROR,
		Category.USER_INPUT,
		Category.USER_INPUT,
		Category.PERMISSION,
		Category.PERMISSION}, 
	desc={"","","","","","","","",""}, 
	exceptionId={0,1,2,3,4,5,6,7,8}, 
	msg={
		"Could not import into the folder %s.",
		"Subsystem down",
		"User input error %s",
		"Programming error",
		"Could not load folder %s",
		"Broken file uploaded: %s",
		"Cowardly refusing to import an entry flagged as confidential.",
		"Module Calendar not enabled for user, cannot import appointments",
		"Module Tasks not enabled for user, cannot import tasks"})

/**
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a> (minor: changes to new interface, bugfixes)
 */
public class ICalImporter extends AbstractImporter implements Importer {
	
	private static final Log LOG = LogFactory.getLog(ICalImporter.class);
	
	private static final ImportExportExceptionFactory EXCEPTIONS = new ImportExportExceptionFactory(ICalImporter.class);
	
	public boolean canImport(final SessionObject sessObj, final Format format, final List<String> folders, final Map<String, String[]> optionalParams) throws ImportExportException{
		if(!format.equals(Format.ICAL)){
			return false;
		}
		final Iterator iterator = folders.iterator();
		while (iterator.hasNext()) {
			final String folder = iterator.next().toString();
			
			int folderId = 0;
			try {
				folderId = Integer.parseInt(folder);
			} catch (NumberFormatException exc) {
				throw EXCEPTIONS.create(0, folder);
			}

			FolderObject fo;
			try {
				fo = FolderObject.loadFolderObjectFromDB(folderId, sessObj.getContext());
			} catch (OXException e) {
				return false;
			}
			
			//check format of folder
			int module = fo.getModule(); 
			if (module == FolderObject.CALENDAR) {
				if (!sessObj.getUserConfiguration().hasCalendar()) {
					return false;
				}
			} else if (module == FolderObject.TASK) {
				if (!sessObj.getUserConfiguration().hasTask()) {
					return false;
				}
			} else {
				return false;
			}

			//check read access to folder
			EffectivePermission perm;
			try {
				perm = fo.getEffectiveUserPermission(sessObj.getUserObject().getId(), sessObj.getUserConfiguration());
			} catch (DBPoolingException e) {
				throw EXCEPTIONS.create(0, folder);
			} catch (SQLException e) {
				throw EXCEPTIONS.create(0, folder);
			}
			
			if (perm.canCreateObjects()) {
				return true;
			}
		}
		
		return false;
	}
	
	public List<ImportResult> importData(final SessionObject sessObj, final Format format, final InputStream is, final List<String> folders, final Map<String, String[]> optionalParams) throws ImportExportException {
		int appointmentFolderId = -1;
		int taskFolderId = -1;
		
		boolean importAppointment = false;
		boolean importTask = false;
		
		final Iterator<String> iterator = folders.iterator();
		while (iterator.hasNext()) {
			final int folderId = Integer.parseInt( iterator.next() );
			FolderObject fo;
			try {
				fo = FolderObject.loadFolderObjectFromDB(folderId, sessObj.getContext());
			} catch (OXException e) {
				throw EXCEPTIONS.create(4,folderId);
			}
			if (fo.getModule() == FolderObject.CALENDAR) {
				appointmentFolderId = folderId;
				importAppointment = true;
			} else if (fo.getModule() == FolderObject.TASK) {
				taskFolderId = folderId;
				importTask = true;
			} else {
				throw EXCEPTIONS.create(0, fo.getModule()); 
			}
		}
		
		AppointmentSQLInterface appointmentInterface = null;
		
		if (importAppointment) {
			if(! sessObj.getUserConfiguration().hasCalendar() ){
				throw EXCEPTIONS.create(7, new OXPermissionException(OXPermissionException.Code.NoPermissionForModul, "Calendar") );
			}
			appointmentInterface = new CalendarSql(sessObj);
		}
		
		TasksSQLInterface taskInterface = null;
		
		if (importTask) {
			if(! sessObj.getUserConfiguration().hasTask() ){
				throw EXCEPTIONS.create(8, new OXPermissionException(OXPermissionException.Code.NoPermissionForModul, "Task") );
			}
			taskInterface = new TasksSQLInterfaceImpl(sessObj);
		}
		
		final OXContainerConverter oxContainerConverter = new OXContainerConverter(sessObj);
		
		List<ImportResult> list = new ArrayList<ImportResult>();
		
		try {
			final VersitDefinition def = ICalendar.definition;
			final VersitDefinition.Reader versitReader = def.getReader(is, "UTF-8");
			VersitObject rootVersitObject = def.parseBegin(versitReader);
			
			boolean hasMoreObjects = true;
			
			while (hasMoreObjects) {
				ImportResult importResult = new ImportResult();
				try {
					VersitObject versitObject = null;
					try {
						versitObject=  def.parseChild(versitReader, rootVersitObject);
					} catch (VersitException ve){
						LOG.info("Trying to import ICAL file, but:\n" + ve);
						importResult.setException(EXCEPTIONS.create(5, ve.getLocalizedMessage()));
						importResult.setDate(new Date(System.currentTimeMillis()));
						list.add(importResult);
						hasMoreObjects = false;
						break;
					}
					
					if (versitObject == null) {
						hasMoreObjects = false;
						break;
					}

					//final Property property = versitObject.getProperty("UID");
					
					if ("VEVENT".equals(versitObject.name) && importAppointment) {
						importResult.setFolder(String.valueOf(appointmentFolderId));
						boolean storeData = true;
						CalendarDataObject appointmentObj = null;
						try {
							appointmentObj = oxContainerConverter.convertAppointment(versitObject);
						} catch (ConverterPrivacyException e){
							storeData = false;
						}
						if(storeData){
							appointmentObj.setContext(sessObj.getContext());
							appointmentObj.setParentFolderID(appointmentFolderId);
							appointmentObj.setIgnoreConflicts(true);
							appointmentInterface.insertAppointmentObject(appointmentObj);
							importResult.setObjectId(String.valueOf(appointmentObj.getObjectID()));
							importResult.setDate(appointmentObj.getLastModified());
						} else {
							importResult.setException(EXCEPTIONS.create(6));
							importResult.setDate(new Date());
						}
						
					} else if ("VTODO".equals(versitObject.name) && importTask) {
						importResult.setFolder(String.valueOf(taskFolderId));
						boolean storeData = true;
						
						Task taskObj = null;
						try {
							taskObj = oxContainerConverter.convertTask(versitObject);
						} catch (ConverterPrivacyException e){
							storeData = false;
						}
						if(storeData){
							taskObj.setParentFolderID(taskFolderId);
							taskInterface.insertTaskObject(taskObj);
							importResult.setObjectId(String.valueOf(taskObj.getObjectID()));
							importResult.setDate(taskObj.getLastModified());
						} else {
							importResult.setException(EXCEPTIONS.create(6));
							importResult.setDate(new Date());
						}
						
					} else {
						LOG.warn("invalid versit object: " + versitObject.name);
					}
				} catch (OXException exc) {
					LOG.error("cannot import calendar object", exc);
					exc = handleDataTruncation(exc); 
					importResult.setException(exc);
				} catch (VersitException exc) {
					LOG.error("cannot parse calendar object", exc);
					importResult.setException(new OXException("cannot parse ical object", exc));
				}
				
				list.add(importResult);
			}
		} catch (Exception exc) {
			throw EXCEPTIONS.create(3, exc);
		} finally {
			oxContainerConverter.close();
		}
		
		return list;
	}

}
