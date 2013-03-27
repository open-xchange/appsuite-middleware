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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.logging.Log;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.helpers.SizedInputStream;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.java.Charsets;
import com.openexchange.log.LogFactory;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a> (minor: changes to new interface; fixes)
 */
public class ICalExporter implements Exporter {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ICalExporter.class));
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
    public SizedInputStream exportData(final ServerSession sessObj, final Format format, final String folder, int[] fieldsToBeExported, final Map<String, Object> optionalParams) throws OXException {
        final Context ctx = ContextStorage.getInstance().getContext(sessObj.getContextId());
        final User user = UserStorage.getInstance().getUser(sessObj.getUserId(), ctx);
        String icalText;
        try {
            final ICalEmitter emitter = ImportExportServices.getICalEmitter();
            final FolderObject fo;
            try {
                fo = new OXFolderAccess(sessObj.getContext()).getFolderObject(Integer.parseInt(folder));
            } catch (final OXException e) {
                throw ImportExportExceptionCodes.LOADING_FOLDER_FAILED.create(e, folder);
            }
            if (fo.getModule() == FolderObject.CALENDAR) {
                if (fieldsToBeExported == null) {
                    fieldsToBeExported = _appointmentFields;
                }

                final AppointmentSQLInterface appointmentSql = ImportExportServices.getAppointmentFactoryService().createAppointmentSql(sessObj);
                final CalendarCollectionService recColl = ImportExportServices.getCalendarCollectionService();
                final SearchIterator<Appointment> searchIterator = appointmentSql.getModifiedAppointmentsInFolder(Integer.parseInt(folder), fieldsToBeExported, DATE_ZERO);
                final List<Appointment> appointments = new LinkedList<Appointment>();
                try {
                    while (searchIterator.hasNext()) {
                        final Appointment appointment = searchIterator.next();
                        if (CalendarObject.NO_RECURRENCE != appointment.getRecurrenceType()) {
                            if (!appointment.containsTimezone()) {
                                appointment.setTimezone(user.getTimeZone());
                            }
                            recColl.replaceDatesWithFirstOccurence(appointment);
                            //appointments need a UID to ensure that exceptions can be associated with them.
                            if(appointment.getUid() == null){
                            	appointment.setUid(UUID.randomUUID().toString());
                            }
                        }
                        appointments.add(appointment);
                       }
                    final List<ConversionError> errors = new LinkedList<ConversionError>();
                    final List<ConversionWarning> warnings = new LinkedList<ConversionWarning>();
                    icalText = emitter.writeAppointments(appointments, sessObj.getContext(), errors, warnings);
                    log(errors, warnings);
                } finally {
                    try {
                        searchIterator.close();
                    } catch (final SearchIteratorException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }

            } else if (fo.getModule() == FolderObject.TASK) {
                if (fieldsToBeExported == null) {
                    fieldsToBeExported = _taskFields;
                }

                final TasksSQLInterface taskSql = new TasksSQLImpl(sessObj);
                final SearchIterator<Task> searchIterator = taskSql.getModifiedTasksInFolder(Integer.parseInt(folder), fieldsToBeExported, DATE_ZERO);
                final List<Task> tasks = new LinkedList<Task>();
                try {
                    while (searchIterator.hasNext()) {
                        tasks.add(searchIterator.next());
                    }
                    final List<ConversionError> errors = new LinkedList<ConversionError>();
                    final List<ConversionWarning> warnings = new LinkedList<ConversionWarning>();
                    icalText = emitter.writeTasks(tasks, errors, warnings, sessObj.getContext());
                    log(errors, warnings);
                } finally {
                    try {
                        searchIterator.close();
                    } catch (final SearchIteratorException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            } else {
                throw ImportExportExceptionCodes.CANNOT_EXPORT.create(folder, format);
            }
        } catch (final NumberFormatException e) {
            throw ImportExportExceptionCodes.NUMBER_FAILED.create(e, folder);
        } catch (final ConversionError e) {
            throw ImportExportExceptionCodes.ICAL_CONVERSION_FAILED.create(e);
        }
        final byte[] bytes = Charsets.getBytes(icalText, Charsets.UTF_8);
        return new SizedInputStream(
                new UnsynchronizedByteArrayInputStream(bytes),
                bytes.length,
                Format.ICAL);
    }

    private void log(final List<ConversionError> errors, final List<ConversionWarning> warnings) {
        for(final ConversionError error : errors) {
            LOG.warn(error.getMessage());
        }

        for(final ConversionWarning warning : warnings) {
            LOG.warn(warning.getMessage());
        }
    }

    @Override
    public SizedInputStream exportData(final ServerSession sessObj, final Format format, final String folder, final int objectId, final int[] fieldsToBeExported, final Map<String, Object> optionalParams) throws OXException {
        final ByteArrayOutputStream byteArrayOutputStream = new UnsynchronizedByteArrayOutputStream();
        try {
            final VersitDefinition versitDefinition = Versit.getDefinition("text/calendar");
            final VersitDefinition.Writer versitWriter = versitDefinition.getWriter(byteArrayOutputStream, "UTF-8");
            final VersitObject versitObjectContainer = OXContainerConverter.newCalendar("2.0");
            versitDefinition.writeProperties(versitWriter, versitObjectContainer);
            final VersitDefinition eventDef = versitDefinition.getChildDef("VEVENT");
            final VersitDefinition taskDef = versitDefinition.getChildDef("VTODO");
            final OXContainerConverter oxContainerConverter = new OXContainerConverter(sessObj);

            FolderObject fo;
            try {
                fo = new OXFolderAccess(sessObj.getContext()).getFolderObject(Integer.parseInt(folder));
            } catch (final OXException e) {
                throw ImportExportExceptionCodes.LOADING_FOLDER_FAILED.create(e, folder);
            }
            if (fo.getModule() == FolderObject.CALENDAR) {
                final AppointmentSQLInterface appointmentSql = ImportExportServices.getAppointmentFactoryService().createAppointmentSql(sessObj);
                final Appointment appointmentObj = appointmentSql.getObjectById(objectId, Integer.parseInt(folder));
                try {
                    exportAppointment(oxContainerConverter, eventDef, versitWriter, appointmentObj);
                    versitDefinition.writeEnd(versitWriter, versitObjectContainer);
                } catch (final Exception e) {
                    LOG.error("Unexpected exception.", e);
                } finally {
                    closeVersitResources(oxContainerConverter, versitWriter);
                }
            } else if (fo.getModule() == FolderObject.TASK) {
                final TasksSQLInterface taskSql = new TasksSQLImpl(sessObj);
                final Task taskObj = taskSql.getTaskById(objectId, Integer.parseInt(folder));
                try {
                    exportTask(oxContainerConverter, taskDef, versitWriter, taskObj);
                    versitDefinition.writeEnd(versitWriter, versitObjectContainer);
                } finally {
                    closeVersitResources(oxContainerConverter, versitWriter);
                }
            } else {
                throw ImportExportExceptionCodes.CANNOT_EXPORT.create(folder, format);
            }

            versitWriter.flush();
        } catch (final NumberFormatException e) {
            throw ImportExportExceptionCodes.NUMBER_FAILED.create(e, folder);
        } catch (final IOException e) {
            throw ImportExportExceptionCodes.ICAL_CONVERSION_FAILED.create(e);
        } catch (final ConverterException e) {
            throw ImportExportExceptionCodes.ICAL_CONVERSION_FAILED.create(e);
        } catch (final SQLException e) {
            throw ImportExportExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        }

        return new SizedInputStream(
                new UnsynchronizedByteArrayInputStream(byteArrayOutputStream.toByteArray()),
                byteArrayOutputStream.size(),
                Format.ICAL);
    }

    protected void exportAppointment(final OXContainerConverter oxContainerConverter, final VersitDefinition versitDef, final VersitDefinition.Writer writer, final Appointment appointmentObj) throws ConverterException, IOException {
        final VersitObject versitObject = oxContainerConverter.convertAppointment(appointmentObj);
        versitDef.write(writer, versitObject);
        writer.flush();
    }

    protected void exportTask(final OXContainerConverter oxContainerConverter, final VersitDefinition versitDef, final VersitDefinition.Writer writer, final Task taskObj) throws ConverterException, IOException {
        final VersitObject versitObject = oxContainerConverter.convertTask(taskObj);
        versitDef.write(writer, versitObject);
        writer.flush();
    }

    private static void closeVersitResources(final OXContainerConverter oxContainerConverter, final VersitDefinition.Writer versitWriter) {
        if (oxContainerConverter != null) {
            oxContainerConverter.close();
        }
        if (versitWriter != null) {
            try {
                versitWriter.close();
            } catch (final IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
}
