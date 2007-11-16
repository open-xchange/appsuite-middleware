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
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.Exporter;
import com.openexchange.groupware.importexport.Format;
import com.openexchange.groupware.importexport.SizedInputStream;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionClasses;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionFactory;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLInterfaceImpl;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.EffectivePermission;
import com.openexchange.sessiond.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.OXContainerConverter;

@OXExceptionSource(
		classId=ImportExportExceptionClasses.ICALEXPORTER,
		component=Component.IMPORT_EXPORT)
@OXThrowsMultiple(
		category={
	Category.PERMISSION,
	Category.SUBSYSTEM_OR_SERVICE_DOWN,
	Category.USER_INPUT,
	Category.CODE_ERROR,
	Category.CODE_ERROR},
		desc={"","","","",""},
		exceptionId={0,1,2,3,4},
		msg={
	"Could not import into the folder %s.",
	"Could not import into folder %s",
	"User input error %s",
	"Could not import into folder %s",
	"Could not load folder %s"})
	
/**
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a> (minor: changes to new interface; fixes)
 */
public class ICalExporter implements Exporter {
	
	private final static int[] _appointmentFields = {
		DataObject.OBJECT_ID,
		DataObject.CREATED_BY,
		DataObject.CREATION_DATE,
		DataObject.LAST_MODIFIED,
		DataObject.MODIFIED_BY,
		FolderChildObject.FOLDER_ID,
		CommonObject.PRIVATE_FLAG,
		CommonObject.CATEGORIES,
		CalendarObject.TITLE,
		AppointmentObject.LOCATION,
		CalendarObject.START_DATE,
		CalendarObject.END_DATE,
		CalendarObject.NOTE,
		CalendarObject.RECURRENCE_TYPE,
		CalendarObject.RECURRENCE_CALCULATOR,
		CalendarObject.RECURRENCE_ID,
		CalendarObject.PARTICIPANTS,
		CalendarObject.USERS,
		AppointmentObject.SHOWN_AS,
		AppointmentObject.FULL_TIME,
		AppointmentObject.COLOR_LABEL,
		CalendarDataObject.TIMEZONE
	};
	
	protected final static int[] _taskFields = {
		DataObject.OBJECT_ID,
		DataObject.CREATED_BY,
		DataObject.CREATION_DATE,
		DataObject.LAST_MODIFIED,
		DataObject.MODIFIED_BY,
		FolderChildObject.FOLDER_ID,
		CommonObject.PRIVATE_FLAG,
		CommonObject.CATEGORIES,
		CalendarObject.TITLE,
		CalendarObject.START_DATE,
		CalendarObject.END_DATE,
		CalendarObject.NOTE,
		CalendarObject.RECURRENCE_TYPE,
		CalendarObject.PARTICIPANTS,
		Task.ACTUAL_COSTS,
		Task.ACTUAL_DURATION,
		Task.ALARM,
		Task.BILLING_INFORMATION,
		Task.CATEGORIES,
		Task.COMPANIES,
		Task.CURRENCY,
		Task.DATE_COMPLETED,
		Task.IN_PROGRESS,
		Task.PERCENT_COMPLETED,
		Task.PRIORITY,
		Task.STATUS,
		Task.TARGET_COSTS,
		Task.TARGET_DURATION,
		Task.TRIP_METER,
		Task.COLOR_LABEL
	};
	
	
	private static final ImportExportExceptionFactory importExportExceptionFactory = new ImportExportExceptionFactory(ICalExporter.class);
	
	public boolean canExport(final Session sessObj, final Format format, final String folder, final Map<String, String[]> optionalParams) throws ImportExportException {
		if(! format.equals(Format.ICAL)){
			return false;
		}
		final int folderId = Integer.parseInt(folder);
		FolderObject fo;
		try {
			fo = FolderObject.loadFolderObjectFromDB(folderId, sessObj.getContext());
		} catch (final OXException e) {
			return false;
		}
		//check format of folder
		int module = fo.getModule(); 
		if (module == FolderObject.CALENDAR) {
			if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessObj.getUserId(), sessObj.getContext()).hasCalendar()) {
				return false;
			}
		} else if (module == FolderObject.TASK) {
			if (UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessObj.getUserId(), sessObj.getContext()).hasTask()) {
				return false;
			}
		} else {
			return false;
		}

		//check read access to folder
		EffectivePermission perm;
		try {
			perm = fo.getEffectiveUserPermission(sessObj.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessObj.getUserId(), sessObj.getContext()));
		} catch (final DBPoolingException e) {
			throw importExportExceptionFactory.create(2, folder);
		} catch (final SQLException e) {
			throw importExportExceptionFactory.create(2, folder);
		}
		
		if (perm.canReadAllObjects()) {
			return true;
		}
		
		return false;
	}
	
	public SizedInputStream exportData(final Session sessObj, final Format format, final String folder, int[] fieldsToBeExported, final Map<String, String[]> optionalParams) throws ImportExportException {
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			final VersitDefinition versitDefinition = Versit.getDefinition("text/calendar");
			final VersitDefinition.Writer versitWriter = versitDefinition.getWriter(byteArrayOutputStream, "UTF-8");
			final VersitObject versitObjectContainer = OXContainerConverter.newCalendar("2.0");
			versitDefinition.writeProperties(versitWriter, versitObjectContainer);
			final VersitDefinition eventDef = versitDefinition.getChildDef("VEVENT");
			final VersitDefinition taskDef = versitDefinition.getChildDef("VTODO");
			final OXContainerConverter oxContainerConverter = new OXContainerConverter(sessObj);
			
			FolderObject fo;
			try {
				fo = FolderObject.loadFolderObjectFromDB(Integer.parseInt(folder), sessObj.getContext());
			} catch (final OXException e) {
				throw importExportExceptionFactory.create(4, folder);
			}
			if (fo.getModule() == FolderObject.CALENDAR) {
				if (fieldsToBeExported == null) {
					fieldsToBeExported = _appointmentFields;
				}
				
				final AppointmentSQLInterface appointmentSql = new CalendarSql(sessObj);
				final SearchIterator searchIterator = appointmentSql.getModifiedAppointmentsInFolder(Integer.parseInt(folder), fieldsToBeExported, new Date(0));
				
				while (searchIterator.hasNext()) {
					exportAppointment(oxContainerConverter, eventDef, versitWriter, (AppointmentObject)searchIterator.next());
				}
			} else if (fo.getModule() == FolderObject.TASK) {
				if (fieldsToBeExported == null) {
					fieldsToBeExported = _taskFields;
				}
				
				final TasksSQLInterface taskSql = new TasksSQLInterfaceImpl(sessObj);
				final SearchIterator searchIterator = taskSql.getModifiedTasksInFolder(Integer.parseInt(folder), fieldsToBeExported, new Date(0));
				
				while (searchIterator.hasNext()) {
					exportTask(oxContainerConverter, taskDef, versitWriter, (Task)searchIterator.next());
				}
			} else {
				throw importExportExceptionFactory.create(3, Integer.valueOf(fo.getModule()));
			}
		} catch (final Exception exc) {
			throw importExportExceptionFactory.create(3, exc, folder);
		}
		
		return new SizedInputStream(
				new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), 
				byteArrayOutputStream.size(),
				Format.ICAL);
	}
	
	public SizedInputStream exportData(final Session sessObj, final Format format, final String folder, final int objectId, final int[] fieldsToBeExported, final Map<String, String[]> optionalParams) throws ImportExportException {
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			final VersitDefinition versitDefinition = Versit.getDefinition("text/calendar");
			final VersitDefinition.Writer versitWriter = versitDefinition.getWriter(byteArrayOutputStream, "UTF-8");
			final VersitObject versitObjectContainer = OXContainerConverter.newCalendar("2.0");
			versitDefinition.writeProperties(versitWriter, versitObjectContainer);
			final VersitDefinition eventDef = versitDefinition.getChildDef("VEVENT");
			final VersitDefinition taskDef = versitDefinition.getChildDef("VTODO");
			final OXContainerConverter oxContainerConverter = new OXContainerConverter(sessObj);
			
			FolderObject fo;
			try {
				fo = FolderObject.loadFolderObjectFromDB(Integer.parseInt(folder), sessObj.getContext());
			} catch (final OXException e) {
				throw importExportExceptionFactory.create(4, folder);
			}
			if (fo.getModule() == FolderObject.CALENDAR) {
				final AppointmentSQLInterface appointmentSql = new CalendarSql(sessObj);
				final AppointmentObject appointmentObj = appointmentSql.getObjectById(objectId, Integer.parseInt(folder));
				
				exportAppointment(oxContainerConverter, eventDef, versitWriter, appointmentObj);
			} else if (fo.getModule() == FolderObject.TASK) {
				final TasksSQLInterface taskSql = new TasksSQLInterfaceImpl(sessObj);
				final Task taskObj = taskSql.getTaskById(objectId, Integer.parseInt(folder));
				
				exportTask(oxContainerConverter, taskDef, versitWriter, taskObj);
			} else {
				throw importExportExceptionFactory.create(3, Integer.valueOf(fo.getModule()));
			}
			
			versitWriter.flush();
		} catch (final Exception exc) {
			throw importExportExceptionFactory.create(4, folder);
		}
		
		return new SizedInputStream(
				new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), 
				byteArrayOutputStream.size(),
				Format.ICAL);
	}
	
	protected void exportAppointment(final OXContainerConverter oxContainerConverter, final VersitDefinition versitDef, final VersitDefinition.Writer writer, final AppointmentObject appointmentObj) throws Exception {
		final VersitObject versitObject = oxContainerConverter.convertAppointment(appointmentObj);
		versitDef.write(writer, versitObject);
		writer.flush();
	}
	
	protected void exportTask(final OXContainerConverter oxContainerConverter, final VersitDefinition versitDef, final VersitDefinition.Writer writer, final Task taskObj) throws Exception {
		final VersitObject versitObject = oxContainerConverter.convertTask(taskObj);
		versitDef.write(writer, versitObject);
		writer.flush();
	}
}
