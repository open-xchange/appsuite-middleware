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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.gdpr.dataexport;

import com.openexchange.java.Strings;

/**
 * {@link TimeOfTheDay} - The time of the day for the 24-hour clock with second granularity; e.g. <code>16:04:15</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class TimeOfTheDay implements Comparable<TimeOfTheDay> {

    /**
     * Pares given time of the day; e.g. <code>16:04:15</code>.
     *
     * @param timeOfTheDay The time of the day to parse
     * @return The parsed instance
     */
    public static TimeOfTheDay parseFrom(String timeOfTheDay) {
        if (Strings.isEmpty(timeOfTheDay)) {
            throw new IllegalArgumentException("Time of the day must no be null or empty");
        }

        String[] tokens = Strings.splitBy(timeOfTheDay, ':', true);
        if (tokens.length > 3) {
            throw new IllegalArgumentException("Illegal time of the day: " + timeOfTheDay);
        }

        try {
            int hour = Integer.parseInt(tokens[0]);
            if (hour < 0 || hour > 24) {
                throw new IllegalArgumentException("Illegal time of the day: " + timeOfTheDay);
            }

            int minute = 0;
            int second = 0;
            if (tokens.length > 1) {
                minute = Integer.parseInt(tokens[1]);
                if (minute < 0 || minute > 59) {
                    throw new IllegalArgumentException("Illegal time of the day: " + timeOfTheDay);
                }

                if (tokens.length > 2) {
                    second = Integer.parseInt(tokens[2]);
                    if (minute < 0 || minute > 59) {
                        throw new IllegalArgumentException("Illegal time of the day: " + timeOfTheDay);
                    }
                }
            }

            return new TimeOfTheDay(hour, minute, second);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Illegal time of the day: " + timeOfTheDay, e);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    private final int hour;
    private final int minute;
    private final int second;
    private int hash;

    /**
     * Initializes a new {@link TimeOfTheDay}.
     *
     * @param atHour The hour of day of the 24-hour clock
     * @param atMinute The minute
     * @param atSecond The second
     */
    public TimeOfTheDay(int atHour, int atMinute, int atSecond) {
        super();
        this.hour = atHour;
        this.minute = atMinute;
        this.second = atSecond;
        hash = 0;
    }

    /**
     * Gets the hour of the day for the 24-hour clock.
     *
     * @return The hour of the day
     */
    public int getHour() {
        return hour;
    }

    /**
     * Gets the minute within the hour.
     *
     * @return The minute
     */
    public int getMinute() {
        return minute;
    }

    /**
     * Gets the second within the minute.
     *
     * @return The second
     */
    public int getSecond() {
        return second;
    }

    @Override
    public int compareTo(TimeOfTheDay o) {
        int oHour = o.hour;
        if (hour < oHour) {
            return -1;
        } else if (hour > oHour) {
            return 1;
        }

        int oMinute = o.minute;
        if (minute < oMinute) {
            return -1;
        } else if (minute > oMinute) {
            return 1;
        }

        int oSecond = o.second;
        return (second < oSecond) ? -1 : (second == oSecond ? 0 : 1);
    }

    @Override
    public int hashCode() {
        int result = hash;
        if (result == 0) {
            int prime = 31;
            result = 1;
            result = prime * result + hour;
            result = prime * result + minute;
            result = prime * result + second;
            hash = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TimeOfTheDay)) {
            return false;
        }
        TimeOfTheDay other = (TimeOfTheDay) obj;
        if (hour != other.hour) {
            return false;
        }
        if (minute != other.minute) {
            return false;
        }
        if (second != other.second) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(hour).append(':').append(minute).append(':').append(second).toString();
    }

}
