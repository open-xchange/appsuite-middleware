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

package com.openexchange.chronos.scheduling;

import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.scheduling.changes.ChangeAction;
import com.openexchange.chronos.scheduling.changes.ScheduleChange;

/**
 * {@link ChangeNotification}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public interface ChangeNotification {

    /**
     * The {@link ChangeAction} that triggered the notification
     * 
     * @return The {@link ChangeAction}
     */
    ChangeAction getAction();

    /**
     * The originator of the scheduling event. The originator can be
     * <li> an attendee</li>
     * <li> the organizer</li>
     * of an event. An attendee becomes originator e.g. if he declines an event an thus triggers an scheduling event.
     * The organizer is the originator e.g. if he changes the start time of the event.
     * 
     * In case another user acts on behalf of an calendar user, this acting user should be set like described in
     * <a href="https://tools.ietf.org/html/rfc5545#section-3.8.4.3">RFC 5545, Section 3.8.4.3</a>
     * 
     * @return The originator of the message.
     */
    @NonNull
    CalendarUser getOriginator();

    /**
     * The recipient of the message. Can be either an attendee or the organizer
     * 
     * @return The recipient of the message.
     */
    @NonNull
    CalendarUser getRecipient();

    /**
     * Get a the {@link CalendarObjectResource}.
     * 
     * @return {@link CalendarObjectResource}
     */
    @NonNull
    CalendarObjectResource getResource();

    /**
     * Get the {@link ScheduleChange} of what changes has been performed.
     *
     * @return A {@link ScheduleChange}
     */
    @NonNull
    ScheduleChange getScheduleChange();

    /**
     * Gets the recipient-specific settings for the notification.
     * 
     * @return The recipient specific settings
     */
    RecipientSettings getRecipientSettings();

    /**
     * Get additional information.
     * 
     * @param key The key for the value
     * @param clazz The class the value has
     * @return The value casted to the given class or <code>null</code> if not found
     */
    @Nullable
    <T> T getAdditional(String key, Class<T> clazz);

}
