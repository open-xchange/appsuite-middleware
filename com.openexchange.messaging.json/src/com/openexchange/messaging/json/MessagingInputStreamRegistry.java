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

package com.openexchange.messaging.json;

import java.io.IOException;
import java.io.InputStream;
import com.openexchange.exception.OXException;

/**
 * A {@link MessagingInputStreamRegistry} can look up a certain InputStream for a given name. This is used to resolve references to binaries
 * when parsing MessagingMessages.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MessagingInputStreamRegistry {

    /**
     * Gets the binary content for specified identifier
     *
     * @param id The identifier
     * @return The binary content as an input stream
     * @throws OXException If a messaging error occurs
     * @throws IOException If an I/O error occurs
     */
    public InputStream get(Object id) throws OXException, IOException;

    /**
     * Gets the registry entry for specified identifier
     *
     * @param id The identifier
     * @return The registry entry
     * @throws OXException If a messaging error occurs
     */
    public Object getRegistryEntry(Object id) throws OXException;
}
