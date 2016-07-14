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

package com.openexchange.groupware.datahandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataHandler;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.java.Streams;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link ICalDataHandler}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public abstract class ICalDataHandler implements DataHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ICalInsertDataHandler.class);

    private static final int LIMIT = 1048576;

    protected void insertTasks(final Session session, final int taskFolder, final List<Task> tasks, final JSONArray folderAndIdArray) throws OXException, JSONException {
        final TasksSQLInterface taskSql = new TasksSQLImpl(session);
        for (final Task task : tasks) {
            task.setParentFolderID(taskFolder);
            taskSql.insertTaskObject(task);
            folderAndIdArray.put(new JSONObject().put(FolderChildFields.FOLDER_ID, taskFolder).put(DataFields.ID, task.getObjectID()));
        }
    }

    protected void insertAppointments(final Session session, final int calendarFolder, final Context ctx, final List<CalendarDataObject> appointments, Confirm confirm, final JSONArray folderAndIdArray) throws OXException, JSONException, OXException {
        final AppointmentSQLInterface appointmentSql = ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class).createAppointmentSql(
            session);
        for (final CalendarDataObject appointment : appointments) {
            appointment.setContext(ctx);
            if (confirm != null) {
                setConfirmation(session, appointment, confirm);
            }
            String uid;
            int objectId = 0;
            if (appointment.containsUid() && (uid = appointment.getUid()) != null) {
                objectId = appointmentSql.resolveUid(uid);
            }

            if (objectId != 0) {
                try {
                    if (confirm == null) {
                        confirm = new Confirm(CalendarDataObject.ACCEPT, null);
                    } else {
                        updateOwnParticipantStatus(session, ctx, objectId, confirm, appointmentSql);                        
                    }
                    appointment.setObjectID(objectId);
                    appointment.setIgnoreConflicts(true);
                    updateAppointment(appointment, calendarFolder, appointmentSql);
                    folderAndIdArray.put(new JSONObject().put(FolderChildFields.FOLDER_ID, calendarFolder).put(DataFields.ID, objectId));
                } catch (final OXException e) {
                    if (e.isGeneric(Generic.NO_PERMISSION)) {
                        handleWithoutPermission(session, appointment, calendarFolder, confirm, appointmentSql);
                    } else {
                        throw e;
                    }
                }
            } else {
                appointment.setParentFolderID(calendarFolder);
                appointment.setIgnoreConflicts(true);
                appointmentSql.insertAppointmentObject(appointment);
                folderAndIdArray.put(new JSONObject().put(FolderChildFields.FOLDER_ID, calendarFolder).put(
                    DataFields.ID,
                    appointment.getObjectID()));
            }
        }
    }

    private void updateAppointment(final CalendarDataObject appointment, final int calendarFolder, final AppointmentSQLInterface appointmentSql) throws OXException {
        try {
            final CalendarDataObject loadAppointment = appointmentSql.getObjectById(appointment.getObjectID(), calendarFolder);
            if (loadAppointment.getSequence() >= appointment.getSequence()) {
                throw OXCalendarExceptionCodes.OUTDATED_SEQUENCE.create();
            }
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e);
        }

        appointmentSql.updateAppointmentObject(appointment, calendarFolder, new Date());
    }

    private void handleWithoutPermission(final Session session, final CalendarDataObject appointment, final int calendarFolder, final Confirm confirm, final AppointmentSQLInterface appointmentSql) throws OXException {
        appointment.removeUid();
        appointment.removeObjectID();
        appointment.setParentFolderID(calendarFolder);

        boolean found = false;
        if (appointment.getUsers() != null) {
            for (UserParticipant up : appointment.getUsers()) {
                if (up.getIdentifier() == session.getUserId()) {
                    up.setConfirm(confirm.getConfirm());
                    up.setConfirmMessage(confirm.getConfirmMessage());
                }
            }
        }
        if (!found) {
            UserParticipant self = new UserParticipant(session.getUserId());
            self.setConfirm(confirm.getConfirm());
            self.setConfirmMessage(confirm.getConfirmMessage());
            ArrayList<UserParticipant> ups = new ArrayList<UserParticipant>();
            ups.add(self);
            if (appointment.getUsers() != null) {
                for (UserParticipant up : appointment.getUsers()) {
                    ups.add(up);
                }
            }
            appointment.setUsers(ups);
        }

        appointmentSql.insertAppointmentObject(appointment);
    }

    private void updateOwnParticipantStatus(final Session session, final Context ctx, final int objectId, final Confirm confirm, final AppointmentSQLInterface appSql) throws OXException, OXException {
        final FolderObject calendarFolder = new OXFolderAccess(ctx).getDefaultFolder(session.getUserId(), FolderObject.CALENDAR);
        try {
            final CalendarDataObject loadedObject = appSql.getObjectById(objectId, calendarFolder.getObjectID());
            final UserParticipant[] users = loadedObject.getUsers();
            boolean found = false;
            for (final UserParticipant user : users) {
                if (user.getIdentifier() == session.getUserId()) {
                    found = true;
                    break;
                }
            }

            if (found) {
                try {
                    appSql.setUserConfirmation(objectId, calendarFolder.getObjectID(), session.getUserId(), confirm.getConfirm(), confirm.getConfirmMessage());
                } catch (final OXException e) {
                    throw DataExceptionCodes.UNABLE_TO_CHANGE_DATA.create("Error during confirmation update.");
                }
            } else {
                throw DataExceptionCodes.UNABLE_TO_CHANGE_DATA.create("Object already exists, but user is not participant.");
            }

        } catch (final SQLException e) {
            throw DataExceptionCodes.ERROR.create(e, e.getMessage());
        }
    }

    /**
     * @param appointment
     * @param confirm
     */
    private void setConfirmation(final Session session, final CalendarDataObject appointment, final Confirm confirm) {
        final Participant[] participants = appointment.getParticipants();
        if (null != participants) {
            for (final Participant participant : participants) {
                if (participant.getType() == Participant.USER) {
                    final UserParticipant user = (UserParticipant) participant;
                    if (user.getIdentifier() == session.getUserId()) {
                        user.setConfirm(confirm.getConfirm());
                        if (confirm.getConfirmMessage() != null) {
                            user.setConfirmMessage(confirm.getConfirmMessage());
                        }
                    }
                }
            }
        }
    }

    protected List<CalendarDataObject> parseAppointmentStream(final Context ctx, final ICalParser iCalParser, final InputStreamCopy inputStreamCopy, final List<ConversionError> conversionErrors, final List<ConversionWarning> conversionWarnings, final TimeZone defaultZone) throws IOException, ConversionError {
        final InputStream inputStream = inputStreamCopy.getInputStream();
        try {
            return iCalParser.parseAppointments(inputStream, defaultZone, ctx, conversionErrors, conversionWarnings);
        } finally {
            Streams.close(inputStream);
        }
    }

    protected List<Task> parseTaskStream(final Context ctx, final ICalParser iCalParser, final InputStreamCopy inputStreamCopy, final List<ConversionError> conversionErrors, final List<ConversionWarning> conversionWarnings, final TimeZone defaultZone) throws IOException, ConversionError {
        final InputStream inputStream = inputStreamCopy.getInputStream();
        try {
            return iCalParser.parseTasks(inputStream, defaultZone, ctx, conversionErrors, conversionWarnings);
        } finally {
            Streams.close(inputStream);
        }
    }

    protected static InputStreamCopy copyStream(final InputStream orig, final long size) throws OXException {
        try {
            return new InputStreamCopy(orig, (size <= 0 || size > LIMIT));
        } catch (final IOException e) {
            throw DataExceptionCodes.ERROR.create(e, e.getMessage());
        }
    }

    protected static final class InputStreamCopy {

        private static final int DEFAULT_BUF_SIZE = 0x2000;

        private static final String FILE_PREFIX = "openexchange-ical-";

        private byte[] bytes;

        private File file;

        private final long size;

        public InputStreamCopy(final InputStream orig, final boolean createFile) throws IOException {
            super();
            if (createFile) {
                size = copy2File(orig);
            } else {
                size = copy2ByteArr(orig);
            }
        }

        public InputStream getInputStream() throws IOException {
            return bytes == null ? (file == null ? null : (new BufferedInputStream(new FileInputStream(file), DEFAULT_BUF_SIZE))) : (new UnsynchronizedByteArrayInputStream(
                bytes));
        }

        public long getSize() {
            return size;
        }

        public void close() {
            if (file != null) {
                if (file.exists()) {
                    file.delete();
                }
                file = null;
            }
            if (bytes != null) {
                bytes = null;
            }
        }

        private int copy2ByteArr(final InputStream in) throws IOException {
            final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(DEFAULT_BUF_SIZE << 1);
            final byte[] bbuf = new byte[DEFAULT_BUF_SIZE];
            int len;
            while ((len = in.read(bbuf)) > 0) {
                out.write(bbuf, 0, len);
            }
            out.flush();
            this.bytes = out.toByteArray();
            return bytes.length;
        }

        private long copy2File(final InputStream in) throws IOException {
            long totalBytes = 0;
            {
                final File tmpFile = File.createTempFile(FILE_PREFIX, null, new File(
                    ServerConfig.getProperty(ServerConfig.Property.UploadDirectory)));
                tmpFile.deleteOnExit();
                OutputStream out = null;
                try {
                    out = new BufferedOutputStream(new FileOutputStream(tmpFile), DEFAULT_BUF_SIZE);
                    final byte[] bbuf = new byte[DEFAULT_BUF_SIZE];
                    int len;
                    while ((len = in.read(bbuf)) > 0) {
                        out.write(bbuf, 0, len);
                        totalBytes += len;
                    }
                    out.flush();
                } finally {
                    Streams.close(out);
                }
                file = tmpFile;
            }
            return totalBytes;
        }
    }

    /**
     * Container Class for Confirm status.
     * {@link Confirm}
     *
     * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
     */
    protected class Confirm {

        private final int confirm;

        private final String confirmMessage;

        public Confirm(final int confirm, final String confirmMessage) {
            this.confirm = confirm;
            this.confirmMessage = confirmMessage;
        }

        public int getConfirm() {
            return confirm;
        }

        public String getConfirmMessage() {
            return confirmMessage;
        }
    }

}
