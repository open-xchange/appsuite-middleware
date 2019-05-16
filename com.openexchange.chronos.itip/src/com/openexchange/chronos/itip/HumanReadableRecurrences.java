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

package com.openexchange.chronos.itip;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.dmfs.rfc5545.Weekday;
import org.dmfs.rfc5545.recur.Freq;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRule.Part;
import org.dmfs.rfc5545.recur.RecurrenceRule.WeekdayNum;
import com.openexchange.chronos.Event;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Autoboxing;

/**
 * {@link HumanReadableRecurrences}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class HumanReadableRecurrences {

    private RecurrenceRule rrule;
    private Locale locale;

    public HumanReadableRecurrences(Event event, Locale locale) {
        this.locale = locale;

        try {
            rrule = new RecurrenceRule(event.getRecurrenceRule());
        } catch (InvalidRecurrenceRuleException e) {
            rrule = null;
        }
    }

    public String getString() {
        if (rrule == null) {
            return no();
        }

        Freq freq = rrule.getFreq();
        switch (freq) {
            case DAILY:
                return daily();
            case WEEKLY:
                return weekly();
            case MONTHLY:
                return monthly();
            case YEARLY:
                return yearly();
            default:
                return no();
        }
    }

    public String getEnd() {
        if (rrule.getCount() != null) {
            return format(locale, HRRStrings.OCCURRENCES, rrule.getCount());
        }

        if (rrule.getUntil() != null) {
            return format(locale, HRRStrings.UNTIL, DateFormat.getDateInstance(DateFormat.FULL, locale).format(new Date(rrule.getUntil().getTimestamp())));
        }

        return format(locale, HRRStrings.FOREVER);
    }

    private String no() {
        return format(locale, HRRStrings.NO);
    }

    private String yearly() {
        List<WeekdayNum> byDayPart = rrule.getByDayPart();
        if (byDayPart != null) {
            if (byDayPart.size() > 1) {
                return no();
            } else {
                return format(locale, HRRStrings.YEARLY_1, parseCount(locale), parseDays(locale), parseMonth(locale));
            }
        } else {
            List<Integer> byMonthDay = rrule.getByPart(Part.BYMONTHDAY);
            if (byMonthDay.size() > 1) {
                return no();
            } else {
                return format(locale, HRRStrings.YEARLY_2, byMonthDay.get(0), parseMonth(locale));
            }
        }
    }

    private String monthly() {
        List<WeekdayNum> byDayPart = rrule.getByDayPart();
        if (byDayPart != null) {
            if (byDayPart.size() > 1) {
                return no();
            } else {
                return format(locale, HRRStrings.MONTHLY_2, parseCount(locale), parseDays(locale), Autoboxing.I(rrule.getInterval()));
            }
        } else {
            List<Integer> byMonthDay = rrule.getByPart(Part.BYMONTHDAY);
            if (byMonthDay.size() > 1) {
                return no();
            } else {
                return format(locale, HRRStrings.MONTHLY_1, byMonthDay.get(0), Autoboxing.I(rrule.getInterval()));
            }
        }
    }

    private String weekly() {
        if (rrule.getInterval() == 1) {
            return format(locale, HRRStrings.WEEKLY_EACH, parseDays(locale));
        }
        return format(locale, HRRStrings.WEEKLY, Autoboxing.I(rrule.getInterval()), parseDays(locale));
    }

    private String daily() {
        return format(locale, HRRStrings.DAILY, Autoboxing.I(rrule.getInterval()));
    }

    private String parseDays(Locale locale) {
        List<WeekdayNum> byDayPart = rrule.getByDayPart();
        Set<Weekday> weekdays = new HashSet<>();
        for (WeekdayNum weekdayNum : byDayPart) {
            weekdays.add(weekdayNum.weekday);
        }

        if (weekdays.size() == 5 && weekdays.containsAll(Arrays.asList(new Weekday[] { Weekday.MO, Weekday.TU, Weekday.WE, Weekday.TH, Weekday.FR }))) {
            return format(locale, HRRStrings.WORK_DAY);
        }

        if (weekdays.size() == 2 && weekdays.containsAll(Arrays.asList(new Weekday[] { Weekday.SA, Weekday.SU }))) {
            return format(locale, HRRStrings.WEEKEND_DAY);
        }

        if (weekdays.size() == 7 && weekdays.containsAll(Arrays.asList(Weekday.values()))) {
            return format(locale, HRRStrings.DAY);
        }

        StringBuilder days = new StringBuilder();

        if (weekdays.contains(Weekday.MO)) {
            days.append(format(locale, HRRStrings.MONDAY)).append(", ");
        }
        if (weekdays.contains(Weekday.TU)) {
            days.append(format(locale, HRRStrings.TUESDAY)).append(", ");
        }
        if (weekdays.contains(Weekday.WE)) {
            days.append(format(locale, HRRStrings.WEDNESDAY)).append(", ");
        }
        if (weekdays.contains(Weekday.TH)) {
            days.append(format(locale, HRRStrings.THURSDAY)).append(", ");
        }
        if (weekdays.contains(Weekday.FR)) {
            days.append(format(locale, HRRStrings.FRIDAY)).append(", ");
        }
        if (weekdays.contains(Weekday.SA)) {
            days.append(format(locale, HRRStrings.SATURDAY)).append(", ");
        }
        if (weekdays.contains(Weekday.SU)) {
            days.append(format(locale, HRRStrings.SUNDAY)).append(", ");
        }

        return days.substring(0, days.length() - 2);
    }

    private String parseMonth(Locale locale) {
        List<Integer> byMonth = rrule.getByPart(Part.BYMONTH);
        if (byMonth.size() > 1) {
            return no();
        }

        switch (Autoboxing.i(byMonth.get(0))) {
            case Calendar.JANUARY:
                return format(locale, HRRStrings.JANUARY);
            case Calendar.FEBRUARY:
                return format(locale, HRRStrings.FEBRUARY);
            case Calendar.MARCH:
                return format(locale, HRRStrings.MARCH);
            case Calendar.APRIL:
                return format(locale, HRRStrings.APRIL);
            case Calendar.MAY:
                return format(locale, HRRStrings.MAY);
            case Calendar.JUNE:
                return format(locale, HRRStrings.JUNE);
            case Calendar.JULY:
                return format(locale, HRRStrings.JULY);
            case Calendar.AUGUST:
                return format(locale, HRRStrings.AUGUST);
            case Calendar.SEPTEMBER:
                return format(locale, HRRStrings.SEPTEMBER);
            case Calendar.OCTOBER:
                return format(locale, HRRStrings.OCTOBER);
            case Calendar.NOVEMBER:
                return format(locale, HRRStrings.NOVEMBER);
            case Calendar.DECEMBER:
                return format(locale, HRRStrings.DECEMBER);
            default:
                return "";
        }
    }

    private Object parseCount(Locale locale) {
        List<WeekdayNum> byDayPart = rrule.getByDayPart();
        if (byDayPart.size() != 1) {
            return "";
        }

        switch (byDayPart.get(0).pos) {
            case 1:
                return format(locale, HRRStrings.FIRST);
            case 2:
                return format(locale, HRRStrings.SECOND);
            case 3:
                return format(locale, HRRStrings.THIRD);
            case 4:
                return format(locale, HRRStrings.FOURTH);
            case 5:
            case -1:
                return format(locale, HRRStrings.LAST);
            default:
                return "";
        }
    }

    private String format(Locale locale, String format, Object... args) {
        return String.format(saneFormatString(StringHelper.valueOf(locale).getString(format)), args);
    }

    private static final Pattern SANE_FORMAT = Pattern.compile("(%[0-9]+)?" + Pattern.quote("$") + "(\\s|$)");

    private static String saneFormatString(final String format) {
        if (com.openexchange.java.Strings.isEmpty(format) || format.indexOf('$') < 0) {
            return format;
        }
        return SANE_FORMAT.matcher(format).replaceAll("$1" + com.openexchange.java.Strings.quoteReplacement("$s") + "$2");
    }
}
