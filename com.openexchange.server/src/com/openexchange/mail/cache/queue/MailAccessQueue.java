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

package com.openexchange.mail.cache.queue;

import java.util.Queue;
import com.openexchange.mail.cache.PooledMailAccess;

/**
 * {@link MailAccessQueue} - A {@link Queue} additionally providing {@link #pollDelayed()} method to obtain expired elements.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MailAccessQueue extends Queue<PooledMailAccess> {

    /**
     * Gets the capacity.
     * 
     * @return The capacity
     */
    int getCapacity();

    /**
     * Marks this queue as deprecated.
     */
    void markDeprecated();

    /**
     * Checks if this queue is marked as deprecated.
     *
     * @return <code>true</code> if this queue is marked as deprecated; otherwise <code>false</code>
     */
    boolean isDeprecated();

    /**
     * Retrieves and removes the head of this queue, or <tt>null</tt> if head has not expired, yet.
     *
     * @return The head of this queue or <tt>null</tt> if head has not expired, yet.
     */
    public PooledMailAccess pollDelayed();

}
