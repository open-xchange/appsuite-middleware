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



package com.openexchange.webdav.xml;

import com.openexchange.api.OXObjectNotFoundException;
import java.io.OutputStream;
import java.util.Date;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLInterfaceImpl;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.webdav.xml.fields.CalendarFields;
import com.openexchange.webdav.xml.fields.TaskFields;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CalendarWriter
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class TaskWriter extends CalendarWriter {
	
	protected final static int[] changeFields = {
		DataObject.OBJECT_ID,
		DataObject.CREATED_BY,
		DataObject.CREATION_DATE,
		DataObject.LAST_MODIFIED,
		DataObject.MODIFIED_BY,
		FolderChildObject.FOLDER_ID,
		CommonObject.PRIVATE_FLAG,
		CommonObject.CATEGORIES,
		CalendarObject.TITLE,
		CalendarObject.START_DATE,
		CalendarObject.END_DATE,
		CalendarObject.NOTE,
		CalendarObject.RECURRENCE_TYPE,
		CalendarObject.PARTICIPANTS,
		Task.ACTUAL_COSTS,
		Task.ACTUAL_DURATION,
		Task.ALARM,
		Task.BILLING_INFORMATION,
		Task.CATEGORIES,
		Task.COMPANIES,
		Task.CURRENCY,
		Task.DATE_COMPLETED,
		Task.IN_PROGRESS,
		Task.PERCENT_COMPLETED,
		Task.PRIORITY,
		Task.STATUS,
		Task.TARGET_COSTS,
		Task.TARGET_DURATION,
		Task.TRIP_METER,
		Task.COLOR_LABEL,
                Task.NUMBER_OF_ATTACHMENTS
	};
	
	protected final static int[] deleteFields = {
		DataObject.OBJECT_ID,
		DataObject.LAST_MODIFIED
	};
	
	private static final Log LOG = LogFactory.getLog(TaskWriter.class);
	
	public TaskWriter() {
		
	}
	
	public TaskWriter(final SessionObject sessionObj) {
		this.sessionObj = sessionObj;
	}
	
	public void startWriter(final int objectId, final int folderId, final OutputStream os) throws Exception {
		final Element eProp = new Element("prop", "D", "DAV:");
		final XMLOutputter xo = new XMLOutputter();
		try {
			final TasksSQLInterface tasksql = new TasksSQLInterfaceImpl(sessionObj);
			final Task taskobject = tasksql.getTaskById(objectId, folderId);
			writeObject(taskobject, false, xo, os);
		} catch (final OXObjectNotFoundException exc) {
			writeResponseElement(eProp, 0, HttpServletResponse.SC_NOT_FOUND, XmlServlet.OBJECT_NOT_FOUND_EXCEPTION, xo, os);
		} catch (final Exception ex) {
			writeResponseElement(eProp, 0, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, XmlServlet.SERVER_ERROR_EXCEPTION, xo, os);
		}
	}
	
	public void startWriter(final boolean modified, final boolean deleted, final boolean bList, final int folder_id, final Date lastsync, final OutputStream os) throws Exception {
		final TasksSQLInterface tasksql = new TasksSQLInterfaceImpl(sessionObj);
		final XMLOutputter xo = new XMLOutputter();
		
		if (modified) {
			SearchIterator it = null;
			try {
				it = tasksql.getModifiedTasksInFolder(folder_id, changeFields, lastsync);
				writeIterator(it, false, xo, os);
			} finally {
				if (it != null) {
					it.close();
				}
			}
		}
		
		if (deleted) {
			SearchIterator it = null;
			try {
				it = tasksql.getDeletedTasksInFolder(folder_id, deleteFields, lastsync);
				writeIterator(it, true, xo, os);
			} finally {
				if (it != null) {
					it.close();
				}
			}
		}
		
		
		if (bList) {
			SearchIterator it = null;
			try {
				it = tasksql.getTaskList(folder_id, -1, -1, 0, null, deleteFields);
				writeList(it, xo, os);
			} finally {
				if (it != null) {
					it.close();
				}
			}
		}
	}
	
	public void writeIterator(final SearchIterator it, final boolean delete, final XMLOutputter xo, final OutputStream os) throws Exception {
		while (it.hasNext()) {
			writeObject((Task)it.next(), delete, xo, os);
		}
	}
	
	public void writeObject(final Task taskObj, final boolean delete, final XMLOutputter xo, final OutputStream os) throws Exception {
		writeObject(taskObj, new Element("prop", "D", "DAV:"), delete, xo, os); 
	}
	
	public void writeObject(final Task taskObj, final Element e_prop, final boolean delete, final XMLOutputter xo, final OutputStream os) throws Exception {
		int status = 200;
		String description = "OK";
		int object_id = 0;
		
		try {
			object_id = taskObj.getObjectID();
			addContent2PropElement(e_prop, taskObj, delete);
		} catch (final Exception exc) {
			LOG.error("writeObject", exc);
			status = 500;
			description = "Server Error: " + exc.getMessage();
			object_id = 0;
		}
		
		writeResponseElement(e_prop, object_id, status, description, xo, os);
	}
	
	public void addContent2PropElement(final Element e_prop, final Task taskObj, final boolean delete) throws Exception {
		if (delete) {
			addElement(TaskFields.OBJECT_ID, taskObj.getObjectID(), e_prop);
			addElement(TaskFields.LAST_MODIFIED, taskObj.getLastModified(), e_prop);
			addElement("object_status", "DELETE", e_prop);
		} else {
			addElement("object_status", "CREATE", e_prop);
			
			if (taskObj.containsStartDate()) {
				addElement(CalendarFields.START_DATE, taskObj.getStartDate(), e_prop);
			}
			
			if (taskObj.containsEndDate()) {
				addElement(CalendarFields.END_DATE, taskObj.getEndDate(), e_prop);
			}
			
			if (taskObj.containsActualCosts()) {
				addElement(TaskFields.ACTUAL_COSTS, taskObj.getActualCosts(), e_prop);
			}
			
			// if (taskObj.containsActualDuration()) {
			addElement(TaskFields.ACTUAL_DURATION, taskObj.getActualDuration(), e_prop);
			// }
			
			addElement(TaskFields.BILLING_INFORMATION, taskObj.getBillingInformation(), e_prop);
			addElement(TaskFields.COMPANIES, taskObj.getCompanies(), e_prop);
			
			if (taskObj.containsCurrency()) {
				addElement(TaskFields.CURRENCY, taskObj.getCurrency(), e_prop);
			}
			
			addElement(TaskFields.DATE_COMPLETED, taskObj.getDateCompleted(), e_prop);
			
			if (taskObj.containsPercentComplete()) {
				addElement(TaskFields.PERCENT_COMPLETED, taskObj.getPercentComplete(), e_prop);
			}
			
			if (taskObj.containsPriority()) {
				addElement(TaskFields.PRIORITY, taskObj.getPriority(), e_prop);
			}
			
			if (taskObj.containsStatus()) {
				addElement(TaskFields.STATUS, taskObj.getStatus(), e_prop);
			}
			
			if (taskObj.containsTargetCosts()) {
				addElement(TaskFields.TARGET_COSTS, taskObj.getTargetCosts(), e_prop);
			}
			
			// if (taskObj.containsTargetDuration()) {
			addElement(TaskFields.TARGET_DURATION, taskObj.getTargetDuration(), e_prop);
			// }
			
			if (taskObj.containsTripMeter()) {
				addElement(TaskFields.TRIP_METER, taskObj.getTripMeter(), e_prop);
			}
			
			if (taskObj.containsAlarm()) {
				addElement(CalendarFields.ALARM_FLAG, true, e_prop);
				addElement(TaskFields.ALARM, taskObj.getAlarm(), e_prop);
			} else {
				addElement(CalendarFields.ALARM_FLAG, false, e_prop);
			}
			
			writeCalendarElements(taskObj, e_prop);
		}
	}
	
	@Override
	protected int getModule() {
		return Types.TASK;
	}
	
}




