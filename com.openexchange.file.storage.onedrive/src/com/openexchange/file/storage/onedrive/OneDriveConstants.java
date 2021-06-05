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

package com.openexchange.file.storage.onedrive;

import com.openexchange.file.storage.FileStorageConstants;

/**
 * {@link OneDriveConstants} - Provides useful constants for OneDrive file storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OneDriveConstants implements FileStorageConstants {

    /**
     * Initializes a new {@link OneDriveConstants}.
     */
    private OneDriveConstants() {
        super();
    }

    /**
     * The identifier for Microsoft OneDrive file storage service.
     */
    public static final String ID = "onedrive";
    
    /**
     * The display name for Microsoft OneDrive file storage service.
     */
    public static final String DISPLAY_NAME = "Microsoft OneDrive File Storage Service";

    // ----------------------------------------------------------------------------------------------------------- //

    /**
     * The type constant for a file.
     */
    public static final String TYPE_FILE = "file";

    /**
     * The type constant for a folder.
     */
    public static final String TYPE_FOLDER = "folder";

    /**
     * The type constant for an album.
     */
    public static final String TYPE_ALBUM = "album";

    // ----------------------------------------------------------------------------------------------------------- //

    /**
     * The "offset" query parameter<br>
     * See <a href="http://msdn.microsoft.com/en-us/library/dn631842.aspx">http://msdn.microsoft.com/en-us/library/dn631842.aspx</a>
     */
    public static final String QUERY_PARAM_OFFSET = "offset";

    /**
     * The "limit" query parameter<br>
     * See <a href="http://msdn.microsoft.com/en-us/library/dn631842.aspx">http://msdn.microsoft.com/en-us/library/dn631842.aspx</a>
     */
    public static final String QUERY_PARAM_LIMIT = "limit";

    /**
     * The <a href="http://msdn.microsoft.com/en-us/library/dn631835.aspx">"filter" query parameter</a><br>
     * See <a href="http://msdn.microsoft.com/en-us/library/dn631842.aspx">http://msdn.microsoft.com/en-us/library/dn631842.aspx</a>
     */
    public static final String QUERY_PARAM_FILTER = "filter";

    // ----------------------------------------------------------------------------------------------------------- //

    /**
     * The <code>"filter"</code> expression to only retrieve folders/albums. <br>
     * See <a href="http://msdn.microsoft.com/en-us/library/dn631835.aspx">http://msdn.microsoft.com/en-us/library/dn631835.aspx</a>
     */
    public static final String FILTER_FOLDERS = "folders,albums";

}
