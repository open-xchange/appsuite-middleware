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

package com.openexchange.chronos.service;

import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Conference;
import com.openexchange.chronos.ConferenceField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.groupware.tools.mappings.common.CollectionUpdate;
import com.openexchange.groupware.tools.mappings.common.ItemUpdate;
import com.openexchange.groupware.tools.mappings.common.SimpleCollectionUpdate;

/**
 * {@link EventUpdate}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface EventUpdate extends ItemUpdate<Event, EventField> {

    /**
     * Gets the attendee-related changes between original and updated event.
     *
     * @return The attendee updates, or an empty collection update if there were no attendee-related changes
     */
    CollectionUpdate<Attendee, AttendeeField> getAttendeeUpdates();

    /**
     * Gets the conference-related changes between original and updated event.
     *
     * @return The conference updates, or an empty collection update if there were no conference-related changes
     */
    CollectionUpdate<Conference, ConferenceField> getConferenceUpdates();

    /**
     * Gets the alarm-related changes between original and updated event. Only alarms of the actual calendar user are considered.
     *
     * @return The alarm updates, or an empty collection update if there were no alarm-related changes
     */
    CollectionUpdate<Alarm, AlarmField> getAlarmUpdates();

    /**
     * Gets the attachment-related changes between original and updated event.
     *
     * @return The attachment updates, or an empty collection update if there were no attachment-related changes
     */
    SimpleCollectionUpdate<Attachment> getAttachmentUpdates();

}
