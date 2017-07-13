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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.importexport.exporters;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.ical.CalendarExport;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.SortOrder;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.helpers.ExportFileNameCreator;
import com.openexchange.importexport.helpers.SizedInputStream;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.java.Streams;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;

/**
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a> (minor: changes to new interface; fixes)
 */
public class ICalExporter implements Exporter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ICalExporter.class);

    private static final Date DATE_ZERO = new Date(0);
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
        Appointment.LOCATION,
        CalendarObject.START_DATE,
        CalendarObject.END_DATE,
        CalendarObject.NOTE,
        CalendarObject.RECURRENCE_TYPE,
        CalendarObject.RECURRENCE_CALCULATOR,
        CalendarObject.RECURRENCE_ID,
        CalendarObject.PARTICIPANTS,
        CalendarObject.USERS,
        Appointment.SHOWN_AS,
        Appointment.FULL_TIME,
        Appointment.COLOR_LABEL,
        Appointment.TIMEZONE,
        Appointment.UID,
        Appointment.SEQUENCE,
        Appointment.ORGANIZER
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
        CalendarObject.INTERVAL,
        CalendarObject.DAY_IN_MONTH,
        CalendarObject.DAYS,
        CalendarObject.MONTH,
        CalendarObject.PARTICIPANTS,
        Task.UID,
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

    /**
     * The event fields being exported by default.
     */
    private final static EventField[] EVENT_FIELDS;
    static {
        EnumSet<EventField> fields = EnumSet.allOf(EventField.class);
        fields.remove(EventField.ATTACHMENTS);
        fields.remove(EventField.ALARMS);
        EVENT_FIELDS = fields.toArray(new EventField[fields.size()]);
    }

    @Override
    public boolean canExport(final ServerSession sessObj, final Format format, final String folder, final Map<String, Object> optionalParams) throws OXException {
        if(! format.equals(Format.ICAL)){
            return false;
        }
        final int folderId = Integer.parseInt(folder);
        FolderObject fo;
        try {
            fo = new OXFolderAccess(sessObj.getContext()).getFolderObject(folderId);
        } catch (final OXException e) {
            return false;
        }
        //check format of folder
        final int module = fo.getModule();
        if (module == FolderObject.CALENDAR) {
            if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessObj.getUserId(), sessObj.getContext()).hasCalendar()) {
                return false;
            }
        } else if (module == FolderObject.TASK) {
            if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessObj.getUserId(), sessObj.getContext()).hasTask()) {
                return false;
            }
        } else {
            return false;
        }

        //check read access to folder
        EffectivePermission perm;
        try {
            perm = fo.getEffectiveUserPermission(sessObj.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessObj.getUserId(), sessObj.getContext()));
        } catch (final OXException e) {
            throw ImportExportExceptionCodes.NO_DATABASE_CONNECTION.create(e);
        } catch (final RuntimeException e) {
            throw ImportExportExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        }
        return perm.canReadAllObjects();
    }

    @Override
    public SizedInputStream exportFolderData(ServerSession session, Format format, String folderID, int[] fieldsToBeExported, Map<String, Object> optionalParams) throws OXException {
        return doExportData(session, format, folderID, null, fieldsToBeExported, optionalParams);
    }

    @Override
    public SizedInputStream exportSingleData(ServerSession session, Format format, String folderID, String objectID, int[] fieldsToBeExported, Map<String, Object> optionalParams) throws OXException {
        return doExportData(session, format, folderID, objectID, fieldsToBeExported, optionalParams);
    }
    
    @Override
    public SizedInputStream exportBatchData(ServerSession session, Format format, Map<String, List<String>> batchIds, int[] fieldsToBeExported, Map<String, Object> optionalParams) throws OXException {
        return doExportBatchData(session, format, batchIds, fieldsToBeExported, optionalParams);
    }    
    
    private SizedInputStream doExportBatchData(ServerSession session, Format format, Map<String, List<String>> batchIds, int[] fieldsToBeExported, Map<String, Object> optionalParams) throws OXException {
        AJAXRequestData requestData = (AJAXRequestData) (optionalParams == null ? null : optionalParams.get("__requestData"));
        ICalEmitter emitter = ImportExportServices.getICalEmitter();
        List<ConversionError> errors = new LinkedList<ConversionError>();
        List<ConversionWarning> warnings = new LinkedList<ConversionWarning>();
        ICalSession iCalSession = emitter.createSession();
        if (null != requestData) {
            try {
                OutputStream out = requestData.optOutputStream();
                if (null != out) {
                    requestData.setResponseHeader("Content-Type", isSaveToDisk(optionalParams) ? "application/octet-stream" : Format.ICAL.getMimeType() + "; charset=UTF-8");
                    requestData.setResponseHeader("Content-Disposition", "attachment; filename="+getBatchExportFileName(session, batchIds) + Format.ICAL.getExtension());
                    requestData.removeCachingHeader();                    
                    for (Map.Entry<String, List<String>> batchEntry : batchIds.entrySet()) {
                        FolderObject folder = getFolder(session, batchEntry.getKey());
                        if (FolderObject.CALENDAR == folder.getModule()) {
                            try {
                                exportBatchAppointments(session, batchEntry.getKey(), batchEntry.getValue(), fieldsToBeExported, emitter, errors, warnings, iCalSession);
                            } catch (NumberFormatException e) {
                                throw ImportExportExceptionCodes.CANNOT_EXPORT.create(batchEntry.getKey(), format);
                            } catch (SQLException e) {
                                throw ImportExportExceptionCodes.CANNOT_EXPORT.create(batchEntry.getKey(), format);
                            }
                        } else if (FolderObject.TASK == folder.getModule()) {
                            exportBatchTasks(session, batchEntry.getKey(), batchEntry.getValue(), fieldsToBeExported, emitter, errors, warnings, iCalSession);
                        } else {
                            throw ImportExportExceptionCodes.CANNOT_EXPORT.create(batchEntry.getKey(), format);
                        }
                    }                    
                    log(errors, warnings);                    
                    emitter.writeSession(iCalSession, out);
                    return null;
                }
            } catch (IOException e) {
                throw ImportExportExceptionCodes.ICAL_CONVERSION_FAILED.create(e);
            }
        }   
        
        ThresholdFileHolder sink = new ThresholdFileHolder();
        for (Map.Entry<String, List<String>> batchEntry : batchIds.entrySet()) {
            FolderObject folder = getFolder(session, batchEntry.getKey());
            if (FolderObject.CALENDAR == folder.getModule()) {
                try {
                    exportBatchAppointments(session, batchEntry.getKey(), batchEntry.getValue(), fieldsToBeExported, emitter, errors, warnings, iCalSession);
                } catch (NumberFormatException e) {
                    throw ImportExportExceptionCodes.CANNOT_EXPORT.create(batchEntry.getKey(), format);
                } catch (SQLException e) {
                    throw ImportExportExceptionCodes.CANNOT_EXPORT.create(batchEntry.getKey(), format);
                }
            } else if (FolderObject.TASK == folder.getModule()) {
                exportBatchTasks(session, batchEntry.getKey(), batchEntry.getValue(), fieldsToBeExported, emitter, errors, warnings, iCalSession);
            } else {
                throw ImportExportExceptionCodes.CANNOT_EXPORT.create(batchEntry.getKey(), format);
            }            
        }
        boolean error = true;
        try {
            emitter.writeSession(iCalSession, sink.asOutputStream());
            error = false;
            return new SizedInputStream(sink.getClosingStream(), sink.getLength(), Format.ICAL);
        } finally {
            if (error) {
                Streams.close(sink);
            }
        }
    }    
    
    private void exportBatchTasks(ServerSession session, String folderID, List<String> objectId, int[] fieldsToBeExported, ICalEmitter emitter, List<ConversionError> errors, List<ConversionWarning> warnings, ICalSession iCalSession) throws OXException {
        TasksSQLInterface tasksSql = new TasksSQLImpl(session);
        for (String object : objectId) {
            emitter.writeTask(iCalSession, tasksSql.getTaskById(Integer.parseInt(object), Integer.parseInt(folderID)), session.getContext(), errors, warnings);
        }
    }        
    
    private void exportBatchAppointments(ServerSession session, String folderID, List<String> objectId, int[] fieldsToBeExported, ICalEmitter emitter, List<ConversionError> errors, List<ConversionWarning> warnings, ICalSession iCalSession) throws OXException, NumberFormatException, SQLException {
        AppointmentSQLInterface appointmentSql = ImportExportServices.getAppointmentFactoryService().createAppointmentSql(session);
        for (String object : objectId) {
            emitter.writeAppointment(iCalSession, appointmentSql.getObjectById(Integer.parseInt(object), Integer.parseInt(folderID)), session.getContext(), errors, warnings);
        }
    }

    private SizedInputStream doExportData(ServerSession session, Format format, String folderID, String optObjectID, int[] fieldsToBeExported, Map<String, Object> optionalParams) throws OXException {
        FolderObject folder = getFolder(session, folderID);
        int objId = 0;
        if (null != optObjectID) {
            try {
                objId = Integer.parseInt(optObjectID);
            } catch (NumberFormatException e) {
                throw OXException.general("Invalid object identifier: " + optObjectID, e);
            }
        }

        AJAXRequestData requestData = (AJAXRequestData) (optionalParams == null ? null : optionalParams.get("__requestData"));
        if (null != requestData) {
            // Try to stream
            try {
                OutputStream out = requestData.optOutputStream();
                if (null != out) {
                    requestData.setResponseHeader("Content-Type", isSaveToDisk(optionalParams) ? "application/octet-stream" : Format.ICAL.getMimeType() + "; charset=UTF-8");
                    requestData.setResponseHeader("Content-Disposition", "attachment; filename="+getFolderExportFileName(session, folderID) + Format.ICAL.getExtension());
                    requestData.removeCachingHeader();

                    if (FolderObject.CALENDAR == folder.getModule()) {
                        if (ImportExportServices.LOOKUP.get().getService(CalendarService.class).init(session).getConfig().isUseLegacyStack()) {
                            exportAppointments(session, folder.getObjectID(), objId, fieldsToBeExported, out);
                        } else {
                            exportEvents(session, folderID, optObjectID, fieldsToBeExported, out);
                        }
                    } else if (FolderObject.TASK == folder.getModule()) {
                        exportTasks(session, folder.getObjectID(), objId, fieldsToBeExported, out);
                    } else {
                        throw ImportExportExceptionCodes.CANNOT_EXPORT.create(folderID, format);
                    }
                    return null;
                }
            } catch (IOException e) {
                throw ImportExportExceptionCodes.ICAL_CONVERSION_FAILED.create(e);
            }
        }

        ThresholdFileHolder sink;
        if (FolderObject.CALENDAR == folder.getModule()) {
            if (ImportExportServices.LOOKUP.get().getService(CalendarService.class).init(session).getConfig().isUseLegacyStack()) {
                sink = exportAppointments(session, folder.getObjectID(), objId, fieldsToBeExported, null);
            } else {
                sink = exportEvents(session, String.valueOf(folder.getObjectID()), String.valueOf(objId), fieldsToBeExported, null);
            }
        } else if (FolderObject.TASK == folder.getModule()) {
            sink = exportTasks(session, folder.getObjectID(), objId, fieldsToBeExported, null);
        } else {
            throw ImportExportExceptionCodes.CANNOT_EXPORT.create(folderID, format);
        }
        return new SizedInputStream(sink.getClosingStream(), sink.getLength(), Format.ICAL);
    }

    /**
     * Exports one or more appointments from a specific folder to iCal.
     *
     * @param session The session
     * @param folderID The source folder identifier
     * @param objectID The object ID of the appointment to export, or <code>0</code> to export all appointment in the folder
     * @param fieldsToBeExported The column identifiers to include when fetching the appointment from the storage, or <code>null</code>
     *                           to use the defaults
     * @param optOut The optional output stream
     * @return The exported appointments
     * @throws OXException
     */
    private static ThresholdFileHolder exportAppointments(ServerSession session, int folderID, int objectID, int[] fieldsToBeExported, OutputStream optOut) throws OXException {
        ICalEmitter emitter = ImportExportServices.getICalEmitter();
        List<ConversionError> errors = new LinkedList<ConversionError>();
        List<ConversionWarning> warnings = new LinkedList<ConversionWarning>();
        ICalSession iCalSession = emitter.createSession();

        AppointmentSQLInterface appointmentSql = ImportExportServices.getAppointmentFactoryService().createAppointmentSql(session);
        if (0 < objectID) {
            try {
                emitter.writeAppointment(iCalSession, appointmentSql.getObjectById(objectID, folderID), session.getContext(), errors, warnings);
            } catch (SQLException e) {
                throw ImportExportExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
            }
        } else {
            int[] fields = null != fieldsToBeExported ? fieldsToBeExported : _appointmentFields;
            CalendarCollectionService collectionService = ImportExportServices.getCalendarCollectionService();
            SearchIterator<Appointment> searchIterator = appointmentSql.getModifiedAppointmentsInFolder(folderID, fields, DATE_ZERO);
            try {
                while (searchIterator.hasNext()) {
                    Appointment appointment = searchIterator.next();
                    try {
                        if (CalendarObject.NO_RECURRENCE != appointment.getRecurrenceType()) {
                            if (false == appointment.containsTimezone()) {
                                appointment.setTimezone(session.getUser().getTimeZone());
                            }
                            collectionService.replaceDatesWithFirstOccurence(appointment);
                            // appointments need a UID to ensure that exceptions can be associated with them.
                            if (appointment.getUid() == null) {
                                appointment.setUid(UUID.randomUUID().toString());
                            }
                        }

                        emitter.writeAppointment(iCalSession, appointment, session.getContext(), errors, warnings);
                    } catch (OXException e) {
                        LOG.error("Failed to calculate recurring appointment {}", appointment.getObjectID(), e);
                    }
                }
            } finally {
                SearchIterators.close(searchIterator);
            }
        }

        log(errors, warnings);

        if (null != optOut) {
            emitter.writeSession(iCalSession, optOut);
            return null;
        }

        ThresholdFileHolder sink = new ThresholdFileHolder();
        boolean error = true;
        try {
            emitter.writeSession(iCalSession, sink.asOutputStream());
            error = false;
            return sink;
        } finally {
            if (error) {
                Streams.close(sink);
            }
        }
    }

    /**
     * Exports one or more events from a specific folder to iCal.
     *
     * @param session The session
     * @param folderID The source folder identifier
     * @param objectID The object ID of the event to export, or <code>null</code> to export all events in the folder
     * @param optOut The optional output stream
     * @return The exported iCalendar in a file holder, or <code>null</code> if directly written to the output stream
     */
    private static ThresholdFileHolder exportEvents(ServerSession session, String folderID, String objectID, int[] fieldsToBeExported, OutputStream optOut) throws OXException {
        /*
         * initialize export
         */
        CalendarService calendarService = ImportExportServices.LOOKUP.get().getService(CalendarService.class);
        CalendarSession calendarSession = calendarService.init(session);
        calendarSession
            .set(CalendarParameters.PARAMETER_ORDER_BY, EventField.START_DATE)
            .set(CalendarParameters.PARAMETER_ORDER, SortOrder.Order.ASC)
            .set(CalendarParameters.PARAMETER_FIELDS, EVENT_FIELDS)
            .set(CalendarParameters.PARAMETER_RECURRENCE_MASTER, Boolean.TRUE)
        ;
        ICalService iCalService = ImportExportServices.LOOKUP.get().getService(ICalService.class);
        ICalParameters iCalParameters = iCalService.initParameters();
        iCalParameters.set(ICalParameters.DEFAULT_TIMEZONE, TimeZone.getTimeZone(session.getUser().getTimeZone()));
        CalendarExport calendarExport = iCalService.exportICal(iCalParameters);
        calendarExport.setMethod("PUBLISH");
        /*
         * perform export
         */
        if (null != objectID) {
            Event event = calendarService.getEvent(calendarSession, folderID, new EventID(folderID, objectID));
            calendarExport.add(event);
            if (CalendarUtils.isSeriesMaster(event) && null != event.getChangeExceptionDates() && 0 < event.getChangeExceptionDates().size()) {
                for (Event changeException : calendarService.getChangeExceptions(calendarSession, folderID, objectID)) {
                    calendarExport.add(changeException);
                }
            }
        } else {
            calendarExport.setName(getFolder(session, folderID).getFolderName());
            for (Event event : calendarService.getEventsInFolder(calendarSession, folderID)) {
                calendarExport.add(event);
            }
        }

        if (null != optOut) {
            calendarExport.writeVCalendar(optOut);
        } else {
            ThresholdFileHolder sink = new ThresholdFileHolder();
            boolean error = true;
            try {
                calendarExport.writeVCalendar(sink.asOutputStream());
                error = false;
                return sink;
            } finally {
                if (error) {
                    Streams.close(sink);
                }
            }
        }
        return null;
    }

    /**
     * Exports one or more tasks from a specific folder to iCal.
     *
     * @param session The session
     * @param folderID The source folder identifier
     * @param objectID The object ID of the task to export, or <code>0</code> to export all tasks in the folder
     * @param fieldsToBeExported The column identifiers to include when fetching the appointment from the storage, or <code>null</code>
     *                           to use the defaults
     * @param optOut The optional output stream
     * @return The exported tasks
     */
    private static ThresholdFileHolder exportTasks(ServerSession session, int folderID, int objectID, int[] fieldsToBeExported, OutputStream optOut) throws OXException {
        ICalEmitter emitter = ImportExportServices.getICalEmitter();
        List<ConversionError> errors = new LinkedList<ConversionError>();
        List<ConversionWarning> warnings = new LinkedList<ConversionWarning>();
        ICalSession iCalSession = emitter.createSession();

        TasksSQLInterface tasksSql = new TasksSQLImpl(session);
        if (0 < objectID) {
            emitter.writeTask(iCalSession, tasksSql.getTaskById(objectID, folderID), session.getContext(), errors, warnings);
        } else {
            int[] fields = null != fieldsToBeExported ? fieldsToBeExported : _taskFields;
            SearchIterator<Task> searchIterator = tasksSql.getModifiedTasksInFolder(folderID, fields, DATE_ZERO);
            try {
                while (searchIterator.hasNext()) {
                    emitter.writeTask(iCalSession, searchIterator.next(), session.getContext(), errors, warnings);
                }
            } finally {
                SearchIterators.close(searchIterator);
            }
        }

        log(errors, warnings);

        if (null != optOut) {
            emitter.writeSession(iCalSession, optOut);
            return null;
        }

        ThresholdFileHolder sink = new ThresholdFileHolder();
        boolean error = true;
        try {
            emitter.writeSession(iCalSession, sink.asOutputStream());
            error = false;
            return sink;
        } finally {
            if (error) {
                Streams.close(sink);
            }
        }
    }

    /**
     * Gets a folder by it's identifier.
     *
     * @param session The session
     * @param folderID The folder identifier
     * @return The folder
     */
    private static FolderObject getFolder(ServerSession session, String folderID) throws OXException {
        try {
            return new OXFolderAccess(session.getContext()).getFolderObject(Integer.parseInt(folderID));
        } catch (OXException e) {
            throw ImportExportExceptionCodes.LOADING_FOLDER_FAILED.create(e, folderID);
        } catch (NumberFormatException e) {
            throw ImportExportExceptionCodes.LOADING_FOLDER_FAILED.create(e, folderID);
        }

    }

    private static void log(final List<ConversionError> errors, final List<ConversionWarning> warnings) {
        for(final ConversionError error : errors) {
            LOG.warn(error.getMessage());
        }

        for(final ConversionWarning warning : warnings) {
            LOG.warn(warning.getMessage());
        }
    }

    private boolean isSaveToDisk(final Map<String, Object> optionalParams) {
        if (null == optionalParams) {
            return false;
        }
        final Object object = optionalParams.get("__saveToDisk");
        if (null == object) {
            return false;
        }
        return (object instanceof Boolean ? ((Boolean) object).booleanValue() : Boolean.parseBoolean(object.toString().trim()));
    }
    
    @Override
    public String getFolderExportFileName(ServerSession sessionObj, String folder) throws OXException {
        return ExportFileNameCreator.createFolderExportFileName(sessionObj, folder);
    }

    @Override
    public String getBatchExportFileName(ServerSession sessionObj, Map<String, List<String>> batchIds) throws OXException {
        return ExportFileNameCreator.createBatchExportFileName(sessionObj, batchIds);
    }
}
