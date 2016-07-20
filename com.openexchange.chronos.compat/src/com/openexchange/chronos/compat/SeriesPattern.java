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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.compat;

import java.util.Calendar;
import java.util.TimeZone;
import com.openexchange.java.Strings;

/**
 * {@link SeriesPattern}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class SeriesPattern {

    private Integer type;
    private Integer interval;
    private Integer daysOfWeek;
    private Integer dayOfMonth;
    private Integer month;
    private Integer occurrences;
    private Long seriesStart;
    private Long seriesEnd;
    private TimeZone tz;
    private Boolean fullTime;

    /**
     * Parses a series pattern from its database representation.
     *
     * @param databasePattern The database pattern string
     * @return The parsed series pattern, or <code>null</code> if the supplied pattern is empty
     */
    public static SeriesPattern parse(String databasePattern, TimeZone tz, Boolean fullTime) {
        if (Strings.isEmpty(databasePattern)) {
            return null;
        }
        SeriesPattern pattern = new SeriesPattern();
        String[] splitted = databasePattern.split("\\|");
        for (int i = 1; i < splitted.length; i += 2) {
            String key = splitted[i - 1];
            String value = splitted[i];
            switch (key) {
                case "t":
                    pattern.type = Integer.valueOf(value);
                    break;
                case "i":
                    pattern.interval = Integer.valueOf(value);
                    break;
                case "a":
                    pattern.daysOfWeek = Integer.valueOf(value);
                    break;
                case "b":
                    pattern.dayOfMonth = Integer.valueOf(value);
                    break;
                case "c":
                    pattern.month = Integer.valueOf(value);
                    break;
                case "o":
                    pattern.occurrences = Integer.valueOf(value);
                    break;
                case "s":
                    pattern.seriesStart = Long.valueOf(value);
                    break;
                case "e":
                    pattern.seriesEnd = Long.valueOf(value);
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected key: " + key);
            }
        }
        pattern.tz = tz;
        pattern.fullTime = fullTime;
        return pattern;
    }

    /**
     * Initializes a new, empty {@link SeriesPattern}.
     */
    public SeriesPattern() {
        super();
    }

    /**
     * Initializes a new {@link SeriesPattern}.
     *
     * @param type The recurrence type
     * @param timeZone The timezone of the event series
     * @param allDay <code>true</code> for an "all-day" event series, <code>false</code>, otherwise
     */
    public SeriesPattern(int type, String timeZone, boolean allDay) {
        super();
        this.type = type;
        this.tz = TimeZone.getTimeZone(timeZone);
        this.fullTime = Boolean.valueOf(allDay);
    }

    private void deserialize(String pattern) throws IllegalArgumentException {
        String[] splitted = pattern.split("\\|");
        for (int i = 1; i < splitted.length; i += 2) {
            String key = splitted[i - 1];
            String value = splitted[i];
            switch (key) {
                case "t":
                    type = Integer.valueOf(value);
                    break;
                case "i":
                    interval = Integer.valueOf(value);
                    break;
                case "a":
                    daysOfWeek = Integer.valueOf(value);
                    break;
                case "b":
                    dayOfMonth = Integer.valueOf(value);
                    break;
                case "c":
                    month = Integer.valueOf(value);
                    break;
                case "o":
                    occurrences = Integer.valueOf(value);
                    break;
                case "s":
                    seriesStart = Long.valueOf(value);
                    break;
                case "e":
                    seriesEnd = Long.valueOf(value);
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected key: " + key);
            }
        }
    }

    /**
     * @return the type
     */
    public Integer getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(Integer type) {
        this.type = type;
    }

    /**
     * @return the interval
     */
    public Integer getInterval() {
        return interval;
    }

    /**
     * @param interval the interval to set
     */
    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    /**
     * @return the daysOfWeek
     */
    public Integer getDaysOfWeek() {
        return daysOfWeek;
    }

    /**
     * @param daysOfWeek the daysOfWeek to set
     */
    public void setDaysOfWeek(Integer daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    /**
     * @return the dayOfMonth
     */
    public Integer getDayOfMonth() {
        return dayOfMonth;
    }

    /**
     * @param dayOfMonth the dayOfMonth to set
     */
    public void setDayOfMonth(Integer dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    /**
     * @return the month
     */
    public Integer getMonth() {
        return month;
    }

    /**
     * @param month the month to set
     */
    public void setMonth(Integer month) {
        this.month = month;
    }

    /**
     * @return the occurrences
     */
    public Integer getOccurrences() {
        return occurrences;
    }

    /**
     * @param occurrences the occurrences to set
     */
    public void setOccurrences(Integer occurrences) {
        this.occurrences = occurrences;
    }

    /**
     * @return the seriesStart
     */
    public Long getSeriesStart() {
        return seriesStart;
    }

    /**
     * @param seriesStart the seriesStart to set
     */
    public void setSeriesStart(Long seriesStart) {
        this.seriesStart = seriesStart;
    }

    /**
     * @return the seriesEnd
     */
    public Long getSeriesEnd() {
        return seriesEnd;
    }

    /**
     * @param seriesEnd the seriesEnd to set
     */
    public void setSeriesEnd(Long seriesEnd) {
        this.seriesEnd = seriesEnd;
    }

    /**
     * Gets the time zone
     *
     * @return The time zone
     */
    public TimeZone getTimeZone() {
        return tz;
    }

    /**
     * @param tz The TimeZone to set
     */
    public void setTz(TimeZone tz) {
        this.tz = tz;
    }

    /**
     * @return true if full time, false otherwise
     */
    public Boolean isFullTime() {
        return fullTime;
    }

    /**
     * @param fullTime
     */
    public void setFullTime(Boolean fullTime) {
        this.fullTime = fullTime;
    }

    /**
     * Gets the series start as Calendar with proper TimeZone.
     *
     * @return
     */
    public Calendar getSeriesStartCalendar() {
        Calendar retval = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        retval.setTimeInMillis(getSeriesStart());
        return retval;
    }

    /**
     * Gets the series end as Calendar with proper TimeZone.
     *
     * @return
     */
    public Calendar getSeriesEndCalendar() {
        Calendar retval = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        retval.setTimeInMillis(getSeriesEnd());
        return retval;
    }

    /**
     * Gets the database string representation of this pattern.
     *
     * @return The database pattern string
     */
    public String getDatabasePattern() {
        StringBuilder stringBuilder = new StringBuilder().append("t|").append(type);
        if (null != interval) {
            stringBuilder.append("|i|").append(interval);
        }
        if (null != daysOfWeek) {
            stringBuilder.append("|a|").append(daysOfWeek);
        }
        if (null != dayOfMonth) {
            stringBuilder.append("|b|").append(dayOfMonth);
        }
        if (null != month) {
            stringBuilder.append("|c|").append(month);
        }
        if (null != occurrences) {
            stringBuilder.append("|o|").append(occurrences);
        }
        if (null != seriesStart) {
            stringBuilder.append("|s|").append(seriesStart);
        }
        if (null != seriesEnd) {
            stringBuilder.append("|e|").append(seriesEnd);
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return getDatabasePattern();
    }

}
