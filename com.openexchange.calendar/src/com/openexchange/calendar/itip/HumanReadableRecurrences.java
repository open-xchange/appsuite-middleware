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

package com.openexchange.calendar.itip;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.i18n.tools.StringHelper;

/**
 * {@link HumanReadableRecurrences}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class HumanReadableRecurrences {

    private final CalendarObject cdao;

    public HumanReadableRecurrences(CalendarObject cdao) {
        this.cdao = cdao;
    }

    public String getString(Locale locale) {
        switch (cdao.getRecurrenceType()) {
        case CalendarObject.DAILY:
            return daily(locale);
        case CalendarObject.WEEKLY:
            return weekly(locale);
        case CalendarObject.MONTHLY:
            return monthly(locale);
        case CalendarObject.YEARLY:
            return yearly(locale);
        case CalendarObject.NO_RECURRENCE:
        default:
            return no(locale);
        }
    }

    public String getEnd(Locale locale) {
        if (cdao.containsOccurrence() && cdao.getOccurrence() > 0) {
            return format(locale, HRRStrings.OCCURRENCES, cdao.getOccurrence());
        }

        if (cdao.containsUntil() && cdao.getUntil() != null) {
            return format(locale, HRRStrings.UNTIL, DateFormat.getDateInstance(DateFormat.FULL, locale).format(cdao.getUntil()));
        }

        return format(locale, HRRStrings.FOREVER);
    }

    private String no(Locale locale) {
        return format(locale, HRRStrings.NO);
    }

    private String yearly(Locale locale) {
        if (cdao.containsDays()) {
            return format(locale, HRRStrings.YEARLY_1, parseCount(locale), parseDays(locale), parseMonth(locale));
        }
        return format(locale, HRRStrings.YEARLY_2, cdao.getDayInMonth(), parseMonth(locale));
    }

    private String monthly(Locale locale) {
        if (cdao.containsDays()) {
            return format(locale, HRRStrings.MONTHLY_2, parseCount(locale), parseDays(locale), cdao.getInterval());
        }
        return format(locale, HRRStrings.MONTHLY_1, cdao.getDayInMonth(), cdao.getInterval());
    }

    private String weekly(Locale locale) {
        if (cdao.getInterval() == 1) {
            return format(locale, HRRStrings.WEEKLY_EACH, parseDays(locale));
        }
        return format(locale, HRRStrings.WEEKLY, cdao.getInterval(), parseDays(locale));
    }

    private String daily(Locale locale) {
        return format(locale, HRRStrings.DAILY, cdao.getInterval());
    }

    private String parseDays(Locale locale) {
        if (cdao.getDays() < 1 || cdao.getDays() > 127) {
            return "";
        }

        if (cdao.getDays() == CalendarObject.WEEKDAY) {
            return format(locale, HRRStrings.WORK_DAY);
        }

        if (cdao.getDays() == CalendarObject.WEEKENDDAY) {
            return format(locale, HRRStrings.WEEKEND_DAY);
        }

        if (cdao.getDays() == CalendarObject.DAY) {
            return format(locale, HRRStrings.DAY);
        }

        StringBuilder days = new StringBuilder();

        if ((cdao.getDays() & CalendarObject.MONDAY) == CalendarObject.MONDAY) {
            days.append(format(locale, HRRStrings.MONDAY)).append(", ");
        }
        if ((cdao.getDays() & CalendarObject.TUESDAY) == CalendarObject.TUESDAY) {
            days.append(format(locale, HRRStrings.TUESDAY)).append(", ");
        }
        if ((cdao.getDays() & CalendarObject.WEDNESDAY) == CalendarObject.WEDNESDAY) {
            days.append(format(locale, HRRStrings.WEDNESDAY)).append(", ");
        }
        if ((cdao.getDays() & CalendarObject.THURSDAY) == CalendarObject.THURSDAY) {
            days.append(format(locale, HRRStrings.THURSDAY)).append(", ");
        }
        if ((cdao.getDays() & CalendarObject.FRIDAY) == CalendarObject.FRIDAY) {
            days.append(format(locale, HRRStrings.FRIDAY)).append(", ");
        }
        if ((cdao.getDays() & CalendarObject.SATURDAY) == CalendarObject.SATURDAY) {
            days.append(format(locale, HRRStrings.SATURDAY)).append(", ");
        }
        if ((cdao.getDays() & CalendarObject.SUNDAY) == CalendarObject.SUNDAY) {
            days.append(format(locale, HRRStrings.SUNDAY)).append(", ");
        }

        return days.substring(0, days.length() - 2);
    }

    private String parseMonth(Locale locale) {
        switch (cdao.getMonth()) {
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
        switch (cdao.getDayInMonth()) {
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
