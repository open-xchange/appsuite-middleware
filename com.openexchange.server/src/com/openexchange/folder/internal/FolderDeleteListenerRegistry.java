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

package com.openexchange.folder.internal;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.folder.FolderDeleteListenerService;

/**
 * {@link FolderDeleteListenerRegistry} - A registry for {@link FolderDeleteListenerService folder delete listeners}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderDeleteListenerRegistry {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FolderDeleteListenerRegistry.class);

    private static volatile FolderDeleteListenerRegistry instance;

    /**
     * Initializes the registry instance.
     */
    static void initInstance() {
        instance = new FolderDeleteListenerRegistry();
    }

    /**
     * Releases the registry instance.
     */
    static void releaseInstance() {
        instance = null;
    }

    /**
     * Gets the registry instance.
     *
     * @return The registry instance
     */
    public static FolderDeleteListenerRegistry getInstance() {
        return instance;
    }

    /*-
     * ++++++++++++++++++++++++++++++++++++++++ MEMBER SECTION ++++++++++++++++++++++++++++++++++++++++
     */

    private final ConcurrentMap<Class<? extends FolderDeleteListenerService>, FolderDeleteListenerService> map;

    /**
     * Initializes a new {@link FolderDeleteListenerRegistry}.
     */
    private FolderDeleteListenerRegistry() {
        super();
        map = new ConcurrentHashMap<Class<? extends FolderDeleteListenerService>, FolderDeleteListenerService>();
    }

    /**
     * Checks if a delete listener of given class is contained in this registry.
     *
     * @param clazz The name of the delete listener's type
     * @return <code>true</code> if delete listener is contained; otherwise <code>false</code>
     */
    public boolean containsByClassName(final String clazz) {
        try {
            return (map.containsKey(Class.forName(clazz).asSubclass(FolderDeleteListenerService.class)));
        } catch (ClassNotFoundException e) {
            LOG.error("", e);
            return false;
        }
    }

    /**
     * Checks if specified delete listener is contained in this registry.
     *
     * @param deleteListenerService The delete listener to check
     * @return <code>true</code> if delete listener is contained; otherwise <code>false</code>
     */
    public boolean containsDeleteListenerService(final FolderDeleteListenerService deleteListenerService) {
        return (map.containsKey(deleteListenerService.getClass()));
    }

    /**
     * Adds specified delete listener to this registry.
     *
     * @param deleteListenerService The delete listener to add
     * @return <code>true</code> if delete listener is successfully added to registry; otherwise <code>false</code>
     */
    public boolean addDeleteListenerService(final FolderDeleteListenerService deleteListenerService) {
        return (null == map.putIfAbsent(deleteListenerService.getClass(), deleteListenerService));
    }

    /**
     * Removes specified delete listener from this registry.
     *
     * @param deleteListenerService The delete listener to remove
     */
    public void removeDeleteListenerService(final FolderDeleteListenerService deleteListenerService) {
        map.remove(deleteListenerService.getClass());
    }

    /**
     * Gets an unmodifiable {@link Iterator iterator} for the delete listeners contained in this registry.
     *
     * @return An unmodifiable {@link Iterator iterator}.
     */
    public Iterator<FolderDeleteListenerService> getDeleteListenerServices() {
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
            throw new NullPointerException();
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
}
