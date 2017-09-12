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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalSession;
import com.openexchange.exception.OXException;
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
import com.openexchange.importexport.helpers.SizedInputStream;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.java.Streams;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ICalTaskExporter}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public class ICalTaskExporter extends AbstractICalExporter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ICalTaskExporter.class);

    private static final Date DATE_ZERO = new Date(0);
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

    @Override
    public boolean canExport(ServerSession session, Format format, String folder, Map<String, Object> optionalParams) throws OXException {
        if(!format.equals(Format.ICAL)){
            return false;
        }
        final int folderId = Integer.parseInt(folder);
        FolderObject fo;
        try {
            fo = new OXFolderAccess(session.getContext()).getFolderObject(folderId);

        } catch (final OXException e) {
            return false;
        }
        //TODO remove CALENDAR check and add a check to clarify if all folders are TASK folders
        //check format of folder
        final int module = fo.getModule();
        if (module == FolderObject.CALENDAR) {
            if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), session.getContext()).hasCalendar()) {
                return false;
            }
        } else if (module == FolderObject.TASK) {
            if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), session.getContext()).hasTask()) {
                return false;
            }
        } else {
            return false;
        }

        //check read access to folder
        EffectivePermission perm;
        try {
            perm = fo.getEffectiveUserPermission(session.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), session.getContext()));
        } catch (final OXException e) {
            throw ImportExportExceptionCodes.NO_DATABASE_CONNECTION.create(e);
        } catch (final RuntimeException e) {
            throw ImportExportExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        }
        return perm.canReadAllObjects();
    }

    @Override
    public boolean canExportBatch(ServerSession session, Format format, Entry<String, List<String>> batchIds, Map<String, Object> optionalParams) throws OXException {
        if (!canExport(session, format, batchIds.getKey(), optionalParams)) {
            return false;
        }
        for (String objectId : batchIds.getValue()) {
            try {
                Integer.parseInt(objectId);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public SizedInputStream exportFolderData(ServerSession session, Format format, String folder, int[] fieldsToBeExported, Map<String, Object> optionalParams) throws OXException {
        return doExportTaskData(session, format, folder, fieldsToBeExported, optionalParams);
    }

    @Override
    public SizedInputStream exportBatchData(ServerSession session, Format format, Map<String, List<String>> batchIds, int[] fieldsToBeExported, Map<String, Object> optionalParams) throws OXException {
        return exportBatchTasks(session, format, batchIds, optionalParams);
    }

    private SizedInputStream doExportTaskData(ServerSession session, Format format, String folderID, int[] fieldsToBeExported, Map<String, Object> optionalParams) throws OXException {
        FolderObject folder = getFolder(session, folderID);

        AJAXRequestData requestData = (AJAXRequestData) (optionalParams == null ? null : optionalParams.get("__requestData"));
        if (null != requestData) {
            // Try to stream
            try {
                OutputStream out = requestData.optOutputStream();
                if (null != out) {
                    requestData.setResponseHeader("Content-Type", isSaveToDisk(optionalParams) ? "application/octet-stream" : Format.ICAL.getMimeType() + "; charset=UTF-8");
                    requestData.setResponseHeader("Content-Disposition", "attachment"+appendFileNameParameter(requestData, getFolderExportFileName(session, folderID, Format.ICAL.getExtension())));
                    requestData.removeCachingHeader();
                    if (FolderObject.TASK == folder.getModule()) {
                        exportTasks(session, folder.getObjectID(), fieldsToBeExported, out);
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
        if (FolderObject.TASK == folder.getModule()) {
            sink = exportTasks(session, folder.getObjectID(), fieldsToBeExported, null);
        } else {
            throw ImportExportExceptionCodes.CANNOT_EXPORT.create(folderID, format);
        }
        return new SizedInputStream(sink.getClosingStream(), sink.getLength(), Format.ICAL);
    }

    private SizedInputStream exportBatchTasks(ServerSession session, Format format, Map<String, List<String>> batchIds, Map<String, Object> optionalParams) throws OXException {
        for (Map.Entry<String, List<String>> batchEntry : batchIds.entrySet()) {
            if (!canExportBatch(session, format, batchEntry, optionalParams)) {
                throw ImportExportExceptionCodes.CANNOT_EXPORT.create(batchEntry.getKey(), format);
            }
        }

        AJAXRequestData requestData = (AJAXRequestData) (optionalParams == null ? null : optionalParams.get("__requestData"));
        ICalEmitter emitter = ImportExportServices.getICalEmitter();
        List<ConversionError> errors = new LinkedList<>();
        List<ConversionWarning> warnings = new LinkedList<>();
        ICalSession iCalSession = emitter.createSession();

        TasksSQLInterface tasksSql = new TasksSQLImpl(session);
        if (null != requestData) {
            try {
                OutputStream out = requestData.optOutputStream();
                if (null != out) {
                    requestData.setResponseHeader("Content-Type", isSaveToDisk(optionalParams) ? "application/octet-stream" : Format.ICAL.getMimeType() + "; charset=UTF-8");
                    requestData.setResponseHeader("Content-Disposition", "attachment"+appendFileNameParameter(requestData, getBatchExportFileName(session, batchIds, Format.ICAL.getExtension())));
                    requestData.removeCachingHeader();
                    for (Map.Entry<String, List<String>> batchEntry : batchIds.entrySet()) {
                        FolderObject folder = getFolder(session, batchEntry.getKey());
                        if (FolderObject.TASK == folder.getModule()) {
                            for (String object : batchEntry.getValue()) {
                                emitter.writeTask(iCalSession, tasksSql.getTaskById(Integer.parseInt(object), Integer.parseInt(batchEntry.getKey())), session.getContext(), errors, warnings);
                            }
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
        boolean error = true;
        try {
            for (Map.Entry<String, List<String>> batchEntry : batchIds.entrySet()) {
                FolderObject folder = getFolder(session, batchEntry.getKey());
                if (FolderObject.TASK == folder.getModule()) {
                    for (String object : batchEntry.getValue()) {
                        emitter.writeTask(iCalSession, tasksSql.getTaskById(Integer.parseInt(object), Integer.parseInt(batchEntry.getKey())), session.getContext(), errors, warnings);
                    }
                    log(errors, warnings);
                } else {
                    throw ImportExportExceptionCodes.CANNOT_EXPORT.create(batchEntry.getKey(), format);
                }
            }
            emitter.writeSession(iCalSession, sink.asOutputStream());
            SizedInputStream sizedInputStream = new SizedInputStream(sink.getClosingStream(), sink.getLength(), Format.ICAL);
            error = false;
            return sizedInputStream;
        } finally {
            if (error) {
                Streams.close(sink);
            }
        }
    }

    /**
     * Exports one or more tasks from a specific folder to iCal.
     *
     * @param session The session
     * @param folderID The source folder identifier
     * @param fieldsToBeExported The column identifiers to include when fetching the appointment from the storage, or <code>null</code>
     *                           to use the defaults
     * @param optOut The optional output stream
     * @return The exported tasks
     */
    private static ThresholdFileHolder exportTasks(ServerSession session, int folderID, int[] fieldsToBeExported, OutputStream optOut) throws OXException {
        ICalEmitter emitter = ImportExportServices.getICalEmitter();
        List<ConversionError> errors = new LinkedList<>();
        List<ConversionWarning> warnings = new LinkedList<>();
        ICalSession iCalSession = emitter.createSession();

        TasksSQLInterface tasksSql = new TasksSQLImpl(session);
        int[] fields = null != fieldsToBeExported ? fieldsToBeExported : _taskFields;
        SearchIterator<Task> searchIterator = tasksSql.getModifiedTasksInFolder(folderID, fields, DATE_ZERO);
        try {
            while (searchIterator.hasNext()) {
                Task task = searchIterator.next();
                if (null != task) {
                    emitter.writeTask(iCalSession, task, session.getContext(), errors, warnings);
                }
            }
        } finally {
            SearchIterators.close(searchIterator);
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

    private static void log(final List<ConversionError> errors, final List<ConversionWarning> warnings) {
        for(final ConversionError error : errors) {
            LOG.warn(error.getMessage());
        }

        for(final ConversionWarning warning : warnings) {
            LOG.warn(warning.getMessage());
        }
    }

}
