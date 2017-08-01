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
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarFolder;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link IDMangling}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class IDMangling {

    /** The fixed prefix used to quickly identify calendar folder identifiers. */
    private static final String CAL_PREFIX = "cal";

    /** A set of fixed root folder identifiers excluded from ID mangling */
    private static final Set<String> ROOT_FOLDER_IDS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
        null, // no parent
        "0",  // com.openexchange.folderstorage.FolderStorage.ROOT_ID
        "1",  // com.openexchange.folderstorage.FolderStorage.PRIVATE_ID
        "2",  // com.openexchange.folderstorage.FolderStorage.PUBLIC_ID
        "3"   // com.openexchange.folderstorage.FolderStorage.SHARED_ID
    )));

    /**
     * Gets a calendar folder equipped with unique composite identifiers representing a calendar folder from a specific calendar account.
     *
     * @param folders The calendar folder from the account
     * @param accountId The identifier of the account
     * @return The calendar folder representation with unique identifiers
     */
    public static CalendarFolder withUniqueID(CalendarFolder folder, int accountId) {
        String newId = getUniqueFolderId(accountId, folder.getId());
        if (GroupwareCalendarFolder.class.isInstance(folder)) {
            GroupwareCalendarFolder groupwareFolder = (GroupwareCalendarFolder) folder;
            String newParentId = getUniqueFolderId(accountId, groupwareFolder.getParentId());
            return new IDManglingGroupwareFolder(groupwareFolder, newId, newParentId);
        }
        return new IDManglingFolder(folder, newId);
    }

    /**
     * Gets a list of calendar folders equipped with unique composite identifiers representing the supplied list of calendar folders from
     * a specific calendar account.
     *
     * @param folders The calendar folders from the account
     * @param accountId The identifier of the account
     * @return The calendar folder representations with unique identifiers
     */
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

    /**
     * Gets the account-relative representation for the supplied calendar folder with unique composite identifiers.
     *
     * @param folder The calendar folder
     * @return The calendar folder representation with relative identifiers
     */
    public static CalendarFolder withRelativeID(CalendarFolder folder) throws OXException {
        String newId = getRelativeFolderId(folder.getId());
        if (GroupwareCalendarFolder.class.isInstance(folder)) {
            GroupwareCalendarFolder groupwareFolder = (GroupwareCalendarFolder) folder;
            String newParentId = getRelativeFolderId(groupwareFolder.getParentId());
            return new IDManglingGroupwareFolder(groupwareFolder, newId, newParentId);
        }
        return new IDManglingFolder(folder, newId);
    }

    /**
     * Gets an event equipped with unique composite identifiers representing an event from a specific calendar account.
     *
     * @param event The event from the account
     * @param accountId The identifier of the account
     * @return The event representation with unique identifiers
     */
    public static Event withUniqueID(Event event, int accountId) {
        String newFolderId = getUniqueFolderId(accountId, event.getFolderId());
        return new IDManglingEvent(event, newFolderId);
    }

    /**
     * Gets a list of events equipped with unique composite identifiers representing the supplied list of events from a specific
     * calendar account.
     *
     * @param events The events from the account
     * @param accountId The identifier of the account
     * @return The event representations with unique identifiers
     */
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

    /**
     * Gets the account-relative representation for the supplied event with unique composite identifiers.
     *
     * @param event The event
     * @return The event representation with relative identifiers
     */
    public static Event withRelativeID(Event event) throws OXException {
        String newFolderId = getRelativeFolderId(event.getFolderId());
        return new IDManglingEvent(event, newFolderId);
    }

    /**
     * Gets the account identifier of a specific unique composite folder identifier.
     *
     * @param uniqueFolderId The unique composite folder identifier, e.g. <code>cal://4/35</code>
     * @return The extracted account identifier
     * @throws OXException {@link CalendarExceptionCodes#UNSUPPORTED_FOLDER} if the account identifier can't be extracted from the passed composite identifier
     */
    public static int getAccountId(String uniqueFolderId) throws OXException {
        try {
            return Integer.parseInt(unmangleFolderId(uniqueFolderId).get(1));
        } catch (IllegalArgumentException e) {
            throw CalendarExceptionCodes.UNSUPPORTED_FOLDER.create(e, uniqueFolderId, null);
        }
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
    public static String getUniqueFolderId(int accountId, String relativeFolderId) {
        if (ROOT_FOLDER_IDS.contains(relativeFolderId)) {
            return relativeFolderId;
        }
        return mangleFolderId(accountId, relativeFolderId);
    }

    /**
     * Gets the relative representation of a specific unique composite folder identifier.
     * <p/>
     * {@link IDMangling#ROOT_FOLDER_IDS} are passed as-is implicitly.
     *
     * @param uniqueFolderId The unique composite folder identifier, e.g. <code>cal://4/35</code>
     * @return The extracted relative folder identifier
     * @throws OXException {@link CalendarExceptionCodes#UNSUPPORTED_FOLDER} if passed identifier can't be unmangled to its relative representation
     */
    public static String getRelativeFolderId(String uniqueFolderId) throws OXException {
        if (ROOT_FOLDER_IDS.contains(uniqueFolderId)) {
            return uniqueFolderId;
        }
        try {
            return unmangleFolderId(uniqueFolderId).get(2);
        } catch (IllegalArgumentException e) {
            throw CalendarExceptionCodes.UNSUPPORTED_FOLDER.create(e, uniqueFolderId, null);
        }
    }

    /**
     * Gets the relative representation of a specific unique full event identifier consisting of composite parts.
     *
     * @param uniqueId The unique full event identifier
     * @return The relative full event identifier
     */
    public static EventID getRelativeId(EventID uniqueEventID) throws OXException {
        if (null == uniqueEventID) {
            return uniqueEventID;
        }
        return new EventID(getRelativeFolderId(uniqueEventID.getFolderID()), uniqueEventID.getObjectID(), uniqueEventID.getRecurrenceID());
    }

    /**
     * <i>Mangles</i> the supplied relative folder identifier, together with its corresponding account information.
     *
     * @param accountId The identifier of the account the folder originates in
     * @param relativeFolderId The relative folder identifier
     * @return The mangled folder identifier
     */
    private static String mangleFolderId(int accountId, String relativeFolderId) {
        return IDMangler.mangle(CAL_PREFIX, String.valueOf(accountId), relativeFolderId);
    }

    /**
     * <i>Unmangles</i> the supplied unique folder identifier into its distinct components.
     *
     * @param uniqueFolderId The unique composite folder identifier, e.g. <code>cal://4/35</code>
     * @return The unmangled components of the folder identifier
     * @throws IllegalArgumentException If passed identifier can't be unmangled into its distinct components
     */
    private static List<String> unmangleFolderId(String uniqueFolderId) {
        if (null == uniqueFolderId || false == uniqueFolderId.startsWith(CAL_PREFIX)) {
            throw new IllegalArgumentException(uniqueFolderId);
        }
        List<String> unmangled = IDMangler.unmangle(uniqueFolderId);
        if (null == unmangled || 3 > unmangled.size() || false == CAL_PREFIX.equals(unmangled.get(0))) {
            throw new IllegalArgumentException(uniqueFolderId);
        }
        return unmangled;
    }

    /**
     * Initializes a new {@link IDMangling}.
     */
    private IDMangling() {
        super();
    }

}
