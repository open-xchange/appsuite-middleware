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

package com.openexchange.chronos.impl.groupware;

import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_SCHEDULING;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.SchedulingControl;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultCalendarParameters;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarEventNotificationService;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.downgrade.DowngradeEvent;
import com.openexchange.groupware.downgrade.DowngradeListener;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link CalendarDowngradeListener} - {@link DowngradeListener} for calendar data
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class CalendarDowngradeListener implements DowngradeListener {

	private final ServiceLookup services;
    private final CalendarUtilities calendarUtilities;
    private final CalendarEventNotificationService notificationService;

    /**
     * Initializes a new {@link CalendarDowngradeListener}.
     *
     * @param calendarUtilities A reference to the calendar utilities
     * @param calendarHandlers The {@link CalendarHandler}s to notify
     */
    public CalendarDowngradeListener(ServiceLookup services, CalendarUtilities calendarUtilities, CalendarEventNotificationService notificationService) {
        super();
        this.services = services;
        this.notificationService = notificationService;
        this.calendarUtilities = calendarUtilities;
    }

    @Override
    public void downgradePerformed(DowngradeEvent event) throws OXException {
        if (false == event.getNewUserConfiguration().hasCalendar()) {
            int userId = event.getNewUserConfiguration().getUserId();
            SimpleDBProvider dbProvider = new SimpleDBProvider(event.getReadCon(), event.getWriteCon());
            EntityResolver entityResolver = calendarUtilities.getEntityResolver(event.getContext().getContextId());
            CalendarStorage storage = Services.getService(CalendarStorageFactory.class).create(event.getContext(), Utils.ACCOUNT_ID, entityResolver, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
            StorageUpdater updater = new StorageUpdater(services, storage, entityResolver, notificationService, userId, event.getContext().getMailadmin());

            // Get all events which the user attends and that are *NOT* located in his private folders.
            CompositeSearchTerm term = new CompositeSearchTerm(CompositeOperation.AND)
                .addSearchTerm(CalendarUtils.getSearchTerm(AttendeeField.ENTITY, SingleOperation.EQUALS, Integer.valueOf(userId)))
                .addSearchTerm(CalendarUtils.getSearchTerm(AttendeeField.FOLDER_ID, SingleOperation.ISNULL));
            List<String> publicEvents = updater.searchEvents(term).stream().map(Event::getId).collect(Collectors.toList());

            // Get all events the user attends
            List<Event> events = updater.searchEvents();
            List<Event> eventsToRemoveTheAttendee = new LinkedList<>();
            for (final Event e : events) {
                if (publicEvents.contains(e.getId()) && false == CalendarUtils.isLastUserAttendee(e.getAttendees(), userId)) {
                    // Don't delete public events where other user still attend
                    eventsToRemoveTheAttendee.add(e);
                }
            }
            updater.removeAttendeeFrom(eventsToRemoveTheAttendee);
            events.removeAll(eventsToRemoveTheAttendee);

            // Delete all other events
            updater.deleteEvent(events, ServerSessionAdapter.valueOf(userId, event.getContext().getContextId()));

            // Trigger calendar events
            updater.notifyCalendarHandlers(event.getSession(), new DefaultCalendarParameters().set(PARAMETER_SCHEDULING, SchedulingControl.NONE));
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }

}
