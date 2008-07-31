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
package com.openexchange.data.conversion.ical.ical4j.internal.calendar;

import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.*;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.data.conversion.ical.ical4j.internal.ParserTools;
import static com.openexchange.data.conversion.ical.ical4j.internal.ParserTools.parseDate;
import static com.openexchange.data.conversion.ical.ical4j.internal.EmitterTools.toDateTime;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ConversionError;

import java.util.*;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class Recurrence<T extends CalendarComponent, U extends CalendarObject> extends AbstractVerifyingAttributeConverter<T,U> {

    private static final Map<String, Integer> weekdays = new HashMap<String, Integer>();
    static {
        weekdays.put("MO", AppointmentObject.MONDAY);
        weekdays.put("TU", AppointmentObject.TUESDAY);
        weekdays.put("WE", AppointmentObject.WEDNESDAY);
        weekdays.put("TH", AppointmentObject.THURSDAY);
        weekdays.put("FR", AppointmentObject.FRIDAY);
        weekdays.put("SA", AppointmentObject.SATURDAY);
        weekdays.put("SO", AppointmentObject.SUNDAY);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasProperty(final T component) {
        return null != component.getProperty("RRULE");
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSet(final U calendar) {
        return calendar.containsRecurrenceType();
    }

    public void emit(final U calendar, final T component, List<ConversionWarning> warnings) {
        //TODO
    }

    /**
     * {@inheritDoc}
     */
    public void parse(final T component, final U cObj, TimeZone timeZone, Context ctx, List<ConversionWarning> warnings) throws ConversionError {
        if(null == cObj.getStartDate()) {
            return;
        }
        Calendar startDate = new GregorianCalendar();
        startDate.setTime(cObj.getStartDate());

        PropertyList list = component.getProperties("RRULE");
        if(list.isEmpty()) {
            return;
        }
        if(list.size() > 1) {
            warnings.add(new ConversionWarning("Only converting first recurrence rule, additional recurrence rules will be ignored."));
        }
        Recur rrule = ((RRule) list.get(0)).getRecur();

        if("DAILY".equalsIgnoreCase(rrule.getFrequency())) {
            cObj.setRecurrenceType(AppointmentObject.DAILY);
        } else if ("WEEKLY".equalsIgnoreCase(rrule.getFrequency())) {
            cObj.setRecurrenceType(AppointmentObject.WEEKLY);
            setDays(cObj, rrule, startDate);
        } else if ("MONTHLY".equalsIgnoreCase(rrule.getFrequency())) {
            cObj.setRecurrenceType(AppointmentObject.MONTHLY);
            setMonthDay(cObj, rrule, startDate);
        } else if ("YEARLY".equalsIgnoreCase(rrule.getFrequency())) {
            cObj.setRecurrenceType(AppointmentObject.YEARLY);
            NumberList monthList = rrule.getMonthList();
            if(!monthList.isEmpty()) {
                cObj.setMonth((Integer)monthList.get(0) - 1);
                setMonthDay(cObj, rrule, startDate);
            } else {
                cObj.setMonth(startDate.get(Calendar.MONTH));
                setMonthDay(cObj, rrule, startDate);

            }


        } else {
            warnings.add(new ConversionWarning("Can only convert DAILY, WEEKLY, MONTHLY and YEARLY recurrences"));
        }
        cObj.setInterval(rrule.getInterval());
        int count = rrule.getCount();
        if(-1 != count) {
            cObj.setRecurrenceCount(rrule.getCount());
        } else {
            cObj.setUntil(ParserTools.recalculate(new Date(rrule.getUntil().getTime()), timeZone));
        }
    }

    private void setMonthDay(CalendarObject cObj, Recur rrule, Calendar startDate) throws ConversionError {
            NumberList monthDayList = rrule.getMonthDayList();
            if(!monthDayList.isEmpty()) {
                cObj.setDayInMonth((Integer)monthDayList.get(0));
            } else {
                NumberList weekNoList = rrule.getWeekNoList();
                if(!weekNoList.isEmpty()) {
                    int week = (Integer)weekNoList.get(0);
                    if(week == -1) { week = 5; }
                    cObj.setDayInMonth(week); // Day in month stores week
                    setDays(cObj, rrule, startDate);
                } else {
                    // Default to monthly series on specific day of month
                    cObj.setDayInMonth(startDate.get(Calendar.DAY_OF_MONTH));
                }
            }
        }

        private void setDays(CalendarObject cObj, Recur rrule, Calendar startDate) throws ConversionError {
            WeekDayList weekdayList = rrule.getDayList();
            if(!weekdayList.isEmpty()) {
                int days = 0;
                for(int i = 0, size = weekdayList.size(); i < size; i++) {
                    WeekDay weekday = (WeekDay) weekdayList.get(i);
                    Integer day = weekdays.get(weekday.getDay());
                    if(null == day) {
                        throw new ConversionError("Unknown day: %s", weekday.getDay());
                    }
                    days |= day;
                }
                cObj.setDays(days);
            } else {
                int day_of_week = startDate.get(Calendar.DAY_OF_WEEK);
                int days = -1;
                switch(day_of_week) {
                    case Calendar.MONDAY : days = AppointmentObject.MONDAY; break;
                    case Calendar.TUESDAY : days = AppointmentObject.TUESDAY; break;
                    case Calendar.WEDNESDAY : days = AppointmentObject.WEDNESDAY; break;
                    case Calendar.THURSDAY : days = AppointmentObject.THURSDAY; break;
                    case Calendar.FRIDAY : days = AppointmentObject.FRIDAY; break;
                    case Calendar.SATURDAY : days = AppointmentObject.SATURDAY; break;
                    case Calendar.SUNDAY : days = AppointmentObject.SUNDAY; break;
                }
                cObj.setDays(days);
            }
        }
    
}
