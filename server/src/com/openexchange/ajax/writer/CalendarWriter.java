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

import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.ParticipantsFields;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * CalendarWriter
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class CalendarWriter extends CommonWriter {
	
	protected JSONArray getParticipantsAsJSONArray(final CalendarObject calendarObj) throws JSONException {
		final JSONArray jsonArray = new JSONArray();
		
		final Participant[] participants = calendarObj.getParticipants();
		if (participants != null) {
			for (int a = 0; a < participants.length; a++) {
				final Participant p = participants[a];
				final JSONObject jsonObj = getParticipantAsJSONObject(p.getIdentifier(), p.getDisplayName(), p.getEmailAddress(), p.getType());
				jsonArray.put(jsonObj);
			}
		}
		
		return jsonArray;
	}
	
	public JSONArray getUsersAsJSONArray(final CalendarObject calendarObject) throws JSONException {
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
	
	public void writeRecurrenceParameter(final CalendarObject calendarObject, final JSONObject jsonObj) throws JSONException {
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
				writeParameter(CalendarFields.DAYS, calendarObject.getDays(), jsonObj);
				break;
			case CalendarObject.MONTHLY:
				if (calendarObject.containsDays()) {
					writeParameter(CalendarFields.DAYS, calendarObject.getDays(), jsonObj);
				}
				
				int dayInMonth = calendarObject.getDayInMonth();
				if (dayInMonth == 5) {
					dayInMonth = -1;
				}
				
				writeParameter(CalendarFields.DAY_IN_MONTH, dayInMonth, jsonObj);
				break;
			case CalendarObject.YEARLY:
				if (calendarObject.containsDays()) {
					writeParameter(CalendarFields.DAYS, calendarObject.getDays(), jsonObj);
				}
				
				writeParameter(CalendarFields.DAY_IN_MONTH, calendarObject.getDayInMonth(), jsonObj);
				writeParameter(CalendarFields.MONTH, calendarObject.getMonth(), jsonObj);
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
	
	public JSONObject getParticipantAsJSONObject(final int id, final String displayname, final String email, final int type) throws JSONException {
		final JSONObject jsonObj = new JSONObject();
		writeParameter(ParticipantsFields.ID, id, jsonObj);
		writeParameter(ParticipantsFields.DISPLAY_NAME, displayname, jsonObj);
		writeParameter(ParticipantsFields.MAIL, email, jsonObj);
		writeParameter("type", type, jsonObj);
		return jsonObj;
	}
	
	public JSONObject getUserAsJSONObject(final int userId, final int status) throws JSONException {
		final JSONObject jsonObj = new JSONObject();
		writeParameter("id", userId, jsonObj);
		writeParameter("status", status, jsonObj);
		return jsonObj;
	}
	
	public JSONArray getExceptionAsJSONArray(final Date[] dateExceptions) throws JSONException {
		if (dateExceptions != null) {
			final JSONArray jsonArray = new JSONArray();
			for (int a = 0; a < dateExceptions.length; a++) {
				writeValue(dateExceptions[a], jsonArray);
			}
			return jsonArray;
		}
		return null;
	}
	
	public JSONObject getUserParticipantAsJSONObject(final UserParticipant userParticipant) throws JSONException {
		final JSONObject jsonObj = new JSONObject();
		writeParameter("id", userParticipant.getIdentifier(), jsonObj);
		writeParameter(CalendarFields.CONFIRMATION, userParticipant.getConfirm(), jsonObj);
		if (userParticipant.containsConfirmMessage()) {
			writeParameter(CalendarFields.CONFIRM_MESSAGE, userParticipant.getConfirmMessage(), jsonObj);
		}
		return jsonObj;
	}
}
