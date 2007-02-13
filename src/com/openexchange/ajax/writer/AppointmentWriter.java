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
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * AppointmentWriter
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class AppointmentWriter extends CalendarWriter {
	
	private static final Log LOG = LogFactory.getLog(AppointmentWriter.class);
	
	public AppointmentWriter(JSONWriter jsonwriter, TimeZone timeZone) {
		this.jsonwriter = jsonwriter;
		this.timeZone = timeZone;
	}
	
	public void writeArray(AppointmentObject appointmentObj, int cols[], Date betweenStart, Date betweenEnd) throws JSONException {
		if (appointmentObj.getFullTime()) {
			if (CalendarCommonCollection.inBetween(appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime(), betweenStart.getTime(), betweenEnd.getTime())) {
				writeArray(appointmentObj, cols);
			} 
		} else {
			writeArray(appointmentObj, cols);
		}
	}
	
	public void writeArray(AppointmentObject appointmentObject, int cols[]) throws JSONException {
		jsonwriter.array();
		
		for (int a = 0; a < cols.length; a++) {
			write(cols[a], appointmentObject);
		}
		jsonwriter.endArray();
	}
	
	public void writeAppointment(AppointmentObject appointmentObject) throws JSONException {
		jsonwriter.object();
		
		writeCommonFields(appointmentObject);
		
		if (appointmentObject.containsTitle()) {
			writeParameter(AppointmentFields.TITLE, appointmentObject.getTitle());
		}
		
		boolean isFullTime = appointmentObject.getFullTime();
		
		if (isFullTime) {
			writeParameter(AppointmentFields.START_DATE, appointmentObject.getStartDate());
			writeParameter(AppointmentFields.END_DATE, appointmentObject.getEndDate());
		} else {
			if (appointmentObject.getRecurrenceType() == AppointmentObject.NO_RECURRENCE) {
				writeParameter(AppointmentFields.START_DATE, appointmentObject.getStartDate(), timeZone);
				writeParameter(AppointmentFields.END_DATE, appointmentObject.getEndDate(), timeZone);
			} else {			
				writeParameter(AppointmentFields.START_DATE, appointmentObject.getStartDate(), new Date(appointmentObject.getRecurringStart()), timeZone);
				writeParameter(AppointmentFields.END_DATE, appointmentObject.getEndDate(), new Date(appointmentObject.getRecurringStart()), timeZone);
			}
		}
		
		if (appointmentObject.containsShownAs()) {
			writeParameter(AppointmentFields.SHOW_AS, appointmentObject.getShownAs());
		}
		
		if (appointmentObject.containsLocation()) {
			writeParameter(AppointmentFields.LOCATION, appointmentObject.getLocation());
		}
		
		if (appointmentObject.containsNote()) {
			writeParameter(AppointmentFields.NOTE, appointmentObject.getNote());
		}
		
		if (appointmentObject.containsFullTime()) {
			writeParameter(AppointmentFields.FULL_TIME,  appointmentObject.getFullTime());
		}
		
		if (appointmentObject.containsCategories()) {
			writeParameter(AppointmentFields.CATEGORIES,  appointmentObject.getCategories());
		}
		
		if (appointmentObject.containsLabel()) {
			writeParameter(AppointmentFields.COLORLABEL,  appointmentObject.getLabel());
		}
		
		if (appointmentObject.containsAlarm()) {
			writeParameter(AppointmentFields.ALARM, appointmentObject.getAlarm());
		} 
		
		if (appointmentObject.containsRecurrenceType()) {
			writeRecurrenceParameter(appointmentObject);
		}

		if (appointmentObject.containsRecurrencePosition()) {
			writeParameter(AppointmentFields.RECURRENCE_POSITION, appointmentObject.getRecurrencePosition());
		}
		
		if (appointmentObject.containsParticipants()) {
			jsonwriter.key(AppointmentFields.PARTICIPANTS);
			writeParticipants(appointmentObject);
		}
		
		if (appointmentObject.containsUserParticipants()) {
			jsonwriter.key(AppointmentFields.USERS);
			writeUsers(appointmentObject);
		}
		
		if (appointmentObject.getIgnoreConflicts()) {
			writeParameter(AppointmentFields.IGNORE_CONFLICTS, true);
		}
		
		if (appointmentObject instanceof CalendarDataObject) {
                    if (((CalendarDataObject)appointmentObject).isHardConflict()) {
                        writeParameter(AppointmentFields.HARD_CONFLICT, true);
                    }
		}
		
		jsonwriter.endObject();
	}
	
	public void write(int field, AppointmentObject appointmentObject) throws JSONException {
		final boolean isFullTime = appointmentObject.getFullTime();
		
		switch (field) {
			case AppointmentObject.OBJECT_ID:
				writeValue(appointmentObject.getObjectID());
				break;
			case AppointmentObject.CREATED_BY:
				writeValue(appointmentObject.getCreatedBy());
				break;
			case AppointmentObject.CREATION_DATE:
				writeValue(appointmentObject.getCreationDate(), timeZone);
				break;
			case AppointmentObject.MODIFIED_BY:
				writeValue(appointmentObject.getModifiedBy());
				break;
			case AppointmentObject.LAST_MODIFIED:
				writeValue(appointmentObject.getLastModified(), timeZone);
				break;
			case AppointmentObject.FOLDER_ID:
				writeValue(appointmentObject.getParentFolderID());
				break;
			case AppointmentObject.TITLE:
				writeValue(appointmentObject.getTitle());
				break;
			case AppointmentObject.START_DATE:
				if (isFullTime) {
					writeValue(appointmentObject.getStartDate());
				} else {
					if (appointmentObject.getRecurrenceType() == AppointmentObject.NO_RECURRENCE) {					
						writeValue(appointmentObject.getStartDate(), timeZone);
					} else {
						writeValue(appointmentObject.getStartDate(), new Date(appointmentObject.getRecurringStart()), timeZone);
					}
				}
				break;
			case AppointmentObject.END_DATE:
				if (isFullTime) {
					writeValue(appointmentObject.getEndDate());
				} else {
					if (appointmentObject.getRecurrenceType() == AppointmentObject.NO_RECURRENCE) {					
						writeValue(appointmentObject.getEndDate(), timeZone);
					} else {
						writeValue(appointmentObject.getEndDate(), new Date(appointmentObject.getRecurringStart()), timeZone);
					}
				}
				break;
			case AppointmentObject.SHOWN_AS:
				writeValue(appointmentObject.getShownAs());
				break;
			case AppointmentObject.LOCATION:
				writeValue(appointmentObject.getLocation());
				break;
			case AppointmentObject.CATEGORIES:
				writeValue(appointmentObject.getCategories());
				break;
			case AppointmentObject.COLOR_LABEL:
				writeValue(appointmentObject.getLabel());
				break;
			case AppointmentObject.PRIVATE_FLAG:
				writeValue(appointmentObject.getPrivateFlag());
				break;
			case AppointmentObject.FULL_TIME:
				writeValue(appointmentObject.getFullTime());
				break;
			case AppointmentObject.NOTE:
				writeValue(appointmentObject.getNote());
				break;
			case AppointmentObject.RECURRENCE_TYPE:
				writeValue(appointmentObject.getRecurrenceType());
				break;
			case AppointmentObject.RECURRENCE_POSITION:
				writeValue(appointmentObject.getRecurrencePosition());
				break;
			case AppointmentObject.TIMEZONE:
				writeValue(appointmentObject.getTimezone());
				break;
			case AppointmentObject.PARTICIPANTS:
				writeParticipants(appointmentObject);
				break;
			case AppointmentObject.USERS:
				writeUsers(appointmentObject);
				break;
			default:
				LOG.warn("missing field in mapping: " + field);
		}
	}
}
