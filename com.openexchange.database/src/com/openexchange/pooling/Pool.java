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

/**
 * Interface for pooling of objects.
 * @param <T> type of object that are pooled.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public interface Pool<T> {

    /**
     * Gives a pooled object from the pool. Insure that the pooled object is
     * returned to the pool after use with the {@link #back(Object)} method.
     * @return a pooled object for usage.
     * @throws PoolingException if the pool is exhausted.
     */
    T get() throws PoolingException;

    /**
     * Returns a pooled object back to the pool.
     * @param pooled object to return.
     * @throws PoolingException if the returned object does not belong to this pool or the given object is <code>null</code>.
     */
    void back(T pooled) throws PoolingException;

    /**
     * Destroys the pool.
     */
    void destroy();

    /**
     * @return <code>true</code> if this pool doesn't contain any objects.
     */
    boolean isEmpty();

    /**
     * @return the number of idle objects.
     */
    int getNumIdle();

    /**
     * @return the number of active objects.
     */
    int getNumActive();

    /**
     * @return the sum of active and idle objects.
     */
    int getPoolSize();

    /**
     * @return the longest time an object has been used.
     */
    long getMaxUseTime();

    /**
     * Resets the maximum use time.
     */
    void resetMaxUseTime();

    /**
     * @return the minimal use time of an object.
     */
    long getMinUseTime();

    /**
     * Resets the minimum use time.
     */
    void resetMinUseTime();

    /**
     * Returns the number of objects that are not useable anymore. This method
     * counts the objects for that the methods
     * {@link PoolableLifecycle#activate(PooledData)},
     * {@link PoolableLifecycle#deactivate(PooledData)} or
     * {@link PoolableLifecycle#validate(PooledData)} return <code>false</code>.
     * @return the number of objects that are not useable anymore.
     */
    int getNumBroken();
}
