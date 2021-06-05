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

package com.openexchange.groupware.generic;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * {@link FolderUpdaterService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public interface FolderUpdaterService<T> {

    /**
     * Whether this {@link FolderUpdaterService} handles the given folder or not
     *
     * @param folder The {@link FolderObject} to check
     * @return true if it handles it, false otherwise
     */
    public boolean handles(FolderObject folder);

    /**
     * Whether this {@link FolderUpdaterService} uses the multiple strategy or not
     *
     * @return true if it uses the multiple strategy, false otherwise
     */
    public boolean usesMultipleStrategy();

    /**
     * Saves the data in the target folder.
     *
     * @param data A search iterator over the data to store
     * @param target The target folder definition
     * @throws OXException
     */
    void save(SearchIterator<T> data, TargetFolderDefinition target) throws OXException;
}
