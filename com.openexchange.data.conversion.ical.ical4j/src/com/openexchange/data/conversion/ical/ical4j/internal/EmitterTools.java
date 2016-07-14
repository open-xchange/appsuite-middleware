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

package com.openexchange.data.conversion.ical.ical4j.internal;

import java.util.Date;
import java.util.TimeZone;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.util.TimeZones;
import net.fortuna.ical4j.zoneinfo.outlook.OutlookTimeZoneRegistryFactory;
import com.openexchange.data.conversion.ical.ZoneInfo;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class EmitterTools {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(EmitterTools.class);
    private static volatile CalendarCollectionService calendarCollection;

    private final TimeZoneRegistry registry;

    public EmitterTools(ZoneInfo zoneInfo) {
        super();
        registry = getRegistry(zoneInfo);
    }

    private static final TimeZoneRegistry getRegistry(final ZoneInfo zoneInfo) {
        final TimeZoneRegistry retval;
        switch (zoneInfo) {
        case OUTLOOK:
            retval = new OutlookTimeZoneRegistryFactory().createRegistry();
            break;
        case FULL:
        default:
            retval = TimeZoneRegistryFactory.getInstance().createRegistry();
            break;
        }
        return retval;
    }

    /**
     * TODO add default timezone
     */
    public static DateTime toDateTime(final java.util.Date date) {
    	final DateTime retval = new DateTime(true);
        retval.setTime(date.getTime());
        return retval;
    }

    public DateTime toDateTime(final java.util.Date date, final String tzid) {
        return toDateTime(registry, date, tzid);
    }

    public static DateTime toDateTime(final ZoneInfo zoneInfo, final java.util.Date date, final String tzid) {
        return toDateTime(getRegistry(zoneInfo), date, tzid);
    }

    private static DateTime toDateTime(final TimeZoneRegistry registry, final java.util.Date date, final String tzid) {
        if (null == tzid) {
            return toDateTime(date);
        }
        net.fortuna.ical4j.model.TimeZone ical4jTimezone = registry.getTimeZone(tzid);
        if (null == ical4jTimezone) {
            return toDateTime(date);
        }
        final DateTime retval = new DateTime(false);
        retval.setTimeZone(ical4jTimezone);
        retval.setTime(date.getTime());
        return retval;
    }

    public static net.fortuna.ical4j.model.Date toDate(final java.util.Date date) {
        return new UTCDate(date.getTime());
    }

    public static net.fortuna.ical4j.model.Date toDate(final java.util.Date date, String tzid) {
    	if(tzid == null) {
            return toDate(date);
        }
        return new TzDate(date.getTime(), tzid);
    }

    public static String extractTimezoneIfPossible(CalendarObject co){
    	if(Appointment.class.isAssignableFrom(co.getClass())) {
            return ((Appointment) co).getTimezone();
        }
    	return null;
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

    private static final class TzDate extends net.fortuna.ical4j.model.Date {

        private static final long serialVersionUID = -4317836084736029666L;

        public TzDate(final long time, final String tzid) {
            super();
            getFormat().setTimeZone(TimeZone.getTimeZone(tzid));
            setTime(time);
        }
    }

    public static java.util.Date calculateExactTime(final CalendarDataObject appointment, final java.util.Date exception) {
        java.util.Date retval = exception;
        try {
            final CalendarCollectionService service = calendarCollection;
            final RecurringResultsInterface rrs = service.calculateRecurring(
                appointment,
                service.normalizeLong(exception.getTime() - Constants.MILLI_WEEK),
                service.normalizeLong(exception.getTime() + Constants.MILLI_WEEK),
                0,
                CalendarCollectionService.MAX_OCCURRENCESE,
                true);
            final int recurrencePosition = rrs.getPositionByLong(exception.getTime());
            if (recurrencePosition > 0) {
                retval = new java.util.Date(rrs.getRecurringResultByPosition(recurrencePosition).getStart());
            }
        } catch (final OXException e) {
            LOG.warn("", e);
        }
        return retval;
    }

    public static void setCalendarCollection(final CalendarCollectionService calendarCollection) {
        EmitterTools.calendarCollection = calendarCollection;
    }

    public TimeZoneRegistry getTimeZoneRegistry() {
        return registry;
    }
}
