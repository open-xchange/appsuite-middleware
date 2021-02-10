/*
 *

 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
