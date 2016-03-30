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
     * @return <code>true</code> if the object didn't perish.
     */
    boolean activate(PooledData<T> data);

    /**
     * This method checks if the pooled object is still useable.
     * @param data data of the pooled object and the pooled object.
     * @return <code>true</code> if the pooled object is still valid.
     */
    boolean validate(PooledData<T> data);

    /**
     * @return a name for the pooled objects that can be used to generate more understandable messages for administrators. E.g.
     *         "Database connection".
     */
    String getObjectName();

}
