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
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.exporters.ical.ICalCompositeEventExporter;
import com.openexchange.importexport.exporters.ical.ICalEventExporter;
import com.openexchange.importexport.exporters.ical.ICalExport;
import com.openexchange.importexport.exporters.ical.ICalTaskExporter;
import com.openexchange.importexport.formats.Format;
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
        if(!format.equals(Format.ICAL)){
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
        return getExporter(getUserizedFolder(session, folder), Collections.emptyMap(), fieldsToBeExported).exportData(session, requestData, isSaveToDisk(optionalParams), appendFileNameParameter(requestData, getFolderExportFileName(session, folder, Format.ICAL.getExtension())));
    }

    @Override
    public SizedInputStream exportBatchData(ServerSession session, Format format, Map<String, List<String>> batchIds, int[] fieldsToBeExported, Map<String, Object> optionalParams) throws OXException {
        for (Map.Entry<String, List<String>> batchEntry : batchIds.entrySet()) {
            if (!canExportBatch(session, format, batchEntry, optionalParams)) {
                throw ImportExportExceptionCodes.CANNOT_EXPORT.create(batchEntry.getKey(), format);
            }
        }
        AJAXRequestData requestData = (AJAXRequestData) (optionalParams == null ? null : optionalParams.get("__requestData"));
        return getBatchExporter(session, batchIds, fieldsToBeExported).exportData(session, requestData, isSaveToDisk(optionalParams), appendFileNameParameter(requestData, getBatchExportFileName(session, batchIds, Format.ICAL.getExtension())));
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
