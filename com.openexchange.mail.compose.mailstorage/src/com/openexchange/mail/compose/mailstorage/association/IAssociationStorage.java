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


package com.openexchange.mail.compose.mailstorage.association;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.openexchange.exception.OXException;

/**
 * {@link IAssociationStorage} - The in-memory cache for a certain user carrying opened composition spaces having an associated draft mail.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public interface IAssociationStorage {

    /**
     * Updates an existent association with specified arguments.
     *
     * @param association The updated association
     * @return The updated association
     * @throws OXException If there is no such association
     * @throws IllegalArgumentException If given association update is <code>null</code>
     */
    CompositionSpaceToDraftAssociation update(CompositionSpaceToDraftAssociationUpdate associationUpdate) throws OXException;

    /**
     * Stores given association if not already present.
     *
     * @param association The association
     * @return The already existent association (if any) or <code>null</code> if there was none and given one has therefore been stored
     */
    CompositionSpaceToDraftAssociation storeIfAbsent(CompositionSpaceToDraftAssociation association);

    /**
     * Gets the association for given arguments.
     *
     * @param compositionSpaceId The composition space identifier
     * @return The association
     * @throws OXException If there is no such association available
     */
    CompositionSpaceToDraftAssociation get(UUID compositionSpaceId) throws OXException;

    /**
     * Gets all associations belonging to session-associated user.
     *
     * @return All associations belonging to session-associated user
     * @throws OXException If associations cannot be returned
     */
    List<CompositionSpaceToDraftAssociation> getAll() throws OXException;

    /**
     * (Optionally) Gets the association for given arguments.
     *
     * @param compositionSpaceId The composition space identifier
     * @return The association or empty
     */
    Optional<CompositionSpaceToDraftAssociation> opt(UUID compositionSpaceId);

    /**
     * Deletes the association.
     *
     * @param compositionSpaceId The composition space identifier
     * @param ensureExistent Whether to ensure if such an association is existent prior to deleting it
     * @return The association or empty
     * @throws OXException If deletion fails
     */
    Optional<CompositionSpaceToDraftAssociation> delete(UUID compositionSpaceId, boolean ensureExistent) throws OXException;

}
