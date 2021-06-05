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

package com.openexchange.importexport.importers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.data.conversion.ical.TruncationInfo;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.importexport.Format;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.importers.ical.ICalEventImporter;
import com.openexchange.importexport.importers.ical.ICalImport;
import com.openexchange.importexport.importers.ical.ICalTaskImporter;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * Imports ICal files. ICal files can be translated to either tasks or
 * appointments within the OX, so the importer works with both SQL interfaces.
 *
 * @see TasksSQLInterface TasksSQLInterface - if you have trouble entering the
 *      parsed entry as Task
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb'
 *         Prinz</a> (changes to new interface, bugfixes, maintenance)
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a> refactoring for chronos calendar
 */
public class ICalImporter extends AbstractImporter {

    private static final ContentType TASK_CONTENT_TYPE = TaskContentType.getInstance();
    private static final ContentType CALENDAR_CONTENT_TYPE = com.openexchange.folderstorage.database.contentType.CalendarContentType.getInstance();
    private static final ContentType EVENT_CONTENT_TYPE = com.openexchange.folderstorage.calendar.contentType.CalendarContentType.getInstance();

    public ICalImporter(ServiceLookup services) {
        super(services);
    }

	@Override
    public boolean canImport(ServerSession session, Format format, List<String> folders, Map<String, String[]> optionalParams) throws OXException {
        if (!format.equals(Format.ICAL) || null == folders || folders.isEmpty()) {
            return false;
        }
        UserizedFolder userizedFolder = getUserizedFolder(session, folders.get(0));
        if (TASK_CONTENT_TYPE.equals(userizedFolder.getContentType())) {
            if (!session.getUserPermissionBits().hasTask()) {
                return false;
            }
        } else if (EVENT_CONTENT_TYPE.equals(userizedFolder.getContentType()) || CALENDAR_CONTENT_TYPE.equals(userizedFolder.getContentType())) {
            if (!session.getUserConfiguration().hasCalendar()) {
                return false;
            }
        } else {
            return false;
        }
        if (!(Permission.WRITE_ALL_OBJECTS <= userizedFolder.getOwnPermission().getWritePermission())) {
            return false;
        }
        return true;
	}

	@Override
    public ImportResults importData(final ServerSession session,
			final Format format, final InputStream is,
			final List<String> folders,
			final Map<String, String[]> optionalParams)
			throws OXException {
        UserizedFolder userizedFolder = getUserizedFolder(session, folders.get(0));
	    if (!canImport(session, format, folders, optionalParams)) {
	        throw ImportExportExceptionCodes.CANNOT_IMPORT.create(format, userizedFolder.getID());
        }
		TruncationInfo truncationInfo = null;
		final List<ImportResult> list = new ArrayList<>();
        ICalImport importer;
        if (TASK_CONTENT_TYPE.equals(userizedFolder.getContentType())) {
            importer = new ICalTaskImporter(session, userizedFolder);
        } else if (CALENDAR_CONTENT_TYPE.equals(userizedFolder.getContentType()) || EVENT_CONTENT_TYPE.equals(userizedFolder.getContentType())) {
            importer = new ICalEventImporter(session, userizedFolder);
        } else {
            throw ImportExportExceptionCodes.CANNOT_IMPORT.create(format, userizedFolder.getID());
        }
        truncationInfo = importer.importData(is, list, optionalParams);
		return new DefaultImportResults(list, truncationInfo);
	}

    private UserizedFolder getUserizedFolder(ServerSession session, String folder) throws OXException {
        return ImportExportServices.getFolderService().getFolder(FolderStorage.REAL_TREE_ID, folder, session, null);
    }

}
