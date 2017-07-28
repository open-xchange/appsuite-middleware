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

package com.openexchange.chronos.provider.composition.impl.idmangling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.composition.CompositeEventID;
import com.openexchange.chronos.provider.composition.CompositeFolderID;
import com.openexchange.chronos.provider.composition.CompositeID;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarFolder;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.UpdatesResult;

/**
 * {@link IDMangling}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class IDMangling {

    /** A set of fixed root folder identifiers excluded from ID mangling */
    private static final Set<String> ROOT_FOLDER_IDS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
        null, // no parent
        "0",  // com.openexchange.folderstorage.FolderStorage.ROOT_ID
        "1",  // com.openexchange.folderstorage.FolderStorage.PRIVATE_ID
        "2",  // com.openexchange.folderstorage.FolderStorage.PUBLIC_ID
        "3"   // com.openexchange.folderstorage.FolderStorage.SHARED_ID
    )));

    /**
     * Initializes a new {@link IDMangling}.
     */
    private IDMangling() {
        super();
    }

    public static CalendarFolder withUniqueID(CalendarFolder folder, CompositeID compositeID) {
        return withUniqueID(folder, compositeID.getAccountId());
    }

    public static List<CalendarFolder> withUniqueID(List<? extends CalendarFolder> folders, CompositeID compositeID) {
        return withUniqueID(folders, compositeID.getAccountId());
    }

    public static List<CalendarFolder> withUniqueID(List<? extends CalendarFolder> folders, int accountId) {
        if (null == folders) {
            return null;
        }
        List<CalendarFolder> foldersWithUniqueIDs = new ArrayList<CalendarFolder>(folders.size());
        for (CalendarFolder folder : folders) {
            foldersWithUniqueIDs.add(withUniqueID(folder, accountId));
        }
        return foldersWithUniqueIDs;
    }

    public static CalendarFolder withUniqueID(CalendarFolder folder, int accountId) {
        String newId = getUniqueFolderId(accountId, folder.getId());
        if (GroupwareCalendarFolder.class.isInstance(folder)) {
            GroupwareCalendarFolder groupwareFolder = (GroupwareCalendarFolder) folder;
            String newParentId = getUniqueFolderId(accountId, groupwareFolder.getParentId());
            return new IDManglingGroupwareFolder(groupwareFolder, newId, newParentId);
        }
        return new IDManglingFolder(folder, newId);
    }

    public static CalendarFolder withRelativeID(CalendarFolder folder) {
        String newId = getRelativeFolderId(folder.getId());
        if (GroupwareCalendarFolder.class.isInstance(folder)) {
            GroupwareCalendarFolder groupwareFolder = (GroupwareCalendarFolder) folder;
            String newParentId = getRelativeFolderId(groupwareFolder.getParentId());
            return new IDManglingGroupwareFolder(groupwareFolder, newId, newParentId);
        }
        return new IDManglingFolder(folder, newId);
    }

    public static Event withRelativeID(Event event) {
        String newId = null == event.getId() ? null : CompositeEventID.parse(event.getId()).getEventId();
        String newFolderId = getRelativeFolderId(event.getFolderId());
        String newSeriesId = null == event.getSeriesId() ? null : CompositeEventID.parse(event.getSeriesId()).getEventId();
        return new IDManglingEvent(event, newId, newFolderId, newSeriesId);
    }

    public static Event withUniqueID(Event event, int accountId) {
        String newId = getUniqueId(accountId, event.getFolderId(), event.getId());
        String newSeriesId = getUniqueId(accountId, event.getFolderId(), event.getSeriesId());
        String newFolderId = getUniqueFolderId(accountId, event.getFolderId());
        return new IDManglingEvent(event, newId, newFolderId, newSeriesId);
    }

    public static Event withUniqueID(Event event, CompositeID compositeID) {
        return withUniqueID(event, compositeID.getAccountId());
    }

    public static List<Event> withUniqueIDs(List<Event> events, CompositeID compositeID) {
        return withUniqueIDs(events, compositeID.getAccountId());
    }

    public static List<Event> withUniqueIDs(List<Event> events, int accountId) {
        if (null == events) {
            return null;
        }
        List<Event> eventsWithUniqueIDs = new ArrayList<Event>(events.size());
        for (Event event : events) {
            eventsWithUniqueIDs.add(withUniqueID(event, accountId));
        }
        return eventsWithUniqueIDs;
    }

    public static UpdatesResult withUniqueIDs(UpdatesResult updatesResult, CompositeID compositeID) {
        return withUniqueIDs(updatesResult, compositeID.getAccountId());
    }

    public static UpdatesResult withUniqueIDs(final UpdatesResult updatesResult, final int accountId) {
        return new UpdatesResult() {

            @Override
            public List<Event> getNewAndModifiedEvents() {
                return withUniqueIDs(updatesResult.getNewAndModifiedEvents(), accountId);
            }

            @Override
            public List<Event> getDeletedEvents() {
                return withUniqueIDs(updatesResult.getDeletedEvents(), accountId);
            }

			@Override
            public long getTimestamp() {
				return updatesResult.getTimestamp();
			}
        };
    }

    public static EventID getRelativeID(CompositeEventID compositeEventID) {
        return new EventID(compositeEventID.getFolderId(), compositeEventID.getEventId(), compositeEventID.getRecurrenceId());
    }

    /**
     * Gets the fully qualified composite representation of a specific relative folder identifier.
     * <p/>
     * {@link IDMangling#ROOT_FOLDER_IDS} are passed as-is implicitly.
     *
     * @param accountId The identifier of the account the folder originates in
     * @param relativeFolderId The relative folder identifier
     * @return The unique folder identifier
     */
    private static String getUniqueFolderId(int accountId, String relativeFolderId) {
        if (ROOT_FOLDER_IDS.contains(relativeFolderId)) {
            return relativeFolderId;
        }
        return new CompositeFolderID(accountId, relativeFolderId).toUniqueID();
    }

    /**
     * Gets the relative representation of a specific unique folder identifier.
     * <p/>
     * {@link IDMangling#ROOT_FOLDER_IDS} are passed as-is implicitly.
     *
     * @param uniqueFolderId The unique folder identifier
     * @return The unique folder identifier
     */
    private static String getRelativeFolderId(String uniqueFolderId) {
        if (ROOT_FOLDER_IDS.contains(uniqueFolderId)) {
            return uniqueFolderId;
        }
        return CompositeFolderID.parse(uniqueFolderId).getFolderId();
    }

    /**
     * Gets the fully qualified composite representation of a specific relative event or series identifier.
     *
     * @param accountId The identifier of the account the folder originates in
     * @param relativeId The relative event or series identifier
     * @param relativeFolderId The relative parent folder identifier
     * @return The unique event or series identifier
     */
    private static String getUniqueId(int accountId, String relativeFolderId, String relativeId) {
        if (null == relativeId) {
            return relativeId;
        }
        return new CompositeEventID(accountId, relativeFolderId, relativeFolderId).toUniqueID();
    }

}
