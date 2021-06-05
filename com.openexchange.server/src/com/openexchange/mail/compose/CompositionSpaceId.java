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

package com.openexchange.mail.compose;

import java.util.UUID;

/**
 * {@link CompositionSpaceId} - Represents a composite identifier for a composition space.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class CompositionSpaceId extends AbstractId {

    /**
     * Gets the corresponding composition space identifier for given composite identifier.
     *
     * @param compositionSpaceId The composite identifier
     * @return The corresponding composition space identifier
     * @throws IllegalArgumentException If passed composite identifier is <code>null</code> or invalid
     */
    public static CompositionSpaceId valueOf(String compositionSpaceId) {
        return compositionSpaceId == null ? null : new CompositionSpaceId(compositionSpaceId);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link CompositionSpaceId}.
     *
     * @param serviceId The identifier of the composition space service
     * @param spaceId The identifier of the composition space
     */
    public CompositionSpaceId(String serviceId, UUID spaceId) {
        super(serviceId, spaceId);
    }

    /**
     * Initializes a new {@link CompositionSpaceId}.
     *
     * @param compositionSpaceId The composite identifier for the composition space
     * @throws IllegalArgumentException If passed composite identifier is <code>null</code> or invalid
     */
    public CompositionSpaceId(String compositionSpaceId) {
        super(compositionSpaceId);
    }

}
