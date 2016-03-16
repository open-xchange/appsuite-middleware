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
        } catch (final ClassNotFoundException e) {
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
