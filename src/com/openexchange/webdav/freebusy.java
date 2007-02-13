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

import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessionObjectWrapper;
import com.openexchange.sessiond.SessiondConnector;
import com.openexchange.tools.iterator.SearchIterator;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *    @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */

public class freebusy extends HttpServlet {
	
	private static final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");
	
	private static final SimpleDateFormat outputFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
	
	private static final Log LOG = LogFactory.getLog(freebusy.class);
	
	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		
		int contextid = -1;
		String mailPrefix = null;
		String mailSuffix = null;
		Date start = null;
		Date end = null;
		
		if (httpServletRequest.getParameter("contextid") != null) {
			contextid = Integer.parseInt(httpServletRequest.getParameter("contextid"));
		} else {
			httpServletResponse.sendError(HttpServletResponse.SC_CONFLICT, "missing parameter: contextid");
			return;
		}
		
		boolean bPeriod = false;
		
		int period = 0;
		
		if (httpServletRequest.getParameter("period") != null) {
			try {
				period = Integer.parseInt(httpServletRequest.getParameter("period"));
				
				if (period > 0) {
					Calendar c = Calendar.getInstance();
					c.add(Calendar.MONTH, period);
					end = c.getTime();
					
					period = period-period*2;
					c = Calendar.getInstance();
					c.add(Calendar.MONTH, period);
					start = c.getTime();
					
					
					bPeriod = true;
				}
			} catch (NumberFormatException ex) {
				httpServletResponse.sendError(HttpServletResponse.SC_CONFLICT, "invalid value in parameter: period");
				return;
			}
		}
		
		if (!bPeriod) {
			if (httpServletRequest.getParameter("start") != null) {
				try {
					start = inputFormat.parse(httpServletRequest.getParameter("start"));
				} catch (ParseException ex) {
					httpServletResponse.sendError(HttpServletResponse.SC_CONFLICT, "invalid value in parameter: start");
					return;
				}
			} else {
				httpServletResponse.sendError(HttpServletResponse.SC_CONFLICT, "missing parameter: start");
				return;
			}
			
			if (httpServletRequest.getParameter("end") != null) {
				try {
					end = inputFormat.parse(httpServletRequest.getParameter("end"));
				} catch (ParseException ex) {
					httpServletResponse.sendError(HttpServletResponse.SC_CONFLICT, "invalid value in parameter: end");
					return;
				}
			} else {
				httpServletResponse.sendError(HttpServletResponse.SC_CONFLICT, "missing parameter: end");
				return;
			}
		}
		
		if (httpServletRequest.getParameter("username") != null) {
			mailPrefix = httpServletRequest.getParameter("username");
		} else {
			httpServletResponse.sendError(HttpServletResponse.SC_CONFLICT, "missing parameter: username");
			return;
		}
		
		if (httpServletRequest.getParameter("server") != null) {
			mailSuffix = httpServletRequest.getParameter("server");
		} else {
			httpServletResponse.sendError(HttpServletResponse.SC_CONFLICT, "missing parameter: server");
			return;
		}
		
		httpServletResponse.setContentType("text/html");
		PrintWriter printWriter = httpServletResponse.getWriter();
		try {
			writeVCalendar(contextid, start, end, mailPrefix, mailSuffix, httpServletRequest.getRemoteHost(), printWriter);
		} catch (Exception exc) {
			LOG.error("doGet", exc);
		}
	}
	
	private void writeVCalendar(int contextId, Date start, Date end, String mailPrefix, String mailSuffix, String remoteHost, PrintWriter printWriter) throws Exception {
		final SessiondConnector sessiondConnector = SessiondConnector.getInstance();
		
		SearchIterator it = null;
		
		printWriter.println("BEGIN:VCALENDAR");
		printWriter.println("PRODID:-//www.open-xchange.org//");
		printWriter.println("VERSION:2.0");
		printWriter.println("METHOD:PUBLISH");
		printWriter.println("BEGIN:VFREEBUSY");
		printWriter.println("ORGANIZER:"+mailPrefix+"@"+mailSuffix);
		
		try {
			final Context context = new ContextImpl(contextId);
			final UserStorage userStorage = UserStorage.getInstance(context);
			User user = userStorage.searchUser(mailPrefix + "@" + mailSuffix);
			final SessionObject sessionObj = SessionObjectWrapper.createSessionObject(user.getId(), context, "freebusysessionobject");
			
			AppointmentSQLInterface appointmentInterface = new CalendarSql(sessionObj);
			it = appointmentInterface.getFreeBusyInformation(user.getId(), Participant.USER, start, end);
			while (it.hasNext()) {
				writeFreeBusy((AppointmentObject)it.next(), printWriter);
			}
		} catch (Exception exc) {
			LOG.error("writeVCalendar", exc);
		} finally {
			if (it != null) {
				it.close();
			}
		}
		
		printWriter.println("END:VFREEBUSY");
		printWriter.println("END:VCALENDAR");
		printWriter.flush();
	}
	
	private void writeFreeBusy(AppointmentObject appointmentObject, PrintWriter printWriter) throws Exception {
		printWriter.print("FREEBUSY;");
		
		switch (appointmentObject.getShownAs()) {
			case AppointmentObject.ABSENT:
				printWriter.print("FBTYPE=BUSY:");
				break;
			case AppointmentObject.RESERVED:
				printWriter.print("FBTYPE=BUSY-TENTATIVE:");
				break;
			case AppointmentObject.TEMPORARY:
				printWriter.print("FBTYPE=BUSY-UNAVAILABLE:");
				break;
			case AppointmentObject.FREE:
				printWriter.print("FBTYPE=FREE:");
				break;
			default:
				printWriter.print("FBTYPE=BUSY:");
		}
		
		printWriter.write(outputFormat.format(appointmentObject.getStartDate()));
		printWriter.write('/');
		printWriter.println(outputFormat.format(appointmentObject.getEndDate()));
		printWriter.flush();
	}
}
