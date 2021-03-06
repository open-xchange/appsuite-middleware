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

package com.openexchange.mail.filter.json.v2.json.mapper.parser;

import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link CommandParserRegistry}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 */
public interface CommandParserRegistry<T,K extends CommandParser<T>> {

    /**
     * Registers a {@link CommandParser} under the specified key
     *
     * @param key The key
     * @param parser The {@link CommandParser} to register
     */
    void register(String key, K parser);

    /**
     * Unregisters the {@link CommandParser} with the specified key
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
     * @return The {@link CommandParser}
     * @throws OXException if the parser is not found
     */
    K get(String key) throws OXException;


    /**
     * Gets the registered parsers and their registered names
     *
     * @return A map of key-value pairs
     */
    Map<String, K> getCommandParsers();
}
