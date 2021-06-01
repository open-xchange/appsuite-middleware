/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.provider.xctx;

import static com.openexchange.chronos.common.CalendarUtils.DISTANT_FUTURE;
import static com.openexchange.chronos.provider.CalendarAccount.DEFAULT_ACCOUNT;
import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR_LITERAL;
import static com.openexchange.chronos.provider.xctx.Constants.CONTENT_TYPE;
import static com.openexchange.chronos.provider.xctx.Constants.PUBLIC_FOLDER_ID;
import static com.openexchange.chronos.provider.xctx.Constants.SHARED_FOLDER_ID;
import static com.openexchange.chronos.provider.xctx.Constants.TREE_ID;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_IGNORE_STORAGE_WARNINGS;
import static com.openexchange.folderstorage.CalendarFolderConverter.getStorageFolder;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.b;
import static com.openexchange.osgi.Tools.requireService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.dmfs.rfc5545.DateTime;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.Check;
import com.openexchange.chronos.common.DefaultCalendarObjectResource;
import com.openexchange.chronos.common.DefaultCalendarParameters;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarCapability;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.CalendarPermission;
import com.openexchange.chronos.provider.DefaultCalendarPermission;
import com.openexchange.chronos.provider.UsedForSync;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.extensions.FolderSearchAware;
import com.openexchange.chronos.provider.extensions.FolderSyncAware;
import com.openexchange.chronos.provider.extensions.QuotaAware;
import com.openexchange.chronos.provider.extensions.SubscribeAware;
import com.openexchange.chronos.provider.extensions.WarningsAware;
import com.openexchange.chronos.provider.groupware.DefaultGroupwareCalendarFolder;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarAccess;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarFolder;
import com.openexchange.chronos.provider.groupware.GroupwareFolderType;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.chronos.service.FreeBusyResult;
import com.openexchange.chronos.service.ImportResult;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.CalendarFolderConverter;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.ParameterizedFolder;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.java.util.TimeZones;
import com.openexchange.quota.Quota;
import com.openexchange.search.SearchTerm;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.subscription.ShareSubscriptionExceptions;

/**
 * {@link XctxCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class XctxCalendarAccess implements SubscribeAware, GroupwareCalendarAccess, FolderSyncAware, FolderSearchAware, QuotaAware, WarningsAware {

    private static final Logger LOG = LoggerFactory.getLogger(XctxCalendarAccess.class);

    private final Session localSession;
    private final ServiceLookup services;
    private final EntityHelper entityHelper;

    private final CalendarSession guestSession;
    private final CalendarAccount account;
    private final List<OXException> warnings;
    private final CalendarParameters parameters;

    /**
     * Initializes a new {@link XctxCalendarAccess}.
     *
     * @param services A service lookup reference
     * @param account The underlying calendar account
     * @param localSession The user's <i>local</i> session associated with the file storage account
     * @param guestSession The <i>remote</i> session of the guest user used to access the contents of the foreign context
     * @param parameters Additional calendar parameters
     */
    public XctxCalendarAccess(ServiceLookup services, CalendarAccount account, Session localSession, Session guestSession, CalendarParameters parameters) throws OXException {
        super();
        this.parameters = parameters;
        this.guestSession = services.getServiceSafe(CalendarService.class).init(guestSession, prepareRemoteParameters(parameters));
        this.services = services;
        this.localSession = localSession;
        this.account = account;
        this.warnings = new ArrayList<OXException>();
        this.entityHelper = new EntityHelper(services, account);
    }

    @Override
    public void close() {
        // nothing to close
    }

    @Override
    public List<GroupwareCalendarFolder> getVisibleFolders(GroupwareFolderType type) throws OXException {
        switch (type) {
            case PRIVATE:
                return Collections.emptyList();
            case SHARED:
                return rememberVisibleCalendars(getCalendarFolders(
                    getSubfoldersRecursively(getFolderService(), initDecorator(), SHARED_FOLDER_ID)), type);
            case PUBLIC:
                return rememberVisibleCalendars(getCalendarFolders(
                    getSubfoldersRecursively(getFolderService(), initDecorator(), PUBLIC_FOLDER_ID)), type);
            default:
                throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
        }
    }

    @Override
    public GroupwareCalendarFolder getFolder(String folderId) throws OXException {
        return getCalendarFolder(getFolderService().getFolder(TREE_ID, folderId, guestSession.getSession(), initDecorator()));
    }

    @Override
    public void deleteFolder(String folderId, long clientTimestamp) throws OXException {
        getFolderService().deleteFolder(TREE_ID, folderId, new Date(clientTimestamp), guestSession.getSession(), initDecorator());
    }

    @Override
    public String updateFolder(String folderId, CalendarFolder folder, long clientTimestamp) throws OXException {
        /*
         * update folder's configuration in underlying account's internal config
         */
        JSONObject internalConfig = null != account.getInternalConfiguration() ? new JSONObject(account.getInternalConfiguration()) : new JSONObject();
        boolean updated = false;
        if (null != folder.isSubscribed() && new AccountConfigHelper(internalConfig).setSubscribed(folderId, folder.isSubscribed())) {
            updated = true;
            if (false == b(folder.isSubscribed())) {
                /*
                 * transition to 'unsubscribed', check if everything else is already unsubscribed
                 */
                List<CalendarFolder> subscribedFolders = getVisibleFolders(false);
                if (null != subscribedFolders && 1 == subscribedFolders.size() && folderId.equals(subscribedFolders.get(0).getId())) {
                    if (b(guestSession.get(PARAMETER_IGNORE_STORAGE_WARNINGS, Boolean.class, Boolean.FALSE))) {
                        /*
                         * delete whole calendar account & return
                         */
                        services.getServiceSafe(CalendarAccountService.class).deleteAccount(localSession, account.getAccountId(), clientTimestamp, null);
                        return folderId;
                    }
                    /*
                     * cancel update operation & track appropriate warning if not ignored
                     */
                    String folderName = subscribedFolders.get(0).getName();
                    String accountName = account.getUserConfiguration().optString("name", "Account " + account.getAccountId());
                    warnings.add(ShareSubscriptionExceptions.ACCOUNT_WILL_BE_REMOVED.create(folderName, accountName));
                    return null;
                }
            }
        }
        if (null != folder.getExtendedProperties() && folder.getExtendedProperties().get(COLOR_LITERAL) != null && new AccountConfigHelper(internalConfig).setColor(folderId, folder.getExtendedProperties().get(COLOR_LITERAL))) {
            updated = true;
        }
        if (updated) {
            JSONObject userConfig = null != account.getUserConfiguration() ? account.getUserConfiguration() : new JSONObject();
            userConfig.putSafe("internalConfig", internalConfig);
            services.getService(CalendarAccountService.class).updateAccount(localSession, account.getAccountId(), userConfig, clientTimestamp, parameters);
        }
        /*
         * forward common folder update to remote context
         */
        DefaultGroupwareCalendarFolder folderUpdate = new DefaultGroupwareCalendarFolder(folder);
        folderUpdate.setExtendedProperties(null);
        folderUpdate.setSubscribed(null);
        folderUpdate.setUsedForSync(null);
        ParameterizedFolder storageFolder = getStorageFolder(TREE_ID, CONTENT_TYPE, folderUpdate, DEFAULT_ACCOUNT.getProviderId(), DEFAULT_ACCOUNT.getAccountId(), null);
        getFolderService().updateFolder(storageFolder, new Date(clientTimestamp), guestSession.getSession(), initDecorator());
        return storageFolder.getID();
    }

    @Override
    public String createFolder(CalendarFolder folder) throws OXException {
        throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
    }

    @Override
    public long getSequenceNumber(String folderId) throws OXException {
        return getCalendarService().getSequenceNumber(guestSession, folderId);
    }

    @Override
    public Event getEvent(String folderId, String eventId, RecurrenceId recurrenceId) throws OXException {
        Event event = getCalendarService().getEvent(guestSession, folderId, new EventID(folderId, eventId, recurrenceId));
        return entityHelper.mangleRemoteEvent(event);
    }

    @Override
    public List<Event> getChangeExceptions(String folderId, String seriesId) throws OXException {
        List<Event> changeExceptions = getCalendarService().getChangeExceptions(guestSession, folderId, seriesId);
        return entityHelper.mangleRemoteEvents(changeExceptions);
    }

    @Override
    public List<Event> getEvents(List<EventID> eventIDs) throws OXException {
        List<Event> events = getCalendarService().getEvents(guestSession, eventIDs);
        return entityHelper.mangleRemoteEvents(events);
    }

    @Override
    public List<Event> getEventsInFolder(String folderId) throws OXException {
        List<Event> events = getCalendarService().getEventsInFolder(guestSession, folderId);
        return entityHelper.mangleRemoteEvents(events);
    }

    @Override
    public Map<String, EventsResult> getEventsInFolders(List<String> folderIds) throws OXException {
        Map<String, EventsResult> resultsPerFolder = getCalendarService().getEventsInFolders(guestSession, folderIds);
        return entityHelper.mangleMappedEventsResults(resultsPerFolder);
    }

    @Override
    public UpdatesResult getUpdatedEventsInFolder(String folderId, long updatedSince) throws OXException {
        UpdatesResult updatesResult = getCalendarService().getUpdatedEventsInFolder(guestSession, folderId, updatedSince);
        return entityHelper.mangleRemoteUpdatesResult(updatesResult);
    }

    @Override
    public List<Event> resolveResource(String folderId, String resourceName) throws OXException {
        List<Event> events = getCalendarService().getUtilities().resolveResource(guestSession, folderId, resourceName);
        return entityHelper.mangleRemoteEvents(events);
    }

    @Override
    public Map<String, EventsResult> resolveResources(String folderId, List<String> resourceNames) throws OXException {
        Map<String, EventsResult> resultsPerFolder = getCalendarService().getUtilities().resolveResources(guestSession, folderId, resourceNames);
        return entityHelper.mangleMappedEventsResults(resultsPerFolder);
    }

    @Override
    public Map<String, EventsResult> searchEvents(List<String> folderIds, List<SearchFilter> filters, List<String> queries) throws OXException {
        Map<String, EventsResult> resultsPerFolder = getCalendarService().searchEvents(guestSession, folderIds, filters, queries);
        return entityHelper.mangleMappedEventsResults(resultsPerFolder);
    }
    
    @Override
    public Map<String, EventsResult> searchEvents(List<String> folderIds, SearchTerm<?> term) throws OXException {
        Map<String, EventsResult> resultsPerFolder = getCalendarService().searchEvents(guestSession, folderIds, term);
        return entityHelper.mangleMappedEventsResults(resultsPerFolder);
    }

    @Override
    public CalendarResult createEvent(String folderId, Event event) throws OXException {
        Check.containsNoSuchAttendees(event, Boolean.TRUE, CalendarUserType.RESOURCE, CalendarUserType.ROOM, CalendarUserType.GROUP);
        Event unmangledEvent = entityHelper.unmangleLocalEvent(event);
        CalendarResult calendarResult = getCalendarService().createEvent(guestSession, folderId, unmangledEvent);
        return entityHelper.mangleRemoteCalendarResult(calendarResult);
    }

    @Override
    public CalendarResult putResource(String folderId, CalendarObjectResource resource, boolean replace) throws OXException {
        List<Event> unmangledEventData = new ArrayList<Event>();
        for (Event event : resource.getEvents()) {
            Check.containsNoSuchAttendees(event, Boolean.TRUE, CalendarUserType.RESOURCE, CalendarUserType.ROOM, CalendarUserType.GROUP);
            unmangledEventData.add(entityHelper.unmangleLocalEvent(event));
        }
        CalendarResult calendarResult = getCalendarService().putResource(guestSession, folderId, new DefaultCalendarObjectResource(unmangledEventData), replace);
        return entityHelper.mangleRemoteCalendarResult(calendarResult);
    }

    @Override
    public CalendarResult updateEvent(EventID eventID, Event event, long clientTimestamp) throws OXException {
        Check.containsNoSuchAttendees(event, Boolean.TRUE, CalendarUserType.RESOURCE, CalendarUserType.ROOM, CalendarUserType.GROUP);
        Event unmangledEvent = entityHelper.unmangleLocalEvent(event);
        CalendarResult calendarResult = getCalendarService().updateEvent(guestSession, eventID, unmangledEvent, clientTimestamp);
        return entityHelper.mangleRemoteCalendarResult(calendarResult);
    }

    @Override
    public CalendarResult moveEvent(EventID eventID, String folderId, long clientTimestamp) throws OXException {
        CalendarResult calendarResult = getCalendarService().moveEvent(guestSession, eventID, folderId, clientTimestamp);
        return entityHelper.mangleRemoteCalendarResult(calendarResult);
    }

    @Override
    public CalendarResult updateAttendee(EventID eventID, Attendee attendee, List<Alarm> alarms, long clientTimestamp) throws OXException {
        Attendee unmangledAttendee = entityHelper.unmangleLocalAttendee(attendee);
        CalendarResult calendarResult = getCalendarService().updateAttendee(guestSession, eventID, unmangledAttendee, alarms, clientTimestamp);
        return entityHelper.mangleRemoteCalendarResult(calendarResult);
    }

    @Override
    public CalendarResult changeOrganizer(EventID eventID, Organizer organizer, long clientTimestamp) throws OXException {
        Organizer unmangledOrganizer = entityHelper.unmangleLocalOrganizer(organizer);
        CalendarResult calendarResult = getCalendarService().changeOrganizer(guestSession, eventID, unmangledOrganizer, clientTimestamp);
        return entityHelper.mangleRemoteCalendarResult(calendarResult);
    }

    @Override
    public CalendarResult deleteEvent(EventID eventID, long clientTimestamp) throws OXException {
        CalendarResult calendarResult = getCalendarService().deleteEvent(guestSession, eventID, clientTimestamp);
        return entityHelper.mangleRemoteCalendarResult(calendarResult);
    }

    @Override
    public CalendarResult splitSeries(EventID eventID, DateTime splitPoint, String uid, long clientTimestamp) throws OXException {
        CalendarResult calendarResult = getCalendarService().splitSeries(guestSession, eventID, splitPoint, uid, clientTimestamp);
        return entityHelper.mangleRemoteCalendarResult(calendarResult);
    }

    @Override
    public List<ImportResult> importEvents(String folderId, List<Event> events) throws OXException {
        List<ImportResult> importResults = getCalendarService().importEvents(guestSession, folderId, events);
        return entityHelper.mangleRemoteImportResults(importResults);
    }

    @Override
    public Quota[] getQuotas() throws OXException {
        return getCalendarService().getUtilities().getQuotas(guestSession);
    }

    @Override
    public IFileHolder getAttachment(EventID eventID, int managedId) throws OXException {
        return getCalendarService().getAttachment(guestSession, eventID, managedId);
    }

    @Override
    public List<OXException> getWarnings() {
        List<OXException> guestSessionWarnings = guestSession.getWarnings();
        List<OXException> thisWarnings = this.warnings;
        if (null == guestSessionWarnings || guestSessionWarnings.isEmpty()) {
            return thisWarnings;
        }
        if (null == thisWarnings || thisWarnings.isEmpty()) {
            return guestSessionWarnings;
        }
        List<OXException> warnings = new ArrayList<OXException>();
        warnings.addAll(guestSessionWarnings);
        warnings.addAll(thisWarnings);
        return warnings;
    }

    /**
     * Gets free/busy information in a certain interval for one ore more attendees.
     * <p/>
     * Optionally, the resulting data is pre-processed and sorted by time, so that any overlapping intervals each of the attendee's
     * free/busy time are merged implicitly to the most conflicting busy times.
     *
     * @param attendees The attendees to get the free/busy data for
     * @param from The start of the requested time range
     * @param until The end of the requested time range
     * @param merge <code>true</code> to merge the resulting free/busy-times, <code>false</code>, otherwise
     * @return The free/busy times for each of the requested attendees, wrapped within a free/busy result structure
     */
    public Map<Attendee, FreeBusyResult> getFreeBusy(List<Attendee> attendees, Date from, Date until, boolean merge) throws OXException {
        List<Attendee> unmangledAttendees = entityHelper.unmangleLocalAttendees(attendees);
        Map<Attendee, FreeBusyResult> result = guestSession.getFreeBusyService().getFreeBusy(guestSession, unmangledAttendees, from, until, merge);
        if (null == result || result.isEmpty()) {
            return result;
        }
        Map<Attendee, FreeBusyResult> mangledResult = new HashMap<Attendee, FreeBusyResult>(result.size());
        for (Entry<Attendee, FreeBusyResult> entry : result.entrySet()) {
            /*
             * attendee may have been resolved to internal user in remote context; check
             */
            Attendee attendee = CalendarUtils.find(attendees, entityHelper.mangleRemoteAttendee(entry.getKey()));
            if (null == attendee) {
                LOG.debug("Skipping unexpected attendee {} in free/busy results from account {}", attendee, I(account.getAccountId()));
                continue;
            }
            mangledResult.put(attendee, entry.getValue());
        }
        return mangledResult;
    }

    private List<CalendarFolder> getVisibleFolders(boolean all) throws OXException {
        List<CalendarFolder> folders = getVisibleFolders();
        if (null == folders || folders.isEmpty() || all) {
            return folders;
        }
        List<CalendarFolder> subscribedFolders = new ArrayList<CalendarFolder>(folders.size());
        for (CalendarFolder folder : folders) {
            if (null == folder.isSubscribed() || b(folder.isSubscribed())) {
                subscribedFolders.add(folder);
            }
        }
        return subscribedFolders;
    }

    /**
     * Gets the folder service, throwing an appropriate exception in case the service is absent.
     *
     * @return The folder service
     */
    private FolderService getFolderService() throws OXException {
        return requireService(FolderService.class, services);
    }

    /**
     * Gets the calendar service.
     *
     * @return The calendar service
     */
    private CalendarService getCalendarService() {
        return guestSession.getCalendarService();
    }

    /**
     * Creates and initializes a folder service decorator ready to use with calls to the underlying folder service.
     *
     * @return A new folder service decorator
     */
    private FolderServiceDecorator initDecorator() throws OXException {
        FolderServiceDecorator decorator = new FolderServiceDecorator();
        decorator.setLocale(guestSession.getEntityResolver().getLocale(guestSession.getUserId()));
        decorator.put("altNames", Boolean.TRUE.toString());
        decorator.setTimeZone(TimeZones.UTC);
        decorator.setAllowedContentTypes(Collections.<ContentType> singletonList(CONTENT_TYPE));
        return decorator;
    }

    /**
     * Collects all calendar subfolders from a parent folder recursively.
     *
     * @param folderService A reference to the folder service
     * @param decorator The optional folder service decorator to use
     * @param parentId The parent folder identifier to get the subfolders from
     * @return The collected subfolders, or an empty list if there are none
     */
    private List<UserizedFolder> getSubfoldersRecursively(FolderService folderService, FolderServiceDecorator decorator, String parentId) throws OXException {
        UserizedFolder[] subfolders = folderService.getSubfolders(TREE_ID, parentId, true, guestSession.getSession(), decorator).getResponse();
        if (null == subfolders || 0 == subfolders.length) {
            return Collections.emptyList();
        }
        List<UserizedFolder> allFolders = new ArrayList<UserizedFolder>();
        for (UserizedFolder subfolder : subfolders) {
            if (CONTENT_TYPE.equals(subfolder.getContentType())) {
                allFolders.add(subfolder);
            }
            if (subfolder.hasSubscribedSubfolders()) {
                allFolders.addAll(getSubfoldersRecursively(folderService, decorator, subfolder.getID()));
            }
        }
        return allFolders;
    }

    /**
     * Gets a list of groupware calendar folders representing the folders in the supplied userized folders.
     *
     * @param folders The folders as retrieved from the folder service
     * @return The groupware calendar folders
     */
    private List<GroupwareCalendarFolder> getCalendarFolders(List<UserizedFolder> folders) {
        if (null == folders || 0 == folders.size()) {
            return Collections.emptyList();
        }
        List<GroupwareCalendarFolder> calendarFolders = new ArrayList<GroupwareCalendarFolder>(folders.size());
        for (UserizedFolder userizedFolder : folders) {
            calendarFolders.add(getCalendarFolder(userizedFolder));
        }
        return calendarFolders;
    }

    /**
     * Gets a groupware calendar foldes representing the supplied userized folder.
     *
     * @param folder The folder as retrieved from the folder service
     * @return The groupware calendar folder
     */
    private DefaultGroupwareCalendarFolder getCalendarFolder(UserizedFolder folder) {
        /*
         * get calendar folder with entities under perspective of remote guest session in foreign context
         */
        DefaultGroupwareCalendarFolder calendarFolder = CalendarFolderConverter.getCalendarFolder(folder);
        calendarFolder.setDefaultFolder(false);
        calendarFolder.setParentId(GroupwareFolderType.PUBLIC.equals(calendarFolder.getType()) ? PUBLIC_FOLDER_ID : SHARED_FOLDER_ID);
        /*
         * qualify remote entities for usage in local session in storage account's context
         */
        calendarFolder.setCreatedFrom(entityHelper.mangleRemoteEntity(entityHelper.addEntityInfo(guestSession.getSession(), calendarFolder.getCreatedFrom())));
        calendarFolder.setModifiedFrom(entityHelper.mangleRemoteEntity(entityHelper.addEntityInfo(guestSession.getSession(), calendarFolder.getModifiedFrom())));
        /*
         * enhance & qualify remote entities in folder permissions for usage in local session in storage account's context
         */
        List<CalendarPermission> permissions = entityHelper.addPermissionEntityInfos(guestSession, calendarFolder.getPermissions());
        permissions = entityHelper.mangleRemoteCalendarPermissions(permissions);
        /*
         * insert user's own permission as system permission to ensure folder is considered as visible for the local session user throughout the stack
         */
        Permission ownPermission = folder.getOwnPermission();
        permissions.add(new DefaultCalendarPermission(String.valueOf(localSession.getUserId()), localSession.getUserId(), null,
            ownPermission.getFolderPermission(), ownPermission.getReadPermission(), ownPermission.getWritePermission(), ownPermission.getDeletePermission(),
            ownPermission.isAdmin(), false, Permissions.createPermissionBits(ownPermission)));
        calendarFolder.setPermissions(permissions);
        /*
         * apply capabilities
         */
        EnumSet<CalendarCapability> capabilities = CalendarCapability.getCapabilities(getClass());
        capabilities.remove(CalendarCapability.PERMISSIONS);
        calendarFolder.setSupportedCapabilites(capabilities);
        /*
         * derive extended properties, and 'subscribed' state from account config
         */
        JSONObject internalConfig = null != account.getInternalConfiguration() ? new JSONObject(account.getInternalConfiguration()) : new JSONObject();
        AccountConfigHelper configHelper = new AccountConfigHelper(internalConfig);
        ExtendedProperties extendedProperties = new ExtendedProperties();
        extendedProperties.add(configHelper.getColor(folder.getID()));
        calendarFolder.setExtendedProperties(extendedProperties);
        calendarFolder.setSubscribed(configHelper.isSubscribed(folder.getID()));
        calendarFolder.setUsedForSync(UsedForSync.DEACTIVATED);
        return calendarFolder;
    }

    /**
     * Remembers the calendar folders for a certain folder type within the account configuration.
     * <p/>
     * Previously remembered folders of this type are purged implicitly, so that the passed collection of folders will effectively replace
     * the last known state for this folder type afterwards.
     *
     * @param calendarFolders The calendar folders to remember
     * @param type The folder type to remember the calendars for
     * @return The passed calendar folders after they were remembered
     */
    private List<GroupwareCalendarFolder> rememberVisibleCalendars(List<GroupwareCalendarFolder> calendarFolders, GroupwareFolderType type) {
        if (null == calendarFolders || null == type) {
            return calendarFolders;
        }
        JSONObject internalConfig = null != account.getInternalConfiguration() ? new JSONObject(account.getInternalConfiguration()) : new JSONObject();
        if (new AccountConfigHelper(internalConfig).rememberVisibleCalendars(calendarFolders, type)) {
            try {
                JSONObject userConfig = null != account.getUserConfiguration() ? account.getUserConfiguration() : new JSONObject();
                userConfig.putSafe("internalConfig", internalConfig);
                services.getService(CalendarAccountService.class).updateAccount(localSession, account.getAccountId(), userConfig, DISTANT_FUTURE, parameters);
            } catch (OXException e) {
                LOG.warn("Error remembering calendar folders in account config", e);
            }
        }
        return calendarFolders;
    }

    private static CalendarParameters prepareRemoteParameters(CalendarParameters parameters) {
        if (null == parameters) {
            return null;
        }
        DefaultCalendarParameters remoteParameters = new DefaultCalendarParameters(parameters);
        remoteParameters.set(CalendarParameters.PARAMETER_CONNECTION(), null);
        return remoteParameters;
    }

}
