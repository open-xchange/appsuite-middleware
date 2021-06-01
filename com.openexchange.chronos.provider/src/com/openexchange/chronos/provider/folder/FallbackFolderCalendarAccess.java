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

package com.openexchange.chronos.provider.folder;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarCapability;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.CalendarPermission;
import com.openexchange.chronos.provider.DefaultCalendarFolder;
import com.openexchange.chronos.provider.DefaultCalendarPermission;
import com.openexchange.chronos.provider.UsedForSync;
import com.openexchange.chronos.provider.extensions.FolderSearchAware;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.exception.OXException;
import com.openexchange.search.SearchTerm;

/**
 * {@link FallbackFolderCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public abstract class FallbackFolderCalendarAccess implements FolderCalendarAccess, FolderSearchAware {

    protected final CalendarAccount account;

    /**
     * Initializes a new {@link FallbackFolderCalendarAccess}.
     *
     * @param account The underlying account
     */
    protected FallbackFolderCalendarAccess(CalendarAccount account) {
        super();
        this.account = account;
    }

    @Override
    public void close() {
        // nothing to do
    }

    @Override
    public String createFolder(CalendarFolder folder) throws OXException {
        throw unsupportedOperation();
    }

    @Override
    public String updateFolder(String folderId, CalendarFolder folder, long clientTimestamp) throws OXException {
        throw unsupportedOperation();
    }

    @Override
    public void deleteFolder(String folderId, long clientTimestamp) throws OXException {
        throw unsupportedOperation();
    }

    @Override
    public Event getEvent(String folderId, String eventId, RecurrenceId recurrenceId) throws OXException {
        throw CalendarExceptionCodes.EVENT_NOT_FOUND_IN_FOLDER.create(folderId, eventId);
    }

    @Override
    public List<Event> getEvents(List<EventID> eventIDs) throws OXException {
        return Collections.emptyList();
    }

    @Override
    public List<Event> getChangeExceptions(String folderId, String seriesId) throws OXException {
        return Collections.emptyList();
    }

    @Override
    public List<Event> getEventsInFolder(String folderId) throws OXException {
        return Collections.emptyList();
    }

    @Override
    public Map<String, EventsResult> getEventsInFolders(List<String> folderIds) throws OXException {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, EventsResult> searchEvents(List<String> folderIds, List<SearchFilter> filters, List<String> queries) throws OXException {
        return Collections.emptyMap();
    }
    
    @Override
    public Map<String, EventsResult> searchEvents(List<String> folderIds, SearchTerm<?> term) throws OXException {
        return Collections.emptyMap();
    }

    protected DefaultCalendarFolder prepareFallbackFolder(String folderId) {
        DefaultCalendarFolder folder = new DefaultCalendarFolder(folderId, "Account " + account.getAccountId());
        folder.setUsedForSync(UsedForSync.DEACTIVATED);
        folder.setSubscribed(Boolean.TRUE);
        folder.setLastModified(account.getLastModified());
        folder.setSupportedCapabilites(EnumSet.noneOf(CalendarCapability.class));
        folder.setPermissions(Collections.singletonList(new DefaultCalendarPermission(
            String.valueOf(account.getUserId()), account.getUserId(), null,
            CalendarPermission.READ_FOLDER, CalendarPermission.READ_ALL_OBJECTS, CalendarPermission.NO_PERMISSIONS,
            CalendarPermission.NO_PERMISSIONS, false, false, 0)));
        return folder;
    }

    protected OXException unsupportedOperation() throws OXException {
        throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
    }

}
