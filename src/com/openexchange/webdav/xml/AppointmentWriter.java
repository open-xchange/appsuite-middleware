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

import java.io.OutputStream;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarRecurringCollection;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.calendar.RecurringResults;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.webdav.xml.fields.AppointmentFields;
import com.openexchange.webdav.xml.fields.CalendarFields;

/**
 * AppointmentWriter
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class AppointmentWriter extends CalendarWriter {
	
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
		AppointmentObject.LOCATION,
		CalendarObject.START_DATE,
		CalendarObject.END_DATE,
		CalendarObject.NOTE,
		CalendarObject.RECURRENCE_TYPE,
		CalendarObject.PARTICIPANTS,
		CalendarObject.USERS,
		AppointmentObject.SHOWN_AS,
		AppointmentObject.FULL_TIME,
		AppointmentObject.COLOR_LABEL,
		AppointmentObject.NUMBER_OF_ATTACHMENTS,
		AppointmentObject.CHANGE_EXCEPTIONS,
		AppointmentObject.DELETE_EXCEPTIONS,
		AppointmentObject.RECURRENCE_ID,
		AppointmentObject.RECURRENCE_POSITION,
        AppointmentObject.RECURRENCE_CALCULATOR,
		CalendarDataObject.TIMEZONE
	};
	
	protected final static int[] deleteFields = {
		DataObject.OBJECT_ID,
		DataObject.LAST_MODIFIED
	};
	
	private static final Log LOG = LogFactory.getLog(AppointmentWriter.class);
	
	public AppointmentWriter() {
		
	}
	
	public AppointmentWriter(SessionObject sessionObj) {
		this.sessionObj = sessionObj;
	}
	
	public void startWriter(final int objectId, final int folderId, final OutputStream os) throws Exception {
		final AppointmentSQLInterface appointmentsql = new CalendarSql(sessionObj);
		
		final Element eProp = new Element("prop", "D", "DAV:");
		final XMLOutputter xo = new XMLOutputter();
		try {
			final AppointmentObject appointmentobject = appointmentsql.getObjectById(objectId, folderId);
			writeObject(appointmentobject, eProp, false, xo, os);
		} catch (OXObjectNotFoundException exc) {
			writeResponseElement(eProp, 0, HttpServletResponse.SC_NOT_FOUND, XmlServlet.OBJECT_NOT_FOUND_EXCEPTION, xo, os);
		} catch (Exception ex) {
			LOG.error("startWriter", ex);
			writeResponseElement(eProp, 0, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, XmlServlet.SERVER_ERROR_EXCEPTION, xo, os);
		}
	}
	
	public void startWriter(final boolean modified, final boolean deleted, final int folder_id, final Date lastsync, final OutputStream os) throws Exception {
		final AppointmentSQLInterface appointmentsql = new CalendarSql(sessionObj);
		final XMLOutputter xo = new XMLOutputter();
		
		SearchIterator it = null;
		
		if (modified) {
			try {
				it = appointmentsql.getModifiedAppointmentsInFolder(folder_id, changeFields, lastsync);
				writeIterator(it, false, xo, os);
			} finally {
				if (it != null) {
					it.close();
				}
			}
		}
		
		if (deleted) {
			try {
				it = appointmentsql.getDeletedAppointmentsInFolder(folder_id, deleteFields, lastsync);
				writeIterator(it, true, xo, os);
			} finally {
				if (it != null) {
					it.close();
				}
			}
		}
		
	}
	
	public void writeIterator(final SearchIterator it, final boolean delete, final XMLOutputter xo, final OutputStream os) throws Exception {
		while (it.hasNext()) {
			writeObject((AppointmentObject)it.next(), delete, xo, os);
		}
	}
	
	public void writeObject(final AppointmentObject appointmentobject, final boolean delete, final XMLOutputter xo, final OutputStream os) throws Exception {
		writeObject(appointmentobject, new Element("prop", "D", "DAV:"), delete, xo, os);
	}
	
	public void writeObject(final AppointmentObject appointmentobject, final Element e_prop, final boolean delete, final XMLOutputter xo, final OutputStream os) throws Exception {
		int status = 200;
		String description = "OK";
		int object_id = 0;
		
		try {
			object_id = appointmentobject.getObjectID();
			addContent2PropElement(e_prop, appointmentobject, delete);
		} catch (Exception exc) {
			LOG.error("writeObject", exc);
			status = 500;
			description = "Server Error: " + exc.toString();
			object_id = 0;
		}
		
		writeResponseElement(e_prop, object_id, status, description, xo, os);
	}
	
	protected void addContent2PropElement(final Element e_prop, final AppointmentObject ao, final boolean delete) throws Exception {
		addContent2PropElement(e_prop, ao, delete, false);
	}
	
	protected void addContent2PropElement(final Element e_prop, final AppointmentObject ao, final boolean delete, final boolean externalUse) throws Exception {
		if (delete) {
			addElement(AppointmentFields.OBJECT_ID, ao.getObjectID(), e_prop);
			addElement(AppointmentFields.LAST_MODIFIED, ao.getLastModified(), e_prop);
			addElement("object_status", "DELETE", e_prop);
		} else {
			addElement("object_status", "CREATE", e_prop);
			
			final boolean fullTime = ao.getFullTime();
			
			if (ao.getRecurrenceType() == CalendarObject.NONE) {
				addElement(CalendarFields.START_DATE, ao.getStartDate(), e_prop);
				addElement(CalendarFields.END_DATE, ao.getEndDate(), e_prop);
			} else {
				if (!externalUse) {
					final RecurringResults recuResults = CalendarRecurringCollection.calculateFirstRecurring(ao);
					if (recuResults.size() == 1) {
						ao.setStartDate(new Date(recuResults.getRecurringResult(0).getStart()));
						ao.setEndDate(new Date(recuResults.getRecurringResult(0).getEnd()));
					} else {
						LOG.warn("cannot load first recurring appointment from appointment object: " + +ao.getRecurrenceType() + " / "+ao.getObjectID()+"\n\n\n");
					}
				}
				addElement(CalendarFields.START_DATE, ao.getStartDate(), e_prop);
				addElement(CalendarFields.END_DATE, ao.getEndDate(), e_prop);
			}
			
			if (ao.containsDeleteExceptions()) {
				final Date[] deleteExceptions = ao.getDeleteException();
				if (deleteExceptions != null) {
					final StringBuilder stringBuilder = new StringBuilder();
					for (int a = 0; a < deleteExceptions.length; a++) {
						if (a > 0) {
							stringBuilder.append(',');
						}
						
						stringBuilder.append(deleteExceptions[a].getTime());
					}
					addElement(CalendarFields.DELETE_EXCEPTIONS, stringBuilder.toString(), e_prop);
				}
			}
			
			if (ao.containsChangeExceptions()) {
				final Date[] changeException = ao.getChangeException();
				if (changeException != null) {
					final StringBuilder stringBuilder = new StringBuilder();
					for (int a = 0; a < changeException.length; a++) {
						if (a > 0) {
							stringBuilder.append(',');
						}
						
						stringBuilder.append(changeException[a].getTime());
					}
					addElement(CalendarFields.CHANGE_EXCEPTIONS, stringBuilder.toString(), e_prop);
				}
			}
			
			addElement(AppointmentFields.LOCATION, ao.getLocation(), e_prop);
			addElement(AppointmentFields.FULL_TIME, fullTime, e_prop);
			addElement(AppointmentFields.SHOW_AS, ao.getShownAs(), e_prop);
			
			if (ao.containsRecurrenceDatePosition()) {
				addElement(AppointmentFields.RECURRENCE_DATE_POSITION, ao.getRecurrenceDatePosition(), e_prop);
			}
			
			if (ao.containsAlarm()) {
				addElement(CalendarFields.ALARM_FLAG, true, e_prop);
				addElement(AppointmentFields.ALARM, ao.getAlarm(), e_prop);
			} else {
				addElement(CalendarFields.ALARM_FLAG, false, e_prop);
			}
			
			if (ao.getIgnoreConflicts()) {
				addElement(AppointmentFields.IGNORE_CONFLICTS, true, e_prop);
			}
			
			addElement(AppointmentFields.COLORLABEL, ao.getLabel(), e_prop);
			
			writeCalendarElements(ao, e_prop);
		}
	}
	
	@Override
	protected int getModule() {
		return Types.APPOINTMENT;
	}
}




