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

package com.openexchange.chronos.itip;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.itip.osgi.Services;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.FreeBusyService;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 *
 * {@link CalendarITipIntegrationUtility}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class CalendarITipIntegrationUtility implements ITipIntegrationUtility {

    private final ContextService contexts;

    public CalendarITipIntegrationUtility() {
        this.contexts = Services.getService(ContextService.class);
    }

    @Override
    public List<EventConflict> getConflicts(final Event event, final CalendarSession session) throws OXException {
        if (event == null) {
            return Collections.emptyList();
        }
        FreeBusyService freeBusyService = session.getFreeBusyService();
        List<EventConflict> conflicts = freeBusyService.checkForConflicts(session, event, event.getAttendees());
        return conflicts;
    }

    @Override
    public List<Event> getExceptions(final Event original, final CalendarSession session) throws OXException {
        CalendarStorage storage = getStorage(session);
        List<Event> exceptions = storage.getEventStorage().loadExceptions(original.getId(), null);
        for (Event exception : exceptions) {
            applyEventData(session, storage, exception);
        }
        return exceptions;
    }

    @Override
    public Event resolveUid(final String uid, final CalendarSession session) throws OXException {
        String id = session.getCalendarService().getUtilities().resolveByUID(session, uid, session.getUserId());
        if (id == null) {
            return null;
        }
        return load(session, id);
    }

    @Override
    public Event resolveUid(final String uid, RecurrenceId recurrenceId, final CalendarSession session) throws OXException {
        String id = session.getCalendarService().getUtilities().resolveByUID(session, uid, recurrenceId, session.getUserId());
        if (Strings.isEmpty(id)) {
            return null;
        }
        return load(session, id);
    }

    private Event load(final CalendarSession session, String id) throws OXException {
        CalendarStorage storage = getStorage(session);
        Event event = storage.getEventStorage().loadEvent(id, null);
        if (event != null) {
            applyEventData(session, storage, event);
        }
        return event;
    }

    private void applyEventData(final CalendarSession session, CalendarStorage storage, Event event) throws OXException {
        event.setAttendees(storage.getAttendeeStorage().loadAttendees(event.getId()));
        Attendee attendee = CalendarUtils.find(event.getAttendees(), session.getUserId());
        if (null != attendee) {
            event.setFolderId(attendee.getFolderId());
        }
        if (event.getFolderId() == null) {
            event.setFolderId(getFolderIdForUser(session.getSession(), event.getId(), session.getUserId()));
        }
        List<Attachment> attachments = storage.getAttachmentStorage().loadAttachments(event.getId());
        if (null != attachments && false == attachments.isEmpty()) {
            event.setAttachments(attachments);
        }
        event.setFlags(CalendarUtils.getFlags(event, session.getUserId()));
        event.setConferences(storage.getConferenceStorage().loadConferences(event.getId()));
    }

    @Override
    public Event loadEvent(final Event event, final CalendarSession session) throws OXException {
        return load(session, event.getId());
    }

    @Override
    public String getFolderIdForUser(Session session, String eventId, int userId) throws OXException {
        CalendarSession calendarSession = Services.getService(CalendarService.class).init(session);
        if (eventId == null) {
            return null;
        }

        Event loadEvent = getStorage(calendarSession).getEventStorage().loadEvent(eventId, null);
        if (loadEvent == null) {
            return getPrivateCalendarFolderId(session.getContextId(), userId);
        }
        loadEvent = getStorage(calendarSession).getUtilities().loadAdditionalEventData(userId, loadEvent, null);
        String retval = null;
        try {
            retval = CalendarUtils.getFolderView(loadEvent, userId);
        } catch (OXException e) {
            if (CalendarExceptionCodes.ATTENDEE_NOT_FOUND.equals(e)) {
                retval = getPrivateCalendarFolderId(calendarSession.getContextId(), calendarSession.getUserId());
            }
        }
        return retval;
    }

    @Override
    public void deleteEvent(final Event event, final CalendarSession session, final Date clientLastModified) throws OXException {
        session.getCalendarService().deleteEvent(session, new EventID(event.getFolderId(), event.getId(), event.getRecurrenceId()), clientLastModified.getTime());
    }

    @Override
    public String getPrivateCalendarFolderId(int cid, int userId) throws OXException {
        final Context ctx = contexts.getContext(cid);
        final OXFolderAccess acc = new OXFolderAccess(ctx);
        return String.valueOf(acc.getDefaultFolderID(userId, FolderObject.CALENDAR));
    }

    private CalendarStorage getStorage(CalendarSession session) throws OXException {
        CalendarStorageFactory storageFactory = Services.getService(CalendarStorageFactory.class);
        Context context = contexts.getContext(session.getSession().getContextId());
        return storageFactory.create(context, CalendarAccount.DEFAULT_ACCOUNT.getAccountId(), session.getEntityResolver());
    }

    @Override
    public boolean isActingOnBehalfOf(Event event, Session session) {
        if (null != event && null != session && null != event.getOrganizer() && null != event.getOrganizer().getSentBy()) {
            return event.getOrganizer().getSentBy().getEntity() == session.getUserId();
        }
        return false;
    }

    @Override
    public Type getFolderType(Event event, CalendarSession session) throws OXException {
        FolderService folderService = Services.getService(FolderService.class);
        Context context = contexts.getContext(session.getContextId());
        User user = Services.getService(UserService.class).getUser(session.getUserId(), context);
        UserizedFolder folder = folderService.getFolder(FolderStorage.REAL_TREE_ID, event.getFolderId(), user, context, null);
        return folder.getType();
    }

}
