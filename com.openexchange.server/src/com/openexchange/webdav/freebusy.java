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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.framework.ServiceException;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.util.TimeZones;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.servlet.CountingHttpServletRequest;
import com.openexchange.tools.servlet.ratelimit.RateLimitedException;
import com.openexchange.user.UserService;

/**
 * Servlet for writing free busy information.
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class freebusy extends HttpServlet {

    private static final long serialVersionUID = 6336387126907903347L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(freebusy.class);

    private static final DateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");

    private static final DateFormat outputFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");

    static {
        outputFormat.setTimeZone(TimeZones.UTC);
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        try {
            // create new HttpSession if it's missing
            req.getSession(true);
            super.service(new CountingHttpServletRequest(req), resp);
        } catch (RateLimitedException e) {
            e.send(resp);
        }
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final Context context = getContext(request);
        if (null == context) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unable to determine context.");
            return;
        }

        final int period = getPeriod(request);
        final Date start;
        final Date end;
        if (-1 == period) {
            start = getStart(request);
            if (null == start) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to determine start of free busy time frame.");
                return;
            }
            end = getEnd(request);
            if (null == end) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to determine end of free busy time frame.");
                return;
            }
        } else {
            final Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -period);
            start = calendar.getTime();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.MONTH, period);
            end = calendar.getTime();
        }

        final String mailAddress = getMailAddress(request);
        if (null == mailAddress) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to determine mail address.");
            return;
        }

        final Participant participant = findParticipant(context, mailAddress);
        if (null == participant) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unable to resolve mail address to a user or a resource.");
            return;
        }

        boolean simple = getSimple(request);

        response.setContentType("text/calendar");
        final PrintWriter printWriter = response.getWriter();
        writeVCalendar(context, start, end, mailAddress, participant.getIdentifier(), participant.getType(), printWriter, simple);
    }

    private String getMailAddress(final HttpServletRequest request) {
        final String userName = request.getParameter("username");
        final String serverName = request.getParameter("server");
        if (null == userName || null == serverName) {
            return null;
        }
        return userName + '@' + serverName;
    }

    private Date getStart(final HttpServletRequest request) {
        if (null == request.getParameter("start")) {
            return null;
        }
        final Date start;
        try {
            synchronized (inputFormat) {
                start = inputFormat.parse(request.getParameter("start"));
            }
        } catch (final ParseException e) {
            LOG.debug("Unable to parse parameter start.", e);
            return null;
        }
        return start;
    }

    private Date getEnd(final HttpServletRequest request) {
        if (null == request.getParameter("end")) {
            return null;
        }
        final Date end;
        try {
            synchronized (inputFormat) {
                end = inputFormat.parse(request.getParameter("end"));
            }
        } catch (final ParseException e) {
            LOG.debug("Unable to parse parameter end.", e);
            return null;
        }
        return end;
    }

    private Context getContext(final HttpServletRequest request) {
        if (request.getParameter("contextid") == null) {
            return null;
        }
        final int contextId;
        try {
            contextId = Integer.parseInt(request.getParameter("contextid"));
        } catch (final NumberFormatException e) {
            LOG.error("Unable to parse context identifier.", e);
            return null;
        }
        Context context;
        try {
            final ContextService service = ServerServiceRegistry.getInstance().getService(ContextService.class, true);
            context = service.getContext(contextId);
        } catch (final ServiceException e) {
            LOG.error("", e);
            return null;
        } catch (final OXException e) {
            LOG.error("Can not load context.", e);
            return null;
        }
        return context;
    }

    private int getPeriod(final HttpServletRequest request) {
        if (null == request.getParameter("period")) {
            return -1;
        }
        final int period;
        try {
            period = Integer.parseInt(request.getParameter("period"));
        } catch (final NumberFormatException e) {
            LOG.error("Unable to parse period parameter.", e);
            return -1;
        }
        return period;
    }

    private boolean getSimple(HttpServletRequest request) {
        String simple = request.getParameter("simple");
        if (null != simple && 0 < simple.length()) {
            return Boolean.parseBoolean(simple);
        }
        return false;
    }

    private void writeVCalendar(final Context context, final Date start, final Date end, final String mailAddress, final int principalId,
        final int type, final PrintWriter printWriter, boolean simple) {
        printWriter.println("BEGIN:VCALENDAR");
        printWriter.println("PRODID:-//www.open-xchange.org//");
        printWriter.println("VERSION:2.0");
        printWriter.println("METHOD:PUBLISH");
        printWriter.println("BEGIN:VFREEBUSY");
        printWriter.println("ORGANIZER:" + mailAddress);
        synchronized (outputFormat) {
            printWriter.println("DTSTART:" + outputFormat.format(start));
            printWriter.println("DTEND:" + outputFormat.format(end));
        }
        try {
            final AppointmentSQLInterface appointmentInterface;
            {
                final Session session = SessionObjectWrapper.createSessionObject(Participant.USER == type ? principalId : context.getMailadmin(), context, "freebusysessionobject");
                appointmentInterface = ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class, true).createAppointmentSql(session);
            }
            /*
             * Write free-busy elements
             */
            final SearchIterator<Appointment> it = appointmentInterface.getFreeBusyInformation(principalId, type, start, end);
            try {
                while (it.hasNext()) {
                    writeFreeBusy(it.next(), printWriter, outputFormat, simple);
                    printWriter.flush();
                }
            } finally {
                it.close();
            }
        } catch (final ServiceException e) {
            LOG.error("Calendar service not found.", e);
        } catch (final OXException e) {
            LOG.error("Problem getting free busy information for '{}'.", mailAddress, e);
        } catch (final RuntimeException e) {
            LOG.error("Problem getting free busy information for '{}'.", mailAddress, e);
        }
        printWriter.println("END:VFREEBUSY");
        printWriter.println("END:VCALENDAR");
        printWriter.flush();
    }

    private void writeFreeBusy(final Appointment appointment, final PrintWriter pw, final DateFormat format, boolean simple) {
        if (simple) {
            if (Appointment.FREE == appointment.getShownAs()) {
                return;
            }
            pw.print("FREEBUSY:");
        } else {
            pw.print("FREEBUSY;FBTYPE=");
            switch (appointment.getShownAs()) {
            case Appointment.FREE:
                pw.print("FREE:");
                break;
            case Appointment.TEMPORARY:
                pw.print("BUSY-TENTATIVE:");
                break;
            case Appointment.ABSENT:
                pw.print("BUSY-UNAVAILABLE:");
                break;
            default:
                pw.print("BUSY:");
            }
        }
        synchronized (format) {
            pw.print(format.format(appointment.getStartDate()));
            pw.print('/');
            pw.println(format.format(appointment.getEndDate()));
        }
    }

    private Participant findParticipant(final Context ctx, final String mailAddress) {
        final ServerServiceRegistry serviceRegistry = ServerServiceRegistry.getInstance();
        User user = null;
        try {
            final UserService service = serviceRegistry.getService(UserService.class, true);
            user = service.searchUser(mailAddress, ctx);
        } catch (final ServiceException e) {
            LOG.error("", e);
        } catch (final OXException e) {
            LOG.debug("User '{}' not found.", mailAddress);
        }

        Resource resource = null;
        try {
            final ResourceService service = serviceRegistry.getService(ResourceService.class, true);
            final Resource[] resources = service.searchResourcesByMail(mailAddress, ctx);
            if (1 == resources.length) {
                resource = resources[0];
            }
        } catch (final ServiceException e) {
            LOG.error("", e);
        } catch (final OXException e) {
            LOG.error("Resource '{}' not found.", mailAddress);
        }

        Participant retval = null;
        if (null != resource && (user == null || !user.getMail().equals(mailAddress))) {
            retval = new ResourceParticipant(resource.getIdentifier());
        } else if (null != user) {
            retval = new UserParticipant(user.getId());
        }
        return retval;
    }
}
