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

import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarService;
import com.openexchange.chronos.Event;
import com.openexchange.exception.OXException;
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

    public static void requireCalendarContentType(UserizedFolder folder) throws OXException {
        if (false == CalendarContentType.class.isInstance(folder.getContentType())) {
            throw new OXException();
        }
    }

    public static void requireFolderPermission(UserizedFolder folder, int requiredPermission) throws OXException {
        if (folder.getOwnPermission().getFolderPermission() < requiredPermission) {
            throw new OXException();
        }
    }

    public static void requireReadPermission(UserizedFolder folder, int requiredPermission) throws OXException {
        if (folder.getOwnPermission().getReadPermission() < requiredPermission) {
            throw new OXException();
        }
    }

    public static void requireWritePermission(UserizedFolder folder, int requiredPermission) throws OXException {
        if (folder.getOwnPermission().getWritePermission() < requiredPermission) {
            throw new OXException();
        }
    }

    public static void requireDeletePermission(UserizedFolder folder, int requiredPermission) throws OXException {
        if (folder.getOwnPermission().getDeletePermission() < requiredPermission) {
            throw new OXException();
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
     * @throws OXException If the check fails
     */
    public static void requireUpToDateTimestamp(Event event, long clientTimestampp) throws OXException {
        if (event.getLastModified().getTime() > clientTimestampp) {
            throw new OXException();
        }
    }

    /**
     * Checks that a specific event is actually present in the supplied folder. Based on the folder type, the event's public folder
     * identifier or the attendee's personal calendar folder is checked.
     *
     * @param event The event to check
     * @param folder The folder where the event should appear in
     * @throws OXException If the check fails
     */
    public static void eventIsInFolder(Event event, UserizedFolder folder) throws OXException {
        if (PublicType.getInstance().equals(folder.getType())) {
            if (event.getPublicFolderId() != Integer.parseInt(folder.getID())) {
                throw OXException.general("Event folder != requested folder"); //TODO
            }
        } else {
            Attendee userAttendee = CalendarUtils.find(event.getAttendees(), folder.getCreatedBy());
            if (null == userAttendee || userAttendee.getFolderID() != Integer.parseInt(folder.getID())) {
                throw OXException.general("Event folder != requested folder"); //TODO
            }
        }
    }

    private Check() {
        super();
    }

}
