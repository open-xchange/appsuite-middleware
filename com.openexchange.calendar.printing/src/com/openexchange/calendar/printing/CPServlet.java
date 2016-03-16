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

package com.openexchange.calendar.printing;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.PermissionServlet;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.printing.blocks.CPFactory;
import com.openexchange.calendar.printing.blocks.CPFormattingInformation;
import com.openexchange.calendar.printing.blocks.CPPartition;
import com.openexchange.calendar.printing.blocks.MonthPartitioningStrategy;
import com.openexchange.calendar.printing.blocks.WeekPartitioningStrategy;
import com.openexchange.calendar.printing.blocks.WorkWeekPartitioningStrategy;
import com.openexchange.calendar.printing.days.Day;
import com.openexchange.calendar.printing.days.Partitioner;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.html.HtmlService;
import com.openexchange.java.AllocatingStringWriter;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.session.ServerSession;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - logic
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a> - skeleton
 */
public class CPServlet extends PermissionServlet {

    private static final long serialVersionUID = -5186422014968264569L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CPServlet.class);

    private static final String APPOINTMENTS = "appointments";

    private static final String VIEW_START = "start";

    private static final String VIEW_END = "end";

    private static final String DEBUG = "debuggingItems";

    private static final String FORMATTINGINFO = "formattinginfo";

    private static final String DAYS = "days";

    private static final String I18N = "i18n";

    private static final String DOCUMENT_TITLE = "documentTitle";

    private static final String DATE_FORMATTER = "dateFormatter";

    private static final AtomicReference<ServiceLookup> SERVICES = new AtomicReference<ServiceLookup>();

    /**
     * Sets the service look-up.
     *
     * @param services The service look-up to set
     */
    public static void setServiceLookup(final ServiceLookup services) {
        SERVICES.set(services);
    }

    /**
     * Gets the service of specified type
     *
     * @param clazz The service's class
     * @return The service or <code>null</code> if absent
     * @throws IllegalStateException If an error occurs while returning the demanded service
     */
    private static <S extends Object> S getService(final Class<? extends S> clazz) {
        final ServiceLookup serviceLookup = SERVICES.get();
        if (null == serviceLookup) {
            throw new IllegalStateException("ServiceLookup is absent. Check bundle activator.");
        }
        return serviceLookup.getService(clazz);
    }

    // --------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link CPServlet}.
     */
    public CPServlet() {
        super();
    }

    @Override
    protected boolean hasModulePermission(final ServerSession session) {
        return session.getUserPermissionBits().hasCalendar();
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final List<String> debuggingItems = new LinkedList<String>();
        final CPTool tool = new CPTool();

        resp.setContentType("text/html; charset=UTF-8");

        final ServerSession session = getSessionObject(req);
        try {
            final User user = session.getUser();
            final TimeZone zone = TimeZone.getTimeZone(user.getTimeZone());
            final CPParameters params = new CPParameters(req, zone);
            if (params.hasUnparseableFields()) {
                throw new ServletException("Could not parse the value of the following parameters: " + Strings.join(
                    params.getUnparseableFields(),
                    ","));
            }
            if (params.isMissingMandatoryFields()) {
                throw new ServletException("Missing one or more mandatory parameters: " + Strings.join(
                    params.getMissingMandatoryFields(),
                    ","));
            }
            if (CPType.getByTemplateName(params.getTemplate()) == null) {
                throw new ServletException("Cannot find template " + params.getTemplate());
            }

            if (tool.isBlockTemplate(params)) {
                tool.calculateNewStartAndEnd(params);
            }

            OXTemplate template;
            if(params.hasUserTemplate()) {
                template = getService(TemplateService.class).loadTemplate(params.getUserTemplate(), params.getTemplate(), session);
            } else {
                template = getService(TemplateService.class).loadTemplate(params.getTemplate());
            }

            final AppointmentSQLInterface appointmentSql = getService(AppointmentSqlFactoryService.class).createAppointmentSql(session);
            SearchIterator<Appointment> iterator;
            if (params.hasFolder()) {
                iterator = appointmentSql.getAppointmentsBetweenInFolder(params.getFolder(), new int[] {
                    DataObject.OBJECT_ID, FolderChildObject.FOLDER_ID, CalendarObject.USERS }, params.getStart(), params.getEnd(), -1, null);
            } else {
                iterator = appointmentSql.getAppointmentsBetween(session.getUserId(), params.getStart(), params.getEnd(), new int[] {
                    DataObject.OBJECT_ID, FolderChildObject.FOLDER_ID, CalendarObject.USERS }, -1, null);
            }
            final List<Appointment> idList = SearchIteratorAdapter.toList(iterator);

            final Locale locale = user.getLocale();
            final CPCalendar cal = CPCalendar.getCalendar(zone, locale);
            modifyCalendar(cal, params);

            final CalendarCollectionService calendarTools = getService(CalendarCollectionService.class);
            final Partitioner partitioner = new Partitioner(params, cal, session.getContext(), appointmentSql, calendarTools);
            final List<Day> perDayList = partitioner.partition(idList, session.getUserId());

            final List<CPAppointment> expandedAppointments = tool.expandAppointements(
                idList,
                params.getStart(),
                params.getEnd(),
                appointmentSql,
                calendarTools,
                session.getUserId());

            tool.sort(expandedAppointments);

            final CPFactory factory = new CPFactory();
            factory.addStrategy(new WorkWeekPartitioningStrategy());
            factory.addStrategy(new WeekPartitioningStrategy());
            factory.addStrategy(new MonthPartitioningStrategy());
            factory.setCalendar(cal);
            factory.setTypeToProduce(CPType.getByTemplateName(params.getTemplate()));

            final CPPartition partitions = factory.partition(expandedAppointments);

            final Map<String, Object> variables = new HashMap<String, Object>(8);
            variables.put(APPOINTMENTS, partitions.getAppointments());
            variables.put(FORMATTINGINFO, partitions.getFormattingInformation());
            variables.put(VIEW_START, params.getStart());
            variables.put(VIEW_END, params.getEnd());
            variables.put(DEBUG, debuggingItems);
            variables.put(DAYS, perDayList);
            variables.put(I18N, new I18n(I18nServices.getInstance().getService(locale)));
            variables.put(DOCUMENT_TITLE, getDocumentTitle(session));
            variables.put(DATE_FORMATTER, new DateFormatter(user.getLocale(), TimeZone.getTimeZone(user.getTimeZone())));

            for (final CPAppointment app : partitions.getAppointments()) {
                debuggingItems.add(app.getTitle());
            }
            for (final CPFormattingInformation info : partitions.getFormattingInformation()) {
                debuggingItems.add(info.toString());
            }
            final AllocatingStringWriter htmlWriter = new AllocatingStringWriter();
            template.process(variables, htmlWriter);
            final String html = getService(HtmlService.class).sanitize(htmlWriter.toString(), null, false, null, null);
            final PrintWriter writer = resp.getWriter();
            writer.write(html);
            writer.flush();
        } catch (final Exception x) {
            writeException(resp, x);
        }
    }

    private String getDocumentTitle(Session session) {
        ConfigViewFactory configViewFactory = CPServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        String retval = null;
        if (null != configViewFactory) {
            ConfigView configView;
            try {
                configView = configViewFactory.getView(session.getUserId(), session.getContextId());
                retval = configView.get("ui/product/name", String.class);
            } catch (OXException e) {
                LOG.error("", e);
            }
        }
        if (null == retval) {
            retval = "Open-Xchange";
        }
        return retval;
    }

    /**
     * Write an exception message as HTML to the response
     * @throws IOException
     */
    private void writeException(final HttpServletResponse resp, final Throwable t) throws IOException {
        LOG.error("", t);
        final PrintWriter writer = resp.getWriter();
        writer.append(t.getMessage());
        // TODO Write HTML page as response
    }

    /**
     * Modify a calendar according to the given parameters
     */
    public void modifyCalendar(final CPCalendar calendar, final CPParameters params) {
        if (params.hasWeekStart()) {
            calendar.setFirstDayOfWeek(params.getWeekStart());
        }
        if (params.hasWorkWeekDuration()) {
            calendar.setWorkWeekDurationInDays(params.getWorkWeekDuration());
        }
        if (params.hasWorkWeekStart()) {
            calendar.setWorkWeekStartingDay(params.getWorkWeekStart());
        }
        if (params.hasWorkDayEnd()) {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(params.getWorkDayEnd());
            calendar.setWorkDayStartingHours(cal.get(Calendar.HOUR_OF_DAY));
        }
        if (params.hasWorkDayStart()) {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(params.getWorkDayStart());
            calendar.setWorkDayStartingHours(cal.get(Calendar.HOUR_OF_DAY));
        }
        if (params.hasTimezone()) {
            calendar.setTimeZone(params.getTimezone());
        }
    }
}
