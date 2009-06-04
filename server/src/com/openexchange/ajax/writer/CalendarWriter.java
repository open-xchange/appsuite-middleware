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

import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.ParticipantsFields;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;

/**
 * {@link CalendarWriter} - Writer for calendar objects
 * 
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class CalendarWriter extends CommonWriter {

    /**
     * Initializes a new {@link CalendarWriter}
     * 
     * @param timeZone
     *            The user time zone
     * @param jsonWriter
     *            The JSON writer to write to
     */
    protected CalendarWriter(final TimeZone timeZone, final JSONWriter jsonWriter) {
        super(timeZone, jsonWriter);
    }

    protected static JSONArray getParticipantsAsJSONArray(final CalendarObject calendarObj) throws JSONException {
        final JSONArray jsonArray = new JSONArray();

        final Participant[] participants = calendarObj.getParticipants();
        if (participants != null) {
            for (int a = 0; a < participants.length; a++) {
                final Participant p = participants[a];
                final JSONObject jsonObj = getParticipantAsJSONObject(p);
                jsonArray.put(jsonObj);
            }
        }

        return jsonArray;
    }

    public static JSONArray getUsersAsJSONArray(final CalendarObject calendarObject) throws JSONException {
        final JSONArray jsonArray = new JSONArray();

        final UserParticipant[] users = calendarObject.getUsers();
        if (users != null) {
            for (int a = 0; a < users.length; a++) {
                final UserParticipant userParticipant = users[a];
                final JSONObject jsonObj = getUserParticipantAsJSONObject(userParticipant);
                jsonArray.put(jsonObj);
            }
        }

        return jsonArray;
    }

    public static void writeRecurrenceParameter(final CalendarObject calendarObject, final JSONObject jsonObj)
            throws JSONException {
        final int recurrenceType = calendarObject.getRecurrenceType();

        if (calendarObject.containsRecurrenceType()) {
            writeParameter(CalendarFields.RECURRENCE_TYPE, recurrenceType, jsonObj);
        }

        switch (recurrenceType) {
        case CalendarObject.NONE:
            break;
        case CalendarObject.DAILY:
            break;
        case CalendarObject.WEEKLY:
            if (calendarObject.containsDays()) {
                writeParameter(CalendarFields.DAYS, calendarObject.getDays(), jsonObj);
            }
            break;
        case CalendarObject.MONTHLY:
            if (calendarObject.containsDays()) {
                writeParameter(CalendarFields.DAYS, calendarObject.getDays(), jsonObj);
            }

            if (calendarObject.containsDayInMonth()) {
                if (calendarObject.getDays() > 0 && calendarObject.getDayInMonth() >= 5) {
                    writeParameter(CalendarFields.DAY_IN_MONTH, -1, jsonObj);
                } else {
                    writeParameter(CalendarFields.DAY_IN_MONTH, calendarObject.getDayInMonth(), jsonObj);
                }
                // int dayInMonth = calendarObject.getDayInMonth();
                // if (dayInMonth == 5) {
                // dayInMonth = -1;
                // }
                //
                // writeParameter(CalendarFields.DAY_IN_MONTH, dayInMonth,
                // jsonObj);
            }
            break;
        case CalendarObject.YEARLY:
            if (calendarObject.containsDays()) {
                writeParameter(CalendarFields.DAYS, calendarObject.getDays(), jsonObj);
            }
            if (calendarObject.containsDayInMonth()) {
                writeParameter(CalendarFields.DAY_IN_MONTH, calendarObject.getDayInMonth(), jsonObj);
            }
            if (calendarObject.containsMonth()) {
                writeParameter(CalendarFields.MONTH, calendarObject.getMonth(), jsonObj);
            }
            break;
        default:
            throw new JSONException("invalid recurrence type: " + recurrenceType);
        }

        if (calendarObject.containsInterval()) {
            writeParameter(CalendarFields.INTERVAL, calendarObject.getInterval(), jsonObj);
        }

        if (calendarObject.containsUntil()) {
            writeParameter(CalendarFields.UNTIL, calendarObject.getUntil(), jsonObj);
        }

        if (calendarObject.containsOccurrence()) {
            writeParameter(CalendarFields.OCCURRENCES, calendarObject.getOccurrence(), jsonObj);
        }
    }

    private static JSONObject getParticipantAsJSONObject(final Participant participant) throws JSONException {
        final JSONObject jsonObj = new JSONObject();
        final int id = participant.getIdentifier();
        if (Participant.NO_ID != id) {
            writeParameter(ParticipantsFields.ID, id, jsonObj);
        }
        writeParameter(ParticipantsFields.DISPLAY_NAME, participant.getDisplayName(), jsonObj);
        writeParameter(ParticipantsFields.MAIL, participant.getEmailAddress(), jsonObj);
        writeParameter(ParticipantsFields.TYPE, participant.getType(), jsonObj, participant.getType() > 0);
        if (Participant.USER == participant.getType()) {
            final UserParticipant userParticipant = (UserParticipant) participant;
            writeParameter(CalendarFields.CONFIRMATION, userParticipant.getConfirm(), jsonObj, userParticipant.containsConfirm());
            writeParameter(CalendarFields.CONFIRM_MESSAGE, userParticipant.getConfirmMessage(), jsonObj, userParticipant.containsConfirmMessage());
        }
        return jsonObj;
    }

    public static JSONObject getUserAsJSONObject(final int userId, final int status) throws JSONException {
        final JSONObject jsonObj = new JSONObject();
        writeParameter(ParticipantsFields.ID, userId, jsonObj);
        writeParameter(ParticipantsFields.STATUS, status, jsonObj);
        return jsonObj;
    }

    public static JSONArray getExceptionAsJSONArray(final Date[] dateExceptions) {
        if (dateExceptions != null) {
            final JSONArray jsonArray = new JSONArray();
            for (int a = 0; a < dateExceptions.length; a++) {
                writeValue(dateExceptions[a], jsonArray);
            }
            return jsonArray;
        }
        return null;
    }

    public static JSONObject getUserParticipantAsJSONObject(final UserParticipant userParticipant) throws JSONException {
        final JSONObject jsonObj = new JSONObject();
        writeParameter(ParticipantsFields.ID, userParticipant.getIdentifier(), jsonObj);
        writeParameter(CalendarFields.CONFIRMATION, userParticipant.getConfirm(), jsonObj);
        if (userParticipant.containsConfirmMessage()) {
            writeParameter(CalendarFields.CONFIRM_MESSAGE, userParticipant.getConfirmMessage(), jsonObj);
        }
        return jsonObj;
    }
}
