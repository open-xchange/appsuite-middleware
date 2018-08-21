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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.mail.compose.security;

import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link CompositionSpaceKeyStorage} - The storage for space-association AES keys.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public interface CompositionSpaceKeyStorage {

    /**
     * Signals the list of needed capabilities or <code>null</code>/empty list if nothing is needed.
     *
     * @return The list of needed capabilities or <code>null</code>
     */
    default List<String> neededCapabilities() {
        return Collections.emptyList();
    }

    /**
     * Gets or creates & stores a random AES key for specified composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param createIfAbsent <code>true</code> to create & store a new random AES key if no such key is available for given composition space
     * @param session The session providing user information
     * @return The key or <code>null</code>; never <code>null</code> if <code>createIfAbsent</code> is <code>true</code>
     * @throws OXException If key cannot be returned
     */
    Key getKeyFor(UUID compositionSpaceId, boolean createIfAbsent, Session session) throws OXException;

    /**
     * Deletes the AES key associated with specified composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param session The session providing user information
     * @return <code>true</code> if deleted; otherwise <code>false</code>
     * @throws OXException If delete operation fails
     */
    default boolean deleteKeyFor(UUID compositionSpaceId, Session session) throws OXException {
        if (null == compositionSpaceId) {
            return false;
        }

        List<UUID> nonDeletedKeys = deleteKeysFor(Collections.singletonList(compositionSpaceId), session);
        return nonDeletedKeys.isEmpty();
    }

    /**
     * Deletes the AES keys associated with specified composition spaces.
     *
     * @param compositionSpaceIds The composition space identifiers
     * @param session The session providing user information
     * @return A listing of those composition space identifiers whose key could not be deleted
     * @throws OXException If delete operation fails
     */
    List<UUID> deleteKeysFor(Collection<UUID> compositionSpaceIds, Session session) throws OXException;

}
