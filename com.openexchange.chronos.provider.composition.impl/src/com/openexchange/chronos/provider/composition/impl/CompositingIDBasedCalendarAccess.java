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

import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.getAccountId;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.getRelativeFolderId;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.getRelativeId;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.getUniqueFolderId;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.withRelativeID;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.withUniqueID;
import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.withUniqueIDs;
import static com.openexchange.java.Autoboxing.I;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.FreeBusyTime;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.FreeBusyUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccess;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.CalendarProviderRegistry;
import com.openexchange.chronos.provider.FreeBusyAwareCalendarAccess;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.provider.composition.IDBasedFreeBusyAccess;
import com.openexchange.chronos.provider.composition.impl.idmangling.IDManglingCalendarResult;
import com.openexchange.chronos.provider.composition.impl.idmangling.IDManglingEventConflict;
import com.openexchange.chronos.provider.composition.impl.idmangling.IDManglingUpdatesResult;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarAccess;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarFolder;
import com.openexchange.chronos.provider.groupware.GroupwareFolderType;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link CompositingIDBasedCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CompositingIDBasedCalendarAccess implements IDBasedCalendarAccess, IDBasedFreeBusyAccess {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CompositingIDBasedCalendarAccess.class);

    private final ServiceLookup services;
    private final Session session;
    private final CalendarProviderRegistry providerRegistry;
    private final Map<String, Object> parameters;
    private final ConcurrentMap<Integer, CalendarAccess> connectedAccesses;

    /**
     * Initializes a new {@link CompositingIDBasedCalendarAccess}.
     *
     * @param session The session to create the ID-based access for
     * @param providerRegistry A reference to the calendar provider registry
     * @param services A service lookup reference
     */
    public CompositingIDBasedCalendarAccess(Session session, CalendarProviderRegistry providerRegistry, ServiceLookup services) throws OXException {
        super();
        this.services = services;
        this.providerRegistry = providerRegistry;
        this.session = session;
        this.parameters = new HashMap<String, Object>();
        this.connectedAccesses = new ConcurrentHashMap<Integer, CalendarAccess>();
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public void startTransaction() throws OXException {
        ConcurrentMap<Integer, CalendarAccess> connectedAccesses = this.connectedAccesses;
        if (false == connectedAccesses.isEmpty()) {
            for (CalendarAccess access : connectedAccesses.values()) {
                LOG.warn("Access already connected: {}", access);
            }
        }
        connectedAccesses.clear();
    }

    @Override
    public void finish() throws OXException {
        /*
         * close any connected calendar accesses
         */
        ConcurrentMap<Integer, CalendarAccess> connectedAccesses = this.connectedAccesses;
        for (Iterator<Entry<Integer, CalendarAccess>> iterator = connectedAccesses.entrySet().iterator(); iterator.hasNext();) {
            Entry<Integer, CalendarAccess> entry = iterator.next();
            CalendarAccess access = entry.getValue();
            LOG.debug("Closing calendar access {} for account {}.", access, entry.getKey());
            access.close();
            iterator.remove();
        }
    }

    @Override
    public void commit() throws OXException {
        //
    }

    @Override
    public void rollback() throws OXException {
        //
    }

    @Override
    public void setTransactional(boolean transactional) {
        //
    }

    @Override
    public void setRequestTransactional(boolean transactional) {
        //
    }

    @Override
    public void setCommitsTransaction(boolean commits) {
        //
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
        Map<Integer, List<EventID>> idsPerAccountId = getIDsPerAccountId(eventIDs);
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
        return withUniqueIDs(getGroupwareAccess(CalendarAccount.DEFAULT_ACCOUNT).getEventsOfUser(), CalendarAccount.DEFAULT_ACCOUNT.getAccountId());
        } catch (OXException e) {
            throw withUniqueIDs(e, CalendarAccount.DEFAULT_ACCOUNT.getAccountId());
        }
    }

    @Override
    public UpdatesResult getUpdatedEventsInFolder(String folderId, long updatedSince) throws OXException {
        int accountId = getAccountId(folderId);
        try {
            UpdatesResult updatesResult = getAccess(accountId).getUpdatedEventsInFolder(getRelativeFolderId(folderId), updatedSince);
            return new IDManglingUpdatesResult(updatesResult, accountId);
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public UpdatesResult getUpdatedEventsOfUser(long updatedSince) throws OXException {
        try {
            UpdatesResult updatesResult = getGroupwareAccess(CalendarAccount.DEFAULT_ACCOUNT).getUpdatedEventsOfUser(updatedSince);
            return new IDManglingUpdatesResult(updatesResult, CalendarAccount.DEFAULT_ACCOUNT.getAccountId());
        } catch (OXException e) {
            throw withUniqueIDs(e, CalendarAccount.DEFAULT_ACCOUNT.getAccountId());
        }
    }

    @Override
    public CalendarFolder getDefaultFolder() throws OXException {
        try {
            GroupwareCalendarFolder defaultFolder = getGroupwareAccess(CalendarAccount.DEFAULT_ACCOUNT).getDefaultFolder();
            return withUniqueID(defaultFolder, CalendarAccount.DEFAULT_ACCOUNT.getAccountId());
        } catch (OXException e) {
            throw withUniqueIDs(e, CalendarAccount.DEFAULT_ACCOUNT.getAccountId());
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
                throw withUniqueIDs(e, account.getAccountId());
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
    public CalendarResult updateEvent(EventID eventID, Event event) throws OXException {
        int accountId = getAccountId(eventID.getFolderID());
        try {
            GroupwareCalendarAccess calendarAccess = getGroupwareAccess(accountId);
            CalendarResult result = calendarAccess.updateEvent(getRelativeId(eventID), withRelativeID(event));
            return new IDManglingCalendarResult(result, accountId);
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public CalendarResult moveEvent(EventID eventID, String targetFolderId) throws OXException {
        int accountId = getAccountId(eventID.getFolderID());
        try {
            GroupwareCalendarAccess calendarAccess = getGroupwareAccess(accountId);
            CalendarResult result = calendarAccess.moveEvent(getRelativeId(eventID), getRelativeFolderId(targetFolderId));
            return new IDManglingCalendarResult(result, accountId);
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public CalendarResult updateAttendee(EventID eventID, Attendee attendee) throws OXException {
        int accountId = getAccountId(eventID.getFolderID());
        try {
            GroupwareCalendarAccess calendarAccess = getGroupwareAccess(accountId);
            CalendarResult result = calendarAccess.updateAttendee(getRelativeId(eventID), attendee);
            return new IDManglingCalendarResult(result, accountId);
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public CalendarResult deleteEvent(EventID eventID) throws OXException {
        int accountId = getAccountId(eventID.getFolderID());
        try {
            GroupwareCalendarAccess calendarAccess = getGroupwareAccess(accountId);
            CalendarResult result = calendarAccess.deleteEvent(getRelativeId(eventID));
            return new IDManglingCalendarResult(result, accountId);
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public void deleteFolder(String folderId) throws OXException {
        int accountId = getAccountId(folderId);
        try {
        GroupwareCalendarAccess calendarAccess = getGroupwareAccess(accountId);
        calendarAccess.deleteFolder(getRelativeFolderId(folderId));
        } catch (OXException e) {
            throw withUniqueIDs(e, accountId);
        }
    }

    @Override
    public String updateFolder(String folderId, CalendarFolder folder) throws OXException {
        int accountId = getAccountId(folderId);
        try {
        GroupwareCalendarAccess calendarAccess = getGroupwareAccess(accountId);
        folderId = calendarAccess.updateFolder(getRelativeFolderId(folderId), withRelativeID(folder));
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
    public <T> CalendarParameters set(String parameter, T value) {
        parameters.put(parameter, value);
        return this;
    }

    @Override
    public <T> T get(String parameter, Class<T> clazz) {
        return get(parameter, clazz, null);
    }

    @Override
    public <T> T get(String parameter, Class<T> clazz, T defaultValue) {
        Object value = parameters.get(parameter);
        return null == value ? defaultValue : clazz.cast(value);
    }

    @Override
    public boolean contains(String parameter) {
        return parameters.containsKey(parameter);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return Collections.unmodifiableSet(parameters.entrySet());
    }

    /**
     * Gets the groupware calendar access for a specific account. The account is connected implicitly and remembered to be closed during
     * {@link #finish()} implicitly, if not already done.
     *
     * @param accountId The identifier to get the calendar access for
     * @return The groupware calendar access for the specified account
     */
    private GroupwareCalendarAccess getGroupwareAccess(int accountId) throws OXException {
        CalendarAccess access = getAccess(accountId);
        if (false == GroupwareCalendarAccess.class.isInstance(access)) {
            throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(getAccount(accountId).getProviderId());
        }
        return (GroupwareCalendarAccess) access;
    }

    /**
     * Gets the calendar access for a specific account. The account is connected implicitly and remembered to be closed during
     * {@link #finish()} implicitly, if not already done.
     *
     * @param accountId The identifier to get the calendar access for
     * @return The calendar access for the specified account
     */
    private CalendarAccess getAccess(int accountId) throws OXException {
        CalendarAccess access = connectedAccesses.get(I(accountId));
        return null != access ? access : getAccess(getAccount(accountId));
    }

    /**
     * Gets the groupware calendar access for a specific account. The account is connected implicitly and remembered to be closed during
     * {@link #finish()} implicitly, if not already done.
     *
     * @param account The account to get the calendar access for
     * @return The groupware calendar access for the specified account
     */
    private GroupwareCalendarAccess getGroupwareAccess(CalendarAccount account) throws OXException {
        CalendarAccess access = getAccess(account);
        if (false == GroupwareCalendarAccess.class.isInstance(access)) {
            throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(getAccount(account.getAccountId()).getProviderId());
        }
        return (GroupwareCalendarAccess) access;
    }

    /**
     * Gets the calendar access for a specific account. The account is connected implicitly and remembered to be closed during
     * {@link #finish()} implicitly, if not already done.
     *
     * @param account The account to get the calendar access for
     * @return The calendar access for the specified account
     */
    private CalendarAccess getAccess(CalendarAccount account) throws OXException {
        ConcurrentMap<Integer, CalendarAccess> connectedAccesses = this.connectedAccesses;
        CalendarAccess access = connectedAccesses.get(I(account.getAccountId()));
        if (null == access) {
            CalendarProvider provider = providerRegistry.getCalendarProvider(account.getProviderId());
            if (null == provider) {
                throw CalendarExceptionCodes.PROVIDER_NOT_AVAILABLE.create(account.getProviderId());
            }
            access = provider.connect(session, account, this);
            CalendarAccess existingAccess = connectedAccesses.put(I(account.getAccountId()), access);
            if (null != existingAccess) {
                access.close();
                access = existingAccess;
            }
        }
        return access;
    }

    /**
     * Gets all calendar accounts of the current session's user.
     *
     * @return The calendar accounts
     */
    private List<CalendarAccount> getAccounts() throws OXException {        
        return services.getService(CalendarAccountService.class).loadAccounts(session); 
    }

    /**
     * Gets a specific calendar account.
     *
     * @param accountId The identifier of the account to get
     * @return The calendar account
     */
    private CalendarAccount getAccount(int accountId) throws OXException {
        CalendarAccountService service = services.getService(CalendarAccountService.class);
        return service.loadAccount(session, accountId);
    }

    @Override
    public String toString() {
        return new StringBuilder("IDBasedCalendarAccess ")
            .append("[user=").append(session.getUserId()).append(", context=").append(session.getContextId())
            .append(", connectedAccesses=").append(connectedAccesses.keySet()).append(']')
        .toString();
    }

    @Override
    public boolean[] hasEventsBetween(Date from, Date until) throws OXException {

        boolean [] result=null;
        for(CalendarAccount account: getAccounts()){
            CalendarAccess access = getAccess(account);
            if(access instanceof FreeBusyAwareCalendarAccess){
                boolean[] hasEventsBetween = ((FreeBusyAwareCalendarAccess) access).hasEventsBetween(from, until);
                if(result==null){
                    result=hasEventsBetween;
                } else {
                    mergeEventsBetween(result, hasEventsBetween);
                }
            }
        }

        return result;
    }

    private void mergeEventsBetween(boolean [] result, boolean [] newValues) throws OXException{
        if(result.length != newValues.length){
            // Should never occur
           throw new OXException(new InvalidParameterException("The two boolean arrays must not have different sizes!"));
        }

        for(int x=0; x<result.length; x++){
            result[x]|=newValues[x];
        }
    }

    @Override
    public Map<Attendee, List<Event>> getFreeBusy(List<Attendee> attendees, Date from, Date until) throws OXException {
        Map<Attendee, List<Event>> result = null;
        for (CalendarAccount account : getAccounts()) {
            CalendarAccess access = getAccess(account);
            if (access instanceof FreeBusyAwareCalendarAccess) {
                Map<Attendee, List<Event>> eventsPerAttendee = ((FreeBusyAwareCalendarAccess) access).getFreeBusy(attendees, from, until);
                if (result == null) {
                    result = eventsPerAttendee;
                    for (Attendee att : result.keySet()) {
                        result.put(att, withUniqueIDs(result.get(att), account.getAccountId()));
                    }
                } else {
                    for (Attendee att : eventsPerAttendee.keySet()) {
                        result.get(att).addAll(withUniqueIDs(eventsPerAttendee.get(att), account.getAccountId()));
                    }
                }
            }
        }

        //TODO properly sort events or remove sorting?

        return result;
    }

    @Override
    public Map<Attendee, List<FreeBusyTime>> getMergedFreeBusy(List<Attendee> attendees, Date from, Date until) throws OXException {
        Map<Attendee, List<FreeBusyTime>> result=null;
        for(CalendarAccount account: getAccounts()){
            CalendarAccess access = getAccess(account);
            if(access instanceof FreeBusyAwareCalendarAccess){
                Map<Attendee, List<FreeBusyTime>> freeBusyTimesPerAttendee = ((FreeBusyAwareCalendarAccess) access).getMergedFreeBusy(attendees, from, until);
                if(result==null){
                    result=freeBusyTimesPerAttendee;
                } else {
                    for(Attendee att: freeBusyTimesPerAttendee.keySet()){
                        result.get(att).addAll(freeBusyTimesPerAttendee.get(att));
                    }
                }
            }
        }

        // Merge results
        for(Attendee att: result.keySet()){
            List<FreeBusyTime> freeBusyTimes = result.get(att);
            result.put(att, FreeBusyUtils.mergeFreeBusy(freeBusyTimes));
        }

        return result;
    }

    @Override
    public List<EventConflict> checkForConflicts(Event event, List<Attendee> attendees) throws OXException {
        List<EventConflict> result=null;
        for(CalendarAccount account: getAccounts()){
            CalendarAccess access = getAccess(account);
            if (access instanceof FreeBusyAwareCalendarAccess) {
                List<EventConflict> eventConflicts = ((FreeBusyAwareCalendarAccess) access).checkForConflicts(event, attendees);
                if (result == null) {
                    result = new ArrayList<>(eventConflicts.size());
                }
                for (EventConflict conflict : eventConflicts) {
                    result.add(new IDManglingEventConflict(conflict, account.getAccountId()));
                }
            }
        }

        //TODO sort results?

        return result;
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

    private static Event find(List<Event> events, EventID eventID) {
        return find(events, eventID.getFolderID(), eventID.getObjectID(), eventID.getRecurrenceID());
    }

    private static Map<Integer, List<EventID>> getIDsPerAccountId(List<EventID> eventIDs) throws OXException {
        Map<Integer, List<EventID>> idsPerAccountId = new HashMap<Integer, List<EventID>>();
        for (EventID eventID : eventIDs) {
            Integer accountId = I(getAccountId(eventID.getFolderID()));
            EventID relativeEventId = getRelativeId(eventID);
            com.openexchange.tools.arrays.Collections.put(idsPerAccountId, accountId, relativeEventId);
        }
        return idsPerAccountId;
    }

}
