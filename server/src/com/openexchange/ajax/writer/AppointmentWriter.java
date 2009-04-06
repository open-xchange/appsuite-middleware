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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.groupware.calendar.CalendarCommonCollection;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.Tools;
import com.openexchange.groupware.container.AppointmentObject;

/**
 * {@link AppointmentWriter} - Writer for appointments
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AppointmentWriter extends CalendarWriter {

    private static final Log LOG = LogFactory.getLog(AppointmentWriter.class);

    private final TimeZone utc;

    /**
     * Initializes a new {@link AppointmentWriter}
     *
     * @param timeZone
     *            The user time zone
     */
    public AppointmentWriter(final TimeZone timeZone) {
        super(timeZone, null);
        utc = Tools.getTimeZone("utc");
    }

    public void writeArray(final AppointmentObject appointmentObj, final int cols[], final Date betweenStart,
            final Date betweenEnd, final JSONArray jsonArray) throws JSONException {
        if (appointmentObj.getFullTime() && betweenStart != null && betweenEnd != null) {
            if (CalendarCommonCollection.inBetween(appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate()
                    .getTime(), betweenStart.getTime(), betweenEnd.getTime())) {
                writeArray(appointmentObj, cols, jsonArray);
            }
        } else {
            writeArray(appointmentObj, cols, jsonArray);
        }
    }

    public void writeArray(final AppointmentObject appointmentObject, final int cols[], final JSONArray jsonArray)
            throws JSONException {
        final JSONArray jsonAppointmentArray = new JSONArray();
        for (int a = 0; a < cols.length; a++) {
            write(cols[a], appointmentObject, jsonAppointmentArray);
        }
        jsonArray.put(jsonAppointmentArray);
    }

    public void writeAppointment(final AppointmentObject appointmentObject, final JSONObject jsonObj)
            throws JSONException {
        writeCommonFields(appointmentObject, jsonObj);

        if (appointmentObject.containsTitle()) {
            writeParameter(AppointmentFields.TITLE, appointmentObject.getTitle(), jsonObj);
        }

        final boolean isFullTime = appointmentObject.getFullTime();

        if (isFullTime) {
            writeParameter(AppointmentFields.START_DATE, appointmentObject.getStartDate(), jsonObj);
            writeParameter(AppointmentFields.END_DATE, appointmentObject.getEndDate(), jsonObj);
        } else {
            if (appointmentObject.getRecurrenceType() == AppointmentObject.NO_RECURRENCE) {
                writeParameter(AppointmentFields.START_DATE, appointmentObject.getStartDate(), timeZone, jsonObj);
                writeParameter(AppointmentFields.END_DATE, appointmentObject.getEndDate(), timeZone, jsonObj);
            } else {
                writeParameter(AppointmentFields.START_DATE, appointmentObject.getStartDate(), appointmentObject
                        .getStartDate(), timeZone, jsonObj);
                writeParameter(AppointmentFields.END_DATE, appointmentObject.getEndDate(), appointmentObject
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
            writeParameter(AppointmentFields.NOTE, appointmentObject.getNote(), jsonObj);
        }

        if (appointmentObject.containsFullTime()) {
            writeParameter(AppointmentFields.FULL_TIME, appointmentObject.getFullTime(), jsonObj);
        }

        if (appointmentObject.containsCategories()) {
            writeParameter(AppointmentFields.CATEGORIES, appointmentObject.getCategories(), jsonObj);
        }

        if (appointmentObject.containsLabel()) {
            writeParameter(AppointmentFields.COLORLABEL, appointmentObject.getLabel(), jsonObj);
        }

        if (appointmentObject.containsAlarm()) {
            writeParameter(AppointmentFields.ALARM, appointmentObject.getAlarm(), jsonObj);
        }

        if (appointmentObject.containsRecurrenceType()) {
            writeRecurrenceParameter(appointmentObject, jsonObj);
        }

        if (appointmentObject.containsRecurrenceID()) {
            writeParameter(AppointmentFields.RECURRENCE_ID, appointmentObject.getRecurrenceID(), jsonObj);
        }

        if (appointmentObject.containsRecurrencePosition()) {
            writeParameter(AppointmentFields.RECURRENCE_POSITION, appointmentObject.getRecurrencePosition(), jsonObj);
        }

        if (appointmentObject.containsRecurrenceDatePosition()) {
            writeParameter(AppointmentFields.RECURRENCE_DATE_POSITION, appointmentObject.getRecurrenceDatePosition(),
                    jsonObj);
        }

        if (appointmentObject.containsParticipants()) {
            jsonObj.put(AppointmentFields.PARTICIPANTS, getParticipantsAsJSONArray(appointmentObject));
        }

        if (appointmentObject.containsUserParticipants()) {
            jsonObj.put(AppointmentFields.USERS, getUsersAsJSONArray(appointmentObject));
        }

        if (appointmentObject.getIgnoreConflicts()) {
            writeParameter(AppointmentFields.IGNORE_CONFLICTS, true, jsonObj);
        }

        if (appointmentObject.containsTimezone()) {
            writeParameter(AppointmentFields.TIMEZONE, appointmentObject.getTimezoneFallbackUTC(), jsonObj);
        }

        if (appointmentObject.containsRecurringStart()) {
            writeParameter(AppointmentFields.RECURRENCE_START, appointmentObject.getRecurringStart(), jsonObj);
        }

        if (appointmentObject instanceof CalendarDataObject && ((CalendarDataObject) appointmentObject).isHardConflict()) {
            writeParameter(AppointmentFields.HARD_CONFLICT, true, jsonObj);
        }
    }

    /**
     * Writes given appointment field's value into specified JSON array
     *
     * @param field The appointment field
     * @param appointmentObject The appointment object to take the value from
     * @param jsonArray The JSON array to put into
     * @throws JSONException If a JSON error occurs while putting into JSON array
     */
    public void write(final int field, final AppointmentObject appointmentObject, final JSONArray jsonArray)
            throws JSONException {
        final AppointmentFieldWriter writer = WRITER_MAP.get(Integer.valueOf(field));
        if (writer != null) {
            writer.write(appointmentObject, jsonArray);
            return;
        }
        /*
         * No appropriate static writer found, write manually
         */
        final boolean isFullTime = appointmentObject.getFullTime();
        switch (field) {
        case AppointmentObject.CREATION_DATE:
            writeValue(appointmentObject.getCreationDate(), timeZone, jsonArray);
            break;
        case AppointmentObject.LAST_MODIFIED:
            writeValue(appointmentObject.getLastModified(), timeZone, jsonArray);
            break;
        case AppointmentObject.LAST_MODIFIED_UTC:
            writeValue(appointmentObject.getLastModified(), utc, jsonArray);
            break;
        case AppointmentObject.START_DATE:
            if (isFullTime) {
                writeValue(appointmentObject.getStartDate(), jsonArray);
            } else {
                if (appointmentObject.getRecurrenceType() == AppointmentObject.NO_RECURRENCE) {
                    writeValue(appointmentObject.getStartDate(), timeZone, jsonArray);
                } else {
                    writeValue(appointmentObject.getStartDate(), appointmentObject.getStartDate(), timeZone, jsonArray);
                }
            }
            break;
        case AppointmentObject.END_DATE:
            if (isFullTime) {
                writeValue(appointmentObject.getEndDate(), jsonArray);
            } else {
                if (appointmentObject.getRecurrenceType() == AppointmentObject.NO_RECURRENCE) {
                    writeValue(appointmentObject.getEndDate(), timeZone, jsonArray);
                } else {
                    writeValue(appointmentObject.getEndDate(), appointmentObject.getEndDate(), timeZone, jsonArray);
                }
            }
            break;
        default:
            LOG.warn("missing field in mapping: " + field);
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
        public void write(AppointmentObject appointmentObject, JSONArray jsonArray) throws JSONException;
    }

    /*-
     * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * ++++++++++++++++++++ INITIALIZATION OF FIELD WRITERS ++++++++++++++++++++
     * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     */

    private static final Map<Integer, AppointmentFieldWriter> WRITER_MAP;

    static {
        final Map<Integer, AppointmentFieldWriter> m = new HashMap<Integer, AppointmentFieldWriter>(64);

        m.put(Integer.valueOf(AppointmentObject.OBJECT_ID), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getObjectID(), jsonArray, appointmentObject.containsObjectID());
            }
        });

        m.put(Integer.valueOf(AppointmentObject.CREATED_BY), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getCreatedBy(), jsonArray, appointmentObject.containsCreatedBy());
            }
        });

        m.put(Integer.valueOf(AppointmentObject.MODIFIED_BY), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getModifiedBy(), jsonArray, appointmentObject.containsModifiedBy());
            }
        });

        m.put(Integer.valueOf(AppointmentObject.FOLDER_ID), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getParentFolderID(), jsonArray, appointmentObject.containsParentFolderID());
            }
        });

        m.put(Integer.valueOf(AppointmentObject.TITLE), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getTitle(), jsonArray);
            }
        });

        m.put(Integer.valueOf(AppointmentObject.SHOWN_AS), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getShownAs(), jsonArray, appointmentObject.containsShownAs());
            }
        });

        m.put(Integer.valueOf(AppointmentObject.LOCATION), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getLocation(), jsonArray);
            }
        });

        m.put(Integer.valueOf(AppointmentObject.CATEGORIES), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getCategories(), jsonArray);
            }
        });

        m.put(Integer.valueOf(AppointmentObject.COLOR_LABEL), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getLabel(), jsonArray, appointmentObject.containsLabel());
            }
        });

        m.put(Integer.valueOf(AppointmentObject.PRIVATE_FLAG), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getPrivateFlag(), jsonArray, appointmentObject.containsPrivateFlag());
            }
        });

        m.put(Integer.valueOf(AppointmentObject.FULL_TIME), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getFullTime(), jsonArray, appointmentObject.containsFullTime());
            }
        });

        m.put(Integer.valueOf(AppointmentObject.NOTE), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getNote(), jsonArray);
            }
        });

        // modification for mobility support
        m.put(Integer.valueOf(AppointmentObject.RECURRENCE_ID), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getRecurrenceID(), jsonArray, appointmentObject.containsRecurrenceID());
            }
        });

        m.put(Integer.valueOf(AppointmentObject.RECURRENCE_TYPE), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getRecurrenceType(), jsonArray, appointmentObject.containsRecurrenceType());
            }
        });

        m.put(Integer.valueOf(AppointmentObject.INTERVAL), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getInterval(), jsonArray, appointmentObject.containsInterval());
            }
        });

        m.put(Integer.valueOf(AppointmentObject.DAYS), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getDays(), jsonArray, appointmentObject.containsDays());
            }
        });

        m.put(Integer.valueOf(AppointmentObject.DAY_IN_MONTH), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getDayInMonth(), jsonArray, appointmentObject.containsDayInMonth());
            }
        });

        m.put(Integer.valueOf(AppointmentObject.MONTH), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getMonth(), jsonArray, appointmentObject.containsMonth());
            }
        });

        m.put(Integer.valueOf(AppointmentObject.UNTIL), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getUntil(), jsonArray);
            }
        });

        m.put(Integer.valueOf(AppointmentObject.RECURRENCE_COUNT), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getOccurrence(), jsonArray, appointmentObject.containsOccurrence());
            }
        });

        m.put(Integer.valueOf(AppointmentObject.RECURRENCE_DATE_POSITION), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getRecurrenceDatePosition(), jsonArray);
            }
        });

        m.put(Integer.valueOf(AppointmentObject.DELETE_EXCEPTIONS), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                final JSONArray jsonDeleteExceptionArray = getExceptionAsJSONArray(appointmentObject
                        .getDeleteException());
                if (jsonDeleteExceptionArray == null) {
                    jsonArray.put(JSONObject.NULL);
                } else {
                    jsonArray.put(jsonDeleteExceptionArray);
                }
            }
        });

        m.put(Integer.valueOf(AppointmentObject.CHANGE_EXCEPTIONS), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
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

        m.put(Integer.valueOf(AppointmentObject.RECURRENCE_POSITION), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getRecurrencePosition(), jsonArray, appointmentObject
                        .containsRecurrencePosition());
            }
        });

        m.put(Integer.valueOf(AppointmentObject.TIMEZONE), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getTimezoneFallbackUTC(), jsonArray);
            }
        });

        m.put(Integer.valueOf(AppointmentObject.RECURRENCE_START), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getRecurringStart(), jsonArray, appointmentObject.containsRecurringStart());
            }
        });

        m.put(Integer.valueOf(AppointmentObject.PARTICIPANTS), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray)
                    throws JSONException {
                final JSONArray jsonParticipantArray = getParticipantsAsJSONArray(appointmentObject);
                if (jsonParticipantArray == null) {
                    jsonArray.put(JSONObject.NULL);
                } else {
                    jsonArray.put(jsonParticipantArray);
                }
            }
        });

        m.put(Integer.valueOf(AppointmentObject.USERS), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray)
                    throws JSONException {
                final JSONArray jsonUserArray = getUsersAsJSONArray(appointmentObject);
                if (jsonUserArray == null) {
                    jsonArray.put(JSONObject.NULL);
                } else {
                    jsonArray.put(jsonUserArray);
                }
            }
        });

        m.put(Integer.valueOf(AppointmentObject.NUMBER_OF_ATTACHMENTS), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getNumberOfAttachments(), jsonArray, appointmentObject
                        .containsNumberOfAttachments());
            }
        });

        m.put(Integer.valueOf(AppointmentObject.NUMBER_OF_LINKS), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getNumberOfLinks(), jsonArray, appointmentObject.containsNumberOfLinks());
            }
        });
        m.put(Integer.valueOf(AppointmentObject.ALARM), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getAlarm(), jsonArray, appointmentObject.containsAlarm());
            }
        });

        m.put(Integer.valueOf(AppointmentObject.NOTIFICATION), new AppointmentFieldWriter() {
            public void write(final AppointmentObject appointmentObject, final JSONArray jsonArray) {
                writeValue(appointmentObject.getNotification(), jsonArray, appointmentObject.getNotification());
            }
        });

        m.put(Integer.valueOf(AppointmentObject.RECURRENCE_CALCULATOR), new AppointmentFieldWriter() {

            public void write(AppointmentObject appointmentObject, JSONArray jsonArray) throws JSONException {
                writeValue(appointmentObject.getRecurrenceCalculator(), jsonArray);
            }
            
        });
        
        WRITER_MAP = Collections.unmodifiableMap(m);
    }
}
