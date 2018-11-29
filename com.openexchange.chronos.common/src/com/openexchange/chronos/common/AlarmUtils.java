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

package com.openexchange.chronos.common;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.Trigger;
import com.openexchange.chronos.Trigger.Related;
import com.openexchange.chronos.common.mapping.AbstractCollectionUpdate;
import com.openexchange.chronos.common.mapping.AlarmMapper;
import com.openexchange.chronos.common.mapping.DefaultCollectionUpdate;
import com.openexchange.chronos.common.mapping.DefaultItemUpdate;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link AlarmUtils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class AlarmUtils extends CalendarUtils {

    /**
     * Constructs a duration string that consists of the supplied properties.
     *
     * @param negative <code>true</code> if the duration should become negative, <code>false</code>, otherwise
     * @param weeks The number of weeks in the duration
     * @param days The number of days in the duration
     * @param hours The number of hours in the duration
     * @param minutes The number of minutes in the duration
     * @param seconds The number of seconds in the duration
     * @return The duration string, or <code>PT0S</code> for a neutral duration
     */
    public static String getDuration(boolean negative, long weeks, long days, long hours, long minutes, long seconds) {
        if (0 == weeks + days + hours + minutes + seconds) {
            return "PT0S";
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (negative) {
            stringBuilder.append('-');
        }
        stringBuilder.append('P');
        if (0 < weeks) {
            stringBuilder.append(weeks).append('W');
        }
        if (0 < days) {
            stringBuilder.append(days).append('D');
        }
        if (0 < hours || 0 < minutes || 0 < seconds) {
            stringBuilder.append('T');
            if (0 < hours) {
                stringBuilder.append(hours).append('H');
            }
            if (0 < minutes) {
                stringBuilder.append(minutes).append('M');
            }
            if (0 < seconds) {
                stringBuilder.append(seconds).append('S');
            }
        }
        return stringBuilder.toString();
    }

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
            previousToken = token;
            token = tokenizer.nextToken();
            switch (token) {
                case "+":
                    negative = false;
                    break;
                case "-":
                    negative = true;
                    break;
                case "W":
                    if (null != previousToken) {
                        totalMillis += TimeUnit.DAYS.toMillis(7 * Long.parseLong(previousToken));
                    }
                    break;
                case "D":
                    if (null != previousToken) {
                        totalMillis += TimeUnit.DAYS.toMillis(Long.parseLong(previousToken));
                    }
                    break;
                case "H":
                    if (null != previousToken) {
                        totalMillis += TimeUnit.HOURS.toMillis(Long.parseLong(previousToken));
                    }
                    break;
                case "M":
                    if (null != previousToken) {
                        totalMillis += TimeUnit.MINUTES.toMillis(Long.parseLong(previousToken));
                    }
                    break;
                case "S":
                    if (null != previousToken) {
                        totalMillis += TimeUnit.SECONDS.toMillis(Long.parseLong(previousToken));
                    }
                    break;
                case "T":
                case "P":
                default:
                    // skip
                    break;
            }
        }
        return negative ? -1 * totalMillis : totalMillis;
    }

    /**
     * Applies a specific trigger duration to the supplied calendar instance.
     *
     * @param duration The duration to apply
     * @param calendar the calendar instance to apply the duration to
     * @return The passed calendar instance, with the duration applied
     */
    public static Calendar applyDuration(String duration, Calendar calendar) {
        boolean negative = duration.indexOf('-') > duration.indexOf('+');
        String token = null;
        String previousToken = null;
        StringTokenizer tokenizer = new StringTokenizer(duration.toUpperCase(), "+-PWDTHMS", true);
        while (tokenizer.hasMoreTokens()) {
            previousToken = token;
            token = tokenizer.nextToken();
            if (null != previousToken) {
                switch (token) {
                    case "W":
                        calendar.add(Calendar.DATE, (int) Long.parseLong(previousToken) * (negative ? -7 : 7));
                        break;
                    case "D":
                        calendar.add(Calendar.DATE, (int) Long.parseLong(previousToken) * (negative ? -1 : 1));
                        break;
                    case "H":
                        calendar.add(Calendar.HOUR_OF_DAY, (int) Long.parseLong(previousToken) * (negative ? -1 : 1));
                        break;
                    case "M":
                        calendar.add(Calendar.MINUTE, (int) Long.parseLong(previousToken) * (negative ? -1 : 1));
                        break;
                    case "S":
                        calendar.add(Calendar.SECOND, (int) Long.parseLong(previousToken) * (negative ? -1 : 1));
                        break;
                    default:
                        // skip
                        break;
                }
            }
        }
        return calendar;
    }

    /**
     * Gets a value indicating whether a specific alarm's trigger is <i>relative</i>, i.e. it has a defined duration and no <i>absolute</i>
     * trigger time.
     *
     * @param alarm The alarm to check the trigger in
     * @return <code>true</code> if the alarm's trigger is <i>relative</i>, <code>false</code>, otherwise
     */
    public static boolean hasRelativeTrigger(Alarm alarm) {
        return null != alarm && null != alarm.getTrigger() && Strings.isNotEmpty(alarm.getTrigger().getDuration());
    }

    /**
     * Filters those alarms that have a <i>relative</i> trigger from the supplied list of alarms.
     *
     * @param alarms The alarms to filter
     * @return The filtered alarms
     * @see #hasRelativeTrigger
     */
    public static List<Alarm> filterRelativeTriggers(List<Alarm> alarms) {
        if (null == alarms) {
            return null;
        }
        List<Alarm> filteredAlarms = new ArrayList<Alarm>(alarms.size());
        for (Alarm alarm : alarms) {
            if (hasRelativeTrigger(alarm)) {
                filteredAlarms.add(alarm);
            }
        }
        return filteredAlarms;
    }

    /**
     * Filters a list of alarms based on their alarm action.
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
     * Filters a list of alarms by removing all <i>acknowledged</i> alarms.
     *
     * @param alarms The alarms to filter
     * @param event The event the alarms are associated with
     * @param timeZone The timezone to consider if the event has <i>floating</i> dates, and to calculate a relative trigger in
     * @return The filtered alarms
     * @see AlarmUtils#isAcknowledged(Alarm, Event, TimeZone)
     */
    public static List<Alarm> removeAcknowledged(List<Alarm> alarms, Event event, TimeZone timeZone) {
        if (null == alarms) {
            return null;
        }
        List<Alarm> filteredAlarms = new ArrayList<Alarm>(alarms.size());
        for (Alarm alarm : alarms) {
            if (false == isAcknowledged(alarm, event, timeZone)) {
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
    public static Alarm findAlarm(Collection<Alarm> alarms, String uid) {
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
     * Looks up a specific alarm in a collection of alarms based on its trigger.
     *
     * @param alarms The alarms to search
     * @param trigger The trigger of the alarm to lookup
     * @return The matching alarm, or <code>null</code> if not found
     */
    public static Alarm find(Collection<Alarm> alarms, Trigger trigger) {
        if (null == alarms) {
            return null;
        }
        for (Alarm alarm : alarms) {
            if (trigger.equals(alarm.getTrigger())) {
                return alarm;
            }
        }
        return null;
    }

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    /**
     * Calculates the actual time of an alarm trigger associated with an event.
     *
     * @param trigger The trigger to get the effective trigger time for
     * @param event The event the trigger's alarm is associated with
     * @param timeZone The timezone to consider if the event has <i>floating</i> dates, and to calculate a relative trigger in
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
            DateTime relatedDate = getRelatedDate(trigger.getRelated(), event);
            if (isFloating(event)) {
                long dateInTimeZone = getDateInTimeZone(relatedDate, timeZone);
                relatedDate = new DateTime(timeZone, dateInTimeZone);
            }
            Calendar calendar = initCalendar(UTC, relatedDate.getTimestamp());
            return applyDuration(trigger.getDuration(), calendar).getTime();
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
        if (0 == duration) {
            return "P0D";
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (0 > duration) {
            stringBuilder.append('-');
            duration = duration * -1;
        }
        stringBuilder.append('P');
        long days = unit.toDays(duration);
        if (0 < days) {
            stringBuilder.append(days).append('D');
            duration -= unit.convert(days, TimeUnit.DAYS);
        }
        if (0 == duration) {
            return stringBuilder.toString();
        }
        stringBuilder.append('T');
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
                Iterator<Event> iterator = recurrenceService.iterateEventOccurrences(event, trigger.getDateTime(), null);
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
     * Calculates the actual relative duration of an alarm trigger associated with a non-series event.
     *
     * @param trigger The trigger to get the effective trigger time for
     * @param event The event the trigger's alarm is associated with
     * @return The relative trigger duration, or <code>null</code> if not specified in supplied trigger
     */
    private static String getTriggerDuration(Trigger trigger, Event event) {
        if (null == trigger) {
            return null;
        }
        if (null != trigger.getDuration()) {
            return trigger.getDuration();
        }
        if (null != trigger.getDateTime()) {
            DateTime relatedDate = getRelatedDate(trigger.getRelated(), event);
            long diff = trigger.getDateTime().getTime() - relatedDate.getTimestamp();
            return getDuration(diff, TimeUnit.MILLISECONDS);
        }
        return null;
    }

    /**
     * Calculates the next date-time for a specific alarm trigger associated with an event series.
     * <p/>
     * The trigger is calculated for the <i>next</i> occurrence after a certain start date, which may be supplied directly via the
     * <code>startDate</code> argument, or is either the last acknowledged date of the alarm or the current server time.
     *
     * @param seriesMaster The series master event the alarm is associated with
     * @param alarm The alarm
     * @param startDate The start date marking the lower (inclusive) limit for the actual event occurrence to begin, or <code>null</code>
     *            to select automatically
     * @param timeZone The timezone to consider if the event has <i>floating</i> dates
     * @param recurrenceService A reference to the recurrence service
     * @return The next trigger time, or <code>null</code> if there is none
     */
    public static Date getNextTriggerTime(Event seriesMaster, Alarm alarm, Date startDate, TimeZone timeZone, RecurrenceService recurrenceService) throws OXException {
        if (null == startDate) {
            startDate = null != alarm.getAcknowledged() ? alarm.getAcknowledged() : new Date();
        }
        Iterator<Event> iterator = recurrenceService.iterateEventOccurrences(seriesMaster, startDate, null);
        while (iterator.hasNext()) {
            Event occurrence = iterator.next();

            Date triggerTime = getTriggerTime(alarm.getTrigger(), occurrence, timeZone);
            if (null == triggerTime) {
                return null;
            }
            if(triggerTime.getTime() < startDate.getTime()) {
                continue;
            }
            if (null == alarm.getAcknowledged() || alarm.getAcknowledged().before(triggerTime)) {
                return triggerTime;
            }
        }
        return null;
    }


    /**
     * Calculates the next date-time for a specific alarm trigger associated with an event series and returns the corresponding event occurrence.
     * <p/>
     * The trigger is calculated for the <i>next</i> occurrence after a certain start date, which may be supplied directly via the
     * <code>startDate</code> argument, or is either the last acknowledged date of the alarm or the current server time.
     *
     * @param seriesMaster The series master event the alarm is associated with
     * @param alarm The alarm
     * @param startDate The start date marking the lower (inclusive) limit for the actual event occurrence to begin, or <code>null</code>
     *            to select automatically
     * @param timeZone The timezone to consider if the event has <i>floating</i> dates
     * @param recurrenceService A reference to the recurrence service
     * @return The event occurrence, or <code>null</code> if there is no next trigger time
     */
    public static Event getNextTriggerEvent(Event seriesMaster, Alarm alarm, Date startDate, TimeZone timeZone, RecurrenceService recurrenceService) throws OXException {
        if (null == startDate) {
            startDate = null != alarm.getAcknowledged() ? alarm.getAcknowledged() : new Date();
        }
        Iterator<Event> iterator = recurrenceService.iterateEventOccurrences(seriesMaster, startDate, null);
        while (iterator.hasNext()) {
            Event occurrence = iterator.next();
            if (occurrence.getStartDate().getTimestamp() < startDate.getTime()) {
                continue;
            }
            Date triggerTime = getTriggerTime(alarm.getTrigger(), occurrence, timeZone);
            if (null == triggerTime) {
                return null;
            }
            if (null == alarm.getAcknowledged() || alarm.getAcknowledged().before(triggerTime)) {
                return occurrence;
            }
        }
        return null;
    }

    /**
     * Gets the actual date-time of an event a trigger relates to, i.e. either the event's start- or end-date.
     *
     * @param related The related property of the trigger to get the related date for
     * @param event The event the trigger is associated with
     * @return The related date
     */
    public static DateTime getRelatedDate(Trigger.Related related, Event event) {
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
     * Gets a value indicating whether a specific alarm is <i>acknowledged</i>, i.e. the alarm action has already been triggered and
     * dismissed by the user/client.
     * <p/>
     * More formally, this method check if the {@link AlarmField#ACKNOWLEDGED} property is set, and its value is greater than or equal to
     * the computed trigger time of the alarm.
     *
     * @param alarm The alarm to inspect
     * @param event The event the alarm is associated with
     * @param timeZone The timezone to consider if the event has <i>floating</i> dates, and to calculate a relative trigger in
     * @return <code>true</code> if the alarm is acknowledged, <code>false</code>, otherwise
     */
    public static boolean isAcknowledged(Alarm alarm, Event event, TimeZone timeZone) {
        Date acknowledged = alarm.getAcknowledged();
        if (null != acknowledged) {
            Date triggerTime = getTriggerTime(alarm.getTrigger(), event, timeZone);
            if (null != triggerTime) {
                return false == acknowledged.before(triggerTime);
            }
        }
        return false;
    }

    /**
     * Gets a value indicating whether a specific alarm represents a <i>snoozed</i> one, i.e. there exists another alarm with a matching
     * <code>SNOOZE</code> relationship in the supplied alarm collection that was 'overridden' by this alarm.
     *
     * @param alarm The alarm to inspect
     * @param allAlarms A collection holding all alarms associated with the event
     * @return <code>true</code> if this alarm holds the next ('snoozed') trigger time, <code>false</code>, otherwise
     */
    public static boolean isSnoozed(Alarm alarm, List<Alarm> allAlarms) {
        return null != getSnoozedAlarm(alarm, allAlarms);
    }

    /**
     * Gets the alarm that has been snoozed and 'overridden' by this snooze alarm, i.e. looks up another alarm with a matching
     * <code>SNOOZE</code> relationship in the supplied alarm collection.
     *
     * @param alarm The possible 'snooze' alarm to inspect
     * @param allAlarms A collection holding all alarms associated with the event
     * @return The alarm that has been snoozed by the supplied alarm, or <code>null</code> if matching alarm found
     */
    public static Alarm getSnoozedAlarm(Alarm alarm, List<Alarm> allAlarms) {
        if (null != alarm.getRelatedTo() && (null == alarm.getRelatedTo().getRelType() || "SNOOZE".equals(alarm.getRelatedTo().getRelType()))) {
            return findAlarm(allAlarms, alarm.getRelatedTo().getValue());
        }
        return null;
    }

    /**
     * Initializes a new alarm collection update based on the supplied original and updated alarm lists.
     *
     * @param originalAlarms The original alarms
     * @param updatedAlarms The updated alarms
     * @return The collection update
     */
    public static CollectionUpdate<Alarm, AlarmField> getAlarmUpdates(final List<Alarm> originalAlarms, final List<Alarm> updatedAlarms) {
        /*
         * special handling to detect change of single reminder (as used in legacy storage)
         */
        if (null != originalAlarms && 1 == originalAlarms.size() && null != updatedAlarms && 1 == updatedAlarms.size()) {
            Alarm originalAlarm = originalAlarms.get(0);
            Alarm updatedAlarm = updatedAlarms.get(0);
            Set<AlarmField> differentBehavioralFields = AlarmMapper.getInstance().getDifferentFields(
                originalAlarm, updatedAlarm, true, AlarmField.ID, AlarmField.UID, AlarmField.DESCRIPTION, AlarmField.EXTENDED_PROPERTIES);
            if (differentBehavioralFields.isEmpty()) {
                return new DefaultCollectionUpdate<Alarm, AlarmField>(null, null, null);
            }
            Set<AlarmField> differentFields = AlarmMapper.getInstance().getDifferentFields(
                originalAlarm, updatedAlarm, true, AlarmField.TRIGGER, AlarmField.UID, AlarmField.DESCRIPTION, AlarmField.EXTENDED_PROPERTIES);
            if (differentFields.isEmpty()) {
                DefaultItemUpdate<Alarm, AlarmField> itemUpdate = new DefaultItemUpdate<>(AlarmMapper.getInstance(), originalAlarm, updatedAlarm);
                return new DefaultCollectionUpdate<Alarm, AlarmField>(null, null, Collections.<ItemUpdate<Alarm, AlarmField>> singletonList(itemUpdate));
            }
        }
        /*
         * default collection update, otherwise
         */
        return new AbstractCollectionUpdate<Alarm, AlarmField>(AlarmMapper.getInstance(), originalAlarms, updatedAlarms) {

            @Override
            protected boolean matches(Alarm alarm1, Alarm alarm2) {
                if (null == alarm1) {
                    return null == alarm2;
                } else if (null != alarm2) {
                    if (0 != alarm1.getId() && alarm1.getId() == alarm2.getId()) {
                        return true;
                    }
                    if (null != alarm1.getUid() && alarm1.getUid().equals(alarm2.getUid())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * Adds an extended property to an alarm, initializing the alarm's extended properties collection as needed.
     *
     * @param alarm The alarm to add the property to
     * @param extendedProperty The extended property to add
     * @param removeExisting <code>true</code> to remove any existing extended property with the same name, <code>false</code>, otherwise
     */
    public static void addExtendedProperty(Alarm alarm, ExtendedProperty extendedProperty, boolean removeExisting) {
        ExtendedProperties extendedProperties = alarm.getExtendedProperties();
        if (null == extendedProperties) {
            extendedProperties = new ExtendedProperties();
            alarm.setExtendedProperties(extendedProperties);
        } else if (removeExisting) {
            extendedProperties.removeAll(extendedProperty.getName());
        }
        extendedProperties.add(extendedProperty);
    }

    /**
     * Optionally gets (the first) extended property of a specific name of an alarm.
     *
     * @param alarm The alarm to get the extended property from
     * @param name The name of the extended property to get
     * @return The extended property, or <code>null</code> if not set
     */
    public static ExtendedProperty optExtendedProperty(Alarm alarm, String name) {
        return optExtendedProperty(alarm.getExtendedProperties(), name);
    }

    /**
     * Optionally gets the value of (the first) extended property of specific name of an alarm.
     *
     * @param alarm The alarm to get the extended property value from
     * @param name The name of the extended property to get
     * @return The extended property value, or <code>null</code> if not set
     */
    public static Object optExtendedPropertyValue(Alarm alarm, String name) {
        ExtendedProperty extendedProperty = optExtendedProperty(alarm, name);
        return null != extendedProperty ? extendedProperty.getValue() : null;
    }

    /**
     * Filters a list of alarm triggers based on their action type.
     *
     * @param triggers The triggers to filter
     * @param actions The alarm actions to consider
     * @return The filtered triggers, or an empty list if there were no matching triggers
     */
    public static List<AlarmTrigger> filter(List<AlarmTrigger> triggers, String... actions) {
        if (null == triggers) {
            return Collections.emptyList();
        }
        if (null == actions) {
            return triggers;
        }
        List<AlarmTrigger> filteredTriggers = new ArrayList<AlarmTrigger>(triggers.size());
        for (AlarmTrigger trigger : triggers) {
            if (null == actions || com.openexchange.tools.arrays.Arrays.contains(actions, trigger.getAction())) {
                filteredTriggers.add(trigger);
            }
        }
        return filteredTriggers;
    }

}
