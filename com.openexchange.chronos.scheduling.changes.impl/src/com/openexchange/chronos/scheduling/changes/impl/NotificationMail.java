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

package com.openexchange.chronos.scheduling.changes.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Conference;
import com.openexchange.chronos.Event;

/**
 * {@link NotificationMail}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class NotificationMail {

    private final Event event;
    private final List<NotificationParticipant> participants;
    private final List<NotificationParticipant> resources;
    private final List<NotificationConference> conferences;

    public NotificationMail(Event event, List<Attendee> participants, List<Attendee> resources, List<Conference> conferences) {
        super();
        this.event = event;
        this.participants = getParticipants(participants);
        this.resources = getParticipants(resources);
        this.conferences = getConferences(conferences);
    }

    public Event getEvent() {
        return event;
    }

    public List<NotificationParticipant> getParticipants() {
        return participants;
    }

    public List<NotificationParticipant> getResources() {
        return resources;
    }

    public List<NotificationConference> getConferences() {
        return conferences;
    }

    private static final List<NotificationParticipant> getParticipants(List<Attendee> attendees) {
        if (null == attendees) {
            return Collections.emptyList();
        }
        List<NotificationParticipant> participants = new ArrayList<NotificationParticipant>(attendees.size());
        for (Attendee attendee : attendees) {
            participants.add(new NotificationParticipant(attendee));
        }
        return participants;
    }

    private static final List<NotificationConference> getConferences(List<Conference> conferences) {
        if (null == conferences) {
            return Collections.emptyList();
        }
        List<NotificationConference> notificationConferences = new ArrayList<NotificationConference>(conferences.size());
        for (Conference conference : conferences) {
            notificationConferences.add(new NotificationConference(conference));
        }
        return notificationConferences;
    }

}
