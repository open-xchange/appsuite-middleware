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

package com.openexchange.jsieve.registry;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.Command;

/**
 * {@link CommandRegistry}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface CommandRegistry<T> {

    /**
     * Registers a {@link Command} under the specified key
     *
     * @param key The key
     * @param command The {@link Command} to register
     */
    void register(String key, T command);

    /**
     * Unregisters the {@link Command} with the specified key
     *
     * @param key The key
     */
    void unregister(String key);

    /**
     * Purges the registry
     */
    void purge();

    /**
     * Get the parser registered under the specified key
     *
     * @param key The key
     * @return The {@link Command}
     * @throws OXException if the parser is not found
     */
    T get(String key) throws OXException;

    /**
     * Gets all registered Commands
     * @return The Collection of {@link Command}s
     */
    Collection<T> getCommands();
}
