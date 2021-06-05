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

package com.openexchange.filemanagement;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link ManagedFileExceptionMessages} - Localizable strings for managed file exceptions.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ManagedFileExceptionMessages implements LocalizableStrings {

    /**
     * File not found: %1$s
     */
    public static final String FILE_NOT_FOUND_MSG = "File not found: %1$s";

    /**
     * No such managed file associated with ID: %1$s
     */
    public static final String NOT_FOUND_MSG = "No such managed file associated with ID: %1$s";

    /**
     * Initializes a new {@link ManagedFileExceptionMessages}
     */
    private ManagedFileExceptionMessages() {
        super();
    }

}
