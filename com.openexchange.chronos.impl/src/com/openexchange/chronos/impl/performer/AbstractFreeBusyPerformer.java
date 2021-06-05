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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.isAttendee;
import static com.openexchange.chronos.common.CalendarUtils.isInternal;
import static com.openexchange.chronos.common.CalendarUtils.isOrganizer;
import static com.openexchange.chronos.common.CalendarUtils.isPublicClassification;
import static com.openexchange.chronos.common.CalendarUtils.matches;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;

/**
 * {@link AbstractFreeBusyPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class AbstractFreeBusyPerformer extends AbstractQueryPerformer {

    private List<CalendarFolder> visibleFolders;

    /**
     * Initializes a new {@link AbstractFreeBusyPerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     */
    protected AbstractFreeBusyPerformer(CalendarSession session, CalendarStorage storage) {
        super(session, storage);
    }

    /**
     * Reads the attendee data from storage
     *
     * @param events The events to load
     * @param internal whether to only consider internal attendees or not
     * @return The {@link Event}s containing the attendee data
     * @throws OXException
     */
    protected List<Event> readAttendeeData(List<Event> events, Boolean internal) throws OXException {
        return FreeBusyPerformerUtil.readAttendeeData(events, internal, storage);
    }

    /**
     * Gets the timezone to consider for <i>floating</i> dates of a specific attendee.
     * <p/>
     * For <i>internal</i>, individual calendar user attendees, this is the configured timezone of the user; otherwise, the timezone of
     * the current session's user is used.
     *
     * @param attendee The attendee to get the timezone to consider for <i>floating</i> dates for
     * @return The timezone
     */
    protected TimeZone getTimeZone(Attendee attendee) throws OXException {
        if (isInternal(attendee) && CalendarUserType.INDIVIDUAL.equals(attendee.getCuType())) {
            return session.getEntityResolver().getTimeZone(attendee.getEntity());
        }
        return Utils.getTimeZone(session);
    }

    /**
     * Gets a value indicating whether a certain event is visible or <i>opaque to</i> free/busy results in the view of the current
     * session's user or not.
     *
     * @param event The event to check
     * @return <code>true</code> if the event should be considered, <code>false</code>, otherwise
     */
    protected boolean considerForFreeBusy(Event event) {

        String maskId = session.get(CalendarParameters.PARAMETER_MASK_ID, String.class);
        if (maskId != null && (maskId.equals(event.getId()) || maskId.equals(event.getSeriesId()))){
            return false;
        }

        // exclude foreign events classified as 'private' (but keep 'confidential' ones)
        int userId = session.getUserId();
        return isPublicClassification(event) || Classification.CONFIDENTIAL.equals(event.getClassification()) ||
            matches(event.getCalendarUser(), userId) || isOrganizer(event, userId) || isAttendee(event, userId);
    }

    /**
     * Gets all calendar folders accessible by the current sesssion's user.
     *
     * @return The folders, or an empty list if there are none
     */
    protected List<CalendarFolder> getVisibleFolders() throws OXException {
        if (null == visibleFolders) {
            visibleFolders = Utils.getVisibleFolders(session);
        }
        return visibleFolders;
    }

    /**
     * Chooses the most appropriate parent folder identifier to render an event in for the current session's user. This is
     * <ul>
     * <li>the common parent folder identifier for an event in a public folder, in case the user has appropriate folder permissions</li>
     * <li><code>-1</code> for an event in a public folder, in case the user has no appropriate folder permissions</li>
     * <li>the user attendee's personal folder identifier for an event in a non-public folder, in case the user is attendee of the event</li>
     * <li>another attendee's personal folder identifier for an event in a non-public folder, in case the user does not attend on his own, but has appropriate folder permissions for this attendee's folder</li>
     * <li><code>-1</code> for an event in a non-public folder, in case the user has no appropriate folder permissions for any of the attendees</li>
     * </ul>
     *
     * @param event The event to choose the folder identifier for
     * @return The chosen folder identifier, or <code>null</code> if there is none
     */
    protected String chooseFolderID(Event event) throws OXException {
        /*
         * check common folder permissions for events with a static parent folder
         */
        if (null != event.getFolderId()) {
            CalendarFolder folder = findFolder(getVisibleFolders(), event.getFolderId());
            if (null != folder) {
                int readPermission = folder.getOwnPermission().getReadPermission();
                if (Permission.READ_ALL_OBJECTS <= readPermission || Permission.READ_OWN_OBJECTS == readPermission && matches(event.getCreatedBy(), session.getUserId())) {
                    return event.getFolderId();
                }
            }
            return null;
        }
        /*
         * prefer user's personal folder if user is attendee
         */
        Attendee ownAttendee = find(event.getAttendees(), session.getUserId());
        if (null != ownAttendee) {
            return ownAttendee.getFolderId();
        }
        /*
         * choose the most appropriate attendee folder, otherwise
         */
        CalendarFolder chosenFolder = null;
        for (Attendee attendee : event.getAttendees()) {
            CalendarFolder folder = findFolder(getVisibleFolders(), attendee.getFolderId());
            if (null != folder) {
                int readPermission = folder.getOwnPermission().getReadPermission();
                if (Permission.READ_ALL_OBJECTS <= readPermission || Permission.READ_OWN_OBJECTS == readPermission && matches(event.getCreatedBy(), session.getUserId())) {
                    chosenFolder = chooseFolder(chosenFolder, folder);
                }
            }
        }
        return null == chosenFolder ? null : chosenFolder.getId();
    }

    /**
     * Chooses a folder from two candidates based on the <i>highest</i> own permissions.
     *
     * @param folder1 The first candidate, or <code>null</code> to always choose the second candidate
     * @param folder2 The second candidate, or <code>null</code> to always choose the first candidate
     * @return The chosen folder, or <code>null</code> in case both candidates were <code>null</code>
     */
    private static CalendarFolder chooseFolder(CalendarFolder folder1, CalendarFolder folder2) {
        if (null == folder1) {
            return folder2;
        }
        if (null == folder2) {
            return folder1;
        }
        Permission permission1 = folder1.getOwnPermission();
        Permission permission2 = folder2.getOwnPermission();
        if (permission1.getReadPermission() > permission2.getReadPermission()) {
            return folder1;
        }
        if (permission1.getReadPermission() < permission2.getReadPermission()) {
            return folder2;
        }
        if (permission1.getWritePermission() > permission2.getWritePermission()) {
            return folder1;
        }
        if (permission1.getWritePermission() < permission2.getWritePermission()) {
            return folder2;
        }
        if (permission1.getDeletePermission() > permission2.getDeletePermission()) {
            return folder1;
        }
        if (permission1.getDeletePermission() < permission2.getDeletePermission()) {
            return folder2;
        }
        if (permission1.getFolderPermission() > permission2.getFolderPermission()) {
            return folder1;
        }
        if (permission1.getFolderPermission() < permission2.getFolderPermission()) {
            return folder2;
        }
        return permission1.isAdmin() ? folder1 : permission2.isAdmin() ? folder2 : folder1;
    }

    /**
     * Searches a userized folder in a collection of folders by its numerical identifier.
     *
     * @param folders The folders to search
     * @param id The identifier of the folder to lookup
     * @return The matching folder, or <code>null</code> if not found
     */
    private static CalendarFolder findFolder(Collection<CalendarFolder> folders, String id) {
        if (null != folders && null != id) {
            for (CalendarFolder folder : folders) {
                if (id.equals(folder.getId())) {
                    return folder;
                }
            }
        }
        return null;
    }

}
