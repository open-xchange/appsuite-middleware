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

package com.openexchange.file.storage.boxcom;

import com.openexchange.file.storage.FileStorageConstants;

/**
 * {@link BoxConstants} - Provides useful constants for Box file storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BoxConstants implements FileStorageConstants {

    /**
     * Initializes a new {@link BoxConstants}.
     */
    private BoxConstants() {
        super();
    }

    /**
     * The identifier for Box file storage service.
     */
    public static final String ID = "boxcom";

    /**
     * The display name for the box file storage service.
     */
    public static final String DISPLAY_NAME = "Box File Storage Service";

    // ----------------------------------------------------------------------------------------------------------- //

    /**
     * The type constant for a file.
     */
    public static final String TYPE_FILE = "file";

    /**
     * The type constant for a folder.
     */
    public static final String TYPE_FOLDER = "folder";

}
