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

import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.groupware.calendar.CalendarCommonCollection;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.AppointmentObject;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * AppointmentWriter
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class AppointmentWriter extends CalendarWriter {
	
	private static final Log LOG = LogFactory.getLog(AppointmentWriter.class);
	
	public AppointmentWriter(TimeZone timeZone) {
		this.timeZone = timeZone;
	}
	
	public void writeArray(final AppointmentObject appointmentObj, final int cols[], final Date betweenStart, final Date betweenEnd, final JSONArray jsonArray) throws JSONException {
		if (appointmentObj.getFullTime()) {
			if (CalendarCommonCollection.inBetween(appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime(), betweenStart.getTime(), betweenEnd.getTime())) {
				writeArray(appointmentObj, cols, jsonArray);
			}
		} else {
			writeArray(appointmentObj, cols, jsonArray);
		}
	}
	
	public void writeArray(final AppointmentObject appointmentObject, final int cols[], final JSONArray jsonArray) throws JSONException {
		final JSONArray jsonAppointmentArray = new JSONArray();
		for (int a = 0; a < cols.length; a++) {
			write(cols[a], appointmentObject, jsonAppointmentArray);
		}
		jsonArray.put(jsonAppointmentArray);
	}
	
	public void writeAppointment(final AppointmentObject appointmentObject, final JSONObject jsonObj) throws JSONException {
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
				writeParameter(AppointmentFields.START_DATE, appointmentObject.getStartDate(), appointmentObject.getStartDate(), timeZone, jsonObj);
				writeParameter(AppointmentFields.END_DATE, appointmentObject.getEndDate(), appointmentObject.getEndDate(), timeZone, jsonObj);
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
			writeParameter(AppointmentFields.FULL_TIME,  appointmentObject.getFullTime(), jsonObj);
		}
		
		if (appointmentObject.containsCategories()) {
			writeParameter(AppointmentFields.CATEGORIES,  appointmentObject.getCategories(), jsonObj);
		}
		
		if (appointmentObject.containsLabel()) {
			writeParameter(AppointmentFields.COLORLABEL,  appointmentObject.getLabel(), jsonObj);
		}
		
		if (appointmentObject.containsAlarm()) {
			writeParameter(AppointmentFields.ALARM, appointmentObject.getAlarm(), jsonObj);
		}
		
		if (appointmentObject.containsRecurrenceType()) {
			writeRecurrenceParameter(appointmentObject, jsonObj);
		}
		
		if (appointmentObject.containsRecurrencePosition()) {
			writeParameter(AppointmentFields.RECURRENCE_POSITION, appointmentObject.getRecurrencePosition(), jsonObj);
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
		
		if (appointmentObject instanceof CalendarDataObject && ((CalendarDataObject)appointmentObject).isHardConflict()) {
			writeParameter(AppointmentFields.HARD_CONFLICT, true, jsonObj);
		}
	}
	
	public void write(final int field, final AppointmentObject appointmentObject, final JSONArray jsonArray) throws JSONException {
		final boolean isFullTime = appointmentObject.getFullTime();
		
		switch (field) {
			case AppointmentObject.OBJECT_ID:
				writeValue(appointmentObject.getObjectID(), jsonArray);
				break;
			case AppointmentObject.CREATED_BY:
				writeValue(appointmentObject.getCreatedBy(), jsonArray);
				break;
			case AppointmentObject.CREATION_DATE:
				writeValue(appointmentObject.getCreationDate(), timeZone, jsonArray);
				break;
			case AppointmentObject.MODIFIED_BY:
				writeValue(appointmentObject.getModifiedBy(), jsonArray);
				break;
			case AppointmentObject.LAST_MODIFIED:
				writeValue(appointmentObject.getLastModified(), timeZone, jsonArray);
				break;
			case AppointmentObject.FOLDER_ID:
				writeValue(appointmentObject.getParentFolderID(), jsonArray);
				break;
			case AppointmentObject.TITLE:
				writeValue(appointmentObject.getTitle(), jsonArray);
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
			case AppointmentObject.SHOWN_AS:
				writeValue(appointmentObject.getShownAs(), jsonArray);
				break;
			case AppointmentObject.LOCATION:
				writeValue(appointmentObject.getLocation(), jsonArray);
				break;
			case AppointmentObject.CATEGORIES:
				writeValue(appointmentObject.getCategories(), jsonArray);
				break;
			case AppointmentObject.COLOR_LABEL:
				writeValue(appointmentObject.getLabel(), jsonArray);
				break;
			case AppointmentObject.PRIVATE_FLAG:
				writeValue(appointmentObject.getPrivateFlag(), jsonArray);
				break;
			case AppointmentObject.FULL_TIME:
				writeValue(appointmentObject.getFullTime(), jsonArray);
				break;
			case AppointmentObject.NOTE:
				writeValue(appointmentObject.getNote(), jsonArray);
				break;
				// modification for mobility support
			case AppointmentObject.RECURRENCE_TYPE:
				writeValue(appointmentObject.getRecurrenceType(), jsonArray);
				break;
			case AppointmentObject.INTERVAL:
				writeValue(appointmentObject.getInterval(), jsonArray);
				break;
			case AppointmentObject.DAYS:
				writeValue(appointmentObject.getDays(), jsonArray);
				break;
			case AppointmentObject.DAY_IN_MONTH:
				writeValue(appointmentObject.getDayInMonth(), jsonArray);
				break;
			case AppointmentObject.MONTH:
				writeValue(appointmentObject.getMonth(), jsonArray);
				break;
			case AppointmentObject.UNTIL:
				writeValue(appointmentObject.getUntil(), jsonArray);
				break;
			case AppointmentObject.RECURRING_OCCURRENCE:
				writeValue(appointmentObject.getOccurrence(), jsonArray);
				break;
			case AppointmentObject.RECURRENCE_DATE_POSITION:
				writeValue(appointmentObject.getRecurrenceDatePosition(), jsonArray);
				break;
			case AppointmentObject.DELETE_EXCEPTIONS:
				final JSONArray jsonDeleteExceptionArray = getExceptionAsJSONArray(appointmentObject.getDeleteException());
				if (jsonDeleteExceptionArray != null) {
					jsonArray.put(jsonDeleteExceptionArray);
				} else {
					jsonArray.put(JSONObject.NULL);
				}
				break;
			case AppointmentObject.CHANGE_EXCEPTIONS:
				final JSONArray jsonChangeExceptionArray = getExceptionAsJSONArray(appointmentObject.getChangeException());
				if (jsonChangeExceptionArray != null) {
					jsonArray.put(jsonChangeExceptionArray);
				} else {
					jsonArray.put(JSONObject.NULL);
				}
				break;
				// end of modification for mobility support
			case AppointmentObject.RECURRENCE_POSITION:
				writeValue(appointmentObject.getRecurrencePosition(), jsonArray);
				break;
			case AppointmentObject.TIMEZONE:
				writeValue(appointmentObject.getTimezone(), jsonArray);
				break;
			case AppointmentObject.PARTICIPANTS:
				final JSONArray jsonParticipantArray = getParticipantsAsJSONArray(appointmentObject);
				if (jsonParticipantArray != null) {
					jsonArray.put(jsonParticipantArray);
				} else {
					jsonArray.put(JSONObject.NULL);
				}
				break;
			case AppointmentObject.USERS:
				final JSONArray jsonUserArray = getUsersAsJSONArray(appointmentObject);
				if (jsonUserArray != null) {
					jsonArray.put(jsonUserArray);
				} else {
					jsonArray.put(JSONObject.NULL);
				}
				break;
			default:
				LOG.warn("missing field in mapping: " + field);
		}
	}
}
