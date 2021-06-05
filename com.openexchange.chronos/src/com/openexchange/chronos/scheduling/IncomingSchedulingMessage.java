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

import java.util.Date;
import java.util.Optional;
import com.openexchange.annotation.NonNull;
import com.openexchange.chronos.CalendarObjectResource;

/**
 * {@link IncomingSchedulingMessage} - Object containing information about an external triggered update of an calendar resource
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public interface IncomingSchedulingMessage {

    /**
     * The {@link SchedulingMethod} to process
     * 
     * @return The {@link SchedulingMethod}
     */
    @NonNull
    SchedulingMethod getMethod();

    /**
     * Get the user identifier for whom to apply the change for
     *
     * @return The identifier of the target user.
     */
    int getTargetUser();

    /**
     * Get the object that triggered the scheduling
     *
     * @return The object
     */
    @NonNull
    IncomingSchedulingObject getSchedulingObject();

    /**
     * Get a the {@link CalendarObjectResource} as transmitted by the external
     * entity scheduling the change.
     * 
     * @return {@link CalendarObjectResource}
     */
    @NonNull
    CalendarObjectResource getResource();

    /**
     * The date when the change was created
     *
     * @return The date of the change
     */
    @NonNull
    Date getTimeStamp();

    /**
     * Get additional information.
     * 
     * @param key The key for the value
     * @param clazz The class the value has
     * @return An Optional holding the value casted to the given class
     * @param <T> The class of the returned object
     */
    <T> Optional<T> getAdditional(String key, Class<T> clazz);

}
