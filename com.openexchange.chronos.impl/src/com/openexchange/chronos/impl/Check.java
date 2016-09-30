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

package com.openexchange.chronos.impl;

import static com.openexchange.chronos.impl.Utils.i;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.type.PublicType;

/**
 * {@link CalendarService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Check {

    /**
     * Checks that the required permissions are fulfilled in a specific userized folder.
     *
     * @param folder The folder to check the permissions for
     * @param requiredFolderPermission The required folder permission, or {@link Permission#NO_PERMISSIONS} if none required
     * @param requiredReadPermission The required read object permission, or {@link Permission#NO_PERMISSIONS} if none required
     * @param requiredWritePermission The required write object permission, or {@link Permission#NO_PERMISSIONS} if none required
     * @param requiredDeletePermission The required delete object permission, or {@link Permission#NO_PERMISSIONS} if none required
     * @throws OXException {@link CalendarExceptionCodes#NO_READ_PERMISSION}, {@link CalendarExceptionCodes#NO_WRITE_PERMISSION}, {@link CalendarExceptionCodes#NO_DELETE_PERMISSION}
     */
    public static void requireCalendarPermission(UserizedFolder folder, int requiredFolderPermission, int requiredReadPermission, int requiredWritePermission, int requiredDeletePermission) throws OXException {
        if (false == CalendarContentType.class.isInstance(folder.getContentType())) {
            throw CalendarExceptionCodes.NO_READ_PERMISSION.create(I(folder.getContext().getContextId()), I(folder.getUser().getId()), I(i(folder)));
        }
        Permission ownPermission = folder.getOwnPermission();
        if (ownPermission.getFolderPermission() < requiredFolderPermission) {
            throw CalendarExceptionCodes.NO_READ_PERMISSION.create(I(folder.getContext().getContextId()), I(folder.getUser().getId()), I(i(folder)));
        }
        if (ownPermission.getReadPermission() < requiredReadPermission) {
            throw CalendarExceptionCodes.NO_READ_PERMISSION.create(I(folder.getContext().getContextId()), I(folder.getUser().getId()), I(i(folder)));
        }
        if (ownPermission.getWritePermission() < requiredWritePermission) {
            throw CalendarExceptionCodes.NO_WRITE_PERMISSION.create(I(folder.getContext().getContextId()), I(folder.getUser().getId()), I(i(folder)));
        }
        if (ownPermission.getDeletePermission() < requiredDeletePermission) {
            throw CalendarExceptionCodes.NO_DELETE_PERMISSION.create(I(folder.getContext().getContextId()), I(folder.getUser().getId()), I(i(folder)));
        }
    }

    public static void allowedOrganizerSchedulingObjectChange(Event originalEvent, Event udpatedEvent) throws OXException {

    }

    public static void allowedAttendeeSchedulingObjectChange(Event originalEvent, Event udpatedEvent) throws OXException {

    }

    public static void requireMinimumSearchPatternLength(String pattern) throws OXException {

    }

    /**
     * Checks that the supplied client timestamp is equal to or greater than the last modification time of the event.
     *
     * @param event The event to check the timestamp against
     * @param clientTimestampp The client timestamp
     * @throws OXException {@link CalendarExceptionCodes#CONCURRENT_MODIFICATION}
     */
    public static void requireUpToDateTimestamp(Event event, long clientTimestampp) throws OXException {
        if (event.getLastModified().getTime() > clientTimestampp) {
            throw CalendarExceptionCodes.CONCURRENT_MODIFICATION.create(I(event.getId()), L(clientTimestampp), L(event.getLastModified().getTime()));
        }
    }

    /**
     * Checks that a specific event is actually present in the supplied folder. Based on the folder type, the event's public folder
     * identifier or the attendee's personal calendar folder is checked.
     *
     * @param event The event to check
     * @param folder The folder where the event should appear in
     * @throws OXException {@link CalendarExceptionCodes#EVENT_NOT_FOUND_IN_FOLDER}
     */
    public static void eventIsInFolder(Event event, UserizedFolder folder) throws OXException {
        if (PublicType.getInstance().equals(folder.getType())) {
            if (event.getPublicFolderId() != i(folder)) {
                throw CalendarExceptionCodes.EVENT_NOT_FOUND_IN_FOLDER.create(I(i(folder)), I(event.getId()));
            }
        } else {
            Attendee userAttendee = CalendarUtils.find(event.getAttendees(), folder.getCreatedBy());
            if (null == userAttendee || userAttendee.getFolderID() != i(folder)) {
                throw CalendarExceptionCodes.EVENT_NOT_FOUND_IN_FOLDER.create(I(i(folder)), I(event.getId()));
            }
        }
    }

    private Check() {
        super();
    }

}
