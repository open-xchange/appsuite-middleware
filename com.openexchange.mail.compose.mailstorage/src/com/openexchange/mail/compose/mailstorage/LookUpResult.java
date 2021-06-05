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

package com.openexchange.mail.compose.mailstorage;

import com.openexchange.mail.compose.mailstorage.association.CompositionSpaceToDraftAssociation;
import com.openexchange.mail.compose.mailstorage.association.IAssociationStorage;

/**
 * A look-up result for a "composition space to draft" association.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class LookUpResult {

    /**
     * Creates an empty look-up.
     *
     * @param associationStorage The association storage for which to signal empty look-up
     * @return The empty look-up
     */
    public static final LookUpResult emptyResult(IAssociationStorage associationStorage) {
        return new LookUpResult(null, false, associationStorage);
    }

    /**
     * Create the look-up result for given arguments.
     *
     * @param association The "composition space to draft" association
     * @param fromCache Whether the association was fetched from cache or not
     * @param associationStorage The association storage from which the association was looked-up
     * @return The look-up result
     */
    public static final LookUpResult resultFor(CompositionSpaceToDraftAssociation association, boolean fromCache, IAssociationStorage associationStorage) {
        return association == null ? emptyResult(associationStorage) : new LookUpResult(association, fromCache, associationStorage);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final CompositionSpaceToDraftAssociation association;
    private final boolean fromCache;
    private final IAssociationStorage associationStorage;

    /**
     * Initializes a new {@link LookUpResult}.
     *
     * @param association The "composition space to draft" association
     * @param fromCache Whether the association was fetched from cache or not
     * @param associationStorage The association storage from which the association was looked-up
     */
    private LookUpResult(CompositionSpaceToDraftAssociation association, boolean fromCache, IAssociationStorage associationStorage) {
        super();
        this.association = association;
        this.fromCache = fromCache;
        this.associationStorage = associationStorage;
    }

    /**
     * Gets the association of this look-up result.
     *
     * @return The association or <code>null</code>
     */
    public CompositionSpaceToDraftAssociation getAssociation() {
        return association;
    }

    /**
     * Checks whether the association was fetched from cache or not.
     *
     * @return <code>true</code> if the association was fetched from cache; otherwise <code>false</code>
     */
    public boolean isFromCache() {
        return fromCache;
    }

    /**
     * Checks if this look-up result is empty.
     *
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return association == null;
    }

    /**
     * Gets the association storage from which the association was looked-up.
     *
     * @return The association storage
     */
    public IAssociationStorage getAssociationStorage() {
        return associationStorage;
    }

}