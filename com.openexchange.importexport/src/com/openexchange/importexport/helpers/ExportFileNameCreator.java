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

package com.openexchange.importexport.helpers;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.folder.FolderService;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.java.Strings;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ExportFileNameCreator}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10
 */
public class ExportFileNameCreator {

    /**
     * Prevent instantiation.
     */
    private ExportFileNameCreator() {
        super();
    }

    /**
     * The default file name for a vcard and csv export, when using batch data
     */
    private static final String CONTACTS_NAME = "Contacts";

    /**
     * The default file name for an ical appointment export, when using batch data
     */
    private static final String ICAL_APPOINTMENT_NAME = "Appointment";

    /**
     * The default file name for an ical task export, when using batch data
     */
    private static final String ICAL_TASKS_NAME = "Tasks";

    /**
     * @param session, the session object
     * @param folder, the folder to create the file name with
     * @return String, the file name
     * @throws OXException
     */
    public static String createFolderExportFileName(ServerSession session, String folder) throws OXException {
        FolderService folderService = ImportExportServices.getFolderService();
        final StringBuilder sb = new StringBuilder();
        try {
            FolderObject folderObj = folderService.getFolderObject(Integer.parseInt(folder), session.getContextId());
            sb.append(folderObj.getFolderName());
        } catch (OXException e) {
            throw ImportExportExceptionCodes.COULD_NOT_CREATE_FILE_NAME.create(e);
        }
        sb.append(".");
        return sb.toString();
    }

    /**
     * @param session, the session object
     * @param batchIds, the batchIds to create the file name with
     * @return String, the file name
     * @throws OXException
     * @throws SQLException
     * @throws NumberFormatException
     */
    public static String createBatchExportFileName(ServerSession session, Map<String, List<String>> batchIds) throws OXException, NumberFormatException {
        StringBuilder sb = new StringBuilder();
        if (batchIds.size() == 1) {
            //check for contacts of the same folder
            String folderId = batchIds.keySet().iterator().next();
            List<String> contactIdList = batchIds.get(folderId);
            if (contactIdList.size() > 1) {
                sb.append(createBatchExportFileName(session, folderId, null));
            } else {
                //exactly one contact to export, file name equals contact name
                String batchId = batchIds.get(folderId).get(0);
                sb.append(createBatchExportFileName(session, folderId, batchId));
            }
        } else {
            //batch of contact ids from different folders, file name is set to a default
            String folderId = batchIds.keySet().iterator().next();
            FolderService folderService = ImportExportServices.getFolderService();
            FolderObject folderObj = folderService.getFolderObject(Integer.parseInt(folderId), session.getContextId());
            if (FolderObject.CONTACT == folderObj.getModule()) {
                sb.append(getLocalizedFileName(session, CONTACTS_NAME));
            } else if (FolderObject.CALENDAR == folderObj.getModule()) {
                sb.append(getLocalizedFileName(session, ICAL_APPOINTMENT_NAME));
            } else if (FolderObject.TASK == folderObj.getModule()) {
                sb.append(getLocalizedFileName(session, ICAL_TASKS_NAME));
            }
            sb.append(".");
        }
        return sb.toString();
    }

    /**
     * @param session, the session object
     * @param folder, the folderId to create the file name with
     * @param batchId, the batchId to create the file name with
     * @return String, the file name
     * @throws OXException
     * @throws SQLException
     * @throws NumberFormatException
     */
    private static String createBatchExportFileName(ServerSession session, String folder, String batchId) throws OXException, NumberFormatException {
        StringBuilder sb = new StringBuilder();
        FolderService folderService = ImportExportServices.getFolderService();
        FolderObject folderObj = folderService.getFolderObject(Integer.parseInt(folder), session.getContextId());
        if (null == batchId || batchId.equals("")) {
            sb.append(getLocalizedFileName(session, folderObj.getFolderName()));
        } else {
            try {
                if (FolderObject.CONTACT == folderObj.getModule()) {
                    sb.append(createSingleContactName(session, folder, batchId));
                } else if (FolderObject.CALENDAR == folderObj.getModule()) {
                    AppointmentSQLInterface appointmentSql = ImportExportServices.getAppointmentFactoryService().createAppointmentSql(session);
                    Appointment appointmentObj = appointmentSql.getObjectById(Integer.parseInt(batchId), Integer.parseInt(folder));
                    String title = appointmentObj.getTitle();
                    if (Strings.isEmpty(title)) {
                        sb.append(getLocalizedFileName(session, ICAL_APPOINTMENT_NAME));
                    } else {
                        sb.append(title);
                    }
                } else if (FolderObject.TASK == folderObj.getModule()) {
                    TasksSQLInterface tasksSql = new TasksSQLImpl(session);
                    Task taskObj = tasksSql.getTaskById(Integer.parseInt(batchId), Integer.parseInt(folder));
                    String title = taskObj.getTitle();
                    if (Strings.isEmpty(title)) {
                        sb.append(getLocalizedFileName(session, ICAL_TASKS_NAME));
                    } else {
                        sb.append(title);
                    }
                }
            } catch (OXException e) {
                throw ImportExportExceptionCodes.COULD_NOT_CREATE_FILE_NAME.create(e);
            } catch (SQLException e) {
                throw ImportExportExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
            }
        }
        sb.append(".");
        return sb.toString();
    }

    /**
     * @param session, the session object
     * @param fileName, the file name of the module
     * @return String, the localized file name
     */
    private static String getLocalizedFileName(ServerSession session, String fileName) {
        return StringHelper.valueOf(session.getUser().getLocale()).getString(fileName);
    }

    /**
     * @param session, the session object
     * @param folder, the folderId
     * @param batchId, the objectId
     * @return String, a file name for a single contact export
     * @throws OXException
     */
    private static String createSingleContactName(ServerSession session, String folder, String batchId) throws OXException {
        StringBuilder sb = new StringBuilder();
        ContactService contactService = ImportExportServices.getContactService();
        Contact contactObj = contactService.getContact(session, folder, batchId, null);
        if (contactObj.containsMarkAsDistributionlist()) {
            String displayName = contactObj.getDisplayName();
            if (Strings.isEmpty(displayName)) {
                sb.append(getLocalizedFileName(session, CONTACTS_NAME));
            } else {
                sb.append(displayName);
            }
        } else {
            if (Strings.isEmpty(contactObj.getGivenName()) && Strings.isEmpty(contactObj.getSurName())) {
                sb.append(getLocalizedFileName(session, CONTACTS_NAME));
            } else if (!Strings.isEmpty(contactObj.getGivenName()) && !Strings.isEmpty(contactObj.getSurName())) {
                sb.append(contactObj.getGivenName() + " " + contactObj.getSurName());
            } else {
                if (!Strings.isEmpty(contactObj.getGivenName()) && Strings.isEmpty(contactObj.getSurName())) {
                    sb.append(contactObj.getGivenName());
                } else if (Strings.isEmpty(contactObj.getGivenName()) && !Strings.isEmpty(contactObj.getSurName())) {
                    sb.append(contactObj.getSurName());
                }
            }
        }
        return sb.toString();
    }
}
