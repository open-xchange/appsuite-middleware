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

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.DateTime;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ParserTools {

    /**
     * Prevent instantiation.
     */
    private ParserTools() {
        super();
    }

    public static final Date parseDate(final CalendarComponent component,
        final DateProperty property, final TimeZone timeZone) {
        final DateProperty value = (DateProperty) component.getProperty(
            property.getName());
        Date retval = new Date(value.getDate().getTime());
        if (inDefaultTimeZone(value, timeZone)) {
            retval = recalculate(retval, timeZone);
        }
        return retval;
    }

    public static boolean inDefaultTimeZone(final DateProperty dateProperty,
        final TimeZone timeZone) {
        if (dateProperty.getParameter("TZID") != null) {
            return false;
        }
        return !dateProperty.isUtc();
    }

    /**
     * Transforms date from the default timezone to the date in the given timezone.
     */
    public static Date recalculate(final Date date, final TimeZone timeZone) {
    
        java.util.Calendar inDefault = new GregorianCalendar();
        inDefault.setTime(date);
    
        java.util.Calendar inTimeZone = new GregorianCalendar();
        inTimeZone.setTimeZone(timeZone);
        inTimeZone.set(inDefault.get(java.util.Calendar.YEAR),
            inDefault.get(java.util.Calendar.MONTH),
            inDefault.get(java.util.Calendar.DATE),
            inDefault.get(java.util.Calendar.HOUR_OF_DAY),
            inDefault.get(java.util.Calendar.MINUTE),
            inDefault.get(java.util.Calendar.SECOND));
        inTimeZone.set(java.util.Calendar.MILLISECOND, 0);
        return inTimeZone.getTime();
    }

    public static Date toDate(DateProperty dateProperty, TimeZone tz) {
        Date date = new Date(dateProperty.getDate().getTime());
        if (ParserTools.inDefaultTimeZone(dateProperty, tz)) {
            date = ParserTools.recalculate(date, tz);
        }
        return date;
    }

    public static Date recalculateAsNeeded(net.fortuna.ical4j.model.Date icaldate, Property property, TimeZone tz) {
        boolean mustRecalculate = true;
        if(property.getParameter("TZID") != null) {
            mustRecalculate = false;
        } else if(DateTime.class.isAssignableFrom(icaldate.getClass())) {
            DateTime dateTime = (DateTime) icaldate;
            mustRecalculate = !dateTime.isUtc();
        }
        if(mustRecalculate) {
            return ParserTools.recalculate(icaldate, tz);
        }
        return new Date(icaldate.getTime());
    }
}
