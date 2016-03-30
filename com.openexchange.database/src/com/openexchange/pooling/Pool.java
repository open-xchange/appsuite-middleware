/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
