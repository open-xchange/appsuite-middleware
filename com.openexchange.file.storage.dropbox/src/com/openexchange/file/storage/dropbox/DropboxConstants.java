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

package com.openexchange.file.storage.dropbox;

import com.openexchange.file.storage.FileStorageConstants;

/**
 * {@link DropboxConstants} - Provides useful constants for Dropbox file storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class DropboxConstants implements FileStorageConstants {

    /**
     * The identifier for Dropbox file storage service.
     */
    public static final String ID = "dropbox";
    
    /**
     * The display name for dropbox file storage service.
     */
    public static final String DISPLAY_NAME = "Dropbox File Storage Service";

    /**
     * The configuration property name for Dropbox API key.
     */
    public static final String API_KEY = "apiKey";

    /**
     * The configuration property name for Dropbox secret key.
     */
    public static final String SECRET_KEY = "secretKey";

    /**
     * The configuration property name for Dropbox product name.
     */
    public static final String PRODUCT_NAME = "productName";

    /**
     * The default API key.
     */
    public static final String KEY_API = "no-default-available";

    /**
     * The default secret key.
     */
    public static final String KEY_SECRET = "no-default-available";

    /**
     * Initializes a new {@link DropboxConstants}.
     */
    private DropboxConstants() {
        super();
    }

}
