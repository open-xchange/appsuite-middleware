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

import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.groupware.tasks.Task;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * TaskWriter
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class TaskWriter extends CalendarWriter {
	
	private static final Log LOG = LogFactory.getLog(TaskWriter.class);

	public TaskWriter(final TimeZone timeZone) {
		this.timeZone = timeZone;
	}
	
	public void writeArray(final Task taskObject, final int cols[], final JSONArray jsonArray) throws JSONException {
		final JSONArray jsonTaskArray = new JSONArray();
		for (int a = 0; a < cols.length; a++) {
			write(cols[a], taskObject, jsonTaskArray);
		}
		jsonArray.put(jsonTaskArray);
	}
	
	public void writeTask(final Task taskObject, final JSONObject jsonObj) throws JSONException {
		writeCommonFields(taskObject, jsonObj);

		writeParameter(TaskFields.TITLE, taskObject.getTitle(), jsonObj);
		writeParameter(TaskFields.START_DATE, taskObject.getStartDate(), jsonObj);
		writeParameter(TaskFields.END_DATE, taskObject.getEndDate(), jsonObj);
        if (taskObject.containsActualCosts()) {
		    writeParameter(TaskFields.ACTUAL_COSTS, taskObject.getActualCosts(), jsonObj);
        }
        if (taskObject.containsActualDuration()) {
		    writeParameter(TaskFields.ACTUAL_DURATION, taskObject.getActualDuration(), jsonObj);
        }
		writeParameter(TaskFields.NOTE, taskObject.getNote(), jsonObj);
		writeParameter(TaskFields.AFTER_COMPLETE, taskObject.getAfterComplete(), jsonObj);
		writeParameter(TaskFields.BILLING_INFORMATION, taskObject.getBillingInformation(), jsonObj);
		writeParameter(TaskFields.CATEGORIES, taskObject.getCategories(), jsonObj);
		writeParameter(TaskFields.COMPANIES, taskObject.getCompanies(), jsonObj);
		writeParameter(TaskFields.CURRENCY, taskObject.getCurrency(), jsonObj);
		writeParameter(TaskFields.DATE_COMPLETED, taskObject.getDateCompleted(), jsonObj);
        if (taskObject.containsPercentComplete()) {
		    writeParameter(TaskFields.PERCENT_COMPLETED, taskObject.getPercentComplete(), jsonObj);
        }
        if (taskObject.containsPriority()) {
		    writeParameter(TaskFields.PRIORITY, taskObject.getPriority(), jsonObj);
        }
        if (taskObject.containsStatus()) {
		    writeParameter(TaskFields.STATUS, taskObject.getStatus(), jsonObj);
        }
        if (taskObject.containsTargetCosts()) {
		    writeParameter(TaskFields.TARGET_COSTS, taskObject.getTargetCosts(), jsonObj);
        }
        if (taskObject.containsTargetDuration()) {
		    writeParameter(TaskFields.TARGET_DURATION, taskObject.getTargetDuration(), jsonObj);
        }
		if (taskObject.containsLabel()) {
			writeParameter(TaskFields.COLORLABEL,  taskObject.getLabel(), jsonObj);
		} 
		
		writeParameter(TaskFields.TRIP_METER, taskObject.getTripMeter(), jsonObj);
		writeParameter(TaskFields.ALARM, taskObject.getAlarm(), timeZone, jsonObj);
		writeRecurrenceParameter(taskObject, jsonObj);
		
		if (taskObject.containsParticipants()) {
			jsonObj.put(TaskFields.PARTICIPANTS, getParticipantsAsJSONArray(taskObject));
		}
		
		if (taskObject.containsUserParticipants()) {
			jsonObj.put(TaskFields.USERS, getUsersAsJSONArray(taskObject));
		}
	}
	
	public void write(final int field, final Task taskObject, final JSONArray jsonArray) throws JSONException {
		switch (field) {
			case Task.OBJECT_ID:
				writeValue(taskObject.getObjectID(), jsonArray);
				break;
			case Task.CREATED_BY:
				writeValue(taskObject.getCreatedBy(), jsonArray);
				break;
			case Task.CREATION_DATE:
                writeValue(taskObject.getCreationDate(), timeZone, jsonArray);
				break;
			case Task.MODIFIED_BY:
				writeValue(taskObject.getModifiedBy(), jsonArray);
				break;
			case Task.LAST_MODIFIED:
                writeValue(taskObject.getLastModified(), timeZone, jsonArray);
				break;
			case Task.FOLDER_ID:
				writeValue(taskObject.getParentFolderID(), jsonArray);
				break;
			case Task.TITLE:
				writeValue(taskObject.getTitle(), jsonArray);
				break;
			case Task.START_DATE:
                writeValue(taskObject.getStartDate(), jsonArray);
				break;
			case Task.END_DATE:
                writeValue(taskObject.getEndDate(), jsonArray);
				break;
			case Task.NOTE:
				writeValue(taskObject.getNote(), jsonArray);
				break;
			case Task.ACTUAL_COSTS:
				writeValue(taskObject.getActualCosts(), jsonArray);
				break;
			case Task.ACTUAL_DURATION:
				writeValue(taskObject.getActualDuration(), jsonArray);
				break;
			case Task.BILLING_INFORMATION:
				writeValue(taskObject.getBillingInformation(), jsonArray);
				break;
			case Task.CATEGORIES:
				writeValue(taskObject.getCategories(), jsonArray);
				break;
			case Task.COMPANIES:
				writeValue(taskObject.getCompanies(), jsonArray);
				break;
			case Task.CURRENCY:
				writeValue(taskObject.getCurrency(), jsonArray);
				break;
			case Task.DATE_COMPLETED:
                writeValue(taskObject.getDateCompleted(), jsonArray);
				break;
			case Task.PERCENT_COMPLETED:
				writeValue(taskObject.getPercentComplete(), jsonArray);
				break;
			case Task.PRIORITY:
				writeValue(taskObject.getPriority(), jsonArray);
				break;
			case Task.STATUS:
				writeValue(taskObject.getStatus(), jsonArray);
				break;
			case Task.TARGET_COSTS:
				writeValue(taskObject.getTargetCosts(), jsonArray);
				break;
			case Task.TARGET_DURATION:
				writeValue(taskObject.getTargetDuration(), jsonArray);
				break;
			case Task.TRIP_METER:
				writeValue(taskObject.getTripMeter(), jsonArray);
				break;
			case Task.RECURRENCE_TYPE:
				writeValue(taskObject.getRecurrenceType(), jsonArray);
				break;
			case Task.COLOR_LABEL:
				writeValue(taskObject.getLabel(), jsonArray);
				break;
			case Task.PARTICIPANTS:
				jsonArray.put(getParticipantsAsJSONArray(taskObject));
				break;
			case Task.USERS:
				jsonArray.put(getUsersAsJSONArray(taskObject));
				break;
            case Task.PRIVATE_FLAG:
                writeValue(taskObject.getPrivateFlag(), jsonArray);
                break;
            case Task.ALARM:
                writeValue(taskObject.getAlarm(), timeZone, jsonArray);
                break;
			default: 
				LOG.warn("missing field in mapping: " + field);
		}
	}
}
