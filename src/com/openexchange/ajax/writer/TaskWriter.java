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
import java.io.PrintWriter;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * TaskWriter
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class TaskWriter extends CalendarWriter {
	
	private static final Log LOG = LogFactory.getLog(TaskWriter.class);
	
	public TaskWriter(PrintWriter w, TimeZone timeZone) {
		jsonwriter = new JSONWriter(w);
		this.timeZone = timeZone;
	}
	
	public TaskWriter(JSONWriter jsonwriter, TimeZone timeZone) {
		this.jsonwriter = jsonwriter;
		this.timeZone = timeZone;
	}
	
	public void writeArray(Task taskObject, int cols[]) throws JSONException {
		jsonwriter.array();
		for (int a = 0; a < cols.length; a++) {
			write(cols[a], taskObject);
		}
		jsonwriter.endArray();
	}
	
	public void writeTask(Task taskObject) throws JSONException {
		jsonwriter.object();
		
		writeCommonFields(taskObject);

		writeParameter(TaskFields.TITLE, taskObject.getTitle());
		writeParameter(TaskFields.START_DATE, taskObject.getStartDate());
		writeParameter(TaskFields.END_DATE, taskObject.getEndDate());
        if (taskObject.containsActualCosts()) {
		    writeParameter(TaskFields.ACTUAL_COSTS, taskObject.getActualCosts());
        }
        if (taskObject.containsActualDuration()) {
		    writeParameter(TaskFields.ACTUAL_DURATION, taskObject.getActualDuration());
        }
		writeParameter(TaskFields.NOTE, taskObject.getNote());
		writeParameter(TaskFields.AFTER_COMPLETE, taskObject.getAfterComplete());
		writeParameter(TaskFields.BILLING_INFORMATION, taskObject.getBillingInformation());
		writeParameter(TaskFields.CATEGORIES, taskObject.getCategories());
		writeParameter(TaskFields.COMPANIES, taskObject.getCompanies());
		writeParameter(TaskFields.CURRENCY, taskObject.getCurrency());
		writeParameter(TaskFields.DATE_COMPLETED, taskObject.getDateCompleted());
        if (taskObject.containsPercentComplete()) {
		    writeParameter(TaskFields.PERCENT_COMPLETED, taskObject.getPercentComplete());
        }
        if (taskObject.containsPriority()) {
		    writeParameter(TaskFields.PRIORITY, taskObject.getPriority());
        }
        if (taskObject.containsStatus()) {
		    writeParameter(TaskFields.STATUS, taskObject.getStatus());
        }
        if (taskObject.containsTargetCosts()) {
		    writeParameter(TaskFields.TARGET_COSTS, taskObject.getTargetCosts());
        }
        if (taskObject.containsTargetDuration()) {
		    writeParameter(TaskFields.TARGET_DURATION, taskObject.getTargetDuration());
        }
		if (taskObject.containsLabel()) {
			writeParameter(TaskFields.COLORLABEL,  taskObject.getLabel());
		} 
		
		writeParameter(TaskFields.TRIP_METER, taskObject.getTripMeter());
		writeParameter(TaskFields.ALARM, taskObject.getAlarm(), timeZone);
		writeRecurrenceParameter(taskObject);
		
        if (taskObject.containsParticipants()) {
            jsonwriter.key(TaskFields.PARTICIPANTS);
            writeParticipants(taskObject);
        }
		
		if (taskObject.containsUserParticipants()) {
            jsonwriter.key(TaskFields.USERS);
			writeUsers(taskObject);
		} 
		
		jsonwriter.endObject();
	}
	
	public void write(int field, Task taskObject) throws JSONException {
		switch (field) {
			case Task.OBJECT_ID:
				writeValue(taskObject.getObjectID());
				break;
			case Task.CREATED_BY:
				writeValue(taskObject.getCreatedBy());
				break;
			case Task.CREATION_DATE:
                writeValue(taskObject.getCreationDate(), timeZone);
				break;
			case Task.MODIFIED_BY:
				writeValue(taskObject.getModifiedBy());
				break;
			case Task.LAST_MODIFIED:
                writeValue(taskObject.getLastModified(), timeZone);
				break;
			case Task.FOLDER_ID:
				writeValue(taskObject.getParentFolderID());
				break;
			case Task.TITLE:
				writeValue(taskObject.getTitle());
				break;
			case Task.START_DATE:
                writeValue(taskObject.getStartDate());
				break;
			case Task.END_DATE:
                writeValue(taskObject.getEndDate());
				break;
			case Task.NOTE:
				writeValue(taskObject.getNote());
				break;
			case Task.ACTUAL_COSTS:
				writeValue(taskObject.getActualCosts());
				break;
			case Task.ACTUAL_DURATION:
				writeValue(taskObject.getActualDuration());
				break;
			case Task.BILLING_INFORMATION:
				writeValue(taskObject.getBillingInformation());
				break;
			case Task.CATEGORIES:
				writeValue(taskObject.getCategories());
				break;
			case Task.COMPANIES:
				writeValue(taskObject.getCompanies());
				break;
			case Task.CURRENCY:
				writeValue(taskObject.getCurrency());
				break;
			case Task.DATE_COMPLETED:
                writeValue(taskObject.getDateCompleted());
				break;
			case Task.PERCENT_COMPLETED:
				writeValue(taskObject.getPercentComplete());
				break;
			case Task.PRIORITY:
				writeValue(taskObject.getPriority());
				break;
			case Task.STATUS:
				writeValue(taskObject.getStatus());
				break;
			case Task.TARGET_COSTS:
				writeValue(taskObject.getTargetCosts());
				break;
			case Task.TARGET_DURATION:
				writeValue(taskObject.getTargetDuration());
				break;
			case Task.TRIP_METER:
				writeValue(taskObject.getTripMeter());
				break;
			case Task.RECURRENCE_TYPE:
				writeValue(taskObject.getRecurrenceType());
				break;
			case Task.COLOR_LABEL:
				writeValue(taskObject.getLabel());
				break;
			case Task.PARTICIPANTS:
				writeParticipants(taskObject);
				break;
			case Task.USERS:
				writeUsers(taskObject);
				break;
            case Task.PRIVATE_FLAG:
                writeValue(taskObject.getPrivateFlag());
                break;
			default: 
				LOG.warn("missing field in mapping: " + field);
		}
	}
}
