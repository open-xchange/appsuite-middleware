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

package com.openexchange.ajax.request;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.writer.ReminderWriter;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.api2.OXException;
import com.openexchange.api2.ReminderSQLInterface;
import com.openexchange.groupware.calendar.CalendarCommonCollection;
import com.openexchange.groupware.reminder.ReminderHandler;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.servlet.AjaxException;

import java.io.Writer;
import java.sql.SQLException;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

public class ReminderRequest {
	
	private SessionObject sessionObj = null;
	
	private JSONWriter jsonWriter = null;
	
	private Date timestamp;
	
	public Date getTimestamp() {
		return timestamp;
	}

	public ReminderRequest(SessionObject sessionObj, Writer w) {
		this.sessionObj = sessionObj;
		this.jsonWriter = new JSONWriter(w);
	}
	
	public void action(String action, JSONObject jsonObject) throws OXMandatoryFieldException, OXException, JSONException, SearchIteratorException, AjaxException {
		if (action.equalsIgnoreCase(AJAXServlet.ACTION_DELETE)) {
			actionDelete(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATES)) {
			actionUpdates(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_RANGE)) {
			actionRange(jsonObject);
		} else {
			throw new AjaxException(AjaxException.Code.UnknownAction, action);
		}
	}

	private void actionDelete(JSONObject jsonObject) throws OXMandatoryFieldException, JSONException, OXException {
		final JSONObject jData = DataParser.checkJSONObject(jsonObject, "data");
		final int id = DataParser.checkInt(jData, AJAXServlet.PARAMETER_ID);
		
		final ReminderSQLInterface reminderSql = new ReminderHandler(sessionObj);

		jsonWriter.array();
		try {
			reminderSql.deleteReminder(id);
		} finally {
			jsonWriter.endArray();
		}
		
	}

	private void actionUpdates(JSONObject jsonObject) throws OXMandatoryFieldException, JSONException, OXException, SearchIteratorException {
		timestamp = DataParser.checkDate(jsonObject, AJAXServlet.PARAMETER_TIMESTAMP);
		
		final ReminderSQLInterface reminderSql = new ReminderHandler(sessionObj);
		final SearchIterator it = reminderSql.listModifiedReminder(sessionObj.getUserObject().getId(), timestamp);

		jsonWriter.array();
		try {
			while (it.hasNext()) {
				ReminderWriter reminderWriter = new ReminderWriter(jsonWriter, TimeZone.getTimeZone(sessionObj.getUserObject().getTimeZone()));
				reminderWriter.writeObject((ReminderObject)it.next());
			}
		} finally {
			if (null != it) {
				it.close();				
			}
			jsonWriter.endArray();
		}
	}

	private void actionRange(JSONObject jsonObject) throws OXMandatoryFieldException, JSONException, OXException, SearchIteratorException {
		Date end = DataParser.checkDate(jsonObject, AJAXServlet.PARAMETER_END);
		
		SearchIterator it = null;
		
		jsonWriter.array();
		
		try {
			ReminderSQLInterface reminderSql = new ReminderHandler(sessionObj);
			it = reminderSql.listModifiedReminder(sessionObj.getUserObject().getId(), end);
			

			while (it.hasNext()) {
				ReminderWriter reminderWriter = new ReminderWriter(jsonWriter, TimeZone.getTimeZone(sessionObj.getUserObject().getTimeZone()));
				ReminderObject reminderObj = (ReminderObject)it.next();
				
				if (reminderObj.isRecurrenceAppointment()) {
					Date alarm = CalendarCommonCollection.getNextReminderDate(Integer.parseInt(reminderObj.getTargetId()), Integer.parseInt(reminderObj.getFolder()), sessionObj);
					
					if (alarm == null) {
						continue;
					} else {
						reminderObj.setDate(alarm);
					}
				}
				
				reminderWriter.writeObject(reminderObj);
				
			}
		} catch (SQLException e) {
			throw new OXException("SQLException occured", e);
		} finally {
			jsonWriter.endArray();
			if (null != it) {
				it.close();
			}
		}
	}

}
