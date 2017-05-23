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

package com.openexchange.chronos.compat.attachments;

import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Utils.getCalendarUser;
import static com.openexchange.chronos.impl.Utils.getFields;
import static com.openexchange.chronos.impl.Utils.getFolder;
import static com.openexchange.chronos.impl.Utils.isClassifiedFor;
import static com.openexchange.chronos.impl.Utils.loadAdditionalEventData;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.READ_ALL_OBJECTS;
import static com.openexchange.folderstorage.Permission.READ_FOLDER;
import static com.openexchange.folderstorage.Permission.READ_OWN_OBJECTS;
import static com.openexchange.folderstorage.Permission.WRITE_ALL_OBJECTS;
import static com.openexchange.folderstorage.Permission.WRITE_OWN_OBJECTS;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.StorageOperation;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.attach.AttachmentAuthorization;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CalendarAttachmentAuthorization}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarAttachmentAuthorization implements AttachmentAuthorization {

    private final CalendarService calendarService;

    /**
     * Initializes a new {@link CalendarAttachmentAuthorization}.
     *
     * @param calendarService A reference to the calendar service
     */
    public CalendarAttachmentAuthorization(CalendarService calendarService) {
        super();
        this.calendarService = calendarService;
    }

    @Override
    public void checkMayAttach(ServerSession session, int folderId, int objectId) throws OXException {
        requireAttachmentPermissions(session, folderId, objectId, true);
    }

    @Override
    public void checkMayDetach(ServerSession session, int folderId, int objectId) throws OXException {
        requireAttachmentPermissions(session, folderId, objectId, true);
    }

    @Override
    public void checkMayReadAttachments(ServerSession session, int folderId, int objectId) throws OXException {
        requireAttachmentPermissions(session, folderId, objectId, false);
    }

    private void requireAttachmentPermissions(ServerSession serverSession, final int folderId, final int objectId, final boolean write) throws OXException {
        CalendarSession session = calendarService.init(serverSession);
        new StorageOperation<Void>(session) {

            @Override
            protected Void execute(CalendarSession session, CalendarStorage storage) throws OXException {
                UserizedFolder folder = getFolder(session, String.valueOf(folderId));
                checkAttachmentPermissions(session, storage, folder, String.valueOf(objectId), write);
                return null;
            }
        }.executeQuery();
    }

    private static void checkAttachmentPermissions(CalendarSession session, CalendarStorage storage, UserizedFolder folder, String objectId, boolean write) throws OXException {
        Event event = storage.getEventStorage().loadEvent(objectId, getFields(new EventField[0], (EventField[]) null));
        if (null == event) {
            throw CalendarExceptionCodes.EVENT_NOT_FOUND.create(objectId);
        }
        if (session.getUserId() != event.getCreatedBy()) {
            requireCalendarPermission(folder, READ_FOLDER, READ_ALL_OBJECTS, write ? WRITE_ALL_OBJECTS : NO_PERMISSIONS, NO_PERMISSIONS);
        } else {
            requireCalendarPermission(folder, READ_FOLDER, READ_OWN_OBJECTS, write ? WRITE_OWN_OBJECTS : NO_PERMISSIONS, NO_PERMISSIONS);
        }
        event = loadAdditionalEventData(storage, getCalendarUser(folder).getId(), event, new EventField[] { EventField.ATTENDEES });
        if (isClassifiedFor(event, session.getUserId())) {
            throw CalendarExceptionCodes.RESTRICTED_BY_CLASSIFICATION.create(folder.getID(), event.getId(), String.valueOf(event.getClassification()));
        }
    }

}
