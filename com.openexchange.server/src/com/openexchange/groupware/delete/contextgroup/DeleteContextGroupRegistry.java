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

package com.openexchange.groupware.delete.contextgroup;

import java.sql.Connection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;

/**
 * {@link DeleteContextGroupRegistry}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DeleteContextGroupRegistry {

    /**
     * The singleton instance.
     */
    private static volatile DeleteContextGroupRegistry INSTANCE;

    /**
     * Get the instance
     *
     * @return the instance
     */
    public static DeleteContextGroupRegistry getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DeleteContextGroupRegistry();
        }
        return INSTANCE;
    }

    /**
     * Perform clean up and release the instance
     */
    static void releaseInstance() {
        INSTANCE.cleanup();
        INSTANCE = null;
    }

    /**
     * The listener registry
     */
    private final ConcurrentMap<Class<? extends DeleteContextGroupListener>, DeleteContextGroupListener> listeners;

    /**
     * Initialises a new {@link DeleteContextGroupRegistry}.
     */
    private DeleteContextGroupRegistry() {
        listeners = new ConcurrentHashMap<>();
    }

    /**
     * Registers an instance of {@link DeleteContextGroupListener}
     *
     * @param listener The listener to register
     * @return true if the specified listener has been added to the registry; false otherwise
     */
    public boolean registerDeleteContextGroupListener(DeleteContextGroupListener listener) {
        return (listeners.putIfAbsent(listener.getClass(), listener) != null);
    }

    /**
     * Removes the specified instance of the {@link DeleteContextGroupListener} from the registry.
     *
     * @param listener The listener to remove
     * @return true if the listener was removed from the registry; false otherwise.
     */
    public boolean unregisterDeleteContextGroupListener(DeleteContextGroupListener listener) {
        return (listeners.remove(listener.getClass()) != null);
    }

    /**
     * Fire the delete context group event
     *
     * @param event The event to fire
     * @param readConnection The read connection to the global db
     * @param writeConnection The write connection to the global db
     * @throws OXException If an error occurs
     */
    public void fireDeleteContextGroupEvent(DeleteContextGroupEvent event, Connection readConnection, Connection writeConnection) throws OXException {
        for (DeleteContextGroupListener listener : listeners.values()) {
            listener.deletePerformed(event, readConnection, writeConnection);
        }
    }

    /**
     * Cleanup
     */
    private void cleanup() {
        listeners.clear();
    }
}
