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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.importexport.importers;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.database.DBPoolingException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarField;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.importexport.AbstractImporter;
import com.openexchange.groupware.importexport.Format;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionClasses;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionFactory;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TaskField;
import com.openexchange.groupware.tasks.TasksSQLInterfaceImpl;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 * Imports ICal files. ICal files can be translated to either tasks or appointments within the OX, so the importer works with both SQL
 * interfaces.
 * 
 * @see OXContainerConverter OXContainerConverter - if you have a problem with the contend of the parsed ICAL file
 * @see AppointmentSQLInterface AppointmentSQLInterface - if you have a problem entering the parsed entry as Appointment
 * @see TasksSQLInterface TasksSQLInterface - if you have trouble entering the parsed entry as Task
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a> (changes to new interface, bugfixes, maintenance)
 */
@OXExceptionSource(classId = ImportExportExceptionClasses.ICALIMPORTER, component = EnumComponent.IMPORT_EXPORT)
@OXThrowsMultiple(category = {
    Category.PERMISSION, Category.SUBSYSTEM_OR_SERVICE_DOWN, Category.USER_INPUT, Category.USER_INPUT, Category.CODE_ERROR,
    Category.USER_INPUT, Category.USER_INPUT, Category.PERMISSION, Category.PERMISSION, Category.USER_INPUT, Category.USER_INPUT,
    Category.USER_INPUT, Category.SETUP_ERROR, Category.USER_INPUT, Category.WARNING }, desc = {
    "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" }, exceptionId = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 }, msg = {
    "Could not import into the folder %s.", "Subsystem down", "User input error %s", "Problem while reading ICal file: %s.",
    "Could not load folder %s", "Broken file uploaded: %s", "Cowardly refusing to import an entry flagged as confidential.",
    "Module Calendar not enabled for user, cannot import appointments.", "Module Tasks not enabled for user, cannot import tasks.",
    "The element %s is not supported.", "Couldn't convert object: %s", "No ICal to import found.",
    "Could not find suitable ICalParser. Is an ICalParser exported as a service?",
    "Failed importing appointment due to hard conflicting resource.", "Warnings when importing file: %i warnings" })
public class ICalImporter extends AbstractImporter {

    private static final Log LOG = LogFactory.getLog(ICalImporter.class);

    private static final ImportExportExceptionFactory EXCEPTIONS = new ImportExportExceptionFactory(ICalImporter.class);

    public boolean canImport(final ServerSession session, final Format format, final List<String> folders, final Map<String, String[]> optionalParams) throws ImportExportException {
        if (!format.equals(Format.ICAL)) {
            return false;
        }
        final OXFolderAccess folderAccess = new OXFolderAccess(session.getContext());
        final Iterator<String> iterator = folders.iterator();
        while (iterator.hasNext()) {
            final String folder = iterator.next();

            int folderId = 0;
            try {
                folderId = Integer.parseInt(folder);
            } catch (final NumberFormatException exc) {
                throw EXCEPTIONS.create(0, exc, folder);
            }

            FolderObject fo;
            try {
                fo = folderAccess.getFolderObject(folderId);
            } catch (final OXException e) {
                return false;
            }

            // check format of folder
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

            // check read access to folder
            EffectivePermission perm;
            try {
                perm = fo.getEffectiveUserPermission(session.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(
                    session.getUserId(),
                    session.getContext()));
            } catch (final DBPoolingException e) {
                throw EXCEPTIONS.create(0, e, folder);
            } catch (final SQLException e) {
                throw EXCEPTIONS.create(0, e, folder);
            }

            if (perm.canCreateObjects()) {
                return true;
            }
        }

        return false;
    }

    public List<ImportResult> importData(final ServerSession session, final Format format, final InputStream is, final List<String> folders, final Map<String, String[]> optionalParams) throws ImportExportException {
        int appointmentFolderId = -1;
        int taskFolderId = -1;

        boolean importAppointment = false;
        boolean importTask = false;
        final OXFolderAccess folderAccess = new OXFolderAccess(session.getContext());
        final Iterator<String> iterator = folders.iterator();
        while (iterator.hasNext()) {
            final int folderId = Integer.parseInt(iterator.next());
            FolderObject fo;
            try {
                fo = folderAccess.getFolderObject(folderId);
            } catch (final OXException e) {
                throw EXCEPTIONS.create(4, e, Integer.valueOf(folderId));
            }
            if (fo.getModule() == FolderObject.CALENDAR) {
                appointmentFolderId = folderId;
                importAppointment = true;
            } else if (fo.getModule() == FolderObject.TASK) {
                taskFolderId = folderId;
                importTask = true;
            } else {
                throw EXCEPTIONS.create(0, Integer.valueOf(fo.getModule()));
            }
        }

        AppointmentSQLInterface appointmentInterface = null;

        if (importAppointment) {
            if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), session.getContext()).hasCalendar()) {
                throw EXCEPTIONS.create(7, new OXPermissionException(OXPermissionException.Code.NoPermissionForModul, "Calendar"));
            }
            appointmentInterface = ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class).createAppointmentSql(session);
        }

        TasksSQLInterface taskInterface = null;

        if (importTask) {
            if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), session.getContext()).hasTask()) {
                throw EXCEPTIONS.create(8, new OXPermissionException(OXPermissionException.Code.NoPermissionForModul, "Task"));
            }
            taskInterface = new TasksSQLInterfaceImpl(session);
        }

        final ICalParser parser = ServerServiceRegistry.getInstance().getService(ICalParser.class);
        if (null == parser) {
            throw EXCEPTIONS.create(12);
        }

        final List<ImportResult> list = new ArrayList<ImportResult>();

        final Context ctx = session.getContext();
        final TimeZone defaultTz = TimeZone.getTimeZone(UserStorage.getStorageUser(session.getUserId(), ctx).getTimeZone());

        final List<ConversionError> errors = new ArrayList<ConversionError>();
        final List<ConversionWarning> warnings = new ArrayList<ConversionWarning>();

        if (importAppointment) {
            List<CalendarDataObject> appointments;
            try {
                appointments = parser.parseAppointments(is, defaultTz, ctx, errors, warnings);
            } catch (final ConversionError conversionError) {
                throw new ImportExportException(conversionError);
            }
            final Map<Integer, ConversionError> errorMap = new HashMap<Integer, ConversionError>();

            for (final ConversionError error : errors) {
                errorMap.put(Integer.valueOf(error.getIndex()), error);
            }

            final Map<Integer, List<ConversionWarning>> warningMap = new HashMap<Integer, List<ConversionWarning>>();

            for (final ConversionWarning warning : warnings) {
                List<ConversionWarning> warningList = warningMap.get(Integer.valueOf(warning.getIndex()));
                if (warningList == null) {
                    warningList = new LinkedList<ConversionWarning>();
                    warningMap.put(Integer.valueOf(warning.getIndex()), warningList);
                }
                warningList.add(warning);
            }

            int index = 0;
            final Iterator<CalendarDataObject> iter = appointments.iterator();
            while (iter.hasNext()) {
                final ImportResult importResult = new ImportResult();
                final ConversionError error = errorMap.get(Integer.valueOf(index));
                if (error != null) {
                    errorMap.remove(Integer.valueOf(index));
                    importResult.setException(new ImportExportException(error));
                } else {
                    final CalendarDataObject appointmentObj = iter.next();
                    appointmentObj.setContext(session.getContext());
                    appointmentObj.setParentFolderID(appointmentFolderId);
                    appointmentObj.setIgnoreConflicts(true);
                    // Check for possible full-time appointment
                    check4FullTime(appointmentObj);
                    try {
                        final Appointment[] conflicts = appointmentInterface.insertAppointmentObject(appointmentObj);
                        if (conflicts == null || conflicts.length == 0) {
                            importResult.setObjectId(String.valueOf(appointmentObj.getObjectID()));
                            importResult.setDate(appointmentObj.getLastModified());
                            importResult.setFolder(String.valueOf(appointmentFolderId));
                        } else {
                            importResult.setException(EXCEPTIONS.create(13));
                        }
                    } catch (final OXException e) {
                        LOG.error(e.getMessage(), e);
                        importResult.setException(e);
                    }
                    final List<ConversionWarning> warningList = warningMap.get(Integer.valueOf(index));
                    if (warningList != null) {
                        importResult.addWarnings(warningList);
                        importResult.setException(EXCEPTIONS.create(14, Integer.valueOf(warningList.size())));
                    }

                }
                importResult.setEntryNumber(index);
                list.add(importResult);
                index++;
            }
            for (final ConversionError error : errorMap.values()) {
                final ImportResult importResult = new ImportResult();
                importResult.setEntryNumber(error.getIndex());
                importResult.setException(new ImportExportException(error));
                list.add(importResult);
            }
        }
        if (importTask) {
            List<Task> tasks;
            try {
                tasks = parser.parseTasks(is, defaultTz, ctx, errors, warnings);
            } catch (final ConversionError conversionError) {
                throw new ImportExportException(conversionError);
            }
            final Map<Integer, ConversionError> errorMap = new HashMap<Integer, ConversionError>();

            for (final ConversionError error : errors) {
                errorMap.put(Integer.valueOf(error.getIndex()), error);
            }

            final Map<Integer, List<ConversionWarning>> warningMap = new HashMap<Integer, List<ConversionWarning>>();

            for (final ConversionWarning warning : warnings) {
                List<ConversionWarning> warningList = warningMap.get(Integer.valueOf(warning.getIndex()));
                if (warningList == null) {
                    warningList = new LinkedList<ConversionWarning>();
                    warningMap.put(Integer.valueOf(warning.getIndex()), warningList);
                }
                warningList.add(warning);
            }

            int index = 0;
            final Iterator<Task> iter = tasks.iterator();
            while (iter.hasNext()) {
                final ImportResult importResult = new ImportResult();
                final ConversionError error = errorMap.get(Integer.valueOf(index));
                if (error != null) {
                    errorMap.remove(Integer.valueOf(index));
                    importResult.setException(new ImportExportException(error));
                } else {
                    // IGNORE WARNINGS. Protocol doesn't allow for warnings. TODO: Verify This
                    final Task task = iter.next();
                    task.setParentFolderID(taskFolderId);
                    try {
                        taskInterface.insertTaskObject(task);
                        importResult.setObjectId(String.valueOf(task.getObjectID()));
                        importResult.setDate(task.getLastModified());
                        importResult.setFolder(String.valueOf(taskFolderId));
                    } catch (final OXException e) {
                        LOG.error(e.getMessage(), e);
                        importResult.setException(e);
                    }

                    final List<ConversionWarning> warningList = warningMap.get(Integer.valueOf(index));
                    if (warningList != null) {
                        importResult.addWarnings(warningList);
                        importResult.setException(EXCEPTIONS.create(14, Integer.valueOf(warningList.size())));
                    }
                }
                importResult.setEntryNumber(index);
                list.add(importResult);
                index++;
            }
            for (final ConversionError error : errorMap.values()) {
                final ImportResult importResult = new ImportResult();
                importResult.setEntryNumber(error.getIndex());
                importResult.setException(new ImportExportException(error));
                list.add(importResult);
            }
        }

        /*-
         * try {
            oxContainerConverter = new OXContainerConverter(session);
            final VersitDefinition def = ICalendar.definition;
            final VersitDefinition.Reader versitReader = def.getReader(is, "UTF-8");
            final VersitObject rootVersitObject = def.parseBegin(versitReader);
            if (null == rootVersitObject) {
                throw EXCEPTIONS.create(11);
            }
            boolean hasMoreObjects = true;

            while (hasMoreObjects) {
                final ImportResult importResult = new ImportResult();
                try {
                    VersitObject versitObject = null;
                    try {
                        versitObject = def.parseChild(versitReader, rootVersitObject);
                    } catch (final VersitException ve) {
                        LOG.info("Trying to import ICAL file, but:\n" + ve);
                        importResult.setException(EXCEPTIONS.create(5, ve.getMessage()));
                        importResult.setDate(new Date(System.currentTimeMillis()));
                        list.add(importResult);
                        hasMoreObjects = false;
                        break;
                    }

                    if (versitObject == null) {
                        hasMoreObjects = false;
                        break;
                    }

                    // final Property property =
                    // versitObject.getProperty("UID");

                    if ("VEVENT".equals(versitObject.name) && importAppointment) {
                        importResult.setFolder(String.valueOf(appointmentFolderId));
                        CalendarDataObject appointmentObj = null;
                        try {
                            appointmentObj = oxContainerConverter.convertAppointment(versitObject);
                        } catch (final ConverterPrivacyException e) {
                            importResult.setException(EXCEPTIONS.create(6));
                        } catch (final ConverterException x) {
                            importResult.setException(EXCEPTIONS.create(10, x.getMessage()));
                        }
                        if (appointmentObj == null) {
                            importResult.setDate(new Date());
                        } else {
                            appointmentObj.setContext(session.getContext());
                            appointmentObj.setParentFolderID(appointmentFolderId);
                            appointmentObj.setIgnoreConflicts(true);
                            appointmentInterface.insertAppointmentObject(appointmentObj);
                            importResult.setObjectId(String.valueOf(appointmentObj.getObjectID()));
                            importResult.setDate(appointmentObj.getLastModified());
                        }
                        list.add(importResult);
                    } else if ("VTODO".equals(versitObject.name) && importTask) {
                        importResult.setFolder(String.valueOf(taskFolderId));

                        Task taskObj = null;
                        try {
                            taskObj = oxContainerConverter.convertTask(versitObject);
                        } catch (final ConverterPrivacyException e) {
                            importResult.setException(EXCEPTIONS.create(6));
                        } catch (final ConverterException x) {
                            importResult.setException(EXCEPTIONS.create(10, x.getMessage()));
                        }
                        if (taskObj == null) {
                            importResult.setDate(new Date());
                        } else {
                            taskObj.setParentFolderID(taskFolderId);
                            taskInterface.insertTaskObject(taskObj);
                            importResult.setObjectId(String.valueOf(taskObj.getObjectID()));
                            importResult.setDate(taskObj.getLastModified());
                        }
                        list.add(importResult);
                    } else {
                        if ("VTODO".equals(versitObject.name)) {
                            LOG.debug("VTODO is only supported when importing tasks");
                        } else if ("VEVENT".equals(versitObject.name)) {
                            LOG.debug("VEVENT is only supported when importing appointments");
                        } else if ("VTIMEZONE".equals(versitObject.name)) {
                            LOG.debug("VTIMEZONE is not supported");
                        } else {
                            LOG.warn("invalid versit object encountered: " + versitObject.name);
                            importResult.setDate(new Date());
                            importResult.setException(EXCEPTIONS.create(9, versitObject.name));
                            list.add(importResult);
                        }
                    }
                } catch (OXException exc) {
                    LOG.error("cannot import calendar object", exc);
                    exc = handleDataTruncation(exc);
                    importResult.setException(exc);
                    list.add(importResult);
                } catch (final VersitException exc) {
                    LOG.error("cannot parse calendar object", exc);
                    importResult.setException(new OXException("cannot parse ical object", exc));
                    list.add(importResult);
                }
            }
        } catch (final IOException e) {
            throw EXCEPTIONS.create(3, e, e.getMessage());
        } catch (final ConverterException e) {
            throw EXCEPTIONS.create(1, e);
        } finally {
            if (oxContainerConverter != null) {
                oxContainerConverter.close();
            }
        } */

        return list;
    }

    /**
     * Checks if specified appointment lasts exactly one day; if so treat it as a full-time appointment through setting
     * {@link CalendarDataObject#setFullTime(boolean)} to <code>true</code>.
     * <p>
     * Moreover its start/end date is changed to match the date in UTC time zone.
     * 
     * @param appointmentObj The appointment to check
     */
    private void check4FullTime(final Appointment appointmentObj) {
        final long start = appointmentObj.getStartDate().getTime();
        final long end = appointmentObj.getEndDate().getTime();
        if (Constants.MILLI_DAY == (end - start)) {
            // Appointment exactly lasts one day; assume a full-time appointment
            appointmentObj.setFullTime(true);
            // Adjust start/end to UTC date's zero time; e.g. "13. January 2009 00:00:00 UTC"
            final TimeZone tz = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class).getTimeZone(appointmentObj.getTimezone());
            long offset = tz.getOffset(start);
            appointmentObj.setStartDate(new Date(start + offset));
            offset = tz.getOffset(end);
            appointmentObj.setEndDate(new Date(end + offset));
        }
    }

    @Override
    protected String getNameForFieldInTruncationError(final int id, final OXException oxex) {
        if (oxex.getComponent() == EnumComponent.APPOINTMENT) {
            final CalendarField field = CalendarField.getByAppointmentObjectId(id);
            if (field != null) {
                return field.getName();
            }
        } else if (oxex.getComponent() == EnumComponent.TASK) {
            final TaskField field = TaskField.getByTaskID(id);
            if (field != null) {
                return field.getName();
            }
        }
        return String.valueOf(id);

    }

}
