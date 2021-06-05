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

package com.openexchange.caching.events;


/**
 * {@link Condition} - The condition for a cache event, which needs to be fulfilled in order to deliver that event. If not fulfilled, the cache event is discarded.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public interface Condition {

    /**
     * Checks whether a cache event is supposed to be delivered or not, waiting if necessary for fulfillment or non-fulfillment to become available.
     *
     * @return <code>true</code> if cache event is supposed to be delivered; otherwise <code>false</code>
     * @throws InterruptedException If interrupted while waiting
     * @see #peekShouldDeliver() for checking value w/o waiting
     */
    boolean shouldDeliver() throws InterruptedException;

    /**
     * Peeks the current value of this condition w/o waiting.
     *
     * @return <ul>
     *          <li><code>-1</code> if condition is currently neither fulfilled nor unfulfilled; nothing set yet</li>
     *          <li><code>0</code> if condition is currently unfulfilled</li>
     *          <li><code>1</code> if condition is currently fulfilled</li>
     *         </ul>
     */
    int peekShouldDeliver();

}
