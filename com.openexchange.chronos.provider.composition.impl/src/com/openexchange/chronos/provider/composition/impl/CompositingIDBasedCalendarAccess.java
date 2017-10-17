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

package com.openexchange.chronos.provider.composition.impl;

import static com.openexchange.chronos.provider.CalendarAccount.DEFAULT_ACCOUNT;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.getAccountId;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.getRelativeFolderId;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.getRelativeFolderIdsPerAccountId;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.getRelativeId;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.getRelativeIdsPerAccountId;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.getUniqueFolderId;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.withRelativeID;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.withUniqueID;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.withUniqueIDs;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.FreeBusyUtils;
import com.openexchange.chronos.common.SelfProtectionFactory;
import com.openexchange.chronos.common.SelfProtectionFactory.SelfProtection;
import com.openexchange.chronos.provider.CalendarAccess;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.CalendarProviderRegistry;
import com.openexchange.chronos.provider.FreeBusyProvider;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.provider.composition.impl.idmangling.IDManglingCalendarResult;
import com.openexchange.chronos.provider.composition.impl.idmangling.IDManglingUpdatesResult;
import com.openexchange.chronos.provider.extensions.PersonalAlarmAware;
import com.openexchange.chronos.provider.extensions.SearchAware;
import com.openexchange.chronos.provider.extensions.SyncAware;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarAccess;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarFolder;
import com.openexchange.chronos.provider.groupware.GroupwareFolderType;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.FreeBusyResult;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.service.SortOrder;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.TimeZones;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link CompositingIDBasedCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public class CompositingIDBasedCalendarAccess extends AbstractCompositingIDBasedCalendarAccess implements IDBasedCalendarAccess {

    private SelfProtection protection = null;

    /**
     * Initializes a new {@link CompositingIDBasedCalendarAccess}.
     *
     * @param session The session to create the ID-based access for
     * @param providerRegistry A reference to the calendar provider registry
     * @param services A service lookup reference
     */
    public CompositingIDBasedCalendarAccess(Session session, CalendarProviderRegistry providerRegistry, ServiceLookup services) throws OXException {
        super(session, providerRegistry, services);
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public List<OXException> getWarnings() {
        return warnings;
    }

    private SelfProtection getSelfProtection() throws OXException {
        if (protection == null) {
            LeanConfigurationService leanConfigurationService = services.getService(LeanConfigurationService.class);
            protection = SelfProtectionFactory.createSelfProtection(getSession(), leanConfigurationService);
        }
        return protection;
    }

    @Override
    public Event getEvent(final EventID eventID) throws OXException {
        int accountId = getAccountId(eventID.getFolderID());
        try {
            EventID relativeEventID = getRelativeId(eventID);
            Event event = getAccess(accountId).getEvent(relativeEventID.getFolderID(), relativeEventID.getObjectID(), relativeEventID.getRecurrenceID());
            return withUniqueID(event, accountId);
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public List<Event> getEvents(List<EventID> eventIDs) throws OXException {
        /*
         * get events from each account
         */
        Map<Integer, List<EventID>> idsPerAccountId = getRelativeIdsPerAccountId(eventIDs);
        Map<Integer, List<Event>> eventsPerAccountId = new HashMap<Integer, List<Event>>(idsPerAccountId.size());
        for (Entry<Integer, List<EventID>> entry : idsPerAccountId.entrySet()) {
            int accountId = entry.getKey().intValue();
            try {
                eventsPerAccountId.put(I(accountId), getAccess(accountId).getEvents(entry.getValue()));
            } catch (OXException e) {
                throw withUniqueIDs(e, accountId);
            }
        }
        /*
         * order resulting events as requested
         */
        List<Event> events = new ArrayList<Event>(eventIDs.size());
        for (EventID requestedID : eventIDs) {
            Integer accountId = I(getAccountId(requestedID.getFolderID()));
            Event event = find(eventsPerAccountId.get(accountId), getRelativeId(requestedID));
            if (null != event) {
                events.add(withUniqueID(event, accountId));
            }
        }
        return events;
    }

    @Override
    public List<Event> getChangeExceptions(String folderId, String seriesId) throws OXException {
        int accountId = getAccountId(folderId);
        try {
            List<Event> changeExceptions = getAccess(accountId).getChangeExceptions(getRelativeFolderId(folderId), seriesId);
            return withUniqueIDs(changeExceptions, accountId);
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public List<Event> getEventsInFolder(String folderId) throws OXException {
        int accountId = getAccountId(folderId);
        try {
            List<Event> eventsInFolder = getAccess(accountId).getEventsInFolder(getRelativeFolderId(folderId));
            return withUniqueIDs(eventsInFolder, accountId);
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public List<Event> getEventsOfUser() throws OXException {
        try {
            return withUniqueIDs(getGroupwareAccess(DEFAULT_ACCOUNT).getEventsOfUser(), DEFAULT_ACCOUNT.getAccountId());
        } catch (OXException e) {
            throw withUniqueIDs(e, DEFAULT_ACCOUNT.getAccountId());
        }
    }

    @Override
    public List<Event> searchEvents(String[] folderIds, List<SearchFilter> filters, List<String> queries) throws OXException {
        if (null == folderIds) {
            return searchEvents(filters, queries);
        }
        List<Event> events = new ArrayList<Event>();
        Map<Integer, List<String>> foldersPerAccountId = getRelativeFolderIdsPerAccountId(folderIds);
        for (Map.Entry<Integer, List<String>> entry : foldersPerAccountId.entrySet()) {
            String[] relativeFolderIds = entry.getValue().toArray(new String[entry.getValue().size()]);
            int accountId = i(entry.getKey());
            try {
                List<Event> eventsInAccount = getAccess(accountId, SearchAware.class).searchEvents(relativeFolderIds, filters, queries);
                events.addAll(withUniqueIDs(eventsInAccount, accountId));
                getSelfProtection().checkEventCollection(events);
            } catch (OXException e) {
                throw withUniqueIDs(e, accountId);
            }
        }
        return 1 < foldersPerAccountId.size() ? sort(events) : events;
    }

    public List<Event> searchEvents(List<SearchFilter> filters, List<String> queries) throws OXException {
        List<Event> events = new ArrayList<Event>();
        for (CalendarAccount account : getAccounts()) {
            CalendarAccess access = getAccess(account);
            if (SearchAware.class.isInstance(access)) {
                try {
                    List<Event> eventsInAccount = getAccess(account, SearchAware.class).searchEvents(null, filters, queries);
                    events.addAll(withUniqueIDs(eventsInAccount, account.getAccountId()));
                    getSelfProtection().checkEventCollection(events);
                } catch (OXException e) {
                    //TODO: persist exception in account data
                    e = withUniqueIDs(e, account.getAccountId());
                    LOG.warn("Error performing search in calendar account {}: {}", I(account.getAccountId()), e.getMessage(), e);
                    continue;
                }
            }
        }
        return sort(events);
    }

    @Override
    public UpdatesResult getUpdatedEventsInFolder(String folderId, long updatedSince) throws OXException {
        int accountId = getAccountId(folderId);
        try {
            UpdatesResult updatesResult = getAccess(accountId, SyncAware.class).getUpdatedEventsInFolder(getRelativeFolderId(folderId), updatedSince);
            return new IDManglingUpdatesResult(updatesResult, accountId);
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public UpdatesResult getUpdatedEventsOfUser(long updatedSince) throws OXException {
        try {
            UpdatesResult updatesResult = getGroupwareAccess(DEFAULT_ACCOUNT).getUpdatedEventsOfUser(updatedSince);
            return new IDManglingUpdatesResult(updatesResult, DEFAULT_ACCOUNT.getAccountId());
        } catch (OXException e) {
            throw withUniqueIDs(e, DEFAULT_ACCOUNT.getAccountId());
        }
    }

    @Override
    public CalendarFolder getDefaultFolder() throws OXException {
        try {
            GroupwareCalendarFolder defaultFolder = getGroupwareAccess(DEFAULT_ACCOUNT).getDefaultFolder();
            return withUniqueID(defaultFolder, DEFAULT_ACCOUNT.getAccountId());
        } catch (OXException e) {
            throw withUniqueIDs(e, DEFAULT_ACCOUNT.getAccountId());
        }
    }

    @Override
    public List<CalendarFolder> getVisibleFolders(GroupwareFolderType type) throws OXException {
        List<CalendarFolder> folders = new ArrayList<CalendarFolder>();
        for (CalendarAccount account : getAccounts()) {
            try {
                CalendarAccess access = getAccess(account);
                if (GroupwareCalendarAccess.class.isInstance(access)) {
                    List<GroupwareCalendarFolder> groupwareFolders = ((GroupwareCalendarAccess) access).getVisibleFolders(type);
                    folders.addAll(withUniqueID(groupwareFolders, account.getAccountId()));
                } else if (GroupwareFolderType.PRIVATE.equals(type)) {
                    folders.addAll(withUniqueID(access.getVisibleFolders(), account.getAccountId()));
                }
            } catch (OXException e) {
                //TODO: persist exception in account data
                e = withUniqueIDs(e, account.getAccountId());
                LOG.warn("Error getting visible folders from calendar account {}: {}", I(account.getAccountId()), e.getMessage(), e);
                continue;
            }
        }
        return folders;
    }

    @Override
    public CalendarFolder getFolder(String folderId) throws OXException {
        int accountId = getAccountId(folderId);
        try {
            CalendarFolder calendarFolder = getAccess(accountId).getFolder(getRelativeFolderId(folderId));
            return withUniqueID(calendarFolder, accountId);
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public CalendarResult createEvent(String folderId, Event event) throws OXException {
        int accountId = getAccountId(folderId);
        try {
            GroupwareCalendarAccess calendarAccess = getGroupwareAccess(accountId);
            CalendarResult result = calendarAccess.createEvent(getRelativeFolderId(folderId), withRelativeID(event));
            return new IDManglingCalendarResult(result, accountId);
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public CalendarResult updateEvent(EventID eventID, Event event, long clientTimestamp) throws OXException {
        int accountId = getAccountId(eventID.getFolderID());
        try {
            GroupwareCalendarAccess calendarAccess = getGroupwareAccess(accountId);
            CalendarResult result = calendarAccess.updateEvent(getRelativeId(eventID), withRelativeID(event), clientTimestamp);
            return new IDManglingCalendarResult(result, accountId);
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public CalendarResult moveEvent(EventID eventID, String targetFolderId, long clientTimestamp) throws OXException {
        int accountId = getAccountId(eventID.getFolderID());
        try {
            GroupwareCalendarAccess calendarAccess = getGroupwareAccess(accountId);
            CalendarResult result = calendarAccess.moveEvent(getRelativeId(eventID), getRelativeFolderId(targetFolderId), clientTimestamp);
            return new IDManglingCalendarResult(result, accountId);
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public CalendarResult updateAttendee(EventID eventID, Attendee attendee, long clientTimestamp) throws OXException {
        int accountId = getAccountId(eventID.getFolderID());
        try {
            GroupwareCalendarAccess calendarAccess = getGroupwareAccess(accountId);
            CalendarResult result = calendarAccess.updateAttendee(getRelativeId(eventID), attendee, clientTimestamp);
            return new IDManglingCalendarResult(result, accountId);
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public CalendarResult updateAlarms(EventID eventID, List<Alarm> alarms, long clientTimestamp) throws OXException {
        int accountId = getAccountId(eventID.getFolderID());
        try {
            PersonalAlarmAware calendarAccess = getAccess(accountId, PersonalAlarmAware.class);
            CalendarResult result = calendarAccess.updateAlarms(getRelativeId(eventID), alarms, clientTimestamp);
            return new IDManglingCalendarResult(result, accountId);
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public CalendarResult deleteEvent(EventID eventID, long clientTimestamp) throws OXException {
        int accountId = getAccountId(eventID.getFolderID());
        try {
            GroupwareCalendarAccess calendarAccess = getGroupwareAccess(accountId);
            CalendarResult result = calendarAccess.deleteEvent(getRelativeId(eventID), clientTimestamp);
            return new IDManglingCalendarResult(result, accountId);
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public void deleteFolder(String folderId, long clientTimestamp) throws OXException {
        int accountId = getAccountId(folderId);
        try {
            GroupwareCalendarAccess calendarAccess = getGroupwareAccess(accountId);
            calendarAccess.deleteFolder(getRelativeFolderId(folderId), clientTimestamp);
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public String updateFolder(String folderId, CalendarFolder folder, long clientTimestamp) throws OXException {
        int accountId = getAccountId(folderId);
        try {
            CalendarAccess calendarAccess = getAccess(accountId);
            folderId = calendarAccess.updateFolder(getRelativeFolderId(folderId), withRelativeID(folder), clientTimestamp);
            return getUniqueFolderId(accountId, folderId);
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public String createFolder(String parentFolderId, CalendarFolder folder) throws OXException {
        int accountId = getAccountId(parentFolderId);
        try {
            GroupwareCalendarAccess calendarAccess = getGroupwareAccess(accountId);
            String folderId = calendarAccess.createFolder(getRelativeFolderId(parentFolderId), withRelativeID(folder));
            return getUniqueFolderId(accountId, folderId);
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public List<AlarmTrigger> getAlarmTrigger(Set<String> actions) throws OXException {
        List<CalendarAccount> accounts = getAccounts();
        List<AlarmTrigger> result = new ArrayList<>();
        for (CalendarAccount account : accounts) {
            GroupwareCalendarAccess calendarAccess = getGroupwareAccess(account.getAccountId());
            result.addAll(calendarAccess.getAlarmTrigger(actions));
        }

        for (AlarmTrigger trigger : result) {
            trigger.setFolder(getUniqueFolderId(trigger.getAccount(), trigger.getFolder()));
        }
        return result;
    }

    @Override
    public IFileHolder getAttachment(EventID eventID, int managedId) throws OXException {
        int accountId = getAccountId(eventID.getFolderID());
        try {
            EventID relativeEventID = getRelativeId(eventID);
            return getGroupwareAccess(accountId).getAttachment(relativeEventID, managedId);
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public Map<Attendee, FreeBusyResult> queryFreeBusy(List<Attendee> attendees, Date from, Date until) throws OXException {
        List<FreeBusyProvider> freeBusyProviders = getFreeBusyProviders();
        if (freeBusyProviders.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Attendee, List<FreeBusyResult>> results = new HashMap<Attendee, List<FreeBusyResult>>();
        for (FreeBusyProvider freeBusyProvider : getFreeBusyProviders()) {
            Map<Attendee, Map<Integer, FreeBusyResult>> resultsForProvider = freeBusyProvider.query(session, attendees, from, until, this);
            if (null != resultsForProvider && 0 < resultsForProvider.size()) {
                for (Entry<Attendee, Map<Integer, FreeBusyResult>> resultsForAttendee : resultsForProvider.entrySet()) {
                    for (Entry<Integer, FreeBusyResult> resultsForAccount : resultsForAttendee.getValue().entrySet()) {
                        FreeBusyResult result = withUniqueID(resultsForAccount.getValue(), i(resultsForAccount.getKey()));
                        com.openexchange.tools.arrays.Collections.put(results, resultsForAttendee.getKey(), result);
                    }
                }
            }
        }
        Map<Attendee, FreeBusyResult> mergedResults = new HashMap<Attendee, FreeBusyResult>(results.size());
        for (Entry<Attendee, List<FreeBusyResult>> entry : results.entrySet()) {
            mergedResults.put(entry.getKey(), FreeBusyUtils.merge(entry.getValue()));
        }
        return mergedResults;
    }

    /////////////////////////////////////////// HELPERS //////////////////////////////////////////////////

    private List<Event> sort(List<Event> events) throws OXException {
        if (null != events && 0 < events.size()) {
            SortOrder[] sortOrders = new SearchOptions(this).getSortOrders();
            if (null != sortOrders && 0 < sortOrders.length) {
                TimeZone timeZone = CalendarUtils.optTimeZone(session.getUser().getTimeZone(), TimeZones.UTC);
                CalendarUtils.sortEvents(events, sortOrders, timeZone);
            }
        }
        return events;
    }

    private static Event find(List<Event> events, EventID eventID) {
        return find(events, eventID.getFolderID(), eventID.getObjectID(), eventID.getRecurrenceID());
    }

    private static Event find(List<Event> events, String folderId, String eventId, RecurrenceId recurrenceId) {
        if (null != events) {
            for (Event event : events) {
                if (folderId.equals(event.getFolderId()) && eventId.equals(event.getId())) {
                    if (null == recurrenceId || recurrenceId.equals(event.getRecurrenceId())) {
                        return event;
                    }
                }
            }
        }
        return null;
    }

}
