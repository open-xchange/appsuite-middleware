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

import static com.openexchange.chronos.impl.groupware.ListenerUtils.equalsFieldUserTerm;
import static com.openexchange.chronos.impl.groupware.ListenerUtils.getAttendeeFolders;
import static com.openexchange.chronos.impl.groupware.ListenerUtils.getUser;
import java.util.Date;
import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.downgrade.DowngradeEvent;
import com.openexchange.groupware.downgrade.DowngradeListener;
import com.openexchange.osgi.ServiceSet;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link ChronosDowngradeListener} - {@link DowngradeListener} for calendar data
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class ChronosDowngradeListener extends DowngradeListener {

    private CalendarStorageFactory      factory;
    private CalendarUtilities           calendarUtilities;
    private ServiceSet<CalendarHandler> calendarHandlers;

    /**
     * Initializes a new {@link ChronosDeleteListener}.
     * 
     * @param factory The {@link CalendarStorageFactory}
     * @param calendarUtilities The {@link CalendarUtilities}
     * @param calendarHandlers The {@link CalendarHandler}s to notify
     */
    public ChronosDowngradeListener(CalendarStorageFactory factory, CalendarUtilities calendarUtilities, ServiceSet<CalendarHandler> calendarHandlers) {
        super();
        this.factory = factory;
        this.calendarUtilities = calendarUtilities;
        this.calendarHandlers = calendarHandlers;
    }

    @Override
    public void downgradePerformed(DowngradeEvent event) throws OXException {
        if (false == event.getNewUserConfiguration().hasCalendar()) {
            // Delete data
            purgeData(new SimpleDBProvider(event.getReadCon(), event.getWriteCon()), event.getContext(), event.getNewUserConfiguration().getUserId(), event.getSession());
        }
    }

    private void purgeData(SimpleDBProvider dbProvider, Context context, int userId, Session adminSession) throws OXException {
        EntityResolver entityResolver = calendarUtilities.getEntityResolver(context.getContextId());
        CalendarStorage storage = factory.create(context, CalendarAccount.DEFAULT_ACCOUNT.getAccountId(), entityResolver, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
        SimpleResultTracker tracker = new SimpleResultTracker(calendarHandlers);

        EventField[] fields = new EventField[] { EventField.ID, EventField.FOLDER_ID };
        List<Event> events = storage.getEventStorage().searchEvents(equalsFieldUserTerm(AttendeeField.ENTITY, userId), null, fields);

        ServerSession serverSession = ServerSessionAdapter.valueOf(userId, context.getContextId());
        Date date = new Date();

        for (Event event : events) {
            String eventId = event.getId();
            List<Attendee> attendees = storage.getAttendeeStorage().loadAttendees(eventId);
            event.setAttendees(attendees);

            if (isPrivate(event) || CalendarUtils.isLastUserAttendee(attendees, userId)) {
                // Private or user is last attendee, so delete
                storage.getAlarmStorage().deleteAlarms(eventId);
                storage.getAlarmTriggerStorage().deleteTriggers(eventId);
                storage.getAttachmentStorage().deleteAttachments(serverSession, CalendarUtils.getFolderView(event, userId), eventId);
                storage.getAttendeeStorage().deleteAttendees(eventId);
                storage.getEventStorage().deleteEvent(eventId);
                tracker.addDelete(getAttendeeFolders(attendees), event, date.getTime());

            } else {
                // Remove user from event
                storage.getAttendeeStorage().deleteAttendees(eventId, getUser(userId, attendees));
                Event originalEvent = calendarUtilities.copyEvent(event, null);
                CalendarUser admin = entityResolver.prepareUserAttendee(context.getMailadmin());
                event.setLastModified(date);
                event.setModifiedBy(admin);
                event.setTimestamp(date.getTime());
                storage.getEventStorage().updateEvent(event);
                tracker.addUpdate(getAttendeeFolders(attendees), originalEvent, event);
            }
        }
    }

    private boolean isPrivate(Event event) {
        if (null != event.getFolderId()) {
            // Only public and not group-sheduled events have folder ID in event
            return false;
        }
        return true;
    }

    @Override
    public int getOrder() {
        return 1;
    }

}
