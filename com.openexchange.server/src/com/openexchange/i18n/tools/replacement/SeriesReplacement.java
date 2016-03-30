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

package com.openexchange.i18n.tools.replacement;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.i18n.Notifications;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.i18n.tools.TemplateReplacement;
import com.openexchange.i18n.tools.TemplateToken;
import com.openexchange.tools.TimeZoneUtils;

/**
 * {@link SeriesReplacement} - The replacement for series information
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SeriesReplacement extends LocalizedStringReplacement {

    private static final String[] MONTHS = {
        Notifications.REC_JAN, Notifications.REC_FEB, Notifications.REC_MARCH, Notifications.REC_APRIL, Notifications.REC_MAY,
        Notifications.REC_JUNE, Notifications.REC_JULY, Notifications.REC_AUG, Notifications.REC_SEP, Notifications.REC_OCT,
        Notifications.REC_NOV, Notifications.REC_DEC };

    private static final String[] WEEKDAYS = {
        Notifications.REC_MONDAY, Notifications.REC_TUESDAY, Notifications.REC_WEDNESDAY, Notifications.REC_THURSDAY,
        Notifications.REC_FRIDAY, Notifications.REC_SATURDAY, Notifications.REC_SUNDAY, Notifications.REC_DAY, Notifications.REC_WEEKDAY,
        Notifications.REC_WEEKENDDAY };

    private static final String[] MONTHLY_DAY = {
        Notifications.REC_FIRST, Notifications.REC_SECOND, Notifications.REC_THIRD, Notifications.REC_FOURTH, Notifications.REC_LAST };

    private CalendarObject calendarObject;

    private boolean isTask;

    /**
     * Initializes a new {@link SeriesReplacement}
     *
     * @param calendarObject The calendar object
     * @param isTask <code>true</code> if calendar object denotes a task; otherwise <code>false</code> for an appointment
     */
    public SeriesReplacement(final CalendarObject calendarObject, final boolean isTask) {
        super(TemplateToken.SERIES, null);
        this.calendarObject = calendarObject;
        this.isTask = isTask;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final SeriesReplacement clone = (SeriesReplacement) super.clone();
        // Shallow copy
        clone.calendarObject = calendarObject;
        return clone;
    }

    @Override
    public TemplateReplacement getClone() throws CloneNotSupportedException {
        return (TemplateReplacement) clone();
    }

    @Override
    public boolean merge(final TemplateReplacement other) {
        if (!SeriesReplacement.class.isInstance(other)) {
            /*
             * Class mismatch
             */
            return false;
        }
        if (super.merge(other)) {
            final SeriesReplacement o = (SeriesReplacement) other;
            this.calendarObject = o.calendarObject;
            this.isTask = o.isTask;
        }
        return false;
    }

    /**
     * Gets the series information or an empty string if recurrence type indicates no recurrence.
     */
    @Override
    public String getReplacement() {
        if (calendarObject.getRecurrenceType() == CalendarObject.NO_RECURRENCE) {
            return "";
        }
        final String repl = format(getSeriesString());
        return new StringBuilder(repl.length() + 1).append(repl).append('\n').toString();
    }

    private String getSeriesString() {
        if (calendarObject.getRecurrenceType() == CalendarObject.NO_RECURRENCE) {
            return getStringHelper().getString(Notifications.NO_SERIES);
        }
        final StringHelper stringHelper = getStringHelper();
        /*
         * Check if recurrence ends never, on a certain occurrence, or on a certain date
         */
        final String appendix;
        if (calendarObject.containsOccurrence() && calendarObject.getOccurrence() > 0) {
            // Ends on a certain occurrence
            appendix = String.format(
                stringHelper.getString(isTask ? Notifications.REC_ENDS_TASK : Notifications.REC_ENDS_APPOINTMENT),
                Integer.valueOf(calendarObject.getOccurrence()));
        } else if (!calendarObject.containsOccurrence() && calendarObject.containsUntil() && calendarObject.getUntil() != null) {
            // Ends on a certain date
            appendix = String.format(stringHelper.getString(Notifications.REC_ENDS_UNTIL), getDateFormat(locale).format(
                calendarObject.getUntil()));
        } else {
            // Ends never
            appendix = "";
        }
        /*
         * Compose string dependent on recurrence type
         */
        if (calendarObject.getRecurrenceType() == CalendarObject.DAILY) {
            if (calendarObject.getInterval() == 1) {
                return concat(stringHelper.getString(Notifications.REC_DAILY1), appendix);
            }
            return concat(
                String.format(stringHelper.getString(Notifications.REC_DAILY2), Integer.valueOf(calendarObject.getInterval())),
                appendix);
        }
        if (calendarObject.getRecurrenceType() == CalendarObject.WEEKLY) {
            if (calendarObject.getInterval() == 1) {
                return concat(String.format(stringHelper.getString(Notifications.REC_WEEKLY1), days2String(
                    calendarObject.getDays(),
                    stringHelper)), appendix);
            }
            return concat(String.format(
                stringHelper.getString(Notifications.REC_WEEKLY2),
                Integer.valueOf(calendarObject.getInterval()),
                days2String(calendarObject.getDays(), stringHelper)), appendix);
        }
        if (calendarObject.getRecurrenceType() == CalendarObject.MONTHLY) {
            if (calendarObject.getDays() <= 0) {
                if (calendarObject.getInterval() == 1) {
                    return concat(String.format(stringHelper.getString(Notifications.REC_MONTHLY1_1), ordinal(
                        calendarObject.getDayInMonth(),
                        stringHelper)), appendix);
                }
                return concat(String.format(stringHelper.getString(Notifications.REC_MONTHLY1_2), ordinal(
                    calendarObject.getDayInMonth(),
                    stringHelper), ordinal(calendarObject.getInterval(), stringHelper)), appendix);
            }
            if (calendarObject.getInterval() == 1) {
                return concat(String.format(stringHelper.getString(Notifications.REC_MONTHLY2_1), dayInMonth2String(
                    calendarObject.getDayInMonth(),
                    stringHelper), days2String(calendarObject.getDays(), stringHelper)), appendix);
            }
            return concat(
                String.format(stringHelper.getString(Notifications.REC_MONTHLY2_2), dayInMonth2String(
                    calendarObject.getDayInMonth(),
                    stringHelper), days2String(calendarObject.getDays(), stringHelper), ordinal(calendarObject.getInterval(), stringHelper)),
                appendix);
        }
        if (calendarObject.getRecurrenceType() == CalendarObject.YEARLY) {
            if (calendarObject.getDays() <= 0) {
                return concat(String.format(stringHelper.getString(Notifications.REC_YEARLY1), ordinal(
                    calendarObject.getDayInMonth(),
                    stringHelper), stringHelper.getString(MONTHS[calendarObject.getMonth()])), appendix);
            }
            return concat(String.format(
                stringHelper.getString(Notifications.REC_YEARLY2),
                dayInMonth2String(calendarObject.getDayInMonth(), stringHelper),
                days2String(calendarObject.getDays(), stringHelper),
                stringHelper.getString(MONTHS[calendarObject.getMonth()])), appendix);
        }
        return "";
    }

    private String format(final String seriesStr) {
        final String result = String.format(getStringHelper().getString(Notifications.FORMAT_SERIES), seriesStr);
        if (changed) {
            return new StringBuilder(PREFIX_MODIFIED.length() + result.length()).append(PREFIX_MODIFIED).append(result).toString();
        }
        return result;
    }

    private static final String days2String(final int days, final StringHelper stringHelper) {
        final List<String> dayStrings = new ArrayList<String>(10);
        if ((days & CalendarObject.MONDAY) == CalendarObject.MONDAY) {
            dayStrings.add(stringHelper.getString(WEEKDAYS[0]));
        }
        if ((days & CalendarObject.TUESDAY) == CalendarObject.TUESDAY) {
            dayStrings.add(stringHelper.getString(WEEKDAYS[1]));
        }
        if ((days & CalendarObject.WEDNESDAY) == CalendarObject.WEDNESDAY) {
            dayStrings.add(stringHelper.getString(WEEKDAYS[2]));
        }
        if ((days & CalendarObject.THURSDAY) == CalendarObject.THURSDAY) {
            dayStrings.add(stringHelper.getString(WEEKDAYS[3]));
        }
        if ((days & CalendarObject.FRIDAY) == CalendarObject.FRIDAY) {
            dayStrings.add(stringHelper.getString(WEEKDAYS[4]));
        }
        if ((days & CalendarObject.SATURDAY) == CalendarObject.SATURDAY) {
            dayStrings.add(stringHelper.getString(WEEKDAYS[5]));
        }
        if ((days & CalendarObject.SUNDAY) == CalendarObject.SUNDAY) {
            dayStrings.add(stringHelper.getString(WEEKDAYS[6]));
        }
        if ((days & CalendarObject.DAY) == CalendarObject.DAY) {
            dayStrings.add(stringHelper.getString(WEEKDAYS[7]));
        }
        if ((days & CalendarObject.WEEKDAY) == CalendarObject.WEEKDAY) {
            dayStrings.add(stringHelper.getString(WEEKDAYS[8]));
        }
        if ((days & CalendarObject.WEEKENDDAY) == CalendarObject.WEEKENDDAY) {
            dayStrings.add(stringHelper.getString(WEEKDAYS[9]));
        }

        final int msize = dayStrings.size() - 1;
        if (msize == -1) {
            return "";
        }
        if (msize == 0) {
            return dayStrings.get(0);
        }
        final StringBuilder b = new StringBuilder((msize + 1) * 9);
        b.append(dayStrings.get(0));
        for (int i = 1; i < msize; i++) {
            b.append(", ").append(dayStrings.get(i));
        }
        b.append(' ').append(stringHelper.getString(Notifications.REC_AND)).append(' ').append(dayStrings.get(msize));
        return b.toString();
    }

    private static String dayInMonth2String(final int dayInMonth, final StringHelper stringHelper) {
        return stringHelper.getString(MONTHLY_DAY[dayInMonth - 1]);
    }

    private static String ordinal(final int ordinal, final StringHelper stringHelper) {
        final StringBuilder sb = new StringBuilder(4).append(ordinal);
        if (1 == ordinal) {
            sb.append(stringHelper.getString(Notifications.REC_1ST_ORDINAL_APPENDIX));
        } else if (2 == ordinal) {
            sb.append(stringHelper.getString(Notifications.REC_2ND_ORDINAL_APPENDIX));
        } else if (3 == ordinal) {
            sb.append(stringHelper.getString(Notifications.REC_3RD_ORDINAL_APPENDIX));
        } else {
            sb.append(stringHelper.getString(Notifications.REC_ORDINAL_APPENDIX));
        }
        return sb.toString();
    }

    private static DateFormat getDateFormat(final Locale locale) {
        final DateFormat retval = locale == null ? DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.ENGLISH) : DateFormat.getDateInstance(
            DateFormat.DEFAULT,
            locale);
        retval.setTimeZone(TimeZoneUtils.getTimeZone("UTC"));
        return retval;
    }

    private static String concat(final String... strings) {
        int len = 0;
        for (final String string : strings) {
            len += string.length();
        }
        final StringBuilder sb = new StringBuilder(len);
        for (final String string : strings) {
            sb.append(string);
        }
        return sb.toString();
    }

    @Override
    public TemplateToken getToken() {
        return TemplateToken.SERIES;
    }
}
