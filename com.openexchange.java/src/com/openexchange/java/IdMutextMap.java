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
                } catch (Exception e) {
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
            if (!(obj instanceof MutexImpl)) {
                return false;
            }
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
