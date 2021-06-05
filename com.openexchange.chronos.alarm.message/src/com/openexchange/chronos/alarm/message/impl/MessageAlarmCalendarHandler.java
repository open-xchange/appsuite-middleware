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

package com.openexchange.chronos.alarm.message.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.CalendarEvent;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.UpdateResult;

/**
 * {@link MessageAlarmCalendarHandler}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class MessageAlarmCalendarHandler implements CalendarHandler {

    private final MessageAlarmDeliveryWorker worker;

    /**
     * Initializes a new {@link MessageAlarmCalendarHandler}.
     * 
     * @param worker The worker
     */
    public MessageAlarmCalendarHandler(MessageAlarmDeliveryWorker worker) {
        this.worker = worker;
    }

    @Override
    public void handle(CalendarEvent event) {
        // Check deleted events and remove the according tasks
        HashSet<String> eventsToCancel = new HashSet<>();
        for(DeleteResult deletion: event.getDeletions()) {
            eventsToCancel.add(deletion.getEventID().getObjectID());
        }
        worker.cancelAll(event.getContextId(), event.getAccountId(), eventsToCancel);
        // Check if an updated events has tasks and if so load and check the appropriate alarm data
        List<Event> eventsToCheck = new ArrayList<>();
        for(UpdateResult updateResult : event.getUpdates()) {
            eventsToCheck.add(updateResult.getOriginal());
        }
        for(CreateResult createResult : event.getCreations()) {
            eventsToCheck.add(createResult.getCreatedEvent());
        }

        worker.checkAndScheduleTasksForEvents(eventsToCheck, event.getContextId(), event.getAccountId());
    }

}
