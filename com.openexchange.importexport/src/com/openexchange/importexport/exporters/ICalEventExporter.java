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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.chronos.ical.CalendarExport;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.helpers.SizedInputStream;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.java.Streams;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ICalEventExporter} Event exporter for the legacy calendar api
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public class ICalEventExporter extends AbstractICalExporter {

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
        final int module = fo.getModule();
        if (module == FolderObject.CALENDAR) {
            if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), session.getContext()).hasCalendar()) {
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
        return doExportFolderEvents(session, format, folder, optionalParams);
    }

    @Override
    public SizedInputStream exportBatchData(ServerSession session, Format format, Map<String, List<String>> batchIds, int[] fieldsToBeExported, Map<String, Object> optionalParams) throws OXException {
        return doExportBatchEvents(session, format, batchIds, optionalParams);
    }

    private SizedInputStream doExportFolderEvents(ServerSession session, Format format, String folder, Map<String, Object> optionalParams) throws OXException {
        if (!canExport(session, format, folder, optionalParams)) {
            throw ImportExportExceptionCodes.CANNOT_EXPORT.create(folder, format);
        }
        CalendarService calendarService = ImportExportServices.getCalendarService();
        CalendarSession calendarSession = calendarService.init(session);
        ICalService iCalService= ImportExportServices.getICalService();
        ICalParameters iCalParameters = iCalService.initParameters();
        CalendarExport calendarExport = iCalService.exportICal(iCalParameters);

        AJAXRequestData requestData = (AJAXRequestData) (optionalParams == null ? null : optionalParams.get("__requestData"));
        if (null != requestData) {
            // Try to stream
            try {
                OutputStream out = requestData.optOutputStream();
                if (null != out) {
                    requestData.setResponseHeader("Content-Type", isSaveToDisk(optionalParams) ? "application/octet-stream" : Format.ICAL.getMimeType() + "; charset=UTF-8");
                    requestData.setResponseHeader("Content-Disposition", "attachment"+appendFileNameParameter(requestData, getFolderExportFileName(session, folder, Format.ICAL.getExtension())));
                    requestData.removeCachingHeader();
                    exportChronosEvents(calendarService.getEventsInFolder(calendarSession, folder), calendarExport, out);
                    return null;
                }
            } catch (IOException e) {
                throw ImportExportExceptionCodes.ICAL_CONVERSION_FAILED.create(e);
            }
        }

        ThresholdFileHolder sink;
        sink = exportChronosEvents(calendarService.getEventsInFolder(calendarSession, folder), calendarExport, null);
        return new SizedInputStream(sink.getClosingStream(), sink.getLength(), Format.ICAL);

    }

    private SizedInputStream doExportBatchEvents(ServerSession session, Format format, Map<String, List<String>> batchIds, Map<String, Object> optionalParams) throws OXException {
        for (Map.Entry<String, List<String>> batchEntry : batchIds.entrySet()) {
            if (!canExportBatch(session, format, batchEntry, optionalParams)) {
                throw ImportExportExceptionCodes.CANNOT_EXPORT.create(batchEntry.getKey(), format);
            }
        }
        CalendarService calendarService = ImportExportServices.getCalendarService();
        CalendarSession calendarSession = calendarService.init(session);
        ICalService iCalService= ImportExportServices.getICalService();
        ICalParameters iCalParameters = iCalService.initParameters();
        CalendarExport calendarExport = iCalService.exportICal(iCalParameters);

        AJAXRequestData requestData = (AJAXRequestData) (optionalParams == null ? null : optionalParams.get("__requestData"));
        if (null != requestData) {
            try {
                OutputStream out = requestData.optOutputStream();
                if (null != out) {
                    requestData.setResponseHeader("Content-Type", isSaveToDisk(optionalParams) ? "application/octet-stream" : Format.ICAL.getMimeType() + "; charset=UTF-8");
                    requestData.setResponseHeader("Content-Disposition", "attachment"+appendFileNameParameter(requestData, getBatchExportFileName(session, batchIds, Format.ICAL.getExtension())));
                    requestData.removeCachingHeader();
                    for (Map.Entry<String, List<String>> batchEntry : batchIds.entrySet()) {
                        FolderObject folder = getFolder(session, batchEntry.getKey());
                        if (FolderObject.CALENDAR == folder.getModule()) {
                            exportChronosEvents(calendarService.getEvents(calendarSession, convertBatchDataToEventIds(batchEntry.getKey(), batchEntry.getValue())), calendarExport, out);
                        } else {
                            throw ImportExportExceptionCodes.CANNOT_EXPORT.create(batchEntry.getKey(), format);
                        }
                    }
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
                if (FolderObject.CALENDAR == folder.getModule()) {
                    sink = exportChronosEvents(calendarService.getEvents(calendarSession, convertBatchDataToEventIds(batchEntry.getKey(), batchEntry.getValue())), calendarExport, null);
                } else {
                    throw ImportExportExceptionCodes.CANNOT_EXPORT.create(batchEntry.getKey(), format);
                }
            }
            SizedInputStream sizedInputStream = new SizedInputStream(sink.getClosingStream(), sink.getLength(), Format.ICAL);
            error = false;
            return sizedInputStream;
        } finally {
            if (error) {
                Streams.close(sink);
            }
        }
    }

}
