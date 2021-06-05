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
 * Interface for managing the life cyle of pooled objects.
 * @param <T> type of pooled objects,
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public interface PoolableLifecycle<T> {

    /**
     * Creates a new object for pooling.
     * @return a poolable object.
     * @throws Exception if creation of the object fails.
     */
    T create() throws Exception;

    /**
     * Destroys a poolable object after it reached its end of lifecycle. The
     * object to destroy may be damaged and not working properly. So be careful
     * when destroying it.
     * @param obj object to destroy.
     */
    void destroy(T obj);

    /**
     * Deactivates an object before putting it into the pool. Depending on pool
     * implementation this method will not be called if validate on returning
     * objects is activated and the
     * {@link PoolableLifecycle#validate(PooledData)} method returns
     * <code>true</code>.
     * @param data data of the object and the pooled object.
     * @return <code>true</code> if the returned object should be placed in pool
     * for reuse.
     */
    boolean deactivate(PooledData<T> data);

    /**
     * Checks if the object can be used after keeping in pool. Maybe the pool is
     * used for objects that perish then the useable state of object must be
     * verified. This method is only called if the pool implementation checks
     * objects before they are lent out to the client.
     * @param data data of the pooled object and the pooled object.
     * @param forceValidityCheck Whether connection's validity is explicitly checked
     * @return <code>true</code> if the object didn't perish.
     */
    boolean activate(PooledData<T> data, boolean forceValidityCheck);

    /**
     * This method checks if the pooled object is still useable.
     * @param data data of the pooled object and the pooled object.
     * @param onActivate Whether this methods is called on activate or on deactivate
     * @return <code>true</code> if the pooled object is still valid.
     */
    boolean validate(PooledData<T> data, boolean onActivate);

    /**
     * @return a name for the pooled objects that can be used to generate more understandable messages for administrators. E.g.
     *         "Database connection".
     */
    String getObjectName();

}
