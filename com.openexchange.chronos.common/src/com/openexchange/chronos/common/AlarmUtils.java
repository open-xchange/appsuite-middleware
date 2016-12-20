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

package com.openexchange.chronos.common;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.Trigger;
import com.openexchange.chronos.Trigger.Related;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.TimeZones;

/**
 * {@link AlarmUtils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class AlarmUtils extends CalendarUtils {

    /**
     * Parses a trigger duration string.
     *
     * @param duration The duration to parse
     * @return The total milliseconds of the parsed duration
     * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.3.6">RFC 5545, section 3.3.6</a>
     */
    public static long getTriggerDuration(String duration) {
        long totalMillis = 0;
        boolean negative = false;
        String token = null;
        String previousToken = null;
        StringTokenizer tokenizer = new StringTokenizer(duration.toUpperCase(), "+-PWDTHMS", true);
        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            switch (token) {
                case "+":
                    negative = false;
                    break;
                case "-":
                    negative = true;
                    break;
                case "W":
                    totalMillis += TimeUnit.DAYS.toMillis(7 * Long.parseLong(previousToken));
                    break;
                case "D":
                    totalMillis += TimeUnit.DAYS.toMillis(Long.parseLong(previousToken));
                    break;
                case "H":
                    totalMillis += TimeUnit.HOURS.toMillis(Long.parseLong(previousToken));
                    break;
                case "M":
                    totalMillis += TimeUnit.MINUTES.toMillis(Long.parseLong(previousToken));
                    break;
                case "S":
                    totalMillis += TimeUnit.SECONDS.toMillis(Long.parseLong(previousToken));
                    break;
                case "T":
                case "P":
                default:
                    // skip
                    break;
            }
            previousToken = token;
        }
        return negative ? -1 * totalMillis : totalMillis;
    }

    /**
     * Filters a list of attendees based on their alarm action.
     *
     * @param alarms The alarms to filter
     * @param actions The {@link AlarmAction}s to consider
     * @return The filtered alarms
     */
    public static List<Alarm> filter(List<Alarm> alarms, AlarmAction... actions) {
        if (null == alarms) {
            return null;
        }
        List<Alarm> filteredAlarms = new ArrayList<Alarm>(alarms.size());
        for (Alarm alarm : alarms) {
            if (null == actions || com.openexchange.tools.arrays.Arrays.contains(actions, alarm.getAction())) {
                filteredAlarms.add(alarm);
            }
        }
        return filteredAlarms;
    }

    /**
     * Looks up a specific alarm in a collection of alarms based on its unique identifier.
     *
     * @param alarms The alarms to search
     * @param uid The unique identifier of the alarm to lookup
     * @return The matching alarm, or <code>null</code> if not found
     */
    public static Alarm find(Collection<Alarm> alarms, String uid) {
        if (null == alarms) {
            return null;
        }
        for (Alarm alarm : alarms) {
            if (uid.equals(alarm.getUid())) {
                return alarm;
            }
        }
        return null;
    }

    /**
     * Calculates the actual time of an alarm trigger associated with an event.
     *
     * @param trigger The trigger to get the effective trigger time for
     * @param event The event the trigger's alarm is associated with
     * @param timeZone The timezone to consider if the event has <i>floating</i> dates
     * @return The trigger time, or <code>null</code> if not specified in supplied trigger
     */
    public static Date getTriggerTime(Trigger trigger, Event event, TimeZone timeZone) {
        if (null == trigger) {
            return null;
        }
        if (null != trigger.getDateTime()) {
            return trigger.getDateTime();
        }
        if (null != trigger.getDuration()) {
            long duration = getTriggerDuration(trigger.getDuration());
            Date relatedDate = getRelatedDate(trigger.getRelated(), event);
            if (isFloating(event)) {
                relatedDate = getDateInTimeZone(relatedDate, timeZone);
            }
            if (0L == duration) {
                return relatedDate;
            }
            Calendar calendar = initCalendar(TimeZones.UTC, relatedDate);
            calendar.add(Calendar.SECOND, (int) TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS));
            return calendar.getTime();
        }
        return null;
    }

    /**
     * Gets the duration string for a duration value in a specific time unit.
     *
     * @param duration The duration to get the duration string for
     * @param unit The time unit of the given duration
     * @return The duration string
     */
    public static String getDuration(long duration, TimeUnit unit) {
        StringBuilder stringBuilder = new StringBuilder();
        if (0 > duration) {
            stringBuilder.append('-');
            duration = duration * -1;
        }
        stringBuilder.append("PT");
        long days = unit.toDays(duration);
        if (0 < days) {
            stringBuilder.append(days).append('D');
            duration -= unit.convert(days, TimeUnit.DAYS);
        }
        long hours = unit.toHours(duration);
        if (0 < hours) {
            stringBuilder.append(hours).append('H');
            duration -= unit.convert(hours, TimeUnit.HOURS);
        }
        long minutes = unit.toMinutes(duration);
        if (0 < minutes) {
            stringBuilder.append(minutes).append('M');
            duration -= unit.convert(minutes, TimeUnit.MINUTES);
        }
        long seconds = unit.toSeconds(duration);
        if (0 < seconds) {
            stringBuilder.append(seconds).append('S');
            duration -= unit.convert(seconds, TimeUnit.SECONDS);
        }
        return stringBuilder.toString();
    }

    /**
     * Calculates the actual relative duration of an alarm trigger associated with an event.
     *
     * @param trigger The trigger to get the effective trigger time for
     * @param event The event the trigger's alarm is associated with
     * @param recurrenceService A reference to the recurrence service
     * @return The relative trigger duration, or <code>null</code> if not specified in supplied trigger
     */
    public static String getTriggerDuration(Trigger trigger, Event event, RecurrenceService recurrenceService) throws OXException {
        if (null == trigger) {
            return null;
        }
        if (null != trigger.getDuration()) {
            return trigger.getDuration();
        }
        if (null != trigger.getDateTime()) {
            if (CalendarUtils.isSeriesMaster(event)) {
                Iterator<Event> iterator = recurrenceService.calculateInstancesRespectExceptions(event, initCalendar(TimeZones.UTC, trigger.getDateTime()), null, null, null);
                if (iterator.hasNext()) {
                    return getTriggerDuration(trigger, iterator.next());
                }
            } else {
                return getTriggerDuration(trigger, event);
            }
        }
        return null;
    }

    /**
     * Calculates the actual relative duration of an alarm trigger associated with an event.
     *
     * @param trigger The trigger to get the effective trigger time for
     * @param event The event the trigger's alarm is associated with
     * @return The relative trigger duration, or <code>null</code> if not specified in supplied trigger
     */
    private static String getTriggerDuration(Trigger trigger, Event event) throws OXException {
        if (null == trigger) {
            return null;
        }
        if (null != trigger.getDuration()) {
            return trigger.getDuration();
        }
        if (null != trigger.getDateTime()) {
            Date relatedDate = getRelatedDate(trigger.getRelated(), event);
            long diff = trigger.getDateTime().getTime() - relatedDate.getTime();
            return getDuration(diff, TimeUnit.MILLISECONDS);
        }
        return null;
    }

    public static Date getNextTriggerTime(Date startDate, Trigger trigger, Event event, TimeZone timeZone, RecurrenceService recurrenceService) throws OXException {
        if (CalendarUtils.isSeriesMaster(event)) {
            Iterator<Event> iterator = recurrenceService.calculateInstancesRespectExceptions(event, initCalendar(timeZone, startDate), null, null, null);
            while (iterator.hasNext()) {
                Date triggerTime = getTriggerTime(trigger, iterator.next(), timeZone);
                if (null == triggerTime) {
                    return null;
                } else if (false == triggerTime.before(startDate)) {
                    return triggerTime;
                }
            }
        }
        return getTriggerTime(trigger, event, timeZone);
    }

    //    public static Date getClosestTriggerTime(Date startDate, List<Alarm> alarms, Event event, TimeZone timeZone) throws OXException {
    //        if (null == alarms || 0 == alarms.size()) {
    //            return null;
    //        }
    //        Date closestTriggerTime = null;
    //        for (Alarm alarm : alarms) {
    //            Date triggerTime = getNextTriggerTime(startDate, alarm.getTrigger(), event, timeZone);
    //            if (null != triggerTime && false == triggerTime.before(startDate) &&
    //                (null == alarm.getAcknowledged() || false == triggerTime.before(alarm.getAcknowledged())) &&
    //                (null == closestTriggerTime || triggerTime.before(closestTriggerTime))) {
    //                closestTriggerTime = triggerTime;
    //            }
    //        }
    //        return closestTriggerTime;
    //    }

    /**
     * Gets the actual date-time of an event a trigger relates to, i.e. either the event's start- or end-date.
     *
     * @param related The related property of the trigger to get the related date for
     * @param event The event the trigger is associated with
     * @return The related date
     */
    public static Date getRelatedDate(Trigger.Related related, Event event) {
        return Related.END.equals(related) ? event.getEndDate() : event.getStartDate();
    }

    /**
     * Compares the effective time of two triggers.
     *
     * @param trigger1 The first trigger to compare
     * @param trigger2 The second trigger to compare
     * @param event The event the trigger's alarms are associated with
     * @param timeZone The timezone to consider if the event has <i>floating</i> dates
     * @return <code>0</code> if trigger1 is equal to trigger2, a value less than <code>0</code> if trigger1 is <i>before</i> trigger2,
     *         and a value greater than <code>0</code> if trigger1 is <i>after</i> trigger2.
     */
    public static int compare(Trigger trigger1, Trigger trigger2, Event event, TimeZone timeZone) {
        Date triggerTime1 = getTriggerTime(trigger1, event, timeZone);
        Date triggerTime2 = getTriggerTime(trigger1, event, timeZone);
        if (null == triggerTime1) {
            return null == triggerTime2 ? 0 : 1;
        }
        if (null == triggerTime2) {
            return -1;
        }
        return triggerTime1.compareTo(triggerTime2);
    }

    /**
     * Gets a value indicating whether a specific alarm represents a <i>snoozed</i> one, i.e. there exists another alarm with a matching
     * <code>SNOOZE</code> relationship in the supplied alarm collection.
     *
     * @param alarm The alarm to inspect
     * @param alarm A collection holding all alarms associated with the event
     * @return <code>true</code> if this is alarm holds the next ('snoozed') trigger time, <code>false</code>, otherwise
     */
    public static boolean isSnoozed(Alarm alarm, List<Alarm> allAlarms) {
        return null != alarm.getRelatedTo() && (null == alarm.getRelatedTo().getRelType() || "SNOOZE".equals(alarm.getRelatedTo().getRelType())) && null != find(allAlarms, alarm.getRelatedTo().getValue());
    }

}
