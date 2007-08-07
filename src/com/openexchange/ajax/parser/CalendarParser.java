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



package com.openexchange.ajax.parser;

import com.openexchange.groupware.container.ExternalUserParticipant;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.ParticipantsFields;
import com.openexchange.api.OXConflictException;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ExternalGroupParticipant;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.container.ResourceGroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.tools.servlet.OXJSONException;

/**
 * CalendarParser
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
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

    protected void parseElementCalendar(final CalendarObject calendarobject, final JSONObject jsonobject) throws JSONException, OXConflictException, OXJSONException {
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
			calendarobject.setDays(parseInt(jsonobject, CalendarFields.DAYS));
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
		
		if (jsonobject.has(CalendarFields.UNTIL)) {
			calendarobject.setUntil(parseDate(jsonobject, CalendarFields.UNTIL));
		}
		
		if (jsonobject.has(CalendarFields.OCCURRENCES)) {
			calendarobject.setOccurrence(parseInt(jsonobject, CalendarFields.OCCURRENCES));
		}
		
		if (jsonobject.has(CalendarFields.NOTIFICATION)) {
			calendarobject.setNotification(parseBoolean(jsonobject, CalendarFields.NOTIFICATION));
		}
		
		if (jsonobject.has(CalendarFields.CONFIRMATION)) {
			calendarobject.setConfirm(parseInt(jsonobject, CalendarFields.CONFIRMATION));
		}
		
		if (jsonobject.has(CalendarFields.CONFIRM_MESSAGE)) {
			calendarobject.setConfirmMessage(parseString(jsonobject, CalendarFields.CONFIRM_MESSAGE));
		}
		
		final Participants participants = new Participants();
		
		if (jsonobject.has(CalendarFields.PARTICIPANTS)) {
			calendarobject.setParticipants(parseParticipants(jsonobject, participants));
		}
		
		if (jsonobject.has(CalendarFields.USERS)) {
			calendarobject.setUsers(parseUsers(jsonobject, participants));
		}
		
		parseElementCommon(calendarobject, jsonobject);
	}
	
	public static Participant[] parseParticipants(final JSONObject jsonObj, final Participants participants) throws JSONException, OXConflictException, OXJSONException {
		final JSONArray jparticipants = jsonObj.getJSONArray(CalendarFields.PARTICIPANTS);
		final Participant[] participant = new Participant[jparticipants.length()];
		for (int i = 0; i < jparticipants.length(); i++) {
			final JSONObject jparticipant = jparticipants.getJSONObject(i);
			final int type = jparticipant.getInt(ParticipantsFields.TYPE);
			final int id;
            try {
                id = jparticipant.getInt(ParticipantsFields.ID);
            } catch (JSONException e) {
                throw new OXJSONException(OXJSONException.Code.JSON_READ_ERROR,
                    e, jparticipant.get(ParticipantsFields.ID));
            }
			Participant p = null;
			switch (type) {
				case Participant.USER:
					p = new UserParticipant();
					p.setIdentifier(id);
					break;
				case Participant.GROUP:
					p = new GroupParticipant();
					p.setIdentifier(id);
					break;
				case Participant.RESOURCE:
					p = new ResourceParticipant();
					p.setIdentifier(id);
					break;
				case Participant.RESOURCEGROUP:
					p = new ResourceGroupParticipant();
					p.setIdentifier(id);
					break;
				case Participant.EXTERNAL_USER:
					final String displayName = DataParser.parseString(jparticipant, ParticipantsFields.DISPLAY_NAME);
					final String mailAddress = DataParser.parseString(jparticipant, ParticipantsFields.MAIL);
					
					p = new ExternalUserParticipant();
					// p.setIdentifier(id);
					p.setDisplayName(displayName);
					p.setEmailAddress(mailAddress);
					
					break;
				case Participant.EXTERNAL_GROUP:
					p = new ExternalGroupParticipant();
					p.setIdentifier(id);
					break;
				default:
					throw new OXConflictException("invalid type");
			}
			participant[i] = p;
		}
		
		return participant;
	}
	
	public static UserParticipant[] parseUsers(final JSONObject jsonObj, final Participants participants) throws JSONException {
		final JSONArray jusers = jsonObj.getJSONArray(CalendarFields.USERS);
		for (int i = 0; i < jusers.length(); i++) {
			final UserParticipant user = new UserParticipant();
			final JSONObject jUser = jusers.getJSONObject(i);
			user.setIdentifier(jUser.getInt(ParticipantsFields.ID));
			if (jUser.has(CalendarFields.CONFIRMATION)) {
				user.setConfirm(jUser.getInt(CalendarFields.CONFIRMATION));
			}
			
			if (jUser.has(CalendarFields.ALARM)) {
				user.setAlarmDate(new Date(jUser.getLong(CalendarFields.ALARM)));
			}
			
			participants.add(user);
		}
		
		return participants.getUsers();
	}
	
	public static int parseRecurrenceType(final String value) throws OXConflictException {
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
			throw new OXConflictException("unknown value in " + CalendarFields.RECURRENCE_TYPE + ": " + value);
		}
	}
}




