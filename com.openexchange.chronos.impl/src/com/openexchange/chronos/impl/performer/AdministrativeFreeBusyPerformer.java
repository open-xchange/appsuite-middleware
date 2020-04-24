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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.filter;
import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.getFields;
import static com.openexchange.chronos.common.CalendarUtils.isGroupScheduled;
import static com.openexchange.chronos.common.CalendarUtils.isInternal;
import static com.openexchange.chronos.common.CalendarUtils.isPublicClassification;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.matches;
import static com.openexchange.chronos.impl.Utils.anonymize;
import static com.openexchange.tools.arrays.Collections.put;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import org.dmfs.rfc5545.Duration;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.FreeBusyTime;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.common.FreeBusyUtils;
import com.openexchange.chronos.common.SelfProtectionFactory;
import com.openexchange.chronos.common.SelfProtectionFactory.SelfProtection;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.service.FreeBusyResult;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.util.TimeZones;

/**
 * {@link AdministrativeFreeBusyPerformer} - similar to the {@link FreeBusyPerformer} but requires no session
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class AdministrativeFreeBusyPerformer {

    private final CalendarStorage storage;

    private final EntityResolver resolver;

    private SelfProtection selfProtection;

    private final Optional<CalendarParameters> params;

    /**
     * Initializes a new {@link AdministrativeFreeBusyPerformer}.
     *
     * @param storage The underlying calendar storage
     * @param resolver The entity resolver
     * @param params The optional {@link CalendarParameters}
     */
    public AdministrativeFreeBusyPerformer(CalendarStorage storage, EntityResolver resolver, Optional<CalendarParameters> params) {
        this.storage = storage;
        this.resolver = resolver;
        this.params = params;
    }

    /**
     * Performs the free/busy operation.
     *
     * @param attendees The attendees to get the free/busy data for
     * @param from The start of the requested time range
     * @param until The end of the requested time range
     * @param merge <code>true</code> to merge the resulting free/busy-times, <code>false</code>, otherwise
     * @return The free/busy times for each of the requested attendees, wrapped within a free/busy result structure
     */
    public Map<Attendee, FreeBusyResult> perform(List<Attendee> attendees, Date from, Date until, boolean merge) throws OXException {
        if (null == attendees || attendees.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Attendee, FreeBusyResult> results = new HashMap<Attendee, FreeBusyResult>(attendees.size());
        /*
         * get intersecting events per attendee & derive (merged) free/busy times
         */
        Map<Attendee, List<Event>> eventsPerAttendee = getOverlappingEvents(attendees, from, until);
        Map<Attendee, List<FreeBusyTime>> freeBusyPerAttendee = new HashMap<Attendee, List<FreeBusyTime>>(eventsPerAttendee.size());
        for (Map.Entry<Attendee, List<Event>> entry : eventsPerAttendee.entrySet()) {
            Attendee attendee = entry.getKey();
            List<Event> events = entry.getValue();
            if (null == events || events.isEmpty()) {
                freeBusyPerAttendee.put(attendee, Collections.emptyList());
                continue;
            }
            List<FreeBusyTime> freeBusyTimes = FreeBusyPerformerUtil.adjustToBoundaries(FreeBusyPerformerUtil.getFreeBusyTimes(events, getTimeZone(attendee)), from, until);
            if (merge && 1 < freeBusyTimes.size()) {
                freeBusyTimes = FreeBusyUtils.mergeFreeBusy(freeBusyTimes);
            }
            freeBusyPerAttendee.put(attendee, freeBusyTimes);
        }

        for (Attendee attendee : attendees) {
            List<FreeBusyTime> freeBusyTimes = freeBusyPerAttendee.get(attendee);
            if (null == freeBusyTimes) {
                OXException e = CalendarExceptionCodes.INVALID_CALENDAR_USER.create(attendee.getUri(), Autoboxing.I(attendee.getEntity()), attendee.getCuType());
                results.put(attendee, new FreeBusyResult(null, Collections.singletonList(e)));
            } else {
                results.put(attendee, new FreeBusyResult(freeBusyTimes, null));
            }
        }
        return results;

    }

    /**
     * Gets the timezone to consider for <i>floating</i> dates of a specific attendee.
     * <p/>
     * For <i>internal</i>, individual calendar user attendees, this is the configured timezone of the user; otherwise, the timezone of
     * the {@link CalendarParameters} are used or if not provided the UTC timezone is used
     *
     * @param attendee The attendee to get the timezone to consider for <i>floating</i> dates for
     * @return The timezone
     */
    protected TimeZone getTimeZone(Attendee attendee) throws OXException {
        if (isInternal(attendee) && CalendarUserType.INDIVIDUAL.equals(attendee.getCuType())) {
            return resolver.getTimeZone(attendee.getEntity());
        }
        return getTimeZone();
    }

    /**
     * Gets a list of overlapping events in a certain range for each requested attendee.
     *
     * @param attendees The attendees to query free/busy information for
     * @param from The start date of the period to consider
     * @param until The end date of the period to consider
     * @return The overlapping events, mapped to each attendee
     */
    private Map<Attendee, List<Event>> getOverlappingEvents(List<Attendee> attendees, Date from, Date until) throws OXException {
        /*
         * prepare & filter internal attendees for lookup
         */
        attendees = resolver.prepare(attendees, true);
        attendees = filter(attendees, Boolean.TRUE, CalendarUserType.INDIVIDUAL, CalendarUserType.RESOURCE, CalendarUserType.GROUP);
        if (0 == attendees.size()) {
            return Collections.emptyMap();
        }
        /*
         * search (potentially) overlapping events for the attendees
         */
        Map<Attendee, List<Event>> eventsPerAttendee = new HashMap<Attendee, List<Event>>(attendees.size());
        for (Attendee attendee : attendees) {
            eventsPerAttendee.put(attendee, new ArrayList<Event>());
        }
        SearchOptions searchOptions = params.isPresent() ? new SearchOptions(params.get()).setRange(from, until) : new SearchOptions().setRange(from, until);
        EventField[] fields = getFields(FreeBusyPerformerUtil.FREEBUSY_FIELDS, EventField.ORGANIZER, EventField.DELETE_EXCEPTION_DATES, EventField.CHANGE_EXCEPTION_DATES, EventField.RECURRENCE_ID);
        List<Event> eventsInPeriod = storage.getEventStorage().searchOverlappingEvents(attendees, true, searchOptions, fields);
        if (0 == eventsInPeriod.size()) {
            return eventsPerAttendee;
        }
        FreeBusyPerformerUtil.readAttendeeData(eventsInPeriod, Boolean.TRUE, storage);
        /*
         * step through events & build free/busy per requested attendee
         */
        for (Event eventInPeriod : eventsInPeriod) {
            if (false == considerForFreeBusy(eventInPeriod)) {
                continue; // exclude events classified as 'private' (but keep 'confidential' ones)
            }
            for (Attendee attendee : attendees) {
                String folderID;
                if (isGroupScheduled(eventInPeriod)) {
                    /*
                     * include if attendee does attend
                     */
                    Attendee eventAttendee = find(eventInPeriod.getAttendees(), attendee);
                    if (null == eventAttendee || eventAttendee.isHidden() || ParticipationStatus.DECLINED.equals(eventAttendee.getPartStat())) {
                        continue;
                    }
                    folderID = null;
                } else {
                    /*
                     * include if attendee matches event owner
                     */
                    if (false == matches(eventInPeriod.getCalendarUser(), attendee.getEntity())) {
                        continue;
                    }
                    folderID = eventInPeriod.getFolderId();
                }
                if (isSeriesMaster(eventInPeriod)) {
                    /*
                     * expand & add all (non overridden) instances of event series in period, expanded by the actual event duration
                     */
                    Date iteratorFrom = from;
                    if (null != eventInPeriod.getEndDate()) {
                        Duration duration = CalendarUtils.getDuration(eventInPeriod.getEndDate(), eventInPeriod.getStartDate());
                        iteratorFrom = new Date(duration.addTo(TimeZones.UTC, from.getTime()));
                    }
                    Iterator<RecurrenceId> iterator = Services.getService(RecurrenceService.class, true).iterateRecurrenceIds(new DefaultRecurrenceData(eventInPeriod), iteratorFrom, until);
                    while (iterator.hasNext()) {
                        put(eventsPerAttendee, attendee, FreeBusyPerformerUtil.getResultingOccurrence(getResultingEvent(eventInPeriod, folderID), eventInPeriod, iterator.next()));
                        getSelfProtection().checkEventCollection(eventsPerAttendee.get(attendee));
                    }
                } else {
                    /*
                     * add event in period
                     */
                    put(eventsPerAttendee, attendee, getResultingEvent(eventInPeriod, folderID));
                    getSelfProtection().checkEventCollection(eventsPerAttendee.get(attendee));
                }
                getSelfProtection().checkMap(eventsPerAttendee);
            }
        }
        return eventsPerAttendee;
    }


    /**
     * Gets a value indicating whether a certain event is visible or <i>opaque to</i> free/busy results in the view of the current
     * session's user or not.
     *
     * @param event The event to check
     * @return <code>true</code> if the event should be considered, <code>false</code>, otherwise
     */
    protected boolean considerForFreeBusy(Event event) {

        String maskId = params.isPresent() ? params.get().get(CalendarParameters.PARAMETER_MASK_ID, String.class) : null;
        if (maskId != null && (maskId.equals(event.getId()) || maskId.equals(event.getSeriesId()))){
            return false;
        }

        // exclude foreign events classified as 'private' (but keep 'confidential' ones)
        return isPublicClassification(event) || Classification.CONFIDENTIAL.equals(event.getClassification());
    }

    /**
     * Gets the {@link SelfProtection}
     *
     * @return The {@link SelfProtection}
     */
    protected SelfProtection getSelfProtection() {
        if (selfProtection==null){
            LeanConfigurationService leanConfigurationService = Services.getService(LeanConfigurationService.class);
            selfProtection = SelfProtectionFactory.createSelfProtection(leanConfigurationService);
        }
        return selfProtection;
    }

    /**
     * Performs the merged free/busy operation.
     *
     * @param attendees The attendees to query free/busy information for
     * @param from The start date of the period to consider
     * @param until The end date of the period to consider
     * @return The free/busy result
     */
    public Map<Attendee, List<FreeBusyTime>> performMerged(List<Attendee> attendees, Date from, Date until) throws OXException {
        Map<Attendee, List<Event>> eventsPerAttendee = getOverlappingEvents(attendees, from, until);
        Map<Attendee, List<FreeBusyTime>> freeBusyDataPerAttendee = new HashMap<Attendee, List<FreeBusyTime>>(eventsPerAttendee.size());
        for (Map.Entry<Attendee, List<Event>> entry : eventsPerAttendee.entrySet()) {
            freeBusyDataPerAttendee.put(entry.getKey(), FreeBusyPerformerUtil.mergeFreeBusy(entry.getValue(), from, until, getTimeZone()));
        }
        return freeBusyDataPerAttendee;
    }

    /**
     * Gets the timezone from the {@link CalendarParameters} or return the UTC timezone
     *
     * @return The timezone
     */
    private TimeZone getTimeZone() {
        TimeZone result = params.isPresent() ? params.get().get(CalendarParameters.PARAMETER_TIMEZONE, TimeZone.class) : null;
        return result == null ? TimeZone.getTimeZone("UTC") : result;
    }

    /**
     * Calculates the free/busy time ranges from the user defined availability and the free/busy operation
     *
     * @param attendees The attendees to calculate the free/busy information for
     * @param from The start time of the interval
     * @param until The end time of the interval
     * @return A {@link Map} with a {@link FreeBusyResult} per {@link Attendee}
     */
    public Map<Attendee, FreeBusyResult> performCalculateFreeBusyTime(List<Attendee> attendees, Date from, Date until) throws OXException {
        // Get the free busy data for the attendees
        Map<Attendee, List<FreeBusyTime>> freeBusyPerAttendee = performMerged(attendees, from, until);
        Map<Attendee, FreeBusyResult> results = new HashMap<>();
        for (Map.Entry<Attendee, List<FreeBusyTime>> attendeeEntry : freeBusyPerAttendee.entrySet()) {
            FreeBusyResult result = new FreeBusyResult();
            result.setFreeBusyTimes(attendeeEntry.getValue());
            results.put(attendeeEntry.getKey(), result);
        }
        return results;
    }

    /**
     * Gets a resulting userized event for the free/busy result based on the supplied event data. Only a subset of properties is copied
     * over, and a folder identifier is applied optionally, depending on the user's access permissions for the actual event data.
     *
     * @param event The event data to get the result for
     * @param folderID The folder identifier representing the user's view on the event, or <code>null</code> if not accessible in any folder
     * @return The resulting event representing the free/busy slot
     */
    private Event getResultingEvent(Event event, String folderID) throws OXException {
        if (null != folderID) {
            Event resultingEvent = EventMapper.getInstance().copy(event, new Event(), FreeBusyPerformerUtil.FREEBUSY_FIELDS);
            resultingEvent.setFolderId(folderID);
            return anonymize(resultingEvent, Locale.US);
        }
        return EventMapper.getInstance().copy(event, new Event(), FreeBusyPerformerUtil.RESTRICTED_FREEBUSY_FIELDS);
    }

}
