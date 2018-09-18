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

package com.openexchange.chronos.itip;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
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
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;

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
        String id = session.getCalendarService().getUtilities().resolveByUID(session, uid);
        if (id == null) {
            return null;
        }
        return load(session, id);
    }

    private Event load(final CalendarSession session, String id) throws OXException {
        CalendarStorage storage = getStorage(session);
        Event event = storage.getEventStorage().loadEvent(id, null);
        applyEventData(session, storage, event);
        return event;
    }

    private void applyEventData(final CalendarSession session, CalendarStorage storage, Event event) throws OXException {
        event.setAttendees(storage.getAttendeeStorage().loadAttendees(event.getId()));
        for (Attendee attendee : event.getAttendees()) {
            if (attendee.getEntity() == session.getUserId()) {
                event.setFolderId(attendee.getFolderId());
            }
        }
        if (event.getFolderId() == null) {
            event.setFolderId(getFolderIdForUser(session.getSession(), event.getId(), session.getUserId()));
        }
        List<Attachment> attachments = storage.getAttachmentStorage().loadAttachments(event.getId());
        if (null != attachments && false == attachments.isEmpty()) {
            event.setAttachments(attachments);
        }
        event.setFlags(CalendarUtils.getFlags(event, session.getUserId()));
    }

    @Override
    public Event loadEvent(final Event event, final CalendarSession session) throws OXException {
        return load(session, event.getId());
    }

    @Override
    public void createEvent(final Event event, final CalendarSession session) throws OXException {
        session.getCalendarService().createEvent(session, event.getFolderId(), event);
    }

    @Override
    public void updateEvent(final Event event, final CalendarSession session, final Date clientLastModified) throws OXException {
        Event loadEvent = getStorage(session).getEventStorage().loadEvent(event.getId(), null);
        loadEvent = getStorage(session).getUtilities().loadAdditionalEventData(session.getUserId(), loadEvent, null);
        String folder = CalendarUtils.getFolderView(loadEvent, session.getUserId());
        session.getCalendarService().updateEvent(session, new EventID(folder, event.getId()), event, clientLastModified.getTime());
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
    public void changeConfirmationForExternalParticipant(Event event, ConfirmationChange change, CalendarSession session) throws OXException {
        Attendee external = null;
        if (event.getAttendees() == null) {
            event = getStorage(session).getUtilities().loadAdditionalEventData(session.getUserId(), event, null);
        }
        for (Attendee attendee : event.getAttendees()) {
            if (change.getIdentifier().equals(attendee.getEMail())) {
                external = attendee;
                break;
            }
        }

        if (external == null) {
            return;
        }

        external.setComment(change.getNewMessage());
        external.setPartStat(change.getNewStatus());

        session.getCalendarService().updateAttendee(session, new EventID(event.getFolderId(), event.getId()), external, null, event.getLastModified().getTime());
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

}
