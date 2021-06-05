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

/**
 * {@link Capability} - Represents a capability.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Capability implements Serializable, Comparable<Capability> {

    private static final long serialVersionUID = 8389975218424678442L;

    private final String id;
    private final int hash;

    /**
     * Initializes a new {@link Capability}.
     *
     * @param id The identifier of the capability
     */
    public Capability(String id) {
        super();
        this.id = id;
        this.hash = 31 * 1 + ((id == null) ? 0 : id.hashCode());
    }

    /**
     * Gets this capability's identifier
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Capability)) {
            return false;
        }
        Capability other = (Capability) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int compareTo(Capability o) {
        return id.compareTo(o.id);
    }

}
