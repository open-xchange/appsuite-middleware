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

package com.openexchange.chronos.compat;

import static com.openexchange.java.Autoboxing.I;

/**
 * {@link SeriesPattern}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class SeriesPattern {

    /**
     * The legacy constant dictating the maximum number of calculated occurrences. Needs to be considered when converting legacy series
     * patterns to recurrence rules and vice versa.
     *
     * @see com.openexchange.groupware.calendar.CalendarCollectionService.MAX_OCCURRENCESE
     */
    public static final int MAX_OCCURRENCESE = 999;

    /**
     * The legacy constant to indicate a "daily"-type series pattern.
     */
    public static final Integer DAILY = I(1);

    /**
     * The legacy constant to indicate a "weekly"-type series pattern.
     */
    public static final Integer WEEKLY = I(2);

    /**
     * The legacy constant to indicate a "monthly (each n-th day of month)"-type series pattern.
     */
    public static final Integer MONTHLY_1 = I(3);

    /**
     * The legacy constant to indicate a "yearly (each n-th day of a certain month)"-type series pattern.
     */
    public static final Integer YEARLY_1 = I(4);

    /**
     * The legacy constant to indicate a "monthly (a specific day of the n-th week of month)"-type series pattern.
     */
    public static final Integer MONTHLY_2 = I(5);

    /**
     * The legacy constant to indicate a "yearly (a specific day of the n-th week of a certain month)"-type series pattern.
     */
    public static final Integer YEARLY_2 = I(6);

    private Integer type;
    private Integer interval;
    private Integer daysOfWeek;
    private Integer dayOfMonth;
    private Integer month;
    private Integer occurrences;
    private Long seriesStart;
    private Long seriesEnd;

    /**
     * Initializes a new, empty {@link SeriesPattern}.
     */
    public SeriesPattern() {
        super();
    }

    /**
     * Initializes a new {@link SeriesPattern}.
     *
     * @param databasePattern The legacy, pipe-separated series pattern, e.g. <code>t|1|i|1|s|1313388000000|e|1313625600000|o|4|</code>
     * @throws IllegalArgumentException If input not parseable
     */
    public SeriesPattern(String databasePattern) throws IllegalArgumentException {
        super();
        deserialize(databasePattern);
    }

    /**
     * Initializes a new {@link SeriesPattern}.
     *
     * @param type The recurrence type
     */
    public SeriesPattern(int type) {
        super();
        this.type = I(type);
    }

    /**
     * Deserializes the supplied legacy series pattern.
     *
     * @param databasePattern The legacy, pipe-separated series pattern, e.g. <code>t|1|i|1|s|1313388000000|e|1313625600000|o|4|</code>
     * @throws IllegalArgumentException If input not parseable
     */
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
     * Gets the database string representation of this pattern.
     *
     * @return The database pattern string
     */
    public String getDatabasePattern() {
        StringBuilder stringBuilder = new StringBuilder().append("t|").append(type).append('|');
        if (null != interval) {
            stringBuilder.append("i|").append(interval).append('|');
        }
        if (null != daysOfWeek) {
            stringBuilder.append("a|").append(daysOfWeek).append('|');
        }
        if (null != dayOfMonth) {
            stringBuilder.append("b|").append(dayOfMonth).append('|');
        }
        if (null != month) {
            stringBuilder.append("c|").append(month).append('|');
        }
        if (null != occurrences) {
            stringBuilder.append("o|").append(occurrences).append('|');
        }
        if (null != seriesStart) {
            stringBuilder.append("s|").append(seriesStart).append('|');
        }
        if (null != seriesEnd) {
            stringBuilder.append("e|").append(seriesEnd).append('|');
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return getDatabasePattern();
    }

}
