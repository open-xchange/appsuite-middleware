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

package com.openexchange.capabilities;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

/**
 * {@link CapabilitySet} - A capability set.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface CapabilitySet extends Iterable<Capability>, Serializable, Cloneable {

    /**
     * Gets the size
     *
     * @return The size
     */
    int size();

    /**
     * Checks if set is empty
     *
     * @return <code>true</code> if empty; else <code>false</code>
     */
    boolean isEmpty();

    /**
     * Checks for presence of given capability.
     *
     * @param capability The capability to look for
     * @return <code>true</code> if contained; else <code>false</code>
     */
    boolean contains(Capability capability);

    /**
     * Checks for presence of denoted capability.
     *
     * @param id The capability identifier to look for
     * @return <code>true</code> if contained; else <code>false</code>
     */
    boolean contains(String id);

    /**
     * Gets the capability identifies by the supplied ID.
     *
     * @param id The capability identifier to look for
     * @return The capability, or <code>null</code> if not found
     */
    Capability get(String id);

    /**
     * Gets an iterator for capabilities.
     *
     * @return An iterator for capabilities
     */
    @Override
    Iterator<Capability> iterator();

    /**
     * Adds given capability.
     *
     * @param capability The capability to add
     * @return <code>true</code> if set changed; otherwise <code>false</code> if already contained
     */
    boolean add(Capability capability);

    /**
     * Removes the given capability.
     *
     * @param capability The capability
     * @return <code>true</code> if removed; otherwise <code>false</code> if no such capability was contained
     */
    boolean remove(Capability capability);

    /**
     * Removes the denoted capability.
     *
     * @param id The capability identifier
     * @return <code>true</code> if removed; otherwise <code>false</code> if no such capability was contained
     */
    boolean remove(String id);

    /**
     * Clears this set.
     */
    void clear();

    /**
     * Creates the {@link Set set} view for this capability set.
     * <p>
     * Changes to returned set are <b>not</b> reflected in this capability set.
     *
     * @return The {@link Set set} view for this capability set
     */
    Set<Capability> asSet();

}
