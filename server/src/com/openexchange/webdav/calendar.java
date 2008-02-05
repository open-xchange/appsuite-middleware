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



package com.openexchange.webdav;

import java.io.OutputStream;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.output.XMLOutputter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.OXConcurrentModificationException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.calendar.OXCalendarException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.session.Session;
import com.openexchange.webdav.xml.AppointmentParser;
import com.openexchange.webdav.xml.AppointmentWriter;
import com.openexchange.webdav.xml.DataParser;
import com.openexchange.webdav.xml.XmlServlet;

/**
 * calendar
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public final class calendar extends XmlServlet {
	
	private static final Log LOG = LogFactory.getLog(calendar.class);
	
	@Override
	protected void parsePropChilds(final HttpServletRequest req, final HttpServletResponse resp, final XmlPullParser parser) throws Exception {
		final OutputStream os = resp.getOutputStream();
		
		final Session sessionObj = getSession(req);
		
		final XMLOutputter xo = new XMLOutputter();
		
		final AppointmentParser ap = new AppointmentParser(sessionObj);
		final AppointmentSQLInterface appointmentsql = new CalendarSql(sessionObj);
		
		if (isTag(parser, "prop", "DAV:")) {
			final Context ctx = ContextStorage.getInstance().getContext(sessionObj.getContextId());
			
			String client_id = null;
			
			int method = 0;
			
			CalendarDataObject appointmentobject = null;
			
			try {
				appointmentobject = new CalendarDataObject();
				parser.nextTag();
				ap.parse(parser, appointmentobject);
				
				method = ap.getMethod();
				
				appointmentobject.setContext(ctx);
				
				final Date lastModified = appointmentobject.getLastModified();
				appointmentobject.removeLastModified();
				
				final int inFolder = ap.getFolder();
				client_id = ap.getClientID();
				
				boolean hasConflicts = false;
				
				CalendarDataObject[] conflicts = null;
				
				switch (method) {
					case DataParser.SAVE:
						if (appointmentobject.containsObjectID()) {
							if (!appointmentobject.getAlarmFlag()) {
								appointmentobject.setAlarm(-1);
							}
							
							if (lastModified == null) {
								throw new OXMandatoryFieldException("missing field last_modified");
							}
							
							conflicts = appointmentsql.updateAppointmentObject(appointmentobject, inFolder, lastModified);
							hasConflicts = (conflicts != null);
						} else {
							if (!appointmentobject.getAlarmFlag()) {
								appointmentobject.removeAlarm();
							}
							
							appointmentobject.setParentFolderID(inFolder);
							conflicts = appointmentsql.insertAppointmentObject(appointmentobject);
							hasConflicts = (conflicts != null);
						}
						break;
					case DataParser.DELETE:
						if (LOG.isDebugEnabled()) {
							LOG.debug("delete appointment: " + appointmentobject.getObjectID() + " in folder: " + inFolder);
						}
						
						if (lastModified == null) {
							throw new OXMandatoryFieldException("missing field last_modified");
						}
						
						appointmentsql.deleteAppointmentObject(appointmentobject, inFolder, lastModified);
						break;
					case DataParser.CONFIRM:
						appointmentsql.setUserConfirmation(appointmentobject.getObjectID(), sessionObj.getUserId(), ap.getConfirm(), appointmentobject.getConfirmMessage());
						break;
					default:
						throw new OXConflictException("invalid method: " + method);
				}
				
				if (hasConflicts) {
					writeResponse(appointmentobject, HttpServletResponse.SC_CONFLICT, APPOINTMENT_CONFLICT_EXCEPTION, client_id, os, xo, conflicts);
				} else {
					writeResponse(appointmentobject, HttpServletResponse.SC_OK, OK, client_id, os, xo);
				}
			} catch (OXMandatoryFieldException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(appointmentobject, HttpServletResponse.SC_CONFLICT, MANDATORY_FIELD_EXCEPTION, client_id, os, xo);
			} catch (OXPermissionException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(appointmentobject, HttpServletResponse.SC_FORBIDDEN, PERMISSION_EXCEPTION, client_id, os, xo);
			} catch (OXConflictException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(appointmentobject, HttpServletResponse.SC_CONFLICT, CONFLICT_EXCEPTION, client_id, os, xo);
			} catch (OXObjectNotFoundException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(appointmentobject, HttpServletResponse.SC_NOT_FOUND, OBJECT_NOT_FOUND_EXCEPTION, client_id, os, xo);
			} catch (OXConcurrentModificationException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(appointmentobject, HttpServletResponse.SC_CONFLICT, MODIFICATION_EXCEPTION, client_id, os, xo);
			} catch (XmlPullParserException exc) {
				LOG.debug(_parsePropChilds, exc);
				writeResponse(appointmentobject, HttpServletResponse.SC_BAD_REQUEST, BAD_REQUEST_EXCEPTION, client_id, os, xo);
			} catch (OXCalendarException exc) {
				if (exc.getCategory() == Category.USER_INPUT) {
					LOG.debug(_parsePropChilds, exc);
					writeResponse(appointmentobject, HttpServletResponse.SC_CONFLICT, USER_INPUT_EXCEPTION, client_id, os, xo);
				} else if (exc.getCategory() == Category.TRUNCATED) {
					LOG.debug(_parsePropChilds, exc);
					writeResponse(appointmentobject, HttpServletResponse.SC_CONFLICT, USER_INPUT_EXCEPTION, client_id, os, xo);
				} else {
					LOG.error(_parsePropChilds, exc);
					writeResponse(appointmentobject, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, SERVER_ERROR_EXCEPTION + exc.toString(), client_id, os, xo);
				}
			} catch (OXException exc) {
				if (exc.getCategory() == Category.TRUNCATED) {
					LOG.debug(_parsePropChilds, exc);
					writeResponse(appointmentobject, HttpServletResponse.SC_CONFLICT,
							USER_INPUT_EXCEPTION, client_id, os, xo);
				} else {
					LOG.error(_parsePropChilds, exc);
					writeResponse(appointmentobject, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							SERVER_ERROR_EXCEPTION + exc.toString(), client_id, os, xo);
				}
			} catch (Exception exc) {
				LOG.error(_parsePropChilds, exc);
				writeResponse(appointmentobject, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, SERVER_ERROR_EXCEPTION + exc.toString(), client_id, os, xo);
			}
		} else {
			parser.next();
		}
	}
	
	@Override
	protected void startWriter(final Session sessionObj, final Context ctx, final int objectId, final int folderId, final OutputStream os) throws Exception {
		final AppointmentWriter appointmentwriter = new AppointmentWriter(sessionObj);
		appointmentwriter.startWriter(objectId, folderId, os);
	}
	
	@Override
	protected void startWriter(final Session sessionObj, final Context ctx, final int folderId, final boolean bModified, final boolean bDelete, final Date lastsync, final OutputStream os) throws Exception {
		startWriter(sessionObj, ctx, folderId, bModified, bDelete, false, lastsync, os);
	}
	
	@Override
	protected void startWriter(final Session sessionObj, final Context ctx, final int folderId, final boolean bModified, final boolean bDelete, final boolean bList, final Date lastsync, final OutputStream os) throws Exception {
		final AppointmentWriter appointmentwriter = new AppointmentWriter(sessionObj);
		appointmentwriter.startWriter(bModified, bDelete, bList, folderId, lastsync, os);
	}
	
	@Override
	protected boolean hasModulePermission(final Session sessionObj, final Context ctx) {
		final UserConfiguration uc = UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(), ctx);
		return (uc.hasWebDAVXML() && uc.hasCalendar());
	}
}