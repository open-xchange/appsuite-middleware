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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.EventStatus;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.Trigger;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.exception.OXException;
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
     * Gets the time transparency appropriate for the supplied "shown as" value.
     *
     * @param confirm The legacy "shown as" constant
     * @return The time transparency, defaulting to {@value Transp#OPAQUE} if not mappable
     */
    public static Transp getTransparency(int shownAs) {
        return ShownAsTransparency.getTransparency(shownAs);
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
        return Recurrence.getRecurrenceRule(pattern);
    }

    /**
     * Calculates the recurrence identifier, i.e. the start time of a specific occurrence of a recurring event, based on the legacy
     * recurrence date position.
     *
     * @param recurrenceRule The recurrence rule
     * @param seriesStart The start-date of the series, i.e. the actual start-date of the series master
     * @param timeZone The timezone to consider (should be <code>UTC</code> for "all-day" event series)
     * @param allDay <code>true</code> for an "all-day" event series, <code>false</code>, otherwise
     * @param recurrenceDatePosition The legacy recurrence date position, i.e. the date where the original occurrence would have been, as
     *            UTC date with truncated time fraction
     * @return The recurrence identifier, or <code>null</code> if no matching recurrence identifier was found
     */
    public static Date getRecurrenceID(String recurrenceRule, Date seriesStart, TimeZone timeZone, boolean allDay, Date recurrenceDatePosition) throws OXException {
        Calendar calendar = CalendarUtils.initCalendar(TimeZone.getTimeZone("UTC"), seriesStart);
        RecurrenceRuleIterator iterator = Recurrence.getRecurrenceIterator(recurrenceRule, seriesStart.getTime(), timeZone, allDay);
        while (iterator.hasNext()) {
            long nextMillis = iterator.nextMillis();
            calendar.setTimeInMillis(nextMillis);
            long nextPosition = CalendarUtils.truncateTime(calendar).getTimeInMillis();
            if (recurrenceDatePosition.getTime() == nextPosition) {
                return new Date(nextMillis);
            }
            if (nextPosition > recurrenceDatePosition.getTime()) {
                break;
            }
        }
        return null;
    }

    /**
     * Calculates the recurrence identifiers, i.e. the start times of the specific occurrences of a recurring event, for a list of legacy
     * recurrence date position.
     *
     * @param recurrenceRule The recurrence rule
     * @param seriesStart The start-date of the series, i.e. the actual start-date of the series master
     * @param timeZone The timezone to consider (should be <code>UTC</code> for "all-day" event series)
     * @param allDay <code>true</code> for an "all-day" event series, <code>false</code>, otherwise
     * @param recurrenceDatePositions The legacy recurrence date positions, i.e. the dates where the original occurrences would have been,
     *            as UTC date with truncated time fraction
     * @return The recurrence identifiers
     */
    public static List<Date> getRecurrenceIDs(String recurrenceRule, Date seriesStart, TimeZone timeZone, boolean allDay, List<Date> recurrenceDatePositions) throws OXException {
        // TODO
        if (null == recurrenceDatePositions) {
            return null;
        }
        List<Date> recurrenceIDs = new ArrayList<Date>(recurrenceDatePositions.size());
        for (Date recurrenceDatePosition : recurrenceDatePositions) {
            recurrenceIDs.add(getRecurrenceID(recurrenceRule, seriesStart, timeZone, allDay, recurrenceDatePosition));
        }
        return recurrenceIDs;
    }

    /**
     * Gets the recurrence identifier, i.e. the original start time of a recurrence instance, based on the supplied legacy recurrence
     * position number.
     *
     * @param recurrenceRule The recurrence rule
     * @param seriesStart The start-date of the series, i.e. the actual start-date of the series master
     * @param timeZone The timezone to consider (should be <code>UTC</code> for "all-day" event series)
     * @param allDay <code>true</code> for an "all-day" event series, <code>false</code>, otherwise
     * @param recurrencePosition The legacy, 1-based recurrence position
     * @return The recurrence identifier, or <code>null</code> if no matching recurrence identifier was found
     */
    public static Date getRecurrenceID(String recurrenceRule, Date seriesStart, TimeZone timeZone, boolean allDay, int recurrencePosition) throws OXException {
        RecurrenceRuleIterator iterator = Recurrence.getRecurrenceIterator(recurrenceRule, seriesStart.getTime(), timeZone, allDay);
        int position = 0;
        while (iterator.hasNext()) {
            long nextMillis = iterator.nextMillis();
            if (++position == recurrencePosition) {
                return new Date(nextMillis);
            }
        }
        return null;
    }

    /**
     * Initializes a new {@link Appointment2Event}.
     */
    private Appointment2Event() {
        super();
    }

}
