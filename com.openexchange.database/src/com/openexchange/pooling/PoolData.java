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

package com.openexchange.pooling;

import java.util.Collection;
import java.util.Iterator;

/**
 * {@link PoolData}
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a> - extraction
 * @param <T> The class of the {@link PooledData}
 * @since v7.10.1
 */
public interface PoolData<T> {

    /**
     * @return the topmost idle object or <code>null</code> if the idle stack is
     *         empty.
     */
    PooledData<T> popIdle();

    /**
     * Adds a new active object.
     * 
     * @param newActive new active object to add.
     */
    void addActive(final PooledData<T> newActive);

    PooledData<T> getActive(final T pooled);

    Iterator<PooledData<T>> listActive();

    /**
     * Removes an active object.
     * 
     * @param toRemove object to remove.
     * @return <code>true</code> if the object was removed successfully.
     */
    boolean removeActive(final PooledData<T> toRemove);

    void addCreating();

    void removeCreating();

    int getCreating();

    /**
     * @return the number of active objects.
     */
    int numActive();

    boolean isActiveEmpty();

    void addIdle(final PooledData<T> newIdle);

    PooledData<T> getIdle(final int index);

    void removeIdle(final int index);

    /**
     * @return the number of idle objects.
     */
    int numIdle();

    boolean isIdleEmpty();

    void addByThread(final PooledData<T> toCheck);

    PooledData<T> getByThread(final Thread thread);

    void removeByThread(final PooledData<T> toCheck);

    Collection<PooledData<T>> getActive();
}
