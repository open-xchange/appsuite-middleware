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

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.openexchange.java.Strings;

/**
 * {@link DataExportConfig} - The configuration for data export.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportConfig {

    /**
     * Gets a new builder instance.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for a configuration instance. */
    public static class Builder {

        private boolean active;
        private final Map<DayOfWeek, DayOfWeekTimeRanges> rangesOfTheWeek;
        private int numberOfConcurrentTasks;
        private int maxFailCountForWorkItem;
        private long checkForTasksFrequency;
        private long checkForAbortedTasksFrequency;
        private long maxProcessingTimeMillis;
        private long maxTimeToLiveMillis;
        private long expirationTimeMillis;
        private long defaultMaxFileSize;

        /**
         * Initializes a new {@link DataExportConfig.Builder}.
         */
        Builder() {
            super();
            active = true;
            rangesOfTheWeek = new HashMap<DayOfWeek, DayOfWeekTimeRanges>(7);
            numberOfConcurrentTasks = DataExportConstants.DEFAULT_NUMBER_OF_CONCURRENT_TASKS;
            maxFailCountForWorkItem = DataExportConstants.DEFAULT_MAX_FAIL_COUNT_FOR_WORK_ITEM;
            checkForTasksFrequency = DataExportConstants.DEFAULT_CHECK_FOR_TASKS_FREQUENCY;
            checkForAbortedTasksFrequency = DataExportConstants.DEFAULT_CHECK_FOR_ABORTED_TASKS_FREQUENCY;
            expirationTimeMillis = DataExportConstants.DEFAULT_EXPIRATION_TIME;
            maxProcessingTimeMillis = -1L;
            maxTimeToLiveMillis = DataExportConstants.DEFAULT_MAX_TIME_TO_LIVE;
            defaultMaxFileSize = DataExportConstants.DFAULT_MAX_FILE_SIZE;
        }

        /**
         * Sets whether processing of data export tasks should be enabled/active on this node.
         *
         * @param active <code>true</code> to activate processing of data export tasks; otherwise <code>false</code>
         * @return This builder
         */
        public Builder withActive(boolean active) {
            this.active = active;
            return this;
        }

        /**
         * Sets the number of concurrent tasks that are allowed being executed by this node.
         *
         * @param numberOfConcurrentTasks The number of concurrent tasks
         * @return This builder
         * @throws IllegalArgumentException If number is less than/equal to 0 (zero)
         */
        public Builder withNumberOfConcurrentTasks(int numberOfConcurrentTasks) {
            if (numberOfConcurrentTasks <= 0) {
                throw new IllegalArgumentException("numberOfConcurrentTasks must not be less than or equal to 0 (zero).");
            }
            this.numberOfConcurrentTasks = numberOfConcurrentTasks;
            return this;
        }

        /**
         * Sets the max. fail count for attempts to export items from a certain provider.
         *
         * @param maxFailCountForWorkItem The max. fail count
         * @return This builder
         * @throws IllegalArgumentException If max. fail count is less than/equal to 0 (zero)
         */
        public Builder withMaxFailCountForWorkItem(int maxFailCountForWorkItem) {
            if (maxFailCountForWorkItem <= 0) {
                throw new IllegalArgumentException("maxFailCountForWorkItem must not be less than or equal to 0 (zero).");
            }
            this.maxFailCountForWorkItem = maxFailCountForWorkItem;
            return this;
        }

        /**
         * Sets the frequency in milliseconds when to check for further tasks to process.
         *
         * @param checkForTasksFrequency The frequency to set
         * @return This builder
         * @throws IllegalArgumentException If frequency is less than/equal to 0 (zero)
         */
        public Builder withCheckForTasksFrequency(long checkForTasksFrequency) {
            if (checkForTasksFrequency <= 0) {
                throw new IllegalArgumentException("checkForTasksFrequency must not be less than or equal to 0 (zero).");
            }
            this.checkForTasksFrequency = checkForTasksFrequency;
            return this;
        }

        /**
         * Sets the frequency in milliseconds when to check for aborted tasks.
         *
         * @param checkForTasksFrequency The frequency to set
         * @return This builder
         * @throws IllegalArgumentException If frequency is less than/equal to 0 (zero)
         */
        public Builder withCheckForAbortedTasksFrequency(long checkForAbortedTasksFrequency) {
            if (checkForAbortedTasksFrequency <= 0) {
                throw new IllegalArgumentException("checkForAbortedTasksFrequency must not be less than or equal to 0 (zero).");
            }
            this.checkForAbortedTasksFrequency = checkForAbortedTasksFrequency;
            return this;
        }

        /**
         * Sets the max. time-to-live for completed tasks in milliseconds
         *
         * @param maxTimeToLiveMillis The max. time-to-live in milliseconds
         * @return This builder
         * @throws IllegalArgumentException If max. time-to-live in milliseconds is less than/equal to 0 (zero)
         */
        public Builder withMaxTimeToLiveMillis(long maxTimeToLiveMillis) {
            if (maxTimeToLiveMillis <= 0) {
                throw new IllegalArgumentException("Max. time-to-live in milliseconds must not be less than/equal to 0 (zero");
            }
            this.maxTimeToLiveMillis = maxTimeToLiveMillis;
            return this;
        }

        /**
         * Sets the max. processing time in milliseconds.
         *
         * @param maxProcessingTimeMillis The max. processing time in milliseconds or <code>-1</code> for infinite
         * @return This builder
         */
        public Builder withMaxProcessingTimeMillis(long maxProcessingTimeMillis) {
            this.maxProcessingTimeMillis = maxProcessingTimeMillis <= 0 ? -1 : maxProcessingTimeMillis;
            return this;
        }

        /**
         * Sets the expiration time in milliseconds.
         *
         * @param expirationTimeMillis The expiration time in milliseconds
         * @return This builder
         * @throws IllegalArgumentException If expiration time in milliseconds is less than/equal to 0 (zero)
         */
        public Builder withExpirationTimeMillis(long expirationTimeMillis) {
            if (expirationTimeMillis <= 0) {
                throw new IllegalArgumentException("Expiration time in milliseconds must not be less than/equal to 0 (zero");
            }
            this.expirationTimeMillis = expirationTimeMillis;
            return this;
        }

        /**
         * Sets the default max. file size for resulting files.
         *
         * @param defaultMaxFileSize The default max. file size
         * @return This builder
         */
        public Builder withDefaultMaxFileSize(long defaultMaxFileSize) {
            this.defaultMaxFileSize = defaultMaxFileSize;
            return this;
        }

        /**
         * Parses the given configuration; e.g. <code>"Mon 0:12-6:45; Tue-Thu 0-7:15; Fri 0-6,22:30-24; Sat,Sun 0-8"</code>.
         *
         * @param config The configuration to parse
         * @return This builder
         * @throws IllegalArgumentException If schedule information is invalid
         */
        public Builder parse(String config) {
            if (Strings.isEmpty(config)) {
                return this;
            }

            // Mon 0:12-6:45; Tue-Thu 0-7:15; Fri 0-6,22:30-24; Sat,Sun 0-8
            String[] parts = Strings.splitBy(config, ';', true);
            for (String part : parts) {
                parsePart(part);
            }

            return this;
        }

        /**
         * Parses the given configuration part; e.g. <code>"Tue-Thu,Fri 0-6,22:30-24"</code>
         *
         * @param config The configuration part to parse
         * @return This builder
         * @throws IllegalArgumentException If schedule information is invalid
         */
        public Builder parsePart(String part) {
            if (Strings.isEmpty(part)) {
                return this;
            }

            // Tue-Thu,Fri 0-6,22:30-24
            // Tue,Thu 0-6,22:30-24
            String daysOfWeek;
            String hoursOfDay;

            int length = part.length();
            int pos = -1;
            for (int i = 0; pos < 0 && i < length; i++) {
                char c = part.charAt(i);
                if (Strings.isDigit(c)) {
                    pos = i;
                }
            }

            if (pos < 0) {
                daysOfWeek = part;
                hoursOfDay = "0-24";
            } else {
                daysOfWeek = part.substring(0, pos).trim();
                hoursOfDay = part.substring(pos);
            }

            List<DayOfWeek> applicableDaysOfWeek = new ArrayList<>(7);

            for (String token : Strings.splitBy(daysOfWeek, ',', true)) {
                pos = token.indexOf('-');
                if (pos > 0) {
                    // A range
                    DayOfWeek day1 = getDayOfWeekFor(token.substring(0, pos));
                    DayOfWeek day2 = getDayOfWeekFor(token.substring(pos+1));
                    if (day2.getValue() == day1.getValue()) {
                        throw new IllegalArgumentException("Illegal days of week range in part: " + part);
                    }
                    DayOfWeek dayOfWeek = day1;
                    // Add all days in-between
                    while (dayOfWeek != day2) {
                        if (!applicableDaysOfWeek.isEmpty() && applicableDaysOfWeek.contains(dayOfWeek)) {
                            throw new IllegalArgumentException("Duplicate days of week in part: " + part);
                        }
                        applicableDaysOfWeek.add(dayOfWeek);
                        dayOfWeek = dayOfWeek.plus(1);
                    }
                    // Add end of range
                    if (!applicableDaysOfWeek.isEmpty() && applicableDaysOfWeek.contains(dayOfWeek)) {
                        throw new IllegalArgumentException("Duplicate days of week in part: " + part);
                    }
                    applicableDaysOfWeek.add(dayOfWeek);
                } else {
                    DayOfWeek dayOfWeek = getDayOfWeekFor(token);
                    if (!applicableDaysOfWeek.isEmpty() && applicableDaysOfWeek.contains(dayOfWeek)) {
                        throw new IllegalArgumentException("Duplicate days of week in part: " + part);
                    }
                    applicableDaysOfWeek.add(dayOfWeek);
                }
            }
            Collections.sort(applicableDaysOfWeek, new Comparator<DayOfWeek>() {

                @Override
                public int compare(DayOfWeek o1, DayOfWeek o2) {
                    return Integer.compare(o1.getValue(), o2.getValue());
                }
            });

            List<TimeRange> applicableTimeRanges = new ArrayList<>(2);

            for (String token : Strings.splitBy(hoursOfDay, ',', true)) {
                TimeRange timeRange = TimeRange.parseFrom(token);
                if (!applicableTimeRanges.isEmpty()) {
                    for (TimeRange anotherRange : applicableTimeRanges) {
                        if (anotherRange.overlapsWith(timeRange)) {
                            throw new IllegalArgumentException("Overlapping time ranges in part: " + part);
                        }
                    }
                }
                applicableTimeRanges.add(timeRange);
            }
            Collections.sort(applicableTimeRanges);

            for (DayOfWeek dayOfWeek : applicableDaysOfWeek) {
                DayOfWeekTimeRanges dayOfWeekTimeRanges = new DayOfWeekTimeRanges(dayOfWeek, applicableTimeRanges);
                this.rangesOfTheWeek.put(dayOfWeek, dayOfWeekTimeRanges);
            }

            return this;
        }

        /**
         * Creates the <code>DataExportConfig</code> instance from this builder's arguments.
         *
         * @return The <code>DataExportConfig</code> instance
         */
        public DataExportConfig build() {
            return new DataExportConfig(active, rangesOfTheWeek, defaultMaxFileSize, numberOfConcurrentTasks, checkForTasksFrequency, checkForAbortedTasksFrequency, maxProcessingTimeMillis, maxTimeToLiveMillis, expirationTimeMillis, maxFailCountForWorkItem);
        }

    } // End of Builder class

    // -----------------------------------------------------------------------------------------------------------------------------

    private final boolean active;
    private final Map<DayOfWeek, DayOfWeekTimeRanges> rangesOfTheWeek;
    private final int numberOfConcurrentTasks;
    private final long checkForTasksFrequency;
    private final long checkForAbortedTasksFrequency;
    private final long maxProcessingTimeMillis;
    private final long maxTimeToLiveMillis;
    private final long expirationTimeMillis;
    private final long defaultMaxFileSize;
    private final int maxFailCountForWorkItem;

    /**
     * Initializes a new {@link DataExportConfig}.
     */
    DataExportConfig(boolean active, Map<DayOfWeek, DayOfWeekTimeRanges> rangesOfTheWeek, long defaultMaxFileSize, int numberOfConcurrentTasks, long checkForTasksFrequency, long checkForAbortedTasksFrequency, long maxProcessingTimeMillis, long maxTimeToLiveMillis, long expirationTimeMillis, int maxFailCountForWorkItem) {
        super();
        this.active = active;
        this.defaultMaxFileSize = defaultMaxFileSize;
        this.numberOfConcurrentTasks = numberOfConcurrentTasks;
        this.maxProcessingTimeMillis = maxProcessingTimeMillis;
        this.maxTimeToLiveMillis = maxTimeToLiveMillis;
        this.expirationTimeMillis = expirationTimeMillis;
        this.maxFailCountForWorkItem = maxFailCountForWorkItem;
        this.rangesOfTheWeek = ImmutableMap.copyOf(rangesOfTheWeek);
        this.checkForTasksFrequency = checkForTasksFrequency;
        this.checkForAbortedTasksFrequency = checkForAbortedTasksFrequency;
    }

    /**
     * Gets the max. fail count for attempts to export items from a certain provider.
     *
     * @return The max. fail count
     */
    public int getMaxFailCountForWorkItem() {
        return maxFailCountForWorkItem;
    }

    /**
     * Checks whether processing of data export tasks should be enabled/active on this node.
     *
     * @return <code>true</code> if processing is activated; otherwise <code>false</code>
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Gets the default max. file size to assume for result files.
     *
     * @return The default max. file size
     */
    public long getDefaultMaxFileSize() {
        return defaultMaxFileSize;
    }

    /**
     * Gets the expiration time in milliseconds.
     * <p>
     * If a task's time stamp has not been touch for this amount of milliseconds, it is considered as expired.
     *
     * @return The expiration time in milliseconds or <code>-1</code> for no expiration time
     */
    public long getExpirationTimeMillis() {
        return expirationTimeMillis;
    }

    /**
     * Gets the max. processing time in milliseconds
     *
     * @return The max. processing time in milliseconds or <code>-1</code> form infinite
     */
    public long getMaxProcessingTimeMillis() {
        return maxProcessingTimeMillis;
    }

    /**
     * Gets the max. time-to-live for completed tasks in milliseconds
     *
     * @return The max. time-to-live in milliseconds
     */
    public long getMaxTimeToLiveMillis() {
        return maxTimeToLiveMillis;
    }

    /**
     * Gets the frequency in milliseconds when to check for further tasks to process.
     *
     * @return The frequency
     */
    public long getCheckForTasksFrequency() {
        return checkForTasksFrequency;
    }

    /**
     * Gets the frequency in milliseconds when to check for aborted tasks.
     *
     * @return The frequency
     */
    public long getCheckForAbortedTasksFrequency() {
        return checkForAbortedTasksFrequency;
    }

    /**
     * Gets the number of concurrent tasks that are allowed being executed by this node.
     *
     * @return The number of concurrent tasks
     */
    public int getNumberOfConcurrentTasks() {
        return numberOfConcurrentTasks;
    }

    /**
     * Gets the ranges of the week.
     *
     * @return The ranges of the week.
     */
    public Map<DayOfWeek, DayOfWeekTimeRanges> getRangesOfTheWeek() {
        return rangesOfTheWeek;
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    private static final Set<String> DAY_OF_WEEK_MONDAY = ImmutableSet.of("mo", "mon", "monday");
    private static final Set<String> DAY_OF_WEEK_TUESDAY = ImmutableSet.of("tu", "tue", "tuesday");
    private static final Set<String> DAY_OF_WEEK_WEDNESDAY = ImmutableSet.of("we", "wed", "wednesday");
    private static final Set<String> DAY_OF_WEEK_THURSDAY = ImmutableSet.of("th", "thu", "thursday");
    private static final Set<String> DAY_OF_WEEK_FRIDAY = ImmutableSet.of("fr", "fri", "friday");
    private static final Set<String> DAY_OF_WEEK_SATURDAY = ImmutableSet.of("sa", "sat", "satday");
    private static final Set<String> DAY_OF_WEEK_SUNDAY = ImmutableSet.of("su", "sun", "sunday");

    /**
     * Parses specified day of week to associated calendar constant.
     *
     * @param day The day of week to parse
     * @return The calendar constant
     * @throws IllegalArgumentException If given day of week cannot be parsed to a calendar constant
     * @see DayOfWeek#SUNDAY
     * @see DayOfWeek#MONDAY
     * @see DayOfWeek#TUESDAY
     * @see DayOfWeek#WEDNESDAY
     * @see DayOfWeek#THURSDAY
     * @see DayOfWeek#FRIDAY
     * @see DayOfWeek#SATURDAY
     */
    public static DayOfWeek getDayOfWeekFor(String day) {
        if (day == null) {
            return null;
        }
        String toCheck = Strings.asciiLowerCase(day.trim());
        if (DAY_OF_WEEK_MONDAY.contains(toCheck)) {
            return DayOfWeek.MONDAY;
        }
        if (DAY_OF_WEEK_TUESDAY.contains(toCheck)) {
            return DayOfWeek.TUESDAY;
        }
        if (DAY_OF_WEEK_WEDNESDAY.contains(toCheck)) {
            return DayOfWeek.WEDNESDAY;
        }
        if (DAY_OF_WEEK_THURSDAY.contains(toCheck)) {
            return DayOfWeek.THURSDAY;
        }
        if (DAY_OF_WEEK_FRIDAY.contains(toCheck)) {
            return DayOfWeek.FRIDAY;
        }
        if (DAY_OF_WEEK_SATURDAY.contains(toCheck)) {
            return DayOfWeek.SATURDAY;
        }
        if (DAY_OF_WEEK_SUNDAY.contains(toCheck)) {
            return DayOfWeek.SUNDAY;
        }
        throw new IllegalArgumentException("Cannot be parsed to a day of week: " + day);
    }

}
