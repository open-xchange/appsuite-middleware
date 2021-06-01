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

package com.openexchange.pop3.storage;

import java.util.Collection;
import java.util.Set;
import com.openexchange.exception.OXException;

/**
 * {@link POP3StorageTrashContainer} - Container for permanently deleted POP3 messages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface POP3StorageTrashContainer {

    /**
     * Adds specified UIDL to this trash container.
     *
     * @param uidl The UIDL of the permanently deleted POP3 message
     * @throws OXException If adding UIDL fails
     */
    public void addUIDL(String uidl) throws OXException;

    /**
     * Adds all specified UIDLs to this trash container.
     *
     * @param uidl The UIDLs of the permanently deleted POP3 messages
     * @throws OXException If adding UIDLs fails
     */
    public void addAllUIDL(Collection<? extends String> uidls) throws OXException;

    /**
     * Removes specified UIDL from this trash container.
     *
     * @param uidl The UIDL to remove
     * @throws OXException If removing UIDL fails
     */
    public void removeUIDL(String uidl) throws OXException;

    /**
     * Gets all UIDLs kept in this container.
     *
     * @return All UIDLs kept in this container
     * @throws OXException If retrieving UIDLs fails
     */
    public Set<String> getUIDLs() throws OXException;

    /**
     * Clears this container.
     *
     * @throws OXException If clearing fails
     */
    public void clear() throws OXException;

}
