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

package com.openexchange.chronos.impl.groupware;

import static com.openexchange.chronos.impl.groupware.ListenerHelper.equalsFieldUserTerm;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.downgrade.DowngradeEvent;
import com.openexchange.groupware.downgrade.DowngradeListener;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link ChronosDowngradeListener} - {@link DowngradeListener} for calendar data
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class ChronosDowngradeListener extends DowngradeListener {

    private static final int ACCOUNT_ID = CalendarAccount.DEFAULT_ACCOUNT.getAccountId();

    private CalendarStorageFactory factory;
    private CalendarUtilities      calendarUtilities;
    private FolderService          folderService;

    /**
     * Initializes a new {@link ChronosDeleteListener}.
     * 
     * @param factory The {@link CalendarStorageFactory}
     * @param calendarUtilities The {@link CalendarUtilities}
     * @param folderService The {@link FolderService}
     */
    public ChronosDowngradeListener(CalendarStorageFactory factory, CalendarUtilities calendarUtilities, FolderService folderService) {
        super();
        this.factory = factory;
        this.calendarUtilities = calendarUtilities;
        this.folderService = folderService;
    }

    @Override
    public void downgradePerformed(DowngradeEvent event) throws OXException {
        if (false == event.getNewUserConfiguration().hasCalendar()) {
            // Delete user data
            purgeUserData(new SimpleDBProvider(event.getReadCon(), event.getWriteCon()), event.getContext(), event.getNewUserConfiguration().getUserId());
        }
    }

    private void purgeUserData(SimpleDBProvider dbProvider, Context context, int userId) throws OXException {
        EntityResolver entityResolver = calendarUtilities.getEntityResolver(context.getContextId());
        CalendarStorage storage = factory.create(context, ACCOUNT_ID, entityResolver, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);

        EventField[] fields = new EventField[] { EventField.ID, EventField.FOLDER_ID };
        List<Event> events = storage.getEventStorage().searchEvents(equalsFieldUserTerm(AttendeeField.ENTITY, userId), null, fields);

        ServerSession session = ServerSessionAdapter.valueOf(userId, context.getContextId());

        for (Event event : events) {
            String eventId = event.getId();
            String folderId = CalendarUtils.getFolderView(event, userId);

            UserizedFolder folder = folderService.getFolder(FolderStorage.REAL_TREE_ID, folderId, session, null);
            List<Attendee> attendees = storage.getAttendeeStorage().loadAttendees(eventId);

            if (folder.isGlobalID() || CalendarUtils.isLastUserAttendee(attendees, userId)) {
                // Private or user is last attendee, so delete
                storage.getAlarmStorage().deleteAlarms(eventId);
                storage.getAlarmTriggerStorage().deleteTriggers(eventId);
                storage.getAttachmentStorage().deleteAttachments(session, folderId, eventId);
                storage.getAttendeeStorage().deleteAttendees(eventId);
                storage.getEventStorage().deleteEvent(eventId);
            } else {
                // Remove user from event
                storage.getAttendeeStorage().deleteAttendees(eventId, attendees.stream().filter(e -> e.getEntity() == userId).collect(Collectors.toList()));
                CalendarUser admin = entityResolver.prepareUserAttendee(context.getMailadmin());
                event.setLastModified(new Date());
                event.setModifiedBy(admin);
                storage.getEventStorage().updateEvent(event);
            }
        }

    }

    @Override
    public int getOrder() {
        return 1;
    }

}
