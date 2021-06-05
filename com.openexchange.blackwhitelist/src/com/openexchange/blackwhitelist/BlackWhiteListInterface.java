
package com.openexchange.blackwhitelist;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

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

/**
 * {@link BlackWhiteListInterface}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public interface BlackWhiteListInterface {

    /**
     * Replaces a whole list with the given entries.
     *
     * @param session The Session.
     * @param type The list type (black/white).
     * @param list The list entries.
     */
    public void setList(ServerSession session, ListType type, List<String> list) throws OXException;

    /**
     * Adds list entries.
     *
     * @param session The Session.
     * @param type The list type (black/white).
     * @param entries The entries to add.
     */
    public void addListEntries(ServerSession session, ListType type, List<String> entries) throws OXException;

    /**
     * Removes list entries.
     *
     * @param session The Session.
     * @param type The list type (black/white).
     * @param entries The entries to remove.
     */
    public void removeListEntries(ServerSession session, ListType type, List<String> entries) throws OXException;

    /**
     * Returns all entries of the given list type.
     *
     * @param session The Session.
     * @param type The list type (black/white).
     * @return A List of entries.
     */
    public List<String> getList(ServerSession session, ListType type) throws OXException;
}
