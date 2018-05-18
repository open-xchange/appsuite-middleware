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

package com.openexchange.chronos.compat;

import static com.openexchange.chronos.common.CalendarUtils.initCalendar;
import static com.openexchange.chronos.common.CalendarUtils.truncateTime;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.Trigger;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;

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
        return privateFlag ? Classification.CONFIDENTIAL : Classification.PUBLIC;
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
     * Gets a string representation of the <code>mailto</code>-URI for the supplied e-mail address.
     * <p/>
     * Non-ASCII characters are encoded implicitly as per {@link URI#toASCIIString()}.
     *
     * @param emailAddress The e-mail address to get the URI for
     * @return The <code>mailto</code>-URI, or <code>null</code> if no address was passed
     * @see {@link URI#toASCIIString()}
     */
    public static String getURI(String emailAddress) {
        return CalendarUtils.getURI(emailAddress);
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
                return "#CFE6FF"; //"lightblue"; // #9bceff ~ #ADD8E6
            case 2:
                return "#9BC8F7"; // "darkblue"; // #6ca0df ~ #00008B
            case 3:
                return "#B89AE9"; // "purple"; // #a889d6 ~ #800080
            case 4:
                return "#F7C7E0"; //"pink"; // #e2b3e2 ~ #FFC0CB
            case 5:
                return "FFE2E2"; // "red"; // #e7a9ab ~ #FF0000
            case 6:
                return "#FDE2B9"; // "orange"; // #ffb870 ~ FFA500
            case 7:
                return "#FFEEB0"; // "yellow"; // #f2de88 ~ #FFFF00
            case 8:
                return "#E6EFBD"; // "lightgreen"; // #c2d082 ~ #90EE90
            case 9:
                return "#C5D481"; // "darkgreen"; // #809753 ~ #006400
            case 10:
                return "#6B6B6B"; // "gray"; // #4d4d4d ~ #808080
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
        String[] splitted = Strings.splitByCommaNotInQuotes(categories);
        return null != splitted ? Arrays.asList(splitted) : null;
    }

    /**
     * Gets an alarm appropriate for the supplied reminder minutes.
     *
     * @param reminder The legacy reminder value
     * @return The alarm
     */
    public static Alarm getAlarm(int reminder) {
        Alarm alarm = new Alarm(new Trigger(AlarmUtils.getDuration(true, 0, 0, 0, reminder, 0)), AlarmAction.DISPLAY);
        alarm.setDescription("Reminder");
        return alarm;
    }

    /**
     * Gets the recurrence data for the supplied series pattern.
     *
     * @param pattern The legacy series pattern
     * @param timeZone The timezone of the corresponding appointment
     * @param fulltime <code>true</code> if the corresponding appointment is marked as <i>fulltime</i>, <code>false</code>, otherwise
     * @return The recurrence data, or <code>null</code> if not mappable
     */
    public static RecurrenceData getRecurrenceData(SeriesPattern pattern, TimeZone timeZone, boolean fulltime) throws OXException {
        if (null == pattern || null == pattern.getType()) {
            return null;
        }
        String recurrenceRule = Recurrence.getRecurrenceRule(pattern, timeZone, fulltime);
        long timestamp = pattern.getSeriesStart().longValue();
        return new DefaultRecurrenceData(recurrenceRule, fulltime ? new DateTime(null, timestamp).toAllDay() : new DateTime(timeZone, timestamp), null);
    }

    /**
     * Calculates the recurrence identifier, i.e. the start time of a specific occurrence of a recurring event, based on the legacy
     * recurrence date position.
     *
     * @param recurrenceService A reference to the recurrence service
     * @param recurrenceData The corresponding recurrence data
     * @param recurrenceDatePosition The legacy recurrence date position, i.e. the date where the original occurrence would have been, as
     *            UTC date with truncated time fraction
     * @return The recurrence identifier
     * @throws {@link CalendarExceptionCodes#INVALID_RECURRENCE_ID}
     */
    public static RecurrenceId getRecurrenceID(RecurrenceService recurrenceService, RecurrenceData recurrenceData, Date recurrenceDatePosition) throws OXException {
        Calendar calendar = initCalendar(TimeZones.UTC, (Date) null);
        RecurrenceIterator<RecurrenceId> iterator = recurrenceService.iterateRecurrenceIds(recurrenceData, recurrenceDatePosition, null);
        while (iterator.hasNext()) {
            DateTime next = iterator.next().getValue();
            calendar.setTimeInMillis(next.getTimestamp());
            long nextDatePosition = truncateTime(calendar).getTimeInMillis();
            if (recurrenceDatePosition.getTime() == nextDatePosition) {
                return new PositionAwareRecurrenceId(recurrenceData, next, iterator.getPosition(), calendar.getTime());
            }
            if (nextDatePosition > recurrenceDatePosition.getTime()) {
                break;
            }
        }
        throw CalendarExceptionCodes.INVALID_RECURRENCE_ID.create("legacy recurrence date position " + recurrenceDatePosition.getTime(), recurrenceData.getRecurrenceRule());
    }

    /**
     * Calculates the recurrence identifiers, i.e. the start times of the specific occurrences of a recurring event, for a list of legacy
     * recurrence date position.
     *
     * @param recurrenceService A reference to the recurrence service
     * @param recurrenceData The corresponding recurrence data
     * @param recurrenceDatePositions The legacy recurrence date positions, i.e. the dates where the original occurrences would have been,
     *            as UTC date with truncated time fraction
     * @return The recurrence identifiers
     * @throws {@link CalendarExceptionCodes#INVALID_RECURRENCE_ID}
     */
    public static SortedSet<RecurrenceId> getRecurrenceIDs(RecurrenceService recurrenceService, RecurrenceData recurrenceData, Collection<Date> recurrenceDatePositions) throws OXException {
        if (null == recurrenceDatePositions) {
            return null;
        }
        if (1 < recurrenceDatePositions.size()) {
            List<Date> dateList = new ArrayList<Date>(recurrenceDatePositions);
            Collections.sort(dateList);
            recurrenceDatePositions = dateList;
        }
        SortedSet<RecurrenceId> recurrenceIDs = new TreeSet<RecurrenceId>();
        RecurrenceIterator<RecurrenceId> iterator = recurrenceService.iterateRecurrenceIds(recurrenceData);
        nextPosition: for (Date recurrenceDatePosition : recurrenceDatePositions) {
            while (iterator.hasNext()) {
                DateTime next = iterator.next().getValue();
                long nextDatePosition = truncateTime(initCalendar(TimeZones.UTC, next.getTimestamp())).getTimeInMillis();
                if (recurrenceDatePosition.getTime() == nextDatePosition) {
                    recurrenceIDs.add(new PositionAwareRecurrenceId(recurrenceData, next, iterator.getPosition(), recurrenceDatePosition));
                    continue nextPosition;
                }
                if (nextDatePosition > recurrenceDatePosition.getTime()) {
                    break;
                }
            }
            throw CalendarExceptionCodes.INVALID_RECURRENCE_ID.create("legacy recurrence date position " + recurrenceDatePosition.getTime(), recurrenceData.getRecurrenceRule());
        }
        return recurrenceIDs;
    }

    /**
     * Gets the recurrence identifier, i.e. the original start time of a recurrence instance, based on the supplied legacy recurrence
     * position number.
     *
     * @param recurrenceService A reference to the recurrence service
     * @param recurrenceData The corresponding recurrence data
     * @param recurrencePosition The legacy, 1-based recurrence position
     * @return The recurrence identifier
     * @throws {@link CalendarExceptionCodes#INVALID_RECURRENCE_ID}
     */
    public static RecurrenceId getRecurrenceID(RecurrenceService recurrenceService, RecurrenceData recurrenceData, int recurrencePosition) throws OXException {
        RecurrenceIterator<RecurrenceId> iterator = recurrenceService.iterateRecurrenceIds(recurrenceData, Autoboxing.I(recurrencePosition), null);
        while (iterator.hasNext()) {
            DateTime next = iterator.next().getValue();
            if (recurrencePosition == iterator.getPosition()) {
                return new PositionAwareRecurrenceId(recurrenceData, next, recurrencePosition, truncateTime(new Date(next.getTimestamp()), TimeZones.UTC));
            }
        }
        throw CalendarExceptionCodes.INVALID_RECURRENCE_ID.create("legacy recurrence position " + recurrencePosition, recurrenceData.getRecurrenceRule());
    }

    /**
     * Gets the string representation of the supplied numerical identifier.
     *
     * @param id The identifier to get the string representation for
     * @return The string representation of the supplied numerical identifier.
     */
    public static String asString(int id) {
        return String.valueOf(id);
    }

    /**
     * Gets the string representation of the supplied numerical identifier.
     *
     * @param id The identifier to get the string representation for
     * @return The string representation of the supplied numerical identifier.
     */
    public static String asString(Integer id) {
        return null == id ? null : id.toString();
    }

    /**
     * Gets the string representation of the supplied numerical identifier.
     *
     * @param id The identifier to get the string representation for
     * @param zeroAsNull <code>true</code> to return <code>null</code> for the id <code>0</code>, <code>false</code>, otherwise
     * @return The string representation of the supplied numerical identifier.
     */
    public static String asString(int id, boolean zeroAsNull) {
        return zeroAsNull && 0 == id ? null : String.valueOf(id);
    }

    /**
     * Gets the string representation of the supplied numerical identifier.
     *
     * @param id The identifier to get the string representation for
     * @param zeroAsNull <code>true</code> to return <code>null</code> for the id <code>0</code>, <code>false</code>, otherwise
     * @return The string representation of the supplied numerical identifier.
     */
    public static String asString(Integer id, boolean zeroAsNull) {
        return null == id ? null : asString(id.intValue(), zeroAsNull);
    }

    /**
     * Initializes a new {@link Appointment2Event}.
     */
    private Appointment2Event() {
        super();
    }

}
