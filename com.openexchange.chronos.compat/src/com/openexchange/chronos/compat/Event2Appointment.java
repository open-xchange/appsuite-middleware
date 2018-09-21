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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.Trigger;
import com.openexchange.chronos.Trigger.Related;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;

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
        return false == Classification.PUBLIC.equals(classification);
    }

    /**
     * Gets the "shown as" value based on the supplied time transparency.
     *
     * @param transparency The time transparency
     * @return The legacy "shown as" constant
     */
    public static int getShownAs(Transp transparency) {
        if (ShownAsTransparency.class.isInstance(transparency)) {
            return ((ShownAsTransparency) transparency).getShownAs();
        }
        switch (transparency.getValue()) {
            case Transp.TRANSPARENT:
                return 4; // com.openexchange.groupware.container.Appointment.FREE
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
        if (ParticipationStatus.ACCEPTED.equals(status)) {
            return 1; // com.openexchange.groupware.container.participants.ConfirmStatus.ACCEPT
        }
        if (ParticipationStatus.DECLINED.equals(status)) {
            return 2; // com.openexchange.groupware.container.participants.ConfirmStatus.DECLINE
        }
        if (ParticipationStatus.TENTATIVE.equals(status)) {
            return 3; // com.openexchange.groupware.container.participants.ConfirmStatus.TENTATIVE
        }
        return 0; // com.openexchange.groupware.container.participants.ConfirmStatus.NONE
    }

    /**
     * Gets the "participant type" value based on the supplied calendar user type.
     *
     * @param cuType The calendar user type
     * @param internal <code>true</code> for an internal entity, <code>false</code>, otherwise
     * @return The legacy "participant type" constant
     */
    public static int getParticipantType(CalendarUserType cuType, boolean internal) {
        if (CalendarUserType.GROUP.equals(cuType)) {
            if (internal) {
                return 2; // com.openexchange.groupware.container.Participant.GROUP
            } else {
                return 6; // com.openexchange.groupware.container.Participant.EXTERNAL_GROUP
            }
        }
        if (CalendarUserType.INDIVIDUAL.equals(cuType)) {
            if (internal) {
                return 1; // com.openexchange.groupware.container.Participant.USER
            } else {
                return 5; // com.openexchange.groupware.container.Participant.EXTERNAL_USER
            }
        }
        if (CalendarUserType.ROOM.equals(cuType) || CalendarUserType.RESOURCE.equals(cuType)) {
            return 3; // com.openexchange.groupware.container.Participant.RESOURCE
        }
        return 5; // com.openexchange.groupware.container.Participant.EXTERNAL_USER
    }

    /**
     * Gets an e-mail address string based on the supplied URI.
     * <p/>
     * ASCII-encoded (punycode) addresses with international domain names (IDN) are converted back to their unicode representation
     * implicitly.
     *
     * @param uri The URI string, e.g. <code>mailto:horst@example.org</code>
     * @return The e-mail address string, or the passed URI as-is in case of no <code>mailto</code>-protocol
     * @see {@link CalendarUtils#extractEMailAddress(String)}
     */
    public static String getEMailAddress(String uri) {
        return CalendarUtils.extractEMailAddress(uri);
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
            case "#CFE6FF":
            case "lightblue":
            case "#ADD8E6":
            case "#9bceff":
                return 1;
            case "#9BC8F7":
            case "darkblue":
            case "#6ca0df":
            case "#00008B":
                return 2;
            case "#B89AE9":
            case "purple":
            case "#a889d6":
            case "#800080":
                return 3;
            case "#F7C7E0":
            case "pink":
            case "#e2b3e2":
            case "#FFC0CB":
                return 4;
            case "#FFE2E2":
            case "red":
            case "#e7a9ab":
            case "#FF0000":
                return 5;
            case "#FDE2B9":
            case "orange":
            case "#ffb870":
            case "#FFA500":
                return 6;
            case "#FFEEB0":
            case "yellow":
            case "#f2de88":
            case "#FFFF00":
                return 7;
            case "#E6EFBD":
            case "lightgreen":
            case "#c2d082":
            case "#90EE90":
                return 8;
            case "#C5D481":
            case "darkgreen":
            case "#809753":
            case "#006400":
                return 9;
            case "#6B6B6B":
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
            stringBuilder.append(',').append(categories.get(i));
        }
        return stringBuilder.toString();
    }

    /**
     * Gets the "reminder" value based on the supplied list of alarms, in case an alarm is contained whose trigger is of type
     * {@link AlarmAction#DISPLAY}, and has a non-positive duration relative to the start date.
     *
     * @param alarms The alarms
     * @return The legacy reminder value, or <code>null</code> if no suitable reminder found
     */
    public static Integer getReminder(List<Alarm> alarms) {
        if (null != alarms && 0 < alarms.size()) {
            for (Alarm alarm : alarms) {
                Integer reminder = getReminder(alarm);
                if (null != reminder) {
                    return reminder;
                }
            }
        }
        return null;
    }

    /**
     * Gets the "reminder" value based on the supplied alarm, in case the alarm's trigger is of type {@link AlarmAction#DISPLAY}, and has
     * a non-positive duration relative to the start date.
     *
     * @param alarm The alarm
     * @return The legacy reminder value, or <code>null</code> if no suitable reminder found
     */
    public static Integer getReminder(Alarm alarm) {
        if (null != alarm && AlarmAction.DISPLAY.equals(alarm.getAction())) {
            Trigger trigger = alarm.getTrigger();
            if (null != trigger && (null == trigger.getRelated() || Related.START.equals(trigger.getRelated()))) {
                if (Strings.isNotEmpty(trigger.getDuration())) {
                    long triggerDuration = AlarmUtils.getTriggerDuration(trigger.getDuration());
                    if (0L >= triggerDuration) {
                        return Autoboxing.I((int) TimeUnit.MILLISECONDS.toMinutes(triggerDuration * -1));
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets the legacy series pattern for the supplied recurrence data.
     *
     * @param recurrenceService A reference to the recurrence service
     * @param recurrenceData The recurrence data
     * @return The series pattern, or <code>null</code> if passed <code>recurrenceData</code> reference was null
     */
    public static SeriesPattern getSeriesPattern(RecurrenceService recurrenceService, RecurrenceData recurrenceData) throws OXException {
        if (null == recurrenceData) {
            return null;
        }
        return Recurrence.getSeriesPattern(recurrenceService, recurrenceData);
    }

    /**
     * Gets the formerly used recurrence position, i.e. the 1-based, sequential position in the series where the original occurrence
     * would have been.
     *
     * @param recurrenceService A reference to the recurrence service
     * @param recurrenceData The recurrence data
     * @param recurrenceId The recurrence identifier
     * @return The recurrence position
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RECURRENCE_ID}
     */
    public static int getRecurrencePosition(RecurrenceService recurrenceService, RecurrenceData recurrenceData, RecurrenceId recurrenceId) throws OXException {
        RecurrenceIterator<RecurrenceId> iterator = recurrenceService.iterateRecurrenceIds(recurrenceData);
        while (iterator.hasNext()) {
            RecurrenceId next = iterator.next();
            if (0 == next.compareTo(recurrenceId)) {
                return iterator.getPosition();
            }
            if (next.getValue().after(recurrenceId.getValue())) {
                break;
            }
        }
        throw CalendarExceptionCodes.INVALID_RECURRENCE_ID.create(String.valueOf(recurrenceId), recurrenceData);
    }

    /**
     * Gets the formerly used recurrence date position, i.e. the date where the original occurrence would have been, as UTC date with
     * truncated time fraction.
     *
     * @param recurrenceId The recurrence identifier, i.e. the date where the original occurrence would have been
     * @return The legacy recurrence date position
     */
    public static Date getRecurrenceDatePosition(RecurrenceId recurrenceId) {
        if (PositionAwareRecurrenceId.class.isInstance(recurrenceId)) {
            return ((PositionAwareRecurrenceId) recurrenceId).getRecurrenceDatePosition();
        }
        return CalendarUtils.truncateTime(new Date(recurrenceId.getValue().getTimestamp()), TimeZones.UTC);
    }


    /**
     * Gets the formerly used recurrence date positions for a list of recurrence IDs, i.e. the date where the original occurrence would
     * have been, as UTC date with truncated time fraction.
     *
     * @param recurrenceIDs The recurrence identifiers, i.e. the dates where the original occurrence would have been
     * @return The corresponding list of legacy recurrence date position
     */
    public static List<Date> getRecurrenceDatePositions(Collection<RecurrenceId> recurrenceIDs) {
        if (null == recurrenceIDs) {
            return null;
        }
        List<Date> recurrenceDatePositions = new ArrayList<Date>(recurrenceIDs.size());
        for (RecurrenceId recurrenceID : recurrenceIDs) {
            recurrenceDatePositions.add(getRecurrenceDatePosition(recurrenceID));
        }
        return recurrenceDatePositions;
    }

    /**
     * Parses the supplied identifier to its numerical integer value.
     *
     * @param id The identifier to get the integer value for
     * @return The integer value of the supplied identifier.
     * @throws NumberFormatException
     */
    public static int asInt(String id) {
        return Integer.parseInt(id);
    }

    /**
     * Parses the supplied identifier to its numerical integer value.
     *
     * @param id The identifier to get the integer value for
     * @return The integer value of the supplied identifier.
     * @throws NumberFormatException
     */
    public static Integer asInteger(String id) {
        return null != id ? Integer.valueOf(id) : null;
    }

    /**
     * Parses the supplied identifier to its numerical integer value.
     *
     * @param id The identifier to get the integer value for
     * @param zeroForNull <code>true</code> to return the value <code>0</code> for an id <code>null</code>, <code>false</code>, otherwise
     * @return The integer value of the supplied identifier.
     * @throws NumberFormatException
     */
    public static int asInt(String id, boolean zeroForNull) {
        return null == id && zeroForNull ? 0 : Integer.parseInt(id);
    }

    /**
     * Parses the supplied identifier to its numerical integer value.
     *
     * @param id The identifier to get the integer value for
     * @param zeroForNull <code>true</code> to return the value <code>0</code> for an id <code>null</code>, <code>false</code>, otherwise
     * @return The integer value of the supplied identifier.
     * @throws NumberFormatException
     */
    public static Integer asInteger(String id, boolean zeroForNull) {
        if (null == id) {
            return zeroForNull ? Integer.valueOf(0) : null;
        } else {
            return Integer.valueOf(id);
        }
    }

    /**
     * Initializes a new {@link Event2Appointment}.
     */
    private Event2Appointment() {
        super();
    }

}
