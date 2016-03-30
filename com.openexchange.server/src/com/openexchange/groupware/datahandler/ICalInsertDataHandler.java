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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataProperties;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * {@link ICalInsertDataHandler} - The data handler to insert appointments and
 * tasks parsed from an ICal input stream.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ICalInsertDataHandler extends ICalDataHandler {

    private static final String[] ARGS = { "com.openexchange.groupware.calendar.folder",
            "com.openexchange.groupware.task.folder" };

    private static final Class<?>[] TYPES = { InputStream.class };

    /**
     * Initializes a new {@link ICalInsertDataHandler}
     */
    public ICalInsertDataHandler() {
        super();
    }

    @Override
    public String[] getRequiredArguments() {
        return ARGS;
    }

    @Override
    public Class<?>[] getTypes() {
        return TYPES;
    }

    @Override
    public Object processData(final Data<? extends Object> data, final DataArguments dataArguments,
            final Session session) throws OXException {
        final int calendarFolder;
        try {
            calendarFolder = Integer.parseInt(dataArguments.get(ARGS[0]));
        } catch (final NumberFormatException e) {
            throw DataExceptionCodes.INVALID_ARGUMENT.create(ARGS[0], e, dataArguments.get(ARGS[0]));
        }
        OXException missingTaskFolderException = null;
        int taskFolder;
        try {
            taskFolder = Integer.parseInt(dataArguments.get(ARGS[1]));
        } catch (final NumberFormatException e) {
            missingTaskFolderException = DataExceptionCodes.INVALID_ARGUMENT.create(ARGS[1], e, dataArguments.get(ARGS[1]));
            taskFolder = -1;
        }

        final Confirm confirm = parseConfirmation(dataArguments);

        final Context ctx = ContextStorage.getStorageContext(session);
        final ICalParser iCalParser = ServerServiceRegistry.getInstance().getService(ICalParser.class);
        if (iCalParser == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ICalParser.class.getName());
        }
        final List<CalendarDataObject> appointments;
        final List<Task> tasks;
        final InputStreamCopy inputStreamCopy;
        {
            long size;
            try {
                size = Long.parseLong(data.getDataProperties().get(DataProperties.PROPERTY_SIZE));
            } catch (final NumberFormatException e) {
                size = 0;
            }
            inputStreamCopy = copyStream((InputStream) data.getData(), size);
        }
        try {
            /*
             * Get user time zone
             */
            final TimeZone defaultZone = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class).getTimeZone(UserStorage.getInstance().getUser(session.getUserId(), ctx)
                    .getTimeZone());
            /*
             * Errors and warnings
             */
            final List<ConversionError> conversionErrors = new ArrayList<ConversionError>(4);
            final List<ConversionWarning> conversionWarnings = new ArrayList<ConversionWarning>(4);
            {
                /*
                 * Start parsing appointments
                 */
                appointments = parseAppointmentStream(ctx, iCalParser, inputStreamCopy, conversionErrors,
                        conversionWarnings, defaultZone);
                // TODO: Handle errors/warnings
                conversionErrors.clear();
                conversionWarnings.clear();
                /*
                 * Start parsing tasks
                 */
                tasks = parseTaskStream(ctx, iCalParser, inputStreamCopy, conversionErrors, conversionWarnings,
                        defaultZone);
            }
        } catch (final IOException e) {
            throw DataExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            inputStreamCopy.close();
        }
        /*
         * The JSON array to return
         */
        final JSONArray folderAndIdArray = new JSONArray();
        if (!appointments.isEmpty()) {
            /*
             * Insert parsed appointments into denoted calendar folder
             */
            try {
                insertAppointments(session, calendarFolder, ctx, appointments, confirm, folderAndIdArray);
            } catch (final JSONException e) {
                throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            }
        }
        if (!tasks.isEmpty()) {
            if (taskFolder < 0) {
                if (null != missingTaskFolderException) {
                    throw missingTaskFolderException;
                }
                throw DataExceptionCodes.MISSING_ARGUMENT.create(ARGS[1]);
            }
            /*
             * Insert parsed tasks into denoted task folder
             */
            try {
                insertTasks(session, taskFolder, tasks, folderAndIdArray);
            } catch (final JSONException e) {
                throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
            }
        }
        return folderAndIdArray;
    }

    private Confirm parseConfirmation(final DataArguments dataArguments) {
        int confirmStatus = -1;
        String confirmMessage = null;
        if (dataArguments.containsKey("com.openexchange.groupware.calendar.confirmstatus")) {
            confirmStatus = Integer.parseInt(dataArguments.get("com.openexchange.groupware.calendar.confirmstatus"));
            if (dataArguments.containsKey("com.openexchange.groupware.calendar.confirmmessage")) {
                confirmMessage = dataArguments.get("com.openexchange.groupware.calendar.confirmmessage");
            }
            return new Confirm(confirmStatus, confirmMessage);
        }
        return null;
    }
}
