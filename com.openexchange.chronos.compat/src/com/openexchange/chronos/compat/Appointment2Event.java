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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.java.Autoboxing.l;
import java.util.ArrayList;
import java.util.List;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Weekday;
import org.dmfs.rfc5545.recur.Freq;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRule.Part;
import org.dmfs.rfc5545.recur.RecurrenceRule.WeekdayNum;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.EventStatus;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.Trigger;
import com.openexchange.java.Strings;

/**
 * {@link Appointment2Event}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Appointment2Event {

    /**
     * Gets the event classification appropriate for the supplied "private flag" value.
     * 
     * @param privateFlag The legacy "private flag"
     * @return The classification
     */
    public static Classification getClassification(boolean privateFlag) {
        return privateFlag ? Classification.PRIVATE : Classification.PUBLIC;
    }

    /**
     * Gets the event status appropriate for the supplied "shown as" value.
     *
     * @param confirm The legacy "shown as" constant
     * @return The event status, defaulting to {@value EventStatus#CONFIRMED} if not mappable
     */
    public static EventStatus getEventStatus(int shownAs) {
        switch (shownAs) {
            case 3: // com.openexchange.groupware.container.Appointment.TEMPORARY
                return EventStatus.TENTATIVE;
            default:
                return EventStatus.CONFIRMED;
        }
    }

    /**
     * Gets a participation status appropriate for the supplied confirmation status.
     *
     * @param confirm The legacy confirmation status constant
     * @return The participation status, or {@value ParticipationStatus#NEEDS_ACTION} if not mappable
     */
    public static ParticipationStatus getParticipationStatus(int confirm) {
        switch (confirm) {
            case 1: // com.openexchange.groupware.container.participants.ConfirmStatus.ACCEPT
                return ParticipationStatus.ACCEPTED;
            case 2: // com.openexchange.groupware.container.participants.ConfirmStatus.DECLINE
                return ParticipationStatus.DECLINED;
            case 3: // com.openexchange.groupware.container.participants.ConfirmStatus.TENTATIVE
                return ParticipationStatus.TENTATIVE;
            default: // com.openexchange.groupware.container.participants.ConfirmStatus.NONE
                return ParticipationStatus.NEEDS_ACTION;
        }
    }

    /**
     * Gets a calendar user type appropriate for the supplied participant type.
     *
     * @param type The legacy participant type constant
     * @return The calendar user type, or {@value CalendarUserType#UNKNOWN} if not mappable
     */
    public static CalendarUserType getCalendarUserType(int type) {
        switch (type) {
            case 1: // com.openexchange.groupware.container.Participant.USER
            case 5: // com.openexchange.groupware.container.Participant.EXTERNAL_USER
                return CalendarUserType.INDIVIDUAL;
            case 2: // com.openexchange.groupware.container.Participant.GROUP
            case 6: // com.openexchange.groupware.container.Participant.EXTERNAL_GROUP
                return CalendarUserType.GROUP;
            case 3: // com.openexchange.groupware.container.Participant.RESOURCE
            case 4: // com.openexchange.groupware.container.Participant.RESOURCEGROUP
                return CalendarUserType.RESOURCE;
            default: // com.openexchange.groupware.container.Participant.NO_ID
                return CalendarUserType.UNKNOWN;
        }
    }

    /**
     * Gets an <code>mailto</code>-URI for the supplied e-mail address.
     * 
     * @param emailAddress The e-mail address to get the URI for
     * @return The <code>mailto</code>-URI, or <code>null</code> if no address was passed
     */
    public static String getURI(String emailAddress) {
        if (Strings.isNotEmpty(emailAddress)) {
            return "mailto:" + emailAddress;
        }
        return null;
    }

    /**
     * Gets the CSS3 color appropriate for the supplied color label.
     * 
     * @param colorLabel The legacy color label constant
     * @return The color, or <code>null</code> if not mappable
     */
    public static String getColor(int colorLabel) {
        switch (colorLabel) {
            case 1:
                return "lightblue"; // #9bceff ~ #ADD8E6
            case 2:
                return "darkblue"; // #6ca0df ~ #00008B
            case 3:
                return "purple"; // #a889d6 ~ #800080 
            case 4:
                return "pink"; // #e2b3e2 ~ #FFC0CB 
            case 5:
                return "red"; // #e7a9ab ~ #FF0000
            case 6:
                return "orange"; // #ffb870 ~ FFA500 
            case 7:
                return "yellow"; // #f2de88 ~ #FFFF00
            case 8:
                return "lightgreen"; // #c2d082 ~ #90EE90
            case 9:
                return "darkgreen"; // #809753 ~ #006400
            case 10:
                return "gray"; // #4d4d4d ~ #808080
            default:
                return null;
        }
    }

    /**
     * Gets a list of categories for the supplied comma-separated categories string.
     * 
     * @param categories The legacy categories string
     * @return The categories list
     */
    public static List<String> getCategories(String categories) {
        // TODO: escaping?
        if (Strings.isEmpty(categories)) {
            return null;
        }
        return Strings.splitAndTrim(categories, ",");
    }

    /**
     * Gets an alarm appropriate for the supplied reminder minutes.
     * 
     * @param reminder The legacy reminder value
     * @return The alarm
     */
    public static Alarm getAlarm(int reminder) {
        Alarm alarm = new Alarm();
        alarm.setAction(AlarmAction.DISPLAY);
        alarm.setDescription("Reminder");
        Trigger trigger = new Trigger();
        trigger.setDuration("-PT" + reminder + 'M');
        alarm.setTrigger(trigger);
        return alarm;
    }

    /**
     * Gets the recurrence rule for the supplied series pattern.
     * 
     * @param pattern The legacy series pattern
     * @return The recurrence rule, or <code>null</code> if not mappable
     */
    public static String getRecurrenceRule(SeriesPattern pattern) {
        if (null == pattern || null == pattern.getType()) {
            return null;
        }
        try {
            switch (pattern.getType().intValue()) {
                case 1: // com.openexchange.groupware.container.CalendarObject.DAILY
                    return getDailyRule(pattern).toString();
                case 2: // com.openexchange.groupware.container.CalendarObject.WEEKLY
                    return getWeeklyRule(pattern).toString();
                case 3: // com.openexchange.groupware.container.CalendarObject.MONTHLY
                    return getMonthlyRule(pattern).toString();
                case 4: // com.openexchange.groupware.container.CalendarObject.YEARLY
                    return getYearlyRule(pattern).toString();
                default:
                    return null;
            }
        } catch (InvalidRecurrenceRuleException e) {
            //TODO
            e.printStackTrace();
            return null;
        }
    }

    private static RecurrenceRule getDailyRule(SeriesPattern pattern) {
        RecurrenceRule rule = new RecurrenceRule(Freq.DAILY);
        if (null != pattern.getOccurrences()) {
            rule.setCount(i(pattern.getOccurrences()));
        } else if (null != pattern.getSeriesEnd()) {
            rule.setUntil(new DateTime(l(pattern.getSeriesEnd())));
        }
        if (null != pattern.getInterval()) {
            rule.setInterval(i(pattern.getInterval()));
        }
        return rule;
    }

    private static RecurrenceRule getWeeklyRule(SeriesPattern pattern) {
        RecurrenceRule rule = new RecurrenceRule(Freq.WEEKLY);
        if (null != pattern.getOccurrences()) {
            rule.setCount(i(pattern.getOccurrences()));
        } else if (null != pattern.getSeriesEnd()) {
            rule.setUntil(new DateTime(l(pattern.getSeriesEnd())));
        }
        if (null != pattern.getInterval()) {
            rule.setInterval(i(pattern.getInterval()));
        }
        if (null != pattern.getDaysOfWeek()) {
            rule.setByDayPart(getWeekdays(pattern.getDaysOfWeek()));
        }
        return rule;
    }

    private static RecurrenceRule getMonthlyRule(SeriesPattern pattern) throws InvalidRecurrenceRuleException {
        RecurrenceRule rule = new RecurrenceRule(Freq.MONTHLY);
        if (null != pattern.getOccurrences()) {
            rule.setCount(i(pattern.getOccurrences()));
        } else if (null != pattern.getSeriesEnd()) {
            rule.setUntil(new DateTime(l(pattern.getSeriesEnd())));
        }
        if (null != pattern.getInterval()) {
            rule.setInterval(i(pattern.getInterval()));
        }
        if (null != pattern.getDaysOfWeek()) {
            rule.setByDayPart(getWeekdays(pattern.getDaysOfWeek()));
            int weekNo = pattern.getDayOfMonth();
            rule.setByPart(Part.BYSETPOS, I(5 == weekNo ? -1 : weekNo));
        } else if (null != pattern.getDayOfMonth()) {
            rule.setByPart(Part.BYMONTHDAY, pattern.getDayOfMonth());
        }
        return rule;
    }

    private static RecurrenceRule getYearlyRule(SeriesPattern pattern) throws InvalidRecurrenceRuleException {
        RecurrenceRule rule = new RecurrenceRule(Freq.YEARLY);
        if (null != pattern.getOccurrences()) {
            rule.setCount(i(pattern.getOccurrences()));
        } else if (null != pattern.getSeriesEnd()) {
            rule.setUntil(new DateTime(l(pattern.getSeriesEnd())));
        }
        if (null != pattern.getInterval()) {
            rule.setInterval(i(pattern.getInterval()));
        }
        if (null != pattern.getDaysOfWeek() && 0 < pattern.getDaysOfWeek().intValue()) {
            rule.setByDayPart(getWeekdays(pattern.getDaysOfWeek()));
            rule.setByPart(Part.BYMONTH, pattern.getMonth());
            rule.setByPart(Part.BYSETPOS, pattern.getDayOfMonth());
        } else if (null != pattern.getMonth()) {
            rule.setByPart(Part.BYMONTH, pattern.getMonth());
            if (null != pattern.getDayOfMonth()) {
                rule.setByPart(Part.BYMONTHDAY, pattern.getDayOfMonth());
            }
        }
        return rule;
    }

    private static List<WeekdayNum> getWeekdays(int daysOfWeek) {
        List<WeekdayNum> weekdays = new ArrayList<>();
        if (1 == (daysOfWeek & 1)) {
            weekdays.add(new WeekdayNum(0, Weekday.SU));
        }
        if (2 == (daysOfWeek & 2)) {
            weekdays.add(new WeekdayNum(0, Weekday.MO));
        }
        if (4 == (daysOfWeek & 4)) {
            weekdays.add(new WeekdayNum(0, Weekday.TU));
        }
        if (8 == (daysOfWeek & 8)) {
            weekdays.add(new WeekdayNum(0, Weekday.WE));
        }
        if (16 == (daysOfWeek & 16)) {
            weekdays.add(new WeekdayNum(0, Weekday.TH));
        }
        if (32 == (daysOfWeek & 32)) {
            weekdays.add(new WeekdayNum(0, Weekday.FR));
        }
        if (64 == (daysOfWeek & 64)) {
            weekdays.add(new WeekdayNum(0, Weekday.SA));
        }
        return weekdays;
    }

    /**
     * Initializes a new {@link Appointment2Event}.
     */
    private Appointment2Event() {
        super();
    }

}
