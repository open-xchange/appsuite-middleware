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

package com.openexchange.data.conversion.ical.ical4j.internal;

import java.util.TimeZone;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.util.TimeZones;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.calendar.RecurringResultsInterface;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class EmitterTools {

    private static final Log LOG = LogFactory.getLog(EmitterTools.class);

    private static CalendarCollectionService calendarCollection;

    /**
     * Prevent instantiation.
     */
    private EmitterTools() {
        super();
    }

    /**
     * TODO add default timezone
     */
    public static DateTime toDateTime(final java.util.Date date) {
        final DateTime retval = new DateTime(true);
        retval.setTime(date.getTime());
        return retval;
    }

    public static net.fortuna.ical4j.model.Date toDate(final java.util.Date date) {
        return new UTCDate(date.getTime());
    }

    /**
     * {@link Date} normally uses the JVM default time zone. This shifts whole
     * day appointments one day earlier for {@link TimeZone}s that have a
     * negative offset. This has no effect for {@link TimeZone}s that have a
     * positive offset because the time is stripped.
     */
    private static final class UTCDate extends net.fortuna.ical4j.model.Date {

        private static final long serialVersionUID = -4317836084736029187L;

        public UTCDate(final long time) {
            super();
            getFormat().setTimeZone(TimeZone.getTimeZone(TimeZones.UTC_ID));
            setTime(time);
        }
    }

    public static java.util.Date calculateExactTime(final CalendarDataObject appointment, final java.util.Date exception) {
        java.util.Date retval = exception;
        try {
            final RecurringResultsInterface rrs = calendarCollection.calculateRecurring(
                appointment,
                calendarCollection.normalizeLong(exception.getTime() - Constants.MILLI_WEEK),
                calendarCollection.normalizeLong(exception.getTime() + Constants.MILLI_WEEK),
                0,
                CalendarCollectionService.MAXTC,
                true);
            final int recurrencePosition = rrs.getPositionByLong(exception.getTime());
            if (recurrencePosition > 0) {
                retval = new java.util.Date(rrs.getRecurringResultByPosition(recurrencePosition).getStart());
            }
        } catch (final OXException e) {
            LOG.warn(e.getMessage(), e);
        }
        return retval;
    }

    public static void setCalendarCollection(CalendarCollectionService calendarCollection) {
        EmitterTools.calendarCollection = calendarCollection;
    }
}
