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

package com.openexchange.pns;

/**
 * {@link PushPriority} - The priority with which a submitted notification is handled. The higher, the faster a notification is processed.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public enum PushPriority {

    /**
     * The (default) "high" priority, which reduces the delays in buffers to a minimum.
     */
    HIGH(1),
    /**
     * The "medium" priority, which allows push notifications to be slightly delayed in buffering queues.
     */
    MEDIUM(2),
    /**
     * The "low" priority, which allows push notifications to be slightly more delayed in buffering queues.
     */
    LOW(4)
    ;

    private final int factor;

    /**
     * Initializes a new {@link PushPriority}.
     *
     * @param factor The factor to apply to the default buffering delays
     */
    private PushPriority(int factor) {
        this.factor = factor;
    }

    /**
     * Gets the delay, adjusted by the individual factor of this push priority.
     *
     * @param baseDelay The base buffering delay
     * @return The (possibly adjusted) buffering delay
     */
    public long getDelay(long baseDelay) {
        return factor * baseDelay;
    }

}
