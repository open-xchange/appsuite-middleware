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

package com.openexchange.caching;

import java.io.Serializable;

/**
 * {@link InvalidatedMarker} - A simple marker for an invalidated entity.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class InvalidatedMarker<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 7202966861003492335L;

    /**
     * Creates a new invalidated marker.
     *
     * @param identifier The identifier
     * @return A new invalidated marker
     */
    public static <T extends Serializable> InvalidatedMarker<T> newInstance(final T identifier) {
        return new InvalidatedMarker<T>(identifier);
    }

    private final T identifier;

    /**
     * Initializes a new {@link InvalidatedMarker}.
     *
     * @param identifier The identifier
     */
    private InvalidatedMarker(final T identifier) {
        super();
        this.identifier = identifier;
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public T getIdentifier() {
        return identifier;
    }

}
