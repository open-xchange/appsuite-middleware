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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.push.internal;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.push.PushManagerService;

/**
 * {@link PushManagerRegistry} - The push manager registry.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PushManagerRegistry {

    private static volatile PushManagerRegistry instance;

    /**
     * Initializes push manager registry.
     */
    public static void init() {
        if (null == instance) {
            synchronized (PushManagerRegistry.class) {
                if (null == instance) {
                    instance = new PushManagerRegistry();
                }
            }
        }
    }

    /**
     * Shuts down push manager registry.
     */
    public static void shutdown() {
        if (null != instance) {
            synchronized (PushManagerRegistry.class) {
                if (null != instance) {
                    instance = null;
                }
            }
        }
    }

    /**
     * Gets the push manager registry.
     *
     * @return The push manager registry
     */
    public static PushManagerRegistry getInstance() {
        return instance;
    }

    /*-
     * Member section
     */

    private final ConcurrentMap<Class<? extends PushManagerService>, PushManagerService> map;

    /**
     * Initializes a new {@link PushManagerRegistry}.
     */
    private PushManagerRegistry() {
        super();
        map = new ConcurrentHashMap<Class<? extends PushManagerService>, PushManagerService>();
    }

    /**
     * Adds specified push manager service.
     *
     * @param pushManager The push manager service to add
     * @return <code>true</code> if push manager service could be successfully added; otherwise <code>false</code>
     */
    public boolean addPushManager(final PushManagerService pushManager) {
        final Class<? extends PushManagerService> clazz = pushManager.getClass();
        if (map.containsKey(clazz)) {
            return false;
        }
        map.put(clazz, pushManager);
        return true;
    }

    /**
     * Removes specified push manager service.
     *
     * @param pushManager The push manager service to remove
     */
    public void removePushManager(final PushManagerService pushManager) {
        map.remove(pushManager.getClass());
    }

    /**
     * Gets a read-only {@link Iterator iterator} over the push managers in this registry.
     * <p>
     * Invoking {@link Iterator#remove() remove} will throw an {@link UnsupportedOperationException}.
     *
     * @return A read-only {@link Iterator iterator} over the push managers in this registry.
     */
    public Iterator<PushManagerService> getPushManagers() {
        return unmodifiableIterator(map.values().iterator());
    }

    /**
     * Strips the <tt>remove()</tt> functionality from an existing iterator.
     * <p>
     * Wraps the supplied iterator into a new one that will always throw an <tt>UnsupportedOperationException</tt> if its <tt>remove()</tt>
     * method is called.
     *
     * @param iterator The iterator to turn into an unmodifiable iterator.
     * @return An iterator with no remove functionality.
     */
    private static <T> Iterator<T> unmodifiableIterator(final Iterator<T> iterator) {
        if (iterator == null) {
            @SuppressWarnings("unchecked") final Iterator<T> empty = EMPTY_ITER;
            return empty;
        }

        return new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private static final Iterator EMPTY_ITER = new Iterator() {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            return null;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    };

}
