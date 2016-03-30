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

package com.openexchange.ajax.parser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.fields.ParticipantsFields;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ExternalGroupParticipant;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.container.ResourceGroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * CalendarParser
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class CalendarParser extends CommonParser {

    protected CalendarParser() {
        super();
    }

    protected CalendarParser(final TimeZone timeZone) {
        super(timeZone);
    }

    protected CalendarParser(final boolean parseAll, final TimeZone timeZone) {
        super(parseAll, timeZone);
    }

    protected void parseElementCalendar(final CalendarObject calendarobject, final JSONObject jsonobject) throws JSONException, OXException {
        if (jsonobject.has(CalendarFields.TITLE)) {
            calendarobject.setTitle(parseString(jsonobject, CalendarFields.TITLE));
        }

        if (jsonobject.has(CalendarFields.NOTE)) {
            calendarobject.setNote(parseString(jsonobject, CalendarFields.NOTE));
        }

        if (jsonobject.has(CalendarFields.RECURRENCE_ID)) {
            calendarobject.setRecurrenceID(parseInt(jsonobject, CalendarFields.RECURRENCE_ID));
        }

        if (jsonobject.has(CalendarFields.RECURRENCE_POSITION)) {
            calendarobject.setRecurrencePosition(parseInt(jsonobject, CalendarFields.RECURRENCE_POSITION));
        }

        if (jsonobject.has(CalendarFields.RECURRENCE_DATE_POSITION)) {
            calendarobject.setRecurrenceDatePosition(parseDate(jsonobject, CalendarFields.RECURRENCE_DATE_POSITION));
        }

        if (jsonobject.has(CalendarFields.RECURRENCE_TYPE)) {
            calendarobject.setRecurrenceType(parseInt(jsonobject, CalendarFields.RECURRENCE_TYPE));
        }

        if (jsonobject.has(CalendarFields.DAYS)) {
            final int days = parseInt(jsonobject, CalendarFields.DAYS);
                calendarobject.setDays(days);
        }

        if (jsonobject.has(CalendarFields.DAY_IN_MONTH)) {
            int dayInMonth = parseInt(jsonobject, CalendarFields.DAY_IN_MONTH);
            if (dayInMonth == -1) {
                dayInMonth = 5;
            }
            calendarobject.setDayInMonth(dayInMonth);
        }

        if (jsonobject.has(CalendarFields.MONTH)) {
            calendarobject.setMonth(parseInt(jsonobject, CalendarFields.MONTH));
        }

        if (jsonobject.has(CalendarFields.INTERVAL)) {
            calendarobject.setInterval(parseInt(jsonobject, CalendarFields.INTERVAL));
        }

        if (jsonobject.has(CalendarFields.UNTIL) && jsonobject.has(CalendarFields.OCCURRENCES)) {
            if(jsonobject.isNull(CalendarFields.UNTIL) != (jsonobject.isNull(CalendarFields.OCCURRENCES) || Integer.parseInt(jsonobject.getString(CalendarFields.OCCURRENCES)) == 0)) {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create("Illegal combination of until and occurrences value.");
            }
        }

        if (jsonobject.has(CalendarFields.UNTIL)) {
            if (jsonobject.isNull(CalendarFields.UNTIL)) {
                calendarobject.setUntil(null);
            } else {
                calendarobject.setUntil(parseDate(jsonobject, CalendarFields.UNTIL));
            }
        }

        if (jsonobject.has(CalendarFields.OCCURRENCES)) {
            if (jsonobject.isNull(CalendarFields.OCCURRENCES)) {
                calendarobject.setOccurrence(0);
            } else {
                calendarobject.setOccurrence(parseInt(jsonobject, CalendarFields.OCCURRENCES));
            }
        }

        if (jsonobject.has(CalendarFields.NOTIFICATION)) {
            calendarobject.setNotification(parseBoolean(jsonobject, CalendarFields.NOTIFICATION));
        }

        if (jsonobject.has(ParticipantsFields.CONFIRMATION)) {
            calendarobject.setConfirm(parseInt(jsonobject, ParticipantsFields.CONFIRMATION));
        }

        if (jsonobject.has(ParticipantsFields.CONFIRM_MESSAGE)) {
            calendarobject.setConfirmMessage(parseString(jsonobject, ParticipantsFields.CONFIRM_MESSAGE));
        }

        final Participants participants = new Participants();

        if (jsonobject.has(CalendarFields.PARTICIPANTS)) {
            calendarobject.setParticipants(parseParticipants(jsonobject, participants));
        }

        if (jsonobject.has(CalendarFields.USERS)) {
            calendarobject.setUsers(parseUsers(jsonobject, participants));
        }

        if (jsonobject.has(CalendarFields.ORGANIZER)) {
            calendarobject.setOrganizer(parseString(jsonobject, CalendarFields.ORGANIZER));
        }

        if (jsonobject.has(CommonFields.UID)) {
            calendarobject.setUid(parseString(jsonobject, CommonFields.UID));
        }

        if (jsonobject.has(CalendarFields.SEQUENCE)) {
            calendarobject.setSequence(parseInt(jsonobject, CalendarFields.SEQUENCE));
        }

        if (jsonobject.has(CalendarFields.ORGANIZER_ID)) {
            calendarobject.setOrganizerId(parseInt(jsonobject, CalendarFields.ORGANIZER_ID));
        }

        if (jsonobject.has(CalendarFields.PRINCIPAL)) {
            calendarobject.setPrincipal(parseString(jsonobject, CalendarFields.PRINCIPAL));
        }

        if (jsonobject.has(CalendarFields.PRINCIPAL_ID)) {
            calendarobject.setPrincipalId(parseInt(jsonobject, CalendarFields.PRINCIPAL_ID));
        }

        if (jsonobject.has(AppointmentFields.FULL_TIME)) {
            calendarobject.setFullTime(parseBoolean(jsonobject, CalendarFields.FULL_TIME));
        }

        parseField(parseAll, calendarobject, timeZone, jsonobject);
        parseElementCommon(calendarobject, jsonobject);
    }

    public static Participant[] parseParticipants(final JSONObject jsonObj, final Participants participants) throws JSONException, OXException {
        final JSONArray jparticipants = jsonObj.getJSONArray(CalendarFields.PARTICIPANTS);
        final Participant[] participant = new Participant[jparticipants.length()];
        for (int i = 0; i < jparticipants.length(); i++) {
            final JSONObject jparticipant = jparticipants.getJSONObject(i);
            final int type = jparticipant.getInt(ParticipantsFields.TYPE);

            int id;
            try {
                id = jparticipant.getInt(ParticipantsFields.ID);
            } catch (final JSONException e) {
                id = Participant.NO_ID;
            }
            Participant p = null;
            switch (type) {
                case Participant.USER:
                    if (Participant.NO_ID == id) {
                        throw OXJSONExceptionCodes.JSON_READ_ERROR.create(
                            jparticipant.get(ParticipantsFields.ID));
                    }
                    p = new UserParticipant(id);
                    break;
                case Participant.GROUP:
                    if (Participant.NO_ID == id) {
                        throw OXJSONExceptionCodes.JSON_READ_ERROR.create(
                            jparticipant.get(ParticipantsFields.ID));
                    }
                    p = new GroupParticipant(id);
                    final String groupDisplayName = DataParser.parseString(jparticipant, ParticipantsFields.DISPLAY_NAME);
                    p.setDisplayName(groupDisplayName);
                    break;
                case Participant.RESOURCE:
                    if (Participant.NO_ID == id) {
                        throw OXJSONExceptionCodes.JSON_READ_ERROR.create(
                            jparticipant.get(ParticipantsFields.ID));
                    }
                    p = new ResourceParticipant(id);
                    break;
                case Participant.RESOURCEGROUP:
                    if (Participant.NO_ID == id) {
                        throw OXJSONExceptionCodes.JSON_READ_ERROR.create(
                            jparticipant.get(ParticipantsFields.ID));
                    }
                    p = new ResourceGroupParticipant(id);
                    break;
                case Participant.EXTERNAL_USER:
                    final String displayName = DataParser.parseString(jparticipant, ParticipantsFields.DISPLAY_NAME);
                    final String mailAddress = DataParser.parseString(jparticipant, ParticipantsFields.MAIL);

                    p = new ExternalUserParticipant(mailAddress);
                    p.setDisplayName(displayName);

                    break;
                case Participant.EXTERNAL_GROUP:
                    p = new ExternalGroupParticipant();
                    p.setIdentifier(id);
                    break;
                default:
                    throw AjaxExceptionCodes.UNEXPECTED_ERROR.create("invalid type");
            }
            participant[i] = p;
        }

        return participant;
    }

    public static UserParticipant[] parseUsers(final JSONObject jsonObj, final Participants participants) throws JSONException {
        final JSONArray jusers = jsonObj.getJSONArray(CalendarFields.USERS);
        for (int i = 0; i < jusers.length(); i++) {
            final JSONObject jUser = jusers.getJSONObject(i);
            final UserParticipant user = new UserParticipant(jUser.getInt(ParticipantsFields.ID));
            if (jUser.has(ParticipantsFields.CONFIRMATION)) {
                user.setConfirm(jUser.getInt(ParticipantsFields.CONFIRMATION));
            }
            if (jUser.has(ParticipantsFields.CONFIRM_MESSAGE)) {
                user.setConfirmMessage(jUser.getString(ParticipantsFields.CONFIRM_MESSAGE));
            }

            if (jUser.has(CalendarFields.ALARM)) {
                user.setAlarmDate(new Date(jUser.getLong(CalendarFields.ALARM)));
            }

            participants.add(user);
        }

        return participants.getUsers();
    }

    public static int parseRecurrenceType(final String value) throws OXException {
        if ("none".equals(value)) {
            return CalendarObject.NONE;
        } else if ("daily".equals(value)) {
            return CalendarObject.DAILY;
        } else if ("weekly".equals(value)) {
            return CalendarObject.WEEKLY;
        } else if ("monthly".equals(value)) {
            return CalendarObject.MONTHLY;
        } else if ("yearly".equals(value)) {
            return CalendarObject.YEARLY;
        } else {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create("unknown value in " + CalendarFields.RECURRENCE_TYPE + ": " + value);
        }
    }

    protected void parseField(final boolean parseAlles, final CalendarObject obj, final TimeZone tz, final JSONObject json) throws OXException {
        for (final FieldParser<CalendarObject> parser : PARSERS) {
            try {
                parser.parse(parseAlles, obj, tz, json);
            } catch (final JSONException e) {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, e.getMessage());
            }
        }
    }

    static interface FieldParser<T> {
        void parse(boolean parseAll, T obj, TimeZone timeZone, JSONObject json) throws JSONException;
    }

    private static final FieldParser<CalendarObject> CONFIRMATIONS_PARSER = new FieldParser<CalendarObject>() {
        @Override
        public void parse(final boolean parseAll, final CalendarObject obj, final TimeZone timeZone, final JSONObject json) throws JSONException {
            if (!parseAll) {
                return;
            }
            final JSONArray confirmations = json.optJSONArray(CalendarFields.CONFIRMATIONS);
            if (null == confirmations) {
                return;
            }
            final ParticipantParser parser = new ParticipantParser();
            final List<ConfirmableParticipant> participants = new ArrayList<ConfirmableParticipant>(confirmations.length());
            for (int i = 0; i < confirmations.length(); i++) {
                final JSONObject jConfirmation = confirmations.optJSONObject(i);
                if (null == jConfirmation) {
                    continue;
                }
                participants.add(parser.parseConfirmation(parseAll, jConfirmation));
            }
            obj.setConfirmations(participants.toArray(new ConfirmableParticipant[participants.size()]));
        }
    };

    @SuppressWarnings("unchecked")
    private static final FieldParser<CalendarObject>[] PARSERS = new FieldParser[] { CONFIRMATIONS_PARSER };
}
