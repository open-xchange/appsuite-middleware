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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.getObjectIDs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventStatus;
import com.openexchange.chronos.FbType;
import com.openexchange.chronos.FreeBusyTime;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.FreeBusyUtils;
import com.openexchange.chronos.compat.ShownAsTransparency;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;

/**
 * {@link FreeBusyPerformerUtil}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class FreeBusyPerformerUtil {

    //@formatter:off
    /** The event fields returned in free/busy queries by default */
    static final EventField[] FREEBUSY_FIELDS = {
        EventField.CREATED_BY, EventField.ID, EventField.SERIES_ID, EventField.FOLDER_ID, EventField.COLOR, EventField.CLASSIFICATION,
        EventField.SUMMARY, EventField.START_DATE, EventField.END_DATE, EventField.CATEGORIES, EventField.TRANSP, EventField.LOCATION,
        EventField.RECURRENCE_ID, EventField.RECURRENCE_RULE, EventField.STATUS
    };

    /** The restricted event fields returned in free/busy queries if the user has no access to the event */
    static final EventField[] RESTRICTED_FREEBUSY_FIELDS = { EventField.CREATED_BY, EventField.ID, EventField.SERIES_ID,
        EventField.CLASSIFICATION, EventField.START_DATE, EventField.END_DATE, EventField.TRANSP, EventField.RECURRENCE_ID,
        EventField.RECURRENCE_RULE, EventField.STATUS
    };
    //@formatter:on

    /**
     * Gets a resulting userized event occurrence for the free/busy result based on the supplied data of the master event. Only a subset
     * of properties is copied over, and a folder identifier is applied optionally, depending on the user's access permissions for the
     * actual event data.
     *
     * @param occurence The occurence
     * @param masterEvent The master event data
     * @param recurrenceId The recurrence identifier of the occurrence
     * @return The resulting event occurrence representing the free/busy slot
     */
    static Event getResultingOccurrence(Event occurence, Event masterEvent, RecurrenceId recurrenceId) {
        occurence.setRecurrenceRule(null);
        occurence.removeSeriesId();
        occurence.removeClassification();
        occurence.setRecurrenceId(recurrenceId);
        occurence.setStartDate(CalendarUtils.calculateStart(masterEvent, recurrenceId));
        occurence.setEndDate(CalendarUtils.calculateEnd(masterEvent, recurrenceId));
        return occurence;
    }

    /**
     * Normalizes the contained free/busy intervals. This means
     * <ul>
     * <li>the intervals are sorted chronologically, i.e. the earliest interval is first</li>
     * <li>all intervals beyond or above the 'from' and 'until' range are removed, intervals overlapping the boundaries are shortened to
     * fit</li>
     * <li>overlapping intervals are merged so that only the most conflicting ones of overlapping time ranges are used</li>
     * </ul>
     *
     * @param events The events to get the free/busy-times from
     * @param from The start date of the period to consider
     * @param until The end date of the period to consider
     * @param timeZone The timezone to consider if the event has <i>floating</i> dates
     */
    static List<FreeBusyTime> mergeFreeBusy(List<Event> events, Date from, Date until, TimeZone timeZone) {
        if (null == events || 0 == events.size()) {
            return Collections.emptyList(); // nothing to do
        }
        /*
         * get free/busy times, normalize to requested period & perform the merge
         */
        List<FreeBusyTime> freeBusyTimes = adjustToBoundaries(getFreeBusyTimes(events, timeZone), from, until);
        if (2 > freeBusyTimes.size()) {
            return freeBusyTimes; // nothing more to do
        }
        return FreeBusyUtils.mergeFreeBusy(freeBusyTimes);
    }

    /**
     * Gets the {@link FbType} for the given event
     * getFbType
     *
     * @param event The event
     * @return The {@link FbType}
     */
    private static FbType getFbType(Event event) {
        Transp transp = event.getTransp();
        if (null == transp) {
            return FbType.BUSY;
        }
        if (ShownAsTransparency.class.isInstance(transp)) {
            switch ((ShownAsTransparency) transp) {
                case ABSENT:
                    return FbType.BUSY_UNAVAILABLE;
                case FREE:
                    return FbType.FREE;
                case TEMPORARY:
                    return FbType.BUSY_TENTATIVE;
                default:
                    return FbType.BUSY;
            }
        }

        if (Transp.TRANSPARENT.equals(transp.getValue())) {
            return FbType.FREE;
        }
        if (event.getStatus() == null) {
            return FbType.BUSY;
        }
        if (EventStatus.TENTATIVE.equals(event.getStatus())) {
            return FbType.BUSY_TENTATIVE;
        }
        if (EventStatus.CANCELLED.equals(event.getStatus())) {
            return FbType.FREE;
        }
        return FbType.BUSY;
    }

    /**
     * Normalizes a list of free/busy times to the boundaries of a given period, i.e. removes free/busy times outside range and adjusts
     * the start-/end-times of periods overlapping the start- or enddate of the period.
     *
     * @param freeBusyTimes The free/busy times to normalize
     * @param from The lower inclusive limit of the range
     * @param until The upper exclusive limit of the range
     * @return The normalized free/busy times
     */
    static List<FreeBusyTime> adjustToBoundaries(List<FreeBusyTime> freeBusyTimes, Date from, Date until) {
        for (Iterator<FreeBusyTime> iterator = freeBusyTimes.iterator(); iterator.hasNext();) {
            FreeBusyTime freeBusyTime = iterator.next();
            if (freeBusyTime.getEndTime().after(from) && freeBusyTime.getStartTime().before(until)) {
                if (freeBusyTime.getStartTime().before(from)) {
                    freeBusyTime.setStartTime(from);
                }
                if (freeBusyTime.getEndTime().after(until)) {
                    freeBusyTime.setEndTime(until);
                }
            } else {
                iterator.remove(); // outside range
            }
        }
        return freeBusyTimes;
    }

    /**
     * Gets a list of free/busy times for the supplied events.
     *
     * @param events The events to get the free/busy times for
     * @param timeZone The timezone to consider if the event has <i>floating</i> dates
     * @return The free/busy times
     */
    static List<FreeBusyTime> getFreeBusyTimes(List<Event> events, TimeZone timeZone) {
        List<FreeBusyTime> freeBusyTimes = new ArrayList<FreeBusyTime>(events.size());
        for (Event event : events) {
            long start = event.getStartDate().getTimestamp();
            long end = event.getEndDate().getTimestamp();
            if (CalendarUtils.isFloating(event)) {
                start = CalendarUtils.getDateInTimeZone(event.getStartDate(), timeZone);
                end = CalendarUtils.getDateInTimeZone(event.getEndDate(), timeZone);
            }
            freeBusyTimes.add(new FreeBusyTime(getFbType(event), new Date(start), new Date(end), event));
        }
        return freeBusyTimes;
    }

    /**
     * Reads the attendee data from storage
     *
     * @param events The events to load
     * @param internal whether to only consider internal attendees or not
     * @param storage The {@link CalendarStorage}
     * @return The {@link Event}s containing the attendee data
     * @throws OXException
     */
    protected static List<Event> readAttendeeData(List<Event> events, Boolean internal, CalendarStorage storage) throws OXException {
        if (null != events && 0 < events.size()) {
            Map<String, List<Attendee>> attendeesById = storage.getAttendeeStorage().loadAttendees(getObjectIDs(events), internal);
            for (Event event : events) {
                event.setAttendees(attendeesById.get(event.getId()));
            }
        }
        return events;
    }

}
