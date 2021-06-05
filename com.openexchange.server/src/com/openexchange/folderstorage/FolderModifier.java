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

package com.openexchange.folderstorage;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link FolderModifier} - Modifies a passed folder instance.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public interface FolderModifier<F extends Folder> {

    /**
     * Modifies a given folder.
     *
     * @param folder The folder to modify
     * @param session The session
     * @return The possibly modified folder
     * @throws OXException If modifying the folder fails
     */
    F modify(F folder, Session session) throws OXException;

    /**
     * Gets the content type to which this modifier applies.
     *
     * @return The content type
     */
    ContentType getContentType();

}
