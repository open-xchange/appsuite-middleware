package com.openexchange.groupware.importexport.importers;

import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.Format;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.importexport.Importer;
import com.openexchange.groupware.importexport.ModuleTypeTranslator;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionClasses;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionFactory;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLInterfaceImpl;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.EffectivePermission;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.OXContainerConverter;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@OXExceptionSource(
	classId=ImportExportExceptionClasses.ICALIMPORTER, 
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
		"Subsystem down",
		"User input Error %s",
		"Programming Error"})

public class ICalImporter implements Importer {
	
	private static final Log LOG = LogFactory.getLog(ICalImporter.class);
	
	private static ImportExportExceptionFactory importExportExceptionFactory = new ImportExportExceptionFactory(ICalImporter.class);
	
	public boolean canImport(final SessionObject sessObj, final Format format, final Map<String, Integer> folderMappings, final Map<String, String[]> optionalParams) throws ImportExportException{
		final Iterator iterator = folderMappings.keySet().iterator();
		while (iterator.hasNext()) {
			String folder = iterator.next().toString();
			
			int folderId = new Integer(folder).intValue();
			FolderObject fo;
			try {
				fo = FolderObject.loadFolderObjectFromDB(folderId, sessObj.getContext());
			} catch (OXException e) {
				return false;
			}
			
			int type = folderMappings.get(folder).intValue();
			//check format of folder
			if ( (type == Types.APPOINTMENT || type == Types.TASK) && fo.getModule() != ModuleTypeTranslator.getFolderObjectConstant(type)) {
				return false;
			}
			//check read access to folder
			EffectivePermission perm;
			try {
				perm = fo.getEffectiveUserPermission(sessObj.getUserObject().getId(), sessObj.getUserConfiguration());
			} catch (DBPoolingException e) {
				throw importExportExceptionFactory.create(1, folder);
			} catch (SQLException e) {
				throw importExportExceptionFactory.create(1, folder);
			}
			
			if (perm.canWriteAllObjects()) {
				if (format.getMimeType().equals("text/calendar")) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public List<ImportResult> importData(final SessionObject sessObj, final Format format, final InputStream is, final Map<String, Integer> folderMappings, final Map<String, String[]> optionalParams) throws ImportExportException {
		int appointmentFolderId = -1;
		int taskFolderId = -1;
		
		boolean importAppointment = false;
		boolean importTask = false;
		
		final Iterator iterator = folderMappings.keySet().iterator();
		while (iterator.hasNext()) {
			String folder = iterator.next().toString();
			int type = folderMappings.get(folder).intValue();
			
			if (type == Types.APPOINTMENT) {
				appointmentFolderId = Integer.parseInt(folder);
				importAppointment = true;
			} else if (type == Types.TASK) {
				taskFolderId = Integer.parseInt(folder);
				importTask = true;
			} else {
				throw importExportExceptionFactory.create(0, type); 
			}
		}
		
		AppointmentSQLInterface appointmentInterface = null;
		
		if (importAppointment) {
			appointmentInterface = new CalendarSql(sessObj);
		}
		
		TasksSQLInterface taskInterface = null;
		
		if (importTask) {
			taskInterface = new TasksSQLInterfaceImpl(sessObj);
		}
		
		final OXContainerConverter oxContainerConverter = new OXContainerConverter(sessObj);
		
		List<ImportResult> list = new ArrayList<ImportResult>();
		
		try {
			final VersitDefinition def = Versit.getDefinition(format.getMimeType());
			final VersitDefinition.Reader versitReader = def.getReader(is, "UTF-8");
			final VersitObject rootVersitObject = def.parseBegin(versitReader);
			VersitObject versitObject = def.parseChild(versitReader, rootVersitObject);
			while (versitObject != null) {
				ImportResult importResult = new ImportResult();
				try {
					final Property property = versitObject.getProperty("UID");
					
					if ("VEVENT".equals(versitObject.name) && importAppointment) {
						importResult.setFolder(String.valueOf(appointmentFolderId));
								
						final CalendarDataObject appointmentObj = oxContainerConverter.convertAppointment(versitObject);
						appointmentObj.setContext(sessObj.getContext());
						appointmentObj.setParentFolderID(appointmentFolderId);
						appointmentInterface.insertAppointmentObject(appointmentObj);
						
						importResult.setObjectId(String.valueOf(appointmentObj.getObjectID()));
						importResult.setDate(appointmentObj.getLastModified());
					} else if ("VTODO".equals(versitObject.name) && importTask) {
						importResult.setFolder(String.valueOf(taskFolderId));
						
						final Task taskObj = oxContainerConverter.convertTask(versitObject);
						taskObj.setParentFolderID(taskFolderId);
						taskInterface.insertTaskObject(taskObj);
						
						importResult.setObjectId(String.valueOf(taskObj.getObjectID()));
						importResult.setDate(taskObj.getLastModified());
					} else {
						LOG.warn("invalid versit object: " + versitObject.name);
					}
				} catch (OXException exc) {
					LOG.debug("cannot import calendar object", exc);
					importResult.setException(exc);
				}
				
				list.add(importResult);
				
				versitObject = def.parseChild(versitReader, rootVersitObject);
			}
		} catch (Exception exc) {
			throw importExportExceptionFactory.create(4, exc);
		} finally {
			oxContainerConverter.close();
		}
		
		return list;
	}
}
