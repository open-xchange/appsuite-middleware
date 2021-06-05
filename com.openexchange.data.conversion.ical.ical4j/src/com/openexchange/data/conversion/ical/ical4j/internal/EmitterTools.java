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

package com.openexchange.data.conversion.ical.ical4j.internal;

import java.util.Date;
import java.util.TimeZone;
import com.openexchange.data.conversion.ical.ZoneInfo;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.util.TimeZones;
import net.fortuna.ical4j.zoneinfo.outlook.OutlookTimeZoneRegistryFactory;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class EmitterTools {

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
    	if (tzid == null) {
            return toDate(date);
        }
        return new TzDate(date.getTime(), tzid);
    }

    public static String extractTimezoneIfPossible(CalendarObject co){
    	if (Appointment.class.isAssignableFrom(co.getClass())) {
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

    public TimeZoneRegistry getTimeZoneRegistry() {
        return registry;
    }
}
