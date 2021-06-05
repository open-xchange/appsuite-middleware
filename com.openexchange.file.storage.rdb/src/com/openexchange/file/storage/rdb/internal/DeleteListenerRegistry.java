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

package com.openexchange.file.storage.rdb.internal;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccountDeleteListener;
import com.openexchange.session.Session;

/**
 * {@link DeleteListenerRegistry} - Registry for file storage account delete listeners.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class DeleteListenerRegistry {

    private static volatile DeleteListenerRegistry instance;

    /**
     * Initializes the registry instance.
     */
    public static void initInstance() {
        instance = new DeleteListenerRegistry();
    }

    /**
     * Releases the registry instance.
     */
    public static void releaseInstance() {
        instance = null;
    }

    /**
     * Gets the registry instance.
     *
     * @return The registry instance
     */
    public static DeleteListenerRegistry getInstance() {
        return instance;
    }

    /*
     * Member section
     */

    private final ConcurrentMap<Class<? extends FileStorageAccountDeleteListener>, FileStorageAccountDeleteListener> registry;

    /**
     * Initializes a new {@link DeleteListenerRegistry}.
     */
    public DeleteListenerRegistry() {
        super();
        registry = new ConcurrentHashMap<Class<? extends FileStorageAccountDeleteListener>, FileStorageAccountDeleteListener>();
    }

    /**
     * Adds specified delete listener to this registry.
     *
     * @param deleteListener The delete listener to add
     * @return <code>true</code> if listener could be successfully added; otherwise <code>false</code>
     */
    public boolean addDeleteListener(final FileStorageAccountDeleteListener deleteListener) {
        return (null == registry.putIfAbsent(deleteListener.getClass(), deleteListener));
    }

    /**
     * Removes specified delete listener from this registry.
     *
     * @param deleteListener The delete listener to add
     */
    public void removeDeleteListener(final FileStorageAccountDeleteListener deleteListener) {
        registry.remove(deleteListener.getClass());
    }

    /**
     * Triggers the {@link FileStorageAccountDeleteListener#onBeforeFileStorageAccountDeletion()} event for registered listeners.
     * @param session TODO
     */
    public void triggerOnBeforeDeletion(Session session, final int id, final Map<String, Object> properties, final Connection con) throws OXException {
        for (final FileStorageAccountDeleteListener listener : registry.values()) {
            listener.onBeforeFileStorageAccountDeletion(session, id, properties, con);
        }
    }

    /**
     * Triggers the {@link FileStorageAccountDeleteListener#onAfterFileStorageAccountDeletion()} event for registered listeners.
     * @param session TODO
     */
    public void triggerOnAfterDeletion(Session session, final int id, final Map<String, Object> properties, final Connection con) throws OXException {
        for (final FileStorageAccountDeleteListener listener : registry.values()) {
            listener.onAfterFileStorageAccountDeletion(session, id, properties, con);
        }
    }
}
