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

import java.util.Arrays;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.java.Strings;

/**
 * 
 * {@link MoveFolderPermissionMode}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.5
 */
public enum MoveFolderPermissionMode {

    INHERIT,
    KEEP,
    MERGE;

    private static final Logger LOGGER = LoggerFactory.getLogger(MoveFolderPermissionMode.class);

    public static MoveFolderPermissionMode getByName(String name) {
        if (Strings.isEmpty(name)) {
            return MoveFolderPermissionMode.INHERIT;
        }

        Optional<MoveFolderPermissionMode> optResult = Arrays.asList(MoveFolderPermissionMode.values()).stream().filter(mode -> mode.name().equals(name.toUpperCase())).findAny();
        if (optResult.isPresent() == false) {
            LOGGER.warn("Invalid move folder permission mode: {}. Using fallback \"inherit\".", name);
        }
        return optResult.orElse(MoveFolderPermissionMode.INHERIT);
    }
}