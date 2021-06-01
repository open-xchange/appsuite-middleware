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

package com.openexchange.chronos.ical.ical4j;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.util.Iterator;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.concurrent.TimeUnit;
import com.openexchange.chronos.ical.LastRuleAware;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.NumberList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.component.Observance;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.RRule;

/**
 * {@link ImportedTimeZone}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class ImportedTimeZone extends net.fortuna.ical4j.model.TimeZone implements LastRuleAware {

    private static final long serialVersionUID = 121216650010358897L;

    private SimpleTimeZone lastRuleInstance;

    /**
     * Initializes a new {@link ImportedTimeZone}.
     *
     * @param vTimeZone The underlying <code>VTIMEZONE</code> component
     */
    public ImportedTimeZone(VTimeZone vTimeZone) {
        super(vTimeZone);
    }

    @Override
    public SimpleTimeZone getLastRuleInstance() {
        if (null == lastRuleInstance) {
            lastRuleInstance = optLastRuleInstance(getVTimeZone());
        }
        return lastRuleInstance;
    }

    @Override
    public boolean hasSameRules(java.util.TimeZone other) {
        if (this == other) {
            return true;
        }
        if (TimeZone.class.isInstance(other)) {
            return equals(other);
        }
        if (SimpleTimeZone.class.isInstance(other)) {
            SimpleTimeZone lastRuleInstance = getLastRuleInstance();
            if (null == lastRuleInstance) {
                return getRawOffset() == other.getRawOffset() && false == other.useDaylightTime();
            }
            return lastRuleInstance.hasSameRules(other);
        }
        return super.hasSameRules(other);
    }

    @Override
    public String toString() {
        return "ImportedTimeZone [id=" + getID() + ", displayName=" + getDisplayName(Locale.US) + ", offset=" + getRawOffset() + ", dstSavings=" + getDSTSavings() + ", useDaylight=" + useDaylightTime() + "]";
    }

    private static SimpleTimeZone optLastRuleInstance(VTimeZone vTimeZone) {
        String ID = null != vTimeZone.getTimeZoneId() ? vTimeZone.getTimeZoneId().getValue() : vTimeZone.getName();
        /*
         * get last standard observance & offset
         */
        Observance standardObservance = getLastObservance(vTimeZone, Observance.STANDARD);
        if (null == standardObservance) {
            return null;
        }
        Integer standardOffset = optOffsetTo(standardObservance);
        if (null == standardOffset) {
            return null;
        }
        /*
         * get last daylight observance & offset
         */
        Observance daylightObservance = getLastObservance(vTimeZone, Observance.DAYLIGHT);
        if (null == daylightObservance) {
            /*
             * timezone w/o DST
             */
            return new SimpleTimeZone(i(standardOffset), ID);
        }
        Integer daylightOffset = optOffsetTo(daylightObservance);
        if (null == daylightOffset) {
            return null;
        }
        /*
         * parse daylight recurrence rule and derive timezone's transitions
         */
        RRule daylightRRule = (RRule) daylightObservance.getProperty(Property.RRULE);
        if (null == daylightRRule || null == daylightRRule.getRecur()) {
            return null;
        }
        int[] daylightMonthAndDayAndDayOfWeek = parseMonthAndDayAndDayOfWeek(daylightRRule.getRecur());
        Integer daylightStartHour  = optSingleNumber(daylightRRule.getRecur().getHourList());
        /*
         * parse standard recurrence rule and derive timezone's transitions
         */
        RRule standardRRule = (RRule) standardObservance.getProperty(Property.RRULE);
        if (null == standardRRule || null == standardRRule.getRecur()) {
            return null;
        }
        int[] standardMonthAndDayAndDayOfWeek = parseMonthAndDayAndDayOfWeek(standardRRule.getRecur());
        Integer standardStartHour = optSingleNumber(standardRRule.getRecur().getHourList());
        /*
         * init simple timezone from parsed observances
         */
        if (null != standardMonthAndDayAndDayOfWeek && null != daylightMonthAndDayAndDayOfWeek) {
            return new SimpleTimeZone(i(standardOffset), ID,
                daylightMonthAndDayAndDayOfWeek[0], daylightMonthAndDayAndDayOfWeek[1], daylightMonthAndDayAndDayOfWeek[2],
                null == daylightStartHour ? 0 : (int) TimeUnit.HOURS.toMillis(i(daylightStartHour)), SimpleTimeZone.WALL_TIME,
                standardMonthAndDayAndDayOfWeek[0], standardMonthAndDayAndDayOfWeek[1], standardMonthAndDayAndDayOfWeek[2],
                null == standardStartHour ? 0 : (int) TimeUnit.HOURS.toMillis(i(standardStartHour)), SimpleTimeZone.WALL_TIME,
                i(daylightOffset) - i(standardOffset));
        }
        return null;
    }

    /**
     * Extracts the {@link SimpleTimeZone} parameter values for <code>startMonth</code> / <code>endMonth</code>, <code>startDay</code> /
     * <code>endDay</code> and <code>startDayOfWeek</code> / <code>endDayOfWeek</code> for the supplied recurrence rule, obeying their
     * semantics in {@link SimpleTimeZone}.
     *
     * @param recur The recurrence rule of the observance to extract the timezone parameter values from
     * @return The extracted month, day-of-month, and day-of-week values to be used in the {@link SimpleTimeZone} c'tor, or <code>null</code> if not possible
     */
    private static int[] parseMonthAndDayAndDayOfWeek(Recur recur) {
        if (null == recur) {
            return null;
        }
        Integer month = optSingleNumber(recur.getMonthList());
        Integer dayOfMonth = optSingleNumber(recur.getMonthDayList());
        WeekDay dayOfWeek = optSingleWeekDay(recur.getDayList());
        /*
         * Exact day of month: month and day-of-month to an exact value, day-of-week to zero
         */
        if (null != month && null != dayOfMonth && null == dayOfWeek) {
            return new int[] { i(month) - 1, i(dayOfMonth), 0 };
        }
        /*
         * Day of week on or after day of month: month to an exact value, day-of-month to the day on or after which the rule is applied,
         * day-of-week to a negative value field value
         */
        if (null != month && null == dayOfMonth && null != dayOfWeek && 0 < dayOfWeek.getOffset()) {
            return new int[] { i(month) - 1, 1 + (dayOfWeek.getOffset() - 1) * 7, -1 * WeekDay.getCalendarDay(dayOfWeek) };
        }
        /*
         * Day of week on or before day of month: day-of-month and day-of-week to a negative value
         */
        // impossible with RRULE (?)
        /*
         * Last day-of-week of month: month to an exact value, day-of-week to a day-of-week value and day-of-month to -1
         */
        if (null != month && null == dayOfMonth && null != dayOfWeek && -1 == dayOfWeek.getOffset()) {
            return new int[] { i(month) - 1, -1, WeekDay.getCalendarDay(dayOfWeek) };
        }
        return null;
    }

    private static Observance getLastObservance(VTimeZone vTimeZone, String name) {
        if (null != vTimeZone && null != vTimeZone.getObservances()) {
            Observance lastObservance = null;
            ComponentList observances = vTimeZone.getObservances().getComponents(name);
            for (Iterator<?> iterator = observances.iterator(); iterator.hasNext();) {
                Observance observance = (Observance) iterator.next();
                if (null == lastObservance || null == lastObservance.getStartDate() ||
                    null != observance.getStartDate() && observance.getStartDate().getDate().after(lastObservance.getStartDate().getDate())) {
                    lastObservance = observance;
                }
            }
            return lastObservance;
        }
        return null;
    }

    private static WeekDay optSingleWeekDay(WeekDayList weekDayList) {
        if (null != weekDayList && 0 < weekDayList.size() && WeekDay.class.isInstance(weekDayList.get(0))) {
            return (WeekDay) weekDayList.get(0);
        }
        return null;
    }

    private static Integer optSingleNumber(NumberList numberList) {
        if (null != numberList && 0 < numberList.size() && Integer.class.isInstance(numberList.get(0))) {
            return (Integer) numberList.get(0);
        }
        return null;
    }

    private static Integer optOffsetTo(Observance observance) {
        if (null != observance && null != observance.getOffsetTo() && null != observance.getOffsetTo().getOffset()) {
            return I((int) observance.getOffsetTo().getOffset().getOffset());
        }
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((lastRuleInstance == null) ? 0 : lastRuleInstance.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ImportedTimeZone other = (ImportedTimeZone) obj;
        if (lastRuleInstance == null) {
            if (other.lastRuleInstance != null)
                return false;
        } else if (!lastRuleInstance.equals(other.lastRuleInstance))
            return false;
        return true;
    }
}
