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

package com.openexchange.importexport.importers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.data.conversion.ical.TruncationInfo;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.importers.ical.ICalCompositeEventImporter;
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
 * @see AppointmentSQLInterface AppointmentSQLInterface - if you have a problem
 *      entering the parsed entry as Appointment
 * @see TasksSQLInterface TasksSQLInterface - if you have trouble entering the
 *      parsed entry as Task
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb'
 *         Prinz</a> (changes to new interface, bugfixes, maintenance)
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a> refactoring for chronos calendar
 */
public class ICalImporter extends AbstractImporter {

    public ICalImporter(ServiceLookup services) {
        super(services);
    }

	@Override
    public boolean canImport(final ServerSession session, final Format format,
			final List<String> folders,
			final Map<String, String[]> optionalParams)
			throws OXException {
	    if(!format.equals(Format.ICAL)){
            return false;
        }

        FolderService service = ImportExportServices.getFolderService();
        UserizedFolder userizedFolder = service.getFolder(FolderStorage.REAL_TREE_ID, folders.get(0), session, null);

        if (TaskContentType.getInstance().equals(userizedFolder.getContentType())) {
            if (!session.getUserPermissionBits().hasTask()) {
                return false;
            }
        } else if (null == userizedFolder.getAccountID()) {
            if (!session.getUserConfiguration().hasCalendar()) {
                return false;
            }
        } else if (null != userizedFolder.getAccountID()) {
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
		TruncationInfo truncationInfo = null;
		final List<ImportResult> list = new ArrayList<>();

        FolderService service = ImportExportServices.getFolderService();
        UserizedFolder userizedFolder = service.getFolder(FolderStorage.REAL_TREE_ID, folders.get(0), session, null);

        boolean ignoreUIDs = isIgnoreUIDs(optionalParams);
        Map<String, String> uidReplacements = ignoreUIDs ? new HashMap<>() : null;

        ICalImport importer;
        if (TaskContentType.getInstance().equals(userizedFolder.getContentType())) {
            importer = new ICalTaskImporter(session);
        } else if (null != userizedFolder.getAccountID()) {
            importer = new ICalCompositeEventImporter(session);
        } else if (null == userizedFolder.getAccountID()) {
            importer = new ICalEventImporter(session);
        } else {
            throw ImportExportExceptionCodes.RESOURCE_HARD_CONFLICT.create();
        }
        truncationInfo = importer.importData(userizedFolder, is, list, optionalParams, uidReplacements);
		return new DefaultImportResults(list, truncationInfo);
	}

    /**
     * Gets a value whether the supplied parameters indicate that UIDs should be ignored during import or not.
     *
     * @param optionalParams The optional parameters as passed from the import request, may be <code>null</code>
     * @return <code>true</code> if UIDs should be ignored, <code>false</code>, otherwise
     */
    private static boolean isIgnoreUIDs(Map<String, String[]> optionalParams) {
        if (null != optionalParams) {
            String[] value = optionalParams.get("ignoreUIDs");
            if (null != value && 0 < value.length) {
                return Boolean.valueOf(value[0]).booleanValue();
            }
        }
        return false;
    }

}
