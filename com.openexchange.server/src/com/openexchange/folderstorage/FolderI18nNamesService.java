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

import java.util.Set;
import com.openexchange.exception.OXException;

/**
 * {@link FolderI18nNamesService} - Provides the localized folder names for specified folder modules.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface FolderI18nNamesService {

    /**
     * Gets the localized folder names for specified folder modules.
     * <p>
     * If no module is specified, the localized names of all modules are returned.
     *
     * @param modules The optional modules
     * @return The localized folder names
     * @throws OXException If name look-up fails
     */
    Set<String> getI18nNamesFor(int... modules) throws OXException;

    /**
     * Gets all known localized folder names in a module for the supplied (default, untranslated) folder strings.
     *
     * @param module The module of the folders
     * @return The localized folder names, combined with the the default names in a set
     * @throws OXException If name look-up fails
     */
    Set<String> getI18nNamesFor(int module, String...folderStrings) throws OXException;

}
