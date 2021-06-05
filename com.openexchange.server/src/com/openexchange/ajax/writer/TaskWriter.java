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

package com.openexchange.ajax.writer;

import static com.openexchange.java.Autoboxing.I;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.util.TimeZones;
import com.openexchange.session.Session;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * JSON writer for tasks.
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class TaskWriter extends CalendarWriter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TaskWriter.class);

    private Session session;

    public TaskWriter(final TimeZone timeZone) {
        super(timeZone, null);
    }

    /**
     * Applies specified session to this writer.
     *
     * @param session The session to set
     * @return This writer with session applied
     */
    public TaskWriter setSession(final Session session) {
        this.session = session;
        return this;
    }

    public void writeArray(final Task taskObject, final int[] columns, final JSONArray jsonArray) throws JSONException {
        final JSONArray jsonTaskArray = new JSONArray();
        for (final int column : columns) {
            writeField(taskObject, column, timeZone, jsonTaskArray);
        }
        jsonArray.put(jsonTaskArray);
    }

    public void writeTask(final Task task, final JSONObject json) throws JSONException {
        super.writeFields(task, timeZone, json, session);
        writeParameter(CalendarFields.TITLE, task.getTitle(), json);
        /*
         * always write legacy start_date / end_date properties as-is
         */
        writeParameter(CalendarFields.START_DATE, task.getStartDate(), json);
        writeParameter(CalendarFields.END_DATE, task.getEndDate(), json);
        /*
         * write start_time / end_time as "Date" or "Time" depending on the full_time flag
         */
        if (task.getFullTime()) {
            writeParameter(TaskFields.START_TIME, truncateToUTCDate(task.getStartDate()), json);
            writeParameter(TaskFields.END_TIME, truncateToUTCDate(task.getEndDate()), json);
        } else {
            writeParameter(TaskFields.START_TIME, task.getStartDate(), timeZone, json);
            writeParameter(TaskFields.END_TIME, task.getEndDate(), timeZone, json);
        }
        writeParameter(TaskFields.ACTUAL_COSTS, task.getActualCosts(), json, task.containsActualCosts());
        writeParameter(TaskFields.ACTUAL_DURATION, task.getActualDuration(), json, task.containsActualDuration());
        writeParameter(CalendarFields.NOTE, task.getNote(), json);
        writeParameter(TaskFields.AFTER_COMPLETE, task.getAfterComplete(), json);
        writeParameter(TaskFields.BILLING_INFORMATION, task.getBillingInformation(), json);
        writeParameter(TaskFields.COMPANIES, task.getCompanies(), json);
        writeParameter(TaskFields.CURRENCY, task.getCurrency(), json);
        writeParameter(TaskFields.DATE_COMPLETED, task.getDateCompleted(), json);
        if (task.containsPercentComplete()) {
            writeParameter(TaskFields.PERCENT_COMPLETED, task.getPercentComplete(), json);
        }
        writeParameter(TaskFields.PRIORITY, task.getPriority(), json, task.containsPriority() && null != task.getPriority());
        if (task.containsStatus()) {
            writeParameter(TaskFields.STATUS, task.getStatus(), json);
        }
        writeParameter(TaskFields.TARGET_COSTS, task.getTargetCosts(), json, task.containsTargetCosts());
        writeParameter(TaskFields.TARGET_DURATION, task.getTargetDuration(), json, task.containsTargetDuration());
        writeParameter(TaskFields.TRIP_METER, task.getTripMeter(), json);
        writeParameter(CalendarFields.ALARM, task.getAlarm(), timeZone, json);
        writeRecurrenceParameter(task, json);
        if (task.containsParticipants()) {
            json.put(CalendarFields.PARTICIPANTS, getParticipantsAsJSONArray(task));
        }
        if (task.containsUserParticipants()) {
            json.put(CalendarFields.USERS, getUsersAsJSONArray(task));
        }
        // Recurrence data
        writeParameter(CalendarFields.DAY_IN_MONTH, task.getDayInMonth(), json, task.containsDayInMonth());
        writeParameter(CalendarFields.DAYS, task.getDays(), json, task.containsDays());
    }

    protected void writeField(final Task task, final int column, final TimeZone tz, final JSONArray json) throws JSONException {
        final TaskFieldWriter writer = WRITER_MAP.get(column);
        if (null != writer) {
            writer.write(task, json);
            return;
        } else if (super.writeField(task, column, tz, json, session)) {
            return;
        }
        // No appropriate static writer found, write manually
        switch (column) {
        case CalendarObject.ALARM:
            writeValue(task.getAlarm(), tz, json);
            break;
        case Task.START_TIME:
            if (task.getFullTime()) {
                writeValue(truncateToUTCDate(task.getStartDate()), json);
            } else {
                writeValue(task.getStartDate(), tz, json);
            }
            break;
        case Task.END_TIME:
            if (task.getFullTime()) {
                writeValue(truncateToUTCDate(task.getEndDate()), json);
            } else {
                writeValue(task.getEndDate(), tz, json);
            }
            break;
        default:
            LOG.warn("Column {} is unknown for tasks.", I(column));
        }
    }

    /**
     * Truncates the supplied date to a HTTP-API "Date"-style date, i.e. the number of milliseconds between 00:00 UTC on that date and
     * 1970-01-01 00:00 UTC.
     *
     * @param dateTime The date to truncate the time-part from
     * @return The truncated UTC date
     */
    private Date truncateToUTCDate(Date dateTime) {
        if (null == dateTime) {
            return null;
        }
        Calendar calendar = Calendar.getInstance(TimeZones.UTC);
        calendar.setTime(dateTime);
        for (int field : new int[] { Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND } ) {
            calendar.set(field, 0);
        }
        return calendar.getTime();
    }

    private static interface TaskFieldWriter {
        /**
         * Writes this writer's value taken from specified task object to given
         * JSON array
         *
         * @param task
         *            The task object
         * @param json
         *            The JSON array
         * @throws JSONException
         *             If writing to JSON array fails
         */
        void write(Task task, JSONArray json) throws JSONException;
    }

    /*-
     * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * ++++++++++++++++++++ INITIALIZATION OF FIELD WRITERS ++++++++++++++++++++
     * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     */

    private static final TIntObjectMap<TaskFieldWriter> WRITER_MAP;

    static {
        final TIntObjectMap<TaskFieldWriter> m = new TIntObjectHashMap<TaskFieldWriter>(25, 1);
        m.put(CalendarObject.TITLE, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) {
                writeValue(taskObject.getTitle(), jsonArray);
            }
        });
        m.put(CalendarObject.START_DATE, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) {
                writeValue(taskObject.getStartDate(), jsonArray);
            }
        });
        m.put(CalendarObject.END_DATE, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) {
                writeValue(taskObject.getEndDate(), jsonArray);
            }
        });
        m.put(CalendarObject.NOTE, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) {
                writeValue(taskObject.getNote(), jsonArray);
            }
        });
        m.put(Task.ACTUAL_COSTS, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) {
                writeValue(taskObject.getActualCosts(), jsonArray, taskObject.containsActualCosts());
            }
        });
        m.put(Task.ACTUAL_DURATION, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) {
                writeValue(taskObject.getActualDuration(), jsonArray, taskObject.containsActualDuration());
            }
        });
        m.put(Task.BILLING_INFORMATION, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) {
                writeValue(taskObject.getBillingInformation(), jsonArray);
            }
        });
        m.put(Task.COMPANIES, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) {
                writeValue(taskObject.getCompanies(), jsonArray);
            }
        });
        m.put(Task.CURRENCY, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) {
                writeValue(taskObject.getCurrency(), jsonArray);
            }
        });
        m.put(Task.DATE_COMPLETED, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) {
                writeValue(taskObject.getDateCompleted(), jsonArray);
            }
        });
        m.put(Task.PERCENT_COMPLETED, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) {
                writeValue(taskObject.getPercentComplete(), jsonArray, taskObject.containsPercentComplete());
            }
        });
        m.put(Task.PRIORITY, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) {
                writeValue(taskObject.getPriority(), jsonArray, taskObject.containsPriority() && null != taskObject.getPriority());
            }
        });
        m.put(Task.STATUS, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) {
                writeValue(taskObject.getStatus(), jsonArray, taskObject.containsStatus());
            }
        });
        m.put(Task.TARGET_COSTS, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) {
                writeValue(taskObject.getTargetCosts(), jsonArray, taskObject.containsTargetCosts());
            }
        });
        m.put(Task.TARGET_DURATION, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) {
                writeValue(taskObject.getTargetDuration(), jsonArray, taskObject.containsTargetDuration());
            }
        });
        m.put(Task.TRIP_METER, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) {
                writeValue(taskObject.getTripMeter(), jsonArray);
            }
        });
        m.put(CalendarObject.RECURRENCE_TYPE, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) {
                writeValue(taskObject.getRecurrenceType(), jsonArray, taskObject.containsRecurrenceType());
            }
        });
        m.put(CalendarObject.PARTICIPANTS, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                jsonArray.put(getParticipantsAsJSONArray(taskObject));
            }
        });
        m.put(CalendarObject.USERS, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                jsonArray.put(getUsersAsJSONArray(taskObject));
            }
        });
        m.put(CalendarObject.DAYS, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) {
                writeValue(taskObject.getDays(), jsonArray, taskObject.containsDays());
            }
        });
        m.put(CalendarObject.DAY_IN_MONTH, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) {
                writeValue(taskObject.getDayInMonth(), jsonArray, taskObject.containsDayInMonth());
            }
        });
        m.put(CalendarObject.MONTH, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) {
                writeValue(taskObject.getMonth(), jsonArray, taskObject.containsMonth());
            }
        });
        m.put(CalendarObject.INTERVAL, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) {
                writeValue(taskObject.getInterval(), jsonArray, taskObject.containsInterval());
            }
        });
        m.put(CalendarObject.UNTIL, new TaskFieldWriter() {
            @Override
            public void write(final Task taskObject, final JSONArray jsonArray) {
                writeValue(taskObject.getUntil(), jsonArray, taskObject.containsUntil());
            }
        });
        m.put(CalendarObject.RECURRENCE_COUNT, new TaskFieldWriter() {
            @Override
            public void write(final Task task, final JSONArray json) {
                writeValue(task.getOccurrence(), json, task.containsOccurrence());
            }
        });
        WRITER_MAP = m;
    }
}
