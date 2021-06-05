/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.importexport.exporters;

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.importexport.Format;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.exporters.ical.ICalCompositeEventExporter;
import com.openexchange.importexport.exporters.ical.ICalEventExporter;
import com.openexchange.importexport.exporters.ical.ICalExport;
import com.openexchange.importexport.exporters.ical.ICalTaskExporter;
import com.openexchange.importexport.helpers.SizedInputStream;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ICalExporter}
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a> (minor: changes to new interface; fixes)
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a> - batch data, chronos support
 */
public class ICalExporter extends AbstractExporter {

    @Override
    public boolean canExport(ServerSession session, Format format, String folder, Map<String, Object> optionalParams) throws OXException {
        if (!format.equals(Format.ICAL)){
            return false;
        }
        UserizedFolder userizedFolder = getUserizedFolder(session, folder);
        if (TaskContentType.getInstance().equals(userizedFolder.getContentType())) {
            if (!session.getUserPermissionBits().hasTask()) {
                return false;
            }
        } else if (com.openexchange.folderstorage.database.contentType.CalendarContentType.getInstance().equals(userizedFolder.getContentType())) {
            if (!session.getUserConfiguration().hasCalendar()) {
                return false;
            }
        } else if (com.openexchange.folderstorage.calendar.contentType.CalendarContentType.getInstance().equals(userizedFolder.getContentType())) {
            if (!session.getUserConfiguration().hasCalendar()) {
                return false;
            }
        } else {
            return false;
        }
        if (!(Permission.READ_ALL_OBJECTS <= userizedFolder.getOwnPermission().getReadPermission())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canExportBatch(ServerSession session, Format format, Entry<String, List<String>> batchIds, Map<String, Object> optionalParams) throws OXException {
        if (!canExport(session, format, batchIds.getKey(), optionalParams)) {
            return false;
        }
        return true;
    }

    @Override
    public SizedInputStream exportFolderData(ServerSession session, Format format, String folder, int[] fieldsToBeExported, Map<String, Object> optionalParams) throws OXException {
        if (!canExport(session, format, folder, optionalParams)) {
            throw ImportExportExceptionCodes.CANNOT_EXPORT.create(folder, format);
        }
        AJAXRequestData requestData = (AJAXRequestData) (optionalParams == null ? null : optionalParams.get("__requestData"));
        OutputStream out = (OutputStream) (optionalParams == null ? null : optionalParams.get("__outputStream"));
        return getExporter(getUserizedFolder(session, folder), Collections.emptyMap(), fieldsToBeExported).exportData(session, requestData, out, isSaveToDisk(optionalParams), appendFileNameParameter(requestData, getFolderExportFileName(session, folder, Format.ICAL.getExtension())));
    }

    @Override
    public SizedInputStream exportBatchData(ServerSession session, Format format, Map<String, List<String>> batchIds, int[] fieldsToBeExported, Map<String, Object> optionalParams) throws OXException {
        for (Map.Entry<String, List<String>> batchEntry : batchIds.entrySet()) {
            if (!canExportBatch(session, format, batchEntry, optionalParams)) {
                throw ImportExportExceptionCodes.CANNOT_EXPORT.create(batchEntry.getKey(), format);
            }
        }
        AJAXRequestData requestData = (AJAXRequestData) (optionalParams == null ? null : optionalParams.get("__requestData"));
        OutputStream out = (OutputStream) (optionalParams == null ? null : optionalParams.get("__outputStream"));
        return getBatchExporter(session, batchIds, fieldsToBeExported).exportData(session, requestData, out, isSaveToDisk(optionalParams), appendFileNameParameter(requestData, getBatchExportFileName(session, batchIds, Format.ICAL.getExtension())));
    }

    private ICalExport getBatchExporter(ServerSession session, Map<String, List<String>> batchIds, int[] fieldsToBeExported) throws OXException {
        return getExporter(getUserizedFolder(session, batchIds.keySet().stream().findFirst().get()), batchIds, fieldsToBeExported);
    }

    private ICalExport getExporter(UserizedFolder userizedFolder, Map<String, List<String>> batchIds, int[] fieldsToBeExported) {
        ICalExport exporter;
        String folderId = userizedFolder.getID();
        if (TaskContentType.getInstance().equals(userizedFolder.getContentType())) {
            exporter = new ICalTaskExporter(folderId, batchIds, fieldsToBeExported);
        } else if (null == userizedFolder.getAccountID()) {
            exporter = new ICalEventExporter(folderId, batchIds);
        } else {
            exporter = new ICalCompositeEventExporter(folderId, batchIds);
        }
        return exporter;
    }

    private UserizedFolder getUserizedFolder(ServerSession session, String folder) throws OXException {
        return ImportExportServices.getFolderService().getFolder(FolderStorage.REAL_TREE_ID, folder, session, null);
    }

}
