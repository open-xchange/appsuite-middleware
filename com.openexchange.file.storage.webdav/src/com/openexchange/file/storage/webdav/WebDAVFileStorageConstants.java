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

package com.openexchange.file.storage.webdav;

import com.openexchange.file.storage.FileStorageConstants;

/**
 * {@link WebDAVFileStorageConstants} - Provides useful constants for Google Drive file storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class WebDAVFileStorageConstants implements FileStorageConstants {

    /**
     * Initializes a new {@link WebDAVFileStorageConstants}.
     */
    private WebDAVFileStorageConstants() {
        super();
    }

    // ------------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Status code (400) indicating the request sent by the client was syntactically incorrect.
     */
    public static final int SC_BAD_REQUEST = 400;

    /**
     * Status code (401) indicating that the request requires HTTP authentication.
     */
    public static final int SC_UNAUTHORIZED = 401;

    /**
     * Status code (403) indicating the server understood the request but refused to fulfill it.
     */
    public static final int SC_FORBIDDEN = 403;

    /**
     * Status code (404) indicating that the requested resource is not available.
     */
    public static final int SC_NOT_FOUND = 404;

    /**
     * Status code (409) indicating that the request could not be completed due to a conflict with the current state of the resource.
     */
    public static final int SC_CONFLICT = 409;

    // ------------------------------------------------------------------------------------------------------------------------------- //

    /**
     * The identifier for WebDAV file storage service.
     */
    public static final String ID = "webdav";

    /**
     * The root folder identifier
     */
    public static final String ROOT_ID = "root";

    // ------------------------------------------------------------------------------------------------------------------------------- //

    public static final String SLASH = "/";

    // ------------------------------------------------------------------------------------------------------------------------------- //

    /**
     * The configuration property name for URL to WebDAV server.
     */
    public static final String WEBDAV_URL = "url";

    // ------------------------------------------------------------------------------------------------------------------------------- //

    /**
     * The name of the ETAG property
     */
    public static final String PROPERTY_ETAG = "ETag";

    /**
     * The name of the content-language property
     */
    public static final String PROPERTY_CONTENT_LANGUAGE = "Content-Language";



}
