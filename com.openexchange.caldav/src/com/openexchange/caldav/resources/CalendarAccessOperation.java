/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.caldav.resources;

import static com.openexchange.osgi.Tools.requireService;
import com.openexchange.chronos.SchedulingControl;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccessFactory;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.webdav.WebDAVRequestContext;

/**
 * {@link CalendarAccessOperation}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class CalendarAccessOperation<T> {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link CalendarAccessOperation}.
     *
     * @param services A service lookup reference
     */
    public CalendarAccessOperation(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Performs the operation
     *
     * @param session The underlying session
     * @return The result
     */
    public T execute(Session session) throws OXException {
        T result;
        IDBasedCalendarAccess calendarAccess = initCalendarAccess(session);
        boolean committed = false;
        try {
            calendarAccess.startTransaction();
            result = perform(calendarAccess);
            calendarAccess.commit();
            committed = true;
        } finally {
            if (false == committed) {
                calendarAccess.rollback();
            }
            calendarAccess.finish();
        }
        return result;
    }

    /**
     * Initializes the calendar access for CalDAV operations and applies default parameters.
     *
     * @param session The underlying session
     * @return The initialized calendar access
     */
    protected IDBasedCalendarAccess initCalendarAccess(Session session) throws OXException {
        IDBasedCalendarAccess calendarAccess = requireService(IDBasedCalendarAccessFactory.class, services).createAccess(session);
        calendarAccess.set(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, Boolean.FALSE);
        calendarAccess.set(CalendarParameters.PARAMETER_CHECK_CONFLICTS, Boolean.FALSE);
        calendarAccess.set(CalendarParameters.PARAMETER_IGNORE_STORAGE_WARNINGS, Boolean.TRUE);
        calendarAccess.set(CalendarParameters.PARAMETER_TRACK_ATTENDEE_USAGE, Boolean.TRUE);
        calendarAccess.set(CalendarParameters.PARAMETER_IGNORE_FORBIDDEN_ATTENDEE_CHANGES, Boolean.TRUE);
        calendarAccess.set(CalendarParameters.PARAMETER_SKIP_EXTERNAL_ATTENDEE_URI_CHECKS, Boolean.TRUE);
        applySuppressScheduleReply(calendarAccess);
        return calendarAccess;
    }

    /**
     * Performs the operation using the initialized calendar access.
     *
     * @param access The initialized calendar access to use
     * @return The result
     */
    protected abstract T perform(IDBasedCalendarAccess access) throws OXException;

    /**
     * Configures the supplied calendar access instance to suppress scheduling message for the current operation, based on the value of
     * the supplied <code>Schedule-Reply</code>-header of the underlying request.
     *
     * @see <a href="https://tools.ietf.org/html/rfc6638#section-8.1">RFC 6638, section 8.1</a>
     */
    private static void applySuppressScheduleReply(IDBasedCalendarAccess access) {
        /*
         * evaluate "Schedule-Reply" and "Scheduling" headers if present, then set calendar parameter accordingly
         */
        WebDAVRequestContext requestContext = DAVProtocol.getRequestContext();
        if (null != requestContext) {
            if ("F".equals(requestContext.getHeader("Schedule-Reply"))) {
                access.set(CalendarParameters.PARAMETER_SCHEDULING, SchedulingControl.NONE);
            }
            String schedulingHeader = requestContext.getHeader("Scheduling");
            if (Strings.isNotEmpty(schedulingHeader)) {
                access.set(CalendarParameters.PARAMETER_SCHEDULING, new SchedulingControl(schedulingHeader));
            }
        }
    }

}
