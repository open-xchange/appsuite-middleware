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

package com.openexchange.groupware.delete;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.cache.CacheFolderStorageDeleteFinishedListener;
import com.openexchange.java.ConcurrentHashSet;
import com.openexchange.tools.oxfolder.OXFolderDeleteFinishedListener;

/**
 * {@link DeleteFinishedListenerRegistry}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DeleteFinishedListenerRegistry {

    /**
     * The singleton instance.
     */
    private static volatile DeleteFinishedListenerRegistry instance;

    /**
     * Initializes singleton instance.
     */
    static void initInstance() {
        instance = new DeleteFinishedListenerRegistry();
    }

    /**
     * Releases singleton instance.
     */
    static void releaseInstance() {
        instance.dispose();
        instance = null;
    }

    /**
     * Gets the singleton instance of {@link DeleteFinishedListenerRegistry}.
     *
     * @return The singleton instance of {@link DeleteFinishedListenerRegistry}.
     */
    public static DeleteFinishedListenerRegistry getInstance() {
        if (null == instance) {
            initInstance();
        }
        return instance;
    }

    /**
     * The class-set to detect duplicate listeners.
     */
    private final ConcurrentHashSet<Class<? extends DeleteFinishedListener>> classes;

    /**
     * The list of static listeners.
     */
    private final List<DeleteFinishedListener> staticListeners;

    /**
     * The listener queue for dynamically added listeners.
     */
    private final List<DeleteFinishedListener> listeners;

    /**
     * Initialises a new {@link DeleteFinishedListenerRegistry}.
     */
    public DeleteFinishedListenerRegistry() {
        super();
        listeners = new CopyOnWriteArrayList<>();
        classes = new ConcurrentHashSet<Class<? extends DeleteFinishedListener>>();
        DeleteFinishedListener[] tmpListeners = getStaticListeners();
        for (DeleteFinishedListener deleteListener : tmpListeners) {
            classes.add(deleteListener.getClass());
        }
        this.staticListeners = new CopyOnWriteArrayList<>(tmpListeners);
    }

    /**
     * Registers an instance of <code>{@link DeleteFinishedListener}</code>.
     * <p>
     * <b>Note</b>: Only one instance of a certain <code>{@link DeleteFinishedListener}</code> implementation is added, meaning if you try to
     * register a certain implementation twice, the latter one is going to be discarded and <code>false</code> is returned.
     *
     * @param listener The listener to register
     * @return <code>true</code> if specified delete listener has been added to registry; otherwise <code>false</code>
     */
    public boolean registerDeleteListener(DeleteFinishedListener listener) {
        if (classes.addIfAbsent(listener.getClass())) {
            return listeners.add(listener);
        }
        return false;
    }

    /**
     * Removes given instance of <code>{@link DeleteFinishedListener}</code> from this registry's known listeners.
     *
     * @param listener The listener to remove
     */
    public void unregisterDeleteListener(DeleteFinishedListener listener) {
        if (classes.remove(listener.getClass())) {
            listeners.remove(listener);
        }
    }

    /**
     * Fires the delete event.
     *
     * @param deleteEvent The delete event
     * @throws OXException If delete event could not be performed
     */
    public void fireDeleteEvent(DeleteEvent deleteEvent) throws OXException {
        // At first trigger dynamically added listeners
        for (DeleteFinishedListener listener : listeners) {
            listener.deleteFinished(deleteEvent);
        }
        // Now trigger static listeners
        for (DeleteFinishedListener listener : staticListeners) {
            listener.deleteFinished(deleteEvent);
        }
    }

    /**
     * Initializes this delete registry; static {@link DeleteFinishedListener listeners} are added.
     */
    private DeleteFinishedListener[] getStaticListeners() {
        return new DeleteFinishedListener[] {
            new CacheFolderStorageDeleteFinishedListener(),
            new OXFolderDeleteFinishedListener(),
        };
    }

    /**
     * Disposes this eviction registry.
     */
    private void dispose() {
        staticListeners.clear();
        classes.clear();
        listeners.clear();
    }
}
