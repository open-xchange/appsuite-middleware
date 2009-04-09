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

package com.openexchange.ajax.writer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.groupware.tasks.Task;

/**
 * JSON writer for tasks.
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class TaskWriter extends CalendarWriter {

    private static final Log LOG = LogFactory.getLog(TaskWriter.class);

    private final TimeZone utc;

    /**
     * Initializes a new {@link TaskWriter}
     *
     * @param timeZone
     *            The user time zone
     */
    public TaskWriter(final TimeZone timeZone) {
        super(timeZone, null);
        utc = TimeZone.getTimeZone("utc");
    }

    public void writeArray(final Task taskObject, final int cols[], final JSONArray jsonArray) throws JSONException {
        final JSONArray jsonTaskArray = new JSONArray();
        for (int a = 0; a < cols.length; a++) {
            write(cols[a], taskObject, jsonTaskArray);
        }
        jsonArray.put(jsonTaskArray);
    }

    public void writeTask(final Task task, final JSONObject json) throws JSONException {
        writeCommonFields(task, json);

        writeParameter(TaskFields.TITLE, task.getTitle(), json);
        writeParameter(TaskFields.START_DATE, task.getStartDate(), json);
        writeParameter(TaskFields.END_DATE, task.getEndDate(), json);
        writeParameter(TaskFields.ACTUAL_COSTS, task.getActualCosts(), json, task.containsActualCosts());
        if (task.containsActualDuration()) {
            writeParameter(TaskFields.ACTUAL_DURATION, task.getActualDuration(), json);
        }
        writeParameter(TaskFields.NOTE, task.getNote(), json);
        writeParameter(TaskFields.AFTER_COMPLETE, task.getAfterComplete(), json);
        writeParameter(TaskFields.BILLING_INFORMATION, task.getBillingInformation(), json);
        writeParameter(TaskFields.CATEGORIES, task.getCategories(), json);
        writeParameter(TaskFields.COMPANIES, task.getCompanies(), json);
        writeParameter(TaskFields.CURRENCY, task.getCurrency(), json);
        writeParameter(TaskFields.DATE_COMPLETED, task.getDateCompleted(), json);
        if (task.containsPercentComplete()) {
            writeParameter(TaskFields.PERCENT_COMPLETED, task.getPercentComplete(), json);
        }
        if (task.containsPriority()) {
            writeParameter(TaskFields.PRIORITY, task.getPriority(), json);
        }
        if (task.containsStatus()) {
            writeParameter(TaskFields.STATUS, task.getStatus(), json);
        }
        writeParameter(TaskFields.TARGET_COSTS, task.getTargetCosts(), json, task.containsTargetCosts());
        if (task.containsTargetDuration()) {
            writeParameter(TaskFields.TARGET_DURATION, task.getTargetDuration(), json);
        }
        if (task.containsLabel()) {
            writeParameter(TaskFields.COLORLABEL, task.getLabel(), json);
        }

        writeParameter(TaskFields.TRIP_METER, task.getTripMeter(), json);
        writeParameter(TaskFields.ALARM, task.getAlarm(), timeZone, json);
        writeRecurrenceParameter(task, json);

        if (task.containsParticipants()) {
            json.put(TaskFields.PARTICIPANTS, getParticipantsAsJSONArray(task));
        }

        if (task.containsUserParticipants()) {
            json.put(TaskFields.USERS, getUsersAsJSONArray(task));
        }
        // Recurrence data
        writeParameter(TaskFields.DAY_IN_MONTH, task.getDayInMonth(), json, task.containsDayInMonth());
        writeParameter(TaskFields.DAYS, task.getDays(), json, task.containsDays());
    }

    public void write(final int field, final Task task, final JSONArray array) throws JSONException {
        final TaskFieldWriter writer = WRITER_MAP.get(Integer.valueOf(field));
        if (writer != null) {
            writer.write(task, array);
            return;
        }
        /*
         * No appropriate static writer found, write manually
         */
        switch (field) {
        case Task.CREATION_DATE:
            writeValue(task.getCreationDate(), timeZone, array);
            break;
        case Task.LAST_MODIFIED:
            writeValue(task.getLastModified(), timeZone, array);
            break;
        case Task.LAST_MODIFIED_UTC:
            writeValue(task.getLastModified(), utc, array);
            break;
        case Task.ALARM:
            writeValue(task.getAlarm(), timeZone, array);
            break;
        default:
            LOG.warn("missing field in mapping: " + field);
        }
    }

    private static interface TaskFieldWriter {
        /**
         * Writes this writer's value taken from specified task object to given
         * JSON array
         *
         * @param taskObject
         *            The task object
         * @param jsonArray
         *            The JSON array
         * @throws JSONException
         *             If writing to JSON array fails
         */
        void write(Task taskObject, JSONArray jsonArray) throws JSONException;
    }

    /*-
     * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * ++++++++++++++++++++ INITIALIZATION OF FIELD WRITERS ++++++++++++++++++++
     * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     */

    private static final Map<Integer, TaskFieldWriter> WRITER_MAP;

    static {
        final Map<Integer, TaskFieldWriter> m = new HashMap<Integer, TaskFieldWriter>(64);

        m.put(Integer.valueOf(Task.OBJECT_ID), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getObjectID(), jsonArray, taskObject.containsObjectID());
            }
        });

        m.put(Integer.valueOf(Task.CREATED_BY), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getCreatedBy(), jsonArray, taskObject.containsCreatedBy());
            }
        });

        m.put(Integer.valueOf(Task.MODIFIED_BY), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getModifiedBy(), jsonArray, taskObject.containsModifiedBy());
            }
        });

        m.put(Integer.valueOf(Task.FOLDER_ID), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getParentFolderID(), jsonArray, taskObject.containsParentFolderID());
            }
        });

        m.put(Integer.valueOf(Task.TITLE), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getTitle(), jsonArray);
            }
        });

        m.put(Integer.valueOf(Task.START_DATE), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getStartDate(), jsonArray);
            }
        });

        m.put(Integer.valueOf(Task.END_DATE), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getEndDate(), jsonArray);
            }
        });

        m.put(Integer.valueOf(Task.NOTE), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getNote(), jsonArray);
            }
        });
        m.put(Integer.valueOf(Task.ACTUAL_COSTS), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getActualCosts(), jsonArray, taskObject.containsActualCosts());
            }
        });

        m.put(Integer.valueOf(Task.ACTUAL_DURATION), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getActualDuration(), jsonArray, taskObject.containsActualDuration());
            }
        });

        m.put(Integer.valueOf(Task.BILLING_INFORMATION), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getBillingInformation(), jsonArray);
            }
        });

        m.put(Integer.valueOf(Task.CATEGORIES), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getCategories(), jsonArray);
            }
        });

        m.put(Integer.valueOf(Task.COMPANIES), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getCompanies(), jsonArray);
            }
        });

        m.put(Integer.valueOf(Task.CURRENCY), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getCurrency(), jsonArray);
            }
        });

        m.put(Integer.valueOf(Task.DATE_COMPLETED), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getDateCompleted(), jsonArray);
            }
        });

        m.put(Integer.valueOf(Task.PERCENT_COMPLETED), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getPercentComplete(), jsonArray, taskObject.containsPercentComplete());
            }
        });

        m.put(Integer.valueOf(Task.PRIORITY), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getPriority(), jsonArray, taskObject.containsPriority());
            }
        });

        m.put(Integer.valueOf(Task.STATUS), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getStatus(), jsonArray, taskObject.containsStatus());
            }
        });

        m.put(Integer.valueOf(Task.TARGET_COSTS), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getTargetCosts(), jsonArray, taskObject.containsTargetCosts());
            }
        });

        m.put(Integer.valueOf(Task.TARGET_DURATION), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getTargetDuration(), jsonArray, taskObject.containsTargetDuration());
            }
        });

        m.put(Integer.valueOf(Task.TRIP_METER), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getTripMeter(), jsonArray);
            }
        });

        m.put(Integer.valueOf(Task.RECURRENCE_TYPE), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getRecurrenceType(), jsonArray, taskObject.containsRecurrenceType());
            }
        });

        m.put(Integer.valueOf(Task.COLOR_LABEL), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getLabel(), jsonArray, taskObject.containsLabel());
            }
        });

        m.put(Integer.valueOf(Task.PARTICIPANTS), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                jsonArray.put(getParticipantsAsJSONArray(taskObject));
            }
        });

        m.put(Integer.valueOf(Task.USERS), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                jsonArray.put(getUsersAsJSONArray(taskObject));
            }
        });

        m.put(Integer.valueOf(Task.PRIVATE_FLAG), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getPrivateFlag(), jsonArray, taskObject.containsPrivateFlag());
            }
        });

        m.put(Integer.valueOf(Task.DAYS), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getDays(), jsonArray, taskObject.containsDays());
            }
        });

        m.put(Integer.valueOf(Task.DAY_IN_MONTH), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getDayInMonth(), jsonArray, taskObject.containsDayInMonth());
            }
        });

        m.put(Integer.valueOf(Task.MONTH), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getMonth(), jsonArray, taskObject.containsMonth());
            }
        });

        m.put(Integer.valueOf(Task.INTERVAL), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getInterval(), jsonArray, taskObject.containsInterval());
            }
        });

        m.put(Integer.valueOf(Task.UNTIL), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getUntil(), jsonArray, taskObject.containsUntil());
            }
        });

        m.put(Integer.valueOf(Task.RECURRENCE_COUNT), new TaskFieldWriter() {
            public void write(final Task task, final JSONArray json) throws JSONException {
                writeValue(task.getOccurrence(), json, task.containsOccurrence());
            }
        });

        m.put(Integer.valueOf(Task.NUMBER_OF_ATTACHMENTS), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getNumberOfAttachments(), jsonArray, taskObject.containsNumberOfAttachments());
            }
        });

        m.put(Integer.valueOf(Task.NUMBER_OF_LINKS), new TaskFieldWriter() {
            public void write(final Task taskObject, final JSONArray jsonArray) throws JSONException {
                writeValue(taskObject.getNumberOfLinks(), jsonArray, taskObject.containsNumberOfLinks());
            }
        });

        WRITER_MAP = Collections.unmodifiableMap(m);
    }
}
