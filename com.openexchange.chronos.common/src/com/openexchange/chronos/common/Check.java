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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.SearchStrings.lengthWithoutWildcards;
import static com.openexchange.tools.arrays.Arrays.contains;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TimeZone;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.SelfProtectionFactory.SelfProtection;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.Mapping;

/**
 * {@link Check}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Check {

    /**
     * Checks an event's geo location for validity.
     *
     * @param event The event to check
     * @return The passed event's geo location, after it was checked for validity
     * @throws OXException {@link CalendarExceptionCodes#INVALID_GEO_LOCATION}
     */
    public static double[] geoLocationIsValid(Event event) throws OXException {
        double[] geo = event.getGeo();
        if (null != geo) {
            if (2 != geo.length) {
                throw CalendarExceptionCodes.INVALID_GEO_LOCATION.create(geo);
            }
            double latitude = geo[0];
            double longitude = geo[1];
            if (90 < latitude || -90 > latitude || 180 < longitude || -180 > longitude) {
                throw CalendarExceptionCodes.INVALID_GEO_LOCATION.create(geo);
            }
        }
        return geo;
    }

    /**
     * Checks an event's recurrence rule for validity.
     *
     * @param recurrenceService A reference to the recurrence service
     * @param event The event to check
     * @return The passed event's recurrence rule, after it was checked for validity
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RRULE}
     */
    public static String recurrenceRuleIsValid(RecurrenceService recurrenceService, Event event) throws OXException {
        String recurrenceRule = event.getRecurrenceRule();
        if (event.containsRecurrenceRule() && null != recurrenceRule) {
            recurrenceService.validate(new DefaultRecurrenceData(recurrenceRule, event.getStartDate(), null));
        }
        return recurrenceRule;
    }

    /**
     * Ensures that all recurrence identifiers are valid for a specific recurring event series, i.e. the targeted occurrences
     * are actually part of the series.
     *
     * @param recurrenceService A reference to the recurrence service
     * @param seriesMaster The series master event providing the recurrence information
     * @param recurrenceID The recurrence identifier
     * @return The passed list of recurrence identifiers, after their existence was checked
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RECURRENCE_ID}
     */
    public static SortedSet<RecurrenceId> recurrenceIdsExist(RecurrenceService recurrenceService, Event seriesMaster, SortedSet<RecurrenceId> recurrenceIDs) throws OXException {
        if (null != recurrenceIDs) {
            for (RecurrenceId recurrenceID : recurrenceIDs) {
                recurrenceIdExists(recurrenceService, seriesMaster, recurrenceID);
            }
        }
        return recurrenceIDs;
    }

    /**
     * Ensures that a specific recurrence identifier is valid for a specific recurring event series, i.e. the targeted occurrence
     * is actually part of the series.
     *
     * @param recurrenceService A reference to the recurrence service
     * @param seriesMaster The series master event providing the recurrence information
     * @param recurrenceID The recurrence identifier
     * @return The passed recurrence identifier, after it was checked for validity
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RECURRENCE_ID}
     */
    public static RecurrenceId recurrenceIdExists(RecurrenceService recurrenceService, Event seriesMaster, RecurrenceId recurrenceID) throws OXException {
        RecurrenceData recurrenceData = new DefaultRecurrenceData(seriesMaster.getRecurrenceRule(), seriesMaster.getStartDate(), null);
        Iterator<RecurrenceId> iterator = recurrenceService.iterateRecurrenceIds(recurrenceData, new Date(recurrenceID.getValue().getTimestamp()), null);
        if (false == iterator.hasNext()) {
            throw CalendarExceptionCodes.INVALID_RECURRENCE_ID.create(String.valueOf(recurrenceID), recurrenceData);
        }
        return recurrenceID;
    }

    /**
     * Checks that the folder identifier matches a specific expected folder id.
     *
     * @param folderId The folder identifier to check
     * @param expectedFolderId The expected folder id to check against
     * @return The passed folder identifier, after it was checked
     * @throws OXException {@link CalendarExceptionCodes#FOLDER_NOT_FOUND}
     */
    public static String folderMatches(String folderId, String expectedFolderId) throws OXException {
        if (false == Objects.equals(expectedFolderId, folderId)) {
            throw CalendarExceptionCodes.FOLDER_NOT_FOUND.create(folderId);
        }
        return folderId;
    }

    /**
     * Checks that the folder identifier within the supplied full event identifier matches a specific expected folder id.
     *
     * @param eventID The full event identifier to check
     * @param expectedFolderId The expected folder id to check against
     * @return The passed event identifier, after it was checked
     * @throws OXException {@link CalendarExceptionCodes#EVENT_NOT_FOUND_IN_FOLDER}
     */
    public static EventID parentFolderMatches(EventID eventID, String expectedFolderId) throws OXException {
        if (null != eventID && false == Objects.equals(expectedFolderId, eventID.getFolderID())) {
            throw CalendarExceptionCodes.EVENT_NOT_FOUND_IN_FOLDER.create(eventID.getFolderID(), eventID.getObjectID());
        }
        return eventID;
    }

    /**
     * Checks that all folder identifiers within the supplied list of full event identifiers match a specific expected folder id.
     *
     * @param eventIDs The list of full event identifiers to check
     * @param expectedFolderId The expected folder id to check against
     * @return The passed event identifiers, after all were checked
     * @throws OXException {@link CalendarExceptionCodes#EVENT_NOT_FOUND_IN_FOLDER}
     */
    public static List<EventID> parentFolderMatches(List<EventID> eventIDs, String expectedFolderId) throws OXException {
        if (null != eventIDs) {
            for (EventID eventID : eventIDs) {
                parentFolderMatches(eventID, expectedFolderId);
            }
        }
        return eventIDs;
    }

    /**
     * Checks that the supplied timezone identifier is valid, i.e. a corresponding Java timezone exists.
     *
     * @param timeZoneID The timezone identifier to check, or <code>null</code> to skip the check
     * @return The identifier of the matching timezone
     * @throws OXException {@link CalendarExceptionCodes#INVALID_TIMEZONE}
     */
    public static String timeZoneExists(String timeZoneID) throws OXException {
        TimeZone timeZone = CalendarUtils.optTimeZone(timeZoneID, null);
        if (null == timeZone) {
            throw CalendarExceptionCodes.INVALID_TIMEZONE.create(timeZoneID);
        }
        return timeZone.getID();
    }

    /**
     * Checks that a list of alarms are valid, i.e. they all contain all mandatory properties.
     *
     * @param alarms The alarms to check
     * @return The passed alarms, after they were checked for validity
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RRULE}
     */
    public static List<Alarm> alarmsAreValid(List<Alarm> alarms) throws OXException {
        if (null != alarms && 0 < alarms.size()) {
            for (Alarm alarm : alarms) {
                alarmIsValid(alarm);
            }
        }
        return alarms;
    }

    /**
     * Checks that the supplied alarm is valid, i.e. it contains all mandatory properties.
     *
     * @param alarm The alarm to check
     * @return The passed alarm, after it was checked for validity
     * @throws OXException {@link CalendarExceptionCodes#INVALID_ALARM}
     */
    public static Alarm alarmIsValid(Alarm alarm) throws OXException {
        return alarmIsValid(alarm, null);
    }

    /**
     * Checks that the supplied alarm is valid, i.e. it contains all mandatory properties.
     *
     * @param alarm The alarm to check
     * @param fields The alarm fields to check, or <code>null</code> to check all fields
     * @return The passed alarm, after it was checked for validity
     * @throws OXException {@link CalendarExceptionCodes#INVALID_ALARM}
     */
    public static Alarm alarmIsValid(Alarm alarm, AlarmField[] fields) throws OXException {
        /*
         * action and trigger are both required for any type of alarm
         */
        if (null == alarm.getAction() && (null == fields || contains(fields, AlarmField.ACTION))) {
            throw CalendarExceptionCodes.MANDATORY_FIELD.create(AlarmField.ACTION.toString());
        }
        if ((null == alarm.getTrigger() || null == alarm.getTrigger().getDateTime() && null == alarm.getTrigger().getDuration()) &&
            (null == fields || contains(fields, AlarmField.TRIGGER))) {
            throw CalendarExceptionCodes.MANDATORY_FIELD.create(AlarmField.TRIGGER.toString());
        }
        /*
         * check further properties based on alarm type
         */
        if (AlarmAction.DISPLAY.equals(alarm.getAction())) {
            if (null == alarm.getDescription() && (null == fields || contains(fields, AlarmField.DESCRIPTION))) {
                throw CalendarExceptionCodes.MANDATORY_FIELD.create(AlarmField.DESCRIPTION.toString());
            }
        }
        if( AlarmAction.SMS.equals(alarm.getAction())) {
            if(!alarm.containsAttendees()) {
                throw CalendarExceptionCodes.MANDATORY_FIELD.create(AlarmField.ATTENDEES.toString());
            }
            for(Attendee att : alarm.getAttendees()) {
                if(!att.containsUri() || !att.getUri().toLowerCase().contains("tel:")) {
                    throw CalendarExceptionCodes.INVALID_ALARM.create(alarm);
                }
            }
        }
        return alarm;
    }

    /**
     * Checks that the supplied alarm has a <i>relative</i> trigger defined.
     *
     * @param alarm The alarm to check
     * @return The passed alarm, after it was checked for validity
     * @throws OXException {@link CalendarExceptionCodes#INVALID_ALARM}
     */
    public static Alarm hasReleativeTrigger(Alarm alarm) throws OXException {
        if (false == AlarmUtils.hasRelativeTrigger(alarm)) {
            throw CalendarExceptionCodes.INVALID_ALARM.create(String.valueOf(alarm));
        }
        return alarm;
    }

    /**
     * Checks that each alarm in the supplied list has a <i>relative</i> trigger defined.
     *
     * @param alarms The alarms to check
     * @return The passed alarms, after they were checked for validity
     * @throws OXException {@link CalendarExceptionCodes#INVALID_ALARM}
     */
    public static List<Alarm> haveReleativeTriggers(List<Alarm> alarms) throws OXException {
        if (null != alarms && 0 < alarms.size()) {
            for (Alarm alarm : alarms) {
                if (false == AlarmUtils.hasRelativeTrigger(alarm)) {
                    throw CalendarExceptionCodes.INVALID_ALARM.create(String.valueOf(alarm));
                }
            }
        }
        return alarms;
    }

    /**
     * Checks that the supplied availability is valid, i.e. its available definitions contain all mandatory properties.
     *
     * @param recurrenceService A reference to the recurrence service
     * @param availability The availability to check
     * @return The passed availability, after it was checked for validity
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RRULE}
     */
    public static Available[] availabilityIsValid(RecurrenceService recurrenceService, Available[] availability) throws OXException {
        if (null != availability) {
            for (Available available : availability) {
                Check.availableIsValid(recurrenceService, available);
            }
        }
        return availability;
    }

    /**
     * Checks that the supplied available definition is valid, i.e. it contains all mandatory properties.
     *
     * @param recurrenceService A reference to the recurrence service
     * @param available The available to check
     * @return The passed available, after it was checked for validity
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RRULE}
     */
    private static Available availableIsValid(RecurrenceService recurrenceService, Available available) throws OXException {
        //TODO
        return available;
    }

    /**
     * Checks that the supplied calendar user's URI denotes a valid e-mail address.
     * <p/>
     * This method should only be invoked for <i>external</i> calendar users.
     *
     * @param calendarUser The (external) calendar user to check
     * @return The calendar user, after its URI has been checked for validity
     * @throws OXException {@link CalendarExceptionCodes#INVALID_CALENDAR_USER}
     */
    public static <T extends CalendarUser> T requireValidEMail(T calendarUser) throws OXException {
        String address = CalendarUtils.extractEMailAddress(calendarUser.getUri());
        if (null == address) {
            throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(calendarUser.getUri(), I(calendarUser.getEntity()), "");
        }
        try {
            new InternetAddress(address).validate();
        } catch (AddressException e) {
            throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(e, calendarUser.getUri(), I(calendarUser.getEntity()), "");
        }
        return calendarUser;
    }

    /**
     * Checks that all specified mandatory fields are <i>set</i> and not <code>null</code> in the event.
     *
     * @param event The event to check
     * @param fields The mandatory fields
     * @throws OXException {@link CalendarExceptionCodes#MANDATORY_FIELD}
     */
    public static void mandatoryFields(Event event, EventField... fields) throws OXException {
        if (null != fields) {
            for (EventField field : fields) {
                Mapping<? extends Object, Event> mapping = EventMapper.getInstance().get(field);
                if (false == mapping.isSet(event) || null == mapping.get(event)) {
                    String readableName = String.valueOf(field); //TODO i18n
                    throw CalendarExceptionCodes.MANDATORY_FIELD.create(readableName, String.valueOf(field));
                }
            }
        }
    }

    /**
     * Checks that the supplied search pattern length is equal to or greater than a configured minimum.
     *
     * @param minimumPatternLength The minimum search pattern length, or <code>0</code> for no limitation
     * @param pattern The pattern to check
     * @return The passed pattern, after the length was checked
     * @throws OXException {@link CalendarExceptionCodes#QUERY_TOO_SHORT}
     */
    public static String minimumSearchPatternLength(String pattern, int minimumPatternLength) throws OXException {
        if (null != pattern && 0 < minimumPatternLength && lengthWithoutWildcards(pattern) < minimumPatternLength) {
            throw CalendarExceptionCodes.QUERY_TOO_SHORT.create(I(minimumPatternLength), pattern);
        }
        return pattern;
    }

    /**
     * Checks that each of the supplied search patterns length is equal to or greater than a configured minimum.
     *
     * @param minimumPatternLength The minimum search pattern length, or <code>0</code> for no limitation
     * @param patterns The patterns to check
     * @return The passed patterns, after their length was checked
     * @throws OXException {@link CalendarExceptionCodes#QUERY_TOO_SHORT}
     */
    public static List<String> minimumSearchPatternLength(List<String> patterns, int minimumPatternLength) throws OXException {
        if (null != patterns && 0 < minimumPatternLength) {
            for (String pattern : patterns) {
                Check.minimumSearchPatternLength(pattern, minimumPatternLength);
            }
        }
        return patterns;
    }

    /**
     * Checks that the size of an event collection does not exceed the maximum allowed size.
     *
     * @param selfProtection A reference to the self protection helper
     * @param events The collection of events to check
     * @param requestedFields The requested fields, or <code>null</code> if all event fields were requested
     * @return The passed collection, after the size was checked
     */
    public static <T extends Collection<Event>> T resultSizeNotExceeded(SelfProtection selfProtection, T events, EventField[] requestedFields) throws OXException {
        if (null == events) {
            return null;
        }
        selfProtection.checkEventCollection(events, requestedFields);
        return events;
    }

    /**
     * Checks that the size of an event collection does not exceed the maximum allowed size.
     *
     * @param selfProtection A reference to the self protection helper
     * @param eventsResults The event results map to check
     * @param requestedFields The requested fields, or <code>null</code> if all event fields were requested
     * @return The passed event result map, after the size was checked
     */
    public static <K> Map<K, ? extends EventsResult> resultSizeNotExceeded(SelfProtection selfProtection, Map<K, ? extends EventsResult> eventsResults, EventField[] requestedFields) throws OXException {
        if (null == eventsResults) {
            return null;
        }
        selfProtection.checkEventResults(eventsResults, requestedFields);
        return eventsResults;
    }

}
