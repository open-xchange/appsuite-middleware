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

package com.openexchange.chronos.compat.impl.attachments;

import static com.openexchange.chronos.common.CalendarUtils.isAttendeeSchedulingResource;
import static com.openexchange.chronos.common.CalendarUtils.isClassifiedFor;
import static com.openexchange.chronos.common.CalendarUtils.matches;
import static com.openexchange.osgi.Tools.requireService;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.storage.AttachmentStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.attach.AttachmentAuthorization;
import com.openexchange.groupware.attach.AttachmentEvent;
import com.openexchange.groupware.attach.AttachmentListener;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CalendarAttachmentHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarAttachmentHandler implements AttachmentAuthorization, AttachmentListener {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link CalendarAttachmentHandler}.
     *
     * @param serivces A service lookup reference
     */
    public CalendarAttachmentHandler(ServiceLookup serivces) {
        super();
        this.services = serivces;
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

    @Override
    public long attached(AttachmentEvent e) throws Exception {
        return touch(e);
    }

    @Override
    public long detached(AttachmentEvent e) throws Exception {
        return touch(e);
    }

    private long touch(AttachmentEvent event) throws OXException {
        if (isManagedInternally(event.getSession())) {
            return -1;
        }
        EventID eventID = new EventID(String.valueOf(event.getFolderId()), String.valueOf(event.getAttachedId()));
        CalendarSession calendarSession = requireService(CalendarService.class, services).init(event.getSession());
        CalendarResult result = calendarSession.getCalendarService().touchEvent(calendarSession, eventID);
        if (0 < result.getUpdates().size()) {
            return result.getUpdates().get(0).getUpdate().getTimestamp();
        }
        return -1;
    }

    private void requireAttachmentPermissions(ServerSession serverSession, int folderId, int objectId, final boolean write) throws OXException {
        if (isManagedInternally(serverSession)) {
            return;
        }
        /*
         * get event & check read access implicitly
         */
        CalendarSession calendarSession = requireService(CalendarService.class, services).init(serverSession);
        EventID eventId = new EventID(String.valueOf(folderId), String.valueOf(objectId));
        Event event = calendarSession.getCalendarService().getEvent(calendarSession, eventId.getFolderID(), eventId);
        if (isClassifiedFor(event, serverSession.getUserId())) {
            throw CalendarExceptionCodes.RESTRICTED_BY_CLASSIFICATION.create(String.valueOf(folderId), event.getId(), String.valueOf(event.getClassification()));
        }
        /*
         * if write access is needed, check permissions for parent folder & require organizer role
         */
        if (write) {
            UserizedFolder folder = requireService(FolderService.class, services).getFolder(FolderStorage.REAL_TREE_ID, String.valueOf(folderId), serverSession, null);
            int requiredWritePermission = matches(event.getCreatedBy(), serverSession.getUserId()) ? Permission.WRITE_OWN_OBJECTS : Permission.WRITE_ALL_OBJECTS;
            if (folder.getOwnPermission().getWritePermission() < requiredWritePermission) {
                throw CalendarExceptionCodes.NO_WRITE_PERMISSION.create(folder.getID());
            }
            int calendarUserId = SharedType.getInstance().equals(folder.getType()) ? folder.getCreatedBy() : serverSession.getUserId();
            if (isAttendeeSchedulingResource(event, calendarUserId)) {
                throw CalendarExceptionCodes.NOT_ORGANIZER.create(folder.getID(), event.getId(), event.getOrganizer().getUri(), event.getOrganizer().getSentBy());
            }
        }
    }

    private static boolean isManagedInternally(Session session) {
        return null != session && Boolean.TRUE.equals(session.getParameter(AttachmentStorage.class.getName()));
    }

}
