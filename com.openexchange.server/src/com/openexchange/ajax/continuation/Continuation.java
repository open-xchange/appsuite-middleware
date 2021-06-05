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

package com.openexchange.ajax.continuation;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;

/**
 * {@link Continuation} - Represents a continuing/background AJAX request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Continuation<V> {

    /**
     * Gets the UUID.
     *
     * @return The UUID
     */
    UUID getUuid();

    /**
     * Gets the format of this continuation's results.
     *
     * @return The format
     */
    String getFormat();

    /**
     * Gets the next available value.
     *
     * @param time The maximum time to wait
     * @param unit The time unit of the {@code time} argument
     * @return The next available value or <code>null</code>
     * @throws OXException If awaiting next available response fails
     * @throws InterruptedException If the current thread is interrupted
     */
    ContinuationResponse<V> getNextResponse(long time, TimeUnit unit) throws OXException, InterruptedException;

    /**
     * Gets the next available value.
     *
     * @param time The maximum time to wait
     * @param unit The time unit of the {@code time} argument
     * @param defaultValue The default response to return if no next value was available in given time span
     * @return The next available value or given <code>defaultValue</code>
     * @throws OXException If awaiting next available response fails
     * @throws InterruptedException If the current thread is interrupted
     */
    ContinuationResponse<V> getNextResponse(long time, TimeUnit unit, V defaultResponse) throws OXException, InterruptedException;

}
