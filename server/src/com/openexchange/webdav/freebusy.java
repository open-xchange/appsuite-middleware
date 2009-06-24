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

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.resource.Resource;
import com.openexchange.resource.storage.ResourceStorage;
import com.openexchange.server.ServiceException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;

/**
 * Servlet for writing free busy information.
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class freebusy extends HttpServlet {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 6336387126907903347L;

    private static final Log LOG = LogFactory.getLog(freebusy.class);

    private static final DateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");

    private static final TimeZone utc = TimeZone.getTimeZone("UTC");

    private static final DateFormat outputFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");

    static {
        outputFormat.setTimeZone(utc);
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        Context context = getContext(request);
        if (null == context) {
            response.sendError(HttpServletResponse.SC_CONFLICT, "Unable to determine context.");
            return;
        }

        int period = getPeriod(request);
        final Date start;
        final Date end;
        if (-1 == period) {
            start = getStart(request);
            if (null == start) {
                response.sendError(HttpServletResponse.SC_CONFLICT, "Unable to determine start of free busy time frame.");
                return;
            }
            end = getEnd(request);
            if (null == end) {
                response.sendError(HttpServletResponse.SC_CONFLICT, "Unable to determine end of free busy time frame.");
                return;
            }
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -period);
            start = calendar.getTime();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.MONTH, period);
            end = calendar.getTime();
        }

        String mailAddress = getMailAddress(request);
        if (null == mailAddress) {
            response.sendError(HttpServletResponse.SC_CONFLICT, "Unable to determine mail address.");
            return;
        }

        User user = null;
        try {
            // TODO Replace with UserService
            user = UserStorage.getInstance().searchUser(mailAddress, context);
        } catch (LdapException e) {
            LOG.debug("User '" + mailAddress + "' not found.");
        }
        Resource resource = null;
        try {
            // TODO Replace with ResourceService
            if (null == user) {
                Resource[] resources = ResourceStorage.getInstance().searchResourcesByMail(mailAddress, context);
                if (1 == resources.length) {
                    resource = resources[0];
                }
            }
        } catch (LdapException e) {
            LOG.error("Resource '" + mailAddress + "' not found.");
        }
        final int principalId;
        final int type;
        if (null != user) {
            type = Participant.USER;
            principalId = user.getId();
        } else if (null != resource) {
            type = Participant.RESOURCE;
            principalId = resource.getIdentifier();
        } else {
            response.sendError(HttpServletResponse.SC_CONFLICT, "Unable to resolve mail address to a user or a resource.");
            return;
        }
        
        response.setContentType("text/calendar");
        final PrintWriter printWriter = response.getWriter();
        writeVCalendar(context, start, end, mailAddress, principalId, type, request.getRemoteHost(), printWriter);
    }

    private String getMailAddress(HttpServletRequest request) {
        if (null == request.getParameter("username") || null == request.getParameter("server")) {
            return null;
        }
        return request.getParameter("username") + '@' + request.getParameter("server");
    }

    private Date getStart(HttpServletRequest request) {
        if (null == request.getParameter("start")) {
            return null;
        }
        final Date start;
        try {
            start = inputFormat.parse(request.getParameter("start"));
        } catch (ParseException e) {
            LOG.debug("Unable to parse parameter start.", e);
            return null;
        }
        return start;
    }

    private Date getEnd(HttpServletRequest request) {
        if (null == request.getParameter("end")) {
            return null;
        }
        final Date end;
        try {
            end = inputFormat.parse(request.getParameter("end"));
        } catch (ParseException e) {
            LOG.debug("Unable to parse parameter end.", e);
            return null;
        }
        return end;
    }

    private Context getContext(final HttpServletRequest request) throws IOException {
        if (request.getParameter("contextid") == null) {
            return null;
        }
        final int contextId;
        try {
            contextId = Integer.parseInt(request.getParameter("contextid"));
        } catch (NumberFormatException e) {
            LOG.error("Unable to parse context identifier.", e);
            return null;
        }
        // TODO Replace with ContextService
        ContextStorage service = ContextStorage.getInstance();
        final Context context;
        try {
            context = service.getContext(contextId);
        } catch (ContextException e) {
            LOG.error("Can not load context.", e);
            return null;
        }
        return context;
    }

    private int getPeriod(HttpServletRequest request) {
        if (null == request.getParameter("period")) {
            return -1;
        }
        final int period;
        try {
            period = Integer.parseInt(request.getParameter("period"));
        } catch (NumberFormatException e) {
            LOG.error("Unable to parse period parameter.", e);
            return -1;
        }
        return period;
    }

    private void writeVCalendar(Context context, Date start, Date end, String mailAddress, int principalId, int type, String remoteHost, PrintWriter printWriter) {
        printWriter.println("BEGIN:VCALENDAR");
        printWriter.println("PRODID:-//www.open-xchange.org//");
        printWriter.println("VERSION:2.0");
        printWriter.println("METHOD:PUBLISH");
        printWriter.println("BEGIN:VFREEBUSY");
        printWriter.println("ORGANIZER:" + mailAddress);
        printWriter.println("DTSTART:" + outputFormat.format(start));
        printWriter.println("DTEND:" + outputFormat.format(end));
        try {
            final Session sessionObj = SessionObjectWrapper.createSessionObject(context.getMailadmin(), context, "freebusysessionobject");
            final AppointmentSQLInterface appointmentInterface = ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class, true).createAppointmentSql(sessionObj);
            SearchIterator<Appointment> it = appointmentInterface.getFreeBusyInformation(principalId, type, start, end);
            try {
                while (it.hasNext()) {
                    writeFreeBusy(it.next(), printWriter, outputFormat);
                    printWriter.flush();
                }
            } finally {
                it.close();
            }
        } catch (SearchIteratorException e) {
            LOG.error("Problem getting free busy information for '" + mailAddress + "'.", e);
        } catch (OXException e) {
            LOG.error("Problem getting free busy information for '" + mailAddress + "'.", e);
        } catch (ServiceException e) {
            LOG.error("Calendar service not found.", e);
        }
        printWriter.println("END:VFREEBUSY");
        printWriter.println("END:VCALENDAR");
        printWriter.flush();
    }

    private void writeFreeBusy(final Appointment appointment, final PrintWriter pw, final DateFormat format) {
        pw.print("FREEBUSY;");
        switch (appointment.getShownAs()) {
        case Appointment.ABSENT:
            pw.print("FBTYPE=BUSY:");
            break;
        case Appointment.RESERVED:
            pw.print("FBTYPE=BUSY-TENTATIVE:");
            break;
        case Appointment.TEMPORARY:
            pw.print("FBTYPE=BUSY-UNAVAILABLE:");
            break;
        case Appointment.FREE:
            pw.print("FBTYPE=FREE:");
            break;
        default:
            pw.print("FBTYPE=BUSY:");
        }
        pw.print(format.format(appointment.getStartDate()));
        pw.print('/');
        pw.println(format.format(appointment.getEndDate()));
    }
}
