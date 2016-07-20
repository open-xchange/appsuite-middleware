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
import static com.openexchange.java.Autoboxing.L;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
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
import com.openexchange.chronos.Trigger.Related;
import com.openexchange.java.Strings;

/**
 * {@link Event2Appointment}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Event2Appointment {

    /**
     * Gets the "private flag" value based on the supplied event classification.
     *
     * @param classification The event classification
     * @return The legacy "private flag"
     */
    public static boolean getPrivateFlag(Classification classification) {
        switch (classification) {
            case PUBLIC:
                return false;
            default:
                return true;
        }
    }

    /**
     * Gets the "shown as" value based on the supplied event status.
     *
     * @param eventStatus The event status
     * @return The legacy "shown as" constant
     */
    public static int getShownAs(EventStatus eventStatus) {
        switch (eventStatus) {
            case TENTATIVE:
                return 3; // com.openexchange.groupware.container.Appointment.TEMPORARY
            default:
                return 1; // com.openexchange.groupware.container.Appointment.RESERVED
        }
    }

    /**
     * Gets the "confirm" value based on the supplied participation status.
     *
     * @param status The participation status
     * @return The legacy "confirm" constant
     */
    public static int getConfirm(ParticipationStatus status) {
        switch (status) {
            case ACCEPTED:
                return 1; // com.openexchange.groupware.container.participants.ConfirmStatus.ACCEPT
            case DECLINED:
                return 2; // com.openexchange.groupware.container.participants.ConfirmStatus.DECLINE
            case TENTATIVE:
                return 3; // com.openexchange.groupware.container.participants.ConfirmStatus.TENTATIVE
            default:
                return 0; // com.openexchange.groupware.container.participants.ConfirmStatus.NONE
        }
    }

    /**
     * Gets the "participant type" value based on the supplied calendar user type.
     *
     * @param cuType The calendar user type
     * @param internal <code>true</code> for an internal entity, <code>false</code>, otherwise
     * @return The legacy "participant type" constant
     */
    public static int getParticipantType(CalendarUserType cuType, boolean internal) {
        if (null == cuType) {
            return 5;
        }
        switch (cuType) {
            case GROUP:
                if (internal) {
                    return 2; // com.openexchange.groupware.container.Participant.GROUP
                } else {
                    return 6; // com.openexchange.groupware.container.Participant.EXTERNAL_GROUP
                }
            case INDIVIDUAL:
                if (internal) {
                    return 1; // com.openexchange.groupware.container.Participant.USER
                } else {
                    return 5; // com.openexchange.groupware.container.Participant.EXTERNAL_USER
                }
            case ROOM:
            case RESOURCE:
                return 3; // com.openexchange.groupware.container.Participant.RESOURCE
            default:
                return 5; // com.openexchange.groupware.container.Participant.EXTERNAL_USER
        }
    }

    /**
     * Gets an e-mail address string based on the supplied URI.
     *
     * @param uri The URI string, e.g. <code>mailto:horst@example.org</code>
     * @return The e-mail address string, or the passed URI as-is in case of no <code>mailto</code>-protocol
     */
    public static String getEMailAddress(String uri) {
        if (Strings.isNotEmpty(uri) && uri.toLowerCase().startsWith("mailto:")) {
            return uri.substring(7);
        }
        return uri;
    }

    /**
     * Gets the "color label" value based on the supplied event color.
     *
     * @param color The CSS3 event color
     * @return The legacy color label, or <code>0</code> if not mappable
     */
    public static int getColorLabel(String color) {
        if (null == color) {
            return 0;
        }
        switch (color) {
            case "lightblue":
            case "#ADD8E6":
            case "#9bceff":
                return 1;
            case "darkblue":
            case "#6ca0df":
            case "#00008B":
                return 2;
            case "purple":
            case "#a889d6":
            case "#800080":
                return 3;
            case "pink":
            case "#e2b3e2":
            case "#FFC0CB":
                return 4;
            case "red":
            case "#e7a9ab":
            case "#FF0000":
                return 5;
            case "orange":
            case "#ffb870":
            case "#FFA500":
                return 6;
            case "yellow":
            case "#f2de88":
            case "#FFFF00":
                return 7;
            case "lightgreen":
            case "#c2d082":
            case "#90EE90":
                return 8;
            case "darkgreen":
            case "#809753":
            case "#006400":
                return 9;
            case "gray":
            case "#4d4d4d":
            case "#808080":
                return 10;
            default:
                return 0;
        }
    }

    /**
     * Gets the comma-separated "categories" string based on the supplied categories list.
     *
     * @param categories The list of categories
     * @return The legacy categories value
     */
    public static String getCategories(List<String> categories) {
        // TODO: escaping?
        if (null == categories || 0 == categories.size()) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(categories.get(0));
        for (int i = 1; i < categories.size(); i++) {
            stringBuilder.append(", ").append(categories.get(0));
        }
        return stringBuilder.toString();
    }

    /**
     * Gets the "reminder" value based on the supplied alarm list.
     *
     * @param alarms The alarms
     * @return The legacy reminder value, or <code>null</code> if no suitable reminder found
     */
    public static Integer getReminder(List<Alarm> alarms) {
        if (null != alarms && 0 < alarms.size()) {
            for (Alarm alarm : alarms) {
                if (AlarmAction.DISPLAY == alarm.getAction()) {
                    Trigger trigger = alarm.getTrigger();
                    if (null != trigger && (null == trigger.getRelated() || Related.START.equals(trigger.getRelated()))) {
                        Integer reminder = parseTriggerDuration(trigger.getDuration());
                        if (null != reminder) {
                            return reminder;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets the series pattern for the supplied recurrence rule.
     *
     * @param recurrenceRule The recurrence rule
     * @param timeZone The timezone of the event series
     * @param allDay <code>true</code> for an "all-day" event series, <code>false</code>, otherwise
     * @return The series pattern, or <code>null</code> if not mappable
     */
    public static SeriesPattern getSeriesPattern(String recurrenceRule, String timeZone, boolean allDay) {
        if (Strings.isNotEmpty(recurrenceRule)) {
            RecurrenceRule rule;
            try {
                rule = new RecurrenceRule(recurrenceRule);
            } catch (InvalidRecurrenceRuleException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
            switch (rule.getFreq()) {
                case DAILY:
                    return getDailyPattern(rule, timeZone, allDay);
                case WEEKLY:
                    return getWeeklyPattern(rule, timeZone, allDay);
                case MONTHLY:
                    return getMonthlyPattern(rule, timeZone, allDay);
                case YEARLY:
                    return getYearlyPattern(rule, timeZone, allDay);
                default:
                    break;
            }
        }
        return null;
    }

    private static SeriesPattern getDailyPattern(RecurrenceRule rule, String timeZone, boolean allDay) {
        SeriesPattern pattern = new SeriesPattern(1, timeZone, allDay); // com.openexchange.groupware.container.CalendarObject.DAILY
        pattern.setInterval(I(rule.getInterval()));
        if (null != rule.getCount()) {
            pattern.setOccurrences(I(rule.getCount()));
        }
        if (null != rule.getUntil()) {
            pattern.setSeriesEnd(L(rule.getUntil().getTimestamp()));
        }
        return pattern;
    }

    private static SeriesPattern getWeeklyPattern(RecurrenceRule rule, String timeZone, boolean allDay) {
        SeriesPattern pattern = new SeriesPattern(2, timeZone, allDay); // com.openexchange.groupware.container.CalendarObject.WEEKLY
        pattern.setInterval(I(rule.getInterval()));
        pattern.setDaysOfWeek(I(getDaysOfWeek(rule.getByDayPart())));
        if (null != rule.getCount()) {
            pattern.setOccurrences(I(rule.getCount()));
        }
        if (null != rule.getUntil()) {
            pattern.setSeriesEnd(L(rule.getUntil().getTimestamp()));
        }
        return pattern;
    }

    private static int getDayOfWeek(WeekdayNum weekday) {
        switch (weekday.weekday) {
            case SU:
                return 1;
            case MO:
                return 2;
            case TU:
                return 4;
            case WE:
                return 8;
            case TH:
                return 16;
            case FR:
                return 32;
            case SA:
                return 64;
            default:
                return 0;
        }
    }

    private static int getDaysOfWeek(List<WeekdayNum> weekdays) {
        int daysOfWeek = 0;
        if (null != weekdays && 0 < weekdays.size()) {
            for (WeekdayNum weekday : weekdays) {
                daysOfWeek |= getDayOfWeek(weekday);
            }
        }
        return daysOfWeek;
    }

    private static SeriesPattern getMonthlyPattern(RecurrenceRule rule, String timeZone, boolean allDay) {
        SeriesPattern pattern = new SeriesPattern(3, timeZone, allDay); // com.openexchange.groupware.container.CalendarObject.MONTHLY
        pattern.setInterval(I(rule.getInterval()));
        List<Integer> byMonthDayParts = rule.getByPart(Part.BYMONTHDAY);
        if (null != byMonthDayParts && 0 < byMonthDayParts.size()) {
            pattern.setDayOfMonth(byMonthDayParts.get(0));
        } else {
            List<Integer> byWeekNoParts = rule.getByPart(Part.BYWEEKNO);
            if (null != byWeekNoParts && 0 < byWeekNoParts.size()) {
                int weekNo = byWeekNoParts.get(0).intValue();
                pattern.setDayOfMonth(I(-1 == weekNo ? 5 : weekNo));
                pattern.setDaysOfWeek(I(getDaysOfWeek(rule.getByDayPart())));
            } else if (null != rule.getByDayPart() && 0 < rule.getByDayPart().size()) {
                WeekdayNum weekday = rule.getByDayPart().get(0);
                pattern.setDaysOfWeek(I(getDayOfWeek(weekday)));
                int weekNo = weekday.pos;
                pattern.setDayOfMonth(I(-1 == weekNo ? 5 : weekNo));
            }
        }
        if (null != rule.getCount()) {
            pattern.setOccurrences(I(rule.getCount()));
        }
        if (null != rule.getUntil()) {
            pattern.setSeriesEnd(L(rule.getUntil().getTimestamp()));
        }
        return pattern;
    }

    private static SeriesPattern getYearlyPattern(RecurrenceRule rule, String timeZone, boolean allDay) {
        SeriesPattern pattern = new SeriesPattern(4, timeZone, allDay); // com.openexchange.groupware.container.CalendarObject.YEARLY
        pattern.setInterval(I(rule.getInterval()));
        List<Integer> byMonthParts = rule.getByPart(Part.BYMONTH);
        if (null != byMonthParts && 0 < byMonthParts.size()) {
            pattern.setMonth(I(byMonthParts.get(0).intValue() - 1));
            List<Integer> byMonthDayParts = rule.getByPart(Part.BYMONTHDAY);
            if (null != byMonthDayParts && 0 < byMonthDayParts.size()) {
                pattern.setDayOfMonth(byMonthDayParts.get(0));
            } else {
                List<Integer> byWeekNoParts = rule.getByPart(Part.BYWEEKNO);
                if (null != byWeekNoParts && 0 < byWeekNoParts.size()) {
                    int weekNo = byWeekNoParts.get(0).intValue();
                    pattern.setDayOfMonth(I(-1 == weekNo ? 5 : weekNo));
                    pattern.setDaysOfWeek(I(getDaysOfWeek(rule.getByDayPart())));
                } else if (null != rule.getByDayPart() && 0 < rule.getByDayPart().size()) {
                    WeekdayNum weekday = rule.getByDayPart().get(0);
                    pattern.setDaysOfWeek(I(getDayOfWeek(weekday)));
                    int weekNo = weekday.pos;
                    pattern.setDayOfMonth(I(-1 == weekNo ? 5 : weekNo));
                }
            }
        }
        if (null != rule.getCount()) {
            pattern.setOccurrences(I(rule.getCount()));
        }
        if (null != rule.getUntil()) {
            pattern.setSeriesEnd(L(rule.getUntil().getTimestamp()));
        }
        return pattern;
    }

    /**
     * Parses a trigger duration string.
     *
     * @param duration The duration to parse
     * @return The total seconds of the parsed duration, or <code>null</code> if not parsable
     * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.3.6">RFC 5545, section 3.3.6</a>
     */
    private static Integer parseTriggerDuration(String duration) {
        if (Strings.isEmpty(duration)) {
            return null;
        }
        boolean negative = false;
        int weeks = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
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
                    weeks = Integer.parseInt(previousToken);
                    break;
                case "D":
                    days = Integer.parseInt(previousToken);
                    break;
                case "H":
                    hours = Integer.parseInt(previousToken);
                    break;
                case "M":
                    minutes = Integer.parseInt(previousToken);
                    break;
                case "S":
                    seconds = Integer.parseInt(previousToken);
                    break;
                case "T":
                case "P":
                default:
                    // skip
                    break;
            }
            previousToken = token;
        }
        long totalMinutes = TimeUnit.DAYS.toMinutes(7 * weeks + days) + TimeUnit.HOURS.toMinutes(hours) + minutes + TimeUnit.SECONDS.toMinutes(seconds);
        return I(negative ? (int) totalMinutes : -1 * (int) totalMinutes);
    }

    /**
     * Initializes a new {@link Event2Appointment}.
     */
    private Event2Appointment() {
        super();
    }

}
