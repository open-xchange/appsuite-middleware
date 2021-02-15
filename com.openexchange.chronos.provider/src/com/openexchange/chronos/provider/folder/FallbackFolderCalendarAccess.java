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
