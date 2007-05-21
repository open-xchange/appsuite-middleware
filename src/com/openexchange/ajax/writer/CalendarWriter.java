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
import org.json.JSONException;

/**
 * CalendarWriter
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class CalendarWriter extends CommonWriter {
	
	public void writeParticipants(CalendarObject calendarObject) throws JSONException {
		jsonwriter.array();
		
		Participant[] participants = calendarObject.getParticipants();
		if (participants != null) {
			for (int a = 0; a < participants.length; a++) {
				Participant p = participants[a];
				writeParticipant(p.getIdentifier(), p.getDisplayName(), p.getEmailAddress(), p.getType());
			}
		}
		
		jsonwriter.endArray();
	}
	
	public void writeUsers(CalendarObject calendarObject) throws JSONException {
		jsonwriter.array();
		
		UserParticipant[] users = calendarObject.getUsers();
		if (users != null) {
			for (int a = 0; a < users.length; a++) {
				UserParticipant userParticipant = users[a];
				writeParticipantUser(userParticipant);
			}
		}
		
		jsonwriter.endArray();
	}
	
	public void writeRecurrenceParameter(CalendarObject calendarObject) throws JSONException {
		int recurrenceType = calendarObject.getRecurrenceType();
		
		if (calendarObject.containsRecurrenceType()) {
			writeParameter(CalendarFields.RECURRENCE_TYPE, recurrenceType);
		}
		
		switch (recurrenceType) {
			case CalendarObject.NONE:
				break;
			case CalendarObject.DAILY:
				break;
			case CalendarObject.WEEKLY:
				writeParameter(CalendarFields.DAYS, calendarObject.getDays());
				break;
			case CalendarObject.MONTHLY:
				if (calendarObject.containsDays()) {
					writeParameter(CalendarFields.DAYS, calendarObject.getDays());
				}
				
				int dayInMonth = calendarObject.getDayInMonth();
				if (dayInMonth == 5) {
					dayInMonth = -1;
				}
				
				writeParameter(CalendarFields.DAY_IN_MONTH, dayInMonth);
				break;
			case CalendarObject.YEARLY:
				if (calendarObject.containsDays()) {
					writeParameter(CalendarFields.DAYS, calendarObject.getDays());
				}
				
				writeParameter(CalendarFields.DAY_IN_MONTH, calendarObject.getDayInMonth());
				writeParameter(CalendarFields.MONTH, calendarObject.getMonth());
				break;
			default:
				throw new JSONException("invalid recurrence type: " + recurrenceType);
		}
		
		if (calendarObject.containsInterval()) {
			writeParameter(CalendarFields.INTERVAL, calendarObject.getInterval());
		}
		
		if (calendarObject.containsUntil()) {
			writeParameter(CalendarFields.UNTIL, calendarObject.getUntil());
		}
		
		if (calendarObject.containsOccurrence()) {
			writeParameter(CalendarFields.OCCURRENCES, calendarObject.getOccurrence());
		}
	}
	
	public void writeParticipant(int id, String displayname, String email, int type) throws JSONException {
		jsonwriter.object();
		writeParameter(ParticipantsFields.ID, id);
		writeParameter(ParticipantsFields.DISPLAY_NAME, displayname);
		writeParameter(ParticipantsFields.MAIL, email);
		writeParameter("type", type);
		jsonwriter.endObject();
	}
	
	public void writeUser(int userId, int status) throws JSONException {
		jsonwriter.object();
		writeParameter("id", userId);
		writeParameter("status", status);
		jsonwriter.endObject();
	}
	
	public void writeException(Date[] dateExceptions) throws JSONException {
		if (dateExceptions != null) {
			jsonwriter.array();
			for (int a = 0; a < dateExceptions.length; a++) {
				writeValue(dateExceptions[a]);
			}
			jsonwriter.endArray();
		}
	}
	
	private void writeParticipantUser(UserParticipant userParticipant) throws JSONException {
		jsonwriter.object();
		writeParameter("id", userParticipant.getIdentifier());
		writeParameter(CalendarFields.CONFIRMATION, userParticipant.getConfirm());
		if (userParticipant.containsConfirmMessage()) {
			writeParameter(CalendarFields.CONFIRM_MESSAGE, userParticipant.getConfirmMessage());
		}
		jsonwriter.endObject();
	}
}
