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
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.getRelativeId;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.getRelativeIdsPerAccountId;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.getUniqueFolderId;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.withRelativeID;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.withUniqueID;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.withUniqueIDs;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.osgi.Tools.requireService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import org.dmfs.rfc5545.DateTime;
import org.json.JSONObject;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.Check;
import com.openexchange.chronos.common.DefaultEventsResult;
import com.openexchange.chronos.common.FreeBusyUtils;
import com.openexchange.chronos.common.SelfProtectionFactory;
import com.openexchange.chronos.common.SelfProtectionFactory.SelfProtection;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.AccountAwareCalendarFolder;
import com.openexchange.chronos.provider.CalendarAccess;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarCapability;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.CalendarPermission;
import com.openexchange.chronos.provider.CalendarProviderRegistry;
import com.openexchange.chronos.provider.CalendarProviders;
import com.openexchange.chronos.provider.DefaultCalendarFolder;
import com.openexchange.chronos.provider.DefaultCalendarPermission;
import com.openexchange.chronos.provider.FreeBusyProvider;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.basic.BasicCalendarAccess;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.provider.composition.impl.idmangling.IDManglingCalendarResult;
import com.openexchange.chronos.provider.composition.impl.idmangling.IDManglingImportResult;
import com.openexchange.chronos.provider.composition.impl.idmangling.IDManglingUpdatesResult;
import com.openexchange.chronos.provider.extensions.BasicSearchAware;
import com.openexchange.chronos.provider.extensions.BasicSyncAware;
import com.openexchange.chronos.provider.extensions.FolderSearchAware;
import com.openexchange.chronos.provider.extensions.FolderSyncAware;
import com.openexchange.chronos.provider.extensions.PersonalAlarmAware;
import com.openexchange.chronos.provider.extensions.SearchAware;
import com.openexchange.chronos.provider.extensions.SyncAware;
import com.openexchange.chronos.provider.folder.FolderCalendarAccess;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarAccess;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarFolder;
import com.openexchange.chronos.provider.groupware.GroupwareFolderType;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.chronos.service.FreeBusyResult;
import com.openexchange.chronos.service.ImportResult;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.service.SortOrder;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link CompositingIDBasedCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
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

    @Override
    public Event getEvent(EventID eventID) throws OXException {
        CalendarAccount account = getAccount(getAccountId(eventID.getFolderID()), true);
        try {
            EventID relativeEventID = getRelativeId(eventID);
            CalendarAccess access = getAccess(account.getAccountId());
            Event event;
            if (FolderCalendarAccess.class.isInstance(access)) {
                event = ((FolderCalendarAccess) access).getEvent(
                    relativeEventID.getFolderID(), relativeEventID.getObjectID(), relativeEventID.getRecurrenceID());
            } else if (BasicCalendarAccess.class.isInstance(access)) {
                Check.parentFolderMatches(relativeEventID, BasicCalendarAccess.FOLDER_ID);
                event = ((BasicCalendarAccess) access).getEvent(
                    relativeEventID.getObjectID(), relativeEventID.getRecurrenceID());
            } else {
                throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
            }
            return withUniqueID(event, account.getAccountId());
        } catch (OXException e) {
            throw withUniqueIDs(e, account.getAccountId());
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
            CalendarAccount account = getAccount(i(entry.getKey()), true);
            try {
                CalendarAccess access = getAccess(account.getAccountId());
                if (FolderCalendarAccess.class.isInstance(access)) {
                    eventsPerAccountId.put(I(account.getAccountId()), ((FolderCalendarAccess) access).getEvents(entry.getValue()));
                } else if (BasicCalendarAccess.class.isInstance(access)) {
                    Check.parentFolderMatches(entry.getValue(), BasicCalendarAccess.FOLDER_ID);
                    eventsPerAccountId.put(I(account.getAccountId()), ((BasicCalendarAccess) access).getEvents(entry.getValue()));
                } else {
                    throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
                }
            } catch (OXException e) {
                throw withUniqueIDs(e, account.getAccountId());
            }
        }
        /*
         * order resulting events as requested
         */
        List<Event> events = new ArrayList<Event>(eventIDs.size());
        for (EventID requestedID : eventIDs) {
            Integer accountId = I(getAccountId(requestedID.getFolderID()));
            Event event = find(eventsPerAccountId.get(accountId), getRelativeId(requestedID));
            events.add(null != event ? withUniqueID(event, accountId) : null);
        }
        return events;
    }

    @Override
    public List<Event> getChangeExceptions(String folderId, String seriesId) throws OXException {
        CalendarAccount account = getAccount(getAccountId(folderId), true);
        try {
            CalendarAccess access = getAccess(account.getAccountId());
            if (FolderCalendarAccess.class.isInstance(access)) {
                List<Event> changeExceptions = ((FolderCalendarAccess) access).getChangeExceptions(getRelativeFolderId(folderId), seriesId);
                return withUniqueIDs(changeExceptions, account.getAccountId());
            }
            if (BasicCalendarAccess.class.isInstance(access)) {
                Check.folderMatches(getRelativeFolderId(folderId), BasicCalendarAccess.FOLDER_ID);
                List<Event> changeExceptions = ((BasicCalendarAccess) access).getChangeExceptions(seriesId);
                return withUniqueIDs(changeExceptions, account.getAccountId());
            }
            throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
        } catch (OXException e) {
            throw withUniqueIDs(e, account.getAccountId());
        }
    }

    @Override
    public Map<String, EventsResult> getEventsInFolders(List<String> folderIds) throws OXException {
        Map<String, EventsResult> eventsResults = new HashMap<String, EventsResult>(folderIds.size());
        /*
         * get folder identifiers per account & track possible errors
         */
        Map<String, OXException> errorsPerFolderId = new HashMap<String, OXException>();
        Map<CalendarAccount, List<String>> relativeFolderIdsPerAccount = getRelativeFolderIdsPerAccount(folderIds, errorsPerFolderId);
        for (Entry<String, OXException> entry : errorsPerFolderId.entrySet()) {
            eventsResults.put(entry.getKey(), new DefaultEventsResult(entry.getValue()));
        }
        /*
         * get events results per folder
         */
        if (1 == relativeFolderIdsPerAccount.size()) {
            Entry<CalendarAccount, List<String>> entry = relativeFolderIdsPerAccount.entrySet().iterator().next();
            eventsResults.putAll(getEventsInFolders(entry.getKey(), entry.getValue()));
        } else {
            CompletionService<Map<String, EventsResult>> completionService = getCompletionService();
            for (Entry<CalendarAccount, List<String>> entry : relativeFolderIdsPerAccount.entrySet()) {
                completionService.submit(new Callable<Map<String, EventsResult>>() {

                    @Override
                    public Map<String, EventsResult> call() throws Exception {
                        return getEventsInFolders(entry.getKey(), entry.getValue());
                    }
                });
            }
            eventsResults.putAll(collectEventsResults(completionService, relativeFolderIdsPerAccount.size()));
        }
        if (1 < eventsResults.size()) {
            /*
             * return a sorted list of results per folder (same order as requested)
             */
            LinkedHashMap<String, EventsResult> sortedResults = new LinkedHashMap<String, EventsResult>(eventsResults.size());
            for (String folderId : folderIds) {
                sortedResults.put(folderId, eventsResults.get(folderId));
            }
            eventsResults = sortedResults;
        }
        return eventsResults;
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
    public List<Event> getEventsOfUser(Boolean rsvp, ParticipationStatus[] partStats) throws OXException {
        try {
            return withUniqueIDs(getGroupwareAccess(DEFAULT_ACCOUNT).getEventsOfUser(rsvp, partStats), DEFAULT_ACCOUNT.getAccountId());
        } catch (OXException e) {
            throw withUniqueIDs(e, DEFAULT_ACCOUNT.getAccountId());
        }
    }

    @Override
    public Event resolveEvent(String eventId) throws OXException {
        try {
            Event event = getGroupwareAccess(DEFAULT_ACCOUNT).resolveEvent(eventId);
            return null == event ? null : withUniqueID(event, DEFAULT_ACCOUNT.getAccountId());
        } catch (OXException e) {
            throw withUniqueIDs(e, DEFAULT_ACCOUNT.getAccountId());
        }
    }

    @Override
    public List<Event> searchEvents(List<String> folderIds, List<SearchFilter> filters, List<String> queries) throws OXException {
        if (null == folderIds) {
            return searchEvents(filters, queries);
        }
        Map<CalendarAccount, List<String>> foldersPerAccount = getRelativeFolderIdsPerAccount(folderIds);
        if (foldersPerAccount.isEmpty()) {
            return Collections.emptyList();
        }
        if (1 == foldersPerAccount.size()) {
            Entry<CalendarAccount, List<String>> entry = foldersPerAccount.entrySet().iterator().next();
            return searchEvents(entry.getKey(), entry.getValue(), filters, queries);
        }
        CompletionService<List<Event>> completionService = getCompletionService();
        for (Map.Entry<CalendarAccount, List<String>> entry : foldersPerAccount.entrySet()) {
            completionService.submit(new Callable<List<Event>>() {

                @Override
                public List<Event> call() throws Exception {
                    return searchEvents(entry.getKey(), entry.getValue(), filters, queries);
                }
            });
        }
        return sort(collectEventListResults(completionService, foldersPerAccount.size()));
    }

    public List<Event> searchEvents(List<SearchFilter> filters, List<String> queries) throws OXException {
        List<CalendarAccount> accounts = getAccounts(CalendarCapability.SEARCH);
        if (accounts.isEmpty()) {
            return Collections.emptyList();
        }
        if (1 == accounts.size()) {
            return searchEvents(accounts.get(0), null, filters, queries);
        }
        CompletionService<List<Event>> completionService = getCompletionService();
        for (CalendarAccount account : accounts) {
            completionService.submit(new Callable<List<Event>>() {

                @Override
                public List<Event> call() throws Exception {
                    return searchEvents(account, null, filters, queries);
                }
            });
        }
        return sort(collectEventListResults(completionService, accounts.size()));
    }

    @Override
    public UpdatesResult getUpdatedEventsInFolder(String folderId, long updatedSince) throws OXException {
        CalendarAccount account = getAccount(getAccountId(folderId), true);
        try {
            CalendarAccess access = getAccess(account.getAccountId(), SyncAware.class);
            if (FolderSyncAware.class.isInstance(access)) {
                UpdatesResult updatesResult = ((FolderSyncAware) access).getUpdatedEventsInFolder(getRelativeFolderId(folderId), updatedSince);
                return new IDManglingUpdatesResult(updatesResult, account.getAccountId());
            } else if (BasicSyncAware.class.isInstance(access)) {
                Check.folderMatches(getRelativeFolderId(folderId), BasicCalendarAccess.FOLDER_ID);
                UpdatesResult updatesResult = ((BasicSyncAware) access).getUpdatedEvents(updatedSince);
                return new IDManglingUpdatesResult(updatesResult, account.getAccountId());
            } else {
                throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
            }
        } catch (OXException e) {
            throw withUniqueIDs(e, account.getAccountId());
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
    public List<Event> resolveResource(String folderId, String resourceName) throws OXException {
        CalendarAccount account = getAccount(getAccountId(folderId), true);
        try {
            CalendarAccess access = getAccess(account.getAccountId(), SyncAware.class);
            if (FolderSyncAware.class.isInstance(access)) {
                List<Event> events = ((FolderSyncAware) access).resolveResource(getRelativeFolderId(folderId), resourceName);
                return withUniqueIDs(events, account.getAccountId());
            } else if (BasicSyncAware.class.isInstance(access)) {
                Check.folderMatches(getRelativeFolderId(folderId), BasicCalendarAccess.FOLDER_ID);
                List<Event> events = ((BasicSyncAware) access).resolveResource(resourceName);
                return withUniqueIDs(events, account.getAccountId());
            } else {
                throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
            }
        } catch (OXException e) {
            throw withUniqueIDs(e, account.getAccountId());
        }
    }

    @Override
    public Map<String, Long> getSequenceNumbers(List<String> folderIds) throws OXException {
        Map<CalendarAccount, List<String>> foldersPerAccount = getRelativeFolderIdsPerAccount(folderIds);
        if (foldersPerAccount.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Long> sequenceNumbers = new HashMap<String, Long>(folderIds.size());
        for (Map.Entry<CalendarAccount, List<String>> entry : foldersPerAccount.entrySet()) {
            CalendarAccount account = entry.getKey();
            requireCapability(account.getProviderId());
            try {
                CalendarAccess access = getAccess(account.getAccountId(), SyncAware.class);
                if (FolderSyncAware.class.isInstance(access)) {
                    for (String folderId : entry.getValue()) {
                        long sequenceNumber = ((FolderSyncAware) access).getSequenceNumber(folderId);
                        sequenceNumbers.put(getUniqueFolderId(account.getAccountId(), folderId), L(sequenceNumber));
                    }
                } else if (BasicSyncAware.class.isInstance(access)) {
                    for (String folderId : entry.getValue()) {
                        Check.folderMatches(folderId, BasicCalendarAccess.FOLDER_ID);
                        long sequenceNumber = ((BasicSyncAware) access).getSequenceNumber();
                        sequenceNumbers.put(getUniqueFolderId(account.getAccountId(), folderId), L(sequenceNumber));
                    }
                } else {
                    throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
                }
            } catch (OXException e) {
                throw withUniqueIDs(e, account.getAccountId());
            }
        }
        return sequenceNumbers;
    }

    @Override
    public CalendarFolder getDefaultFolder() throws OXException {
        try {
            GroupwareCalendarFolder defaultFolder = getGroupwareAccess(DEFAULT_ACCOUNT).getDefaultFolder();
            return withUniqueID(defaultFolder, DEFAULT_ACCOUNT);
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
                    folders.addAll(withUniqueID(groupwareFolders, account));
                } else if (GroupwareFolderType.PRIVATE.equals(type)) {
                    if (FolderCalendarAccess.class.isInstance(access)) {
                        folders.addAll(withUniqueID(((FolderCalendarAccess) access).getVisibleFolders(), account));
                    } else if (BasicCalendarAccess.class.isInstance(access)) {
                        CalendarFolder folder;
                        if (false == hasCapability(account.getProviderId())) {
                            OXException accountError = CalendarExceptionCodes.MISSING_CAPABILITY.create(CalendarProviders.getCapabilityName(account.getProviderId()));
                            warnings.add(accountError);
                            folder = getBasicCalendarFolder((BasicCalendarAccess) access, isAutoProvisioned(account), accountError);
                        } else {
                            folder = getBasicCalendarFolder((BasicCalendarAccess) access, isAutoProvisioned(account));
                        }
                        folders.add(withUniqueID(folder, account));
                    }
                }
            } catch (OXException e) {
                // TODO: persist exception in account data
                if (false == CalendarExceptionCodes.MISSING_CAPABILITY.equals(e)) {
                    e = withUniqueIDs(e, account.getAccountId());
                    LOG.warn("Error getting visible folders from calendar account {}: {}", I(account.getAccountId()), e.getMessage(), e);
                }
                continue;
            }
        }
        return folders;
    }

    @Override
    public AccountAwareCalendarFolder getFolder(String folderId) throws OXException {
        CalendarAccount account = getAccount(getAccountId(folderId), false);
        try {
            CalendarAccess access = getAccess(account.getAccountId());
            if (FolderCalendarAccess.class.isInstance(access)) {
                CalendarFolder folder = ((FolderCalendarAccess) access).getFolder(getRelativeFolderId(folderId));
                return withUniqueID(folder, account);
            }
            if (BasicCalendarAccess.class.isInstance(access)) {
                Check.folderMatches(getRelativeFolderId(folderId), BasicCalendarAccess.FOLDER_ID);
                CalendarFolder folder;
                if (false == hasCapability(account.getProviderId())) {
                    OXException accountError = CalendarExceptionCodes.MISSING_CAPABILITY.create(CalendarProviders.getCapabilityName(account.getProviderId()));
                    folder = getBasicCalendarFolder((BasicCalendarAccess) access, isAutoProvisioned(account), accountError);
                } else {
                    folder = getBasicCalendarFolder((BasicCalendarAccess) access, isAutoProvisioned(account));
                }
                return withUniqueID(folder, account);
            }
            throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
        } catch (OXException e) {
            throw withUniqueIDs(e, account.getAccountId());
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
        CalendarAccount account = getAccount(getAccountId(eventID.getFolderID()), true);
        try {
            PersonalAlarmAware calendarAccess = getAccess(account.getAccountId(), PersonalAlarmAware.class);
            CalendarResult result = calendarAccess.updateAlarms(getRelativeId(eventID), alarms, clientTimestamp);
            return new IDManglingCalendarResult(result, account.getAccountId());
        } catch (OXException e) {
            throw withUniqueIDs(e, account.getAccountId());
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
    public CalendarResult splitSeries(EventID eventID, DateTime splitPoint, String uid, long clientTimestamp) throws OXException {
        int accountId = getAccountId(eventID.getFolderID());
        try {
            GroupwareCalendarAccess calendarAccess = getGroupwareAccess(accountId);
            CalendarResult result = calendarAccess.splitSeries(getRelativeId(eventID), splitPoint, uid, clientTimestamp);
            return new IDManglingCalendarResult(result, accountId);
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public List<ImportResult> importEvents(String folderId, List<Event> events) throws OXException {
        int accountId = getAccountId(folderId);
        try {
            GroupwareCalendarAccess calendarAccess = getGroupwareAccess(accountId);
            List<ImportResult> results = calendarAccess.importEvents(getRelativeFolderId(folderId), events);
            if (null == results) {
                return null;
            }
            List<ImportResult> importResultsWithUniqueId = new ArrayList<ImportResult>(results.size());
            for (ImportResult result : results) {
                importResultsWithUniqueId.add(new IDManglingImportResult(result, accountId));
            }
            return importResultsWithUniqueId;
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public List<AlarmTrigger> getAlarmTriggers(Set<String> actions) throws OXException {
        List<AlarmTrigger> result = new ArrayList<AlarmTrigger>();
        for (CalendarAccount account : getAccounts(CalendarCapability.ALARMS)) {
            List<AlarmTrigger> alarmTriggers = getAccess(account, PersonalAlarmAware.class).getAlarmTriggers(actions);
            for (AlarmTrigger trigger : alarmTriggers) {
                trigger.setFolder(getUniqueFolderId(account.getAccountId(), trigger.getFolder()));
            }
            result.addAll(alarmTriggers);
        }
        if (result.size() > 1) {
            Collections.sort(result);
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
    public Map<Attendee, FreeBusyResult> queryFreeBusy(List<Attendee> attendees, Date from, Date until, boolean merge) throws OXException {
        List<FreeBusyProvider> freeBusyProviders = getFreeBusyProviders();
        if (freeBusyProviders.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Attendee, List<FreeBusyResult>> results = new HashMap<Attendee, List<FreeBusyResult>>();
        for (FreeBusyProvider freeBusyProvider : getFreeBusyProviders()) {
            Map<Attendee, Map<Integer, FreeBusyResult>> resultsForProvider = freeBusyProvider.query(session, attendees, from, until, merge, this);
            if (null != resultsForProvider && 0 < resultsForProvider.size()) {
                for (Entry<Attendee, Map<Integer, FreeBusyResult>> resultsForAttendee : resultsForProvider.entrySet()) {
                    for (Entry<Integer, FreeBusyResult> resultsForAccount : resultsForAttendee.getValue().entrySet()) {
                        FreeBusyResult result = withUniqueID(resultsForAccount.getValue(), i(resultsForAccount.getKey()));
                        com.openexchange.tools.arrays.Collections.put(results, resultsForAttendee.getKey(), result);
                    }
                }
            }
        }
        Map<Attendee, FreeBusyResult> combinedResults = new HashMap<Attendee, FreeBusyResult>(results.size());
        for (Entry<Attendee, List<FreeBusyResult>> entry : results.entrySet()) {
            combinedResults.put(entry.getKey(), merge ? FreeBusyUtils.merge(entry.getValue()) : FreeBusyUtils.combine(entry.getValue()));
        }
        return combinedResults;
    }

    @Override
    public String createFolder(String providerId, CalendarFolder folder, JSONObject userConfig) throws OXException {
        /*
         * create folder within matching folder-aware account targeted by parent folder if set
         */
        String parentFolderId = GroupwareCalendarFolder.class.isInstance(folder) ? ((GroupwareCalendarFolder) folder).getParentId() : null;
        if (Strings.isNotEmpty(parentFolderId)) {
            int accountId = getAccountId(parentFolderId);
            CalendarAccount existingAccount = optAccount(accountId);
            if (null != existingAccount && (null == providerId || providerId.equals(existingAccount.getProviderId()))) {
                try {
                    String folderId = getAccess(accountId, FolderCalendarAccess.class).createFolder(withRelativeID(folder));
                    return getUniqueFolderId(existingAccount.getAccountId(), folderId);
                } catch (OXException e) {
                    throw withUniqueIDs(e, existingAccount.getAccountId());
                }
            }
        }
        /*
         * dynamically create new account for provider, otherwise
         */
        if (null == providerId) {
            throw CalendarExceptionCodes.MANDATORY_FIELD.create("provider");
        }
        CalendarSettings settings = getBasicCalendarSettings(folder, userConfig);
        CalendarAccount newAccount = requireService(CalendarAccountService.class, services).createAccount(session, providerId, settings, this);
        return getUniqueFolderId(newAccount.getAccountId(), BasicCalendarAccess.FOLDER_ID);
    }

    @Override
    public String updateFolder(String folderId, CalendarFolder folder, JSONObject userConfig, long clientTimestamp) throws OXException {
        int accountId = getAccountId(folderId);
        try {
            CalendarAccess calendarAccess = getAccess(accountId);
            if (FolderCalendarAccess.class.isInstance(calendarAccess)) {
                /*
                 * update folder directly within folder-aware account
                 */
                String updatedId = ((FolderCalendarAccess) calendarAccess).updateFolder(getRelativeFolderId(folderId), withRelativeID(folder), clientTimestamp);
                return getUniqueFolderId(accountId, updatedId);
            }
            /*
             * update account settings
             */
            Check.folderMatches(getRelativeFolderId(folderId), BasicCalendarAccess.FOLDER_ID);
            CalendarSettings settings = getBasicCalendarSettings(folder, userConfig);
            CalendarAccount updatedAccount = requireService(CalendarAccountService.class, services).updateAccount(session, accountId, settings, clientTimestamp, this);
            return getUniqueFolderId(updatedAccount.getAccountId(), BasicCalendarAccess.FOLDER_ID);
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public void deleteFolder(String folderId, long clientTimestamp) throws OXException {
        int accountId = getAccountId(folderId);
        try {
            CalendarAccess calendarAccess = getAccess(accountId);
            if (FolderCalendarAccess.class.isInstance(calendarAccess)) {
                /*
                 * delete folder in calendar account
                 */
                ((FolderCalendarAccess) calendarAccess).deleteFolder(getRelativeFolderId(folderId), clientTimestamp);
            } else {
                /*
                 * delete whole calendar account if not folder-aware
                 */
                Check.folderMatches(getRelativeFolderId(folderId), BasicCalendarAccess.FOLDER_ID);
                requireService(CalendarAccountService.class, services).deleteAccount(session, accountId, clientTimestamp, this);
            }
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    /**
     * Gets all events in a list of folders from a specific calendar account. Potential errors are placed in the results implicitly.
     *
     * @param account The calendar account
     * @param folderIds The relative identifiers of the folders to get the events from
     * @return The events results per folder, already adjusted to contain unique composite identifiers
     */
    private Map<String, EventsResult> getEventsInFolders(CalendarAccount account, List<String> folderIds) throws OXException {
        Map<String, EventsResult> eventsPerFolderId = new HashMap<String, EventsResult>(folderIds.size());
        try {
            requireCapability(account.getProviderId());
            CalendarAccess access = getAccess(account);
            if (FolderCalendarAccess.class.isInstance(access)) {
                eventsPerFolderId.putAll(((FolderCalendarAccess) access).getEventsInFolders(folderIds));
            } else if (BasicCalendarAccess.class.isInstance(access)) {
                for (String folderId : folderIds) {
                    try {
                        Check.folderMatches(folderId, BasicCalendarAccess.FOLDER_ID);
                        eventsPerFolderId.put(folderId, new DefaultEventsResult(((BasicCalendarAccess) access).getEvents()));
                    } catch (OXException e) {
                        eventsPerFolderId.put(folderId, new DefaultEventsResult(e));
                    }
                }
            } else {
                throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
            }
        } catch (OXException e) {
            for (String folderId : folderIds) {
                eventsPerFolderId.put(folderId, new DefaultEventsResult(e));
            }
        }
        return withUniqueIDs(eventsPerFolderId, account.getAccountId());
    }

    /**
     * Searches for events within a specific calendar account.
     *
     * @param account The calendar account
     * @param folderIds The relative identifiers of the folders to perform the search in, or <code>null</code> to search across all visible folders
     * @param filters A list of additional filters to be applied on the search, or <code>null</code> if not specified
     * @param queries The queries to search for, or <code>null</code> if not specified
     * @return The found events, already adjusted to contain unique composite identifiers, or an empty list if there are none
     */
    private List<Event> searchEvents(CalendarAccount account, List<String> folderIds, List<SearchFilter> filters, List<String> queries) throws OXException {
        try {
            requireCapability(account.getProviderId());
            List<Event> eventsInAccount;
            CalendarAccess access = getAccess(account, SearchAware.class);
            if (FolderSearchAware.class.isInstance(access)) {
                if (null != folderIds) {
                    eventsInAccount = ((FolderSearchAware) access).searchEvents(folderIds.toArray(new String[folderIds.size()]), filters, queries);
                } else {
                    eventsInAccount = ((FolderSearchAware) access).searchEvents(null, filters, queries);
                }
            } else if (BasicSearchAware.class.isInstance(access)) {
                if (null != folderIds) {
                    for (String folderId : folderIds) {
                        Check.folderMatches(folderId, BasicCalendarAccess.FOLDER_ID);
                    }
                }
                eventsInAccount = ((BasicSearchAware) access).searchEvents(filters, queries);
            } else {
                throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
            }
            getSelfProtection().checkEventCollection(eventsInAccount);
            return withUniqueIDs(eventsInAccount, account.getAccountId());
        } catch (OXException e) {
            throw withUniqueIDs(e, account.getAccountId());
        }
    }

    /**
     * Takes a specific number of event list results from the completion service, and adds them to a single resulting, sorted list of
     * events.
     *
     * @param completionService The completion service to take the results from
     * @param count The number of results to collect
     * @return The resulting list of events
     */
    private Map<String, EventsResult> collectEventsResults(CompletionService<Map<String, EventsResult>> completionService, int count) throws OXException {
        Map<String, EventsResult> results = new HashMap<String, EventsResult>();
        for (int i = 0; i < count; i++) {
            try {
                results.putAll(completionService.take().get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (null != cause && OXException.class.isInstance(e.getCause())) {
                    throw (OXException) cause;
                }
                throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
            getSelfProtection().checkResultMap(results);
        }
        return results;
    }

    /**
     * Takes a specific number of event list results from the completion service, and adds them to a single resulting, sorted list of
     * events.
     *
     * @param completionService The completion service to take the results from
     * @param count The number of results to collect
     * @return The resulting list of events
     */
    private List<Event> collectEventListResults(CompletionService<List<Event>> completionService, int count) throws OXException {
        List<Event> events = new ArrayList<Event>();
        for (int i = 0; i < count; i++) {
            try {
                events.addAll(completionService.take().get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (null != cause && OXException.class.isInstance(e.getCause())) {
                    throw (OXException) cause;
                }
                throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
            getSelfProtection().checkEventCollection(events);
        }
        return events;
    }

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

    private SelfProtection getSelfProtection() throws OXException {
        if (protection == null) {
            LeanConfigurationService leanConfigurationService = services.getService(LeanConfigurationService.class);
            protection = SelfProtectionFactory.createSelfProtection(getSession(), leanConfigurationService);
        }
        return protection;
    }

    private static Event find(List<Event> events, EventID eventID) {
        return find(events, eventID.getFolderID(), eventID.getObjectID(), eventID.getRecurrenceID());
    }

    private static Event find(List<Event> events, String folderId, String eventId, RecurrenceId recurrenceId) {
        if (null != events) {
            for (Event event : events) {
                if (null != event && folderId.equals(event.getFolderId()) && eventId.equals(event.getId())) {
                    if (null == recurrenceId || recurrenceId.equals(event.getRecurrenceId())) {
                        return event;
                    }
                }
            }
        }
        return null;
    }

    private CalendarFolder getBasicCalendarFolder(BasicCalendarAccess calendarAccess, boolean autoProvisioned) {
        return getBasicCalendarFolder(calendarAccess, autoProvisioned, null);
    }

    private CalendarFolder getBasicCalendarFolder(BasicCalendarAccess calendarAccess, boolean autoProvisioned, OXException accountError) {
        DefaultCalendarFolder folder = new DefaultCalendarFolder();
        folder.setId(BasicCalendarAccess.FOLDER_ID);
        CalendarSettings settings = calendarAccess.getSettings();
        folder.setAccountError(settings.getError());
        folder.setExtendedProperties(settings.getExtendedProperties());
        folder.setName(settings.getName());
        folder.setLastModified(settings.getLastModified());
        folder.setSubscribed(settings.isSubscribed());
        folder.setPermissions(Collections.singletonList(new DefaultCalendarPermission(session.getUserId(),
            CalendarPermission.READ_FOLDER, CalendarPermission.READ_ALL_OBJECTS, CalendarPermission.NO_PERMISSIONS,
            CalendarPermission.NO_PERMISSIONS, false == autoProvisioned, false, 0)));
        folder.setSupportedCapabilites(CalendarCapability.getCapabilities(calendarAccess.getClass()));
        if (null != accountError) {
            folder.setAccountError(accountError); // prefer passed account error if assigned
        }
        return folder;
    }

    private CalendarSettings getBasicCalendarSettings(CalendarFolder calendarFolder, JSONObject userConfig) {
        CalendarSettings settings = new CalendarSettings();
        if (null != calendarFolder.getExtendedProperties()) {
            settings.setExtendedProperties(calendarFolder.getExtendedProperties());
        }
        if (null != calendarFolder.getAccountError()) {
            settings.setError(calendarFolder.getAccountError());
        }
        if (null != calendarFolder.getName()) {
            settings.setName(calendarFolder.getName());
        }
        if (null != calendarFolder.getLastModified()) {
            settings.setLastModified(calendarFolder.getLastModified());
        }
        if (null != userConfig) {
            settings.setConfig(userConfig);
        }
        settings.setSubscribed(calendarFolder.isSubscribed());
        return settings;
    }

}
