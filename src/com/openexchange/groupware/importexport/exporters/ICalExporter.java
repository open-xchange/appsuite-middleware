package com.openexchange.groupware.importexport.exporters;

import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.groupware.tasks.TasksSQLInterfaceImpl;
import java.io.InputStream;
import java.sql.SQLException;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.Exporter;
import com.openexchange.groupware.importexport.Format;
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
	
	private static ImportExportExceptionFactory importExportExceptionFactory = new ImportExportExceptionFactory(ICalExporter.class);
	
	public boolean canExport(final SessionObject sessObj, final Format format, final String folder, final int type, final Map<String, String[]> optionalParams) throws ImportExportException {
		int folderId = new Integer(folder).intValue();
		FolderObject fo;
		try {
			fo = FolderObject.loadFolderObjectFromDB(folderId, sessObj.getContext());
		} catch (OXException e) {
			return false;
		}
		//check format of folder
		if ( (type == Types.APPOINTMENT || type == Types.TASK) && fo.getModule() != type ){
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
		return perm.canReadAllObjects();
	}
	
	public InputStream exportData(SessionObject sessObj, Format format, String folder, int type, int[] fieldsToBeExported, Map<String, String[]> optionalParams) throws ImportExportException {
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
				final SearchIterator searchIterator = appointmentSql.getModifiedAppointmentsInFolder(Integer.parseInt(folder), fieldsToBeExported, new Date(0));
				
				while (searchIterator.hasNext()) {
					exportAppointment(oxContainerConverter, eventDef, versitWriter, (AppointmentObject)searchIterator.next());
				}
			} else if (type == Types.TASK) {
				final TasksSQLInterface taskSql = new TasksSQLInterfaceImpl(sessObj);
				final SearchIterator searchIterator = taskSql.getModifiedTasksInFolder(Integer.parseInt(folder), fieldsToBeExported, new Date(0));
				
				while (searchIterator.hasNext()) {
					exportTask(oxContainerConverter, taskDef, versitWriter, (Task)searchIterator.next());
				}
			} else {
				throw importExportExceptionFactory.create(3, type);
			}
		} catch (Exception exc) {
			throw importExportExceptionFactory.create(4, folder);
		}
		
		return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
	}
	
	public InputStream exportData(SessionObject sessObj, Format format, String folder, int type, int objectId, int[] fieldsToBeExported, Map<String, String[]> optionalParams) throws ImportExportException {
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
		
		return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
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
