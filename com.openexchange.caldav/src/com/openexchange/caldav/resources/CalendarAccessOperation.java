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

package com.openexchange.caldav.resources;

import static com.openexchange.osgi.Tools.requireService;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccessFactory;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.exception.OXException;
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
         * evaluate "Schedule-Reply" header if present, then set calendar parameter accordingly
         */
        WebDAVRequestContext requestContext = DAVProtocol.getRequestContext();
        if (null != requestContext && "F".equals(requestContext.getHeader("Schedule-Reply"))) {
            access.set(CalendarParameters.PARAMETER_SUPPRESS_ITIP, Boolean.TRUE);
        }
    }

}
