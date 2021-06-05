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

package com.openexchange.jslob.storage;

import java.util.Collection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobId;

/**
 * {@link JSlobStorage} - The JSlob storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface JSlobStorage {

    /**
     * Gets the identifier of this JSlob storage.
     *
     * @return The identifier of this JSlob storage.
     */
    String getIdentifier();

    /**
     * Stores an element with the given identifier.
     *
     * @param id Identifier.
     * @param t Element.
     * @return <code>true</code> if there was no such element before; other <code>false</code> if another one had been replaced
     * @throws OXException If storing fails
     */
    boolean store(JSlobId id, JSlob t) throws OXException;

    /**
     * Reads the element associated with the given identifier.
     *
     * @param id The identifier
     * @return The element.
     * @throws OXException If loading fails or no element is associated with specified identifier
     */
    JSlob load(JSlobId id) throws OXException;

    /**
     * Invalidates denoted element.
     *
     * @param id The identifier
     */
    void invalidate(JSlobId id);

    /**
     * Reads the element associated with the given identifier.
     *
     * @param id The identifier.
     * @return The element or <code>null</code>
     * @throws OXException If loading fails
     */
    JSlob opt(JSlobId id) throws OXException;

    /**
     * Reads the elements associated with the given identifiers.
     *
     * @param ids The identifiers
     * @return The elements
     * @throws OXException If loading fails
     */
    List<JSlob> list(List<JSlobId> ids) throws OXException;

    /**
     * Reads the elements associated with the given identifier.
     *
     * @param id The identifier.
     * @return The elements
     * @throws OXException If loading fails
     */
    Collection<JSlob> list(JSlobId id) throws OXException;

    /**
     * Deletes the element associated with the given identifier.
     *
     * @param id Identifier.
     * @return The deleted Element.
     * @throws OXException If removal fails
     */
    JSlob remove(JSlobId id) throws OXException;

}
