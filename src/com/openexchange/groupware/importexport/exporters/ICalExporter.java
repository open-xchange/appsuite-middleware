package com.openexchange.groupware.importexport.exporters;

import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.groupware.importexport.SizedInputStream;
import com.openexchange.groupware.tasks.TasksSQLInterfaceImpl;
import java.sql.SQLException;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.Types;
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
import com.openexchange.groupware.importexport.ModuleTypeTranslator;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionClasses;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionFactory;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.EffectivePermission;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.OXContainerConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Map;

@OXExceptionSource(
		classId=ImportExportExceptionClasses.ICALEXPORTER,
		component=Component.IMPORT_EXPORT)
@OXThrowsMultiple(
		category={
	Category.PERMISSION,
	Category.SUBSYSTEM_OR_SERVICE_DOWN,
	Category.USER_INPUT,
	Category.PROGRAMMING_ERROR},
		desc={"","","",""},
		exceptionId={0,1,2,3},
		msg={
	"Could not import into the folder %s.",
	"Subsystem down - Could not import into folder %s",
	"User input Error %s",
	"Programming Error - Could not import into folder %s"})
	
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
	
	
	private static ImportExportExceptionFactory importExportExceptionFactory = new ImportExportExceptionFactory(ICalExporter.class);
	
	public boolean canExport(final SessionObject sessObj, final Format format, final String folder, final int type, final Map<String, String[]> optionalParams) throws ImportExportException {
		int folderId = Integer.parseInt(folder);
		FolderObject fo;
		try {
			fo = FolderObject.loadFolderObjectFromDB(folderId, sessObj.getContext());
		} catch (OXException e) {
			return false;
		}
		//check format of folder
		if ((type == Types.APPOINTMENT || type == Types.TASK) && fo.getModule() != ModuleTypeTranslator.getFolderObjectConstant(type)) {
			return false;
		}
		//check read access to folder
		EffectivePermission perm;
		try {
			perm = fo.getEffectiveUserPermission(sessObj.getUserObject().getId(), sessObj.getUserConfiguration());
		} catch (DBPoolingException e) {
			throw importExportExceptionFactory.create(2, folder);
		} catch (SQLException e) {
			throw importExportExceptionFactory.create(2, folder);
		}
		
		if (perm.canReadAllObjects()) {
			if (format.getMimeType().equals("text/calendar")) {
				return true;
			}
		}
		
		return false;
	}
	
	public SizedInputStream exportData(SessionObject sessObj, Format format, String folder, int type, int[] fieldsToBeExported, Map<String, String[]> optionalParams) throws ImportExportException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			final VersitDefinition versitDefinition = Versit.getDefinition("text/calendar");
			final VersitDefinition.Writer versitWriter = versitDefinition.getWriter(byteArrayOutputStream, "UTF-8");
			final VersitObject versitObjectContainer = OXContainerConverter.newCalendar("2.0");
			versitDefinition.writeProperties(versitWriter, versitObjectContainer);
			final VersitDefinition eventDef = versitDefinition.getChildDef("VEVENT");
			final VersitDefinition taskDef = versitDefinition.getChildDef("VTODO");
			final OXContainerConverter oxContainerConverter = new OXContainerConverter(sessObj);
			
			if (type == Types.APPOINTMENT) {
				if (fieldsToBeExported == null) {
					fieldsToBeExported = _appointmentFields;
				}
				
				final AppointmentSQLInterface appointmentSql = new CalendarSql(sessObj);
				final SearchIterator searchIterator = appointmentSql.getModifiedAppointmentsInFolder(Integer.parseInt(folder), fieldsToBeExported, new Date(0));
				
				while (searchIterator.hasNext()) {
					exportAppointment(oxContainerConverter, eventDef, versitWriter, (AppointmentObject)searchIterator.next());
				}
			} else if (type == Types.TASK) {
				if (fieldsToBeExported == null) {
					fieldsToBeExported = _taskFields;
				}
				
				final TasksSQLInterface taskSql = new TasksSQLInterfaceImpl(sessObj);
				final SearchIterator searchIterator = taskSql.getModifiedTasksInFolder(Integer.parseInt(folder), fieldsToBeExported, new Date(0));
				
				while (searchIterator.hasNext()) {
					exportTask(oxContainerConverter, taskDef, versitWriter, (Task)searchIterator.next());
				}
			} else {
				throw importExportExceptionFactory.create(3, type);
			}
		} catch (Exception exc) {
			throw importExportExceptionFactory.create(3, exc, folder);
		}
		
		return new SizedInputStream(
				new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), 
				byteArrayOutputStream.size(),
				Format.ICAL);
	}
	
	public SizedInputStream exportData(SessionObject sessObj, Format format, String folder, int type, int objectId, int[] fieldsToBeExported, Map<String, String[]> optionalParams) throws ImportExportException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			final VersitDefinition versitDefinition = Versit.getDefinition("text/calendar");
			VersitDefinition.Writer versitWriter = versitDefinition.getWriter(byteArrayOutputStream, "UTF-8");
			final VersitObject versitObjectContainer = OXContainerConverter.newCalendar("2.0");
			versitDefinition.writeProperties(versitWriter, versitObjectContainer);
			final VersitDefinition eventDef = versitDefinition.getChildDef("VEVENT");
			final VersitDefinition taskDef = versitDefinition.getChildDef("VTODO");
			final OXContainerConverter oxContainerConverter = new OXContainerConverter(sessObj);
			
			if (type == Types.APPOINTMENT) {
				final AppointmentSQLInterface appointmentSql = new CalendarSql(sessObj);
				final AppointmentObject appointmentObj = appointmentSql.getObjectById(objectId, Integer.parseInt(folder));
				
				exportAppointment(oxContainerConverter, eventDef, versitWriter, appointmentObj);
			} else if (type == Types.TASK) {
				final TasksSQLInterface taskSql = new TasksSQLInterfaceImpl(sessObj);
				final Task taskObj = taskSql.getTaskById(objectId, Integer.parseInt(folder));
				
				exportTask(oxContainerConverter, taskDef, versitWriter, taskObj);
			} else {
				throw importExportExceptionFactory.create(3, type);
			}
		} catch (Exception exc) {
			throw importExportExceptionFactory.create(4, folder);
		}
		
		return new SizedInputStream(
				new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), 
				byteArrayOutputStream.size(),
				Format.ICAL);
	}
	
	protected void exportAppointment(OXContainerConverter oxContainerConverter, VersitDefinition versitDef, VersitDefinition.Writer writer, AppointmentObject appointmentObj) throws Exception {
		VersitObject versitObject = oxContainerConverter.convertAppointment(appointmentObj);
		versitDef.write(writer, versitObject);
	}
	
	protected void exportTask(OXContainerConverter oxContainerConverter, VersitDefinition versitDef, VersitDefinition.Writer writer, Task taskObj) throws Exception {
		VersitObject versitObject = oxContainerConverter.convertTask(taskObj);
		versitDef.write(writer, versitObject);
	}
}
