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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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



package com.openexchange.cache.dynamic.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.locks.Lock;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

import com.openexchange.cache.OXCachingException;
import com.openexchange.cache.dynamic.OXNoRefresh;
import com.openexchange.groupware.AbstractOXException;

public class CachedObjectInvocationHandler<T> implements InvocationHandler {

    private static final Method EQUALS;

    private final Object key;
    private T cached;
    private final OXObjectFactory<T> factory;
    private final JCS cache;

    public CachedObjectInvocationHandler(final OXObjectFactory<T> factory,
        final JCS cache) {
        key = factory.getKey();
        this.factory = factory;
        this.cache = cache;
    }

    /**
     * {@inheritDoc}
     */
    public final Object invoke(final Object object, final Method method,
        final Object[] arguments) throws Throwable {
        if (!method.isAnnotationPresent(OXNoRefresh.class) || null == cached) {
            refresh();
        }
        if (EQUALS.equals(method)) {
            final Object other = arguments[0];
            return Boolean.valueOf(other.equals(cached));
        }
        method.setAccessible(true);
        return method.invoke(cached, arguments);
    }

    /**
     * Checks if the object was removed from the cache and must be reloaded from
     * the database.
     * @throws AbstractOXException if loading or putting into cache fails.
     */
    private final void refresh() throws AbstractOXException {
        final Lock lock = factory.getCacheLock();
        lock.lock();
        try {
            cached = (T) cache.get(key);
            if (null == cached) {
                cached = factory.load();
                try {
                    cache.putSafe(key, cached);
                } catch (CacheException e) {
                    throw new OXCachingException(OXCachingException.Code
                        .FAILED_PUT, e);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    static {
        try {
            EQUALS = Object.class.getMethod("equals",
                new Class[] { Object.class });
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
