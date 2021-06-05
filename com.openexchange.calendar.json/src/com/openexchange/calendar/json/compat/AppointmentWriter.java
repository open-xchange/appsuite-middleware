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

package com.openexchange.calendar.json.compat;

import static com.openexchange.java.Autoboxing.I;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.writer.CalendarWriter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * moved from com.openexchange.ajax.writer.AppointmentWriter
 *
 * {@link AppointmentWriter} - Writer for appointments
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AppointmentWriter extends CalendarWriter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AppointmentWriter.class);

    private volatile CalendarCollection calColl;

    private final boolean forTesting;

    private ServerSession session;

    /**
     * Initializes a new {@link AppointmentWriter}
     *
     * @param timeZone The user time zone
     */
    public AppointmentWriter(final TimeZone timeZone) {
        this(timeZone, false);
    }

    /** Use for testing only */
    public AppointmentWriter(final TimeZone timeZone, final boolean forTesting) {
        super(timeZone, null);
        this.forTesting = forTesting;
    }

    /**
     * Applies specified session to this writer.
     *
     * @param session The session to set
     * @return This writer with session applied
     */
    public AppointmentWriter setSession(final ServerSession session) {
        this.session = session;
        return this;
    }

    public CalendarCollection getCalendarCollectionService() {
        CalendarCollection calColl = this.calColl;
        if (null == calColl) {
            calColl = new CalendarCollection();
            this.calColl = calColl;
        }
        return calColl;
    }

    public void setCalendarCollectionService(final CalendarCollection calColl) {
        this.calColl = calColl;
    }

    public JSONArray writeArray(final Appointment appointmentObj, final int cols[], final Date betweenStart, final Date betweenEnd, final JSONArray jsonArray) throws JSONException {
        if (appointmentObj.getFullTime() && betweenStart != null && betweenEnd != null) {
            final CalendarCollection collectionService = getCalendarCollectionService();
            if (null != collectionService && collectionService.inBetween(appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime(), betweenStart.getTime(), betweenEnd.getTime())) {
                return writeArray(appointmentObj, cols, jsonArray);
            }
        } else {
            return writeArray(appointmentObj, cols, jsonArray);
        }
        return null;
    }

    public JSONArray writeArray(final Appointment appointment, final int[] columns, final JSONArray json) throws JSONException {
        final JSONArray array = new JSONArray();
        for (final int column : columns) {
            writeField(appointment, column, timeZone, array);
        }
        json.put(array);
        return array;
    }

    public void writeAppointment(final Appointment appointmentObject, final JSONObject jsonObj) throws JSONException {
        super.writeFields(appointmentObject, timeZone, jsonObj, session);
        if (appointmentObject.containsTitle()) {
            writeParameter(CalendarFields.TITLE, appointmentObject.getTitle(), jsonObj);
        }
        final boolean isFullTime = appointmentObject.getFullTime();
        if (isFullTime) {
            writeParameter(CalendarFields.START_DATE, appointmentObject.getStartDate(), jsonObj);
            writeParameter(CalendarFields.END_DATE, appointmentObject.getEndDate(), jsonObj);
        } else {
            if (appointmentObject.getRecurrenceType() == CalendarObject.NO_RECURRENCE) {
                writeParameter(CalendarFields.START_DATE, appointmentObject.getStartDate(), timeZone, jsonObj);
                writeParameter(CalendarFields.END_DATE, appointmentObject.getEndDate(), timeZone, jsonObj);
            } else {
                writeParameter(CalendarFields.START_DATE, appointmentObject.getStartDate(), appointmentObject
                        .getStartDate(), timeZone, jsonObj);
                writeParameter(CalendarFields.END_DATE, appointmentObject.getEndDate(), appointmentObject
                        .getEndDate(), timeZone, jsonObj);
            }
        }
        if (appointmentObject.containsShownAs()) {
            writeParameter(AppointmentFields.SHOW_AS, appointmentObject.getShownAs(), jsonObj);
        }
        if (appointmentObject.containsLocation()) {
            writeParameter(AppointmentFields.LOCATION, appointmentObject.getLocation(), jsonObj);
        }
        if (appointmentObject.containsNote()) {
            writeParameter(CalendarFields.NOTE, appointmentObject.getNote(), jsonObj);
        }
        if (appointmentObject.containsAlarm()) {
            writeParameter(CalendarFields.ALARM, appointmentObject.getAlarm(), jsonObj);
        } else if (appointmentObject.containsUserParticipants() && null != appointmentObject.getUsers() && null != session && isPublicFolder(appointmentObject)) {
            final int userId  = session.getUserId();
            /*
             * Check for alarm for requesting user in user participants
             */
            NextUser: for (final UserParticipant userParticipant : appointmentObject.getUsers()) {
                if (userId == userParticipant.getIdentifier() && userParticipant.getAlarmMinutes() >= 0) {
                    /*
                     * Set appropriate alarm in appointment
                     */
                    writeParameter(CalendarFields.ALARM, userParticipant.getAlarmMinutes(), jsonObj);
                    break NextUser;
                }
            }
        }
        if (appointmentObject.containsRecurrenceType()) {
            writeRecurrenceParameter(appointmentObject, jsonObj);
        } else if (!forTesting) {
            writeParameter(CalendarFields.RECURRENCE_TYPE, CalendarObject.NO_RECURRENCE, jsonObj);
        }
        if (appointmentObject.containsRecurrenceID()) {
            writeParameter(CalendarFields.RECURRENCE_ID, appointmentObject.getRecurrenceID(), jsonObj);
        }
        if (appointmentObject.containsRecurrencePosition()) {
            writeParameter(CalendarFields.RECURRENCE_POSITION, appointmentObject.getRecurrencePosition(), jsonObj);
        }
        if (appointmentObject.containsRecurrenceDatePosition()) {
            writeParameter(CalendarFields.RECURRENCE_DATE_POSITION, appointmentObject.getRecurrenceDatePosition(),
                    jsonObj);
        }
        if (appointmentObject.containsChangeExceptions() && appointmentObject.getChangeException() != null &&appointmentObject.getChangeException().length > 0) {
            jsonObj.put(CalendarFields.CHANGE_EXCEPTIONS, getExceptionAsJSONArray(appointmentObject.getChangeException()));
        }
        if (appointmentObject.containsDeleteExceptions() && appointmentObject.getDeleteException() != null && appointmentObject.getDeleteException().length > 0) {
            jsonObj.put(CalendarFields.DELETE_EXCEPTIONS,  getExceptionAsJSONArray(appointmentObject.getDeleteException()));
        }
        if (appointmentObject.containsParticipants()) {
            jsonObj.put(CalendarFields.PARTICIPANTS, getParticipantsAsJSONArray(appointmentObject));
        }
        if (appointmentObject.containsUserParticipants()) {
            jsonObj.put(CalendarFields.USERS, getUsersAsJSONArray(appointmentObject));
        }
        if (appointmentObject.getIgnoreConflicts()) {
            writeParameter(AppointmentFields.IGNORE_CONFLICTS, true, jsonObj);
        }
        if (appointmentObject.containsTimezone()) {
            writeParameter(AppointmentFields.TIMEZONE, appointmentObject.getTimezone(), jsonObj);
        }
        if (appointmentObject.containsRecurringStart()) {
            writeParameter(CalendarFields.RECURRENCE_START, appointmentObject.getRecurringStart(), jsonObj);
        }
        if (appointmentObject instanceof CalendarDataObject && ((CalendarDataObject) appointmentObject).isHardConflict()) {
            writeParameter(AppointmentFields.HARD_CONFLICT, true, jsonObj);
        }
    }

    private boolean isPublicFolder(final Appointment appointmentObject) {
        try {
            final int folderID = appointmentObject.getParentFolderID();
            return folderID > 0 && FolderObject.PUBLIC == new OXFolderAccess(session.getContext()).getFolderType(folderID);
        } catch (OXException e) {
            return false;
        }
    }

    protected void writeField(final Appointment appointment, final int column, final TimeZone tz, final JSONArray json) throws JSONException {
        final AppointmentFieldWriter writer = WRITER_MAP.get(column);
        if (null != writer) {
            writer.write(appointment, json);
            return;
        } else if (super.writeField(appointment, column, tz, json, session)) {
            return;
        }
        // No appropriate static writer found, write manually
        final boolean isFullTime = appointment.getFullTime();
        switch (column) {
        case CalendarObject.START_DATE:
            if (isFullTime) {
                writeValue(appointment.getStartDate(), json);
            } else {
                if (appointment.getRecurrenceType() == CalendarObject.NO_RECURRENCE) {
                    writeValue(appointment.getStartDate(), timeZone, json);
                } else {
                    writeValue(appointment.getStartDate(), appointment.getStartDate(), timeZone, json);
                }
            }
            break;
        case CalendarObject.END_DATE:
            if (isFullTime) {
                writeValue(appointment.getEndDate(), json);
            } else {
                if (appointment.getRecurrenceType() == CalendarObject.NO_RECURRENCE) {
                    writeValue(appointment.getEndDate(), timeZone, json);
                } else {
                    writeValue(appointment.getEndDate(), appointment.getEndDate(), timeZone, json);
                }
            }
            break;
        default:
            LOG.warn("Column {} is unknown for appointment.", I(column));
        }
    }

    private static interface AppointmentFieldWriter {
        /**
         * Writes this writer's value taken from specified appointment object to
         * given JSON array
         *
         * @param appointmentObject
         *            The appointment object
         * @param jsonArray
         *            The JSON array
         * @throws JSONException
         *             If writing to JSON array fails
         */
        public void write(Appointment appointmentObject, JSONArray jsonArray) throws JSONException;
    }

    /*-
     * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * ++++++++++++++++++++ INITIALIZATION OF FIELD WRITERS ++++++++++++++++++++
     * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     */

    private static final TIntObjectMap<AppointmentFieldWriter> WRITER_MAP;

    static {
        final TIntObjectMap<AppointmentFieldWriter> m = new TIntObjectHashMap<AppointmentFieldWriter>(24, 1);
        m.put(CalendarObject.TITLE, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getTitle(), jsonArray);
            }
        });
        m.put(Appointment.SHOWN_AS, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getShownAs(), jsonArray, appointmentObject.containsShownAs());
            }
        });
        m.put(Appointment.LOCATION, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getLocation(), jsonArray);
            }
        });
        m.put(CalendarObject.NOTE, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getNote(), jsonArray);
            }
        });
        // modification for mobility support
        m.put(CalendarObject.RECURRENCE_ID, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getRecurrenceID(), jsonArray, appointmentObject.containsRecurrenceID());
            }
        });
        m.put(CalendarObject.RECURRENCE_TYPE, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getRecurrenceType(), jsonArray, appointmentObject.containsRecurrenceType());
            }
        });
        m.put(CalendarObject.INTERVAL, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getInterval(), jsonArray, appointmentObject.containsInterval());
            }
        });
        m.put(CalendarObject.DAYS, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getDays(), jsonArray, appointmentObject.containsDays());
            }
        });
        m.put(CalendarObject.DAY_IN_MONTH, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getDayInMonth(), jsonArray, appointmentObject.containsDayInMonth());
            }
        });
        m.put(CalendarObject.MONTH, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getMonth(), jsonArray, appointmentObject.containsMonth());
            }
        });
        m.put(CalendarObject.UNTIL, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                if (appointmentObject.containsOccurrence()) {
                    jsonArray.put(JSONObject.NULL);
                } else {
                    if (appointmentObject.containsUntil()) {
                        writeValue(appointmentObject.getUntil(), jsonArray);
                    } else {
                        jsonArray.put(JSONObject.NULL);
                    }
                }
            }
        });
        m.put(CalendarObject.RECURRENCE_COUNT, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getOccurrence(), jsonArray, appointmentObject.containsOccurrence());
            }
        });
        m.put(CalendarObject.RECURRENCE_DATE_POSITION, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getRecurrenceDatePosition(), jsonArray);
            }
        });
        m.put(CalendarObject.DELETE_EXCEPTIONS, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                final JSONArray jsonDeleteExceptionArray = getExceptionAsJSONArray(appointmentObject
                        .getDeleteException());
                if (jsonDeleteExceptionArray == null) {
                    jsonArray.put(JSONObject.NULL);
                } else {
                    jsonArray.put(jsonDeleteExceptionArray);
                }
            }
        });
        m.put(CalendarObject.CHANGE_EXCEPTIONS, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                final JSONArray jsonChangeExceptionArray = getExceptionAsJSONArray(appointmentObject
                        .getChangeException());
                if (jsonChangeExceptionArray == null) {
                    jsonArray.put(JSONObject.NULL);
                } else {
                    jsonArray.put(jsonChangeExceptionArray);
                }
            }
        });
        // end of modification for mobility support
        m.put(CalendarObject.RECURRENCE_POSITION, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getRecurrencePosition(), jsonArray, appointmentObject
                        .containsRecurrencePosition());
            }
        });
        m.put(Appointment.TIMEZONE, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getTimezone(), jsonArray);
            }
        });
        m.put(Appointment.RECURRENCE_START, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getRecurringStart(), jsonArray, appointmentObject.containsRecurringStart());
            }
        });
        m.put(CalendarObject.PARTICIPANTS, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray)
                    throws JSONException {
                final JSONArray jsonParticipantArray = getParticipantsAsJSONArray(appointmentObject);
                if (jsonParticipantArray == null) {
                    jsonArray.put(JSONObject.NULL);
                } else {
                    jsonArray.put(jsonParticipantArray);
                }
            }
        });
        m.put(CalendarObject.USERS, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray)
                    throws JSONException {
                final JSONArray jsonUserArray = getUsersAsJSONArray(appointmentObject);
                if (jsonUserArray == null) {
                    jsonArray.put(JSONObject.NULL);
                } else {
                    jsonArray.put(jsonUserArray);
                }
            }
        });
        m.put(CalendarObject.ALARM, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getAlarm(), jsonArray, appointmentObject.containsAlarm());
            }
        });
        m.put(CalendarObject.NOTIFICATION, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getNotification(), jsonArray, appointmentObject.getNotification());
            }
        });
        m.put(CalendarObject.RECURRENCE_CALCULATOR, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getRecurrenceCalculator(), jsonArray);
            }
        });
        m.put(CalendarObject.ORGANIZER, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getOrganizer(), jsonArray, appointmentObject.containsOrganizer());
            }
        });
        m.put(CommonObject.UID, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getUid(), jsonArray, appointmentObject.containsUid());
            }
        });
        m.put(CalendarObject.SEQUENCE, new AppointmentFieldWriter() {
            @Override
            public void write(final Appointment appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getSequence(), jsonArray, appointmentObject.containsSequence());
            }
        });
        WRITER_MAP = m;
    }

}
