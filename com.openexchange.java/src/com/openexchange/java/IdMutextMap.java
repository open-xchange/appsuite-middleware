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

package com.openexchange.java;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link IdMutextMap} - Allows to synchronize based on a <code>java.lang.String</code> identifier. This allows a mutual exclusion lock
 * (mutex).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class IdMutextMap {

    /**
     * A mutex instance.
     */
    public static interface Mutex {
        // Empty interface
    }

    /**
     * The mutex provider.
     */
    public static final IdMutextMap MUTEXT_PROVIDER = new IdMutextMap();

    // ----------------------------------------------------------------------------------- //

    private final ConcurrentMap<String, MutexImpl> map;

    /**
     * Initializes a new {@link IdMutextMap}.
     */
    private IdMutextMap() {
        super();
        map = new ConcurrentHashMap<String, IdMutextMap.MutexImpl>(1024, 0.9f, 1);
    }

    /**
     * Gets the mutex for given identifier.
     *
     * @param id The identifier
     * @return The associated mutex
     */
    public Mutex getMutex(final String id) {
        if (null == id) {
            return null;
        }
        MutexImpl mutex = map.get(id);
        if (null == mutex) {
            final MutexImpl newMutex = new MutexImpl(id);
            mutex = map.putIfAbsent(id, newMutex);
            if (null == mutex) {
                mutex = newMutex;
            }
        }
        return mutex;
    }

    /**
     * Shrinks this mutex map by entries that elapsed given time-to-live
     *
     * @param ttl The time to live
     */
    public void shrink(final long ttl) {
        if (ttl <= 0) {
            return;
        }
        final ConcurrentMap<String, MutexImpl> map = this.map;
        final Runnable target = new Runnable() {

            @Override
            public void run() {
                try {
                    final long maxStamp = System.currentTimeMillis() - ttl;
                    for (final Iterator<MutexImpl> it = map.values().iterator(); it.hasNext();) {
                        final IdMutextMap.MutexImpl value = it.next();
                        if (value.stamp < maxStamp) {
                            it.remove();
                        }
                    }
                } catch (final Exception e) {
                    // Ignore
                }
            }
        };
        new Thread(target, IdMutextMap.class.getSimpleName()+"-Shrinker").start();
    }

    private static class MutexImpl implements Mutex {

        private final String id;
        private final int hash;
        final long stamp;

        MutexImpl(final String id) {
            super();
            this.id = id;
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            this.hash = result;
            stamp = System.currentTimeMillis();
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            final MutexImpl other = (MutexImpl) obj;
            if (id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!id.equals(other.id)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return id;
        }
    } // End of class MutexImpl

}
